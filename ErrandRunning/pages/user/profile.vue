<template>
  <view class="user-profile-container">
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>
    
    <view v-else>
      <!-- 用户信息卡片 -->
      <view class="profile-header">
        <image class="bg-image" src="/static/profile-bg.png" mode="aspectFill"></image>
        <view class="profile-content">
          <image class="avatar" :src="userInfo.avatar" mode="aspectFill"></image>
          <text class="username">{{ userInfo.nickname }}</text>
          <view class="user-badges">
            <text class="badge badge-role">{{ getRoleName(userRole) }}</text>
            <text class="badge" v-if="userInfo.isVerified">✓ 已认证</text>
            <text class="badge badge-vip" v-if="userInfo.isVip">VIP</text>
          </view>
        </view>
      </view>

    <!-- 用户统计 - 根据角色显示不同内容 -->
    <view class="stats-card">
      <view class="stat-item">
        <text class="stat-value">{{ getStatValue('orders') }}</text>
        <text class="stat-label">{{ getStatLabel('orders') }}</text>
      </view>
      <view class="stat-divider" v-if="userRole === 'runner'"></view>
      <view class="stat-item" v-if="userRole === 'runner'">
        <text class="stat-value">{{ getStatValue('rate') }}%</text>
        <text class="stat-label">{{ getStatLabel('rate') }}</text>
      </view>
    </view>

    <!-- 基本信息 -->
    <view class="info-card">
      <view class="card-title">基本信息</view>
      <view class="info-item">
        <text class="label">昵称</text>
        <text class="value">{{ userInfo.nickname }}</text>
      </view>
      <view class="info-item">
        <text class="label">角色</text>
        <text class="value text-primary">{{ getRoleName(userRole) }}</text>
      </view>
      <view class="info-item">
        <text class="label">手机号</text>
        <text class="value">{{ userInfo.phone }}</text>
      </view>
      <view class="info-item">
        <text class="label">注册时间</text>
        <text class="value">{{ userInfo.registerTime }}</text>
      </view>
      <view class="info-item" v-if="userRole !== 'admin'">
        <text class="label">个性签名</text>
        <text class="value">{{ userInfo.signature }}</text>
      </view>
    </view>

    <!-- 跑腿员专属信息 -->
    <view class="service-card" v-if="userRole === 'runner'">
      <view class="card-title">服务能力</view>
      <view class="service-tags">
        <text
          v-for="tag in userInfo.serviceTags"
          :key="tag"
          class="service-tag"
        >
          {{ tag }}
        </text>
      </view>
      <view class="info-item">
        <text class="label">服务范围</text>
        <text class="value">{{ userInfo.serviceArea }}</text>
      </view>
      <view class="info-item">
        <text class="label">接单时间</text>
        <text class="value">{{ userInfo.workTime }}</text>
      </view>
    </view>

    <!-- 管理员专属信息 -->
    <view class="admin-card" v-if="userRole === 'admin'">
      <view class="card-title">管理功能</view>
      <view class="menu-list">
          <view class="menu-item" @click="goToTaskManage">
            <text class="menu-icon">📋</text>
            <text class="menu-text">任务管理</text>
            <text class="menu-arrow">›</text>
          </view>

          <view class="menu-item" @click="goToRunnerApplications">
            <text class="menu-icon">📋</text>
            <text class="menu-text">跑腿员申请管理</text>
            <text class="menu-arrow">›</text>
          </view>
          <view class="menu-item" @click="goToUserManagement">
            <text class="menu-icon">👨‍💼</text>
            <text class="menu-text">平台用户管理</text>
            <text class="menu-arrow">›</text>
          </view>
          <view class="menu-item" @click="goToReportManage">
            <text class="menu-icon">🚨</text>
            <text class="menu-text">聊天举报管理</text>
            <text class="menu-arrow">›</text>
          </view>
        </view>
    </view>

    <!-- 用户评价 - 跑腿员和普通用户显示 -->
    <view class="review-card" v-if="userRole !== 'admin'">
      <view class="card-header">
        <text class="card-title">{{ userRole === 'runner' ? '用户评价' : '评价历史' }}</text>
      </view>
      <!-- 评分摘要只显示给跑腿员 -->
      <view class="review-summary" v-if="userRole === 'runner'">
        <view class="rating-item">
          <text class="rating-label">速度</text>
          <view class="rating-stars">
            <text
              v-for="i in 5"
              :key="i"
              class="star"
              :class="{ filled: i <= userInfo.ratings.speed }"
            >
              ★
            </text>
          </view>
        </view>
        <view class="rating-item">
          <text class="rating-label">态度</text>
          <view class="rating-stars">
            <text
              v-for="i in 5"
              :key="i"
              class="star"
              :class="{ filled: i <= userInfo.ratings.attitude }"
            >
              ★
            </text>
          </view>
        </view>
        <view class="rating-item">
          <text class="rating-label">质量</text>
          <view class="rating-stars">
            <text
              v-for="i in 5"
              :key="i"
              class="star"
              :class="{ filled: i <= userInfo.ratings.quality }"
            >
              ★
            </text>
          </view>
        </view>
      </view>

      <view class="review-list">
        <view
          v-for="(review, index) in userInfo.reviews"
          :key="index"
          class="review-item"
        >
          <view class="review-header">
            <image class="review-avatar" :src="review.avatar" mode="aspectFill"></image>
            <view class="review-user">
              <text class="review-name">{{ review.nickname }}</text>
              <text class="review-time">{{ review.time }}</text>
            </view>
            <view class="review-rating">
              <text class="rating-score">{{ review.rating }}.0</text>
            </view>
          </view>
          <text class="review-text">{{ review.content }}</text>
          <view class="review-tags" v-if="review.tags && review.tags.length">
            <text
              v-for="tag in review.tags"
              :key="tag"
              class="review-tag"
            >
              {{ tag }}
            </text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="footer-actions">
      <uni-button class="btn btn-primary" @click="editProfile">
        编辑资料
      </uni-button>
      <uni-button class="btn btn-primary" @click="applyForRunner" v-if="userRole === 'user'">
        申请跑腿员
      </uni-button>
      <uni-button class="btn btn-primary" @click="acceptTasks" v-if="userRole === 'runner'">
        我的接单
      </uni-button>
      <uni-button class="btn btn-secondary" @click="handleLogout">
        退出登录
      </uni-button>
    </view>
    </view>
  </view>
