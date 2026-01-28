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
      <!-- 腾讯地图容器 -->
      <div id="tencentMap" class="map"></div>
      <!-- 地图中心点标记 -->
      <view class="map-center-marker">
        <div class="red-dot-shadow"></div>
        <div class="red-dot"></div>
      </view>

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
      selectedLocation: null,
      // 腾讯地图实例
      mapInstance: null,
      // 地图标记
      mapMarkers: []
    }
  },
  onLoad(options) {
    // 接收页面参数
    this.locationType = options.type || ''
  },
  mounted() {
    // 初始化地图
    this.initMap()
  },
  methods: {
    initMap() {
      // 确保DOM已加载完成
      this.$nextTick(() => {
        // 创建腾讯地图实例
        this.mapInstance = new TMap.Map('tencentMap', {
          center: new TMap.LatLng(this.mapCenter.latitude, this.mapCenter.longitude),
          zoom: this.mapScale
        })
        
        // 添加地图点击事件监听
        this.mapInstance.on('click', (evt) => {
          const { lat, lng } = evt.latLng
          this.onMapTap({ detail: { latitude: lat, longitude: lng } })
        })
        
        // 添加地图拖动结束事件监听
        this.mapInstance.on('dragend', () => {
          const center = this.mapInstance.getCenter()
          const { lat, lng } = center
          this.mapCenter = { latitude: lat, longitude: lng }
        })
        
        // 加载用户位置
        this.loadUserLocation()
        
        // 加载附近任务标记
        this.loadNearbyTasks()
      })
    },

    loadUserLocation() {
      // 直接使用浏览器原生定位API，避免uni-app坐标转换问题
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const { latitude, longitude } = position.coords
            
            this.userLocation = {
              latitude,
              longitude
            }
            this.mapCenter = {
              latitude,
              longitude
            }

            // 如果地图实例已初始化，更新地图中心
            if (this.mapInstance) {
              this.mapInstance.setCenter(new TMap.LatLng(latitude, longitude))
            }

            // 添加用户位置标记
            this.addUserLocationMarker(latitude, longitude)
            
            // 获取详细地址
            this.getAddressFromCoords(latitude, longitude)
          },
          (err) => {
            console.error('获取位置失败', err)
            uni.showToast({
              title: '获取位置失败',
              icon: 'none'
            })
          },
          {
            enableHighAccuracy: true,
            timeout: 5000,
            maximumAge: 0
          }
        )
      } else {
        uni.showToast({
          title: '浏览器不支持地理定位',
          icon: 'none'
        })
      }
    },
    
    // 添加用户位置标记
    addUserLocationMarker(latitude, longitude) {
      // 清除现有标记
      this.clearMapMarkers()
      
      // 添加用户位置标记
      const userMarker = new TMap.MultiMarker({
        map: this.mapInstance,
        geometries: [{
          id: 'user',
          position: new TMap.LatLng(latitude, longitude),
          styleId: 'userMarkerStyle'
        }],
        styles: {
          userMarkerStyle: {
            width: 30,
            height: 30,
            anchor: { x: 15, y: 30 },
            src: 'https://mapapi.qq.com/web/lbs/javascriptGL/demo/img/location.png' // 使用腾讯地图示例图标
          }
        }
      })
      
      this.mapMarkers.push(userMarker)
    },
    
    // 清除地图标记
    clearMapMarkers() {
      this.mapMarkers.forEach(layer => {
        layer.setMap(null)
      })
      this.mapMarkers = []
    },
    
    // 根据坐标获取地址
    getAddressFromCoords(latitude, longitude) {
      // 使用JSONP解决跨域问题
      const callbackName = `jsonp_${Date.now()}`
      const url = `https://apis.map.qq.com/ws/geocoder/v1/?location=${latitude},${longitude}&key=PROBZ-W7JCI-NTUGC-UQYP7-2HRMH-TEFQN&output=jsonp&callback=${callbackName}`
      
      // 创建script标签
      const script = document.createElement('script')
      script.src = url
      script.type = 'text/javascript'
      
      // 定义回调函数
      window[callbackName] = (res) => {
        if (res.status === 0) {
          // 更新当前位置信息
          this.selectedLocation = {
            name: res.result.formatted_addresses.recommend,
            address: res.result.address,
            latitude,
            longitude
          }
        }
        // 移除script标签和回调函数
        document.body.removeChild(script)
        delete window[callbackName]
      }
      
      // 添加到页面
      document.body.appendChild(script)
    },

    loadNearbyTasks() {
      // 加载附近任务标记
      const taskMarkers = []
      
      this.nearbyTasks.forEach((task, index) => {
        taskMarkers.push({
          id: task.id,
          position: new TMap.LatLng(task.latitude, task.longitude),
          styleId: 'taskMarkerStyle'
        })
      })
      
      // 创建任务标记图层
      const taskMarkerLayer = new TMap.MultiMarker({
        map: this.mapInstance,
        geometries: taskMarkers,
        styles: {
          taskMarkerStyle: {
            width: 30,
            height: 30,
            anchor: { x: 15, y: 30 },
            src: 'https://mapapi.qq.com/web/lbs/javascriptGL/demo/img/marker.png' // 使用腾讯地图示例图标
          }
        }
      })
      
      // 添加任务标记点击事件
      taskMarkerLayer.on('click', (evt) => {
        const markerId = evt.geometry.id
        const task = this.nearbyTasks.find(t => t.id === markerId)
        if (task) {
          this.selectedTask = task
        }
      })
      
      this.mapMarkers.push(taskMarkerLayer)
    },

    onSearch() {
      if (!this.searchKeyword) {
        return
      }

      // 使用腾讯地图的地点搜索API
      const callbackName = `jsonp_${Date.now()}`
      const url = `https://apis.map.qq.com/ws/place/v1/search/?keyword=${encodeURIComponent(this.searchKeyword)}&boundary=nearby(${this.mapCenter.latitude},${this.mapCenter.longitude},1000)&key=PROBZ-W7JCI-NTUGC-UQYP7-2HRMH-TEFQN&output=jsonp&callback=${callbackName}`
      
      // 创建script标签
      const script = document.createElement('script')
      script.src = url
      script.type = 'text/javascript'
      
      // 定义回调函数
      window[callbackName] = (res) => {
        if (res.status === 0) {
          // 处理搜索结果
          this.searchResults = res.data.map(item => ({
            name: item.title,
            address: item.address,
            distance: `${Math.round(item._distance)}m`,
            latitude: item.location.lat,
            longitude: item.location.lng
          }))
        } else {
          // 搜索失败，显示空结果
          this.searchResults = []
          uni.showToast({
            title: '搜索失败，请重试',
            icon: 'none'
          })
        }
        // 移除script标签和回调函数
        document.body.removeChild(script)
        delete window[callbackName]
      }
      
      // 添加到页面
      document.body.appendChild(script)
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

      // 移动地图到选中位置
      if (this.mapInstance) {
        this.mapInstance.setCenter(new TMap.LatLng(item.latitude, item.longitude))
      }
      
      // 添加选中位置标记
      const selectedMarkerLayer = new TMap.MultiMarker({
        map: this.mapInstance,
        geometries: [{
          id: 'selected-' + Date.now(),
          position: new TMap.LatLng(item.latitude, item.longitude),
          styleId: 'selectedMarkerStyle'
        }],
        styles: {
          selectedMarkerStyle: {
            width: 40,
            height: 40,
            anchor: { x: 20, y: 40 },
            src: 'https://mapapi.qq.com/web/lbs/javascriptGL/demo/img/marker.png' // 使用腾讯地图示例图标
          }
        }
      })
      
      this.mapMarkers.push(selectedMarkerLayer)
      
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
        // 移动地图到用户位置
        if (this.mapInstance) {
          this.mapInstance.setCenter(new TMap.LatLng(this.userLocation.latitude, this.userLocation.longitude))
          this.mapInstance.setZoom(15)
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
        if (this.mapInstance) {
          this.mapInstance.setZoom(this.mapScale)
        }
      }
    },

    zoomOut() {
      if (this.mapScale > 5) {
        this.mapScale--
        if (this.mapInstance) {
          this.mapInstance.setZoom(this.mapScale)
        }
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

/* 地图中心点标记 */
.map-center-marker {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 999;
  pointer-events: none; /* 允许点击穿透到地图 */
}

.red-dot {
  width: 20rpx;
  height: 20rpx;
  background: #f44336;
  border-radius: 50%;
  border: 4rpx solid white;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.3);
  z-index: 1000;
  animation: pulse 1.5s infinite;
}

.red-dot-shadow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 30rpx;
  height: 30rpx;
  background: rgba(244, 67, 54, 0.3);
  border-radius: 50%;
  z-index: 999;
  animation: shadow-pulse 1.5s infinite;
}

/* 脉冲动画 */
@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.8;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes shadow-pulse {
  0% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.5;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.5);
    opacity: 0.2;
  }
  100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.5;
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
