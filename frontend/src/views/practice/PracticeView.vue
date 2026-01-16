<template>
  <AppLayout>
    <div class="practice-page">
      <!-- 加载中 -->
      <div v-if="loading" class="loading-wrapper">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <!-- 答题区域 -->
      <template v-else-if="questions.length > 0">
        <!-- 顶部导航 -->
        <div class="top-bar">
          <div class="top-left">
            <el-button text @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <span class="bank-name">{{ session?.bankName }}</span>
            <el-tag :type="getModeTagType()" size="small">{{ getModeLabel() }}</el-tag>
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
              @click="nextQuestion"
            >
              <span>下一题</span>
              <el-icon><ArrowRight /></el-icon>
            </button>
          </div>
          <div class="top-right">
            <el-switch
              v-model="autoNext"
              active-text="正确自动下一题"
              size="small"
              style="margin-right: 12px;"
            />
            <el-button
              :type="isFavorited ? 'warning' : 'default'"
              size="small"
              @click="toggleFavorite"
            >
              <el-icon><Star /></el-icon>
              {{ isFavorited ? '已收藏' : '收藏' }}
            </el-button>
          </div>
        </div>

        <div class="main-content">
          <!-- 左侧答题卡 -->
          <div class="sidebar-left">
            <div class="sidebar-card">
              <div class="sidebar-title">
                <span>答题卡</span>
              </div>
              <div class="progress-info">
                <div class="progress-row">
                  <span class="progress-label">正确</span>
                  <span class="progress-value correct">{{ correctCount }}</span>
                </div>
                <div class="progress-row">
                  <span class="progress-label">错误</span>
                  <span class="progress-value wrong">{{ wrongCount }}</span>
                </div>
                <div class="progress-row">
                  <span class="progress-label">未答</span>
                  <span class="progress-value unanswered">{{ questions.length - answeredCount }}</span>
                </div>
              </div>
              <el-progress
                :percentage="(answeredCount / questions.length) * 100"
                :show-text="false"
                :stroke-width="6"
              />
              <div class="answer-sheet">
                <div
                  v-for="(q, index) in questions"
                  :key="q.id"
                  class="sheet-item"
                  :class="{
                    'correct': answeredQuestions.get(q.id!)?.isCorrect === true,
                    'wrong': answeredQuestions.get(q.id!)?.isCorrect === false,
                    'current': currentIndex === index
                  }"
                  @click="goToQuestion(index)"
                >
                  {{ index + 1 }}
                </div>
              </div>
            </div>
          </div>

          <!-- 中间题目区域 -->
          <div class="question-area">
            <div class="question-card" v-if="currentQuestion">
              <!-- 题目头部 -->
              <div class="question-header">
                <div class="question-info">
                  <el-tag :type="getQuestionTypeTag(currentQuestion.type)" size="small">
                    {{ getQuestionTypeLabel(currentQuestion.type) }}
                  </el-tag>
                  <span class="question-num">第 {{ currentIndex + 1 }} 题</span>
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
                  :class="getOptionClass(index)"
                  @click="selectSingleOption(index)"
                >
                  <span class="option-label">{{ getOptionLabel(index) }}</span>
                  <span class="option-text">{{ option }}</span>
                  <el-icon v-if="showResult && isCorrectOption(index)" class="result-icon correct"><CircleCheck /></el-icon>
                  <el-icon v-if="showResult && isWrongSelected(index)" class="result-icon wrong"><CircleClose /></el-icon>
                </div>
              </div>

              <!-- 多选题选项 -->
              <div v-if="currentQuestion.type === 'multiple'" class="options-list">
                <div class="multiple-tip">
                  <el-icon><InfoFilled /></el-icon>
                  多选题，选择完成后点击确认
                </div>
                <div
                  v-for="(option, index) in currentQuestion.options"
                  :key="index"
                  class="option-item multiple"
                  :class="getOptionClass(index)"
                  @click="selectMultipleOption(index)"
                >
                  <el-checkbox
                    :model-value="selectedOptions.includes(index)"
                    @click.stop
                    @change="selectMultipleOption(index)"
                    :disabled="showResult"
                  />
                  <span class="option-label">{{ getOptionLabel(index) }}</span>
                  <span class="option-text">{{ option }}</span>
                  <el-icon v-if="showResult && isCorrectOption(index)" class="result-icon correct"><CircleCheck /></el-icon>
                  <el-icon v-if="showResult && isWrongSelected(index)" class="result-icon wrong"><CircleClose /></el-icon>
                </div>
                <div v-if="!showResult" class="submit-area">
                  <el-button type="primary" @click="submitAnswer" :disabled="selectedOptions.length === 0">
                    确认答案
                  </el-button>
                </div>
              </div>

              <!-- 判断题选项 -->
              <div v-if="currentQuestion.type === 'judge'" class="judge-options">
                <div
                  class="judge-item"
                  :class="{
                    'selected': userAnswer === 'true' && !showResult,
                    'correct': showResult && isJudgeAnswerTrue(currentQuestion.correctAnswer),
                    'wrong': showResult && userAnswer === 'true' && !isJudgeAnswerTrue(currentQuestion.correctAnswer)
                  }"
                  @click="selectJudge('true')"
                >
                  <el-icon :size="28"><CircleCheck /></el-icon>
                  <span>正确</span>
                </div>
                <div
                  class="judge-item"
                  :class="{
                    'selected': userAnswer === 'false' && !showResult,
                    'correct': showResult && !isJudgeAnswerTrue(currentQuestion.correctAnswer),
                    'wrong': showResult && userAnswer === 'false' && isJudgeAnswerTrue(currentQuestion.correctAnswer)
                  }"
                  @click="selectJudge('false')"
                >
                  <el-icon :size="28"><CircleClose /></el-icon>
                  <span>错误</span>
                </div>
              </div>

              <!-- 简答题 -->
              <div v-if="currentQuestion.type === 'essay'" class="essay-area">
                <el-input
                  v-model="userAnswer"
                  type="textarea"
                  :rows="6"
                  placeholder="请输入您的答案"
                  :disabled="showResult"
                />
                <div v-if="!showResult" class="submit-area">
                  <el-button type="primary" @click="submitAnswer" :disabled="!userAnswer.trim()">
                    确认答案
                  </el-button>
                </div>
              </div>
            </div>

          </div>

          <!-- 右侧答案解析 -->
          <div class="sidebar-right">
            <transition name="result-fade" mode="out-in">
              <div v-if="showResult" key="result" class="result-card">
                <div class="result-header" :class="isCorrect ? 'correct' : 'wrong'">
                  <el-icon :size="20"><CircleCheck v-if="isCorrect" /><CircleClose v-else /></el-icon>
                  <span>{{ isCorrect ? '回答正确' : '回答错误' }}</span>
                </div>
                <div class="result-body">
                  <div class="answer-info">
                    <div class="answer-row">
                      <span class="label">正确答案</span>
                      <span class="value correct-text">{{ formatCorrectAnswer() }}</span>
                    </div>
                    <div class="answer-row" v-if="!isCorrect">
                      <span class="label">你的答案</span>
                      <span class="value wrong-text">{{ formatUserAnswer() }}</span>
                    </div>
                  </div>
                  <div v-if="currentQuestion?.analysis" class="analysis-section">
                    <div class="analysis-title">
                      <el-icon><Document /></el-icon>
                      解析
                    </div>
                    <div class="analysis-content" v-html="currentQuestion.analysis"></div>
                  </div>
                  <div v-else class="no-analysis">
                    暂无解析
                  </div>
                </div>
              </div>
              <div v-else key="empty" class="result-card empty">
                <div class="empty-tip">
                  <el-icon :size="40"><QuestionFilled /></el-icon>
                  <p>答题后显示解析</p>
                </div>
              </div>
            </transition>
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
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight, Star, Loading, CircleCheck, CircleClose, Document, QuestionFilled, InfoFilled } from '@element-plus/icons-vue'
import AppLayout from '@/components/common/AppLayout.vue'
import type { Question, QuizSession } from '@/types/index'
import * as sessionApi from '@/api/session'
import * as favoriteApi from '@/api/favorite'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const session = ref<QuizSession | null>(null)
const questions = ref<Question[]>([])
const currentIndex = ref(0)
const userAnswer = ref('')
const showResult = ref(false)
const isCorrect = ref(false)
const isFavorited = ref(false)
const selectedOptions = ref<number[]>([])
const answeredQuestions = ref<Map<number, { isCorrect: boolean, userAnswer: string }>>(new Map())
// 从 localStorage 读取自动下一题设置，默认为 true
const autoNext = ref(localStorage.getItem('quiz_auto_next') !== 'false')

