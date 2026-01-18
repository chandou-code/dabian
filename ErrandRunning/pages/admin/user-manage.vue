<template>
  <view class="admin-user-container">
    <view class="admin-header">
      <text class="admin-title">用户管理</text>
      <text class="admin-subtitle">管理平台所有用户，包括普通用户和跑腿员</text>
    </view>

    <view class="stats-section">
      <view class="stat-card">
        <text class="stat-value">{{ totalUsers }}</text>
        <text class="stat-label">总用户数</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ userCount }}</text>
        <text class="stat-label">普通用户</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ runnerCount }}</text>
        <text class="stat-label">跑腿员</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ adminCount }}</text>
        <text class="stat-label">管理员</text>
      </view>
    </view>

    <view class="user-list">
      <view class="list-header">
        <text class="header-item width-150">用户信息</text>
        <text class="header-item width-100">角色</text>
        <text class="header-item width-100">状态</text>
        <text class="header-item width-150">注册时间</text>
        <text class="header-item width-80">操作</text>
      </view>

      <view 
        v-for="user in users" 
        :key="user.id"
        class="user-item"
      >
        <view class="user-info">
          <image class="user-avatar" :src="user.avatar" mode="aspectFill"></image>
          <view class="user-detail">
            <text class="user-name">{{ user.nickname || user.realName || user.username }}</text>
            <text class="user-account">{{ user.username }}</text>
            <text class="user-phone">{{ user.phone }}</text>
          </view>
        </view>
        
        <view class="user-role">
          <text class="role-tag" :class="user.role">{{ getRoleLabel(user.role) }}</text>
        </view>
        
        <view class="user-status">
          <text class="status-tag" :class="user.status === 1 ? 'active' : 'inactive'">
            {{ user.status === 1 ? '启用' : '禁用' }}
          </text>
        </view>
        
        <view class="user-time">
          {{ formatDate(user.registerTime) }}
        </view>
        
        <view class="user-actions">
          <!-- 小三角展开按钮 -->
          <view 
            class="expand-btn" 
            :class="{ expanded: expandedUserId === user.id }"
            @click="expandedUserId = expandedUserId === user.id ? null : user.id"
          >
            <text class="triangle">▼</text>
          </view>
          
          <!-- 展开的操作菜单 -->
          <view 
            class="action-menu" 
            :class="{ show: expandedUserId === user.id }"
          >
            <button class="action-btn" @click="editUser(user)">编辑</button>
            <button 
              class="action-btn" 
              @click="toggleUserStatus(user)"
            >
              {{ user.status === 1 ? '禁用' : '启用' }}
            </button>
            <button class="action-btn" @click="changeRole(user)">变更角色</button>
            <button class="action-btn danger" @click="deleteUser(user)">删除</button>
          </view>
        </view>
      </view>

      <view v-if="users.length === 0" class="empty-state">
        <text class="empty-icon">👥</text>
        <text class="empty-text">暂无用户数据</text>
      </view>
    </view>

    <uni-load-more 
      v-if="hasMore" 
      :status="loadStatus" 
      @clickLoadMore="loadMore"
    ></uni-load-more>

    <!-- 角色变更弹窗 -->
    <view class="custom-popup" v-if="showRolePopup">
      <!-- 遮罩层 -->
      <view class="popup-mask" @click="closeRolePopup"></view>
      <!-- 弹窗内容 -->
      <view class="popup-container">
        <view class="popup-header">
          <text class="popup-title">变更角色</text>
          <text class="popup-close" @click="closeRolePopup">×</text>
        </view>
        <view class="popup-content">
          <text class="popup-subtitle">为 {{ currentUser.nickname || currentUser.username }} 选择新角色</text>
          <view class="role-options">
            <view 
              v-for="role in roleOptions.filter(r => r.value)" 
              :key="role.value"
              class="role-option"
              :class="{ active: selectedRole === role.value }"
              @click="selectedRole = role.value"
            >
              <text class="role-option-text">{{ role.label }}</text>
            </view>
          </view>
        </view>
        <view class="popup-footer">
          <button class="btn btn-secondary" @click="closeRolePopup">取消</button>
          <button class="btn btn-primary" @click="confirmChangeRole">确认</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { get, put, del } from '../../api/request'

