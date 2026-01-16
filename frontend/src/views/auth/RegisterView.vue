<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <h2 class="register-title">用户注册</h2>
        <p class="register-subtitle">创建您的账号，开始学习之旅</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        size="large"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名（3-20个字符）"
            prefix-icon="User"
            clearable
            @blur="checkUsernameAvailable"
          >
            <template #suffix>
              <el-icon v-if="usernameChecking" class="is-loading">
                <Loading />
              </el-icon>
              <el-icon v-else-if="usernameStatus === 'success'" color="#67c23a">
                <CircleCheck />
              </el-icon>
              <el-icon v-else-if="usernameStatus === 'error'" color="#f56c6c">
                <CircleClose />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            type="email"
            placeholder="请输入邮箱地址"
            prefix-icon="Message"
            clearable
            @blur="checkEmailAvailable"
          >
            <template #suffix>
              <el-icon v-if="emailChecking" class="is-loading">
                <Loading />
              </el-icon>
              <el-icon v-else-if="emailStatus === 'success'" color="#67c23a">
                <CircleCheck />
              </el-icon>
              <el-icon v-else-if="emailStatus === 'error'" color="#f56c6c">
                <CircleClose />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="verificationCode">
          <div class="verification-code-group">
            <el-input
              v-model="registerForm.verificationCode"
              placeholder="请输入邮箱验证码"
              prefix-icon="Key"
              clearable
              maxlength="6"
              style="flex: 1; margin-right: 12px;"
            />
            <el-button
              type="primary"
              :loading="sendingCode"
              :disabled="!registerForm.email || emailStatus !== 'success' || countdown > 0"
              @click="sendVerificationCode"
              style="min-width: 120px;"
            >
              {{ countdown > 0 ? `${countdown}s后重发` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item prop="nickname">
          <el-input
            v-model="registerForm.nickname"
            placeholder="请输入昵称（选填）"
            prefix-icon="Avatar"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（6-20个字符）"
            prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            prefix-icon="Lock"
            show-password
            clearable
            @keyup.enter="handleRegister"
          />
        </el-form-item>

        <el-form-item>
          <div class="register-terms">
            <el-checkbox v-model="agreeTerms" required>
              我已阅读并同意
              <el-link type="primary" @click="showTerms">《用户协议》</el-link>
              和
              <el-link type="primary" @click="showPrivacy">《隐私政策》</el-link>
            </el-checkbox>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="register-button"
            :loading="loading"
            :disabled="!agreeTerms"
            @click="handleRegister"
          >
            {{ loading ? '注册中...' : '立即注册' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <span>已有账号？</span>
        <el-link type="primary" @click="$router.push('/login')">
          立即登录
        </el-link>
      </div>
    </div>

    <!-- 装饰图片 -->
    <div class="register-decoration">
      <el-image
        src="/images/register-bg.svg"
        alt="注册背景"
        fit="contain"
        style="width: 100%; height: 100%;"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/store/user'
import type { RegisterRequest } from '@/types/index'
import { sendVerificationCode as sendCodeAPI } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

// 表单引用
const registerFormRef = ref<FormInstance>()

// 表单数据
const registerForm = reactive<RegisterRequest>({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: '',
  verificationCode: ''
})

// 用户名检查状态
const usernameChecking = ref(false)
const usernameStatus = ref<'success' | 'error' | 'none'>('none')

// 邮箱检查状态
const emailChecking = ref(false)
const emailStatus = ref<'success' | 'error' | 'none'>('none')

// 验证码相关状态
const sendingCode = ref(false)
const countdown = ref(0)
let countdownTimer: ReturnType<typeof setInterval> | null = null

// 同意条款
const agreeTerms = ref(false)

// 加载状态
const loading = ref(false)

// 表单验证规则
const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应为3-20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码必须是6位数字', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码必须是6位数字', trigger: 'blur' }
  ],
  nickname: [
    { max: 50, message: '昵称长度不能超过50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应为6-20个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d)/, message: '密码必须包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 检查用户名是否可用
const checkUsernameAvailable = async () => {
  if (!registerForm.username || registerForm.username.length < 3) {
    usernameStatus.value = 'none'
    return
  }

  usernameChecking.value = true
  usernameStatus.value = 'none'

  try {
    const exists = await userStore.checkUsername(registerForm.username)
    usernameStatus.value = exists ? 'error' : 'success'
    
    if (exists) {
      ElMessage.warning('用户名已存在，请选择其他用户名')
    }
  } catch (error) {
    console.error('检查用户名失败:', error)
  } finally {
    usernameChecking.value = false
  }
}

// 检查邮箱是否可用
const checkEmailAvailable = async () => {
  if (!registerForm.email) {
    emailStatus.value = 'none'
    return
  }

  // 先检查邮箱格式
  if (!isValidEmail(registerForm.email)) {
    emailStatus.value = 'error'
    return
  }

  emailChecking.value = true
  emailStatus.value = 'none'

  try {
    // 检查邮箱是否已注册
    const exists = await checkEmailExists(registerForm.email)
    emailStatus.value = exists ? 'error' : 'success'
    
    if (exists) {
      ElMessage.warning('该邮箱已被注册，请使用其他邮箱')
    }
  } catch (error) {
    console.error('检查邮箱失败:', error)
    emailStatus.value = 'error'
  } finally {
    emailChecking.value = false
  }
}

// 检查邮箱是否已注册
const checkEmailExists = async (email: string): Promise<boolean> => {
  try {
    return await userStore.checkEmail(email)
  } catch (error) {
    console.error('检查邮箱是否存在失败:', error)
    throw error
  }
}

// 验证邮箱格式
const isValidEmail = (email: string) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

// 发送验证码
const sendVerificationCode = async () => {
  if (!registerForm.email || !isValidEmail(registerForm.email)) {
    ElMessage.warning('请先输入有效的邮箱地址')
    return
  }

  if (emailStatus.value !== 'success') {
    ElMessage.warning('请先验证邮箱地址')
    return
  }

  sendingCode.value = true

  try {
    await sendCodeAPI({ email: registerForm.email })
    ElMessage.success('验证码已发送到您的邮箱')
    
    // 开始倒计时
    startCountdown()
  } catch (error: any) {
    console.error('发送验证码失败:', error)
    ElMessage.error(error.message || '发送验证码失败')
  } finally {
    sendingCode.value = false
  }
}

// 开始倒计时
const startCountdown = () => {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer!)
      countdownTimer = null
    }
  }, 1000)
}

