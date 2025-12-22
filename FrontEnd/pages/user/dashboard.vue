<template>
  <view class="dashboard-container">
    <Sidebar />
    
    <!-- 主内容区域 -->
    <view class="main-content" :class="{ 'main-content-expanded': !showSidebar }">
      <!-- 顶部欢迎栏 -->
      <view class="welcome-section">
        <view class="welcome-content">
          <text class="welcome-text">
            {{ loading ? '加载中...' : (userInfo && userInfo.name ? userInfo.name : '用户') }}！
          </text>
          <text class="date-text">{{ currentDate }}</text>
        </view>
        <view class="user-actions">
          <button class="refresh-btn" @click="loadUserData" :disabled="loading">
            <text class="icon">{{ loading ? '⏳' : '🔄' }}</text>
          </button>
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
          <text class="view-more" @click="viewMoreActivities">查看更多</text>
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
      <view class="recommended-matches" v-if="recommendedMatches && recommendedMatches.length > 0">
        <view class="section-header">
          <text class="section-title">推荐匹配</text>
          <text class="view-more" @click="viewMoreMatches">查看全部</text>
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
      unreadCount: 0,
      currentDate: '',
      loading: false,
      stats: {
        totalLost: 0,
        totalFound: 0,
        recovered: 0,
        pending: 0
      },
      recentActivities: [],
      recommendedMatches: []
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
  
  onShow() {
    // 确保页面显示时数据正确
    this.ensureDataInitialized()
  },
  
  methods: {
    initDashboard() {
      // 确保数据初始化
      this.ensureDataInitialized()
      
      // 设置当前日期
      const now = new Date()
      const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }
      this.currentDate = now.toLocaleDateString('zh-CN', options)
      
      // 加载用户数据
      this.loadUserData()
    },
    
    async loadUserData() {
      this.loading = true
      try {
        console.log('加载用户数据...')
        
        // 导入API
        const { getDashboardData, getDashboardActivities, getDashboardMatches } = await import('@/api/stats')
        
        // 并行加载所有数据
        const [dashboardResult, activitiesResult, matchesResult] = await Promise.all([
          getDashboardData(),
          getDashboardActivities(1, 10),
          getDashboardMatches(1, 10)
        ])
        
        // 处理仪表板数据
        if (dashboardResult.success && dashboardResult.data) {
          const dashboard = dashboardResult.data
          
          // 更新统计信息
          if (dashboard.userStats) {
            this.stats = {
              totalLost: dashboard.userStats.totalLost || 0,
              totalFound: dashboard.userStats.totalFound || 0,
              recovered: dashboard.userStats.recovered || 0,
              pending: dashboard.userStats.pending || 0
            }
            
            // 更新未读通知数量
            this.unreadCount = dashboard.userStats.unreadNotifications || 0
          }
          
          // 更新最近活动
          if (dashboard.recentActivities && dashboard.recentActivities.length > 0) {
            this.recentActivities = dashboard.recentActivities.map(activity => ({
              id: activity.id,
              icon: activity.icon || '📝',
              title: activity.title,
              description: activity.description,
              time: this.formatTime(activity.time),
              status: activity.status,
              type: activity.type
            }))
          }
          
          // 更新推荐匹配
          if (dashboard.recommendedMatches && dashboard.recommendedMatches.length > 0) {
            this.recommendedMatches = dashboard.recommendedMatches.map(match => ({
              id: match.id,
              title: match.title,
              description: match.description,
              image: match.image || '/static/logo.png',
              location: match.location,
              score: match.score,
              type: match.type,
              relatedItemId: match.relatedItemId
            }))
          }
        }
        
        console.log('用户数据加载成功')
      } catch (error) {
        console.error('加载用户数据失败:', error)
        // 确保在出错时使用默认数据
        this.ensureDataInitialized()
      } finally {
        this.loading = false
      }
    },
    
    // 格式化时间
    formatTime(time) {
      if (!time) return ''
      
      try {
        const date = new Date(time)
        const now = new Date()
        const diff = now - date
        
        // 计算时间差
        const minutes = Math.floor(diff / 60000)
        const hours = Math.floor(diff / 3600000)
        const days = Math.floor(diff / 86400000)
        
        if (minutes < 1) return '刚刚'
        if (minutes < 60) return `${minutes}分钟前`
        if (hours < 24) return `${hours}小时前`
        if (days < 7) return `${days}天前`
        
        return date.toLocaleDateString('zh-CN')
      } catch (e) {
        return ''
      }
    },
    
    ensureDataInitialized() {
      // 确保所有必要的数组都已初始化
      if (!this.recommendedMatches) {
        this.recommendedMatches = []
      }
      if (!this.recentActivities) {
        this.recentActivities = []
      }
      if (!this.stats) {
        this.stats = {
          totalLost: 0,
          totalFound: 0,
          recovered: 0,
          pending: 0
        }
      }
    },
    
    navigateTo(url) {
      uni.navigateTo({ url })
    },
    
    // 查看更多活动
    viewMoreActivities() {
      uni.navigateTo({ 
        url: '/pages/user/activities' 
      })
    },
    
    // 查看更多匹配
    viewMoreMatches() {
      uni.navigateTo({ 
        url: '/pages/user/search' 
      })
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

.notification-btn, .refresh-btn {
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
  margin-left: 20rpx;
}

.refresh-btn:disabled {
  opacity: 0.6;
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