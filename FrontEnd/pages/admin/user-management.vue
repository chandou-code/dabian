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
          <button class="export-btn" @click="handleExportUsers">导出数据</button>
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
          
          <view v-else-if="users.length === 0" class="empty-state">
            <text class="empty-icon">👥</text>
            <text class="empty-text">暂无用户数据</text>
          </view>
          
          <view v-else>
            <view 
              v-for="user in users" 
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
                <view class="status-badge" :class="user.status === 1 ? 'active' : 'inactive'">
                  {{ getStatusText(user.status) }}
                </view>
              </view>
              <view class="table-cell">
                <text class="register-time">{{ user.registerTime }}</text>
              </view>
              <view class="table-cell actions">
              <button class="action-btn edit-btn" @click="editUser(user)">编辑</button>
              <button class="action-btn status-btn" @click="toggleUserStatus(user)">
                {{ user.status === 1 ? '禁用' : '启用' }}
              </button>
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
    
    <!-- 编辑用户模态框 -->
    <view v-if="showEditModal" class="modal-overlay" @click.self="closeEditModal">
      <view class="modal-container">
        <view class="modal-header">
          <text class="modal-title">编辑用户</text>
          <text class="modal-close" @click="closeEditModal">×</text>
        </view>
        
        <view class="modal-content">
          <view class="form-item">
            <text class="form-label">用户名</text>
            <input 
              v-model="editForm.username" 
              class="form-input" 
              placeholder="请输入用户名"
            />
          </view>
          
          <view class="form-item">
            <text class="form-label">真实姓名</text>
            <input 
              v-model="editForm.realName" 
              class="form-input" 
              placeholder="请输入真实姓名"
            />
          </view>
          
          <view class="form-item">
            <text class="form-label">邮箱</text>
            <input 
              v-model="editForm.email" 
              class="form-input" 
              placeholder="请输入邮箱"
              type="email"
            />
          </view>
          
          <view class="form-item">
            <text class="form-label">手机号</text>
            <input 
              v-model="editForm.phone" 
              class="form-input" 
              placeholder="请输入手机号"
              type="number"
            />
          </view>
          
          <view class="form-item">
            <text class="form-label">学院</text>
            <input 
              v-model="editForm.college" 
              class="form-input" 
              placeholder="请输入学院"
            />
          </view>
          
          <view class="form-item">
            <text class="form-label">年级</text>
            <input 
              v-model="editForm.grade" 
              class="form-input" 
              placeholder="请输入年级（如：2022级）"
            />
          </view>
          
          <view class="form-item">
            <text class="form-label">专业</text>
            <input 
              v-model="editForm.major" 
              class="form-input" 
              placeholder="请输入专业"
            />
          </view>
          
          <view class="form-item">
            <text class="form-label">性别</text>
            <picker 
              v-model="editForm.gender" 
              :range="['男', '女']" 
              class="form-picker"
            >
              <view class="picker-content">{{ editForm.gender === 1 ? '男' : '女' }}</view>
            </picker>
          </view>
          
          <view class="form-item">
            <text class="form-label">角色</text>
            <view class="role-selector">
              <button 
                v-for="roleOption in roleOptions" 
                :key="roleOption.value"
                :class="['role-btn', { 'active': editForm.role === roleOption.value }]"
                @click="editForm.role = roleOption.value"
              >
                {{ roleOption.label }}
              </button>
            </view>
          </view>
        </view>
        
        <view class="modal-footer">
          <button class="btn-cancel" @click="closeEditModal">取消</button>
          <button class="btn-confirm" @click="saveUserChanges" :disabled="isSaving">
            {{ isSaving ? '保存中...' : '保存' }}
          </button>
        </view>
      </view>
    </view>
    
    <!-- 升级为审核员模态框 -->
    <view v-if="showUpgradeModal" class="modal-overlay" @click.self="closeUpgradeModal">
      <view class="modal-container">
        <view class="modal-header">
          <text class="modal-title">升级为审核员</text>
          <text class="modal-close" @click="closeUpgradeModal">×</text>
        </view>
        
        <view class="modal-content">
          <text class="confirm-text">确定要将用户 {{ currentUser.username }} 升级为审核员吗？</text>
        </view>
        
        <view class="modal-footer">
          <button class="btn-cancel" @click="closeUpgradeModal">取消</button>
          <button class="btn-confirm" @click="upgradeUser" :disabled="isUpgrading">
            {{ isUpgrading ? '处理中...' : '确认升级' }}
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import Sidebar from '@/components/Sidebar.vue'
import { getUserList, updateUserStatus, resetUserPassword, batchDeleteUsers, batchUpdateUserStatus, exportUsers as exportUsersApi, updateUserInfo } from '@/api/user'

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
      users: [],
      totalUsers: 0,
      totalPages: 0,
      selectedRole: '',
      selectedStatus: '',
      
      // 编辑用户相关
      showEditModal: false,
      editForm: {
        id: '',
        username: '',
        realName: '',
        email: '',
        phone: '',
        college: '',
        grade: '',
        major: '',
        gender: 1,
        role: 'user',
        status: 1
      },
      isSaving: false,
      // 角色选项
      roleOptions: [
        { value: 'user', label: '普通用户' },
        { value: 'reviewer', label: '审核员' },
        { value: 'admin', label: '管理员' }
      ],
      
      // 升级为审核员相关
      showUpgradeModal: false,
      currentUser: null,
      isUpgrading: false,
      
      // 导出相关
      exportFormat: 'csv'
    }
  },
  
  computed: {
    allSelected() {
      return this.users.length > 0 && 
             this.users.every(user => this.selectedUsers.includes(user.id))
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
        const response = await getUserList({
          current: this.currentPage,
          pageSize: this.pageSize,
          keyword: this.searchKeyword,
          role: this.selectedRole,
          status: this.selectedStatus
        })
        
        if (response.success && response.data) {
          // 处理用户数据，转换字段格式
          this.users = (response.data.users || []).map(user => ({
            ...user,
            registerTime: user.createdAt ? new Date(user.createdAt).toLocaleDateString() : '',
            // 确保status是数字类型
            status: parseInt(user.status) || 0,
            // 确保role是字符串类型
            role: user.role || 'user'
          }))
          this.totalUsers = response.data.pagination.total
          this.totalPages = response.data.pagination.pages
          this.currentPage = response.data.pagination.current
        }
        
      } catch (error) {
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    
    handleSearch() {
      this.currentPage = 1
      this.loadUsers()
    },
    
    selectAllUsers(e) {
      if (e.detail.value.length > 0) {
        // 全选当前页
        const pageUserIds = this.users.map(user => user.id)
        this.selectedUsers = [...new Set([...this.selectedUsers, ...pageUserIds])]
      } else {
        // 取消选择当前页
        const pageUserIds = this.users.map(user => user.id)
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
        'admin': '管理员',
        'reviewer': '审核员',
        'user': '普通用户'
      }
      return roleMap[role] || '未知'
    },
    
    getStatusText(status) {
      const statusMap = {
        1: '正常',
        0: '已禁用'
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
      // 填充编辑表单
      this.editForm = {
        ...user,
        // 确保gender是数字类型
        gender: parseInt(user.gender) || 1
      }
      // 显示模态框
      this.showEditModal = true
    },
    
    closeEditModal() {
      // 隐藏模态框
      this.showEditModal = false
      // 重置表单
      this.resetEditForm()
    },
    
    resetEditForm() {
      this.editForm = {
        id: '',
        username: '',
        realName: '',
        email: '',
        phone: '',
        college: '',
        grade: '',
        major: '',
        gender: 1,
        role: 'user',
        status: 1
      }
      this.isSaving = false
    },
    
    async saveUserChanges() {
      // 基本验证
      if (!this.editForm.username.trim()) {
        uni.showToast({
          title: '用户名不能为空',
          icon: 'none'
        })
        return
      }
      
      if (!this.editForm.email.trim()) {
        uni.showToast({
          title: '邮箱不能为空',
          icon: 'none'
        })
        return
      }
      
      if (!this.editForm.phone.trim()) {
        uni.showToast({
          title: '手机号不能为空',
          icon: 'none'
        })
        return
      }
      
      this.isSaving = true
      
      try {
        // 调用API更新用户信息
        const response = await updateUserInfo(this.editForm.id, this.editForm)
        
        if (response.success) {
          uni.showToast({
            title: '保存成功',
            icon: 'success'
          })
          
          // 关闭模态框
          this.closeEditModal()
          
          // 重新加载用户列表
          this.loadUsers()
        } else {
          uni.showToast({
            title: '保存失败: ' + (response.message || '未知错误'),
            icon: 'none'
          })
        }
      } catch (error) {
        uni.showToast({
          title: '保存失败: ' + error.message,
          icon: 'none'
        })
      } finally {
        this.isSaving = false
      }
    },
    
    toggleUserStatus(user) {
      const action = user.status === 1 ? '禁用' : '启用'
      const newStatus = user.status === 1 ? 0 : 1 // 0: inactive, 1: active
      
      uni.showModal({
        title: `确认${action}`,
        content: `确定要${action}用户 ${user.username} 吗？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              const response = await updateUserStatus(user.id, newStatus)
              if (response.success) {
                // 更新本地用户状态
                user.status = newStatus
                uni.showToast({
                  title: `${action}成功`,
                  icon: 'success'
                })
              } else {
                uni.showToast({
                  title: `${action}失败: ${response.message}`,
                  icon: 'none'
                })
              }
            } catch (error) {
              uni.showToast({
                title: `${action}失败`,
                icon: 'none'
              })
            }
          }
        }
      })
    },
    
    resetPassword(user) {
      uni.showModal({
        title: '重置密码',
        content: `确定要重置用户 ${user.username} 的密码吗？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              const response = await resetUserPassword(user.id)
              if (response.success && response.data) {
                // 显示新密码
                uni.showModal({
                  title: '密码重置成功',
                  content: `新密码：${response.data.newPassword}`,
                  showCancel: false
                })
              } else {
                uni.showToast({
                  title: '密码重置失败: ' + response.message,
                  icon: 'none'
                })
              }
            } catch (error) {
              uni.showToast({
                title: '密码重置失败',
                icon: 'none'
              })
            }
          }
        }
      })
    },
    
    async handleExportUsers() {
      uni.showLoading({
        title: '正在导出...',
        mask: true
      })
      
      try {
        // 1. 获取所有用户数据（为了导出所有数据，不考虑分页）
        console.log('开始获取用户数据...')
        
        // 调用API获取所有用户数据
        const response = await getUserList({
          current: 1,
          pageSize: 10000, // 足够大的数值，确保获取所有用户
          keyword: this.searchKeyword,
          status: this.selectedStatus,
          role: this.selectedRole
        })
        
        if (!response.success || !response.data) {
          throw new Error('获取用户数据失败')
        }
        
        const users = response.data.users || []
        console.log('获取到用户数据:', users.length, '条')
        
        // 2. 将用户数据转换为CSV格式
        const csvContent = this.convertToCSV(users)
        
        // 3. 创建Blob对象
        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
        
        // 4. 生成下载链接并触发下载
        const link = document.createElement('a')
        const url = URL.createObjectURL(blob)
        link.setAttribute('href', url)
        link.setAttribute('download', `用户数据_${new Date().toISOString().slice(0, 10)}.csv`)
        link.style.visibility = 'hidden'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        URL.revokeObjectURL(url)
        
        console.log('导出CSV成功')
        
        // 显示导出成功提示
        uni.showToast({
          title: '导出成功',
          icon: 'success'
        })
        
      } catch (error) {
        console.error('导出失败:', error)
        uni.showToast({
          title: '导出失败: ' + (error.message || '未知错误'),
          icon: 'none'
        })
      } finally {
        // 确保在任何情况下都会隐藏加载提示
        uni.hideLoading()
      }
    },
    
    // 将用户数据转换为CSV格式
    convertToCSV(users) {
      // CSV表头
      const headers = ['用户ID', '用户名', '真实姓名', '邮箱', '手机号', '学院', '年级', '专业', '性别', '角色', '状态', '注册时间']
      
      // 转换用户数据
      const rows = users.map(user => [
        user.id,
        user.username,
        user.realName || '',
        user.email || '',
        user.phone || '',
        user.college || '',
        user.grade || '',
        user.major || '',
        user.gender === 1 ? '男' : '女',
        this.getRoleText(user.role),
        this.getStatusText(user.status),
        user.registerTime || ''
      ])
      
      // 组合表头和数据行
      const csvContent = [
        headers.join(','), // 表头行
        ...rows.map(row => row.join(',')) // 数据行
      ].join('\n')
      
      // 添加BOM以支持Excel正确显示中文
      return '\uFEFF' + csvContent
    },
    
    // Base64转Blob
    base64ToBlob(base64, mimeType) {
      const byteCharacters = atob(base64)
      const byteArrays = []
      
      for (let offset = 0; offset < byteCharacters.length; offset += 512) {
        const slice = byteCharacters.slice(offset, offset + 512)
        const byteNumbers = new Array(slice.length)
        
        for (let i = 0; i < slice.length; i++) {
          byteNumbers[i] = slice.charCodeAt(i)
        }
        
        const byteArray = new Uint8Array(byteNumbers)
        byteArrays.push(byteArray)
      }
      
      return new Blob(byteArrays, { type: mimeType })
    },
    
    async batchEnable() {
      if (this.selectedUsers.length === 0) {
        uni.showToast({
          title: '请先选择用户',
          icon: 'none'
        })
        return
      }
      
      try {
        const response = await batchUpdateUserStatus(this.selectedUsers, 1) // 1: active
        if (response.success) {
          uni.showToast({
            title: '批量启用成功',
            icon: 'success'
          })
          this.selectedUsers = []
          this.loadUsers() // 重新加载数据
        } else {
          uni.showToast({
            title: '批量启用失败: ' + response.message,
            icon: 'none'
          })
        }
      } catch (error) {
        uni.showToast({
          title: '批量启用失败',
          icon: 'none'
        })
      }
    },
    
    async batchDisable() {
      if (this.selectedUsers.length === 0) {
        uni.showToast({
          title: '请先选择用户',
          icon: 'none'
        })
        return
      }
      
      try {
        const response = await batchUpdateUserStatus(this.selectedUsers, 0) // 0: inactive
        if (response.success) {
          uni.showToast({
            title: '批量禁用成功',
            icon: 'success'
          })
          this.selectedUsers = []
          this.loadUsers() // 重新加载数据
        } else {
          uni.showToast({
            title: '批量禁用失败: ' + response.message,
            icon: 'none'
          })
        }
      } catch (error) {
        uni.showToast({
          title: '批量禁用失败',
          icon: 'none'
        })
      }
    },
    
    async batchDelete() {
      if (this.selectedUsers.length === 0) {
        uni.showToast({
          title: '请先选择用户',
          icon: 'none'
        })
        return
      }
      
      uni.showModal({
        title: '确认删除',
        content: `确定要删除选中的 ${this.selectedUsers.length} 位用户吗？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              const response = await batchDeleteUsers(this.selectedUsers)
              if (response.success) {
                uni.showToast({
                  title: '删除成功',
                  icon: 'success'
                })
                this.selectedUsers = []
                this.loadUsers() // 重新加载数据
              } else {
                uni.showToast({
                  title: '删除失败: ' + response.message,
                  icon: 'none'
                })
              }
            } catch (error) {
              uni.showToast({
                title: '删除失败',
                icon: 'none'
              })
            }
          }
        }
      })
    },
    
    // 显示升级为审核员模态框
    showUpgradeModal(user) {
      this.currentUser = user
      this.showUpgradeModal = true
    },
    
    // 关闭升级模态框
    closeUpgradeModal() {
      this.showUpgradeModal = false
      this.currentUser = null
    },
    
    // 升级为审核员
    async upgradeUser() {
      if (!this.currentUser) return
      
      this.isUpgrading = true
      
      try {
        // 调用API将用户升级为审核员
        // 这里需要替换为实际的升级API
        // 模拟API调用成功
        uni.showToast({
          title: '升级成功',
          icon: 'success'
        })
        
        // 更新本地用户角色
        this.currentUser.role = 'reviewer'
        
        // 关闭模态框
        this.closeUpgradeModal()
        
        // 重新加载用户列表
        this.loadUsers()
      } catch (error) {
        uni.showToast({
          title: '升级失败: ' + error.message,
          icon: 'none'
        })
      } finally {
        this.isUpgrading = false
      }
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
  width: 80rpx;
  height: 60rpx;
  background: #2196f3;
  color: white;
  border: none;
  border-radius: 8rpx;
  font-size: 26rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
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

/* 模态框遮罩层 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

/* 模态框样式 */
.modal-container {
  background: white;
  border-radius: 16rpx;
  width: 90%;
  max-width: 600rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.15);
  animation: modalFadeIn 0.3s ease-out;
  position: relative;
  z-index: 1001;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.modal-close {
  font-size: 48rpx;
  color: #999;
  cursor: pointer;
  line-height: 1;
}

.modal-content {
  padding: 32rpx;
}

.form-item {
  margin-bottom: 32rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 12rpx;
}

.form-input, .form-picker {
  width: 100%;
  height: 76rpx;
  padding: 0 20rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.picker-content {
  line-height: 72rpx;
  color: #333;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 20rpx;
  padding: 24rpx 32rpx;
  border-top: 2rpx solid #f0f0f0;
}

.btn-cancel, .btn-confirm {
  height: 72rpx;
  padding: 0 40rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: 500;
}

.btn-cancel {
  background: white;
  color: #666;
  border: 2rpx solid #e0e0e0;
}

.btn-confirm {
  background: #2196f3;
  color: white;
  border: none;
}

.btn-confirm:disabled {
  opacity: 0.5;
}

/* 模态框动画 */
@keyframes modalFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 角色选择器样式 */
.role-selector {
  display: flex;
  gap: 10rpx;
  flex-wrap: wrap;
  margin-top: 10rpx;
}

.role-btn {
  flex: 1;
  min-width: 120rpx;
  height: 60rpx;
  background: #f5f5f5;
  color: #666;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  font-size: 24rpx;
  transition: all 0.3s ease;
}

.role-btn.active {
  background: #2196f3;
  color: white;
  border-color: #2196f3;
}

.role-btn:hover {
  background: #e3f2fd;
  border-color: #2196f3;
}

.role-btn.active:hover {
  background: #1976d2;
}

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