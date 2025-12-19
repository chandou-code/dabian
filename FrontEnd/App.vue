<script>
import Vue from 'vue'
import store from './store'
import './uni.scss'

Vue.prototype.$store = store

export default {
  onLaunch: function() {
    console.log('App Launch')
    // 初始化用户状态
    this.$store.dispatch('initUser')
  },
  onShow: function() {
    console.log('App Show')
  },
  onHide: function() {
    console.log('App Hide')
  }
}
</script>

<style>
/*每个页面公共css */
page {
  background-color: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 通用样式 */
.container {
  padding: 30rpx;
}

.card {
  background: white;
  border-radius: 12rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.btn {
  height: 80rpx;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  border: none;
  transition: all 0.3s;
}

.btn-primary {
  background: #2196f3;
  color: white;
}

.btn-success {
  background: #4caf50;
  color: white;
}

.btn-warning {
  background: #ff9800;
  color: white;
}

.btn-danger {
  background: #f44336;
  color: white;
}

.btn:active {
  opacity: 0.8;
}

.text-center {
  text-align: center;
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
  color: #666;
}

.mb-20 {
  margin-bottom: 20rpx;
}

.mt-20 {
  margin-top: 20rpx;
}

.flex {
  display: flex;
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

.flex-1 {
  flex: 1;
}

.font-bold {
  font-weight: bold;
}

.font-lg {
  font-size: 36rpx;
}

.font-md {
  font-size: 30rpx;
}

.font-sm {
  font-size: 24rpx;
}

.status-pending {
  color: #ff9800;
}

.status-approved {
  color: #4caf50;
}

.status-rejected {
  color: #f44336;
}
</style>
