<template>
  <view class="register-container">
    <view class="register-card">
      <!-- 头部 -->
      <view class="register-header">
        <text class="page-title">用户注册</text>
        <text class="page-subtitle">加入我们，让失物找到回家的路</text>
      </view>
      
      <!-- 注册表单 -->
      <view class="register-form">
        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">👤</text>
            <input 
              v-model="registerForm.username" 
              class="form-input" 
              placeholder="请输入用户名"
              :class="{ 'input-error': errors.username }"
            />
          </view>
          <text v-if="errors.username" class="error-text">{{ errors.username }}</text>
        </view>
        
        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">📧</text>
            <input 
              v-model="registerForm.email" 
              class="form-input" 
              placeholder="请输入邮箱"
              :class="{ 'input-error': errors.email }"
            />
          </view>
          <text v-if="errors.email" class="error-text">{{ errors.email }}</text>
        </view>
        
        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">📱</text>
            <input 
              v-model="registerForm.phone" 
              class="form-input" 
              placeholder="请输入手机号"
              :class="{ 'input-error': errors.phone }"
            />
          </view>
          <text v-if="errors.phone" class="error-text">{{ errors.phone }}</text>
        </view>
        
        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">👤</text>
            <input 
              v-model="registerForm.realName" 
              class="form-input" 
              placeholder="请输入真实姓名"
              :class="{ 'input-error': errors.realName }"
            />
          </view>
          <text v-if="errors.realName" class="error-text">{{ errors.realName }}</text>
        </view>
        
        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">🏫</text>
            <input 
              v-model="registerForm.college" 
              class="form-input" 
              placeholder="请输入学院"
              :class="{ 'input-error': errors.college }"
            />
          </view>
          <text v-if="errors.college" class="error-text">{{ errors.college }}</text>
        </view>
        
        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">🎓</text>
            <input 
              v-model="registerForm.grade" 
              class="form-input" 
              placeholder="请输入年级（如：2022级）"
              :class="{ 'input-error': errors.grade }"
            />
          </view>
          <text v-if="errors.grade" class="error-text">{{ errors.grade }}</text>
        </view>
        
        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">📚</text>
            <input 
              v-model="registerForm.major" 
              class="form-input" 
              placeholder="请输入专业"
              :class="{ 'input-error': errors.major }"
            />
          </view>
          <text v-if="errors.major" class="error-text">{{ errors.major }}</text>
        </view>
        
        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">⚧️</text>
            <picker 
              v-model="registerForm.gender" 
              range="['男', '女']" 
              class="form-input"
              :class="{ 'input-error': errors.gender }"
            >
              <view class="picker-content">{{ registerForm.gender === 1 ? '男' : '女' }}</view>
            </picker>
          </view>
          <text v-if="errors.gender" class="error-text">{{ errors.gender }}</text>
        </view>
        
        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">🔒</text>
            <input 
              v-model="registerForm.password" 
              type="password" 
              class="form-input" 
              placeholder="请输入密码"
              :class="{ 'input-error': errors.password }"
            />
          </view>
          <text v-if="errors.password" class="error-text">{{ errors.password }}</text>
        </view>
        
        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">🔒</text>
            <input 
              v-model="registerForm.confirmPassword" 
              type="password" 
              class="form-input" 
              placeholder="请确认密码"
              :class="{ 'input-error': errors.confirmPassword }"
            />
          </view>
          <text v-if="errors.confirmPassword" class="error-text">{{ errors.confirmPassword }}</text>
        </view>
        
        <view class="form-item">
          <view class="checkbox-wrapper" @click="agreeToTerms = !agreeToTerms">
            <view class="checkbox" :class="{ 'checkbox-checked': agreeToTerms }">
              <text v-if="agreeToTerms" class="check-icon">✓</text>
            </view>
            <text class="checkbox-text">我已阅读并同意《用户协议》和《隐私政策》</text>
          </view>
          <text v-if="errors.terms" class="error-text">{{ errors.terms }}</text>
        </view>
        
        <view class="form-actions">
          <button 
            class="register-btn" 
            :class="{ 'btn-loading': isLoading }" 
            @click="handleRegister"
            :disabled="isLoading"
          >
            {{ isLoading ? '注册中...' : '立即注册' }}
          </button>
        </view>
        
        <!-- 底部链接 -->
        <view class="register-footer">
          <text class="link-text" @click="goToLogin">已有账号？立即登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { mapActions } from 'vuex'
import { register as apiRegister } from '../../api' // 导入注册API

