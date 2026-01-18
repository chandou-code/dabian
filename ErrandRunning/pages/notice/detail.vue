<template>
  <view class="notice-detail">
    <view class="content">
      <text class="title">{{ notice.title }}</text>
      <text class="time">{{ notice.publishTime }}</text>
      <view class="body">
        <text class="text" v-for="(paragraph, index) in paragraphs" :key="index">{{ paragraph }}</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      notice: {
        id: '',
        title: '',
        content: '',
        publishTime: ''
      },
      paragraphs: []
    }
  },
  onLoad(options) {
    this.loadNotice(options.id)
  },
  methods: {
    loadNotice(id) {
      // TODO: 调用API
      this.notice = {
        id: id,
        title: '系统维护通知',
        content: '平台将于1月5日凌晨进行系统维护',
        publishTime: '2025-01-03 10:00'
      }
      this.paragraphs = [
        '亲爱的用户：',
        '',
        '为了提供更好的服务体验，平台将于2025年1月5日凌晨2:00-4:00进行系统维护升级。',
        '',
        '维护期间，以下功能将暂时不可用：',
        '• 任务发布与接单',
        '• 订单查询',
        '• 用户充值与提现',
        '',
        '给您带来的不便，敬请谅解！',
        '',
        '校园跑腿运营团队'
      ]
    }
  }
}
</script>

<style lang="scss" scoped>
.notice-detail { min-height: 100vh; background: #f5f5f5; }
.content { background: white; margin: 20rpx; padding: 40rpx; border-radius: 16rpx; }
.title { display: block; font-size: 40rpx; font-weight: bold; color: #333; margin-bottom: 20rpx; line-height: 1.4; }
.time { display: block; font-size: 24rpx; color: #999; margin-bottom: 40rpx; }
.body .text { display: block; font-size: 28rpx; color: #666; line-height: 2; margin-bottom: 20rpx; }
</style>
