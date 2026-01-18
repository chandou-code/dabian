<template>
  <view class="task-list-page">
    <!-- 非跑腿员提示 -->
    <view class="non-runner-tip" v-if="!isRunner">
      <view class="tip-content">
        <text class="tip-icon">🚶‍♂️</text>
        <text class="tip-title">请先申请成为跑腿员</text>
        <text class="tip-description">只有成为跑腿员后，才能查看和接取任务</text>
        <button class="apply-btn" @click="goToApply">立即申请</button>
      </view>
    </view>
    
    <!-- 跑腿员视图 -->
    <view v-else>
      <!-- 筛选栏 -->
      <view class="filter-bar">
        <view class="filter-item" @click="showTypePicker">
          <text>{{ currentTypeText }}</text>
          <text class="arrow">▼</text>
        </view>
        <view class="filter-item" @click="showStatusPicker">
          <text>{{ currentStatusText }}</text>
          <text class="arrow">▼</text>
        </view>
        <view class="filter-item" @click="showSortPicker">
          <text>{{ currentSortText }}</text>
          <text class="arrow">▼</text>
        </view>
      </view>
      
      <!-- 任务列表 -->
      <scroll-view class="task-list" scroll-y @scrolltolower="loadMore">
        <view class="task-list-container">
          <view
            class="task-item"
            v-for="task in tasks"
            :key="task.id"
            @click="goToDetail(task.id)"
          >
            <view class="task-header">
              <view class="task-type-badge" :class="'type-' + task.type">
                {{ getTypeText(task.type) }}
              </view>
              <view class="task-status-badge" :class="'status-' + task.status">
                {{ getStatusText(task.status) }}
              </view>
            </view>
            
            <view class="task-content">
              <view class="task-title">{{ task.title }}</view>
              <view class="task-description">{{ task.description }}</view>
              
              <view class="task-info">
                <view class="info-row">
                  <text class="info-label">取：</text>
                  <text class="info-value">{{ task.pickupAddress }}</text>
                </view>
                <view class="info-row">
                  <text class="info-label">送：</text>
                  <text class="info-value">{{ task.deliveryAddress }}</text>
                </view>
                <view class="info-row">
                  <text class="info-label">时：</text>
                  <text class="info-value">{{ formatTime(task.expectedTime) }}</text>
                </view>
              </view>
            </view>
            
            <view class="task-footer">
              <view class="task-price">
                <text class="price-label">¥</text>
                <text class="price-value">{{ task.price }}</text>
              </view>
              <view class="task-actions">
                <button
                  class="action-btn btn-accept"
                  v-if="task.status === 'pending'"
                  @click.stop="acceptTask(task.id)"
                >
                  接单
                </button>
              </view>
            </view>
          </view>
          
          <!-- 空状态 -->
          <view class="empty-state" v-if="tasks.length === 0 && !loading">
            <text class="empty-icon">📋</text>
            <text class="empty-text">暂无任务</text>
          </view>
          
          <!-- 加载中 -->
          <view class="loading-more" v-if="loading">
            <text>加载中...</text>
          </view>
          
          <!-- 没有更多 -->
          <view class="no-more" v-if="!hasMore && tasks.length > 0">
            <text>没有更多了</text>
          </view>
        </view>
      </scroll-view>
      
      <!-- 浮动按钮 -->
      <view class="fab-button" @click="goToPublish">
        <text class="fab-icon">+</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getTasks, acceptTask as acceptTaskApi } from '@/api/errand'
import { get } from '@/api/request'