</template>

<script>
import { get } from '../../api/request'

export default {
  data() {
    return {
      userRole: '', // 'user', 'runner', 'admin'
      userInfo: {
        id: '',
        nickname: '',
        avatar: '/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg',
        phone: '',
        isVerified: true,
        isVip: false,
        registerTime: '',
        signature: '',
        creditScore: 98,
        completeCount: 156,
        goodRate: 99,
        publishCount: 0,
        acceptCount: 0,
        serviceTags: [],
        serviceArea: '',
        workTime: '',
        ratings: {
          speed: 5,
          attitude: 5,
          quality: 5
        },
        reviews: []
      },
      loading: true
    }
  },

  onLoad() {
    this.loadUserInfo()
  },

  methods: {
    // 加载用户信息（从后端API）
    async loadUserInfo() {
      try {
        this.loading = true
        
        // 从本地存储获取用户信息
        const user = uni.getStorageSync('user')
        if (!user || !user.id) {
          uni.showToast({
            title: '用户未登录',
            icon: 'none'
          })
          return
        }

        // 获取当前登录用户的角色
        this.userRole = uni.getStorageSync('role') || user.role || 'user'

        // 从后端API获取用户详细信息
        const response = await get(`/auth/user/profile`)
        
        if (response.code === 200 && response.data) {
          const userData = response.data
          
          // 构建用户信息
          this.userInfo = {
            id: userData.id,
            nickname: userData.nickname || userData.realName || userData.username || '用户',
            avatar: userData.avatar || '/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg',
            phone: userData.phone || '',
            isVerified: true,
            isVip: false,
            registerTime: userData.registerTime ? this.formatDate(userData.registerTime) : '',
            signature: userData.signature || '用心服务，准时送达',
            creditScore: 98,
            completeCount: userData.publishCount || 0,
            goodRate: userData.goodRate || 0,
            publishCount: userData.publishCount || 0,
            acceptCount: 0,
            serviceTags: [],
            serviceArea: '',
            workTime: '',
            ratings: {
              speed: 5,
              attitude: 5,
              quality: 5
            },
            reviews: (userData.givenReviews || []).map(review => ({
              nickname: review.revieweeName || '用户',
              avatar: review.revieweeAvatar || '/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg',
              rating: review.rating,
              time: this.formatDate(review.createTime),
              content: review.content || '',
              tags: review.tags || []
            }))
          }
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        uni.showToast({
          title: '获取用户信息失败',
          icon: 'none'
        })
        
        // 失败时使用本地存储的用户信息作为 fallback
        const user = uni.getStorageSync('user')
        if (user) {
          this.userInfo = {
            id: user.id,
            nickname: user.realName || user.username || user.nickname || '用户',
            avatar: user.avatar || '/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg',
            phone: user.phone || '',
            isVerified: true,
            isVip: false,
            registerTime: '2024-01-01',
            signature: user.signature || '用心服务，准时送达',
            creditScore: 98,
            completeCount: 156,
            goodRate: 99,
            publishCount: 0,
            acceptCount: 0,
            serviceTags: [],
            serviceArea: '',
            workTime: '',
            ratings: {
              speed: 5,
              attitude: 5,
              quality: 5
            },
            reviews: []
          }
        }
      } finally {
        this.loading = false
      }
    },
    
    // 格式化日期
    formatDate(date) {
      if (!date) return ''
      const d = new Date(date)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    },

    // 根据角色获取不同的数据
    getRoleData(role) {
      switch (role) {
        case 'admin':
          return {
            creditScore: 100,
            completeCount: 0,
            goodRate: 0,
            signature: '系统管理员',
            ratings: { speed: 0, attitude: 0, quality: 0 },
            reviews: []
          }
        case 'runner':
          return {
            creditScore: 98,
            completeCount: 156,
            goodRate: 99,
            signature: '用心服务，准时送达',
            serviceTags: ['快递代取', '外卖代送', '物品购买'],
            serviceArea: '全校',
            workTime: '8:00-22:00',
            ratings: { speed: 5, attitude: 5, quality: 5 },
            reviews: [
              {
                nickname: '张同学',
                avatar: '/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg',
                rating: 5,
                time: '昨天',
                content: '服务态度很好，速度很快，强烈推荐！',
                tags: ['速度快', '态度好']
              },
              {
                nickname: '李同学',
                avatar: '/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg',
                rating: 5,
                time: '3天前',
                content: '非常负责任，一直保持联系',
                tags: ['负责任']
              }
            ]
          }
        case 'user':
        default:
          return {
            creditScore: 95,
            completeCount: 23,
            goodRate: 98,
            signature: '爱生活，爱校园',
            serviceTags: [],
            serviceArea: '',
            workTime: '',
            ratings: { speed: 4, attitude: 5, quality: 5 },
            reviews: [
              {
                nickname: '跑腿员小李',
                avatar: '/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg',
                rating: 5,
                time: '2天前',
                content: '用户很友善，配合度高',
                tags: ['配合度高']
              }
            ]
          }
      }
    },

    // 获取角色名称
    getRoleName(role) {
      const roleMap = {
        user: '用户',
        runner: '跑腿员',
        admin: '管理员'
      }
      return roleMap[role] || '用户'
    },

    // 获取统计数据
    getStatValue(type) {
      if (this.userRole === 'admin') {
        switch (type) {
          case 'credit': return 'N/A'
          case 'orders': return this.userInfo.completeCount
          case 'rate': return 0
          default: return '-'
        }
      }
      switch (type) {
        case 'credit': return this.userInfo.creditScore
        case 'orders': return this.userInfo.completeCount
        case 'rate': return this.userInfo.goodRate
        default: return '-'
      }
    },

    // 获取统计标签
    getStatLabel(type) {
      if (this.userRole === 'admin') {
        switch (type) {
          case 'credit': return '系统状态'
          case 'orders': return '管理天数'
          case 'rate': return '系统评分'
          default: return '-'
        }
      }
      switch (type) {
        case 'credit': return '信用分'
        case 'orders': return this.userRole === 'runner' ? '完成订单' : '发布订单'
        case 'rate': return '好评率'
        default: return '-'
      }
    },

    // 编辑资料
    editProfile() {
      uni.navigateTo({
        url: '/pages/user/edit-profile'
      })
    },

    // 申请跑腿员（用户）
    applyForRunner() {
      uni.navigateTo({
        url: '/pages/runner/apply'
      })
    },

    // 发布任务（用户）
    createTask() {
      uni.navigateTo({
        url: '/pages/task/publish'
      })
    },

    // 接单大厅（跑腿员）
    acceptTasks() {
      uni.navigateTo({
        url: '/pages/runner/order-manage'
      })
    },

    // 用户管理（管理员）
    goToUserManage() {
      uni.showToast({
        title: '用户管理功能开发中',
        icon: 'none'
      })
    },

    // 任务管理（管理员）
    goToTaskManage() {
      uni.navigateTo({
        url: '/pages/admin/task-manage'
      })
    },



    // 跑腿员申请管理（管理员）
    goToRunnerApplications() {
      uni.navigateTo({
        url: '/pages/runner/admin'
      })
    },
    
    // 平台用户管理（管理员）
    goToUserManagement() {
      uni.navigateTo({
        url: '/pages/admin/user-manage'
      })
    },
    
    // 聊天举报管理（管理员）
    goToReportManage() {
      uni.navigateTo({
        url: '/pages/admin/report-manage'
      })
    },

    // 查看全部评价
    viewAllReviews() {
      uni.showToast({
        title: '查看全部评价',
        icon: 'none'
      })
    },

    // 退出登录
    handleLogout() {
      uni.showModal({
        title: '确认退出',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            // 清除本地存储
            uni.removeStorageSync('token')
            uni.removeStorageSync('user')
            uni.removeStorageSync('role')

            // 跳转到登录页
            uni.reLaunch({
              url: '/pages/login/login'
            })
          }
        }
      })
    }
  }
}
</script>

