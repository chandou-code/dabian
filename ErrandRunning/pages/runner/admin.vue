<template>
  <view class="admin-container">
    <view class="admin-header">
      <text class="admin-title">跑腿员申请管理</text>
      <text class="admin-subtitle">管理所有跑腿员申请，进行审核操作</text>
    </view>

    <view class="filter-section">
      <uni-picker-view 
        class="status-filter"
        :value="filterStatusIndex"
        @change="handleStatusChange"
      >
        <uni-picker-view-column>
          <text v-for="status in statusOptions" :key="status.value">{{ status.label }}</text>
        </uni-picker-view-column>
      </uni-picker-view>
      <uni-search-bar 
        v-model="searchKeyword" 
        class="search-bar"
        placeholder="搜索用户名、姓名或手机号"
        @confirm="handleSearch"
      ></uni-search-bar>
    </view>

    <view class="application-list">
      <view 
        v-for="app in applications" 
        :key="app.id"
        class="application-item"
        :class="{ 'pending': app.status === 'pending', 'approved': app.status === 'approved', 'rejected': app.status === 'rejected' }"
      >
        <view class="app-header">
          <view class="app-info">
            <text class="app-user">{{ app.userInfo.nickname }}</text>
            <text class="app-time">{{ formatDate(app.applyTime) }}</text>
          </view>
          <view class="app-status" :class="app.status">
            {{ getStatusText(app.status) }}
          </view>
        </view>

        <view class="app-details">
          <view class="detail-item">
            <text class="detail-label">真实姓名：</text>
            <text class="detail-value">{{ app.userInfo.realName }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">手机号码：</text>
            <text class="detail-value">{{ app.userInfo.phone }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">学院年级：</text>
            <text class="detail-value">{{ app.userInfo.college }} {{ app.userInfo.grade }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">服务范围：</text>
            <text class="detail-value">{{ app.serviceArea }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">工作时间：</text>
            <text class="detail-value">{{ app.workTime }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">服务标签：</text>
            <view class="tag-list">
              <text 
                v-for="(tag, index) in app.serviceTags" 
                :key="index"
                class="tag"
              >
                {{ tag }}
              </text>
            </view>
          </view>
        </view>

        <view v-if="app.status === 'pending'" class="app-actions">
          <uni-button class="btn btn-approve" @click="approveApplication(app)">
            批准
          </uni-button>
          <uni-button class="btn btn-reject" @click="rejectApplication(app)">
            拒绝
          </uni-button>
        </view>

        <view v-else class="app-review-info">
          <view class="review-item">
            <text class="review-label">审核人：</text>
            <text class="review-value">{{ app.reviewerName || '系统' }}</text>
          </view>
          <view class="review-item">
            <text class="review-label">审核时间：</text>
            <text class="review-value">{{ formatDate(app.reviewTime) }}</text>
          </view>
          <view v-if="app.reviewComment" class="review-item">
            <text class="review-label">审核意见：</text>
            <text class="review-value">{{ app.reviewComment }}</text>
          </view>
        </view>
      </view>

      <view v-if="applications.length === 0" class="empty-state">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无申请记录</text>
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
import { getRunnerApplications, approveRunnerApplication, rejectRunnerApplication } from '../../api/errand'

export default {
  data() {
    return {
      applications: [],
      statusOptions: [
        { label: '全部状态', value: 'all' },
        { label: '待审核', value: 'pending' },
        { label: '已批准', value: 'approved' },
        { label: '已拒绝', value: 'rejected' }
      ],
      filterStatus: 'all',
      filterStatusIndex: [0],
      searchKeyword: '',
      loadStatus: 'more',
      hasMore: true,
      currentPage: 1,
      pageSize: 10,
      loading: false
    }
  },

  onLoad() {
    this.loadApplications()
  },

  methods: {
    async loadApplications(refresh = false) {
      if (this.loading) return
      
      this.loading = true
      if (refresh) {
        this.currentPage = 1
        this.applications = []
      }
      
      try {
        // 构建请求参数
        const params = {
          status: this.filterStatus === 'all' ? null : this.filterStatus,
          page: this.currentPage,
          pageSize: this.pageSize,
          keyword: this.searchKeyword
        }
        
        // 调用真实API获取申请列表
        const response = await getRunnerApplications(params)
        
        if (response.code === 200 && response.data) {
          const { list, total } = response.data
          
          if (refresh) {
            this.applications = list
          } else {
            this.applications = [...this.applications, ...list]
          }
          
          // 更新加载状态
          this.hasMore = this.applications.length < total
          this.loadStatus = this.hasMore ? 'more' : 'noMore'
        } else {
          uni.showToast({
            title: response.msg || '获取申请列表失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('获取申请列表失败:', error)
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
      this.loadApplications(true)
    },

    handleSearch() {
      this.loadApplications(true)
    },

    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    },

    getStatusText(status) {
      const statusMap = {
        pending: '待审核',
        approved: '已批准',
        rejected: '已拒绝'
      }
      return statusMap[status] || status
    },

    async approveApplication(app) {
      uni.showModal({
        title: '批准申请',
        content: `确定要批准 ${app.userInfo.nickname} 的跑腿员申请吗？`,
        success: async (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '处理中...' })
            try {
              // 调用真实API批准申请
              const response = await approveRunnerApplication(app.id, '符合条件，批准成为跑腿员')
              
              if (response.code === 200) {
                // 更新本地数据
                const index = this.applications.findIndex(item => item.id === app.id)
                if (index !== -1) {
                  this.applications[index].status = 'approved'
                  this.applications[index].reviewTime = new Date().toISOString()
                  this.applications[index].reviewerName = '当前管理员'
                  this.applications[index].reviewComment = '符合条件，批准成为跑腿员'
                }
                
                uni.showToast({
                  title: '批准成功',
                  icon: 'success'
                })
              } else {
                uni.showToast({
                  title: response.msg || '批准失败',
                  icon: 'none'
                })
              }
            } catch (error) {
              console.error('批准申请失败:', error)
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

    async rejectApplication(app) {
      uni.showModal({
        title: '拒绝申请',
        content: `确定要拒绝 ${app.userInfo.nickname} 的跑腿员申请吗？`,
        success: (res) => {
          if (res.confirm) {
            uni.showInput({
              title: '拒绝原因',
              placeholder: '请输入拒绝原因',
              success: async (inputRes) => {
                if (inputRes.value) {
                  uni.showLoading({ title: '处理中...' })
                  try {
                    // 调用真实API拒绝申请
                    const response = await rejectRunnerApplication(app.id, inputRes.value)
                    
                    if (response.code === 200) {
                      // 更新本地数据
                      const index = this.applications.findIndex(item => item.id === app.id)
                      if (index !== -1) {
                        this.applications[index].status = 'rejected'
                        this.applications[index].reviewTime = new Date().toISOString()
                        this.applications[index].reviewerName = '当前管理员'
                        this.applications[index].reviewComment = inputRes.value
                      }
                      
                      uni.showToast({
                        title: '拒绝成功',
                        icon: 'success'
                      })
                    } else {
                      uni.showToast({
                        title: response.msg || '拒绝失败',
                        icon: 'none'
                      })
                    }
                  } catch (error) {
                    console.error('拒绝申请失败:', error)
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
          }
        }
      })
    },

    loadMore() {
      if (this.loadStatus === 'more' && !this.loading) {
        this.currentPage++
        this.loadApplications()
      }
    }
  }
}
</script>

<style scoped lang="scss">
.admin-container {
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

  .status-filter {
    margin-bottom: 20rpx;
  }

  .search-bar {
    margin-top: 20rpx;
  }
}

.application-list {
  margin-bottom: 40rpx;

  .application-item {
    background: #fff;
    border-radius: 12rpx;
    padding: 24rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;

    &.pending {
      border-left: 8rpx solid #ffc107;
    }

    &.approved {
      border-left: 8rpx solid #28a745;
    }

    &.rejected {
      border-left: 8rpx solid #dc3545;
    }

    .app-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20rpx;

      .app-info {
        .app-user {
          display: block;
          font-size: 28rpx;
          font-weight: 600;
          color: #333;
          margin-bottom: 4rpx;
        }

        .app-time {
          font-size: 22rpx;
          color: #999;
        }
      }

      .app-status {
        padding: 6rpx 16rpx;
        border-radius: 20rpx;
        font-size: 22rpx;
        font-weight: 500;

        &.pending {
          background: #fff3cd;
          color: #856404;
        }

        &.approved {
          background: #d4edda;
          color: #155724;
        }

        &.rejected {
          background: #f8d7da;
          color: #721c24;
        }
      }
    }

    .app-details {
      margin-bottom: 24rpx;

      .detail-item {
        display: flex;
        margin-bottom: 12rpx;

        .detail-label {
          font-size: 24rpx;
          color: #666;
          width: 140rpx;
          flex-shrink: 0;
        }

        .detail-value {
          flex: 1;
          font-size: 24rpx;
          color: #333;
        }

        .tag-list {
          flex: 1;
          display: flex;
          flex-wrap: wrap;
          gap: 8rpx;

          .tag {
            padding: 4rpx 12rpx;
            background: #f8f8f8;
            color: #666;
            border-radius: 12rpx;
            font-size: 20rpx;
          }
        }
      }
    }

    .app-actions {
      display: flex;
      gap: 12rpx;
      justify-content: flex-end;

      .btn {
        padding: 12rpx 24rpx;
        border-radius: 8rpx;
        font-size: 24rpx;
        font-weight: 500;

        &.btn-approve {
          background: #28a745;
          color: #fff;
        }

        &.btn-reject {
          background: #dc3545;
          color: #fff;
        }
      }
    }

    .app-review-info {
      background: #f8f8f8;
      border-radius: 8rpx;
      padding: 16rpx;

      .review-item {
        display: flex;
        margin-bottom: 8rpx;

        .review-label {
          font-size: 22rpx;
          color: #666;
          width: 100rpx;
          flex-shrink: 0;
        }

        .review-value {
          flex: 1;
          font-size: 22rpx;
          color: #333;
        }
      }
    }
  }

  .empty-state {
    text-align: center;
    padding: 80rpx 20rpx;
    background: #fff;
    border-radius: 12rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);

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