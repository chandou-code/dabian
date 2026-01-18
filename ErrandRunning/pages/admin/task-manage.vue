<template>
  <view class="admin-task-container">
    <view class="admin-header">
      <text class="admin-title">任务管理</text>
      <text class="admin-subtitle">管理平台所有任务，包括发布、取消和删除</text>
    </view>

    <view class="filter-section">
      <view class="filter-row">
        <view class="filter-item">
          <text class="filter-label">状态筛选：</text>
          <uni-picker-view 
            class="status-filter"
            :value="filterStatusIndex"
            @change="handleStatusChange"
          >
            <uni-picker-view-column>
              <text v-for="status in statusOptions" :key="status.value">{{ status.label }}</text>
            </uni-picker-view-column>
          </uni-picker-view>
        </view>
        <view class="filter-item">
          <text class="filter-label">类型筛选：</text>
          <uni-picker-view 
            class="type-filter"
            :value="filterTypeIndex"
            @change="handleTypeChange"
          >
            <uni-picker-view-column>
              <text v-for="type in typeOptions" :key="type.value">{{ type.label }}</text>
            </uni-picker-view-column>
          </uni-picker-view>
        </view>
      </view>
      <uni-search-bar 
        v-model="searchKeyword" 
        class="search-bar"
        placeholder="搜索任务标题或发布者"
        @confirm="handleSearch"
      ></uni-search-bar>
    </view>

    <view class="stats-section">
      <view class="stat-card">
        <text class="stat-value">{{ totalTasks }}</text>
        <text class="stat-label">总任务数</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ pendingTasks }}</text>
        <text class="stat-label">待审核</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ activeTasks }}</text>
        <text class="stat-label">进行中</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ completedTasks }}</text>
        <text class="stat-label">已完成</text>
      </view>
    </view>

    <view class="task-list">
      <view 
        v-for="task in tasks" 
        :key="task.id"
        class="task-item"
        :class="getTaskStatusClass(task.status)"
      >
        <view class="task-header">
          <text class="task-title">{{ task.title }}</text>
          <text class="task-status" :class="task.status">
            {{ getStatusLabel(task.status) }}
          </text>
        </view>
        
        <view class="task-info">
          <view class="info-row">
            <text class="info-label">发布者：</text>
            <text class="info-value">{{ task.publisherName }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">类型：</text>
            <text class="info-value">{{ getTypeLabel(task.type) }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">金额：</text>
            <text class="info-value price">¥{{ task.price }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">地点：</text>
            <text class="info-value">{{ task.location }}</text>
          </view>
          <view class="info-row">
            <text class="info-label">发布时间：</text>
            <text class="info-value">{{ formatDate(task.publishTime) }}</text>
          </view>
          <view class="info-row" v-if="task.runnerName">
            <text class="info-label">跑腿员：</text>
            <text class="info-value">{{ task.runnerName }}</text>
          </view>
        </view>
        
        <view class="task-actions">
          <button class="btn btn-primary" @click="viewTaskDetail(task)">查看详情</button>
          <button class="btn btn-warning" v-if="task.status !== 'cancelled'" @click="cancelTask(task)">取消任务</button>
          <button class="btn btn-danger" @click="deleteTask(task)">删除</button>
        </view>
      </view>

      <view v-if="tasks.length === 0" class="empty-state">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无任务数据</text>
      </view>
    </view>

    <uni-load-more 
      v-if="hasMore" 
      :status="loadStatus" 
      @clickLoadMore="loadMore"
    ></uni-load-more>
  </view>
</template>

<script>
import { get, post, del } from '../../api/request'

export default {
  data() {
    return {
      tasks: [],
      statusOptions: [
        { label: '全部状态', value: '' },
        { label: '待接单', value: 'pending' },
        { label: '进行中', value: 'in_progress' },
        { label: '已完成', value: 'completed' },
        { label: '已取消', value: 'cancelled' }
      ],
      typeOptions: [
        { label: '全部类型', value: '' },
        { label: '快递代取', value: 'express' },
        { label: '外卖代送', value: 'food' },
        { label: '物品购买', value: 'shopping' },
        { label: '其他服务', value: 'other' }
      ],
      filterStatus: '',
      filterStatusIndex: [0],
      filterType: '',
      filterTypeIndex: [0],
      searchKeyword: '',
      loadStatus: 'more',
      hasMore: true,
      currentPage: 1,
      pageSize: 10,
      loading: false,
      totalTasks: 0,
      pendingTasks: 0,
      activeTasks: 0,
      completedTasks: 0
    }
  },

  onLoad() {
    this.loadTaskStats()
    this.loadTasks()
  },

  methods: {
    async loadTaskStats() {
      try {
        const response = await get('/admin/tasks/stats')
        if (response.code === 200 && response.data) {
          this.totalTasks = response.data.total || 0
          this.pendingTasks = response.data.pending || 0
          this.activeTasks = response.data.in_progress || 0
          this.completedTasks = response.data.completed || 0
        }
      } catch (error) {
        console.error('获取任务统计失败:', error)
        uni.showToast({
          title: '获取统计数据失败',
          icon: 'none'
        })
      }
    },

    async loadTasks(refresh = false) {
      if (this.loading) return
      
      this.loading = true
      if (refresh) {
        this.currentPage = 1
        this.tasks = []
      }
      
      try {
        // 构建请求参数
        const params = {
          status: this.filterStatus || null,
          type: this.filterType || null,
          keyword: this.searchKeyword || null,
          page: this.currentPage,
          pageSize: this.pageSize
        }
        
        // 调用API获取任务列表
        const response = await get('/admin/tasks/list', params)
        
        if (response.code === 200 && response.data) {
          const { list, total } = response.data
          
          if (refresh) {
            this.tasks = list
          } else {
            this.tasks = [...this.tasks, ...list]
          }
          
          // 更新加载状态
          this.hasMore = this.tasks.length < total
          this.loadStatus = this.hasMore ? 'more' : 'noMore'
        } else {
          uni.showToast({
            title: response.msg || '获取任务列表失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('获取任务列表失败:', error)
        uni.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },

    handleStatusChange(e) {
      const index = e.detail.value[0]
      this.filterStatus = this.statusOptions[index].value
      this.filterStatusIndex = [index]
      this.loadTasks(true)
    },

    handleTypeChange(e) {
      const index = e.detail.value[0]
      this.filterType = this.typeOptions[index].value
      this.filterTypeIndex = [index]
      this.loadTasks(true)
    },

    handleSearch() {
      this.loadTasks(true)
    },

    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    },

    getStatusLabel(status) {
      const statusMap = {
        pending: '待接单',
        in_progress: '进行中',
        completed: '已完成',
        cancelled: '已取消'
      }
      return statusMap[status] || status
    },

    getTypeLabel(type) {
      const typeMap = {
        delivery: '快递代取',
        express: '快递代取',
        food: '外卖代送',
        shopping: '物品购买',
        other: '其他服务'
      }
      return typeMap[type] || type
    },

    getTaskStatusClass(status) {
      return status
    },

    viewTaskDetail(task) {
      uni.navigateTo({
        url: `/pages/task/detail?id=${task.id}&from=admin`
      })
    },



    async cancelTask(task) {
      uni.showModal({
        title: '取消任务',
        content: `确定要取消任务 "${task.title}" 吗？`,
        success: async (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '处理中...' })
            try {
              const response = await post(`/admin/tasks/${task.id}/cancel`)
              
              if (response.code === 200) {
                task.status = 'cancelled'
                uni.showToast({
                  title: '取消成功',
                  icon: 'success'
                })
                this.loadTaskStats() // 更新统计数据
              } else {
                uni.showToast({
                  title: response.msg || '取消失败',
                  icon: 'none'
                })
              }
            } catch (error) {
              console.error('取消任务失败:', error)
              uni.showToast({
                title: '网络错误，请稍后重试',
                icon: 'none'
              })
            } finally {
              uni.hideLoading()
            }
          }
        }
      })
    },

    async deleteTask(task) {
      uni.showModal({
        title: '删除任务',
        content: `确定要删除任务 "${task.title}" 吗？此操作不可恢复。`,
        confirmColor: '#f44336',
        success: async (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '处理中...' })
            try {
              const response = await del(`/admin/tasks/${task.id}`)
              
              if (response.code === 200) {
                // 从列表中移除该任务
                const index = this.tasks.findIndex(item => item.id === task.id)
                if (index !== -1) {
                  this.tasks.splice(index, 1)
                }
                uni.showToast({
                  title: '删除成功',
                  icon: 'success'
                })
                this.loadTaskStats() // 更新统计数据
              } else {
                uni.showToast({
                  title: response.msg || '删除失败',
                  icon: 'none'
                })
              }
            } catch (error) {
              console.error('删除任务失败:', error)
              uni.showToast({
                title: '网络错误，请稍后重试',
                icon: 'none'
              })
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
        this.loadTasks()
      }
    }
  }
}
</script>

<style scoped lang="scss">
.admin-task-container {
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

.filter-section {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);

  .filter-row {
    display: flex;
    gap: 20rpx;
    align-items: center;
    flex-wrap: wrap;
  }

  .filter-item {
    display: flex;
    align-items: center;
    gap: 12rpx;
    margin-bottom: 20rpx;
  }

  .filter-label {
    font-size: 26rpx;
    color: #666;
  }

  .status-filter,
  .type-filter {
    width: 200rpx;
    height: 70rpx;
  }

  .search-bar {
    margin-top: 20rpx;
  }
}

.stats-section {
  display: flex;
  gap: 20rpx;
  margin-bottom: 24rpx;

  .stat-card {
    flex: 1;
    background: #fff;
    border-radius: 12rpx;
    padding: 24rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
    text-align: center;

    .stat-value {
      display: block;
      font-size: 48rpx;
      font-weight: 600;
      color: #007aff;
      margin-bottom: 8rpx;
    }

    .stat-label {
      font-size: 24rpx;
      color: #666;
    }
  }
}

.task-list {
  background: #fff;
  border-radius: 12rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  margin-bottom: 40rpx;
  overflow: hidden;

  .task-item {
    padding: 24rpx;
    border-bottom: 1rpx solid #e9ecef;
    transition: all 0.2s ease;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background-color: #f8f9fa;
    }

    &.pending {
      border-left: 8rpx solid #ffc107;
    }

    &.in_progress {
      border-left: 8rpx solid #007aff;
    }

    &.completed {
      border-left: 8rpx solid #28a745;
    }

    &.cancelled {
      border-left: 8rpx solid #dc3545;
      opacity: 0.7;
    }

    .task-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 20rpx;

      .task-title {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
        flex: 1;
        margin-right: 20rpx;
      }

      .task-status {
        padding: 6rpx 16rpx;
        border-radius: 20rpx;
        font-size: 22rpx;
        font-weight: 500;

        &.pending {
          background: #fff3cd;
          color: #856404;
        }

        &.in_progress {
          background: #cce5ff;
          color: #004085;
        }

        &.completed {
          background: #d4edda;
          color: #155724;
        }

        &.cancelled {
          background: #f8d7da;
          color: #721c24;
        }
      }
    }

    .task-info {
      margin-bottom: 24rpx;

      .info-row {
        display: flex;
        margin-bottom: 12rpx;

        .info-label {
          font-size: 24rpx;
          color: #666;
          width: 120rpx;
          flex-shrink: 0;
        }

        .info-value {
          flex: 1;
          font-size: 24rpx;
          color: #333;
        }

        .price {
          color: #ff6b6b;
          font-weight: 600;
        }
      }
    }

    .task-actions {
      display: flex;
      gap: 12rpx;
      justify-content: flex-end;
      flex-wrap: wrap;

      .btn {
        padding: 12rpx 20rpx;
        border-radius: 8rpx;
        font-size: 24rpx;
        font-weight: 500;
        border: none;
        transition: all 0.2s ease;

        &.btn-primary {
          background: #1976d2;
          color: #fff;
        }

        &.btn-success {
          background: #388e3c;
          color: #fff;
        }

        &.btn-warning {
          background: #f57c00;
          color: #fff;
        }

        &.btn-danger {
          background: #d32f2f;
          color: #fff;
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
</style>