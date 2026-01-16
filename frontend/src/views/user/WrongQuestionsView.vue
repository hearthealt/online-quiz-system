<template>
  <AppLayout>
    <div class="wrong-questions-page">
      <!-- 统计卡片区域 -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-icon total">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ overallStats.total }}</div>
            <div class="stat-label">总错题</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon error">
            <el-icon><Close /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value error">{{ overallStats.unmastered }}</div>
            <div class="stat-label">未掌握</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon success">
            <el-icon><Check /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value success">{{ overallStats.mastered }}</div>
            <div class="stat-label">已掌握</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon primary">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value primary">{{ overallStats.accuracy }}%</div>
            <div class="stat-label">掌握率</div>
          </div>
        </div>
      </div>

      <!-- 筛选和操作 -->
      <div class="toolbar-section">
        <div class="toolbar-left">
          <el-select
            v-model="selectedBankId"
            placeholder="选择题库"
            clearable
            @change="handleFilterChange"
          >
            <el-option
              v-for="bank in bankStats"
              :key="bank.bankId"
              :label="`${bank.bankName} (${bank.unmasteredCount})`"
              :value="bank.bankId"
            />
          </el-select>
          <el-select
            v-model="filterStatus"
            placeholder="全部状态"
            clearable
            @change="handleFilterChange"
          >
            <el-option label="未掌握" :value="0" />
            <el-option label="已掌握" :value="1" />
          </el-select>
        </div>
        <div class="toolbar-right">
          <el-button
            type="primary"
            :disabled="!selectedBankId || currentBankUnmastered === 0"
            @click="startPractice"
          >
            <el-icon><VideoPlay /></el-icon>
            错题练习
          </el-button>
        </div>
      </div>

      <!-- 错题列表 -->
      <div class="list-section">
        <div v-if="loading" class="loading-wrapper">
          <el-skeleton :rows="5" animated />
        </div>

        <div v-else-if="wrongQuestions.length === 0" class="empty-wrapper">
          <el-empty :description="selectedBankId ? '该题库暂无错题' : '暂无错题记录'">
            <el-button type="primary" @click="$router.push('/banks')">去答题</el-button>
          </el-empty>
        </div>

        <div v-else class="questions-list">
          <div
            v-for="item in wrongQuestions"
            :key="item.id"
            class="question-card"
            @click="viewQuestion(item)"
          >
            <div class="card-left">
              <div class="status-indicator" :class="item.status === 1 ? 'mastered' : 'unmastered'"></div>
            </div>
            <div class="card-content">
              <div class="card-header">
                <div class="header-tags">
                  <span class="type-tag" :class="item.question?.type">
                    {{ getTypeLabel(item.question?.type) }}
                  </span>
                  <span class="bank-name">{{ item.bankName }}</span>
                </div>
                <div class="header-meta">
                  <span class="error-count">错误 {{ item.errorCount || 1 }} 次</span>
                  <span class="status-text" :class="item.status === 1 ? 'mastered' : 'unmastered'">
                    {{ item.status === 1 ? '已掌握' : '未掌握' }}
                  </span>
                </div>
              </div>
              <div class="card-body">
                {{ truncateText(item.question?.content, 150) }}
              </div>
            </div>
            <div class="card-actions">
              <el-button
                v-if="item.status === 0"
                size="small"
                type="success"
                plain
                @click.stop="markAsMastered(item)"
              >
                标记掌握
              </el-button>
              <el-button
                v-else
                size="small"
                type="warning"
                plain
                @click.stop="markAsUnmastered(item)"
              >
                重新学习
              </el-button>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="pagination.total > 0" class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>

    <!-- 题目详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="题目详情"
      width="640px"
      top="8vh"
      class="detail-dialog"
    >
      <div class="detail-content" v-if="viewingQuestion">
        <div class="detail-meta">
          <span class="type-tag" :class="viewingQuestion.question?.type">
            {{ getTypeLabel(viewingQuestion.question?.type) }}
          </span>
          <span class="status-badge" :class="viewingQuestion.status === 1 ? 'mastered' : 'unmastered'">
            {{ viewingQuestion.status === 1 ? '已掌握' : '未掌握' }}
          </span>
          <span class="error-info">累计错误 {{ viewingQuestion.errorCount || 1 }} 次</span>
        </div>

        <div class="detail-block">
          <div class="block-title">题目内容</div>
          <div class="block-content question-text">{{ viewingQuestion.question?.content }}</div>
        </div>

        <div class="detail-block" v-if="viewingQuestion.question?.options?.length">
          <div class="block-title">选项</div>
          <div class="options-grid">
            <div
              v-for="(option, index) in viewingQuestion.question.options"
              :key="index"
              class="option-item"
              :class="{ correct: isCorrectOption(index) }"
            >
              <span class="option-letter" :class="{ correct: isCorrectOption(index) }">
                {{ String.fromCharCode(65 + index) }}
              </span>
              <span class="option-text">{{ option }}</span>
            </div>
          </div>
        </div>

        <div class="answer-row">
          <div class="answer-item">
            <div class="answer-label">正确答案</div>
            <div class="answer-value correct">{{ formatAnswer(viewingQuestion.question) }}</div>
          </div>
          <div class="answer-item" v-if="viewingQuestion.lastErrorAnswer">
            <div class="answer-label">你的答案</div>
            <div class="answer-value wrong">{{ formatUserAnswer(viewingQuestion) }}</div>
          </div>
        </div>

        <div class="detail-block" v-if="viewingQuestion.question?.analysis">
          <div class="block-title">答案解析</div>
          <div class="block-content analysis">{{ viewingQuestion.question.analysis }}</div>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="viewingQuestion?.status === 0"
          type="success"
          @click="markAsMasteredAndClose"
        >
          标记为已掌握
        </el-button>
      </template>
    </el-dialog>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, Close, Check, TrendCharts, VideoPlay } from '@element-plus/icons-vue'
