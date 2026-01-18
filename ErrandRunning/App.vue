<script>
	export default {
		onLaunch: function() {
			console.log('App Launch')
		},
		onShow: function() {
			console.log('App Show')
		},
		onHide: function() {
			console.log('App Hide')
		}
	}
</script>

<template>
	<view>
	</view>
</template>

<style>
/* 全局样式 */
page {
  background-color: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 容器 */
.container {
  padding: 30rpx;
}

/* 卡片 */
.card {
  background: white;
  border-radius: 12rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
}

/* 按钮 */
.btn {
  height: 80rpx;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  border: none;
  transition: all 0.3s;
}

.btn-primary {
  background: linear-gradient(135deg, #2196f3, #1976d2);
  color: white;
}

.btn-success {
  background: linear-gradient(135deg, #4caf50, #43a047);
  color: white;
}

.btn-warning {
  background: linear-gradient(135deg, #ff9800, #fb8c00);
  color: white;
}

.btn-danger {
  background: linear-gradient(135deg, #f44336, #e53935);
  color: white;
}

.btn:active {
  opacity: 0.8;
  transform: scale(0.98);
}

/* 文本 */
.text-center {
  text-align: center;
}

.text-left {
  text-align: left;
}

.text-right {
  text-align: right;
}

.text-primary {
  color: #2196f3;
}

.text-success {
  color: #4caf50;
}

.text-warning {
  color: #ff9800;
}

.text-danger {
  color: #f44336;
}

.text-muted {
  color: #999;
}

/* 间距 */
.mt-10 { margin-top: 10rpx; }
.mt-20 { margin-top: 20rpx; }
.mt-30 { margin-top: 30rpx; }

.mb-10 { margin-bottom: 10rpx; }
.mb-20 { margin-bottom: 20rpx; }
.mb-30 { margin-bottom: 30rpx; }

.ml-10 { margin-left: 10rpx; }
.ml-20 { margin-left: 20rpx; }

.mr-10 { margin-right: 10rpx; }
.mr-20 { margin-right: 20rpx; }

.pt-10 { padding-top: 10rpx; }
.pt-20 { padding-top: 20rpx; }

.pb-10 { padding-bottom: 10rpx; }
.pb-20 { padding-bottom: 20rpx; }

.pl-10 { padding-left: 10rpx; }
.pl-20 { padding-left: 20rpx; }

.pr-10 { padding-right: 10rpx; }
.pr-20 { padding-right: 20rpx; }

/* 布局 */
.flex {
  display: flex;
}

.flex-column {
  display: flex;
  flex-direction: column;
}

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.flex-around {
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.flex-1 {
  flex: 1;
}

/* 字体 */
.font-sm {
  font-size: 24rpx;
}

.font-md {
  font-size: 28rpx;
}

.font-lg {
  font-size: 32rpx;
}

.font-xl {
  font-size: 36rpx;
}

.font-bold {
  font-weight: bold;
}

/* 状态颜色 */
.status-pending {
  color: #ff9800;
  background: #fff8e1;
}

.status-accepted {
  color: #2196f3;
  background: #e3f2fd;
}

.status-delivering {
  color: #9c27b0;
  background: #f3e5f5;
}

.status-completed {
  color: #4caf50;
  background: #e8f5e8;
}

.status-cancelled {
  color: #f44336;
  background: #ffebee;
}

/* 阴影 */
.shadow-sm {
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
}

.shadow-md {
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
}

.shadow-lg {
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
}

/* 圆角 */
.rounded-sm {
  border-radius: 8rpx;
}

.rounded-md {
  border-radius: 12rpx;
}

.rounded-lg {
  border-radius: 16rpx;
}

.rounded-full {
  border-radius: 9999rpx;
}

/* 辅助类 */
.overflow-hidden {
  overflow: hidden;
}

.overflow-auto {
  overflow: auto;
}

.ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.position-relative {
  position: relative;
}

.position-absolute {
  position: absolute;
}

.position-fixed {
  position: fixed;
}

.cursor-pointer {
  cursor: pointer;
}

/* 动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.fade-in {
  animation: fadeIn 0.3s ease;
}

.slide-up {
  animation: slideUp 0.3s ease;
}
</style>
