<template>
  <view class="login-container">
    <view class="login-card">
      <!-- 头部logo -->
      <view class="login-header">
        <image class="logo" src="/static/logo.png" mode="aspectFit"></image>
        <text class="app-title">校园失物招领</text>
        <text class="app-subtitle">让失物回家，让爱心传递</text>
      </view>
      
      <!-- 登录表单 -->
      <view class="login-form">
        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">👤</text>
            <input 
              v-model="loginForm.username" 
              class="form-input" 
              placeholder="请输入用户名"
              :class="{ 'input-error': errors.username }"
            />
          </view>
          <text v-if="errors.username" class="error-text">{{ errors.username }}</text>
        </view>
        
        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">🔒</text>
            <input 
              v-model="loginForm.password" 
              type="password" 
              class="form-input" 
              placeholder="请输入密码"
              :class="{ 'input-error': errors.password }"
            />
          </view>
          <text v-if="errors.password" class="error-text">{{ errors.password }}</text>
        </view>
        
        <view class="form-actions">
          <button 
            class="login-btn" 
            :class="{ 'btn-loading': isLoading }" 
            @click="handleLogin"
            :disabled="isLoading"
          >
            {{ isLoading ? '登录中...' : '登录' }}
          </button>
        </view>
        
        <!-- 角色选择 -->
        <view class="role-section">
          <text class="role-title">或以其他身份登录</text>
          <view class="role-buttons">
            <button class="role-btn admin-btn" @click="handleRoleLogin('admin')">
              管理员登录
            </button>
            <button class="role-btn reviewer-btn" @click="handleRoleLogin('reviewer')">
              审核员登录
            </button>
          </view>
        </view>
        
        <!-- 底部链接 -->
        <view class="login-footer">
          <text class="link-text" @click="goToRegister">还没有账号？立即注册</text>
        </view>
      </view>
    </view>
    
    <!-- 版权信息 -->
    <view class="copyright">
      <text>© 校园失物招领系统</text>
    </view>
  </view>
</template>

<script>
import { mapActions } from 'vuex'
import { login as apiLogin } from '../../api' // 导入登录API

