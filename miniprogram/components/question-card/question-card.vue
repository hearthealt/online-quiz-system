<template>
  <view class="question-card" :class="cardClass">
    <!-- 题目头部 -->
    <view class="question-header" v-if="showHeader">
      <view class="question-meta">
        <view class="meta-tag difficulty" :class="'level-' + (question.difficulty || 1)">
          {{ getDifficultyLabel(question.difficulty || 1) }}
        </view>
        <view class="meta-tag type">{{ getTypeLabel(question.type) }}</view>
        <view class="meta-tag category" v-if="question.category">{{ question.category }}</view>
      </view>
        <view class="question-score">
          <view class="score-info" v-if="question.score">
            <text class="score-icon">⭐</text>
            <text class="score-text">{{ question.score }}分</text>
          </view>
          <view class="favorite-btn" @click="toggleFavorite" v-if="showFavorite">
            <text class="favorite-icon" :class="{ active: isFavorited }">
              {{ isFavorited ? '❤️' : '🤍' }}
            </text>
          </view>
        </view>
    </view>
    
    <!-- 题目内容 -->
    <view class="question-content">
      <view class="question-title" v-if="question.title">{{ question.title }}</view>
      <view class="question-text" v-if="question.content">
        {{ question.content }}
      </view>
    </view>
    
    <!-- 选项区域 -->
    <view class="options-section" v-if="question.type !== 'essay' && question.options">
      <view 
        v-for="(option, index) in getOptions()" 
        :key="index"
        class="option-item"
        :class="getOptionClass(index)"
        @click="handleOptionClick(index)"
      >
        <view class="option-label">{{ getOptionLabel(index) }}</view>
        <view class="option-content">{{ option }}</view>
        <view class="option-status" v-if="showAnswer">
          <text v-if="isCorrectOption(index)" class="status-icon correct">✓</text>
          <text v-else-if="isSelectedButIncorrect(index)" class="status-icon incorrect">✗</text>
        </view>
      </view>
    </view>
    
    <!-- 简答题输入 -->
    <view class="essay-section" v-if="question.type === 'essay'">
      <textarea 
        v-model="localAnswer"
        class="essay-input"
        placeholder="请输入您的答案..."
        maxlength="1000"
        :show-confirm-bar="false"
        @input="handleAnswerChange"
      />
      <view class="char-count">{{ localAnswer.length }}/1000</view>
    </view>
    
    <!-- 答案分析 -->
    <view class="analysis-section" v-if="showAnswer && question.analysis">
      <view class="analysis-title">答案解析</view>
      <view class="analysis-content">{{ question.analysis }}</view>
    </view>
  </view>
</template>

<script>
import { ref, computed } from 'vue'

