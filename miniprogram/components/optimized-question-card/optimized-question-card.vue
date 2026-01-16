<template>
  <view class="optimized-question-card" :class="cardClass">
    <!-- 题目头部信息 -->
    <view class="question-header" v-if="showHeader">
      <view class="question-meta">
        <view class="meta-tag difficulty" :class="`level-${question.difficulty || 1}`">
          {{ difficultyLabels[question.difficulty || 1] }}
        </view>
        <view class="meta-tag type" :class="`type-${question.type}`">
          {{ typeLabels[question.type] }}
        </view>
        <view class="meta-tag category" v-if="question.category">
          {{ question.category }}
        </view>
      </view>

      <view class="question-actions">
        <view class="score-display" v-if="question.score">
          <text class="score-icon">⭐</text>
          <text class="score-text">{{ question.score }}分</text>
        </view>
        <view
          class="favorite-btn"
          :class="{ active: isFavorited }"
          @tap="handleFavoriteToggle"
          v-if="showFavorite"
        >
          <text class="favorite-icon">{{ isFavorited ? '❤️' : '🤍' }}</text>
        </view>
      </view>
    </view>

    <!-- 题目内容区域 -->
    <view class="question-content">
      <view class="question-title" v-if="question.title">
        {{ question.title }}
      </view>
      <view class="question-text">
        <text>{{ question.content }}</text>
      </view>
    </view>

    <!-- 选择题选项 -->
    <view
      class="options-container"
      v-if="isChoiceQuestion && question.options && question.options.length"
    >
      <view
        v-for="(option, index) in question.options"
        :key="`option-${index}`"
        class="option-item"
        :class="getOptionClass(index)"
        @tap="handleOptionSelect(index)"
      >
        <view class="option-indicator">
          <text class="option-label">{{ getOptionLabel(index) }}</text>
          <view class="option-status" v-if="showResult">
            <text
              class="status-icon"
              :class="getStatusIconClass(index)"
            >
              {{ getStatusIcon(index) }}
            </text>
          </view>
        </view>
        <view class="option-content">
          <text>{{ option }}</text>
        </view>
      </view>
    </view>

    <!-- 判断题选项 -->
    <view class="judge-options" v-if="question.type === 'judge'">
      <view
        class="judge-option"
        :class="{
          selected: selectedAnswer === 'true',
          correct: showResult && correctAnswer === 'true',
          incorrect: showResult && selectedAnswer === 'true' && correctAnswer !== 'true'
        }"
        @tap="handleJudgeSelect('true')"
      >
        <text class="judge-icon">✓</text>
        <text class="judge-text">正确</text>
      </view>
      <view
        class="judge-option"
        :class="{
          selected: selectedAnswer === 'false',
          correct: showResult && correctAnswer === 'false',
          incorrect: showResult && selectedAnswer === 'false' && correctAnswer !== 'false'
        }"
        @tap="handleJudgeSelect('false')"
      >
        <text class="judge-icon">✗</text>
        <text class="judge-text">错误</text>
      </view>
    </view>

    <!-- 简答题输入框 -->
    <view class="essay-container" v-if="question.type === 'essay'">
      <textarea
        class="essay-input"
        :value="selectedAnswer"
        @input="handleEssayInput"
        placeholder="请输入您的答案..."
        :disabled="showResult"
        :maxlength="1000"
        auto-height
      />
      <view class="essay-info">
        <text class="char-count">{{ essayCharCount }}/1000</text>
      </view>
    </view>

    <!-- 答案解析 -->
    <view class="answer-analysis" v-if="showResult && question.analysis">
      <view class="analysis-header">
        <text class="analysis-title">答案解析</text>
      </view>
      <view class="analysis-content">
        <text>{{ question.analysis }}</text>
      </view>
    </view>

    <!-- 操作按钮区域 -->
    <view class="question-actions-bottom" v-if="showActions">
      <button
        class="action-btn prev-btn"
        :disabled="!canGoPrev"
        @tap="handlePrevQuestion"
        v-if="showNavigation"
      >
        上一题
      </button>

      <button
        class="action-btn submit-btn"
        :class="{ disabled: !hasAnswer }"
        @tap="handleSubmitAnswer"
        v-if="!showResult"
      >
        {{ submitButtonText }}
      </button>

      <button
        class="action-btn next-btn"
        :disabled="!canGoNext"
        @tap="handleNextQuestion"
        v-if="showNavigation"
      >
        下一题
      </button>
    </view>
  </view>