export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      errors: {
        username: '',
        password: ''
      },
      isLoading: false
    }
  },
  
  onLoad() {
    // 检查是否已登录
    this.checkLoginStatus()
  },
  
  methods: {
    ...mapActions(['login', 'initUser']),
    
    checkLoginStatus() {
      this.initUser()
      if (this.$store.getters.isLoggedIn) {
        this.redirectToDashboard()
      }
    },
    
    validateForm() {
      let isValid = true
      
      // 重置错误信息
      this.errors = {
        username: '',
        password: ''
      }
      
      // 验证用户名
      if (!this.loginForm.username.trim()) {
        this.errors.username = '请输入用户名'
        isValid = false
      } else if (this.loginForm.username.length < 3) {
        this.errors.username = '用户名至少3个字符'
        isValid = false
      }
      
      // 验证密码
      if (!this.loginForm.password) {
        this.errors.password = '请输入密码'
        isValid = false
      } else if (this.loginForm.password.length < 6) {
        this.errors.password = '密码至少6个字符'
        isValid = false
      }
      
      return isValid
    },
    
    async handleLogin() {
      if (!this.validateForm()) {
        return
      }
      
      this.isLoading = true
      
      try {
        // 调用真实登录API
        const response = await apiLogin(this.loginForm)
        
        console.log('登录响应:', response)
        
        // 检查响应数据结构
        if (response.code === 200 && response.data) {
          const { user, token } = response.data
          
          if (!user || !user.role) {
            throw new Error('用户信息不完整，缺少role字段')
          }
          
          // 登录成功，保存用户信息和token
          this.login({
            user: user,
            token: token
          })
        } else {
          throw new Error(response.message || '登录失败')
        }
        
        uni.showToast({
          title: '登录成功',
          icon: 'success'
        })
        
        // 延迟跳转，让用户看到成功提示
        setTimeout(() => {
          this.redirectToDashboard()
        }, 1500)
        
      } catch (error) {
        uni.showToast({
          title: error.message || '登录失败',
          icon: 'none'
        })
      } finally {
        this.isLoading = false
      }
    },
    
    handleRoleLogin(role) {
      // 预填充角色账号密码
      const roleAccounts = {
        admin: { username: 'admin', password: 'admin123' },
        reviewer: { username: 'reviewer', password: 'review123' }
      }
      
      const account = roleAccounts[role]
      if (account) {
        this.loginForm = account
        uni.showModal({
          title: '提示',
          content: `已填充${role === 'admin' ? '管理员' : '审核员'}测试账号，点击确定登录`,
          success: (res) => {
            if (res.confirm) {
              this.handleLogin()
            }
          }
        })
      }
    },
    
    redirectToDashboard() {
      console.log('准备跳转到仪表板，当前用户角色:', this.$store.getters.userRole)
      
      const role = this.$store.getters.userRole
      if (!role) {
        console.error('用户角色为空，无法跳转')
        uni.showToast({
          title: '用户角色获取失败',
          icon: 'none'
        })
        return
      }
      
      let route = '/pages/user/dashboard'
      
      if (role === 'admin') {
        route = '/pages/admin/dashboard'
      } else if (role === 'reviewer') {
        route = '/pages/reviewer/dashboard'
      }
      
      console.log('跳转到页面:', route)
      uni.reLaunch({ url: route })
    },
    
    goToRegister() {
      uni.navigateTo({ url: '/pages/register/register' })
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}

.login-card {
  background: white;
  border-radius: 20rpx;
  padding: 60rpx 40rpx;
  width: 100%;
  max-width: 600rpx;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 80rpx;
}

.logo {
  width: 120rpx;
  height: 120rpx;
  margin-bottom: 20rpx;
}

.app-title {
  display: block;
  font-size: 48rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 10rpx;
}

.app-subtitle {
  display: block;
  font-size: 24rpx;
  color: #666;
}

.login-form {
  width: 100%;
}

.form-item {
  margin-bottom: 40rpx;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  border: 2rpx solid #e0e0e0;
  border-radius: 12rpx;
  padding: 20rpx;
  transition: border-color 0.3s;
}

.input-wrapper:focus-within {
  border-color: #667eea;
}

.input-icon {
  margin-right: 20rpx;
  font-size: 32rpx;
}

.form-input {
  flex: 1;
  font-size: 32rpx;
  border: none;
  outline: none;
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

.form-actions {
  margin-bottom: 60rpx;
}

.login-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(45deg, #667eea, #764ba2);
  color: white;
  border: none;
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: 600;
  transition: opacity 0.3s;
}

.login-btn:hover {
  opacity: 0.9;
}

.btn-loading {
  opacity: 0.7;
}

.role-section {
  margin-bottom: 40rpx;
}

.role-title {
  display: block;
  text-align: center;
  font-size: 28rpx;
  color: #666;
  margin-bottom: 30rpx;
}

.role-buttons {
  display: flex;
  gap: 20rpx;
}

.role-btn {
  flex: 1;
  height: 80rpx;
  border: 2rpx solid #e0e0e0;
  background: white;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #666;
  transition: all 0.3s;
}

.admin-btn:hover {
  border-color: #ff9800;
  color: #ff9800;
  background: #fff3e0;
}

.reviewer-btn:hover {
  border-color: #4caf50;
  color: #4caf50;
  background: #e8f5e8;
}

.login-footer {
  text-align: center;
}

.link-text {
  font-size: 28rpx;
  color: #667eea;
  text-decoration: underline;
}

.copyright {
  position: absolute;
  bottom: 40rpx;
  left: 0;
  right: 0;
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
  font-size: 24rpx;
}

/* 响应式设计 */
@media (max-width: 400px) {
  .login-container {
    padding: 20rpx;
  }
  
  .login-card {
    padding: 40rpx 30rpx;
  }
  
  .role-buttons {
    flex-direction: column;
  }
}
</style>