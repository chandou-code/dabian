<template>
  <view class="system-settings">
    <Sidebar />
    
    <view class="main-content" :class="{ 'main-content-expanded': !showSidebar }">
      <view class="settings-container">
        <!-- 系统参数配置 -->
        <view class="settings-section">
          <text class="section-title">系统参数配置</text>
          
          <view class="setting-item">
            <text class="setting-label">系统名称</text>
            <input v-model="systemConfig.systemName" class="setting-input" />
          </view>
          
          <view class="setting-item">
            <text class="setting-label">系统描述</text>
            <textarea v-model="systemConfig.description" class="setting-textarea"></textarea>
          </view>
          
          <view class="setting-item">
            <text class="setting-label">版本号</text>
            <input v-model="systemConfig.version" class="setting-input" />
          </view>
          
          <view class="setting-item">
            <text class="setting-label">维护模式</text>
            <switch :checked="systemConfig.maintenanceMode" @change="toggleMaintenance" />
          </view>
        </view>
        
        <!-- 邮件通知模板 -->
        <view class="settings-section">
          <text class="section-title">邮件通知模板</text>
          
          <view class="template-item">
            <text class="template-title">审核通过通知</text>
            <textarea v-model="emailTemplates.approval" class="template-content" placeholder="邮件内容模板..."></textarea>
            <button class="preview-btn" @click="previewTemplate('approval')">预览</button>
          </view>
          
          <view class="template-item">
            <text class="template-title">审核驳回通知</text>
            <textarea v-model="emailTemplates.rejection" class="template-content" placeholder="邮件内容模板..."></textarea>
            <button class="preview-btn" @click="previewTemplate('rejection')">预览</button>
          </view>
          
          <view class="template-item">
            <text class="template-title">找回物品通知</text>
            <textarea v-model="emailTemplates.recovery" class="template-content" placeholder="邮件内容模板..."></textarea>
            <button class="preview-btn" @click="previewTemplate('recovery')">预览</button>
          </view>
        </view>
        
        <!-- AI接口配置 -->
        <view class="settings-section">
          <text class="section-title">AI接口配置</text>
          
          <view class="setting-item">
            <text class="setting-label">AI识别API地址</text>
            <input v-model="aiConfig.apiUrl" class="setting-input" placeholder="https://api.example.com/recognize" />
          </view>
          
          <view class="setting-item">
            <text class="setting-label">API密钥</text>
            <input v-model="aiConfig.apiKey" type="password" class="setting-input" placeholder="请输入API密钥" />
          </view>
          
          <view class="setting-item">
            <text class="setting-label">AI描述生成</text>
            <switch :checked="aiConfig.enabled" @change="toggleAI" />
          </view>
          
          <view class="setting-item">
            <text class="setting-label">识别准确率阈值</text>
            <slider 
              :value="aiConfig.threshold" 
              @change="onThresholdChange"
              min="0"
              max="100"
              show-value
              class="setting-slider"
            />
          </view>
          
          <button class="test-btn" @click="testAIConfig">测试AI接口</button>
        </view>
        
        <!-- 公告管理 -->
        <view class="settings-section">
          <text class="section-title">公告管理</text>
          
          <view class="announcement-form">
            <view class="form-item">
              <text class="form-label">公告标题</text>
              <input v-model="newAnnouncement.title" class="form-input" placeholder="请输入公告标题" />
            </view>
            
            <view class="form-item">
              <text class="form-label">公告内容</text>
              <textarea v-model="newAnnouncement.content" class="form-textarea" placeholder="请输入公告内容"></textarea>
            </view>
            
            <view class="form-item">
              <text class="form-label">公告类型</text>
              <picker 
                :range="announcementTypes" 
                :value="announcementTypeIndex"
                @change="onAnnouncementTypeChange"
                class="form-picker"
              >
                <view class="picker-content">
                  {{ announcementTypes[announcementTypeIndex] }}
                </view>
              </picker>
            </view>
            
            <button class="publish-btn" @click="publishAnnouncement">发布公告</button>
          </view>
          
          <!-- 已发布公告列表 -->
          <view class="announcement-list">
            <text class="list-title">已发布公告</text>
            
            <view v-for="announcement in announcements" :key="announcement.id" class="announcement-item">
              <view class="announcement-content">
                <text class="announcement-title">{{ announcement.title }}</text>
                <text class="announcement-desc">{{ announcement.content }}</text>
                <text class="announcement-time">{{ announcement.time }}</text>
              </view>
              
              <view class="announcement-actions">
                <button class="action-btn edit-btn" @click="editAnnouncement(announcement)">编辑</button>
                <button class="action-btn delete-btn" @click="deleteAnnouncement(announcement)">删除</button>
              </view>
            </view>
          </view>
        </view>
        
        <!-- 保存按钮 -->
        <view class="save-section">
          <button class="save-btn" @click="saveSettings">保存所有设置</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import Sidebar from '@/components/Sidebar.vue'

