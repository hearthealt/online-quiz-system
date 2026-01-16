<template>
  <AppLayout>
    <div class="favorites-page">
      <!-- 统计区域 -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-icon primary">
            <el-icon><Star /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ pagination.total }}</div>
            <div class="stat-label">收藏总数</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon success">
            <el-icon><FolderOpened /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value success">{{ bankStats.length }}</div>
            <div class="stat-label">题库数量</div>
          </div>
        </div>
      </div>

      <!-- 筛选和操作区域 -->
      <div class="toolbar-section">
        <div class="toolbar-left">
          <el-select
            v-model="selectedBankId"
            placeholder="选择题库"
            clearable
            @change="handleBankChange"
          >
            <el-option
              v-for="bank in bankStats"
              :key="bank.bankId"
              :label="`${bank.bankName} (${bank.count})`"
              :value="bank.bankId"
            />
          </el-select>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索题目..."
            clearable
            @keyup.enter="searchFavorites"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        <div class="toolbar-right">
          <el-button
            type="primary"
            :disabled="!selectedBankId"
            @click="practiceFavorites"
          >
            <el-icon><VideoPlay /></el-icon>
            收藏练习
          </el-button>
          <el-button
            v-if="favorites && favorites.length > 0"
            @click="toggleSelectAll"
          >
            {{ isAllSelected ? '取消全选' : '全选' }}
          </el-button>
          <el-button
            v-if="selectedFavorites.length > 0"
            type="danger"
            plain
            @click="batchRemoveFavorites"
          >
            批量取消 ({{ selectedFavorites.length }})
          </el-button>
        </div>
      </div>

      <!-- 收藏列表 -->
      <div class="list-section">
        <div v-if="loading" class="loading-wrapper">
          <el-skeleton :rows="5" animated />
        </div>

        <div v-else-if="!favorites || favorites.length === 0" class="empty-wrapper">
          <el-empty description="暂无收藏题目">
            <el-button type="primary" @click="$router.push('/banks')">
              去题库看看
            </el-button>
          </el-empty>
        </div>

        <div v-else class="favorites-list">
          <div
            v-for="favorite in favorites"
            :key="favorite.id"
            class="favorite-card"
            :class="{ selected: selectedFavorites.includes(favorite.id!) }"
          >
            <div
              class="card-checkbox"
              @click.stop="selectFavorite(favorite.id!)"
            >
              <div class="checkbox-inner" :class="{ checked: selectedFavorites.includes(favorite.id!) }">
                <el-icon v-if="selectedFavorites.includes(favorite.id!)"><Check /></el-icon>
              </div>
            </div>

            <div class="card-content" @click="viewQuestion(favorite)">
              <div class="card-header">
                <div class="header-tags">
                  <span class="type-tag" :class="favorite.question?.type">
                    {{ getTypeLabel(favorite.question?.type) }}
                  </span>
                  <span class="bank-name" v-if="favorite.bankName">{{ favorite.bankName }}</span>
                </div>
                <div class="header-time">
                  {{ formatDate(favorite.createdAt) }}
                </div>
              </div>
              <div class="card-body">
                {{ truncateText(favorite.question?.content || '', 150) }}
              </div>
              <div v-if="favorite.notes" class="card-notes">
                <el-icon><EditPen /></el-icon>
                <span>{{ favorite.notes }}</span>
              </div>
            </div>

            <div class="card-actions">
              <el-button
                size="small"
                type="primary"
                plain
                @click.stop="viewQuestion(favorite)"
              >
                查看
              </el-button>
              <el-button
                size="small"
                @click.stop="editNotes(favorite)"
              >
                备注
              </el-button>
              <el-button
                size="small"
                type="danger"
                plain
                @click.stop="removeFavorite(favorite)"
              >
                取消
              </el-button>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="favorites && favorites.length > 0" class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :page-sizes="[5, 8, 10, 12]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>

    <!-- 编辑备注对话框 -->
    <el-dialog
      v-model="notesDialogVisible"
      title="编辑收藏备注"
      width="480px"
      @close="resetNotesDialog"
    >
      <div class="notes-dialog-content">
        <div class="question-preview-box">
          <div class="preview-label">题目</div>
          <div class="preview-text">
            {{ truncateText(currentFavorite?.question?.content || '', 100) }}
          </div>
        </div>
        <div class="notes-input-box">
          <div class="input-label">备注</div>
          <el-input
            v-model="notesForm.notes"
            type="textarea"
            :rows="4"
            placeholder="为这道题添加一些备注..."
            maxlength="200"
            show-word-limit
          />
        </div>
      </div>

      <template #footer>
        <el-button @click="notesDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveNotes" :loading="savingNotes">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 题目详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="题目详情"
      width="640px"
      top="8vh"
    >
      <div class="detail-content" v-if="viewingFavorite">
        <div class="detail-meta">
          <span class="type-tag" :class="viewingFavorite.question?.type">
            {{ getTypeLabel(viewingFavorite.question?.type) }}
          </span>
          <span class="favorite-time">
            收藏于 {{ formatDate(viewingFavorite.createdAt) }}
          </span>
        </div>

        <div class="detail-block">
          <div class="block-title">题目内容</div>
          <div class="block-content question-text">{{ viewingFavorite.question?.content }}</div>
        </div>

        <div class="detail-block" v-if="viewingFavorite.question?.options?.length">
          <div class="block-title">选项</div>
          <div class="options-grid">
            <div
              v-for="(option, index) in viewingFavorite.question.options"
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

        <div class="detail-block">
          <div class="block-title">正确答案</div>
          <div class="answer-value">{{ formatAnswer(viewingFavorite.question) }}</div>
        </div>

        <div class="detail-block" v-if="viewingFavorite.question?.analysis">
          <div class="block-title">答案解析</div>
          <div class="block-content analysis">{{ viewingFavorite.question.analysis }}</div>
        </div>

        <div class="detail-block" v-if="viewingFavorite.notes">
          <div class="block-title">收藏备注</div>
          <div class="block-content notes">{{ viewingFavorite.notes }}</div>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button @click="editNotesFromDetail">编辑备注</el-button>
        <el-button type="danger" plain @click="removeFavoriteFromDetail">取消收藏</el-button>
      </template>
    </el-dialog>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Check, Star, FolderOpened, VideoPlay, EditPen } from '@element-plus/icons-vue'
