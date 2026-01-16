<template>
  <div class="exam-timer" :class="{ warning: isWarning, danger: isDanger }">
    <div class="timer-display">
      <el-icon class="timer-icon">
        <Timer />
      </el-icon>
      <div class="time-info">
        <span class="timer-text">{{ formatTime(remainingTime) }}</span>
        <div class="time-details">
          <span class="time-label">剩余时间</span>
          <span class="time-percentage">{{ Math.round(progressPercentage) }}%</span>
        </div>
      </div>
    </div>

    <div class="timer-progress">
      <el-progress
        :percentage="progressPercentage"
        :color="progressColor"
        :stroke-width="6"
        :show-text="false"
      />
      <div class="progress-labels">
        <span class="progress-start">0</span>
        <span class="progress-end">{{ formatTime(totalTime) }}</span>
      </div>
    </div>

    <div class="timer-status" v-if="isWarning || isDanger">
      <el-icon v-if="isDanger" class="warning-icon danger-icon">
        <Warning />
      </el-icon>
      <el-icon v-else-if="isWarning" class="warning-icon warning-icon">
        <Clock />
      </el-icon>
      <span class="status-text">
        {{ getStatusText() }}
      </span>
    </div>

    <!-- 时间提醒弹窗 -->
    <div v-if="showTimeAlert" class="time-alert" :class="{ danger: isDanger }">
      <div class="alert-content">
        <el-icon class="alert-icon">
          <Warning />
        </el-icon>
        <div class="alert-text">
          <div class="alert-title">{{ getAlertTitle() }}</div>
          <div class="alert-message">{{ getAlertMessage() }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, watch, ref, onUnmounted } from 'vue'
import { Timer, Warning, Clock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

interface Props {
  totalTime: number
  remainingTime: number
  warningTime?: number
  dangerTime?: number
  enableSounds?: boolean
}

interface Emits {
  (e: 'timeUp'): void
  (e: 'warning'): void
  (e: 'danger'): void
}

const props = withDefaults(defineProps<Props>(), {
  warningTime: 300, // 5分钟警告
  dangerTime: 60,   // 1分钟紧急
  enableSounds: true
})

const emit = defineEmits<Emits>()

const hasWarned = ref(false)
const hasDangered = ref(false)
const showTimeAlert = ref(false)
const alertTimer = ref<ReturnType<typeof setTimeout> | null>(null)

// 计算属性
const isWarning = computed(() => {
  return props.remainingTime <= props.warningTime && props.remainingTime > props.dangerTime
})

const isDanger = computed(() => {
  return props.remainingTime <= props.dangerTime && props.remainingTime > 0
})

const progressPercentage = computed(() => {
  if (props.totalTime <= 0) return 0
  return Math.max(0, Math.min(100, (props.remainingTime / props.totalTime) * 100))
})

const progressColor = computed(() => {
  if (isDanger.value) return '#f56c6c'
  if (isWarning.value) return '#e6a23c'
  return '#409eff'
})

// 格式化时间
const formatTime = (seconds: number) => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const remainingSeconds = seconds % 60

  if (hours > 0) {
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`
  } else {
    return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`
  }
}

// 获取状态文本
const getStatusText = () => {
  if (isDanger.value) {
    return `⚠️ 时间紧急！还剩 ${Math.floor(props.remainingTime / 60)} 分钟`
  }
  if (isWarning.value) {
    return `⏰ 注意时间！还剩 ${Math.floor(props.remainingTime / 60)} 分钟`
  }
  return ''
}

// 获取警告标题
const getAlertTitle = () => {
  if (isDanger.value) {
    return '时间紧急！'
  }
  if (isWarning.value) {
    return '时间提醒'
  }
  return ''
}

// 获取警告消息
const getAlertMessage = () => {
  if (isDanger.value) {
    return `考试即将结束，请尽快完成答题！剩余时间：${formatTime(props.remainingTime)}`
  }
  if (isWarning.value) {
    return `考试时间已过半，请注意答题进度！剩余时间：${formatTime(props.remainingTime)}`
  }
  return ''
}

