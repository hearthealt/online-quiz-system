<template>
  <div class="ranking-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <div class="title-with-back">
            <el-button @click="goBack" type="text" class="back-button">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <h1 class="page-title">
              <el-icon class="title-icon"><Trophy /></el-icon>
              成绩排行榜
            </h1>
          </div>
          <p class="page-description">查看各项排行榜，展示优秀学习成果</p>
        </div>

        <div class="filter-controls">
          <el-space wrap>
            <el-select v-model="rankingType" @change="loadRanking" placeholder="排行类型" style="width: 140px">
              <el-option label="正确数" value="correct" />
              <el-option label="正确率" value="accuracy" />
              <el-option label="答题数量" value="answers" />
            </el-select>

            <el-select v-model="period" @change="loadRanking" placeholder="时间范围" style="width: 120px">
              <el-option label="今日" value="day" />
              <el-option label="本周" value="week" />
              <el-option label="本月" value="month" />
              <el-option label="总排行" value="all" />
            </el-select>

            <el-button @click="loadRanking" type="primary">
              <el-icon><Refresh /></el-icon>
              刷新排行榜
            </el-button>
          </el-space>
        </div>
      </div>
    </div>

    <!-- 排行榜内容 -->
    <div class="ranking-content">
      <div class="ranking-card">
        <!-- 排行榜标题 -->
        <div class="ranking-header">
          <h2 class="ranking-title">
            {{ getRankingTitle() }}
            <el-tag :type="getRankingTypeColor()" size="small">{{ getRankingTypeName() }}</el-tag>
          </h2>
          <div class="ranking-meta">
            <span>更新时间：{{ formatTime(updateTime) }}</span>
            <span>共 {{ rankingData.length }} 人</span>
          </div>
        </div>

        <!-- 前三名特殊展示 -->
        <div class="top-three" v-if="rankingData.length > 0">
          <div class="podium">
            <!-- 第二名 -->
            <div
              v-if="rankingData.length > 1"
              class="podium-item podium-second"
              @click="viewUserDetail(rankingData[1])"
            >
              <div class="podium-rank">2</div>
              <div class="podium-avatar">
                <el-avatar
                  :size="60"
                  :src="getAvatarUrl(rankingData[1].avatar, rankingData[1].username)"
                  class="rank-avatar silver"
                >
                  {{ getUserInitial(rankingData[1].nickname, rankingData[1].username) }}
                </el-avatar>
              </div>
              <div class="podium-info">
                <div class="podium-name">{{ getDisplayName(rankingData[1].nickname, rankingData[1].username) }}</div>
                <div class="podium-value">{{ formatValue(rankingData[1].value) }}</div>
              </div>
            </div>

            <!-- 第一名 -->
            <div
              class="podium-item podium-first"
              @click="viewUserDetail(rankingData[0])"
            >
              <div class="podium-rank">1</div>
              <div class="podium-crown">
                <el-icon class="crown-icon"><Star /></el-icon>
              </div>
              <div class="podium-avatar">
                <el-avatar
                  :size="80"
                  :src="getAvatarUrl(rankingData[0].avatar, rankingData[0].username)"
                  class="rank-avatar gold"
                >
                  {{ getUserInitial(rankingData[0].nickname, rankingData[0].username) }}
                </el-avatar>
              </div>
              <div class="podium-info">
                <div class="podium-name">{{ getDisplayName(rankingData[0].nickname, rankingData[0].username) }}</div>
                <div class="podium-value">{{ formatValue(rankingData[0].value) }}</div>
              </div>
            </div>

            <!-- 第三名 -->
            <div
              v-if="rankingData.length > 2"
              class="podium-item podium-third"
              @click="viewUserDetail(rankingData[2])"
            >
              <div class="podium-rank">3</div>
              <div class="podium-avatar">
                <el-avatar
                  :size="50"
                  :src="getAvatarUrl(rankingData[2].avatar, rankingData[2].username)"
                  class="rank-avatar bronze"
                >
                  {{ getUserInitial(rankingData[2].nickname, rankingData[2].username) }}
                </el-avatar>
              </div>
              <div class="podium-info">
                <div class="podium-name">{{ getDisplayName(rankingData[2].nickname, rankingData[2].username) }}</div>
                <div class="podium-value">{{ formatValue(rankingData[2].value) }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 完整排行榜 -->
        <div class="ranking-list" v-loading="loading">
          <div class="list-header">
            <span class="header-rank">排名</span>
            <span class="header-user">用户</span>
            <span class="header-value">{{ getRankingValueName() }}</span>
            <span class="header-progress">相对进度</span>
          </div>

          <div
            v-for="(item, index) in rankingData"
            :key="item.userId"
            class="ranking-item"
            :class="{
              'is-current-user': item.userId === userStore.user?.id,
              'is-top-three': index < 3
            }"
            @click="viewUserDetail(item)"
          >
            <div class="item-rank">
              <div class="rank-number" :class="getRankClass(item.rank)">
                <el-icon v-if="item.rank <= 3" class="rank-medal">
                  <Trophy />
                </el-icon>
                <span v-else>{{ item.rank }}</span>
              </div>
            </div>

            <div class="item-user">
              <el-avatar
                :size="40"
                :src="getAvatarUrl(item.avatar, item.username)"
                class="user-avatar"
              >
                {{ getUserInitial(item.nickname, item.username) }}
              </el-avatar>
              <div class="user-info">
                <div class="user-name">
                  {{ getDisplayName(item.nickname, item.username) }}
                  <el-tag v-if="item.userId === userStore.user?.id" type="success" size="small">我</el-tag>
                </div>
                <div class="user-meta">@{{ item.username }}</div>
                <div class="user-stats">
                  <span class="stat-item">答题 {{ item.totalAnswers }}题</span>
                  <span class="stat-item">答对 {{ item.correctAnswers }}题</span>
                  <span class="stat-item">正确率 {{ item.accuracy }}%</span>
                </div>
              </div>
            </div>

            <div class="item-value">
              <span class="value-number">{{ formatValue(item.value) }}</span>
            </div>

            <div class="item-progress">
              <el-progress
                :percentage="getProgressPercentage(item.value)"
                :color="getProgressColor(index)"
                :stroke-width="8"
                :show-text="false"
              />
              <span class="progress-text">{{ getProgressPercentage(item.value) }}%</span>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="!loading && rankingData.length === 0" class="empty-state">
            <el-empty description="暂无排行榜数据">
              <el-button type="primary" @click="loadRanking">重新加载</el-button>
            </el-empty>
          </div>
        </div>
      </div>

      <!-- 我的排名卡片 -->
      <div class="my-ranking-card" v-if="myRanking">
        <div class="my-ranking-header">
          <h3>我的排名</h3>
          <el-tag :type="myRanking.rank <= 10 ? 'success' : myRanking.rank <= 50 ? 'warning' : 'info'">
            第 {{ myRanking.rank }} 名
          </el-tag>
        </div>
        <div class="my-ranking-content">
          <div class="my-stat">
            <span class="stat-label">{{ getRankingValueName() }}</span>
            <span class="stat-value">{{ formatValue(myRanking.value) }}</span>
          </div>
          <div class="my-stat">
            <span class="stat-label">超越用户</span>
            <span class="stat-value">{{ myRanking.percentage }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="userDetailVisible"
      title="用户详情"
      width="500px"
      @close="userDetailVisible = false"
    >
      <div v-if="selectedUser" class="user-detail">
        <div class="detail-header">
          <el-avatar
            :size="80"
            :src="getAvatarUrl(selectedUser.avatar, selectedUser.username)"
          >
            {{ getUserInitial(selectedUser.nickname, selectedUser.username) }}
          </el-avatar>
          <div class="detail-info">
            <h3>{{ getDisplayName(selectedUser.nickname, selectedUser.username) }}</h3>
            <p>@{{ selectedUser.username }}</p>
            <el-tag :type="getRankingTypeColor()">
              第 {{ selectedUser.rank }} 名 · {{ formatValue(selectedUser.value) }}
            </el-tag>
          </div>
        </div>

        <!-- 用户详细信息 -->
        <div class="detail-stats">
          <el-descriptions title="统计信息" :column="2" border>
            <el-descriptions-item label="排名">第 {{ selectedUser.rank }} 名</el-descriptions-item>
            <el-descriptions-item :label="getRankingValueName()">{{ formatValue(selectedUser.value) }}</el-descriptions-item>
            <el-descriptions-item label="答题总数">{{ selectedUser.totalAnswers }} 题</el-descriptions-item>
            <el-descriptions-item label="答对题数">{{ selectedUser.correctAnswers }} 题</el-descriptions-item>
            <el-descriptions-item label="正确率">{{ selectedUser.accuracy }}%</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <template #footer>
        <el-button @click="userDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Trophy,
  Refresh,
  UserFilled,
  Star,
  ArrowLeft
} from '@element-plus/icons-vue'
import { getRankingData } from '@/api/statistics'
import { useUserStore } from '@/store/user'
import { getAvatarUrl, getDisplayName, getUserInitial } from '@/utils/avatar'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const rankingType = ref<'correct' | 'accuracy' | 'answers'>('correct')
const period = ref<'day' | 'week' | 'month' | 'all'>('all')
const rankingData = ref<Array<{
  rank: number
  userId: number
  username: string
  nickname?: string
  avatar?: string
  value: number
  percentage?: number
  totalAnswers: number
  correctAnswers: number
  accuracy: number
}>>([])
const updateTime = ref<Date>(new Date())
const userDetailVisible = ref(false)
const selectedUser = ref<any>(null)

