<template>
  <view class="practice-container">
    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 答题区域 -->
    <template v-else-if="questions.length > 0">
      <!-- 顶部导航栏 -->
      <view class="top-bar">
        <view class="top-row">
          <text class="back-btn" @click="goBack">返回</text>
          <text class="bank-name">{{ bankName }}</text>
          <text class="mode-tag" :class="practiceMode">{{ getModeLabel() }}</text>
        </view>
        <!-- 题目导航 -->
        <view class="nav-row">
          <button
            class="nav-btn prev"
            :disabled="currentIndex === 0"
            @click="prevQuestion"
          >
            上一题
          </button>
          <view class="nav-center">
            <text class="current-num">{{ currentIndex + 1 }}</text>
            <text class="separator">/</text>
            <text class="total-num">{{ questions.length }}</text>
          </view>
          <button
            class="nav-btn next"
            @click="nextQuestion"
          >
            {{ currentIndex >= questions.length - 1 ? '完成' : '下一题' }}
          </button>
        </view>
        <!-- 统计栏 -->
        <view class="stats-row">
          <view class="stat-item">
            <text class="stat-value correct">{{ correctCount }}</text>
            <text class="stat-label">正确</text>
          </view>
          <view class="stat-item">
            <text class="stat-value wrong">{{ wrongCount }}</text>
            <text class="stat-label">错误</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ questions.length - correctCount - wrongCount }}</text>
            <text class="stat-label">未答</text>
          </view>
          <text class="swipe-tip">左右滑动切换题目</text>
        </view>
      </view>

      <!-- 题目滑动区域 -->
      <swiper
        class="question-swiper"
        :current="currentIndex"
        @change="onSwiperChange"
        :duration="200"
      >
        <swiper-item v-for="(question, qIndex) in questions" :key="question.id">
          <!-- 只渲染当前题目及前后各一题的详细内容 -->
          <scroll-view class="question-scroll" scroll-y v-if="Math.abs(qIndex - currentIndex) <= 1">
            <view class="question-card">
              <!-- 题目头部 -->
              <view class="question-header">
                <text class="question-type" :class="question.type">
                  {{ getTypeLabel(question.type) }}
                </text>
                <text class="question-num">第 {{ qIndex + 1 }} 题</text>
              </view>

              <!-- 题目内容 -->
              <view class="question-content">{{ question.content }}</view>

              <!-- 单选题选项 -->
              <view v-if="question.type === 'single'" class="options-list">
                <view
                  v-for="(option, index) in question.options"
                  :key="index"
                  class="option-item"
                  :class="getOptionClassForQuestion(qIndex, index)"
                  @click="qIndex === currentIndex && selectOption(index)"
                >
                  <text class="option-label">{{ getOptionLabel(index) }}</text>
                  <text class="option-text">{{ option }}</text>
                  <text v-if="isShowResultForQuestion(qIndex) && isCorrectOptionForQuestion(qIndex, index)" class="result-icon correct">✓</text>
                  <text v-if="isShowResultForQuestion(qIndex) && isWrongSelectedForQuestion(qIndex, index)" class="result-icon wrong">✗</text>
                </view>
              </view>

              <!-- 多选题选项 -->
              <view v-if="question.type === 'multiple'" class="options-list">
                <view class="multiple-tip">多选题，选择完成后点击确认</view>
                <view
                  v-for="(option, index) in question.options"
                  :key="index"
                  class="option-item multiple"
                  :class="getOptionClassForQuestion(qIndex, index)"
                  @click="qIndex === currentIndex && !isShowResultForQuestion(qIndex) && toggleMultipleOption(index)"
                >
                  <view class="checkbox" :class="{ checked: qIndex === currentIndex && selectedOptions.includes(index) }">
                    <text v-if="qIndex === currentIndex && selectedOptions.includes(index)">✓</text>
                  </view>
                  <text class="option-label">{{ getOptionLabel(index) }}</text>
                  <text class="option-text">{{ option }}</text>
                  <text v-if="isShowResultForQuestion(qIndex) && isCorrectOptionForQuestion(qIndex, index)" class="result-icon correct">✓</text>
                  <text v-if="isShowResultForQuestion(qIndex) && isWrongSelectedForQuestion(qIndex, index)" class="result-icon wrong">✗</text>
                </view>
                <button v-if="qIndex === currentIndex && !showResult" class="confirm-btn" @click="submitMultipleAnswer" :disabled="selectedOptions.length === 0">
                  确认答案
                </button>
              </view>

              <!-- 判断题选项 -->
              <view v-if="question.type === 'judge'" class="judge-options">
                <view
                  class="judge-item"
                  :class="getJudgeClass(qIndex, 'true')"
                  @click="qIndex === currentIndex && selectJudge('true')"
                >
                  <text class="judge-icon">✓</text>
                  <text class="judge-text">正确</text>
                </view>
                <view
                  class="judge-item"
                  :class="getJudgeClass(qIndex, 'false')"
                  @click="qIndex === currentIndex && selectJudge('false')"
                >
                  <text class="judge-icon">✗</text>
                  <text class="judge-text">错误</text>
                </view>
              </view>

              <!-- 答案解析区域 -->
              <view v-if="isShowResultForQuestion(qIndex)" class="result-section">
                <view class="result-header">
                  <text class="result-status" :class="isCorrectForQuestion(qIndex) ? 'correct' : 'wrong'">
                    {{ isCorrectForQuestion(qIndex) ? '回答正确' : '回答错误' }}
                  </text>
                </view>
                <view class="answer-info">
                  <view class="answer-row">
                    <text class="answer-label">正确答案：</text>
                    <text class="answer-value correct">{{ formatCorrectAnswer(question) }}</text>
                  </view>
                  <view class="answer-row" v-if="!isCorrectForQuestion(qIndex)">
                    <text class="answer-label">你的答案：</text>
                    <text class="answer-value wrong">{{ formatUserAnswerForQuestion(qIndex) }}</text>
                  </view>
                </view>
                <view class="analysis-section" v-if="question.analysis">
                  <text class="analysis-label">答案解析</text>
                  <text class="analysis-content">{{ question.analysis }}</text>
                </view>
              </view>
            </view>
          </scroll-view>
          <!-- 占位符 - 未渲染的题目 -->
          <view v-else class="question-placeholder">
            <text class="placeholder-text">第 {{ qIndex + 1 }} 题</text>
          </view>
        </swiper-item>
      </swiper>
    </template>

    <!-- 空状态 -->
    <view class="empty-container" v-else-if="!loading">
      <text class="empty-icon">📚</text>
      <text class="empty-title">暂无题目</text>
      <text class="empty-desc">该题库暂无题目</text>
      <button class="back-home-btn" @click="goBack">返回首页</button>
    </view>
  </view>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { sessionAPI } from '@/utils/api'

