<template>
  <view class="order-detail-page">
    <view class="page-header">
      <view class="header-back" @click="goBack">
        <text>‹</text>
      </view>
      <text class="page-title">订单详情</text>
      <view class="header-action">
        <text class="iconfont icon-more"></text>
      </view>
    </view>
    
    <scroll-view class="scroll-content" scroll-y>
      <!-- 订单状态 -->
      <view class="status-section">
        <view class="status-icon">{{ getStatusIcon(order.status) }}</view>
        <view class="status-text">{{ getStatusText(order.status) }}</view>
        <view class="status-time">{{ order.statusTime || '更新于刚刚' }}</view>
      </view>
      
      <!-- 地图区域 -->
      <view class="map-section">
        <map
          id="orderMap"
          class="map"
          :latitude="mapCenter.latitude"
          :longitude="mapCenter.longitude"
          :markers="mapMarkers"
          :polyline="mapPolyline"
          :enable-3D="true"
          :enable-zoom="true"
        ></map>
        
        <view class="map-overlay">
          <view class="distance-info">
            <text class="distance">{{ order.distance || '0.5' }}km</text>
            <text class="duration">{{ order.estimatedTime || '约10分钟' }}</text>
          </view>
        </view>
      </view>
      
      <!-- 订单进度 -->
      <view class="progress-section">
        <view class="section-title">订单进度</view>
        <view class="progress-timeline">
          <view
            class="progress-item"
            :class="{ active: step.completed, current: step.current }"
            v-for="(step, index) in progressSteps"
            :key="index"
          >
            <view class="progress-dot">
              <text v-if="step.completed">✓</text>
            </view>
            <view class="progress-content">
              <text class="progress-title">{{ step.title }}</text>
              <text class="progress-time" v-if="step.time">{{ step.time }}</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 任务信息 -->
      <view class="task-info-section">
        <view class="section-title">任务信息</view>
        <view class="info-card">
          <view class="info-row">
            <text class="label">任务类型</text>
            <text class="value">{{ getTypeText(order.type) }}</text>
          </view>
          <view class="info-row">
            <text class="label">任务标题</text>
            <text class="value">{{ order.title }}</text>
          </view>
          <view class="info-row">
            <text class="label">取货地址</text>
            <text class="value">{{ order.pickupAddress }}</text>
          </view>
          <view class="info-row">
            <text class="label">送达地址</text>
            <text class="value">{{ order.deliveryAddress }}</text>
          </view>
          <view class="info-row">
            <text class="label">期望时间</text>
            <text class="value">{{ formatTime(order.expectedTime) }}</text>
          </view>
          <view class="info-row">
            <text class="label">任务描述</text>
            <text class="value">{{ order.description }}</text>
          </view>
        </view>
      </view>
      
      <!-- 跑腿员信息 -->
      <view class="runner-section" v-if="order.runner">
        <view class="section-title">跑腿员信息</view>
        <view class="runner-card">
          <image class="runner-avatar" :src="order.runner.avatar || '/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg'" mode="aspectFill" />
          <view class="runner-info">
            <text class="runner-name">{{ order.runner.name }}</text>
            <view class="runner-rating">
              <text class="star">★</text>
              <text class="score">{{ order.runner.rating || '5.0' }}</text>
              <text class="orders">{{ order.runner.orderCount || '0' }}单</text>
            </view>
          </view>
          <view class="runner-actions">
            <button class="action-btn btn-chat" @click="openChat">
              <text>💬</text>
              联系
            </button>
            <button class="action-btn btn-phone" @click="callRunner">
              <text>📞</text>
              电话
            </button>
          </view>
        </view>
      </view>
      
      <!-- 订单信息 -->
      <view class="order-info-section">
        <view class="section-title">订单信息</view>
        <view class="info-card">
          <view class="info-row">
            <text class="label">订单编号</text>
            <text class="value copyable" @click="copyText(order.orderNo)">{{ order.orderNo }}</text>
          </view>
          <view class="info-row">
            <text class="label">下单时间</text>
            <text class="value">{{ formatTime(order.createdAt) }}</text>
          </view>
          <view class="info-row">
            <text class="label">跑腿费用</text>
            <text class="value price">¥{{ order.price }}</text>
          </view>
          <view class="info-row">
            <text class="label">联系电话</text>
            <text class="value">{{ order.phone }}</text>
          </view>
        </view>
      </view>
      
      <!-- 备注信息 -->
      <view class="remark-section" v-if="order.remark">
        <view class="section-title">备注信息</view>
        <view class="remark-card">
          <text class="remark-text">{{ order.remark }}</text>
        </view>
      </view>
      
      <!-- 图片展示 -->
      <view class="images-section" v-if="order.images && order.images.length > 0">
        <view class="section-title">任务图片</view>
        <scroll-view class="image-list" scroll-x>
          <view
            class="image-item"
            v-for="(img, index) in order.images"
            :key="index"
            @click="previewImage(index)"
          >
            <image :src="img" mode="aspectFill" />
          </view>
        </scroll-view>
      </view>
    </scroll-view>
    
    <!-- 底部操作栏 -->
    <view class="footer-actions">
      <button class="action-btn btn-cancel" v-if="order.status === 'pending'" @click="cancelOrder">
        取消订单
      </button>
      <button class="action-btn btn-confirm" v-if="order.status === 'delivering'" @click="confirmReceipt">
        确认收货
      </button>
      <button class="action-btn btn-evaluate" v-if="order.status === 'completed' && !order.evaluated" @click="evaluateOrder">
        评价订单
      </button>
    </view>
  </view>
