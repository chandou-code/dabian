<template>
  <view class="reviewer-management">
    <Sidebar />
    
    <view class="main-content" :class="{ 'main-content-expanded': !showSidebar }">
      <!-- 操作栏 -->
      <view class="action-bar">
        <view class="search-section">
          <input 
            v-model="searchKeyword" 
            class="search-input" 
            placeholder="搜索审核员姓名或用户名..."
            @confirm="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">🔍</button>
        </view>
      </view>
      
      <!-- 审核员列表 -->
      <view class="reviewers-grid">
        <view v-if="loading" class="loading-state">
          <text>加载中...</text>
        </view>
        
        <view v-else-if="filteredReviewers.length === 0" class="empty-state">
          <text class="empty-icon">👨‍💼</text>
          <text class="empty-text">暂无审核员数据</text>
        </view>
        
        <view v-else class="reviewer-cards">
          <view 
            v-for="reviewer in paginatedReviewers" 
            :key="reviewer.id" 
            class="reviewer-card"
          >
            <view class="card-header">
              <image class="reviewer-avatar" :src="reviewer.avatar" mode="aspectFill"></image>
              <view class="reviewer-info">
                <text class="reviewer-name">{{ reviewer.name }}</text>
                <text class="reviewer-username">@{{ reviewer.username }}</text>
              </view>
              <view class="status-badge" :class="reviewer.status">
                {{ getStatusText(reviewer.status) }}
              </view>
            </view>
            
            <view class="card-content">
              <view class="info-row">
                <text class="info-label">邮箱：</text>
                <text class="info-value">{{ reviewer.email }}</text>
              </view>
              <view class="info-row">
                <text class="info-label">手机：</text>
                <text class="info-value">{{ reviewer.phone }}</text>
              </view>
              <view class="info-row">
                <text class="info-label">入职时间：</text>
                <text class="info-value">{{ reviewer.joinTime }}</text>
              </view>
            </view>
            
            <view class="stats-section">
              <text class="stats-title">工作统计</text>
              <view class="stats-grid">
                <view class="stat-item">
                  <text class="stat-number">{{ reviewer.totalReviewed }}</text>
                  <text class="stat-label">总审核数</text>
                </view>
                <view class="stat-item">
                  <text class="stat-number">{{ reviewer.approved }}</text>
                  <text class="stat-label">通过数</text>
                </view>
                <view class="stat-item">
                  <text class="stat-number">{{ reviewer.rejected }}</text>
                  <text class="stat-label">驳回数</text>
                </view>
                <view class="stat-item">
                  <text class="stat-number">{{ reviewer.approvalRate }}%</text>
                  <text class="stat-label">通过率</text>
                </view>
              </view>
            </view>
            
            <view class="card-actions">
              <button class="action-btn edit-btn" @click="editReviewer(reviewer)">编辑</button>
              <button class="action-btn toggle-btn" @click="toggleReviewerStatus(reviewer)">
                {{ reviewer.status === 'active' ? '禁用' : '启用' }}
              </button>
              <button class="action-btn view-btn" @click="viewReviewerDetail(reviewer)">详情</button>
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
    
    <!-- 编辑审核员模态框 -->
    <view v-if="showEditModal" class="modal-overlay" @click.self="closeEditModal">
      <view class="modal-container">
        <view class="modal-header">
          <text class="modal-title">编辑审核员</text>
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
          
          <view class="form-item">
            <text class="form-label">状态</text>
            <picker 
              v-model="editForm.status" 
              :range="['active', 'inactive']" 
              class="form-picker"
            >
              <view class="picker-content">{{ getStatusText(editForm.status) }}</view>
            </picker>
          </view>
        </view>
        
        <view class="modal-footer">
          <button class="btn-cancel" @click="closeEditModal">取消</button>
          <button class="btn-confirm" @click="saveReviewerChanges" :disabled="isSaving">
            {{ isSaving ? '保存中...' : '保存' }}
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import Sidebar from '@/components/Sidebar.vue'
import { getReviewers } from '@/api/system.js'
import { updateUserInfo } from '@/api/user.js'

