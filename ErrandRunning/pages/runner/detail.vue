<template>
  <view class="runner-detail">
    <!-- 顶部信息 -->
    <view class="header">
      <image class="bg" src="/static/profile-bg.png" mode="aspectFill"></image>
      <image class="avatar" :src="runner.avatar || '/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg'" mode="aspectFill"></image>
      <text class="name">{{ runner.nickname }}</text>
      <view class="badges">
        <text class="badge verified" v-if="runner.isVerified">已认证</text>
        <text class="badge vip" v-if="runner.isVip">VIP</text>
      </view>
      <!-- 基本信息 -->
      <view class="basic-info">
        <text class="info-item" v-if="runner.college">{{ runner.college }}</text>
        <text class="info-item" v-if="runner.grade">{{ runner.grade }}</text>
        <text class="info-item" v-if="runner.registerTime">{{ runner.registerTime.split('T')[0] }} 加入</text>
      </view>
    </view>

    <!-- 统计数据 -->
    <view class="stats">
      <view class="stat-item">
        <text class="value">{{ runner.goodRate }}</text>
        <text class="label">好评率</text>
      </view>
      <view class="stat-item">
        <text class="value">{{ runner.completeCount }}</text>
        <text class="label">完成订单</text>
      </view>
      <view class="stat-item">
        <text class="value">{{ runner.creditScore }}</text>
        <text class="label">信用分</text>
      </view>
    </view>

    <!-- 评分详情 -->
    <view class="section">
      <text class="section-title">评分详情</text>
      <view class="rating-details">
        <view class="rating-item">
          <text class="rating-label">速度</text>
          <text class="rating-value">{{ ratings.speed }}.0</text>
          <view class="rating-bar">
            <view class="rating-bar-fill" :style="{ width: `${ratings.speed * 20}%` }"></view>
          </view>
        </view>
        <view class="rating-item">
          <text class="rating-label">态度</text>
          <text class="rating-value">{{ ratings.attitude }}.0</text>
          <view class="rating-bar">
            <view class="rating-bar-fill" :style="{ width: `${ratings.attitude * 20}%` }"></view>
          </view>
        </view>
        <view class="rating-item">
          <text class="rating-label">质量</text>
          <text class="rating-value">{{ ratings.quality }}.0</text>
          <view class="rating-bar">
            <view class="rating-bar-fill" :style="{ width: `${ratings.quality * 20}%` }"></view>
          </view>
        </view>
      </view>
    </view>

    <!-- 服务标签 -->
    <view class="section">
      <text class="section-title">服务类型</text>
      <view class="tags">
        <text v-for="tag in runner.serviceTags" :key="tag" class="tag">{{ tag }}</text>
      </view>
    </view>

    <!-- 评价历史 -->
    <view class="section">
      <text class="section-title">评价历史</text>
      <view class="reviews" v-if="reviews.length > 0">
        <view v-for="review in reviews" :key="review.id" class="review">
          <view class="review-header">
            <image class="review-avatar" :src="review.revieweeAvatar" mode="aspectFill"></image>
            <text class="review-name">{{ review.revieweeName }}</text>
            <text class="review-rating">{{ review.rating }}.0</text>
          </view>
          <text class="review-content">{{ review.content }}</text>
          <view class="review-meta">
            <text class="review-time">{{ review.createTime.split('T')[0] }}</text>
            <view class="review-tags" v-if="review.tags">
              <text v-for="tag in review.tags" :key="tag" class="review-tag">{{ tag }}</text>
            </view>
          </view>
        </view>
      </view>
      <view class="no-reviews" v-else>
        <text class="no-reviews-text">暂无评价</text>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="footer">
      <button class="btn-chat" @click="chatUser">私信</button>
      <button class="btn-task" @click="createTask">下单</button>
    </view>
  </view>
</template>

<script>
import { get } from '../../api/request'

