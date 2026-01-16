<template>
  <AppLayout>
    <div class="exam-page">
      <!-- 加载中 -->
      <div v-if="loading" class="loading-wrapper">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <!-- 考试区域 -->
      <template v-else-if="questions.length > 0">
        <!-- 顶部导航 -->
        <div class="top-bar">
          <div class="top-left">
            <el-button text @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <span class="bank-name">{{ bankName }}</span>
            <el-tag type="warning" size="small">考试模式</el-tag>
          </div>
          <div class="top-center">
            <button
              class="nav-btn prev"
              :disabled="currentIndex === 0"
              @click="prevQuestion"
            >
              <el-icon><ArrowLeft /></el-icon>
              <span>上一题</span>
            </button>
            <div class="nav-progress">
              <span class="current">{{ currentIndex + 1 }}</span>
              <span class="separator">/</span>
              <span class="total">{{ questions.length }}</span>
            </div>
            <button
              class="nav-btn next"
              :disabled="currentIndex >= questions.length - 1"
              @click="nextQuestion"
            >
              <span>下一题</span>
              <el-icon><ArrowRight /></el-icon>
            </button>
          </div>
          <div class="top-right">
            <el-button type="warning" @click="submitExamConfirm">
              <el-icon><Finished /></el-icon>
              交卷
            </el-button>
          </div>
        </div>

        <div class="main-content">
          <!-- 左侧答题卡 -->
          <div class="sidebar">
            <div class="sidebar-card">
              <div class="sidebar-title">
                <span>答题卡</span>
              </div>
              <div class="progress-info">
                <div class="progress-row">
                  <span class="progress-label">已答</span>
                  <span class="progress-value answered">{{ answeredCount }}</span>
                </div>
                <div class="progress-row">
                  <span class="progress-label">未答</span>
                  <span class="progress-value unanswered">{{ questions.length - answeredCount }}</span>
                </div>
                <div class="progress-row">
                  <span class="progress-label">总计</span>
                  <span class="progress-value total">{{ questions.length }}</span>
                </div>
              </div>
              <el-progress
                :percentage="(answeredCount / questions.length) * 100"
                :show-text="false"
                :stroke-width="6"
                status="warning"
              />
              <div class="answer-sheet">
                <div
                  v-for="(q, index) in questions"
                  :key="q.id"
                  class="sheet-item"
                  :class="{
                    'answered': isAnswered(q.id!),
                    'current': currentIndex === index
                  }"
                  @click="goToQuestion(index)"
                >
                  {{ index + 1 }}
                </div>
              </div>
            </div>
          </div>

          <!-- 右侧题目区域 -->
          <div class="question-area">
            <div class="question-card" v-if="currentQuestion">
              <!-- 题目头部 -->
              <div class="question-header">
                <div class="question-info">
                  <el-tag :type="getQuestionTypeTag(currentQuestion.type)" size="small">
                    {{ getQuestionTypeLabel(currentQuestion.type) }}
                  </el-tag>
                  <span class="question-num">第 {{ currentIndex + 1 }} 题 / 共 {{ questions.length }} 题</span>
                </div>
              </div>

              <!-- 题目内容 -->
              <div class="question-content" v-html="currentQuestion.content"></div>

              <!-- 单选题选项 -->
              <div v-if="currentQuestion.type === 'single'" class="options-list">
                <div
                  v-for="(option, index) in currentQuestion.options"
                  :key="index"
                  class="option-item"
                  :class="{ 'selected': isOptionSelected(currentQuestion.id!, index) }"
                  @click="selectOption(currentQuestion.id!, index)"
                >
                  <span class="option-label">{{ getOptionLabel(index) }}</span>
                  <span class="option-text">{{ option }}</span>
                </div>
              </div>

              <!-- 多选题选项 -->
              <div v-if="currentQuestion.type === 'multiple'" class="options-list">
                <div class="multiple-tip">
                  <el-icon><InfoFilled /></el-icon>
                  多选题，可选择多个答案
                </div>
                <div
                  v-for="(option, index) in currentQuestion.options"
                  :key="index"
                  class="option-item multiple"
                  :class="{ 'selected': isOptionSelected(currentQuestion.id!, index) }"
                  @click="selectOption(currentQuestion.id!, index)"
                >
                  <el-checkbox
                    :model-value="isOptionSelected(currentQuestion.id!, index)"
                    @click.stop
                    @change="selectOption(currentQuestion.id!, index)"
                  />
                  <span class="option-label">{{ getOptionLabel(index) }}</span>
                  <span class="option-text">{{ option }}</span>
                </div>
              </div>

              <!-- 判断题选项 -->
              <div v-if="currentQuestion.type === 'judge'" class="judge-options">
                <div
                  class="judge-item"
                  :class="{ 'selected': answers[currentQuestion.id!] === 'true' }"
                  @click="selectJudge(currentQuestion.id!, 'true')"
                >
                  <el-icon :size="28"><CircleCheck /></el-icon>
                  <span>正确</span>
                </div>
                <div
                  class="judge-item"
                  :class="{ 'selected': answers[currentQuestion.id!] === 'false' }"
                  @click="selectJudge(currentQuestion.id!, 'false')"
                >
                  <el-icon :size="28"><CircleClose /></el-icon>
                  <span>错误</span>
                </div>
              </div>

              <!-- 简答题 -->
              <div v-if="currentQuestion.type === 'essay'" class="essay-area">
                <el-input
                  v-model="answers[currentQuestion.id!]"
                  type="textarea"
                  :rows="8"
                  placeholder="请输入您的答案..."
                />
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- 空状态 -->
      <el-empty v-else-if="!loading" description="题库中暂无题目">
        <el-button type="primary" @click="goBack">返回题库</el-button>
      </el-empty>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight, Loading, CircleCheck, CircleClose, Finished, InfoFilled } from '@element-plus/icons-vue'