export default {
  name: 'PracticePage',
  setup() {
    const loading = ref(true)
    const bankId = ref(null)
    const bankName = ref('')
    const sessionId = ref('')
    const questions = ref([])
    const currentIndex = ref(0)
    const answeredQuestions = ref(new Map()) // questionId -> { answer, isCorrect }
    const practiceMode = ref('practice') // practice, favorite, wrong

    // 当前题目状态
    const userAnswer = ref('')
    const selectedOptions = ref([])
    const showResult = ref(false)

    // 当前题目
    const currentQuestion = computed(() => questions.value[currentIndex.value])

    // 统计
    const correctCount = computed(() => {
      let count = 0
      answeredQuestions.value.forEach(v => { if (v.isCorrect) count++ })
      return count
    })

    const wrongCount = computed(() => {
      let count = 0
      answeredQuestions.value.forEach(v => { if (!v.isCorrect) count++ })
      return count
    })

    const isCurrentCorrect = computed(() => {
      if (!currentQuestion.value) return false
      const answered = answeredQuestions.value.get(currentQuestion.value.id)
      return answered?.isCorrect === true
    })

    // 初始化会话
    const initSession = async () => {
      loading.value = true
      try {
        // 获取页面参数
        const pages = getCurrentPages()
        const currentPage = pages[pages.length - 1]
        const options = currentPage.options

        if (!options.bankId) {
          throw new Error('缺少题库参数')
        }

        bankId.value = Number(options.bankId)
        bankName.value = decodeURIComponent(options.bankName || '')
        const existingSessionId = options.sessionId
        const source = options.source // favorites 或 wrong

        // 根据 source 设置模式
        if (source === 'favorites') {
          practiceMode.value = 'favorite'
        } else if (source === 'wrong') {
          practiceMode.value = 'wrong'
        } else {
          practiceMode.value = 'practice'
        }

        let currentSession
        let questionList = []

        if (existingSessionId) {
          // 继续已有会话 - 使用 getSessionDetail
          const detail = await sessionAPI.getSessionDetail(existingSessionId)
          currentSession = detail.session
          questionList = detail.questions || []
          // 恢复模式
          if (currentSession.mode) {
            practiceMode.value = currentSession.mode
          }
          // 恢复已答题记录
          if (detail.answers) {
            detail.answers.forEach(a => {
              if (a.userAnswer) {
                answeredQuestions.value.set(a.questionId, {
                  answer: a.userAnswer,
                  isCorrect: !!a.isCorrect
                })
              }
            })
          }
        } else {
          // 创建新会话 - 使用 startSession
          const result = await sessionAPI.startSession({
            bankId: bankId.value,
            mode: practiceMode.value
          })
          currentSession = result.session
          questionList = result.questions || []
        }

        sessionId.value = currentSession.sessionId
        bankName.value = currentSession.bankName || bankName.value
        questions.value = questionList
        currentIndex.value = currentSession.currentIndex || 0

        // 加载当前题的已答状态
        loadCurrentQuestionState()
      } catch (error) {
        console.error('初始化会话失败:', error)
        uni.showToast({
          title: error.message || '加载失败',
          icon: 'none'
        })
      } finally {
        loading.value = false
      }
    }

    // 加载当前题的已答状态
    const loadCurrentQuestionState = () => {
      if (!currentQuestion.value) return

      const answered = answeredQuestions.value.get(currentQuestion.value.id)
      if (answered) {
        userAnswer.value = answered.answer
        showResult.value = true

        // 恢复多选选项 - 支持 ABC 或 A,B,C 格式
        if (currentQuestion.value.type === 'multiple' && answered.answer) {
          const answerStr = answered.answer.replace(/,/g, '')
          selectedOptions.value = answerStr.split('').map(label => label.charCodeAt(0) - 65)
        } else if (currentQuestion.value.type === 'single' && answered.answer) {
          // 单选答案格式是 "A"
          selectedOptions.value = [answered.answer.charCodeAt(0) - 65]
        }
      } else {
        userAnswer.value = ''
        selectedOptions.value = []
        showResult.value = false
      }
    }

    // 选择单选选项 - 答案格式改为 A, B, C
    const selectOption = async (index) => {
      if (showResult.value) return

      selectedOptions.value = [index]
      userAnswer.value = getOptionLabel(index)

      await submitAnswer()
    }

    // 切换多选选项
    const toggleMultipleOption = (index) => {
      if (showResult.value) return

      const idx = selectedOptions.value.indexOf(index)
      if (idx > -1) {
        selectedOptions.value.splice(idx, 1)
      } else {
        selectedOptions.value.push(index)
      }
      selectedOptions.value.sort((a, b) => a - b)
      // 多选答案格式：ABCD（无逗号）
      userAnswer.value = selectedOptions.value.map(i => getOptionLabel(i)).join('')
    }

    // 提交多选答案
    const submitMultipleAnswer = async () => {
      if (selectedOptions.value.length === 0) return

      // 多选答案格式：ABCD（无逗号）
      userAnswer.value = selectedOptions.value
        .sort((a, b) => a - b)
        .map(idx => getOptionLabel(idx))
        .join('')

      await submitAnswer()
    }

    // 选择判断题
    const selectJudge = async (value) => {
      if (showResult.value) return

      userAnswer.value = value

      await submitAnswer()
    }

    // 提交答案
    const submitAnswer = async () => {
      if (!sessionId.value || !currentQuestion.value?.id || !userAnswer.value) return

      try {
        const result = await sessionAPI.submitAnswer(sessionId.value, {
          questionId: currentQuestion.value.id,
          questionIndex: currentIndex.value,
          userAnswer: userAnswer.value
        })

        const isCorrect = result.isCorrect
        answeredQuestions.value.set(currentQuestion.value.id, {
          answer: userAnswer.value,
          isCorrect
        })

        showResult.value = true
      } catch (error) {
        console.error('提交答案失败:', error)
        uni.showToast({ title: '提交失败', icon: 'none' })
      }
    }

    // 上一题
    const prevQuestion = () => {
      if (currentIndex.value > 0) {
        currentIndex.value--
        loadCurrentQuestionState()
      }
    }

    // 下一题
    const nextQuestion = () => {
      if (currentIndex.value < questions.value.length - 1) {
        currentIndex.value++
        loadCurrentQuestionState()
      } else {
        // 完成练习
        finishPractice()
      }
    }

    // 完成练习
    const finishPractice = () => {
      uni.showModal({
        title: '练习完成',
        content: `本次练习：正确 ${correctCount.value} 题，错误 ${wrongCount.value} 题`,
        confirmText: '查看结果',
        cancelText: '继续练习',
        success: (res) => {
          if (res.confirm) {
            uni.redirectTo({
              url: `/pages/result/result?sessionId=${sessionId.value}`
            })
          }
        }
      })
    }

    // 返回
    const goBack = () => {
      uni.navigateBack()
    }

    // swiper 滑动事件
    const onSwiperChange = (e) => {
      const newIndex = e.detail.current
      if (newIndex !== currentIndex.value) {
        currentIndex.value = newIndex
        loadCurrentQuestionState()
      }
    }

    // 工具函数
    const getTypeLabel = (type) => {
      const labels = { single: '单选题', multiple: '多选题', judge: '判断题', essay: '简答题' }
      return labels[type] || type
    }

    const getOptionLabel = (index) => String.fromCharCode(65 + index)

    // 获取练习模式标签
    const getModeLabel = () => {
      const labels = {
        practice: '练习',
        favorite: '收藏练习',
        wrong: '错题练习'
      }
      return labels[practiceMode.value] || '练习'
    }

    // 针对特定题目的工具函数（用于swiper中显示已答题的状态）
    const isShowResultForQuestion = (qIndex) => {
      const question = questions.value[qIndex]
      if (!question) return false
      return answeredQuestions.value.has(question.id)
    }

    const isCorrectForQuestion = (qIndex) => {
      const question = questions.value[qIndex]
      if (!question) return false
      const answered = answeredQuestions.value.get(question.id)
      return answered?.isCorrect === true
    }

    const getAnswerForQuestion = (qIndex) => {
      const question = questions.value[qIndex]
      if (!question) return ''
      const answered = answeredQuestions.value.get(question.id)
      return answered?.answer || ''
    }

    const getOptionClassForQuestion = (qIndex, optionIndex) => {
      const classes = []
      const question = questions.value[qIndex]
      if (!question) return ''

      const optionLabel = getOptionLabel(optionIndex)
      const answered = answeredQuestions.value.get(question.id)
      const hasResult = !!answered

      if (!hasResult) {
        // 当前题目未答，显示选中状态
        if (qIndex === currentIndex.value && selectedOptions.value.includes(optionIndex)) {
          classes.push('selected')
        }
      } else {
        // 已答题目，显示正确/错误状态
        // 支持 ABC 或 A,B,C 格式
        const correctAnswer = (question.correctAnswer || '').replace(/,/g, '')
        const isCorrect = correctAnswer.includes(optionLabel)
        const userAnswer = (answered.answer || '').replace(/,/g, '')
        const isSelected = userAnswer.includes(optionLabel)

        if (isCorrect) {
          classes.push('correct')
        }
        if (isSelected && !isCorrect) {
          classes.push('wrong')
        }
      }

      return classes.join(' ')
    }

    const isCorrectOptionForQuestion = (qIndex, optionIndex) => {
      const question = questions.value[qIndex]
      if (!question) return false
      // 支持 ABC 或 A,B,C 格式
      const correctAnswer = (question.correctAnswer || '').replace(/,/g, '')
      const optionLabel = getOptionLabel(optionIndex)
      return correctAnswer.includes(optionLabel)
    }

    const isWrongSelectedForQuestion = (qIndex, optionIndex) => {
      const question = questions.value[qIndex]
      if (!question) return false
      const answered = answeredQuestions.value.get(question.id)
      if (!answered) return false

      const optionLabel = getOptionLabel(optionIndex)
      // 支持 ABC 或 A,B,C 格式
      const userAnswer = (answered.answer || '').replace(/,/g, '')
      const isSelected = userAnswer.includes(optionLabel)
      return isSelected && !isCorrectOptionForQuestion(qIndex, optionIndex)
    }

    // 判断题正确答案是否为"正确"（true）
    // 支持多种格式：true/false, 正确/错误, 对/错, 是/否, 1/0
    const isJudgeAnswerTrue = (answer) => {
      if (!answer) return false
      const normalized = answer.toLowerCase()
      return normalized === 'true' || answer === '正确' ||
             answer === '对' || answer === '是' || answer === '1'
    }

    const getJudgeClass = (qIndex, value) => {
      const question = questions.value[qIndex]
      if (!question) return ''

      const answered = answeredQuestions.value.get(question.id)
      const hasResult = !!answered

      const classes = []

      if (!hasResult) {
        // 未答，显示选中状态
        if (qIndex === currentIndex.value && userAnswer.value === value) {
          classes.push('selected')
        }
      } else {
        // 已答，显示正确/错误
        // 判断正确答案是否是"正确"（true）
        const correctIsTrue = isJudgeAnswerTrue(question.correctAnswer)
        const valueIsTrue = value === 'true'

        if (correctIsTrue === valueIsTrue) {
          classes.push('correct')
        }
        const userAnswerIsTrue = answered.answer === 'true'
        if (userAnswerIsTrue === valueIsTrue && correctIsTrue !== valueIsTrue) {
          classes.push('wrong')
        }
      }

      return classes.join(' ')
    }

    const formatCorrectAnswer = (question) => {
      if (!question) return ''
      if (question.type === 'judge') {
        // 支持多种格式：true/false, 正确/错误, 对/错, 是/否, 1/0
        const answer = question.correctAnswer
        const normalized = answer.toLowerCase()
        if (normalized === 'true' || answer === '正确' ||
            answer === '对' || answer === '是' || answer === '1') {
          return '正确'
        }
        return '错误'
      }
      if (question.type === 'single' && question.options) {
        // 单选题：显示选项和答案
        const index = question.correctAnswer.charCodeAt(0) - 65
        return `${question.correctAnswer}. ${question.options[index] || ''}`
      }
      if (question.type === 'multiple' && question.options) {
        // 多选题：支持 ABC 或 A,B,C 格式，多行显示
        const answerStr = (question.correctAnswer || '').replace(/,/g, '')
        return answerStr.split('').map(a => {
          const index = a.charCodeAt(0) - 65
          return `${a}. ${question.options[index] || ''}`
        }).join('\n')
      }
      return question.correctAnswer
    }

    const formatUserAnswerForQuestion = (qIndex) => {
      const question = questions.value[qIndex]
      if (!question) return ''
      const answered = answeredQuestions.value.get(question.id)
      if (!answered) return ''

      const answer = answered.answer
      if (question.type === 'judge') {
        return answer === 'true' ? '正确' : '错误'
      }
      return answer.split(',').join('、')
    }

    const formatUserAnswer = () => {
      if (!currentQuestion.value) return ''
      if (currentQuestion.value.type === 'judge') {
        return userAnswer.value === 'true' ? '正确' : '错误'
      }
      return userAnswer.value.split(',').join('、')
    }

    // 保留原有的函数供当前题目使用
    const getOptionClass = (index) => getOptionClassForQuestion(currentIndex.value, index)
    const isCorrectOption = (index) => isCorrectOptionForQuestion(currentIndex.value, index)
    const isWrongSelected = (index) => isWrongSelectedForQuestion(currentIndex.value, index)

    onMounted(() => {
      initSession()
    })

    return {
      loading,
      bankName,
      questions,
      currentIndex,
      currentQuestion,
      userAnswer,
      selectedOptions,
      showResult,
      correctCount,
      wrongCount,
      isCurrentCorrect,
      practiceMode,
      goBack,
      selectOption,
      toggleMultipleOption,
      submitMultipleAnswer,
      selectJudge,
      prevQuestion,
      nextQuestion,
      onSwiperChange,
      getTypeLabel,
      getOptionLabel,
      getModeLabel,
      getOptionClass,
      isCorrectOption,
      isWrongSelected,
      formatCorrectAnswer,
      formatUserAnswer,
      // 新增的方法
      isShowResultForQuestion,
      isCorrectForQuestion,
      getOptionClassForQuestion,
      isCorrectOptionForQuestion,
      isWrongSelectedForQuestion,
      getJudgeClass,
      formatUserAnswerForQuestion
    }
  }
}
</script>

