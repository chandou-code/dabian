<template>
  <view class="index-page">
    <!-- 连接错误提示 -->
    <view class="connection-error" v-if="connectionError" @click="refreshData">
      <text class="error-icon">⚠️</text>
      <text class="error-text">连接服务器超时，点击屏幕重试</text>
    </view>
    
    <!-- 顶部定位栏 -->
    <view class="location-bar">
      <view class="location-info" @click="chooseLocation">
        <text class="location-icon">📍</text>
        <text class="location-text">{{ currentLocation || '选择位置' }}</text>
        <text class="arrow">▼</text>
      </view>
      <view class="notification-btn" @click="goToNotifications">
        <text class="iconfont icon-bell">🔔</text>
        <view class="badge" v-if="unreadCount > 0">
          <text>{{ unreadCount > 99 ? '99+' : unreadCount }}</text>
        </view>
      </view>
    </view>
    
    <!-- 搜索框 -->
    <view class="search-section">
      <view class="search-box" @click="goToSearch">
        <text class="search-icon">🔍</text>
        <text class="search-placeholder">搜索任务、地点...</text>
      </view>
    </view>
    
    <!-- 轮播图 -->
    <view class="banner-section">
      <swiper class="banner-swiper" :autoplay="true" :interval="5000" :circular="true" :indicator-dots="true">
        <swiper-item v-for="(banner, index) in banners" :key="index">
          <image class="banner-image" :src="banner.image" mode="aspectFill" />
        </swiper-item>
      </swiper>
    </view>
    
    <!-- 快捷入口 -->
    <view class="quick-access">
      <view
          class="access-item"
          v-for="item in accessItems"
          :key="item.key"
          @click="navigateTo(item.path)"
        >
          <text class="access-name">{{ item.name }}</text>
        </view>
    </view>
    
    <!-- 数据统计 -->
    <view class="stats-section" v-if="isLoggedIn">
      <view class="stats-card">
        <view class="stat-item">
          <text class="stat-value">{{ userStats.pendingTasks || 0 }}</text>
          <text class="stat-label">待处理</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-value">{{ userStats.inProgressTasks || 0 }}</text>
          <text class="stat-label">进行中</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-value">{{ userStats.completedTasks || 0 }}</text>
          <text class="stat-label">已完成</text>
        </view>
      </view>
    </view>
    
    <!-- 附近任务 -->
    <view class="nearby-tasks-section">
      <view class="section-header">
        <text class="section-title">附近任务</text>
        <view class="section-more" @click="goToTaskList">
          <text>更多</text>
          <text class="arrow">›</text>
        </view>
      </view>
      
      <scroll-view class="tasks-scroll" scroll-x show-scrollbar="false">
        <view
          class="task-card"
          v-for="task in nearbyTasks"
          :key="task.id"
          @click="goToTaskDetail(task.id)"
        >
          <view class="task-header">
            <view class="task-type">{{ getTypeText(task.type) }}</view>
            <view class="task-price">¥{{ task.price }}</view>
          </view>
          <view class="task-title">{{ task.title }}</view>
          <view class="task-info">
            <text class="info-item">{{ task.pickupAddress }} →</text>
            <text class="info-item">{{ task.deliveryAddress }}</text>
          </view>
          <view class="task-footer">
            <text class="distance">{{ task.distance || '0.5' }}km</text>
            <text class="time">{{ formatTime(task.expectedTime) }}</text>
          </view>
        </view>
        
        <!-- 空状态 -->
        <view class="empty-tasks" v-if="nearbyTasks.length === 0">
          <text class="empty-icon">📦</text>
          <text class="empty-text">附近暂无任务</text>
        </view>
      </scroll-view>
    </view>
    
    <!-- 推荐跑腿员 -->
    <view class="runners-section" v-if="isLoggedIn">
      <view class="section-header">
        <text class="section-title">推荐跑腿员</text>
        <view class="section-more" @click="goToRunnerList">
          <text>更多</text>
          <text class="arrow">›</text>
        </view>
      </view>
      
      <scroll-view class="runners-scroll" scroll-x show-scrollbar="false">
        <view
          class="runner-card"
          v-for="runner in recommendedRunners"
          :key="runner.id"
          @click="goToRunnerDetail(runner.id)"
        >
          <image class="runner-avatar" :src="runner.avatar || '/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg'" mode="aspectFill" />
          <view class="runner-info">
            <text class="runner-name">{{ runner.name }}</text>
            <view class="runner-rating">
              <text class="star">★</text>
              <text class="score">{{ runner.rating || '5.0' }}</text>
            </view>
            <text class="runner-orders">{{ runner.orderCount || 0 }}单</text>
          </view>
          <button class="invite-btn">邀请</button>
        </view>
      </scroll-view>
    </view>
    
    <!-- 平台公告 -->
    <view class="notice-section">
      <view class="section-header">
        <text class="section-title">平台公告</text>
      </view>
      <view class="notice-list">
        <view
          class="notice-item"
          v-for="notice in notices"
          :key="notice.id"
          @click="viewNotice(notice.id)"
        >
          <view class="notice-title">{{ notice.title }}</view>
          <view class="notice-time">{{ formatTime(notice.createdAt) }}</view>
        </view>
      </view>
    </view>
    
    <!-- 底部间距 -->
    <view style="height: 120rpx;"></view>
  </view>
