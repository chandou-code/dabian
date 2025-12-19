<template>
  <view class="user-management">
    <Sidebar />
    
    <view class="main-content" :class="{ 'main-content-expanded': !showSidebar }">
      <!-- 操作栏 -->
      <view class="action-bar">
        <view class="search-section">
          <input 
            v-model="searchKeyword" 
            class="search-input" 
            placeholder="搜索用户名、邮箱或手机号..."
            @confirm="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">🔍</button>
        </view>
        
        <view class="action-buttons">
          <button class="add-btn" @click="showAddUserModal">添加用户</button>
          <button class="export-btn" @click="exportUsers">导出数据</button>
        </view>
      </view>
      
      <!-- 用户列表 -->
      <view class="users-table">
        <view class="table-header">
          <text class="table-title">用户管理</text>
          <text class="user-count">共 {{ totalUsers }} 位用户</text>
        </view>
        
        <view class="table-container">
          <!-- 表头 -->
          <view class="table-row table-head">
            <view class="table-cell">
              <checkbox @change="selectAllUsers" :checked="allSelected" />
            </view>
            <view class="table-cell">用户信息</view>
            <view class="table-cell">联系方式</view>
            <view class="table-cell">角色</view>
            <view class="table-cell">状态</view>
            <view class="table-cell">注册时间</view>
            <view class="table-cell">操作</view>
          </view>
          
          <!-- 数据行 -->
          <view v-if="loading" class="loading-state">
            <text>加载中...</text>
          </view>
          
          <view v-else-if="filteredUsers.length === 0" class="empty-state">
            <text class="empty-icon">👥</text>
            <text class="empty-text">暂无用户数据</text>
          </view>
          
          <view v-else>
            <view 
              v-for="user in paginatedUsers" 
              :key="user.id" 
              class="table-row table-body"
            >
              <view class="table-cell">
                <checkbox 
                  :value="user.id" 
                  :checked="selectedUsers.includes(user.id)"
                  @change="toggleUserSelection(user.id)"
                />
              </view>
              <view class="table-cell user-info">
                <image class="user-avatar" :src="user.avatar" mode="aspectFill"></image>
                <view class="user-details">
                  <text class="username">{{ user.username }}</text>
                  <text class="real-name">{{ user.realName }}</text>
                </view>
              </view>
              <view class="table-cell contact-info">
                <text class="contact-item">{{ user.email }}</text>
                <text class="contact-item">{{ user.phone }}</text>
              </view>
              <view class="table-cell">
                <text class="role-badge" :class="user.role">
                  {{ getRoleText(user.role) }}
                </text>
              </view>
              <view class="table-cell">
                <view class="status-badge" :class="user.status">
                  {{ getStatusText(user.status) }}
                </view>
              </view>
              <view class="table-cell">
                <text class="register-time">{{ user.registerTime }}</text>
              </view>
              <view class="table-cell actions">
                <button class="action-btn edit-btn" @click="editUser(user)">编辑</button>
                <button class="action-btn status-btn" @click="toggleUserStatus(user)">
                  {{ user.status === 'active' ? '禁用' : '启用' }}
                </button>
                <button class="action-btn reset-btn" @click="resetPassword(user)">重置</button>
              </view>
            </view>
          </view>
        </view>
        
        <!-- 分页 -->
        <view v-if="totalPages > 1" class="pagination">
          <button 
            class="page-btn" 
            :disabled="currentPage === 1"
            @click="changePage(currentPage - 1)"
          >
            上一页
          </button>
          
          <view class="page-numbers">
            <button 
              v-for="page in visiblePages" 
              :key="page"
              class="page-number"
              :class="{ 'active': page === currentPage }"
              @click="changePage(page)"
            >
              {{ page }}
            </button>
          </view>
          
          <button 
            class="page-btn" 
            :disabled="currentPage === totalPages"
            @click="changePage(currentPage + 1)"
          >
            下一页
          </button>
        </view>
      </view>
      
      <!-- 批量操作 -->
      <view v-if="selectedUsers.length > 0" class="batch-actions">
        <text class="selected-count">已选择 {{ selectedUsers.length }} 位用户</text>
        <view class="batch-buttons">
          <button class="batch-btn enable-btn" @click="batchEnable">批量启用</button>
          <button class="batch-btn disable-btn" @click="batchDisable">批量禁用</button>
          <button class="batch-btn delete-btn" @click="batchDelete">批量删除</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import Sidebar from '@/components/Sidebar.vue'

