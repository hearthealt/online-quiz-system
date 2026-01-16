<template>
  <AppLayout>
    <div class="start-quiz-page">
      <div class="main-content">
        <!-- 页面标题 -->
        <div class="page-header">
          <h1 class="page-title">开始答题</h1>
          <p class="page-desc">选择题库开始练习或考试</p>
        </div>
        <!-- 继续答题区域 -->
        <div v-if="ongoingSessions.length > 0" class="ongoing-section">
          <div class="section-header">
            <div class="section-title">
              <el-icon class="title-icon"><Clock /></el-icon>
              <span>继续答题</span>
            </div>
            <span class="section-badge">{{ ongoingSessions.length }} 个进行中</span>
          </div>
          <div class="ongoing-list">
            <div
              v-for="session in ongoingSessions"
              :key="session.id"
              class="ongoing-card"
              @click="continueSession(session)"
            >
              <div class="ongoing-info">
                <div class="ongoing-icon" :class="session.mode">
                  <el-icon v-if="session.mode === 'practice'"><EditPen /></el-icon>
                  <el-icon v-else-if="session.mode === 'exam'"><Trophy /></el-icon>
                  <el-icon v-else-if="session.mode === 'favorite'"><Star /></el-icon>
                  <el-icon v-else-if="session.mode === 'wrong'"><Warning /></el-icon>
                  <el-icon v-else><EditPen /></el-icon>
                </div>
                <div class="ongoing-detail">
                  <div class="ongoing-name">{{ session.bankName }}</div>
                  <div class="ongoing-meta">
                    <el-tag :type="getModeTagType(session.mode)" size="small" effect="plain">
                      {{ getModeLabel(session.mode) }}
                    </el-tag>
                    <span class="ongoing-progress-text">
                      已完成 {{ session.answeredQuestions ?? 0 }} / {{ session.totalQuestions ?? 0 }} 题
                    </span>
                  </div>
                </div>
              </div>
              <div class="ongoing-progress">
                <el-progress
                  :percentage="session.totalQuestions ? Math.round(((session.answeredQuestions ?? 0) / session.totalQuestions) * 100) : 0"
                  :stroke-width="8"
                  :show-text="false"
                  :color="getModeColor(session.mode)"
                />
              </div>
              <div class="ongoing-actions">
                <el-button type="primary" size="small" @click.stop="continueSession(session)">
                  继续
                </el-button>
                <el-button size="small" @click.stop="resetSessionConfirm(session)">
                  重置
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 题库列表 -->
        <div class="banks-section">
          <div class="section-header">
            <div class="section-title">
              <el-icon class="title-icon"><Grid /></el-icon>
              <span>全部题库</span>
            </div>
          </div>

          <div class="banks-grid" v-loading="loading">
            <div
              v-for="bank in banks"
              :key="bank.id"
              class="bank-card"
            >
              <div class="bank-card-header">
                <div class="bank-icon" :style="{ background: getBankGradient(bank.id!) }">
                  <el-icon :size="28"><Reading /></el-icon>
                </div>
                <div class="bank-count">
                  <span class="count-num">{{ bank.questionCount || 0 }}</span>
                  <span class="count-label">题</span>
                </div>
              </div>

              <div class="bank-card-body">
                <h3 class="bank-name">{{ bank.name }}</h3>
                <p class="bank-desc">{{ bank.description || '暂无描述' }}</p>
              </div>

              <div class="bank-card-footer">
                <el-button
                  class="action-btn practice-btn"
                  @click="startPractice(bank)"
                  :disabled="!bank.questionCount"
                >
                  <el-icon><EditPen /></el-icon>
                  顺序练习
                </el-button>
                <el-button
                  class="action-btn exam-btn"
                  @click="startExam(bank)"
                  :disabled="!bank.questionCount"
                >
                  <el-icon><Trophy /></el-icon>
                  考试模式
                </el-button>
              </div>
            </div>
          </div>

          <el-empty
            v-if="!loading && banks.length === 0"
            description="暂无可用题库"
            :image-size="150"
          />
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Reading, Clock, EditPen, Trophy, Grid, Star, Warning } from '@element-plus/icons-vue'
import AppLayout from '@/components/common/AppLayout.vue'
import type { QuestionBank, QuizSession } from '@/types/index'
import * as bankApi from '@/api/bank'
import * as sessionApi from '@/api/session'

const router = useRouter()
const loading = ref(false)
const banks = ref<QuestionBank[]>([])
const ongoingSessions = ref<QuizSession[]>([])

// 计算总题目数
const totalQuestions = computed(() => {
  return banks.value.reduce((sum, bank) => sum + (bank.questionCount || 0), 0)
})

// 获取题库渐变色
const getBankGradient = (id: number) => {
  const gradients = [
    'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
    'linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%)',
    'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)'
  ]
  return gradients[id % gradients.length]
}

// 获取题库列表
const fetchBanks = async () => {
  loading.value = true
  try {
    const result = await bankApi.getAllBanks()
    banks.value = result
  } catch (error) {
    console.error('获取题库列表失败:', error)
    ElMessage.error('获取题库列表失败')
  } finally {
    loading.value = false
  }
}

// 获取进行中的会话
const fetchOngoingSessions = async () => {
  try {
    const result = await sessionApi.getOngoingSessions()
    ongoingSessions.value = result
  } catch (error) {
    console.error('获取进行中会话失败:', error)
  }
}

// 开始练习
const startPractice = (bank: QuestionBank) => {
  router.push(`/practice/${bank.id}`)
}

// 开始考试
const startExam = (bank: QuestionBank) => {
  router.push(`/exam/${bank.id}`)
}