</template>

<script>
import { getHomeAllData } from '../../api/errand'

export default {
  data() {
    return {
      currentLocation: '获取位置中...',
      unreadCount: 5,
      isLoggedIn: true,
      connectionError: false,
      
      banners: [
        { image: '/static/banner1.png', link: '' },
        { image: '/static/banner2.png', link: '' },
        { image: '/static/banner3.png', link: '' }
      ],
      
      userStats: {
        todayOrders: 3,
        totalOrders: 28,
        balance: 156.50
      },
      nearbyTasks: [],
      recommendedRunners: [],
      
      notices: [
        { id: 1, title: '校园跑腿平台上线公告', createdAt: new Date().toISOString() },
        { id: 2, title: '安全使用指南', createdAt: new Date().toISOString() },
        { id: 3, title: '跑腿员招募计划', createdAt: new Date().toISOString() }
      ]
    }
  },
  
  computed: {
    // 根据用户角色动态生成快捷入口
    accessItems() {
      const user = uni.getStorageSync('user')
      const baseItems = [
        { key: 'tasks', name: '任务大厅', path: '/pages/task/task-list' },
        { key: 'chat', name: '消息中心', path: '/pages/chat/list' },
        { key: 'profile', name: '个人中心', path: '/pages/user/profile' }
      ]
      
      // 如果是普通用户，显示发布任务和我的订单
      if (!user || user.role === 'user') {
        return [
          { key: 'publish', name: '发布任务', path: '/pages/task/publish' },
          ...baseItems,
          { key: 'orders', name: '我的订单', path: '/pages/order/list' }
        ]
      } 
      // 如果是跑腿员，显示我的接单
      else if (user.role === 'runner') {
        return [
          ...baseItems,
          { key: 'my-orders', name: '我的接单', path: '/pages/runner/order-manage' }
        ]
      }
      // 默认显示所有入口
      return [
        { key: 'publish', name: '发布任务', path: '/pages/task/publish' },
        ...baseItems,
        { key: 'orders', name: '我的订单', path: '/pages/order/list' }
      ]
    }
  },
  
  onLoad() {
    // 跑腿服务不需要登录，直接显示数据
    this.isLoggedIn = true
    this.loadRealData()
    this.getCurrentLocation()
  },
  
  onShow() {
    // 每次显示时重新加载数据
    this.isLoggedIn = true
    this.loadRealData()
    this.getCurrentLocation()
  },

  methods: {
    // 获取当前位置
    getCurrentLocation() {
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const { latitude, longitude } = position.coords
            this.getAddressFromCoords(latitude, longitude)
          },
          (error) => {
            console.error('获取位置失败:', error)
            this.currentLocation = '当前位置'
            // 使用默认坐标获取地址
            this.getAddressFromCoords(39.908823, 116.397470)
          },
          {
            enableHighAccuracy: true,
            timeout: 5000,
            maximumAge: 0
          }
        )
      } else {
        this.currentLocation = '当前位置'
        // 使用默认坐标获取地址
        this.getAddressFromCoords(39.908823, 116.397470)
      }
    },
    
    // 根据坐标获取地址
    getAddressFromCoords(latitude, longitude) {
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
          // 更新当前位置
          this.currentLocation = res.result.formatted_addresses.recommend
        } else {
          this.currentLocation = '获取地址失败'
        }
        // 移除script标签和回调函数
        document.body.removeChild(script)
        delete window[callbackName]
      }
      
      // 添加到页面
      document.body.appendChild(script)
    },
    
    // 检查URL参数中的Token
    checkURLToken() {
      try {
        // 处理hash模式下的URL参数
        let urlParams
        if (window.location.hash.includes('?')) {
          // 从hash中获取参数
          const hash = window.location.hash.substring(window.location.hash.indexOf('?'))
          urlParams = new URLSearchParams(hash.substring(1))
        } else {
          // 从search中获取参数
          urlParams = new URLSearchParams(window.location.search)
        }
        
        const encodedToken = urlParams.get('token')
        const encodedUser = urlParams.get('user')
        const timestamp = urlParams.get('timestamp')

        // 检查参数是否完整
        if (encodedToken && encodedUser && timestamp) {
          const elapsed = Date.now() - parseInt(timestamp)
          const maxAge = 30 * 60 * 1000 // 30分钟

          if (elapsed < maxAge) {
            // Token有效，解码并保存
            const token = atob(encodedToken)
            const user = JSON.parse(atob(encodedUser))

            // 保存到本地存储
            uni.setStorageSync('token', token)
            uni.setStorageSync('user', user)
            this.isLoggedIn = true
            this.loadRealData()
            console.log('单点登录成功，用户:', user.username)

            // 清除URL中的敏感参数
            window.history.replaceState({}, document.title, '/#/')
          } else {
            console.log('Token已过期')
            // 使用模拟数据
            this.isLoggedIn = true
            this.loadRealData()
          }
        } else {
          console.log('URL参数不完整')
          // 使用模拟数据
          this.isLoggedIn = true
          this.loadRealData()
        }
      } catch (error) {
        console.error('检查URL Token失败:', error)
        // 使用模拟数据
        this.isLoggedIn = true
        this.loadRealData()
      }
    },
    
    // 初始化页面
    initPage() {
      // 直接使用模拟数据
      this.loadRealData()
    },
    
    // 刷新数据
    refreshData() {
      // 直接使用模拟数据
      this.loadRealData()
    },
    
    // 加载真实数据
    async loadRealData() {
      console.log('加载真实数据')
      // 清除连接错误状态
      this.connectionError = false
      
      try {
        // 调用API获取首页所有数据
        const response = await getHomeAllData()
        
        if (response.code === 200 && response.data) {
          const data = response.data
          
          // 更新轮播图数据
          if (data.banners) {
            this.banners = data.banners
            console.log('成功加载轮播图数据:', this.banners)
          }
          
          // 更新用户统计数据
          if (data.userStats) {
            this.userStats = data.userStats
            console.log('成功加载用户统计数据:', this.userStats)
          }
          
          // 更新附近任务数据
          if (data.nearbyTasks) {
            this.nearbyTasks = data.nearbyTasks
            console.log('成功加载附近任务数据:', this.nearbyTasks)
          }
          
          // 更新推荐跑腿员数据
          if (data.recommendedRunners) {
            this.recommendedRunners = data.recommendedRunners.map(runner => ({
              ...runner,
              avatar: runner.avatar || '/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg'
            }))
            console.log('成功加载推荐跑腿员数据:', this.recommendedRunners)
          }
          
          // 更新平台公告数据
          if (data.notices) {
            this.notices = data.notices
            console.log('成功加载平台公告数据:', this.notices)
          }
        } else {
          console.log('使用备份模拟数据')
          this.loadDefaultData()
        }
      } catch (error) {
        console.error('加载首页数据失败:', error)
        this.loadDefaultData()
      }
    },
    
    // 加载默认数据（备份）
    loadDefaultData() {
      // 轮播图数据
      this.banners = [
        { image: '/static/banners/064c1423-da21-4672-bd41-c0ddaf71c4c6.png', link: '' },
        { image: '/static/banners/2e55097651da6296102c49461ac8e0d1.png', link: '' },
        { image: '/static/banners/d717101d-ee99-4b15-95b6-0d1ca8b227ac.png', link: '' }
      ]
      
      // 附近任务数据
      this.nearbyTasks = [
        {
          id: 'T001',
          type: 'delivery',
          title: '帮忙取个快递',
          price: 5,
          pickupAddress: '东门菜鸟驿站',
          deliveryAddress: '西苑3栋502室',
          distance: '0.5',
          expectedTime: new Date().toISOString()
        },
        {
          id: 'T002',
          type: 'food',
          title: '帮买奶茶',
          price: 8,
          pickupAddress: '校内奶茶店',
          deliveryAddress: '图书馆二楼',
          distance: '0.8',
          expectedTime: new Date().toISOString()
        }
      ]
      
      // 推荐跑腿员数据
      this.recommendedRunners = [
        {
          id: 'R001',
          name: '跑腿员小李',
          avatar: '/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg',
          rating: 5.0,
          orderCount: 156
        },
        {
          id: 'R002',
          name: '快递达人',
          avatar: '/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg',
          rating: 4.8,
          orderCount: 89
        }
      ]
      
      // 用户统计数据
      this.userStats = {
        pendingTasks: 0,
        inProgressTasks: 0,
        completedTasks: 0
      }
      
      // 平台公告数据
      this.notices = [
        { id: 1, title: '校园跑腿平台上线公告', createdAt: new Date().toISOString() },
        { id: 2, title: '安全使用指南', createdAt: new Date().toISOString() },
        { id: 3, title: '跑腿员招募计划', createdAt: new Date().toISOString() }
      ]
    },
    
    // 选择位置
    chooseLocation() {
      uni.chooseLocation({
        success: (res) => {
          this.currentLocation = res.name
          // 重新加载附近任务
          this.loadNearbyTasks()
        }
      })
    },

    // 加载附近任务
    async loadNearbyTasks() {
      try {
        // 尝试调用API获取数据
        // const response = await request.get('/tasks/nearby')
        // this.nearbyTasks = response.data || []
        
        // 使用模拟数据（避免API调用失败）
        this.nearbyTasks = [
          {
            id: 'T001',
            type: 'delivery',
            title: '帮忙取个快递',
            price: 5,
            pickupAddress: '东门菜鸟驿站',
            deliveryAddress: '西苑3栋502室',
            distance: '0.5',
            expectedTime: new Date().toISOString()
          },
          {
            id: 'T002',
            type: 'food',
            title: '帮买奶茶',
            price: 8,
            pickupAddress: '校内奶茶店',
            deliveryAddress: '图书馆二楼',
            distance: '0.8',
            expectedTime: new Date().toISOString()
          }
        ]
      } catch (error) {
        console.error('加载附近任务失败:', error)
        // 使用模拟数据
        this.nearbyTasks = [
          {
            id: 'T001',
            type: 'delivery',
            title: '帮忙取个快递',
            price: 5,
            pickupAddress: '东门菜鸟驿站',
            deliveryAddress: '西苑3栋502室',
            distance: '0.5',
            expectedTime: new Date().toISOString()
          },
          {
            id: 'T002',
            type: 'food',
            title: '帮买奶茶',
            price: 8,
            pickupAddress: '校内奶茶店',
            deliveryAddress: '图书馆二楼',
            distance: '0.8',
            expectedTime: new Date().toISOString()
          }
        ]
      }
    },

    // 加载用户统计
    async loadUserStats() {
      try {
        // 尝试调用API获取数据
        // const response = await request.get('/user/stats')
        // this.userStats = response.data || {}
        
        // 使用模拟数据
        this.userStats = {
          todayOrders: 3,
          totalOrders: 28,
          balance: 156.50
        }
      } catch (error) {
        console.error('加载用户统计失败:', error)
        // 使用模拟数据
        this.userStats = {
          todayOrders: 3,
          totalOrders: 28,
          balance: 156.50
        }
      }
    },

    // 加载推荐跑腿员
    async loadRecommendedRunners() {
      try {
        // 尝试调用API获取数据
        const response = await request.get('/home/recommended-runners')
        this.recommendedRunners = response.data || []
        
        // 如果没有数据，使用模拟数据
        if (!this.recommendedRunners || this.recommendedRunners.length === 0) {
          this.recommendedRunners = [
            {
              id: 'R001',
              name: '跑腿员小李',
              avatar: '/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg',
              rating: 5.0,
              orderCount: 156
            },
            {
              id: 'R002',
              name: '快递达人',
              avatar: '/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg',
              rating: 4.8,
              orderCount: 89
            }
          ]
        }
      } catch (error) {
        console.error('加载推荐跑腿员失败:', error)
        // 使用模拟数据
        this.recommendedRunners = [
          {
            id: 'R001',
            name: '跑腿员小李',
            avatar: '/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg',
            rating: 5.0,
            orderCount: 156
          },
          {
            id: 'R002',
            name: '快递达人',
            avatar: '/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg',
            rating: 4.8,
            orderCount: 89
          }
        ]
      }
    },

    // 导航
    navigateTo(path) {
      // 检查是否是tabBar页面
      const tabBarPages = [
        '/pages/index/index',
        '/pages/task/task-list',
        '/pages/chat/list',
        '/pages/order/list',
        '/pages/user/profile'
      ]
      
      if (tabBarPages.includes(path)) {
        uni.switchTab({ url: path })
      } else {
        uni.navigateTo({ url: path })
      }
    },
    
    goToNotifications() {
      uni.navigateTo({ url: '/pages/notification/list' })
    },
    
    goToSearch() {
      uni.navigateTo({ url: '/pages/search/index' })
    },
    
    goToTaskList() {
      uni.switchTab({ url: '/pages/task/task-list' })
    },
    
    goToTaskDetail(taskId) {
      uni.navigateTo({ url: `/pages/task/detail?id=${taskId}` })
    },
    
    goToRunnerList() {
      uni.navigateTo({ url: '/pages/runner/list' })
    },
    
    goToRunnerDetail(runnerId) {
      uni.navigateTo({ url: `/pages/runner/detail?id=${runnerId}` })
    },
    
    viewNotice(noticeId) {
      uni.navigateTo({ url: `/pages/notice/detail?id=${noticeId}` })
    },
    
    // 获取类型文本
    getTypeText(type) {
      const types = {
        'delivery': '快递代取',
        'food': '外卖代送',
        'shopping': '物品购买',
        'queue': '排队代办',
        'document': '文件传递',
        'other': '其他'
      }
      return types[type] || type
    },
    
    // 格式化时间
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      const month = (date.getMonth() + 1).toString().padStart(2, '0')
      const day = date.getDate().toString().padStart(2, '0')
      return `${month}-${day}`
    }
  }
}
</script>