export default {
  data() {
    return {
      users: [],
      roleOptions: [
        { label: '全部角色', value: '' },
        { label: '普通用户', value: 'user' },
        { label: '跑腿员', value: 'runner' },
        { label: '管理员', value: 'admin' }
      ],
      filterRole: '',
      loadStatus: 'more',
      hasMore: true,
      currentPage: 1,
      pageSize: 10,
      loading: false,
      totalUsers: 0,
      userCount: 0,
      runnerCount: 0,
      adminCount: 0,
      
      // 展开/收起状态控制
      expandedUserId: null,
      
      // 角色变更弹窗相关
      currentUser: {},
      selectedRole: '',
      showRolePopup: false
    }
  },

  onLoad() {
    this.loadUserStats()
    this.loadUsers()
  },

  methods: {
    async loadUserStats() {
      try {
        const response = await get('/admin/users/stats')
        if (response.code === 200 && response.data) {
          this.totalUsers = response.data.total || 0
          this.userCount = response.data.users || 0
          this.runnerCount = response.data.runners || 0
          this.adminCount = response.data.admins || 0
        }
      } catch (error) {
        console.error('获取用户统计失败:', error)
        uni.showToast({
          title: '获取统计数据失败',
          icon: 'none'
        })
      }
    },

    async loadUsers(refresh = false) {
      if (this.loading) return
      
      this.loading = true
      if (refresh) {
        this.currentPage = 1
        this.users = []
      }
      
      try {
        // 构建请求参数
        const params = {
          role: this.filterRole || null,
          page: this.currentPage,
          pageSize: this.pageSize
        }
        
        // 调用获取用户列表API
        const response = await get('/admin/users/list', params)
        
        if (response.code === 200 && response.data) {
          const { list, total } = response.data
          
          if (refresh) {
            this.users = list
          } else {
            this.users = [...this.users, ...list]
          }
          
          // 更新加载状态
          this.hasMore = this.users.length < total
          this.loadStatus = this.hasMore ? 'more' : 'noMore'
        } else {
          uni.showToast({
            title: response.msg || '获取用户列表失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('获取用户列表失败:', error)
        uni.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },

    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    },

    getRoleLabel(role) {
      const roleMap = {
        user: '普通用户',
        runner: '跑腿员',
        admin: '管理员'
      }
      return roleMap[role] || role
    },

    editUser(user) {
      uni.showToast({
        title: '编辑功能开发中',
        icon: 'none'
      })
    },

    async toggleUserStatus(user) {
      const newStatus = user.status === 1 ? 0 : 1
      const action = newStatus === 1 ? '启用' : '禁用'
      
      uni.showModal({
        title: `${action}用户`,
        content: `确定要${action}用户 ${user.nickname || user.username} 吗？`,
        success: async (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '处理中...' })
            try {
              const response = await put(`/admin/users/${user.id}/status`, { status: newStatus })
              
              if (response.code === 200) {
                user.status = newStatus
                uni.showToast({
                  title: `${action}成功`,
                  icon: 'success'
                })
                this.loadUserStats() // 更新统计数据
              } else {
                uni.showToast({
                  title: response.msg || `${action}失败`,
                  icon: 'none'
                })
              }
            } catch (error) {
              console.error(`${action}用户失败:`, error)
              // 检查是否是网络错误
              if (error.errMsg) {
                uni.showToast({
                  title: '网络错误，请稍后重试',
                  icon: 'none'
                })
              } else {
                uni.showToast({
                  title: error.msg || `${action}失败`,
                  icon: 'none'
                })
              }
            } finally {
              uni.hideLoading()
            }
          }
        }
      })
    },

    changeRole(user) {
      this.currentUser = user
      this.selectedRole = user.role
      this.showRolePopup = true
    },

    closeRolePopup() {
      this.showRolePopup = false
    },

    async confirmChangeRole() {
      if (this.selectedRole === this.currentUser.role) {
        this.closeRolePopup()
        return
      }
      
      // 保存当前选择的角色和用户信息
      const selectedRole = this.selectedRole
      const currentUser = this.currentUser
      
      // 先关闭自定义弹窗，让确认对话框能正常显示
      this.closeRolePopup()
      
      uni.showModal({
        title: '变更角色',
        content: `确定要将用户 ${currentUser.nickname || currentUser.username} 的角色变更为 ${this.getRoleLabel(selectedRole)} 吗？`,
        success: async (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '处理中...' })
            try {
              const response = await put(`/admin/users/${currentUser.id}/role`, { role: selectedRole })
              
              if (response.code === 200) {
                // 更新本地数据
                const userIndex = this.users.findIndex(u => u.id === currentUser.id)
                if (userIndex !== -1) {
                  this.users[userIndex].role = selectedRole
                }
                
                uni.showToast({
                  title: '角色变更成功',
                  icon: 'success'
                })
                this.loadUserStats() // 更新统计数据
              } else {
                uni.showToast({
                  title: response.msg || '角色变更失败',
                  icon: 'none'
                })
              }
            } catch (error) {
              console.error('角色变更失败:', error)
              // 检查是否是网络错误
              if (error.errMsg) {
                uni.showToast({
                  title: '网络错误，请稍后重试',
                  icon: 'none'
                })
              } else {
                uni.showToast({
                  title: error.msg || '角色变更失败',
                  icon: 'none'
                })
              }
            } finally {
              uni.hideLoading()
            }
          }
        }
      })
    },

    async deleteUser(user) {
      uni.showModal({
        title: '删除用户',
        content: `确定要删除用户 ${user.nickname || user.username} 吗？此操作不可恢复。`,
        confirmColor: '#f44336',
        success: async (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '处理中...' })
            try {
              const response = await del(`/admin/users/${user.id}`)
              
              if (response.code === 200) {
                // 从列表中移除该用户
                const index = this.users.findIndex(item => item.id === user.id)
                if (index !== -1) {
                  this.users.splice(index, 1)
                }
                uni.showToast({
                  title: '删除成功',
                  icon: 'success'
                })
                this.loadUserStats() // 更新统计数据
              } else {
                uni.showToast({
                  title: response.msg || '删除失败',
                  icon: 'none'
                })
              }
            } catch (error) {
              console.error('删除用户失败:', error)
              // 检查是否是网络错误
              if (error.errMsg) {
                uni.showToast({
                  title: '网络错误，请稍后重试',
                  icon: 'none'
                })
              } else {
                uni.showToast({
                  title: error.msg || '删除失败',
                  icon: 'none'
                })
              }
            } finally {
              uni.hideLoading()
            }
          }
        }
      })
    },

    loadMore() {
      if (this.loadStatus === 'more' && !this.loading) {
        this.currentPage++
        this.loadUsers()
      }
    }
  }
}
</script>

