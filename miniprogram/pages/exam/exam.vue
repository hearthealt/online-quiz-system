<template>
  <view class="exam-container">
    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 答题区域 -->
    <template v-else-if="questions.length > 0">
      <!-- 顶部导航 -->
      <view class="top-bar">
        <view class="top-row">
          <text class="back-btn" @click="goBack">返回</text>
          <text class="bank-name">{{ bankName }}</text>
          <text class="mode-tag exam">考试</text>
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
            :disabled="currentIndex >= questions.length - 1"
            @click="nextQuestion"
          >
            下一题
          </button>
        </view>
        <!-- 统计栏 -->
        <view class="stats-row">
          <view class="stat-item">
            <text class="stat-value answered">{{ answeredCount }}</text>
            <text class="stat-label">已答</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ questions.length - answeredCount }}</text>
            <text class="stat-label">未答</text>
          </view>
          <view class="stat-item">
            <text class="stat-value total">{{ questions.length }}</text>
            <text class="stat-label">总题数</text>
          </view>
          <button class="submit-btn" @click="submitExam">交卷</button>
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
                  :class="{ 'selected': isOptionSelectedForQuestion(qIndex, index) }"
                  @click="qIndex === currentIndex && selectOption(index)"
                >
                  <text class="option-label">{{ getOptionLabel(index) }}</text>
                  <text class="option-text">{{ option }}</text>
                </view>
              </view>

              <!-- 多选题选项 -->
              <view v-if="question.type === 'multiple'" class="options-list">
                <view class="multiple-tip">多选题，可选择多个答案</view>
                <view
                  v-for="(option, index) in question.options"
                  :key="index"
                  class="option-item multiple"
                  :class="{ 'selected': isMultipleSelectedForQuestion(qIndex, index) }"
                  @click="qIndex === currentIndex && toggleMultipleOption(index)"
                >
                  <view class="checkbox" :class="{ checked: qIndex === currentIndex && selectedOptions.includes(index) }">
                    <text v-if="qIndex === currentIndex && selectedOptions.includes(index)">✓</text>
                  </view>
                  <text class="option-label">{{ getOptionLabel(index) }}</text>
                  <text class="option-text">{{ option }}</text>
                </view>
              </view>

              <!-- 判断题选项 -->
              <view v-if="question.type === 'judge'" class="judge-options">
                <view
                  class="judge-item"
                  :class="{ 'selected': isJudgeSelectedForQuestion(qIndex, 'true') }"
                  @click="qIndex === currentIndex && selectJudge('true')"
                >
                  <text class="judge-icon">✓</text>
                  <text class="judge-text">正确</text>
                </view>
                <view
                  class="judge-item"
                  :class="{ 'selected': isJudgeSelectedForQuestion(qIndex, 'false') }"
                  @click="qIndex === currentIndex && selectJudge('false')"
                >
                  <text class="judge-icon">✗</text>
                  <text class="judge-text">错误</text>
                </view>
              </view>

              <!-- 已作答标记 -->
              <view v-if="isAnsweredForQuestion(qIndex)" class="answered-mark">
                <text class="mark-icon">✓</text>
                <text class="mark-text">已作答</text>
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
import { ref, computed, onMounted, watch } from 'vue'
import { sessionAPI } from '@/utils/api'