// 计算我的排名
const myRanking = computed(() => {
  if (!userStore.user?.id) return null
  return rankingData.value.find(item => item.userId === userStore.user?.id)
})

// 获取排行榜数据
const loadRanking = async () => {
  loading.value = true
  try {
    const params = {
      type: rankingType.value,
      period: period.value
    }
    const data = await getRankingData(params)

    // 转换数据格式以匹配前端期望的结构
    rankingData.value = data.map((item: any) => {
      let value = 0
      if (rankingType.value === 'correct') {
        value = item.correctAnswers || 0
      } else if (rankingType.value === 'accuracy') {
        value = item.accuracy || 0
      } else if (rankingType.value === 'answers') {
        value = item.totalAnswers || 0
      }

      return {
        rank: item.rank,
        userId: item.id, // 后端返回的是 id，映射为 userId
        username: item.username,
        nickname: item.nickname,
        avatar: item.avatar,
        value: value,
        percentage: 0, // 稍后计算
        totalAnswers: item.totalAnswers || 0,
        correctAnswers: item.correctAnswers || 0,
        accuracy: item.accuracy || 0
      }
    })
    
    // 计算相对进度百分比
    if (rankingData.value.length > 0) {
      const maxValue = rankingData.value[0].value
      rankingData.value.forEach(item => {
        item.percentage = maxValue > 0 ? Math.round((item.value / maxValue) * 100) : 0
      })
    }
    
    updateTime.value = new Date()
  } catch (error) {
    console.error('获取排行榜失败:', error)
    ElMessage.error('获取排行榜数据失败')
    rankingData.value = []
  } finally {
    loading.value = false
  }
}

