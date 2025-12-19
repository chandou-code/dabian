<template>
  <view class="review-found">
    <Sidebar />
    
    <view class="main-content" :class="{ 'main-content-expanded': !showSidebar }">
      <!-- 操作栏 -->
      <view class="action-bar">
        <view class="search-section">
          <input 
            v-model="searchKeyword" 
            class="search-input" 
            placeholder="搜索物品名称或描述..."
            @confirm="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">🔍</button>
        </view>
        
        <view class="filter-section">
          <picker 
            :range="statusFilters" 
            :value="statusIndex"
            @change="onStatusChange"
            class="filter-picker"
          >
            <view class="picker-content">
              {{ statusFilters[statusIndex] }}
            </view>
          </picker>
          
          <picker 
            :range="timeFilters" 
            :value="timeIndex"
            @change="onTimeChange"
            class="filter-picker"
          >
            <view class="picker-content">
              {{ timeFilters[timeIndex] }}
            </view>
          </picker>
        </view>
      </view>
      
      <!-- 审核列表 -->
      <view class="review-list">
        <view class="list-header">
          <text class="list-title">招领审核 ({{ filteredItems.length }})</text>
          <view class="batch-actions" v-if="selectedItems.length > 0">
            <text class="selected-count">已选择 {{ selectedItems.length }} 项</text>
            <button class="batch-btn approve" @click="batchApprove">批量通过</button>
            <button class="batch-btn reject" @click="batchReject">批量驳回</button>
          </view>
        </view>
        
        <view v-if="loading" class="loading-state">
          <text>加载中...</text>
        </view>
        
        <view v-else-if="filteredItems.length === 0" class="empty-state">
          <text class="empty-icon">✅</text>
          <text class="empty-text">暂无待审核的招领信息</text>
        </view>
        
        <view v-else class="review-items">
          <view 
            v-for="item in paginatedItems" 
            :key="item.id" 
            class="review-item"
            :class="{ 'item-selected': selectedItems.includes(item.id) }"
          >
            <view class="item-header">
              <view class="select-section">
                <checkbox 
                  :value="item.id"
                  :checked="selectedItems.includes(item.id)"
                  @change="toggleSelection(item.id)"
                />
              </view>
              
              <view class="item-image" @click="previewImage(item.image)">
                <image :src="item.image || '/static/default-item.jpg'" mode="aspectFill"></image>
                <view class="status-badge" :class="item.status">
                  {{ getStatusText(item.status) }}
                </view>
              </view>
              
              <view class="item-content">
                <view class="content-header">
                  <text class="item-title">{{ item.title }}</text>
                  <text class="item-time">{{ item.submitTime }}</text>
                </view>
                
                <text class="item-desc">{{ item.description }}</text>
                
                <view class="item-info">
                  <text class="info-item">📍 {{ item.location }}</text>
                  <text class="info-item">📅 {{ item.foundTime }}</text>
                  <text class="info-item">👤 {{ item.finder }}</text>
                  <text class="info-item">📧 {{ item.contact }}</text>
                </view>
                
                <!-- 领取信息 -->
                <view v-if="item.pickupLocation" class="pickup-info">
                  <text class="pickup-label">领取地点：</text>
                  <text class="pickup-location">{{ item.pickupLocation }}</text>
                </view>
              </view>
              
              <view class="item-actions">
                <button class="action-btn approve-btn" @click="approveItem(item)">
                  ✅ 通过
                </button>
                <button class="action-btn reject-btn" @click="rejectItem(item)">
                  ❌ 驳回
                </button>
                <button class="action-btn detail-btn" @click="viewDetail(item)">
                  📋 详情
                </button>
              </view>
            </view>
            
            <!-- 匹配建议 -->
            <view v-if="item.matches && item.matches.length > 0" class="match-suggestions">
              <text class="match-title">匹配的失物信息</text>
              <view class="match-list">
                <view 
                  v-for="match in item.matches" 
                  :key="match.id" 
                  class="match-item"
                  @click="viewMatch(match)"
                >
                  <view class="match-info">
                    <text class="match-title">{{ match.title }}</text>
                    <text class="match-desc">{{ match.description }}</text>
                    <text class="match-time">{{ match.time }}</text>
                  </view>
                  <view class="match-score">
                    <text class="score-value">{{ match.score }}%</text>
                    <text class="score-label">匹配度</text>
                  </view>
                </view>
              </view>
            </view>
            
            <!-- 审核历史 -->
            <view v-if="item.reviewHistory && item.reviewHistory.length > 0" class="review-history">
              <text class="history-title">审核历史</text>
              <view class="history-list">
                <view 
                  v-for="history in item.reviewHistory" 
                  :key="history.id" 
                  class="history-item"
                >
                  <text class="history-action">{{ history.action }}</text>
                  <text class="history-reviewer">{{ history.reviewer }}</text>
                  <text class="history-time">{{ history.time }}</text>
                  <text class="history-comment">{{ history.comment }}</text>
                </view>
              </view>
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
  name: 'ReviewFound',
  components: {
    Sidebar
  },
  
  data() {
    return {
      showSidebar: true,
      loading: false,
      searchKeyword: '',
      selectedItems: [],
      currentPage: 1,
      pageSize: 5,
      statusIndex: 0,
      timeIndex: 0,
      
      statusFilters: ['全部状态', '待审核', '已通过', '已驳回'],
      timeFilters: ['全部时间', '今天', '最近3天', '最近7天', '最近30天'],
      
      reviewItems: []
    }
  },
  
  computed: {
    filteredItems() {
      let filtered = [...this.reviewItems]
      
      // 状态筛选
      if (this.statusIndex === 1) {
        filtered = filtered.filter(item => item.status === 'pending')
      } else if (this.statusIndex === 2) {
        filtered = filtered.filter(item => item.status === 'approved')
      } else if (this.statusIndex === 3) {
        filtered = filtered.filter(item => item.status === 'rejected')
      }
      
      // 关键词搜索
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        filtered = filtered.filter(item => 
          item.title.toLowerCase().includes(keyword) ||
          item.description.toLowerCase().includes(keyword) ||
          item.location.toLowerCase().includes(keyword)
        )
      }
      
      return filtered
    },
    
    totalPages() {
      return Math.ceil(this.filteredItems.length / this.pageSize)
    },
    
    paginatedItems() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredItems.slice(start, end)
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
    this.loadReviewItems()
  },
  
  methods: {
    async loadReviewItems() {
      this.loading = true
      
      try {
        // 模拟API请求
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        this.reviewItems = this.generateMockItems()
        
      } catch (error) {
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    
    generateMockItems() {
      const statuses = ['pending', 'approved', 'rejected']
      const finderNames = ['张三', '李四', '王五', '赵六', '钱七']
      const mockItems = []
      
      for (let i = 1; i <= 20; i++) {
        const status = statuses[Math.floor(Math.random() * statuses.length)]
        const item = {
          id: i,
          title: `捡到的物品 ${i}`,
          description: '详细描述捡到物品的特征、状态、位置等信息...',
          location: `图书馆${Math.floor(Math.random() * 3) + 1}楼`,
          foundTime: `${Math.floor(Math.random() * 7) + 1}天前`,
          finder: finderNames[i % finderNames.length],
          contact: `139${String(i).padStart(8, '0')}`,
          submitTime: `${Math.floor(Math.random() * 24)}小时前`,
          status,
          image: Math.random() > 0.3 ? '/static/found-item.jpg' : null,
          pickupLocation: Math.random() > 0.5 ? `学生事务中心${Math.floor(Math.random() * 3) + 1}号窗口` : null,
          reviewHistory: [],
          matches: []
        }
        
        // 添加审核历史
        if (status !== 'pending') {
          item.reviewHistory = [{
            id: 1,
            action: status === 'approved' ? '通过' : '驳回',
            reviewer: '审核员A',
            time: `${Math.floor(Math.random() * 12)}小时前`,
            comment: status === 'approved' ? '信息完整，符合要求' : '信息不完整，需要补充'
          }]
        }
        
        // 添加匹配建议（随机）
        if (Math.random() > 0.6) {
          item.matches = [{
            id: i * 100,
            title: `疑似匹配的失物 ${i}`,
            description: '丢失物品描述信息...',
            time: `${Math.floor(Math.random() * 10) + 1}天前`,
            score: Math.floor(Math.random() * 30) + 70 // 70-99%匹配度
          }]
        }
        
        mockItems.push(item)
      }
      
      return mockItems
    },
    
    handleSearch() {
      this.currentPage = 1
    },
    
    onStatusChange(e) {
      this.statusIndex = e.detail.value
      this.currentPage = 1
    },
    
    onTimeChange(e) {
      this.timeIndex = e.detail.value
      this.currentPage = 1
    },
    
    changePage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page
      }
    },
    
    toggleSelection(itemId) {
      const index = this.selectedItems.indexOf(itemId)
      if (index > -1) {
        this.selectedItems.splice(index, 1)
      } else {
        this.selectedItems.push(itemId)
      }
    },
    
    getStatusText(status) {
      const statusMap = {
        pending: '待审核',
        approved: '已通过',
        rejected: '已驳回'
      }
      return statusMap[status] || '未知'
    },
    
    async approveItem(item) {
      uni.showModal({
        title: '确认通过',
        content: `确定要通过"${item.title}"的审核吗？`,
        success: async (res) => {
          if (res.confirm) {
            await this.updateItemStatus(item, 'approved')
          }
        }
      })
    },
    
    async rejectItem(item) {
      uni.showModal({
        title: '确认驳回',
        content: `确定要驳回"${item.title}"的审核吗？`,
        editable: true,
        placeholderText: '请输入驳回理由',
        success: async (res) => {
          if (res.confirm) {
            await this.updateItemStatus(item, 'rejected', res.content || '信息不符合要求')
          }
        }
      })
    },
    
    async updateItemStatus(item, status, comment = '') {
      try {
        // 模拟API请求
        await new Promise(resolve => setTimeout(resolve, 500))
        
        item.status = status
        
        // 添加审核历史
        if (!item.reviewHistory) {
          item.reviewHistory = []
        }
        
        item.reviewHistory.unshift({
          id: Date.now(),
          action: status === 'approved' ? '通过' : '驳回',
          reviewer: '当前审核员',
          time: '刚刚',
          comment: comment || (status === 'approved' ? '信息完整，符合要求' : '信息不符合要求')
        })
        
        uni.showToast({
          title: status === 'approved' ? '审核通过' : '审核驳回',
          icon: 'success'
        })
        
      } catch (error) {
        uni.showToast({
          title: '操作失败',
          icon: 'none'
        })
      }
    },
    
    viewDetail(item) {
      uni.navigateTo({ 
        url: `/pages/reviewer/item-detail?id=${item.id}&type=found` 
      })
    },
    
    viewMatch(match) {
      uni.navigateTo({ 
        url: `/pages/user/item-detail?id=${match.id}&type=lost` 
      })
    },
    
    previewImage(image) {
      if (image) {
        uni.previewImage({
          urls: [image],
          current: image
        })
      }
    },
    
    async batchApprove() {
      uni.showModal({
        title: '批量通过',
        content: `确定要通过选中的 ${this.selectedItems.length} 项吗？`,
        success: async (res) => {
          if (res.confirm) {
            await this.batchUpdateStatus('approved')
          }
        }
      })
    },
    
    async batchReject() {
      uni.showModal({
        title: '批量驳回',
        content: `确定要驳回选中的 ${this.selectedItems.length} 项吗？`,
        success: async (res) => {
          if (res.confirm) {
            await this.batchUpdateStatus('rejected')
          }
        }
      })
    },
    
    async batchUpdateStatus(status) {
      try {
        // 模拟API请求
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        this.selectedItems.forEach(itemId => {
          const item = this.reviewItems.find(i => i.id === itemId)
          if (item) {
            item.status = status
          }
        })
        
        this.selectedItems = []
        
        uni.showToast({
          title: status === 'approved' ? '批量通过成功' : '批量驳回成功',
          icon: 'success'
        })
        
      } catch (error) {
        uni.showToast({
          title: '操作失败',
          icon: 'none'
        })
      }
    }
  }
}
</script>