import AppLayout from '@/components/common/AppLayout.vue'
import type { Question, QuizSession } from '@/types/index'
import * as sessionApi from '@/api/session'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const session = ref<QuizSession | null>(null)
const questions = ref<Question[]>([])
const currentIndex = ref(0)
const answers = ref<Record<number, string>>({})
const bankName = ref('')

const bankId = computed(() => Number(route.params.bankId))
const sessionIdParam = computed(() => route.query.sessionId ? String(route.query.sessionId) : null)
const currentQuestion = computed(() => questions.value[currentIndex.value])
const answeredCount = computed(() => Object.keys(answers.value).filter(k => answers.value[Number(k)]).length)

// 初始化
const init = async () => {
  loading.value = true
  try {
    let currentSession: QuizSession

    if (sessionIdParam.value) {
      const detail = await sessionApi.getSessionDetail(sessionIdParam.value)
      currentSession = detail.session
      questions.value = detail.questions

      // 恢复已有答案
      detail.answers.forEach(a => {
        if (a.userAnswer) {
          answers.value[a.questionId] = a.userAnswer
        }
      })
    } else {
      const result = await sessionApi.startSession({
        bankId: bankId.value,
        mode: 'exam'
      })
      currentSession = result.session
      questions.value = result.questions
    }

    session.value = currentSession
    bankName.value = currentSession.bankName || ''
    // 恢复上次的答题进度
    currentIndex.value = currentSession.currentIndex || 0
  } catch (error: any) {
    console.error('初始化失败:', error)
    ElMessage.error(error.message || '初始化失败')
  } finally {
    loading.value = false
  }
}

// 是否已答题
const isAnswered = (questionId: number) => {
  return !!answers.value[questionId]
}

// 保存答案到服务器（静默保存）
const saveAnswerToServer = async (questionId: number, answer: string) => {
  if (!session.value?.sessionId || !answer) return

  try {
    const questionIndex = questions.value.findIndex(q => q.id === questionId)
    await sessionApi.submitAnswer(session.value.sessionId, {
      questionId,
      questionIndex,
      userAnswer: answer
    })
  } catch (error) {
    console.error('保存答案失败:', error)
  }
}

