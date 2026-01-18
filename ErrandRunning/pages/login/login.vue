<template>
  <view class="login-page">
    <view class="login-container">
      <!-- Logo区域 -->
      <view class="logo-section">
        <text class="logo-icon">🏃</text>
        <text class="logo-text">校园跑腿</text>
        <text class="logo-subtitle">让校园生活更便利</text>
      </view>
      
      <!-- 登录表单 -->
      <view class="login-form">
        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">👤</text>
            <input
              class="form-input"
              v-model="loginForm.username"
              :placeholder="getUsernamePlaceholder()"
              placeholder-class="input-placeholder"
            />
          </view>
        </view>

        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">🔒</text>
            <input
              class="form-input"
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              placeholder-class="input-placeholder"
            />
            <text class="toggle-password" @click="togglePassword">
              {{ showPassword ? '👁️' : '🙈' }}
            </text>
          </view>
        </view>
        
        <view class="form-options">
          <view class="remember-me" @click="toggleRemember">
            <view class="checkbox" :class="{ checked: rememberMe }">
              <text v-if="rememberMe">✓</text>
            </view>
            <text class="checkbox-label">记住密码</text>
          </view>
          <text class="forgot-password" @click="forgotPassword">忘记密码？</text>
        </view>
        
        <button class="login-btn" @click="handleLogin" :disabled="isLoading">
          {{ isLoading ? '登录中...' : '登录' }}
        </button>
        
        <view class="register-link">
          <text class="link-text">还没有账号？</text>
          <text class="link-btn" @click="goToRegister">立即注册</text>
        </view>
      </view>
      
      <!-- 第三方登录 -->
      <view class="social-login">
        <view class="divider">
          <view class="divider-line"></view>
          <text class="divider-text">其他登录方式</text>
          <view class="divider-line"></view>
        </view>
        
        <view class="social-buttons">
          <view class="social-btn" @click="wechatLogin">
            <text class="social-icon">💬</text>
          </view>
          <view class="social-btn" @click="qqLogin">
            <text class="social-icon">🐧</text>
          </view>
        </view>
      </view>
      
      <!-- 用户协议 -->
      <view class="agreement">
        <text class="agreement-text">登录即表示同意</text>
        <text class="link-btn" @click="viewAgreement">《用户协议》</text>
        <text class="agreement-text">和</text>
        <text class="link-btn" @click="viewPrivacy">《隐私政策》</text>
      </view>
    </view>
  </view>
</template>

<script>
import { post } from '../../api/request'