<style scoped>
.review-found {
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

.filter-section {
  display: flex;
  gap: 15rpx;
}

.filter-picker {
  min-width: 150rpx;
  height: 60rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  background: white;
}

.picker-content {
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 20rpx;
  font-size: 26rpx;
  color: #333;
}

/* 审核列表 */
.review-list {
  background: white;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  background: #f8f9fa;
  border-bottom: 1rpx solid #e0e0e0;
}

.list-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.selected-count {
  font-size: 24rpx;
  color: #666;
}

.batch-btn {
  padding: 8rpx 16rpx;
  border: none;
  border-radius: 6rpx;
  font-size: 22rpx;
  color: white;
}

.batch-btn.approve {
  background: #4caf50;
}

.batch-btn.reject {
  background: #f44336;
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
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #666;
}

.review-items {
  display: flex;
  flex-direction: column;
}

.review-item {
  border-bottom: 1rpx solid #f0f0f0;
  transition: background 0.3s;
}

.review-item:hover {
  background: #f8f9fa;
}

.item-selected {
  background: #e8f5e8;
}

.item-header {
  display: flex;
  padding: 25rpx 30rpx;
  gap: 20rpx;
}

.select-section {
  display: flex;
  align-items: flex-start;
  padding-top: 10rpx;
}

.item-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  overflow: hidden;
  position: relative;
  flex-shrink: 0;
}