<style scoped lang="scss">
.user-profile-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 150rpx;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
  padding: 40rpx;
}

.loading-text {
  margin-top: 20rpx;
  font-size: 32rpx;
  color: #666;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 6rpx solid #f3f3f3;
  border-top: 6rpx solid #007aff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.profile-header {
  position: relative;
  height: 450rpx;
  overflow: hidden;

  .bg-image {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(135deg, #007aff 0%, #0055ff 100%);
    filter: blur(20rpx);
  }

  .profile-content {
    position: relative;
    z-index: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 100rpx;

    .avatar {
      width: 160rpx;
      height: 160rpx;
      border-radius: 80rpx;
      border: 4rpx solid white;
      margin-bottom: 20rpx;
      background: #fff;
    }

    .username {
      font-size: 36rpx;
      font-weight: 600;
      color: #fff;
      margin-bottom: 16rpx;
    }

    .user-badges {
      display: flex;
      gap: 12rpx;

      .badge {
        padding: 6rpx 16rpx;
        background: rgba(255, 255, 255, 0.2);
        color: #fff;
        border-radius: 20rpx;
        font-size: 24rpx;
        backdrop-filter: blur(10rpx);

        &.badge-role {
          background: #fff;
          color: #007aff;
          font-weight: 500;
        }

        &.badge-vip {
          background: linear-gradient(135deg, #ffd700, #ffaa00);
          color: white;
          font-weight: 500;
        }
      }
    }
  }
}

.stats-card {
  display: flex;
  background: #fff;
  margin: -40rpx 30rpx 20rpx;
  border-radius: 12rpx;
  padding: 40rpx;
  position: relative;
  z-index: 2;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);

  .stat-item {
    flex: 1;
    text-align: center;

    .stat-value {
      display: block;
      font-size: 48rpx;
      font-weight: 600;
      color: #007aff;
      margin-bottom: 8rpx;
    }

    .stat-label {
      font-size: 24rpx;
      color: #999;
    }
  }

  .stat-divider {
    width: 1rpx;
    background: #e0e0e0;
  }
}

.info-card,
.service-card,
.admin-card,
.review-card {
  background: #fff;
  margin: 20rpx 30rpx;
  border-radius: 12rpx;
  padding: 30rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
  }

  .card-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
  }

  .more-link {
    font-size: 24rpx;
    color: #007aff;
  }

  .info-item {
    display: flex;
    justify-content: space-between;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f8f8f8;

    &:last-child {
      border-bottom: none;
    }

    .label {
      font-size: 28rpx;
      color: #666;
    }

    .value {
      font-size: 28rpx;
      color: #333;
      text-align: right;
      flex: 1;
      margin-left: 40rpx;
    }
  }
}

