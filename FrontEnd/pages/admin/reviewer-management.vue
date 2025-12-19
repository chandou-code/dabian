<template>
  <view class="reviewer-management">
    <Sidebar />
    
    <view class="main-content" :class="{ 'main-content-expanded': !showSidebar }">
      <!-- 操作栏 -->
      <view class="action-bar">
        <view class="search-section">
          <input 
            v-model="searchKeyword" 
            class="search-input" 
            placeholder="搜索审核员姓名或用户名..."
            @confirm="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">🔍</button>
        </view>
        
        <button class="add-btn" @click="showAddReviewerModal">添加审核员</button>
      </view>
      
      <!-- 审核员列表 -->
      <view class="reviewers-grid">
        <view v-if="loading" class="loading-state">
          <text>加载中...</text>
        </view>
        
        <view v-else-if="filteredReviewers.length === 0" class="empty-state">
          <text class="empty-icon">👨‍💼</text>
          <text class="empty-text">暂无审核员数据</text>
        </view>
        
        <view v-else class="reviewer-cards">
          <view 
            v-for="reviewer in paginatedReviewers" 
            :key="reviewer.id" 
            class="reviewer-card"
          >
            <view class="card-header">
              <image class="reviewer-avatar" :src="reviewer.avatar" mode="aspectFill"></image>
              <view class="reviewer-info">
                <text class="reviewer-name">{{ reviewer.name }}</text>
                <text class="reviewer-username">@{{ reviewer.username }}</text>
              </view>
              <view class="status-badge" :class="reviewer.status">
                {{ getStatusText(reviewer.status) }}
              </view>
            </view>
            
            <view class="card-content">
              <view class="info-row">
                <text class="info-label">邮箱：</text>
                <text class="info-value">{{ reviewer.email }}</text>
              </view>
              <view class="info-row">
                <text class="info-label">手机：</text>
                <text class="info-value">{{ reviewer.phone }}</text>
              </view>
              <view class="info-row">
                <text class="info-label">入职时间：</text>
                <text class="info-value">{{ reviewer.joinTime }}</text>
              </view>
            </view>
            
            <view class="stats-section">
              <text class="stats-title">工作统计</text>
              <view class="stats-grid">
                <view class="stat-item">
                  <text class="stat-number">{{ reviewer.totalReviewed }}</text>
                  <text class="stat-label">总审核数</text>
                </view>
                <view class="stat-item">
                  <text class="stat-number">{{ reviewer.approved }}</text>
                  <text class="stat-label">通过数</text>
                </view>
                <view class="stat-item">
                  <text class="stat-number">{{ reviewer.rejected }}</text>
                  <text class="stat-label">驳回数</text>
                </view>
                <view class="stat-item">
                  <text class="stat-number">{{ reviewer.approvalRate }}%</text>
                  <text class="stat-label">通过率</text>
                </view>
              </view>
            </view>
            
            <view class="card-actions">
              <button class="action-btn edit-btn" @click="editReviewer(reviewer)">编辑</button>
              <button class="action-btn toggle-btn" @click="toggleReviewerStatus(reviewer)">
                {{ reviewer.status === 'active' ? '禁用' : '启用' }}
              </button>
              <button class="action-btn view-btn" @click="viewReviewerDetail(reviewer)">详情</button>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 分页 -->
      <view v-if="totalPages > 1" class="pagination">
        <button 
          class="page-btn" 
          :disabled="currentPage === 1"
          @click="changePage(currentPage - 1)"
        >
          上一页
        </button>
        
        <view class="page-numbers">
          <button 
            v-for="page in visiblePages" 
            :key="page"
            class="page-number"
            :class="{ 'active': page === currentPage }"
            @click="changePage(page)"
          >
            {{ page }}
          </button>
        </view>
        
        <button 
          class="page-btn" 
          :disabled="currentPage === totalPages"
          @click="changePage(currentPage + 1)"
        >
          下一页
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import Sidebar from '@/components/Sidebar.vue'

