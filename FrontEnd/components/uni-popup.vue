<template>
  <view v-if="show" class="uni-popup" :class="[type]">
    <view class="uni-popup-mask" @click="handleMaskClick"></view>
    <view class="uni-popup-content">
      <slot></slot>
    </view>
  </view>
</template>

<script>
export default {
  name: 'UniPopup',
  props: {
    type: {
      type: String,
      default: 'center' // center, bottom, top
    }
  },
  data() {
    return {
      show: false
    }
  },
  methods: {
    open() {
      this.show = true
    },
    close() {
      this.show = false
    },
    // 为ref调用提供方法
    openPopup() {
      this.open()
    },
    closePopup() {
      this.close()
    },
    handleMaskClick() {
      this.close()
    }
  }
}
</script>

<style scoped>
.uni-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
}

.uni-popup-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
}

.uni-popup-content {
  position: absolute;
  background: white;
}

/* center类型 */
.uni-popup.center .uni-popup-content {
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  border-radius: 12px;
  min-width: 200px;
  max-width: 80%;
  max-height: 80%;
}

/* bottom类型 */
.uni-popup.bottom .uni-popup-content {
  bottom: 0;
  left: 0;
  right: 0;
  border-radius: 20px 20px 0 0;
  max-height: 80vh;
}

/* top类型 */
.uni-popup.top .uni-popup-content {
  top: 0;
  left: 0;
  right: 0;
  border-radius: 0 0 20px 20px;
  max-height: 80vh;
}
</style>