.service-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 30rpx;

  .service-tag {
    padding: 8rpx 24rpx;
    background: #e3f2fd;
    color: #007aff;
    border-radius: 24rpx;
    font-size: 24rpx;
  }
}

.menu-list {
  .menu-item {
    display: flex;
    align-items: center;
    padding: 24rpx 0;
    border-bottom: 1rpx solid #f8f8f8;
    transition: all 0.3s ease;

    &:last-child {
      border-bottom: none;
    }

    &:active {
      background: #f8f8f8;
    }

    .menu-icon {
      font-size: 36rpx;
      margin-right: 20rpx;
    }

    .menu-text {
      flex: 1;
      font-size: 28rpx;
      color: #333;
    }

    .menu-arrow {
      font-size: 32rpx;
      color: #c0c0c0;
    }
  }
}

.review-summary {
  display: flex;
  justify-content: space-around;
  padding: 20rpx 0;
  margin-bottom: 30rpx;
  border-bottom: 1rpx solid #f8f8f8;

  .rating-item {
    text-align: center;

    .rating-label {
      display: block;
      font-size: 24rpx;
      color: #666;
      margin-bottom: 8rpx;
    }

    .rating-stars {
      .star {
        font-size: 32rpx;
        color: #ddd;

        &.filled {
          color: #ffc107;
        }
      }
    }
  }
}

