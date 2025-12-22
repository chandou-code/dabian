<template>
  <view class="publish-container">
    <Sidebar />
    
    <view class="main-content" :class="{ 'main-content-expanded': !showSidebar }">
      <view class="publish-card">
        <view class="card-header">
          <text class="page-title">发布失物信息</text>
          <text class="page-subtitle">请详细描述您丢失的物品，帮助好心人找到它</text>
        </view>
        
        <form class="publish-form" @submit="handleSubmit">
          <!-- 基本信息 -->
          <view class="form-section">
            <text class="section-title">基本信息</text>
            
            <view class="form-item">
              <text class="form-label">物品名称 *</text>
              <input 
                v-model="form.itemName" 
                class="form-input" 
                placeholder="请输入物品名称"
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
              <text class="form-label">丢失时间 *</text>
              <picker 
                mode="date" 
                :value="form.lostTime"
                @change="onDateChange"
                class="form-picker"
              >
                <view class="picker-content">
                  {{ form.lostTime || '请选择丢失时间' }}
                </view>
              </picker>
            </view>
            
            <view class="form-item">
              <text class="form-label">丢失地点 *</text>
              <input 
                v-model="form.lostLocation" 
                class="form-input" 
                placeholder="请输入详细的丢失地点"
                :class="{ 'input-error': errors.lostLocation }"
              />
              <text v-if="errors.lostLocation" class="error-text">{{ errors.lostLocation }}</text>
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
                placeholder="请详细描述物品的特征、颜色、品牌等信息"
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
                  v-if="form.images.length < 6" 
                  class="upload-placeholder"
                  @click="chooseImage"
                >
                  <text class="upload-icon">📷</text>
                  <text class="upload-text">上传图片</text>
                </view>
              </view>
              <text class="upload-tip">最多上传6张图片，支持JPG/PNG格式，单张不超过5MB</text>
            </view>
          </view>
          
          <!-- AI识别 -->
          <view class="form-section">
            <view class="ai-section">
              <view class="ai-header">
                <text class="ai-title">🤖 AI智能识别</text>
                <button 
                  class="ai-btn" 
                  @click="aiRecognition"
                  :disabled="isAiProcessing"
                >
                  {{ isAiProcessing ? '识别中...' : '智能生成描述' }}
                </button>
              </view>
              
              <view v-if="aiResult" class="ai-result">
                <text class="ai-label">AI识别结果：</text>
                <text class="ai-content">{{ aiResult }}</text>
                <button class="ai-apply-btn" @click="applyAiResult">应用此描述</button>
              </view>
            </view>
          </view>
          
          <!-- 提交按钮 -->
          <view class="form-actions">
            <button 
              class="submit-btn" 
              type="submit"
              :disabled="isSubmitting"
              @click="handleSubmit"
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
  name: 'PublishLost',
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
        lostTime: '',
        lostLocation: '',
        description: '',
        contact: '',
        images: []
      },
      uploadedImages: [], // 存储已上传的图片信息
      itemId: null, // 发布成功后的物品ID
      errors: {},
      isSubmitting: false,
      isAiProcessing: false,
      aiResult: ''
    }
  },
  
  methods: {
    onCategoryChange(e) {
      this.categoryIndex = e.detail.value
      this.form.category = this.categories[this.categoryIndex]
    },
    
    onDateChange(e) {
      this.form.lostTime = e.detail.value
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
            
            const uploadResult = await uploadItemImages(files, 'lost', this.itemId)
            
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
    
    async aiRecognition() {
      if (this.form.images.length === 0) {
        uni.showToast({
          title: '请先上传图片',
          icon: 'none'
        })
        return
      }
      
      this.isAiProcessing = true
      
      try {
        // 模拟AI识别
        await new Promise(resolve => setTimeout(resolve, 2000))
        
        this.aiResult = `根据图片分析，这可能是一个${this.form.itemName || '物品'}，主要特征包括：外观颜色、材质特点和尺寸大小。建议在描述中补充更多细节信息，如品牌型号、特殊标记等，以便更好识别。`
        
        uni.showToast({
          title: 'AI识别完成',
          icon: 'success'
        })
      } catch (error) {
        uni.showToast({
          title: 'AI识别失败',
          icon: 'none'
        })
      } finally {
        this.isAiProcessing = false
      }
    },
    
    applyAiResult() {
      this.form.description = this.aiResult
      this.aiResult = ''
    },
    
    validateForm() {
      console.log('===== 开始表单验证 =====')
      this.errors = {}
      let isValid = true
      
      console.log('验证物品名称:', this.form.itemName)
      if (!this.form.itemName.trim()) {
        this.errors.itemName = '请输入物品名称'
        isValid = false
        console.log('物品名称验证失败:', this.errors.itemName)
      } else {
        console.log('物品名称验证通过')
      }
      
      console.log('验证物品类别:', this.form.category)
      if (!this.form.category) {
        console.log('物品类别验证失败: 未选择类别')
        uni.showToast({
          title: '请选择物品类别',
          icon: 'none'
        })
        isValid = false
      } else {
        console.log('物品类别验证通过')
      }
      
      console.log('验证丢失时间:', this.form.lostTime)
      if (!this.form.lostTime) {
        console.log('丢失时间验证失败: 未选择时间')
        uni.showToast({
          title: '请选择丢失时间',
          icon: 'none'
        })
        isValid = false
      } else {
        console.log('丢失时间验证通过')
      }
      
      console.log('验证丢失地点:', this.form.lostLocation)
      if (!this.form.lostLocation.trim()) {
        this.errors.lostLocation = '请输入丢失地点'
        isValid = false
        console.log('丢失地点验证失败:', this.errors.lostLocation)
      } else {
        console.log('丢失地点验证通过')
      }
      
      console.log('验证物品描述:', this.form.description)
      if (!this.form.description.trim()) {
        this.errors.description = '请输入物品描述'
        isValid = false
        console.log('物品描述验证失败:', this.errors.description)
      } else {
        console.log('物品描述验证通过')
      }
      
      console.log('验证联系方式:', this.form.contact)
      if (!this.form.contact.trim()) {
        this.errors.contact = '请输入联系方式'
        isValid = false
        console.log('联系方式验证失败:', this.errors.contact)
      } else {
        console.log('联系方式验证通过')
      }
      
      console.log('表单验证结果:', isValid)
      console.log('验证错误:', this.errors)
      console.log('=========================')
      return isValid
    },
    
    async handleSubmit(e) {
      console.log('===== 表单提交事件触发 =====')
      console.log('事件对象:', e)
      
      // 阻止表单默认提交行为
      if (e && e.preventDefault) {
        e.preventDefault()
        console.log('已阻止表单默认提交行为')
      }
      
      console.log('表单数据:', this.form)
      console.log('验证表单...')
      
      if (!this.validateForm()) {
        console.log('表单验证失败，终止提交')
        return
      }
      
      console.log('表单验证通过，开始提交...')
      this.isSubmitting = true
      
      try {
        console.log('调用API发布失物信息，URL:', '/items/lost-items')
        console.log('请求数据:', this.form)
        
        // 准备表单数据
        const formData = { ...this.form }
        formData.images = JSON.stringify(this.form.images)
        formData.type = 'lost' // 明确设置类型为失物
        console.log('转换后的数据:', formData)
        
        // 调用真实API发布失物信息
        const response = await api.publishLostItem(formData)
        console.log('API请求成功，响应:', response)
        
        // 如果有图片，将图片与物品ID关联
        if (response && response.data && this.form.images.length > 0) {
          this.itemId = response.data.id
          
          try {
            // 更新数据库中的图片关联关系
            await api.updateItemImageAssociation({
              itemId: this.itemId,
              itemType: 'lost',
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
        console.error('API请求失败，错误:', error)
        uni.showToast({
          title: error || '发布失败',
          icon: 'none'
        })
      } finally {
        this.isSubmitting = false
        console.log('提交过程结束，isSubmitting:', this.isSubmitting)
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
  border-color: #2196f3;
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
  border-color: #2196f3;
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
  border-color: #2196f3;
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

.ai-section {
  background: #f8f9fa;
  padding: 30rpx;
  border-radius: 12rpx;
  border: 2rpx dashed #e3f2fd;
}

.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.ai-title {
  font-size: 28rpx;
  color: #2196f3;
  font-weight: 600;
}

.ai-btn {
  background: #2196f3;
  color: white;
  border: none;
  padding: 12rpx 24rpx;
  border-radius: 6rpx;
  font-size: 24rpx;
}

.ai-btn:disabled {
  background: #ccc;
}

.ai-result {
  background: white;
  padding: 20rpx;
  border-radius: 8rpx;
  border-left: 4rpx solid #2196f3;
}

.ai-label {
  display: block;
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 10rpx;
}

.ai-content {
  display: block;
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
  margin-bottom: 15rpx;
}

.ai-apply-btn {
  background: #4caf50;
  color: white;
  border: none;
  padding: 10rpx 20rpx;
  border-radius: 6rpx;
  font-size: 24rpx;
}

.form-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 40rpx;
}

.submit-btn {
  flex: 1;
  height: 80rpx;
  background: #2196f3;
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