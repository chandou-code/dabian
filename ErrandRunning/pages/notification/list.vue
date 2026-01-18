<template>
  <view class="notification-container">
    <!-- 顶部标签 -->
    <view class="tabs">
      <text
        class="tab-item"
        :class="{ active: currentTab === 'all' }"
        @click="switchTab('all')"
      >
        全部
      </text>
      <text
        class="tab-item"
        :class="{ active: currentTab === 'task' }"
        @click="switchTab('task')"
      >
        任务
      </text>
      <text
        class="tab-item"
        :class="{ active: currentTab === 'system' }"
        @click="switchTab('system')"
      >
        系统
      </text>
    </view>

    <!-- 通知列表 -->
    <scroll-view scroll-y class="notification-list" @scrolltolower="loadMore">
      <view class="empty-state" v-if="filteredNotifications.length === 0">
        <image class="empty-image" src="/static/empty-notification.png" mode="aspectFit"></image>
        <text class="empty-text">暂无通知</text>
      </view>

      <view
        v-for="(notification, index) in filteredNotifications"
        :key="index"
        class="notification-item"
        :class="{ unread: !notification.read }"
        @click="viewNotification(notification)"
      >
        <view class="notification-icon">
          <text>{{ getNotificationIcon(notification.type) }}</text>
        </view>
        <view class="notification-content">
          <view class="notification-header">
            <text class="notification-title">{{ notification.title }}</text>
            <text class="notification-time">{{ notification.time }}</text>
          </view>
          <text class="notification-text">{{ notification.content }}</text>
          <view class="notification-tag" v-if="notification.tagName">
            {{ notification.tagName }}
          </view>
        </view>
        <view class="unread-dot" v-if="!notification.read"></view>
      </view>

      <view class="loading-more" v-if="loading">
        <text class="loading-text">加载中...</text>
      </view>
      <view class="loading-more" v-if="!hasMore && filteredNotifications.length > 0">
        <text class="loading-text">没有更多了</text>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="footer-actions" v-if="filteredNotifications.length > 0">
      <button class="btn btn-read" @click="markAllRead">全部标为已读</button>
      <button class="btn btn-clear" @click="clearAll">清空通知</button>
    </view>
  </view>
</template>

<script>
import { getNotifications, markAsRead, markAllAsRead, clearAllNotifications } from '../../api/errand.js'

export default {
  data() {
    return {
      currentTab: 'all',
      notifications: [],
      page: 1,
      pageSize: 10,
      loading: false,
      hasMore: true
    }
  },
  computed: {
    filteredNotifications() {
      if (this.currentTab === 'all') {
        return this.notifications
      }
      return this.notifications.filter(n => n.type === this.currentTab)
    }
  },
  onLoad() {
    this.loadNotifications()
  },
  onShow() {
    // 每次显示时刷新
    this.refreshNotifications()
  },
  methods: {
    switchTab(tab) {
      this.currentTab = tab
    },

    // 刷新通知列表
    refreshNotifications() {
      this.page = 1
      this.notifications = []
      this.hasMore = true
      this.loadNotifications()
    },

    // 加载通知列表
    loadNotifications() {
      if (this.loading || !this.hasMore) {
        return
      }
      
      this.loading = true
      
      getNotifications({
        type: this.currentTab === 'all' ? '' : this.currentTab,
        page: this.page,
        pageSize: this.pageSize
      }).then(res => {
        if (res.code === 200) {
          if (this.page === 1) {
            this.notifications = res.data
          } else {
            this.notifications = [...this.notifications, ...res.data]
          }
          
          this.hasMore = res.hasMore
          this.page++
        } else {
          uni.showToast({
            title: res.msg || '获取通知失败',
            icon: 'none'
          })
        }
      }).catch(err => {
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        })
        console.error('获取通知失败', err)
      }).finally(() => {
        this.loading = false
      })
    },

    // 加载更多通知
    loadMore() {
      this.loadNotifications()
    },

    // 查看通知
    viewNotification(notification) {
      // 标记为已读
      if (!notification.read) {
        markAsRead(notification.id)
        notification.read = true
      }

      // 跳转到相应页面
      if (notification.type === 'task' && notification.taskId) {
        uni.navigateTo({
          url: `/pages/task/detail?id=${notification.taskId}`
        })
      } else if (notification.type === 'system' && notification.noticeId) {
        uni.navigateTo({
          url: `/pages/notice/detail?id=${notification.noticeId}`
        })
      }
    },

    // 标记所有通知为已读
    markAllRead() {
      uni.showModal({
        title: '确认操作',
        content: '确定要将所有通知标记为已读吗？',
        success: (res) => {
          if (res.confirm) {
            markAllAsRead().then(res => {
              if (res.code === 200) {
                // 更新本地数据
                this.notifications.forEach(n => {
                  n.read = true
                })
                uni.showToast({
                  title: '已全部标为已读',
                  icon: 'success'
                })
              } else {
                uni.showToast({
                  title: res.msg || '操作失败',
                  icon: 'none'
                })
              }
            }).catch(err => {
              uni.showToast({
                title: '网络错误',
                icon: 'none'
              })
              console.error('标记所有已读失败', err)
            })
          }
        }
      })
    },

    // 清空所有通知
    clearAll() {
      uni.showModal({
        title: '确认清空',
        content: '确定要清空所有通知吗？',
        confirmColor: '#f44336',
        success: (res) => {
          if (res.confirm) {
            clearAllNotifications().then(res => {
              if (res.code === 200) {
                // 清空本地数据
                this.notifications = []
                uni.showToast({
                  title: '已清空通知',
                  icon: 'success'
                })
              } else {
                uni.showToast({
                  title: res.msg || '操作失败',
                  icon: 'none'
                })
              }
            }).catch(err => {
              uni.showToast({
                title: '网络错误',
                icon: 'none'
              })
              console.error('清空所有通知失败', err)
            })
          }
        }
      })
    },

    // 获取通知图标
    getNotificationIcon(type) {
      const icons = {
        task: '📋',
        system: '📢',
        chat: '💬',
        order: '📦'
      }
      return icons[type] || '📄'
    }
  }
}
</script>

