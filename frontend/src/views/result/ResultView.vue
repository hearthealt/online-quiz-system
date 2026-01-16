<template>
  <AppLayout>
    <div class="result-container">
      <!-- 加载中 -->
      <div v-if="loading" class="loading-wrapper">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <template v-else-if="session">
        <!-- 结果概览 -->
        <div class="result-header">
          <div class="result-icon" :class="accuracy >= 60 ? 'pass' : 'fail'">
            <el-icon :size="48"><Trophy v-if="accuracy >= 60" /><Warning v-else /></el-icon>
          </div>
          <h1 class="result-title">{{ accuracy >= 60 ? '恭喜通过' : '继续加油' }}</h1>
          <p class="result-subtitle">{{ session.bankName }} - {{ session.mode === 'practice' ? '练习模式' : '考试模式' }}</p>
        </div>

        <!-- 统计数据 -->
        <div class="stats-cards">
          <div class="stat-card">
            <div class="stat-value">{{ correctCount }}</div>
            <div class="stat-label">正确题数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ totalQuestions }}</div>
            <div class="stat-label">总题数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value accuracy" :class="accuracy >= 60 ? 'pass' : 'fail'">{{ accuracy }}%</div>
            <div class="stat-label">正确率</div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button type="primary" size="large" @click="reviewAnswers">
            <el-icon><Document /></el-icon>
            查看详情
          </el-button>
          <el-button size="large" @click="retryQuiz">
            <el-icon><Refresh /></el-icon>
            重新答题
          </el-button>
          <el-button size="large" @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回题库
          </el-button>
        </div>

        <!-- 答题详情 -->
        <div v-if="showDetail" class="detail-section">
          <h2>答题详情</h2>
          <div v-for="(item, index) in detailList" :key="item.questionId" class="detail-item">
            <div class="detail-header">
              <span class="detail-num">{{ index + 1 }}</span>
              <el-tag :type="item.isCorrect ? 'success' : 'danger'" size="small">
                {{ item.isCorrect ? '正确' : '错误' }}
              </el-tag>
              <el-tag type="info" size="small">{{ getQuestionTypeLabel(item.question?.type) }}</el-tag>
            </div>
            <div class="detail-content" v-html="item.question?.content"></div>
            <div class="detail-answer">
              <div class="answer-row">
                <span class="answer-label">您的答案：</span>
                <span :class="item.isCorrect ? 'correct' : 'wrong'">{{ formatAnswer(item.userAnswer, item.question) }}</span>
              </div>
              <div class="answer-row" v-if="!item.isCorrect">
                <span class="answer-label">正确答案：</span>
                <span class="correct">{{ formatAnswer(item.question?.correctAnswer, item.question) }}</span>
              </div>
            </div>
            <div v-if="item.question?.analysis" class="detail-analysis">
              <strong>解析：</strong>
              <div v-html="item.question.analysis"></div>
            </div>
          </div>
        </div>
      </template>

      <!-- 空状态 -->
      <el-empty v-else-if="!loading" description="未找到答题记录">
        <el-button type="primary" @click="goBack">返回题库</el-button>
      </el-empty>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Trophy, Warning, Document, Refresh, Loading } from '@element-plus/icons-vue'
import AppLayout from '@/components/common/AppLayout.vue'
import type { Question, QuizSession, UserAnswer } from '@/types/index'
import * as sessionApi from '@/api/session'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const session = ref<QuizSession | null>(null)
const questions = ref<Question[]>([])
const userAnswers = ref<UserAnswer[]>([])
const showDetail = ref(false)

const sessionId = computed(() => String(route.params.sessionId))
const totalQuestions = computed(() => session.value?.totalQuestions || 0)
const correctCount = computed(() => session.value?.correctCount || 0)
const accuracy = computed(() => {
  if (totalQuestions.value === 0) return 0
  return Math.round((correctCount.value / totalQuestions.value) * 100)
})

interface DetailItem {
  questionId: number
  question?: Question
  userAnswer: string
  isCorrect: boolean
}