</template>

<script>
import { store } from '@/utils/store.js'
import { performanceOptimizer } from '@/utils/performance.js'

export default {
  name: 'OptimizedQuestionCard',

  props: {
    question: {
      type: Object,
      required: true
    },
    showHeader: {
      type: Boolean,
      default: true
    },
    showFavorite: {
      type: Boolean,
      default: true
    },
    showActions: {
      type: Boolean,
      default: true
    },
    showNavigation: {
      type: Boolean,
      default: true
    },
    showResult: {
      type: Boolean,
      default: false
    },
    mode: {
      type: String,
      default: 'practice' // practice | exam
    }
  },

  data() {
    return {
      selectedAnswer: '',
      isFavorited: false,

      // 静态数据，避免重复计算
      difficultyLabels: {
        1: '简单',
        2: '中等',
        3: '困难'
      },

      typeLabels: {
        single: '单选',
        multiple: '多选',
        judge: '判断',
        essay: '简答'
      }
    }
  },

  computed: {
    cardClass() {
      return {
        'exam-mode': this.mode === 'exam',
        'practice-mode': this.mode === 'practice',
        'has-result': this.showResult,
        [`type-${this.question.type}`]: true
      }
    },

    isChoiceQuestion() {
      return ['single', 'multiple'].includes(this.question.type)
    },

    correctAnswer() {
      return this.question.correctAnswer
    },

    hasAnswer() {
      if (this.question.type === 'essay') {
        return this.selectedAnswer.trim().length > 0
      }
      return this.selectedAnswer !== ''
    },

    essayCharCount() {
      return this.selectedAnswer.length
    },

    submitButtonText() {
      return this.hasAnswer ? '提交答案' : '请选择答案'
    },

    canGoPrev() {
      const currentIndex = store.getState('questions.currentIndex')
      return currentIndex > 0
    },

    canGoNext() {
      const currentIndex = store.getState('questions.currentIndex')
      const questionList = store.getState('questions.questionList')
      return currentIndex < questionList.length - 1
    }
  },

  watch: {
    question: {
      handler(newQuestion) {
        if (newQuestion && newQuestion.id) {
          this.loadUserAnswer()
          this.loadFavoriteStatus()
        }
      },
      immediate: true
    }
  },

  methods: {
    // 加载用户答案
    loadUserAnswer() {
      const answers = store.getState('quiz.answers')
      const answer = answers.get(this.question.id)

      if (answer !== undefined) {
        this.selectedAnswer = answer
      } else {
        this.selectedAnswer = this.getDefaultAnswer()
      }
    },

    // 获取默认答案
    getDefaultAnswer() {
      switch (this.question.type) {
        case 'single':
        case 'judge':
          return ''
        case 'multiple':
          return []
        case 'essay':
          return ''
        default:
          return ''
      }
    },

    // 加载收藏状态
    async loadFavoriteStatus() {
      try {
        // 这里可以调用API检查收藏状态
        // const isFavorited = await checkFavoriteStatus(this.question.id)
        // this.isFavorited = isFavorited
      } catch (error) {
        console.error('加载收藏状态失败:', error)
      }
    },

    // 选项点击处理
    handleOptionSelect(index) {
      if (this.showResult) return

      const optionValue = this.getOptionValue(index)

      if (this.question.type === 'single') {
        this.selectedAnswer = optionValue
      } else if (this.question.type === 'multiple') {
        if (!Array.isArray(this.selectedAnswer)) {
          this.selectedAnswer = []
        }

        const selectedIndex = this.selectedAnswer.indexOf(optionValue)
        if (selectedIndex > -1) {
          this.selectedAnswer.splice(selectedIndex, 1)
        } else {
          this.selectedAnswer.push(optionValue)
        }
      }

      this.saveAnswer()
    },

    // 判断题选择处理
    handleJudgeSelect(value) {
      if (this.showResult) return

      this.selectedAnswer = value
      this.saveAnswer()
    },

    // 简答题输入处理
    handleEssayInput: performanceOptimizer.debounce(function(e) {
      this.selectedAnswer = e.detail.value
      this.saveAnswer()
    }, 500),

    // 保存答案
    saveAnswer() {
      store.actions.saveAnswer(this.question.id, this.selectedAnswer)

      // 触发答案变化事件
      this.$emit('answer-change', {
        questionId: this.question.id,
        answer: this.selectedAnswer
      })
    },

    // 提交答案
    async handleSubmitAnswer() {
      if (!this.hasAnswer) {
        uni.showToast({
          title: '请先选择答案',
          icon: 'none'
        })
        return
      }

      try {
        const result = await store.actions.submitAnswer({
          questionId: this.question.id,
          userAnswer: this.selectedAnswer,
          mode: this.mode
        })

        this.$emit('answer-submitted', result)

        uni.showToast({
          title: '答案已提交',
          icon: 'success'
        })
      } catch (error) {
        uni.showToast({
          title: error.message || '提交失败',
          icon: 'none'
        })
      }
    },

    // 切换收藏状态
    async handleFavoriteToggle() {
      try {
        // 调用收藏API
        const newStatus = !this.isFavorited
        // await toggleQuestionFavorite(this.question.id, newStatus)

        this.isFavorited = newStatus

        uni.showToast({
          title: newStatus ? '已收藏' : '已取消收藏',
          icon: 'success'
        })

        this.$emit('favorite-change', {
          questionId: this.question.id,
          isFavorited: newStatus
        })
      } catch (error) {
        uni.showToast({
          title: error.message || '操作失败',
          icon: 'none'
        })
      }
    },

    // 上一题
    handlePrevQuestion() {
      if (this.canGoPrev) {
        const currentIndex = store.getState('questions.currentIndex')
        store.actions.setCurrentQuestion(currentIndex - 1)
        this.$emit('question-change', 'prev')
      }
    },

    // 下一题
    handleNextQuestion() {
      if (this.canGoNext) {
        const currentIndex = store.getState('questions.currentIndex')
        store.actions.setCurrentQuestion(currentIndex + 1)
        this.$emit('question-change', 'next')
      }
    },

    // 获取选项标签
    getOptionLabel(index) {
      return String.fromCharCode(65 + index) // A, B, C, D...
    },

    // 获取选项值
    getOptionValue(index) {
      return this.getOptionLabel(index)
    },

    // 获取选项样式类
    getOptionClass(index) {
      const optionValue = this.getOptionValue(index)
      const isSelected = this.isOptionSelected(optionValue)

      return {
        selected: isSelected,
        correct: this.showResult && this.isCorrectOption(index),
        incorrect: this.showResult && isSelected && !this.isCorrectOption(index)
      }
    },

    // 检查选项是否被选中
    isOptionSelected(optionValue) {
      if (this.question.type === 'single' || this.question.type === 'judge') {
        return this.selectedAnswer === optionValue
      } else if (this.question.type === 'multiple') {
        return Array.isArray(this.selectedAnswer) && this.selectedAnswer.includes(optionValue)
      }
      return false
    },

    // 检查是否为正确选项
    isCorrectOption(index) {
      const optionValue = this.getOptionValue(index)

      if (this.question.type === 'single') {
        return this.correctAnswer === optionValue
      } else if (this.question.type === 'multiple') {
        const correctAnswers = Array.isArray(this.correctAnswer)
          ? this.correctAnswer
          : [this.correctAnswer]
        return correctAnswers.includes(optionValue)
      }

      return false
    },

    // 获取状态图标类名
    getStatusIconClass(index) {
      if (!this.showResult) return ''

      const isCorrect = this.isCorrectOption(index)
      const isSelected = this.isOptionSelected(this.getOptionValue(index))

      if (isCorrect) return 'correct'
      if (isSelected && !isCorrect) return 'incorrect'
      return ''
    },

    // 获取状态图标
    getStatusIcon(index) {
      if (!this.showResult) return ''

      const isCorrect = this.isCorrectOption(index)
      const isSelected = this.isOptionSelected(this.getOptionValue(index))

      if (isCorrect) return '✓'
      if (isSelected && !isCorrect) return '✗'
      return ''
    }
  }
}
</script>