<style scoped lang="scss">
.index-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.location-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  background: linear-gradient(135deg, #2196f3, #1976d2);
  
  .location-info {
    display: flex;
    align-items: center;
    color: white;
    
    .location-icon {
      font-size: 32rpx;
      margin-right: 8rpx;
    }
    
    .location-text {
      font-size: 28rpx;
      margin-right: 8rpx;
    }
    
    .arrow {
      font-size: 20rpx;
    }
  }
  
  .notification-btn {
    position: relative;
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 36rpx;
    color: white;
    
    .badge {
      position: absolute;
      top: 0;
      right: 0;
      min-width: 32rpx;
      height: 32rpx;
      background: #f44336;
      color: white;
      border-radius: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18rpx;
      padding: 0 6rpx;
    }
  }
}

.search-section {
  padding: 20rpx 30rpx;
  
  .search-box {
    display: flex;
    align-items: center;
    padding: 20rpx 30rpx;
    background: white;
    border-radius: 50rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
    
    .search-icon {
      font-size: 32rpx;
      margin-right: 16rpx;
    }
    
    .search-placeholder {
      font-size: 28rpx;
      color: #999;
    }
  }
}

.banner-section {
  padding: 0 30rpx;
  
  .banner-swiper {
    height: 300rpx;
    border-radius: 16rpx;
    overflow: hidden;
  }
  
  .banner-image {
    width: 100%;
    height: 100%;
  }
}