// 选择选项
const selectOption = (questionId: number, index: number) => {
  const question = questions.value.find(q => q.id === questionId)
  if (!question) return

  if (question.type === 'single') {
    answers.value[questionId] = getOptionLabel(index)
    // 单选直接保存
    saveAnswerToServer(questionId, answers.value[questionId])
  } else if (question.type === 'multiple') {
    const current = answers.value[questionId] || ''
    const selected = current ? current.split(',') : []
    const label = getOptionLabel(index)

    const pos = selected.indexOf(label)
    if (pos > -1) {
      selected.splice(pos, 1)
    } else {
      selected.push(label)
      selected.sort()
    }
    answers.value[questionId] = selected.join(',')
    // 多选每次变更都保存
    saveAnswerToServer(questionId, answers.value[questionId])
  }
}

// 是否选中选项
const isOptionSelected = (questionId: number, index: number) => {
  const answer = answers.value[questionId] || ''
  const label = getOptionLabel(index)
  return answer.split(',').includes(label)
}

// 选择判断题答案
const selectJudge = (questionId: number, value: string) => {
  answers.value[questionId] = value
  // 判断题保存
  saveAnswerToServer(questionId, value)
}

// 跳转到指定题目
const goToQuestion = (index: number) => {
  currentIndex.value = index
}

// 上一题
const prevQuestion = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
  }
}

// 下一题
const nextQuestion = () => {
  if (currentIndex.value < questions.value.length - 1) {
    currentIndex.value++
  }
}

// 提交确认
const submitExamConfirm = () => {
  const unanswered = questions.value.length - answeredCount.value

  let message = '确定要提交试卷吗？提交后无法修改。'
  if (unanswered > 0) {
    message = `还有 ${unanswered} 道题目未作答，确定要提交吗？`
  }

  ElMessageBox.confirm(message, '提交确认', {
    confirmButtonText: '确定提交',
    cancelButtonText: '继续作答',
    type: 'warning'
  }).then(() => {
    submitExam()
  }).catch(() => {})
}

// 提交试卷
const submitExam = async () => {
  if (!session.value?.sessionId) return

  try {
    const answerList = questions.value.map((q, index) => ({
      questionId: q.id!,
      questionIndex: index,
      userAnswer: answers.value[q.id!] || ''
    }))

    await sessionApi.submitExam(session.value.sessionId, answerList)
    ElMessage.success('提交成功')
    router.push(`/result/${session.value.sessionId}`)
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败')
  }
}

// 返回题库
const goBack = () => {
  ElMessageBox.confirm('确定要退出考试吗？当前进度将会保存，可以稍后继续作答。', '提示', {
    confirmButtonText: '确定退出',
    cancelButtonText: '继续作答',
    type: 'warning'
  }).then(() => {
    router.push('/banks')
  }).catch(() => {})
}

// 获取题型标签
const getQuestionTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    single: '单选题',
    multiple: '多选题',
    judge: '判断题',
    essay: '简答题'
  }
  return labels[type] || type
}

// 获取题型标签类型
const getQuestionTypeTag = (type: string) => {
  const tags: Record<string, string> = {
    single: '',
    multiple: 'success',
    judge: 'warning',
    essay: 'info'
  }
  return tags[type] || 'info'
}

// 获取选项标签
const getOptionLabel = (index: number) => {
  return String.fromCharCode(65 + index)
}

onMounted(() => {
  init()
})
</script>

<style scoped>
.exam-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.loading-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  color: #909399;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background: white;
  border-bottom: 1px solid #ebeef5;
  position: sticky;
  top: 0;
  z-index: 100;
}

.top-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.top-center {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 20px;
  border: 1px solid #dcdfe6;
  background: white;
  color: #606266;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.nav-btn:hover:not(:disabled) {
  border-color: #e6a23c;
  color: #e6a23c;
  background: #fdf6ec;
}

.nav-btn:active:not(:disabled) {
  transform: scale(0.97);
}

.nav-btn:disabled {
  background: #f5f7fa;
  border-color: #e4e7ed;
  color: #c0c4cc;
  cursor: not-allowed;
}

.nav-btn .el-icon {
  font-size: 14px;
}

.nav-progress {
  display: flex;
  align-items: baseline;
  gap: 4px;
  padding: 0 8px;
}

.nav-progress .current {
  font-size: 22px;
  font-weight: 700;
  color: #e6a23c;
}

.nav-progress .separator {
  font-size: 16px;
  color: #dcdfe6;
  margin: 0 2px;
}