<style scoped>
.optimized-question-card {
  background: #ffffff;
  border-radius: 12rpx;
  margin: 20rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24rpx;
}

.question-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.meta-tag {
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  color: #ffffff;
}

.meta-tag.difficulty.level-1 { background: #52c41a; }
.meta-tag.difficulty.level-2 { background: #faad14; }
.meta-tag.difficulty.level-3 { background: #f5222d; }

.meta-tag.type { background: #1890ff; }
.meta-tag.category { background: #722ed1; }

.question-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.score-display {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 26rpx;
  color: #faad14;
}

.favorite-btn {
  padding: 8rpx;
  border-radius: 50%;
  transition: transform 0.2s ease;
}

.favorite-btn:active {
  transform: scale(0.9);
}

.question-content {
  margin-bottom: 32rpx;
}

.question-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #262626;
  margin-bottom: 16rpx;
  line-height: 1.5;
}

.question-text {
  font-size: 30rpx;
  color: #595959;
  line-height: 1.6;
}

.options-container {
  margin-bottom: 32rpx;
}

.option-item {
  display: flex;
  align-items: flex-start;
  padding: 20rpx;
  margin-bottom: 16rpx;
  border: 2rpx solid #f0f0f0;
  border-radius: 12rpx;
  transition: all 0.3s ease;
}

.option-item:active {
  transform: scale(0.98);
}

.option-item.selected {
  border-color: #1890ff;
  background: #f6ffed;
}

.option-item.correct {
  border-color: #52c41a;
  background: #f6ffed;
}

.option-item.incorrect {
  border-color: #f5222d;
  background: #fff2f0;
}

.option-indicator {
  display: flex;
  align-items: center;
  margin-right: 16rpx;
  min-width: 60rpx;
}

.option-label {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: #f0f0f0;
  color: #8c8c8c;
  font-size: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12rpx;
}

.option-item.selected .option-label {
  background: #1890ff;
  color: white;
}

.status-icon {
  font-size: 24rpx;
  font-weight: 600;
}

.status-icon.correct { color: #52c41a; }
.status-icon.incorrect { color: #f5222d; }

.option-content {
  flex: 1;
  font-size: 28rpx;
  color: #262626;
  line-height: 1.5;
}

.judge-options {
  display: flex;
  gap: 24rpx;
  margin-bottom: 32rpx;
}

.judge-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32rpx 20rpx;
  border: 2rpx solid #f0f0f0;
  border-radius: 12rpx;
  transition: all 0.3s ease;
}

.judge-option:active {
  transform: scale(0.98);
}

.judge-option.selected {
  border-color: #1890ff;
  background: #f6ffed;
}

.judge-option.correct {
  border-color: #52c41a;
  background: #f6ffed;
}

.judge-option.incorrect {
  border-color: #f5222d;
  background: #fff2f0;
}

.judge-icon {
  font-size: 48rpx;
  margin-bottom: 12rpx;
}

.judge-text {
  font-size: 28rpx;
  color: #262626;
}

.essay-container {
  margin-bottom: 32rpx;
}

.essay-input {
  width: 100%;
  min-height: 200rpx;
  padding: 20rpx;
  border: 2rpx solid #f0f0f0;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #262626;
  line-height: 1.5;
  box-sizing: border-box;
}

.essay-input:focus {
  border-color: #1890ff;
}

.essay-info {
  display: flex;
  justify-content: flex-end;
  margin-top: 12rpx;
}

.char-count {
  font-size: 24rpx;
  color: #8c8c8c;
}

.answer-analysis {
  margin-top: 32rpx;
  padding: 24rpx;
  background: #fafafa;
  border-radius: 12rpx;
}

.analysis-header {
  margin-bottom: 16rpx;
}

.analysis-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1890ff;
}

.analysis-content {
  font-size: 26rpx;
  color: #595959;
  line-height: 1.6;
}

.question-actions-bottom {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
  margin-top: 32rpx;
}

.action-btn {
  flex: 1;
  padding: 24rpx;
  border-radius: 8rpx;
  font-size: 28rpx;
  border: none;
  transition: all 0.3s ease;
}

.prev-btn, .next-btn {
  background: #f0f0f0;
  color: #262626;
}

.prev-btn:disabled, .next-btn:disabled {
  background: #f5f5f5;
  color: #bfbfbf;
}

.submit-btn {
  background: #1890ff;
  color: white;
}

.submit-btn.disabled {
  background: #d9d9d9;
  color: #bfbfbf;
}

.action-btn:active:not(:disabled) {
  transform: scale(0.98);
}

.exam-mode {
  border-left: 4rpx solid #faad14;
}

.practice-mode {
  border-left: 4rpx solid #52c41a;
}
</style>