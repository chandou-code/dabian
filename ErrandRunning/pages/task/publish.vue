<template>
  <view class="publish-page">
    <view class="page-header">
      <text class="page-title">发布任务</text>
    </view>
    
    <scroll-view class="scroll-content" scroll-y>
      <view class="form-section">
        <!-- 任务类型 -->
        <view class="form-item">
          <view class="form-label">任务类型</view>
          <view class="type-grid">
            <view
              class="type-item"
              :class="{ active: formData.type === item.value }"
              v-for="item in taskTypes"
              :key="item.value"
              @click="selectType(item.value)"
            >
              <text class="type-icon">{{ item.icon }}</text>
              <text class="type-name">{{ item.name }}</text>
            </view>
          </view>
        </view>
        
        <!-- 任务标题 -->
        <view class="form-item">
          <view class="form-label">任务标题</view>
          <input
            class="form-input"
            v-model="formData.title"
            placeholder="简要描述你的任务"
            maxlength="50"
          />
          <text class="input-count">{{ formData.title.length }}/50</text>
        </view>
        
        <!-- 任务描述 -->
        <view class="form-item">
          <view class="form-label">任务描述</view>
          <textarea
            class="form-textarea"
            v-model="formData.description"
            placeholder="详细描述任务要求、注意事项等"
            maxlength="500"
            :auto-height="true"
          />
          <text class="input-count">{{ formData.description.length }}/500</text>
        </view>
        
        <!-- 取货地址 -->
        <view class="form-item">
          <view class="form-label">取货地址</view>
          <view class="address-section">
            <view class="address-preview" @click="openPickupMap">
              <view class="address-icon">📍</view>
              <view class="address-info">
                <text class="address-main">{{ formData.pickupAddress || '点击选择取货地址' }}</text>
                <text class="address-detail">{{ formData.pickupDetail || '' }}</text>
              </view>
              <view class="address-arrow">›</view>
            </view>
          </view>
        </view>
        
        <!-- 送达地址 -->
        <view class="form-item">
          <view class="form-label">送达地址</view>
          <view class="address-section">
            <view class="address-preview" @click="openDeliveryMap">
              <view class="address-icon">🎯</view>
              <view class="address-info">
                <text class="address-main">{{ formData.deliveryAddress || '点击选择送达地址' }}</text>
                <text class="address-detail">{{ formData.deliveryDetail || '' }}</text>
              </view>
              <view class="address-arrow">›</view>
            </view>
          </view>
        </view>
        
        <!-- 期望时间 -->
        <view class="form-item">
          <view class="form-label">期望送达时间</view>
          <view class="time-picker-section">
            <view class="time-picker">
              <picker
                mode="date"
                :value="formData.expectedDate"
                @change="onDateChange"
              >
                <view class="picker-content">
                  <text class="picker-label">日期</text>
                  <text class="picker-value">{{ formData.expectedDate || '选择日期' }}</text>
                  <text class="picker-arrow">›</text>
                </view>
              </picker>
            </view>
            <view class="time-picker">
              <picker
                mode="time"
                :value="formData.expectedTime"
                @change="onTimeChange"
              >
                <view class="picker-content">
                  <text class="picker-label">时间</text>
                  <text class="picker-value">{{ formData.expectedTime || '选择时间' }}</text>
                  <text class="picker-arrow">›</text>
                </view>
              </picker>
            </view>
          </view>
        </view>
        
        <!-- 跑腿费用 -->
        <view class="form-item">
          <view class="form-label">跑腿费用</view>
          <view class="price-input-section">
            <view class="price-symbol">¥</view>
            <input
              class="price-input"
              type="digit"
              v-model="formData.price"
              placeholder="0.00"
            />
            <view class="price-hint">建议：{{ recommendedPrice }}元</view>
          </view>
        </view>
        
        <!-- 联系电话 -->
        <view class="form-item">
          <view class="form-label">联系电话</view>
          <input
            class="form-input"
            type="number"
            v-model="formData.phone"
            placeholder="请输入联系电话"
            maxlength="11"
          />
        </view>
        
        <!-- 备注信息 -->
        <view class="form-item">
          <view class="form-label">备注信息（选填）</view>
          <textarea
            class="form-textarea"
            v-model="formData.remark"
            placeholder="其他需要说明的信息"
            maxlength="200"
            :auto-height="true"
          />
          <text class="input-count">{{ formData.remark.length }}/200</text>
        </view>
        
        <!-- 图片上传 -->
        <view class="form-item">
          <view class="form-label">上传图片（选填）</view>
          <view class="image-upload-section">
            <view class="image-list">
              <view
                class="image-item"
                v-for="(img, index) in formData.images"
                :key="index"
              >
                <image :src="img" mode="aspectFill" />
                <view class="delete-btn" @click="removeImage(index)">
                  <text>✕</text>
                </view>
              </view>
              <view class="add-image-btn" @click="chooseImage" v-if="formData.images.length < 9">
                <text class="add-icon">+</text>
                <text class="add-text">添加图片</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
    
    <!-- 底部按钮 -->
    <view class="footer-actions">
      <view class="total-price">
        <text class="total-label">预估费用：</text>
        <text class="total-amount">¥{{ formData.price || '0.00' }}</text>
      </view>
      <button class="publish-btn" @click="publishTask" :disabled="isPublishing">
        {{ isPublishing ? '发布中...' : '立即发布' }}
      </button>
    </view>
    
    <!-- 地图选择弹窗 -->
    <view class="map-modal" v-if="showPickupMap || showDeliveryMap">
      <view class="map-modal-header">
        <view class="modal-title">
          {{ showPickupMap ? '选择取货地址' : '选择送达地址' }}
        </view>
        <view class="modal-actions">
          <button class="modal-btn" @click="confirmMapSelection">确认</button>
          <button class="modal-btn close-btn" @click="closeMapModal">取消</button>
        </view>
      </view>
      
      <!-- 地图搜索栏 -->
      <view class="map-search-bar">
        <view class="search-box">
          <text class="search-icon">🔍</text>
          <input
            class="search-input"
            v-model="mapSearchKeyword"
            placeholder="搜索地址"
            @confirm="onMapSearch"
          />
          <text class="clear-icon" v-if="mapSearchKeyword" @click="clearSearch">✕</text>
        </view>
        <button class="location-btn" @click="locateCurrentPosition">📍</button>
      </view>
      
      <!-- 地图容器 -->
      <view class="map-content">
        <map-picker
          ref="mapPicker"
          :initialLocation="initialMapLocation"
          :showRouteBtn="false"
          @confirm="onMapConfirm"
        />
      </view>
      
      <!-- 地图选择提示 -->
      <view class="map-tip">
        <view class="tip-content">
          <text class="tip-icon">📌</text>
          <text class="tip-text">拖动地图选择位置</text>
        </view>
      </view>
      
      <!-- 搜索结果列表 -->
      <view class="search-results" v-if="searchResults.length > 0">
        <view class="result-item" v-for="(item, index) in searchResults" :key="index" @click="selectSearchResult(item)">
          <text class="result-name">{{ item.name }}</text>
          <text class="result-address">{{ item.address }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { createTask } from '@/api/errand'
import mapPicker from '@/components/map-picker/map-picker.vue'

export default {
  components: {
    mapPicker
  },
  data() {
    return {
      taskTypes: [
        { value: 'delivery', name: '快递代取', icon: '📦' },
        { value: 'food', name: '外卖代送', icon: '🍱' },
        { value: 'shopping', name: '物品购买', icon: '🛒' },
        { value: 'queue', name: '排队代办', icon: '⏰' },
        { value: 'document', name: '文件传递', icon: '📄' },
        { value: 'other', name: '其他服务', icon: '🔧' }
      ],
      formData: {
        type: 'delivery',
        title: '',
        description: '',
        pickupAddress: '',
        pickupDetail: '',
        deliveryAddress: '',
        deliveryDetail: '',
        expectedDate: '',
        expectedTime: '',
        price: '',
        phone: '',
        remark: '',
        images: []
      },
      isPublishing: false,
      recommendedPrice: '5.00',
      showPickupMap: false,
      showDeliveryMap: false,
      mapSearchKeyword: '',
      searchResults: [],
      initialMapLocation: {
        latitude: 39.908823,
        longitude: 116.397470
      },
      currentLocationType: '' // 'pickup' 或 'delivery'
    }
  },
  
  computed: {
    userInfo() {
      return { phone: '13800138000' } // 模拟用户信息
    }
  },

  onLoad() {
    // 设置默认时间为明天
    const tomorrow = new Date()
    tomorrow.setDate(tomorrow.getDate() + 1)
    this.formData.expectedDate = this.formatDate(tomorrow)
    
    // 初始化获取当前位置
    this.getCurrentLocation()
  },
  
  methods: {
    // 获取当前位置作为地图初始位置
    getCurrentLocation() {
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            this.initialMapLocation = {
              latitude: position.coords.latitude,
              longitude: position.coords.longitude
            }
          },
          (error) => {
            console.error('获取位置失败:', error)
            // 使用默认位置
          },
          {
            enableHighAccuracy: true,
            timeout: 5000,
            maximumAge: 0
          }
        )
      }
    },
    
    // 选择任务类型
    selectType(type) {
      this.formData.type = type
    },
    
    // 打开取货地址地图
    openPickupMap() {
      this.currentLocationType = 'pickup'
      this.showPickupMap = true
      // 确保地图选择器获得焦点并定位到当前位置
      this.$nextTick(() => {
        if (this.$refs.mapPicker) {
          this.$refs.mapPicker.getCurrentLocation()
        }
      })
    },
    
    // 打开送达地址地图
    openDeliveryMap() {
      this.currentLocationType = 'delivery'
      this.showDeliveryMap = true
      // 确保地图选择器获得焦点并定位到当前位置
      this.$nextTick(() => {
        if (this.$refs.mapPicker) {
          this.$refs.mapPicker.getCurrentLocation()
        }
      })
    },
    
    // 关闭地图弹窗
    closeMapModal() {
      this.showPickupMap = false
      this.showDeliveryMap = false
      this.mapSearchKeyword = ''
      this.searchResults = []
    },
    
    // 确认地图选择
    confirmMapSelection() {
      if (this.$refs.mapPicker && this.$refs.mapPicker.selectedLocation) {
        // 直接获取地图组件的选中位置并触发确认事件
        this.onMapConfirm(this.$refs.mapPicker.selectedLocation)
      }
    },
    
    // 地图选择确认回调
    onMapConfirm(location) {
      if (this.currentLocationType === 'pickup') {
        this.formData.pickupAddress = location.name || location.addressStr || location.address
        this.formData.pickupDetail = location.address
      } else if (this.currentLocationType === 'delivery') {
        this.formData.deliveryAddress = location.name || location.addressStr || location.address
        this.formData.deliveryDetail = location.address
      }
      this.closeMapModal()
    },
    
    // 地图搜索
    onMapSearch() {
      if (!this.mapSearchKeyword.trim()) return
      
      // 调用地图组件的搜索方法
      if (this.$refs.mapPicker) {
        this.$refs.mapPicker.searchLocation(this.mapSearchKeyword)
      }
    },
    
    // 清除搜索
    clearSearch() {
      this.mapSearchKeyword = ''
      this.searchResults = []
    },
    
    // 定位当前位置
    locateCurrentPosition() {
      if (this.$refs.mapPicker) {
        this.$refs.mapPicker.getCurrentLocation()
      }
    },
    
    // 选择搜索结果
    selectSearchResult(item) {
      if (this.$refs.mapPicker) {
        this.$refs.mapPicker.selectSearchResult(item)
      }
      this.searchResults = []
      this.mapSearchKeyword = ''
    },
    
    // 日期改变
    onDateChange(e) {
      this.formData.expectedDate = e.detail.value
    },
    
    // 时间改变
    onTimeChange(e) {
      this.formData.expectedTime = e.detail.value
    },
    
    // 选择图片
    chooseImage() {
      const maxCount = 9 - this.formData.images.length
      uni.chooseImage({
        count: maxCount,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          this.formData.images.push(...res.tempFilePaths)
        }
      })
    },
    
    // 删除图片
    removeImage(index) {
      this.formData.images.splice(index, 1)
    },
    
    // 上传图片
    async uploadImages() {
      const uploadPromises = this.formData.images.map(filePath => {
        return new Promise((resolve, reject) => {
          uni.uploadFile({
            url: 'http://localhost:18083/api/upload/image',
            filePath: filePath,
            name: 'file',
            header: {
              'Authorization': 'Bearer ' + uni.getStorageSync('token')
            },
            success: (res) => {
              try {
                const data = JSON.parse(res.data)
                if (data.code === 200) {
                  resolve(data.data.url)
                } else {
                  reject(new Error(data.msg || '图片上传失败'))
                }
              } catch (e) {
                reject(new Error('上传响应格式错误'))
              }
            },
            fail: (err) => {
              reject(new Error('网络错误：' + (err.errMsg || '上传失败')))
            }
          })
        })
      })
      
      try {
        return await Promise.all(uploadPromises)
      } catch (error) {
        throw error
      }
    },
    
    // 发布任务
    async publishTask() {
      // 表单验证
      if (!this.validateForm()) {
        return
      }
      
      this.isPublishing = true
      
      try {
        // 上传图片
        let imageUrls = []
        if (this.formData.images.length > 0) {
          imageUrls = await this.uploadImages()
        }
        
        // 准备任务数据
        const taskData = {
          type: this.formData.type,
          title: this.formData.title,
          description: this.formData.description,
          pickupAddress: this.formData.pickupAddress,
          pickupDetail: this.formData.pickupDetail,
          deliveryAddress: this.formData.deliveryAddress,
          deliveryDetail: this.formData.deliveryDetail,
          expectedTime: `${this.formData.expectedDate} ${this.formData.expectedTime}`,
          price: parseFloat(this.formData.price),
          phone: this.formData.phone,
          remark: this.formData.remark,
          images: imageUrls
        }
        
        const response = await createTask(taskData)
        
        if (response.code === 200) {
          uni.showToast({
            title: '发布成功',
            icon: 'success'
          })
          
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } else {
          throw new Error(response.msg || '发布失败')
        }
      } catch (error) {
        console.error('发布任务失败:', error)
        uni.showToast({
          title: error.message || '发布失败',
          icon: 'none'
        })
      } finally {
        this.isPublishing = false
      }
    },
    
    // 表单验证
    validateForm() {
      if (!this.formData.type) {
        uni.showToast({
          title: '请选择任务类型',
          icon: 'none'
        })
        return false
      }
      
      if (!this.formData.title.trim()) {
        uni.showToast({
          title: '请输入任务标题',
          icon: 'none'
        })
        return false
      }
      
      if (!this.formData.description.trim()) {
        uni.showToast({
          title: '请输入任务描述',
          icon: 'none'
        })
        return false
      }
      
      if (!this.formData.pickupAddress) {
        uni.showToast({
          title: '请选择取货地址',
          icon: 'none'
        })
        return false
      }
      
      if (!this.formData.deliveryAddress) {
        uni.showToast({
          title: '请选择送达地址',
          icon: 'none'
        })
        return false
      }
      
      if (!this.formData.expectedDate || !this.formData.expectedTime) {
        uni.showToast({
          title: '请选择期望送达时间',
          icon: 'none'
        })
        return false
      }
      
      if (!this.formData.price || parseFloat(this.formData.price) <= 0) {
        uni.showToast({
          title: '请输入跑腿费用',
          icon: 'none'
        })
        return false
      }
      
      if (!this.formData.phone) {
        uni.showToast({
          title: '请输入联系电话',
          icon: 'none'
        })
        return false
      }
      
      if (!/^1[3-9]\d{9}$/.test(this.formData.phone)) {
        uni.showToast({
          title: '手机号格式不正确',
          icon: 'none'
        })
        return false
      }
      
      return true
    },
    
    // 格式化日期
    formatDate(date) {
      const year = date.getFullYear()
      const month = (date.getMonth() + 1).toString().padStart(2, '0')
      const day = date.getDate().toString().padStart(2, '0')
      return `${year}-${month}-${day}`
    }
  }
}
</script>

