<template>
  <view class="order-manage-container">
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
        <text class="empty-text">{{ currentTab === 0 ? '暂无接到的订单' : '暂无已完成的订单' }}</text>
      </view>

      <view
        v-for="order in orders"
        :key="order.id"
        class="order-card"
      >
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

        <view class="card-footer">
          <!-- 状态变更按钮 -->
          <view class="status-actions" v-if="order.status !== 'completed'">
            <button 
              v-if="order.status === 'accepted'" 
              class="btn-action btn-primary" 
              @click="updateStatus(order, 'delivering')"
            >
              开始配送
            </button>
            <button 
              v-if="order.status === 'delivering'" 
              class="btn-action btn-success" 
              @click="updateStatus(order, 'completed')"
            >
              完成配送
            </button>
          </view>
          
          <!-- 操作按钮 -->
          <view class="action-buttons">
            <button class="btn-action btn-chat" @click="chatWithUser(order)">
              <text class="icon">💬</text> 聊天
            </button>
            <button class="btn-action btn-detail" @click="viewDetail(order)">
              <text class="icon">📋</text> 详情
            </button>
          </view>
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
import { getOrders, updateOrderStatus } from '@/api/errand'

export default {
  data() {
    return {
      currentTab: 0,
      tabs: [
        { name: '我的订单', count: 0, status: ['accepted', 'delivering'] },
        { name: '已完成', count: 0, status: ['completed'] }
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
    // 每次显示时刷新数据
    this.loadOrders(true)
    this.loadOrderStats()
  },
  
  methods: {
    // 获取订单统计数据
    async loadOrderStats() {
      try {
        // 这里可以调用订单统计API，如果有的话
        // const response = await getOrderStats()
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
      if (refresh) {
        this.refreshing = true
        this.page = 1
        this.hasMore = true
      }

      if (this.loading) {
        return
      }

      this.loading = true

      try {
        // 获取当前标签对应的状态
        const status = this.tabs[this.currentTab].status
        
        // 调用API获取订单列表，只获取当前用户作为跑腿员的订单
        const response = await getOrders({
          status: Array.isArray(status) ? status.join(',') : status,
          page: this.page,
          pageSize: this.pageSize,
          role: 'runner' // 指定获取跑腿员接的订单
        })
        
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
              evaluated: task.evaluated || false
            }
          })
          
          if (this.page === 1) {
            this.orders = formattedOrders
          } else {
            this.orders = [...this.orders, ...formattedOrders]
          }
          
          this.hasMore = this.orders.length < total
        } else {
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

    // 更新订单状态
    async updateStatus(order, newStatus) {
      try {
        const response = await updateOrderStatus(order.id, newStatus)
        
        if (response.code === 200) {
          // 更新本地订单状态
          order.status = newStatus
          order.statusText = this.getStatusText(newStatus)
          
          uni.showToast({
            title: '状态更新成功',
            icon: 'success'
          })
        } else {
          uni.showToast({
            title: '状态更新失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('更新订单状态异常:', error)
        uni.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none'
        })
      }
    },

    // 与发布者聊天
    chatWithUser(order) {
      uni.navigateTo({
        url: `/pages/chat/detail?userId=${order.publisherId}&name=任务发布者`
      })
    },

    // 查看订单详情
    viewDetail(order) {
      uni.navigateTo({
        url: `/pages/task/detail?id=${order.id}`
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
.order-manage-container {
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

    .status-actions {
      display: flex;
      gap: 16rpx;
      margin-bottom: 16rpx;
    }

    .action-buttons {
      display: flex;
      gap: 16rpx;
    }

    .btn-action {
      flex: 1;
      padding: 12rpx 0;
      border-radius: 8rpx;
      font-size: 26rpx;
      border: none;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8rpx;

      .icon {
        font-size: 28rpx;
      }

      &.btn-primary {
        background: linear-gradient(135deg, #2196f3, #1976d2);
        color: white;
      }

      &.btn-success {
        background: linear-gradient(135deg, #4caf50, #388e3c);
        color: white;
      }

      &.btn-chat {
        background: linear-gradient(135deg, #ff9800, #f57c00);
        color: white;
      }

      &.btn-detail {
        background: linear-gradient(135deg, #9c27b0, #7b1fa2);
        color: white;
      }
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