.review-list {
  .review-item {
    padding: 24rpx 0;
    border-bottom: 1rpx solid #f8f8f8;

    &:last-child {
      border-bottom: none;
    }

    .review-header {
      display: flex;
      align-items: center;
      margin-bottom: 16rpx;

      .review-avatar {
        width: 64rpx;
        height: 64rpx;
        border-radius: 32rpx;
        margin-right: 16rpx;
      }

      .review-user {
        flex: 1;

        .review-name {
          display: block;
          font-size: 28rpx;
          color: #333;
          margin-bottom: 4rpx;
        }

        .review-time {
          font-size: 22rpx;
          color: #999;
        }
      }

      .review-rating {
        .rating-score {
          font-size: 32rpx;
          color: #ffc107;
          font-weight: 600;
        }
      }
    }

    .review-text {
      display: block;
      font-size: 28rpx;
      color: #666;
      line-height: 1.5;
      margin-bottom: 16rpx;
    }

    .review-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 12rpx;

      .review-tag {
        padding: 4rpx 16rpx;
        background: #f8f8f8;
        color: #999;
        border-radius: 12rpx;
        font-size: 22rpx;
      }
    }
  }
}

.footer-actions {
  background: #fff;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin: 30rpx;
  border-radius: 12rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
  overflow: hidden;

  .btn {
    width: 100%;
    height: 80rpx;
    border-radius: 0;
    font-size: 28rpx;
    font-weight: 400;
    border: none;
    border-bottom: 1rpx solid #f0f0f0;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;

    &:last-child {
      border-bottom: none;
    }

    &:active {
      background: #f8f8f8;
    }

    &.btn-primary {
      background: #f5f9ff;
      color: #1a73e8;
    }

    &.btn-secondary {
      background: #fafafa;
      color: #666;
    }
  }
}

.text-primary {
  color: #007aff !important;
}
</style>