// 获取排行榜标题
const getRankingTitle = () => {
  const typeNames = {
    correct: '正确数排行榜',
    accuracy: '正确率排行榜',
    answers: '答题量排行榜'
  }
  const periodNames = {
    day: '今日',
    week: '本周',
    month: '本月',
    all: '总'
  }
  return `${periodNames[period.value]}${typeNames[rankingType.value]}`
}

// 获取排行榜类型名称
const getRankingTypeName = () => {
  const names = {
    correct: '按正确数',
    accuracy: '按正确率',
    answers: '按答题数'
  }
  return names[rankingType.value]
}

// 获取排行榜类型颜色
const getRankingTypeColor = () => {
  const colors = {
    correct: 'primary',
    accuracy: 'success',
    answers: 'warning'
  }
  return colors[rankingType.value]
}

// 获取数值名称
const getRankingValueName = () => {
  const names = {
    correct: '正确数',
    accuracy: '正确率',
    answers: '答题数'
  }
  return names[rankingType.value]
}

// 格式化数值
const formatValue = (value: number) => {
  if (rankingType.value === 'accuracy') {
    return `${value}%`
  } else {
    return `${value}题`
  }
}

// 获取排名样式类
const getRankClass = (rank: number) => {
  if (rank === 1) return 'rank-gold'
  if (rank === 2) return 'rank-silver'
  if (rank === 3) return 'rank-bronze'
  return ''
}

