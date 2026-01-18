<template>
  <view class="order-list-container">
    <!-- 顶部标签栏 -->
    <view class="tabs">
      <view
        v-for="(tab, index) in tabs"
        :key="index"
        class="tab-item"
        :class="{ active: currentTab === index }"
        @click="switchTab(index)"
      >
        <text class="tab-text">{{ tab.name }}</text>
        <view class="tab-badge" v-if="tab.count > 0">{{ tab.count }}</view>
      </view>
    </view>

    <!-- 订单列表 -->
    <scroll-view
      scroll-y
      class="order-list"
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="empty-state" v-if="orders.length === 0">
        <image class="empty-image" src="/static/empty-order.png" mode="aspectFit"></image>
        <text class="empty-text">暂无订单</text>
      </view>

      <view
        v-for="order in orders"
        :key="order.id"
        class="order-card"
      >
        <view @click="viewOrder(order)">
          <view class="card-header">
            <view class="order-type">{{ order.typeName }}</view>
            <view class="order-status" :class="'status-' + order.status">
              {{ order.statusText }}
            </view>
          </view>

          <view class="card-content">
            <text class="order-title">{{ order.title }}</text>
            <view class="order-info">
              <text class="info-item">
                <text class="icon">📍</text>
                {{ order.pickupAddress }}
              </text>
              <text class="info-item">
                <text class="icon">→</text>
                {{ order.deliveryAddress }}
              </text>
            </view>
            <view class="order-meta">
              <text class="meta-item">{{ order.publishTime }}</text>
              <text class="meta-item price">¥{{ order.price }}</text>
            </view>
          </view>
        </view>

        <view class="card-footer">
          <!-- 评价按钮 -->
          <button class="btn-evaluate" @click.stop="evaluate(order.id)" v-if="order.status === 'completed' && !order.evaluated">去评价</button>
          <!-- 取消订单按钮 -->
          <button class="btn-cancel" @click.stop="cancelOrderAction(order.id)" v-if="['pending', 'accepted'].includes(order.status)">取消订单</button>
        </view>
      </view>

      <view class="loading-tip" v-if="loading">
        <text class="tip-text">加载中...</text>
      </view>
      <view class="loading-tip" v-if="!hasMore && orders.length > 0">
        <text class="tip-text">没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { getOrders, getOrderStats, cancelTask } from '@/api/errand'