<style scoped lang="scss">
.admin-user-container {
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

.stats-section {
  display: flex;
  gap: 20rpx;
  margin-bottom: 24rpx;
  flex-wrap: wrap;

  .stat-card {
    flex: 1;
    min-width: 180rpx;
    background: #fff;
    border-radius: 12rpx;
    padding: 24rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
    text-align: center;
    transition: transform 0.2s ease, box-shadow 0.2s ease;

    &:hover {
      transform: translateY(-2rpx);
      box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.15);
    }

    .stat-value {
      display: block;
      font-size: 48rpx;
      font-weight: 600;
      color: #1976d2;
      margin-bottom: 8rpx;
      line-height: 1;
    }

    .stat-label {
      font-size: 24rpx;
      color: #666;
      line-height: 1;
    }
  }
}

.user-list {
  background: #fff;
  border-radius: 12rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  margin-bottom: 40rpx;
  overflow: visible;

  .list-header {
    display: flex;
    background: #f8f9fa;
    padding: 20rpx;
    border-bottom: 1rpx solid #e9ecef;
    font-weight: 600;
    font-size: 26rpx;
    color: #495057;
  }

  .header-item {
    display: flex;
    align-items: center;
    justify-content: center;

    &.width-150 {
      width: 300rpx;
      justify-content: flex-start;
    }

    &.width-100 {
      width: 200rpx;
    }

    &.width-80 {
      width: 160rpx;
    }
  }

  .user-item {
    display: flex;
    align-items: center;
    padding: 20rpx;
    border-bottom: 1rpx solid #e9ecef;
    transition: background-color 0.2s ease;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background-color: #f8f9fa;
    }

    .user-info {
      display: flex;
      align-items: center;
      width: 300rpx;

      .user-avatar {
        width: 80rpx;
        height: 80rpx;
        border-radius: 40rpx;
        margin-right: 20rpx;
        background: #e9ecef;
      }

      .user-detail {
        flex: 1;

        .user-name {
          display: block;
          font-size: 28rpx;
          font-weight: 600;
          color: #212529;
          margin-bottom: 4rpx;
        }

        .user-account {
          display: block;
          font-size: 24rpx;
          color: #6c757d;
          margin-bottom: 4rpx;
        }

        .user-phone {
          display: block;
          font-size: 24rpx;
          color: #6c757d;
        }
      }
    }

    .user-role {
      width: 200rpx;
      text-align: center;

      .role-tag {
        padding: 8rpx 16rpx;
        border-radius: 20rpx;
        font-size: 24rpx;
        font-weight: 500;

        &.user {
          background: #e3f2fd;
          color: #1976d2;
        }

        &.runner {
          background: #e8f5e8;
          color: #388e3c;
        }

        &.admin {
          background: #fce4ec;
          color: #c2185b;
        }
      }
    }

    .user-status {
      width: 200rpx;
      text-align: center;

      .status-tag {
        padding: 8rpx 16rpx;
        border-radius: 20rpx;
        font-size: 24rpx;
        font-weight: 500;

        &.active {
          background: #e8f5e8;
          color: #388e3c;
        }

        &.inactive {
          background: #ffebee;
          color: #d32f2f;
        }
      }
    }

    .user-time {
      width: 200rpx;
      text-align: center;
      font-size: 24rpx;
      color: #6c757d;
    }

    .user-actions {
      width: 160rpx;
      text-align: center;
      position: relative;

      .expand-btn {
        display: inline-block;
        cursor: pointer;
        padding: 8rpx;
        transition: transform 0.2s ease;

        .triangle {
          font-size: 20rpx;
          color: #6c757d;
        }

        &.expanded {
          transform: rotate(180deg);

          .triangle {
            color: #1976d2;
          }
        }
      }

      .action-menu {
        position: absolute;
        top: 100%;
        right: 0;
        background: #fff;
        border: 1rpx solid #e9ecef;
        border-radius: 8rpx;
        box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
        padding: 8rpx 0;
        min-width: 160rpx;
        opacity: 0;
        visibility: hidden;
        transform: translateY(-10rpx);
        transition: all 0.2s ease;
        z-index: 100;

        &.show {
          opacity: 1;
          visibility: visible;
          transform: translateY(0);
        }

        .action-btn {
          display: block;
          width: 100%;
          padding: 12rpx 20rpx;
          font-size: 24rpx;
          color: #495057;
          background: transparent;
          border: none;
          text-align: left;
          transition: all 0.2s ease;

          &:hover {
            background: #f8f9fa;
            color: #1976d2;
          }

          &.danger {
            color: #d32f2f;

            &:hover {
              background: #ffebee;
            }
          }
        }
      }
    }
  }

  .empty-state {
    text-align: center;
    padding: 80rpx 20rpx;

    .empty-icon {
      display: block;
      font-size: 80rpx;
      margin-bottom: 20rpx;
    }

    .empty-text {
      font-size: 28rpx;
      color: #999;
    }
  }
}