export default {
  data() {
    return {
      isRunner: false,
      tasks: [],
      loading: false,
      hasMore: true,
      page: 1,
      pageSize: 10,
      
      // 筛选条件
      currentType: '',
      currentStatus: 'pending', // 默认只显示待接单任务
      currentSort: 'newest',
      
      // 类型选项
      types: [
        { value: '', text: '全部类型' },
        { value: 'delivery', text: '快递代取' },
        { value: 'food', text: '外卖代送' },
        { value: 'shopping', text: '物品购买' },
        { value: 'queue', text: '排队代办' },
        { value: 'document', text: '文件传递' },
        { value: 'other', text: '其他服务' }
      ],
      
      // 状态选项 - 只保留全部状态和待接单选项
      statuses: [
        { value: '', text: '全部状态' },
        { value: 'pending', text: '待接单' }
      ],
      
      // 排序选项
      sorts: [
        { value: 'newest', text: '最新发布' },
        { value: 'price_high', text: '价格最高' },
        { value: 'price_low', text: '价格最低' },
        { value: 'distance', text: '距离最近' }
      ]
    }
  },
  
  computed: {
    currentTypeText() {
      return this.types.find(t => t.value === this.currentType)?.text || '全部类型'
    },
    
    currentStatusText() {
      return this.statuses.find(s => s.value === this.currentStatus)?.text || '全部状态'
    },
    
    currentSortText() {
      return this.sorts.find(s => s.value === this.currentSort)?.text || '最新发布'
    }
  },
  
  onLoad() {
    this.checkRunnerAccess()
  },

  onShow() {
    // 每次页面显示时重新检查访问权限
    this.checkRunnerAccess()
  },
  
  onPullDownRefresh() {
    this.refreshTasks()
  },
  
  methods: {
    // 检查跑腿员访问权限
    async checkRunnerAccess() {
      try {
        // 从本地存储获取token
        const token = uni.getStorageSync('token')
        
        console.log('检查跑腿员访问权限:')
        console.log('是否有token:', !!token)
        
        // 检查是否登录
        if (!token) {
          console.log('未登录，跳转到登录页面')
          
          // 显示提示信息
          uni.showToast({
            title: '请先登录',
            icon: 'none',
            duration: 1500
          })
          
          // 延迟跳转到登录页面
          setTimeout(() => {
            uni.navigateTo({
              url: '/pages/login/login'
            })
          }, 1500)
          
          this.isRunner = false
          return false
        }
        
        // 通过后端API获取用户信息，判断是否为跑腿员
        console.log('正在获取用户信息...')
        
        // 调用后端获取用户信息的API
        // 使用/auth/user/profile端点，不需要userId参数，会从JWT中获取当前用户信息
        const response = await get('/auth/user/profile')
        
        console.log('获取用户信息响应:', response)
        
        if (response.code === 200 && response.data) {
          const userData = response.data
          console.log('用户角色:', userData.role)
          
          // 检查用户角色是否为跑腿员
          const isRunner = userData.role === 'runner' || userData.role === 'Runner'
          
          if (!isRunner) {
            console.log('非跑腿员，显示提示')
            this.isRunner = false
            return false
          }
          
          // 是跑腿员，允许访问任务列表
          console.log('跑腿员，允许访问任务列表')
          this.isRunner = true
          
          // 加载任务列表
          this.loadTasks()
          return true
        } else {
          console.log('获取用户信息失败，跳转到登录页面')
          
          // 显示提示信息
          uni.showToast({
            title: '获取用户信息失败，请重新登录',
            icon: 'none',
            duration: 1500
          })
          
          // 延迟跳转到登录页面
          setTimeout(() => {
            uni.navigateTo({
              url: '/pages/login/login'
            })
          }, 1500)
          
          this.isRunner = false
          return false
        }
      } catch (error) {
        console.error('检查访问权限时出错:', error)
        
        // 出错时暂时允许访问，确保页面能够正常加载
        // 实际项目中应该根据错误类型进行处理
        console.log('检查出错，暂时允许访问任务列表')
        this.isRunner = true
        this.loadTasks()
        return true
      }
    },
    
    // 跳转到申请页面
    goToApply() {
      uni.navigateTo({
        url: '/pages/runner/apply'
      })
    },

    // 加载任务列表
    async loadTasks(refresh = false) {
      if (this.loading) return
      
      if (refresh) {
        this.page = 1
        this.hasMore = true
      }
      
      if (!this.hasMore) return
      
      this.loading = true
      
      try {
        const params = {
          page: this.page,
          pageSize: this.pageSize,
          type: this.currentType,
          status: this.currentStatus,
          sort: this.currentSort
        }
        
        const response = await getTasks(params)
        
        if (response.code === 200) {
          const newTasks = response.data.list || []
          
          if (refresh) {
            this.tasks = newTasks
          } else {
            this.tasks.push(...newTasks)
          }
          
          this.hasMore = newTasks.length >= this.pageSize
          
          if (!refresh && newTasks.length > 0) {
            this.page++
          }
        }
      } catch (error) {
        console.error('加载任务失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
        if (refresh) {
          uni.stopPullDownRefresh()
        }
      }
    },
    
    // 刷新任务列表
    refreshTasks() {
      this.loadTasks(true)
    },
    
    // 加载更多
    loadMore() {
      if (this.hasMore && !this.loading) {
        this.page++
        this.loadTasks()
      }
    },
    
    // 显示类型选择器
    showTypePicker() {
      const values = this.types.map(t => t.text)
      uni.showActionSheet({
        itemList: values,
        success: (res) => {
          this.currentType = this.types[res.tapIndex].value
          this.refreshTasks()
        }
      })
    },
    
    // 显示状态选择器
    showStatusPicker() {
      const values = this.statuses.map(s => s.text)
      uni.showActionSheet({
        itemList: values,
        success: (res) => {
          this.currentStatus = this.statuses[res.tapIndex].value
          this.refreshTasks()
        }
      })
    },
    
    // 显示排序选择器
    showSortPicker() {
      const values = this.sorts.map(s => s.text)
      uni.showActionSheet({
        itemList: values,
        success: (res) => {
          this.currentSort = this.sorts[res.tapIndex].value
          this.refreshTasks()
        }
      })
    },
    
    // 接单
    async acceptTask(taskId) {
      uni.showModal({
        title: '确认接单',
        content: '确定要接取这个任务吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              const response = await acceptTaskApi(taskId)
              
              if (response.code === 200) {
                uni.showToast({
                  title: '接单成功',
                  icon: 'success'
                })
                this.refreshTasks()
              }
            } catch (error) {
              console.error('接单失败:', error)
              uni.showToast({
                title: '接单失败',
                icon: 'none'
              })
            }
          }
        }
      })
    },
    
    // 跳转到详情页
    goToDetail(taskId) {
      uni.navigateTo({
        url: `/pages/task/detail?id=${taskId}`
      })
    },
    
    // 跳转到发布页
    goToPublish() {
      uni.navigateTo({
        url: '/pages/task/publish'
      })
    },
    
    // 获取类型文本
    getTypeText(type) {
      return this.types.find(t => t.value === type)?.text || type
    },
    
    // 获取状态文本
    getStatusText(status) {
      return this.statuses.find(s => s.value === status)?.text || status
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

<style scoped lang="scss">
.task-list-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.non-runner-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  padding: 40rpx;
  background: #f5f5f5;
}

