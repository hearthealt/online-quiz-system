<template>
  <view class="result-container">
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <view class="loading-spinner"></view>
      <text class="loading-text">正在计算成绩...</text>
    </view>

    <!-- 结果内容 -->
    <view v-else class="result-content">
      <!-- 成绩卡片 -->
      <view class="score-card">
        <view class="score-header">
          <view class="score-title">{{ isPassed ? '答题完成！' : '继续努力！' }}</view>
          <view class="score-subtitle">{{ bankName }}</view>
        </view>

        <view class="score-overview">
          <view class="score-circle" :class="{ passed: isPassed }">
            <view class="score-value">{{ Math.round(accuracy) }}</view>
            <view class="score-unit">%</view>
          </view>
          <view class="score-label">正确率</view>
        </view>

        <view class="score-details">
          <view class="detail-item">
            <view class="detail-value success">{{ correctCount }}</view>
            <view class="detail-label">正确</view>
          </view>
          <view class="detail-divider"></view>
          <view class="detail-item">
            <view class="detail-value error">{{ wrongCount }}</view>
            <view class="detail-label">错误</view>
          </view>
          <view class="detail-divider"></view>
          <view class="detail-item">
            <view class="detail-value">{{ totalCount }}</view>
            <view class="detail-label">总题数</view>
          </view>
        </view>
      </view>

      <!-- 答题详情 -->
      <view class="question-details-card">
        <view class="card-title">
          <text class="title-text">答题详情</text>
        </view>

        <view class="question-list" v-if="answeredQuestions.length > 0">
          <view
            v-for="(item, index) in answeredQuestions"
            :key="index"
            class="question-item"
            :class="{ correct: item.isCorrect, incorrect: !item.isCorrect }"
          >
            <!-- 题目头部 -->
            <view class="question-header">
              <view class="question-left">
                <text class="question-number">第{{ index + 1 }}题</text>
                <text class="question-type" :class="item.question?.type">
                  {{ getTypeLabel(item.question?.type) }}
                </text>
              </view>
              <text class="question-status" :class="{ correct: item.isCorrect, incorrect: !item.isCorrect }">
                {{ item.isCorrect ? '✓ 正确' : '✗ 错误' }}
              </text>
            </view>

            <!-- 题目内容 -->
            <view class="question-content">
              {{ item.question?.content || '题目内容' }}
            </view>

            <!-- 答案对比 -->
            <view class="answer-section">
              <view class="answer-item">
                <text class="answer-label">你的答案：</text>
                <text class="answer-text" :class="{ correct: item.isCorrect, incorrect: !item.isCorrect }">
                  {{ formatUserAnswer(item) }}
                </text>
              </view>
              <view v-if="!item.isCorrect" class="answer-item">
                <text class="answer-label">正确答案：</text>
                <text class="answer-text correct">
                  {{ formatCorrectAnswer(item.question) }}
                </text>
              </view>
              <view v-if="item.question?.analysis" class="answer-item analysis">
                <text class="answer-label">解析：</text>
                <text class="answer-analysis">{{ item.question.analysis }}</text>
              </view>
            </view>
          </view>
        </view>

        <view v-else class="no-data">
          <text>暂无答题记录</text>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-section">
        <button class="action-btn primary" @click="retryExam">
          再来一次
        </button>
        <button class="action-btn secondary" @click="goToWrongQuestions" v-if="wrongCount > 0">
          查看错题
        </button>
        <button class="action-btn default" @click="goToHome">
          返回首页
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { sessionAPI } from '@/utils/api'