import AppLayout from '@/components/common/AppLayout.vue'
import type { WrongQuestion, Question } from '@/types/index'
import * as wrongQuestionApi from '@/api/wrongQuestion'

const router = useRouter()

// 数据状态
const loading = ref(false)
const wrongQuestions = ref<(WrongQuestion & { question?: Question, bankName?: string })[]>([])

// 筛选
const selectedBankId = ref<number | null>(null)
const filterStatus = ref<number | null>(null)

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 统计数据
const overallStats = ref({ total: 0, mastered: 0, unmastered: 0, accuracy: 0 })
const bankStats = ref<{ bankId: number, bankName: string, total: number, masteredCount: number, unmasteredCount: number }[]>([])

// 当前选中题库的未掌握数
const currentBankUnmastered = computed(() => {
  if (!selectedBankId.value) return overallStats.value.unmastered
  const bank = bankStats.value.find(b => b.bankId === selectedBankId.value)
  return bank?.unmasteredCount || 0
})

// 详情弹窗
const detailDialogVisible = ref(false)
const viewingQuestion = ref<(WrongQuestion & { question?: Question }) | null>(null)

// 获取总体统计
const fetchOverallStats = async () => {
  try {
    overallStats.value = await wrongQuestionApi.getWrongQuestionOverallStats()
  } catch (error) {
    console.error('获取统计失败:', error)
  }
}

// 获取按题库分组的统计
const fetchBankStats = async () => {
  try {
    bankStats.value = await wrongQuestionApi.getWrongQuestionStatsByBank()
  } catch (error) {
    console.error('获取题库统计失败:', error)
  }
}

// 获取错题列表
const fetchWrongQuestions = async () => {
  try {
    loading.value = true
    const result = await wrongQuestionApi.getWrongQuestions({
      current: pagination.current,
      size: pagination.size,
      status: filterStatus.value ?? undefined,
      bankId: selectedBankId.value ?? undefined
    })
    wrongQuestions.value = result.records || []
    pagination.total = result.total || 0
  } catch (error) {
    console.error('获取错题列表失败:', error)
    ElMessage.error('获取错题列表失败')
  } finally {
    loading.value = false
  }
}

