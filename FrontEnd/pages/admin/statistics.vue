<template>
  <view class="statistics">
    <Sidebar />
    
    <view class="main-content" :class="{ 'main-content-expanded': !showSidebar }">
      <!-- 时间筛选 -->
      <view class="time-filter">
        <text class="filter-title">数据统计</text>
        <view class="filter-options">
          <picker 
            :range="timeRanges" 
            :value="timeRangeIndex"
            @change="onTimeRangeChange"
            class="time-picker"
          >
            <view class="picker-content">
              {{ timeRanges[timeRangeIndex] }}
            </view>
          </picker>
          
          <picker 
            mode="date" 
            :value="startDate"
            @change="onStartDateChange"
            class="date-picker"
          >
            <view class="picker-content">
              {{ startDate || '开始日期' }}
            </view>
          </picker>
          
          <picker 
            mode="date" 
            :value="endDate"
            @change="onEndDateChange"
            class="date-picker"
          >
            <view class="picker-content">
              {{ endDate || '结束日期' }}
            </view>
          </picker>
          
          <button class="refresh-btn" @click="refreshData">刷新数据</button>
        </view>
      </view>
      
      <!-- 核心指标 -->
      <view class="core-metrics">
        <view class="metric-card">
          <view class="metric-icon users-icon">👥</view>
          <view class="metric-content">
            <text class="metric-number">{{ coreMetrics.totalUsers }}</text>
            <text class="metric-label">总用户数</text>
            <text class="metric-trend positive">+{{ coreMetrics.userGrowth }}%</text>
          </view>
        </view>
        
        <view class="metric-card">
          <view class="metric-icon items-icon">📋</view>
          <view class="metric-content">
            <text class="metric-number">{{ coreMetrics.totalItems }}</text>
            <text class="metric-label">失物招领总数</text>
            <text class="metric-trend positive">+{{ coreMetrics.itemGrowth }}%</text>
          </view>
        </view>
        
        <view class="metric-card">
          <view class="metric-icon recovery-icon">🎯</view>
          <view class="metric-content">
            <text class="metric-number">{{ coreMetrics.recoveryRate }}%</text>
            <text class="metric-label">找回率</text>
            <text class="metric-trend" :class="coreMetrics.recoveryTrend > 0 ? 'positive' : 'negative'">
              {{ coreMetrics.recoveryTrend > 0 ? '+' : '' }}{{ coreMetrics.recoveryTrend }}%
            </text>
          </view>
        </view>
        
        <view class="metric-card">
          <view class="metric-icon activity-icon">⚡</view>
          <view class="metric-content">
            <text class="metric-number">{{ coreMetrics.activeUsers }}</text>
            <text class="metric-label">活跃用户</text>
            <text class="metric-trend positive">+{{ coreMetrics.activityGrowth }}%</text>
          </view>
        </view>
      </view>
      
      <!-- 图表区域 -->
      <view class="charts-grid">
        <view class="chart-card">
          <view class="chart-header">
            <text class="chart-title">失物招领趋势</text>
            <button class="chart-btn" @click="exportChart('trend')">导出</button>
          </view>
          <view class="chart-container">
            <view class="chart-placeholder">
              <text>📊 ECharts图表区域</text>
              <text class="chart-desc">显示失物招领数量随时间的变化趋势</text>
            </view>
          </view>
        </view>
        
        <view class="chart-card">
          <view class="chart-header">
            <text class="chart-title">物品类别分布</text>
            <button class="chart-btn" @click="exportChart('category')">导出</button>
          </view>
          <view class="chart-container">
            <view class="chart-placeholder">
              <text>🥧 ECharts图表区域</text>
              <text class="chart-desc">饼图显示不同类别物品的占比</text>
            </view>
          </view>
        </view>
        
        <view class="chart-card">
          <view class="chart-header">
            <text class="chart-title">高频丢失地点</text>
            <button class="chart-btn" @click="exportChart('location')">导出</button>
          </view>
          <view class="chart-container">
            <view class="chart-placeholder">
              <text>📊 ECharts图表区域</text>
              <text class="chart-desc">柱状图显示不同地点的丢失频率</text>
            </view>
          </view>
        </view>
        
        <view class="chart-card">
          <view class="chart-header">
            <text class="chart-title">找回效率分析</text>
            <button class="chart-btn" @click="exportChart('efficiency')">导出</button>
          </view>
          <view class="chart-container">
            <view class="chart-placeholder">
              <text>📈 ECharts图表区域</text>
              <text class="chart-desc">分析不同类别物品的找回效率</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 详细统计表格 -->
      <view class="stats-table">
        <view class="table-header">
          <text class="table-title">详细统计</text>
          <button class="export-btn" @click="exportStatistics">导出报表</button>
        </view>
        
        <view class="table-content">
          <view class="table-row table-head">
            <view class="table-cell">类别</view>
            <view class="table-cell">发布数量</view>
            <view class="table-cell">找回数量</view>
            <view class="table-cell">找回率</view>
            <view class="table-cell">平均找回时间</view>
          </view>
          
          <view 
            v-for="item in categoryStats" 
            :key="item.category" 
            class="table-row table-body"
          >
            <view class="table-cell category-name">{{ item.category }}</view>
            <view class="table-cell">{{ item.total }}</view>
            <view class="table-cell">{{ item.recovered }}</view>
            <view class="table-cell recovery-rate">{{ item.recoveryRate }}%</view>
            <view class="table-cell">{{ item.avgRecoveryTime }}天</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import Sidebar from '@/components/Sidebar.vue'