.nav-progress .total {
  font-size: 16px;
  color: #909399;
  font-weight: 500;
}

.top-right {
  display: flex;
  align-items: center;
}

.bank-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.main-content {
  display: flex;
  gap: 16px;
  padding: 16px;
  width: 80%;
  max-width: 1400px;
  margin: 0 auto;
  height: calc(100vh - 65px);
  overflow: hidden;
}

.sidebar {
  width: 200px;
  flex-shrink: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.sidebar-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.sidebar-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  flex-shrink: 0;
}

.progress-row {
  text-align: center;
}

.progress-label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.progress-value {
  font-size: 20px;
  font-weight: 600;
}

.progress-value.answered {
  color: #e6a23c;
}

.progress-value.unanswered {
  color: #909399;
}

.progress-value.total {
  color: #303133;
}

.sidebar-card .el-progress {
  flex-shrink: 0;
}

.answer-sheet {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 6px;
  margin-top: 12px;
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
  align-content: start;
}

.answer-sheet::-webkit-scrollbar {
  width: 4px;
}

.answer-sheet::-webkit-scrollbar-track {
  background: #f5f7fa;
  border-radius: 2px;
}

.answer-sheet::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 2px;
}

.answer-sheet::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

.sheet-item {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
  background: white;
}

.sheet-item:hover {
  border-color: #e6a23c;
}

.sheet-item.current {
  border-color: #e6a23c;
  background: #fdf6ec;
  font-weight: 600;
}

.sheet-item.answered {
  background: #e6a23c;
  border-color: #e6a23c;
  color: white;
}

.question-area {
  flex: 1;
  min-width: 0;
  overflow-y: auto;
}

.question-area::-webkit-scrollbar {
  width: 6px;
}

.question-area::-webkit-scrollbar-track {
  background: #f5f7fa;
  border-radius: 3px;
}

.question-area::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.question-area::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

.question-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.question-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.question-num {
  color: #909399;
  font-size: 14px;
}

.question-content {
  font-size: 16px;
  line-height: 1.8;
  color: #303133;
  margin-bottom: 24px;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.multiple-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 13px;
  margin-bottom: 4px;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border: 2px solid #e4e7ed;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.option-item:hover {
  border-color: #e6a23c;
  background: #fdf6ec;
}

.option-item.selected {
  border-color: #e6a23c;
  background: #fdf6ec;
}

.option-item.multiple {
  gap: 12px;
}

.option-label {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  margin-right: 12px;
  flex-shrink: 0;
}

.option-item.selected .option-label {
  background: #e6a23c;
  color: white;
}

.option-text {
  flex: 1;
  font-size: 15px;
}

.judge-options {
  display: flex;
  gap: 24px;
  justify-content: center;
}

.judge-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 140px;
  height: 110px;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  gap: 8px;
  font-size: 16px;
  color: #606266;
}

.judge-item:hover {
  border-color: #e6a23c;
  background: #fdf6ec;
}

.judge-item.selected {
  border-color: #e6a23c;
  background: #fdf6ec;
  color: #e6a23c;
}

.essay-area {
  margin-top: 8px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
    padding: 12px;
    width: 100%;
    height: auto;
    overflow: visible;
  }

  .sidebar {
    width: 100%;
    overflow: visible;
  }

  .sidebar-card {
    height: auto;
    max-height: 200px;
  }

  .answer-sheet {
    grid-template-columns: repeat(10, 1fr);
  }

  .question-area {
    overflow: visible;
  }

  .question-card {
    padding: 16px;
  }

  .judge-options {
    gap: 12px;
  }

  .judge-item {
    width: 100px;
    height: 80px;
  }

  .top-bar {
    flex-wrap: wrap;
    gap: 8px;
  }

  .top-center {
    order: 3;
    width: 100%;
    justify-content: center;
    padding-top: 8px;
    border-top: 1px solid #ebeef5;
  }

  .nav-btn {
    padding: 6px 12px;
    font-size: 13px;
  }

  .nav-progress .current {
    font-size: 18px;
  }

  .nav-progress .separator,
  .nav-progress .total {
    font-size: 14px;
  }
}
</style>