// 监听 autoNext 变化，保存到 localStorage
watch(autoNext, (val) => {
  localStorage.setItem('quiz_auto_next', val ? 'true' : 'false')
})

const bankId = computed(() => Number(route.params.bankId))
const sessionIdParam = computed(() => route.query.sessionId ? String(route.query.sessionId) : null)
const modeParam = computed(() => route.query.mode ? String(route.query.mode) : 'practice')
const currentQuestion = computed(() => questions.value[currentIndex.value])
const answeredCount = computed(() => answeredQuestions.value.size)
const correctCount = computed(() => {
  let count = 0
  answeredQuestions.value.forEach(v => {
    if (v.isCorrect) count++
  })
  return count
})
const wrongCount = computed(() => answeredCount.value - correctCount.value)

// 初始化
const init = async () => {
  loading.value = true
  try {
    let currentSession: QuizSession
    let questionList: Question[] = []

    if (sessionIdParam.value) {
      const detail = await sessionApi.getSessionDetail(sessionIdParam.value)
      currentSession = detail.session
      questionList = detail.questions
      // 恢复已答题记录
      detail.answers.forEach(a => {
        if (a.userAnswer) {
          answeredQuestions.value.set(a.questionId, {
            isCorrect: !!a.isCorrect,
            userAnswer: a.userAnswer
          })
        }
      })
    } else {
      const result = await sessionApi.startSession({
        bankId: bankId.value,
        mode: modeParam.value as 'practice' | 'exam' | 'favorite' | 'wrong'
      })
      currentSession = result.session
      questionList = result.questions
    }

    session.value = currentSession
    questions.value = questionList
    currentIndex.value = currentSession.currentIndex || 0

    loadCurrentQuestion()
  } catch (error: any) {
    console.error('初始化失败:', error)
    ElMessage.error(error.message || '初始化失败')
  } finally {
    loading.value = false
  }
}