export default {
  name: 'Statistics',
  components: {
    Sidebar
  },
  
  data() {
    return {
      showSidebar: true,
      timeRangeIndex: 0,
      startDate: '',
      endDate: '',
      
      timeRanges: ['最近7天', '最近30天', '最近3个月', '最近6个月', '最近1年', '全部时间'],
      
      coreMetrics: {
        totalUsers: 1248,
        userGrowth: 12.5,
        totalItems: 562,
        itemGrowth: 8.3,
        recoveryRate: 78.5,
        recoveryTrend: 2.1,
        activeUsers: 386,
        activityGrowth: 15.2
      },
      
      categoryStats: [
        { category: '电子产品', total: 156, recovered: 98, recoveryRate: 62.8, avgRecoveryTime: 3.2 },
        { category: '钱包证件', total: 203, recovered: 189, recoveryRate: 93.1, avgRecoveryTime: 1.5 },
        { category: '书籍文具', total: 89, recovered: 76, recoveryRate: 85.4, avgRecoveryTime: 2.1 },
        { category: '生活用品', total: 67, recovered: 45, recoveryRate: 67.2, avgRecoveryTime: 4.3 },
        { category: '衣物饰品', total: 34, recovered: 28, recoveryRate: 82.4, avgRecoveryTime: 2.8 },
        { category: '体育用品', total: 13, recovered: 9, recoveryRate: 69.2, avgRecoveryTime: 3.7 }
      ]
    }
  },
  
  onLoad() {
    this.initDateRange()
    this.loadStatistics()
  },
  
  methods: {
    initDateRange() {
      // 默认选择最近30天
      const endDate = new Date()
      const startDate = new Date()
      startDate.setDate(startDate.getDate() - 30)
      
      this.endDate = this.formatDate(endDate)
      this.startDate = this.formatDate(startDate)
      this.timeRangeIndex = 1
    },
    
    formatDate(date) {
      return date.toISOString().split('T')[0]
    },
    
    onTimeRangeChange(e) {
      this.timeRangeIndex = e.detail.value
      this.updateDateRange()
      this.loadStatistics()
    },
    
    onStartDateChange(e) {
      this.startDate = e.detail.value
      this.loadStatistics()
    },
    
    onEndDateChange(e) {
      this.endDate = e.detail.value
      this.loadStatistics()
    },
    
    updateDateRange() {
      const endDate = new Date()
      const startDate = new Date()
      
      switch (this.timeRangeIndex) {
        case 0: // 最近7天
          startDate.setDate(startDate.getDate() - 7)
          break
        case 1: // 最近30天
          startDate.setDate(startDate.getDate() - 30)
          break
        case 2: // 最近3个月
          startDate.setMonth(startDate.getMonth() - 3)
          break
        case 3: // 最近6个月
          startDate.setMonth(startDate.getMonth() - 6)
          break
        case 4: // 最近1年
          startDate.setFullYear(startDate.getFullYear() - 1)
          break
        default:
          return
      }
      
      this.endDate = this.formatDate(endDate)
      this.startDate = this.formatDate(startDate)
    },
    
    async loadStatistics() {
      try {
        // 模拟API请求
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // 更新统计数据
        this.updateMetrics()
        
      } catch (error) {
        uni.showToast({
          title: '数据加载失败',
          icon: 'none'
        })
      }
    },
    
    updateMetrics() {
      // 模拟数据更新
      const randomGrowth = () => (Math.random() * 20 - 5).toFixed(1)
      
      this.coreMetrics = {
        ...this.coreMetrics,
        userGrowth: parseFloat(randomGrowth()),
        itemGrowth: parseFloat(randomGrowth()),
        recoveryTrend: parseFloat(randomGrowth()),
        activityGrowth: parseFloat(randomGrowth())
      }
    },
    
    refreshData() {
      uni.showLoading({ title: '刷新中...' })
      this.loadStatistics().then(() => {
        uni.hideLoading()
        uni.showToast({
          title: '数据已刷新',
          icon: 'success'
        })
      })
    },
    
    exportChart(chartType) {
      uni.showToast({
        title: `${chartType}图表导出功能开发中`,
        icon: 'none'
      })
    },
    
    exportStatistics() {
      uni.showToast({
        title: '报表导出功能开发中',
        icon: 'none'
      })
    }
  }
}
</script>