export default {
  name: 'Register',
  data() {
    return {
      registerForm: {
        username: '',
        email: '',
        phone: '',
        realName: '',
        college: '',
        grade: '',
        major: '',
        gender: 1, // 1:男, 2:女
        password: '',
        confirmPassword: ''
      },
      errors: {
        username: '',
        email: '',
        phone: '',
        realName: '',
        college: '',
        grade: '',
        major: '',
        gender: '',
        password: '',
        confirmPassword: '',
        terms: ''
      },
      agreeToTerms: false,
      isLoading: false
    }
  },
  
  methods: {
    ...mapActions(['login']),
    
    validateForm() {
      let isValid = true
      
      // 重置错误信息
      this.errors = {
        username: '',
        email: '',
        phone: '',
        realName: '',
        college: '',
        grade: '',
        major: '',
        gender: '',
        password: '',
        confirmPassword: '',
        terms: ''
      }
      
      // 验证用户名
      if (!this.registerForm.username.trim()) {
        this.errors.username = '请输入用户名'
        isValid = false
      } else if (this.registerForm.username.length < 3) {
        this.errors.username = '用户名至少3个字符'
        isValid = false
      }
      
      // 验证邮箱
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!this.registerForm.email.trim()) {
        this.errors.email = '请输入邮箱'
        isValid = false
      } else if (!emailRegex.test(this.registerForm.email)) {
        this.errors.email = '请输入有效的邮箱地址'
        isValid = false
      }
      
      // 验证手机号
      const phoneRegex = /^1[3-9]\d{9}$/
      if (!this.registerForm.phone.trim()) {
        this.errors.phone = '请输入手机号'
        isValid = false
      } else if (!phoneRegex.test(this.registerForm.phone)) {
        this.errors.phone = '请输入有效的手机号'
        isValid = false
      }
      
      // 验证真实姓名
      if (!this.registerForm.realName.trim()) {
        this.errors.realName = '请输入真实姓名'
        isValid = false
      } else if (this.registerForm.realName.length > 20) {
        this.errors.realName = '真实姓名不能超过20个字符'
        isValid = false
      }
      
      // 验证学院
      if (!this.registerForm.college.trim()) {
        this.errors.college = '请输入学院'
        isValid = false
      } else if (this.registerForm.college.length > 50) {
        this.errors.college = '学院名称不能超过50个字符'
        isValid = false
      }
      
      // 验证年级
      if (!this.registerForm.grade.trim()) {
        this.errors.grade = '请输入年级'
        isValid = false
      } else if (!/^\d{4}级$/.test(this.registerForm.grade)) {
        this.errors.grade = '年级格式不正确，如：2022级'
        isValid = false
      }
      
      // 验证专业
      if (!this.registerForm.major.trim()) {
        this.errors.major = '请输入专业'
        isValid = false
      } else if (this.registerForm.major.length > 50) {
        this.errors.major = '专业名称不能超过50个字符'
        isValid = false
      }
      
      // 验证性别
      if (!this.registerForm.gender || ![1, 2].includes(this.registerForm.gender)) {
        this.errors.gender = '请选择性别'
        isValid = false
      }
      
      // 验证密码
      if (!this.registerForm.password) {
        this.errors.password = '请输入密码'
        isValid = false
      } else if (this.registerForm.password.length < 6) {
        this.errors.password = '密码至少6个字符'
        isValid = false
      }
      
      // 验证确认密码
      if (!this.registerForm.confirmPassword) {
        this.errors.confirmPassword = '请确认密码'
        isValid = false
      } else if (this.registerForm.password !== this.registerForm.confirmPassword) {
        this.errors.confirmPassword = '两次输入的密码不一致'
        isValid = false
      }
      
      // 验证用户协议
      if (!this.agreeToTerms) {
        this.errors.terms = '请阅读并同意用户协议'
        isValid = false
      }
      
      return isValid
    },
    
    async handleRegister() {
      if (!this.validateForm()) {
        return
      }
      
      this.isLoading = true
      
      try {
        // 准备注册数据，移除confirmPassword字段
        const registerData = {
          ...this.registerForm,
          confirmPassword: undefined // 不需要发送确认密码到后端
        }
        
        // 调用真实注册API
        const response = await apiRegister(registerData)
        
        uni.showToast({
          title: '注册成功',
          icon: 'success'
        })
        
        // 注册成功后自动登录（如果API返回用户信息和token）
        if (response.user && response.token) {
          this.login({ user: response.user, token: response.token })
        }
        
        // 跳转登录页面
        setTimeout(() => {
          uni.reLaunch({ url: '/pages/login/login' })
        }, 1500)
        
      } catch (error) {
        uni.showToast({
          title: error.message || '注册失败',
          icon: 'none'
        })
      } finally {
        this.isLoading = false
      }
    },
    
    goToLogin() {
      uni.navigateBack()
    }
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}

.register-card {
  background: white;
  border-radius: 20rpx;
  padding: 60rpx 40rpx;
  width: 100%;
  max-width: 600rpx;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.1);
}

.register-header {
  text-align: center;
  margin-bottom: 60rpx;
}

.page-title {
  display: block;
  font-size: 48rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 10rpx;
}

.page-subtitle {
  display: block;
  font-size: 28rpx;
  color: #666;
}

.register-form {
  width: 100%;
}

.form-item {
  margin-bottom: 30rpx;
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
  border-color: #84fab0;
}

.input-icon {
  margin-right: 20rpx;
  font-size: 32rpx;
}

.form-input {
  flex: 1;
  font-size: 28rpx;
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

.checkbox-wrapper {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  cursor: pointer;
}

.checkbox {
  width: 32rpx;
  height: 32rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 6rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 4rpx;
  transition: all 0.3s;
}

.checkbox-checked {
  background: #84fab0;
  border-color: #84fab0;
}

.check-icon {
  color: white;
  font-size: 20rpx;
  font-weight: bold;
}

.checkbox-text {
  flex: 1;
  font-size: 26rpx;
  color: #666;
  line-height: 1.4;
}

.form-actions {
  margin: 60rpx 0 40rpx;
}

.register-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(45deg, #84fab0, #8fd3f4);
  color: white;
  border: none;
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: 600;
  transition: opacity 0.3s;
}

.register-btn:hover {
  opacity: 0.9;
}

.btn-loading {
  opacity: 0.7;
}

.register-footer {
  text-align: center;
}

.link-text {
  font-size: 28rpx;
  color: #84fab0;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 400px) {
  .register-container {
    padding: 20rpx;
  }
  
  .register-card {
    padding: 40rpx 30rpx;
  }
}
</style>