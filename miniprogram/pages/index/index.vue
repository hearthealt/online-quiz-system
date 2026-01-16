<template>
  <view class="index-container">
    <!-- 进行中的答题 -->
    <view class="ongoing-section" v-if="ongoingSessions.length > 0">
      <view class="section-header">
        <text class="section-title">继续答题</text>
        <text class="section-badge">{{ ongoingSessions.length }}</text>
      </view>
      <view class="ongoing-list">
        <view
          v-for="session in ongoingSessions"
          :key="session.id"
          class="ongoing-card"
          :class="session.mode"
        >
          <view class="ongoing-left">
            <view class="ongoing-icon" :class="session.mode">
              <text>{{ getModeIcon(session.mode) }}</text>
            </view>
            <view class="ongoing-info">
              <text class="ongoing-name">{{ session.bankName }}</text>
              <view class="ongoing-meta">
                <text class="ongoing-tag" :class="session.mode">
                  {{ getModeLabel(session.mode) }}
                </text>
                <text class="ongoing-progress">
                  进度 {{ session.answeredQuestions || 0 }}/{{ session.totalQuestions || 0 }}
                </text>
              </view>
            </view>
          </view>
          <view class="ongoing-actions">
            <button class="continue-btn" @click="continueSession(session)">继续</button>
            <button class="reset-btn" @click="resetSession(session)">重置</button>
          </view>
        </view>
      </view>
    </view>

    <!-- 题库列表 -->
    <view class="banks-section">
      <view class="section-header">
        <text class="section-title">全部题库</text>
        <text class="section-count" v-if="totalQuestions > 0">共{{ totalQuestions }}题</text>
      </view>

      <!-- 加载状态 -->
      <view class="loading-container" v-if="loading">
        <view class="loading-spinner"></view>
        <text class="loading-text">加载中...</text>
      </view>

      <!-- 题库列表 -->
      <view class="banks-list" v-else-if="banks.length > 0">
        <view
          v-for="bank in banks"
          :key="bank.id"
          class="bank-card"
        >
          <view class="bank-main">
            <view class="bank-icon" :style="{ background: getBankColor(bank.id) }">
              <text class="icon-text">{{ bank.name.charAt(0) }}</text>
            </view>
            <view class="bank-info">
              <view class="bank-title-row">
                <text class="bank-name">{{ bank.name }}</text>
                <text class="bank-count">{{ bank.questionCount || 0 }}题</text>
              </view>
              <text class="bank-desc">{{ bank.description || '暂无描述' }}</text>
            </view>
          </view>
          <view class="bank-actions">
            <button
              class="action-btn practice"
              @click="startPractice(bank)"
              :disabled="!bank.questionCount"
            >
              顺序练习
            </button>
            <button
              class="action-btn exam"
              @click="startExam(bank)"
              :disabled="!bank.questionCount"
            >
              模拟考试
            </button>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-container" v-else>
        <text class="empty-title">暂无可用题库</text>
        <text class="empty-desc">请稍后再试</text>
      </view>
    </view>
  </view>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { bankAPI, sessionAPI } from '@/utils/api'

export default {
  name: 'IndexPage',
  setup() {
    const loading = ref(false)
    const banks = ref([])
    const ongoingSessions = ref([])

    // 题库颜色列表
    const bankColors = [
      '#409eff', '#67c23a', '#e6a23c', '#f56c6c',
      '#909399', '#00bcd4', '#9c27b0', '#ff5722'
    ]

    // 获取题库颜色
    const getBankColor = (bankId) => {
      return bankColors[bankId % bankColors.length]
    }

    // 计算总题目数
    const totalQuestions = computed(() => {
      return banks.value.reduce((sum, bank) => sum + (bank.questionCount || 0), 0)
    })

    // 获取题库列表
    const fetchBanks = async () => {
      loading.value = true
      try {
        const result = await bankAPI.getBanks()
        banks.value = result || []
      } catch (error) {
        console.error('获取题库列表失败:', error)
        uni.showToast({
          title: '获取题库失败',
          icon: 'none'
        })
      } finally {
        loading.value = false
      }
    }

    // 获取进行中的会话
    const fetchOngoingSessions = async () => {
      try {
        const result = await sessionAPI.getOngoingSessions()
        ongoingSessions.value = result || []
      } catch (error) {
        console.error('获取进行中会话失败:', error)
      }
    }

    // 开始练习
    const startPractice = (bank) => {
      if (!bank.questionCount) return
      uni.navigateTo({
        url: `/pages/practice/practice?bankId=${bank.id}&bankName=${encodeURIComponent(bank.name)}`
      })
    }

    // 开始考试
    const startExam = (bank) => {
      if (!bank.questionCount) return
      uni.navigateTo({
        url: `/pages/exam/exam?bankId=${bank.id}&bankName=${encodeURIComponent(bank.name)}`
      })
    }

    // 获取模式图标
    const getModeIcon = (mode) => {
      const icons = {
        practice: '练',
        exam: '考',
        favorite: '收',
        wrong: '错'
      }
      return icons[mode] || '练'
    }

    // 获取模式标签
    const getModeLabel = (mode) => {
      const labels = {
        practice: '练习模式',
        exam: '考试模式',
        favorite: '收藏练习',
        wrong: '错题练习'
      }
      return labels[mode] || '练习模式'
    }

    // 继续会话
    const continueSession = (session) => {
      if (session.mode === 'exam') {
        uni.navigateTo({
          url: `/pages/exam/exam?bankId=${session.bankId}&sessionId=${session.sessionId}`
        })
      } else {
        // practice, favorite, wrong 都走练习页面
        let url = `/pages/practice/practice?bankId=${session.bankId}&sessionId=${session.sessionId}`
        if (session.mode === 'favorite') {
          url += '&source=favorites'
        } else if (session.mode === 'wrong') {
          url += '&source=wrong'
        }
        uni.navigateTo({ url })
      }
    }

    // 重置会话
    const resetSession = (session) => {
      uni.showModal({
        title: '重置确认',
        content: '确定要重新开始吗？当前进度将会清除。',
        success: async (res) => {
          if (res.confirm) {
            try {
              await sessionAPI.resetSession(session.bankId, session.mode)
              uni.showToast({
                title: '已重置',
                icon: 'success'
              })
              fetchOngoingSessions()
            } catch (error) {
              console.error('重置会话失败:', error)
              uni.showToast({
                title: '重置失败',
                icon: 'none'
              })
            }
          }
        }
      })
    }

    // 页面加载
    onMounted(() => {
      fetchBanks()
      fetchOngoingSessions()
    })

    // 页面显示时刷新
    onShow(() => {
      fetchBanks()
      fetchOngoingSessions()
    })

    return {
      loading,
      banks,
      ongoingSessions,
      totalQuestions,
      getBankColor,
      getModeIcon,
      getModeLabel,
      startPractice,
      startExam,
      continueSession,
      resetSession
    }
  }
}
</script>