const detailList = computed<DetailItem[]>(() => {
  return userAnswers.value.map(ua => {
    const question = questions.value.find(q => q.id === ua.questionId)
    return {
      questionId: ua.questionId,
      question,
      userAnswer: ua.userAnswer || '',
      isCorrect: ua.isCorrect || false
    }
  })
})

// 初始化
const init = async () => {
  loading.value = true
  try {
    const detail = await sessionApi.getSessionDetail(sessionId.value)
    session.value = detail.session
    questions.value = detail.questions
    userAnswers.value = detail.answers
  } catch (error: any) {
    console.error('获取结果失败:', error)
    ElMessage.error(error.message || '获取结果失败')
  } finally {
    loading.value = false
  }
}

// 查看详情
const reviewAnswers = () => {
  showDetail.value = !showDetail.value
}

// 重新答题
const retryQuiz = async () => {
  if (!session.value?.id) return

  try {
    await sessionApi.resetSession(session.value.bankId!, session.value.mode!)
    if (session.value.mode === 'practice') {
      router.push(`/practice/${session.value.bankId}`)
    } else {
      router.push(`/exam/${session.value.bankId}`)
    }
  } catch (error) {
    console.error('重置失败:', error)
    ElMessage.error('重置失败')
  }
}

// 返回题库
const goBack = () => {
  router.push('/banks')
}

// 获取题型标签
const getQuestionTypeLabel = (type?: string) => {
  if (!type) return ''
  const labels: Record<string, string> = {
    single: '单选题',
    multiple: '多选题',
    judge: '判断题',
    essay: '简答题'
  }
  return labels[type] || type
}

// 格式化答案
const formatAnswer = (answer?: string, question?: Question) => {
  if (!answer) return '未作答'
  if (!question) return answer

  if (question.type === 'judge') {
    // 支持多种格式：true/false, 正确/错误, 对/错, 是/否, 1/0
    const normalized = answer.toLowerCase()
    if (normalized === 'true' || answer === '正确' ||
        answer === '对' || answer === '是' || answer === '1') {
      return '正确'
    }
    return '错误'
  }

  if ((question.type === 'single' || question.type === 'multiple') && question.options) {
    const answers = answer.split(',')
    return answers.map(a => {
      const index = a.charCodeAt(0) - 65
      if (index >= 0 && index < question.options!.length) {
        return `${a}. ${question.options![index]}`
      }
      return a
    }).join('; ')
  }

  return answer
}

onMounted(() => {
  init()
})
</script>

<style scoped>
.result-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.loading-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  color: #909399;
}

.result-header {
  text-align: center;
  padding: 40px 20px;
  background: white;
  border-radius: 16px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.result-icon {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.result-icon.pass {
  background: linear-gradient(135deg, #67c23a, #85ce61);
  color: white;
}

.result-icon.fail {
  background: linear-gradient(135deg, #e6a23c, #f0c78a);
  color: white;
}

.result-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.result-subtitle {
  color: #909399;
  font-size: 14px;
}

.stats-cards {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  flex: 1;
  background: white;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 8px;
}

.stat-value.accuracy.pass {
  color: #67c23a;
}

.stat-value.accuracy.fail {
  color: #e6a23c;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 32px;
}

.detail-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.detail-section h2 {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.detail-item {
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 16px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.detail-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
}

.detail-content {
  font-size: 16px;
  line-height: 1.6;
  color: #303133;
  margin-bottom: 16px;
}

.detail-answer {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.answer-row {
  margin-bottom: 8px;
}

.answer-row:last-child {
  margin-bottom: 0;
}

.answer-label {
  color: #909399;
  margin-right: 8px;
}

.answer-row .correct {
  color: #67c23a;
}

.answer-row .wrong {
  color: #f56c6c;
}

.detail-analysis {
  padding: 16px;
  background: #fdf6ec;
  border-radius: 8px;
  border-left: 4px solid #e6a23c;
}

.detail-analysis strong {
  color: #e6a23c;
}

@media (max-width: 768px) {
  .stats-cards {
    flex-direction: column;
  }

  .action-buttons {
    flex-direction: column;
  }
}
</style>