// 获取模式标签
const getModeLabel = (mode?: string) => {
  const labels: Record<string, string> = {
    practice: '练习',
    exam: '考试',
    favorite: '收藏练习',
    wrong: '错题练习'
  }
  return labels[mode || ''] || '练习'
}

// 获取模式标签类型
const getModeTagType = (mode?: string) => {
  const types: Record<string, string> = {
    practice: 'success',
    exam: 'warning',
    favorite: '',
    wrong: 'danger'
  }
  return types[mode || ''] || 'success'
}

// 获取模式进度条颜色
const getModeColor = (mode?: string) => {
  const colors: Record<string, string> = {
    practice: '#67c23a',
    exam: '#e6a23c',
    favorite: '#409eff',
    wrong: '#f56c6c'
  }
  return colors[mode || ''] || '#67c23a'
}

// 继续会话
const continueSession = (session: QuizSession) => {
  const mode = session.mode
  if (mode === 'exam') {
    router.push(`/exam/${session.bankId}?sessionId=${session.sessionId}`)
  } else {
    // practice, favorite, wrong 都走练习页面
    router.push(`/practice/${session.bankId}?sessionId=${session.sessionId}&mode=${mode}`)
  }
}

// 重置会话确认
const resetSessionConfirm = (session: QuizSession) => {
  ElMessageBox.confirm(
    '确定要重新开始吗？当前进度将会清除。',
    '重置确认',
    {
      confirmButtonText: '确定重置',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await sessionApi.resetSession(session.bankId!, session.mode!)
      ElMessage.success('已重置，可以重新开始')
      fetchOngoingSessions()
    } catch (error) {
      console.error('重置会话失败:', error)
      ElMessage.error('重置失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchBanks()
  fetchOngoingSessions()
})
</script>

<style scoped>
.start-quiz-page {
  min-height: 100vh;
  background: #f5f7fa;
}

/* Hero 区域 */
.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 48px 24px;
  position: relative;
  overflow: hidden;
}

.hero-content {
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
  text-align: center;
}

.hero-title {
  font-size: 36px;
  font-weight: 700;
  color: white;
  margin: 0 0 12px 0;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.hero-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0 0 32px 0;
}

.hero-stats {
  display: inline-flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 20px 40px;
  gap: 32px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: white;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 6px;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.3);
}

/* 装饰圆圈 */
.hero-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -50px;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -80px;
  left: 10%;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 20%;
  left: -30px;
}

/* 主内容区 */
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
}

/* Section 通用样式 */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
}

.title-icon {
  font-size: 24px;
  color: #667eea;
}

.section-badge {
  background: #667eea;
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
}

/* 继续答题区域 */
.ongoing-section {
  margin-bottom: 40px;
}

.ongoing-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ongoing-card {
  background: white;
  border-radius: 16px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #ebeef5;
}

.ongoing-card:hover {
  transform: translateX(4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  border-color: #667eea;
}

.ongoing-info {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.ongoing-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: white;
}

.ongoing-icon.practice {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.ongoing-icon.exam {
  background: linear-gradient(135deg, #e6a23c, #f5c14e);
}

.ongoing-icon.favorite {
  background: linear-gradient(135deg, #409eff, #66b1ff);
}

.ongoing-icon.wrong {
  background: linear-gradient(135deg, #f56c6c, #f89898);
}

.ongoing-detail {
  flex: 1;
}

.ongoing-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 6px;
}

.ongoing-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ongoing-progress-text {
  color: #909399;
  font-size: 13px;
}

.ongoing-progress {
  width: 200px;
}

.ongoing-actions {
  display: flex;
  gap: 8px;
}

/* 题库列表 */
.banks-section {
  margin-bottom: 40px;
}

.banks-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.bank-card {
  background: white;
  border-radius: 20px;
  padding: 24px;
  transition: all 0.3s ease;
  border: 1px solid #ebeef5;
}

.bank-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.1);
  border-color: transparent;
}

.bank-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.bank-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.bank-count {
  text-align: right;
}

.count-num {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1;
}

.count-label {
  font-size: 14px;
  color: #909399;
}

.bank-card-body {
  margin-bottom: 20px;
}

.bank-name {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bank-desc {
  font-size: 14px;
  color: #909399;
  margin: 0;
  height: 42px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.bank-card-footer {
  display: flex;
  gap: 10px;
}

.action-btn {
  flex: 1;
  height: 42px;
  border-radius: 10px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.practice-btn {
  background: linear-gradient(135deg, #67c23a, #85ce61);
  border: none;
  color: white;
}

.practice-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #5daf34, #73c255);
}

.practice-btn:disabled {
  background: #e4e7ed;
  color: #c0c4cc;
}

.exam-btn {
  background: linear-gradient(135deg, #e6a23c, #f5c14e);
  border: none;
  color: white;
}

.exam-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #d69230, #e8b540);
}

.exam-btn:disabled {
  background: #e4e7ed;
  color: #c0c4cc;
}

/* 响应式 */
@media (max-width: 768px) {
  .hero-section {
    padding: 32px 16px;
  }

  .hero-title {
    font-size: 26px;
  }

  .hero-stats {
    padding: 16px 24px;
    gap: 20px;
  }

  .stat-value {
    font-size: 24px;
  }

  .main-content {
    padding: 20px 16px;
  }

  .ongoing-card {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  .ongoing-progress {
    width: 100%;
  }

  .ongoing-actions {
    justify-content: flex-end;
  }

  .banks-grid {
    grid-template-columns: 1fr;
  }

  .bank-card-footer {
    flex-direction: column;
  }
}
</style>
