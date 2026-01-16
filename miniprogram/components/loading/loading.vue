<template>
  <view class="loading-overlay" v-if="visible" @click="handleOverlayClick">
    <view class="loading-content" @click.stop>
      <view class="loading-spinner" :class="size">
        <view class="spinner-ring"></view>
      </view>
      <text class="loading-text" v-if="text">{{ text }}</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'Loading',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    text: {
      type: String,
      default: '加载中...'
    },
    size: {
      type: String,
      default: 'medium',
      validator: (value) => ['small', 'medium', 'large'].includes(value)
    },
    mask: {
      type: Boolean,
      default: true
    }
  },
  emits: ['click-overlay'],
  setup(props, { emit }) {
    const handleOverlayClick = () => {
      if (props.mask) {
        emit('click-overlay')
      }
    }
    
    return {
      handleOverlayClick
    }
  }
}
</script>

<style scoped>
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20rpx;
  background: rgba(255, 255, 255, 0.95);
  padding: 60rpx 40rpx;
  border-radius: 20rpx;
  backdrop-filter: blur(10rpx);
}

.loading-spinner {
  position: relative;
}

.loading-spinner.small {
  width: 40rpx;
  height: 40rpx;
}

.loading-spinner.medium {
  width: 60rpx;
  height: 60rpx;
}

.loading-spinner.large {
  width: 80rpx;
  height: 80rpx;
}

.spinner-ring {
  width: 100%;
  height: 100%;
  border: 4rpx solid #f3f3f3;
  border-top: 4rpx solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-text {
  font-size: 28rpx;
  color: #666;
  text-align: center;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