// 加载当前题目
const loadCurrentQuestion = () => {
  const questionId = currentQuestion.value?.id
  const answered = questionId ? answeredQuestions.value.get(questionId) : null

  if (answered) {
    // 已答题，恢复状态
    userAnswer.value = answered.userAnswer
    showResult.value = true
    isCorrect.value = answered.isCorrect

    // 恢复选项状态
    if (currentQuestion.value?.type === 'single') {
      const index = answered.userAnswer.charCodeAt(0) - 65
      selectedOptions.value = [index]
    } else if (currentQuestion.value?.type === 'multiple') {
      // 多选答案格式：ABCD（无逗号）或 A,B,C,D（有逗号，兼容旧数据）
      const answerStr = answered.userAnswer.replace(/,/g, '')
      selectedOptions.value = answerStr.split('').map(label => label.charCodeAt(0) - 65)
    } else {
      selectedOptions.value = []
    }
  } else {
    // 未答题，重置状态
    userAnswer.value = ''
    selectedOptions.value = []
    showResult.value = false
    isCorrect.value = false
  }

  if (questionId) {
    checkFavoriteStatus(questionId)
  }
}

// 跳转到指定题目
const goToQuestion = (index: number) => {
  currentIndex.value = index
  loadCurrentQuestion()
}

