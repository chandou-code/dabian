<template>
  <view class="map-container">
    <!-- 顶部搜索栏 -->
    <view class="map-header">
      <view class="search-box">
        <text class="search-icon">🔍</text>
        <input
          type="text"
          class="search-input"
          placeholder="搜索地点"
          v-model="searchKeyword"
          @confirm="onSearch"
        />
        <text class="clear-icon" v-if="searchKeyword" @click="clearSearch">✕</text>
      </view>
      <view class="header-actions">
        <text class="action-btn" @click="locateMe">📍</text>
        <text class="action-btn" @click="showFilter">🔽</text>
      </view>
    </view>

    <!-- 地图区域 -->
    <view class="map-wrapper">
      <map
        id="map"
        class="map"
        :latitude="mapCenter.latitude"
        :longitude="mapCenter.longitude"
        :scale="mapScale"
        :markers="markers"
        :polyline="polyline"
        :enable-traffic="true"
        :show-location="true"
        @markertap="onMarkerTap"
        @tap="onMapTap"
      >
        <!-- 用户位置标记 -->
        <cover-view class="user-location" v-if="userLocation">
          <cover-view class="location-dot"></cover-view>
        </cover-view>
      </map>

      <!-- 搜索结果面板 -->
      <view class="search-results" v-if="searchResults.length > 0">
        <scroll-view scroll-y class="results-list">
          <view
            v-for="(item, index) in searchResults"
            :key="index"
            class="result-item"
            @click="selectLocation(item)"
          >
            <view class="result-info">
              <text class="result-name">{{ item.name }}</text>
              <text class="result-address">{{ item.address }}</text>
            </view>
            <text class="result-distance">{{ item.distance }}</text>
          </view>
        </scroll-view>
      </view>

      <!-- 任务筛选器 -->
      <view class="filter-panel" v-if="showFilterPanel">
        <view class="filter-header">
          <text class="filter-title">筛选任务类型</text>
          <text class="close-btn" @click="hideFilter">✕</text>
        </view>
        <view class="filter-tags">
          <text
            v-for="(type, index) in taskTypes"
            :key="index"
            class="filter-tag"
            :class="{ active: selectedTypes.includes(type.value) }"
            @click="toggleFilter(type.value)"
          >
            {{ type.name }}
          </text>
        </view>
        <button class="filter-confirm" @click="applyFilter">确认</button>
      </view>

      <!-- 任务卡片 -->
      <view class="task-card" v-if="selectedTask">
        <view class="task-header">
          <text class="task-type">{{ selectedTask.typeName }}</text>
          <text class="task-price">¥{{ selectedTask.price }}</text>
        </view>
        <text class="task-title">{{ selectedTask.title }}</text>
        <text class="task-address">{{ selectedTask.address }}</text>
        <button class="task-btn" @click="viewTaskDetail">查看详情</button>
      </view>

      <!-- 地图工具栏 -->
      <view class="map-tools">
        <text class="tool-btn" @click="zoomIn">+</text>
        <text class="tool-btn" @click="zoomOut">-</text>
        <text class="tool-btn" @click="toggleTraffic">{{ showTraffic ? '🚦' : '📊' }}</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      mapCenter: {
        latitude: 39.908823,
        longitude: 116.397470
      },
      mapScale: 15,
      searchKeyword: '',
      searchResults: [],
      userLocation: null,
      markers: [],
      polyline: [],
      showTraffic: true,
      showFilterPanel: false,
      selectedTypes: [],
      selectedTask: null,
      taskTypes: [
        { name: '快递代取', value: 'delivery' },
        { name: '外卖代送', value: 'food' },
        { name: '物品购买', value: 'buy' },
        { name: '排队代办', value: 'queue' },
        { name: '文件传递', value: 'file' }
      ],
      nearbyTasks: [
        {
          id: 'T001',
          typeName: '快递代取',
          title: '帮忙取个快递',
          price: 5,
          address: '东门菜鸟驿站',
          latitude: 39.908823,
          longitude: 116.397470
        },
        {
          id: 'T002',
          typeName: '外卖代送',
          title: '帮买奶茶',
          price: 8,
          address: '校内奶茶店',
          latitude: 39.909823,
          longitude: 116.398470
        },
        {
          id: 'T003',
          typeName: '物品购买',
          title: '买一支笔',
          price: 15,
          address: '学校超市',
          latitude: 39.907823,
          longitude: 116.396470
        }
      ],
      // 新增：接收页面参数
      locationType: '',
      selectedLocation: null
    }
  },
  onLoad(options) {
    // 接收页面参数
    this.locationType = options.type || ''
    this.initMap()
    this.loadUserLocation()
    this.loadNearbyTasks()
  },
  methods: {
    initMap() {
      // 初始化地图
      this.mapContext = uni.createMapContext('map', this)
    },

    loadUserLocation() {
      // 获取用户当前位置
      uni.getLocation({
        type: 'gcj02',
        success: (res) => {
          this.userLocation = {
            latitude: res.latitude,
            longitude: res.longitude
          }
          this.mapCenter = {
            latitude: res.latitude,
            longitude: res.longitude
          }

          // 添加用户位置标记
          this.markers.push({
            id: 'user',
            latitude: res.latitude,
            longitude: res.longitude,
            iconPath: '/static/marker-user.png',
            width: 30,
            height: 30,
            zIndex: 999
          })
        },
        fail: (err) => {
          console.error('获取位置失败', err)
          uni.showToast({
            title: '获取位置失败',
            icon: 'none'
          })
        }
      })
    },

    loadNearbyTasks() {
      // 加载附近任务
      this.nearbyTasks.forEach((task, index) => {
        this.markers.push({
          id: task.id,
          latitude: task.latitude,
          longitude: task.longitude,
          iconPath: '/static/marker-task.png',
          width: 30,
          height: 30,
          callout: {
            content: `¥${task.price}`,
            color: '#ffffff',
            fontSize: 12,
            borderRadius: 4,
            bgColor: '#2196f3',
            padding: 4
          }
        })
      })
    },

    onSearch() {
      if (!this.searchKeyword) {
        return
      }

      // TODO: 调用地图搜索API
      // 模拟搜索结果
      this.searchResults = [
        {
          name: '东门菜鸟驿站',
          address: '学校东门北侧50米',
          distance: '300m',
          latitude: 39.908823,
          longitude: 116.397470
        },
        {
          name: '南门快递柜',
          address: '学校南门西侧',
          distance: '500m',
          latitude: 39.907823,
          longitude: 116.396470
        },
        {
          name: '北门菜鸟驿站',
          address: '学校北门东侧',
          distance: '800m',
          latitude: 39.909823,
          longitude: 116.398470
        }
      ]
    },

    clearSearch() {
      this.searchKeyword = ''
      this.searchResults = []
    },

    selectLocation(item) {
      this.mapCenter = {
        latitude: item.latitude,
        longitude: item.longitude
      }
      this.searchResults = []
      this.selectedLocation = item

      // 添加标记
      this.markers.push({
        id: 'selected-' + Date.now(),
        latitude: item.latitude,
        longitude: item.longitude,
        iconPath: '/static/marker-selected.png',
        width: 30,
        height: 30
      })
      
      // 如果是从发布任务页面跳转过来的，直接返回结果
      if (this.locationType) {
        this.confirmLocation()
      }
    },
    
    // 确认位置
    confirmLocation() {
      if (this.selectedLocation) {
        // 返回结果给上一个页面
        const locationData = {
          name: this.selectedLocation.name,
          address: this.selectedLocation.address,
          latitude: this.selectedLocation.latitude,
          longitude: this.selectedLocation.longitude
        }
        
        // 将位置数据存储到全局，供上一个页面获取
        getApp().globalData.selectedLocation = {
          type: this.locationType,
          data: locationData
        }
        
        uni.navigateBack({
          delta: 1
        })
      }
    },

    onMarkerTap(e) {
      const markerId = e.detail.markerId
      const task = this.nearbyTasks.find(t => t.id === markerId)

      if (task) {
        this.selectedTask = task
      }
    },

    onMapTap(e) {
      // 点击地图可以选择位置
      this.selectedTask = null
    },

    viewTaskDetail() {
      if (this.selectedTask) {
        uni.navigateTo({
          url: `/pages/task/detail?id=${this.selectedTask.id}`
        })
      }
    },

    locateMe() {
      if (this.userLocation) {
        this.mapCenter = {
          latitude: this.userLocation.latitude,
          longitude: this.userLocation.longitude
        }
        this.mapScale = 15
      } else {
        this.loadUserLocation()
      }
    },

    showFilter() {
      this.showFilterPanel = true
    },

    hideFilter() {
      this.showFilterPanel = false
    },

    toggleFilter(type) {
      const index = this.selectedTypes.indexOf(type)
      if (index > -1) {
        this.selectedTypes.splice(index, 1)
      } else {
        this.selectedTypes.push(type)
      }
    },

    applyFilter() {
      this.showFilterPanel = false
      // TODO: 根据筛选条件重新加载任务
      this.loadNearbyTasks()
    },

    zoomIn() {
      if (this.mapScale < 18) {
        this.mapScale++
      }
    },

    zoomOut() {
      if (this.mapScale > 5) {
        this.mapScale--
      }
    },

    toggleTraffic() {
      this.showTraffic = !this.showTraffic
    }
  }
}
</script>