<style scoped>
.practice-container {
  height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 加载状态 */
.loading-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 24rpx;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid #e9ecef;
  border-top: 4rpx solid #67c23a;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-text {
  font-size: 28rpx;
  color: #909399;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 顶部导航 */
.top-bar {
  background: white;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #ebeef5;
}

.top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
  padding: 0 30rpx;
}

.nav-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-center {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.current-num {
  font-size: 36rpx;
  font-weight: 700;
  color: #67c23a;
}

.separator {
  font-size: 28rpx;
  color: #909399;
}

.total-num {
  font-size: 28rpx;
  color: #909399;
}

.back-btn {
  font-size: 28rpx;
  color: #409eff;
  min-width: 80rpx;
}

.bank-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #303133;
  max-width: 360rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: center;
  flex: 1;
}

.mode-tag {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  min-width: 80rpx;
  text-align: center;
}

.mode-tag.practice {
  background: #f0f9eb;
  color: #67c23a;
}

.mode-tag.favorite {
  background: #ecf5ff;
  color: #409eff;
}

.mode-tag.wrong {
  background: #fef0f0;
  color: #f56c6c;
}

.progress-text {
  font-size: 28rpx;
  color: #606266;
  font-weight: 500;
}

/* 题目滑动区域 */
.question-swiper {
  flex: 1;
  width: 100%;
  min-height: 400rpx;
}

