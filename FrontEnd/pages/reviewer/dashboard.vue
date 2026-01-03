<template>
  <view class="reviewer-dashboard">
    <Sidebar />
    
    <view class="main-content" :class="{ 'main-content-expanded': !showSidebar }">
      <!-- 顶部统计 -->
      <view class="stats-header">
        <view class="stat-card pending">
          <view class="stat-content">
            <text class="stat-number">{{ reviewStats.pending }}</text>
            <text class="stat-label">待审核</text>
          </view>
        </view>
        
        <view class="stat-card approved">
          <view class="stat-content">
            <text class="stat-number">{{ reviewStats.approved }}</text>
            <text class="stat-label">已通过</text>
          </view>
        </view>
        
        <view class="stat-card rejected">
          <view class="stat-content">
            <text class="stat-number">{{ reviewStats.rejected }}</text>
            <text class="stat-label">已驳回</text>
          </view>
        </view>
        
        <view class="stat-card efficiency">
          <view class="stat-content">
            <text class="stat-number">{{ reviewStats.efficiency }}%</text>
            <text class="stat-label">通过率</text>
          </view>
        </view>
      </view>
      
      <!-- 审核队列 -->
      <view class="review-queue">
        <view class="section-header">
          <text class="section-title">待审核队列</text>
          <view class="filter-tabs">
            <button 
              v-for="tab in reviewTabs" 
              :key="tab.value"
              class="tab-btn"
              :class="{ 'active': activeTab === tab.value }"
              @click="switchTab(tab.value)"
            >
              {{ tab.label }}
              <text class="tab-badge">{{ tab.count }}</text>
            </button>
          </view>
        </view>
        
        <view class="review-list">
          <view v-if="loading" class="loading-state">
            <text>加载中...</text>
          </view>
          
          <view v-else-if="currentReviews.length === 0" class="empty-state">
            <text class="empty-text">暂无待审核信息</text>
          </view>
          
          <view v-else>
            <view 
              v-for="item in currentReviews" 
              :key="item.id" 
              class="review-item"
            >
              <view class="item-image" @click="previewImage(item.image)">
                <image :src="item.image" mode="aspectFill"></image>
              </view>
              
              <view class="item-content">
                <view class="item-header">
                  <text class="item-title">{{ item.itemName }}</text>
                  <view class="item-type" :class="item.type">
                    {{ item.type === 'lost' ? '失物' : '招领' }}
                  </view>
                </view>
                
                <text class="item-desc">{{ item.description }}</text>
                
                <view class="item-meta">
                  <text class="meta-item">{{ item.location }}</text>
                  <text class="meta-item">{{ item.time }}</text>
                  <text class="meta-item">{{ item.submitter }}</text>
                </view>
              </view>
              
              <view class="review-actions">
                <button v-if="item.status === 'pending'" class="action-btn approve" @click="handleApprove(item)">
                  通过
                </button>
                <button v-if="item.status === 'pending'" class="action-btn reject" @click="handleReject(item)">
                  驳回
                </button>
                <button class="action-btn detail" @click="viewDetail(item)">
                  详情
                </button>
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 审核统计 -->
      <view class="review-statistics">
        <text class="section-title">审核统计</text>
        
        <view class="stats-grid">
          <view class="stat-item">
            <text class="stat-label">今日审核</text>
            <text class="stat-value">{{ todayStats.todayReviewed }}</text>
          </view>
          
          <view class="stat-item">
            <text class="stat-label">本周审核量</text>
            <text class="stat-value">{{ todayStats.weeklyReviewed }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import Sidebar from '@/components/Sidebar.vue'
import { getReviewerDashboard, getPendingReviews, reviewItem } from '@/api/review'

export default {
  name: 'ReviewerDashboard',
  components: {
    Sidebar
  },
  
  data() {
    return {
      showSidebar: true,
      loading: false,
      activeTab: 'pending',
      
      reviewStats: {
        pending: 23,
        approved: 156,
        rejected: 12,
        efficiency: 93
      },
      
      todayStats: {
        todayReviewed: 18,
        avgTime: 5,
        weeklyReviewed: 89,
        accuracy: 96
      },
      
      reviewTabs: [
        { label: '全部', value: 'all', count: 0 },
        { label: '失物', value: 'lost', count: 0 },
        { label: '招领', value: 'found', count: 0 },
        { label: '待审核', value: 'pending', count: 0 }
      ],
      
      reviews: []
    }
  },
  
  computed: {
    currentReviews() {
      let filtered = [...this.reviews]
      
      if (this.activeTab === 'pending') {
        filtered = filtered.filter(item => item.status === 'pending')
      } else if (this.activeTab !== 'all') {
        filtered = filtered.filter(item => item.type === this.activeTab)
      }
      
      return filtered.slice(0, 10) // 只显示前10条
    }
  },
  
  onLoad() {
    this.loadReviewData()
  },
  
  methods: {
    async loadReviewData() {
      this.loading = true
      
      try {
        // 并行请求获取仪表板数据和待审核列表
        const [dashboardResponse, reviewsResponse] = await Promise.all([
          getReviewerDashboard(),
          getPendingReviews({ current: 1, pageSize: 20 })
        ])
        
        // 更新仪表板统计数据
        const stats = dashboardResponse.data
        this.reviewStats = {
          pending: stats.pending || 0,
          approved: stats.approved || 0,
          rejected: stats.rejected || 0,
          efficiency: stats.efficiency || 0
        }
        
        this.todayStats = {
          todayReviewed: stats.todayReviewed || 0,
          avgTime: stats.avgTime || 0,
          weeklyReviewed: stats.weeklyReviewed || 0,
          accuracy: stats.accuracy || 0
        }
        
        // 更新待审核列表
        this.reviews = (reviewsResponse.data.items || []).map(item => ({
          ...item,
          status: item.status || 'pending',
          time: this.formatTime(item.createdAt)
        }))
        this.updateTabCounts()
        
      } catch (error) {
        uni.showToast({
          title: '加载失败: ' + (error.message || '未知错误'),
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    
    updateTabCounts() {
      this.reviewTabs[0].count = this.reviews.length
      this.reviewTabs[1].count = this.reviews.filter(item => item.type === 'lost').length
      this.reviewTabs[2].count = this.reviews.filter(item => item.type === 'found').length
      this.reviewTabs[3].count = this.reviews.filter(item => item.status === 'pending').length
    },
    
    formatTime(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      const now = new Date()
      const diffMinutes = Math.floor((now - date) / (1000 * 60))
      
      if (diffMinutes < 1) return '刚刚'
      if (diffMinutes < 60) return `${diffMinutes}分钟前`
      if (diffMinutes < 1440) return `${Math.floor(diffMinutes / 60)}小时前`
      if (diffMinutes < 10080) return `${Math.floor(diffMinutes / 1440)}天前`
      
      return date.toLocaleDateString()
    },
    
    switchTab(tab) {
      this.activeTab = tab
    },
    
    async handleApprove(item) {
      uni.showModal({
        title: '确认通过',
        content: `确定要通过"${item.itemName}"的审核吗？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              // 调用API通过审核
              const response = await reviewItem(item.id, {
                status: 'approved',
                reason: '符合要求，审核通过',
                type: item.type
              })
              
              if (response.success) {
                item.status = 'approved'
                this.reviewStats.approved++
                this.reviewStats.pending--
                this.updateTabCounts()
                
                uni.showToast({
                  title: '审核通过',
                  icon: 'success'
                })
              } else {
                uni.showToast({
                  title: '操作失败: ' + (response.message || '未知错误'),
                  icon: 'none'
                })
              }
            } catch (error) {
              uni.showToast({
                title: '操作失败: ' + (error.message || '未知错误'),
                icon: 'none'
              })
            }
          }
        }
      })
    },
    
    async handleReject(item) {
      uni.showModal({
        title: '确认驳回',
        content: `确定要驳回"${item.itemName}"的审核吗？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              // 调用API驳回审核
              const response = await reviewItem(item.id, {
                status: 'rejected',
                reason: '不符合要求，审核驳回',
                type: item.type
              })
              
              if (response.success) {
                item.status = 'rejected'
                this.reviewStats.rejected++
                this.reviewStats.pending--
                this.updateTabCounts()
                
                uni.showToast({
                  title: '审核驳回',
                  icon: 'success'
                })
              } else {
                uni.showToast({
                  title: '操作失败: ' + (response.message || '未知错误'),
                  icon: 'none'
                })
              }
            } catch (error) {
              uni.showToast({
                title: '操作失败: ' + (error.message || '未知错误'),
                icon: 'none'
              })
            }
          }
        }
      })
    },
    
    viewDetail(item) {
      uni.navigateTo({ 
        url: `/pages/user/item-detail?id=${item.id}` 
      })
    },
    
    previewImage(image) {
      uni.previewImage({
        urls: [image],
        current: image
      })
    },
    
    navigateTo(url) {
      uni.navigateTo({ url })
    }
  }
}
</script>

<style scoped>
.reviewer-dashboard {
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

/* 统计头部 */
.stats-header {
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
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-4rpx);
}

.stat-card.pending { border-left: 4rpx solid #ff9800; }
.stat-card.approved { border-left: 4rpx solid #4caf50; }
.stat-card.rejected { border-left: 4rpx solid #f44336; }
.stat-card.efficiency { border-left: 4rpx solid #2196f3; }

.stat-icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.stat-number {
  font-size: 36rpx;
  font-weight: 700;
  color: #333;
}

.stat-label {
  font-size: 24rpx;
  color: #666;
}

/* 审核队列 */
.review-queue {
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

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.filter-tabs {
  display: flex;
  gap: 10rpx;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 20rpx;
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 20rpx;
  font-size: 24rpx;
  transition: all 0.3s;
}

.tab-btn.active {
  background: #2196f3;
  color: white;
}

.tab-badge {
  background: rgba(255, 255, 255, 0.3);
  padding: 2rpx 8rpx;
  border-radius: 10rpx;
  font-size: 20rpx;
  min-width: 20rpx;
  text-align: center;
}

.tab-btn.active .tab-badge {
  background: rgba(255, 255, 255, 0.3);
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx;
  text-align: center;
}

.empty-icon {
  font-size: 60rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #666;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.review-item {
  display: flex;
  gap: 20rpx;
  padding: 20rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  transition: background 0.3s;
}

.review-item:hover {
  background: #f0f1f3;
}

.item-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  overflow: hidden;
  flex-shrink: 0;
}

.item-image image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.item-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  flex: 1;
}

.item-type {
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  font-size: 20rpx;
  font-weight: 500;
}

.item-type.lost {
  background: #fff3e0;
  color: #ff9800;
}

.item-type.found {
  background: #e8f5e8;
  color: #4caf50;
}

.item-desc {
  font-size: 24rpx;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.item-meta {
  display: flex;
  gap: 20rpx;
}

.meta-item {
  font-size: 22rpx;
  color: #999;
}

.review-actions {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  align-items: stretch;
  min-width: 120rpx;
}

.action-btn {
  padding: 8rpx 16rpx;
  border: none;
  border-radius: 6rpx;
  font-size: 24rpx;
  text-align: center;
}

.action-btn.approve {
  background: #4caf50;
  color: white;
}

.action-btn.reject {
  background: #f44336;
  color: white;
}

.action-btn.detail {
  background: #f5f5f5;
  color: #666;
}

/* 审核统计 */
.review-statistics {
  background: white;
  padding: 30rpx;
  border-radius: 12rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150rpx, 1fr));
  gap: 20rpx;
}

.stat-item {
  text-align: center;
  padding: 20rpx;
  background: #f8f9fa;
  border-radius: 8rpx;
}

.stat-item .stat-label {
  display: block;
  font-size: 24rpx;
  color: #666;
  margin-bottom: 10rpx;
}

.stat-value {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #2196f3;
}

/* 快速操作 */
.quick-actions {
  background: white;
  padding: 30rpx;
  border-radius: 12rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.action-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx;
  border-radius: 12rpx;
  transition: background 0.3s;
  cursor: pointer;
}

.action-item:hover {
  background: #f8f9fa;
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

.lost-icon { background: #fff3e0; }
.found-icon { background: #e8f5e8; }
.stats-icon { background: #e3f2fd; }

.action-text {
  font-size: 28rpx;
  color: #666;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
    padding: 20rpx;
  }
  
  .stats-header {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .filter-tabs {
    flex-wrap: wrap;
  }
  
  .review-item {
    flex-direction: column;
  }
  
  .review-actions {
    flex-direction: row;
    justify-content: space-between;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>