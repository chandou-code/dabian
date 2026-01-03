<template>
  <view v-if="showAnnouncement" class="announcement-popup">
    <view class="popup-content">
      <view class="popup-header">
        <text class="popup-title">
          <text class="announcement-label">公告</text>
          {{ currentAnnouncement.title }}
        </text>
        <text class="popup-close" @click="closeAnnouncement">×</text>
      </view>
      
      <view class="popup-body">
        <text class="popup-text">{{ currentAnnouncement.content }}</text>
      </view>
      
      <view class="popup-footer">
        <text class="countdown-text">{{ countdown }}秒后自动关闭</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'AnnouncementPopup',
  data() {
    return {
      showAnnouncement: false,
      currentAnnouncement: {
        title: '',
        content: '',
        time: ''
      },
      timer: null,
      countdown: 3,
      countdownTimer: null
    }
  },
  
  mounted() {
    this.loadLatestAnnouncement()
  },
  
  beforeDestroy() {
    if (this.timer) {
      clearTimeout(this.timer)
    }
  },
  
  methods: {
    // 加载最新公告
    async loadLatestAnnouncement() {
      console.log('开始加载最新公告...')
      try {
        console.log('导入公告API...')
        const { getLatestAnnouncement } = await import('../api/announcement')
        console.log('调用获取最新公告API...')
        const response = await getLatestAnnouncement()
        console.log('获取最新公告API返回结果:', response)
        
        if (response.success && response.data) {
          console.log('公告数据获取成功，显示公告...')
          // 重置倒计时
          this.countdown = 3
          this.currentAnnouncement = response.data
          this.showAnnouncement = true
          
          // 3秒后自动关闭
          this.timer = setTimeout(() => {
            this.closeAnnouncement()
          }, 3000)
          
          // 开始倒计时
          this.startCountdown()
        }
      } catch (error) {
        console.error('加载公告失败:', error)
      } finally {
        console.log('加载最新公告完成')
      }
    },
    
    // 开始倒计时
    startCountdown() {
      this.countdown = 3
      this.countdownTimer = setInterval(() => {
        this.countdown--
        if (this.countdown <= 0) {
          this.clearCountdownTimer()
        }
      }, 1000)
    },
    
    // 清除倒计时计时器
    clearCountdownTimer() {
      if (this.countdownTimer) {
        clearInterval(this.countdownTimer)
        this.countdownTimer = null
      }
    },
    
    // 关闭公告
    closeAnnouncement() {
      this.showAnnouncement = false
      if (this.timer) {
        clearTimeout(this.timer)
        this.timer = null
      }
      this.clearCountdownTimer()
    }
  }
}
</script>

<style scoped>
/* 公告弹窗容器 - 固定在右下角 */
.announcement-popup {
  position: fixed;
  bottom: 20rpx;
  right: 20rpx;
  z-index: 1000;
  animation: slideUp 0.3s ease-out;
  width: 80%;
  max-width: 600rpx;
}

/* 弹窗内容 */
.popup-content {
  background: white;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

/* 弹窗头部 */
.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 20rpx;
  background: #f8f9fa;
  border-bottom: 2rpx solid #e9ecef;
}

/* 弹窗标题 */
.popup-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  flex: 1;
}

/* 公告标签 */
.announcement-label {
  font-size: 22rpx;
  color: #666;
  font-weight: 400;
  margin-right: 10rpx;
}

/* 关闭按钮 */
.popup-close {
  font-size: 36rpx;
  color: #999;
  cursor: pointer;
  line-height: 1;
  margin-left: 20rpx;
}

/* 弹窗主体内容 */
.popup-body {
  padding: 20rpx;
}

/* 弹窗文本 */
.popup-text {
  font-size: 26rpx;
  color: #333;
  line-height: 1.5;
  white-space: pre-wrap;
}

/* 弹窗底部 */
.popup-footer {
  padding: 12rpx 20rpx;
  background: #fafafa;
  border-top: 2rpx solid #e9ecef;
  text-align: right;
}

/* 倒计时文本 */
.countdown-text {
  font-size: 22rpx;
  color: #666;
}

/* 动画效果 */
@keyframes slideUp {
  from {
    transform: translateY(100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .announcement-popup {
    width: 95%;
    bottom: 10rpx;
    right: 10rpx;
  }
  
  .popup-title {
    font-size: 26rpx;
  }
  
  .popup-text {
    font-size: 24rpx;
  }
}
</style>
