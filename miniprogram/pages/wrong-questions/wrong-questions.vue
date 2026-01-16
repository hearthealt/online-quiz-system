<template>
  <view class="wrong-questions-page">
    <!-- 统计卡片 -->
    <view class="stats-card">
      <view class="stat-item">
        <text class="stat-value">{{ overallStats.total }}</text>
        <text class="stat-label">总错题</text>
      </view>
      <view class="stat-item">
        <text class="stat-value error">{{ overallStats.unmastered }}</text>
        <text class="stat-label">未掌握</text>
      </view>
      <view class="stat-item">
        <text class="stat-value success">{{ overallStats.mastered }}</text>
        <text class="stat-label">已掌握</text>
      </view>
      <view class="stat-item">
        <text class="stat-value primary">{{ overallStats.accuracy }}%</text>
        <text class="stat-label">掌握率</text>
      </view>
    </view>

    <!-- 筛选和操作区域 -->
    <view class="filter-section" v-if="bankStats.length > 0">
      <picker
        mode="selector"
        :range="bankOptions"
        :value="selectedBankIndex"
        @change="handleBankChange"
      >
        <view class="bank-picker">
          <text class="picker-label">
            {{ selectedBankIndex >= 0 ? bankOptions[selectedBankIndex] : '选择题库' }}
          </text>
          <text class="picker-arrow">▼</text>
        </view>
      </picker>
      <button
        class="practice-btn"
        :disabled="selectedBankIndex < 0 || currentBankUnmastered === 0"
        @click="practiceWrongQuestions"
      >
        错题练习
      </button>
    </view>

    <!-- 筛选标签 -->
    <view class="filter-tabs">
      <view
        class="filter-tab"
        :class="{ active: filterStatus === null }"
        @click="changeFilter(null)"
      >
        全部
      </view>
      <view
        class="filter-tab"
        :class="{ active: filterStatus === 0 }"
        @click="changeFilter(0)"
      >
        未掌握
      </view>
      <view
        class="filter-tab"
        :class="{ active: filterStatus === 1 }"
        @click="changeFilter(1)"
      >
        已掌握
      </view>
    </view>

    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 错题列表 -->
    <view class="questions-list" v-else-if="wrongQuestions.length > 0">
      <view
        v-for="item in wrongQuestions"
        :key="item.id"
        class="question-item"
        @click="showDetail(item)"
      >
        <view class="question-main">
          <view class="question-tags">
            <text class="tag type" :class="item.question?.type">
              {{ getTypeLabel(item.question?.type) }}
            </text>
            <text class="tag status" :class="item.status === 1 ? 'mastered' : 'unmastered'">
              {{ item.status === 1 ? '已掌握' : '未掌握' }}
            </text>
            <text class="meta-text" v-if="item.bankName">{{ item.bankName }}</text>
          </view>
          <view class="question-content">
            {{ truncateText(item.question?.content, 80) }}
          </view>
          <view class="question-footer">
            <text class="error-count">错误 {{ item.errorCount || 1 }} 次</text>
            <button
              v-if="item.status === 0"
              class="action-btn success"
              @click.stop="markAsMastered(item)"
            >
              标记掌握
            </button>
            <button
              v-else
              class="action-btn default"
              @click.stop="markAsUnmastered(item)"
            >
              重新学习
            </button>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-container" v-else>
      <text class="empty-title">暂无错题</text>
      <text class="empty-desc">恭喜你，目前没有错题需要复习</text>
      <button class="primary-btn" @click="goHome">去答题</button>
    </view>

    <!-- 详情弹窗 -->
    <view class="detail-modal" v-if="showDetailModal" @click="closeDetail">
      <view class="detail-content" @click.stop>
        <view class="detail-header">
          <text class="detail-title">题目详情</text>
          <text class="close-btn" @click="closeDetail">×</text>
        </view>

        <scroll-view class="detail-body" scroll-y>
          <view class="detail-section" v-if="currentItem">
            <view class="section-tags">
              <text class="tag type" :class="currentItem.question?.type">
                {{ getTypeLabel(currentItem.question?.type) }}
              </text>
              <text class="tag status" :class="currentItem.status === 1 ? 'mastered' : 'unmastered'">
                {{ currentItem.status === 1 ? '已掌握' : '未掌握' }}
              </text>
              <text class="error-info">累计错误 {{ currentItem.errorCount || 1 }} 次</text>
            </view>
          </view>

          <view class="detail-section">
            <text class="section-label">题目内容</text>
            <text class="section-text">{{ currentItem?.question?.content }}</text>
          </view>

          <view class="detail-section" v-if="currentItem?.question?.options?.length">
            <text class="section-label">选项</text>
            <view class="options-list">
              <view
                v-for="(option, index) in currentItem.question.options"
                :key="index"
                class="option-item"
                :class="{ correct: isCorrectOption(index) }"
              >
                <text class="option-label">{{ getOptionLabel(index) }}</text>
                <text class="option-text">{{ option }}</text>
              </view>
            </view>
          </view>

          <view class="detail-section">
            <text class="section-label">正确答案</text>
            <text class="answer-text correct">{{ formatCorrectAnswer(currentItem?.question) }}</text>
          </view>

          <view class="detail-section" v-if="currentItem?.lastErrorAnswer">
            <text class="section-label">你的答案</text>
            <text class="answer-text wrong">{{ formatUserAnswer(currentItem) }}</text>
          </view>

          <view class="detail-section" v-if="currentItem?.question?.analysis">
            <text class="section-label">答案解析</text>
            <text class="analysis-text">{{ currentItem.question.analysis }}</text>
          </view>
        </scroll-view>

        <view class="detail-footer">
          <button class="footer-btn default" @click="closeDetail">关闭</button>
          <button
            v-if="currentItem?.status === 0"
            class="footer-btn primary"
            @click="markAsMasteredAndClose"
          >
            标记为已掌握
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { wrongQuestionAPI } from '@/utils/api'