<style scoped lang="scss">
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.publish-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.page-header {
  height: 88rpx;
  padding: 0 30rpx;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1rpx solid #f0f0f0;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  
  .page-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
  }
}

.scroll-content {
  flex: 1;
  padding: 20rpx;
}

.form-section {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.form-item {
  padding: 30rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
  
  &:last-child {
    border-bottom: none;
  }
  
  .form-label {
    font-size: 28rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
    display: block;
  }
  
  .input-count {
    font-size: 22rpx;
    color: #999;
    float: right;
    margin-top: 10rpx;
  }
}

/* 任务类型 */
.type-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
}

.type-item {
  padding: 30rpx 20rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 12rpx;
  text-align: center;
  transition: all 0.3s ease;
  background: white;
  
  &.active {
    border-color: #2196f3;
    background: #e3f2fd;
    transform: translateY(-2rpx);
    box-shadow: 0 4rpx 12rpx rgba(33, 150, 243, 0.2);
  }
  
  &:active {
    transform: scale(0.98);
  }
  
  .type-icon {
    font-size: 48rpx;
    display: block;
    margin-bottom: 10rpx;
  }
  
  .type-name {
    font-size: 24rpx;
    color: #333;
  }
}

/* 表单输入 */
.form-input {
  width: 100%;
  height: 80rpx;
  padding: 0 24rpx;
  background: #fafafa;
  border: 1rpx solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;
  transition: all 0.3s ease;
  
  &:focus {
    outline: none;
    background: white;
    border-color: #2196f3;
    box-shadow: 0 0 0 4rpx rgba(33, 150, 243, 0.1);
  }
}

