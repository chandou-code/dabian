<template>
  <view class="publish-container">
    <Sidebar />
    
    <view class="main-content" :class="{ 'main-content-expanded': !showSidebar }">
      <view class="publish-card">
        <view class="card-header">
          <text class="page-title">发布招领信息</text>
          <text class="page-subtitle">感谢您的善心，让失物找到回家的路</text>
        </view>
        
        <form class="publish-form" @submit.prevent="handleSubmit">
          <!-- 基本信息 -->
          <view class="form-section">
            <text class="section-title">基本信息</text>
            
            <view class="form-item">
              <text class="form-label">物品名称 *</text>
              <input 
                v-model="form.itemName" 
                class="form-input" 
                placeholder="请输入您捡到的物品名称"
                :class="{ 'input-error': errors.itemName }"
              />
              <text v-if="errors.itemName" class="error-text">{{ errors.itemName }}</text>
            </view>
            
            <view class="form-item">
              <text class="form-label">物品类别 *</text>
              <picker 
                :range="categories" 
                :value="categoryIndex"
                @change="onCategoryChange"
                class="form-picker"
              >
                <view class="picker-content" :class="{ 'picker-empty': categoryIndex === -1 }">
                  {{ categoryIndex === -1 ? '请选择物品类别' : categories[categoryIndex] }}
                </view>
              </picker>
            </view>
            
            <view class="form-item">
              <text class="form-label">捡到时间 *</text>
              <picker 
                mode="date" 
                :value="form.foundTime"
                @change="onDateChange"
                class="form-picker"
              >
                <view class="picker-content">
                  {{ form.foundTime || '请选择捡到时间' }}
                </view>
              </picker>
            </view>
            
            <view class="form-item">
              <text class="form-label">捡到地点 *</text>
              <input 
                v-model="form.foundLocation" 
                class="form-input" 
                placeholder="请输入详细的捡到地点"
                :class="{ 'input-error': errors.foundLocation }"
              />
              <text v-if="errors.foundLocation" class="error-text">{{ errors.foundLocation }}</text>
            </view>
          </view>
          
          <!-- 详细描述 -->
          <view class="form-section">
            <text class="section-title">详细描述</text>
            
            <view class="form-item">
              <text class="form-label">物品描述 *</text>
              <textarea 
                v-model="form.description" 
                class="form-textarea" 
                placeholder="请详细描述捡到物品的特征、状态等信息"
                :class="{ 'input-error': errors.description }"
                maxlength="500"
              ></textarea>
              <view class="text-counter">{{ form.description.length }}/500</view>
              <text v-if="errors.description" class="error-text">{{ errors.description }}</text>
            </view>
            
            <view class="form-item">
              <text class="form-label">联系方式 *</text>
              <input 
                v-model="form.contact" 
                class="form-input" 
                placeholder="请输入手机号或邮箱"
                :class="{ 'input-error': errors.contact }"
              />
              <text v-if="errors.contact" class="error-text">{{ errors.contact }}</text>
            </view>
            
            <view class="form-item">
              <text class="form-label">领取地点</text>
              <input 
                v-model="form.pickupLocation" 
                class="form-input" 
                placeholder="请填写领取地点（可选）"
              />
            </view>
          </view>
          
          <!-- 图片上传 -->
          <view class="form-section">
            <text class="section-title">物品图片</text>
            
            <view class="image-upload">
              <view class="upload-grid">
                <view 
                  v-for="(image, index) in form.images" 
                  :key="index" 
                  class="upload-item"
                >
                  <image :src="image" mode="aspectFill" class="uploaded-image"></image>
                  <view class="delete-btn" @click="deleteImage(index)">×</view>
                </view>
                
                <view 
                  v-if="form.images.length < 4" 
                  class="upload-placeholder"
                  @click="chooseImage"
                >
                  <text class="upload-icon">📷</text>
                  <text class="upload-text">上传图片</text>
                </view>
              </view>
              <text class="upload-tip">最多上传4张图片，支持JPG/PNG格式</text>
            </view>
          </view>
          
          <!-- 提交按钮 -->
          <view class="form-actions">
            <button 
              class="submit-btn" 
              type="submit"
              :disabled="isSubmitting"
            >
              {{ isSubmitting ? '提交中...' : '提交发布' }}
            </button>
            
            <button class="cancel-btn" @click="handleCancel">取消</button>
          </view>
        </form>
      </view>
    </view>
  </view>
