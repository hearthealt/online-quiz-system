<template>
  <view class="login-container">
    <!-- 登录卡片 -->
    <view class="login-card">
      <!-- 头部 -->
      <view class="login-header">
        <view class="logo">
          <text class="logo-text">Q</text>
        </view>
        <view class="title">在线答题系统</view>
        <view class="subtitle">欢迎回来，开始你的学习之旅</view>
      </view>

      <!-- 登录表单 -->
      <view class="login-form">
        <view class="form-item">
          <input
            v-model="loginForm.username"
            class="form-input"
            placeholder="请输入用户名"
            placeholder-class="input-placeholder"
            maxlength="20"
          />
        </view>

        <view class="form-item">
          <input
            v-model="loginForm.password"
            class="form-input"
            placeholder="请输入密码"
            placeholder-class="input-placeholder"
            password
            maxlength="20"
          />
        </view>

        <view class="form-item">
          <view class="remember-row">
            <view class="remember-item" @click="toggleRemember">
              <view class="checkbox" :class="{ checked: loginForm.rememberMe }">
                <text v-if="loginForm.rememberMe" class="check-icon">✓</text>
              </view>
              <text class="remember-text">记住我</text>
            </view>
          </view>
        </view>

        <button
          class="login-btn"
          :class="{ loading: loading }"
          @click="handleLogin"
          :disabled="loading"
        >
          <text v-if="!loading">登录</text>
          <text v-else>登录中...</text>
        </button>
      </view>

      <!-- 底部提示 -->
      <view class="login-footer">
        <text class="footer-text">还没有账号？请前往网站注册</text>
      </view>
    </view>
  </view>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { authAPI } from '@/utils/api'
import { userStorage } from '@/utils/storage'

export default {
  name: 'LoginPage',
  setup() {
    const loading = ref(false)

    const loginForm = reactive({
      username: '',
      password: '',
      rememberMe: false
    })

    // 切换记住我状态
    const toggleRemember = () => {
      loginForm.rememberMe = !loginForm.rememberMe
    }

    // 处理登录
    const handleLogin = async () => {
      // 表单验证
      if (!loginForm.username.trim()) {
        uni.showToast({
          title: '请输入用户名',
          icon: 'none'
        })
        return
      }

      if (!loginForm.password.trim()) {
        uni.showToast({
          title: '请输入密码',
          icon: 'none'
        })
        return
      }

      if (loginForm.username.length < 3) {
        uni.showToast({
          title: '用户名至少3个字符',
          icon: 'none'
        })
        return
      }

      if (loginForm.password.length < 6) {
        uni.showToast({
          title: '密码至少6个字符',
          icon: 'none'
        })
        return
      }

      try {
        loading.value = true

        // 调用登录API
        const response = await authAPI.login({
          username: loginForm.username.trim(),
          password: loginForm.password.trim(),
          rememberMe: loginForm.rememberMe
        })

        // 保存用户信息和token
        userStorage.saveUser(response.user)
        userStorage.saveToken(response.token)

        // 处理记住我功能（记住用户名和密码）
        if (loginForm.rememberMe) {
          uni.setStorageSync('rememberedUsername', loginForm.username)
          uni.setStorageSync('rememberedPassword', loginForm.password)
          uni.setStorageSync('rememberMe', true)
        } else {
          uni.removeStorageSync('rememberedUsername')
          uni.removeStorageSync('rememberedPassword')
          uni.removeStorageSync('rememberMe')
        }

        uni.showToast({
          title: '登录成功',
          icon: 'success'
        })

        // 延迟跳转，让用户看到成功提示
        setTimeout(() => {
          uni.switchTab({
            url: '/pages/index/index'
          })
        }, 1500)

      } catch (error) {
        console.error('登录失败:', error)
        uni.showToast({
          title: error.message || '登录失败',
          icon: 'none',
          duration: 2000
        })
      } finally {
        loading.value = false
      }
    }

    // 页面加载时检查登录状态
    onMounted(() => {
      // 如果已登录，直接跳转首页
      if (userStorage.isLoggedIn()) {
        uni.switchTab({
          url: '/pages/index/index'
        })
        return
      }

      // 预填记住的用户名和密码
      const rememberMe = uni.getStorageSync('rememberMe')
      if (rememberMe) {
        const rememberedUsername = uni.getStorageSync('rememberedUsername')
        const rememberedPassword = uni.getStorageSync('rememberedPassword')
        if (rememberedUsername) {
          loginForm.username = rememberedUsername
        }
        if (rememberedPassword) {
          loginForm.password = rememberedPassword
        }
        loginForm.rememberMe = true
      }
    })

    return {
      loading,
      loginForm,
      toggleRemember,
      handleLogin
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}

.login-card {
  width: 100%;
  max-width: 600rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 48rpx 32rpx;
}

.login-header {
  text-align: center;
  margin-bottom: 48rpx;
}

.logo {
  width: 80rpx;
  height: 80rpx;
  background: #409eff;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24rpx;
}

.logo-text {
  font-size: 40rpx;
  font-weight: 700;
  color: white;
}

.title {
  font-size: 40rpx;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12rpx;
}

.subtitle {
  font-size: 26rpx;
  color: #909399;
  line-height: 1.5;
}

.login-form {
  margin-bottom: 32rpx;
}

.form-item {
  margin-bottom: 24rpx;
}

.form-input {
  height: 88rpx;
  padding: 0 24rpx;
  font-size: 30rpx;
  color: #303133;
  background: #f5f7fa;
  border-radius: 8rpx;
  border: 2rpx solid transparent;
}

.form-input:focus {
  border-color: #409eff;
  background: #fff;
}

.input-placeholder {
  color: #c0c4cc;
  font-size: 30rpx;
}

.remember-row {
  display: flex;
  align-items: center;
}

.remember-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.checkbox {
  width: 36rpx;
  height: 36rpx;
  border: 2rpx solid #dcdfe6;
  border-radius: 6rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.checkbox.checked {
  background: #409eff;
  border-color: #409eff;
}

.check-icon {
  color: white;
  font-size: 24rpx;
  font-weight: bold;
}

.remember-text {
  font-size: 28rpx;
  color: #606266;
}

.login-btn {
  width: 100%;
  height: 88rpx;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 8rpx;
  font-size: 32rpx;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 32rpx;
}

.login-btn:active {
  background: #337ecc;
}

.login-btn.loading {
  opacity: 0.7;
}

.login-btn:disabled {
  opacity: 0.7;
}

.login-footer {
  text-align: center;
  padding-top: 32rpx;
  border-top: 1rpx solid #f0f0f0;
}

.footer-text {
  font-size: 26rpx;
  color: #909399;
}
</style>