.form-textarea {
  width: 100%;
  min-height: 180rpx;
  padding: 24rpx;
  background: #fafafa;
  border: 1rpx solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;
  resize: vertical;
  transition: all 0.3s ease;
  
  &:focus {
    outline: none;
    background: white;
    border-color: #2196f3;
    box-shadow: 0 0 0 4rpx rgba(33, 150, 243, 0.1);
  }
}

/* 地址选择 */
.address-section {
  margin-top: 10rpx;
}

.address-preview {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background: #fafafa;
  border: 1rpx solid #e0e0e0;
  border-radius: 12rpx;
  transition: all 0.3s ease;
  
  &:active {
    background: #f0f0f0;
  }
  
  .address-icon {
    font-size: 40rpx;
    margin-right: 20rpx;
    color: #2196f3;
  }
  
  .address-info {
    flex: 1;
  }
  
  .address-main {
    display: block;
    font-size: 28rpx;
    color: #333;
    margin-bottom: 8rpx;
  }
  
  .address-detail {
    display: block;
    font-size: 24rpx;
    color: #999;
    line-height: 1.4;
  }
  
  .address-arrow {
    font-size: 40rpx;
    color: #999;
  }
}

/* 时间选择 */
.time-picker-section {
  display: flex;
  gap: 20rpx;
}

