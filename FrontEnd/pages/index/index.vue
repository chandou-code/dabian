<template>
  <view class="home-container">
    <!-- 顶部横幅 -->
    <view class="hero-section">
      <view class="hero-content">
        <text class="hero-title">校园失物招领</text>
        <text class="hero-subtitle">让失物有归途，让爱心传递</text>
      </view>
      <view class="hero-actions">
        <button class="btn btn-primary hero-btn" @click="navigateTo('/pages/login/login')">
          立即登录
        </button>
        <button class="btn btn-outline hero-btn" @click="navigateTo('/pages/register/register')">
          注册账号
        </button>
      </view>
    </view>

    <!-- 快速操作 -->
    <view class="quick-actions">
      <view class="section-title">快速操作</view>
      <view class="action-grid">
        <view class="action-item" @click="navigateTo('/pages/user/publish-lost')">
          <view class="action-icon">🔍</view>
          <text class="action-text">发布失物</text>
        </view>
        <view class="action-item" @click="navigateTo('/pages/user/publish-found')">
          <view class="action-icon">✅</view>
          <text class="action-text">发布招领</text>
        </view>
        <view class="action-item" @click="navigateTo('/pages/user/lost-found')">
          <view class="action-icon">📋</view>
          <text class="action-text">浏览信息</text>
        </view>
        <view class="action-item" @click="navigateTo('/pages/user/search')">
          <view class="action-icon">🎯</view>
          <text class="action-text">智能搜索</text>
        </view>
      </view>
    </view>

    <!-- 统计信息 -->
    <view class="stats-section">
      <view class="section-title">平台数据</view>
      <view class="stats-grid">
        <view class="stat-item">
          <text class="stat-number">{{ stats.totalLost }}</text>
          <text class="stat-label">失物登记</text>
        </view>
        <view class="stat-item">
          <text class="stat-number">{{ stats.totalFound }}</text>
          <text class="stat-label">招领信息</text>
        </view>
        <view class="stat-item">
          <text class="stat-number">{{ stats.recovered }}</text>
          <text class="stat-label">成功找回</text>
        </view>
        <view class="stat-item">
          <text class="stat-number">{{ stats.recoveryRate }}%</text>
          <text class="stat-label">找回率</text>
        </view>
      </view>
    </view>

    <!-- 最新信息 -->
    <view class="recent-section">
      <view class="section-title">最新信息</view>
      <view class="recent-list">
        <view 
          v-for="item in recentItems" 
          :key="item.id"
          class="recent-item"
          @click="viewItem(item)"
        >
          <view class="item-icon">{{ item.type === 'lost' ? '🔍' : '✅' }}</view>
          <view class="item-content">
            <text class="item-title">{{ item.title }}</text>
            <text class="item-desc">{{ item.description }}</text>
            <text class="item-time">{{ item.time }}</text>
          </view>
          <view class="item-status" :class="'status-' + item.status">
            {{ getStatusText(item.status) }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      stats: {
        totalLost: 1248,
        totalFound: 956,
        recovered: 892,
        recoveryRate: 71.5
      },
      recentItems: [
        {
          id: 1,
          type: 'lost',
          title: '黑色钱包',
          description: '在图书馆二楼丢失，内有身份证和银行卡',
          time: '2小时前',
          status: 'pending'
        },
        {
          id: 2,
          type: 'found',
          title: '蓝色水杯',
          description: '在教学楼三楼卫生间发现的蓝色保温杯',
          time: '5小时前',
          status: 'approved'
        },
        {
          id: 3,
          type: 'lost',
          title: ' AirPods Pro',
          description: '黑色降噪耳机，在食堂丢失',
          time: '1天前',
          status: 'recovered'
        }
      ]
    }
  },
  
  methods: {
    navigateTo(url) {
      uni.navigateTo({ url })
    },
    
    viewItem(item) {
      uni.showModal({
        title: item.title,
        content: item.description,
        confirmText: '查看详情',
        success: (res) => {
          if (res.confirm) {
            uni.navigateTo({ 
              url: `/pages/user/lost-found?type=${item.type}&id=${item.id}` 
            })
          }
        }
      })
    },
    
    getStatusText(status) {
      const statusMap = {
        pending: '待审核',
        approved: '已通过',
        recovered: '已找回'
      }
      return statusMap[status] || '待审核'
    }
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8f9fa 0%, #e9ecef 100%);
}

/* 横幅区域 */
.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 80rpx 40rpx 60rpx;
  text-align: center;
}

.hero-content {
  margin-bottom: 40rpx;
}

.hero-title {
  font-size: 48rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 20rpx;
}

.hero-subtitle {
  font-size: 28rpx;
  opacity: 0.9;
  display: block;
}

.hero-actions {
  display: flex;
  gap: 20rpx;
  justify-content: center;
}

.hero-btn {
  flex: 1;
  max-width: 200rpx;
  height: 70rpx;
  font-size: 28rpx;
}

/* 快速操作 */
.quick-actions {
  padding: 40rpx 30rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 30rpx;
  display: block;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
}

.action-item {
  background: white;
  border-radius: 12rpx;
  padding: 30rpx 20rpx;
  text-align: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.action-item:active {
  transform: scale(0.95);
}

.action-icon {
  font-size: 40rpx;
  margin-bottom: 10rpx;
  display: block;
}

.action-text {
  font-size: 24rpx;
  color: #666;
  display: block;
}

/* 统计信息 */
.stats-section {
  padding: 0 30rpx 40rpx;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
}

.stat-item {
  background: white;
  border-radius: 12rpx;
  padding: 30rpx 20rpx;
  text-align: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.stat-number {
  font-size: 36rpx;
  font-weight: bold;
  color: #2196f3;
  display: block;
  margin-bottom: 10rpx;
}

.stat-label {
  font-size: 22rpx;
  color: #666;
  display: block;
}

/* 最新信息 */
.recent-section {
  padding: 0 30rpx 40rpx;
}

.recent-list {
  background: white;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.recent-item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.recent-item:last-child {
  border-bottom: none;
}

.item-icon {
  font-size: 32rpx;
  margin-right: 20rpx;
  width: 40rpx;
  text-align: center;
}

.item-content {
  flex: 1;
}

.item-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 10rpx;
}

.item-desc {
  font-size: 24rpx;
  color: #666;
  display: block;
  margin-bottom: 10rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-time {
  font-size: 20rpx;
  color: #999;
  display: block;
}

.item-status {
  font-size: 22rpx;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  background: #f5f5f5;
}

.status-pending {
  color: #ff9800;
  background: #fff3e0;
}

.status-approved {
  color: #4caf50;
  background: #e8f5e8;
}

.status-recovered {
  color: #2196f3;
  background: #e3f2fd;
}

.btn-outline {
  background: transparent;
  border: 2rpx solid white;
  color: white;
}

.btn-outline:active {
  background: rgba(255, 255, 255, 0.1);
}
</style>