export default {
  data() {
      return {
        currentTab: 0,
        tabs: [
          { name: '全部已接', count: 0, status: '' },
          { name: '已接单', count: 0, status: 'accepted' },
          { name: '进行中', count: 0, status: 'delivering' },
          { name: '已完成', count: 0, status: 'completed' },
          { name: '已取消', count: 0, status: 'cancelled' }
        ],
        orders: [],
        page: 1,
        pageSize: 10,
        loading: false,
        refreshing: false,
        hasMore: true
      }
    },
  onLoad() {
    this.loadOrders()
  },
  onShow() {
    try {
      // 每次显示时刷新数据
      this.loadOrders(true)
      this.loadOrderStats()
    } catch (error) {
      console.error('onShow方法执行失败:', error)
      uni.showToast({
        title: '页面加载失败',
        icon: 'none'
      })
    }
  },
  
  methods: {
    // 获取订单统计数据
    async loadOrderStats() {
      try {
        console.log('开始获取订单统计数据...')
        const response = await getOrderStats()
        console.log('获取订单统计数据响应:', response)
        
        if (response.code === 200 && response.data) {
          const stats = response.data
          
          // 更新标签栏的数字
          this.tabs.forEach(tab => {
            if (tab.status === '') {
              // 全部订单数量
              tab.count = stats.total || 0
            } else {
              // 各状态订单数量
              tab.count = stats[tab.status] || 0
            }
          })
        } else {
          console.error('获取订单统计数据失败:', response.msg || '未知错误')
        }
      } catch (error) {
        console.error('获取订单统计数据异常:', error)
      }
    },
    
    switchTab(index) {
      this.currentTab = index
      this.page = 1
      this.hasMore = true
      this.orders = []
      this.loadOrders()
    },

    async loadOrders(refresh = false) {
      try {
        if (refresh) {
          this.refreshing = true
          this.page = 1
          this.hasMore = true
        }

        if (this.loading) {
          return
        }

        this.loading = true

        // 获取当前标签对应的状态
        const status = this.tabs[this.currentTab].status
        
        // 获取当前登录用户信息
        const user = uni.getStorageSync('user')
        
        // 检查用户是否已登录
        if (!user || !user.id) {
          console.error('用户未登录，无法获取订单列表')
          this.orders = []
          this.hasMore = false
          uni.showToast({
            title: '请先登录',
            icon: 'none'
          })
          this.loading = false
          this.refreshing = false
          return
        }
        
        // 调用API获取订单列表，根据用户角色获取相应订单
        console.log('正在获取订单列表...')
        console.log('参数:', {
          status: status,
          role: 'publisher', // 用户订单列表获取的是自己发布的订单
          page: this.page,
          pageSize: this.pageSize
        })
        
        const response = await getOrders({
          status: status,
          role: 'publisher', // 用户订单列表获取的是自己发布的订单
          page: this.page,
          pageSize: this.pageSize
        })
        
        console.log('获取订单列表响应:', response)
        
        if (response.code === 200 && response.data) {
          const { list, total } = response.data
          
          // 转换后端数据为前端需要的格式
          const formattedOrders = list.map(task => {
            return {
              id: task.id,
              type: task.type,
              typeName: this.getTypeText(task.type),
              title: task.title,
              status: task.status,
              statusText: this.getStatusText(task.status),
              pickupAddress: task.pickupAddress,
              deliveryAddress: task.deliveryAddress,
              publishTime: this.formatTime(task.publishTime),
              price: task.price,
              publisherId: task.publisherId,
              runnerId: task.runnerId,
              evaluated: task.evaluated || false // 使用后端返回的评价状态
            }
          })
          
          if (this.page === 1) {
            this.orders = formattedOrders
          } else {
            this.orders = [...this.orders, ...formattedOrders]
          }
          
          this.hasMore = this.orders.length < total
          console.log('处理后的订单数据:', this.orders)
          console.log('是否有更多:', this.hasMore)
        } else if (response.code === 401) {
          console.error('用户未认证，跳转到登录页面')
          uni.navigateTo({
            url: '/pages/login/login'
          })
        } else {
          console.error('获取订单列表失败:', response.msg)
          uni.showToast({
            title: '获取订单列表失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('获取订单列表异常:', error)
        uni.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
        this.refreshing = false
      }
    },

    loadMore() {
      if (!this.hasMore || this.loading) {
        return
      }
      this.page++
      this.loadOrders()
    },

    onRefresh() {
      this.page = 1
      this.hasMore = true
      this.loadOrders(true)
    },

    viewOrder(order) {
      // 从本地存储中获取当前用户ID
      const currentUserId = uni.getStorageSync('userId')
      
      // 检查当前用户是否是订单的发布者或跑腿员
      if (currentUserId === order.publisherId) {
        // 如果是发布者，跳转到订单详情页
        uni.navigateTo({
          url: `/pages/order/detail?id=${order.id}`
        })
      } else if (currentUserId === order.runnerId) {
        // 如果是跑腿员，跳转到任务详情页
        uni.navigateTo({
          url: `/pages/task/detail?id=${order.id}`
        })
      } else {
        // 默认跳转到订单详情页
        uni.navigateTo({
          url: `/pages/order/detail?id=${order.id}`
        })
      }
    },

    evaluate(orderId) {
      uni.navigateTo({
        url: `/pages/evaluate/index?orderId=${orderId}`
      })
    },
    
    // 取消订单
    async cancelOrderAction(orderId) {
      uni.showModal({
        title: '取消订单',
        content: '确认取消此订单吗？',
        confirmColor: '#f44336',
        success: async (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '处理中...' })
            try {
              const response = await cancelTask(orderId)
              if (response.code === 200) {
                uni.showToast({
                  title: '订单已取消',
                  icon: 'success'
                })
                // 刷新订单列表
                this.loadOrders(true)
              } else {
                uni.showToast({
                  title: response.msg || '操作失败',
                  icon: 'none'
                })
              }
            } catch (error) {
              console.error('取消订单失败:', error)
              uni.showToast({
                title: '网络错误，请稍后重试',
                icon: 'none'
              })
            } finally {
              uni.hideLoading()
            }
          }
        }
      })
    },

    // 获取任务类型文本
    getTypeText(type) {
      const typeMap = {
        'delivery': '快递代取',
        'food': '外卖代送',
        'shopping': '物品购买',
        'queue': '排队代办',
        'document': '文件传递',
        'other': '其他服务'
      }
      return typeMap[type] || type
    },

    // 获取任务状态文本
    getStatusText(status) {
      const statusMap = {
        'pending': '待接单',
        'accepted': '已接单',
        'delivering': '配送中',
        'completed': '已完成',
        'cancelled': '已取消'
      }
      return statusMap[status] || status
    },

    // 格式化时间
    formatTime(time) {
      if (!time) return ''
      
      const date = new Date(time)
      const month = (date.getMonth() + 1).toString().padStart(2, '0')
      const day = date.getDate().toString().padStart(2, '0')
      const hours = date.getHours().toString().padStart(2, '0')
      const minutes = date.getMinutes().toString().padStart(2, '0')
      
      return `${month}-${day} ${hours}:${minutes}`
    }
  }
}
</script>