export default {
  name: 'QuestionCard',
  props: {
    question: {
      type: Object,
      required: true
    },
    userAnswer: {
      type: [String, Number, Array],
      default: null
    },
    showAnswer: {
      type: Boolean,
      default: false
    },
    showHeader: {
      type: Boolean,
      default: true
    },
    showFavorite: {
      type: Boolean,
      default: true
    },
    isFavorited: {
      type: Boolean,
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  emits: ['answer-change', 'option-click', 'favorite-toggle'],
  setup(props, { emit }) {
    const localAnswer = ref('')
    
    // 计算属性
    const cardClass = computed(() => ({
      'disabled': props.disabled,
      'show-answer': props.showAnswer
    }))
    
    // 监听用户答案变化
    watch(() => props.userAnswer, (newAnswer) => {
      if (props.question.type === 'essay') {
        localAnswer.value = newAnswer || ''
      }
    }, { immediate: true })
    
    // 获取选项
    const getOptions = () => {
      if (!props.question?.options) return []
      
      try {
        const options = props.question.options
        if (Array.isArray(options)) {
          return options
        }
        if (typeof options === 'string') {
          return JSON.parse(options)
        }
        return []
      } catch (error) {
        console.error('解析选项失败:', error)
        return []
      }
    }
    
    // 获取选项标签
    const getOptionLabel = (index) => {
      return String.fromCharCode(65 + index)
    }
    
    // 获取选项样式类
    const getOptionClass = (index) => {
      const classes = []
      
      if (isOptionSelected(index)) {
        classes.push('selected')
      }
      
      if (props.showAnswer) {
        if (isCorrectOption(index)) {
          classes.push('correct')
        } else if (isSelectedButIncorrect(index)) {
          classes.push('incorrect')
        }
      }
      
      return classes
    }
    
    // 检查选项是否被选中
    const isOptionSelected = (index) => {
      if (!props.question?.id) return false
      
      if (props.question.type === 'multiple') {
        return Array.isArray(props.userAnswer) && props.userAnswer.includes(index)
      }
      return props.userAnswer === index
    }
    
    // 检查是否为正确答案
    const isCorrectOption = (index) => {
      if (!props.question?.correctAnswer) return false
      
      try {
        const correctAnswer = props.question.correctAnswer
        
        if (Array.isArray(correctAnswer)) {
          return correctAnswer.includes(index)
        }
        
        if (typeof correctAnswer === 'string') {
          try {
            const parsed = JSON.parse(correctAnswer)
            if (Array.isArray(parsed)) {
              return parsed.includes(index)
            }
          } catch {
            const letterIndex = String.fromCharCode(65 + index)
            return correctAnswer.includes(letterIndex)
          }
        }
        
        return false
      } catch {
        return false
      }
    }
    
    // 检查是否选中但错误
    const isSelectedButIncorrect = (index) => {
      return isOptionSelected(index) && !isCorrectOption(index)
    }
    
    // 处理选项点击
    const handleOptionClick = (index) => {
      if (props.disabled) return
      
      emit('option-click', index)
    }
    
    // 处理答案变化
    const handleAnswerChange = (e) => {
      emit('answer-change', e.detail.value)
    }
    
    // 切换收藏状态
    const toggleFavorite = () => {
      if (props.disabled) return
      emit('favorite-toggle', props.question.id)
    }
    
    // 工具函数
    const getTypeLabel = (type) => {
      const labels = {
        single: '单选题',
        multiple: '多选题',
        judge: '判断题',
        essay: '简答题'
      }
      return labels[type] || type
    }
    
    const getDifficultyLabel = (difficulty) => {
      const labels = {
        1: '简单',
        2: '中等',
        3: '困难'
      }
      return labels[difficulty] || '未知'
    }
    
    return {
      localAnswer,
      cardClass,
      getOptions,
      getOptionLabel,
      getOptionClass,
      isOptionSelected,
      isCorrectOption,
      isSelectedButIncorrect,
      handleOptionClick,
      handleAnswerChange,
      toggleFavorite,
      getTypeLabel,
      getDifficultyLabel
    }
  }
}
</script>

<style scoped>
.question-card {
  background: white;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.question-card.disabled {
  opacity: 0.6;
  pointer-events: none;
}

.question-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
}

.question-meta {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}

.meta-tag {
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: 500;
  color: white;
}

.difficulty.level-1 {
  background: rgba(40, 167, 69, 0.8);
}

.difficulty.level-2 {
  background: rgba(255, 193, 7, 0.8);
}

.difficulty.level-3 {
  background: rgba(220, 53, 69, 0.8);
}

.type {
  background: rgba(0, 123, 255, 0.8);
}

.category {
  background: rgba(108, 117, 125, 0.8);
}

.question-score {
  display: flex;
  align-items: center;
  gap: 16rpx;
  font-weight: 600;
}

.score-info {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.score-icon {
  font-size: 28rpx;
}

.score-text {
  font-size: 28rpx;
}

.favorite-btn {
  padding: 8rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.favorite-btn:active {
  transform: scale(0.9);
}

.favorite-icon {
  font-size: 32rpx;
  transition: all 0.3s ease;
}

.favorite-icon.active {
  animation: heartBeat 0.6s ease-in-out;
}

@keyframes heartBeat {
  0% { transform: scale(1); }
  50% { transform: scale(1.2); }
  100% { transform: scale(1); }
}

.question-content {
  padding: 40rpx 30rpx;
}

.question-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 20rpx;
  line-height: 1.6;
}

.question-text {
  font-size: 32rpx;
  color: #5a6c7d;
  line-height: 1.8;
}

.options-section {
  padding: 0 30rpx 30rpx;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  margin-bottom: 16rpx;
  border: 2rpx solid #e9ecef;
  border-radius: 12rpx;
  transition: all 0.3s ease;
  background: white;
  cursor: pointer;
}

.option-item:active {
  transform: translateY(2rpx);
}

.option-item.selected {
  border-color: #667eea;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.option-item.correct {
  border-color: #28a745;
  background: #d4edda;
  color: #155724;
}

.option-item.incorrect {
  border-color: #dc3545;
  background: #f8d7da;
  color: #721c24;
}

.option-label {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  margin-right: 20rpx;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.option-item.selected .option-label {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.option-content {
  flex: 1;
  font-size: 30rpx;
  line-height: 1.5;
}

.option-status {
  margin-left: 16rpx;
}

.status-icon {
  font-size: 32rpx;
  font-weight: bold;
}

.status-icon.correct {
  color: #28a745;
}

.status-icon.incorrect {
  color: #dc3545;
}

.essay-section {
  padding: 0 30rpx 30rpx;
}

.essay-input {
  width: 100%;
  min-height: 200rpx;
  padding: 20rpx;
  border: 2rpx solid #e9ecef;
  border-radius: 12rpx;
  font-size: 30rpx;
  line-height: 1.6;
  background: #f8f9fa;
}

.char-count {
  text-align: right;
  font-size: 24rpx;
  color: #6c757d;
  margin-top: 8rpx;
}

.analysis-section {
  margin: 30rpx;
  padding: 30rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  border-left: 6rpx solid #667eea;
}

.analysis-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 16rpx;
}

.analysis-content {
  font-size: 30rpx;
  color: #5a6c7d;
  line-height: 1.6;
}
</style>