.question-scroll {
  height: 100%;
  padding: 24rpx;
  box-sizing: border-box;
}

.swiper-item-content {
  height: 100%;
}

.question-card {
  background: white;
  border-radius: 20rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.question-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.question-type {
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
}

.question-type.single { background: #ecf5ff; color: #409eff; }
.question-type.multiple { background: #f0f9eb; color: #67c23a; }
.question-type.judge { background: #fdf6ec; color: #e6a23c; }
.question-type.essay { background: #f4f4f5; color: #909399; }

.question-num {
  font-size: 26rpx;
  color: #909399;
}

.favorite-btn {
  margin-left: auto;
  font-size: 24rpx;
  color: #e6a23c;
  padding: 8rpx 16rpx;
  background: #fdf6ec;
  border-radius: 8rpx;
}

.question-content {
  font-size: 32rpx;
  color: #303133;
  line-height: 1.8;
  margin-bottom: 32rpx;
}

/* 选项列表 */
.options-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.multiple-tip {
  font-size: 24rpx;
  color: #909399;
  padding: 16rpx;
  background: #f4f4f5;
  border-radius: 8rpx;
  text-align: center;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  border: 2rpx solid transparent;
  transition: all 0.2s;
}

.option-item.selected {
  background: #ecf5ff;
  border-color: #409eff;
}

.option-item.correct {
  background: #f0f9eb;
  border-color: #67c23a;
}

.option-item.wrong {
  background: #fef0f0;
  border-color: #f56c6c;
}

.checkbox {
  width: 36rpx;
  height: 36rpx;
  border: 2rpx solid #dcdfe6;
  border-radius: 6rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  font-size: 24rpx;
  color: white;
}

.checkbox.checked {
  background: #409eff;
  border-color: #409eff;
}

.option-label {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  font-weight: 600;
  color: #606266;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.option-text {
  flex: 1;
  font-size: 28rpx;
  color: #303133;
  line-height: 1.5;
}

.result-icon {
  font-size: 36rpx;
  margin-left: 16rpx;
}

.result-icon.correct { color: #67c23a; }
.result-icon.wrong { color: #f56c6c; }

.confirm-btn {
  margin-top: 24rpx;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 12rpx;
  height: 80rpx;
  font-size: 30rpx;
}

.confirm-btn[disabled] {
  opacity: 0.5;
}

/* 判断题选项 */
.judge-options {
  display: flex;
  gap: 24rpx;
}

.judge-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40rpx 24rpx;
  background: #f8f9fa;
  border-radius: 16rpx;
  border: 2rpx solid transparent;
  transition: all 0.2s;
}

.judge-item.selected {
  background: #ecf5ff;
  border-color: #409eff;
}

.judge-item.correct {
  background: #f0f9eb;
  border-color: #67c23a;
}

.judge-item.wrong {
  background: #fef0f0;
  border-color: #f56c6c;
}

.judge-icon {
  font-size: 48rpx;
  margin-bottom: 12rpx;
}

.judge-text {
  font-size: 28rpx;
  color: #303133;
}

/* 结果区域 */
.result-section {
  margin-top: 32rpx;
  padding-top: 32rpx;
  border-top: 1rpx solid #ebeef5;
}

.result-header {
  margin-bottom: 24rpx;
}

.result-status {
  font-size: 32rpx;
  font-weight: 600;
  padding: 12rpx 24rpx;
  border-radius: 8rpx;
}

.result-status.correct {
  background: #f0f9eb;
  color: #67c23a;
}

.result-status.wrong {
  background: #fef0f0;
  color: #f56c6c;
}

.answer-info {
  margin-bottom: 24rpx;
}

.answer-row {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.answer-label {
  font-size: 26rpx;
  color: #909399;
  width: 160rpx;
}

.answer-value {
  font-size: 28rpx;
  font-weight: 600;
}

.answer-value.correct { color: #67c23a; }
.answer-value.wrong { color: #f56c6c; }

.analysis-section {
  background: #f8f9fa;
  border-radius: 12rpx;
  padding: 24rpx;
}

.analysis-label {
  font-size: 26rpx;
  font-weight: 600;
  color: #303133;
  display: block;
  margin-bottom: 12rpx;
}

.analysis-content {
  font-size: 26rpx;
  color: #606266;
  line-height: 1.8;
}

/* 统计栏 - 顶部 */
.stats-row {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 40rpx;
  margin-top: 16rpx;
  padding: 16rpx 30rpx 0;
  border-top: 1rpx solid #ebeef5;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.stat-value {
  font-size: 28rpx;
  font-weight: 700;
  color: #303133;
}

.stat-value.correct {
  color: #67c23a;
}

.stat-value.wrong {
  color: #f56c6c;
}

.stat-label {
  font-size: 24rpx;
  color: #909399;
}

.swipe-tip {
  margin-left: auto;
  font-size: 22rpx;
  color: #c0c4cc;
}

.nav-btn {
  min-width: 160rpx;
  height: 68rpx;
  border-radius: 0;
  font-size: 28rpx;
  background: #f5f7fa;
  color: #606266;
  border: none;
  padding: 0 30rpx;
}

.nav-btn[disabled] {
  opacity: 0.5;
}

.nav-btn.prev {
  background: #f5f7fa;
  color: #606266;
}

.nav-btn.next {
  background: #67c23a;
  color: white;
}

/* 空状态 */
.empty-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 32rpx;
  opacity: 0.6;
}

.empty-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16rpx;
}

.empty-desc {
  font-size: 28rpx;
  color: #909399;
  margin-bottom: 40rpx;
}

.back-home-btn {
  background: #67c23a;
  color: white;
  border: none;
  border-radius: 12rpx;
  padding: 20rpx 60rpx;
  font-size: 28rpx;
}

/* 占位符样式 */
.question-placeholder {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.placeholder-text {
  font-size: 32rpx;
  color: #c0c4cc;
}
</style>
