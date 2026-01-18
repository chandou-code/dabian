<template>
  <view class="admin-container">
    <view class="admin-header">
      <text class="admin-title">举报管理</text>
      <text class="admin-subtitle">处理用户举报信息</text>
    </view>

    <view class="stats-section">
      <view class="stat-card">
        <text class="stat-value">{{ totalReports }}</text>
        <text class="stat-label">总举报数</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ pendingReports }}</text>
        <text class="stat-label">待处理</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ processedReports }}</text>
        <text class="stat-label">已处理</text>
      </view>
    </view>

    <view class="report-list">
      <view 
        v-for="report in reports" 
        :key="report.id"
        class="report-item"
        :class="report.status"
      >
        <view class="report-header">
          <view class="report-info">
            <text class="report-type">{{ getReportTypeLabel(report.type) }}</text>
            <text class="report-time">{{ formatDate(report.createdAt) }}</text>
          </view>
          <text class="report-status" :class="report.status">
            {{ getStatusLabel(report.status) }}
          </text>
        </view>
        
        <view class="report-content">
          <view class="user-info">
            <view class="reporter">
              <text class="info-label">举报人：</text>
              <text class="info-value">{{ report.reporterName || '匿名用户' }} (ID: {{ report.reporterId }})</text>
            </view>
            <view class="reported">
              <text class="info-label">被举报人：</text>
              <text class="info-value">{{ report.reportedName || '未知用户' }} (ID: {{ report.reportedUserId }})</text>
            </view>
          </view>
          <view class="report-desc">
            <text class="desc-label">举报内容：</text>
            <text class="desc-content">{{ report.content }}</text>
          </view>
          <view class="report-chat-tag" v-if="report.chatRecords && JSON.parse(report.chatRecords).length > 0">
            <text class="chat-tag-icon">💬</text>
            <text class="chat-tag-text">包含{{ JSON.parse(report.chatRecords).length }}条聊天记录</text>
          </view>
          <view class="report-remark" v-if="report.adminRemark">
            <text class="remark-label">处理备注：</text>
            <text class="remark-content">{{ report.adminRemark }}</text>
          </view>
        </view>
        
        <view class="report-actions">
          <!-- 小三角展开按钮 -->
          <view 
            class="expand-btn" 
            :class="{ expanded: expandedReportId === report.id }"
            @click="expandedReportId = expandedReportId === report.id ? null : report.id"
          >
            <text class="triangle">▼</text>
          </view>
          
          <!-- 展开的操作菜单 -->
          <view 
            class="action-menu" 
            :class="{ show: expandedReportId === report.id }"
          >
            <button class="action-btn" @click="viewReportDetail(report)">查看详情</button>
            <button class="action-btn" v-if="report.status === 'pending'" @click="handleReport(report, 'processed')">已处理</button>
            <button class="action-btn" v-if="report.status === 'pending'" @click="handleReport(report, 'dismissed')">驳回</button>
            <button class="action-btn" v-else @click="reopenReport(report)">重新处理</button>
            <button class="action-btn danger" @click="banUser(report)">封禁用户</button>
            <button class="action-btn danger" @click="deleteReport(report)">删除</button>
          </view>
        </view>
      </view>

      <view v-if="reports.length === 0" class="empty-state">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无举报数据</text>
      </view>
    </view>

    <uni-load-more 
      v-if="hasMore" 
      :status="loadStatus" 
      @clickLoadMore="loadMore"
    ></uni-load-more>

    <!-- 举报详情弹窗 -->
    <view class="custom-popup" v-if="showReportDetailPopup">
      <view class="popup-mask" @click="closeReportDetail"></view>
      <view class="popup-container">
        <view class="popup-header">
          <text class="popup-title">举报详情</text>
          <text class="popup-close" @click="closeReportDetail">×</text>
        </view>
        <view class="popup-content">
          <view class="detail-item">
            <text class="detail-label">举报类型：</text>
            <text class="detail-value">{{ currentReport ? getReportTypeLabel(currentReport.type) : '' }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">举报时间：</text>
            <text class="detail-value">{{ currentReport ? formatDate(currentReport.createdAt) : '' }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">举报人：</text>
            <text class="detail-value">{{ currentReport ? (currentReport.reporterName || '匿名用户') : '' }} (ID: {{ currentReport ? currentReport.reporterId : '' }})</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">被举报人：</text>
            <text class="detail-value">{{ currentReport ? currentReport.reportedName : '未知用户' }} (ID: {{ currentReport ? currentReport.reportedUserId : '' }})</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">举报内容：</text>
            <text class="detail-value content">
              {{ currentReport ? currentReport.content : '' }}
            </text>
          </view>
          <!-- 聊天记录显示 -->
          <view class="detail-item" v-if="currentReport && currentReport.chatRecords && JSON.parse(currentReport.chatRecords).length > 0">
            <text class="detail-label">聊天记录：</text>
            <view class="chat-records">
              <view 
                v-for="(record, index) in JSON.parse(currentReport.chatRecords)" 
                :key="index"
                class="chat-message"
                :class="record.sender === 'me' ? 'self-message' : 'other-message'"
              >
                <view class="message-sender">{{ record.sender === 'me' ? (currentReport.reporterName || '匿名用户') : (currentReport.reportedName || '未知用户') }}</view>
                <view class="message-content">{{ record.content }}</view>
                <view class="message-time" v-if="record.time && formatDate(record.time)">{{ formatDate(record.time) }}</view>
              </view>
            </view>
          </view>
          <view class="detail-item" v-if="currentReport && currentReport.status !== 'pending'">
            <text class="detail-label">处理状态：</text>
            <text class="detail-value status" :class="currentReport.status">
              {{ getStatusLabel(currentReport.status) }}
            </text>
          </view>
          <view class="detail-item" v-if="currentReport && currentReport.adminRemark">
            <text class="detail-label">处理备注：</text>
            <text class="detail-value content">
              {{ currentReport.adminRemark }}
            </text>
          </view>
          <view class="remark-section" v-if="currentReport && currentReport.status === 'pending'">
            <text class="remark-title">处理备注：</text>
            <uni-easyinput 
              v-model="adminRemark" 
              placeholder="请输入处理备注" 
              type="textarea"
              rows="3"
              class="remark-input"
            ></uni-easyinput>
          </view>
        </view>
        <view class="popup-footer" v-if="currentReport && currentReport.status === 'pending'">
          <button class="btn btn-secondary" @click="closeReportDetail">取消</button>
          <button class="btn btn-success" @click="approveReport">已处理</button>
          <button class="btn btn-warning" @click="dismissReport">驳回</button>
        </view>
        <view class="popup-footer" v-else>
          <button class="btn btn-primary" @click="closeReportDetail">关闭</button>
        </view>
      </view>
    </view>
    
    <!-- 封禁用户弹窗 -->
    <view class="custom-popup" v-if="showBanPopup">
      <view class="popup-mask" @click="closeBanPopup"></view>
      <view class="popup-container">
        <view class="popup-header">
          <text class="popup-title">封禁用户</text>
          <text class="popup-close" @click="closeBanPopup">×</text>
        </view>
        <view class="popup-content">
          <view class="detail-item">
            <text class="detail-label">被封禁用户：</text>
            <text class="detail-value">{{ currentBanReport ? currentBanReport.reportedName : '未知用户' }} (ID: {{ currentBanReport ? currentBanReport.reportedUserId : '' }})</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">封禁时长：</text>
            <view class="ban-duration">
              <uni-easyinput 
                v-model="banDuration" 
                type="number" 
                placeholder="请输入封禁天数" 
                style="width: 120rpx; margin-right: 20rpx;"
              ></uni-easyinput>
              <text class="duration-unit">天</text>
            </view>
          </view>
          <view class="detail-item">
            <text class="detail-label">封禁原因：</text>
            <uni-easyinput 
              v-model="banReason" 
              placeholder="请输入封禁原因" 
              type="textarea"
              rows="3"
              class="remark-input"
            ></uni-easyinput>
          </view>
        </view>
        <view class="popup-footer">
          <button class="btn btn-secondary" @click="closeBanPopup">取消</button>
          <button class="btn btn-danger" @click="confirmBanUser">确认封禁</button>
        </view>
      </view>
    </view>
    
    <!-- 删除举报弹窗 -->
    <view class="custom-popup" v-if="showDeletePopup">
      <view class="popup-mask" @click="closeDeletePopup"></view>
      <view class="popup-container">
        <view class="popup-header">
          <text class="popup-title">删除举报</text>
          <text class="popup-close" @click="closeDeletePopup">×</text>
        </view>
        <view class="popup-content">
          <view class="detail-item">
            <text class="detail-value content">确定要删除这条举报记录吗？此操作不可恢复。</text>
          </view>
        </view>
        <view class="popup-footer">
          <button class="btn btn-secondary" @click="closeDeletePopup">取消</button>
          <button class="btn btn-danger" @click="confirmDeleteReport">确认删除</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { get, put } from '../../api/request'

export default {
  data() {
    return {
      reports: [],
      loadStatus: 'more',
      hasMore: true,
      currentPage: 1,
      pageSize: 10,
      loading: false,
      totalReports: 0,
      pendingReports: 0,
      processedReports: 0,
      
      // 展开/收起状态控制
      expandedReportId: null,
      
      // 举报详情弹窗相关
      showReportDetailPopup: false,
      currentReport: null,
      adminRemark: '',
      
      // 封禁用户弹窗相关
      showBanPopup: false,
      currentBanReport: null,
      banDuration: 7, // 默认封禁7天
      banReason: '',
      
      // 删除举报弹窗相关
      showDeletePopup: false,
      currentDeleteReport: null
    }
  },

  onLoad() {
    this.loadReports()
    this.loadReportStats()
  },

  methods: {
    async loadReportStats() {
      try {
        // 获取举报统计数据
        const response = await get('/reports/stats')
        if (response.code === 200 && response.data) {
          this.totalReports = response.data.total || 0
          this.pendingReports = response.data.pending || 0
          this.processedReports = response.data.processed || 0
        }
      } catch (error) {
        console.error('获取举报统计失败:', error)
      }
    },

    async loadReports(refresh = false) {
      if (this.loading) return
      
      this.loading = true
      if (refresh) {
        this.currentPage = 1
        this.reports = []
      }
      
      try {
        // 构建请求参数
        const params = {
          page: this.currentPage,
          pageSize: this.pageSize,
          status: this.filterStatus || null,
          keyword: this.searchKeyword || null
        }
        
        // 调用API获取举报列表
        const response = await get('/reports', params)
        
        if (response.code === 200) {
          const { data, total } = response
          
          if (refresh) {
            this.reports = data
          } else {
            this.reports = [...this.reports, ...data]
          }
          
          // 更新加载状态
          this.hasMore = this.reports.length < total
          this.loadStatus = this.hasMore ? 'more' : 'noMore'
        } else {
          uni.showToast({
            title: response.msg || '获取举报列表失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('获取举报列表失败:', error)
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
      // 检查日期是否有效
      if (isNaN(date.getTime())) {
        return ''
      }
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    },

    getStatusLabel(status) {
      const statusMap = {
        pending: '待处理',
        processed: '已处理',
        dismissed: '已驳回'
      }
      return statusMap[status] || status
    },

    getReportTypeLabel(type) {
      const typeMap = {
        'harassment': '恶意骚扰',
        'false_info': '虚假信息',
        'illegal_content': '违法内容',
        'other': '其他问题'
      }
      return typeMap[type] || type
    },

    viewReportDetail(report) {
      this.currentReport = report
      this.adminRemark = ''
      this.showReportDetailPopup = true
    },

    closeReportDetail() {
      this.showReportDetailPopup = false
      this.currentReport = null
      this.adminRemark = ''
    },

    async handleReport(report, status) {
      uni.showModal({
        title: '处理举报',
        content: `确定要将举报标记为"${this.getStatusLabel(status)}"吗？`,
        success: async (res) => {
          if (res.confirm) {
            await this.updateReportStatus(report.id, status)
          }
        }
      })
    },

    async approveReport() {
      if (this.currentReport) {
        await this.updateReportStatus(this.currentReport.id, 'processed')
        this.closeReportDetail()
      }
    },

    async dismissReport() {
      if (this.currentReport) {
        await this.updateReportStatus(this.currentReport.id, 'dismissed')
        this.closeReportDetail()
      }
    },

    async updateReportStatus(reportId, status) {
      uni.showLoading({ title: '处理中...' })
      try {
        const response = await put(`/reports/${reportId}/status`, {
          status,
          adminRemark: this.adminRemark
        })
        
        if (response.code === 200) {
          // 更新本地数据
          const report = this.reports.find(item => item.id === reportId)
          if (report) {
            report.status = status
            report.adminRemark = this.adminRemark
          }
          
          uni.showToast({
            title: '处理成功',
            icon: 'success'
          })
          
          // 更新统计数据
          this.loadReportStats()
        } else {
          uni.showToast({
            title: response.msg || '处理失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('处理举报失败:', error)
        uni.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none'
        })
      } finally {
        uni.hideLoading()
      }
    },

    async reopenReport(report) {
      uni.showModal({
        title: '重新处理',
        content: '确定要将该举报重新标记为待处理吗？',
        success: async (res) => {
          if (res.confirm) {
            await this.updateReportStatus(report.id, 'pending')
          }
        }
      })
    },

    loadMore() {
      if (this.loadStatus === 'more' && !this.loading) {
        this.currentPage++
        this.loadReports()
      }
    },
    
    // 封禁用户相关方法
    banUser(report) {
      this.currentBanReport = report
      this.banDuration = 7 // 重置为默认值
      this.banReason = '' // 重置为默认值
      this.showBanPopup = true
    },
    
    closeBanPopup() {
      this.showBanPopup = false
      this.currentBanReport = null
    },
    
    async confirmBanUser() {
      if (!this.currentBanReport) {
        return
      }
      
      if (!this.banDuration || this.banDuration <= 0) {
        uni.showToast({
          title: '请输入有效的封禁时长',
          icon: 'none'
        })
        return
      }
      
      if (!this.banReason.trim()) {
        uni.showToast({
          title: '请输入封禁原因',
          icon: 'none'
        })
        return
      }
      
      uni.showLoading({ title: '封禁中...' })
      try {
        // 调用真实的封禁用户API
        const response = await put(`/admin/users/${this.currentBanReport.reportedUserId}/status`, {
          status: 0 // 0表示禁用
        })
        
        if (response.code === 200) {
          uni.showToast({
            title: '用户封禁成功',
            icon: 'success'
          })
          this.closeBanPopup()
          // 更新举报状态为已处理
          this.updateReportStatus(this.currentBanReport.id, 'processed')
        } else {
          uni.showToast({
            title: response.msg || '封禁失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('封禁用户失败:', error)
        uni.showToast({
          title: '封禁失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        uni.hideLoading()
      }
    },
    
    // 删除举报相关方法
    deleteReport(report) {
      this.currentDeleteReport = report
      this.showDeletePopup = true
    },
    
    closeDeletePopup() {
      this.showDeletePopup = false
      this.currentDeleteReport = null
    },
    
    async confirmDeleteReport() {
      if (!this.currentDeleteReport) {
        return
      }
      
      uni.showLoading({ title: '删除中...' })
      try {
        // 调用真实的删除举报API
        const response = await put(`/reports/${this.currentDeleteReport.id}/delete`, {})
        
        if (response.code === 200) {
          uni.showToast({
            title: '举报删除成功',
            icon: 'success'
          })
          this.closeDeletePopup()
          // 重新加载举报列表
          this.loadReports(true)
          // 更新统计数据
          this.loadReportStats()
        } else {
          uni.showToast({
            title: response.msg || '删除失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('删除举报失败:', error)
        uni.showToast({
          title: '删除失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        uni.hideLoading()
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

.report-list {
  background: #fff;
  border-radius: 12rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  margin-bottom: 40rpx;
  overflow: visible; /* 改为visible，允许操作菜单超出容器 */
  position: relative; /* 确保z-index生效 */
  z-index: 1; /* 设置基础z-index */
}

.report-item {
  padding: 24rpx;
  border-bottom: 1rpx solid #e9ecef;
  transition: all 0.2s ease;
  position: relative;
  display: flex;
  flex-direction: column;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background-color: #f8f9fa;
  }

  &.pending {
    border-left: 8rpx solid #ffc107;
  }

  &.processed {
    border-left: 8rpx solid #28a745;
  }

  &.dismissed {
    border-left: 8rpx solid #dc3545;
    opacity: 0.8;
  }

  .report-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16rpx;

    .report-info {
      display: flex;
      flex-direction: column;
    }

    .report-type {
      font-size: 26rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 4rpx;
    }

    .report-time {
      font-size: 22rpx;
      color: #999;
    }

    .report-status {
      padding: 6rpx 16rpx;
      border-radius: 20rpx;
      font-size: 22rpx;
      font-weight: 500;

      &.pending {
        background: #fff3cd;
        color: #856404;
      }

      &.processed {
        background: #d4edda;
        color: #155724;
      }

      &.dismissed {
        background: #f8d7da;
        color: #721c24;
      }
    }
  }

  .report-content {
    margin-bottom: 60rpx; /* 增加底部边距，确保操作按钮不会覆盖内容 */
  }

  .user-info {
    display: flex;
    gap: 30rpx;
    margin-bottom: 12rpx;
    flex-wrap: wrap;
  }

  .reporter,
  .reported {
    display: flex;
    align-items: center;
  }

  .info-label,
  .desc-label,
  .detail-label,
  .remark-label {
    font-size: 24rpx;
    color: #666;
    width: 120rpx;
    flex-shrink: 0;
  }

  .info-value,
  .desc-content,
  .detail-content,
  .remark-content {
    font-size: 24rpx;
    color: #333;
    flex: 1;
  }

  .report-desc {
    display: flex;
    margin-bottom: 12rpx;
    align-items: flex-start;
  }

  .desc-content {
    flex: 1;
  }

  .report-detail,
  .report-remark {
    display: flex;
    margin-bottom: 12rpx;
    align-items: flex-start;
  }

  /* 聊天记录标记样式 */
  .report-chat-tag {
    display: flex;
    align-items: center;
    gap: 8rpx;
    margin-bottom: 12rpx;
    padding: 8rpx 16rpx;
    background-color: #e3f2fd;
    border-radius: 20rpx;
    align-self: flex-start;
  }

  .chat-tag-icon {
    font-size: 24rpx;
  }

  .chat-tag-text {
    font-size: 22rpx;
    color: #1976d2;
    font-weight: 500;
  }

  .report-actions {
  position: absolute;
  bottom: 20rpx;
  right: 20rpx;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  z-index: 10;
}

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
  max-height: 70vh;
  overflow-y: auto;

  .popup-subtitle {
    display: block;
    font-size: 28rpx;
    color: #495057;
    margin-bottom: 24rpx;
  }
}

.detail-item {
  display: flex;
  margin-bottom: 20rpx;
  align-items: flex-start;
}

.detail-label {
  font-size: 24rpx;
  color: #666;
  width: 120rpx;
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  font-size: 26rpx;
  color: #333;
}

.detail-value.content {
  white-space: pre-wrap;
  line-height: 1.5;
}

.detail-value.status {
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
  font-weight: 500;
  display: inline-block;
}

/* 聊天记录样式 */
.chat-records {
  margin-top: 16rpx;
  border: 1rpx solid #e9ecef;
  border-radius: 8rpx;
  padding: 16rpx;
  max-height: 400rpx;
  overflow-y: auto;
  background-color: #f8f9fa;
}

.chat-message {
  margin-bottom: 16rpx;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.self-message {
  align-items: flex-end;
}

.other-message {
  align-items: flex-start;
}

.message-sender {
  font-size: 20rpx;
  color: #666;
  font-weight: 500;
}

.message-content {
  padding: 12rpx 16rpx;
  border-radius: 16rpx;
  font-size: 24rpx;
  line-height: 1.4;
  max-width: 70%;
}

.self-message .message-content {
  background-color: #d1ecf1;
  color: #0c5460;
  border-bottom-right-radius: 4rpx;
}

.other-message .message-content {
  background-color: #fff3cd;
  color: #856404;
  border-bottom-left-radius: 4rpx;
}

.message-time {
  font-size: 18rpx;
  color: #999;
}

.remark-section {
  margin-top: 24rpx;
}

.remark-title {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 12rpx;
}

.remark-input {
  width: 100%;
  margin-bottom: 24rpx;
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

    &.btn-success {
      background: #28a745;
      color: #fff;
    }

    &.btn-warning {
      background: #f57c00;
      color: #fff;
    }
    
    &.btn-danger {
      background: #dc3545;
      color: #fff;
    }
  }
}

/* 封禁用户弹窗样式 */
.ban-duration {
  display: flex;
  align-items: center;
}

.duration-unit {
  font-size: 26rpx;
  color: #666;
}
</style>