// 获取进度百分比
const getProgressPercentage = (value: number) => {
  if (rankingData.value.length === 0) return 0
  const maxValue = rankingData.value[0].value
  return maxValue > 0 ? Math.round((value / maxValue) * 100) : 0
}

// 获取进度条颜色
const getProgressColor = (index: number) => {
  if (index === 0) return '#f56c6c'
  if (index === 1) return '#e6a23c'
  if (index === 2) return '#909399'
  return '#409eff'
}

// 格式化时间
const formatTime = (time: Date) => {
  return time.toLocaleString('zh-CN')
}

// 查看用户详情
const viewUserDetail = (user: any) => {
  selectedUser.value = user
  userDetailVisible.value = true
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 页面加载时获取数据
onMounted(() => {
  loadRanking()
})
</script>

<style scoped>
.ranking-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0;
}

.page-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding: 20px 24px;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.title-section {
  flex: 1;
}

.title-with-back {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 8px;
}

.back-button {
  font-size: 14px;
  color: #666;
  padding: 8px 12px;
  border-radius: 6px;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.back-button:hover {
  background: rgba(255, 255, 255, 0.8);
  color: #409eff;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 32px;
  color: #f39c12;
}

.page-description {
  color: #666;
  margin: 0;
  font-size: 16px;
}

.ranking-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 24px;
}

