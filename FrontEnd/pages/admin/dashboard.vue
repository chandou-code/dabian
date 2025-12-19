<template>
  <view class="admin-dashboard">
    <Sidebar />
    
    <view class="main-content" :class="{ 'main-content-expanded': !showSidebar }">
      <!-- 顶部统计卡片 -->
      <view class="stats-overview">
        <view class="stat-card primary">
          <view class="stat-icon">👥</view>
          <view class="stat-content">
            <text class="stat-number">{{ statistics.totalUsers }}</text>
            <text class="stat-label">总用户数</text>
          </view>
        </view>
        
        <view class="stat-card success">
          <view class="stat-icon">📋</view>
          <view class="stat-content">
            <text class="stat-number">{{ statistics.totalItems }}</text>
            <text class="stat-label">失物招领总数</text>
          </view>
        </view>
        
        <view class="stat-card warning">
          <view class="stat-icon">🎯</view>
          <view class="stat-content">
            <text class="stat-number">{{ statistics.recoveryRate }}%</text>
            <text class="stat-label">找回率</text>
          </view>
        </view>
        
        <view class="stat-card danger">
          <view class="stat-icon">⏳</view>
          <view class="stat-content">
            <text class="stat-number">{{ statistics.pendingReviews }}</text>
            <text class="stat-label">待审核信息</text>
          </view>
        </view>
      </view>
      
      <!-- 图表区域 -->
      <view class="charts-section">
        <view class="chart-card">
          <text class="chart-title">失物招领趋势</text>
          <view class="chart-container">
            <!-- 这里应该集成ECharts -->
            <view class="chart-placeholder">
              <text>📊 ECharts图表区域</text>
              <text class="chart-desc">显示过去30天的失物招领趋势变化</text>
            </view>
          </view>
        </view>
        
        <view class="chart-card">
          <text class="chart-title">高频丢失物品分析</text>
          <view class="chart-container">
            <view class="chart-placeholder">
              <text>📊 ECharts图表区域</text>
              <text class="chart-desc">按类别统计丢失物品数量</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 快速操作 -->
      <view class="quick-actions">
        <text class="section-title">快速操作</text>
        <view class="action-grid">
          <view class="action-item" @click="navigateTo('/pages/admin/user-management')">
            <view class="action-icon user-icon">👥</view>
            <text class="action-text">用户管理</text>
          </view>
          
          <view class="action-item" @click="navigateTo('/pages/admin/reviewer-management')">
            <view class="action-icon reviewer-icon">👨‍💼</view>
            <text class="action-text">审核员管理</text>
          </view>
          
          <view class="action-item" @click="navigateTo('/pages/admin/statistics')">
            <view class="action-icon stats-icon">📈</view>
            <text class="action-text">数据统计</text>
          </view>
          
          <view class="action-item" @click="navigateTo('/pages/admin/system-settings')">
            <view class="action-icon settings-icon">⚙️</view>
            <text class="action-text">系统设置</text>
          </view>
        </view>
      </view>
      
      <!-- 最新动态 -->
      <view class="recent-activities">
        <view class="section-header">
          <text class="section-title">最新动态</text>
          <text class="view-more">查看全部</text>
        </view>
        
        <view class="activity-list">
          <view v-for="activity in recentActivities" :key="activity.id" class="activity-item">
            <view class="activity-icon" :class="activity.type">{{ activity.icon }}</view>
            <view class="activity-content">
              <text class="activity-title">{{ activity.title }}</text>
              <text class="activity-desc">{{ activity.description }}</text>
              <text class="activity-time">{{ activity.time }}</text>
            </view>
            <view class="activity-action">
              <button class="action-btn" @click="handleActivity(activity)">
                处理
              </button>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 系统状态 -->
      <view class="system-status">
        <text class="section-title">系统状态</text>
        <view class="status-grid">
          <view class="status-item">
            <view class="status-indicator online"></view>
            <text class="status-text">服务运行正常</text>
          </view>
          <view class="status-item">
            <view class="status-indicator online"></view>
            <text class="status-text">数据库连接正常</text>
          </view>
          <view class="status-item">
            <view class="status-indicator warning"></view>
            <text class="status-text">存储空间使用75%</text>
          </view>
          <view class="status-item">
            <view class="status-indicator online"></view>
            <text class="status-text">API响应正常</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import Sidebar from '@/components/Sidebar.vue'

