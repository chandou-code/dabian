<template>
  <view class="register-container">
    <view class="register-card">
      <view class="register-header">
        <text class="page-title">用户注册</text>
        <text class="page-subtitle">加入我们，让失物找到回家的路</text>
      </view>

      <view class="register-form">
        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">👤</text>
            <input
              class="form-input"
              type="text"
              v-model="form.username"
              placeholder="请输入用户名"
              maxlength="50"
            />
          </view>
        </view>

        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">📧</text>
            <input
              class="form-input"
              type="text"
              v-model="form.email"
              placeholder="请输入邮箱"
              maxlength="100"
            />
          </view>
        </view>

        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">📱</text>
            <input
              class="form-input"
              type="number"
              v-model="form.phone"
              placeholder="请输入手机号"
              maxlength="11"
            />
          </view>
        </view>

        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">👤</text>
            <input
              class="form-input"
              type="text"
              v-model="form.realName"
              placeholder="请输入真实姓名"
              maxlength="50"
            />
          </view>
        </view>

        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">🏫</text>
            <input
              class="form-input"
              type="text"
              v-model="form.college"
              placeholder="请输入学院"
              maxlength="100"
            />
          </view>
        </view>

        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">🎓</text>
            <input
              class="form-input"
              type="text"
              v-model="form.grade"
              placeholder="请输入年级（如：2022级）"
              maxlength="20"
            />
          </view>
        </view>

        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">📚</text>
            <input
              class="form-input"
              type="text"
              v-model="form.major"
              placeholder="请输入专业"
              maxlength="100"
            />
          </view>
        </view>

        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">⚧️</text>
            <view class="gender-picker">
              <view
                class="gender-option"
                :class="{ active: form.gender === '男' }"
                @click="form.gender = '男'"
              >
                <text class="gender-text">男</text>
              </view>
              <view
                class="gender-option"
                :class="{ active: form.gender === '女' }"
                @click="form.gender = '女'"
              >
                <text class="gender-text">女</text>
              </view>
            </view>
          </view>
        </view>

        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">🔒</text>
            <input
              class="form-input"
              type="password"
              v-model="form.password"
              placeholder="请输入密码"
              maxlength="20"
            />
          </view>
        </view>

        <view class="form-item">
          <view class="input-wrapper">
            <text class="input-icon">🔒</text>
            <input
              class="form-input"
              type="password"
              v-model="form.confirmPassword"
              placeholder="请确认密码"
              maxlength="20"
            />
          </view>
        </view>

        <view class="agreement">
          <checkbox
            :checked="agreed"
            @click="agreed = !agreed"
            color="#2196f3"
          />
          <text class="agreement-text">
            我已阅读并同意
            <text class="link" @click="showAgreement">《用户服务协议》</text>
            和
            <text class="link" @click="showPrivacy">《隐私政策》</text>
          </text>
        </view>

        <button class="register-btn" @click="handleRegister">注册</button>

        <view class="login-tip">
          <text class="tip-text">已有账号？</text>
          <text class="link" @click="goToLogin">立即登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { post } from '../../api/request'