export default {
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      showPassword: false,
      rememberMe: false,
      isLoading: false
    }
  },

  onLoad() {
    // 加载记住的账号密码
    this.loadRememberedAccount()
  },
  
  methods: {
    // 获取用户名提示
    getUsernamePlaceholder() {
      return '请输入用户名/手机号'
    },

    // 登录（调用后端API）
    async handleLogin() {
      if (!this.validateForm()) {
        return
      }

      this.isLoading = true

      try {
        const { username, password } = this.loginForm

        // 调用后端登录API
        const response = await post('/auth/login', {
          username,
          password
        })

        if (response.code === 200 && response.data) {
          const { user, token } = response.data

          // 保存到Vuex
          this.$store.dispatch('login', { user, token })

          // 保存到本地存储
          uni.setStorageSync('token', token)
          uni.setStorageSync('user', user)
          uni.setStorageSync('role', user.role)

          // 记住密码
          if (this.rememberMe) {
            this.saveAccount()
          }

          uni.showToast({
            title: '登录成功',
            icon: 'success'
          })

          setTimeout(() => {
            this.redirectToHome()
          }, 1500)
        } else {
          uni.showToast({
            title: response.msg || '登录失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('登录失败:', error)
        uni.showToast({
          title: '登录失败，请检查网络连接',
          icon: 'none'
        })
      } finally {
        this.isLoading = false
      }
    },

    // 表单验证
    validateForm() {
      if (!this.loginForm.username.trim()) {
        uni.showToast({
          title: '请输入用户名或手机号',
          icon: 'none'
        })
        return false
      }
      
      if (!this.loginForm.password) {
        uni.showToast({
          title: '请输入密码',
          icon: 'none'
        })
        return false
      }
      
      return true
    },
    
    // 切换密码显示
    togglePassword() {
      this.showPassword = !this.showPassword
    },
    
    // 切换记住密码
    toggleRemember() {
      this.rememberMe = !this.rememberMe
    },
    
    // 保存账号
    saveAccount() {
      uni.setStorageSync('rememberedAccount', {
        username: this.loginForm.username,
        password: this.loginForm.password
      })
    },

    // 加载记住的账号
    loadRememberedAccount() {
      const remembered = uni.getStorageSync('rememberedAccount')
      if (remembered) {
        this.loginForm.username = remembered.username
        this.loginForm.password = remembered.password
        this.rememberMe = true
      }
    },
    
    // 忘记密码
    forgotPassword() {
      uni.showToast({
        title: '功能开发中',
        icon: 'none'
      })
    },
    
    // 跳转到注册
    goToRegister() {
      uni.navigateTo({
        url: '/pages/register/register'
      })
    },
    
    // 微信登录
    wechatLogin() {
      uni.showToast({
        title: '微信登录开发中',
        icon: 'none'
      })
    },
    
    // QQ登录
    qqLogin() {
      uni.showToast({
        title: 'QQ登录开发中',
        icon: 'none'
      })
    },
    
    // 查看用户协议
    viewAgreement() {
      uni.navigateTo({
        url: '/pages/agreement/user'
      })
    },
    
    // 查看隐私政策
    viewPrivacy() {
      uni.navigateTo({
        url: '/pages/agreement/privacy'
      })
    },
    
    // 跳转到首页（根据角色）
    redirectToHome() {
      const user = uni.getStorageSync('user')
      const role = user?.role || 'user'

      if (role === 'admin') {
        // 管理员跳转到管理页面（暂时跳转到首页）
        uni.reLaunch({
          url: '/pages/index/index'
        })
      } else if (role === 'runner') {
        // 跑腿员跳转到任务列表
        uni.reLaunch({
          url: '/pages/task/task-list'
        })
      } else {
        // 普通用户跳转到首页
        uni.reLaunch({
          url: '/pages/index/index'
        })
      }
    }
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}

.login-container {
  width: 100%;
  max-width: 600rpx;
}

.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 80rpx;
  
  .logo-icon {
    font-size: 120rpx;
    margin-bottom: 20rpx;
  }
  
  .logo-text {
    font-size: 56rpx;
    font-weight: bold;
    color: white;
    margin-bottom: 12rpx;
  }
  
  .logo-subtitle {
    font-size: 28rpx;
    color: rgba(255, 255, 255, 0.8);
  }
}

.login-form {
  background: white;
  border-radius: 24rpx;
  padding: 60rpx 40rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.15);
}

.form-item {
  margin-bottom: 30rpx;
  
  .input-wrapper {
    display: flex;
    align-items: center;
    padding: 24rpx 30rpx;
    background: #f8f8f8;
    border-radius: 12rpx;
    border: 2rpx solid transparent;
    transition: all 0.3s;
    
    &:focus-within {
      border-color: #667eea;
      background: white;
    }
    
    .input-icon {
      font-size: 36rpx;
      margin-right: 20rpx;
    }
    
    .form-input {
      flex: 1;
      font-size: 28rpx;
      color: #333;
    }
    
    .toggle-password {
      font-size: 32rpx;
      color: #999;
      margin-left: 20rpx;
    }
  }
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
  
  .remember-me {
    display: flex;
    align-items: center;
    
    .checkbox {
      width: 36rpx;
      height: 36rpx;
      border: 2rpx solid #e0e0e0;
      border-radius: 6rpx;
      margin-right: 12rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20rpx;
      color: white;
      transition: all 0.3s;
      
      &.checked {
        background: #667eea;
        border-color: #667eea;
      }
    }
    
    .checkbox-label {
      font-size: 26rpx;
      color: #666;
    }
  }
  
  .forgot-password {
    font-size: 26rpx;
    color: #667eea;
  }
}

.login-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 30rpx;
  
  &[disabled] {
    background: #ccc;
  }
}

.register-link {
  text-align: center;
  
  .link-text {
    font-size: 26rpx;
    color: #666;
  }
  
  .link-btn {
    font-size: 26rpx;
    color: #667eea;
    font-weight: bold;
  }
}

.social-login {
  margin-top: 60rpx;
  
  .divider {
    display: flex;
    align-items: center;
    margin-bottom: 40rpx;
    
    .divider-line {
      flex: 1;
      height: 1rpx;
      background: #e0e0e0;
    }
    
    .divider-text {
      padding: 0 20rpx;
      font-size: 24rpx;
      color: #999;
    }
  }
  
  .social-buttons {
    display: flex;
    justify-content: center;
    gap: 60rpx;
  }
  
  .social-btn {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    background: #f8f8f8;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .social-icon {
      font-size: 40rpx;
    }
  }
}

.agreement {
  text-align: center;
  margin-top: 40rpx;
  
  .agreement-text {
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.8);
  }
  
  .link-btn {
    font-size: 24rpx;
    color: white;
    text-decoration: underline;
  }
}
</style>