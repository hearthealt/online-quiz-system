<template>
  <view id="app">
    <!-- 全局加载状态 -->
    <view v-if="globalLoading" class="global-loading">
      <view class="loading-content">
        <view class="loading-spinner"></view>
        <text class="loading-text">加载中...</text>
      </view>
    </view>
  </view>
</template>

<script>
import { ref, onMounted } from 'vue'
import { userStorage } from '@/utils/storage'

export default {
  name: 'App',
  setup() {
    const globalLoading = ref(true)
    
    // 检查登录状态
    const checkLoginStatus = async () => {
      try {
        // 使用userStorage检查登录状态
        const isLoggedIn = userStorage.isLoggedIn()
        
        if (isLoggedIn) {
          // 已登录且token有效，跳转到首页
          console.log('用户已登录，跳转到首页')
          uni.reLaunch({
            url: '/pages/index/index'
          })
        } else {
          // 未登录或token过期，清除存储并跳转到登录页
          console.log('用户未登录或token过期，跳转到登录页')
          userStorage.clearLogin()
          uni.reLaunch({
            url: '/pages/login/login'
          })
        }
      } catch (error) {
        console.error('检查登录状态失败:', error)
        // 出错时清除存储并跳转到登录页
        userStorage.clearLogin()
        uni.reLaunch({
          url: '/pages/login/login'
        })
      } finally {
        globalLoading.value = false
      }
    }
    
    onMounted(() => {
      // 检查登录状态
      checkLoginStatus()
    })
    
    return {
      globalLoading
    }
  }
}
</script>

<style>
/* 全局样式 */
page {
  background-color: #f8f9fa;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

/* 全局加载样式 */
.global-loading {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(255, 255, 255, 0.9);
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
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid #f3f3f3;
  border-top: 4rpx solid #007aff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-text {
  font-size: 28rpx;
  color: #666;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 通用样式 */
.container {
  padding: 20rpx;
}

.card {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.1);
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12rpx;
  padding: 24rpx 48rpx;
  font-size: 32rpx;
  font-weight: 500;
}

.btn-secondary {
  background: #f8f9fa;
  color: #666;
  border: 2rpx solid #e9ecef;
  border-radius: 12rpx;
  padding: 24rpx 48rpx;
  font-size: 32rpx;
}

.text-center {
  text-align: center;
}

.text-primary {
  color: #007aff;
}

.text-success {
  color: #28a745;
}

.text-danger {
  color: #dc3545;
}

.text-muted {
  color: #6c757d;
}

.mb-20 {
  margin-bottom: 20rpx;
}

.mb-30 {
  margin-bottom: 30rpx;
}

.mt-20 {
  margin-top: 20rpx;
}

.mt-30 {
  margin-top: 30rpx;
}
</style>