<style lang="scss" scoped>
.map-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.map-header {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  background: white;
  gap: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

  .search-box {
    flex: 1;
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

  .header-actions {
    display: flex;
    gap: 20rpx;

    .action-btn {
      font-size: 40rpx;
    }
  }
}

.map-wrapper {
  flex: 1;
  position: relative;
}

.map {
  width: 100%;
  height: 100%;
}

.user-location {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1000;

  .location-dot {
    width: 20rpx;
    height: 20rpx;
    background: #2196f3;
    border-radius: 50%;
    box-shadow: 0 0 0 10rpx rgba(33, 150, 243, 0.3);
  }
}

.search-results {
  position: absolute;
  top: 20rpx;
  left: 20rpx;
  right: 20rpx;
  background: white;
  border-radius: 16rpx;
  max-height: 500rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);

  .results-list {
    max-height: 500rpx;
  }

  .result-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx;
    border-bottom: 1rpx solid #f5f5f5;

    &:active {
      background: #fafafa;
    }

    .result-info {
      flex: 1;

      .result-name {
        display: block;
        font-size: 28rpx;
        color: #333;
        margin-bottom: 8rpx;
      }

      .result-address {
        display: block;
        font-size: 24rpx;
        color: #999;
      }
    }

    .result-distance {
      font-size: 24rpx;
      color: #2196f3;
    }
  }
}

