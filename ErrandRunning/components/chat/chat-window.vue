<template>
  <view class="chat-window">
    <!-- 聊天头部 -->
    <view class="chat-header">
      <view class="header-left" @click="goBack">
        <text class="iconfont icon-back"></text>
      </view>
      <view class="header-center">
        <text class="chat-title">{{ chatTitle }}</text>
        <text class="chat-status" v-if="isOnline">在线</text>
      </view>
      <view class="header-right" @click="showOptions">
        <text class="iconfont icon-more"></text>
      </view>
    </view>
    
    <!-- 消息列表 -->
    <scroll-view
      class="message-list"
      scroll-y
      :scroll-top="scrollTop"
      :scroll-into-view="scrollIntoView"
      @scrolltoupper="loadMoreMessages"
    >
      <view class="message-container">
        <!-- 时间戳 -->
        <view class="time-stamp" v-for="(time, index) in timestamps" :key="'time-' + index">
          <text>{{ time }}</text>
        </view>
        
        <!-- 消息项 -->
        <view
          class="message-item"
          :class="{ 'is-self': message.isSelf }"
          v-for="(message, index) in messages"
          :key="'msg-' + index"
          :id="'msg-' + index"
        >
          <image
            class="avatar"
            :src="message.isSelf ? myAvatar : otherAvatar"
            mode="aspectFill"
          />
          <view class="message-content">
            <text class="sender-name" v-if="!message.isSelf">{{ message.senderName }}</text>
            <view class="message-bubble" :class="{ 'is-self': message.isSelf }">
              <text class="message-text">{{ message.text }}</text>
            </view>
            <text class="message-time">{{ formatTime(message.time) }}</text>
          </view>
        </view>
      </view>
    </scroll-view>
    
    <!-- 输入区域 -->
    <view class="input-area">
      <view class="input-left">
        <view class="action-btn" @click="chooseImage">
          <text class="iconfont icon-image"></text>
        </view>
        <view class="action-btn" @click="chooseLocation">
          <text class="iconfont icon-location"></text>
        </view>
      </view>
      <view class="input-center">
        <textarea
          class="message-input"
          v-model="inputMessage"
          placeholder="输入消息..."
          :auto-height="true"
          :maxlength="500"
          @focus="handleFocus"
          @blur="handleBlur"
        />
      </view>
      <view class="input-right">
        <button
          class="send-btn"
          :class="{ 'disabled': !inputMessage.trim() }"
          @click="sendMessage"
          :disabled="!inputMessage.trim()"
        >
          发送
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import { getMessages, sendMessage as sendApiMessage, getConversations } from '@/api/errand'