</template>

<script>
import { getOrderDetail, updateOrderStatus } from '@/api/errand'

export default {
  data() {
    return {
      orderId: null,
      order: {},
      
      mapCenter: {
        latitude: 39.909187,
        longitude: 116.397451
      },
      
      mapMarkers: [],
      mapPolyline: [],
      
      progressSteps: []
    }
  },
  
  onLoad(options) {
    if (options.id) {
      this.orderId = options.id
      this.loadOrderDetail()
    }
  },
  
  onPullDownRefresh() {
    this.loadOrderDetail()
    uni.stopPullDownRefresh()
  },
  
  methods: {
    // 加载订单详情
    async loadOrderDetail() {
      try {
        const response = await getOrderDetail(this.orderId)

        if (response.code === 200) {
          this.order = response.data
          this.initMap()
          this.initProgress()
        }
      } catch (error) {
        console.error('加载订单详情失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      }
    },
    
    // 初始化地图
    initMap() {
      // 设置地图中心
      this.mapCenter = {
        latitude: this.order.latitude || 39.909187,
        longitude: this.order.longitude || 116.397451
      }
      
      // 添加标记
      this.mapMarkers = [
        {
          id: 1,
          latitude: this.order.pickupLatitude || this.mapCenter.latitude,
          longitude: this.order.pickupLongitude || this.mapCenter.longitude,
          iconPath: '/static/marker-pickup.png',
          width: 30,
          height: 30,
          title: '取货点'
        },
        {
          id: 2,
          latitude: this.order.deliveryLatitude || this.mapCenter.latitude,
          longitude: this.order.deliveryLongitude || this.mapCenter.longitude,
          iconPath: '/static/marker-delivery.png',
          width: 30,
          height: 30,
          title: '送达点'
        }
      ]
      
      // 如果有跑腿员位置，添加跑腿员标记
      if (this.order.runner && this.order.runner.latitude) {
        this.mapMarkers.push({
          id: 3,
          latitude: this.order.runner.latitude,
          longitude: this.order.runner.longitude,
          iconPath: '/static/marker-runner.png',
          width: 30,
          height: 30,
          title: '跑腿员'
        })
      }
      
      // 绘制路线
      if (this.order.polyline) {
        this.mapPolyline = [{
          points: this.order.polyline,
          color: '#2196f3',
          width: 6,
          arrowLine: true
        }]
      }
    },
    
    // 初始化进度
    initProgress() {
      const allSteps = [
        { title: '订单已创建', completed: true },
        { title: '等待接单', completed: this.order.status !== 'pending' },
        { title: '跑腿员已接单', completed: ['accepted', 'delivering', 'completed'].includes(this.order.status) },
        { title: '已取货', completed: ['delivering', 'completed'].includes(this.order.status) },
        { title: '配送中', completed: this.order.status === 'completed' },
        { title: '已完成', completed: this.order.status === 'completed' }
      ]
      
      // 根据当前状态确定当前步骤
      const statusIndex = {
        'pending': 1,
        'accepted': 2,
        'delivering': 3,
        'completed': 5
      }
      
      this.progressSteps = allSteps.map((step, index) => ({
        ...step,
        current: index === (statusIndex[this.order.status] || 0),
        time: index === (statusIndex[this.order.status] || 0) ? this.order.statusTime : null
      }))
    },
    
    // 取消订单
    cancelOrder() {
      uni.showModal({
        title: '确认取消',
        content: '确定要取消这个订单吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              const response = await updateOrderStatus(this.orderId, 'cancelled')
              
              if (response.code === 200) {
                uni.showToast({
                  title: '取消成功',
                  icon: 'success'
                })
                this.loadOrderDetail()
              }
            } catch (error) {
              console.error('取消订单失败:', error)
              uni.showToast({
                title: '取消失败',
                icon: 'none'
              })
            }
          }
        }
      })
    },
    
    // 确认收货
    confirmReceipt() {
      uni.showModal({
        title: '确认收货',
        content: '确认已收到物品？',
        success: async (res) => {
          if (res.confirm) {
            try {
              const response = await updateOrderStatus(this.orderId, 'completed')
              
              if (response.code === 200) {
                uni.showToast({
                  title: '收货成功',
                  icon: 'success'
                })
                this.loadOrderDetail()
              }
            } catch (error) {
              console.error('确认收货失败:', error)
              uni.showToast({
                title: '操作失败',
                icon: 'none'
              })
            }
          }
        }
      })
    },
    
    // 评价订单
    evaluateOrder() {
      uni.navigateTo({
        url: `/pages/evaluate/index?orderId=${this.orderId}`
      })
    },
    
    // 打开聊天
    openChat() {
      uni.navigateTo({
        url: `/pages/chat/detail?userId=${this.order.runner.id}`
      })
    },
    
    // 拨打电话
    callRunner() {
      uni.makePhoneCall({
        phoneNumber: this.order.runner.phone
      })
    },
    
    // 预览图片
    previewImage(index) {
      uni.previewImage({
        current: index,
        urls: this.order.images
      })
    },
    
    // 复制文本
    copyText(text) {
      uni.setClipboardData({
        data: text,
        success: () => {
          uni.showToast({
            title: '已复制',
            icon: 'success'
          })
        }
      })
    },
    
    // 返回
    goBack() {
      uni.navigateBack()
    },
    
    // 获取状态图标
    getStatusIcon(status) {
      const icons = {
        'pending': '⏳',
        'accepted': '✅',
        'delivering': '🚴',
        'completed': '🎉',
        'cancelled': '❌'
      }
      return icons[status] || '📋'
    },
    
    // 获取状态文本
    getStatusText(status) {
      const texts = {
        'pending': '等待接单',
        'accepted': '已接单',
        'delivering': '配送中',
        'completed': '已完成',
        'cancelled': '已取消'
      }
      return texts[status] || '未知状态'
    },
    
    // 获取类型文本
    getTypeText(type) {
      const types = {
        'delivery': '快递代取',
        'food': '外卖代送',
        'shopping': '物品购买',
        'queue': '排队代办',
        'document': '文件传递',
        'other': '其他服务'
      }
      return types[type] || type
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
.order-detail-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 30rpx;
  background: white;
  
  .header-back {
    width: 60rpx;
    font-size: 48rpx;
    color: #333;
  }
  
  .page-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
  }
  
  .header-action {
    width: 60rpx;
    font-size: 36rpx;
    color: #666;
  }
}

