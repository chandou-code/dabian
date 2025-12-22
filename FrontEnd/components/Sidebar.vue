<template>
  <view class="sidebar-container" :class="{ 'sidebar-collapsed': isCollapsed }">
    <!-- 移动端遮罩层 -->
    <view 
      v-if="!isDesktop && !isCollapsed" 
      class="sidebar-overlay"
      @click="toggleSidebar"
    ></view>
    
    <!-- 侧边栏主体 -->
    <view class="sidebar" :class="{ 'sidebar-mobile': !isDesktop }">
      <!-- 头部信息 -->
      <view class="sidebar-header">
        <view class="user-info">
          <image class="user-avatar" :src="userAvatar" mode="aspectFill"></image>
          <view v-if="!isCollapsed" class="user-details">
            <text class="username">{{ userInfo && userInfo.username ? userInfo.username : '用户' }}</text>
            <text class="user-role">{{ roleText }}</text>
          </view>
        </view>
        <view class="collapse-btn" @click="toggleSidebar">
          <text class="iconfont">{{ isCollapsed ? '→' : '←' }}</text>
        </view>
      </view>
      
      <!-- 导航菜单 -->
      <view class="sidebar-menu">
        <view 
          v-for="item in menuItems" 
          :key="item.path"
          class="menu-item"
          :class="{ 'menu-item-active': currentPage === item.path }"
          @click="navigateTo(item.path)"
        >
          <view class="menu-icon">
            <text class="iconfont">{{ item.icon }}</text>
          </view>
          <text v-if="!isCollapsed" class="menu-text">{{ item.text }}</text>
        </view>
      </view>
      
      <!-- 底部操作 -->
      <view class="sidebar-footer">
        <view class="menu-item" @click="handleLogout">
          <view class="menu-icon">
            <text class="iconfont">退出</text>
          </view>
          <text v-if="!isCollapsed" class="menu-text">退出登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Sidebar',
  data() {
    return {
      isCollapsed: false,
      isDesktop: true,
      currentPage: ''
    }
  },
  
  computed: {
    ...mapGetters(['user', 'userRole']),
    
    userInfo() {
      return this.user
    },
    
    userAvatar() {
      return this.userInfo && this.userInfo.avatar ? this.userInfo.avatar : '/static/logo.png'
    },
    
    roleText() {
      const roleMap = {
        admin: '管理员',
        reviewer: '审核员',
        user: '用户'
      }
      return roleMap[this.userRole] || '用户'
    },
    
    menuItems() {
      const menus = {
        user: [
          { path: '/pages/user/dashboard', text: '用户中心', icon: '🏠' },
          { path: '/pages/user/lost-found', text: '失物招领', icon: '🔍' },
          { path: '/pages/user/publish-lost', text: '发布失物', icon: '📝' },
          { path: '/pages/user/publish-found', text: '发布招领', icon: '✅' },
          { path: '/pages/user/search', text: '智能搜索', icon: '🎯' },
          { path: '/pages/user/profile', text: '个人信息', icon: '👤' }
        ],
        admin: [
          { path: '/pages/admin/dashboard', text: '控制台', icon: '📊' },
          { path: '/pages/admin/user-management', text: '用户管理', icon: '👥' },
          { path: '/pages/admin/reviewer-management', text: '审核员管理', icon: '👨‍💼' },
          { path: '/pages/admin/statistics', text: '数据统计', icon: '📈' },
          { path: '/pages/admin/system-settings', text: '系统设置', icon: '⚙️' }
        ],
        reviewer: [
          { path: '/pages/reviewer/dashboard', text: '工作台', icon: '📋' },
          { path: '/pages/reviewer/review-lost', text: '失物审核', icon: '❌' },
          { path: '/pages/reviewer/review-found', text: '招领审核', icon: '✅' },
          { path: '/pages/reviewer/statistics', text: '审核统计', icon: '📊' }
        ]
      }
      
      return menus[this.userRole] || menus.user
    }
  },
  
  mounted() {
    this.initSidebar()
    this.getCurrentPage()
  },
  
  methods: {
    initSidebar() {
      // 检测设备类型
      const systemInfo = uni.getSystemInfoSync()
      this.isDesktop = systemInfo.platform !== 'ios' && systemInfo.platform !== 'android'
      
      // 移动端默认收起
      if (!this.isDesktop) {
        this.isCollapsed = true
      }
    },
    
    getCurrentPage() {
      const pages = getCurrentPages()
      const currentPage = pages[pages.length - 1]
      this.currentPage = currentPage.route
    },
    
    toggleSidebar() {
      this.isCollapsed = !this.isCollapsed
    },
    
    navigateTo(path) {
      // tabBar页面需要使用switchTab跳转
      const tabBarPages = [
        '/pages/index/index',
        '/pages/user/lost-found',
        '/pages/user/publish-lost',
        '/pages/user/profile'
      ]
      
      if (tabBarPages.includes(path)) {
        uni.switchTab({ url: path })
      } else {
        uni.navigateTo({ url: path })
      }
      
      // 移动端点击后自动收起
      if (!this.isDesktop) {
        this.isCollapsed = true
      }
    },
    
    handleLogout() {
      uni.showModal({
        title: '确认退出',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            this.$store.dispatch('logout')
            uni.reLaunch({ url: '/pages/login/login' })
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.sidebar-container {
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  z-index: 1000;
  transition: all 0.3s ease;
}

.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
}

.sidebar {
  width: 250px;
  height: 100vh;
  background: #ffffff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
}

.sidebar-collapsed .sidebar {
  width: 70px;
}

.sidebar-mobile {
  position: fixed;
  left: 0;
  top: 0;
  height: 100vh;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f5f5f5;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.username {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.user-role {
  font-size: 12px;
  color: #666;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 12px;
}

.collapse-btn {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s;
}

.collapse-btn:hover {
  background: #f5f5f5;
}

.sidebar-menu {
  flex: 1;
  padding: 20px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  gap: 12px;
}

.menu-item:hover {
  background: #f8f9fa;
}

.menu-item-active {
  background: #e3f2fd;
  border-right: 3px solid #2196f3;
  color: #2196f3;
}

.menu-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-text {
  font-size: 14px;
  white-space: nowrap;
}

.sidebar-footer {
  padding: 20px 0;
  border-top: 1px solid #f0f0f0;
}

.sidebar-collapsed .user-details,
.sidebar-collapsed .menu-text {
  display: none;
}

.sidebar-collapsed .sidebar-header {
  justify-content: center;
}

.sidebar-collapsed .user-info {
  flex-direction: column;
  gap: 8px;
}

.sidebar-collapsed .collapse-btn {
  position: absolute;
  right: -15px;
  top: 20px;
  background: white;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    width: 250px;
  }
  
  .sidebar-collapsed .sidebar {
    transform: translateX(-100%);
  }
}
</style>