.time-picker {
  flex: 1;
}

.picker-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
  background: #fafafa;
  border: 1rpx solid #e0e0e0;
  border-radius: 12rpx;
  transition: all 0.3s ease;
  
  .picker-label {
    font-size: 28rpx;
    color: #666;
  }
  
  .picker-value {
    font-size: 28rpx;
    color: #333;
    flex: 1;
    text-align: center;
  }
  
  .picker-arrow {
    font-size: 32rpx;
    color: #999;
  }
}

/* 价格输入 */
.price-input-section {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #fafafa;
  border: 1rpx solid #e0e0e0;
  border-radius: 12rpx;
  
  .price-symbol {
    font-size: 36rpx;
    color: #ff5722;
    margin-right: 10rpx;
  }
  
  .price-input {
    flex: 1;
    font-size: 48rpx;
    color: #ff5722;
    font-weight: 600;
    background: transparent;
    border: none;
    text-align: left;
    
    &:focus {
      outline: none;
    }
  }
  
  .price-hint {
    font-size: 24rpx;
    color: #999;
    margin-left: 20rpx;
  }
}

/* 图片上传 */
.image-upload-section {
  margin-top: 10rpx;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.image-item {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  
  image {
    width: 100%;
    height: 100%;
    border-radius: 12rpx;
  }
  
  .delete-btn {
    position: absolute;
    top: -10rpx;
    right: -10rpx;
    width: 40rpx;
    height: 40rpx;
    background: #f44336;
    color: white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.2);
    transition: all 0.3s ease;
    
    &:active {
      transform: scale(0.9);
    }
  }
}