/* 自定义弹窗样式 */
.custom-popup {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
}

.popup-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
}

.popup-container {
  position: relative;
  width: 600rpx;
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
  z-index: 10000;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  border-bottom: 1rpx solid #e9ecef;

  .popup-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #212529;
  }

  .popup-close {
    font-size: 40rpx;
    color: #6c757d;
    cursor: pointer;
  }
}

.popup-content {
  padding: 32rpx;

  .popup-subtitle {
    display: block;
    font-size: 28rpx;
    color: #495057;
    margin-bottom: 24rpx;
  }

  .role-options {
    display: flex;
    flex-direction: column;
    gap: 16rpx;
  }

  .role-option {
    padding: 24rpx;
    border: 2rpx solid #e9ecef;
    border-radius: 8rpx;
    text-align: center;
    font-size: 28rpx;
    color: #495057;
    cursor: pointer;
    transition: all 0.2s ease;

    &.active {
      border-color: #1976d2;
      background: #e3f2fd;
      color: #1976d2;
      font-weight: 600;
    }
  }
}

.popup-footer {
  display: flex;
  gap: 20rpx;
  padding: 24rpx;
  border-top: 1rpx solid #e9ecef;
  justify-content: flex-end;

  .btn {
    padding: 16rpx 32rpx;
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
}
</style>
