<template>
  <view class="runner-list-container">
    <!-- 筛选栏 -->
    <view class="filter-bar">
      <view class="filter-item" @click="showSortFilter">
        <text class="filter-label">排序</text>
        <text class="filter-value">{{ currentSortText }}</text>
        <text class="filter-arrow">▼</text>
      </view>
      <view class="filter-item" @click="showAreaFilter">
        <text class="filter-label">区域</text>
        <text class="filter-value">{{ currentAreaText }}</text>
        <text class="filter-arrow">▼</text>
      </view>
      <view class="filter-item" @click="showServiceFilter">
        <text class="filter-label">服务</text>
        <text class="filter-value">筛选</text>
        <text class="filter-arrow">▼</text>
      </view>
    </view>

    <!-- 跑腿员列表 -->
    <scroll-view scroll-y class="runner-list" @scrolltolower="loadMore">
      <view class="empty-state" v-if="runners.length === 0">
        <text class="empty-text">暂无跑腿员</text>
      </view>

      <view
        v-for="runner in runners"
        :key="runner.id"
        class="runner-card"
        @click="viewRunnerDetail(runner.id)"
      >
        <view class="card-header">
          <view class="runner-info">
            <image class="avatar" :src="runner.avatar || '/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg'" mode="aspectFill"></image>
            <view class="info-detail">
              <text class="username">{{ runner.nickname }}</text>
              <view class="badges">
                <text class="badge verified" v-if="runner.isVerified">✓</text>
                <text class="badge vip" v-if="runner.isVip">VIP</text>
              </view>
            </view>
          </view>
          <view class="runner-rating">
            <text class="rating-score">{{ runner.rating }}.0</text>
            <text class="rating-count">{{ runner.reviewCount }}条</text>
          </view>
        </view>

        <view class="card-stats">
          <view class="stat-item">
            <text class="stat-value">{{ runner.completeCount }}</text>
            <text class="stat-label">完成</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ runner.creditScore }}</text>
            <text class="stat-label">信用分</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ runner.goodRate }}%</text>
            <text class="stat-label">好评率</text>
          </view>
        </view>

        <view class="card-footer">
          <button class="btn btn-chat" @click.stop="chatRunner(runner.id)">私信</button>
          <button class="btn btn-task" @click.stop="createTask(runner.id)">下单</button>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      runners: [],
      loading: false
    }
  },
  onLoad() {
    this.loadRunners()
  },
  methods: {
    loadRunners() {
      // TODO: 调用API
      this.runners = [
        {
          id: 'R001',
          nickname: '跑腿员小李',
          avatar: '/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg',
          rating: 5.0,
          reviewCount: 128,
          completeCount: 256,
          creditScore: 98,
          goodRate: 99,
          isVerified: true,
          isVip: false
        }
      ]
    },
    viewRunnerDetail(id) {
      uni.navigateTo({ url: `/pages/runner/detail?id=${id}` })
    },
    chatRunner(id) {
      uni.navigateTo({ url: `/pages/chat/detail?userId=${id}` })
    },
    createTask(id) {
      uni.navigateTo({ url: `/pages/task/publish?runnerId=${id}` })
    }
  }
}
</script>

<style lang="scss" scoped>
.runner-list-container { min-height: 100vh; background: #f5f5f5; }
.runner-card { background: white; margin: 20rpx; padding: 30rpx; border-radius: 16rpx; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx; }
.runner-info { display: flex; align-items: center; gap: 20rpx; }
.avatar { width: 96rpx; height: 96rpx; border-radius: 48rpx; }
.username { font-size: 32rpx; font-weight: bold; }
.badges { display: flex; gap: 8rpx; margin-top: 8rpx; }
.badge { padding: 4rpx 12rpx; border-radius: 12rpx; font-size: 20rpx; }
.badge.verified { background: #e8f5e8; color: #4caf50; }
.badge.vip { background: linear-gradient(135deg, #ffd700, #ffaa00); color: white; }
.runner-rating { text-align: right; }
.rating-score { font-size: 48rpx; color: #ffc107; font-weight: bold; }
.rating-count { font-size: 24rpx; color: #999; }
.card-stats { display: flex; justify-content: space-around; padding: 24rpx 0; background: #f9f9f9; border-radius: 12rpx; margin-bottom: 24rpx; }
.stat-item { text-align: center; }
.stat-value { font-size: 36rpx; color: #2196f3; font-weight: bold; }
.stat-label { font-size: 24rpx; color: #999; }
.card-footer { display: flex; gap: 16rpx; }
.btn { flex: 1; height: 80rpx; border-radius: 40rpx; font-size: 28rpx; border: none; }
.btn-chat { background: #e3f2fd; color: #2196f3; }
.btn-task { background: linear-gradient(135deg, #2196f3, #1976d2); color: white; }
</style>