export default {
  name: 'ResultPage',
  setup() {
    const loading = ref(true)
    const sessionId = ref('')
    const bankId = ref('')
    const bankName = ref('')
    const mode = ref('practice')
    const questions = ref([])
    const answeredQuestions = ref([])

    // 统计数据
    const totalCount = computed(() => questions.value.length)
    const correctCount = computed(() => answeredQuestions.value.filter(a => a.isCorrect).length)
    const wrongCount = computed(() => answeredQuestions.value.filter(a => !a.isCorrect).length)
    const accuracy = computed(() => {
      if (totalCount.value === 0) return 0
      return (correctCount.value / totalCount.value) * 100
    })
    const isPassed = computed(() => accuracy.value >= 60)

    // 加载结果数据 - 使用 getSessionDetail
    const loadResultData = async () => {
      try {
        loading.value = true

        // 获取页面参数
        const pages = getCurrentPages()
        const currentPage = pages[pages.length - 1]
        const options = currentPage.options
        sessionId.value = options.sessionId || ''

        if (!sessionId.value) {
          throw new Error('缺少会话ID')
        }

        // 获取会话详情
        const detail = await sessionAPI.getSessionDetail(sessionId.value)
        const sessionData = detail.session

        bankId.value = sessionData.bankId
        bankName.value = sessionData.bankName || '答题结果'
        mode.value = sessionData.mode || 'practice'
        questions.value = detail.questions || []

        // 处理已答题记录
        if (detail.answers && detail.answers.length > 0) {
          // 将答题记录与题目关联
          answeredQuestions.value = detail.answers.map(aq => {
            const question = questions.value.find(q => q.id === aq.questionId)
            return {
              questionId: aq.questionId,
              userAnswer: aq.userAnswer,
              isCorrect: aq.isCorrect,
              question: question
            }
          })
        } else {
          // 如果没有答题记录，显示所有题目为未作答
          answeredQuestions.value = questions.value.map(q => ({
            questionId: q.id,
            userAnswer: '',
            isCorrect: false,
            question: q
          }))
        }

      } catch (error) {
        console.error('加载结果数据失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      } finally {
        loading.value = false
      }
    }

    // 格式化用户答案
    const formatUserAnswer = (item) => {
      if (!item.userAnswer) return '未作答'

      const question = item.question
      if (!question) return item.userAnswer

      switch (question.type) {
        case 'single':
          // 单选题：A -> A. 选项内容
          if (question.options && item.userAnswer.length === 1) {
            const index = item.userAnswer.charCodeAt(0) - 65
            if (index >= 0 && index < question.options.length) {
              return `${item.userAnswer}. ${question.options[index]}`
            }
          }
          return item.userAnswer

        case 'multiple':
          // 多选题：A,B,C -> A. 选项1、B. 选项2
          if (question.options) {
            const labels = item.userAnswer.split(',').filter(s => s)
            return labels.map(label => {
              const index = label.charCodeAt(0) - 65
              if (index >= 0 && index < question.options.length) {
                return `${label}. ${question.options[index]}`
              }
              return label
            }).join('、')
          }
          return item.userAnswer

        case 'judge':
          return item.userAnswer === 'true' ? '正确' : '错误'

        default:
          return item.userAnswer
      }
    }

    // 格式化正确答案
    const formatCorrectAnswer = (question) => {
      if (!question || !question.correctAnswer) return '无'

      switch (question.type) {
        case 'single':
          if (question.options && question.correctAnswer.length === 1) {
            const index = question.correctAnswer.charCodeAt(0) - 65
            if (index >= 0 && index < question.options.length) {
              return `${question.correctAnswer}. ${question.options[index]}`
            }
          }
          return question.correctAnswer

        case 'multiple':
          if (question.options) {
            const labels = question.correctAnswer.split(',').filter(s => s)
            return labels.map(label => {
              const index = label.charCodeAt(0) - 65
              if (index >= 0 && index < question.options.length) {
                return `${label}. ${question.options[index]}`
              }
              return label
            }).join('、')
          }
          return question.correctAnswer

        case 'judge':
          // 支持多种格式：true/false, 正确/错误, 对/错, 是/否, 1/0
          const answer = question.correctAnswer
          const normalized = answer.toLowerCase()
          if (normalized === 'true' || answer === '正确' ||
              answer === '对' || answer === '是' || answer === '1') {
            return '正确'
          }
          return '错误'

        default:
          return question.correctAnswer
      }
    }

    // 获取题型标签
    const getTypeLabel = (type) => {
      const labels = { single: '单选', multiple: '多选', judge: '判断', essay: '简答' }
      return labels[type] || type
    }

    // 再来一次 - 使用 resetSession
    const retryExam = async () => {
      try {
        // 重置会话
        await sessionAPI.resetSession(bankId.value, mode.value)

        // 跳转到答题页面
        if (mode.value === 'exam') {
          uni.redirectTo({
            url: `/pages/exam/exam?bankId=${bankId.value}&bankName=${encodeURIComponent(bankName.value)}`
          })
        } else {
          uni.redirectTo({
            url: `/pages/practice/practice?bankId=${bankId.value}&bankName=${encodeURIComponent(bankName.value)}`
          })
        }
      } catch (error) {
        console.error('重置失败:', error)
        uni.showToast({
          title: '操作失败',
          icon: 'none'
        })
      }
    }

    // 跳转到错题页面
    const goToWrongQuestions = () => {
      uni.switchTab({
        url: '/pages/wrong-questions/wrong-questions'
      })
    }

    // 跳转到首页
    const goToHome = () => {
      uni.switchTab({
        url: '/pages/index/index'
      })
    }

    // 页面加载
    onMounted(() => {
      loadResultData()
    })

    return {
      loading,
      bankName,
      totalCount,
      correctCount,
      wrongCount,
      accuracy,
      isPassed,
      answeredQuestions,
      formatUserAnswer,
      formatCorrectAnswer,
      getTypeLabel,
      retryExam,
      goToWrongQuestions,
      goToHome
    }
  }
}
</script>

<style scoped>
.result-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  gap: 24rpx;
}