export default {
  name: 'AdminDashboard',
  components: {
    Sidebar
  },
  
  data() {
    return {
      showSidebar: true,
      statistics: {
        totalUsers: 1248,
        totalItems: 562,
        recoveryRate: 78,
        pendingReviews: 23
      },
      recentActivities: [
        {
          id: 1,
          type: 'user',
          icon: '👤',
          title: '新用户注册',
          description: '用户张三完成注册，需要审核',
          time: '5分钟前'
        },
        {
          id: 2,
          type: 'review',
          icon: '📋',
          title: '失物信息待审核',
          description: '用户提交了新的失物信息，等待审核',
          time: '15分钟前'
        },
        {
          id: 3,
          type: 'system',
          icon: '⚠️',
          title: '系统警告',
          description: '服务器存储空间使用率超过70%',
          time: '1小时前'
        },
        {
          id: 4,
          type: 'success',
          icon: '🎉',
          title: '物品找回成功',
          description: '用户李四成功找回丢失的手机',
          time: '2小时前'
        }
      ]
    }
  },
  
  onLoad() {
    this.loadDashboardData()
  },
  
  methods: {
    loadDashboardData() {
      // 实际项目中这里会调用API获取数据
      console.log('加载管理员控制台数据...')
    },
    
    navigateTo(url) {
      uni.navigateTo({ url })
    },
    
    handleActivity(activity) {
      switch (activity.type) {
        case 'user':
          uni.navigateTo({ url: '/pages/admin/user-management' })
          break
        case 'review':
          uni.navigateTo({ url: '/pages/reviewer/review-lost' })
          break
        case 'system':
          uni.navigateTo({ url: '/pages/admin/system-settings' })
          break
        default:
          uni.showToast({
            title: '功能开发中',
            icon: 'none'
          })
      }
    }
  }
}
</script>

<style scoped>
.admin-dashboard {
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

/* 统计卡片 */
.stats-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250rpx, 1fr));
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
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
}

.stat-card.primary { border-left: 4rpx solid #2196f3; }
.stat-card.success { border-left: 4rpx solid #4caf50; }
.stat-card.warning { border-left: 4rpx solid #ff9800; }
.stat-card.danger { border-left: 4rpx solid #f44336; }

.stat-icon {
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.stat-number {
  font-size: 40rpx;
  font-weight: 700;
  color: #333;
}

.stat-label {
  font-size: 24rpx;
  color: #666;
}

/* 图表区域 */
.charts-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400rpx, 1fr));
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.chart-card {
  background: white;
  padding: 30rpx;
  border-radius: 12rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.chart-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.chart-container {
  height: 300rpx;
}

.chart-placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  border-radius: 8rpx;
  border: 2rpx dashed #e0e0e0;
  gap: 10rpx;
}

.chart-placeholder text:first-child {
  font-size: 36rpx;
}

.chart-desc {
  font-size: 24rpx;
  color: #999;
}

/* 快速操作 */
.quick-actions {
  background: white;
  padding: 30rpx;
  border-radius: 12rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
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
  gap: 15rpx;
  padding: 30rpx;
  border-radius: 12rpx;
  transition: all 0.3s;
  cursor: pointer;
}

.action-item:hover {
  background: #f8f9fa;
  transform: translateY(-2rpx);
}

.action-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
}

.user-icon { background: #e3f2fd; }
.reviewer-icon { background: #fff3e0; }
.stats-icon { background: #e8f5e8; }
.settings-icon { background: #fce4ec; }

.action-text {
  font-size: 26rpx;
  color: #666;
  font-weight: 500;
}

/* 最新动态 */
.recent-activities {
  background: white;
  padding: 30rpx;
  border-radius: 12rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
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
  cursor: pointer;
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
  transition: background 0.3s;
}

.activity-item:hover {
  background: #f0f1f3;
}

.activity-icon {
  width: 50rpx;
  height: 50rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
}

.activity-icon.user { background: #e3f2fd; }
.activity-icon.review { background: #fff3e0; }
.activity-icon.system { background: #ffebee; }
.activity-icon.success { background: #e8f5e8; }

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

.activity-action {
  display: flex;
  align-items: center;
}

.action-btn {
  background: #2196f3;
  color: white;
  border: none;
  padding: 10rpx 20rpx;
  border-radius: 6rpx;
  font-size: 24rpx;
}

/* 系统状态 */
.system-status {
  background: white;
  padding: 30rpx;
  border-radius: 12rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200rpx, 1fr));
  gap: 20rpx;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 15rpx;
  padding: 20rpx;
  background: #f8f9fa;
  border-radius: 8rpx;
}

.status-indicator {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
}

.status-indicator.online {
  background: #4caf50;
  box-shadow: 0 0 10rpx rgba(76, 175, 80, 0.3);
}

.status-indicator.warning {
  background: #ff9800;
  box-shadow: 0 0 10rpx rgba(255, 152, 0, 0.3);
}

.status-text {
  font-size: 26rpx;
  color: #666;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
    padding: 20rpx;
  }
  
  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .charts-section {
    grid-template-columns: 1fr;
  }
  
  .action-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .status-grid {
    grid-template-columns: 1fr;
  }
}
</style>