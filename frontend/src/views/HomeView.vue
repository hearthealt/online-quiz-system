<template>
  <AppLayout>
    <div class="home-container">
      <!-- Hero Section -->
      <section class="hero-section">
        <div class="hero-content">
          <div class="hero-badge">
            <el-icon><Star /></el-icon>
            <span>专业在线学习平台</span>
          </div>
          <h1 class="hero-title">
            <span class="gradient-text">智能答题系统</span>
          </h1>
          <p class="hero-description">
            提供专业的在线答题练习，支持多种题型，智能分析学习效果，帮助您高效学习和提升能力
          </p>
          <div class="hero-image">
            <img src="/images/quiz-hero.svg" alt="智能答题系统" />
          </div>
          <div class="hero-stats">
            <div class="stat-item">
              <div class="stat-number">{{ animatedQuestions }}</div>
              <div class="stat-label">题库题目</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ animatedUsers }}</div>
              <div class="stat-label">注册用户</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ animatedSessions }}</div>
              <div class="stat-label">答题次数</div>
            </div>
          </div>
          <div class="hero-actions">
            <el-button type="primary" size="large" @click="startQuiz" class="primary-btn">
              <el-icon><Edit /></el-icon>
              开始答题
            </el-button>
            <el-button size="large" @click="viewQuestions" class="secondary-btn">
              <el-icon><Reading /></el-icon>
              浏览题库
            </el-button>
            <el-button size="large" @click="viewDemo" class="demo-btn">
              <el-icon><VideoPlay /></el-icon>
              观看演示
            </el-button>
          </div>
        </div>
        <div class="hero-image">
          <div class="floating-cards">
            <div class="floating-card card-1">
              <el-icon><QuestionFilled /></el-icon>
              <span>多题型</span>
            </div>
            <div class="floating-card card-2">
              <el-icon><Trophy /></el-icon>
              <span>智能评分</span>
            </div>
            <div class="floating-card card-3">
              <el-icon><TrendCharts /></el-icon>
              <span>数据分析</span>
            </div>
          </div>
          <el-image
            src="/images/quiz-hero.svg"
            alt="在线答题"
            fit="contain"
            class="hero-main-image"
          />
        </div>
      </section>

      <!-- Features Section -->
      <section class="features-section">
        <div class="section-header">
          <h2 class="section-title">系统特色</h2>
          <p class="section-subtitle">为您提供专业的学习体验</p>
        </div>
        <el-row :gutter="24">
          <el-col :xs="24" :sm="12" :md="6" v-for="(feature, index) in features" :key="feature.title">
            <div class="feature-card-wrapper" :style="{ '--delay': index * 0.1 + 's' }">
              <el-card class="feature-card" shadow="hover">
                <div class="feature-icon">
                  <div class="icon-bg" :style="{ backgroundColor: feature.color + '20' }">
                    <el-icon :size="32" :color="feature.color">
                      <component :is="feature.icon" />
                    </el-icon>
                  </div>
                </div>
                <h3 class="feature-title">{{ feature.title }}</h3>
                <p class="feature-description">{{ feature.description }}</p>
                <div class="feature-arrow">
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </el-card>
            </div>
          </el-col>
        </el-row>
      </section>

      <!-- Quick Start Section -->
      <section class="quickstart-section" v-if="userStore.isLoggedIn">
        <div class="section-header">
          <h2 class="section-title">快速开始</h2>
          <p class="section-subtitle">选择您的学习方式</p>
        </div>
        <el-row :gutter="24">
          <el-col :xs="24" :sm="12" :md="6">
            <div class="action-card-wrapper" @click="startPractice">
              <el-card class="action-card" shadow="hover">
                <div class="action-icon">
                  <div class="icon-circle practice">
                    <el-icon :size="24"><Trophy /></el-icon>
                  </div>
                </div>
                <h3>练习模式</h3>
                <p>随机选择题目进行练习，可查看答案解析</p>
                <div class="action-badge">推荐</div>
              </el-card>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <div class="action-card-wrapper" @click="startExam">
              <el-card class="action-card" shadow="hover">
                <div class="action-icon">
                  <div class="icon-circle exam">
                    <el-icon :size="24"><Timer /></el-icon>
                  </div>
                </div>
                <h3>考试模式</h3>
                <p>限时答题，提交后统一查看结果</p>
                <div class="action-badge">挑战</div>
              </el-card>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <div class="action-card-wrapper" @click="viewWrongQuestions">
              <el-card class="action-card" shadow="hover">
                <div class="action-icon">
                  <div class="icon-circle wrong">
                    <el-icon :size="24"><Warning /></el-icon>
                  </div>
                </div>
                <h3>错题本</h3>
                <p>复习答错的题目，巩固薄弱知识点</p>
                <div class="action-badge">复习</div>
              </el-card>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <div class="action-card-wrapper" @click="viewRanking">
              <el-card class="action-card" shadow="hover">
                <div class="action-icon">
                  <div class="icon-circle ranking">
                    <el-icon :size="24"><TrendCharts /></el-icon>
                  </div>
                </div>
                <h3>排行榜</h3>
                <p>查看成绩排名，与他人比较学习成果</p>
                <div class="action-badge">竞争</div>
              </el-card>
            </div>
          </el-col>
        </el-row>
      </section>

      <!-- Statistics Section -->
      <section class="stats-section" v-if="userStore.isLoggedIn && stats">
        <h2 class="section-title">我的数据</h2>
        <el-row :gutter="24">
          <el-col :xs="12" :sm="6" v-for="stat in statsData" :key="stat.label">
            <el-card class="stat-card">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </el-card>
          </el-col>
        </el-row>
      </section>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, type Ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Edit, Reading, Trophy, Timer, Warning, QuestionFilled, Star, TrendCharts, VideoPlay, ArrowRight } from '@element-plus/icons-vue'