export default {
  name: 'SystemSettings',
  components: {
    Sidebar
  },
  
  data() {
    return {
      showSidebar: true,
      announcementTypeIndex: 0,
      announcementTypes: ['系统公告', '活动通知', '维护通知', '其他'],
      
      systemConfig: {
        systemName: '校园失物招领系统',
        description: '让失物回家，让爱心传递',
        version: '1.0.0',
        maintenanceMode: false
      },
      
      emailTemplates: {
        approval: '您好，您发布的信息已通过审核，感谢您的支持！',
        rejection: '您好，很遗憾您发布的信息未通过审核，请检查后重新提交。',
        recovery: '您好，恭喜您找回丢失的物品，记得给好心人点赞哦！'
      },
      
      aiConfig: {
        apiUrl: 'https://api.example.com/recognize',
        apiKey: '',
        enabled: true,
        threshold: 80
      },
      
      newAnnouncement: {
        title: '',
        content: '',
        type: '系统公告'
      },
      
      announcements: [
        {
          id: 1,
          title: '系统升级通知',
          content: '系统将于今晚22:00进行升级维护，预计2小时完成。',
          time: '2024-01-15 10:30'
        },
        {
          id: 2,
          title: '使用指南更新',
          content: '新增AI智能识别功能，欢迎使用！',
          time: '2024-01-14 15:20'
        }
      ]
    }
  },
  
  onLoad() {
    this.loadSettings()
  },
  
  methods: {
    loadSettings() {
      // 实际项目中从API加载配置
      console.log('加载系统设置...')
    },
    
    toggleMaintenance(e) {
      this.systemConfig.maintenanceMode = e.detail.value
    },
    
    toggleAI(e) {
      this.aiConfig.enabled = e.detail.value
    },
    
    onThresholdChange(e) {
      this.aiConfig.threshold = e.detail.value
    },
    
    onAnnouncementTypeChange(e) {
      this.announcementTypeIndex = e.detail.value
      this.newAnnouncement.type = this.announcementTypes[this.announcementTypeIndex]
    },
    
    previewTemplate(type) {
      const content = this.emailTemplates[type]
      uni.showModal({
        title: '模板预览',
        content: content || '模板内容为空',
        showCancel: false
      })
    },
    
    async testAIConfig() {
      if (!this.aiConfig.apiUrl) {
        uni.showToast({
          title: '请先配置API地址',
          icon: 'none'
        })
        return
      }
      
      uni.showLoading({ title: '测试中...' })
      
      try {
        // 模拟API测试
        await new Promise(resolve => setTimeout(resolve, 2000))
        
        uni.hideLoading()
        uni.showToast({
          title: 'AI接口测试成功',
          icon: 'success'
        })
      } catch (error) {
        uni.hideLoading()
        uni.showToast({
          title: 'AI接口测试失败',
          icon: 'none'
        })
      }
    },
    
    publishAnnouncement() {
      if (!this.newAnnouncement.title || !this.newAnnouncement.content) {
        uni.showToast({
          title: '请填写完整公告信息',
          icon: 'none'
        })
        return
      }
      
      const announcement = {
        id: Date.now(),
        ...this.newAnnouncement,
        time: new Date().toLocaleString()
      }
      
      this.announcements.unshift(announcement)
      
      // 清空表单
      this.newAnnouncement = {
        title: '',
        content: '',
        type: '系统公告'
      }
      
      uni.showToast({
        title: '公告发布成功',
        icon: 'success'
      })
    },
    
    editAnnouncement(announcement) {
      uni.showModal({
        title: '编辑公告',
        content: announcement.content,
        editable: true,
        success: (res) => {
          if (res.confirm) {
            announcement.content = res.content
            uni.showToast({
              title: '公告修改成功',
              icon: 'success'
            })
          }
        }
      })
    },
    
    deleteAnnouncement(announcement) {
      uni.showModal({
        title: '确认删除',
        content: `确定要删除公告"${announcement.title}"吗？`,
        success: (res) => {
          if (res.confirm) {
            const index = this.announcements.findIndex(a => a.id === announcement.id)
            this.announcements.splice(index, 1)
            uni.showToast({
              title: '公告删除成功',
              icon: 'success'
            })
          }
        }
      })
    },
    
    async saveSettings() {
      uni.showLoading({ title: '保存中...' })
      
      try {
        // 模拟API请求
        await new Promise(resolve => setTimeout(resolve, 1500))
        
        uni.hideLoading()
        uni.showToast({
          title: '设置保存成功',
          icon: 'success'
        })
      } catch (error) {
        uni.hideLoading()
        uni.showToast({
          title: '保存失败',
          icon: 'none'
        })
      }
    }
  }
}
</script>

