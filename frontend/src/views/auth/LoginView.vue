<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2 class="login-title">用户登录</h2>
        <p class="login-subtitle">欢迎回到在线答题系统</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        size="large"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <div class="remember-me-container">
            <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
            <el-tooltip 
              content="勾选后将保存用户名和密码到本地，请确保在安全的设备上使用" 
              placement="top"
              effect="dark"
            >
              <el-icon class="info-icon"><InfoFilled /></el-icon>
            </el-tooltip>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <div class="footer-links">
          <span>还没有账号？</span>
          <el-link type="primary" @click="$router.push('/register')">
            立即注册
          </el-link>
        </div>
        <div class="footer-links">
          <span>忘记密码？</span>
          <el-link type="primary" @click="showContactDialog = true">
            联系管理员
          </el-link>
        </div>
      </div>
    </div>

    <!-- 装饰图片 -->
    <div class="login-decoration">
      <el-image
        src="/images/login-bg.svg"
        alt="登录背景"
        fit="contain"
        style="width: 100%; height: 100%;"
      />
    </div>
    
    <!-- 联系管理员弹窗 -->
    <ContactAdminDialog v-model="showContactDialog" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/store/user'
import type { LoginRequest } from '@/types/index'
import ContactAdminDialog from '@/components/common/ContactAdminDialog.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 联系管理员弹窗状态
const showContactDialog = ref(false)

// 表单引用
const loginFormRef = ref<FormInstance>()

// 表单数据
const loginForm = reactive<LoginRequest>({
  username: '',
  password: '',
  rememberMe: false
})

// 表单验证规则
const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应为6-20个字符', trigger: 'blur' }
  ]
}

// 加载状态
const loading = ref(false)

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    // 表单验证
    await loginFormRef.value.validate()
    
    loading.value = true
    
    // 调用登录API
    await userStore.login(loginForm)
    
    // 处理记住我功能
    if (loginForm.rememberMe) {
      // 保存用户名到localStorage
      localStorage.setItem('rememberedUsername', loginForm.username)
      // 保存密码（简单编码，不是真正的加密）
      const encodedPassword = btoa(loginForm.password)
      localStorage.setItem('rememberedPassword', encodedPassword)
    } else {
      // 清除保存的用户名和密码
      localStorage.removeItem('rememberedUsername')
      localStorage.removeItem('rememberedPassword')
    }
    
    ElMessage.success('登录成功')
    
    // 跳转到目标页面或首页
    const redirect = (route.query.redirect as string) || '/home'
    router.push(redirect)
    
  } catch (error: any) {
    console.error('登录失败:', error)
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

// 页面加载时初始化
onMounted(() => {
  // 检查是否有记住的用户名和密码
  const rememberedUsername = localStorage.getItem('rememberedUsername')
  const rememberedPassword = localStorage.getItem('rememberedPassword')
  
  if (rememberedUsername) {
    loginForm.username = rememberedUsername
    loginForm.rememberMe = true
    
    // 如果有保存的密码，解码并填充
    if (rememberedPassword) {
      try {
        loginForm.password = atob(rememberedPassword)
      } catch (error) {
        // 如果解码失败，清除保存的密码
        localStorage.removeItem('rememberedPassword')
        console.warn('密码解码失败，已清除保存的密码')
      }
    }
  }
})

// 快速登录（开发时使用）
const quickLogin = async (username: string, password: string) => {
  loginForm.username = username
  loginForm.password = password
  await handleLogin()
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 2;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.login-subtitle {
  color: #909399;
  font-size: 14px;
}

.login-form {
  margin-bottom: 24px;
}

.remember-me-container {
  display: flex;
  align-items: center;
  gap: 4px;
}

.info-icon {
  color: #909399;
  cursor: help;
  font-size: 14px;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
}

.login-footer {
  text-align: center;
  color: #606266;
  font-size: 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.footer-links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.login-decoration {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  opacity: 0.1;
  pointer-events: none;
}

/* 响应式设计 */
@media (min-width: 768px) {
  .login-container {
    justify-content: center;
  }
  
  .login-card {
    margin-right: 10%;
  }
}

@media (max-width: 480px) {
  .login-card {
    padding: 24px;
    margin: 0 16px;
  }
  
  .login-title {
    font-size: 24px;
  }
}

/* 开发环境快速登录按钮 */
.quick-login {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1000;
}

.quick-login .el-button {
  margin: 0 4px;
}
</style>
