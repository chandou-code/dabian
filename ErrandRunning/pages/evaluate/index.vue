<template>
  <view class="evaluate-page">
    <view class="page-header">
      <view class="header-back" @click="goBack">
        <text>‹</text>
      </view>
      <text class="page-title">评价订单</text>
      <view class="header-placeholder"></view>
    </view>
    
    <scroll-view class="scroll-content" scroll-y>
      <!-- 跑腿员信息 -->
      <view class="runner-section" v-if="orderData.runner">
        <image class="runner-avatar" :src="orderData.runner.avatar || '/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg'" mode="aspectFill" />
        <view class="runner-info">
          <text class="runner-name">{{ orderData.runner.name }}</text>
          <text class="runner-tag">感谢您的使用，请为跑腿员服务评价</text>
        </view>
      </view>
      
      <!-- 评分区域 -->
      <view class="rating-section">
        <view class="section-title">综合评分</view>
        <view class="stars-container">
          <text
            class="star"
            :class="{ active: rating >= index + 1 }"
            v-for="index in 5"
            :key="index"
            @click="setRating(index + 1)"
          >
            ★
          </text>
        </view>
        <view class="rating-text">{{ getRatingText() }}</view>
      </view>
      
      <!-- 细分评分 -->
      <view class="detail-rating-section">
        <view class="detail-rating-item" v-for="item in detailRatings" :key="item.key">
          <text class="detail-label">{{ item.label }}</text>
          <view class="detail-stars">
            <text
              class="mini-star"
              :class="{ active: item.rating >= index + 1 }"
              v-for="index in 5"
              :key="index"
              @click="setDetailRating(item.key, index + 1)"
            >
              ★
            </text>
          </view>
        </view>
      </view>
      
      <!-- 标签选择 -->
      <view class="tags-section">
        <view class="section-title">服务评价</view>
        <view class="tags-grid">
          <view
            class="tag-item"
            :class="{ selected: selectedTags.includes(tag) }"
            v-for="tag in evaluationTags"
            :key="tag"
            @click="toggleTag(tag)"
          >
            {{ tag }}
          </view>
        </view>
      </view>
      
      <!-- 文字评价 -->
      <view class="comment-section">
        <view class="section-title">文字评价</view>
        <textarea
          class="comment-input"
          v-model="comment"
          placeholder="分享您的使用体验..."
          :maxlength="500"
          :auto-height="true"
        />
        <view class="input-count">{{ comment.length }}/500</view>
      </view>
      
      <!-- 图片上传 -->
      <view class="images-section">
        <view class="section-title">上传图片（选填）</view>
        <view class="image-upload">
          <view class="image-list">
            <view
              class="image-item"
              v-for="(img, index) in images"
              :key="index"
            >
              <image :src="img" mode="aspectFill" />
              <view class="delete-btn" @click="removeImage(index)">
                <text>✕</text>
              </view>
            </view>
            <view class="add-image-btn" @click="chooseImage" v-if="images.length < 6">
              <text class="add-icon">+</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 匿名选项 -->
      <view class="anonymous-section">
        <view class="anonymous-item" @click="toggleAnonymous">
          <view class="checkbox" :class="{ checked: isAnonymous }">
            <text v-if="isAnonymous">✓</text>
          </view>
          <text class="anonymous-text">匿名评价</text>
        </view>
      </view>
    </scroll-view>
    
    <!-- 提交按钮 -->
    <view class="footer-actions">
      <button class="submit-btn" @click="submitEvaluation" :disabled="isSubmitting">
        {{ isSubmitting ? '提交中...' : '提交评价' }}
      </button>
    </view>
  </view>
</template>

<script>
import { submitReview, getOrderDetail } from '@/api/errand'