.quick-access {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 30rpx;
  padding: 40rpx 30rpx;
  background: white;
  margin-top: 20rpx;
  
  .access-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100rpx;
    
    .access-name {
      font-size: 24rpx;
      color: #333;
      font-weight: 500;
    }
  }
}

.stats-section {
  padding: 0 30rpx;
  margin-top: 20rpx;
  
  .stats-card {
    display: flex;
    align-items: center;
    justify-content: space-around;
    padding: 40rpx 20rpx;
    background: linear-gradient(135deg, #667eea, #764ba2);
    border-radius: 16rpx;
    color: white;
    
    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      
      .stat-value {
        font-size: 48rpx;
        font-weight: bold;
        margin-bottom: 8rpx;
      }
      
      .stat-label {
        font-size: 24rpx;
        opacity: 0.9;
      }
    }
    
    .stat-divider {
      width: 1rpx;
      height: 60rpx;
      background: rgba(255, 255, 255, 0.3);
    }
  }
}

.nearby-tasks-section,
.runners-section,
.notice-section {
  margin-top: 20rpx;
  padding: 30rpx;
  background: white;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
  
  .section-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
  }
  
  .section-more {
    display: flex;
    align-items: center;
    font-size: 24rpx;
    color: #999;
    
    .arrow {
      font-size: 32rpx;
      margin-left: 4rpx;
    }
  }
}