// 清理倒计时
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    // 表单验证
    await registerFormRef.value.validate()
    
    // 检查是否同意条款
    if (!agreeTerms.value) {
      ElMessage.warning('请先阅读并同意用户协议和隐私政策')
      return
    }

    // 检查用户名是否可用
    if (usernameStatus.value !== 'success') {
      await checkUsernameAvailable()
    }
    
    if (usernameStatus.value !== 'success') {
      ElMessage.error('用户名不可用，请重新选择')
      return
    }

    // 检查邮箱是否可用
    if (emailStatus.value !== 'success') {
      await checkEmailAvailable()
    }
    
    if (emailStatus.value !== 'success') {
      ElMessage.error('邮箱验证失败，请重新输入')
      return
    }

    // 检查验证码
    if (!registerForm.verificationCode || registerForm.verificationCode.length !== 6) {
      ElMessage.error('请输入6位验证码')
      return
    }
    
    loading.value = true
    
    // 调用带验证码的注册API
    const verificationData = {
      username: registerForm.username,
      password: registerForm.password,
      nickname: registerForm.nickname,
      email: registerForm.email!,
      verificationCode: registerForm.verificationCode!
    }
    await userStore.registerWithVerification(verificationData)
    
    ElMessage.success('注册成功！请登录')
    
    // 跳转到登录页面
    router.push({
      path: '/login',
      query: { username: registerForm.username }
    })
    
  } catch (error: any) {
    console.error('注册失败:', error)
    ElMessage.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}

// 显示用户协议
const showTerms = () => {
  ElMessageBox.alert(
    '这里是用户协议的内容...',
    '用户协议',
    {
      confirmButtonText: '我知道了',
      type: 'info'
    }
  )
}

// 显示隐私政策
const showPrivacy = () => {
  ElMessageBox.alert(
    '这里是隐私政策的内容...',
    '隐私政策',
    {
      confirmButtonText: '我知道了',
      type: 'info'
    }
  )
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-card {
  width: 100%;
  max-width: 450px;
  margin: 0 auto;
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 2;
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.register-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.register-subtitle {
  color: #909399;
  font-size: 14px;
}

.register-form {
  margin-bottom: 24px;
}

.register-terms {
  text-align: center;
  font-size: 14px;
  color: #606266;
}

.register-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
}

.register-footer {
  text-align: center;
  color: #606266;
  font-size: 14px;
}

.register-decoration {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  opacity: 0.1;
  pointer-events: none;
}

/* 验证码输入组样式 */
.verification-code-group {
  display: flex;
  align-items: center;
  width: 100%;
}

/* 用户名和邮箱检查状态样式 */
.el-input__suffix .el-icon.is-loading {
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 响应式设计 */
@media (min-width: 768px) {
  .register-container {
    justify-content: center;
  }
  
  .register-card {
    margin-left: 10%;
  }
}

@media (max-width: 480px) {
  .register-card {
    padding: 24px;
    margin: 0 16px;
  }
  
  .register-title {
    font-size: 24px;
  }
}
</style>
