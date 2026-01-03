<template>
  <view class="profile-container">
    <Sidebar />
    
    <view class="main-content" :class="{ 'main-content-expanded': !showSidebar }">
      <view class="profile-card">
        <view class="card-header">
          <text class="page-title">个人信息</text>
        </view>
        
        <!-- 用户基本信息 -->
        <view class="user-info-section">
          <view class="avatar-section">
            <image class="user-avatar" :src="userInfo.avatar" mode="aspectFill"></image>
            <button class="change-avatar-btn">更换头像</button>
          </view>
          
          <view class="info-form">
            <view class="form-item">
              <text class="form-label">用户名</text>
              <input v-model="userInfo.username" class="form-input" disabled />
            </view>
            
            <view class="form-item">
              <text class="form-label">真实姓名</text>
              <input v-model="userInfo.realName" class="form-input" placeholder="请输入真实姓名" />
            </view>
            
            <view class="form-item">
              <text class="form-label">邮箱</text>
              <input v-model="userInfo.email" class="form-input" placeholder="请输入邮箱地址" />
            </view>
            

            
            <view class="form-item">
              <text class="form-label">手机号</text>
              <input v-model="userInfo.phone" class="form-input" placeholder="请输入手机号" />
            </view>
            
            <view class="form-item">
              <text class="form-label">学院</text>
              <input v-model="userInfo.college" class="form-input" placeholder="请输入所在学院" />
            </view>
            
            <view class="form-item">
              <text class="form-label">年级</text>
              <input v-model="userInfo.grade" class="form-input" placeholder="请输入年级" />
            </view>
          </view>
        </view>
        
        <!-- 修改密码 -->
        <view class="password-section">
          <text class="section-title">修改密码</text>
          
          <view class="form-item">
            <text class="form-label">当前密码</text>
            <input v-model="passwordForm.currentPassword" type="password" class="form-input" placeholder="请输入当前密码" />
          </view>
          
          <view class="form-item">
            <text class="form-label">新密码</text>
            <input v-model="passwordForm.newPassword" type="password" class="form-input" placeholder="请输入新密码" />
          </view>
          
          <view class="form-item">
            <text class="form-label">确认新密码</text>
            <input v-model="passwordForm.confirmPassword" type="password" class="form-input" placeholder="请再次输入新密码" />
          </view>
          
          <button class="update-password-btn" @click="updatePassword">修改密码</button>
        </view>
        
        <!-- 保存按钮 -->
        <view class="form-actions">
          <button class="save-btn" @click="saveProfile">保存信息</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import Sidebar from '@/components/Sidebar.vue'
import { mapGetters } from 'vuex'

export default {
  name: 'UserProfile',
  components: {
    Sidebar
  },
  
  data() {
    return {
      showSidebar: true,
      userInfo: {
        username: '',
        realName: '',
        email: '',
        phone: '',
        college: '',
        grade: '',
        avatar: '/static/default-avatar.png'
      },
      passwordForm: {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    }
  },
  
  computed: {
    ...mapGetters(['user'])
  },
  
  onLoad() {
    this.loadUserInfo()
  },
  
  methods: {
    loadUserInfo() {
      if (this.user) {
        this.userInfo = { ...this.userInfo, ...this.user }
      }
    },
    
    saveProfile() {
      uni.showToast({
        title: '保存成功',
        icon: 'success'
      })
    },
    
    updatePassword() {
      if (!this.passwordForm.currentPassword || !this.passwordForm.newPassword || !this.passwordForm.confirmPassword) {
        uni.showToast({
          title: '请填写完整密码信息',
          icon: 'none'
        })
        return
      }
      
      if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
        uni.showToast({
          title: '两次输入的密码不一致',
          icon: 'none'
        })
        return
      }
      
      uni.showToast({
        title: '密码修改成功',
        icon: 'success'
      })
      
      // 清空密码表单
      this.passwordForm = {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    }
  }
}
</script>

<style scoped>
.profile-container {
  display: flex;
  min-height: 100vh;
  background: #f5f5f5;
}

.main-content {
  flex: 1;
  margin-left: 250px;
  padding: 30rpx;
  transition: margin-left 0.3s ease;
}

.main-content-expanded {
  margin-left: 70px;
}

.profile-card {
  background: white;
  border-radius: 16rpx;
  padding: 40rpx;
  max-width: 800rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
}

.card-header {
  margin-bottom: 40rpx;
}

.page-title {
  font-size: 40rpx;
  font-weight: 600;
  color: #333;
}

.user-info-section {
  margin-bottom: 40rpx;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 40rpx;
}

.user-avatar {
  width: 150rpx;
  height: 150rpx;
  border-radius: 50%;
  margin-bottom: 20rpx;
  border: 4rpx solid #f0f0f0;
}

.change-avatar-btn {
  background: #2196f3;
  color: white;
  border: none;
  padding: 15rpx 30rpx;
  border-radius: 8rpx;
  font-size: 26rpx;
}

.info-form {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300rpx, 1fr));
  gap: 30rpx;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.form-label {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.form-input {
  height: 70rpx;
  padding: 0 20rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 28rpx;
  background: #fafafa;
}

.form-input:focus {
  border-color: #2196f3;
  background: white;
}

.password-section {
  padding: 30rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  margin-bottom: 40rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 30rpx;
  display: block;
}

.update-password-btn {
  background: #4caf50;
  color: white;
  border: none;
  padding: 20rpx 40rpx;
  border-radius: 8rpx;
  font-size: 28rpx;
  margin-top: 20rpx;
}

.form-actions {
  text-align: center;
}

.save-btn {
  background: #2196f3;
  color: white;
  border: none;
  padding: 20rpx 60rpx;
  border-radius: 8rpx;
  font-size: 32rpx;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
    padding: 20rpx;
  }
  
  .info-form {
    grid-template-columns: 1fr;
  }
}
</style>