import AppLayout from '@/components/common/AppLayout.vue'
import type { Favorite, Question } from '@/types/index'
import * as favoriteApi from '@/api/favorite'

const router = useRouter()

// 数据状态
const loading = ref(false)
const favorites = ref<(Favorite & { question?: Question, bankName?: string })[]>([])
const selectedFavorites = ref<number[]>([])
const searchKeyword = ref('')
const selectedBankId = ref<number | null>(null)
const bankStats = ref<{ bankId: number, bankName: string, count: number }[]>([])

// 计算属性
const isAllSelected = computed(() => {
  return favorites.value.length > 0 && selectedFavorites.value.length === favorites.value.length
})

// 分页参数
const pagination = reactive({
  current: 1,
  size: 8,
  total: 0
})

// 编辑备注相关
const notesDialogVisible = ref(false)
const savingNotes = ref(false)
const currentFavorite = ref<Favorite & { question?: Question } | null>(null)
const notesForm = reactive({
  notes: ''
})

// 题目详情弹窗相关
const detailDialogVisible = ref(false)
const viewingFavorite = ref<Favorite & { question?: Question } | null>(null)

// 获取收藏列表
const fetchFavorites = async () => {
  try {
    loading.value = true

    const result = await favoriteApi.getFavorites({
      current: pagination.current,
      size: pagination.size,
      keyword: searchKeyword.value || undefined
    })

    favorites.value = result.records || []
    pagination.total = result.total || 0

  } catch (error) {
    console.error('获取收藏列表失败:', error)
    ElMessage.error('获取收藏列表失败')
    favorites.value = []
  } finally {
    loading.value = false
  }
}

// 搜索收藏
const searchFavorites = () => {
  pagination.current = 1
  fetchFavorites()
}

