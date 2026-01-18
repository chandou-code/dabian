<template>
  <view class="admin-container">
    <view class="admin-header">
      <text class="admin-title">系统设置</text>
      <text class="admin-subtitle">管理平台全局配置</text>
    </view>
    
    <!-- 调试信息：直接显示原始数据 -->
    <view class="debug-section">
      <text class="debug-title">调试信息</text>
      <text class="debug-content">{{ JSON.stringify(settings, null, 2) }}</text>
    </view>

    <view class="settings-section">
      <!-- 基本设置 -->
      <view class="setting-card">
        <view class="setting-header">
          <text class="setting-title">基本设置</text>
        </view>
        <view class="setting-content">
          <view class="form-item">
            <text class="form-label">平台名称</text>
            <input 
              v-model="settings.platformName" 
              placeholder="请输入平台名称" 
              class="form-input"
            />
          </view>
          <view class="form-item">
            <text class="form-label">平台描述</text>
            <textarea 
              v-model="settings.platformDescription" 
              placeholder="请输入平台描述" 
              rows="3"
              class="form-textarea"
            ></textarea>
          </view>
          <view class="form-item">
            <text class="form-label">客服邮箱</text>
            <input 
              v-model="settings.supportEmail" 
              placeholder="请输入客服邮箱" 
              class="form-input"
            />
          </view>
          <view class="form-item">
            <text class="form-label">客服电话</text>
            <input 
              v-model="settings.supportPhone" 
              placeholder="请输入客服电话" 
              class="form-input"
            />
          </view>
        </view>
      </view>

      <!-- 通知设置 -->
      <view class="setting-card">
        <view class="setting-header">
          <text class="setting-title">通知设置</text>
        </view>
        <view class="setting-content">
          <view class="form-item">
            <text class="form-label">新任务通知</text>
            <view class="switch-container">
              <input 
                type="checkbox" 
                v-model="settings.newTaskNotification" 
                class="custom-switch"
              />
              <text>{{ settings.newTaskNotification ? '开启' : '关闭' }}</text>
            </view>
          </view>
          <view class="form-item">
            <text class="form-label">任务状态变更通知</text>
            <view class="switch-container">
              <input 
                type="checkbox" 
                v-model="settings.taskStatusNotification" 
                class="custom-switch"
              />
              <text>{{ settings.taskStatusNotification ? '开启' : '关闭' }}</text>
            </view>
          </view>
          <view class="form-item">
            <text class="form-label">评价通知</text>
            <view class="switch-container">
              <input 
                type="checkbox" 
                v-model="settings.reviewNotification" 
                class="custom-switch"
              />
              <text>{{ settings.reviewNotification ? '开启' : '关闭' }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 任务设置 -->
      <view class="setting-card">
        <view class="setting-header">
          <text class="setting-title">任务设置</text>
        </view>
        <view class="setting-content">
          <view class="form-item">
            <text class="form-label">默认任务超时时间（小时）</text>
            <input 
              v-model="settings.defaultTaskTimeout" 
              placeholder="请输入默认任务超时时间" 
              type="number"
              class="form-input"
            />
            <text class="form-debug">{{ settings.defaultTaskTimeout }} ({{ typeof settings.defaultTaskTimeout }})</text>
          </view>
          <view class="form-item">
            <text class="form-label">任务审核开关</text>
            <view class="switch-container">
              <input 
                type="checkbox" 
                v-model="settings.taskApprovalRequired" 
                class="custom-switch"
              />
              <text>{{ settings.taskApprovalRequired ? '开启' : '关闭' }}</text>
            </view>
            <text class="form-debug">{{ settings.taskApprovalRequired }} ({{ typeof settings.taskApprovalRequired }})</text>
          </view>
          <view class="form-item">
            <text class="form-label">最大同时接单数量</text>
            <input 
              v-model="settings.maxConcurrentTasks" 
              placeholder="请输入最大同时接单数量" 
              type="number"
              class="form-input"
            />
            <text class="form-debug">{{ settings.maxConcurrentTasks }} ({{ typeof settings.maxConcurrentTasks }})</text>
          </view>
        </view>
      </view>

      <!-- 支付设置 -->
      <view class="setting-card">
        <view class="setting-header">
          <text class="setting-title">支付设置</text>
        </view>
        <view class="setting-content">
          <view class="form-item">
            <text class="form-label">启用在线支付</text>
            <view class="switch-container">
              <input 
                type="checkbox" 
                v-model="settings.enableOnlinePayment" 
                class="custom-switch"
              />
              <text>{{ settings.enableOnlinePayment ? '开启' : '关闭' }}</text>
            </view>
          </view>
          <view class="form-item">
            <text class="form-label">平台服务费比例（%）</text>
            <input 
              v-model="settings.platformFeeRate" 
              placeholder="请输入平台服务费比例" 
              type="number"
              class="form-input"
            />
          </view>
        </view>
      </view>
    </view>

    <!-- 底部操作按钮 -->
    <view class="footer-actions">
      <button class="btn btn-primary" @click="saveSettings">保存设置</button>
      <button class="btn btn-secondary" @click="resetSettings">重置</button>
    </view>
  </view>
</template>

<script>
import { get, put } from '../../api/request'

export default {
  data() {
    return {
      settings: {
        platformName: '校园跑腿',
        platformDescription: '专注于校园内的跑腿服务平台',
        supportEmail: 'support@example.com',
        supportPhone: '1234567890',
        newTaskNotification: true,
        taskStatusNotification: true,
        reviewNotification: true,
        defaultTaskTimeout: 24,
        taskApprovalRequired: false,
        maxConcurrentTasks: 5,
        enableOnlinePayment: true,
        platformFeeRate: 5.0
      },
      originalSettings: {}
    }
  },

  onLoad() {
    this.loadSettings()
  },
  
  // 添加onReady钩子，确保组件初始化完成后再更新数据
  onReady() {
    console.log('组件初始化完成，当前设置数据:', this.settings)
  },

  methods: {
    async loadSettings() {
      try {
        console.log('开始获取系统设置...')
        const response = await get('/admin/settings')
        console.log('获取系统设置响应:', response)
        if (response.code === 200 && response.data) {
          // 打印原始数据
          console.log('原始设置数据:', this.settings)
          // 打印后端返回数据
          console.log('后端返回数据:', response.data)
          // 使用Object.assign确保settings对象保持响应式
          Object.assign(this.settings, response.data)
          // 打印合并后数据
          console.log('合并后设置数据:', this.settings)
          this.originalSettings = JSON.parse(JSON.stringify(this.settings))
        }
      } catch (error) {
        console.error('获取系统设置失败:', error)
        uni.showToast({
          title: '获取设置失败',
          icon: 'none'
        })
      }
    },

    async saveSettings() {
      uni.showLoading({ title: '保存中...' })
      try {
        const response = await put('/admin/settings', this.settings)
        if (response.code === 200) {
          this.originalSettings = JSON.parse(JSON.stringify(this.settings))
          uni.showToast({
            title: '保存成功',
            icon: 'success'
          })
        } else {
          uni.showToast({
            title: response.msg || '保存失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('保存系统设置失败:', error)
        uni.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none'
        })
      } finally {
        uni.hideLoading()
      }
    },

    resetSettings() {
      this.settings = JSON.parse(JSON.stringify(this.originalSettings))
      uni.showToast({
        title: '已重置',
        icon: 'success'
      })
    }
  }
}
</script>

<style scoped lang="scss">
.admin-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 30rpx;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.admin-header {
  text-align: center;
  margin-bottom: 40rpx;

  .admin-title {
    display: block;
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 12rpx;
  }

  .admin-subtitle {
    display: block;
    font-size: 24rpx;
    color: #666;
  }
}

.settings-section {
  margin-bottom: 40rpx;
}

.setting-card {
  background: #fff;
  border-radius: 12rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.setting-header {
  padding: 24rpx 30rpx;
  background: #f8f9fa;
  border-bottom: 1rpx solid #e9ecef;

  .setting-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #333;
  }
}

.setting-content {
  padding: 30rpx;
}

.form-item {
  display: flex;
  flex-direction: column;
  margin-bottom: 30rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.form-label {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 12rpx;
}

.form-debug {
  font-size: 20rpx;
  color: #999;
  margin-top: 8rpx;
  font-family: monospace;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 20rpx;
  border: 1rpx solid #e9ecef;
  border-radius: 8rpx;
  font-size: 26rpx;
  color: #333;
  box-sizing: border-box;
}

.form-textarea {
  height: 160rpx;
  min-height: 120rpx;
  resize: vertical;
  line-height: 1.5;
}

/* 自定义开关样式 */
.switch-container {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 8rpx;
}

.custom-switch {
  width: 60rpx;
  height: 36rpx;
  -webkit-appearance: none;
  appearance: none;
  background-color: #e9ecef;
  border-radius: 18rpx;
  position: relative;
  outline: none;
  cursor: pointer;
  transition: background-color 0.3s;
}

.custom-switch:checked {
  background-color: #2196f3;
}

.custom-switch::before {
  content: '';
  position: absolute;
  width: 30rpx;
  height: 30rpx;
  border-radius: 50%;
  background-color: white;
  top: 3rpx;
  left: 3rpx;
  transition: transform 0.3s;
  box-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.2);
}

.custom-switch:checked::before {
  transform: translateX(24rpx);
}

/* 按钮样式修复 */
.btn {
  flex: 1;
  height: 80rpx;
  border-radius: 8rpx;
  font-size: 28rpx;
  font-weight: 500;
  border: none;
  transition: all 0.2s ease;
  color: white;
  background-color: #2196f3;
}

.btn-primary {
  background-color: #1976d2;
}

.btn-secondary {
  background-color: #6c757d;
}

.footer-actions {
  display: flex;
  gap: 20rpx;
  padding: 0 30rpx 40rpx;
}

.btn {
  flex: 1;
  height: 80rpx;
  border-radius: 8rpx;
  font-size: 28rpx;
  font-weight: 500;
  border: none;
  transition: all 0.2s ease;

  &.btn-primary {
    background: #1976d2;
    color: #fff;
  }

  &.btn-secondary {
    background: #6c757d;
    color: #fff;
  }
}

/* 调试区域样式 */
.debug-section {
  background: #f0f8ff;
  border: 1rpx solid #b3e5fc;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 30rpx;
  overflow: auto;
}

.debug-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #0277bd;
  display: block;
  margin-bottom: 16rpx;
}

.debug-content {
  font-size: 22rpx;
  color: #01579b;
  font-family: monospace;
  white-space: pre-wrap;
  line-height: 1.5;
}
</style>