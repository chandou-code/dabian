<template>
  <view class="search-container">
    <Sidebar />
    
    <view class="main-content" :class="{ 'main-content-expanded': !showSidebar }">
      <!-- 搜索区域 -->
      <view class="search-section">
        <text class="section-title">文本搜索</text>
        
        <!-- 文本搜索 -->
        <view class="text-search">
          <view class="search-box">
            <input 
              v-model="searchQuery" 
              class="search-input" 
              placeholder="输入物品名称、描述或地点进行搜索..."
              @confirm="handleSearch"
            />
            <button class="search-btn" @click="handleSearch">🔍</button>
          </view>
          
          <view class="search-filters">
            <picker 
              :range="categories" 
              :value="categoryIndex"
              @change="onCategoryChange"
              class="filter-picker"
            >
              <view class="picker-content">
                {{ categoryIndex === -1 ? '全部分类' : categories[categoryIndex] }}
              </view>
            </picker>
            
            <picker 
              :range="timeRanges" 
              :value="timeRangeIndex"
              @change="onTimeRangeChange"
              class="filter-picker"
            >
              <view class="picker-content">
                {{ timeRanges[timeRangeIndex] }}
              </view>
            </picker>
          </view>
        </view>
      </view>
      
      <!-- 搜索结果 -->
      <view class="search-results">
        <view class="results-header">
          <text class="results-title">搜索结果</text>
          <text v-if="searchResults.length > 0" class="results-count">
            找到 {{ searchResults.length }} 个相关结果
          </text>
        </view>
        
        <view v-if="loading" class="loading-state">
          <text>搜索中...</text>
        </view>
        
        <view v-else-if="searchResults.length === 0 && hasSearched" class="empty-state">
          <text class="empty-icon">🔍</text>
          <text class="empty-text">未找到相关结果</text>
          <text class="empty-desc">请尝试使用其他关键词或重新上传图片</text>
        </view>
        
        <view v-else-if="!hasSearched" class="search-hint">
          <text class="hint-icon">💡</text>
          <text class="hint-text">输入关键词或上传图片开始搜索</text>
        </view>
        
        <view v-else class="results-grid">
          <view 
            v-for="item in searchResults" 
            :key="item.id" 
            class="result-card"
            @click="viewItem(item)"
          >
            <view class="result-image">
              <image :src="item.image || '/static/default-item.jpg'" mode="aspectFill"></image>
            </view>
            
            <view class="result-content">
              <text class="result-title">{{ item.title }}</text>
              <text class="result-desc">{{ item.description }}</text>
              
              <view class="result-info">
                <text class="info-item">📍 {{ item.location }}</text>
                <text class="info-item">📅 {{ item.time }}</text>
                <text class="info-item">🏷️ {{ item.category }}</text>
              </view>
              
              <view class="result-footer">
                <text class="result-type" :class="item.type">
                  {{ item.type === 'lost' ? '失物' : '招领' }}
                </text>
                <text class="result-status" :class="getStatusClass(item.status)">
                  {{ getStatusText(item.status) }}
                </text>
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 推荐搜索 -->
      <view v-if="!hasSearched" class="recommended-searches">
        <text class="section-title">热门搜索</text>
        <view class="popular-tags">
          <button 
            v-for="tag in popularTags" 
            :key="tag"
            class="tag-btn"
            @click="searchByTag(tag)"
          >
            {{ tag }}
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import Sidebar from '@/components/Sidebar.vue'
import * as api from '@/api'