export default {
  name: 'ChatWindow',
  
  props: {
    // 会话ID
    conversationId: {
      type: [String, Number],
      required: true
    },
    // 聊天标题
    chatTitle: {
      type: String,
      default: '聊天'
    },
    // 对方信息
    otherUserInfo: {
      type: Object,
      default: () => ({})
    }
  },
  
  data() {
    return {
      messages: [],
      timestamps: [],
      scrollTop: 0,
      scrollIntoView: '',
      inputMessage: '',
      myAvatar: '/static/default-avatar.png',
      otherAvatar: '/static/default-avatar.png',
      isOnline: false,
      page: 1,
      pageSize: 20,
      hasMore: true,
      timer: null
    }
  },

  mounted() {
    this.initChat()
    this.startPolling()
  },
  
  beforeDestroy() {
    this.stopPolling()
  },
  
  methods: {
    // 初始化聊天
    async initChat() {
      // 设置头像
      if (this.myUserInfo.avatar) {
        this.myAvatar = this.myUserInfo.avatar
      }
      if (this.otherUserInfo.avatar) {
        this.otherAvatar = this.otherUserInfo.avatar
      }
      
      // 加载消息
      await this.loadMessages()
      
      // 滚动到底部
      this.scrollToBottom()
    },
    
    // 加载消息
    async loadMessages(loadMore = false) {
      if (!this.hasMore && loadMore) return
      
      try {
        const params = {
          page: this.page,
          pageSize: this.pageSize
        }
        
        const response = await getMessages(this.conversationId, params)
        
        if (response.code === 200) {
          const newMessages = response.data.list || []
          
          // 处理消息，标记是否为自己发送
          const processedMessages = newMessages.map(msg => ({
            ...msg,
            isSelf: msg.senderId === this.myUserInfo.id
          }))
          
          if (loadMore) {
            this.messages = [...processedMessages, ...this.messages]
          } else {
            this.messages = processedMessages
          }
          
          // 处理时间戳
          this.processTimestamps()
          
          // 检查是否有更多
          this.hasMore = newMessages.length >= this.pageSize
          
          if (loadMore) {
            this.page++
          }
        }
      } catch (error) {
        console.error('加载消息失败:', error)
      }
    },
    
    // 加载更多消息
    loadMoreMessages() {
      if (this.hasMore) {
        this.loadMessages(true)
      }
    },
    
    // 处理时间戳
    processTimestamps() {
      this.timestamps = []
      let lastTime = null
      
      this.messages.forEach((msg, index) => {
        const msgTime = new Date(msg.time).getTime()
        const timeDiff = lastTime ? msgTime - lastTime : Infinity
        
        // 如果距离上一条消息超过5分钟，显示时间戳
        if (timeDiff > 5 * 60 * 1000) {
          this.timestamps.push({
            index,
            time: this.formatFullTime(msgTime)
          })
          lastTime = msgTime
        }
      })
    },
    
    // 发送消息
    async sendMessage() {
      const text = this.inputMessage.trim()
      if (!text) return
      
      try {
        const messageData = {
          text,
          type: 'text',
          conversationId: this.conversationId
        }
        
        const response = await sendApiMessage(this.conversationId, messageData)
        
        if (response.code === 200) {
          // 添加到消息列表
          const newMessage = {
            ...response.data,
            isSelf: true,
            senderId: this.myUserInfo.id,
            senderName: this.myUserInfo.realName || '我',
            time: new Date().toISOString()
          }
          
          this.messages.push(newMessage)
          this.processTimestamps()
          
          // 清空输入框
          this.inputMessage = ''
          
          // 滚动到底部
          this.scrollToBottom()
          
          uni.showToast({
            title: '发送成功',
            icon: 'success',
            duration: 1000
          })
        }
      } catch (error) {
        console.error('发送消息失败:', error)
        uni.showToast({
          title: '发送失败',
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
          const tempFilePaths = res.tempFilePaths
          this.uploadImage(tempFilePaths[0])
        }
      })
    },
    
    // 上传图片
    async uploadImage(filePath) {
      uni.showLoading({
        title: '上传中...'
      })
      
      try {
        const [uploadRes] = await uni.uploadFile({
          url: 'http://localhost:18080/api/upload',
          filePath: filePath,
          name: 'file',
          header: {
              'Authorization': 'Bearer mock-token'
          }
        })
        
        const data = JSON.parse(uploadRes.data)
        
        if (data.code === 200) {
          // 发送图片消息
          const messageData = {
            type: 'image',
            imageUrl: data.data.url,
            conversationId: this.conversationId
          }
          
          const response = await sendApiMessage(this.conversationId, messageData)
          
          if (response.code === 200) {
            const newMessage = {
              ...response.data,
              isSelf: true,
              type: 'image',
              text: '[图片]',
              time: new Date().toISOString()
            }
            
            this.messages.push(newMessage)
            this.processTimestamps()
            this.scrollToBottom()
          }
        }
      } catch (error) {
        console.error('上传图片失败:', error)
        uni.showToast({
          title: '上传失败',
          icon: 'none'
        })
      } finally {
        uni.hideLoading()
      }
    },
    
    // 选择位置
    chooseLocation() {
      uni.chooseLocation({
        success: (res) => {
          // 发送位置消息
          this.sendLocationMessage(res)
        },
        fail: () => {
          uni.showToast({
            title: '选择位置失败',
            icon: 'none'
          })
        }
      })
    },
    
    // 发送位置消息
    async sendLocationMessage(location) {
      try {
        const messageData = {
          type: 'location',
          location: {
            latitude: location.latitude,
            longitude: location.longitude,
            name: location.name,
            address: location.address
          },
          conversationId: this.conversationId
        }
        
        const response = await sendApiMessage(this.conversationId, messageData)
        
        if (response.code === 200) {
          const newMessage = {
            ...response.data,
            isSelf: true,
            type: 'location',
            text: `[位置] ${location.name}`,
            time: new Date().toISOString()
          }
          
          this.messages.push(newMessage)
          this.processTimestamps()
          this.scrollToBottom()
        }
      } catch (error) {
        console.error('发送位置失败:', error)
      }
    },
    
    // 滚动到底部
    scrollToBottom() {
      this.$nextTick(() => {
        if (this.messages.length > 0) {
          this.scrollIntoView = 'msg-' + (this.messages.length - 1)
        }
      })
    },
    
    // 开始轮询
    startPolling() {
      this.timer = setInterval(() => {
        this.loadMessages()
      }, 3000) // 每3秒轮询一次
    },
    
    // 停止轮询
    stopPolling() {
      if (this.timer) {
        clearInterval(this.timer)
        this.timer = null
      }
    },
    
    // 格式化时间
    formatTime(time) {
      const date = new Date(time)
      const hours = date.getHours().toString().padStart(2, '0')
      const minutes = date.getMinutes().toString().padStart(2, '0')
      return `${hours}:${minutes}`
    },
    
    // 格式化完整时间
    formatFullTime(timestamp) {
      const date = new Date(timestamp)
      const now = new Date()
      const diff = now - date
      
      // 今天
      if (date.getDate() === now.getDate() &&
          date.getMonth() === now.getMonth() &&
          date.getFullYear() === now.getFullYear()) {
        return this.formatTime(date)
      }
      
      // 昨天
      const yesterday = new Date(now)
      yesterday.setDate(yesterday.getDate() - 1)
      if (date.getDate() === yesterday.getDate()) {
        return '昨天 ' + this.formatTime(date)
      }
      
      // 其他
      const month = (date.getMonth() + 1).toString().padStart(2, '0')
      const day = date.getDate().toString().padStart(2, '0')
      return `${month}-${day} ${this.formatTime(date)}`
    },
    
    // 处理聚焦
    handleFocus() {
      this.$nextTick(() => {
        this.scrollToBottom()
      })
    },
    
    // 处理失焦
    handleBlur() {},
    
    // 返回
    goBack() {
      uni.navigateBack()
    },
    
    // 显示选项
    showOptions() {
      uni.showActionSheet({
        itemList: ['清空聊天记录', '举报用户'],
        success: (res) => {
          if (res.tapIndex === 0) {
            this.clearChatHistory()
          } else if (res.tapIndex === 1) {
            this.reportUser()
          }
        }
      })
    },
    
    // 清空聊天记录
    clearChatHistory() {
      uni.showModal({
        title: '确认清空',
        content: '确定要清空所有聊天记录吗？',
        success: (res) => {
          if (res.confirm) {
            this.messages = []
            this.timestamps = []
            uni.showToast({
              title: '已清空',
              icon: 'success'
            })
          }
        }
      })
    },
    
    // 举报用户
    reportUser() {
      uni.showToast({
        title: '举报功能开发中',
        icon: 'none'
      })
    }
  }
}
</script>