<style scoped>
.system-settings {
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

.settings-container {
  max-width: 900rpx;
}

.settings-section {
  background: white;
  padding: 30rpx;
  border-radius: 12rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 25rpx;
  padding-bottom: 15rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.setting-item {
  display: flex;
  align-items: center;
  margin-bottom: 25rpx;
  gap: 20rpx;
}

.setting-item:last-child {
  margin-bottom: 0;
}

.setting-label {
  font-size: 26rpx;
  color: #333;
  min-width: 150rpx;
}

.setting-input {
  flex: 1;
  height: 60rpx;
  padding: 0 20rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 26rpx;
  background: #fafafa;
}

.setting-input:focus {
  border-color: #2196f3;
  background: white;
}

.setting-textarea {
  flex: 1;
  min-height: 100rpx;
  padding: 15rpx 20rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 26rpx;
  background: #fafafa;
  resize: vertical;
}

.setting-textarea:focus {
  border-color: #2196f3;
  background: white;
}

.setting-slider {
  flex: 1;
}

.template-item {
  margin-bottom: 30rpx;
  padding: 20rpx;
  background: #f8f9fa;
  border-radius: 8rpx;
}

.template-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 15rpx;
  display: block;
}

.template-content {
  width: 100%;
  min-height: 80rpx;
  padding: 15rpx 20rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 26rpx;
  background: white;
  margin-bottom: 15rpx;
  resize: vertical;
}

.preview-btn,
.test-btn {
  background: #2196f3;
  color: white;
  border: none;
  padding: 12rpx 24rpx;
  border-radius: 6rpx;
  font-size: 24rpx;
}

.test-btn {
  background: #4caf50;
}

/* 公告管理 */
.announcement-form {
  margin-bottom: 40rpx;
  padding: 25rpx;
  background: #f8f9fa;
  border-radius: 8rpx;
}

.form-item {
  margin-bottom: 20rpx;
}

.form-label {
  font-size: 26rpx;
  color: #333;
  margin-bottom: 10rpx;
  display: block;
}

.form-input {
  width: 100%;
  height: 60rpx;
  padding: 0 20rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 26rpx;
  background: white;
}

.form-textarea {
  width: 100%;
  min-height: 120rpx;
  padding: 15rpx 20rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 26rpx;
  background: white;
  resize: vertical;
}

.form-picker {
  width: 100%;
  height: 60rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  background: white;
}

.picker-content {
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 20rpx;
  font-size: 26rpx;
  color: #333;
}

.publish-btn {
  background: #4caf50;
  color: white;
  border: none;
  padding: 15rpx 30rpx;
  border-radius: 8rpx;
  font-size: 28rpx;
  width: 100%;
}

.list-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.announcement-item {
  display: flex;
  gap: 20rpx;
  padding: 20rpx;
  background: #fafafa;
  border-radius: 8rpx;
  align-items: flex-start;
}

.announcement-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.announcement-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #333;
}

.announcement-desc {
  font-size: 24rpx;
  color: #666;
  line-height: 1.4;
}

.announcement-time {
  font-size: 22rpx;
  color: #999;
}

.announcement-actions {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.action-btn {
  padding: 8rpx 16rpx;
  border: none;
  border-radius: 6rpx;
  font-size: 22rpx;
  text-align: center;
  min-width: 80rpx;
}

.edit-btn {
  background: #2196f3;
  color: white;
}

.delete-btn {
  background: #f44336;
  color: white;
}

/* 保存区域 */
.save-section {
  text-align: center;
  margin-top: 40rpx;
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
  
  .setting-item {
    flex-direction: column;
    align-items: stretch;
    gap: 10rpx;
  }
  
  .setting-label {
    min-width: auto;
  }
  
  .announcement-item {
    flex-direction: column;
  }
  
  .announcement-actions {
    flex-direction: row;
    justify-content: flex-end;
  }
}
</style>