export default {
  name: 'ReviewerManagement',
  components: {
    Sidebar
  },
  
  data() {
    return {
      showSidebar: true,
      loading: false,
      searchKeyword: '',
      currentPage: 1,
      pageSize: 6,
      reviewers: [],
      
      // 编辑审核员相关
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
        role: 'reviewer',
        status: 'active'
      },
      isSaving: false,
      // 角色选项
      roleOptions: [
        { value: 'user', label: '普通用户' },
        { value: 'reviewer', label: '审核员' },
        { value: 'admin', label: '管理员' }
      ]
    }
  },
  
  computed: {
    filteredReviewers() {
      if (!this.searchKeyword) {
        return this.reviewers
      }
      
      const keyword = this.searchKeyword.toLowerCase()
      return this.reviewers.filter(reviewer => 
        reviewer.name.toLowerCase().includes(keyword) ||
        reviewer.username.toLowerCase().includes(keyword) ||
        reviewer.email.toLowerCase().includes(keyword)
      )
    },
    
    totalPages() {
      return Math.ceil(this.filteredReviewers.length / this.pageSize)
    },
    
    paginatedReviewers() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredReviewers.slice(start, end)
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
    this.loadReviewers()
  },
  
  methods: {
    async loadReviewers() {
      this.loading = true
      
      try {
        const response = await getReviewers()
        // 后端返回的数据结构是 { data: { list: [], total: number } }
        const reviewersData = response.data || { list: [], total: 0 }
        // 将后端返回的realName映射为前端需要的name，status转换为字符串格式
        this.reviewers = reviewersData.list.map(item => ({
          ...item,
          name: item.realName || item.name,
          status: item.status === 1 ? 'active' : 'inactive',
          joinTime: item.createdAt ? new Date(item.createdAt).toLocaleDateString() : '未知',
          // 初始化统计数据，后续可以从审核历史中计算
          totalReviewed: item.totalReviewed || 0,
          approved: item.approved || 0,
          rejected: item.rejected || 0,
          approvalRate: item.approvalRate || 0
        }))
        
      } catch (error) {
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
        console.error('加载审核员列表失败:', error)
      } finally {
        this.loading = false
      }
    },
    
    handleSearch() {
      this.currentPage = 1
    },
    
    changePage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page
      }
    },
    
    getStatusText(status) {
      const statusMap = {
        active: '正常',
        inactive: '已禁用'
      }
      return statusMap[status] || '未知'
    },
    
    getRoleText(role) {
      const roleMap = {
        admin: '管理员',
        reviewer: '审核员',
        user: '普通用户'
      }
      return roleMap[role] || '未知'
    },
    
    editReviewer(reviewer) {
      // 填充编辑表单
      this.editForm = {
        ...reviewer,
        // 确保gender是数字类型
        gender: parseInt(reviewer.gender) || 1,
        // 确保role是字符串类型
        role: reviewer.role || 'reviewer'
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
        role: 'reviewer',
        status: 'active'
      }
      this.isSaving = false
    },
    
    async saveReviewerChanges() {
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
      
      this.isSaving = true
      
      try {
        // 调用API更新审核员信息
        const response = await updateUserInfo(this.editForm.id, this.editForm)
        
        if (response.success) {
          uni.showToast({
            title: '保存成功',
            icon: 'success'
          })
          
          // 关闭模态框
          this.closeEditModal()
          
          // 重新加载审核员列表
          this.loadReviewers()
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
    
    toggleReviewerStatus(reviewer) {
      const action = reviewer.status === 'active' ? '禁用' : '启用'
      const newStatus = reviewer.status === 'active' ? 'inactive' : 'active'
      
      uni.showModal({
        title: `确认${action}`,
        content: `确定要${action}审核员 ${reviewer.name} 吗？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              // 调用API更新审核员状态
              // 这里需要替换为实际的更新审核员状态API
              // 更新本地审核员状态
              reviewer.status = newStatus
              uni.showToast({
                title: `${action}成功`,
                icon: 'success'
              })
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
    
    viewReviewerDetail(reviewer) {
      uni.navigateTo({ 
        url: `/pages/admin/reviewer-detail?id=${reviewer.id}` 
      })
    }
  }
}
</script>

<style scoped>
.reviewer-management {
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
  margin-bottom: 30rpx;
  gap: 20rpx;
  flex-wrap: wrap;
}

.search-section {
  display: flex;
  align-items: center;
  gap: 20rpx;
  flex: 1;
  max-width: 400rpx;
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

.add-btn {
  background: #4caf50;
  color: white;
  border: none;
  padding: 15rpx 30rpx;
  border-radius: 8rpx;
  font-size: 26rpx;
}

/* 审核员网格 */
.reviewers-grid {
  min-height: 400rpx;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx;
  background: white;
  border-radius: 12rpx;
  text-align: center;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #666;
}

.reviewer-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400rpx, 1fr));
  gap: 30rpx;
}

.reviewer-card {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
}

.reviewer-card:hover {
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
}

/* 卡片头部 */
.card-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 25rpx;
}

.reviewer-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #f0f0f0;
}

.reviewer-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.reviewer-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.reviewer-username {
  font-size: 24rpx;
  color: #999;
}

.status-badge {
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: 500;
  color: white;
}

.status-badge.active { background: #4caf50; }
.status-badge.inactive { background: #999; }

/* 卡片内容 */
.card-content {
  margin-bottom: 25rpx;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 10rpx;
}

.info-label {
  font-size: 24rpx;
  color: #666;
  min-width: 120rpx;
}

.info-value {
  font-size: 24rpx;
  color: #333;
  flex: 1;
}

/* 统计区域 */
.stats-section {
  background: #f8f9fa;
  padding: 20rpx;
  border-radius: 12rpx;
  margin-bottom: 25rpx;
}

.stats-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 15rpx;
  display: block;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15rpx;
}

.stat-item {
  text-align: center;
  padding: 15rpx 10rpx;
  background: white;
  border-radius: 8rpx;
}

.stat-number {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #2196f3;
  margin-bottom: 5rpx;
}

.stat-label {
  font-size: 20rpx;
  color: #666;
}

/* 卡片操作 */
.card-actions {
  display: flex;
  gap: 15rpx;
}

.action-btn {
  flex: 1;
  height: 60rpx;
  border: none;
  border-radius: 8rpx;
  font-size: 24rpx;
  text-align: center;
}

.edit-btn {
  background: #2196f3;
  color: white;
}

.toggle-btn {
  background: #ff9800;
  color: white;
}

.view-btn {
  background: #f5f5f5;
  color: #666;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20rpx;
  margin-top: 40rpx;
  padding: 30rpx;
  background: white;
  border-radius: 12rpx;
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

/* 降级按钮样式 */
.demote-btn {
  background: #f44336;
  color: white;
}

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

/* 模态框头部 */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

/* 模态框标题 */
.modal-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

/* 模态框关闭按钮 */
.modal-close {
  font-size: 48rpx;
  color: #999;
  cursor: pointer;
  line-height: 1;
}

/* 模态框内容 */
.modal-content {
  padding: 32rpx;
}

/* 确认文本 */
.confirm-text {
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
  text-align: center;
}

/* 表单项 */
.form-item {
  margin-bottom: 32rpx;
}

/* 表单标签 */
.form-label {
  display: block;
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 12rpx;
}

/* 表单输入框 */
.form-input, .form-picker {
  width: 100%;
  height: 76rpx;
  padding: 0 20rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

/* 选择器内容 */
.picker-content {
  line-height: 72rpx;
  color: #333;
}

/* 模态框底部 */
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 20rpx;
  padding: 24rpx 32rpx;
  border-top: 2rpx solid #f0f0f0;
}

/* 取消按钮 */
.btn-cancel {
  background: white;
  color: #666;
  border: 2rpx solid #e0e0e0;
  height: 72rpx;
  padding: 0 40rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: 500;
}

/* 确认按钮 */
.btn-confirm {
  background: #2196f3;
  color: white;
  border: none;
  height: 72rpx;
  padding: 0 40rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: 500;
}

/* 禁用状态 */
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
  
  .reviewer-cards {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .card-actions {
    flex-direction: column;
  }
}
</style>