// 显示时间提醒
const showAlert = () => {
  showTimeAlert.value = true
  if (alertTimer.value) {
    clearTimeout(alertTimer.value)
  }
  alertTimer.value = setTimeout(() => {
    showTimeAlert.value = false
  }, 5000) // 5秒后自动隐藏
}

// 播放提示音
const playSound = (type: 'warning' | 'danger' | 'timeup') => {
  if (!props.enableSounds) return

  try {
    const audio = new Audio()
    switch (type) {
      case 'warning':
        // 可以添加警告音效文件
        audio.src = 'data:audio/wav;base64,UklGRnoGAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQoGAACBhYqFbF1fdJivrJBhNjVgodDbq2EcBj+a2/LDciUFLIHO8tiJNwgZaLvt559NEAxQp+PwtmMcBjiR1/LMeSwFJHfH8N2QQAoUXrTp66hVFApGn+DyvmMcBjiS2fPEdiMFl2y+7taXVhMJPpHY88p9IgYoeuPuznwZBIeN2fzCefIEE7rh7K+QOgQScN3vsncsRi4LZ/3xsZdGAQIMUrXowJoKAiL12u7y'
        break
      case 'danger':
        // 可以添加紧急音效文件
        audio.src = 'data:audio/wav;base64,UklGRnoGAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQoGAACBhYqFbF1fdJivrJBhNjVgodDbq2EcBj+a2/LDciUFLIHO8tiJNwgZaLvt559NEAxQp+PwtmMcBjiR1/LMeSwFJHfH8N2QQAoUXrTp66hVFApGn+DyvmMcBjiS2fPEdiMFl2y+7taXVhMJPpHY88p9IgYoeuPuznwZBIeN2fzCefIEE7rh7K+QOgQScN3vsncsRi4LZ/3xsZdGAQIMUrXowJoKAiL12u7y'
        break
      case 'timeup':
        // 可以添加时间到音效文件
        audio.src = 'data:audio/wav;base64,UklGRnoGAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQoGAACBhYqFbF1fdJivrJBhNjVgodDbq2EcBj+a2/LDciUFLIHO8tiJNwgZaLvt559NEAxQp+PwtmMcBjiR1/LMeSwFJHfH8N2QQAoUXrTp66hVFApGn+DyvmMcBjiS2fPEdiMFl2y+7taXVhMJPpHY88p9IgYoeuPuznwZBIeN2fzCefIEE7rh7K+QOgQScN3vsncsRi4LZ/3xsZdGAQIMUrXowJoKAiL12u7y'
        break
    }
    audio.volume = 0.3
    audio.play().catch(() => {
      // 忽略播放失败的错误（浏览器可能阻止自动播放）
    })
  } catch (error) {
    // 忽略音频播放错误
  }
}

// 监听时间变化
watch(() => props.remainingTime, (newTime, oldTime) => {
  // 时间到了
  if (newTime <= 0 && oldTime > 0) {
    playSound('timeup')
    emit('timeUp')
    return
  }

  // 进入危险状态
  if (newTime <= props.dangerTime && !hasDangered.value) {
    hasDangered.value = true
    playSound('danger')
    emit('danger')
    showAlert()
    ElMessage.error({
      message: `⚠️ 时间紧急！还剩 ${Math.floor(newTime / 60)} 分钟`,
      duration: 5000,
      showClose: true
    })
    return
  }

  // 进入警告状态
  if (newTime <= props.warningTime && !hasWarned.value && newTime > props.dangerTime) {
    hasWarned.value = true
    playSound('warning')
    emit('warning')
    showAlert()
    ElMessage.warning({
      message: `⏰ 注意时间！还剩 ${Math.floor(newTime / 60)} 分钟`,
      duration: 4000,
      showClose: true
    })
  }

  // 每30秒提醒一次（在警告和危险状态下）
  if ((isWarning.value || isDanger.value) && newTime % 30 === 0 && newTime > 0) {
    ElMessage.info({
      message: `⏱️ 剩余时间：${formatTime(newTime)}`,
      duration: 2000
    })
  }
})

// 重置警告状态
const resetWarnings = () => {
  hasWarned.value = false
  hasDangered.value = false
  showTimeAlert.value = false
  if (alertTimer.value) {
    clearTimeout(alertTimer.value)
    alertTimer.value = null
  }
}

