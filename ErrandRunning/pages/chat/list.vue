<template>
  <view class="chat-list-container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-input">
        <text class="search-icon">🔍</text>
        <input
          type="text"
          placeholder="搜索联系人"
          v-model="searchKeyword"
          @input="onSearch"
        />
      </view>
    </view>

    <!-- 聊天列表 -->
    <scroll-view scroll-y class="chat-list">
      <view class="empty-state" v-if="filteredChats.length === 0">
        <image class="empty-image" src="/static/empty-chat.png" mode="aspectFit"></image>
        <text class="empty-text">{{ searchKeyword ? '未找到相关聊天' : '暂无聊天记录' }}</text>
      </view>

      <view
        v-for="chat in filteredChats"
        :key="chat.id"
        class="chat-item"
        @click="openChat(chat.id, chat.userId, chat.name)"
      >
        <view class="avatar-wrapper">
          <image class="avatar" :src="chat.avatar" mode="aspectFill"></image>
          <view class="unread-badge" v-if="chat.unread > 0">
            {{ chat.unread > 99 ? '99+' : chat.unread }}
          </view>
        </view>
        <view class="chat-info">
          <view class="chat-header">
            <text class="username">{{ chat.name }}</text>
            <text class="time">{{ chat.time }}</text>
          </view>
          <view class="chat-preview">
            <text class="message">{{ chat.lastMessage }}</text>
            <view class="task-tag" v-if="chat.taskId">任务</view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { request } from '../../api/request'

export default {
  data() {
    return {
      searchKeyword: '',
      chats: [],
      loading: false,
      page: 1,
      pageSize: 10,
      hasMore: true
    }
  },
  computed: {
    filteredChats() {
      if (!this.searchKeyword) {
        return this.chats
      }
      return this.chats.filter(chat =>
        chat.name.includes(this.searchKeyword) ||
        chat.lastMessage.includes(this.searchKeyword)
      )
    }
  },
  onLoad() {
    this.loadChatList()
  },
  onShow() {
    // 每次显示时刷新列表
    this.loadChatList()
  },
  methods: {
    async loadChatList() {
      if (this.loading || !this.hasMore) return
      
      this.loading = true
      try {
        const token = uni.getStorageSync('token')
        if (!token) {
          uni.showToast({ title: '请先登录', icon: 'none' })
          return
        }

        const res = await request({
          url: '/chat/list',
          method: 'GET',
          header: {
            'token': token
          },
          data: {
            page: this.page,
            pageSize: this.pageSize
          }
        })

        if (res.code === 200) {
          const chatList = res.data.list || []
          
          // 处理聊天列表数据，添加用户信息
          const processedChats = await Promise.all(chatList.map(async (chat) => {
            // 获取对方用户信息
            const user = uni.getStorageSync('user')
            const currentUserId = user ? user.id : null
            const otherUserId = chat.userId1 === currentUserId ? chat.userId2 : chat.userId1
            
            // 跳过与自己的聊天记录
            if (otherUserId === currentUserId) {
              return null
            }
            
            const userInfo = await this.getUserInfo(otherUserId)
            
            return {
              id: chat.id,
              userId: otherUserId,
              name: userInfo ? userInfo.username : '用户',
              avatar: userInfo ? userInfo.avatar : '/static/avatar1.png',
              lastMessage: chat.lastMessage || '',
              time: this.formatTime(chat.lastMessageTime),
              unread: chat.userId1 === currentUserId ? chat.unreadCount1 : chat.unreadCount2,
              taskId: chat.taskId || ''
            }
          }))
          
          // 过滤掉无效的聊天记录
          const validChats = processedChats.filter(chat => chat !== null)

          if (this.page === 1) {
            this.chats = validChats
          } else {
            this.chats = [...this.chats, ...validChats]
          }

          this.hasMore = processedChats.length >= this.pageSize
          this.page++
        } else {
          uni.showToast({ title: res.msg || '获取聊天列表失败', icon: 'none' })
        }
      } catch (error) {
        console.error('获取聊天列表错误:', error)
        uni.showToast({ title: '网络错误，请稍后重试', icon: 'none' })
      } finally {
        this.loading = false
      }
    },

    async getUserInfo(userId) {
      try {
        const res = await request({
          url: `/auth/user/basic-info/${userId}`,
          method: 'GET'
        })

        return res.code === 200 ? res.data : null
      } catch (error) {
        console.error('获取用户信息错误:', error)
        return null
      }
    },

    formatTime(timeStr) {
      if (!timeStr) return ''
      
      const now = new Date()
      const messageTime = new Date(timeStr)
      const diff = now - messageTime
      
      if (diff < 60 * 1000) {
        return '刚刚'
      } else if (diff < 60 * 60 * 1000) {
        return Math.floor(diff / (60 * 1000)) + '分钟前'
      } else if (diff < 24 * 60 * 60 * 1000) {
        return Math.floor(diff / (60 * 60 * 1000)) + '小时前'
      } else if (diff < 7 * 24 * 60 * 60 * 1000) {
        return Math.floor(diff / (24 * 60 * 60 * 1000)) + '天前'
      } else {
        return messageTime.getMonth() + 1 + '-' + messageTime.getDate()
      }
    },

    onSearch() {
      // 搜索过滤已通过计算属性实现
    },

    openChat(chatId, userId, name) {
      uni.navigateTo({
        url: `/pages/chat/detail?chatId=${chatId}&userId=${userId}&name=${name}`
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.chat-list-container {
  height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.search-bar {
  background: white;
  padding: 20rpx 30rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

  .search-input {
    display: flex;
    align-items: center;
    height: 72rpx;
    background: #f5f5f5;
    border-radius: 36rpx;
    padding: 0 24rpx;
    gap: 16rpx;

    .search-icon {
      font-size: 32rpx;
    }

    input {
      flex: 1;
      height: 100%;
      font-size: 28rpx;
      color: #333;
    }
  }
}

.chat-list {
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

.chat-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background: white;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  transition: all 0.3s;

  &:active {
    background: #fafafa;
  }

  .avatar-wrapper {
    position: relative;
    margin-right: 20rpx;

    .avatar {
      width: 96rpx;
      height: 96rpx;
      border-radius: 48rpx;
    }

    .unread-badge {
      position: absolute;
      top: -8rpx;
      right: -8rpx;
      min-width: 40rpx;
      height: 40rpx;
      padding: 0 8rpx;
      background: #f44336;
      color: white;
      border-radius: 20rpx;
      font-size: 20rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 3rpx solid white;
    }
  }

  .chat-info {
    flex: 1;
    overflow: hidden;

    .chat-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12rpx;

      .username {
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
      }

      .time {
        font-size: 24rpx;
        color: #999;
      }
    }

    .chat-preview {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16rpx;

      .message {
        flex: 1;
        font-size: 26rpx;
        color: #666;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .task-tag {
        padding: 4rpx 12rpx;
        background: #e3f2fd;
        color: #2196f3;
        border-radius: 12rpx;
        font-size: 20rpx;
        flex-shrink: 0;
      }
    }
  }
}
</style>
