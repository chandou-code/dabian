<template>
  <view class="map-picker-container">
    <!-- 地图容器 -->
    <div id="errandMap" class="map">
      <!-- 地图中心点红点标记 -->
      <div class="map-center-marker">
        <div class="red-dot-shadow"></div>
        <div class="red-dot"></div>
      </div>
    </div>
    
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
          <text v-if="selectedLocation.longitude">经度: {{ selectedLocation.longitude.toFixed(6) }}</text>
          <text v-if="selectedLocation.latitude">纬度: {{ selectedLocation.latitude.toFixed(6) }}</text>
        </view>
      </view>
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
      selectedLocation: null,
      mapInstance: null,
      reverseGeocodingTimer: null, // 防抖定时器
      lastGeocodingRequest: 0 // 上次请求时间戳
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
      // 确保DOM已加载完成
      this.$nextTick(() => {
        // 创建地图实例
        this.mapInstance = new TMap.Map('errandMap', {
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
          this.onMapTap({ detail: { latitude: lat, longitude: lng } })
        })
        
        // 添加地图移动事件监听，实时更新中心点位置
        this.mapInstance.on('pan', () => {
          const center = this.mapInstance.getCenter()
          const { lat, lng } = center
          this.mapCenter = { latitude: lat, longitude: lng }
        })
      })
    },
    
    // 获取当前位置
    getCurrentLocation() {
      // 直接使用浏览器原生定位API，避免uni-app坐标转换问题
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const { latitude, longitude } = position.coords
            
            // 更新地图中心
            this.mapCenter = {
              latitude,
              longitude
            }
            
            // 移动地图到当前位置
            if (this.mapInstance) {
              this.mapInstance.setCenter(new TMap.LatLng(latitude, longitude))
            }
            
            // 立即更新选中位置，显示加载状态
            this.selectedLocation = {
              latitude,
              longitude,
              name: '获取位置中...',
              address: '请稍候...'
            }
            
            // 获取详细地址
            this.getReverseGeocoding(latitude, longitude)
          },
          (error) => {
            let errorMsg = '获取位置失败'
            switch (error.code) {
              case error.PERMISSION_DENIED:
                errorMsg = '用户拒绝了定位请求'
                break
              case error.POSITION_UNAVAILABLE:
                errorMsg = '位置信息不可用'
                break
              case error.TIMEOUT:
                errorMsg = '定位超时'
                break
              case error.UNKNOWN_ERROR:
                errorMsg = '发生未知错误'
                break
            }
            uni.showToast({
              title: errorMsg,
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
    
    // 逆地理编码获取地址 - 添加防抖和节流
    getReverseGeocoding(latitude, longitude) {
      const now = Date.now()
      const requestInterval = 1000 // 请求间隔，单位：毫秒
      
      // 节流：如果距离上次请求不到1秒，直接返回
      if (now - this.lastGeocodingRequest < requestInterval) {
        return
      }
      
      // 防抖：清除之前的定时器
      if (this.reverseGeocodingTimer) {
        clearTimeout(this.reverseGeocodingTimer)
      }
      
      // 设置新的定时器，延迟500毫秒执行
      this.reverseGeocodingTimer = setTimeout(() => {
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
            // 更新选中位置
            this.selectedLocation = {
              ...this.selectedLocation,
              address: res.result.address,
              addressStr: res.result.formatted_addresses.recommend,
              name: res.result.formatted_addresses.recommend
            }
          }
          // 移除script标签和回调函数
          document.body.removeChild(script)
          delete window[callbackName]
        }
        
        // 添加到页面
        document.body.appendChild(script)
        
        // 更新上次请求时间
        this.lastGeocodingRequest = Date.now()
      }, 500) // 防抖延迟，单位：毫秒
    },
    
    // 地图点击事件
    onMapTap(e) {
      const { latitude, longitude } = e.detail
      this.mapCenter = { latitude, longitude }
      
      // 立即更新选中位置，显示加载状态
      this.selectedLocation = {
        latitude,
        longitude,
        name: '获取位置中...',
        address: '请稍候...'
      }
      
      // 获取地址
      this.getReverseGeocoding(latitude, longitude)
    },
    
    // 清除选择
    clearSelection() {
      this.selectedLocation = null
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
  height: 100%;
  background: #f5f5f5;
  overflow: hidden;
}

.map {
  width: 100%;
  height: 100%;
  position: relative;
}

/* 地图中心点红点标记 */
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
  max-height: 400rpx;
  overflow: auto;
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
}
</style>