<template>
  <view class="config-container">
    <!-- 页面头部 -->
    <view class="header">
      <view class="header-left">
        <text class="header-icon">⚙️</text>
        <text class="header-title">答题配置</text>
      </view>
      <view class="reset-btn" @click="resetConfig">
        <text class="reset-icon">🔄</text>
        <text class="reset-text">重置</text>
      </view>
    </view>

    <!-- 配置选项 -->
    <view class="config-content">
      <!-- 题目分类 - 必须首先选择 -->
      <view class="config-section">
        <view class="section-title">
          题目分类
          <text class="required-mark">*</text>
        </view>
        <view class="category-select-wrapper">
          <picker 
            mode="selector" 
            :value="categoryIndex" 
            :range="categoryOptions" 
            range-key="label"
            @change="onCategoryChange"
          >
            <view class="category-picker">
              <view class="picker-content">
                <view class="picker-icon">{{ selectedCategory.icon }}</view>
                <view class="picker-text">
                  <view class="picker-label">{{ selectedCategory.label }}</view>
                  <view class="picker-count">{{ selectedCategory.count }}题</view>
                </view>
              </view>
              <view class="picker-arrow">▼</view>
            </view>
          </picker>
        </view>
      </view>

      <!-- 题目数量 - 根据分类动态调整 -->
      <view class="config-section" v-if="config.category">
        <view class="section-title">题目数量</view>
        <view class="number-input">
          <view class="number-btn" @click="decreaseCount" :class="{ disabled: config.count <= 5 }">-5</view>
          <input 
            class="number-field" 
            type="number" 
            v-model="config.count"
            @input="onCountChange"
            :max="maxCount"
            :min="5"
          />
          <view class="number-btn" @click="increaseCount" :class="{ disabled: config.count >= maxCount }">+5</view>
          <text class="number-unit">题</text>
        </view>
        <view class="count-info">
          <text class="count-text">最多可选择 {{ maxCount }} 题</text>
        </view>
      </view>

      <!-- 答题模式 -->
      <view class="config-section">
        <view class="section-title">答题模式</view>
        <view class="mode-options">
          <view 
            class="mode-option" 
            :class="{ active: config.mode === 'practice' }"
            @click="setMode('practice')"
          >
            <view class="option-icon">🏆</view>
            <view class="option-content">
              <view class="option-title">练习模式</view>
              <view class="option-desc">可查看答案解析，不限时间</view>
            </view>
          </view>
          <view 
            class="mode-option" 
            :class="{ active: config.mode === 'exam' }"
            @click="setMode('exam')"
          >
            <view class="option-icon">⏰</view>
            <view class="option-content">
              <view class="option-title">考试模式</view>
              <view class="option-desc">限时答题，模拟真实考试</view>
            </view>
          </view>
        </view>
      </view>

      <!-- 练习模式子选项 -->
      <view class="config-section" v-if="config.mode === 'practice'">
        <view class="section-title">练习模式</view>
        <view class="practice-options">
          <view 
            class="practice-option" 
            :class="{ active: config.practiceMode === 'random' }"
            @click="setPracticeMode('random')"
          >
            <view class="option-icon">🔄</view>
            <view class="option-content">
              <view class="option-title">随机练习</view>
              <view class="option-desc">随机获取题目</view>
            </view>
          </view>
          <view 
            class="practice-option" 
            :class="{ active: config.practiceMode === 'sequential' }"
            @click="setPracticeMode('sequential')"
          >
            <view class="option-icon">📜</view>
            <view class="option-content">
              <view class="option-title">顺序练习</view>
              <view class="option-desc">按题目ID顺序获取</view>
            </view>
          </view>
        </view>
      </view>

      <!-- 难度等级 -->
      <view class="config-section">
        <view class="section-title">难度等级</view>
        <view class="difficulty-options">
          <view 
            class="difficulty-option" 
            :class="{ active: config.difficulty === 'random' }"
            @click="setDifficulty('random')"
          >
            <text class="difficulty-icon">🔄</text>
            <text class="difficulty-text">随机难度</text>
          </view>
          <view 
            class="difficulty-option" 
            :class="{ active: config.difficulty === 'easy' }"
            @click="setDifficulty('easy')"
          >
            <text class="difficulty-icon">✔️</text>
            <text class="difficulty-text">简单</text>
          </view>
          <view 
            class="difficulty-option" 
            :class="{ active: config.difficulty === 'medium' }"
            @click="setDifficulty('medium')"
          >
            <text class="difficulty-icon">ⓘ</text>
            <text class="difficulty-text">中等</text>
          </view>
          <view 
            class="difficulty-option" 
            :class="{ active: config.difficulty === 'hard' }"
            @click="setDifficulty('hard')"
          >
            <text class="difficulty-icon">⭐</text>
            <text class="difficulty-text">困难</text>
          </view>
        </view>
      </view>

      <!-- 考试时间设置 -->
      <view class="config-section" v-if="config.mode === 'exam'">
        <view class="section-title">考试时间</view>
        <view class="time-input">
          <input 
            class="time-field" 
            type="number" 
            v-model="config.duration"
            @input="onDurationChange"
          />
          <text class="time-unit">分钟</text>
        </view>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="bottom-actions">
      <button class="start-btn" @click="startQuiz">
        <text class="start-icon">◎</text>
        <text class="start-text">开始答题 ({{ config.count }}题)</text>
      </button>
      <button class="reset-config-btn" @click="resetConfig">
        <text class="reset-icon">🔄</text>
        <text class="reset-text">重置配置</text>
      </button>
    </view>
  </view>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { questionAPI } from '@/utils/api'

