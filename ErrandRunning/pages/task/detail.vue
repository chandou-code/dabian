<template>
  <view class="task-detail-container">
    <!-- 顶部状态栏 -->
    <view class="status-bar" :class="'status-' + task.status">
      <text class="status-icon">{{ statusIcon }}</text>
      <text class="status-text">{{ statusText }}</text>
    </view>

    <scroll-view scroll-y class="content">
      <!-- 任务信息 -->
      <view class="card">
        <view class="card-header">
          <text class="task-type">{{ task.typeName }}</text>
          <text class="task-price">¥{{ task.price }}</text>
        </view>
        <text class="task-title">{{ task.title }}</text>
        <text class="task-desc">{{ task.description }}</text>

        <view class="task-info">
          <view class="info-row">
            <text class="label">发布时间</text>
            <text class="value">{{ task.publishTime }}</text>
          </view>
          <view class="info-row">
            <text class="label">期望完成时间</text>
            <text class="value">{{ task.expectTime }}</text>
          </view>
          <view class="info-row">
            <text class="label">任务编号</text>
            <text class="value">{{ task.taskNo }}</text>
          </view>
          <view class="info-row">
            <text class="label">取件详情</text>
            <text class="value">{{ task.pickupDetail }}</text>
          </view>
          <view class="info-row">
            <text class="label">送达详情</text>
            <text class="value">{{ task.deliveryDetail }}</text>
          </view>
        </view>
      </view>

      <!-- 地址信息 -->
      <view class="card">
        <view class="card-title">地址信息</view>
        <view class="address-item">
          <text class="address-label">取件地址</text>
          <text class="address-text">{{ task.pickupAddress }}</text>
        </view>
        <view class="address-item">
          <text class="address-label">送达地址</text>
          <text class="address-text">{{ task.deliveryAddress }}</text>
        </view>
        <view class="map-preview" @click="viewMap">
          <image class="map-image" :src="task.mapImage" mode="aspectFill"></image>
          <text class="map-label">查看路线</text>
        </view>
      </view>

      <!-- 发布者信息 -->
      <view class="card">
        <view class="card-title">发布者信息</view>
        <view class="user-info">
          <image class="avatar" :src="task.publisher.avatar" mode="aspectFill"></image>
          <view class="user-detail">
            <text class="username">{{ task.publisher.nickname }}</text>
            <!-- 跑腿员不需要看到信用分和发布数量 -->
            <view class="user-stats" v-if="userRole === 'admin'">
              <text class="stat-item">信用分: {{ task.publisher.creditScore }}</text>
              <text class="stat-item">发布: {{ task.publisher.publishCount }}</text>
            </view>
          </view>
          <view class="user-actions">
            <button class="action-btn chat" @click="chatPublisher">私信</button>
          </view>
        </view>
      </view>

      <!-- 任务图片 -->
      <view class="card" v-if="task.images && task.images.length">
        <view class="card-title">任务图片</view>
        <view class="images-grid">
          <image
            v-for="(img, index) in task.images"
            :key="index"
            class="task-image"
            :src="img"
            mode="aspectFill"
            @click="previewImage(index)"
          ></image>
        </view>
      </view>

      <!-- 订单进度 -->
      <view class="card" v-if="task.status !== 'pending'">
        <view class="card-title">订单进度</view>
        <view class="timeline">
          <view
            v-for="(item, index) in timeline"
            :key="index"
            class="timeline-item"
            :class="{ active: index <= currentStep }"
          >
            <view class="timeline-dot"></view>
            <view class="timeline-content">
              <text class="timeline-title">{{ item.title }}</text>
              <text class="timeline-time">{{ item.time }}</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="footer" v-if="showActions">
      <button class="btn btn-primary" @click="acceptTask" v-if="canAccept">
        接单
      </button>
      <button class="btn btn-warning" @click="startTask" v-if="canStart">
        开始配送
      </button>
      <button class="btn btn-success" @click="completeTask" v-if="canComplete">
        完成订单
      </button>
      <button class="btn btn-danger" @click="cancelTask" v-if="canCancel">
        取消订单
      </button>
      <button class="btn btn-info" @click="goToEvaluate" v-if="canEvaluate">
        评价订单
      </button>
    </view>
  </view>
</template>

<script>
import { getTaskDetail, acceptTask, cancelTask, completeTask } from '@/api/errand'