// 筛选变化
const handleFilterChange = () => {
  pagination.current = 1
  fetchWrongQuestions()
}

// 分页变化
const handlePageChange = (page: number) => {
  pagination.current = page
  fetchWrongQuestions()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchWrongQuestions()
}

// 开始错题练习
const startPractice = () => {
  if (selectedBankId.value) {
    router.push(`/practice/${selectedBankId.value}?mode=wrong`)
  }
}

// 查看题目详情
const viewQuestion = (item: WrongQuestion & { question?: Question }) => {
  viewingQuestion.value = item
  detailDialogVisible.value = true
}

// 标记为已掌握
const markAsMastered = async (item: WrongQuestion & { question?: Question }) => {
  try {
    await wrongQuestionApi.markWrongQuestionAsMastered(item.questionId)
    item.status = 1
    await fetchOverallStats()
    await fetchBankStats()
    ElMessage.success('已标记为掌握')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 标记为未掌握
const markAsUnmastered = async (item: WrongQuestion & { question?: Question }) => {
  try {
    await wrongQuestionApi.markWrongQuestionAsUnmastered(item.questionId)
    item.status = 0
    await fetchOverallStats()
    await fetchBankStats()
    ElMessage.success('已标记为未掌握')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 标记并关闭弹窗
const markAsMasteredAndClose = async () => {
  if (viewingQuestion.value) {
    await markAsMastered(viewingQuestion.value)
    detailDialogVisible.value = false
  }
}

// 判断是否为正确选项
const isCorrectOption = (index: number) => {
  if (!viewingQuestion.value?.question) return false
  const correctAnswer = viewingQuestion.value.question.correctAnswer || ''
  const optionLabel = String.fromCharCode(65 + index)
  return correctAnswer.split(',').includes(optionLabel)
}

// 格式化答案
const formatAnswer = (question?: Question) => {
  if (!question) return ''
  if (question.type === 'judge') {
    return question.correctAnswer === 'true' ? '正确' : '错误'
  }
  return question.correctAnswer
}

// 格式化用户答案
const formatUserAnswer = (item: WrongQuestion & { question?: Question }) => {
  if (!item.lastErrorAnswer) return ''
  if (item.question?.type === 'judge') {
    return item.lastErrorAnswer === 'true' ? '正确' : '错误'
  }
  return item.lastErrorAnswer
}

// 截断文本
const truncateText = (text?: string, max: number = 100) => {
  if (!text) return ''
  return text.length > max ? text.substring(0, max) + '...' : text
}

// 题型标签
const getTypeLabel = (type?: string) => {
  const labels: Record<string, string> = {
    single: '单选题', multiple: '多选题', judge: '判断题', essay: '简答题'
  }
  return labels[type || ''] || type
}

onMounted(async () => {
  await Promise.all([
    fetchOverallStats(),
    fetchBankStats(),
    fetchWrongQuestions()
  ])
})
</script>

<style scoped>
.wrong-questions-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px 20px;
}

/* 统计卡片 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.stat-icon.total {
  background: #f0f7ff;
  color: #409eff;
}

.stat-icon.error {
  background: #fef0f0;
  color: #f56c6c;
}

.stat-icon.success {
  background: #f0f9eb;
  color: #67c23a;
}

.stat-icon.primary {
  background: #ecf5ff;
  color: #409eff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-value.error { color: #f56c6c; }
.stat-value.success { color: #67c23a; }
.stat-value.primary { color: #409eff; }

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

/* 工具栏 */
.toolbar-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.toolbar-left {
  display: flex;
  gap: 12px;
}

.toolbar-left :deep(.el-select) {
  width: 180px;
}

.toolbar-right {
  display: flex;
  gap: 12px;
}

/* 列表区域 */
.list-section {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  min-height: 400px;
}

.loading-wrapper,
.empty-wrapper {
  padding: 80px 20px;
}

.questions-list {
  padding: 8px;
}

.question-card {
  display: flex;
  align-items: stretch;
  padding: 16px;
  margin: 8px;
  border-radius: 10px;
  background: #fafafa;
  cursor: pointer;
  transition: all 0.2s;
}

.question-card:hover {
  background: #f5f7fa;
  transform: translateY(-1px);
}

.card-left {
  display: flex;
  align-items: stretch;
  padding-right: 16px;
}

.status-indicator {
  width: 4px;
  border-radius: 2px;
}

.status-indicator.unmastered {
  background: #f56c6c;
}

.status-indicator.mastered {
  background: #67c23a;
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  flex-wrap: wrap;
  gap: 8px;
}

.header-tags {
  display: flex;
  align-items: center;
  gap: 10px;
}

.type-tag {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 4px;
  font-weight: 500;
}

.type-tag.single {
  background: #ecf5ff;
  color: #409eff;
}

.type-tag.multiple {
  background: #f0f9eb;
  color: #67c23a;
}

.type-tag.judge {
  background: #fdf6ec;
  color: #e6a23c;
}

.type-tag.essay {
  background: #f4f4f5;
  color: #909399;
}

.bank-name {
  font-size: 13px;
  color: #606266;
}

.header-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.error-count {
  font-size: 12px;
  color: #909399;
}

.status-text {
  font-size: 12px;
  font-weight: 500;
}

.status-text.unmastered {
  color: #f56c6c;
}

.status-text.mastered {
  color: #67c23a;
}

.card-body {
  font-size: 14px;
  color: #606266;
  line-height: 1.7;
}

.card-actions {
  display: flex;
  align-items: center;
  margin-left: 16px;
  flex-shrink: 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px;
  border-top: 1px solid #f0f0f0;
}

/* 详情弹窗 */
.detail-content {
  padding: 0 4px;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.status-badge {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: 500;
}

.status-badge.unmastered {
  background: #fef0f0;
  color: #f56c6c;
}

.status-badge.mastered {
  background: #f0f9eb;
  color: #67c23a;
}

.error-info {
  font-size: 13px;
  color: #909399;
  margin-left: auto;
}

.detail-block {
  margin-bottom: 24px;
}

.block-title {
  font-size: 13px;
  font-weight: 500;
  color: #909399;
  margin-bottom: 10px;
}

.block-content {
  font-size: 15px;
  color: #303133;
  line-height: 1.8;
}

.block-content.question-text {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.block-content.analysis {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  color: #606266;
}

.options-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: background 0.2s;
}

.option-item.correct {
  background: #f0f9eb;
}

.option-letter {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 13px;
  color: #606266;
  margin-right: 12px;
  flex-shrink: 0;
}

.option-letter.correct {
  background: #67c23a;
  color: #fff;
}

.option-text {
  flex: 1;
  font-size: 14px;
  color: #303133;
}

.answer-row {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.answer-item {
  flex: 1;
}

.answer-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.answer-value {
  font-size: 16px;
  font-weight: 600;
}

.answer-value.correct {
  color: #67c23a;
}

.answer-value.wrong {
  color: #f56c6c;
}

/* 响应式 */
@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .stat-card {
    padding: 16px;
  }

  .stat-value {
    font-size: 24px;
  }

  .toolbar-section {
    flex-direction: column;
    gap: 12px;
    padding: 16px;
  }

  .toolbar-left {
    width: 100%;
    flex-direction: column;
  }

  .toolbar-left :deep(.el-select) {
    width: 100%;
  }

  .toolbar-right {
    width: 100%;
  }

  .toolbar-right .el-button {
    width: 100%;
  }

  .question-card {
    flex-direction: column;
    gap: 12px;
  }

  .card-left {
    display: none;
  }

  .card-actions {
    margin-left: 0;
    width: 100%;
    justify-content: flex-end;
  }

  .answer-row {
    flex-direction: column;
    gap: 16px;
  }
}
</style>
