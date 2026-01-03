<template>
  <view class="home-container">
    <!-- 公告弹窗 -->
    <AnnouncementPopup />
    
    <!-- 顶部横幅 -->
    <view class="hero-section">
      <view class="hero-content">
        <text class="hero-title">校园失物招领</text>
        <text class="hero-subtitle">让失物有归途，让爱心传递</text>
      </view>
      <view class="hero-actions">
        <button class="btn btn-primary hero-btn" @click="navigateTo('/pages/login/login')">
          立即登录
        </button>
        <button class="btn btn-outline hero-btn" @click="navigateTo('/pages/register/register')">
          注册账号
        </button>
      </view>
    </view>

    <!-- 快速操作 -->
    <view class="quick-actions">
      <view class="section-title">快速操作</view>
      <view class="action-grid">
        <view class="action-item" @click="navigateTo('/pages/user/publish-lost')">
          <view class="action-icon">失</view>
          <text class="action-text">发布失物</text>
        </view>
        <view class="action-item" @click="navigateTo('/pages/user/publish-found')">
          <view class="action-icon">招</view>
          <text class="action-text">发布招领</text>
        </view>
        <view class="action-item" @click="navigateTo('/pages/user/lost-found')">
          <view class="action-icon">览</view>
          <text class="action-text">浏览信息</text>
        </view>
        <view class="action-item" @click="navigateTo('/pages/user/search')">
          <view class="action-icon">搜</view>
          <text class="action-text">智能搜索</text>
        </view>
      </view>
    </view>

    <!-- 统计信息 -->
    <view class="stats-section">
      <view class="section-title">平台数据</view>
      <view class="stats-grid">
        <view class="stat-item">
          <text class="stat-number">{{ stats.totalLost }}</text>
          <text class="stat-label">失物登记</text>
        </view>
        <view class="stat-item">
          <text class="stat-number">{{ stats.totalFound }}</text>
          <text class="stat-label">招领信息</text>
        </view>
        <view class="stat-item">
          <text class="stat-number">{{ stats.recovered }}</text>
          <text class="stat-label">成功找回</text>
        </view>
        <view class="stat-item">
          <text class="stat-number">{{ stats.recoveryRate }}%</text>
          <text class="stat-label">找回率</text>
        </view>
      </view>
    </view>

    <!-- 最新信息 -->
    <view class="recent-section">
      <view class="section-title">最新信息</view>
      <view class="recent-list">
        <view 
          v-for="item in recentItems" 
          :key="item.id"
          class="recent-item"
          @click="viewItem(item)"
        >
          <view class="item-icon">{{ item.type === 'lost' ? '失' : '招' }}</view>
          <view class="item-content">
            <text class="item-title">{{ item.title }}</text>
            <text class="item-desc">{{ item.description }}</text>
            <text class="item-time">{{ item.time }}</text>
          </view>
          <view class="item-status" :class="'status-' + item.status">
            {{ getStatusText(item.status) }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getHomeStats, getHomeRecentItems } from '@/api/stats'
import AnnouncementPopup from '@/components/AnnouncementPopup.vue'