<style scoped lang="scss">
.chat-window {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 30rpx;
  background: white;
  border-bottom: 1rpx solid #f0f0f0;
  
  .header-left,
  .header-right {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 36rpx;
    color: #666;
  }
  
  .header-center {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .chat-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }
    
    .chat-status {
      font-size: 22rpx;
      color: #4caf50;
      margin-top: 4rpx;
    }
  }
}

.message-list {
  flex: 1;
  padding: 20rpx;
}

.message-container {
  min-height: 100%;
}

.time-stamp {
  display: flex;
  justify-content: center;
  margin: 30rpx 0;
  
  text {
    padding: 10rpx 20rpx;
    background: rgba(0, 0, 0, 0.1);
    border-radius: 20rpx;
    font-size: 22rpx;
    color: #999;
  }
}

.message-item {
  display: flex;
  margin-bottom: 30rpx;
  
  &.is-self {
    flex-direction: row-reverse;
  }
  
  .avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    background: #e0e0e0;
  }
  
  .message-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    padding: 0 20rpx;
    max-width: 70%;
    
    .sender-name {
      font-size: 24rpx;
      color: #999;
      margin-bottom: 8rpx;
    }
    
    .message-bubble {
      padding: 20rpx;
      border-radius: 12rpx;
      max-width: 100%;
      word-wrap: break-word;
      
      &.is-self {
        background: #2196f3;
        
        .message-text {
          color: white;
        }
      }
      
      &:not(.is-self) {
        background: white;
        
        .message-text {
          color: #333;
        }
      }
      
      .message-text {
        font-size: 28rpx;
        line-height: 1.5;
      }
    }
    
    .message-time {
      font-size: 22rpx;
      color: #999;
      margin-top: 8rpx;
    }
  }
  
  &.is-self {
    .message-content {
      align-items: flex-end;
      
      .message-bubble {
        background: #2196f3;
      }
    }
  }
}

.input-area {
  display: flex;
  align-items: flex-end;
  padding: 20rpx;
  background: white;
  border-top: 1rpx solid #f0f0f0;
  gap: 20rpx;
  
  .input-left {
    display: flex;
    gap: 10rpx;
  }
  
  .action-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 36rpx;
    color: #666;
  }
  
  .input-center {
    flex: 1;
  }
  
  .message-input {
    width: 100%;
    min-height: 60rpx;
    max-height: 200rpx;
    padding: 16rpx 20rpx;
    background: #f5f5f5;
    border-radius: 30rpx;
    font-size: 28rpx;
  }
  
  .input-right {
    .send-btn {
      width: 120rpx;
      height: 60rpx;
      background: #2196f3;
      color: white;
      border: none;
      border-radius: 30rpx;
      font-size: 28rpx;
      padding: 0;
      line-height: 60rpx;
      
      &.disabled {
        background: #ccc;
      }
    }
  }
}
</style>