<style scoped>
.index-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20rpx;
  padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
}

/* Section 通用样式 */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
  padding: 0 8rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #303133;
}

.section-badge {
  background: #409eff;
  color: white;
  padding: 4rpx 16rpx;
  border-radius: 16rpx;
  font-size: 24rpx;
}

.section-count {
  font-size: 26rpx;
  color: #909399;
}

/* 继续答题区域 */
.ongoing-section {
  margin-bottom: 24rpx;
}

.ongoing-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.ongoing-card {
  background: white;
  border-radius: 12rpx;
  padding: 24rpx;
  border-left: 6rpx solid #67c23a;
}

.ongoing-card.exam {
  border-left-color: #e6a23c;
}

.ongoing-card.favorite {
  border-left-color: #409eff;
}

.ongoing-card.wrong {
  border-left-color: #f56c6c;
}

.ongoing-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.ongoing-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
}

.ongoing-icon.practice {
  background: #67c23a;
}

.ongoing-icon.exam {
  background: #e6a23c;
}

.ongoing-icon.favorite {
  background: #409eff;
}

.ongoing-icon.wrong {
  background: #f56c6c;
}

.ongoing-info {
  flex: 1;
  min-width: 0;
}

.ongoing-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #303133;
  display: block;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ongoing-meta {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.ongoing-tag {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
}

.ongoing-tag.practice {
  background: #f0f9eb;
  color: #67c23a;
}

.ongoing-tag.exam {
  background: #fdf6ec;
  color: #e6a23c;
}

.ongoing-tag.favorite {
  background: #ecf5ff;
  color: #409eff;
}

.ongoing-tag.wrong {
  background: #fef0f0;
  color: #f56c6c;
}

.ongoing-progress {
  font-size: 24rpx;
  color: #909399;
}

.ongoing-actions {
  display: flex;
  gap: 16rpx;
}

.continue-btn, .reset-btn {
  flex: 1;
  height: 68rpx;
  border-radius: 8rpx;
  font-size: 28rpx;
  font-weight: 500;
  border: none;
}

.continue-btn {
  background: #409eff;
  color: white;
}

.reset-btn {
  background: #f5f7fa;
  color: #606266;
}

/* 题库列表 */
.banks-section {
  flex: 1;
}

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

.banks-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.bank-card {
  background: white;
  border-radius: 12rpx;
  padding: 24rpx;
}

.bank-main {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.bank-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-text {
  font-size: 32rpx;
  font-weight: 700;
  color: white;
}

.bank-info {
  flex: 1;
  min-width: 0;
}

.bank-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8rpx;
}

.bank-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: 16rpx;
}

.bank-count {
  font-size: 26rpx;
  color: #409eff;
  font-weight: 500;
  flex-shrink: 0;
}

.bank-desc {
  font-size: 24rpx;
  color: #909399;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.bank-actions {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  flex: 1;
  height: 72rpx;
  border-radius: 8rpx;
  border: none;
  font-size: 28rpx;
  font-weight: 500;
}

.action-btn[disabled] {
  opacity: 0.4;
}

.action-btn.practice {
  background: #f0f9eb;
  color: #67c23a;
}

.action-btn.exam {
  background: #fdf6ec;
  color: #e6a23c;
}

.action-btn.practice:active {
  background: #e1f3d8;
}

.action-btn.exam:active {
  background: #faecd8;
}

/* 空状态 */
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 40rpx;
  text-align: center;
}

.empty-title {
  font-size: 32rpx;
  font-weight: 500;
  color: #303133;
  margin-bottom: 12rpx;
  display: block;
}

.empty-desc {
  font-size: 28rpx;
  color: #909399;
  display: block;
}
</style>
