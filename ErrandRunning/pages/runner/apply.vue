<template>
  <view class="apply-container">
    <view class="apply-header">
      <text class="apply-title">申请成为跑腿员</text>
      <text class="apply-subtitle">加入我们，为同学提供优质的跑腿服务</text>
    </view>

    <view class="form-wrapper">
      <view class="form-card">
        <view class="form-header">
          <text class="form-title">基本信息</text>
        </view>
        
        <view class="form-item">
          <text class="label">真实姓名 <text class="required">*</text></text>
          <input 
            v-model="formData.realName" 
            class="native-input" 
            placeholder="请输入您的真实姓名"
          />
        </view>

        <view class="form-item">
          <text class="label">手机号码 <text class="required">*</text></text>
          <input 
            v-model="formData.phone" 
            class="native-input" 
            placeholder="请输入您的手机号码"
            type="number"
          />
        </view>

        <view class="form-item">
          <text class="label">学院 <text class="required">*</text></text>
          <input 
            v-model="formData.college" 
            class="native-input" 
            placeholder="请输入您的学院"
          />
        </view>

        <view class="form-item">
          <text class="label">年级 <text class="required">*</text></text>
          <input 
            v-model="formData.grade" 
            class="native-input" 
            placeholder="请输入您的年级（如：2022级）"
          />
        </view>

        <view class="form-item">
          <text class="label">专业 <text class="required">*</text></text>
          <input 
            v-model="formData.major" 
            class="native-input" 
            placeholder="请输入您的专业"
          />
        </view>

        <view class="form-item">
          <text class="label">性别 <text class="required">*</text></text>
          <view class="gender-selector" @click="toggleGender">
            <view class="gender-option" :class="{ selected: formData.gender === '男' }" @click.stop="selectGender('男')">
              <view class="gender-radio" :class="{ checked: formData.gender === '男' }"></view>
              <text>男</text>
            </view>
            <view class="gender-option" :class="{ selected: formData.gender === '女' }" @click.stop="selectGender('女')">
              <view class="gender-radio" :class="{ checked: formData.gender === '女' }"></view>
              <text>女</text>
            </view>
          </view>
        </view>
      </view>

      <view class="form-card">
        <view class="form-header">
          <text class="form-title">服务信息</text>
        </view>

        <view class="form-item">
          <text class="label">服务范围 <text class="required">*</text></text>
          <input 
            v-model="formData.serviceArea" 
            class="native-input" 
            placeholder="请输入您的服务范围（如：全校）"
          />
        </view>

        <view class="form-item">
          <text class="label">工作时间 <text class="required">*</text></text>
          <input 
            v-model="formData.workTime" 
            class="native-input" 
            placeholder="请输入您的工作时间（如：8:00-22:00）"
          />
        </view>

        <view class="form-item">
          <text class="label">服务标签</text>
          <view class="tag-selector">
            <text 
              v-for="tag in tagOptions" 
              :key="tag"
              class="tag"
              :class="{ selected: formData.serviceTags.includes(tag) }"
              @click="toggleTag(tag)"
            >
              {{ tag }}
            </text>
          </view>
        </view>

        <view class="form-item">
          <text class="label">个人简介</text>
          <textarea 
            v-model="formData.introduction" 
            class="textarea" 
            placeholder="请简要介绍您的优势和服务理念"
            rows="4"
          ></textarea>
        </view>
      </view>

      <view class="form-card">
        <view class="form-header">
          <text class="form-title">身份验证</text>
        </view>

        <view class="form-item">
          <text class="label">学生证照片 <text class="required">*</text></text>
          <view class="upload-section">
            <view class="upload-btn" @click="chooseImage">
              <text class="upload-icon">📷</text>
              <text class="upload-text">点击上传</text>
            </view>
            <view v-if="formData.studentIdPhoto" class="uploaded-image">
              <uni-image :src="formData.studentIdPhoto" mode="aspectFill" style="width: 100%; height: 100%;"></uni-image>
              <view class="remove-btn" @click="removeImage">
                <text>×</text>
              </view>
            </view>
          </view>
          <text class="hint">请上传清晰的学生证照片，用于身份验证</text>
        </view>
      </view>

      <view class="agreement-section" @click="toggleAgree">
        <view class="checkbox-wrapper">
          <view class="checkbox" :class="{ checked: agreeTerms }">
            <text v-if="agreeTerms">✓</text>
          </view>
        </view>
        <text class="agreement-text">
          我已阅读并同意 <text class="link" @click.stop="showTerms">《跑腿员服务协议》</text> 和 <text class="link" @click.stop="showPrivacy">《隐私政策》</text>
        </text>
      </view>

      <view class="submit-section">
        <button class="submit-btn" type="primary" :disabled="!isFormValid" @click="submitApply">
          提交申请
        </button>
        <text v-if="!isFormValid" class="submit-hint">
          {{ getSubmitHint() }}
        </text>
      </view>
    </view>
  </view>