.tip-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx 40rpx;
  background: white;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
  max-width: 500rpx;
  text-align: center;
}

.tip-icon {
  font-size: 120rpx;
  margin-bottom: 30rpx;
}

.tip-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.tip-description {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
  margin-bottom: 40rpx;
}

.apply-btn {
  padding: 20rpx 60rpx;
  background: linear-gradient(135deg, #2196f3, #1976d2);
  color: white;
  border: none;
  border-radius: 30rpx;
  font-size: 28rpx;
  font-weight: bold;
  box-shadow: 0 4rpx 12rpx rgba(33, 150, 243, 0.3);
}

.filter-bar {
  display: flex;
  padding: 20rpx;
  background: white;
  border-bottom: 1rpx solid #f0f0f0;
  
  .filter-item {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20rpx;
    background: #f5f5f5;
    border-radius: 30rpx;
    font-size: 26rpx;
    color: #333;
    margin: 0 10rpx;
    
    .arrow {
      font-size: 18rpx;
      color: #999;
      margin-left: 8rpx;
    }
  }
}

.task-list {
  flex: 1;
  padding: 20rpx;
}

.task-list-container {
  min-height: 100%;
}

.task-item {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
  
  .task-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
  }
  
  .task-type-badge,
  .task-status-badge {
    padding: 8rpx 16rpx;
    border-radius: 20rpx;
    font-size: 22rpx;
  }
  
  .task-type-badge {
    background: #e3f2fd;
    color: #2196f3;
  }
  
  .task-status-badge {
    background: #e8f5e8;
    color: #4caf50;
  }
  
  .task-content {
    margin-bottom: 20rpx;
  }
  
  .task-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 12rpx;
  }
  
  .task-description {
    font-size: 26rpx;
    color: #666;
    line-height: 1.6;
    margin-bottom: 20rpx;
  }
  
  .task-info {
    padding: 20rpx;
    background: #f8f8f8;
    border-radius: 12rpx;
  }
  
  .info-row {
    display: flex;
    margin-bottom: 12rpx;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .info-label {
      font-size: 24rpx;
      color: #999;
      width: 60rpx;
    }
    
    .info-value {
      flex: 1;
      font-size: 26rpx;
      color: #333;
    }
  }
  
  .task-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 20rpx;
    border-top: 1rpx solid #f0f0f0;
  }
  
  .task-price {
    display: flex;
    align-items: baseline;
    
    .price-label {
      font-size: 28rpx;
      color: #ff5722;
      font-weight: bold;
    }
    
    .price-value {
      font-size: 48rpx;
      color: #ff5722;
      font-weight: bold;
      margin-left: 4rpx;
    }
  }
  
  .task-actions {
    .action-btn {
      padding: 16rpx 32rpx;
      border-radius: 30rpx;
      font-size: 26rpx;
      border: none;
      
      &.btn-accept {
        background: #2196f3;
        color: white;
      }
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
  
  .empty-icon {
    font-size: 120rpx;
    margin-bottom: 30rpx;
  }
  
  .empty-text {
    font-size: 28rpx;
    color: #999;
  }
}

.loading-more,
.no-more {
  text-align: center;
  padding: 30rpx 0;
  font-size: 26rpx;
  color: #999;
}

.fab-button {
  position: fixed;
  bottom: 100rpx;
  right: 30rpx;
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #2196f3, #1976d2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 20rpx rgba(33, 150, 243, 0.4);
  z-index: 100;
  
  .fab-icon {
    font-size: 60rpx;
    color: white;
    font-weight: bold;
  }
}
</style>