export default {
  components: {
    AnnouncementPopup
  },
  data() {
    return {
      stats: {
        totalLost: 0,
        totalFound: 0,
        recovered: 0,
        recoveryRate: 0
      },
      recentItems: [],
      loading: false
    }
  },
  
  onLoad() {
    this.loadHomeData()
  },
  
  onShow() {
    // 页面显示时刷新数据
    this.loadHomeData()
  },
  
  methods: {
    // 加载首页数据
    async loadHomeData() {
      this.loading = true
      try {
        // 并行请求统计数据和最新物品
        await Promise.all([
          this.loadStats(),
          this.loadRecentItems()
        ])
      } catch (error) {
        console.error('加载首页数据失败:', error)
        uni.showToast({
          title: '加载数据失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    
    // 加载统计数据
    async loadStats() {
      try {
        const response = await getHomeStats()
        if (response.code === 200 && response.data) {
          this.stats = {
            totalLost: response.data.totalLostItems || 0,
            totalFound: response.data.totalFoundItems || 0,
            recovered: response.data.completedClaims || 0,
            recoveryRate: response.data.recoveryRate || 0
          }
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    },
    
    // 加载最新物品列表
    async loadRecentItems() {
      try {
        const response = await getHomeRecentItems(10)
        if (response.code === 200 && response.data && response.data.recentItems) {
          this.recentItems = response.data.recentItems.map(item => {
            return {
              id: item.id,
              type: item.type || 'lost',
              title: item.title || '未知物品',
              description: item.description || '暂无描述',
              time: this.formatTime(item.createTime),
              status: this.mapStatus(item.status)
            }
          })
        }
      } catch (error) {
        console.error('加载最新物品失败:', error)
      }
    },
    
    // 格式化时间
    formatTime(timeStr) {
      if (!timeStr) return '未知时间'
      
      try {
        const date = new Date(timeStr)
        const now = new Date()
        const diff = now - date
        const hours = Math.floor(diff / (1000 * 60 * 60))
        const days = Math.floor(hours / 24)
        
        if (hours < 1) {
          return '刚刚'
        } else if (hours < 24) {
          return `${hours}小时前`
        } else if (days < 7) {
          return `${days}天前`
        } else {
          return date.toLocaleDateString()
        }
      } catch (error) {
        return '时间格式错误'
      }
    },
    
    // 映射状态
    mapStatus(status) {
      const statusMap = {
        'pending': 'pending',
        'approved': 'approved',
        'rejected': 'rejected',
        'claimed': 'recovered'
      }
      return statusMap[status] || 'pending'
    },
    
    navigateTo(url) {
      uni.navigateTo({ url })
    },
    
    viewItem(item) {
      uni.showModal({
        title: item.title,
        content: item.description,
        confirmText: '查看详情',
        success: (res) => {
          if (res.confirm) {
            // 跳转到失物招领页面，同时传递类型和ID参数
            uni.navigateTo({ 
              url: `/pages/user/lost-found?type=${item.type}&id=${item.id}` 
            })
          }
        }
      })
    },
    
    getStatusText(status) {
      const statusMap = {
        pending: '待审核',
        approved: '已通过',
        rejected: '已驳回',
        recovered: '已找回'
      }
      return statusMap[status] || '待审核'
    }
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8f9fa 0%, #e9ecef 100%);
}

/* 横幅区域 */
.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 80rpx 40rpx 60rpx;
  text-align: center;
}

.hero-content {
  margin-bottom: 40rpx;
}

.hero-title {
  font-size: 48rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 20rpx;
}

.hero-subtitle {
  font-size: 28rpx;
  opacity: 0.9;
  display: block;
}

.hero-actions {
  display: flex;
  gap: 20rpx;
  justify-content: center;
}

.hero-btn {
  flex: 1;
  max-width: 200rpx;
  height: 70rpx;
  font-size: 28rpx;
}

/* 快速操作 */
.quick-actions {
  padding: 40rpx 30rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 30rpx;
  display: block;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
}

.action-item {
  background: white;
  border-radius: 12rpx;
  padding: 30rpx 20rpx;
  text-align: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.action-item:active {
  transform: scale(0.95);
}

.action-icon {
  font-size: 40rpx;
  margin-bottom: 10rpx;
  display: block;
}

.action-text {
  font-size: 24rpx;
  color: #666;
  display: block;
}

/* 统计信息 */
.stats-section {
  padding: 0 30rpx 40rpx;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
}

.stat-item {
  background: white;
  border-radius: 12rpx;
  padding: 30rpx 20rpx;
  text-align: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.stat-number {
  font-size: 36rpx;
  font-weight: bold;
  color: #2196f3;
  display: block;
  margin-bottom: 10rpx;
}

.stat-label {
  font-size: 22rpx;
  color: #666;
  display: block;
}

/* 最新信息 */
.recent-section {
  padding: 0 30rpx 40rpx;
}

.recent-list {
  background: white;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.recent-item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.recent-item:last-child {
  border-bottom: none;
}

.item-icon {
  font-size: 32rpx;
  margin-right: 20rpx;
  width: 40rpx;
  text-align: center;
}

.item-content {
  flex: 1;
}

.item-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 10rpx;
}

.item-desc {
  font-size: 24rpx;
  color: #666;
  display: block;
  margin-bottom: 10rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-time {
  font-size: 20rpx;
  color: #999;
  display: block;
}

.item-status {
  font-size: 22rpx;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  background: #f5f5f5;
}

.status-pending {
  color: #ff9800;
  background: #fff3e0;
}

.status-approved {
  color: #4caf50;
  background: #e8f5e8;
}

.status-recovered {
  color: #2196f3;
  background: #e3f2fd;
}

.btn-outline {
  background: transparent;
  border: 2rpx solid white;
  color: white;
}

.btn-outline:active {
  background: rgba(255, 255, 255, 0.1);
}
</style>
