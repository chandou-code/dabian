<template>
  <view class="dashboard-container">
    <Sidebar />
    
    <!-- 主内容区域 -->
    <view class="main-content" :class="{ 'main-content-expanded': !showSidebar }">
      <!-- 顶部欢迎栏 -->
      <view class="welcome-section">
        <view class="welcome-content">
          <text class="welcome-text">欢迎回来，{{ userInfo && userInfo.name ? userInfo.name : '用户' }}！</text>
          <text class="date-text">{{ currentDate }}</text>
        </view>
        <view class="user-actions">
          <button class="notification-btn" @click="showNotifications">
            <text class="icon">🔔</text>
            <text v-if="unreadCount > 0" class="badge">{{ unreadCount }}</text>
          </button>
        </view>
      </view>
      
      <!-- 快速统计 -->
      <view class="stats-grid">
        <view class="stat-card">
          <view class="stat-icon lost-icon">🔍</view>
          <view class="stat-content">
            <text class="stat-number">{{ stats.totalLost }}</text>
            <text class="stat-label">我发布的失物</text>
          </view>
        </view>
        
        <view class="stat-card">
          <view class="stat-icon found-icon">✅</view>
          <view class="stat-content">
            <text class="stat-number">{{ stats.totalFound }}</text>
            <text class="stat-label">我发布的招领</text>
          </view>
        </view>
        
        <view class="stat-card">
          <view class="stat-icon recovered-icon">🎉</view>
          <view class="stat-content">
            <text class="stat-number">{{ stats.recovered }}</text>
            <text class="stat-label">已找回物品</text>
          </view>
        </view>
        
        <view class="stat-card">
          <view class="stat-icon pending-icon">⏳</view>
          <view class="stat-content">
            <text class="stat-number">{{ stats.pending }}</text>
            <text class="stat-label">待处理信息</text>
          </view>
        </view>
      </view>
      
      <!-- 快速操作 -->
      <view class="quick-actions">
        <text class="section-title">快速操作</text>
        <view class="action-grid">
          <view class="action-item" @click="navigateTo('/pages/user/publish-lost')">
            <view class="action-icon lost-action">📝</view>
            <text class="action-text">发布失物</text>
          </view>
          
          <view class="action-item" @click="navigateTo('/pages/user/publish-found')">
            <view class="action-icon found-action">✅</view>
            <text class="action-text">发布招领</text>
          </view>
          
          <view class="action-item" @click="navigateTo('/pages/user/search')">
            <view class="action-icon search-action">🎯</view>
            <text class="action-text">智能搜索</text>
          </view>
          
          <view class="action-item" @click="navigateTo('/pages/user/lost-found')">
            <view class="action-icon list-action">📋</view>
            <text class="action-text">浏览信息</text>
          </view>
        </view>
      </view>
      
      <!-- 最近活动 -->
      <view class="recent-activities">
        <view class="section-header">
          <text class="section-title">最近活动</text>
          <text class="view-more" @click="navigateTo('/pages/user/lost-found')">查看更多</text>
        </view>
        
        <view class="activity-list">
          <view v-for="activity in recentActivities" :key="activity.id" class="activity-item">
            <view class="activity-icon">{{ activity.icon }}</view>
            <view class="activity-content">
              <text class="activity-title">{{ activity.title }}</text>
              <text class="activity-desc">{{ activity.description }}</text>
              <text class="activity-time">{{ activity.time }}</text>
            </view>
            <view class="activity-status" :class="getStatusClass(activity.status)">
              {{ getStatusText(activity.status) }}
            </view>
          </view>
        </view>
      </view>
      
      <!-- 推荐匹配 -->
      <view class="recommended-matches" v-if="recommendedMatches.length > 0">
        <view class="section-header">
          <text class="section-title">推荐匹配</text>
          <text class="view-more" @click="navigateTo('/pages/user/search')">查看全部</text>
        </view>
        
        <view class="match-list">
          <view v-for="match in recommendedMatches" :key="match.id" class="match-item">
            <image class="match-image" :src="match.image" mode="aspectFill"></image>
            <view class="match-content">
              <text class="match-title">{{ match.title }}</text>
              <text class="match-desc">{{ match.description }}</text>
              <text class="match-location">📍 {{ match.location }}</text>
            </view>
            <view class="match-action">
              <text class="match-score">匹配度 {{ match.score }}%</text>
              <button class="match-btn" @click="viewMatch(match)">查看</button>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import Sidebar from '@/components/Sidebar.vue'
import { mapGetters } from 'vuex'

