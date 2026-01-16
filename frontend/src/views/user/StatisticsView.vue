<template>
  <AppLayout>
    <div class="statistics-container">
      <div class="page-header">
        <h1 class="page-title">统计分析</h1>
        <p class="page-description">查看您的答题数据分析和学习进度</p>
      </div>

      <!-- 概览统计 -->
      <el-row :gutter="24" class="overview-stats">
        <el-col :xs="12" :sm="6" v-for="stat in overviewStats" :key="stat.label">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" :style="{ background: stat.color + '20', color: stat.color }">
                <el-icon :size="24">
                  <component :is="stat.icon" />
                </el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stat.value }}</div>
                <div class="stat-label">{{ stat.label }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="24">
        <!-- 答题趋势图 -->
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <el-icon><TrendCharts /></el-icon>
                <span>答题趋势</span>
                <div class="header-actions">
                  <el-select v-model="trendPeriod" size="small" style="width: 100px;" @change="updateTrendChart">
                    <el-option label="7天" value="7d" />
                    <el-option label="30天" value="30d" />
                    <el-option label="90天" value="90d" />
                  </el-select>
                </div>
              </div>
            </template>
            <div class="trend-chart">
              <div class="chart-header">
                <span class="chart-title">最近{{ trendPeriod === '7d' ? '7天' : trendPeriod === '30d' ? '30天' : '90天' }}答题趋势</span>
              </div>
              <div class="trend-line-chart">
                <div class="y-axis">
                  <div class="y-label" v-for="n in 5" :key="n">{{ Math.round(maxTrendValue * (5 - n + 1) / 5) }}</div>
                </div>
                <div class="chart-area">
                  <svg viewBox="0 0 400 200" class="line-chart">
                    <!-- 网格线 -->
                    <defs>
                      <pattern id="grid" width="40" height="40" patternUnits="userSpaceOnUse">
                        <path d="M 40 0 L 0 0 0 40" fill="none" stroke="#f0f0f0" stroke-width="1"/>
                      </pattern>
                    </defs>
                    <rect width="400" height="200" fill="url(#grid)" />

                    <!-- 数据线 -->
                    <polyline
                      :points="trendLinePoints"
                      fill="none"
                      stroke="#409eff"
                      stroke-width="3"
                      stroke-linecap="round"
                    />

                    <!-- 数据点 -->
                    <circle
                      v-for="(point, index) in trendPoints"
                      :key="index"
                      :cx="point.x"
                      :cy="point.y"
                      r="4"
                      fill="#409eff"
                      class="trend-point"
                    />
                  </svg>
                  <div class="x-axis">
                    <div
                      v-for="(item, index) in trendData"
                      :key="index"
                      class="x-label"
                    >
                      {{ formatDate(item.date) }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 正确率分析 -->
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <el-icon><DataBoard /></el-icon>
                <span>正确率分析</span>
              </div>
            </template>
            <div class="accuracy-chart">
              <div class="chart-center">
                <div class="accuracy-ring">
                  <svg viewBox="0 0 120 120" class="ring-chart">
                    <!-- 背景圆环 -->
                    <circle
                      cx="60"
                      cy="60"
                      r="45"
                      fill="none"
                      stroke="#f0f0f0"
                      stroke-width="8"
                    />
                    <!-- 进度圆环 -->
                    <circle
                      cx="60"
                      cy="60"
                      r="45"
                      fill="none"
                      stroke="#67c23a"
                      stroke-width="8"
                      stroke-linecap="round"
                      :stroke-dasharray="accuracyCircumference"
                      :stroke-dashoffset="accuracyOffset"
                      transform="rotate(-90 60 60)"
                      class="progress-ring"
                    />
                  </svg>
                  <div class="ring-content">
                    <div class="accuracy-value">{{ overallAccuracy }}%</div>
                    <div class="accuracy-label">总正确率</div>
                  </div>
                </div>

                <div class="accuracy-stats">
                  <div class="stat-item">
                    <div class="stat-color correct"></div>
                    <span>正确: {{ totalCorrect }}题</span>
                  </div>
                  <div class="stat-item">
                    <div class="stat-color wrong"></div>
                    <span>错误: {{ totalWrong }}题</span>
                  </div>
                  <div class="stat-item">
                    <div class="stat-color total"></div>
                    <span>总计: {{ totalAnswered }}题</span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="24">
        <!-- 题型分布 -->
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <el-icon><DataAnalysis /></el-icon>
                <span>题型分布</span>
              </div>
            </template>
            <div class="type-chart">
              <div class="pie-chart-container">
                <svg viewBox="0 0 200 200" class="pie-chart">
                  <g v-for="(segment, index) in typeSegments" :key="index">
                    <path
                      :d="segment.path"
                      :fill="segment.color"
                      :stroke="'#fff'"
                      stroke-width="2"
                      class="pie-segment"
                    />
                    <text
                      v-if="segment.percentage > 5"
                      :x="segment.labelX"
                      :y="segment.labelY"
                      text-anchor="middle"
                      dominant-baseline="middle"
                      fill="white"
                      font-size="12"
                      font-weight="bold"
                    >
                      {{ segment.percentage }}%
                    </text>
                  </g>
                </svg>
              </div>
              <div class="pie-legend">
                <div
                  v-for="(type, index) in typeDistribution"
                  :key="type.type"
                  class="legend-item"
                >
                  <div class="legend-color" :style="{ backgroundColor: getTypeColor(type.type) }"></div>
                  <span class="legend-label">{{ getTypeLabel(type.type) }}</span>
                  <span class="legend-value">{{ type.count }}题 ({{ Math.round(type.count / totalTypeQuestions * 100) }}%)</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 空白占位或其他统计 -->
        <el-col :xs="24" :lg="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <el-icon><Trophy /></el-icon>
                <span>学习成就</span>
              </div>
            </template>
            <div class="achievement-content">
              <div class="achievement-item">
                <div class="achievement-icon">🎯</div>
                <div class="achievement-info">
                  <div class="achievement-title">答题达人</div>
                  <div class="achievement-desc">累计答题 {{ overviewStatsData.totalAnswers }} 道</div>
                </div>
              </div>
              <div class="achievement-item">
                <div class="achievement-icon">✅</div>
                <div class="achievement-info">
                  <div class="achievement-title">正确率</div>
                  <div class="achievement-desc">{{ overviewStatsData.accuracy }}% 正确率</div>
                </div>
              </div>
              <div class="achievement-item">
                <div class="achievement-icon">📚</div>
                <div class="achievement-info">
                  <div class="achievement-title">错题攻克</div>
                  <div class="achievement-desc">还有 {{ overviewStatsData.wrongQuestionCount }} 道错题待复习</div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 详细数据表格 -->
      <el-card class="table-card">
        <template #header>
          <div class="card-header">
            <el-icon><Document /></el-icon>
            <span>详细数据</span>
            <div class="header-actions">
              <el-button size="small" @click="exportStatistics">
                <el-icon><Download /></el-icon>
                导出报告
              </el-button>
            </div>
          </div>
        </template>

        <el-table :data="detailData" stripe style="width: 100%">
          <el-table-column prop="date" label="日期" width="120" />
          <el-table-column prop="totalAnswers" label="答题数" width="100" />
          <el-table-column prop="correctAnswers" label="正确数" width="100" />
          <el-table-column label="正确率" width="100">
            <template #default="{ row }">
              <el-tag :type="getAccuracyTagType(row.accuracy)" size="small">
                {{ row.accuracy }}%
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  TrendCharts, DataAnalysis,
  Document, Download, Edit, Check, Trophy, Warning, DataBoard
} from '@element-plus/icons-vue'
import AppLayout from '@/components/common/AppLayout.vue'
import * as statisticsApi from '@/api/statistics'

// 图表容器引用（保留以备将来使用）
const trendChartRef = ref<HTMLDivElement>()
const accuracyChartRef = ref<HTMLDivElement>()
const typeChartRef = ref<HTMLDivElement>()
const difficultyChartRef = ref<HTMLDivElement>()

// 数据状态
const loading = ref(false)
const trendPeriod = ref('30d')

// 真实数据状态
const trendData = ref<Array<{
  date: string
  totalAnswers: number
  correctAnswers: number
  accuracy: number
}>>([])

const typeDistribution = ref<Array<{
  type: string
  count: number
}>>([])

const overviewStatsData = ref({
  totalAnswers: 0,
  correctAnswers: 0,
  accuracy: 0,
  wrongQuestionCount: 0
})

// 计算属性
const maxTrendValue = computed(() => {
  if (trendData.value.length === 0) return 1
  return Math.max(...trendData.value.map(item => item.totalAnswers || 0), 1)
})

const trendLinePoints = computed(() => {
  if (trendData.value.length === 0) return ''
  const points = trendData.value.map((item, index) => {
    const x = trendData.value.length === 1 ? 200 : (index / (trendData.value.length - 1)) * 380 + 10
    const y = 180 - ((item.totalAnswers || 0) / maxTrendValue.value) * 160
    return `${x},${y}`
  })
  return points.join(' ')
})

const trendPoints = computed(() => {
  if (trendData.value.length === 0) return []
  return trendData.value.map((item, index) => {
    const x = trendData.value.length === 1 ? 200 : (index / (trendData.value.length - 1)) * 380 + 10
    const y = 180 - ((item.totalAnswers || 0) / maxTrendValue.value) * 160
    return { x, y }
  })
})

// 正确率分析计算
const totalAnswered = computed(() => {
  return overviewStatsData.value.totalAnswers
})

const totalCorrect = computed(() => {
  return overviewStatsData.value.correctAnswers
})

const totalWrong = computed(() => {
  return totalAnswered.value - totalCorrect.value
})

const overallAccuracy = computed(() => {
  return overviewStatsData.value.accuracy || 0
})

const accuracyCircumference = computed(() => {
  return 2 * Math.PI * 45 // r = 45
})

const accuracyOffset = computed(() => {
  const progress = overallAccuracy.value / 100
  return accuracyCircumference.value * (1 - progress)
})

// 题型分布计算
const totalTypeQuestions = computed(() => {
  if (typeDistribution.value.length === 0) return 1
  return typeDistribution.value.reduce((sum, type) => sum + type.count, 0)
})

const typeSegments = computed(() => {
  if (typeDistribution.value.length === 0) return []

  let currentAngle = 0
  const center = 100
  const radius = 80

  return typeDistribution.value.map(type => {
    const percentage = Math.round((type.count / totalTypeQuestions.value) * 100)
    const angle = (type.count / totalTypeQuestions.value) * 2 * Math.PI

    const startAngle = currentAngle
    const endAngle = currentAngle + angle

    const x1 = center + radius * Math.cos(startAngle)
    const y1 = center + radius * Math.sin(startAngle)
    const x2 = center + radius * Math.cos(endAngle)
    const y2 = center + radius * Math.sin(endAngle)

    const largeArcFlag = angle > Math.PI ? 1 : 0

    const path = [
      `M ${center} ${center}`,
      `L ${x1} ${y1}`,
      `A ${radius} ${radius} 0 ${largeArcFlag} 1 ${x2} ${y2}`,
      'Z'
    ].join(' ')

    const labelAngle = startAngle + angle / 2
    const labelRadius = radius * 0.7
    const labelX = center + labelRadius * Math.cos(labelAngle)
    const labelY = center + labelRadius * Math.sin(labelAngle)

    currentAngle += angle

    return {
      path,
      color: getTypeColor(type.type),
      percentage,
      labelX,
      labelY
    }
  })
})

const getTypeColor = (type: string) => {
  const colors: Record<string, string> = {
    single: '#409eff',
    multiple: '#67c23a',
    judge: '#e6a23c',
    essay: '#f56c6c'
  }
  return colors[type] || '#909399'
}

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// 概览统计数据（动态计算）
const overviewStats = computed(() => [
  {
    label: '总答题数',
    value: overviewStatsData.value.totalAnswers.toLocaleString(),
    icon: 'Edit',
    color: '#409eff'
  },
  {
    label: '正确率',
    value: `${overviewStatsData.value.accuracy}%`,
    icon: 'Check',
    color: '#67c23a'
  },
  {
    label: '正确数',
    value: overviewStatsData.value.correctAnswers.toLocaleString(),
    icon: 'Trophy',
    color: '#e6a23c'
  },
  {
    label: '错题数',
    value: overviewStatsData.value.wrongQuestionCount.toLocaleString(),
    icon: 'Warning',
    color: '#f56c6c'
  }
])

// 加载概览统计数据
const loadOverviewStats = async () => {
  try {
    const stats = await statisticsApi.getDetailedStats()

    overviewStatsData.value = {
      totalAnswers: stats.totalAnswers,
      correctAnswers: stats.correctAnswers,
      accuracy: Math.round(stats.accuracy),
      wrongQuestionCount: stats.wrongQuestionCount
    }

  } catch (error) {
    console.error('加载概览统计失败:', error)
    ElMessage.error('加载概览统计失败')
  }
}

// 加载趋势数据
const loadTrendData = async () => {
  try {
    const period = trendPeriod.value === '7d' ? 'week' :
                   trendPeriod.value === '30d' ? 'month' : 'year'

    const data = await statisticsApi.getAnswerTrend({ period })
    trendData.value = data

  } catch (error) {
    console.error('加载趋势数据失败:', error)
    ElMessage.error('加载趋势数据失败')
  }
}

// 加载题型分布数据
const loadTypeDistribution = async () => {
  try {
    const data = await statisticsApi.getTypeStats()
    typeDistribution.value = data.map(item => ({
      type: item.type,
      count: item.count
    }))

  } catch (error) {
    console.error('加载题型分布失败:', error)
    ElMessage.error('加载题型分布失败')
    typeDistribution.value = []
  }
}

// 加载详细数据表格
const loadDetailData = async () => {
  try {
    const stats = await statisticsApi.getDetailedStats()

    // 使用真实的 dailyStats 数据
    detailData.value = stats.dailyStats?.map(item => ({
      date: item.date,
      totalAnswers: item.totalAnswers,
      correctAnswers: item.correctAnswers,
      accuracy: item.accuracy
    })) || []

  } catch (error) {
    console.error('加载详细数据失败:', error)
    ElMessage.error('加载详细数据失败')
    detailData.value = []
  }
}

// 加载所有数据
const loadAllData = async () => {
  try {
    loading.value = true

    await Promise.all([
      loadOverviewStats(),
      loadTrendData(),
      loadTypeDistribution(),
      loadDetailData()
    ])

  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

// 详细数据
const detailData = ref<Array<{
  date: string
  totalAnswers: number
  correctAnswers: number
  accuracy: number
}>>([])

// 初始化 - 不再需要，直接在onMounted中调用loadAllData

// 更新趋势图
const updateTrendChart = async () => {
  try {
    await loadTrendData()
    ElMessage.success(`已切换到${trendPeriod.value === '7d' ? '7天' : trendPeriod.value === '30d' ? '30天' : '90天'}数据`)
  } catch (error) {
    ElMessage.error('更新趋势数据失败')
  }
}

// 导出统计报告
const exportStatistics = async () => {
  try {
    const blob = await statisticsApi.exportStatisticsReport({
      format: 'text'
    })

    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `统计报告_${new Date().toISOString().split('T')[0]}.txt`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('报告导出成功')
  } catch (error) {
    console.error('导出失败:', error)
  }
}

// 工具函数
const getAccuracyTagType = (accuracy: number) => {
  if (accuracy >= 80) return 'success'
  if (accuracy >= 60) return 'warning'
  return 'danger'
}

const getTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    single: '单选题',
    multiple: '多选题',
    judge: '判断题',
    essay: '简答题'
  }
  return labels[type] || type
}

onMounted(() => {
  loadAllData()
})
</script>

<style scoped>
.statistics-container {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-title {
  font-size: 32px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.page-description {
  font-size: 16px;
  color: #606266;
}

.overview-stats {
  margin-bottom: 32px;
}

.stat-card {
  margin-bottom: 16px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.card-header > span {
  margin-left: 8px;
  flex: 1;
}

.chart-card,
.knowledge-card,
.table-card {
  margin-bottom: 24px;
}

.chart-container {
  width: 100%;
  background: #fafafa;
  border-radius: 6px;
  border: 1px dashed #d9d9d9;
}

/* 趋势图样式 */
.trend-chart {
  padding: 20px;
  height: 300px;
}

.chart-header {
  margin-bottom: 16px;
}

.chart-title {
  font-size: 14px;
  color: #666;
}

.trend-line-chart {
  display: flex;
  height: 240px;
}

.y-axis {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  width: 40px;
  padding-right: 10px;
}

.y-label {
  font-size: 12px;
  color: #999;
  text-align: right;
}

.chart-area {
  flex: 1;
  position: relative;
}

.line-chart {
  width: 100%;
  height: 200px;
}

.trend-point {
  cursor: pointer;
  transition: all 0.3s;
}

.trend-point:hover {
  r: 6;
  fill: #1890ff;
}

.x-axis {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
}

.x-label {
  font-size: 12px;
  color: #999;
  text-align: center;
}

/* 正确率环形图样式 */
.accuracy-chart {
  padding: 20px;
  height: 300px;
}

.chart-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  height: 100%;
}

.accuracy-ring {
  position: relative;
  width: 120px;
  height: 120px;
}

.ring-chart {
  width: 100%;
  height: 100%;
}

.progress-ring {
  transition: stroke-dashoffset 0.6s ease;
}

.ring-content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.accuracy-value {
  font-size: 24px;
  font-weight: bold;
  color: #67c23a;
}

.accuracy-label {
  font-size: 12px;
  color: #999;
}

.accuracy-stats {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.stat-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.stat-color.correct {
  background-color: #67c23a;
}

.stat-color.wrong {
  background-color: #f56c6c;
}

.stat-color.total {
  background-color: #409eff;
}

/* 题型分布饼图样式 */
.type-chart {
  padding: 20px;
  height: 250px;
  display: flex;
  gap: 20px;
}

.pie-chart-container {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.pie-chart {
  width: 160px;
  height: 160px;
}

.pie-segment {
  cursor: pointer;
  transition: all 0.3s;
}

.pie-segment:hover {
  opacity: 0.8;
  transform: scale(1.05);
}

.pie-legend {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.legend-label {
  flex: 1;
}

.legend-value {
  font-size: 12px;
  color: #999;
}

/* 难度分析柱状图样式 */
.difficulty-chart {
  padding: 20px;
  height: 250px;
}

.bar-chart-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chart-bars {
  flex: 1;
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  gap: 20px;
  padding: 0 20px 20px;
  border-bottom: 2px solid #f0f0f0;
}

.bar-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.bar-container {
  height: 120px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 4px;
  width: 100%;
}

.bar {
  width: 20px;
  border-radius: 2px 2px 0 0;
  transition: all 0.3s;
  cursor: pointer;
}

.bar:hover {
  opacity: 0.8;
}

.correct-bar {
  background: linear-gradient(to top, #67c23a, #85ce61);
}

.wrong-bar {
  background: linear-gradient(to top, #f56c6c, #f78989);
}

.bar-label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.bar-values {
  display: flex;
  gap: 8px;
  font-size: 12px;
}

.correct-count {
  color: #67c23a;
}

.wrong-count {
  color: #f56c6c;
}

.chart-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 16px;
}

/* 学习成就样式 */
.achievement-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.achievement-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.achievement-icon {
  font-size: 32px;
}

.achievement-info {
  flex: 1;
}

.achievement-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.achievement-desc {
  font-size: 14px;
  color: #606266;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-title {
    font-size: 24px;
  }

  .overview-stats .el-col {
    margin-bottom: 12px;
  }

  .stat-content {
    flex-direction: column;
    text-align: center;
    gap: 8px;
  }

  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
  }

  .chart-container {
    height: 200px !important;
  }

  .knowledge-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}

@media (max-width: 480px) {
  .statistics-container {
    padding: 0 10px;
  }

  .el-table {
    font-size: 12px;
  }
}
</style>
