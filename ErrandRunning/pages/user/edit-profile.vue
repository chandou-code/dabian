<template>
  <view class="edit-profile-container">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-left" @click="goBack">
        <text class="nav-icon">←</text>
      </view>
      <view class="nav-title">编辑资料</view>
      <view class="nav-right" @click="saveProfile">
        <text class="nav-btn">保存</text>
      </view>
    </view>

    <!-- 编辑资料表单 -->
    <scroll-view scroll-y class="form-container">
      <!-- 头像上传 -->
      <view class="form-section">
        <view class="form-title">头像</view>
        <view class="avatar-upload">
          <view class="avatar-wrapper">
            <image class="avatar" :src="userInfo.avatar" mode="aspectFill"></image>
            <view class="upload-btn" @click="chooseImage">
              <text class="upload-icon">📷</text>
              <text class="upload-text">更换头像</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 基本信息 -->
      <view class="form-section">
        <view class="form-title">基本信息</view>
        
        <!-- 昵称 -->
        <view class="form-item">
          <view class="item-label">昵称</view>
          <input 
            class="item-input" 
            v-model="userInfo.nickname" 
            placeholder="请输入昵称"
            placeholder-class="placeholder"
          />
        </view>
        
        <!-- 手机号 -->
        <view class="form-item">
          <view class="item-label">手机号</view>
          <input 
            class="item-input" 
            v-model="userInfo.phone" 
            placeholder="请输入手机号"
            placeholder-class="placeholder"
            type="number"
          />
        </view>
        
        <!-- 邮箱 -->
        <view class="form-item">
          <view class="item-label">邮箱</view>
          <input 
            class="item-input" 
            v-model="userInfo.email" 
            placeholder="请输入邮箱"
            placeholder-class="placeholder"
            type="email"
          />
        </view>
        
        <!-- 个性签名 -->
        <view class="form-item">
          <view class="item-label">个性签名</view>
          <textarea 
            class="item-textarea" 
            v-model="userInfo.signature" 
            placeholder="请输入个性签名"
            placeholder-class="placeholder"
            maxlength="100"
            auto-height
          ></textarea>
          <view class="textarea-counter">{{ userInfo.signature.length }}/100</view>
        </view>
      </view>

      <!-- 其他信息 -->
      <view class="form-section">
        <view class="form-title">其他信息</view>
        
        <!-- 真实姓名 -->
        <view class="form-item">
          <view class="item-label">真实姓名</view>
          <input 
            class="item-input" 
            v-model="userInfo.realName" 
            placeholder="请输入真实姓名"
            placeholder-class="placeholder"
          />
        </view>
        
        <!-- 性别 -->
        <view class="form-item">
          <view class="item-label">性别</view>
          <view class="gender-selector">
            <view 
              class="gender-item" 
              :class="{ active: userInfo.gender === 'male' }"
              @click="userInfo.gender = 'male'"
            >
              <text class="gender-text">男</text>
            </view>
            <view 
              class="gender-item" 
              :class="{ active: userInfo.gender === 'female' }"
              @click="userInfo.gender = 'female'"
            >
              <text class="gender-text">女</text>
            </view>
            <view 
              class="gender-item" 
              :class="{ active: userInfo.gender === 'other' }"
              @click="userInfo.gender = 'other'"
            >
              <text class="gender-text">其他</text>
            </view>
          </view>
        </view>
        
        <!-- 所在学院 -->
        <view class="form-item">
          <view class="item-label">所在学院</view>
          <input 
            class="item-input" 
            v-model="userInfo.college" 
            placeholder="请输入所在学院"
            placeholder-class="placeholder"
          />
        </view>
        
        <!-- 年级 -->
        <view class="form-item">
          <view class="item-label">年级</view>
          <input 
            class="item-input" 
            v-model="userInfo.grade" 
            placeholder="请输入年级"
            placeholder-class="placeholder"
          />
        </view>
        
        <!-- 专业 -->
        <view class="form-item">
          <view class="item-label">专业</view>
          <input 
            class="item-input" 
            v-model="userInfo.major" 
            placeholder="请输入专业"
            placeholder-class="placeholder"
          />
        </view>
      </view>
    </scroll-view>

    <!-- 加载遮罩 -->
    <view class="loading-mask" v-if="loading">
      <view class="loading-content">
        <uni-loading-icon type="spinner" size="40"></uni-loading-icon>
        <text class="loading-text">保存中...</text>
      </view>
    </view>
  </view>
</template>

<script>
import { get, post, upload } from '../../api/request'

