<template>
  <view class="lost-found-container">
    <Sidebar />
    
    <view class="main-content" :class="{ 'main-content-expanded': !showSidebar }">
      <!-- 搜索和筛选栏 -->
      <view class="search-filter-bar">
        <view class="search-section">
          <view class="search-box">
            <input 
              v-model="searchKeyword" 
              class="search-input" 
              placeholder="搜索物品名称或描述..."
              @confirm="handleSearch"
            />
            <button class="search-btn" @click="handleSearch">🔍</button>
          </view>
        </view>
        
        <view class="filter-section">
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
          
          <button class="filter-toggle" @click="toggleAdvancedFilter">
            {{ showAdvancedFilter ? '收起' : '高级筛选' }}
          </button>
        </view>
      </view>
      
      <!-- 高级筛选 -->
      <view v-if="showAdvancedFilter" class="advanced-filter">
        <view class="filter-row">
          <view class="filter-item">
            <text class="filter-label">状态：</text>
            <view class="filter-options">
              <button 
                v-for="status in statusOptions" 
                :key="status.value"
                class="filter-option-btn"
                :class="{ 'active': selectedStatus === status.value }"
                @click="selectedStatus = status.value"
              >
                {{ status.label }}
              </button>
            </view>
          </view>
        </view>
        
        <view class="filter-row">
          <view class="filter-item">
            <text class="filter-label">地点：</text>
            <input 
              v-model="locationFilter" 
              class="filter-input" 
              placeholder="输入地点关键词"
            />
          </view>
        </view>
        
        <button class="apply-filter-btn" @click="applyFilter">应用筛选</button>
      </view>
      
      <!-- 标签切换 -->
      <view class="tab-bar">
        <view 
          v-for="tab in tabs" 
          :key="tab.value"
          class="tab-item"
          :class="{ 'active': activeTab === tab.value }"
          @click="switchTab(tab.value)"
        >
          <text class="tab-icon">{{ tab.icon }}</text>
          <text class="tab-text">{{ tab.label }}</text>
          <text class="tab-count">{{ tab.count }}</text>
        </view>
      </view>
      
      <!-- 物品列表 -->
      <view class="items-list">
        <view v-if="loading" class="loading-state">
          <text>加载中...</text>
        </view>
        
        <view v-else-if="filteredItems.length === 0" class="empty-state">
          <text class="empty-icon">🔍</text>
          <text class="empty-text">暂无相关失物招领信息</text>
          <button class="publish-btn" @click="navigateTo('/pages/user/publish-lost')">
            发布失物信息
          </button>
        </view>
        
        <view v-else class="item-grid">
          <view 
            v-for="item in paginatedItems" 
            :key="item.id" 
            class="item-card"
            @click="viewItem(item)"
          >
            <view class="item-image">
              <image :src="item.image || '/static/default-item.jpg'" mode="aspectFill"></image>
              <view class="item-status" :class="getStatusClass(item.status)">
                {{ getStatusText(item.status) }}
              </view>
              <!-- 图片数量标识 -->
              <view v-if="item.imageCount > 1" class="image-count">
                {{ item.imageCount }}+
              </view>
            </view>
            
            <view class="item-content">
              <text class="item-title">{{ item.title }}</text>
              <text class="item-desc">{{ item.description }}</text>
              
              <view class="item-info">
                <text class="info-item">📍 {{ item.location }}</text>
                <text class="info-item">📅 {{ item.time }}</text>
                <text class="info-item">🏷️ {{ item.category }}</text>
                <text class="info-item">👤 {{ item.userName || '匿名用户' }}</text>
              </view>
              
              <view class="item-footer">
                <text class="item-type" :class="item.type">
                  {{ item.type === 'lost' ? '失物' : '招领' }}
                </text>
                <text class="item-time">{{ item.publishTime }}</text>
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
import * as api from '@/api'