export default {
  name: 'ExamPage',
  setup() {
    const loading = ref(true)
    const bankId = ref(null)
    const bankName = ref('')
    const sessionId = ref('')
    const questions = ref([])
    const currentIndex = ref(0)

    // 存储所有答案 questionId -> answer
    const answers = ref({})

    // 当前题目状态
    const userAnswer = ref('')
    const selectedOptions = ref([])

    // 当前题目
    const currentQuestion = computed(() => questions.value[currentIndex.value])

    // 统计已答数量
    const answeredCount = computed(() => {
      return Object.keys(answers.value).filter(key => {
        const answer = answers.value[key]
        return answer !== undefined && answer !== null && answer !== ''
      }).length
    })

    // 当前题是否已作答
    const isCurrentAnswered = computed(() => {
      if (!currentQuestion.value) return false
      const answer = answers.value[currentQuestion.value.id]
      return answer !== undefined && answer !== null && answer !== ''
    })

    // 初始化会话
    const initSession = async () => {
      loading.value = true
      try {
        // 获取页面参数
        const pages = getCurrentPages()
        const currentPage = pages[pages.length - 1]
        const options = currentPage.options

        bankId.value = Number(options.bankId)
        bankName.value = decodeURIComponent(options.bankName || '')
        const existingSessionId = options.sessionId

        let currentSession
        let questionList = []

        if (existingSessionId) {
          // 继续已有会话 - 使用 getSessionDetail
          const detail = await sessionAPI.getSessionDetail(existingSessionId)
          currentSession = detail.session
          questionList = detail.questions || []
          // 恢复已有答案
          if (detail.answers) {
            detail.answers.forEach(a => {
              if (a.userAnswer) {
                answers.value[a.questionId] = a.userAnswer
              }
            })
          }
        } else {
          // 创建新考试会话 - 使用 startSession
          const result = await sessionAPI.startSession({
            bankId: bankId.value,
            mode: 'exam'
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
          title: '加载失败',
          icon: 'none'
        })
      } finally {
        loading.value = false
      }
    }

    // 加载当前题的已答状态
    const loadCurrentQuestionState = () => {
      if (!currentQuestion.value) return

      const answer = answers.value[currentQuestion.value.id]
      if (answer !== undefined && answer !== null && answer !== '') {
        userAnswer.value = answer

        // 恢复选项状态 - 支持 ABC 或 A,B,C 格式
        if (currentQuestion.value.type === 'multiple') {
          const answerStr = answer.replace(/,/g, '')
          selectedOptions.value = answerStr.split('').map(label => label.charCodeAt(0) - 65)
        } else if (currentQuestion.value.type === 'single') {
          // 单选答案格式是 "A"
          userAnswer.value = answer
        }
      } else {
        userAnswer.value = ''
        selectedOptions.value = []
      }
    }

    // 保存当前题答案到answers对象，并同步到服务器
    const saveCurrentAnswer = async () => {
      if (!currentQuestion.value) return

      const qId = currentQuestion.value.id
      let answer = ''

      if (currentQuestion.value.type === 'multiple') {
        if (selectedOptions.value.length > 0) {
          const labels = selectedOptions.value
            .sort((a, b) => a - b)
            .map(idx => getOptionLabel(idx))
          // 多选答案格式：ABCD（无逗号）
          answer = labels.join('')
        }
      } else if (currentQuestion.value.type === 'single') {
        answer = userAnswer.value
      } else if (currentQuestion.value.type === 'judge') {
        answer = userAnswer.value
      }

      // 保存到本地
      answers.value[qId] = answer

      // 同步到服务器（静默保存，不显示结果）
      if (sessionId.value && answer) {
        try {
          await sessionAPI.submitAnswer(sessionId.value, {
            questionId: qId,
            questionIndex: currentIndex.value,
            userAnswer: answer
          })
        } catch (error) {
          console.error('保存答案失败:', error)
        }
      }
    }

    // 选择单选选项
    const selectOption = (index) => {
      const optionLabel = getOptionLabel(index)
      userAnswer.value = optionLabel
      saveCurrentAnswer()
    }

    // 检查选项是否被选中
    const isOptionSelected = (index) => {
      const optionLabel = getOptionLabel(index)
      return userAnswer.value === optionLabel
    }

    // 切换多选选项
    const toggleMultipleOption = (index) => {
      const idx = selectedOptions.value.indexOf(index)
      if (idx > -1) {
        selectedOptions.value.splice(idx, 1)
      } else {
        selectedOptions.value.push(index)
      }
      saveCurrentAnswer()
    }

    // 选择判断题
    const selectJudge = (value) => {
      userAnswer.value = value
      saveCurrentAnswer()
    }

    // 上一题
    const prevQuestion = () => {
      if (currentIndex.value > 0) {
        saveCurrentAnswer()
        currentIndex.value--
        loadCurrentQuestionState()
      }
    }

    // 下一题
    const nextQuestion = () => {
      if (currentIndex.value < questions.value.length - 1) {
        saveCurrentAnswer()
        currentIndex.value++
        loadCurrentQuestionState()
      }
    }

    // 提交考试
    const submitExam = async () => {
      await saveCurrentAnswer()

      const unansweredCount = questions.value.length - answeredCount.value

      let content = '确定要交卷吗？提交后无法修改。'
      if (unansweredCount > 0) {
        content = `还有 ${unansweredCount} 道题目未作答，确定要交卷吗？`
      }

      uni.showModal({
        title: '交卷确认',
        content: content,
        confirmText: '确定交卷',
        cancelText: '继续作答',
        success: async (res) => {
          if (res.confirm) {
            await doSubmitExam()
          }
        }
      })
    }

    // 执行提交 - 使用 submitExam 批量提交
    const doSubmitExam = async () => {
      try {
        uni.showLoading({ title: '提交中...' })

        // 构建答案列表
        const answerList = questions.value.map((q, index) => ({
          questionId: q.id,
          questionIndex: index,
          userAnswer: answers.value[q.id] || ''
        }))

        // 批量提交考试答案
        await sessionAPI.submitExam(sessionId.value, answerList)

        uni.hideLoading()
        uni.showToast({
          title: '提交成功',
          icon: 'success'
        })

        setTimeout(() => {
          uni.redirectTo({
            url: `/pages/result/result?sessionId=${sessionId.value}`
          })
        }, 1500)
      } catch (error) {
        uni.hideLoading()
        console.error('提交失败:', error)
        uni.showToast({
          title: '提交失败',
          icon: 'none'
        })
      }
    }

    // 返回
    const goBack = () => {
      const unansweredCount = questions.value.length - answeredCount.value

      if (answeredCount.value > 0) {
        uni.showModal({
          title: '退出考试',
          content: '确定要退出吗？当前进度将会保存，可以稍后继续作答。',
          confirmText: '确定退出',
          cancelText: '继续作答',
          success: (res) => {
            if (res.confirm) {
              uni.navigateBack()
            }
          }
        })
      } else {
        uni.navigateBack()
      }
    }

    // swiper 滑动事件
    const onSwiperChange = (e) => {
      const newIndex = e.detail.current
      if (newIndex !== currentIndex.value) {
        saveCurrentAnswer()
        currentIndex.value = newIndex
        loadCurrentQuestionState()
      }
    }

    // 针对特定题目的辅助函数（用于swiper中显示已答题的状态）
    const getAnswerForQuestion = (qIndex) => {
      const question = questions.value[qIndex]
      if (!question) return ''
      return answers.value[question.id] || ''
    }

    const isAnsweredForQuestion = (qIndex) => {
      const answer = getAnswerForQuestion(qIndex)
      return answer !== undefined && answer !== null && answer !== ''
    }

    const isOptionSelectedForQuestion = (qIndex, optionIndex) => {
      const question = questions.value[qIndex]
      if (!question || question.type !== 'single') return false

      const answer = answers.value[question.id]
      if (!answer) {
        // 当前题且未保存到answers时，检查userAnswer
        if (qIndex === currentIndex.value) {
          return userAnswer.value === getOptionLabel(optionIndex)
        }
        return false
      }
      return answer === getOptionLabel(optionIndex)
    }

    const isMultipleSelectedForQuestion = (qIndex, optionIndex) => {
      const question = questions.value[qIndex]
      if (!question || question.type !== 'multiple') return false

      const answer = answers.value[question.id]
      if (!answer) {
        // 当前题且未保存到answers时，检查selectedOptions
        if (qIndex === currentIndex.value) {
          return selectedOptions.value.includes(optionIndex)
        }
        return false
      }
      // 支持 ABC 或 A,B,C 格式
      const answerStr = answer.replace(/,/g, '')
      return answerStr.includes(getOptionLabel(optionIndex))
    }

    const isJudgeSelectedForQuestion = (qIndex, value) => {
      const question = questions.value[qIndex]
      if (!question || question.type !== 'judge') return false

      const answer = answers.value[question.id]
      if (!answer) {
        // 当前题且未保存到answers时，检查userAnswer
        if (qIndex === currentIndex.value) {
          return userAnswer.value === value
        }
        return false
      }
      return answer === value
    }

    // 工具函数
    const getTypeLabel = (type) => {
      const labels = { single: '单选题', multiple: '多选题', judge: '判断题', essay: '简答题' }
      return labels[type] || type
    }

    const getOptionLabel = (index) => String.fromCharCode(65 + index)

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
      answeredCount,
      isCurrentAnswered,
      goBack,
      selectOption,
      isOptionSelected,
      toggleMultipleOption,
      selectJudge,
      prevQuestion,
      nextQuestion,
      submitExam,
      getTypeLabel,
      getOptionLabel,
      onSwiperChange,
      isAnsweredForQuestion,
      isOptionSelectedForQuestion,
      isMultipleSelectedForQuestion,
      isJudgeSelectedForQuestion
    }
  }
}
</script>

