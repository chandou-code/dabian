<template>
  <view class="search-container">
    <Sidebar />
    
    <view class="main-content" :class="{ 'main-content-expanded': !showSidebar }">
      <!-- 搜索区域 -->
      <view class="search-section">
        <text class="section-title">智能搜索</text>
        
        <view class="search-tabs">
          <button 
            v-for="tab in searchTabs" 
            :key="tab.value"
            class="tab-btn"
            :class="{ 'active': activeSearchTab === tab.value }"
            @click="switchSearchTab(tab.value)"
          >
            {{ tab.label }}
          </button>
        </view>
        
        <!-- 文本搜索 -->
        <view v-if="activeSearchTab === 'text'" class="text-search">
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
        
        <!-- 以图搜图 -->
        <view v-if="activeSearchTab === 'image'" class="image-search">
          <view class="upload-area" @click="chooseSearchImage">
            <image v-if="searchImage" :src="searchImage" mode="aspectFit" class="preview-image"></image>
            <view v-else class="upload-placeholder">
              <text class="upload-icon">📷</text>
              <text class="upload-text">点击上传图片搜索</text>
            </view>
          </view>
          
          <view v-if="searchImage" class="image-search-actions">
            <button class="search-image-btn" @click="searchByImage">开始搜索</button>
            <button class="clear-image-btn" @click="clearSearchImage">清除图片</button>
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
              <view class="match-score" v-if="item.matchScore">
                匹配度 {{ item.matchScore }}%
              </view>
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

export default {
  name: 'SearchPage',
  components: {
    Sidebar
  },
  
  data() {
    return {
      showSidebar: true,
      activeSearchTab: 'text',
      searchQuery: '',
      searchImage: '',
      loading: false,
      hasSearched: false,
      categoryIndex: -1,
      timeRangeIndex: 0,
      
      searchTabs: [
        { label: '文本搜索', value: 'text' },
        { label: '以图搜图', value: 'image' }
      ],
      
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
    switchSearchTab(tab) {
      this.activeSearchTab = tab
      this.clearResults()
    },
    
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
        // 模拟API请求
        await new Promise(resolve => setTimeout(resolve, 1500))
        
        this.searchResults = this.generateMockResults()
        
      } catch (error) {
        uni.showToast({
          title: '搜索失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    
    chooseSearchImage() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          this.searchImage = res.tempFilePaths[0]
        },
        fail: (err) => {
          uni.showToast({
            title: '选择图片失败',
            icon: 'none'
          })
        }
      })
    },
    
    async searchByImage() {
      if (!this.searchImage) {
        uni.showToast({
          title: '请先上传图片',
          icon: 'none'
        })
        return
      }
      
      this.loading = true
      this.hasSearched = true
      
      try {
        // 模拟以图搜图
        await new Promise(resolve => setTimeout(resolve, 2000))
        
        this.searchResults = this.generateImageSearchResults()
        
        uni.showToast({
          title: '图片搜索完成',
          icon: 'success'
        })
        
      } catch (error) {
        uni.showToast({
          title: '图片搜索失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    
    clearSearchImage() {
      this.searchImage = ''
      this.clearResults()
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
    
    generateMockResults() {
      const mockData = []
      const types = ['lost', 'found']
      const statuses = ['pending', 'approved', 'found']
      
      for (let i = 1; i <= 8; i++) {
        const type = types[Math.floor(Math.random() * types.length)]
        const status = statuses[Math.floor(Math.random() * statuses.length)]
        
        mockData.push({
          id: i,
          title: `${this.searchQuery}相关物品 ${i}`,
          description: `与"${this.searchQuery}"相关的物品描述信息，包含详细特征...`,
          category: this.categories[Math.floor(Math.random() * this.categories.length)],
          location: `教学楼A${Math.floor(Math.random() * 5) + 1}楼`,
          time: `${Math.floor(Math.random() * 7) + 1}天前`,
          type,
          status,
          image: Math.random() > 0.5 ? '/static/item-sample.jpg' : null
        })
      }
      
      return mockData
    },
    
    generateImageSearchResults() {
      const mockData = []
      const types = ['lost', 'found']
      const statuses = ['pending', 'approved', 'found']
      
      for (let i = 1; i <= 6; i++) {
        const type = types[Math.floor(Math.random() * types.length)]
        const status = statuses[Math.floor(Math.random() * statuses.length)]
        
        mockData.push({
          id: i,
          title: `图片匹配物品 ${i}`,
          description: '根据上传图片匹配到的相似物品，特征高度相似...',
          category: this.categories[Math.floor(Math.random() * this.categories.length)],
          location: `图书馆${Math.floor(Math.random() * 3) + 1}楼`,
          time: `${Math.floor(Math.random() * 7) + 1}天前`,
          type,
          status,
          image: '/static/item-sample.jpg',
          matchScore: Math.floor(Math.random() * 30) + 70 // 70-99% 匹配度
        })
      }
      
      return mockData
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

/* 以图搜图 */
.image-search {
  text-align: center;
}

.upload-area {
  width: 300rpx;
  height: 300rpx;
  margin: 0 auto 20rpx;
  border: 2rpx dashed #e0e0e0;
  border-radius: 12rpx;
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.3s;
}

.upload-area:hover {
  border-color: #2196f3;
}

.preview-image {
  width: 100%;
  height: 100%;
}

.upload-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
}

.upload-icon {
  font-size: 60rpx;
}

.upload-text {
  font-size: 24rpx;
  color: #666;
}

.image-search-actions {
  display: flex;
  gap: 20rpx;
  justify-content: center;
}

.search-image-btn {
  background: #2196f3;
  color: white;
  border: none;
  padding: 15rpx 30rpx;
  border-radius: 8rpx;
  font-size: 26rpx;
}

.clear-image-btn {
  background: #f5f5f5;
  color: #666;
  border: 2rpx solid #e0e0e0;
  padding: 15rpx 30rpx;
  border-radius: 8rpx;
  font-size: 26rpx;
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