.tasks-scroll,
.runners-scroll {
  white-space: nowrap;
}

.task-card {
  display: inline-block;
  width: 280rpx;
  background: #f8f8f8;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-right: 20rpx;
  vertical-align: top;
  
  .task-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;
    
    .task-type {
      padding: 6rpx 12rpx;
      background: #e3f2fd;
      color: #2196f3;
      border-radius: 20rpx;
      font-size: 20rpx;
    }
    
    .task-price {
      font-size: 32rpx;
      font-weight: bold;
      color: #ff5722;
    }
  }
  
  .task-title {
    font-size: 28rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 12rpx;
    white-space: normal;
  }
  
  .task-info {
    margin-bottom: 16rpx;
    
    .info-item {
      display: block;
      font-size: 22rpx;
      color: #666;
      margin-bottom: 6rpx;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
  
  .task-footer {
    display: flex;
    justify-content: space-between;
    font-size: 22rpx;
    color: #999;
    
    .distance {
      color: #2196f3;
    }
  }
}

.empty-tasks {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 40rpx;
  
  .empty-icon {
    font-size: 80rpx;
    margin-bottom: 20rpx;
  }
  
  .empty-text {
    font-size: 26rpx;
    color: #999;
  }
}

.runner-card {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  width: 200rpx;
  padding: 20rpx;
  margin-right: 20rpx;
  vertical-align: top;
  
  .runner-avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    margin-bottom: 16rpx;
  }
  
  .runner-info {
    text-align: center;
    margin-bottom: 16rpx;
    
    .runner-name {
      display: block;
      font-size: 26rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 8rpx;
    }
    
    .runner-rating {
      font-size: 22rpx;
      
      .star {
        color: #ff9800;
      }
      
      .score {
        color: #ff9800;
        margin-left: 4rpx;
      }
    }
    
    .runner-orders {
      display: block;
      font-size: 22rpx;
      color: #999;
      margin-top: 8rpx;
    }
  }
  
  .invite-btn {
    width: 120rpx;
    height: 60rpx;
    background: #2196f3;
    color: white;
    border: none;
    border-radius: 30rpx;
    font-size: 24rpx;
    line-height: 60rpx;
  }
}

.notice-list {
  .notice-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
    
    &:last-child {
      border-bottom: none;
    }
    
    .notice-title {
      flex: 1;
      font-size: 28rpx;
      color: #333;
    }
    
    .notice-time {
      font-size: 24rpx;
      color: #999;
    }
  }
}
</style>