import AppLayout from '@/components/common/AppLayout.vue'
import { useUserStore } from '@/store/user'
import type { AnswerStats } from '@/types/index'
import * as answerApi from '@/api/answer'
import * as statisticsApi from '@/api/statistics'

const router = useRouter()
const userStore = useUserStore()

// 系统特色
const features = ref([
  {
    icon: 'QuestionFilled',
    title: '多种题型',
    description: '支持单选、多选、判断、简答等多种题型',
    color: '#409eff'
  },
  {
    icon: 'Star',
    title: '智能收藏',
    description: '收藏重要题目，建立个人题库',
    color: '#67c23a'
  },
  {
    icon: 'Collection',
    title: '错题管理',
    description: '自动收集错题，针对性复习',
    color: '#e6a23c'
  },
  {
    icon: 'TrendCharts',
    title: '数据分析',
    description: '详细的答题统计和能力分析',
    color: '#f56c6c'
  }
])

// 用户统计数据
const stats = ref<AnswerStats | null>(null)

// 系统统计数据
const totalQuestions = ref(0)
const totalUsers = ref(0)
const totalSessions = ref(0)

// 动画数字
const animatedQuestions = ref(0)
const animatedUsers = ref(0)
const animatedSessions = ref(0)

const statsData = computed(() => {
  if (!stats.value) return []

  return [
    { label: '总答题数', value: stats.value.totalAnswers },
    { label: '正确题数', value: stats.value.correctAnswers },
    { label: '正确率', value: `${stats.value.accuracy}%` }
  ]
})

// 页面方法
const startQuiz = () => {
  if (userStore.isLoggedIn) {
    router.push('/banks')
  } else {
    router.push('/login?redirect=/banks')
  }
}

const viewQuestions = () => {
  router.push('/banks')
}

const startPractice = () => {
  router.push('/banks')
}

const startExam = () => {
  router.push('/banks')
}

const viewWrongQuestions = () => {
  router.push('/wrong-questions')
}

const viewRanking = () => {
  router.push('/ranking')
}

const viewDemo = () => {
  // 可以添加演示视频或引导
  ElMessage.info('演示功能开发中...')
}