export default {
  name: 'SearchPage',
  components: {
    Sidebar
  },
  
  data() {
    return {
      showSidebar: true,
      searchQuery: '',
      loading: false,
      hasSearched: false,
      categoryIndex: -1,
      timeRangeIndex: 0,
      
      categories: [
        '电子产品', '钱包证件', '书籍文具', '生活用品', 
        '衣物饰品', '体育用品', '其他物品'
      ],
      
      timeRanges: ['全部时间', '最近一天', '最近三天', '最近一周', '最近一月'],
      
      popularTags: [
        '手机', '钱包', '身份证', '钥匙', '耳机', '书本', '水杯', '雨伞'
      ],
      
      searchResults: []
    }
  },
  
  methods: {
    async handleSearch() {
      if (!this.searchQuery.trim()) {
        uni.showToast({
          title: '请输入搜索关键词',
          icon: 'none'
        })
        return
      }
      
      this.loading = true
      this.hasSearched = true
      
      try {
        // 准备搜索参数
        const searchParams = {
          keyword: this.searchQuery,
          category: this.categoryIndex !== -1 ? this.categories[this.categoryIndex] : '',
          timeRange: this.getTimeRangeValue(this.timeRangeIndex),
          page: 1,
          size: 20
        }
        
        // 调用真实API搜索物品
        const response = await api.searchItems(searchParams)
        
        if (response.success && response.data && response.data.list) {
          // 转换API返回的数据格式
          this.searchResults = response.data.list.map(item => ({
            id: item.id,
            title: item.title || '未命名物品',
            description: item.description || '',
            category: item.category || '其他物品',
            location: (item.location || '未填写地点'),
            time: this.formatTime(item.time),
            type: item.type || 'lost',
            status: item.status || 'pending',
            image: item.image || ''
          }))
        } else {
          this.searchResults = []
          uni.showToast({
            title: '未找到相关结果',
            icon: 'none'
          })
        }
        
      } catch (error) {
        console.error('搜索失败:', error)
        this.searchResults = []
        uni.showToast({
          title: '搜索失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    
    searchByTag(tag) {
      this.searchQuery = tag
      this.handleSearch()
    },
    
    onCategoryChange(e) {
      this.categoryIndex = e.detail.value
      if (this.hasSearched) {
        this.handleSearch()
      }
    },
    
    onTimeRangeChange(e) {
      this.timeRangeIndex = e.detail.value
      if (this.hasSearched) {
        this.handleSearch()
      }
    },
    
    /**
     * 获取时间范围值
     */
    getTimeRangeValue(index) {
      const timeRanges = ['all', 'day', '3days', 'week', 'month']
      return timeRanges[index] || 'all'
    },
    
    /**
     * 格式化时间
     */
    formatTime(time) {
      if (!time) return '未知时间'
      
      try {
        const date = new Date(time)
        const now = new Date()
        const diff = now - date
        
        const minutes = Math.floor(diff / 60000)
        const hours = Math.floor(diff / 3600000)
        const days = Math.floor(diff / 86400000)
        const months = Math.floor(diff / (86400000 * 30))
        
        if (minutes < 60) return `${minutes}分钟前`
        if (hours < 24) return `${hours}小时前`
        if (days < 30) return `${days}天前`
        if (months < 12) return `${months}月前`
        
        return date.toLocaleDateString('zh-CN')
      } catch (e) {
        return '未知时间'
      }
    },
    
    /**
     * 从图片数据中获取第一张图片
     */
    getFirstImage(images) {
      if (!images) return null
      
      // 如果是数组，返回第一张有效图片
      if (Array.isArray(images)) {
        const firstImage = images.find(img => img && typeof img === 'string' && img.startsWith('http'))
        return firstImage || null
      }
      
      // 如果是字符串，尝试解析为JSON数组
      if (typeof images === 'string') {
        try {
          const imagesArray = JSON.parse(images)
          if (Array.isArray(imagesArray)) {
            const firstImage = imagesArray.find(img => img && typeof img === 'string' && img.startsWith('http'))
            return firstImage || null
          }
        } catch (error) {
          // 如果解析失败，直接检查是否是单个图片URL
          if (images.startsWith('http')) {
            return images
          }
        }
      }
      
      return null
    },
    
    clearResults() {
      this.searchResults = []
      this.hasSearched = false
    },
    
    viewItem(item) {
      uni.navigateTo({ 
        url: `/pages/user/item-detail?id=${item.id}&type=${item.type}` 
      })
    },
    
    getStatusClass(status) {
      const classMap = {
        pending: 'status-pending',
        approved: 'status-approved',
        found: 'status-found'
      }
      return classMap[status] || 'status-pending'
    },
    
    getStatusText(status) {
      const textMap = {
        pending: '待审核',
        approved: '已发布',
        found: '已找回'
      }
      return textMap[status] || '待审核'
    }
  }
}
</script>

<style scoped>
.search-container {
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

/* 搜索区域 */
.search-section {
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

.search-tabs {
  display: flex;
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.tab-btn {
  padding: 15rpx 30rpx;
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 20rpx;
  font-size: 26rpx;
  transition: all 0.3s;
}

.tab-btn.active {
  background: #2196f3;
  color: white;
}

.text-search {
  margin-bottom: 20rpx;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.search-input {
  flex: 1;
  height: 70rpx;
  padding: 0 20rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 28rpx;
}

.search-btn {
  width: 70rpx;
  height: 70rpx;
  background: #2196f3;
  color: white;
  border: none;
  border-radius: 8rpx;
  font-size: 28rpx;
}

.search-filters {
  display: flex;
  gap: 20rpx;
}

.filter-picker {
  flex: 1;
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



/* 搜索结果 */
.search-results {
  background: white;
  padding: 30rpx;
  border-radius: 12rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.results-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.results-count {
  font-size: 24rpx;
  color: #666;
}

.loading-state,
.empty-state,
.search-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx;
  text-align: center;
}

.empty-icon,
.hint-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.empty-text,
.hint-text {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 10rpx;
}

.empty-desc {
  font-size: 24rpx;
  color: #999;
}

.results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300rpx, 1fr));
  gap: 20rpx;
}

.result-card {
  background: #f8f9fa;
  border-radius: 12rpx;
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
}

.result-card:hover {
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
}

.result-image {
  position: relative;
  height: 200rpx;
  overflow: hidden;
}

.result-image image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.match-score {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  background: #4caf50;
  color: white;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  font-size: 20rpx;
  font-weight: 500;
}

.result-content {
  padding: 20rpx;
}

.result-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 10rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-desc {
  display: block;
  font-size: 24rpx;
  color: #666;
  margin-bottom: 15rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.result-info {
  display: flex;
  flex-direction: column;
  gap: 5rpx;
  margin-bottom: 15rpx;
}

.info-item {
  font-size: 22rpx;
  color: #999;
}

.result-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-type {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  font-weight: 500;
}

.result-type.lost {
  background: #fff3e0;
  color: #ff9800;
}

.result-type.found {
  background: #e8f5e8;
  color: #4caf50;
}

.result-status {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  color: white;
}

.status-pending { background: #ff9800; }
.status-approved { background: #4caf50; }
.status-found { background: #2196f3; }

/* 推荐搜索 */
.recommended-searches {
  background: white;
  padding: 30rpx;
  border-radius: 12rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.popular-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 15rpx;
}

.tag-btn {
  background: #f5f5f5;
  color: #666;
  border: 2rpx solid #e0e0e0;
  padding: 10rpx 20rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  transition: all 0.3s;
}

.tag-btn:hover {
  background: #e3f2fd;
  color: #2196f3;
  border-color: #2196f3;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
    padding: 20rpx;
  }
  
  .search-tabs {
    justify-content: center;
  }
  
  .results-grid {
    grid-template-columns: 1fr;
  }
  
  .upload-area {
    width: 250rpx;
    height: 250rpx;
  }
  
  .image-search-actions {
    flex-direction: column;
    gap: 15rpx;
  }
}
</style>