// 获取题库统计
const fetchBankStats = async () => {
  try {
    const stats = await favoriteApi.getFavoriteStatsByBank()
    bankStats.value = stats || []
  } catch (error) {
    console.error('获取题库统计失败:', error)
  }
}

// 题库筛选变化
const handleBankChange = () => {
  pagination.current = 1
}

// 分页处理
const handlePageChange = (page: number) => {
  pagination.current = page
  fetchFavorites()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchFavorites()
}

// 选择收藏
const selectFavorite = (id: number) => {
  const index = selectedFavorites.value.indexOf(id)
  if (index > -1) {
    selectedFavorites.value.splice(index, 1)
  } else {
    selectedFavorites.value.push(id)
  }
}

// 全选/取消全选
const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedFavorites.value = []
  } else {
    selectedFavorites.value = favorites.value.map(f => f.id!).filter(id => id !== undefined)
  }
}

// 练习收藏题目
const practiceFavorites = () => {
  if (!selectedBankId.value) {
    ElMessage.warning('请先选择要练习的题库')
    return
  }
  router.push(`/practice/${selectedBankId.value}?mode=favorite`)
}

// 查看题目详情
const viewQuestion = (favorite: Favorite & { question?: Question }) => {
  viewingFavorite.value = favorite
  detailDialogVisible.value = true
}

// 判断是否为正确选项
const isCorrectOption = (index: number) => {
  if (!viewingFavorite.value?.question) return false
  const correctAnswer = viewingFavorite.value.question.correctAnswer || ''
  const optionLabel = String.fromCharCode(65 + index)
  return correctAnswer.split(',').includes(optionLabel)
}

// 格式化答案
const formatAnswer = (question?: Question) => {
  if (!question) return ''
  if (question.type === 'judge') {
    return question.correctAnswer === 'true' ? '正确' : '错误'
  }
  if (question.type === 'multiple') {
    return question.correctAnswer?.split(',').join('、')
  }
  return question.correctAnswer
}

// 从详情弹窗编辑备注
const editNotesFromDetail = () => {
  if (viewingFavorite.value) {
    detailDialogVisible.value = false
    editNotes(viewingFavorite.value)
  }
}

// 从详情弹窗取消收藏
const removeFavoriteFromDetail = async () => {
  if (viewingFavorite.value) {
    await removeFavorite(viewingFavorite.value)
    detailDialogVisible.value = false
  }
}

// 编辑备注
const editNotes = (favorite: Favorite & { question?: Question }) => {
  currentFavorite.value = favorite
  notesForm.notes = favorite.notes || ''
  notesDialogVisible.value = true
}

// 保存备注
const saveNotes = async () => {
  if (!currentFavorite.value?.id) return

  try {
    savingNotes.value = true

    await favoriteApi.updateFavoriteNotes(currentFavorite.value.id, notesForm.notes)

    if (favorites.value) {
      const index = favorites.value.findIndex(f => f.id === currentFavorite.value?.id)
      if (index > -1) {
        favorites.value[index].notes = notesForm.notes
      }
    }

    ElMessage.success('备注更新成功')
    notesDialogVisible.value = false

  } catch (error) {
    console.error('更新备注失败:', error)
    ElMessage.error('更新备注失败')
  } finally {
    savingNotes.value = false
  }
}

// 重置备注对话框
const resetNotesDialog = () => {
  currentFavorite.value = null
  notesForm.notes = ''
}

// 取消收藏
const removeFavorite = async (favorite: Favorite & { question?: Question }) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消收藏这道题吗？`,
      '确认取消收藏',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    if (!favorite.id) return

    await favoriteApi.removeFavorite(favorite.id)

    if (favorites.value) {
      const index = favorites.value.findIndex(f => f.id === favorite.id)
      if (index > -1) {
        favorites.value.splice(index, 1)
        pagination.total--
      }
    }

    ElMessage.success('已取消收藏')

  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('取消收藏失败:', error)
      ElMessage.error('取消收藏失败')
    }
  }
}

// 批量取消收藏
const batchRemoveFavorites = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要取消收藏选中的 ${selectedFavorites.value.length} 个题目吗？`,
      '批量取消收藏',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await favoriteApi.batchRemoveFavorites(selectedFavorites.value)

    if (favorites.value) {
      favorites.value = favorites.value.filter(f => !selectedFavorites.value.includes(f.id!))
      pagination.total -= selectedFavorites.value.length
      selectedFavorites.value = []
    }

    ElMessage.success('已批量取消收藏')

  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('批量取消收藏失败:', error)
      ElMessage.error('批量取消收藏失败')
    }
  }
}

