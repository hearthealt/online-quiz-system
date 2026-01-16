<template>
  <div v-if="showWarning" class="token-warning">
    <el-alert
      :title="`登录即将过期，剩余时间：${remainingTimeText}`"
      type="warning"
      show-icon
      :closable="false"
    >
      <template #default>
        <div class="warning-content">
          <span>为了不影响您的使用，建议重新登录</span>
          <el-button type="primary" size="small" @click="refreshLogin">
            重新登录
          </el-button>
        </div>
      </template>
    </el-alert>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getTokenRemainingTime, formatRemainingTime } from '@/utils/token'

const router = useRouter()
const userStore = useUserStore()

const showWarning = ref(false)
const remainingTimeText = ref('')
let timer: ReturnType<typeof setInterval> | null = null

// 检查token状态
const checkTokenStatus = () => {
  const token = localStorage.getItem('token')
  if (!token || !userStore.isLoggedIn) {
    showWarning.value = false
    return
  }

  const remainingSeconds = getTokenRemainingTime(token)
  remainingTimeText.value = formatRemainingTime(remainingSeconds)

  // 如果剩余时间少于30分钟，显示警告
  if (remainingSeconds > 0 && remainingSeconds < 30 * 60) {
    showWarning.value = true
  } else {
    showWarning.value = false
  }

  // 如果已过期，自动清理
  if (remainingSeconds <= 0) {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
  }
}

// 重新登录
const refreshLogin = () => {
  const currentPath = router.currentRoute.value.fullPath
  router.push(`/login?redirect=${encodeURIComponent(currentPath)}`)
}

onMounted(() => {
  // 立即检查一次
  checkTokenStatus()
  
  // 每分钟检查一次
  timer = setInterval(checkTokenStatus, 60 * 1000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.token-warning {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 2000;
  width: 400px;
  max-width: 90vw;
}

.warning-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

@media (max-width: 768px) {
  .token-warning {
    top: 10px;
    right: 10px;
    left: 10px;
    width: auto;
  }
  
  .warning-content {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
  }
}</style>