// 检查收藏状态
const checkFavoriteStatus = async (questionId: number) => {
  try {
    isFavorited.value = await favoriteApi.checkFavoriteStatus(questionId)
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

// 切换收藏
const toggleFavorite = async () => {
  if (!currentQuestion.value?.id) return

  try {
    if (isFavorited.value) {
      await favoriteApi.removeFavoriteByQuestionId(currentQuestion.value.id)
      isFavorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await favoriteApi.addFavorite(currentQuestion.value.id, currentQuestion.value.bankId)
      isFavorited.value = true
      ElMessage.success('已收藏')
    }
  } catch (error) {
    console.error('切换收藏失败:', error)
    ElMessage.error('操作失败')
  }
}

// 单选题选择（自动提交）
const selectSingleOption = async (index: number) => {
  if (showResult.value) return

  selectedOptions.value = [index]
  userAnswer.value = getOptionLabel(index)

  await submitAnswer()
}

// 多选题选择
const selectMultipleOption = (index: number) => {
  if (showResult.value) return

  const pos = selectedOptions.value.indexOf(index)
  if (pos > -1) {
    selectedOptions.value.splice(pos, 1)
  } else {
    selectedOptions.value.push(index)
  }
  selectedOptions.value.sort((a, b) => a - b)
  // 多选答案格式：ABCD（无逗号）
  userAnswer.value = selectedOptions.value.map(i => getOptionLabel(i)).join('')
}

// 判断题选择（自动提交）
const selectJudge = async (value: string) => {
  if (showResult.value) return

  userAnswer.value = value

  await submitAnswer()
}

// 提交答案
const submitAnswer = async () => {
  if (!session.value?.sessionId || !currentQuestion.value?.id || !userAnswer.value) return

  try {
    const result = await sessionApi.submitAnswer(session.value.sessionId, {
      questionId: currentQuestion.value.id,
      questionIndex: currentIndex.value,
      userAnswer: userAnswer.value
    })

    isCorrect.value = result.isCorrect
    showResult.value = true
    answeredQuestions.value.set(currentQuestion.value.id, {
      isCorrect: result.isCorrect,
      userAnswer: userAnswer.value
    })

    // 回答正确且开启自动下一题
    if (result.isCorrect && autoNext.value) {
      setTimeout(() => {
        if (currentIndex.value < questions.value.length - 1) {
          nextQuestion()
        }
      }, 800) // 延迟800ms让用户看到结果
    }
  } catch (error) {
    console.error('提交答案失败:', error)
    ElMessage.error('提交失败')
  }
}

// 上一题
const prevQuestion = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
    loadCurrentQuestion()
  }
}

// 下一题
const nextQuestion = () => {
  if (currentIndex.value >= questions.value.length - 1) {
    router.push(`/result/${session.value?.sessionId}`)
  } else {
    currentIndex.value++
    loadCurrentQuestion()
  }
}