export default {
  name: 'ConfigPage',
  setup() {
    const config = reactive({
      mode: 'practice', // practice | exam
      count: 5,
      category: '', // 必须选择分类
      practiceMode: 'random', // random | sequential
      difficulty: 'random', // random | easy | medium | hard
      duration: 60 // 考试时间（分钟）
    })

    // 分类数据
    const categories = ref([
      { value: 'java', label: 'Java基础', icon: '☕', count: 0 },
      { value: 'spring', label: 'Spring框架', icon: '🌱', count: 0 },
      { value: 'database', label: '数据库', icon: '🗄️', count: 0 },
      { value: 'algorithm', label: '算法', icon: '🧮', count: 0 },
      { value: 'frontend', label: '前端', icon: '💻', count: 0 },
      { value: 'all', label: '全部分类', icon: '▦', count: 0 }
    ])

    // 分类选项（用于下拉框）
    const categoryOptions = ref([])
    
    // 当前选中的分类索引
    const categoryIndex = ref(0)
    
    // 当前选中的分类
    const selectedCategory = ref({ value: '', label: '请选择分类', icon: '📝', count: 0 })

    // 最大题目数量
    const maxCount = ref(5)

    // 获取分类数据
    const loadCategories = async () => {
      try {
        const response = await questionAPI.getCategories()
        if (response && response.length > 0) {
          // 更新分类数据，保持原有的图标和标签
          const categoryMap = {
            'java': { label: 'Java基础', icon: '☕' },
            'spring': { label: 'Spring框架', icon: '🌱' },
            'database': { label: '数据库', icon: '🗄️' },
            'algorithm': { label: '算法', icon: '🧮' },
            'frontend': { label: '前端', icon: '💻' },
            '2024中级粮农食品安全评价': { label: '2024中级粮农食品安全评价', icon: '🌾' },
            'all': { label: '全部分类', icon: '▦' }
          }
          
          const newCategories = response.map(item => ({
            value: item.category,
            label: categoryMap[item.category]?.label || item.category,
            icon: categoryMap[item.category]?.icon || '📝',
            count: item.count || 0
          }))
          
          categories.value = newCategories
          categoryOptions.value = newCategories
          
          // 设置默认选中第一个分类
          if (newCategories.length > 0) {
            selectedCategory.value = newCategories[0]
            config.category = newCategories[0].value
            maxCount.value = newCategories[0].count || 5
            config.count = Math.max(5, Math.floor((newCategories[0].count || 5) / 5) * 5)
          }
        }
      } catch (error) {
        console.error('获取分类失败:', error)
        uni.showToast({
          title: '获取分类失败',
          icon: 'none'
        })
      }
    }

    // 页面加载时获取URL参数（可选）
    onMounted(() => {
      const pages = getCurrentPages()
      const currentPage = pages[pages.length - 1]
      const options = currentPage.options
      
      // 如果有URL参数，则设置对应模式，否则保持默认
      if (options.mode) {
        config.mode = options.mode
      }
      
      // 加载分类数据
      loadCategories()
    })

    // 设置答题模式
    const setMode = (mode) => {
      config.mode = mode
    }

    // 分类选择变化
    const onCategoryChange = (e) => {
      const index = e.detail.value
      categoryIndex.value = index
      const category = categoryOptions.value[index]
      
      if (category) {
        selectedCategory.value = category
        config.category = category.value
        maxCount.value = category.count || 5
        
        // 如果当前数量超过最大值，调整为最大值
        if (config.count > maxCount.value) {
          config.count = maxCount.value
        }
        // 确保数量是5的倍数
        config.count = Math.max(5, Math.floor(config.count / 5) * 5)
      }
    }

    // 设置分类（保留用于兼容）
    const setCategory = (category) => {
      config.category = category
      // 根据分类更新最大题目数量
      const selectedCategory = categories.value.find(cat => cat.value === category)
      if (selectedCategory) {
        maxCount.value = selectedCategory.count || 5
        // 如果当前数量超过最大值，调整为最大值
        if (config.count > maxCount.value) {
          config.count = maxCount.value
        }
        // 确保数量是5的倍数
        config.count = Math.max(5, Math.floor(config.count / 5) * 5)
      }
    }

    // 设置练习模式
    const setPracticeMode = (practiceMode) => {
      config.practiceMode = practiceMode
    }

    // 设置难度
    const setDifficulty = (difficulty) => {
      config.difficulty = difficulty
    }

    // 增加题目数量（每次+5）
    const increaseCount = () => {
      if (config.count < maxCount.value) {
        const newCount = Math.min(config.count + 5, maxCount.value)
        config.count = Math.floor(newCount / 5) * 5 // 确保是5的倍数
      }
    }

    // 减少题目数量（每次-5）
    const decreaseCount = () => {
      if (config.count > 5) {
        config.count = Math.max(5, config.count - 5)
      }
    }

    // 题目数量变化
    const onCountChange = (e) => {
      const value = parseInt(e.detail.value)
      if (value && value >= 5 && value <= maxCount.value) {
        // 确保是5的倍数
        config.count = Math.floor(value / 5) * 5
      }
    }

    // 考试时间变化
    const onDurationChange = (e) => {
      const value = parseInt(e.detail.value)
      if (value && value > 0) {
        config.duration = value
      }
    }

    // 重置配置
    const resetConfig = () => {
      Object.assign(config, {
        mode: 'practice',
        count: 5,
        category: '',
        practiceMode: 'random',
        difficulty: 'random',
        duration: 60
      })
      maxCount.value = 5
      uni.showToast({
        title: '配置已重置',
        icon: 'success'
      })
    }

    // 开始答题
    const startQuiz = () => {
      // 检查是否选择了分类
      if (!config.category) {
        uni.showToast({
          title: '请先选择题目分类',
          icon: 'none'
        })
        return
      }

      // 构建跳转参数
      const params = {
        mode: config.mode,
        count: config.count,
        category: config.category,
        difficulty: config.difficulty
      }

      if (config.mode === 'practice') {
        params.practiceMode = config.practiceMode
        uni.navigateTo({
          url: `/pages/practice/practice?${Object.keys(params).map(key => `${key}=${params[key]}`).join('&')}`
        })
      } else if (config.mode === 'exam') {
        params.duration = config.duration
        uni.navigateTo({
          url: `/pages/exam/exam?${Object.keys(params).map(key => `${key}=${params[key]}`).join('&')}`
        })
      }
    }

    return {
      config,
      categories,
      categoryOptions,
      categoryIndex,
      selectedCategory,
      maxCount,
      setMode,
      setCategory,
      onCategoryChange,
      setPracticeMode,
      setDifficulty,
      increaseCount,
      decreaseCount,
      onCountChange,
      onDurationChange,
      resetConfig,
      startQuiz
    }
  }
}
</script>