<style lang="scss" scoped>
.order-list-container {
  height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.tabs {
  display: flex;
  background: white;
  padding: 0 20rpx;
  border-bottom: 1rpx solid #eee;

  .tab-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 24rpx 0;
    position: relative;

    &.active {
      .tab-text {
        color: #2196f3;
        font-weight: bold;
      }

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 60rpx;
        height: 4rpx;
        background: #2196f3;
        border-radius: 2rpx;
      }
    }

    .tab-text {
      font-size: 28rpx;
      color: #333;
      margin-bottom: 4rpx;
    }

    .tab-badge {
      min-width: 32rpx;
      height: 32rpx;
      padding: 0 8rpx;
      background: #f44336;
      color: white;
      border-radius: 16rpx;
      font-size: 20rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}

.order-list {
  flex: 1;
  padding: 20rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;

  .empty-image {
    width: 400rpx;
    height: 400rpx;
    margin-bottom: 40rpx;
  }

  .empty-text {
    font-size: 28rpx;
    color: #999;
  }
}

.order-card {
  background: white;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
  }

  .order-type {
    padding: 6rpx 16rpx;
    background: #e3f2fd;
    color: #2196f3;
    border-radius: 16rpx;
    font-size: 24rpx;
  }

  .order-status {
    padding: 6rpx 16rpx;
    border-radius: 16rpx;
    font-size: 24rpx;

    &.status-pending {
      background: #fff8e1;
      color: #ff9800;
    }

    &.status-accepted {
      background: #e3f2fd;
      color: #2196f3;
    }

    &.status-delivering {
      background: #f3e5f5;
      color: #9c27b0;
    }

    &.status-completed {
      background: #e8f5e8;
      color: #4caf50;
    }

    &.status-cancelled {
      background: #ffebee;
      color: #f44336;
    }
  }

  .card-content {
    .order-title {
      display: block;
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 16rpx;
    }

    .order-info {
      margin-bottom: 16rpx;

      .info-item {
        display: flex;
        align-items: center;
        gap: 12rpx;
        font-size: 26rpx;
        color: #666;
        line-height: 1.6;

        .icon {
          font-size: 28rpx;
        }
      }
    }

    .order-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .meta-item {
        font-size: 24rpx;
        color: #999;

        &.price {
          font-size: 36rpx;
          color: #f44336;
          font-weight: bold;
        }
      }
    }
  }

  .card-footer {
    margin-top: 20rpx;
    padding-top: 20rpx;
    border-top: 1rpx solid #f5f5f5;
    text-align: right;
    display: flex;
    justify-content: flex-end;
    gap: 16rpx;

    .btn-evaluate {
      padding: 12rpx 32rpx;
      background: linear-gradient(135deg, #2196f3, #1976d2);
      color: white;
      border-radius: 24rpx;
      font-size: 26rpx;
      border: none;
    }
    
    .btn-cancel {
      padding: 12rpx 32rpx;
      background: linear-gradient(135deg, #ffebee, #f44336);
      color: white;
      border-radius: 24rpx;
      font-size: 26rpx;
      border: none;
    }
  }
}

.loading-tip {
  text-align: center;
  padding: 30rpx 0;

  .tip-text {
    font-size: 24rpx;
    color: #999;
  }
}
</style>