export default {
  name: 'ReviewerManagement',
  components: {
    Sidebar
  },
  
  data() {
    return {
      showSidebar: true,
      loading: false,
      searchKeyword: '',
      currentPage: 1,
      pageSize: 6,
      reviewers: []
    }
  },
  
  computed: {
    filteredReviewers() {
      if (!this.searchKeyword) {
        return this.reviewers
      }
      
      const keyword = this.searchKeyword.toLowerCase()
      return this.reviewers.filter(reviewer => 
        reviewer.name.toLowerCase().includes(keyword) ||
        reviewer.username.toLowerCase().includes(keyword) ||
        reviewer.email.toLowerCase().includes(keyword)
      )
    },
    
    totalPages() {
      return Math.ceil(this.filteredReviewers.length / this.pageSize)
    },
    
    paginatedReviewers() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredReviewers.slice(start, end)
    },
    
    visiblePages() {
      const pages = []
      const maxVisible = 5
      let start = Math.max(1, this.currentPage - Math.floor(maxVisible / 2))
      let end = Math.min(this.totalPages, start + maxVisible - 1)
      
      if (end - start < maxVisible - 1) {
        start = Math.max(1, end - maxVisible + 1)
      }
      
      for (let i = start; i <= end; i++) {
        pages.push(i)
      }
      
      return pages
    }
  },
  
  onLoad() {
    this.loadReviewers()
  },
  
  methods: {
    async loadReviewers() {
      this.loading = true
      
      try {
        // 模拟API请求
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        this.reviewers = this.generateMockReviewers()
        
      } catch (error) {
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    
    generateMockReviewers() {
      const statuses = ['active', 'inactive']
      const names = ['张审核', '李审核', '王审核', '刘审核', '陈审核', '杨审核', '赵审核', '钱审核']
      const mockReviewers = []
      
      for (let i = 1; i <= 15; i++) {
        const status = statuses[Math.floor(Math.random() * statuses.length)]
        const totalReviewed = Math.floor(Math.random() * 200) + 50
        const approved = Math.floor(totalReviewed * (Math.random() * 0.3 + 0.7)) // 70%-100%通过率
        const rejected = totalReviewed - approved
        
        mockReviewers.push({
          id: i,
          name: names[i % names.length] + i,
          username: `reviewer${i}`,
          email: `reviewer${i}@example.com`,
          phone: `139${String(i).padStart(8, '0')}`,
          status,
          avatar: '/static/default-avatar.png',
          joinTime: `${Math.floor(Math.random() * 365) + 1}天前`,
          totalReviewed,
          approved,
          rejected,
          approvalRate: Math.round((approved / totalReviewed) * 100)
        })
      }
      
      return mockReviewers
    },
    
    handleSearch() {
      this.currentPage = 1
    },
    
    changePage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page
      }
    },
    
    getStatusText(status) {
      const statusMap = {
        active: '正常',
        inactive: '已禁用'
      }
      return statusMap[status] || '未知'
    },
    
    showAddReviewerModal() {
      uni.showToast({
        title: '功能开发中',
        icon: 'none'
      })
    },
    
    editReviewer(reviewer) {
      uni.showModal({
        title: '编辑审核员',
        content: `编辑审核员：${reviewer.name}`,
        showCancel: false
      })
    },
    
    toggleReviewerStatus(reviewer) {
      const action = reviewer.status === 'active' ? '禁用' : '启用'
      uni.showModal({
        title: `确认${action}`,
        content: `确定要${action}审核员 ${reviewer.name} 吗？`,
        success: (res) => {
          if (res.confirm) {
            reviewer.status = reviewer.status === 'active' ? 'inactive' : 'active'
            uni.showToast({
              title: `${action}成功`,
              icon: 'success'
            })
          }
        }
      })
    },
    
    viewReviewerDetail(reviewer) {
      uni.navigateTo({ 
        url: `/pages/admin/reviewer-detail?id=${reviewer.id}` 
      })
    }
  }
}
</script>

<style scoped>
.reviewer-management {
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

/* 操作栏 */
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
  gap: 20rpx;
  flex-wrap: wrap;
}

.search-section {
  display: flex;
  align-items: center;
  gap: 20rpx;
  flex: 1;
  max-width: 400rpx;
}

.search-input {
  flex: 1;
  height: 60rpx;
  padding: 0 20rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 26rpx;
}

.search-btn {
  width: 60rpx;
  height: 60rpx;
  background: #2196f3;
  color: white;
  border: none;
  border-radius: 8rpx;
  font-size: 26rpx;
}

.add-btn {
  background: #4caf50;
  color: white;
  border: none;
  padding: 15rpx 30rpx;
  border-radius: 8rpx;
  font-size: 26rpx;
}

/* 审核员网格 */
.reviewers-grid {
  min-height: 400rpx;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx;
  background: white;
  border-radius: 12rpx;
  text-align: center;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #666;
}

.reviewer-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400rpx, 1fr));
  gap: 30rpx;
}

.reviewer-card {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
}

.reviewer-card:hover {
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
}

/* 卡片头部 */
.card-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 25rpx;
}

.reviewer-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #f0f0f0;
}

.reviewer-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.reviewer-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.reviewer-username {
  font-size: 24rpx;
  color: #999;
}

.status-badge {
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: 500;
  color: white;
}

.status-badge.active { background: #4caf50; }
.status-badge.inactive { background: #999; }

/* 卡片内容 */
.card-content {
  margin-bottom: 25rpx;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 10rpx;
}

.info-label {
  font-size: 24rpx;
  color: #666;
  min-width: 120rpx;
}

.info-value {
  font-size: 24rpx;
  color: #333;
  flex: 1;
}

/* 统计区域 */
.stats-section {
  background: #f8f9fa;
  padding: 20rpx;
  border-radius: 12rpx;
  margin-bottom: 25rpx;
}

.stats-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 15rpx;
  display: block;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15rpx;
}

.stat-item {
  text-align: center;
  padding: 15rpx 10rpx;
  background: white;
  border-radius: 8rpx;
}

.stat-number {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #2196f3;
  margin-bottom: 5rpx;
}

.stat-label {
  font-size: 20rpx;
  color: #666;
}

/* 卡片操作 */
.card-actions {
  display: flex;
  gap: 15rpx;
}

.action-btn {
  flex: 1;
  height: 60rpx;
  border: none;
  border-radius: 8rpx;
  font-size: 24rpx;
  text-align: center;
}

.edit-btn {
  background: #2196f3;
  color: white;
}

.toggle-btn {
  background: #ff9800;
  color: white;
}

.view-btn {
  background: #f5f5f5;
  color: #666;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20rpx;
  margin-top: 40rpx;
  padding: 30rpx;
  background: white;
  border-radius: 12rpx;
}

.page-btn {
  height: 60rpx;
  padding: 0 24rpx;
  background: #f5f5f5;
  color: #666;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 24rpx;
}

.page-btn:disabled {
  opacity: 0.5;
}

.page-numbers {
  display: flex;
  gap: 10rpx;
}

.page-number {
  width: 60rpx;
  height: 60rpx;
  background: #f5f5f5;
  color: #666;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 24rpx;
}

.page-number.active {
  background: #2196f3;
  color: white;
  border-color: #2196f3;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
    padding: 20rpx;
  }
  
  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-section {
    max-width: none;
  }
  
  .reviewer-cards {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .card-actions {
    flex-direction: column;
  }
}
</style>