</template>

<script>
import Sidebar from '@/components/Sidebar.vue'
import * as api from '@/api'
import { uploadItemImages, getItemImages } from '@/api/upload'

export default {
  name: 'PublishFound',
  components: {
    Sidebar
  },
  
  data() {
    return {
      showSidebar: true,
      categoryIndex: -1,
      categories: [
        '电子产品', '钱包证件', '书籍文具', '生活用品', 
        '衣物饰品', '体育用品', '其他物品'
      ],
      form: {
        itemName: '',
        category: '',
        foundTime: '',
        foundLocation: '',
        description: '',
        contact: '',
        pickupLocation: '',
        images: []
      },
      uploadedImages: [], // 存储已上传的图片信息
      itemId: null, // 发布成功后的物品ID
      errors: {},
      isSubmitting: false
    }
  },
  
  methods: {
    onCategoryChange(e) {
      this.categoryIndex = e.detail.value
      this.form.category = this.categories[this.categoryIndex]
    },
    
    onDateChange(e) {
      this.form.foundTime = e.detail.value
    },
    
    async chooseImage() {
      const maxCount = 6 - this.form.images.length
      
      uni.chooseImage({
        count: maxCount,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: async (res) => {
          try {
            uni.showLoading({
              title: '上传中...'
            })
            
            // 上传图片到服务器
            const files = res.tempFilePaths.map((tempPath, index) => {
              return {
                path: tempPath
              }
            })
            
            const uploadResult = await uploadItemImages(files, 'found', this.itemId)
            
            if (uploadResult && uploadResult.data) {
              // 添加到表单图片数组
              const newImages = uploadResult.data.map(img => img.url)
              this.form.images.push(...newImages)
              
              // 添加到已上传图片信息
              this.uploadedImages.push(...uploadResult.data)
              
              uni.showToast({
                title: '图片上传成功',
                icon: 'success'
              })
            }
          } catch (error) {
            console.error('图片上传失败:', error)
            uni.showToast({
              title: '图片上传失败',
              icon: 'none'
            })
          } finally {
            uni.hideLoading()
          }
        },
        fail: (err) => {
          console.error('选择图片失败:', err)
          uni.showToast({
            title: '选择图片失败',
            icon: 'none'
          })
        }
      })
    },
    
    deleteImage(index) {
      // 从表单中删除
      this.form.images.splice(index, 1)
      
      // 从已上传图片信息中删除
      this.uploadedImages.splice(index, 1)
    },
    
    validateForm() {
      this.errors = {}
      let isValid = true
      
      if (!this.form.itemName.trim()) {
        this.errors.itemName = '请输入物品名称'
        isValid = false
      }
      
      if (!this.form.category) {
        uni.showToast({
          title: '请选择物品类别',
          icon: 'none'
        })
        isValid = false
      }
      
      if (!this.form.foundTime) {
        uni.showToast({
          title: '请选择捡到时间',
          icon: 'none'
        })
        isValid = false
      }
      
      if (!this.form.foundLocation.trim()) {
        this.errors.foundLocation = '请输入捡到地点'
        isValid = false
      }
      
      if (!this.form.description.trim()) {
        this.errors.description = '请输入物品描述'
        isValid = false
      }
      
      if (!this.form.contact.trim()) {
        this.errors.contact = '请输入联系方式'
        isValid = false
      }
      
      return isValid
    },
    
    async handleSubmit() {
      if (!this.validateForm()) {
        return
      }
      
      this.isSubmitting = true
      
      try {
        // 准备表单数据
        const formData = { ...this.form }
        formData.images = JSON.stringify(this.form.images)
        formData.type = 'found' // 明确设置类型为招领
        
        // 调用真实API发布招领信息
        const response = await api.publishFoundItem(formData)
        
        // 如果有图片，将图片与物品ID关联
        if (response && response.data && this.form.images.length > 0) {
          this.itemId = response.data.id
          
          try {
            // 更新数据库中的图片关联关系
            await api.updateItemImageAssociation({
              itemId: this.itemId,
              itemType: 'found',
              imageUrls: this.form.images
            })
            console.log('图片与物品关联成功')
          } catch (imageError) {
            console.error('图片关联失败:', imageError)
            // 不影响主流程，但记录错误
          }
        }
        
        uni.showToast({
          title: '发布成功',
          icon: 'success'
        })
        
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
        
      } catch (error) {
        uni.showToast({
          title: error || '发布失败',
          icon: 'none'
        })
      } finally {
        this.isSubmitting = false
      }
    },
    
    handleCancel() {
      uni.showModal({
        title: '确认取消',
        content: '确定要取消发布吗？已填写的信息将丢失。',
        success: (res) => {
          if (res.confirm) {
            uni.navigateBack()
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.publish-container {
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

.publish-card {
  background: white;
  border-radius: 16rpx;
  padding: 40rpx;
  max-width: 800rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
}

.card-header {
  margin-bottom: 40rpx;
  text-align: center;
}

.page-title {
  display: block;
  font-size: 40rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 10rpx;
}

.page-subtitle {
  display: block;
  font-size: 28rpx;
  color: #666;
}

.publish-form {
  width: 100%;
}

.form-section {
  margin-bottom: 40rpx;
}

.section-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
  padding-bottom: 10rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.form-item {
  margin-bottom: 30rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 10rpx;
  font-weight: 500;
}

.form-input {
  width: 100%;
  height: 80rpx;
  padding: 0 20rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 28rpx;
  background: #fafafa;
}

.form-input:focus {
  border-color: #4caf50;
  background: white;
}

.form-picker {
  width: 100%;
  height: 80rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  background: #fafafa;
}

.picker-content {
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 20rpx;
  font-size: 28rpx;
  color: #333;
}

.picker-empty {
  color: #999;
}

.form-textarea {
  width: 100%;
  min-height: 200rpx;
  padding: 20rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 28rpx;
  background: #fafafa;
  resize: vertical;
}

.form-textarea:focus {
  border-color: #4caf50;
  background: white;
}

.text-counter {
  text-align: right;
  font-size: 24rpx;
  color: #999;
  margin-top: 10rpx;
}

.input-error {
  border-color: #f44336 !important;
}

.error-text {
  display: block;
  color: #f44336;
  font-size: 24rpx;
  margin-top: 10rpx;
}

.image-upload {
  margin-top: 20rpx;
}

.upload-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150rpx, 1fr));
  gap: 20rpx;
}

.upload-item {
  position: relative;
  width: 150rpx;
  height: 150rpx;
}

.uploaded-image {
  width: 100%;
  height: 100%;
  border-radius: 8rpx;
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
  font-weight: bold;
}

.upload-placeholder {
  width: 150rpx;
  height: 150rpx;
  border: 2rpx dashed #e0e0e0;
  border-radius: 8rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  cursor: pointer;
  transition: border-color 0.3s;
}

.upload-placeholder:hover {
  border-color: #4caf50;
}

.upload-icon {
  font-size: 40rpx;
}

.upload-text {
  font-size: 24rpx;
  color: #666;
}

.upload-tip {
  display: block;
  font-size: 24rpx;
  color: #999;
  margin-top: 10rpx;
}

.form-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 40rpx;
}

.submit-btn {
  flex: 1;
  height: 80rpx;
  background: #4caf50;
  color: white;
  border: none;
  border-radius: 8rpx;
  font-size: 32rpx;
  font-weight: 600;
}

.submit-btn:disabled {
  background: #ccc;
}

.cancel-btn {
  width: 200rpx;
  height: 80rpx;
  background: #f5f5f5;
  color: #666;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 32rpx;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
    padding: 20rpx;
  }
  
  .publish-card {
    padding: 30rpx 20rpx;
  }
  
  .upload-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .cancel-btn {
    width: 100%;
  }
}
</style>