// 获取用户统计数据
const fetchStats = async () => {
  if (!userStore.isLoggedIn) return
  
  try {
    const data = await answerApi.getAnswerStats()
    stats.value = data
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 数字动画函数
const animateNumber = (target: number, current: Ref<number>, duration: number = 2000) => {
  const start = current.value
  const increment = (target - start) / (duration / 16)
  let currentValue = start
  
  const timer = setInterval(() => {
    currentValue += increment
    if ((increment > 0 && currentValue >= target) || (increment < 0 && currentValue <= target)) {
      current.value = target
      clearInterval(timer)
    } else {
      current.value = Math.floor(currentValue)
    }
  }, 16)
}

// 获取系统统计数据
const fetchSystemStats = async () => {
  try {
    const data = await statisticsApi.getSystemStats()
    totalQuestions.value = data.totalQuestions
    totalUsers.value = data.totalUsers
    totalSessions.value = data.totalSessions
    
    // 启动数字动画
    animateNumber(data.totalQuestions, animatedQuestions, 2000)
    animateNumber(data.totalUsers, animatedUsers, 2500)
    animateNumber(data.totalSessions, animatedSessions, 3000)
  } catch (error) {
    console.error('获取系统统计数据失败:', error)
    // 如果API失败，使用默认值
    totalQuestions.value = 1256
    totalUsers.value = 892
    totalSessions.value = 15678
    
    // 启动默认数字动画
    animateNumber(1256, animatedQuestions, 2000)
    animateNumber(892, animatedUsers, 2500)
    animateNumber(15678, animatedSessions, 3000)
  }
}

onMounted(() => {
  // 初始化用户状态
  userStore.initUserState()
  
  // 获取统计数据
  fetchStats()
  fetchSystemStats()
})
</script>

<style scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* Hero Section */
.hero-section {
  display: flex;
  align-items: center;
  min-height: 500px;
  margin-bottom: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24px;
  padding: 60px 40px;
  color: white;
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="white" opacity="0.1"/><circle cx="75" cy="75" r="1" fill="white" opacity="0.1"/><circle cx="50" cy="10" r="0.5" fill="white" opacity="0.1"/><circle cx="10" cy="60" r="0.5" fill="white" opacity="0.1"/><circle cx="90" cy="40" r="0.5" fill="white" opacity="0.1"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
  opacity: 0.3;
  pointer-events: none;
}

.hero-content {
  flex: 1;
  padding-right: 40px;
  position: relative;
  z-index: 2;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.2);
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 20px;
  backdrop-filter: blur(10px);
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 16px;
}

.gradient-text {
  background: linear-gradient(45deg, #fff, #f0f9ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-stats {
  display: flex;
  gap: 32px;
  margin: 32px 0;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.hero-description {
  font-size: 18px;
  line-height: 1.6;
  margin-bottom: 32px;
  opacity: 0.9;
}

.hero-image {
  margin: 2rem 0;
  text-align: center;
}

.hero-image img {
  max-width: 100%;
  height: auto;
  max-height: 300px;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.hero-image img:hover {
  transform: translateY(-5px);
}

.hero-actions {
  display: flex;
  gap: 16px;
}

.hero-actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.primary-btn {
  background: linear-gradient(45deg, #ff6b6b, #ee5a24);
  border: none;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.4);
}

.secondary-btn {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  backdrop-filter: blur(10px);
}

.demo-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  backdrop-filter: blur(10px);
}


.hero-actions .el-button {
  padding: 12px 24px;
  font-size: 16px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.hero-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
}

.hero-image {
  flex: 1;
  max-width: 400px;
  position: relative;
  z-index: 2;
}

.floating-cards {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.floating-card {
  position: absolute;
  background: rgba(255, 255, 255, 0.9);
  padding: 12px 16px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  animation: float 3s ease-in-out infinite;
}

.floating-card.card-1 {
  top: 20%;
  right: 10%;
  animation-delay: 0s;
}

.floating-card.card-2 {
  top: 50%;
  right: 5%;
  animation-delay: 1s;
}

.floating-card.card-3 {
  bottom: 20%;
  right: 15%;
  animation-delay: 2s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}

.hero-main-image {
  width: 100%;
  height: 300px;
  position: relative;
  z-index: 1;
}

/* Sections */
.section-header {
  text-align: center;
  margin-bottom: 48px;
}

.section-title {
  font-size: 32px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.section-subtitle {
  font-size: 16px;
  color: #909399;
  margin: 0;
}

.features-section {
  margin-bottom: 80px;
}

.feature-card-wrapper {
  animation: fadeInUp 0.6s ease-out;
  animation-delay: var(--delay);
  animation-fill-mode: both;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.feature-card {
  text-align: center;
  padding: 32px 24px;
  height: 280px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea, #764ba2);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.feature-card:hover::before {
  transform: scaleX(1);
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  margin-bottom: 20px;
}

.icon-bg {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  transition: transform 0.3s ease;
}

.feature-card:hover .icon-bg {
  transform: scale(1.1);
}

.feature-arrow {
  position: absolute;
  top: 20px;
  right: 20px;
  opacity: 0;
  transition: opacity 0.3s ease;
  color: #909399;
}

.feature-card:hover .feature-arrow {
  opacity: 1;
}

.feature-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.feature-description {
  color: #606266;
  line-height: 1.5;
}

/* Quick Start */
.quickstart-section {
  margin-bottom: 80px;
}

.action-card-wrapper {
  cursor: pointer;
  transition: transform 0.3s ease;
}

.action-card-wrapper:hover {
  transform: translateY(-4px);
}

.action-card {
  text-align: center;
  padding: 32px 24px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  height: 200px;
}

.action-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.action-card:hover::before {
  opacity: 1;
}

.action-card:hover {
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
  border-color: #667eea;
}

.action-icon {
  margin-bottom: 16px;
  position: relative;
  z-index: 2;
}

.icon-circle {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  transition: transform 0.3s ease;
}

.icon-circle.practice {
  background: linear-gradient(135deg, #67c23a, #85ce61);
  color: white;
}

.icon-circle.exam {
  background: linear-gradient(135deg, #e6a23c, #f0c78a);
  color: white;
}

.icon-circle.wrong {
  background: linear-gradient(135deg, #f56c6c, #f89898);
  color: white;
}

.icon-circle.ranking {
  background: linear-gradient(135deg, #409eff, #79bbff);
  color: white;
}

.action-card:hover .icon-circle {
  transform: scale(1.1);
}

.action-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  z-index: 2;
}

.action-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  position: relative;
  z-index: 2;
}

.action-card p {
  color: #606266;
  line-height: 1.5;
  position: relative;
  z-index: 2;
}

/* Statistics */
.stats-section {
  margin-bottom: 40px;
}

.stat-card {
  text-align: center;
  padding: 24px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  color: #606266;
  font-size: 14px;
}

/* Responsive */
@media (max-width: 768px) {
  .hero-section {
    flex-direction: column;
    text-align: center;
    padding: 24px;
  }

  .hero-content {
    padding-right: 0;
    margin-bottom: 24px;
  }

  .hero-title {
    font-size: 32px;
  }

  .hero-description {
    font-size: 16px;
  }

  .hero-actions {
    justify-content: center;
    flex-wrap: wrap;
  }

  .section-title {
    font-size: 24px;
    margin-bottom: 32px;
  }
}
</style>