// 工具函数
const getTypeLabel = (type?: string) => {
  const labels: Record<string, string> = {
    single: '单选题',
    multiple: '多选题',
    judge: '判断题',
    essay: '简答题'
  }
  return labels[type || ''] || type
}

const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

const truncateText = (text: string, maxLength: number) => {
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

onMounted(() => {
  fetchFavorites()
  fetchBankStats()
})
</script>

<style scoped>
.favorites-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px 20px;
}

/* 统计卡片 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 20px;
  max-width: 500px;
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

.stat-icon.primary {
  background: #fdf6ec;
  color: #e6a23c;
}

.stat-icon.success {
  background: #f0f9eb;
  color: #67c23a;
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

.stat-value.success {
  color: #67c23a;
}

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
  gap: 16px;
  flex-wrap: wrap;
}

.toolbar-left {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-left :deep(.el-select) {
  width: 180px;
}

.toolbar-left :deep(.el-input) {
  width: 200px;
}

.toolbar-right {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
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

.favorites-list {
  padding: 8px;
}

.favorite-card {
  display: flex;
  align-items: stretch;
  padding: 16px;
  margin: 8px;
  border-radius: 10px;
  background: #fafafa;
  transition: all 0.2s;
}

.favorite-card:hover {
  background: #f5f7fa;
}

.favorite-card.selected {
  background: #f0f7ff;
}

.card-checkbox {
  display: flex;
  align-items: center;
  padding-right: 16px;
  cursor: pointer;
}

.checkbox-inner {
  width: 20px;
  height: 20px;
  border: 2px solid #dcdfe6;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.checkbox-inner:hover {
  border-color: #409eff;
}

.checkbox-inner.checked {
  background: #409eff;
  border-color: #409eff;
  color: #fff;
  font-size: 12px;
}

.card-content {
  flex: 1;
  min-width: 0;
  cursor: pointer;
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

.header-time {
  font-size: 12px;
  color: #909399;
}

.card-body {
  font-size: 14px;
  color: #606266;
  line-height: 1.7;
  margin-bottom: 8px;
}

.card-notes {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
  padding: 8px 12px;
  background: #fff;
  border-radius: 6px;
}

.card-notes .el-icon {
  font-size: 14px;
  color: #c0c4cc;
}

.card-actions {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
  margin-left: 16px;
  flex-shrink: 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px;
  border-top: 1px solid #f0f0f0;
}

/* 备注对话框 */
.notes-dialog-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.question-preview-box,
.notes-input-box {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preview-label,
.input-label {
  font-size: 13px;
  font-weight: 500;
  color: #606266;
}

.preview-text {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
  color: #606266;
  line-height: 1.6;
  font-size: 14px;
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

.favorite-time {
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

.block-content.notes {
  padding: 16px;
  background: #f0f7ff;
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

.answer-value {
  font-size: 16px;
  font-weight: 600;
  color: #67c23a;
}

/* 响应式 */
@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: 1fr;
    max-width: none;
  }

  .toolbar-section {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-left,
  .toolbar-right {
    width: 100%;
  }

  .toolbar-left :deep(.el-select),
  .toolbar-left :deep(.el-input) {
    width: 100%;
  }

  .favorite-card {
    flex-direction: column;
    gap: 12px;
  }

  .card-checkbox {
    position: absolute;
    top: 16px;
    right: 16px;
    padding: 0;
  }

  .favorite-card {
    position: relative;
    padding-right: 48px;
  }

  .card-actions {
    margin-left: 0;
    flex-direction: row;
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