.loading-spinner {
  width: 48rpx;
  height: 48rpx;
  border: 4rpx solid #e9ecef;
  border-top: 4rpx solid #409eff;
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

.result-content {
  max-width: 750rpx;
  margin: 0 auto;
}

/* 成绩卡片 */
.score-card {
  background: #fff;
  border-radius: 12rpx;
  padding: 32rpx;
  margin-bottom: 16rpx;
}

.score-header {
  text-align: center;
  margin-bottom: 24rpx;
}

.score-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8rpx;
}

.score-subtitle {
  font-size: 26rpx;
  color: #909399;
}

.score-overview {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24rpx;
  padding-bottom: 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.score-circle {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  border: 8rpx solid #f56c6c;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
}

.score-circle.passed {
  border-color: #67c23a;
}

.score-value {
  font-size: 52rpx;
  font-weight: 700;
  color: #303133;
  line-height: 1;
}

.score-unit {
  font-size: 24rpx;
  color: #909399;
}

.score-label {
  font-size: 26rpx;
  color: #909399;
}

.score-details {
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.detail-item {
  text-align: center;
  flex: 1;
}

.detail-divider {
  width: 1rpx;
  height: 60rpx;
  background: #f0f0f0;
}

.detail-value {
  font-size: 36rpx;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4rpx;
}

.detail-value.success { color: #67c23a; }
.detail-value.error { color: #f56c6c; }

.detail-label {
  font-size: 24rpx;
  color: #909399;
}

/* 答题详情卡片 */
.question-details-card {
  background: white;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 16rpx;
}

.card-title {
  margin-bottom: 16rpx;
  padding-bottom: 12rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.title-text {
  font-size: 30rpx;
  font-weight: 600;
  color: #303133;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.question-item {
  padding: 16rpx;
  border: 2rpx solid #e4e7ed;
  border-radius: 8rpx;
  background: white;
}

.question-item.correct {
  border-color: #67c23a;
  background: #f0f9eb;
}

.question-item.incorrect {
  border-color: #f56c6c;
  background: #fef0f0;
}

.question-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.question-left {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.question-number {
  font-size: 26rpx;
  font-weight: 600;
  color: #303133;
}

.question-type {
  font-size: 22rpx;
  padding: 4rpx 10rpx;
  border-radius: 4rpx;
}

.question-type.single { background: #ecf5ff; color: #409eff; }
.question-type.multiple { background: #f0f9eb; color: #67c23a; }
.question-type.judge { background: #fdf6ec; color: #e6a23c; }
.question-type.essay { background: #f4f4f5; color: #909399; }

.question-status {
  font-size: 24rpx;
  font-weight: 500;
}

.question-status.correct { color: #67c23a; }
.question-status.incorrect { color: #f56c6c; }

.question-content {
  font-size: 28rpx;
  color: #303133;
  line-height: 1.6;
  margin-bottom: 12rpx;
  padding: 12rpx;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 6rpx;
}

.answer-section {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.answer-item {
  display: flex;
  align-items: flex-start;
  gap: 8rpx;
}

.answer-label {
  font-size: 24rpx;
  color: #606266;
  font-weight: 500;
  min-width: 120rpx;
  flex-shrink: 0;
}

.answer-text {
  font-size: 24rpx;
  line-height: 1.5;
  word-break: break-word;
  flex: 1;
}

.answer-text.correct { color: #67c23a; font-weight: 500; }
.answer-text.incorrect { color: #f56c6c; font-weight: 500; }

.answer-item.analysis {
  margin-top: 8rpx;
  padding-top: 10rpx;
  border-top: 1rpx dashed #dcdfe6;
}

.answer-analysis {
  font-size: 24rpx;
  color: #606266;
  line-height: 1.6;
  padding: 10rpx;
  background: #f5f7fa;
  border-radius: 6rpx;
  flex: 1;
}

.no-data {
  padding: 60rpx 20rpx;
  text-align: center;
  color: #909399;
  font-size: 28rpx;
}

/* 操作按钮 */
.action-section {
  display: flex;
  gap: 12rpx;
}

.action-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 8rpx;
  border: none;
  font-size: 28rpx;
  font-weight: 500;
}

.action-btn.primary {
  background: #409eff;
  color: white;
}

.action-btn.secondary {
  background: #e6a23c;
  color: white;
}

.action-btn.default {
  background: #fff;
  color: #606266;
  border: 2rpx solid #dcdfe6;
}

.action-btn:active {
  opacity: 0.8;
}
</style>