.item-image image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.status-badge {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  font-size: 20rpx;
  color: white;
}

.status-badge.pending { background: #ff9800; }
.status-badge.approved { background: #4caf50; }
.status-badge.rejected { background: #f44336; }

.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.item-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  flex: 1;
  margin-right: 15rpx;
}

.item-time {
  font-size: 22rpx;
  color: #999;
  white-space: nowrap;
}

.item-desc {
  font-size: 24rpx;
  color: #666;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.item-info {
  display: flex;
  flex-wrap: wrap;
  gap: 15rpx;
}

.info-item {
  font-size: 22rpx;
  color: #999;
  background: #f5f5f5;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.pickup-info {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 8rpx 12rpx;
  background: #e8f5e8;
  border-radius: 8rpx;
}

.pickup-label {
  font-size: 22rpx;
  color: #4caf50;
  font-weight: 500;
}

.pickup-location {
  font-size: 22rpx;
  color: #333;
}

.item-actions {
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
  font-size: 22rpx;
  text-align: center;
  transition: opacity 0.3s;
}

.action-btn:active {
  opacity: 0.8;
}

.approve-btn {
  background: #4caf50;
  color: white;
}

.reject-btn {
  background: #f44336;
  color: white;
}

.detail-btn {
  background: #2196f3;
  color: white;
}

/* 匹配建议 */
.match-suggestions {
  padding: 20rpx 30rpx;
  background: #f0f8ff;
  border-top: 1rpx solid #e0e0e0;
}

.match-title {
  font-size: 24rpx;
  font-weight: 600;
  color: #2196f3;
  margin-bottom: 15rpx;
  display: block;
}

.match-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.match-item {
  display: flex;
  gap: 15rpx;
  padding: 15rpx;
  background: white;
  border-radius: 8rpx;
  cursor: pointer;
  transition: background 0.3s;
}

.match-item:hover {
  background: #f5f5f5;
}

.match-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.match-info .match-title {
  font-size: 22rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 0;
}

.match-desc {
  font-size: 20rpx;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}

.match-time {
  font-size: 18rpx;
  color: #999;
}

.match-score {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5rpx;
  padding: 10rpx;
  min-width: 80rpx;
  background: #e8f5e8;
  border-radius: 8rpx;
}

.score-value {
  font-size: 24rpx;
  font-weight: 600;
  color: #4caf50;
}

.score-label {
  font-size: 18rpx;
  color: #4caf50;
}

/* 审核历史 */
.review-history {
  padding: 20rpx 30rpx;
  background: #fafafa;
  border-top: 1rpx solid #f0f0f0;
}

.history-title {
  font-size: 24rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 15rpx;
  display: block;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.history-item {
  display: flex;
  gap: 15rpx;
  align-items: center;
  padding: 10rpx;
  background: white;
  border-radius: 6rpx;
}

.history-action {
  font-size: 22rpx;
  font-weight: 500;
  color: #333;
  min-width: 60rpx;
}

.history-reviewer {
  font-size: 22rpx;
  color: #666;
  min-width: 80rpx;
}

.history-time {
  font-size: 22rpx;
  color: #999;
  min-width: 80rpx;
}

.history-comment {
  flex: 1;
  font-size: 22rpx;
  color: #666;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20rpx;
  padding: 30rpx;
  border-top: 1rpx solid #e0e0e0;
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
  
  .item-header {
    flex-direction: column;
    gap: 15rpx;
  }
  
  .item-actions {
    flex-direction: row;
    justify-content: space-between;
  }
  
  .item-info {
    flex-direction: column;
    gap: 8rpx;
  }
  
  .match-item {
    flex-direction: column;
    gap: 10rpx;
  }
}
</style>