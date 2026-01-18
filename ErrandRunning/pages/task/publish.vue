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
          <view class="address-card" @click="choosePickupAddress">
            <text class="address-icon">📍</text>
            <view class="address-info">
              <text class="address-text">{{ formData.pickupAddress || '点击选择取货地址' }}</text>
              <text class="address-detail">{{ formData.pickupDetail || '' }}</text>
            </view>
            <text class="arrow">›</text>
          </view>
        </view>
        
        <!-- 送达地址 -->
        <view class="form-item">
          <view class="form-label">送达地址</view>
          <view class="address-card" @click="chooseDeliveryAddress">
            <text class="address-icon">🎯</text>
            <view class="address-info">
              <text class="address-text">{{ formData.deliveryAddress || '点击选择送达地址' }}</text>
              <text class="address-detail">{{ formData.deliveryDetail || '' }}</text>
            </view>
            <text class="arrow">›</text>
          </view>
        </view>
        
        <!-- 期望时间 -->
        <view class="form-item">
          <view class="form-label">期望送达时间</view>
          <picker
            mode="date"
            :value="formData.expectedDate"
            @change="onDateChange"
          >
            <view class="picker-item">
              <text>{{ formData.expectedDate || '选择日期' }}</text>
              <text class="arrow">›</text>
            </view>
          </picker>
          <picker
            mode="time"
            :value="formData.expectedTime"
            @change="onTimeChange"
          >
            <view class="picker-item">
              <text>{{ formData.expectedTime || '选择时间' }}</text>
              <text class="arrow">›</text>
            </view>
          </picker>
        </view>
        
        <!-- 跑腿费用 -->
        <view class="form-item">
          <view class="form-label">跑腿费用</view>
          <view class="price-section">
            <text class="price-symbol">¥</text>
            <input
              class="price-input"
              type="digit"
              v-model="formData.price"
              placeholder="0.00"
            />
            <text class="price-hint">建议：{{ recommendedPrice }}元</text>
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
          <view class="image-upload">
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
        <text class="label">预估费用：</text>
        <text class="price">¥{{ formData.price || '0.00' }}</text>
      </view>
      <button class="publish-btn" @click="publishTask" :disabled="isPublishing">
        {{ isPublishing ? '发布中...' : '立即发布' }}
      </button>
    </view>
  </view>
</template>

<script>
import { createTask } from '@/api/errand'

export default {
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
      recommendedPrice: '5.00'
    }
  },
  
  computed: {
    userInfo() {
      return { phone: '13800138000' } // 模拟用户信息
    }
  },

  onLoad() {
    // 如果用户已登录，自动填充电话
    if (this.userInfo && this.userInfo.phone) {
      this.formData.phone = this.userInfo.phone
    }

    // 设置默认时间为明天
    const tomorrow = new Date()
    tomorrow.setDate(tomorrow.getDate() + 1)
    this.formData.expectedDate = this.formatDate(tomorrow)
  },
  
  onShow() {
    // 获取从地图页面返回的位置数据
    const app = getApp()
    if (app.globalData && app.globalData.selectedLocation) {
      const { type, data } = app.globalData.selectedLocation
      if (type === 'pickup') {
        this.formData.pickupAddress = data.name
        this.formData.pickupDetail = data.address
      } else if (type === 'delivery') {
        this.formData.deliveryAddress = data.name
        this.formData.deliveryDetail = data.address
      }
      // 清除全局数据
      app.globalData.selectedLocation = null
    }
  },

  methods: {
    // 选择任务类型
    selectType(type) {
      this.formData.type = type
    },
    
    // 选择取货地址
    choosePickupAddress() {
      // 在H5环境下，使用自定义地图选择页面
      uni.navigateTo({
        url: '/pages/map/index?type=pickup'
      })
    },
    
    // 选择送达地址
    chooseDeliveryAddress() {
      // 在H5环境下，使用自定义地图选择页面
      uni.navigateTo({
        url: '/pages/map/index?type=delivery'
      })
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
      
      // 地址改为选填
      // if (!this.formData.pickupAddress) {
      //   uni.showToast({
      //     title: '请选择取货地址',
      //     icon: 'none'
      //   })
      //   return false
      // }
      // 
      // if (!this.formData.deliveryAddress) {
      //   uni.showToast({
      //     title: '请选择送达地址',
      //     icon: 'none'
      //   })
      //   return false
      // }
      
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
.publish-page {
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

.scroll-content {
  flex: 1;
  padding: 20rpx;
}

.form-section {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
}

.form-item {
  padding: 30rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
  
  &:last-child {
    border-bottom: none;
  }
  
  .form-label {
    font-size: 28rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 20rpx;
  }
  
  .input-count {
    font-size: 22rpx;
    color: #999;
    float: right;
  }
}

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
  transition: all 0.3s;
  
  &.active {
    border-color: #2196f3;
    background: #e3f2fd;
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

.form-input {
  width: 100%;
  height: 70rpx;
  padding: 0 20rpx;
  background: #f5f5f5;
  border-radius: 8rpx;
  font-size: 28rpx;
}

.form-textarea {
  width: 100%;
  min-height: 150rpx;
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 8rpx;
  font-size: 28rpx;
}

.address-card {
  display: flex;
  align-items: center;
  padding: 25rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  
  .address-icon {
    font-size: 40rpx;
    margin-right: 20rpx;
  }
  
  .address-info {
    flex: 1;
    
    .address-text {
      display: block;
      font-size: 28rpx;
      color: #333;
      margin-bottom: 8rpx;
    }
    
    .address-detail {
      font-size: 24rpx;
      color: #999;
    }
  }
  
  .arrow {
    font-size: 40rpx;
    color: #999;
  }
}

.picker-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 25rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  
  text {
    font-size: 28rpx;
    color: #333;
  }
  
  .arrow {
    font-size: 32rpx;
    color: #999;
  }
}

.price-section {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #f5f5f5;
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
    font-weight: bold;
  }
  
  .price-hint {
    font-size: 24rpx;
    color: #999;
  }
}

.image-upload {
  .image-list {
    display: flex;
    flex-wrap: wrap;
    gap: 20rpx;
  }
  
  .image-item {
    position: relative;
    width: 160rpx;
    height: 160rpx;
    
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
}

.footer-actions {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  background: white;
  border-top: 1rpx solid #f0f0f0;
  gap: 20rpx;
  
  .total-price {
    flex: 1;
    
    .label {
      font-size: 28rpx;
      color: #666;
    }
    
    .price {
      font-size: 36rpx;
      font-weight: bold;
      color: #ff5722;
    }
  }
  
  .publish-btn {
    flex: 1;
    height: 80rpx;
    background: #2196f3;
    color: white;
    border: none;
    border-radius: 40rpx;
    font-size: 32rpx;
    font-weight: bold;
  }
}
</style>
