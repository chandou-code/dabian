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
            <!-- ECharts图表容器 -->
            <view id="trend-chart" class="chart" ref="trendChart"></view>
          </view>
        </view>
        
        <view class="chart-card">
          <text class="chart-title">高频丢失物品分析</text>
          <view class="chart-container">
            <!-- ECharts图表容器 -->
            <view id="category-chart" class="chart" ref="categoryChart"></view>
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
import { getAdminDashboard, getAdminDashboardActivities, getAdminTrendData, getAdminCategoryData } from '@/api/system'

export default {
  name: 'AdminDashboard',
  components: {
    Sidebar
  },
  
  data() {
    return {
      showSidebar: true,
      statistics: {
        totalUsers: 0,
        totalItems: 0,
        recoveryRate: 0,
        pendingReviews: 0
      },
      recentActivities: [],
      // 图表数据
      trendData: {
        labels: [],
        lostData: [],
        foundData: []
      },
      categoryData: {
        categories: [],
        counts: []
      },
      // 图表实例
      trendChart: null,
      categoryChart: null
    }
  },
  
  onLoad() {
    this.loadDashboardData()
  },
  
  onShow() {
    // 页面显示时重新初始化图表，确保DOM元素已渲染
    this.$nextTick(() => {
      this.initTrendChart()
      this.initCategoryChart()
    })
  },
  
  methods: {
    async loadDashboardData() {
      try {
        console.log('加载管理员控制台数据...')
        // 并行请求仪表板数据、最新动态和图表数据
        const [dashboardResponse, activitiesResponse, trendResponse, categoryResponse] = await Promise.all([
          getAdminDashboard(),
          getAdminDashboardActivities(),
          getAdminTrendData(),
          getAdminCategoryData()
        ])
        
        // 更新统计数据
        if (dashboardResponse.success && dashboardResponse.data) {
          console.log('获取到的仪表板数据:', dashboardResponse.data)
          this.statistics = {
            totalUsers: dashboardResponse.data.totalUsers || 0,
            totalItems: dashboardResponse.data.totalItems || 0,
            recoveryRate: dashboardResponse.data.recoveryRate || 0,
            pendingReviews: dashboardResponse.data.pendingReviews || 0
          }
        }
        
        // 更新最新动态
        if (activitiesResponse.success && activitiesResponse.data) {
          console.log('获取到的最新动态:', activitiesResponse.data)
          this.recentActivities = activitiesResponse.data
        }
        
        // 更新趋势图表数据
        if (trendResponse.success && trendResponse.data) {
          console.log('获取到的趋势数据:', trendResponse.data)
          // 处理后端返回的趋势数据格式
          const trendData = trendResponse.data.trendData || []
          this.trendData = {
            labels: trendData.map(item => item.date),
            lostData: trendData.map(item => item.lostCount),
            foundData: trendData.map(item => item.foundCount)
          }
          this.initTrendChart()
        }
        
        // 更新类别图表数据
        if (categoryResponse.success && categoryResponse.data) {
          console.log('获取到的类别数据:', categoryResponse.data)
          // 处理后端返回的类别数据格式
          const categories = categoryResponse.data.categories || []
          this.categoryData = {
            categories: categories.map(item => item.category),
            counts: categories.map(item => item.total)
          }
          this.initCategoryChart()
        }
      } catch (error) {
        console.error('加载管理员控制台数据失败:', error)
        uni.showToast({
          title: '加载数据失败',
          icon: 'none'
        })
      }
    },
    
    // 初始化趋势图表
    initTrendChart() {
      console.log('初始化趋势图表:', this.trendData)
      
      // 获取Canvas上下文
      const chartElement = document.getElementById('trend-chart')
      if (!chartElement) return
      
      // 创建Canvas元素
      let canvas = chartElement.querySelector('canvas')
      if (!canvas) {
        canvas = document.createElement('canvas')
        chartElement.innerHTML = ''
        chartElement.appendChild(canvas)
      }
      
      // 确保Canvas尺寸正确，使用设备像素比
      const dpr = window.devicePixelRatio || 1
      const rect = chartElement.getBoundingClientRect()
      
      // 设置Canvas的实际像素大小
      canvas.width = rect.width * dpr
      canvas.height = rect.height * dpr
      
      // 设置Canvas的CSS大小
      canvas.style.width = rect.width + 'px'
      canvas.style.height = rect.height + 'px'
      
      const ctx = canvas.getContext('2d')
      if (!ctx) return
      
      // 清空画布
      ctx.clearRect(0, 0, canvas.width, canvas.height)
      
      // 缩放上下文以匹配设备像素比
      ctx.scale(dpr, dpr)
      
      // 绘制趋势图
      this.drawTrendChart(ctx, rect.width, rect.height)
    },
    
    // 绘制趋势图
    drawTrendChart(ctx, width, height) {
      const { labels, lostData, foundData } = this.trendData
      if (labels.length === 0) return
      
      const padding = 40
      const chartWidth = width - 2 * padding
      const chartHeight = height - 2 * padding
      
      // 计算数据的最大值
      const maxData = Math.max(...lostData, ...foundData)
      
      // 绘制坐标轴
      ctx.beginPath()
      ctx.moveTo(padding, padding)
      ctx.lineTo(padding, height - padding)
      ctx.lineTo(width - padding, height - padding)
      ctx.strokeStyle = '#ccc'
      ctx.stroke()
      
      // 绘制失物数据折线
      ctx.beginPath()
      for (let i = 0; i < labels.length; i++) {
        const x = padding + (i / (labels.length - 1)) * chartWidth
        const y = height - padding - (lostData[i] / maxData) * chartHeight
        if (i === 0) {
          ctx.moveTo(x, y)
        } else {
          ctx.lineTo(x, y)
        }
      }
      ctx.strokeStyle = '#f44336'
      ctx.lineWidth = 2
      ctx.stroke()
      
      // 绘制招领数据折线
      ctx.beginPath()
      for (let i = 0; i < labels.length; i++) {
        const x = padding + (i / (labels.length - 1)) * chartWidth
        const y = height - padding - (foundData[i] / maxData) * chartHeight
        if (i === 0) {
          ctx.moveTo(x, y)
        } else {
          ctx.lineTo(x, y)
        }
      }
      ctx.strokeStyle = '#4caf50'
      ctx.lineWidth = 2
      ctx.stroke()
      
      // 绘制图例
      ctx.fillStyle = '#f44336'
      ctx.fillRect(padding, padding - 20, 10, 10)
      ctx.fillStyle = '#333'
      ctx.font = '12px sans-serif'
      ctx.fillText('失物', padding + 15, padding - 8)
      
      ctx.fillStyle = '#4caf50'
      ctx.fillRect(padding + 80, padding - 20, 10, 10)
      ctx.fillStyle = '#333'
      ctx.font = '12px sans-serif'
      ctx.fillText('招领', padding + 95, padding - 8)
    },
    
    // 初始化类别图表
    initCategoryChart() {
      console.log('初始化类别图表:', this.categoryData)
      
      // 获取Canvas上下文
      const chartElement = document.getElementById('category-chart')
      if (!chartElement) return
      
      // 创建Canvas元素
      let canvas = chartElement.querySelector('canvas')
      if (!canvas) {
        canvas = document.createElement('canvas')
        chartElement.innerHTML = ''
        chartElement.appendChild(canvas)
      }
      
      // 确保Canvas尺寸正确，使用设备像素比
      const dpr = window.devicePixelRatio || 1
      const rect = chartElement.getBoundingClientRect()
      
      // 设置Canvas的实际像素大小
      canvas.width = rect.width * dpr
      canvas.height = rect.height * dpr
      
      // 设置Canvas的CSS大小
      canvas.style.width = rect.width + 'px'
      canvas.style.height = rect.height + 'px'
      
      const ctx = canvas.getContext('2d')
      if (!ctx) return
      
      // 清空画布
      ctx.clearRect(0, 0, canvas.width, canvas.height)
      
      // 缩放上下文以匹配设备像素比
      ctx.scale(dpr, dpr)
      
      // 绘制类别图
      this.drawCategoryChart(ctx, rect.width, rect.height)
    },
    
    // 绘制类别图
    drawCategoryChart(ctx, width, height) {
      const { categories, counts } = this.categoryData
      if (categories.length === 0) return
      
      // 调整半径大小，避免超出容器
      const centerX = width / 2
      const centerY = height / 2
      const radius = Math.min(width, height) / 4 // 减小半径，避免溢出
      
      // 计算总和
      const total = counts.reduce((sum, count) => sum + count, 0)
      if (total === 0) return
      
      // 绘制饼图
      let startAngle = -Math.PI / 2
      for (let i = 0; i < categories.length; i++) {
        const count = counts[i]
        const percentage = count / total
        const endAngle = startAngle + 2 * Math.PI * percentage
        
        // 绘制扇形
        ctx.beginPath()
        ctx.moveTo(centerX, centerY)
        ctx.arc(centerX, centerY, radius, startAngle, endAngle)
        ctx.closePath()
        
        // 随机颜色
        const color = `hsl(${i * 360 / categories.length}, 70%, 60%)`
        ctx.fillStyle = color
        ctx.fill()
        ctx.strokeStyle = '#fff'
        ctx.lineWidth = 2
        ctx.stroke()
        
        // 绘制图例
        const legendX = 20
        const legendY = 20 + i * 25
        ctx.fillStyle = color
        ctx.fillRect(legendX, legendY, 15, 15)
        ctx.fillStyle = '#333'
        ctx.font = '12px sans-serif'
        ctx.fillText(`${categories[i]}: ${count}`, legendX + 25, legendY + 12)
        
        startAngle = endAngle
      }
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

.chart {
  height: 100%;
  width: 100%;
  background: #f8f9fa;
  border-radius: 8rpx;
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