.filter-panel {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);

  .filter-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;

    .filter-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }

    .close-btn {
      font-size: 36rpx;
      color: #999;
    }
  }

  .filter-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
    margin-bottom: 30rpx;

    .filter-tag {
      padding: 12rpx 24rpx;
      background: #f5f5f5;
      color: #666;
      border-radius: 24rpx;
      font-size: 24rpx;

      &.active {
        background: #2196f3;
        color: white;
      }
    }
  }

  .filter-confirm {
    width: 100%;
    height: 72rpx;
    background: linear-gradient(135deg, #2196f3, #1976d2);
    color: white;
    border-radius: 36rpx;
    font-size: 28rpx;
    border: none;
  }
}

.task-card {
  position: absolute;
  bottom: 30rpx;
  left: 20rpx;
  right: 20rpx;
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);

  .task-header {
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
    font-size: 48rpx;
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
    margin-bottom: 20rpx;
  }

  .task-btn {
    width: 100%;
    height: 80rpx;
    background: linear-gradient(135deg, #2196f3, #1976d2);
    color: white;
    border-radius: 40rpx;
    font-size: 28rpx;
    border: none;
  }
}

.map-tools {
  position: absolute;
  right: 20rpx;
  bottom: 300rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;

  .tool-btn {
    width: 80rpx;
    height: 80rpx;
    background: white;
    border-radius: 40rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
  }
}
</style>