<style scoped>
.config-container {
  min-height: 100vh;
  background: #f8f9fa;
  padding: 20rpx;
  padding-bottom: 120rpx;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30rpx;
  padding: 0 10rpx;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.header-icon {
  font-size: 36rpx;
}

.header-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #2c3e50;
}

.reset-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx 24rpx;
  background: #f8f9fa;
  border-radius: 20rpx;
  border: 1rpx solid #e9ecef;
}

.reset-icon {
  font-size: 28rpx;
}

.reset-text {
  font-size: 28rpx;
  color: #6c757d;
}

.config-content {
  background: white;
  border-radius: 24rpx;
  padding: 40rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
}

.config-section {
  margin-bottom: 40rpx;
}

.config-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
}

.required-mark {
  color: #ff4757;
  margin-left: 8rpx;
}

.mode-options {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.mode-option {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background: #f8f9fa;
  border-radius: 16rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s ease;
}

.mode-option.active {
  background: #667eea;
  border-color: #667eea;
  color: white;
}

.option-icon {
  font-size: 40rpx;
  margin-right: 20rpx;
}

.option-content {
  flex: 1;
}

.option-title {
  font-size: 32rpx;
  font-weight: 500;
  margin-bottom: 8rpx;
}

.option-desc {
  font-size: 26rpx;
  opacity: 0.8;
}

.number-input {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.number-btn {
  width: 60rpx;
  height: 60rpx;
  background: #667eea;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 600;
  transition: all 0.3s ease;
}

.number-btn.disabled {
  background: #e9ecef;
  color: #6c757d;
}

.number-field {
  flex: 1;
  height: 80rpx;
  background: #f8f9fa;
  border-radius: 16rpx;
  border: 2rpx solid #e9ecef;
  text-align: center;
  font-size: 32rpx;
  font-weight: 600;
  color: #2c3e50;
}

.number-unit {
  font-size: 28rpx;
  color: #6c757d;
}

.count-info {
  margin-top: 16rpx;
  text-align: center;
}

.count-text {
  font-size: 24rpx;
  color: #6c757d;
}

/* 分类选择器样式 */
.category-select-wrapper {
  margin-top: 8rpx;
}

.category-picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
  background: #f8f9fa;
  border: 2rpx solid #e9ecef;
  border-radius: 16rpx;
  transition: all 0.3s ease;
}

.category-picker:active {
  background: #e9ecef;
  border-color: #667eea;
}

.picker-content {
  display: flex;
  align-items: center;
  flex: 1;
}

.picker-icon {
  font-size: 32rpx;
  margin-right: 16rpx;
}

.picker-text {
  flex: 1;
}

.picker-label {
  font-size: 28rpx;
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 4rpx;
}

.picker-count {
  font-size: 24rpx;
  color: #6c757d;
}

.picker-arrow {
  font-size: 24rpx;
  color: #6c757d;
  margin-left: 16rpx;
}

.practice-options {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.practice-option {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background: #f8f9fa;
  border-radius: 16rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s ease;
}

.practice-option.active {
  background: #667eea;
  border-color: #667eea;
  color: white;
}

.difficulty-options {
  display: flex;
  gap: 16rpx;
}

.difficulty-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 16rpx;
  background: #f8f9fa;
  border-radius: 16rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s ease;
}

.difficulty-option.active {
  background: #667eea;
  border-color: #667eea;
  color: white;
}

.difficulty-icon {
  font-size: 32rpx;
  margin-bottom: 12rpx;
}

.difficulty-text {
  font-size: 26rpx;
  font-weight: 500;
}

.time-input {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.time-field {
  flex: 1;
  height: 80rpx;
  background: #f8f9fa;
  border-radius: 16rpx;
  border: 2rpx solid #e9ecef;
  text-align: center;
  font-size: 32rpx;
  font-weight: 600;
  color: #2c3e50;
}

.time-unit {
  font-size: 28rpx;
  color: #6c757d;
}

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 20rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.08);
  display: flex;
  gap: 20rpx;
}

.start-btn {
  flex: 2;
  height: 100rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 16rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  font-size: 32rpx;
  font-weight: 600;
}

.reset-config-btn {
  flex: 1;
  height: 100rpx;
  background: #f8f9fa;
  color: #6c757d;
  border-radius: 16rpx;
  border: 2rpx solid #e9ecef;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  font-size: 28rpx;
}

.start-icon {
  font-size: 36rpx;
}

.reset-icon {
  font-size: 28rpx;
}
</style>