export default {
  data() {
    return {
      orderId: null,
      orderData: {},
      
      rating: 0,
      detailRatings: [
        { key: 'speed', label: '配送速度', rating: 0 },
        { key: 'attitude', label: '服务态度', rating: 0 },
        { key: 'quality', label: '服务质量', rating: 0 }
      ],
      
      evaluationTags: [
        '配送及时',
        '服务态度好',
        '认真负责',
        '沟通顺畅',
        '物品完好',
        '准时到达',
        '包装完好',
        '值得推荐'
      ],
      selectedTags: [],
      
      comment: '',
      images: [],
      isAnonymous: false,
      
      isSubmitting: false
    }
  },
  
  onLoad(options) {
    if (options.orderId) {
      this.orderId = options.orderId
      this.loadOrderDetail()
    } else if (options.taskId) {
      this.orderId = options.taskId
      this.loadOrderDetail()
    }
  },
  
  async onShow() {
    // 检查当前用户是否有权限评价
    await this.checkPermission()
  },
  
  methods: {
    // 检查权限
    async checkPermission() {
      const user = uni.getStorageSync('user')
      if (!user) {
        uni.showToast({
          title: '请先登录',
          icon: 'none'
        })
        uni.navigateBack()
        return
      }
      
      // 如果还没有加载订单详情，先加载
      if (!this.orderData.taskId) {
        await this.loadOrderDetail()
      }
      
      // 检查当前用户是否是订单发布者
      if (user.id !== this.orderData.publisherId) {
        uni.showToast({
          title: '只有订单发布者可以评价',
          icon: 'none'
        })
        uni.navigateBack()
      }
    },
    
    // 加载订单详情
    async loadOrderDetail() {
      try {
        const response = await getOrderDetail(this.orderId)
        
        if (response.code === 200) {
          this.orderData = response.data
        }
      } catch (error) {
        console.error('加载订单详情失败:', error)
      }
    },
    
    // 设置评分
    setRating(value) {
      this.rating = value
      // 综合评分影响细分评分
      this.detailRatings.forEach(item => {
        item.rating = value
      })
    },
    
    // 设置细分评分
    setDetailRating(key, value) {
      const item = this.detailRatings.find(i => i.key === key)
      if (item) {
        item.rating = value
        // 更新综合评分
        const avgRating = this.detailRatings.reduce((sum, item) => sum + item.rating, 0) / 3
        this.rating = Math.round(avgRating)
      }
    },
    
    // 切换标签
    toggleTag(tag) {
      const index = this.selectedTags.indexOf(tag)
      if (index > -1) {
        this.selectedTags.splice(index, 1)
      } else {
        if (this.selectedTags.length < 3) {
          this.selectedTags.push(tag)
        } else {
          uni.showToast({
            title: '最多选择3个标签',
            icon: 'none'
          })
        }
      }
    },
    
    // 切换匿名
    toggleAnonymous() {
      this.isAnonymous = !this.isAnonymous
    },
    
    // 选择图片
    chooseImage() {
      const maxCount = 6 - this.images.length
      uni.chooseImage({
        count: maxCount,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          this.images.push(...res.tempFilePaths)
        }
      })
    },
    
    // 删除图片
    removeImage(index) {
      this.images.splice(index, 1)
    },
    
    // 提交评价
    async submitEvaluation() {
      if (this.rating === 0) {
        uni.showToast({
          title: '请给出评分',
          icon: 'none'
        })
        return
      }
      
      this.isSubmitting = true
      
      try {
        // 上传图片
        let imageUrls = []
        if (this.images.length > 0) {
          imageUrls = await this.uploadImages()
        }
        
        // 准备评价数据
        const reviewData = {
          orderId: this.orderId,
          rating: this.rating,
          detailRatings: this.detailRatings.map(item => ({
            key: item.key,
            rating: item.rating
          })),
          tags: this.selectedTags,
          comment: this.comment,
          images: imageUrls,
          isAnonymous: this.isAnonymous
        }
        
        const response = await submitReview(this.orderId, reviewData)
        
        if (response.code === 200) {
          uni.showToast({
            title: '评价成功',
            icon: 'success'
          })
          
          // 将任务ID添加到已评价列表
          const evaluatedTasks = uni.getStorageSync('evaluatedTasks') || []
          if (!evaluatedTasks.includes(this.orderId)) {
            evaluatedTasks.push(this.orderId)
            uni.setStorageSync('evaluatedTasks', evaluatedTasks)
          }
          
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        }
      } catch (error) {
        console.error('提交评价失败:', error)
        uni.showToast({
          title: '提交失败',
          icon: 'none'
        })
      } finally {
        this.isSubmitting = false
      }
    },
    
    // 上传图片
    async uploadImages() {
      const uploadPromises = this.images.map(filePath => {
        return new Promise((resolve, reject) => {
          uni.uploadFile({
            url: 'http://localhost:18080/api/upload',
            filePath: filePath,
            name: 'file',
            header: {
              'Authorization': 'Bearer mock-token'
            },
            success: (res) => {
              const data = JSON.parse(res.data)
              if (data.code === 200) {
                resolve(data.data.url)
              } else {
                reject(data)
              }
            },
            fail: reject
          })
        })
      })
      
      return await Promise.all(uploadPromises)
    },
    
    // 获取评分文本
    getRatingText() {
      const texts = ['非常差', '较差', '一般', '满意', '非常满意']
      return texts[this.rating - 1] || '请评分'
    },
    
    // 返回
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style scoped lang="scss">
.evaluate-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 30rpx;
  background: white;
  
  .header-back {
    width: 60rpx;
    font-size: 48rpx;
    color: #333;
  }
  
  .page-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
  }
  
  .header-placeholder {
    width: 60rpx;
  }
}

