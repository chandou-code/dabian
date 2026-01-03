<template>
  <view class="system-settings">
    <Sidebar />
    
    <view class="main-content" :class="{ 'main-content-expanded': !showSidebar }">
      <view class="settings-container">
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
      </view>
    </view>
  </view>
</template>

<script>
import Sidebar from '@/components/Sidebar.vue'
import { getAnnouncements, publishAnnouncement, updateAnnouncement, deleteAnnouncement } from '@/api/announcement'

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
      
      newAnnouncement: {
        title: '',
        content: '',
        type: '系统公告'
      },
      
      announcements: []
    }
  },
  
  onLoad() {
    this.loadAnnouncements()
  },
  
  methods: {
    // 加载公告列表
    async loadAnnouncements() {
      try {
        uni.showLoading({ title: '加载中...' })
        const response = await getAnnouncements()
        if (response.success) {
          this.announcements = response.data || []
        } else {
          uni.showToast({ title: '加载失败', icon: 'none' })
        }
      } catch (error) {
        console.error('加载公告失败:', error)
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },
    
    // 公告类型变更
    onAnnouncementTypeChange(e) {
      this.announcementTypeIndex = e.detail.value
      this.newAnnouncement.type = this.announcementTypes[this.announcementTypeIndex]
    },
    
    // 发布公告
    async publishAnnouncement() {
      if (!this.newAnnouncement.title || !this.newAnnouncement.content) {
        uni.showToast({
          title: '请填写完整公告信息',
          icon: 'none'
        })
        return
      }
      
      try {
        uni.showLoading({ title: '发布中...' })
        const response = await publishAnnouncement(this.newAnnouncement)
        if (response.success) {
          // 重新加载公告列表
          await this.loadAnnouncements()
          
          // 清空表单
          this.newAnnouncement = {
            title: '',
            content: '',
            type: '系统公告'
          }
          this.announcementTypeIndex = 0
          
          uni.showToast({
            title: '公告发布成功',
            icon: 'success'
          })
        } else {
          uni.showToast({ title: response.message || '发布失败', icon: 'none' })
        }
      } catch (error) {
        console.error('发布公告失败:', error)
        uni.showToast({ title: '发布失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },
    
    // 编辑公告
    async editAnnouncement(announcement) {
      try {
        const res = await uni.showModal({
          title: '编辑公告',
          content: announcement.content,
          editable: true
        })
        
        if (res.confirm) {
          uni.showLoading({ title: '修改中...' })
          const response = await updateAnnouncement(announcement.id, {
            ...announcement,
            content: res.content
          })
          
          if (response.success) {
            // 重新加载公告列表
            await this.loadAnnouncements()
            uni.showToast({ title: '公告修改成功', icon: 'success' })
          } else {
            uni.showToast({ title: response.message || '修改失败', icon: 'none' })
          }
        }
      } catch (error) {
        console.error('编辑公告失败:', error)
        uni.showToast({ title: '修改失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },
    
    // 删除公告
    async deleteAnnouncement(announcement) {
      try {
        const res = await uni.showModal({
          title: '确认删除',
          content: `确定要删除公告"${announcement.title}"吗？`
        })
        
        if (res.confirm) {
          uni.showLoading({ title: '删除中...' })
          const response = await deleteAnnouncement(announcement.id)
          
          if (response.success) {
            // 重新加载公告列表
            await this.loadAnnouncements()
            uni.showToast({ title: '公告删除成功', icon: 'success' })
          } else {
            uni.showToast({ title: response.message || '删除失败', icon: 'none' })
          }
        }
      } catch (error) {
        console.error('删除公告失败:', error)
        uni.showToast({ title: '删除失败', icon: 'none' })
      } finally {
        uni.hideLoading()
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