export default {
  name: 'WrongQuestionsPage',
  setup() {
    const loading = ref(false)
    const wrongQuestions = ref([])
    const filterStatus = ref(null)

    // 统计数据
    const overallStats = ref({ total: 0, mastered: 0, unmastered: 0, accuracy: 0 })

    // 题库统计
    const bankStats = ref([])
    const selectedBankIndex = ref(-1)

    // 计算题库选项列表
    const bankOptions = computed(() => {
      return bankStats.value.map(b => `${b.bankName} (${b.unmasteredCount}题未掌握)`)
    })

    // 当前选中题库的未掌握数
    const currentBankUnmastered = computed(() => {
      if (selectedBankIndex.value < 0) return 0
      const bank = bankStats.value[selectedBankIndex.value]
      return bank?.unmasteredCount || 0
    })

    // 详情弹窗
    const showDetailModal = ref(false)
    const currentItem = ref(null)

    // 获取总体统计
    const fetchOverallStats = async () => {
      try {
        overallStats.value = await wrongQuestionAPI.getOverallStats()
      } catch (error) {
        console.error('获取统计失败:', error)
      }
    }

    // 获取题库统计
    const fetchBankStats = async () => {
      try {
        const stats = await wrongQuestionAPI.getStatsByBank()
        bankStats.value = stats || []
      } catch (error) {
        console.error('获取题库统计失败:', error)
      }
    }

    // 获取错题列表
    const fetchWrongQuestions = async () => {
      try {
        loading.value = true
        const params = {
          current: 1,
          size: 100
        }
        if (filterStatus.value !== null) {
          params.status = filterStatus.value
        }
        const result = await wrongQuestionAPI.getWrongQuestions(params)
        wrongQuestions.value = result.records || result || []
      } catch (error) {
        console.error('获取错题列表失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      } finally {
        loading.value = false
      }
    }

    // 题库选择变化
    const handleBankChange = (e) => {
      selectedBankIndex.value = parseInt(e.detail.value)
    }

    // 练习错题
    const practiceWrongQuestions = () => {
      if (selectedBankIndex.value < 0) {
        uni.showToast({ title: '请先选择题库', icon: 'none' })
        return
      }
      const bank = bankStats.value[selectedBankIndex.value]
      if (bank) {
        uni.navigateTo({
          url: `/pages/practice/practice?bankId=${bank.bankId}&source=wrong`
        })
      }
    }

    // 切换筛选
    const changeFilter = (status) => {
      filterStatus.value = status
      fetchWrongQuestions()
    }

    // 显示详情
    const showDetail = (item) => {
      currentItem.value = item
      showDetailModal.value = true
    }

    // 关闭详情
    const closeDetail = () => {
      showDetailModal.value = false
      currentItem.value = null
    }

    // 标记为已掌握
    const markAsMastered = async (item) => {
      try {
        await wrongQuestionAPI.markAsMastered(item.questionId)
        item.status = 1
        await fetchOverallStats()
        await fetchBankStats()
        uni.showToast({ title: '已标记为掌握', icon: 'success' })
      } catch (error) {
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    }

    // 标记为未掌握
    const markAsUnmastered = async (item) => {
      try {
        await wrongQuestionAPI.markAsUnmastered(item.questionId)
        item.status = 0
        await fetchOverallStats()
        await fetchBankStats()
        uni.showToast({ title: '已标记为未掌握', icon: 'success' })
      } catch (error) {
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    }

    // 标记并关闭
    const markAsMasteredAndClose = async () => {
      if (currentItem.value) {
        await markAsMastered(currentItem.value)
        closeDetail()
      }
    }

    // 判断是否为正确选项
    const isCorrectOption = (index) => {
      if (!currentItem.value?.question) return false
      const correctAnswer = currentItem.value.question.correctAnswer || ''
      const optionLabel = getOptionLabel(index)
      return correctAnswer.split(',').includes(optionLabel)
    }

    // 格式化正确答案
    const formatCorrectAnswer = (question) => {
      if (!question || !question.correctAnswer) return ''
      if (question.type === 'judge') {
        return question.correctAnswer === 'true' ? '正确' : '错误'
      }
      if (question.type === 'multiple') {
        return question.correctAnswer.split(',').join('、')
      }
      return question.correctAnswer
    }

    // 格式化用户答案
    const formatUserAnswer = (item) => {
      if (!item?.lastErrorAnswer) return ''
      if (item.question?.type === 'judge') {
        return item.lastErrorAnswer === 'true' ? '正确' : '错误'
      }
      return item.lastErrorAnswer
    }

    // 截断文本
    const truncateText = (text, max = 80) => {
      if (!text) return ''
      return text.length > max ? text.substring(0, max) + '...' : text
    }

    // 获取题型标签
    const getTypeLabel = (type) => {
      const labels = { single: '单选', multiple: '多选', judge: '判断', essay: '简答' }
      return labels[type] || type
    }

    // 获取选项标签
    const getOptionLabel = (index) => String.fromCharCode(65 + index)

    // 返回首页
    const goHome = () => {
      uni.switchTab({ url: '/pages/index/index' })
    }

    // 页面加载
    onMounted(() => {
      fetchOverallStats()
      fetchBankStats()
      fetchWrongQuestions()
    })

    // 页面显示时刷新
    onShow(() => {
      fetchOverallStats()
      fetchBankStats()
      fetchWrongQuestions()
    })

    return {
      loading,
      wrongQuestions,
      filterStatus,
      overallStats,
      bankStats,
      selectedBankIndex,
      bankOptions,
      currentBankUnmastered,
      showDetailModal,
      currentItem,
      changeFilter,
      handleBankChange,
      practiceWrongQuestions,
      showDetail,
      closeDetail,
      markAsMastered,
      markAsUnmastered,
      markAsMasteredAndClose,
      isCorrectOption,
      formatCorrectAnswer,
      formatUserAnswer,
      truncateText,
      getTypeLabel,
      getOptionLabel,
      goHome
    }
  }
}
</script>

<style scoped>
.wrong-questions-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20rpx;
  padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
}

/* 统计卡片 */
.stats-card {
  padding: 24rpx 16rpx;
  background: #fff;
  border-radius: 12rpx;
  display: flex;
  justify-content: space-around;
  margin-bottom: 16rpx;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 36rpx;
  font-weight: 600;
  color: #303133;
  display: block;
  margin-bottom: 4rpx;
}

.stat-value.error { color: #f56c6c; }
.stat-value.success { color: #67c23a; }
.stat-value.primary { color: #409eff; }

.stat-label {
  font-size: 22rpx;
  color: #909399;
}

/* 筛选区域 */
.filter-section {
  padding: 16rpx;
  background: #fff;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.bank-picker {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 20rpx;
  background: #f5f7fa;
  border-radius: 8rpx;
}

.picker-label {
  font-size: 28rpx;
  color: #303133;
}

.picker-arrow {
  font-size: 20rpx;
  color: #909399;
}

.practice-btn {
  padding: 16rpx 32rpx;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  font-size: 28rpx;
  font-weight: 500;
  flex-shrink: 0;
  line-height: 1.2;
}

.practice-btn[disabled] {
  opacity: 0.5;
  background: #c0c4cc;
}

/* 筛选标签 */
.filter-tabs {
  display: flex;
  background: #fff;
  border-radius: 8rpx;
  padding: 6rpx;
  margin-bottom: 16rpx;
}

.filter-tab {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  font-size: 28rpx;
  color: #606266;
  border-radius: 6rpx;
  transition: all 0.2s;
}

.filter-tab.active {
  background: #409eff;
  color: #fff;
  font-weight: 500;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
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

/* 错题列表 */
.questions-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.question-item {
  background: #fff;
  border-radius: 12rpx;
  padding: 20rpx;
}

.question-main {
  width: 100%;
}

.question-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.tag {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
}

.tag.type.single { background: #ecf5ff; color: #409eff; }
.tag.type.multiple { background: #f0f9eb; color: #67c23a; }
.tag.type.judge { background: #fdf6ec; color: #e6a23c; }
.tag.type.essay { background: #f4f4f5; color: #909399; }

.tag.status.mastered { background: #f0f9eb; color: #67c23a; }
.tag.status.unmastered { background: #fef0f0; color: #f56c6c; }

.meta-text {
  font-size: 22rpx;
  color: #909399;
}

.question-content {
  font-size: 28rpx;
  color: #303133;
  line-height: 1.6;
  margin-bottom: 12rpx;
}

.question-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.error-count {
  font-size: 24rpx;
  color: #909399;
}

.action-btn {
  font-size: 24rpx;
  padding: 10rpx 20rpx;
  border-radius: 6rpx;
  border: none;
  line-height: 1;
}

.action-btn.success {
  background: #f0f9eb;
  color: #67c23a;
}

.action-btn.default {
  background: #f5f7fa;
  color: #606266;
}

/* 空状态 */
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 40rpx;
}

.empty-title {
  font-size: 32rpx;
  font-weight: 500;
  color: #303133;
  margin-bottom: 12rpx;
}

.empty-desc {
  font-size: 28rpx;
  color: #909399;
  margin-bottom: 40rpx;
}

.primary-btn {
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  padding: 20rpx 48rpx;
  font-size: 28rpx;
}

/* 详情弹窗 */
.detail-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  z-index: 1000;
}

.detail-content {
  background: #fff;
  width: 100%;
  max-height: 85vh;
  border-radius: 24rpx 24rpx 0 0;
  display: flex;
  flex-direction: column;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx 32rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.detail-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #303133;
}

.close-btn {
  font-size: 48rpx;
  color: #909399;
  line-height: 1;
}

.detail-body {
  flex: 1;
  padding: 24rpx 32rpx;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 24rpx;
}

.section-tags {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-wrap: wrap;
}

.error-info {
  font-size: 24rpx;
  color: #909399;
  margin-left: auto;
}

.section-label {
  font-size: 26rpx;
  color: #909399;
  margin-bottom: 12rpx;
  display: block;
}

.section-text {
  font-size: 30rpx;
  color: #303133;
  line-height: 1.7;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #f5f7fa;
  border-radius: 8rpx;
}

.option-item.correct {
  background: #f0f9eb;
}

.option-label {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 600;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.option-item.correct .option-label {
  background: #67c23a;
  color: #fff;
}

.option-text {
  flex: 1;
  font-size: 28rpx;
  color: #303133;
}

.answer-text {
  font-size: 30rpx;
  font-weight: 600;
}

.answer-text.correct { color: #67c23a; }
.answer-text.wrong { color: #f56c6c; }

.analysis-text {
  font-size: 28rpx;
  color: #606266;
  line-height: 1.7;
  padding: 20rpx;
  background: #f5f7fa;
  border-radius: 8rpx;
}

.detail-footer {
  display: flex;
  gap: 16rpx;
  padding: 20rpx 32rpx;
  border-top: 1rpx solid #f0f0f0;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}

.footer-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 8rpx;
  border: none;
  font-size: 28rpx;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
}

.footer-btn.default {
  background: #f5f7fa;
  color: #606266;
}

.footer-btn.primary {
  background: #409eff;
  color: #fff;
}
</style>
