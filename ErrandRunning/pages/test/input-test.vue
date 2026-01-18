<template>
  <view class="test-container">
    <text class="test-title">输入框测试</text>
    
    <view class="test-item">
      <text class="test-label">测试输入框 1:</text>
      <input v-model="testValue1" placeholder="请输入内容" class="test-input" />
      <text class="test-output">输入值: {{ testValue1 }}</text>
    </view>
    
    <view class="test-item">
      <text class="test-label">测试输入框 2:</text>
      <uni-input v-model="testValue2" placeholder="请输入内容" class="test-input" />
      <text class="test-output">输入值: {{ testValue2 }}</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      testValue1: '',
      testValue2: ''
    }
  }
}
</script>

<style scoped>
.test-container {
  padding: 30rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.test-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 40rpx;
  display: block;
  text-align: center;
}

.test-item {
  background: #fff;
  padding: 24rpx;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.test-label {
  display: block;
  font-size: 26rpx;
  color: #333;
  margin-bottom: 12rpx;
}

.test-input {
  width: 100%;
  height: 80rpx;
  border: 1rpx solid #e0e0e0;
  border-radius: 8rpx;
  padding: 0 20rpx;
  font-size: 26rpx;
  color: #333;
  background: #f9f9f9;
  box-sizing: border-box;
  margin-bottom: 12rpx;
}

.test-output {
  font-size: 24rpx;
  color: #666;
}
</style>