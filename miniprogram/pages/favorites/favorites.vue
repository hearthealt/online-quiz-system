<template>
  <view class="favorites-page">
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
        :disabled="selectedBankIndex < 0"
        @click="practiceFavorites"
      >
        开始练习
      </button>
    </view>

    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 收藏列表 -->
    <view class="favorites-list" v-else-if="favorites.length > 0">
      <view
        v-for="item in favorites"
        :key="item.id"
        class="favorite-item"
        @click="showDetail(item)"
      >
        <view class="item-main">
          <view class="item-tags">
            <text class="tag type" :class="item.question?.type">
              {{ getTypeLabel(item.question?.type) }}
            </text>
            <text class="tag bank" v-if="item.bankName">{{ item.bankName }}</text>
          </view>
          <view class="item-content">
            {{ truncateText(item.question?.content, 80) }}
          </view>
          <view class="item-footer" v-if="item.notes">
            <text class="notes-preview">备注: {{ truncateText(item.notes, 30) }}</text>
          </view>
        </view>
        <button class="remove-btn" @click.stop="removeFavorite(item)">
          移除
        </button>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-container" v-else>
      <text class="empty-title">暂无收藏题目</text>
      <text class="empty-desc">去答题页面收藏一些题目吧</text>
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
              <text class="tag bank" v-if="currentItem.bankName">{{ currentItem.bankName }}</text>
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

          <view class="detail-section" v-if="currentItem?.question?.analysis">
            <text class="section-label">答案解析</text>
            <text class="analysis-text">{{ currentItem.question.analysis }}</text>
          </view>

          <view class="detail-section" v-if="currentItem?.notes">
            <text class="section-label">收藏备注</text>
            <text class="notes-text">{{ currentItem.notes }}</text>
          </view>
        </scroll-view>

        <view class="detail-footer">
          <button class="footer-btn default" @click="closeDetail">关闭</button>
          <button class="footer-btn danger" @click="removeFavoriteAndClose">取消收藏</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { favoriteAPI } from '@/utils/api'

export default {
  name: 'FavoritesPage',
  setup() {
    const loading = ref(false)
    const favorites = ref([])

    // 题库统计
    const bankStats = ref([])
    const selectedBankIndex = ref(-1)

    // 计算题库选项列表
    const bankOptions = computed(() => {
      return bankStats.value.map(b => `${b.bankName} (${b.count}题)`)
    })

    // 详情弹窗
    const showDetailModal = ref(false)
    const currentItem = ref(null)

    // 获取收藏列表
    const fetchFavorites = async () => {
      try {
        loading.value = true
        const result = await favoriteAPI.getFavorites({
          current: 1,
          size: 100
        })
        favorites.value = result.records || result || []
      } catch (error) {
        console.error('获取收藏列表失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      } finally {
        loading.value = false
      }
    }

    // 获取题库统计
    const fetchBankStats = async () => {
      try {
        const stats = await favoriteAPI.getStatsByBank()
        bankStats.value = stats || []
      } catch (error) {
        console.error('获取题库统计失败:', error)
      }
    }

    // 题库选择变化
    const handleBankChange = (e) => {
      selectedBankIndex.value = parseInt(e.detail.value)
    }

    // 练习收藏题目
    const practiceFavorites = () => {
      if (selectedBankIndex.value < 0) {
        uni.showToast({ title: '请先选择题库', icon: 'none' })
        return
      }
      const bank = bankStats.value[selectedBankIndex.value]
      if (bank) {
        uni.navigateTo({
          url: `/pages/practice/practice?bankId=${bank.bankId}&source=favorites`
        })
      }
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

    // 移除收藏
    const removeFavorite = async (item) => {
      uni.showModal({
        title: '确认移除',
        content: '确定要取消收藏吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await favoriteAPI.removeFavorite(item.id)
              favorites.value = favorites.value.filter(f => f.id !== item.id)
              uni.showToast({ title: '已取消收藏', icon: 'success' })
            } catch (error) {
              uni.showToast({ title: '操作失败', icon: 'none' })
            }
          }
        }
      })
    }

    // 移除并关闭
    const removeFavoriteAndClose = async () => {
      if (currentItem.value) {
        try {
          await favoriteAPI.removeFavorite(currentItem.value.id)
          favorites.value = favorites.value.filter(f => f.id !== currentItem.value.id)
          closeDetail()
          uni.showToast({ title: '已取消收藏', icon: 'success' })
        } catch (error) {
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
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
      fetchFavorites()
      fetchBankStats()
    })

    // 页面显示时刷新
    onShow(() => {
      fetchFavorites()
      fetchBankStats()
    })

    return {
      loading,
      favorites,
      bankStats,
      selectedBankIndex,
      bankOptions,
      showDetailModal,
      currentItem,
      showDetail,
      closeDetail,
      removeFavorite,
      removeFavoriteAndClose,
      handleBankChange,
      practiceFavorites,
      isCorrectOption,
      formatCorrectAnswer,
      truncateText,
      getTypeLabel,
      getOptionLabel,
      goHome
    }
  }
}
</script>

<style scoped>
.favorites-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 0 0 calc(20rpx + env(safe-area-inset-bottom));
}

/* 筛选区域 */
.filter-section {
  margin: 20rpx;
  padding: 20rpx;
  background: #fff;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
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

/* 收藏列表 */
.favorites-list {
  padding: 0 20rpx;
}

.favorite-item {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-main {
  flex: 1;
  min-width: 0;
}

.item-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.tag {
  font-size: 22rpx;
  padding: 6rpx 14rpx;
  border-radius: 6rpx;
}

.tag.type.single { background: #ecf5ff; color: #409eff; }
.tag.type.multiple { background: #f0f9eb; color: #67c23a; }
.tag.type.judge { background: #fdf6ec; color: #e6a23c; }
.tag.type.essay { background: #f4f4f5; color: #909399; }

.tag.bank {
  background: #f5f7fa;
  color: #909399;
}

.item-content {
  font-size: 28rpx;
  color: #303133;
  line-height: 1.6;
}

.item-footer {
  margin-top: 12rpx;
}

.notes-preview {
  font-size: 24rpx;
  color: #909399;
}

.remove-btn {
  font-size: 24rpx;
  padding: 12rpx 20rpx;
  border-radius: 6rpx;
  border: none;
  background: #fef0f0;
  color: #f56c6c;
  margin-left: 16rpx;
  flex-shrink: 0;
  line-height: 1;
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
  margin-bottom: 28rpx;
}

.section-tags {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-wrap: wrap;
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

.analysis-text {
  font-size: 28rpx;
  color: #606266;
  line-height: 1.7;
  padding: 20rpx;
  background: #f5f7fa;
  border-radius: 8rpx;
}

.notes-text {
  font-size: 28rpx;
  color: #606266;
  line-height: 1.7;
  padding: 20rpx;
  background: #ecf5ff;
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

.footer-btn.danger {
  background: #f56c6c;
  color: #fff;
}
</style>