<style lang="scss" scoped>
.notification-container {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  padding-bottom: 100rpx;
}

.tabs {
  display: flex;
  background: white;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #eee;

  .tab-item {
    flex: 1;
    text-align: center;
    padding: 16rpx 0;
    font-size: 28rpx;
    color: #666;
    position: relative;

    &.active {
      color: #2196f3;
      font-weight: bold;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 60rpx;
        height: 4rpx;
        background: #2196f3;
        border-radius: 2rpx;
      }
    }
  }
}

.notification-list {
  flex: 1;
  padding: 20rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;

  .empty-image {
    width: 400rpx;
    height: 400rpx;
    margin-bottom: 40rpx;
  }

  .empty-text {
    font-size: 28rpx;
    color: #999;
  }
}

.notification-item {
  display: flex;
  background: white;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  position: relative;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

  &.unread {
    background: linear-gradient(135deg, #e3f2fd, #f5f9ff);
  }

  .notification-icon {
    width: 80rpx;
    height: 80rpx;
    background: #f5f5f5;
    border-radius: 40rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 40rpx;
    margin-right: 20rpx;
    flex-shrink: 0;
  }

  .notification-content {
    flex: 1;
    overflow: hidden;

    .notification-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12rpx;

      .notification-title {
        font-size: 28rpx;
        font-weight: bold;
        color: #333;
      }

      .notification-time {
        font-size: 22rpx;
        color: #999;
      }
    }

    .notification-text {
      display: block;
      font-size: 26rpx;
      color: #666;
      line-height: 1.6;
      margin-bottom: 12rpx;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .notification-tag {
      display: inline-block;
      padding: 4rpx 12rpx;
      background: #e3f2fd;
      color: #2196f3;
      border-radius: 12rpx;
      font-size: 22rpx;
    }
  }

  .unread-dot {
    position: absolute;
    top: 20rpx;
    right: 20rpx;
    width: 16rpx;
    height: 16rpx;
    background: #f44336;
    border-radius: 50%;
  }
}

.loading-more {
  text-align: center;
  padding: 30rpx 0;

  .loading-text {
    font-size: 24rpx;
    color: #999;
  }
}

.footer-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  background: white;
  display: flex;
  gap: 20rpx;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.08);

  .btn {
    flex: 1;
    height: 80rpx;
    border-radius: 40rpx;
    font-size: 28rpx;
    border: none;

    &.btn-read {
      background: #e3f2fd;
      color: #2196f3;
    }

    &.btn-clear {
      background: #ffebee;
      color: #f44336;
    }
  }
}
</style>
