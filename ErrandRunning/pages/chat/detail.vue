<template>
  <view class="chat-detail-container">
    <!-- 顶部用户信息 -->
    <view class="chat-header">
      <view class="header-left">
        <text class="back-icon" @click="goBack">←</text>
      </view>
      <view class="user-info" @click="viewUserProfile">
        <image class="avatar" :src="userInfo.avatar" mode="aspectFill"></image>
        <view class="user-detail">
          <text class="username">{{ userInfo.name }}</text>
          <text class="user-status" v-if="userInfo.taskId">
            关联任务: {{ userInfo.taskId }}
          </text>
        </view>
      </view>
      <view class="header-actions">
        <text class="action-icon" @click="makePhoneCall">📞</text>
        <text class="action-icon" @click="showMore">⋯</text>
      </view>
    </view>

    <!-- 聊天消息 -->
    <view class="message-wrapper">
      <scroll-view
        scroll-y
        class="message-container"
        :scroll-into-view="scrollIntoView"
        :scroll-with-animation="true"
        @scrolltolower="loadMoreMessages"
      >
        <view class="time-divider">
          <text class="time-text">{{ chatTime }}</text>
        </view>

        <view
          v-for="(message, index) in messages"
          :key="index"
          :id="'msg-' + index"
          class="message-item"
          :class="{ self: message.isSelf }"
        >
          <image
            class="message-avatar"
            :src="message.isSelf ? selfInfo.avatar : userInfo.avatar"
            mode="aspectFill"
          ></image>
          <view class="message-content">
            <view
              class="message-bubble"
              :class="{ self: message.isSelf }"
            >
              <text class="message-text">{{ message.text }}</text>
              <image
                v-if="message.image"
                class="message-image"
                :src="message.image"
                mode="aspectFill"
                @click="previewImage(message.image)"
              ></image>
              <view v-if="message.location" class="message-location" @click="openLocation(message.location)">
                <text class="location-icon">📍</text>
                <text class="location-text">{{ message.location.address }}</text>
              </view>
            </view>
            <text class="message-time">{{ message.time }}</text>
          </view>
        </view>

        <view class="loading-more" v-if="loading">
          <text class="loading-text">加载中...</text>
        </view>
      </scroll-view>
    </view>

    <!-- 底部输入框 -->
    <view class="input-bar">
      <view class="input-left">
        <text class="icon-btn" @click="chooseImage">📷</text>
        <text class="icon-btn" @click="chooseLocation">📍</text>
      </view>
      <input
        class="message-input"
        v-model="inputText"
        placeholder="输入消息..."
        @confirm="sendMessage"
      />
      <button class="send-btn" @click="sendMessage" :disabled="!inputText">
        发送
      </button>
    </view>
  </view>
</template>

<script>
import { request } from '../../api/request'