.scroll-content {
  flex: 1;
  padding: 20rpx;
}

.runner-section {
  display: flex;
  align-items: center;
  padding: 30rpx;
  background: white;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  
  .runner-avatar {
    width: 100rpx;
    height: 100rpx;
    border-radius: 50%;
    margin-right: 20rpx;
  }
  
  .runner-info {
    flex: 1;
    
    .runner-name {
      display: block;
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 10rpx;
    }
    
    .runner-tag {
      font-size: 24rpx;
      color: #999;
    }
  }
}

.rating-section,
.detail-rating-section,
.tags-section,
.comment-section,
.images-section,
.anonymous-section {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.stars-container {
  display: flex;
  justify-content: center;
  gap: 20rpx;
  padding: 20rpx 0;
}

.star {
  font-size: 80rpx;
  color: #e0e0e0;
  
  &.active {
    color: #ff9800;
  }
}

.rating-text {
  text-align: center;
  font-size: 28rpx;
  color: #ff9800;
  margin-top: 10rpx;
}

.detail-rating-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
  
  &:last-child {
    border-bottom: none;
  }
  
  .detail-label {
    font-size: 28rpx;
    color: #333;
    width: 150rpx;
  }
  
  .detail-stars {
    flex: 1;
    display: flex;
    gap: 10rpx;
  }
  
  .mini-star {
    font-size: 48rpx;
    color: #e0e0e0;
    
    &.active {
      color: #ff9800;
    }
  }
}

.tags-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;
}

.tag-item {
  padding: 20rpx 10rpx;
  text-align: center;
  background: #f5f5f5;
  border-radius: 30rpx;
  font-size: 24rpx;
  color: #666;
  transition: all 0.3s;
  
  &.selected {
    background: #e3f2fd;
    color: #2196f3;
    border: 1rpx solid #2196f3;
  }
}

.comment-input {
  width: 100%;
  min-height: 200rpx;
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  font-size: 28rpx;
  line-height: 1.6;
}

.input-count {
  text-align: right;
  font-size: 22rpx;
  color: #999;
  margin-top: 10rpx;
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
    align-items: center;
    justify-content: center;
    
    .add-icon {
      font-size: 60rpx;
      color: #999;
    }
  }
}

.anonymous-item {
  display: flex;
  align-items: center;
  
  .checkbox {
    width: 40rpx;
    height: 40rpx;
    border: 2rpx solid #e0e0e0;
    border-radius: 50%;
    margin-right: 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24rpx;
    color: white;
    
    &.checked {
      background: #2196f3;
      border-color: #2196f3;
      color: white;
    }
  }
  
  .anonymous-text {
    font-size: 28rpx;
    color: #333;
  }
}

.footer-actions {
  padding: 20rpx 30rpx;
  background: white;
  border-top: 1rpx solid #f0f0f0;
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #2196f3, #1976d2);
  color: white;
  border: none;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: bold;
  
  &[disabled] {
    background: #ccc;
  }
}
</style>