export default {
  name: 'UserDashboard',
  components: {
    Sidebar
  },
  
  data() {
    return {
      showSidebar: true,
      unreadCount: 3,
      currentDate: '',
      stats: {
        totalLost: 5,
        totalFound: 3,
        recovered: 2,
        pending: 1
      },
      recentActivities: [
        {
          id: 1,
          icon: '📝',
          title: '发布了失物信息',
          description: '黑色钱包，内有身份证和银行卡',
          time: '2小时前',
          status: 'pending'
        },
        {
          id: 2,
          icon: '💬',
          title: '收到评论',
          description: '有人在评论区提供了线索',
          time: '5小时前',
          status: 'approved'
        },
        {
          id: 3,
          icon: '🎉',
          title: '物品已找回',
          description: '您的蓝色水杯已被好心人找回',
          time: '1天前',
          status: 'recovered'
        }
      ],
      recommendedMatches: [
        {
          id: 1,
          title: '黑色钱包',
          description: '在图书馆二楼发现黑色钱包',
          image: '/static/wallet.jpg',
          location: '图书馆二楼',
          score: 95
        }
      ]
    }
  },
  
  computed: {
    ...mapGetters(['user']),
    
    userInfo() {
      return this.user
    }
  },
  
  onLoad() {
    this.initDashboard()
  },
  
  methods: {
    initDashboard() {
      // 设置当前日期
      const now = new Date()
      const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }
      this.currentDate = now.toLocaleDateString('zh-CN', options)
      
      // 加载用户数据
      this.loadUserData()
    },
    
    loadUserData() {
      // 实际项目中这里会调用API获取用户数据
      console.log('加载用户数据...')
    },
    
    navigateTo(url) {
      uni.navigateTo({ url })
    },
    
    showNotifications() {
      uni.showModal({
        title: '通知中心',
        content: `您有 ${this.unreadCount} 条未读消息`,
        showCancel: false
      })
    },
    
    getStatusClass(status) {
      const classMap = {
        pending: 'status-pending',
        approved: 'status-approved',
        rejected: 'status-rejected',
        recovered: 'status-success'
      }
      return classMap[status] || 'status-pending'
    },
    
    getStatusText(status) {
      const textMap = {
        pending: '待审核',
        approved: '已通过',
        rejected: '已驳回',
        recovered: '已找回'
      }
      return textMap[status] || '待审核'
    },
    
    viewMatch(match) {
      uni.showModal({
        title: match.title,
        content: match.description + '\n\n' + '发现地点：' + match.location + '\n匹配度：' + match.score + '%',
        confirmText: '查看详情',
        success: (res) => {
          if (res.confirm) {
            uni.navigateTo({ url: `/pages/user/item-detail?id=${match.id}` })
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  display: flex;
  min-height: 100vh;
  background: #f5f5f5;
}

.main-content {
  flex: 1;
  margin-left: 250px;
  padding: 30rpx;
  transition: margin-left 0.3s ease;
}

.main-content-expanded {
  margin-left: 70px;
}

/* 欢迎区域 */
.welcome-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 40rpx 30rpx;
  border-radius: 16rpx;
  margin-bottom: 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-content {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.welcome-text {
  font-size: 36rpx;
  font-weight: 600;
}

.date-text {
  font-size: 24rpx;
  opacity: 0.9;
}

.notification-btn {
  position: relative;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.badge {
  position: absolute;
  top: -5rpx;
  right: -5rpx;
  background: #f44336;
  color: white;
  font-size: 18rpx;
  padding: 2rpx 8rpx;
  border-radius: 10rpx;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200rpx, 1fr));
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.stat-card {
  background: white;
  padding: 30rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
}

.lost-icon { background: #e3f2fd; }
.found-icon { background: #e8f5e8; }
.recovered-icon { background: #fff3e0; }
.pending-icon { background: #fce4ec; }

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.stat-number {
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
}

.stat-label {
  font-size: 24rpx;
  color: #666;
}

/* 快速操作 */
.quick-actions {
  background: white;
  padding: 30rpx;
  border-radius: 12rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150rpx, 1fr));
  gap: 20rpx;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  padding: 20rpx;
  border-radius: 12rpx;
  transition: background 0.3s;
}

.action-item:active {
  background: #f5f5f5;
}

.action-icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
}

.lost-action { background: #ffebee; }
.found-action { background: #e8f5e8; }
.search-action { background: #e3f2fd; }
.list-action { background: #fff3e0; }

.action-text {
  font-size: 24rpx;
  color: #666;
}

/* 最近活动 */
.recent-activities {
  background: white;
  padding: 30rpx;
  border-radius: 12rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.view-more {
  font-size: 26rpx;
  color: #2196f3;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
  padding: 20rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
}

.activity-icon {
  width: 50rpx;
  height: 50rpx;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
}

.activity-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.activity-title {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.activity-desc {
  font-size: 24rpx;
  color: #666;
}

.activity-time {
  font-size: 22rpx;
  color: #999;
}

.activity-status {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

/* 推荐匹配 */
.recommended-matches {
  background: white;
  padding: 30rpx;
  border-radius: 12rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.match-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.match-item {
  display: flex;
  gap: 20rpx;
  padding: 20rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  align-items: center;
}

.match-image {
  width: 100rpx;
  height: 100rpx;
  border-radius: 8rpx;
  background: #e0e0e0;
}

.match-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.match-title {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.match-desc {
  font-size: 24rpx;
  color: #666;
}

.match-location {
  font-size: 22rpx;
  color: #999;
}

.match-action {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10rpx;
}

.match-score {
  font-size: 22rpx;
  color: #4caf50;
  font-weight: 500;
}

.match-btn {
  background: #2196f3;
  color: white;
  border: none;
  padding: 10rpx 20rpx;
  border-radius: 6rpx;
  font-size: 22rpx;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
    padding: 20rpx;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .action-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>