<style scoped>
.exam-container {
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
  border-top: 4rpx solid #e6a23c;
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
  color: #e6a23c;
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

.mode-tag.exam {
  background: #fdf6ec;
  color: #e6a23c;
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
  background: #fdf6ec;
  border-color: #e6a23c;
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
  background: #e6a23c;
  border-color: #e6a23c;
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

.option-item.selected .option-label {
  background: #e6a23c;
  color: white;
}

.option-text {
  flex: 1;
  font-size: 28rpx;
  color: #303133;
  line-height: 1.5;
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
  background: #fdf6ec;
  border-color: #e6a23c;
}

.judge-icon {
  font-size: 48rpx;
  margin-bottom: 12rpx;
}

.judge-text {
  font-size: 28rpx;
  color: #303133;
}

/* 已作答标记 */
.answered-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  margin-top: 32rpx;
  padding: 16rpx;
  background: #fdf6ec;
  border-radius: 12rpx;
}

.mark-icon {
  color: #e6a23c;
  font-size: 28rpx;
}

.mark-text {
  font-size: 26rpx;
  color: #e6a23c;
  font-weight: 500;
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

.stat-value.answered {
  color: #e6a23c;
}

.stat-value.total {
  color: #409eff;
}

.stat-label {
  font-size: 24rpx;
  color: #909399;
}

.submit-btn {
  margin-left: auto;
  margin-right: -30rpx;
  background: #f56c6c;
  color: white;
  font-size: 26rpx;
  padding: 12rpx 32rpx;
  border-radius: 0;
  border: none;
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
  background: #e6a23c;
  color: white;
}

.nav-btn.next[disabled] {
  background: #faecd8;
  opacity: 0.6;
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
  background: #e6a23c;
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