.ranking-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.ranking-header {
  padding: 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ranking-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.ranking-meta {
  font-size: 14px;
  color: #666;
}

.ranking-meta span {
  margin-left: 16px;
}

.ranking-meta span:first-child {
  margin-left: 0;
}

.top-three {
  padding: 32px 24px;
  background: linear-gradient(135deg, #f8f9ff 0%, #e3f2fd 100%);
}

.podium {
  display: flex;
  justify-content: center;
  align-items: end;
  gap: 24px;
  max-width: 600px;
  margin: 0 auto;
}

.podium-item {
  text-align: center;
  cursor: pointer;
  transition: transform 0.3s;
  position: relative;
}

.podium-item:hover {
  transform: translateY(-5px);
}

.podium-first {
  order: 2;
}

.podium-second {
  order: 1;
}

.podium-third {
  order: 3;
}

.podium-rank {
  position: absolute;
  top: -10px;
  right: -10px;
  width: 24px;
  height: 24px;
  background: #409eff;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  z-index: 1;
}

.podium-crown {
  position: absolute;
  top: -15px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1;
}

.crown-icon {
  font-size: 24px;
  color: #f39c12;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.podium-avatar {
  margin: 0 auto 12px;
  position: relative;
}

.rank-avatar {
  border: 3px solid;
}

.rank-avatar.gold {
  border-color: #f39c12;
  box-shadow: 0 0 20px rgba(243, 156, 18, 0.3);
}

.rank-avatar.silver {
  border-color: #95a5a6;
  box-shadow: 0 0 15px rgba(149, 165, 166, 0.3);
}

.rank-avatar.bronze {
  border-color: #cd7f32;
  box-shadow: 0 0 12px rgba(205, 127, 50, 0.3);
}

.podium-name {
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
  font-size: 14px;
}

.podium-value {
  font-size: 16px;
  font-weight: 700;
  color: #409eff;
}

.ranking-list {
  min-height: 400px;
}

.list-header {
  display: grid;
  grid-template-columns: 80px 1fr 120px 150px;
  gap: 16px;
  padding: 16px 24px;
  background: #f8f9fa;
  font-weight: 600;
  color: #666;
  font-size: 14px;
  border-bottom: 1px solid #f0f0f0;
}

.ranking-item {
  display: grid;
  grid-template-columns: 80px 1fr 120px 150px;
  gap: 16px;
  padding: 16px 24px;
  border-bottom: 1px solid #f8f9fa;
  transition: all 0.3s;
  cursor: pointer;
  align-items: center;
}

.ranking-item:hover {
  background: #f8f9ff;
}

.ranking-item.is-current-user {
  background: #e8f5e8;
  border-left: 4px solid #67c23a;
}

.ranking-item.is-top-three {
  background: linear-gradient(90deg, rgba(255, 215, 0, 0.05) 0%, rgba(255, 255, 255, 0) 100%);
}

.item-rank {
  display: flex;
  justify-content: center;
}

.rank-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
}

.rank-number.rank-gold {
  background: linear-gradient(135deg, #f39c12, #f1c40f);
  color: white;
}

.rank-number.rank-silver {
  background: linear-gradient(135deg, #95a5a6, #bdc3c7);
  color: white;
}

.rank-number.rank-bronze {
  background: linear-gradient(135deg, #cd7f32, #d4915d);
  color: white;
}

.rank-medal {
  font-size: 16px;
}

.item-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  flex: 1;
}

.user-name {
  font-weight: 600;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;
}

.user-meta {
  font-size: 12px;
  color: #999;
}

.user-stats {
  display: flex;
  gap: 8px;
  margin-top: 4px;
  flex-wrap: wrap;
}

.stat-item {
  font-size: 11px;
  color: #666;
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  white-space: nowrap;
}

.item-value {
  text-align: center;
}

.value-number {
  font-size: 18px;
  font-weight: 700;
  color: #409eff;
}

.item-progress {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-text {
  font-size: 12px;
  color: #666;
  min-width: 35px;
}

.my-ranking-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  padding: 24px;
  height: fit-content;
  position: sticky;
  top: 24px;
}

.my-ranking-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.my-ranking-header h3 {
  margin: 0;
  font-size: 18px;
  color: #1a1a1a;
}

.my-ranking-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.my-stat {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.stat-value {
  font-size: 16px;
  font-weight: 700;
  color: #409eff;
}

.empty-state {
  padding: 60px 24px;
  text-align: center;
}

.user-detail {
  padding: 0;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-info h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #1a1a1a;
}

.detail-info p {
  margin: 0 0 12px 0;
  color: #666;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .ranking-content {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .my-ranking-card {
    position: static;
    order: -1;
  }

  .podium {
    flex-wrap: wrap;
    justify-content: space-around;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }

  .title-with-back {
    justify-content: center;
    flex-wrap: wrap;
  }

  .back-button {
    font-size: 12px;
    padding: 6px 10px;
  }

  .page-title {
    font-size: 22px;
  }

  .filter-controls {
    width: 100%;
    justify-content: center;
  }

  .filter-controls .el-select {
    width: 130px !important;
  }

  .ranking-content {
    padding: 16px;
  }

  .podium {
    flex-direction: column;
    gap: 16px;
  }

  .podium-item {
    display: flex;
    align-items: center;
    gap: 16px;
    text-align: left;
    max-width: 300px;
    margin: 0 auto;
  }

  .podium-first {
    order: 1;
  }

  .podium-second {
    order: 2;
  }

  .podium-third {
    order: 3;
  }

  .list-header,
  .ranking-item {
    grid-template-columns: 60px 1fr 80px;
    gap: 8px;
  }

  .item-progress {
    display: none;
  }

  .header-value,
  .header-progress {
    text-align: center;
  }

  .navigator-grid {
    grid-template-columns: repeat(auto-fill, minmax(40px, 1fr));
    gap: 8px;
  }

  .navigator-item {
    width: 40px;
    height: 40px;
    font-size: 12px;
  }
}
</style>