// 暴露方法给父组件
defineExpose({
  resetWarnings
})

onUnmounted(() => {
  resetWarnings()
})
</script>

<style scoped>
.exam-timer {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 12px;
  border: 2px solid #b3d8ff;
  transition: all 0.3s ease;
  min-width: 180px;
  position: relative;
  box-shadow: 0 4px 20px rgba(64, 158, 255, 0.1);
}

.exam-timer.warning {
  background: linear-gradient(135deg, #fffbf0 0%, #fef3e2 100%);
  border-color: #f5c74e;
  animation: pulse-warning 2s infinite;
}

.exam-timer.danger {
  background: linear-gradient(135deg, #fff5f5 0%, #fed7d7 100%);
  border-color: #f56c6c;
  animation: pulse-danger 1s infinite;
}

.timer-display {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.time-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.time-details {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 10px;
  color: #666;
}

.time-label {
  font-weight: 500;
}

.time-percentage {
  font-weight: 600;
  color: #409eff;
}

.timer-icon {
  font-size: 20px;
  color: #409eff;
  transition: color 0.3s;
}

.exam-timer.warning .timer-icon {
  color: #e6a23c;
}

.exam-timer.danger .timer-icon {
  color: #f56c6c;
}

.timer-text {
  font-size: 22px;
  font-weight: 700;
  color: #409eff;
  font-family: 'Courier New', monospace;
  letter-spacing: 1px;
  transition: color 0.3s;
}

.exam-timer.warning .timer-text {
  color: #e6a23c;
}

.exam-timer.danger .timer-text {
  color: #f56c6c;
}

.timer-progress {
  width: 100%;
  max-width: 160px;
  position: relative;
}

.progress-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 2px;
  font-size: 9px;
  color: #999;
}

.timer-status {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
}

.warning-icon {
  font-size: 14px;
}

.warning-icon.warning-icon {
  color: #e6a23c;
}

.warning-icon.danger-icon {
  color: #f56c6c;
  animation: shake 0.5s infinite;
}

.status-text {
  color: #666;
}

.exam-timer.warning .status-text {
  color: #e6a23c;
}

.exam-timer.danger .status-text {
  color: #f56c6c;
}

/* 时间提醒弹窗 */
.time-alert {
  position: absolute;
  top: -80px;
  left: 50%;
  transform: translateX(-50%);
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  border: 2px solid #e6a23c;
  z-index: 1000;
  animation: slideDown 0.3s ease-out;
}

.time-alert.danger {
  border-color: #f56c6c;
}

.alert-content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  min-width: 280px;
}

.alert-icon {
  font-size: 24px;
  color: #e6a23c;
}

.time-alert.danger .alert-icon {
  color: #f56c6c;
}

.alert-text {
  flex: 1;
}

.alert-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.alert-message {
  font-size: 14px;
  color: #666;
  line-height: 1.4;
}

/* 动画效果 */
@keyframes pulse-warning {
  0% {
    box-shadow: 0 0 0 0 rgba(230, 162, 60, 0.4);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(230, 162, 60, 0.1);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(230, 162, 60, 0);
  }
}

@keyframes pulse-danger {
  0% {
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.6);
  }
  50% {
    box-shadow: 0 0 0 10px rgba(245, 108, 108, 0.2);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0);
  }
}

@keyframes shake {
  0%, 100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-2px);
  }
  75% {
    transform: translateX(2px);
  }
}

@keyframes slideDown {
  0% {
    opacity: 0;
    transform: translateX(-50%) translateY(-20px);
  }
  100% {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .exam-timer {
    min-width: 150px;
    padding: 10px 12px;
  }

  .timer-text {
    font-size: 18px;
  }

  .timer-icon {
    font-size: 16px;
  }

  .time-details {
    font-size: 9px;
  }

  .timer-status {
    font-size: 10px;
  }

  .warning-icon {
    font-size: 12px;
  }

  .alert-content {
    min-width: 200px;
    padding: 10px 12px;
  }

  .alert-title {
    font-size: 12px;
  }

  .alert-message {
    font-size: 10px;
  }
}
</style>