export default {
  data() {
    return {
      taskId: '',
      task: {
        id: '',
        taskNo: '',
        type: '',
        typeName: '',
        title: '',
        description: '',
        price: 0,
        status: 'pending',
        publishTime: '',
        expectTime: '',
        pickupAddress: '',
        pickupDetail: '',
        deliveryAddress: '',
        deliveryDetail: '',
        mapImage: '',
        images: [],
        publisher: {},
        runner: null
      },
      hasEvaluated: false, // 是否已评价
      isPublisher: false, // 当前用户是否是发布者
      timeline: [
        { title: '订单已发布', time: '' },
        { title: '跑腿员已接单', time: '' },
        { title: '开始配送', time: '' },
        { title: '订单已完成', time: '' }
      ],
      currentStep: 0
    }
  },
  computed: {
    userRole() {
      const user = uni.getStorageSync('user')
      return user ? user.role : null
    },
    statusIcon() {
      const icons = {
        pending: '📋',
        accepted: '🚚',
        delivering: '📍',
        completed: '✅',
        cancelled: '❌'
      }
      return icons[this.task.status] || '📋'
    },
    statusText() {
      const texts = {
        pending: '等待接单',
        accepted: '已接单',
        delivering: '配送中',
        completed: '已完成',
        cancelled: '已取消'
      }
      return texts[this.task.status] || '未知'
    },
    canAccept() {
      return this.task.status === 'pending'
    },
    canStart() {
      return this.task.status === 'accepted'
    },
    canComplete() {
      return this.task.status === 'delivering'
    },
    canCancel() {
      return ['pending', 'accepted'].includes(this.task.status)
    },
    canEvaluate() {
      // 只有订单已完成、当前用户是发布者、且尚未评价时才能显示评价按钮
      return this.task.status === 'completed' && this.isPublisher && !this.hasEvaluated
    },
    showActions() {
      return this.canAccept || this.canStart || this.canComplete || this.canCancel || this.canEvaluate
    }
  },
  onLoad(options) {
    this.taskId = options.id
    
    // 权限检查：只有跑腿员和管理员才能访问任务详情页
    const user = uni.getStorageSync('user')
    if (!user || (user.role !== 'runner' && user.role !== 'admin')) {
      uni.showToast({
        title: '您没有权限访问此页面',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
      return
    }
    
    this.loadTaskDetail()
  },
  methods: {
    loadTaskDetail() {
      uni.showLoading({ title: '加载中...' })
      getTaskDetail(this.taskId)
        .then(response => {
          uni.hideLoading()
          if (response.code === 200 && response.data) {
            const taskData = response.data
            this.task = {
              id: taskData.id,
              taskNo: taskData.taskNo,
              type: taskData.type,
              typeName: this.getTypeDisplayName(taskData.type),
              title: taskData.title,
              description: taskData.description,
              price: taskData.price,
              status: taskData.status,
              publishTime: this.formatDate(taskData.publishTime),
              expectTime: this.formatDate(taskData.expectedTime),
              pickupAddress: taskData.pickupAddress || '',
              pickupDetail: taskData.pickupDetail || '',
              deliveryAddress: taskData.deliveryAddress || '',
              deliveryDetail: taskData.deliveryDetail || '',
              mapImage: '/static/map-placeholder.png',
              images: this.parseImages(taskData.images),
              publisher: {
                id: taskData.publisherId || '1',
                nickname: '发布者',
                avatar: '/static/avatar1.png',
                creditScore: 98,
                publishCount: 15
              },
              runner: taskData.runnerId ? {
                id: taskData.runnerId,
                nickname: '跑腿员',
                avatar: '/static/avatar2.png',
                creditScore: 95,
                completeCount: 20
              } : null
            }
            
            // 检查当前用户是否是发布者
            this.checkUserIdentity()
            
            // 检查评价状态
            this.checkEvaluationStatus()
            
            // 更新时间线
            this.updateTimeline()
          } else {
            uni.showToast({
              title: '获取任务详情失败',
              icon: 'none'
            })
          }
        })
        .catch(error => {
          uni.hideLoading()
          console.error('获取任务详情失败:', error)
          uni.showToast({
            title: '网络错误，请稍后重试',
            icon: 'none'
          })
        })
    },
    
    // 检查用户身份
    checkUserIdentity() {
      const user = uni.getStorageSync('user')
      if (user && user.id === this.task.publisher.id) {
        this.isPublisher = true
      } else {
        this.isPublisher = false
      }
    },
    
    // 检查评价状态
    checkEvaluationStatus() {
      // 从本地存储检查是否已评价
      const evaluatedTasks = uni.getStorageSync('evaluatedTasks') || []
      this.hasEvaluated = evaluatedTasks.includes(this.task.id.toString())
    },
    
    // 跳转到评价页面
    goToEvaluate() {
      uni.navigateTo({
        url: '/pages/evaluate/index?taskId=' + this.task.id
      })
    },
    
    getTypeDisplayName(type) {
      const typeMap = {
        delivery: '快递代取',
        errand: '校园跑腿',
        purchase: '帮忙购买',
        other: '其他任务'
      }
      return typeMap[type] || type
    },
    
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    
    parseImages(imagesString) {
      if (!imagesString) return []
      try {
        const images = JSON.parse(imagesString)
        return Array.isArray(images) ? images : []
      } catch (error) {
        return []
      }
    },
    
    updateTimeline() {
      this.timeline[0].time = this.task.publishTime
      // 可以根据任务状态更新其他时间线项目
    },

    viewMap() {
      uni.navigateTo({
        url: '/pages/map/index'
      })
    },

    chatPublisher() {
      uni.navigateTo({
        url: `/pages/chat/detail?userId=${this.task.publisher.id}`
      })
    },

    chatRunner() {
      if (this.task.runner) {
        uni.navigateTo({
          url: `/pages/chat/detail?userId=${this.task.runner.id}`
        })
      }
    },

    callRunner() {
      if (this.task.runner && this.task.runner.phone) {
        uni.makePhoneCall({
          phoneNumber: this.task.runner.phone
        })
      }
    },

    previewImage(index) {
      uni.previewImage({
        current: index,
        urls: this.task.images
      })
    },

    acceptTask() {
      uni.showModal({
        title: '确认接单',
        content: '确认接取此任务吗？',
        success: async (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '处理中...' })
            try {
              const response = await acceptTask(this.task.id)
              if (response.code === 200) {
                uni.showToast({
                  title: '接单成功',
                  icon: 'success'
                })
                this.task.status = 'accepted'
              } else {
                uni.showToast({
                  title: response.msg || '接单失败',
                  icon: 'none'
                })
              }
            } catch (error) {
              console.error('接单失败:', error)
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

    startTask() {
      uni.showModal({
        title: '确认开始',
        content: '确认开始配送吗？',
        success: async (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '处理中...' })
            try {
              // 调用状态更新API，将状态改为delivering
              const { put } = await import('@/api/request')
              const response = await put(`/task/${this.task.id}/status?status=delivering`)
              if (response.code === 200) {
                uni.showToast({
                  title: '配送开始',
                  icon: 'success'
                })
                this.task.status = 'delivering'
              } else {
                uni.showToast({
                  title: response.msg || '操作失败',
                  icon: 'none'
                })
              }
            } catch (error) {
              console.error('开始配送失败:', error)
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

    completeTask() {
      uni.showModal({
        title: '确认完成',
        content: '确认订单已完成吗？',
        success: async (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '处理中...' })
            try {
              const response = await completeTask(this.task.id)
              if (response.code === 200) {
                uni.showToast({
                  title: '订单完成',
                  icon: 'success'
                })
                this.task.status = 'completed'
                // 检查当前用户是否是订单发布者
                const user = uni.getStorageSync('user')
                if (user && user.id === this.task.publisher.id) {
                  // 只有发布者才能评价
                  setTimeout(() => {
                    uni.navigateTo({
                      url: '/pages/evaluate/index?taskId=' + this.task.id
                    })
                  }, 1500)
                }
              } else {
                uni.showToast({
                  title: response.msg || '操作失败',
                  icon: 'none'
                })
              }
            } catch (error) {
              console.error('完成订单失败:', error)
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

    cancelTask() {
      uni.showModal({
        title: '取消订单',
        content: '确认取消此订单吗？',
        confirmColor: '#f44336',
        success: async (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '处理中...' })
            try {
              const response = await cancelTask(this.task.id)
              if (response.code === 200) {
                uni.showToast({
                  title: '订单已取消',
                  icon: 'success'
                })
                this.task.status = 'cancelled'
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
    }
  }
}
</script>

<style lang="scss" scoped>
.task-detail-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 140rpx;
}

.content {
  padding: 20rpx;
  padding-bottom: 140rpx;
}

.status-bar {
  height: 120rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  font-size: 32rpx;
  font-weight: bold;

  &.status-pending {
    background: linear-gradient(135deg, #fff8e1, #ffecb3);
    color: #ff9800;
  }

  &.status-accepted {
    background: linear-gradient(135deg, #e3f2fd, #bbdefb);
    color: #2196f3;
  }

  &.status-delivering {
    background: linear-gradient(135deg, #f3e5f5, #e1bee7);
    color: #9c27b0;
  }

  &.status-completed {
    background: linear-gradient(135deg, #e8f5e8, #c8e6c9);
    color: #4caf50;
  }

  &.status-cancelled {
    background: linear-gradient(135deg, #ffebee, #ffcdd2);
    color: #f44336;
  }

  .status-icon {
    font-size: 40rpx;
  }
}

.content {
  padding: 20rpx;
}

.card {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
  }

  .task-type {
    padding: 8rpx 20rpx;
    background: #e3f2fd;
    color: #2196f3;
    border-radius: 20rpx;
    font-size: 24rpx;
  }

  .task-price {
    font-size: 48rpx;
    color: #f44336;
    font-weight: bold;
  }

  .task-title {
    display: block;
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 16rpx;
  }

  .task-desc {
    display: block;
    font-size: 28rpx;
    color: #666;
    line-height: 1.6;
    margin-bottom: 30rpx;
  }

  .card-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 24rpx;
    padding-bottom: 16rpx;
    border-bottom: 2rpx solid #f5f5f5;
  }

  .task-info {
    .info-row {
      display: flex;
      justify-content: space-between;
      padding: 16rpx 0;

      .label {
        color: #999;
        font-size: 28rpx;
      }

      .value {
        color: #333;
        font-size: 28rpx;
      }
    }
  }
}

.address-item {
  margin-bottom: 20rpx;

  .address-label {
    display: block;
    font-size: 24rpx;
    color: #999;
    margin-bottom: 8rpx;
  }

  .address-text {
    display: block;
    font-size: 28rpx;
    color: #333;
    line-height: 1.5;
  }
}

.map-preview {
  position: relative;
  height: 200rpx;
  border-radius: 12rpx;
  overflow: hidden;
  margin-top: 20rpx;

  .map-image {
    width: 100%;
    height: 100%;
  }

  .map-label {
    position: absolute;
    bottom: 20rpx;
    right: 20rpx;
    padding: 8rpx 20rpx;
    background: rgba(0, 0, 0, 0.6);
    color: white;
    border-radius: 20rpx;
    font-size: 24rpx;
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20rpx;

  .avatar {
    width: 96rpx;
    height: 96rpx;
    border-radius: 48rpx;
  }

  .user-detail {
    flex: 1;

    .username {
      display: block;
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 8rpx;
    }

    .user-stats {
      display: flex;
      gap: 20rpx;

      .stat-item {
        font-size: 24rpx;
        color: #999;
      }
    }
  }

  .user-actions {
    display: flex;
    gap: 16rpx;

    .action-btn {
      padding: 12rpx 24rpx;
      border-radius: 24rpx;
      font-size: 24rpx;
      border: none;

      &.chat {
        background: #e3f2fd;
        color: #2196f3;
      }

      &.phone {
        background: #e8f5e8;
        color: #4caf50;
      }
    }
  }
}

.images-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;

  .task-image {
    width: 200rpx;
    height: 200rpx;
    border-radius: 12rpx;
  }
}

.timeline {
  padding-left: 20rpx;

  .timeline-item {
    position: relative;
    padding-left: 60rpx;
    padding-bottom: 40rpx;

    &:last-child {
      padding-bottom: 0;
    }

    .timeline-dot {
      position: absolute;
      left: 0;
      top: 4rpx;
      width: 20rpx;
      height: 20rpx;
      border-radius: 50%;
      background: #ddd;
    }

    &.active {
      .timeline-dot {
        background: #2196f3;
      }

      .timeline-title {
        color: #2196f3;
        font-weight: bold;
      }
    }

    .timeline-content {
      .timeline-title {
        display: block;
        font-size: 28rpx;
        color: #666;
        margin-bottom: 8rpx;
      }

      .timeline-time {
        display: block;
        font-size: 24rpx;
        color: #999;
      }
    }
  }
}

.footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  background: white;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.08);

  .btn {
    width: 100%;
    height: 88rpx;
    border-radius: 44rpx;
    font-size: 32rpx;
    font-weight: bold;
    border: none;
    color: white;
  }
  
  .btn-info {
    background: linear-gradient(135deg, #e3f2fd, #2196f3);
  }
}
</style>
