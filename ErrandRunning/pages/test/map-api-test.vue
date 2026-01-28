<template>
  <view class="map-api-test">
    <view class="page-header">
      <text class="page-title">腾讯地图API测试</text>
    </view>
    
    <scroll-view class="test-content" scroll-y>
      <!-- 测试结果展示 -->
      <view class="result-section">
        <text class="section-title">测试结果</text>
        <view class="result-card" v-if="testResults.length > 0">
          <view class="result-item" v-for="(result, index) in testResults" :key="index">
            <text class="result-type" :class="{ success: result.success, error: !result.success }">{{ result.type }}</text>
            <text class="result-message">{{ result.message }}</text>
            <view class="result-detail" v-if="result.data">
              <text>{{ JSON.stringify(result.data, null, 2) }}</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 逆地理编码测试 -->
      <view class="test-section">
        <text class="section-title">1. 逆地理编码测试</text>
        <view class="test-inputs">
          <view class="input-item">
            <text class="input-label">纬度：</text>
            <input v-model="geocoderLat" placeholder="请输入纬度" type="digit" />
          </view>
          <view class="input-item">
            <text class="input-label">经度：</text>
            <input v-model="geocoderLng" placeholder="请输入经度" type="digit" />
          </view>
        </view>
        <button class="test-btn" @click="testReverseGeocoding">测试逆地理编码</button>
      </view>
      
      <!-- 地点搜索测试 -->
      <view class="test-section">
        <text class="section-title">2. 地点搜索测试</text>
        <view class="test-inputs">
          <view class="input-item">
            <text class="input-label">关键词：</text>
            <input v-model="searchKeyword" placeholder="请输入搜索关键词" />
          </view>
          <view class="input-item">
            <text class="input-label">中心纬度：</text>
            <input v-model="searchLat" placeholder="请输入中心纬度" type="digit" />
          </view>
          <view class="input-item">
            <text class="input-label">中心经度：</text>
            <input v-model="searchLng" placeholder="请输入中心经度" type="digit" />
          </view>
        </view>
        <button class="test-btn" @click="testPlaceSearch">测试地点搜索</button>
      </view>
      
      <!-- 地图显示测试 -->
      <view class="test-section">
        <text class="section-title">3. 地图显示测试</text>
        <view class="map-container">
          <!-- 手动创建地图容器 -->
          <div id="mapContainer" class="map"></div>
        </view>
        <button class="test-btn" @click="testMapLocation">获取当前位置</button>
      </view>
      
      <!-- 清除测试结果 -->
      <view class="test-section">
        <button class="clear-btn" @click="clearResults">清除测试结果</button>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      // 测试结果列表
      testResults: [],
      
      // 逆地理编码测试数据
      geocoderLat: '39.908823',
      geocoderLng: '116.397470',
      
      // 地点搜索测试数据
      searchKeyword: '北京大学',
      searchLat: '39.908823',
      searchLng: '116.397470',
      
      // 地图测试数据
      mapCenter: {
        latitude: 39.908823,
        longitude: 116.397470
      },
      markers: [],
      mapInstance: null,
      
      // 腾讯地图API密钥
      mapKey: 'PROBZ-W7JCI-NTUGC-UQYP7-2HRMH-TEFQN'
    }
  },
  
  mounted() {
    // 初始化腾讯地图
    this.initMap()
  },
  
  methods: {
    // 初始化腾讯地图
    initMap() {
      // 确保DOM已加载完成
      this.$nextTick(() => {
        // 创建地图实例
        this.mapInstance = new TMap.Map('mapContainer', {
          center: new TMap.LatLng(this.mapCenter.latitude, this.mapCenter.longitude),
          zoom: 15
        })
        
        // 添加标记
        this.updateMapMarkers()
      })
    },
    
    // 更新地图中心
    updateMapCenter(latitude, longitude) {
      if (this.mapInstance) {
        this.mapInstance.setCenter(new TMap.LatLng(latitude, longitude))
      }
    },
    
    // 更新地图标记
    updateMapMarkers() {
      if (this.mapInstance && this.markers.length > 0) {
        // 清除现有标记
        this.mapInstance.removeOverlays()
        
        // 添加新标记
        this.markers.forEach((marker, index) => {
          const markerLayer = new TMap.MultiMarker({
            map: this.mapInstance,
            geometries: [{
              id: index.toString(),
              position: new TMap.LatLng(marker.latitude, marker.longitude),
              styleId: 'markerStyle'
            }],
            styles: {
              markerStyle: {
                width: 30,
                height: 30,
                anchor: { x: 15, y: 30 }
              }
            }
          })
        })
      }
    },
    
    // 添加测试结果
    addTestResult(type, message, data = null, success = true) {
      this.testResults.unshift({
        type,
        message,
        data,
        success,
        time: new Date().toLocaleString()
      })
    },
    
    // 清除测试结果
    clearResults() {
      this.testResults = []
    },
    
    // 测试逆地理编码
    testReverseGeocoding() {
      if (!this.geocoderLat || !this.geocoderLng) {
        this.addTestResult('逆地理编码', '请输入完整的经纬度', null, false)
        return
      }
      
      // 使用JSONP解决跨域问题
      const callbackName = `jsonp_${Date.now()}`
      const url = `https://apis.map.qq.com/ws/geocoder/v1/?location=${this.geocoderLat},${this.geocoderLng}&key=${this.mapKey}&output=jsonp&callback=${callbackName}`
      
      // 创建script标签
      const script = document.createElement('script')
      script.src = url
      script.type = 'text/javascript'
      
      // 定义回调函数
      window[callbackName] = (res) => {
        if (res.status === 0) {
          this.addTestResult('逆地理编码', '请求成功', res)
          // 更新地图中心和标记
          const lat = parseFloat(this.geocoderLat)
          const lng = parseFloat(this.geocoderLng)
          
          this.mapCenter = {
            latitude: lat,
            longitude: lng
          }
          this.markers = [{
            id: 1,
            latitude: lat,
            longitude: lng,
            iconPath: '/static/marker-selected.png',
            width: 30,
            height: 30,
            title: res.result.formatted_addresses.recommend
          }]
          
          // 更新地图实例
          this.updateMapCenter(lat, lng)
          this.updateMapMarkers()
        } else {
          this.addTestResult('逆地理编码', `请求失败：${res.message}`, res, false)
        }
        // 移除script标签和回调函数
        document.body.removeChild(script)
        delete window[callbackName]
      }
      
      // 添加到页面
      document.body.appendChild(script)
    },
    
    // 测试地点搜索
    testPlaceSearch() {
      if (!this.searchKeyword) {
        this.addTestResult('地点搜索', '请输入搜索关键词', null, false)
        return
      }
      
      // 使用JSONP解决跨域问题
      const callbackName = `jsonp_${Date.now()}`
      const url = `https://apis.map.qq.com/ws/place/v1/search/?keyword=${encodeURIComponent(this.searchKeyword)}&boundary=nearby(${this.searchLat || 39.908823},${this.searchLng || 116.397470},1000)&key=${this.mapKey}&output=jsonp&callback=${callbackName}`
      
      // 创建script标签
      const script = document.createElement('script')
      script.src = url
      script.type = 'text/javascript'
      
      // 定义回调函数
      window[callbackName] = (res) => {
        if (res.status === 0) {
          this.addTestResult('地点搜索', `找到 ${res.count} 个结果`, res)
          // 更新地图标记
          if (res.data.length > 0) {
            const firstResult = res.data[0]
            this.mapCenter = {
              latitude: firstResult.location.lat,
              longitude: firstResult.location.lng
            }
            this.markers = res.data.map((item, index) => ({
              id: index + 1,
              latitude: item.location.lat,
              longitude: item.location.lng,
              iconPath: '/static/marker-selected.png',
              width: 30,
              height: 30,
              title: item.title
            }))
            
            // 更新地图实例
            this.updateMapCenter(this.mapCenter.latitude, this.mapCenter.longitude)
            this.updateMapMarkers()
          }
        } else {
          this.addTestResult('地点搜索', `请求失败：${res.message}`, res, false)
        }
        // 移除script标签和回调函数
        document.body.removeChild(script)
        delete window[callbackName]
      }
      
      // 添加到页面
      document.body.appendChild(script)
    },
    
    // 测试地图定位
    testMapLocation() {
      // 直接使用浏览器原生定位API，避免uni-app坐标转换问题
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const { latitude, longitude } = position.coords
            // 直接使用WGS84坐标
            this.addTestResult('获取位置', '定位成功', {
              latitude,
              longitude,
              errMsg: '定位成功',
              type: 'wgs84'
            })
            // 更新地图中心和标记
            this.mapCenter = {
              latitude,
              longitude
            }
            this.markers = [{
              id: 1,
              latitude,
              longitude,
              iconPath: '/static/marker-current.png',
              width: 30,
              height: 30,
              title: '当前位置'
            }]
            // 更新地图实例
            this.updateMapCenter(latitude, longitude)
            this.updateMapMarkers()
            // 更新逆地理编码测试数据
            this.geocoderLat = latitude.toString()
            this.geocoderLng = longitude.toString()
          },
          (error) => {
            let errorMsg = '定位失败'
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
            this.addTestResult('获取位置', errorMsg, error, false)
          },
          {
            enableHighAccuracy: true,
            timeout: 5000,
            maximumAge: 0
          }
        )
      } else {
        this.addTestResult('获取位置', '浏览器不支持地理定位', null, false)
      }
    },
    
    // 地图点击事件
    onMapTap(e) {
      const { latitude, longitude } = e.detail
      // 更新逆地理编码测试数据
      this.geocoderLat = latitude.toString()
      this.geocoderLng = longitude.toString()
      // 自动测试逆地理编码
      this.testReverseGeocoding()
    }
  }
}
</script>

