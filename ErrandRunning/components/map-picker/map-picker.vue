<template>
  <view class="map-picker-container">
    <!-- 地图容器 -->
    <map
      id="errandMap"
      class="map"
      :latitude="mapCenter.latitude"
      :longitude="mapCenter.longitude"
      :scale="mapScale"
      :markers="markers"
      :polyline="polyline"
      :show-location="true"
      :enable-3D="true"
      :enable-overlooking="true"
      :enable-zoom="true"
      :enable-scroll="true"
      @markertap="onMarkerTap"
      @tap="onMapTap"
    ></map>
    
    <!-- 地图操作按钮 -->
    <view class="map-controls">
      <view class="control-btn" @click="getCurrentLocation">
        <text class="iconfont icon-location"></text>
        <text>定位</text>
      </view>
      <view class="control-btn" @click="resetMap">
        <text class="iconfont icon-reset"></text>
        <text>重置</text>
      </view>
      <view class="control-btn" @click="showRoute" v-if="showRouteBtn">
        <text class="iconfont icon-route"></text>
        <text>路线</text>
      </view>
    </view>
    
    <!-- 位置信息卡片 -->
    <view class="location-info" v-if="selectedLocation">
      <view class="location-card">
        <view class="location-header">
          <text class="location-name">{{ selectedLocation.name || '选择的位置' }}</text>
          <view class="btn-close" @click="clearSelection">
            <text>✕</text>
          </view>
        </view>
        <view class="location-detail">
          <text class="address">{{ selectedLocation.address || selectedLocation.addressStr || '' }}</text>
        </view>
        <view class="location-coords">
          <text>经度: {{ selectedLocation.longitude.toFixed(6) }}</text>
          <text>纬度: {{ selectedLocation.latitude.toFixed(6) }}</text>
        </view>
        <view class="location-actions">
          <button class="btn btn-primary" @click="confirmLocation">确认位置</button>
        </view>
      </view>
    </view>
    
    <!-- 搜索框 -->
    <view class="search-box">
      <input
        class="search-input"
        v-model="searchKeyword"
        placeholder="搜索地点"
        @confirm="searchLocation"
      />
      <view class="search-btn" @click="searchLocation">
        <text>搜索</text>
      </view>
    </view>
    
    <!-- 搜索结果列表 -->
    <view class="search-results" v-if="searchResults.length > 0">
      <scroll-view scroll-y class="result-list">
        <view
          class="result-item"
          v-for="(item, index) in searchResults"
          :key="index"
          @click="selectSearchResult(item)"
        >
          <view class="result-name">{{ item.name }}</view>
          <view class="result-address">{{ item.address }}</view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'MapPicker',
  
  props: {
    // 初始位置
    initialLocation: {
      type: Object,
      default: () => ({
        latitude: 39.909187,
        longitude: 116.397451
      })
    },
    // 是否显示路线规划按钮
    showRouteBtn: {
      type: Boolean,
      default: false
    },
    // 地图缩放级别
    defaultScale: {
      type: Number,
      default: 16
    }
  },
  
  data() {
    return {
      mapCenter: {
        latitude: this.initialLocation.latitude,
        longitude: this.initialLocation.longitude
      },
      mapScale: this.defaultScale,
      markers: [],
      polyline: [],
      selectedLocation: null,
      searchKeyword: '',
      searchResults: [],
      mapContext: null
    }
  },
  
  computed: {
    // 移除未定义的mapState引用
  },
  
  mounted() {
    this.initMap()
    this.getCurrentLocation()
  },
  
  methods: {
    // 初始化地图
    initMap() {
      this.mapContext = uni.createMapContext('errandMap', this)
      this.addCurrentLocationMarker()
    },
    
    // 添加当前位置标记
    addCurrentLocationMarker() {
      uni.getLocation({
        type: 'gcj02',
        success: (res) => {
          this.markers = [
            {
              id: 1,
              latitude: res.latitude,
              longitude: res.longitude,
              iconPath: '/static/marker-current.png',
              width: 30,
              height: 30,
              title: '我的位置'
            }
          ]
        }
      })
    },
    
    // 获取当前位置
    getCurrentLocation() {
      uni.getLocation({
        type: 'gcj02',
        altitude: true,
        success: (res) => {
          const location = {
            latitude: res.latitude,
            longitude: res.longitude,
            altitude: res.altitude,
            accuracy: res.accuracy,
            name: '我的位置'
          }
          
          this.updateLocation(location)
          this.mapCenter = {
            latitude: res.latitude,
            longitude: res.longitude
          }
          
          this.addCurrentLocationMarker()
          
          // 获取详细地址
          this.getReverseGeocoding(res.latitude, res.longitude)
        },
        fail: (err) => {
          uni.showToast({
            title: '获取位置失败',
            icon: 'none'
          })
        }
      })
    },
    
    // 逆地理编码获取地址
    getReverseGeocoding(latitude, longitude) {
      // 这里应该调用地图API获取详细地址
      // 暂时使用模拟数据
      uni.request({
        url: `https://apis.map.qq.com/ws/geocoder/v1/`,
        data: {
          location: `${latitude},${longitude}`,
          key: 'YOUR_MAP_KEY'
        },
        success: (res) => {
          if (res.data.status === 0) {
            const location = {
              ...this.currentLocation,
              address: res.data.result.address,
              addressStr: res.data.result.formatted_addresses.recommend
            }
            this.updateLocation(location)
          }
        }
      })
    },
    
    // 地图点击事件
    onMapTap(e) {
      const { latitude, longitude } = e.detail
      this.mapCenter = { latitude, longitude }
      
      // 添加选中标记
      this.markers = [
        ...this.markers.filter(m => m.id !== 2),
        {
          id: 2,
          latitude,
          longitude,
          iconPath: '/static/marker-selected.png',
          width: 30,
          height: 30,
          title: '选中位置'
        }
      ]
      
      this.selectedLocation = {
        latitude,
        longitude
      }
      
      // 获取地址
      this.getReverseGeocoding(latitude, longitude)
    },
    
    // 标记点击事件
    onMarkerTap(e) {
      const marker = this.markers.find(m => m.id === e.detail.markerId)
      if (marker) {
        this.selectedLocation = {
          latitude: marker.latitude,
          longitude: marker.longitude,
          name: marker.title
        }
      }
    },
    
    // 搜索地点
    searchLocation() {
      if (!this.searchKeyword.trim()) {
        uni.showToast({
          title: '请输入搜索关键词',
          icon: 'none'
        })
        return
      }
      
      // 这里应该调用地图API搜索
      // 暂时使用模拟数据
      uni.request({
        url: `https://apis.map.qq.com/ws/place/v1/search/`,
        data: {
          keyword: this.searchKeyword,
          boundary: `nearby(${this.mapCenter.latitude},${this.mapCenter.longitude},1000)`,
          key: 'YOUR_MAP_KEY'
        },
        success: (res) => {
          if (res.data.status === 0) {
            this.searchResults = res.data.data.map(item => ({
              name: item.title,
              address: item.address,
              latitude: item.location.lat,
              longitude: item.location.lng
            }))
          }
        }
      })
    },
    
    // 选择搜索结果
    selectSearchResult(item) {
      this.mapCenter = {
        latitude: item.latitude,
        longitude: item.longitude
      }
      
      this.markers = [
        ...this.markers.filter(m => m.id !== 2),
        {
          id: 2,
          latitude: item.latitude,
          longitude: item.longitude,
          iconPath: '/static/marker-selected.png',
          width: 30,
          height: 30,
          title: item.name
        }
      ]
      
      this.selectedLocation = {
        latitude: item.latitude,
        longitude: item.longitude,
        name: item.name,
        address: item.address
      }
      
      this.searchResults = []
      this.searchKeyword = ''
    },
    
    // 显示路线
    showRoute() {
      if (!this.currentLocation || !this.selectedLocation) {
        uni.showToast({
          title: '请先选择起点和终点',
          icon: 'none'
        })
        return
      }
      
      // 调用路线规划API
      uni.request({
        url: `https://apis.map.qq.com/ws/direction/v1/walking/`,
        data: {
          from: `${this.currentLocation.latitude},${this.currentLocation.longitude}`,
          to: `${this.selectedLocation.latitude},${this.selectedLocation.longitude}`,
          key: 'YOUR_MAP_KEY'
        },
        success: (res) => {
          if (res.data.status === 0) {
            const route = res.data.result.routes[0]
            const polyline = route.polyline
            
            // 解码polyline
            const points = this.decodePolyline(polyline)
            
            this.polyline = [{
              points,
              color: '#2196f3',
              width: 6,
              arrowLine: true
            }]
          }
        }
      })
    },
    
    // 解码polyline
    decodePolyline(encoded) {
      const points = []
      const len = encoded.length
      let index = 0
      let lat = 0
      let lng = 0
      
      while (index < len) {
        let b, shift = 0, result = 0
        
        do {
          b = encoded.charCodeAt(index++) - 63
          result |= (b & 0x1f) << shift
          shift += 5
        } while (b >= 0x20)
        
        const dlat = ((result & 1) ? ~(result >> 1) : (result >> 1))
        lat += dlat
        shift = 0
        result = 0
        
        do {
          b = encoded.charCodeAt(index++) - 63
          result |= (b & 0x1f) << shift
          shift += 5
        } while (b >= 0x20)
        
        const dlng = ((result & 1) ? ~(result >> 1) : (result >> 1))
        lng += dlng
        
        points.push({
          latitude: lat / 1e5,
          longitude: lng / 1e5
        })
      }
      
      return points
    },
    
    // 确认位置
    confirmLocation() {
      if (this.selectedLocation) {
        this.selectLocation(this.selectedLocation)
        this.$emit('confirm', this.selectedLocation)
      }
    },
    
    // 清除选择
    clearSelection() {
      this.selectedLocation = null
      this.markers = this.markers.filter(m => m.id !== 2)
      this.polyline = []
    },
    
    // 重置地图
    resetMap() {
      this.mapCenter = {
        latitude: this.initialLocation.latitude,
        longitude: this.initialLocation.longitude
      }
      this.mapScale = this.defaultScale
      this.clearSelection()
      this.getCurrentLocation()
    }
  }
}
</script>

