<template>
  <view class="profile-container">
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="user-avatar">
        <image
          v-if="userInfo.avatar"
          :src="userInfo.avatar"
          class="avatar-image"
          mode="aspectFill"
        />
        <text v-else class="avatar-text">{{ userInfo.nickname?.charAt(0) || 'U' }}</text>
      </view>
      <view class="user-info">
        <view class="user-name">{{ userInfo.nickname || userInfo.username || '用户' }}</view>
        <view class="user-email">{{ userInfo.email || '未设置邮箱' }}</view>
        <view class="user-role">{{ getRoleText(userInfo.role) }}</view>
      </view>
      <view class="user-actions">
        <button class="edit-btn" @click="editProfile">编辑</button>
      </view>
    </view>

    <!-- 学习统计卡片 -->
    <view class="stats-card">
      <view class="card-header">
        <text class="card-title">学习统计</text>
      </view>

      <view class="stats-grid">
        <view class="stat-item">
          <view class="stat-number">{{ statistics.totalQuestions || 0 }}</view>
          <view class="stat-label">总答题数</view>
        </view>
        <view class="stat-item">
          <view class="stat-number success">{{ statistics.correctAnswers || 0 }}</view>
          <view class="stat-label">正确答题</view>
        </view>
        <view class="stat-item">
          <view class="stat-number primary">{{ statistics.accuracy || 0 }}%</view>
          <view class="stat-label">正确率</view>
        </view>
      </view>

      <view class="stats-details">
        <view class="detail-item">
          <text class="detail-label">练习次数</text>
          <text class="detail-value">{{ statistics.practiceCount || 0 }}次</text>
        </view>
        <view class="detail-item">
          <text class="detail-label">考试次数</text>
          <text class="detail-value">{{ statistics.examCount || 0 }}次</text>
        </view>
        <view class="detail-item">
          <text class="detail-label">收藏题目</text>
          <text class="detail-value">{{ statistics.favoriteCount || 0 }}题</text>
        </view>
        <view class="detail-item">
          <text class="detail-label">错题数量</text>
          <text class="detail-value">{{ statistics.wrongCount || 0 }}题</text>
        </view>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-section">
      <button class="logout-btn" @click="logout">退出登录</button>
    </view>
  </view>
</template>

<script>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { userStorage } from '@/utils/storage'
import { statisticsAPI } from '@/utils/api'

export default {
  name: 'ProfilePage',
  onPullDownRefresh() {
    getUserInfo()
    getStatistics()
    uni.stopPullDownRefresh()
  },
  setup() {
    const userInfo = ref({})
    const statistics = ref({})
    const loading = ref(false)

    // 获取用户信息
    const getUserInfo = () => {
      const user = userStorage.getUser()
      if (user) {
        userInfo.value = user
      }
    }

    // 获取学习统计
    const getStatistics = async () => {
      try {
        loading.value = true
        const stats = await statisticsAPI.getUserStats()
        statistics.value = stats || {}
      } catch (error) {
        console.error('获取统计信息失败:', error)
        uni.showToast({
          title: '获取统计信息失败',
          icon: 'none'
        })
      } finally {
        loading.value = false
      }
    }

    // 获取角色文本
    const getRoleText = (role) => {
      const roleMap = {
        'admin': '管理员',
        'user': '普通用户',
        'teacher': '教师'
      }
      return roleMap[role] || '用户'
    }

    // 编辑个人资料
    const editProfile = () => {
      uni.showModal({
        title: '编辑资料',
        content: '此功能开发中，敬请期待',
        showCancel: false,
        confirmText: '知道了'
      })
    }

    // 退出登录
    const logout = () => {
      uni.showModal({
        title: '退出登录',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            // 清除本地存储
            userStorage.clearLogin()

            uni.showToast({
              title: '已退出登录',
              icon: 'success'
            })

            // 跳转到登录页面
            setTimeout(() => {
              uni.reLaunch({
                url: '/pages/login/login'
              })
            }, 1500)
          }
        }
      })
    }

    // 刷新所有数据
    const refreshAllData = async () => {
      await getStatistics()
    }

    onMounted(() => {
      getUserInfo()
      refreshAllData()
    })

    // 页面显示时刷新数据
    onShow(() => {
      refreshAllData()
    })

    return {
      userInfo,
      statistics,
      loading,
      getRoleText,
      editProfile,
      logout
    }
  }
}
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20rpx;
  padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
}

/* 用户信息卡片 */
.user-card {
  background: white;
  border-radius: 12rpx;
  padding: 32rpx;
  margin-bottom: 16rpx;
  display: flex;
  align-items: center;
}

.user-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  overflow: hidden;
  flex-shrink: 0;
}

.avatar-image {
  width: 100%;
  height: 100%;
}

.avatar-text {
  font-size: 40rpx;
  font-weight: 600;
  color: white;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6rpx;
}

.user-email {
  font-size: 26rpx;
  color: #909399;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-role {
  font-size: 22rpx;
  color: #409eff;
  background: #ecf5ff;
  padding: 6rpx 12rpx;
  border-radius: 6rpx;
  display: inline-block;
}

.user-actions {
  margin-left: 16rpx;
  flex-shrink: 0;
}

.edit-btn {
  background: #409eff;
  color: white;
  border: none;
  border-radius: 8rpx;
  padding: 14rpx 28rpx;
  font-size: 26rpx;
  line-height: 1;
}

/* 学习统计卡片 */
.stats-card {
  background: white;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.card-header {
  margin-bottom: 20rpx;
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #303133;
}

.stats-grid {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 40rpx;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4rpx;
}

.stat-number.primary { color: #409eff; }
.stat-number.success { color: #67c23a; }

.stat-label {
  font-size: 24rpx;
  color: #909399;
}

.stats-details {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #f5f7fa;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  font-size: 28rpx;
  color: #606266;
}

.detail-value {
  font-size: 28rpx;
  font-weight: 500;
  color: #303133;
}

/* 退出登录 */
.logout-section {
  margin-top: 32rpx;
}

.logout-btn {
  width: 100%;
  background: #fff;
  color: #f56c6c;
  border: 2rpx solid #f56c6c;
  border-radius: 12rpx;
  padding: 24rpx;
  font-size: 30rpx;
  font-weight: 500;
}

.logout-btn:active {
  background: #fef0f0;
}
</style>