<style scoped lang="scss">
.map-api-test {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.page-header {
  height: 88rpx;
  padding: 0 30rpx;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1rpx solid #f0f0f0;
  
  .page-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
  }
}

.test-content {
  flex: 1;
  padding: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.test-section {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.test-inputs {
  margin-bottom: 20rpx;
}

.input-item {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  
  .input-label {
    width: 120rpx;
    font-size: 28rpx;
    color: #666;
  }
  
  input {
    flex: 1;
    height: 70rpx;
    padding: 0 20rpx;
    background: #f5f5f5;
    border-radius: 12rpx;
    font-size: 28rpx;
    color: #333;
  }
}

.test-btn {
  width: 100%;
  height: 80rpx;
  background: #2196f3;
  color: white;
  border: none;
  border-radius: 40rpx;
  font-size: 32rpx;
  font-weight: bold;
  margin-top: 10rpx;
}

.clear-btn {
  width: 100%;
  height: 80rpx;
  background: #f44336;
  color: white;
  border: none;
  border-radius: 40rpx;
  font-size: 32rpx;
  font-weight: bold;
}

.result-section {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.result-card {
  background: #fafafa;
  border-radius: 12rpx;
  padding: 20rpx;
  max-height: 400rpx;
  overflow-y: auto;
}

.result-item {
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #e0e0e0;
  
  &:last-child {
    margin-bottom: 0;
    padding-bottom: 0;
    border-bottom: none;
  }
}

.result-type {
  font-size: 28rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 10rpx;
  
  &.success {
    color: #4caf50;
  }
  
  &.error {
    color: #f44336;
  }
}

.result-message {
  font-size: 26rpx;
  color: #666;
  display: block;
  margin-bottom: 10rpx;
}

.result-detail {
  background: white;
  padding: 20rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
  color: #333;
  overflow-x: auto;
  
  text {
    font-family: monospace;
    white-space: pre-wrap;
  }
}

.map-container {
  width: 100%;
  height: 400rpx;
  margin-bottom: 20rpx;
  border-radius: 12rpx;
  overflow: hidden;
  background: #e0e0e0;
}

.map {
  width: 100%;
  height: 100%;
}
</style>