export default {
  data() {
    return {
      chatId: '',
      userId: '',
      userName: '',
      userInfo: {
        id: '',
        name: '',
        avatar: '',
        taskId: ''
      },
      selfInfo: {
        id: 'SELF',
        avatar: '/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg'
      },
      messages: [],
      inputText: '',
      scrollIntoView: '',
      chatTime: '',
      loading: false,
      loadingMore: false,
      page: 1,
      hasMore: true,
      pollingTimer: null,
      socket: null,
      socketConnected: false
    }
  },
  onLoad(options) {
    this.chatId = options.chatId || ''
    this.userId = options.userId || ''
    this.userName = options.name || '用户'

    // 设置聊天时间
    const now = new Date()
    this.chatTime = `${now.getMonth() + 1}月${now.getDate()}日`

    this.userInfo = {
      id: this.userId,
      name: this.userName,
      avatar: '/static/avatars/b_29b8403823ac002ad652af4f2a429767.jpg',
      taskId: 'T20250103001'
    }

    // 如果没有chatId，创建或获取聊天会话
    if (!this.chatId && this.userId) {
      this.createOrGetChat()
    } else {
      this.loadMessages()
    }
    this.connectWebSocket()
  },
  onUnload() {
    this.stopPolling()
    this.closeWebSocket()
  },

  methods: {
    connectWebSocket() {
      const token = uni.getStorageSync('token')
      if (!token) return

      // 建立WebSocket连接
      const socketUrl = `ws://localhost:18083/ws/chat?token=${token}`
      this.socket = uni.connectSocket({
        url: socketUrl,
        success: () => {
          console.log('WebSocket连接请求成功')
        },
        fail: (error) => {
          console.error('WebSocket连接失败:', error)
        }
      })

      // 监听连接打开
      this.socket.onOpen(() => {
        console.log('WebSocket连接已打开')
        this.socketConnected = true
      })

      // 监听接收消息
      this.socket.onMessage((res) => {
        console.log('收到WebSocket消息:', res.data)
        this.handleWebSocketMessage(res.data)
      })

      // 监听连接关闭
      this.socket.onClose(() => {
        console.log('WebSocket连接已关闭')
        this.socketConnected = false
      })

      // 监听连接错误
      this.socket.onError((error) => {
        console.error('WebSocket连接错误:', error)
        this.socketConnected = false
      })
    },

    closeWebSocket() {
      if (this.socket) {
        uni.closeSocket()
        this.socket = null
        this.socketConnected = false
      }
    },

    handleWebSocketMessage(message) {
      try {
        const data = JSON.parse(message)
        
        // 处理不同类型的消息
        if (data.type === 'message') {
          // 新消息
          const user = uni.getStorageSync('user')
          const currentUserId = user ? String(user.id) : ''
          const newMessage = {
            id: data.id,
            isSelf: String(data.senderId) === currentUserId,
            text: data.messageType === 'text' ? data.content : '',
            time: this.formatMessageTime(data.createTime),
            image: data.messageType === 'image' ? data.content : null,
            location: data.messageType === 'location' ? JSON.parse(data.content) : null,
            isRead: data.isRead
          }
          
          this.messages.push(newMessage)
          this.$nextTick(() => {
            this.scrollToBottom()
          })
        } else if (data.type === 'read') {
          // 消息已读状态更新
          this.updateMessageReadStatus(data.messageId)
        }
      } catch (error) {
        console.error('处理WebSocket消息错误:', error)
      }
    },

    updateMessageReadStatus(messageId) {
      const message = this.messages.find(msg => msg.id === messageId)
      if (message) {
        message.isRead = true
      }
    },

    async loadMessages() {
      if (!this.chatId) return

      this.loading = true
      this.page = 1
      this.hasMore = true
      try {
        const token = uni.getStorageSync('token')
        if (!token) {
          uni.showToast({ title: '请先登录', icon: 'none' })
          return
        }

        const res = await request({
          url: `/chat/${this.chatId}/messages`,
          method: 'GET',
          header: {
            'token': token
          },
          data: {
            page: this.page,
            pageSize: 50
          }
        })

        if (res.code === 200) {
          const messageList = res.data.list || []
          
          // 处理消息数据
          const user = uni.getStorageSync('user')
          const currentUserId = user ? String(user.id) : ''
          this.messages = messageList.reverse().map(msg => ({
            id: msg.id,
            isSelf: String(msg.senderId) === currentUserId,
            text: msg.messageType === 'text' ? msg.content : '',
            time: this.formatMessageTime(msg.createTime),
            image: msg.messageType === 'image' ? msg.content : null,
            location: msg.messageType === 'location' ? JSON.parse(msg.content) : null,
            isRead: msg.isRead
          }))

          this.hasMore = messageList.length >= 50
          this.page++
        } else {
          uni.showToast({ title: res.msg || '获取消息失败', icon: 'none' })
        }
      } catch (error) {
        console.error('获取消息错误:', error)
        uni.showToast({ title: '网络错误，请稍后重试', icon: 'none' })
      } finally {
        this.loading = false
        this.$nextTick(() => {
          this.scrollToBottom()
        })
      }
    },

    async loadMoreMessages() {
      if (!this.chatId || this.loadingMore || !this.hasMore) return

      this.loadingMore = true
      try {
        const token = uni.getStorageSync('token')
        if (!token) {
          return
        }

        const res = await request({
          url: `/chat/${this.chatId}/messages`,
          method: 'GET',
          header: {
            'token': token
          },
          data: {
            page: this.page,
            pageSize: 50
          }
        })

        if (res.code === 200) {
          const messageList = res.data.list || []
          const user = uni.getStorageSync('user')
          const currentUserId = user ? String(user.id) : ''
          
          if (messageList.length > 0) {
            // 处理消息数据
            const newMessages = messageList.reverse().map(msg => ({
              id: msg.id,
              isSelf: String(msg.senderId) === currentUserId,
              text: msg.messageType === 'text' ? msg.content : '',
              time: this.formatMessageTime(msg.createTime),
              image: msg.messageType === 'image' ? msg.content : null,
              location: msg.messageType === 'location' ? JSON.parse(msg.content) : null,
              isRead: msg.isRead
            }))

            // 添加到消息列表的前面
            this.messages = [...newMessages, ...this.messages]
            this.hasMore = messageList.length >= 50
            this.page++
          } else {
            this.hasMore = false
          }
        }
      } catch (error) {
        console.error('加载更多消息错误:', error)
      } finally {
        this.loadingMore = false
      }
    },

    formatMessageTime(timeStr) {
      if (!timeStr) return ''
      
      const date = new Date(timeStr)
      return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    },

    async sendMessage() {
      const content = this.inputText.trim()
      if (!content || !this.chatId) {
        return
      }

      const token = uni.getStorageSync('token')
      if (!token) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }

      const message = {
        isSelf: true,
        text: content,
        time: this.getCurrentTime(),
        image: null,
        location: null,
        status: 'sending'
      }

      const messageIndex = this.messages.length
      this.messages.push(message)
      this.inputText = ''

      this.$nextTick(() => {
        this.scrollToBottom()
      })

      try {
        const res = await request({
          url: '/chat/send',
          method: 'POST',
          header: {
            'token': token
          },
          data: {
            chatId: this.chatId,
            content: content,
            messageType: 'text'
          }
        })

        if (res.code === 200) {
          // 更新消息状态为已发送
          this.messages[messageIndex].status = 'sent'
          this.messages[messageIndex].id = res.data.id
        } else {
          // 更新消息状态为发送失败
          this.messages[messageIndex].status = 'failed'
          uni.showToast({ title: res.msg || '发送失败', icon: 'none' })
        }
      } catch (error) {
        console.error('发送消息错误:', error)
        // 更新消息状态为发送失败
        this.messages[messageIndex].status = 'failed'
        uni.showToast({ title: '网络错误，请稍后重试', icon: 'none' })
      }
    },

    async chooseImage() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: async (res) => {
          const tempFilePaths = res.tempFilePaths
          const token = uni.getStorageSync('token')
          
          if (!token) {
            uni.showToast({ title: '请先登录', icon: 'none' })
            return
          }

          // 显示上传中状态
          const message = {
            isSelf: true,
            text: '',
            time: this.getCurrentTime(),
            image: tempFilePaths[0],
            location: null,
            status: 'sending'
          }

          const messageIndex = this.messages.length
          this.messages.push(message)
          this.$nextTick(() => {
            this.scrollToBottom()
          })

          try {
            // 上传图片
            const uploadRes = await request.upload({
              url: '/file/upload',
              filePath: tempFilePaths[0],
              name: 'file',
              header: {
                'token': token
              },
              formData: {
                type: 'image'
              }
            })

            if (uploadRes.code === 200) {
              const imageUrl = uploadRes.data.url
              
              // 发送图片消息
              const sendRes = await request({
                url: '/chat/send',
                method: 'POST',
                header: {
                  'token': token
                },
                data: {
                  chatId: this.chatId,
                  content: imageUrl,
                  messageType: 'image'
                }
              })

              if (sendRes.code === 200) {
                // 更新消息状态为已发送
                this.messages[messageIndex].status = 'sent'
                this.messages[messageIndex].id = sendRes.data.id
              } else {
                // 更新消息状态为发送失败
                this.messages[messageIndex].status = 'failed'
                uni.showToast({ title: sendRes.msg || '发送失败', icon: 'none' })
              }
            } else {
              // 更新消息状态为发送失败
              this.messages[messageIndex].status = 'failed'
              uni.showToast({ title: uploadRes.msg || '上传失败', icon: 'none' })
            }
          } catch (error) {
            console.error('上传图片错误:', error)
            // 更新消息状态为发送失败
            this.messages[messageIndex].status = 'failed'
            uni.showToast({ title: '网络错误，请稍后重试', icon: 'none' })
          }
        }
      })
    },

    async chooseLocation() {
      uni.chooseLocation({
        success: async (res) => {
          const token = uni.getStorageSync('token')
          if (!token) {
            uni.showToast({ title: '请先登录', icon: 'none' })
            return
          }

          // 创建位置消息对象
          const locationData = {
            latitude: res.latitude,
            longitude: res.longitude,
            address: res.address || res.name
          }

          const message = {
            isSelf: true,
            text: '',
            time: this.getCurrentTime(),
            image: null,
            location: locationData,
            status: 'sending'
          }

          const messageIndex = this.messages.length
          this.messages.push(message)
          this.$nextTick(() => {
            this.scrollToBottom()
          })

          try {
            // 发送位置消息
            const sendRes = await request({
              url: '/chat/send',
              method: 'POST',
              header: {
                'token': token
              },
              data: {
                chatId: this.chatId,
                content: JSON.stringify(locationData),
                messageType: 'location'
              }
            })

            if (sendRes.code === 200) {
              // 更新消息状态为已发送
              this.messages[messageIndex].status = 'sent'
              this.messages[messageIndex].id = sendRes.data.id
            } else {
              // 更新消息状态为发送失败
              this.messages[messageIndex].status = 'failed'
              uni.showToast({ title: sendRes.msg || '发送失败', icon: 'none' })
            }
          } catch (error) {
            console.error('发送位置消息错误:', error)
            // 更新消息状态为发送失败
            this.messages[messageIndex].status = 'failed'
            uni.showToast({ title: '网络错误，请稍后重试', icon: 'none' })
          }
        }
      })
    },

    previewImage(url) {
      uni.previewImage({
        urls: [url]
      })
    },

    openLocation(location) {
      uni.openLocation({
        latitude: location.latitude,
        longitude: location.longitude,
        name: location.address
      })
    },

    viewUserProfile() {
      uni.navigateTo({
        url: `/pages/runner/detail?id=${this.userId}`
      })
    },

    async makePhoneCall() {
      // 假设我们可以从用户信息中获取电话，或者需要调用API获取
      // 这里简化处理，直接让用户输入电话号码
      uni.showModal({
        title: '拨打电话',
        content: '请输入对方电话号码',
        editable: true,
        placeholderText: '请输入电话号码',
        success: (res) => {
          if (res.confirm && res.content) {
            const phoneNumber = res.content.trim()
            if (/^1[3-9]\d{9}$/.test(phoneNumber)) {
              uni.makePhoneCall({
                phoneNumber: phoneNumber,
                success: () => {
                  console.log('拨打电话成功')
                },
                fail: (error) => {
                  console.error('拨打电话失败:', error)
                  uni.showToast({ title: '拨打电话失败', icon: 'none' })
                }
              })
            } else {
              uni.showToast({ title: '请输入正确的电话号码', icon: 'none' })
            }
          }
        }
      })
    },

    showMore() {
      uni.showActionSheet({
        itemList: ['查看资料', '举报用户', '清空聊天记录'],
        success: (res) => {
          switch (res.tapIndex) {
            case 0:
              this.viewUserProfile()
              break
            case 1:
              this.reportUser()
              break
            case 2:
              this.clearMessages()
              break
          }
        }
      })
    },

    async reportUser() {
      // 显示举报类型选择
      uni.showActionSheet({
        itemList: ['恶意骚扰', '虚假信息', '违规内容', '其他'],
        success: (res) => {
          const reportTypes = ['harassment', 'false_info', 'illegal_content', 'other']
          const selectedType = reportTypes[res.tapIndex]
          const typeLabel = ['恶意骚扰', '虚假信息', '违规内容', '其他'][res.tapIndex]
          
          // 显示举报内容输入框
          uni.showModal({
            title: '举报用户',
            content: `请描述您要举报的内容（举报类型：${typeLabel}）`,
            editable: true,
            placeholderText: '请详细描述您的举报内容',
            success: async (modalRes) => {
              if (modalRes.confirm && modalRes.content) {
                const token = uni.getStorageSync('token')
                if (!token) {
                  uni.showToast({ title: '请先登录', icon: 'none' })
                  return
                }

                try {
                  // 准备聊天记录数据
                  const chatRecords = this.messages.map(msg => ({
                    sender: msg.isSelf ? 'me' : 'other',
                    content: msg.text || (msg.image ? '[图片]' : (msg.location ? '[位置]' : '')),
                    time: msg.time
                  }))
                  
                  // 调用API创建举报
                  const reportRes = await request({
                    url: '/reports',
                    method: 'POST',
                    header: {
                      'token': token
                    },
                    data: {
                      reportedUserId: this.userId,
                      chatId: this.chatId,
                      type: selectedType,
                      content: modalRes.content.trim(),
                      chatRecords: JSON.stringify(chatRecords)
                    }
                  })

                  if (reportRes.code === 200) {
                    uni.showToast({ title: '举报成功，我们将尽快处理', icon: 'success' })
                  } else {
                    uni.showToast({ title: '举报失败', icon: 'none' })
                  }
                } catch (error) {
                  console.error('举报用户错误:', error)
                  uni.showToast({ title: '网络错误，请稍后重试', icon: 'none' })
                }
              }
            }
          })
        }
      })
    },

    async clearMessages() {
      uni.showModal({
        title: '确认清空',
        content: '确定要清空聊天记录吗？',
        success: async (res) => {
          if (res.confirm) {
            const token = uni.getStorageSync('token')
            if (!token) {
              uni.showToast({ title: '请先登录', icon: 'none' })
              return
            }

            try {
              // 调用API清空聊天记录
              const clearRes = await request({
                url: `/chat/${this.chatId}/messages`,
                method: 'DELETE',
                header: {
                  'token': token
                }
              })

              if (clearRes.code === 200) {
                // 清空本地消息列表
                this.messages = []
                uni.showToast({ title: '聊天记录已清空', icon: 'success' })
              } else {
                uni.showToast({ title: '清空聊天记录失败', icon: 'none' })
              }
            } catch (error) {
              console.error('清空聊天记录错误:', error)
              uni.showToast({ title: '网络错误，请稍后重试', icon: 'none' })
            }
          }
        }
      })
    },

    scrollToBottom() {
      const lastIndex = this.messages.length - 1
      if (lastIndex >= 0) {
        this.scrollIntoView = 'msg-' + lastIndex
      }
    },

    getCurrentTime() {
      const now = new Date()
      const hours = String(now.getHours()).padStart(2, '0')
      const minutes = String(now.getMinutes()).padStart(2, '0')
      return `${hours}:${minutes}`
    },

    startPolling() {
      // 每3秒轮询一次新消息
      this.pollingTimer = setInterval(() => {
        this.checkNewMessages()
      }, 3000)
    },

    stopPolling() {
      if (this.pollingTimer) {
        clearInterval(this.pollingTimer)
        this.pollingTimer = null
      }
    },

    checkNewMessages() {
      // TODO: 调用API检查新消息
      // 如果有新消息，更新messages数组
    },
    
    goBack() {
      uni.navigateBack()
    },
    
    async createOrGetChat() {
      const token = uni.getStorageSync('token')
      if (!token) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      
      try {
        const res = await request({
          url: '/chat/create',
          method: 'POST',
          header: {
            'token': token
          },
          data: {
            targetUserId: this.userId
          }
        })
        
        if (res.code === 200 && res.data) {
          this.chatId = res.data.id
          this.loadMessages()
        } else {
          uni.showToast({ title: '创建聊天会话失败', icon: 'none' })
        }
      } catch (error) {
        console.error('创建聊天会话错误:', error)
        uni.showToast({ title: '网络错误，请稍后重试', icon: 'none' })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.chat-detail-container {
  height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  background: white;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  z-index: 100;
  flex-shrink: 0;
  height: 128rpx;
  box-sizing: border-box;

  .header-left {
    display: flex;
    align-items: center;
    padding-right: 20rpx;

    .back-icon {
      font-size: 48rpx;
      color: #333;
      font-weight: bold;
    }
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 20rpx;
    flex: 1;

    .avatar {
      width: 88rpx;
      height: 88rpx;
      border-radius: 44rpx;
    }

    .user-detail {
      display: flex;
      flex-direction: column;

      .username {
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
        margin-bottom: 4rpx;
      }

      .user-status {
        font-size: 24rpx;
        color: #2196f3;
      }
    }
  }

  .header-actions {
    display: flex;
    gap: 24rpx;
    padding-left: 20rpx;

    .action-icon {
      font-size: 40rpx;
    }
  }
}

.message-wrapper {
  flex: 1;
  overflow: hidden;
}

.message-container {
  width: 100%;
  height: 100%;
  padding: 20rpx;
}

.time-divider {
  text-align: center;
  margin: 30rpx 0;

  .time-text {
    padding: 8rpx 24rpx;
    background: rgba(0, 0, 0, 0.1);
    color: white;
    border-radius: 20rpx;
    font-size: 24rpx;
  }
}

.message-item {
  display: flex;
  margin-bottom: 30rpx;

  &.self {
    flex-direction: row-reverse;
  }

  .message-avatar {
    width: 72rpx;
    height: 72rpx;
    border-radius: 36rpx;
    flex-shrink: 0;
  }

  .message-content {
    max-width: 70%;
    margin: 0 20rpx;
    display: flex;
    flex-direction: column;

    &.self {
      align-items: flex-end;
    }

    .message-bubble {
      padding: 20rpx;
      background: white;
      border-radius: 16rpx;
      position: relative;
      word-wrap: break-word;

      &.self {
        background: #2196f3;

        .message-text {
          color: white;
        }
      }

      .message-text {
        font-size: 28rpx;
        color: #333;
        line-height: 1.6;
      }

      .message-image {
        max-width: 400rpx;
        max-height: 400rpx;
        border-radius: 8rpx;
        margin-top: 10rpx;
      }

      .message-location {
        display: flex;
        align-items: center;
        gap: 12rpx;
        padding: 12rpx;
        background: #f5f5f5;
        border-radius: 8rpx;
        margin-top: 10rpx;

        .location-icon {
          font-size: 32rpx;
        }

        .location-text {
          font-size: 26rpx;
          color: #666;
        }
      }
    }

    .message-time {
      font-size: 20rpx;
      color: #999;
      margin-top: 8rpx;
    }
  }
}

.loading-more {
  text-align: center;
  padding: 20rpx 0;

  .loading-text {
    font-size: 24rpx;
      color: #999;
  }
}

.input-bar {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  background: white;
  box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.06);
  gap: 16rpx;
  z-index: 100;
  flex-shrink: 0;

  .input-left {
    display: flex;
    gap: 16rpx;

    .icon-btn {
      font-size: 48rpx;
    }
  }

  .message-input {
    flex: 1;
    height: 72rpx;
    background: #f5f5f5;
    border-radius: 36rpx;
    padding: 0 24rpx;
    font-size: 28rpx;
  }

  .send-btn {
    width: 120rpx;
    height: 72rpx;
    background: linear-gradient(135deg, #2196f3, #1976d2);
    color: white;
    border-radius: 36rpx;
    font-size: 28rpx;
    border: none;
    padding: 0;

    &[disabled] {
      background: #ccc;
    }
  }
}
</style>
