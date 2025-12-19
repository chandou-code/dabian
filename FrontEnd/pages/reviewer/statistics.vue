<template>
  <view class="reviewer-statistics">
    <Sidebar />
    
    <view class="main-content" :class="{ 'main-content-expanded': !showSidebar }">
      <!-- 时间筛选 -->
      <view class="time-filter">
        <text class="filter-title">审核统计</text>
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
        <view class="metric-card primary">
          <view class="metric-icon reviewed-icon">📋</view>
          <view class="metric-content">
            <text class="metric-number">{{ coreMetrics.totalReviewed }}</text>
            <text class="metric-label">总审核数</text>
            <text class="metric-trend positive">+{{ coreMetrics.todayReviewed }}</text>
          </view>
        </view>
        
        <view class="metric-card success">
          <view class="metric-icon approved-icon">✅</view>
          <view class="metric-content">
            <text class="metric-number">{{ coreMetrics.approved }}</text>
            <text class="metric-label">通过数</text>
            <text class="metric-trend positive">{{ coreMetrics.approvalRate }}%</text>
          </view>
        </view>
        
        <view class="metric-card warning">
          <view class="metric-icon rejected-icon">❌</view>
          <view class="metric-content">
            <text class="metric-number">{{ coreMetrics.rejected }}</text>
            <text class="metric-label">驳回数</text>
            <text class="metric-trend negative">{{ coreMetrics.rejectionRate }}%</text>
          </view>
        </view>
        
        <view class="metric-card info">
          <view class="metric-icon efficiency-icon">⏱️</view>
          <view class="metric-content">
            <text class="metric-number">{{ coreMetrics.avgTime }}分钟</text>
            <text class="metric-label">平均处理时间</text>
            <text class="metric-trend positive">-{{ coreMetrics.timeImprovement }}%</text>
          </view>
        </view>
      </view>
      
      <!-- 图表区域 -->
      <view class="charts-grid">
        <view class="chart-card">
          <view class="chart-header">
            <text class="chart-title">审核趋势</text>
            <button class="chart-btn" @click="exportChart('trend')">导出</button>
          </view>
          <view class="chart-container">
            <view class="chart-placeholder">
              <text>📈 ECharts图表区域</text>
              <text class="chart-desc">显示审核数量的日/周/月趋势变化</text>
            </view>
          </view>
        </view>
        
        <view class="chart-card">
          <view class="chart-header">
            <text class="chart-title">审核通过率</text>
            <button class="chart-btn" @click="exportChart('approval')">导出</button>
          </view>
          <view class="chart-container">
            <view class="chart-placeholder">
              <text>📊 ECharts图表区域</text>
              <text class="chart-desc">显示通过率的变化趋势</text>
            </view>
          </view>
        </view>
        
        <view class="chart-card">
          <view class="chart-header">
            <text class="chart-title">处理效率分析</text>
            <button class="chart-btn" @click="exportChart('efficiency')">导出</button>
          </view>
          <view class="chart-container">
            <view class="chart-placeholder">
              <text>⏰ ECharts图表区域</text>
              <text class="chart-desc">显示平均处理时间分布</text>
            </view>
          </view>
        </view>
        
        <view class="chart-card">
          <view class="chart-header">
            <text class="chart-title">工作量对比</text>
            <button class="chart-btn" @click="exportChart('workload')">导出</button>
          </view>
          <view class="chart-container">
            <view class="chart-placeholder">
              <text>👥 ECharts图表区域</text>
              <text class="chart-desc">与其他审核员的工作量对比</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 详细统计表格 -->
      <view class="stats-table">
        <view class="table-header">
          <text class="table-title">详细审核记录</text>
          <button class="export-btn" @click="exportStatistics">导出报表</button>
        </view>
        
        <view class="table-content">
          <view class="table-row table-head">
            <view class="table-cell">日期</view>
            <view class="table-cell">审核总数</view>
            <view class="table-cell">通过数</view>
            <view class="table-cell">驳回数</view>
            <view class="table-cell">通过率</view>
            <view class="table-cell">平均时间</view>
          </view>
          
          <view 
            v-for="record in dailyStats" 
            :key="record.date" 
            class="table-row table-body"
          >
            <view class="table-cell date-cell">{{ record.date }}</view>
            <view class="table-cell">{{ record.total }}</view>
            <view class="table-cell">{{ record.approved }}</view>
            <view class="table-cell">{{ record.rejected }}</view>
            <view class="table-cell approval-rate">{{ record.approvalRate }}%</view>
            <view class="table-cell">{{ record.avgTime }}分钟</view>
          </view>
        </view>
      </view>
      
      <!-- 业绩排名 -->
      <view class="ranking-section">
        <text class="section-title">业绩排名</text>
        
        <view class="ranking-cards">
          <view class="ranking-card">
            <text class="ranking-title">今日排名</text>
            <view class="ranking-item">
              <text class="rank-number">#{{ currentRanking.today }}</text>
              <text class="rank-desc">今日审核数：{{ currentRanking.todayCount }}</text>
            </view>
          </view>
          
          <view class="ranking-card">
            <text class="ranking-title">本周排名</text>
            <view class="ranking-item">
              <text class="rank-number">#{{ currentRanking.week }}</text>
              <text class="rank-desc">本周审核数：{{ currentRanking.weekCount }}</text>
            </view>
          </view>
          
          <view class="ranking-card">
            <text class="ranking-title">本月排名</text>
            <view class="ranking-item">
              <text class="rank-number">#{{ currentRanking.month }}</text>
              <text class="rank-desc">本月审核数：{{ currentRanking.monthCount }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import Sidebar from '@/components/Sidebar.vue'

export default {
  name: 'ReviewerStatistics',
  components: {
    Sidebar
  },
  
  data() {
    return {
      showSidebar: true,
      timeRangeIndex: 1,
      startDate: '',
      endDate: '',
      
      timeRanges: ['今天', '最近7天', '最近30天', '最近3个月', '全部时间'],
      
      coreMetrics: {
        totalReviewed: 156,
        todayReviewed: 12,
        approved: 142,
        approvalRate: 91.0,
        rejected: 14,
        rejectionRate: 9.0,
        avgTime: 3.5,
        timeImprovement: 15.2
      },
      
      dailyStats: [
        { date: '2024-01-15', total: 18, approved: 16, rejected: 2, approvalRate: 88.9, avgTime: 3.2 },
        { date: '2024-01-14', total: 22, approved: 20, rejected: 2, approvalRate: 90.9, avgTime: 3.8 },
        { date: '2024-01-13', total: 15, approved: 14, rejected: 1, approvalRate: 93.3, avgTime: 4.1 },
        { date: '2024-01-12', total: 25, approved: 23, rejected: 2, approvalRate: 92.0, avgTime: 3.5 },
        { date: '2024-01-11', total: 20, approved: 18, rejected: 2, approvalRate: 90.0, avgTime: 3.9 },
        { date: '2024-01-10', total: 19, approved: 17, rejected: 2, approvalRate: 89.5, avgTime: 3.6 },
        { date: '2024-01-09', total: 21, approved: 19, rejected: 2, approvalRate: 90.5, avgTime: 3.3 }
      ],
      
      currentRanking: {
        today: 2,
        todayCount: 12,
        week: 3,
        weekCount: 89,
        month: 4,
        monthCount: 156
      }
    }
  },
  
  onLoad() {
    this.initDateRange()
    this.loadStatistics()
  },
  
  methods: {
    initDateRange() {
      // 默认选择最近7天
      const endDate = new Date()
      const startDate = new Date()
      startDate.setDate(startDate.getDate() - 7)
      
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
        case 0: // 今天
          startDate.setDate(startDate.getDate() - 1)
          break
        case 1: // 最近7天
          startDate.setDate(startDate.getDate() - 7)
          break
        case 2: // 最近30天
          startDate.setDate(startDate.getDate() - 30)
          break
        case 3: // 最近3个月
          startDate.setMonth(startDate.getMonth() - 3)
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
      const randomGrowth = () => (Math.random() * 10 - 5).toFixed(1)
      
      this.coreMetrics = {
        ...this.coreMetrics,
        todayReviewed: Math.floor(Math.random() * 20) + 5,
        approvalRate: parseFloat((Math.random() * 20 + 80).toFixed(1)),
        rejectionRate: parseFloat((Math.random() * 20 + 5).toFixed(1)),
        avgTime: parseFloat((Math.random() * 5 + 2).toFixed(1)),
        timeImprovement: parseFloat(Math.abs(randomGrowth()))
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
.reviewer-statistics {
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

.metric-card.primary { border-left: 4rpx solid #2196f3; }
.metric-card.success { border-left: 4rpx solid #4caf50; }
.metric-card.warning { border-left: 4rpx solid #ff9800; }
.metric-card.info { border-left: 4rpx solid #00bcd4; }

.metric-icon {
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
}

.reviewed-icon { background: #e3f2fd; }
.approved-icon { background: #e8f5e8; }
.rejected-icon { background: #ffebee; }
.efficiency-icon { background: #e0f7fa; }

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
  margin-bottom: 30rpx;
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
  grid-template-columns: 1.2fr 1fr 1fr 1fr 1fr 1fr;
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

.date-cell {
  font-weight: 500;
  color: #333;
  text-align: left;
}

.approval-rate {
  color: #4caf50;
  font-weight: 500;
}

/* 业绩排名 */
.ranking-section {
  background: white;
  padding: 30rpx;
  border-radius: 12rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 25rpx;
  display: block;
}

.ranking-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250rpx, 1fr));
  gap: 20rpx;
}

.ranking-card {
  background: #f8f9fa;
  padding: 25rpx;
  border-radius: 12rpx;
  text-align: center;
  transition: transform 0.3s;
}

.ranking-card:hover {
  transform: translateY(-4rpx);
}

.ranking-title {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 15rpx;
  display: block;
}

.ranking-item {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.rank-number {
  font-size: 48rpx;
  font-weight: 700;
  color: #2196f3;
}

.rank-desc {
  font-size: 22rpx;
  color: #999;
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
    grid-template-columns: repeat(6, 1fr);
  }
  
  .ranking-cards {
    grid-template-columns: 1fr;
  }
}
</style>