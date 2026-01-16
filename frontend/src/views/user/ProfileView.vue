<template>
  <AppLayout>
    <div class="profile-container">
      <div class="page-header">
        <h1 class="page-title">个人信息</h1>
        <p class="page-description">管理您的账户信息和偏好设置</p>
      </div>

      <el-row :gutter="24">
        <!-- 基本信息 -->
        <el-col :xs="24" :lg="16">
          <el-card class="profile-card">
            <template #header>
              <div class="card-header">
                <el-icon><User /></el-icon>
                <span>基本信息</span>
              </div>
            </template>

            <el-form
              ref="profileFormRef"
              :model="profileForm"
              :rules="profileRules"
              label-width="100px"
              size="large"
            >
              <el-form-item label="头像">
                <AvatarUpload
                  v-model="profileForm.avatar"
                  @success="handleAvatarUploadSuccess"
                  @error="handleAvatarUploadError"
                />
              </el-form-item>

              <el-form-item label="用户名" prop="username">
                <el-input
                  v-model="profileForm.username"
                  disabled
                  placeholder="用户名不可修改"
                />
              </el-form-item>

              <el-form-item label="昵称" prop="nickname">
                <el-input
                  v-model="profileForm.nickname"
                  placeholder="请输入昵称"
                  clearable
                />
              </el-form-item>

              <el-form-item label="邮箱" prop="email">
                <el-input
                  v-model="profileForm.email"
                  type="email"
                  placeholder="请输入邮箱地址"
                  clearable
                />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="updateProfile" :loading="updating">
                  保存修改
                </el-button>
                <el-button @click="resetForm">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 修改密码 -->
          <el-card class="password-card">
            <template #header>
              <div class="card-header">
                <el-icon><Lock /></el-icon>
                <span>修改密码</span>
              </div>
            </template>

            <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-width="100px"
              size="large"
            >
              <el-form-item label="当前密码" prop="oldPassword">
                <el-input
                  v-model="passwordForm.oldPassword"
                  type="password"
                  placeholder="请输入当前密码"
                  show-password
                  clearable
                />
              </el-form-item>

              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  placeholder="请输入新密码"
                  show-password
                  clearable
                />
              </el-form-item>

              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  placeholder="请确认新密码"
                  show-password
                  clearable
                />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="updatePassword" :loading="updatingPassword">
                  修改密码
                </el-button>
                <el-button @click="resetPasswordForm">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>

        <!-- 账户统计 -->
        <el-col :xs="24" :lg="8">
          <el-card class="stats-card">
            <template #header>
              <div class="card-header">
                <el-icon><TrendCharts /></el-icon>
                <span>账户统计</span>
              </div>
            </template>

            <div class="stats-content">
              <div class="stat-item">
                <div class="stat-icon">
                  <el-icon color="#409eff"><Edit /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ userStats.totalAnswers }}</div>
                  <div class="stat-label">总答题数</div>
                </div>
              </div>

              <div class="stat-item">
                <div class="stat-icon">
                  <el-icon color="#67c23a"><Check /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ userStats.correctAnswers }}</div>
                  <div class="stat-label">正确题数</div>
                </div>
              </div>

              <div class="stat-item">
                <div class="stat-icon">
                  <el-icon color="#e6a23c"><Star /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ userStats.accuracy }}%</div>
                  <div class="stat-label">正确率</div>
                </div>
              </div>

              <div class="stat-item">
                <div class="stat-icon">
                  <el-icon color="#f56c6c"><Trophy /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ userStats.correctAnswers }}</div>
                  <div class="stat-label">正确数</div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 账户信息 -->
          <el-card class="account-info-card">
            <template #header>
              <div class="card-header">
                <el-icon><InfoFilled /></el-icon>
                <span>账户信息</span>
              </div>
            </template>

            <div class="account-info">
              <div class="info-item">
                <div class="info-label">用户ID</div>
                <div class="info-value">{{ userStore.userInfo?.id }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">注册时间</div>
                <div class="info-value">{{ formatDate(userStore.userInfo?.createdAt) }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">最后登录</div>
                <div class="info-value">{{ formatDate(userStore.userInfo?.lastLoginTime) }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">账户状态</div>
                <div class="info-value">
                  <el-tag :type="userStore.userInfo?.status === 1 ? 'success' : 'danger'" size="small">
                    {{ userStore.userInfo?.status === 1 ? '正常' : '禁用' }}
                  </el-tag>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 快速操作 -->
          <el-card class="quick-actions-card">
            <template #header>
              <div class="card-header">
                <el-icon><Setting /></el-icon>
                <span>快速操作</span>
              </div>
            </template>

            <div class="quick-actions">
              <el-button class="action-button" @click="$router.push('/favorites')">
                <el-icon><Star /></el-icon>
                我的收藏
              </el-button>
              <el-button class="action-button" @click="$router.push('/wrong-questions')">
                <el-icon><Warning /></el-icon>
                错题本
              </el-button>
              <el-button class="action-button" @click="$router.push('/statistics')">
                <el-icon><TrendCharts /></el-icon>
                统计分析
              </el-button>
              <el-button class="action-button" @click="exportData">
                <el-icon><Download /></el-icon>
                导出数据
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  User, Lock, TrendCharts, InfoFilled, Setting,
  Edit, Check, Star, Trophy, Warning, Download
} from '@element-plus/icons-vue'
import AppLayout from '@/components/common/AppLayout.vue'
import AvatarUpload from '@/components/common/AvatarUpload.vue'
import { useUserStore } from '@/store/user'
import * as authApi from '@/api/auth'
import * as statisticsApi from '@/api/statistics'
import type { User as UserType, AnswerStats, UploadResponse } from '@/types/index'

const userStore = useUserStore()

// 表单引用
const profileFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

// 加载状态
const updating = ref(false)
const updatingPassword = ref(false)

// 个人信息表单
const profileForm = reactive<Partial<UserType>>({
  username: '',
  nickname: '',
  email: '',
  avatar: ''
})

// 密码修改表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 用户统计数据
const userStats = ref({
  totalAnswers: 0,
  correctAnswers: 0,
  accuracy: 0
})

// 表单验证规则
const profileRules: FormRules = {
  nickname: [
    { max: 50, message: '昵称长度不能超过50个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ]
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 头像上传成功回调
const handleAvatarUploadSuccess = (response: UploadResponse) => {
  profileForm.avatar = response.url
  ElMessage.success('头像上传成功')
}

// 头像上传失败回调
const handleAvatarUploadError = (error: any) => {
  ElMessage.error(error.message || '头像上传失败')
}

// 初始化表单数据
const initializeForm = () => {
  if (userStore.userInfo) {
    Object.assign(profileForm, {
      username: userStore.userInfo.username,
      nickname: userStore.userInfo.nickname,
      email: userStore.userInfo.email,
      avatar: userStore.userInfo.avatar
    })
  }
}

// 更新个人信息
const updateProfile = async () => {
  if (!profileFormRef.value) return

  try {
    await profileFormRef.value.validate()
    
    updating.value = true
    
    // 调用更新API
    await userStore.updateUserInfo({
      nickname: profileForm.nickname,
      email: profileForm.email,
      avatar: profileForm.avatar
    })
    
    ElMessage.success('个人信息更新成功')
    
  } catch (error: any) {
    if (error.message) {
      ElMessage.error(error.message)
    }
  } finally {
    updating.value = false
  }
}

// 重置表单
const resetForm = () => {
  initializeForm()
}

// 修改密码
const updatePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
    
    updatingPassword.value = true
    
    await authApi.updatePassword(passwordForm)
    
    ElMessage.success('密码修改成功，请重新登录')
    
    // 清空表单
    resetPasswordForm()
    
    // 退出登录
    setTimeout(() => {
      userStore.logout()
      location.reload()
    }, 1000)
    
  } catch (error: any) {
    if (error.message) {
      ElMessage.error(error.message)
    }
  } finally {
    updatingPassword.value = false
  }
}

// 重置密码表单
const resetPasswordForm = () => {
  Object.assign(passwordForm, {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
  passwordFormRef.value?.clearValidate()
}


// 获取用户统计数据
const fetchUserStats = async () => {
  try {
    const stats = await statisticsApi.getUserStats()
    userStats.value = {
      totalAnswers: stats.totalAnswers || 0,
      correctAnswers: stats.correctAnswers || 0,
      accuracy: Math.round((stats.accuracy || 0) * 100) / 100
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    // 设置默认值
    userStats.value = {
      totalAnswers: 0,
      correctAnswers: 0,
      accuracy: 0
    }
  }
}

// 导出数据
const exportData = async () => {
  try {
    const blob = await statisticsApi.exportStatisticsReport({
      format: 'text'
    })

    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `个人数据导出_${new Date().toISOString().split('T')[0]}.txt`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('数据导出成功')
  } catch (error) {
    console.error('导出失败:', error)
  }
}

// 工具函数
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(() => {
  initializeForm()
  fetchUserStats()
})
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-title {
  font-size: 32px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.page-description {
  font-size: 16px;
  color: #606266;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.profile-card,
.password-card {
  margin-bottom: 24px;
}


.stats-card,
.account-info-card,
.quick-actions-card {
  margin-bottom: 24px;
}

.stats-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 50%;
  font-size: 18px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.account-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  color: #606266;
  font-size: 14px;
}

.info-value {
  color: #303133;
  font-weight: 500;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-button {
  justify-content: flex-start;
  padding: 16px;
  height: auto;
  margin: 0 !important;
}

.action-button .el-icon {
  margin-right: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-title {
    font-size: 24px;
  }


  .stat-item {
    padding: 12px;
  }

  .stat-value {
    font-size: 20px;
  }

  .quick-actions {
    gap: 8px;
  }

  .action-button {
    padding: 12px;
  }
}
</style>