<style scoped>
.statistics {
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

/* 时间筛选 */
.time-filter {
  background: white;
  padding: 30rpx;
  border-radius: 12rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.filter-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.filter-options {
  display: flex;
  gap: 20rpx;
  align-items: center;
  flex-wrap: wrap;
}

.time-picker,
.date-picker {
  min-width: 200rpx;
  height: 60rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  background: #fafafa;
}

.picker-content {
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 20rpx;
  font-size: 26rpx;
  color: #333;
}

.refresh-btn {
  background: #2196f3;
  color: white;
  border: none;
  padding: 15rpx 30rpx;
  border-radius: 8rpx;
  font-size: 26rpx;
}

/* 核心指标 */
.core-metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250rpx, 1fr));
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.metric-card {
  background: white;
  padding: 30rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.metric-card:hover {
  transform: translateY(-4rpx);
}

.metric-icon {
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
}

.users-icon { background: #e3f2fd; }
.items-icon { background: #fff3e0; }
.recovery-icon { background: #e8f5e8; }
.activity-icon { background: #fce4ec; }

.metric-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.metric-number {
  font-size: 36rpx;
  font-weight: 700;
  color: #333;
}

.metric-label {
  font-size: 24rpx;
  color: #666;
}

.metric-trend {
  font-size: 20rpx;
  font-weight: 500;
}

.metric-trend.positive {
  color: #4caf50;
}

.metric-trend.negative {
  color: #f44336;
}

/* 图表区域 */
.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400rpx, 1fr));
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.chart-card {
  background: white;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  background: #f8f9fa;
  border-bottom: 1rpx solid #e0e0e0;
}

.chart-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.chart-btn {
  background: #f5f5f5;
  color: #666;
  border: none;
  padding: 10rpx 20rpx;
  border-radius: 6rpx;
  font-size: 22rpx;
}

.chart-container {
  height: 300rpx;
  padding: 20rpx;
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
  font-size: 22rpx;
  color: #999;
  text-align: center;
}

/* 统计表格 */
.stats-table {
  background: white;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  background: #f8f9fa;
  border-bottom: 1rpx solid #e0e0e0;
}

.table-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.export-btn {
  background: #4caf50;
  color: white;
  border: none;
  padding: 12rpx 24rpx;
  border-radius: 6rpx;
  font-size: 24rpx;
}

.table-content {
  overflow-x: auto;
}

.table-row {
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr 1fr 1fr;
  align-items: center;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.table-head {
  background: #fafafa;
  font-weight: 600;
  color: #333;
  font-size: 26rpx;
}

.table-body {
  transition: background 0.3s;
}

.table-body:hover {
  background: #f8f9fa;
}

.table-cell {
  font-size: 24rpx;
  color: #666;
  text-align: center;
}

.category-name {
  font-weight: 500;
  color: #333;
  text-align: left;
}

.recovery-rate {
  color: #4caf50;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
    padding: 20rpx;
  }
  
  .filter-options {
    flex-direction: column;
    align-items: stretch;
  }
  
  .core-metrics {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .table-row {
    grid-template-columns: 1fr 1fr;
    gap: 10rpx;
  }
  
  .table-cell {
    text-align: left;
  }
  
  .table-row.table-head {
    grid-template-columns: repeat(5, 1fr);
  }
}
</style>