export default {
  name: 'UserManagement',
  components: {
    Sidebar
  },
  
  data() {
    return {
      showSidebar: true,
      loading: false,
      searchKeyword: '',
      selectedUsers: [],
      currentPage: 1,
      pageSize: 10,
      users: []
    }
  },
  
  computed: {
    filteredUsers() {
      if (!this.searchKeyword) {
        return this.users
      }
      
      const keyword = this.searchKeyword.toLowerCase()
      return this.users.filter(user => 
        user.username.toLowerCase().includes(keyword) ||
        user.email.toLowerCase().includes(keyword) ||
        user.phone.includes(keyword) ||
        user.realName.toLowerCase().includes(keyword)
      )
    },
    
    totalUsers() {
      return this.filteredUsers.length
    },
    
    totalPages() {
      return Math.ceil(this.totalUsers / this.pageSize)
    },
    
    paginatedUsers() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredUsers.slice(start, end)
    },
    
    allSelected() {
      return this.paginatedUsers.length > 0 && 
             this.paginatedUsers.every(user => this.selectedUsers.includes(user.id))
    },
    
    visiblePages() {
      const pages = []
      const maxVisible = 5
      let start = Math.max(1, this.currentPage - Math.floor(maxVisible / 2))
      let end = Math.min(this.totalPages, start + maxVisible - 1)
      
      if (end - start < maxVisible - 1) {
        start = Math.max(1, end - maxVisible + 1)
      }
      
      for (let i = start; i <= end; i++) {
        pages.push(i)
      }
      
      return pages
    }
  },
  
  onLoad() {
    this.loadUsers()
  },
  
  methods: {
    async loadUsers() {
      this.loading = true
      
      try {
        // 模拟API请求
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        this.users = this.generateMockUsers()
        
      } catch (error) {
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    
    generateMockUsers() {
      const roles = ['user', 'admin', 'reviewer']
      const statuses = ['active', 'inactive']
      const mockUsers = []
      
      for (let i = 1; i <= 50; i++) {
        const role = roles[Math.floor(Math.random() * roles.length)]
        const status = statuses[Math.floor(Math.random() * statuses.length)]
        
        mockUsers.push({
          id: i,
          username: `user${i}`,
          realName: `用户${i}`,
          email: `user${i}@example.com`,
          phone: `138${String(i).padStart(8, '0')}`,
          role,
          status,
          avatar: '/static/default-avatar.png',
          registerTime: `${Math.floor(Math.random() * 365) + 1}天前`,
          college: '计算机学院',
          grade: `202${Math.floor(Math.random() * 4)}级`
        })
      }
      
      return mockUsers
    },
    
    handleSearch() {
      this.currentPage = 1
    },
    
    selectAllUsers(e) {
      if (e.detail.value.length > 0) {
        // 全选当前页
        const pageUserIds = this.paginatedUsers.map(user => user.id)
        this.selectedUsers = [...new Set([...this.selectedUsers, ...pageUserIds])]
      } else {
        // 取消选择当前页
        const pageUserIds = this.paginatedUsers.map(user => user.id)
        this.selectedUsers = this.selectedUsers.filter(id => !pageUserIds.includes(id))
      }
    },
    
    toggleUserSelection(userId) {
      const index = this.selectedUsers.indexOf(userId)
      if (index > -1) {
        this.selectedUsers.splice(index, 1)
      } else {
        this.selectedUsers.push(userId)
      }
    },
    
    changePage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page
      }
    },
    
    getRoleText(role) {
      const roleMap = {
        admin: '管理员',
        reviewer: '审核员',
        user: '普通用户'
      }
      return roleMap[role] || '未知'
    },
    
    getStatusText(status) {
      const statusMap = {
        active: '正常',
        inactive: '已禁用'
      }
      return statusMap[status] || '未知'
    },
    
    showAddUserModal() {
      uni.showToast({
        title: '功能开发中',
        icon: 'none'
      })
    },
    
    editUser(user) {
      uni.showModal({
        title: '编辑用户',
        content: `编辑用户：${user.username}`,
        showCancel: false
      })
    },
    
    toggleUserStatus(user) {
      const action = user.status === 'active' ? '禁用' : '启用'
      uni.showModal({
        title: `确认${action}`,
        content: `确定要${action}用户 ${user.username} 吗？`,
        success: (res) => {
          if (res.confirm) {
            user.status = user.status === 'active' ? 'inactive' : 'active'
            uni.showToast({
              title: `${action}成功`,
              icon: 'success'
            })
          }
        }
      })
    },
    
    resetPassword(user) {
      uni.showModal({
        title: '重置密码',
        content: `确定要重置用户 ${user.username} 的密码吗？`,
        success: (res) => {
          if (res.confirm) {
            uni.showToast({
              title: '密码重置成功',
              icon: 'success'
            })
          }
        }
      })
    },
    
    exportUsers() {
      uni.showToast({
        title: '导出功能开发中',
        icon: 'none'
      })
    },
    
    batchEnable() {
      this.batchOperation('启用', 'active')
    },
    
    batchDisable() {
      this.batchOperation('禁用', 'inactive')
    },
    
    batchDelete() {
      uni.showModal({
        title: '确认删除',
        content: `确定要删除选中的 ${this.selectedUsers.length} 位用户吗？`,
        success: (res) => {
          if (res.confirm) {
            this.users = this.users.filter(user => !this.selectedUsers.includes(user.id))
            this.selectedUsers = []
            uni.showToast({
              title: '删除成功',
              icon: 'success'
            })
          }
        }
      })
    },
    
    batchOperation(action, status) {
      uni.showModal({
        title: `批量${action}`,
        content: `确定要批量${action}选中的 ${this.selectedUsers.length} 位用户吗？`,
        success: (res) => {
          if (res.confirm) {
            this.users.forEach(user => {
              if (this.selectedUsers.includes(user.id)) {
                user.status = status
              }
            })
            this.selectedUsers = []
            uni.showToast({
              title: `${action}成功`,
              icon: 'success'
            })
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.user-management {
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

/* 操作栏 */
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  gap: 20rpx;
  flex-wrap: wrap;
}

.search-section {
  display: flex;
  align-items: center;
  gap: 20rpx;
  flex: 1;
  max-width: 500rpx;
}

.search-input {
  flex: 1;
  height: 60rpx;
  padding: 0 20rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 26rpx;
}

.search-btn {
  width: 60rpx;
  height: 60rpx;
  background: #2196f3;
  color: white;
  border: none;
  border-radius: 8rpx;
  font-size: 26rpx;
}

.action-buttons {
  display: flex;
  gap: 15rpx;
}

.add-btn,
.export-btn {
  padding: 15rpx 25rpx;
  border: none;
  border-radius: 8rpx;
  font-size: 26rpx;
}

.add-btn {
  background: #4caf50;
  color: white;
}

.export-btn {
  background: #ff9800;
  color: white;
}

/* 用户表格 */
.users-table {
  background: white;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  background: #f8f9fa;
  border-bottom: 1rpx solid #e0e0e0;
}

.table-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.user-count {
  font-size: 24rpx;
  color: #666;
}

.table-container {
  min-height: 400rpx;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx;
  text-align: center;
}

.empty-icon {
  font-size: 60rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 26rpx;
  color: #666;
}

.table-row {
  display: grid;
  grid-template-columns: 60rpx 2fr 1.5fr 1fr 1fr 1fr 2fr;
  align-items: center;
  padding: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.table-head {
  background: #fafafa;
  font-weight: 600;
  color: #333;
  font-size: 26rpx;
}

.table-body {
  transition: background 0.3s;
}

.table-body:hover {
  background: #f8f9fa;
}

.table-cell {
  display: flex;
  align-items: center;
  font-size: 24rpx;
  color: #666;
  min-height: 80rpx;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.user-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: #f0f0f0;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.username {
  font-weight: 600;
  color: #333;
}

.real-name {
  font-size: 20rpx;
  color: #999;
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.contact-item {
  font-size: 22rpx;
  color: #666;
}

.role-badge {
  padding: 6rpx 12rpx;
  border-radius: 12rpx;
  font-size: 20rpx;
  font-weight: 500;
  color: white;
}

.role-badge.admin { background: #f44336; }
.role-badge.reviewer { background: #ff9800; }
.role-badge.user { background: #2196f3; }

.status-badge {
  padding: 6rpx 12rpx;
  border-radius: 12rpx;
  font-size: 20rpx;
  font-weight: 500;
  color: white;
}

.status-badge.active { background: #4caf50; }
.status-badge.inactive { background: #999; }

.register-time {
  font-size: 22rpx;
  color: #999;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.action-btn {
  padding: 8rpx 16rpx;
  border: none;
  border-radius: 6rpx;
  font-size: 20rpx;
  text-align: center;
}

.edit-btn {
  background: #2196f3;
  color: white;
}

.status-btn {
  background: #ff9800;
  color: white;
}

.reset-btn {
  background: #9c27b0;
  color: white;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20rpx;
  padding: 30rpx;
  border-top: 1rpx solid #e0e0e0;
}

.page-btn {
  height: 60rpx;
  padding: 0 24rpx;
  background: #f5f5f5;
  color: #666;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 24rpx;
}

.page-btn:disabled {
  opacity: 0.5;
}

.page-numbers {
  display: flex;
  gap: 10rpx;
}

.page-number {
  width: 60rpx;
  height: 60rpx;
  background: #f5f5f5;
  color: #666;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 24rpx;
}

.page-number.active {
  background: #2196f3;
  color: white;
  border-color: #2196f3;
}

/* 批量操作 */
.batch-actions {
  position: fixed;
  bottom: 30rpx;
  left: 50%;
  transform: translateX(-50%);
  background: white;
  padding: 20rpx 30rpx;
  border-radius: 12rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  gap: 30rpx;
  z-index: 100;
}

.selected-count {
  font-size: 26rpx;
  color: #333;
  font-weight: 600;
}

.batch-buttons {
  display: flex;
  gap: 15rpx;
}

.batch-btn {
  padding: 12rpx 24rpx;
  border: none;
  border-radius: 8rpx;
  font-size: 24rpx;
  color: white;
}

.enable-btn { background: #4caf50; }
.disable-btn { background: #ff9800; }
.delete-btn { background: #f44336; }

/* 响应式设计 */
@media (max-width: 1200px) {
  .table-row {
    grid-template-columns: 60rpx 1.5fr 1fr 0.8fr 0.8fr 0.8fr 1.5fr;
  }
}

@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
    padding: 20rpx;
  }
  
  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-section {
    max-width: none;
  }
  
  .table-container {
    overflow-x: auto;
  }
  
  .table-row {
    grid-template-columns: 60rpx 2fr 1.5fr 1fr 1fr 1fr 2fr;
    min-width: 800rpx;
  }
  
  .actions {
    flex-direction: row;
    gap: 5rpx;
  }
  
  .action-btn {
    padding: 6rpx 12rpx;
    font-size: 18rpx;
  }
}
</style>