.add-image-btn {
  width: 160rpx;
  height: 160rpx;
  border: 2rpx dashed #ddd;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  transition: all 0.3s ease;
  
  &:active {
    background: #f0f0f0;
  }
  
  .add-icon {
    font-size: 48rpx;
    color: #999;
    margin-bottom: 10rpx;
  }
  
  .add-text {
    font-size: 22rpx;
    color: #999;
  }
}

/* 底部操作栏 */
.footer-actions {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  background: white;
  border-top: 1rpx solid #f0f0f0;
  box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.05);
  gap: 20rpx;
}

.total-price {
  flex: 1;
  display: flex;
  align-items: center;
  
  .total-label {
    font-size: 28rpx;
    color: #666;
  }
  
  .total-amount {
    font-size: 36rpx;
    font-weight: 600;
    color: #ff5722;
    margin-left: 10rpx;
  }
}

.publish-btn {
  flex: 1;
  height: 88rpx;
  background: linear-gradient(135deg, #2196f3, #1976d2);
  color: white;
  border: none;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: 600;
  box-shadow: 0 4rpx 16rpx rgba(33, 150, 243, 0.3);
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.98);
    box-shadow: 0 2rpx 8rpx rgba(33, 150, 243, 0.2);
  }
  
  &:disabled {
    background: #bdbdbd;
    box-shadow: none;
    transform: none;
  }
}