// 返回题库
const goBack = () => {
  router.push('/banks')
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

// 获取答题模式标签
const getModeLabel = () => {
  const mode = session.value?.mode || modeParam.value
  const labels: Record<string, string> = {
    practice: '练习模式',
    exam: '考试模式',
    favorite: '收藏练习',
    wrong: '错题练习'
  }
  return labels[mode] || '练习模式'
}

// 获取答题模式标签类型
const getModeTagType = () => {
  const mode = session.value?.mode || modeParam.value
  const types: Record<string, string> = {
    practice: 'success',
    exam: 'warning',
    favorite: '',
    wrong: 'danger'
  }
  return types[mode] || 'success'
}

// 判断题正确答案是否为"正确"（true）
// 支持多种格式：true/false, 正确/错误, 对/错, 是/否, 1/0
const isJudgeAnswerTrue = (answer: string | undefined) => {
  if (!answer) return false
  const normalized = answer.toLowerCase()
  return normalized === 'true' || answer === '正确' ||
         answer === '对' || answer === '是' || answer === '1'
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

// 获取选项样式类
const getOptionClass = (index: number) => {
  const classes: string[] = []

  if (selectedOptions.value.includes(index)) {
    classes.push('selected')
  }

  if (showResult.value) {
    // 支持 ABC 或 A,B,C 格式
    const correctAnswers = (currentQuestion.value?.correctAnswer || '').replace(/,/g, '')
    const optionLabel = getOptionLabel(index)

    if (correctAnswers.includes(optionLabel)) {
      classes.push('correct')
    } else if (selectedOptions.value.includes(index)) {
      classes.push('wrong')
    }
  }

  return classes
}

// 是否为正确选项
const isCorrectOption = (index: number) => {
  if (!showResult.value) return false
  // 支持 ABC 或 A,B,C 格式
  const correctAnswers = (currentQuestion.value?.correctAnswer || '').replace(/,/g, '')
  return correctAnswers.includes(getOptionLabel(index))
}

// 是否为错选选项
const isWrongSelected = (index: number) => {
  if (!showResult.value) return false
  // 支持 ABC 或 A,B,C 格式
  const correctAnswers = (currentQuestion.value?.correctAnswer || '').replace(/,/g, '')
  const optionLabel = getOptionLabel(index)
  return selectedOptions.value.includes(index) && !correctAnswers.includes(optionLabel)
}

// 格式化正确答案
const formatCorrectAnswer = () => {
  if (!currentQuestion.value) return ''

  const { type, correctAnswer, options } = currentQuestion.value

  if (type === 'judge') {
    // 支持多种格式：true/false, 正确/错误, 对/错, 是/否, 1/0
    const normalized = correctAnswer.toLowerCase()
    if (normalized === 'true' || correctAnswer === '正确' ||
        correctAnswer === '对' || correctAnswer === '是' || correctAnswer === '1') {
      return '正确'
    }
    return '错误'
  }

  if (type === 'single' && options) {
    // 单选题：显示单个选项
    const index = correctAnswer.charCodeAt(0) - 65
    return `${correctAnswer}. ${options[index] || ''}`
  }

  if (type === 'multiple' && options) {
    // 多选题：支持 ABC 或 A,B,C 格式，多行显示
    const answerStr = correctAnswer.replace(/,/g, '') // 去除逗号
    return answerStr.split('').map(a => {
      const index = a.charCodeAt(0) - 65
      return `${a}. ${options[index] || ''}`
    }).join('\n')
  }

  return correctAnswer
}

// 格式化用户答案
const formatUserAnswer = () => {
  if (!currentQuestion.value) return ''

  const { type, options } = currentQuestion.value

  if (type === 'judge') {
    return userAnswer.value === 'true' ? '正确' : '错误'
  }

  if ((type === 'single' || type === 'multiple') && options) {
    const answers = userAnswer.value.split(',')
    return answers.map(a => {
      const index = a.charCodeAt(0) - 65
      return `${a}. ${options[index] || ''}`
    }).join('\n')
  }

  return userAnswer.value
}

onMounted(() => {
  init()
})
</script>

<style scoped>
.practice-page {
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
  border-color: #67c23a;
  color: #67c23a;
  background: #f0f9eb;
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
  color: #67c23a;
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

/* 左侧答题卡 */
.sidebar-left {
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

.progress-value.correct {
  color: #67c23a;
}

.progress-value.wrong {
  color: #f56c6c;
}

.progress-value.unanswered {
  color: #909399;
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
  border-color: #409eff;
}

.sheet-item.current {
  border-color: #409eff;
  background: #ecf5ff;
  font-weight: 600;
}

.sheet-item.correct {
  background: #67c23a;
  border-color: #67c23a;
  color: white;
}

.sheet-item.wrong {
  background: #f56c6c;
  border-color: #f56c6c;
  color: white;
}

/* 中间题目区域 */
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

.option-item:hover:not(.correct):not(.wrong) {
  border-color: #409eff;
  background: #f5f7fa;
}

.option-item.selected:not(.correct):not(.wrong) {
  border-color: #409eff;
  background: #ecf5ff;
}

.option-item.correct {
  border-color: #67c23a;
  background: #f0f9eb;
  cursor: default;
}

.option-item.wrong {
  border-color: #f56c6c;
  background: #fef0f0;
  cursor: default;
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

.option-item.selected:not(.correct):not(.wrong) .option-label {
  background: #409eff;
  color: white;
}

.option-item.correct .option-label {
  background: #67c23a;
  color: white;
}

.option-item.wrong .option-label {
  background: #f56c6c;
  color: white;
}

.option-text {
  flex: 1;
  font-size: 15px;
}

.result-icon {
  margin-left: auto;
  font-size: 20px;
}

.result-icon.correct {
  color: #67c23a;
}

.result-icon.wrong {
  color: #f56c6c;
}

.submit-area {
  margin-top: 20px;
  text-align: center;
}

.judge-options {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.judge-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 100px;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  gap: 8px;
  font-size: 15px;
  color: #606266;
}

.judge-item:hover:not(.correct):not(.wrong) {
  border-color: #409eff;
  background: #f5f7fa;
}

.judge-item.selected {
  border-color: #409eff;
  background: #ecf5ff;
  color: #409eff;
}

.judge-item.correct {
  border-color: #67c23a;
  background: #f0f9eb;
  color: #67c23a;
  cursor: default;
}

.judge-item.wrong {
  border-color: #f56c6c;
  background: #fef0f0;
  color: #f56c6c;
  cursor: default;
}

.essay-area {
  margin-top: 8px;
}

/* 右侧答案解析 */
.sidebar-right {
  width: 300px;
  flex-shrink: 0;
  overflow-y: auto;
}

.sidebar-right::-webkit-scrollbar {
  width: 6px;
}

.sidebar-right::-webkit-scrollbar-track {
  background: #f5f7fa;
  border-radius: 3px;
}

.sidebar-right::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.sidebar-right::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

.result-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.result-card.empty {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-tip {
  text-align: center;
  color: #c0c4cc;
}

.empty-tip p {
  margin-top: 12px;
  font-size: 14px;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 16px;
  font-weight: 600;
  font-size: 15px;
  color: white;
}

.result-header.correct {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.result-header.wrong {
  background: linear-gradient(135deg, #f56c6c, #f89898);
}

.result-body {
  padding: 16px;
}

.answer-info {
  margin-bottom: 16px;
}

.answer-row {
  margin-bottom: 12px;
}

.answer-row .label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.answer-row .value {
  font-size: 14px;
  white-space: pre-line;
}

.correct-text {
  color: #67c23a;
}

.wrong-text {
  color: #f56c6c;
}

.analysis-section {
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.analysis-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  margin-bottom: 10px;
}

.analysis-content {
  font-size: 13px;
  line-height: 1.8;
  color: #606266;
}

.no-analysis {
  text-align: center;
  color: #c0c4cc;
  font-size: 13px;
  padding: 20px 0;
}

/* 答案解析过渡动画 */
.result-fade-enter-active {
  animation: resultFadeIn 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.result-fade-leave-active {
  animation: resultFadeOut 0.2s ease-out;
}

@keyframes resultFadeIn {
  0% {
    opacity: 0;
    transform: translateY(12px) scale(0.96);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes resultFadeOut {
  0% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
  100% {
    opacity: 0;
    transform: translateY(-8px) scale(0.98);
  }
}

/* 移动端适配 */
@media (max-width: 1200px) {
  .sidebar-right {
    display: none;
  }
}

@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
    padding: 12px;
    width: 100%;
    height: auto;
    overflow: visible;
  }

  .sidebar-left {
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

  .top-right .el-switch {
    display: none;
  }
}
</style>