.scroll-content {
  flex: 1;
}

.status-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx 30rpx;
  background: white;
  
  .status-icon {
    font-size: 100rpx;
    margin-bottom: 20rpx;
  }
  
  .status-text {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 10rpx;
  }
  
  .status-time {
    font-size: 24rpx;
    color: #999;
  }
}

.map-section {
  position: relative;
  height: 400rpx;
  background: #e0e0e0;
  
  .map {
    width: 100%;
    height: 100%;
  }
  
  .map-overlay {
    position: absolute;
    bottom: 20rpx;
    left: 20rpx;
    right: 20rpx;
    background: white;
    border-radius: 12rpx;
    padding: 20rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.1);
    
    .distance-info {
      display: flex;
      justify-content: space-between;
      
      .distance {
        font-size: 32rpx;
        font-weight: bold;
        color: #2196f3;
      }
      
      .duration {
        font-size: 24rpx;
        color: #666;
      }
    }
  }
}

.progress-section,
.task-info-section,
.runner-section,
.order-info-section,
.remark-section,
.images-section {
  margin-top: 20rpx;
  padding: 30rpx;
  background: white;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.progress-timeline {
  .progress-item {
    display: flex;
    padding-bottom: 40rpx;
    position: relative;
    
    &:not(:last-child)::after {
      content: '';
      position: absolute;
      left: 19rpx;
      top: 40rpx;
      bottom: 0;
      width: 2rpx;
      background: #e0e0e0;
    }
    
    &.active::after {
      background: #2196f3;
    }
    
    .progress-dot {
      width: 40rpx;
      height: 40rpx;
      border-radius: 50%;
      background: #e0e0e0;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20rpx;
      color: white;
      margin-right: 20rpx;
      flex-shrink: 0;
      
      &.active {
        background: #2196f3;
      }
    }
    
    .progress-content {
      flex: 1;
      
      .progress-title {
        display: block;
        font-size: 28rpx;
        color: #333;
        margin-bottom: 8rpx;
      }
      
      .progress-time {
        font-size: 24rpx;
        color: #999;
      }
    }
  }
}

.info-card,
.remark-card {
  .info-row {
    display: flex;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    .label {
      font-size: 26rpx;
      color: #666;
      width: 180rpx;
      flex-shrink: 0;
    }
    
    .value {
      flex: 1;
      font-size: 28rpx;
      color: #333;
      
      &.price {
        color: #ff5722;
        font-weight: bold;
      }
      
      &.copyable {
        color: #2196f3;
      }
    }
  }
  
  .remark-text {
    font-size: 28rpx;
    color: #333;
    line-height: 1.6;
  }
}

.runner-card {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #f8f8f8;
  border-radius: 12rpx;
  
  .runner-avatar {
    width: 100rpx;
    height: 100rpx;
    border-radius: 50%;
    margin-right: 20rpx;
  }
  
  .runner-info {
    flex: 1;
    
    .runner-name {
      display: block;
      font-size: 30rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 10rpx;
    }
    
    .runner-rating {
      display: flex;
      align-items: center;
      font-size: 24rpx;
      
      .star {
        color: #ff9800;
        margin-right: 8rpx;
      }
      
      .score {
        color: #ff9800;
        font-weight: bold;
        margin-right: 16rpx;
      }
      
      .orders {
        color: #999;
      }
    }
  }
  
  .runner-actions {
    display: flex;
    gap: 16rpx;
    
    .action-btn {
      padding: 16rpx 24rpx;
      border-radius: 30rpx;
      font-size: 24rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      border: none;
      
      text {
        font-size: 32rpx;
        margin-bottom: 4rpx;
      }
      
      &.btn-chat {
        background: #e3f2fd;
        color: #2196f3;
      }
      
      &.btn-phone {
        background: #e8f5e8;
        color: #4caf50;
      }
    }
  }
}

.image-list {
  white-space: nowrap;
  
  .image-item {
    display: inline-block;
    width: 200rpx;
    height: 200rpx;
    margin-right: 20rpx;
    border-radius: 12rpx;
    overflow: hidden;
    
    image {
      width: 100%;
      height: 100%;
    }
  }
}

.footer-actions {
  display: flex;
  padding: 20rpx 30rpx;
  background: white;
  border-top: 1rpx solid #f0f0f0;
  gap: 20rpx;
  
  .action-btn {
    flex: 1;
    height: 80rpx;
    border-radius: 40rpx;
    font-size: 32rpx;
    border: none;
    
    &.btn-cancel {
      background: #fff;
      border: 2rpx solid #e0e0e0;
      color: #666;
    }
    
    &.btn-confirm,
    &.btn-evaluate {
      background: #2196f3;
      color: white;
    }
  }
}
</style>