</template>

<script>
import { post, upload } from '../../api/request'

export default {
  data() {
    return {
      formData: {
        realName: '',
        phone: '',
        college: '',
        grade: '',
        major: '',
        gender: '',
        serviceArea: '',
        workTime: '',
        serviceTags: [],
        introduction: '',
        studentIdPhoto: ''
      },
      tagOptions: ['快递代取', '外卖代送', '物品购买', '文件打印', '其他服务'],
      agreeTerms: false,
      loading: false
    }
  },

  computed: {
    isFormValid() {
      return this.formData.realName && 
             this.formData.phone && 
             this.formData.college && 
             this.formData.grade && 
             this.formData.major && 
             this.formData.gender && 
             this.formData.serviceArea && 
             this.formData.workTime && 
             this.formData.studentIdPhoto && 
             this.agreeTerms
    }
  },

  methods: {

    toggleAgree() {
      this.agreeTerms = !this.agreeTerms
      console.log('协议同意状态:', this.agreeTerms)
    },

    selectGender(gender) {
      this.formData.gender = gender
      console.log('选择性别:', gender)
      console.log('formData.gender:', this.formData.gender)
    },

    toggleGender() {
      // 简单切换性别
      this.formData.gender = this.formData.gender === '男' ? '女' : '男'
      console.log('切换性别:', this.formData.gender)
    },

    toggleTag(tag) {
      const index = this.formData.serviceTags.indexOf(tag)
      if (index > -1) {
        this.formData.serviceTags.splice(index, 1)
      } else {
        this.formData.serviceTags.push(tag)
      }
    },

    chooseImage() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          // 上传图片到后端
          this.uploadImage(res.tempFilePaths[0])
        }
      })
    },

    async uploadImage(tempFilePath) {
      uni.showLoading({
        title: '上传中...'
      })
      try {
        console.log('开始上传文件:', tempFilePath)
        console.log('上传接口路径:', '/runner/applications/upload')
        
        // 调用文件上传接口
        const response = await upload('/runner/applications/upload', {
          filePath: tempFilePath,
          name: 'file'
        })

        console.log('上传响应:', response)
        
        if (response.code === 200 && response.data && response.data.url) {
          this.formData.studentIdPhoto = response.data.url
          uni.showToast({
            title: '上传成功',
            icon: 'success'
          })
        } else {
          console.error('上传失败，响应不符合预期:', response)
          uni.showToast({
            title: '上传失败，请重试',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('上传失败:', error)
        console.error('错误详情:', JSON.stringify(error))
        uni.showToast({
          title: '上传失败，请重试',
          icon: 'none'
        })
      } finally {
        uni.hideLoading()
      }
    },

    removeImage() {
      this.formData.studentIdPhoto = ''
    },

    showTerms() {
      uni.showModal({
        title: '跑腿员服务协议',
        content: '感谢您申请成为校园跑腿员。作为跑腿员，您需要遵守以下规定：\n1. 遵守法律法规和学校规章制度\n2. 提供优质、准时的服务\n3. 保护用户隐私\n4. 诚实守信，不欺诈用户\n5. 及时处理用户投诉',
        showCancel: false
      })
    },

    showPrivacy() {
      uni.showModal({
        title: '隐私政策',
        content: '我们重视您的隐私保护。在您使用我们的服务过程中，我们会收集必要的个人信息，用于：\n1. 身份验证\n2. 服务匹配\n3. 交易安全\n4. 客户服务\n我们承诺不会将您的个人信息用于其他目的。',
        showCancel: false
      })
    },

    checkFormValidity() {
      const missingFields = []
      
      if (!this.formData.realName) missingFields.push('真实姓名')
      if (!this.formData.phone) missingFields.push('手机号')
      if (!this.formData.college) missingFields.push('学院')
      if (!this.formData.grade) missingFields.push('年级')
      if (!this.formData.major) missingFields.push('专业')
      if (!this.formData.gender) missingFields.push('性别')
      if (!this.formData.serviceArea) missingFields.push('服务范围')
      if (!this.formData.workTime) missingFields.push('工作时间')
      if (!this.formData.studentIdPhoto) missingFields.push('学生证照片')
      if (!this.agreeTerms) missingFields.push('同意服务协议')
      
      return missingFields
    },

    getSubmitHint() {
      const missingFields = this.checkFormValidity()
      if (missingFields.length > 0) {
        return `请填写以下必填信息：${missingFields.join('、')}`
      }
      return ''
    },

    async submitApply() {
      const missingFields = this.checkFormValidity()
      
      if (missingFields.length > 0) {
        uni.showToast({
          title: `请填写以下信息：${missingFields.join('、')}`,
          icon: 'none',
          duration: 3000
        })
        return
      }

      this.loading = true
      try {
        console.log('提交申请数据:', this.formData)
        
        // 构建申请数据
        const applicationData = {
          serviceArea: this.formData.serviceArea,
          workTime: this.formData.workTime,
          serviceTags: JSON.stringify(this.formData.serviceTags),
          introduction: this.formData.introduction,
          studentIdPhoto: this.formData.studentIdPhoto
        }

        console.log('发送到后端的数据:', applicationData)
        
        // 调用后端接口
        const response = await post('/runner/applications/submit', applicationData)
        
        console.log('后端响应:', response)
        
        if (response.code === 200) {
          uni.showToast({
            title: '申请提交成功',
            icon: 'success'
          })

          // 跳转到个人中心
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } else {
          uni.showToast({
            title: response.msg || '申请提交失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('申请提交失败:', error)
        uni.showToast({
          title: '网络错误，请重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped lang="scss">
.apply-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 30rpx;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.apply-header {
  text-align: center;
  margin-bottom: 40rpx;

  .apply-title {
    display: block;
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 12rpx;
  }

  .apply-subtitle {
    display: block;
    font-size: 24rpx;
    color: #666;
  }
}

.form-card {
  background: #fff;
  border-radius: 12rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);

  .form-header {
    margin-bottom: 24rpx;

    .form-title {
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
    }
  }

  .form-item {
    margin-bottom: 28rpx;

    .label {
      display: block;
      font-size: 26rpx;
      color: #333;
      margin-bottom: 12rpx;

      .required {
        color: #dd524d;
      }
    }

    .input,
    .native-input,
    .textarea {
      width: 100%;
      border: 1rpx solid #e0e0e0;
      border-radius: 8rpx;
      padding: 20rpx;
      font-size: 26rpx;
      color: #333;
      background: #f9f9f9;
      height: 80rpx;
      line-height: 40rpx;
      box-sizing: border-box;
      display: block;

      &:focus {
        border-color: #007aff;
        background: #fff;
      }
    }

    .textarea {
      height: 200rpx;
      line-height: 40rpx;
      resize: none;
    }

    .gender-selector {
      display: flex;
      gap: 40rpx;
      padding: 10rpx 0;
    }

    .gender-option {
      display: flex;
      align-items: center;
      gap: 8rpx;
      cursor: pointer;
      padding: 8rpx 16rpx;
      border-radius: 20rpx;
      transition: all 0.3s ease;

      &.selected {
        background-color: #e3f2fd;
      }

      .gender-radio {
        width: 24rpx;
        height: 24rpx;
        border: 2rpx solid #ddd;
        border-radius: 50%;
        transition: all 0.3s ease;

        &.checked {
          background-color: #007aff;
          border-color: #007aff;
          position: relative;

          &::after {
            content: '';
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            width: 12rpx;
            height: 12rpx;
            background-color: #fff;
            border-radius: 50%;
          }
        }
      }

      text {
        font-size: 26rpx;
        color: #333;
      }
    }

    .tag-selector {
      display: flex;
      flex-wrap: wrap;
      gap: 12rpx;

      .tag {
        padding: 12rpx 24rpx;
        border: 1rpx solid #e0e0e0;
        border-radius: 20rpx;
        font-size: 24rpx;
        color: #666;
        background: #f9f9f9;
        transition: all 0.3s ease;

        &.selected {
          border-color: #007aff;
          color: #007aff;
          background: #e3f2fd;
        }
      }
    }

    .upload-section {
      position: relative;

      .upload-btn {
        width: 100%;
        height: 200rpx;
        border: 2rpx dashed #e0e0e0;
        border-radius: 8rpx;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        background: #f9f9f9;

        .upload-icon {
          font-size: 48rpx;
          margin-bottom: 12rpx;
        }

        .upload-text {
          font-size: 24rpx;
          color: #999;
        }
      }

      .uploaded-image {
        position: relative;
        width: 100%;
        height: 200rpx;
        border-radius: 8rpx;
        overflow: hidden;

        image {
          width: 100%;
          height: 100%;
        }

        .remove-btn {
          position: absolute;
          top: 12rpx;
          right: 12rpx;
          width: 40rpx;
          height: 40rpx;
          border-radius: 20rpx;
          background: rgba(0, 0, 0, 0.6);
          color: #fff;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 32rpx;
          font-weight: bold;
        }
      }
    }

    .hint {
      display: block;
      font-size: 22rpx;
      color: #999;
      margin-top: 8rpx;
    }
  }
}

.agreement-section {
  display: flex;
  align-items: flex-start;
  margin: 30rpx 0;
  cursor: pointer;

  .checkbox-wrapper {
    margin-top: 4rpx;
    margin-right: 12rpx;
  }

  .checkbox {
    width: 32rpx;
    height: 32rpx;
    border: 2rpx solid #ddd;
    border-radius: 4rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;

    &.checked {
      background-color: #007aff;
      border-color: #007aff;
      color: #fff;
    }

    text {
      font-size: 24rpx;
      font-weight: bold;
    }
  }

  .agreement-text {
    flex: 1;
    font-size: 24rpx;
    color: #666;
    line-height: 1.4;

    .link {
      color: #007aff;
    }
  }
}

.submit-section {
  margin-top: 20rpx;
  text-align: center;

  .submit-btn {
    width: 100%;
    height: 88rpx;
    background: #007aff;
    color: #fff;
    border: none;
    border-radius: 44rpx;
    font-size: 32rpx;
    font-weight: 600;
    transition: all 0.3s ease;

    &:active {
      background: #0056b3;
    }

    &:disabled {
      background: #c0c0c0;
    }
  }

  .submit-hint {
    display: block;
    font-size: 22rpx;
    color: #ff6b6b;
    margin-top: 12rpx;
    line-height: 1.4;
  }
}
</style>