/* 地图弹窗 */
.map-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: white;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.map-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  background: white;
  border-bottom: 1rpx solid #f0f0f0;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  z-index: 10;
  
  .modal-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
  }
  
  .modal-actions {
    display: flex;
    gap: 20rpx;
  }
  
  .modal-btn {
    padding: 12rpx 30rpx;
    border: none;
    border-radius: 24rpx;
    font-size: 28rpx;
    font-weight: 500;
    transition: all 0.3s ease;
    
    &:active {
      transform: scale(0.95);
    }
    
    &.close-btn {
      background: #f5f5f5;
      color: #666;
    }
    
    &:not(.close-btn) {
      background: #2196f3;
      color: white;
    }
  }
}

/* 地图搜索栏 */
.map-search-bar {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  background: white;
  border-bottom: 1rpx solid #f0f0f0;
  z-index: 10;
  
  .search-box {
    flex: 1;
    display: flex;
    align-items: center;
    height: 72rpx;
    background: #f5f5f5;
    border-radius: 36rpx;
    padding: 0 24rpx;
    gap: 16rpx;
  }
  
  .search-icon {
    font-size: 32rpx;
    color: #999;
  }
  
  .search-input {
    flex: 1;
    height: 100%;
    font-size: 28rpx;
    color: #333;
    background: transparent;
    border: none;
    
    &:focus {
      outline: none;
    }
  }
  
  .clear-icon {
    font-size: 28rpx;
    color: #999;
    cursor: pointer;
  }
  
  .location-btn {
    width: 72rpx;
    height: 72rpx;
    background: #f5f5f5;
    border: none;
    border-radius: 50%;
    margin-left: 20rpx;
    font-size: 36rpx;
    transition: all 0.3s ease;
    
    &:active {
      transform: scale(0.95);
      background: #e0e0e0;
    }
  }
}

/* 地图内容 */
.map-content {
  flex: 1;
  overflow: hidden;
  position: relative;
}

/* 地图提示 */
.map-tip {
  position: absolute;
  bottom: 20rpx;
  left: 50%;
  transform: translateX(-50%);
  z-index: 20;
  
  .tip-content {
    display: flex;
    align-items: center;
    padding: 16rpx 32rpx;
    background: rgba(0, 0, 0, 0.7);
    color: white;
    border-radius: 32rpx;
    font-size: 26rpx;
    gap: 10rpx;
    backdrop-filter: blur(10rpx);
  }
}

/* 搜索结果 */
.search-results {
  position: absolute;
  top: 200rpx;
  left: 20rpx;
  right: 20rpx;
  max-height: 500rpx;
  background: white;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
  z-index: 30;
  overflow: hidden;
}

.result-item {
  padding: 24rpx;
  border-bottom: 1rpx solid #f5f5f5;
  transition: all 0.3s ease;
  
  &:active {
    background: #fafafa;
  }
  
  &:last-child {
    border-bottom: none;
  }
  
  .result-name {
    display: block;
    font-size: 28rpx;
    color: #333;
    margin-bottom: 8rpx;
    font-weight: 500;
  }
  
  .result-address {
    display: block;
    font-size: 24rpx;
    color: #999;
    line-height: 1.4;
  }
}
</style>