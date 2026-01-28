<template>
  <view class="search-container">
    <!-- 搜索框 -->
    <view class="search-bar">
      <view class="search-input-wrapper">
        <text class="search-icon">🔍</text>
        <input
          type="text"
          class="search-input"
          v-model="searchKeyword"
          placeholder="搜索任务、用户、地点"
          @input="onSearchInput"
          @confirm="onSearch"
          :focus="autoFocus"
        />
        <text class="clear-icon" v-if="searchKeyword" @click="clearSearch">✕</text>
      </view>
    </view>

    <!-- 搜索历史 -->
    <view class="search-history" v-if="!searchKeyword && searchHistory.length > 0">
      <view class="section-header">
        <text class="section-title">搜索历史</text>
        <text class="clear-history" @click="clearHistory">清空</text>
      </view>
      <view class="history-tags">
        <text
          v-for="(item, index) in searchHistory"
          :key="index"
          class="history-tag"
          @click="searchHistoryItem(item)"
        >
          {{ item }}
        </text>
      </view>
    </view>

    <!-- 热门搜索 -->
    <view class="hot-search" v-if="!searchKeyword">
      <view class="section-header">
        <text class="section-title">热门搜索</text>
      </view>
      <view class="hot-tags">
        <text
          v-for="(item, index) in hotSearches"
          :key="index"
          class="hot-tag"
          :class="{ top: index < 3 }"
          @click="searchHotItem(item)"
        >
          <text v-if="index < 3" class="hot-index">{{ index + 1 }}</text>
          {{ item }}
        </text>
      </view>
    </view>

    <!-- 搜索结果 -->
    <view class="search-results" v-if="searchKeyword">
      <view class="tabs">
        <text
          class="tab-item"
          :class="{ active: currentTab === 'task' }"
          @click="switchTab('task')"
        >
          任务 {{ taskCount > 0 ? `(${taskCount})` : '' }}
        </text>
        <text
          class="tab-item"
          :class="{ active: currentTab === 'user' }"
          @click="switchTab('user')"
        >
          用户 {{ userCount > 0 ? `(${userCount})` : '' }}
        </text>
        <text
          class="tab-item"
          :class="{ active: currentTab === 'location' }"
          @click="switchTab('location')"
        >
          地点 {{ locationCount > 0 ? `(${locationCount})` : '' }}
        </text>
      </view>

      <!-- 任务结果 -->
      <scroll-view scroll-y class="result-list" v-if="currentTab === 'task'">
        <view class="empty-state" v-if="taskResults.length === 0">
          <text class="empty-text">未找到相关任务</text>
        </view>
        <view
          v-for="task in taskResults"
          :key="task.id"
          class="result-card task-card"
          @click="viewTask(task.id)"
        >
          <view class="card-header">
            <text class="task-type">{{ task.typeName }}</text>
            <text class="task-price">¥{{ task.price }}</text>
          </view>
          <text class="task-title">{{ task.title }}</text>
          <text class="task-address">{{ task.address }}</text>
          <view class="card-footer">
            <text class="task-time">{{ task.time }}</text>
          </view>
        </view>
      </scroll-view>

      <!-- 用户结果 -->
      <scroll-view scroll-y class="result-list" v-if="currentTab === 'user'">
        <view class="empty-state" v-if="userResults.length === 0">
          <text class="empty-text">未找到相关用户</text>
        </view>
        <view
          v-for="user in userResults"
          :key="user.id"
          class="result-card user-card"
          @click="viewUser(user.id)"
        >
          <image class="user-avatar" :src="user.avatar" mode="aspectFill"></image>
          <view class="user-info">
            <text class="user-name">{{ user.nickname }}</text>
            <text class="user-stats">完成: {{ user.completeCount }} | 好评率: {{ user.goodRate }}%</text>
          </view>
          <button class="btn-chat">私信</button>
        </view>
      </scroll-view>

      <!-- 地点结果 -->
      <scroll-view scroll-y class="result-list" v-if="currentTab === 'location'">
        <view class="empty-state" v-if="locationResults.length === 0">
          <text class="empty-text">未找到相关地点</text>
        </view>
        <view
          v-for="location in locationResults"
          :key="location.id"
          class="result-card location-card"
          @click="selectLocation(location)"
        >
          <view class="location-info">
            <text class="location-icon">📍</text>
            <view class="location-detail">
              <text class="location-name">{{ location.name }}</text>
              <text class="location-address">{{ location.address }}</text>
            </view>
          </view>
          <text class="location-distance">{{ location.distance }}</text>
        </view>
      </scroll-view>

      <!-- 加载更多 -->
      <view class="loading-more" v-if="loading">
        <text class="loading-text">加载中...</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      autoFocus: true,
      searchKeyword: '',
      searchHistory: ['快递代取', '外卖', '奶茶', '打印'],
      hotSearches: [
        '快递代取',
        '外卖代送',
        '奶茶配送',
        '文件打印',
        '超市购物',
        '图书馆',
        '食堂',
        '校医院'
      ],
      currentTab: 'task',
      taskResults: [],
      userResults: [],
      locationResults: [],
      taskCount: 0,
      userCount: 0,
      locationCount: 0,
      loading: false,
      searchTimer: null,
      page: 1,
      hasMore: true
    }
  },
  onLoad(options) {
    if (options.keyword) {
      this.searchKeyword = options.keyword
      this.onSearch()
    }
    
    // 获取热门搜索词
    this.getHotSearches()
  },
  
  onShow() {
    // 页面显示时获取热门搜索词
    this.getHotSearches()
  },
  methods: {
    onSearchInput() {
      // 防抖搜索
      clearTimeout(this.searchTimer)
      this.searchTimer = setTimeout(() => {
        if (this.searchKeyword) {
          this.onSearch()
        }
      }, 500)
    },

    async onSearch() {
      if (!this.searchKeyword.trim()) {
        return
      }

      // 保存搜索历史
      this.saveToHistory(this.searchKeyword)

      // 执行搜索
      this.loading = true

      try {
        const token = uni.getStorageSync('token')
        if (!token) {
          uni.showToast({ title: '请先登录', icon: 'none' })
          return
        }

        // 调用搜索API
        const res = await request({
          url: '/search',
          method: 'GET',
          header: {
            'token': token
          },
          params: {
            keyword: this.searchKeyword,
            type: 'all',
            page: this.page,
            pageSize: 20
          }
        })

        if (res.code === 200) {
          const searchData = res.data || {}
          
          // 处理任务结果
          this.taskResults = (searchData.tasks || []).map(task => ({
            id: task.id,
            typeName: this.getTaskTypeName(task.type),
            title: task.title,
            price: task.price,
            address: task.pickupAddress,
            time: this.formatTaskTime(task.publishTime)
          }))
          this.taskCount = searchData.taskCount || 0

          // 处理用户结果
          this.userResults = (searchData.users || []).map(user => ({
            id: user.id,
            nickname: user.nickname || user.realName || user.username,
            avatar: user.avatar || '/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg',
            completeCount: 0, // 这里可以从用户统计信息中获取
            goodRate: 100 // 这里可以从用户评价中计算
          }))
          this.userCount = searchData.userCount || 0

          // 处理地点结果
          this.locationResults = (searchData.locations || []).map(location => ({
            id: location.id,
            name: location.name,
            address: location.address,
            distance: location.distance || '未知距离',
            latitude: location.latitude,
            longitude: location.longitude
          }))
          this.locationCount = searchData.locationCount || 0
        } else {
          uni.showToast({ title: res.msg || '搜索失败', icon: 'none' })
        }
      } catch (error) {
        console.error('搜索错误:', error)
        uni.showToast({ title: '网络错误，请稍后重试', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    
    // 获取任务类型名称
    getTaskTypeName(type) {
      const typeMap = {
        'delivery': '快递代取',
        'food': '外卖代送',
        'shopping': '超市购物',
        'document': '文件打印',
        'queue': '排队取号',
        'other': '其他任务'
      }
      return typeMap[type] || '其他任务'
    },
    
    // 格式化任务时间
    formatTaskTime(timeStr) {
      if (!timeStr) return ''
      
      const date = new Date(timeStr)
      return `${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    },

    clearSearch() {
      this.searchKeyword = ''
      this.taskResults = []
      this.userResults = []
      this.locationResults = []
      this.taskCount = 0
      this.userCount = 0
      this.locationCount = 0
    },

    switchTab(tab) {
      this.currentTab = tab
    },

    viewTask(id) {
      uni.navigateTo({
        url: `/pages/task/detail?id=${id}`
      })
    },

    viewUser(id) {
      uni.navigateTo({
        url: `/pages/user/profile?id=${id}`
      })
    },

    selectLocation(location) {
      uni.showToast({
        title: '地图功能已关闭',
        icon: 'none'
      })
    },

    searchHistoryItem(keyword) {
      this.searchKeyword = keyword
      this.onSearch()
    },

    searchHotItem(keyword) {
      this.searchKeyword = keyword
      this.onSearch()
    },

    saveToHistory(keyword) {
      const index = this.searchHistory.indexOf(keyword)
      if (index > -1) {
        this.searchHistory.splice(index, 1)
      }
      this.searchHistory.unshift(keyword)
      if (this.searchHistory.length > 10) {
        this.searchHistory.pop()
      }
    },

    clearHistory() {
      uni.showModal({
        title: '确认清空',
        content: '确定要清空搜索历史吗？',
        success: (res) => {
          if (res.confirm) {
            this.searchHistory = []
          }
        }
      })
    },
    
    // 获取热门搜索词
    async getHotSearches() {
      try {
        const token = uni.getStorageSync('token')
        
        const res = await request({
          url: '/search/hot',
          method: 'GET',
          header: {
            'token': token
          }
        })
        
        if (res.code === 200) {
          this.hotSearches = res.data || this.hotSearches
        }
      } catch (error) {
        console.error('获取热门搜索词错误:', error)
        // 使用默认热门搜索词
      }
    },
    
    // 加载更多搜索结果
    async loadMore() {
      if (this.loading || !this.hasMore) {
        return
      }
      
      this.page++
      await this.onSearch()
    }
  }
}
</script>

<style lang="scss" scoped>
.search-container {
  min-height: 100vh;
  background: #f5f5f5;
}

.search-bar {
  background: white;
  padding: 20rpx 30rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

  .search-input-wrapper {
    display: flex;
    align-items: center;
    height: 72rpx;
    background: #f5f5f5;
    border-radius: 36rpx;
    padding: 0 24rpx;
    gap: 16rpx;

    .search-icon {
      font-size: 32rpx;
    }

    .search-input {
      flex: 1;
      height: 100%;
      font-size: 28rpx;
      color: #333;
    }

    .clear-icon {
      font-size: 28rpx;
      color: #999;
    }
  }
}

.search-history,
.hot-search {
  background: white;
  margin-top: 20rpx;
  padding: 30rpx;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;

    .section-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }

    .clear-history {
      font-size: 24rpx;
      color: #999;
    }
  }

  .history-tags,
  .hot-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
  }

  .history-tag,
  .hot-tag {
    padding: 12rpx 24rpx;
    background: #f5f5f5;
    color: #666;
    border-radius: 24rpx;
    font-size: 26rpx;

    &.top {
      background: #fff3e0;
      color: #ff9800;
      font-weight: bold;

      .hot-index {
        display: inline-block;
        width: 36rpx;
        height: 36rpx;
        line-height: 36rpx;
        text-align: center;
        background: #ff9800;
        color: white;
        border-radius: 50%;
        margin-right: 8rpx;
        font-size: 20rpx;
      }
    }
  }
}

.search-results {
  background: white;
  margin-top: 20rpx;

  .tabs {
    display: flex;
    padding: 20rpx 30rpx;
    border-bottom: 1rpx solid #f5f5f5;

    .tab-item {
      flex: 1;
      text-align: center;
      padding: 16rpx 0;
      font-size: 28rpx;
      color: #666;
      position: relative;

      &.active {
        color: #2196f3;
        font-weight: bold;

        &::after {
          content: '';
          position: absolute;
          bottom: 0;
          left: 50%;
          transform: translateX(-50%);
          width: 60rpx;
          height: 4rpx;
          background: #2196f3;
          border-radius: 2rpx;
        }
      }
    }
  }

  .result-list {
    max-height: calc(100vh - 300rpx);
    padding: 20rpx;
  }
}

.empty-state {
  text-align: center;
  padding: 80rpx 0;

  .empty-text {
    font-size: 28rpx;
    color: #999;
  }
}

.result-card {
  background: white;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

  &.task-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16rpx;
    }

    .task-type {
      padding: 6rpx 16rpx;
      background: #e3f2fd;
      color: #2196f3;
      border-radius: 16rpx;
      font-size: 24rpx;
    }

    .task-price {
      font-size: 40rpx;
      color: #f44336;
      font-weight: bold;
    }

    .task-title {
      display: block;
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 12rpx;
    }

    .task-address {
      display: block;
      font-size: 26rpx;
      color: #666;
      margin-bottom: 16rpx;
    }

    .card-footer {
      .task-time {
        font-size: 24rpx;
        color: #999;
      }
    }
  }

  &.user-card {
    display: flex;
    align-items: center;
    gap: 20rpx;

    .user-avatar {
      width: 88rpx;
      height: 88rpx;
      border-radius: 44rpx;
    }

    .user-info {
      flex: 1;

      .user-name {
        display: block;
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
        margin-bottom: 8rpx;
      }

      .user-stats {
        font-size: 24rpx;
        color: #999;
      }
    }

    .btn-chat {
      padding: 12rpx 24rpx;
      background: #e3f2fd;
      color: #2196f3;
      border-radius: 24rpx;
      font-size: 24rpx;
      border: none;
    }
  }

  &.location-card {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .location-info {
      flex: 1;
      display: flex;
      align-items: center;
      gap: 16rpx;

      .location-icon {
        font-size: 40rpx;
      }

      .location-detail {
        .location-name {
          display: block;
          font-size: 28rpx;
          font-weight: bold;
          color: #333;
          margin-bottom: 8rpx;
        }

        .location-address {
          display: block;
          font-size: 24rpx;
          color: #999;
        }
      }
    }

    .location-distance {
      font-size: 24rpx;
      color: #2196f3;
    }
  }
}

.loading-more {
  text-align: center;
  padding: 30rpx 0;

  .loading-text {
    font-size: 24rpx;
    color: #999;
  }
}
</style>