<style scoped lang="scss">
.map-picker-container {
  position: relative;
  width: 100%;
  height: 100vh;
  background: #f5f5f5;
}

.map {
  width: 100%;
  height: 100%;
}

.map-controls {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  z-index: 100;
}

.control-btn {
  width: 100rpx;
  height: 100rpx;
  background: white;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.15);
  font-size: 24rpx;
  color: #666;
  
  .iconfont {
    font-size: 36rpx;
    margin-bottom: 4rpx;
  }
}

.location-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  border-radius: 30rpx 30rpx 0 0;
  padding: 30rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.location-card {
  .location-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
  }
  
  .location-name {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
  }
  
  .btn-close {
    width: 50rpx;
    height: 50rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 36rpx;
    color: #999;
  }
  
  .location-detail {
    margin-bottom: 20rpx;
  }
  
  .address {
    font-size: 28rpx;
    color: #666;
    line-height: 1.6;
  }
  
  .location-coords {
    display: flex;
    justify-content: space-between;
    margin-bottom: 30rpx;
    padding: 20rpx;
    background: #f8f8f8;
    border-radius: 12rpx;
  }
  
  .location-coords text {
    font-size: 24rpx;
    color: #999;
  }
  
  .location-actions {
    .btn {
      width: 100%;
      height: 88rpx;
      border-radius: 12rpx;
      font-size: 32rpx;
    }
  }
}

.search-box {
  position: absolute;
  top: 20rpx;
  left: 20rpx;
  right: 140rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  z-index: 100;
  
  .search-input {
    flex: 1;
    height: 70rpx;
    padding: 0 20rpx;
    background: white;
    border-radius: 35rpx;
    font-size: 28rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.1);
  }
  
  .search-btn {
    width: 120rpx;
    height: 70rpx;
    background: #2196f3;
    color: white;
    border-radius: 35rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28rpx;
  }
}

.search-results {
  position: absolute;
  top: 100rpx;
  left: 20rpx;
  right: 20rpx;
  max-height: 600rpx;
  background: white;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.15);
  z-index: 99;
  overflow: hidden;
}

.result-list {
  max-height: 600rpx;
}

.result-item {
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
  }
  
  .result-name {
    font-size: 30rpx;
    color: #333;
    margin-bottom: 10rpx;
  }
  
  .result-address {
    font-size: 24rpx;
    color: #999;
  }
}
</style>