export default {
  data() {
    return {
      userInfo: {
        id: '',
        nickname: '',
        avatar: '',
        phone: '',
        email: '',
        signature: '',
        realName: '',
        gender: '',
        college: '',
        grade: '',
        major: ''
      },
      loading: false
    }
  },
  
  onLoad() {
    this.loadUserInfo()
  },
  
  methods: {
    // 加载用户信息
    async loadUserInfo() {
      try {
        const response = await get('/auth/user/profile')
        if (response.code === 200 && response.data) {
          // 复制用户信息到编辑表单
          this.userInfo = {
            id: response.data.id,
            nickname: response.data.nickname || '',
            avatar: response.data.avatar || '/static/avatar4.png',
            phone: response.data.phone || '',
            email: response.data.email || '',
            signature: response.data.signature || '',
            realName: response.data.realName || '',
            gender: response.data.gender || '',
            college: response.data.college || '',
            grade: response.data.grade || '',
            major: response.data.major || ''
          }
        }
      } catch (error) {
        console.error('加载用户信息失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      }
    },
    
    // 选择图片
    chooseImage() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          const tempFilePath = res.tempFilePaths[0]
          this.uploadImage(tempFilePath)
        },
        fail: (error) => {
          console.error('选择图片失败:', error)
          uni.showToast({
            title: '选择图片失败',
            icon: 'none'
          })
        }
      })
    },
    
    // 上传图片
    async uploadImage(tempFilePath) {
      this.loading = true
      try {
        const response = await upload('/upload/image', {
          filePath: tempFilePath,
          name: 'file'
        })
        if (response.code === 200 && response.data) {
          // 上传成功，更新头像地址
          this.userInfo.avatar = response.data.url
          uni.showToast({
            title: '头像上传成功',
            icon: 'success'
          })
        } else {
          throw new Error(response.msg || '上传失败')
        }
      } catch (error) {
        console.error('上传图片失败:', error)
        uni.showToast({
          title: '上传失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    
    // 保存资料
    async saveProfile() {
      this.loading = true
      try {
        const response = await post('/auth/user/update-profile', this.userInfo)
        if (response.code === 200) {
          uni.showToast({
            title: '保存成功',
            icon: 'success'
          })
          // 返回上一页并刷新
          setTimeout(() => {
            uni.navigateBack({
              delta: 1
            })
          }, 1500)
        } else {
          throw new Error(response.msg || '保存失败')
        }
      } catch (error) {
        console.error('保存资料失败:', error)
        uni.showToast({
          title: error.message || '保存失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    
    // 返回上一页
    goBack() {
      uni.navigateBack({
        delta: 1
      })
    }
  }
}
</script>

<style scoped lang="scss">
.edit-profile-container {
  min-height: 100vh;
  background: #f5f5f5;
}

/* 顶部导航栏 */
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  background: #fff;
  padding: 0 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
  
  .nav-left, .nav-right {
    width: 60px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .nav-icon {
    font-size: 20px;
    color: #333;
  }
  
  .nav-title {
    flex: 1;
    text-align: center;
    font-size: 18px;
    font-weight: 500;
    color: #333;
  }
  
  .nav-btn {
    font-size: 16px;
    color: #2196f3;
  }
}

/* 表单容器 */
.form-container {
  padding: 16px;
}

/* 表单区块 */
.form-section {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 16px;
  padding: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.08);
  
  .form-title {
    font-size: 16px;
    font-weight: 500;
    color: #333;
    margin-bottom: 16px;
  }
}

/* 头像上传 */
.avatar-upload {
  display: flex;
  justify-content: center;
  
  .avatar-wrapper {
    position: relative;
    width: 100px;
    height: 100px;
    border-radius: 50%;
    overflow: hidden;
    border: 2px solid #e0e0e0;
    
    .avatar {
      width: 100%;
      height: 100%;
    }
    
    .upload-btn {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      background: rgba(0, 0, 0, 0.5);
      color: #fff;
      text-align: center;
      padding: 8px;
      font-size: 12px;
      display: flex;
      flex-direction: column;
      align-items: center;
      
      .upload-icon {
        font-size: 16px;
        margin-bottom: 4px;
      }
      
      .upload-text {
        font-size: 12px;
      }
    }
  }
}

/* 表单项 */
.form-item {
  margin-bottom: 20px;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .item-label {
    font-size: 14px;
    color: #666;
    margin-bottom: 8px;
    display: block;
  }
  
  .item-input, .item-textarea {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #e0e0e0;
    border-radius: 4px;
    font-size: 14px;
    color: #333;
    background: #fff;
    
    &:focus {
      border-color: #2196f3;
      outline: none;
    }
  }
  
  .item-textarea {
    min-height: 80px;
    resize: none;
    line-height: 1.5;
  }
  
  .placeholder {
    color: #999;
  }
  
  .textarea-counter {
    text-align: right;
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }
}

/* 性别选择器 */
.gender-selector {
  display: flex;
  gap: 16px;
  
  .gender-item {
    flex: 1;
    padding: 10px;
    border: 1px solid #e0e0e0;
    border-radius: 4px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s;
    
    &.active {
      border-color: #2196f3;
      background: #e3f2fd;
      color: #2196f3;
    }
    
    .gender-text {
      font-size: 14px;
    }
  }
}

/* 加载遮罩 */
.loading-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
  
  .loading-content {
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .loading-text {
      margin-top: 12px;
      font-size: 14px;
      color: #333;
    }
  }
}
</style>