export default {
  data() {
    return {
      runner: {
        id: '',
        nickname: '',
        avatar: '',
        rating: 0,
        completeCount: 0,
        creditScore: 0,
        isVerified: false,
        isVip: false,
        serviceTags: [],
        publishCount: 0,
        goodRate: 0,
        registerTime: '',
        college: '',
        grade: ''
      },
      reviews: [],
      ratings: {
        speed: 5.0,
        attitude: 5.0,
        quality: 5.0
      }
    }
  },
  onLoad(options) {
    this.loadRunner(options.id)
  },
  methods: {
    async loadRunner(id) {
      try {
        const response = await get(`/auth/user/basic-info/${id}`)
        if (response.code === 200 && response.data) {
          const userData = response.data
          
          // 映射跑腿员基本信息
          this.runner = {
            id: userData.id,
            nickname: userData.nickname || userData.username,
            avatar: userData.avatar || '/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg',
            rating: userData.goodRate || 0,
            completeCount: userData.publishCount || 0,
            creditScore: Math.round((userData.goodRate || 0) * 2), // 信用分从好评率转换
            isVerified: true, // 假设所有用户都已认证
            isVip: false, // 暂时没有VIP功能
            serviceTags: ['快递代取', '外卖代送', '物品购买', '帮拿帮送'], // 暂时固定标签
            publishCount: userData.publishCount || 0,
            goodRate: userData.goodRate || 0,
            registerTime: userData.registerTime,
            college: userData.college || '',
            grade: userData.grade || ''
          }
          
          // 映射评价数据
          this.reviews = userData.reviews || []
          
          // 映射评分数据
          this.ratings = userData.ratings || {
            speed: 5.0,
            attitude: 5.0,
            quality: 5.0
          }
        }
      } catch (error) {
        console.error('加载跑腿员信息失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      }
    },
    chatUser() {
      uni.navigateTo({ url: `/pages/chat/detail?userId=${this.runner.id}` })
    },
    createTask() {
      uni.navigateTo({ url: `/pages/task/publish?runnerId=${this.runner.id}` })
    }
  }
}
</script>

<style lang="scss" scoped>
.runner-detail { min-height: 100vh; background: #f5f5f5; padding-bottom: 150rpx; }
.header { position: relative; height: 450rpx; overflow: hidden; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.header .bg { position: absolute; width: 100%; height: 100%; filter: blur(20rpx); }
.header .avatar { position: relative; width: 160rpx; height: 160rpx; border-radius: 80rpx; margin: 40rpx auto 20rpx; display: block; border: 4rpx solid white; }
.header .name { display: block; text-align: center; font-size: 36rpx; font-weight: bold; color: #333; margin-top: 20rpx; }
.badges { display: flex; justify-content: center; gap: 16rpx; margin-top: 16rpx; }
.badge { padding: 6rpx 20rpx; border-radius: 20rpx; font-size: 24rpx; }
.badge.verified { background: #e8f5e8; color: #4caf50; }
.badge.vip { background: linear-gradient(135deg, #ffd700, #ffaa00); color: white; }
.basic-info { display: flex; flex-wrap: wrap; justify-content: center; gap: 20rpx; margin-top: 20rpx; }
.info-item { font-size: 24rpx; color: #666; background: rgba(255, 255, 255, 0.8); padding: 4rpx 16rpx; border-radius: 16rpx; }
.stats { display: flex; background: white; margin: -60rpx 30rpx 20rpx; border-radius: 16rpx; padding: 40rpx; position: relative; box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08); }
.stat-item { flex: 1; text-align: center; }
.stat-item .value { display: block; font-size: 48rpx; color: #2196f3; font-weight: bold; }
.stat-item .label { font-size: 24rpx; color: #999; }
.section { background: white; margin: 20rpx; padding: 30rpx; border-radius: 16rpx; box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05); }
.section-title { font-size: 32rpx; font-weight: bold; color: #333; display: block; margin-bottom: 20rpx; }

/* 评分详情 */
.rating-details { display: flex; flex-direction: column; gap: 20rpx; }
.rating-item { display: flex; align-items: center; gap: 20rpx; }
.rating-label { width: 80rpx; font-size: 28rpx; color: #666; }
.rating-value { width: 60rpx; font-size: 28rpx; color: #ffc107; font-weight: bold; }
.rating-bar { flex: 1; height: 12rpx; background: #f5f5f5; border-radius: 6rpx; overflow: hidden; }
.rating-bar-fill { height: 100%; background: linear-gradient(90deg, #ffc107, #ff9800); border-radius: 6rpx; }

/* 服务标签 */
.tags { display: flex; flex-wrap: wrap; gap: 16rpx; }
.tag { padding: 8rpx 24rpx; background: #e3f2fd; color: #2196f3; border-radius: 24rpx; font-size: 24rpx; }

/* 评价 */
.reviews { display: flex; flex-direction: column; gap: 20rpx; }
.review { padding: 24rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.review-header { display: flex; align-items: center; gap: 16rpx; margin-bottom: 12rpx; }
.review-avatar { width: 50rpx; height: 50rpx; border-radius: 25rpx; }
.review-name { font-size: 28rpx; color: #333; flex: 1; }
.review-rating { font-size: 28rpx; color: #ffc107; font-weight: bold; }
.review-content { font-size: 26rpx; color: #666; line-height: 1.6; margin-bottom: 12rpx; }
.review-meta { display: flex; flex-direction: column; gap: 8rpx; }
.review-time { font-size: 22rpx; color: #999; }
.review-tags { display: flex; flex-wrap: wrap; gap: 12rpx; }
.review-tag { font-size: 22rpx; color: #666; background: #f5f5f5; padding: 4rpx 12rpx; border-radius: 12rpx; }
.no-reviews { text-align: center; padding: 60rpx 0; color: #999; font-size: 28rpx; }

/* 底部按钮 */
.footer { position: fixed; bottom: 0; left: 0; right: 0; padding: 20rpx 30rpx; background: white; display: flex; gap: 20rpx; box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.08); }
.footer button { flex: 1; height: 88rpx; border-radius: 44rpx; font-size: 32rpx; border: none; }
.btn-chat { background: #e3f2fd; color: #2196f3; }
.btn-task { background: linear-gradient(135deg, #2196f3, #1976d2); color: white; font-weight: bold; }
</style>