export default {
  name: 'LostFound',
  components: {
    Sidebar
  },
  
  data() {
    return {
      showSidebar: true,
      loading: false,
      searchKeyword: '',
      categoryIndex: -1,
      timeRangeIndex: 0,
      showAdvancedFilter: false,
      selectedStatus: '',
      locationFilter: '',
      activeTab: 'all',
      currentPage: 1,
      pageSize: 12,
      
      categories: [
        '电子产品', '钱包证件', '书籍文具', '生活用品', 
        '衣物饰品', '体育用品', '其他物品'
      ],
      
      timeRanges: ['最近一天', '最近三天', '最近一周', '最近一月'],
      
      statusOptions: [
        { label: '全部', value: '' },
        { label: '待审核', value: 'pending' },
        { label: '已通过', value: 'approved' },
        { label: '已找回', value: 'found' }
      ],
      
      tabs: [
        { label: '全部', value: 'all', icon: '📋', count: 0 },
        { label: '失物', value: 'lost', icon: '🔍', count: 0 },
        { label: '招领', value: 'found', icon: '✅', count: 0 },
        { label: '已找回', value: 'recovered', icon: '🎉', count: 0 }
      ],
      
      items: []
    }
  },
  
  computed: {
    filteredItems() {
      let filtered = [...this.items]
      
      console.log('===== 开始筛选物品 =====')
      console.log('原始物品列表数量:', this.items.length)
      console.log('当前活动标签:', this.activeTab)
      console.log('当前分类索引:', this.categoryIndex)
      console.log('当前选中状态:', this.selectedStatus)
      console.log('当前位置筛选:', this.locationFilter)
      console.log('当前搜索关键词:', this.searchKeyword)
      
      // 按标签筛选
      if (this.activeTab !== 'all') {
        if (this.activeTab === 'recovered') {
          filtered = filtered.filter(item => item.status === 'found')
        } else {
          filtered = filtered.filter(item => item.type === this.activeTab)
        }
        console.log('按标签筛选后数量:', filtered.length)
      }
      
      // 按分类筛选
      if (this.categoryIndex !== -1) {
        const category = this.categories[this.categoryIndex]
        filtered = filtered.filter(item => item.category === category)
        console.log('按分类筛选后数量:', filtered.length)
      }
      
      // 按状态筛选
      if (this.selectedStatus) {
        filtered = filtered.filter(item => item.status === this.selectedStatus)
        console.log('按状态筛选后数量:', filtered.length)
      }
      
      // 按地点筛选
      if (this.locationFilter) {
        filtered = filtered.filter(item => 
          item.location.toLowerCase().includes(this.locationFilter.toLowerCase())
        )
        console.log('按地点筛选后数量:', filtered.length)
      }
      
      // 按关键词搜索
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        filtered = filtered.filter(item => 
          item.title.toLowerCase().includes(keyword) ||
          item.description.toLowerCase().includes(keyword)
        )
        console.log('按关键词筛选后数量:', filtered.length)
      }
      
      console.log('最终筛选结果数量:', filtered.length)
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
    this.loadItems()
  },
  
  onShow() {
    // 每次页面显示时刷新数据，确保能看到最新提交的物品
    this.loadItems()
  },
  
  methods: {
    async loadItems() {
      this.loading = true
      
      try {
        // 调用真实API获取失物和招领列表
        console.log('===== 开始加载物品数据 =====')
        const [lostItemsResponse, foundItemsResponse] = await Promise.all([
          api.getLostItems(),
          api.getFoundItems()
        ])
        
        console.log('失物API返回数据:', lostItemsResponse)
        console.log('招领API返回数据:', foundItemsResponse)
        
        // 检查API响应结构
        const lostItems = lostItemsResponse && lostItemsResponse.data ? lostItemsResponse.data : {}
        const foundItems = foundItemsResponse && foundItemsResponse.data ? foundItemsResponse.data : {}
        
        console.log('失物数据:', lostItems)
        console.log('招领数据:', foundItems)
        
        // 从API返回的对象中提取list字段
        const lostItemsList = Array.isArray(lostItems.list) ? lostItems.list : (Array.isArray(lostItems) ? lostItems : [])
        const foundItemsList = Array.isArray(foundItems.list) ? foundItems.list : (Array.isArray(foundItems) ? foundItems : [])
        
        console.log('失物列表:', lostItemsList)
        console.log('招领列表:', foundItemsList)
        
        // 处理招领物品的位置字段（可能是foundLocation而不是lostLocation）
        const processedItems = [...lostItemsList, ...foundItemsList].map(item => {
          // 确保item是对象
          const safeItem = typeof item === 'object' && item !== null ? item : {}
          
          // 确定物品类型和位置字段名
          const itemType = safeItem.type || (safeItem.foundTime ? 'found' : 'lost')
          const locationField = itemType === 'lost' ? 'lostLocation' : 'foundLocation'
          
          const imagesArray = this.getImagesArray(safeItem.images)
          return {
            id: safeItem.id || '',
            title: safeItem.itemName || (itemType === 'lost' ? '未命名失物' : '未命名招领'),
            description: safeItem.description || '',
            category: safeItem.category || '其他物品',
            location: safeItem[locationField] || safeItem.location || '未知地点',
            time: this.formatTime(safeItem.lostTime || safeItem.foundTime || safeItem.eventTime),
            publishTime: this.formatTime(safeItem.createdAt),
            type: itemType,
            status: safeItem.status || 'pending',
            image: this.getFirstImage(safeItem.images),
            imageCount: imagesArray.length,
            userName: safeItem.userName || safeItem.submitterName || '匿名用户'
          }
        })
        
        this.items = processedItems
        this.updateTabCounts()
        console.log('处理后的物品数据:', this.items)
        console.log('总物品数量:', this.items.length)
        
      } catch (error) {
        console.error('加载物品列表失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    
    generateMockItems() {
      const mockData = []
      const types = ['lost', 'found']
      const statuses = ['pending', 'approved', 'found']
      
      for (let i = 1; i <= 30; i++) {
        const type = types[Math.floor(Math.random() * types.length)]
        const status = statuses[Math.floor(Math.random() * statuses.length)]
        
        mockData.push({
          id: i,
          title: type === 'lost' ? `丢失的物品 ${i}` : `捡到的物品 ${i}`,
          description: '这是一份详细描述，包含了物品的各种特征信息...',
          category: this.categories[Math.floor(Math.random() * this.categories.length)],
          location: `教学楼A${Math.floor(Math.random() * 5) + 1}楼`,
          time: `${Math.floor(Math.random() * 30) + 1}天前`,
          publishTime: `${Math.floor(Math.random() * 24)}小时前`,
          type,
          status,
          image: Math.random() > 0.5 ? '/static/item-sample.jpg' : null
        })
      }
      
      return mockData
    },
    
    updateTabCounts() {
      this.tabs[0].count = this.items.length
      this.tabs[1].count = this.items.filter(item => item.type === 'lost').length
      this.tabs[2].count = this.items.filter(item => item.type === 'found').length
      this.tabs[3].count = this.items.filter(item => item.status === 'found').length
    },
    
    onCategoryChange(e) {
      this.categoryIndex = e.detail.value
      this.currentPage = 1
    },
    
    onTimeRangeChange(e) {
      this.timeRangeIndex = e.detail.value
      this.currentPage = 1
    },
    
    toggleAdvancedFilter() {
      this.showAdvancedFilter = !this.showAdvancedFilter
    },
    
    applyFilter() {
      this.currentPage = 1
      this.showAdvancedFilter = false
      
      uni.showToast({
        title: '筛选已应用',
        icon: 'success'
      })
    },
    
    switchTab(tab) {
      this.activeTab = tab
      this.currentPage = 1
    },
    
    handleSearch() {
      this.currentPage = 1
    },
    
    changePage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page
        // 滚动到顶部
        uni.pageScrollTo({ scrollTop: 0 })
      }
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
    },
    
    formatTime(dateString) {
      if (!dateString) return '未知时间'
      
      const date = new Date(dateString)
      const now = new Date()
      const diffTime = Math.abs(now - date)
      const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
      const diffHours = Math.floor(diffTime / (1000 * 60 * 60))
      
      if (diffDays > 0) {
        return `${diffDays}天前`
      } else if (diffHours > 0) {
        return `${diffHours}小时前`
      } else {
        return '刚刚'
      }
    },
    
    getImagesArray(imagesJson) {
      if (!imagesJson) return []
      
      // 如果已经是数组，处理每个元素
      if (Array.isArray(imagesJson)) {
        return imagesJson.map(img => {
          // 处理被双引号包裹的URL
          if (typeof img === 'string') {
            return this.cleanImageUrl(img)
          }
          return img
        }).filter(img => img && typeof img === 'string' && img.startsWith('http'))
      }
      
      // 如果是字符串，尝试解析为JSON
      if (typeof imagesJson === 'string') {
        try {
          // 先检查是否是有效的JSON字符串
          const trimmed = imagesJson.trim()
          if (!trimmed || trimmed === 'null' || trimmed === 'undefined') {
            return []
          }
          
          // 处理JSON数组字符串
          let images = []
          if (trimmed.startsWith('[') && trimmed.endsWith(']')) {
            // 尝试解析为数组
            images = JSON.parse(trimmed)
            if (Array.isArray(images)) {
              return images.map(img => this.cleanImageUrl(img))
                .filter(img => img && typeof img === 'string' && img.startsWith('http'))
            }
          } else {
            // 单个图片URL字符串
            const cleanUrl = this.cleanImageUrl(trimmed)
            return cleanUrl.startsWith('http') ? [cleanUrl] : []
          }
        } catch (error) {
          console.error('解析图片数据失败:', error)
          console.log('原始图片数据:', imagesJson)
          
          // 尝试直接处理字符串，可能是被双引号包裹的URL
          const cleanUrl = this.cleanImageUrl(imagesJson)
          return cleanUrl.startsWith('http') ? [cleanUrl] : []
        }
      }
      
      // 如果是对象，尝试转换为数组
      if (typeof imagesJson === 'object' && imagesJson !== null) {
        // 如果是FileUpload对象，尝试提取fileUrl
        if (imagesJson.fileUrl) {
          return [this.cleanImageUrl(imagesJson.fileUrl)]
        }
        
        // 如果是包含imageUrls或urls的对象
        if (imagesJson.imageUrls && Array.isArray(imagesJson.imageUrls)) {
          return imagesJson.imageUrls.map(img => this.cleanImageUrl(img))
            .filter(img => img && typeof img === 'string' && img.startsWith('http'))
        }
        
        if (imagesJson.urls && Array.isArray(imagesJson.urls)) {
          return imagesJson.urls.map(img => this.cleanImageUrl(img))
            .filter(img => img && typeof img === 'string' && img.startsWith('http'))
        }
        
        // 尝试将对象的值转换为数组
        const values = Object.values(imagesJson)
        return values
          .map(img => {
            if (typeof img === 'string') {
              return this.cleanImageUrl(img)
            }
            return img
          })
          .filter(img => typeof img === 'string' && img.startsWith('http'))
      }
      
      return []
    },
    
    // 清理图片URL，移除多余的引号和处理特殊格式
    cleanImageUrl(url) {
      if (!url || typeof url !== 'string') return ''
      
      // 移除前后的引号（处理类似 "http://..." 的格式）
      let cleaned = url.trim()
      while (cleaned.startsWith('"') || cleaned.startsWith('\'') || cleaned.startsWith('`')) {
        cleaned = cleaned.slice(1)
      }
      while (cleaned.endsWith('"') || cleaned.endsWith('\'') || cleaned.endsWith('`')) {
        cleaned = cleaned.slice(0, -1)
      }
      
      // 处理blob URL（前端临时URL，需要替换为实际上传的URL）
      if (cleaned.startsWith('blob:')) {
        console.warn('忽略blob URL，这是前端临时URL:', cleaned)
        return ''
      }
      
      // 处理相对路径，添加完整URL
      if (cleaned.startsWith('/uploads/')) {
        return `http://localhost:18080/api${cleaned}`
      }
      
      return cleaned
    },
    
    getFirstImage(imagesJson) {
      const images = this.getImagesArray(imagesJson)
      return images.length > 0 ? images[0] : '/static/default-item.jpg'
    }
  }
}
</script>

<style scoped>
.lost-found-container {
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

/* 搜索筛选栏 */
.search-filter-bar {
  background: white;
  padding: 30rpx;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
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

.filter-section {
  display: flex;
  gap: 20rpx;
  align-items: center;
  flex-wrap: wrap;
}

.filter-picker {
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

.filter-toggle {
  height: 60rpx;
  padding: 0 24rpx;
  background: #f5f5f5;
  color: #666;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 26rpx;
}

/* 高级筛选 */
.advanced-filter {
  background: white;
  padding: 30rpx;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.filter-row {
  margin-bottom: 20rpx;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.filter-label {
  font-size: 26rpx;
  color: #333;
  min-width: 80rpx;
}

.filter-options {
  display: flex;
  gap: 10rpx;
  flex-wrap: wrap;
}

.filter-option-btn {
  height: 50rpx;
  padding: 0 20rpx;
  background: #f5f5f5;
  color: #666;
  border: 2rpx solid #e0e0e0;
  border-radius: 20rpx;
  font-size: 24rpx;
}

.filter-option-btn.active {
  background: #2196f3;
  color: white;
  border-color: #2196f3;
}

.filter-input {
  flex: 1;
  height: 50rpx;
  padding: 0 15rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 6rpx;
  font-size: 24rpx;
}

.apply-filter-btn {
  width: 100%;
  height: 60rpx;
  background: #2196f3;
  color: white;
  border: none;
  border-radius: 8rpx;
  font-size: 26rpx;
  margin-top: 20rpx;
}

/* 标签栏 */
.tab-bar {
  display: flex;
  background: white;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.tab-item {
  flex: 1;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  border-bottom: 4rpx solid transparent;
  transition: all 0.3s;
}

.tab-item.active {
  background: #e3f2fd;
  border-bottom-color: #2196f3;
  color: #2196f3;
}

.tab-icon {
  font-size: 32rpx;
}

.tab-text {
  font-size: 26rpx;
  font-weight: 500;
}

.tab-count {
  font-size: 20rpx;
  background: #f5f5f5;
  padding: 2rpx 8rpx;
  border-radius: 10rpx;
  min-width: 30rpx;
  text-align: center;
}

/* 物品列表 */
.items-list {
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
  margin-bottom: 30rpx;
}

.publish-btn {
  background: #2196f3;
  color: white;
  border: none;
  padding: 15rpx 30rpx;
  border-radius: 8rpx;
  font-size: 26rpx;
}

.item-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300rpx, 1fr));
  gap: 20rpx;
}

.item-card {
  background: white;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
}

.item-card:hover {
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
}

.item-image {
  position: relative;
  height: 200rpx;
  overflow: hidden;
}

.item-image image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-status {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  font-size: 20rpx;
  color: white;
}

.status-pending { background: #ff9800; }
.status-approved { background: #4caf50; }
.status-found { background: #2196f3; }

.image-count {
  position: absolute;
  top: 10rpx;
  left: 10rpx;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 4rpx 8rpx;
  border-radius: 8rpx;
  font-size: 20rpx;
  min-width: 40rpx;
  text-align: center;
}

.item-content {
  padding: 20rpx;
}

.item-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 10rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-desc {
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

.item-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-bottom: 15rpx;
}

.info-item {
  font-size: 22rpx;
  color: #999;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-type {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
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

.item-time {
  font-size: 20rpx;
  color: #999;
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
  font-size: 26rpx;
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
  font-size: 26rpx;
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
  
  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .item-grid {
    grid-template-columns: 1fr;
  }
  
  .pagination {
    flex-wrap: wrap;
  }
}
</style>