export default {
  data() {
    return {
      form: {
        username: '',
        email: '',
        phone: '',
        realName: '',
        college: '',
        grade: '',
        major: '',
        gender: '',
        password: '',
        confirmPassword: ''
      },
      agreed: false
    }
  },
  methods: {
    validateForm() {
      if (!this.form.username) {
        uni.showToast({
          title: '请输入用户名',
          icon: 'none'
        })
        return false
      }

      if (!this.form.email) {
        uni.showToast({
          title: '请输入邮箱',
          icon: 'none'
        })
        return false
      }

      if (!this.form.phone) {
        uni.showToast({
          title: '请输入手机号',
          icon: 'none'
        })
        return false
      }

      if (!this.form.realName) {
        uni.showToast({
          title: '请输入真实姓名',
          icon: 'none'
        })
        return false
      }

      if (!this.form.college) {
        uni.showToast({
          title: '请输入学院',
          icon: 'none'
        })
        return false
      }

      if (!this.form.grade) {
        uni.showToast({
          title: '请输入年级',
          icon: 'none'
        })
        return false
      }

      if (!this.form.major) {
        uni.showToast({
          title: '请输入专业',
          icon: 'none'
        })
        return false
      }

      if (!this.form.gender) {
        uni.showToast({
          title: '请选择性别',
          icon: 'none'
        })
        return false
      }

      if (this.form.password.length < 6 || this.form.password.length > 20) {
        uni.showToast({
          title: '密码长度为6-20位',
          icon: 'none'
        })
        return false
      }

      if (this.form.password !== this.form.confirmPassword) {
        uni.showToast({
          title: '两次密码输入不一致',
          icon: 'none'
        })
        return false
      }

      if (!this.agreed) {
        uni.showToast({
          title: '请阅读并同意协议',
          icon: 'none'
        })
        return false
      }

      return true
    },

    async handleRegister() {
      if (!this.validateForm()) {
        return
      }

      uni.showLoading({ title: '注册中...' })

      try {
        const response = await post('/auth/register', {
          username: this.form.username,
          password: this.form.password,
          email: this.form.email,
          phone: this.form.phone,
          realName: this.form.realName,
          nickname: this.form.realName, // 使用真实姓名作为昵称
          college: this.form.college,
          grade: this.form.grade,
          major: this.form.major,
          gender: this.form.gender
        })

        uni.hideLoading()

        if (response.code === 200) {
          uni.showToast({
            title: '注册成功',
            icon: 'success'
          })

          // 保存token和用户信息
          uni.setStorageSync('token', response.data.token)
          uni.setStorageSync('user', response.data.user)
          uni.setStorageSync('role', response.data.user.role)

          setTimeout(() => {
            uni.navigateTo({
              url: '/pages/user/profile'
            })
          }, 1500)
        } else {
          uni.showToast({
            title: response.msg || '注册失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.hideLoading()
        uni.showToast({
          title: '注册失败，请稍后重试',
          icon: 'none'
        })
        console.error('注册失败:', error)
      }
    },

    goToLogin() {
      uni.navigateTo({
        url: '/pages/login/login'
      })
    },

    showAgreement() {
      uni.showToast({
        title: '用户服务协议',
        icon: 'none'
      })
    },

    showPrivacy() {
      uni.showToast({
        title: '隐私政策',
        icon: 'none'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}

.register-card {
  width: 100%;
  max-width: 600rpx;
  background: white;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.register-header {
  padding: 50rpx 40rpx 30rpx;
  text-align: center;

  .page-title {
    display: block;
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 16rpx;
  }

  .page-subtitle {
    display: block;
    font-size: 24rpx;
    color: #999;
  }
}

.register-form {
  padding: 0 40rpx 50rpx;
}

.form-item {
  margin-bottom: 30rpx;

  .input-wrapper {
    display: flex;
    align-items: center;
    background: #f5f5f5;
    border-radius: 12rpx;
    padding: 0 24rpx;
    height: 88rpx;

    .input-icon {
      font-size: 32rpx;
      margin-right: 20rpx;
      color: #666;
    }

    .form-input {
      flex: 1;
      height: 100%;
      font-size: 28rpx;
      color: #333;
      background: transparent;
      border: none;
      outline: none;
    }

    .gender-picker {
      flex: 1;
      display: flex;
      gap: 20rpx;

      .gender-option {
        flex: 1;
        height: 60rpx;
        background: white;
        border-radius: 8rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 2rpx solid #e0e0e0;

        &.active {
          border-color: #2196f3;
          background: #e3f2fd;
        }

        .gender-text {
          font-size: 28rpx;
          color: #333;
        }
      }
    }
  }
}

.agreement {
  display: flex;
  align-items: center;
  margin: 40rpx 0;
  gap: 16rpx;

  .agreement-text {
    font-size: 24rpx;
    color: #666;
    line-height: 1.4;

    .link {
      color: #2196f3;
    }
  }
}

.register-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 48rpx;
  font-size: 32rpx;
  border: none;
  font-weight: bold;
  margin-bottom: 30rpx;
}

.login-tip {
  text-align: center;

  .tip-text {
    font-size: 24rpx;
    color: #999;
  }

  .link {
    color: #2196f3;
    font-size: 24rpx;
    margin-left: 8rpx;
  }
}
</style>
