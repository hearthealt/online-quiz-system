<template>
  <AppLayout>
    <div class="bank-detail-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-left">
          <el-button link @click="goBack" class="back-btn">
            <el-icon><ArrowLeft /></el-icon>
            返回题库列表
          </el-button>
          <div class="bank-info" v-if="bank">
            <div class="bank-icon" :style="{ background: getBankColor(bank.id!) }">
              <el-icon :size="24"><FolderOpened /></el-icon>
            </div>
            <div class="bank-meta">
              <h1 class="bank-title">{{ bank.name }}</h1>
              <p class="bank-desc">{{ bank.description || '暂无描述' }}</p>
            </div>
          </div>
        </div>
        <div class="header-right">
          <el-button @click="showAddDialog" v-if="canCreate">
            <el-icon><Plus /></el-icon>
            添加题目
          </el-button>
          <el-button type="primary" @click="showImportDialog" v-if="canUpload">
            <el-icon><Upload /></el-icon>
            导入题目
          </el-button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <el-row :gutter="16" class="stats-row">
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <div class="stat-icon total">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.total }}</div>
              <div class="stat-label"></div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <div class="stat-icon single">
              <el-icon><Select /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.single }}</div>
              <div class="stat-label">单选题</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <div class="stat-icon multiple">
              <el-icon><Finished /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.multiple }}</div>
              <div class="stat-label">多选题</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <div class="stat-icon judge">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.judge }}</div>
              <div class="stat-label">判断题</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 搜索筛选 -->
      <el-card class="filter-card" shadow="never">
        <div class="filter-row">
          <el-select v-model="filterType" placeholder="题目类型" clearable style="width: 140px">
            <el-option label="单选题" value="single" />
            <el-option label="多选题" value="multiple" />
            <el-option label="判断题" value="judge" />
            <el-option label="简答题" value="essay" />
          </el-select>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索题目内容..."
            clearable
            style="width: 300px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>

          <div class="filter-right" v-if="selectedIds.length > 0">
            <el-button type="danger" @click="batchDelete">
              <el-icon><Delete /></el-icon>
              删除选中 ({{ selectedIds.length }})
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 题目列表 -->
      <el-card class="question-card" shadow="never">
        <el-table
          ref="tableRef"
          :data="questions"
          v-loading="loading"
          @selection-change="handleSelectionChange"
          stripe
          style="width: 100%"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column label="序号" width="70" align="center">
            <template #default="{ $index }">
              {{ (currentPage - 1) * pageSize + $index + 1 }}
            </template>
          </el-table-column>
          <el-table-column label="题目类型" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getTypeTagType(row.type)" size="small">
                {{ getTypeLabel(row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="题目内容" min-width="400">
            <template #default="{ row }">
              <div class="question-content">
                <div class="question-text">{{ truncateText(row.content, 120) }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="答案" width="150">
            <template #default="{ row }">
              <el-tooltip :content="row.correctAnswer" placement="top" :disabled="!row.correctAnswer || row.correctAnswer.length < 20">
                <span class="answer-text">{{ truncateText(row.correctAnswer, 20) }}</span>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="viewQuestion(row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button link type="warning" size="small" @click="editQuestion(row)" v-if="canEdit">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button link type="danger" size="small" @click="deleteQuestion(row)" v-if="canDelete">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 添加/编辑题目对话框 -->
    <el-dialog
      v-model="questionDialogVisible"
      :title="isEditQuestion ? '编辑题目' : '添加题目'"
      width="800px"
      :close-on-click-modal="false"
      top="5vh"
    >
      <el-form
        ref="questionFormRef"
        :model="questionForm"
        :rules="questionRules"
        label-width="100px"
      >
        <el-form-item label="题目类型" prop="type">
          <el-select v-model="questionForm.type" placeholder="选择题目类型" style="width: 200px">
            <el-option label="单选题" value="single" />
            <el-option label="多选题" value="multiple" />
            <el-option label="判断题" value="judge" />
            <el-option label="简答题" value="essay" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容" prop="content">
          <el-input
            v-model="questionForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入题目内容"
          />
        </el-form-item>

        <!-- 选项 -->
        <template v-if="questionForm.type === 'single' || questionForm.type === 'multiple'">
          <el-form-item
            v-for="(option, index) in questionForm.options"
            :key="index"
            :label="'选项' + String.fromCharCode(65 + index)"
            :prop="'options.' + index"
            :rules="index < 2 ? [{ required: true, message: '请输入选项', trigger: 'blur' }] : []"
          >
            <div style="display: flex; gap: 8px;">
              <el-input v-model="questionForm.options[index]" :placeholder="'选项' + String.fromCharCode(65 + index)" />
              <el-button v-if="index >= 2" type="danger" :icon="Delete" circle @click="removeOption(index)" />
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" link @click="addOption">
              <el-icon><Plus /></el-icon>
              添加选项
            </el-button>
          </el-form-item>
        </template>

        <el-form-item label="正确答案" prop="correctAnswer">
          <el-input
            v-model="questionForm.correctAnswer"
            :type="questionForm.type === 'essay' ? 'textarea' : 'text'"
            :rows="questionForm.type === 'essay' ? 3 : 1"
            :placeholder="getAnswerPlaceholder()"
          />
        </el-form-item>

        <el-form-item label="答案解析">
          <el-input
            v-model="questionForm.analysis"
            type="textarea"
            :rows="3"
            placeholder="答案解析（选填）"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="questionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitQuestion" :loading="submitting">
          {{ isEditQuestion ? '保存修改' : '添加题目' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 导入题目对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="导入题目"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-alert type="info" :closable="false" style="margin-bottom: 20px">
        <template #title>
          <div style="line-height: 1.8;">
            <p>导入的题目将添加到当前题库「{{ bank?.name }}」中</p>
            <p><strong>文件要求：</strong>支持 Excel (.xlsx/.xls) 格式</p>
            <el-button type="primary" size="small" text @click="downloadTemplate">
              <el-icon><Download /></el-icon>
              下载导入模板
            </el-button>
          </div>
        </template>
      </el-alert>

      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :show-file-list="true"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleFileChange"
        drag
        class="upload-area"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
      </el-upload>

      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmImport" :loading="importing" :disabled="!selectedFile">
          开始导入
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看题目详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="题目详情"
      width="700px"
    >
      <div class="question-detail" v-if="viewingQuestion">
        <div class="detail-row">
          <span class="detail-label">题目类型：</span>
          <el-tag :type="getTypeTagType(viewingQuestion.type)">
            {{ getTypeLabel(viewingQuestion.type) }}
          </el-tag>
        </div>
        <div class="detail-row">
          <span class="detail-label">题目内容：</span>
          <div class="detail-content">{{ viewingQuestion.content }}</div>
        </div>
        <template v-if="viewingQuestion.type === 'single' || viewingQuestion.type === 'multiple'">
          <div class="detail-row">
            <span class="detail-label">选项：</span>
            <div class="options-list">
              <div v-for="(option, index) in viewingQuestion.options" :key="index">
                {{ String.fromCharCode(65 + index) }}. {{ option }}
              </div>
            </div>
          </div>
        </template>
        <div class="detail-row">
          <span class="detail-label">正确答案：</span>
          <span class="answer-highlight">{{ viewingQuestion.correctAnswer }}</span>
        </div>
        <div class="detail-row" v-if="viewingQuestion.analysis">
          <span class="detail-label">答案解析：</span>
          <div class="detail-content">{{ viewingQuestion.analysis }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="editFromView" v-if="canEdit">编辑</el-button>
      </template>
    </el-dialog>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  ArrowLeft, FolderOpened, Plus, Upload, Search, Document,
  Select, Finished, CircleCheck, View, Edit, Delete, UploadFilled, Download
} from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import AppLayout from '@/components/common/AppLayout.vue'
import { useUserStore } from '@/store/user'
import * as bankApi from '@/api/bank'
import * as questionApi from '@/api/question'
import type { QuestionBank, Question, QuestionType } from '@/types/index'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const bankId = computed(() => Number(route.params.bankId))

// 权限
const canCreate = computed(() => userStore.hasPermission('question:create'))
const canEdit = computed(() => userStore.hasPermission('question:edit'))
const canDelete = computed(() => userStore.hasPermission('question:delete'))
const canUpload = computed(() => userStore.hasPermission('question:upload'))

// 题库信息
const bank = ref<QuestionBank | null>(null)
const stats = reactive({ total: 0, single: 0, multiple: 0, judge: 0, essay: 0 })

// 题目列表
const loading = ref(false)
const questions = ref<Question[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const filterType = ref('')
const searchKeyword = ref('')
const selectedIds = ref<number[]>([])

// 题目表单
const questionDialogVisible = ref(false)
const isEditQuestion = ref(false)
const questionFormRef = ref<FormInstance>()
const submitting = ref(false)
const questionForm = reactive({
  id: 0,
  type: 'single' as QuestionType,
  content: '',
  options: ['', ''] as string[],
  correctAnswer: '',
  analysis: ''
})

const questionRules: FormRules = {
  type: [{ required: true, message: '请选择题目类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
  correctAnswer: [{ required: true, message: '请输入正确答案', trigger: 'blur' }]
}

// 导入相关
const importDialogVisible = ref(false)
const uploadRef = ref()
const selectedFile = ref<File | null>(null)
const importing = ref(false)

// 查看详情
const viewDialogVisible = ref(false)
const viewingQuestion = ref<Question | null>(null)

// 获取题库信息
const fetchBank = async () => {
  try {
    const result = await bankApi.getBank(bankId.value)
    bank.value = result
  } catch (error) {
    ElMessage.error('获取题库信息失败')
  }
}

// 获取统计信息
const fetchStats = async () => {
  try {
    const result = await bankApi.getBankStats(bankId.value)
    stats.total = result.totalQuestions || 0
    stats.single = result.singleCount || 0
    stats.multiple = result.multipleCount || 0
    stats.judge = result.judgeCount || 0
    stats.essay = result.essayCount || 0
  } catch (error) {
    console.error('获取统计信息失败:', error)
  }
}

// 获取题目列表
const fetchQuestions = async () => {
  loading.value = true
  try {
    const result = await questionApi.getQuestions({
      current: currentPage.value,
      size: pageSize.value,
      bankId: bankId.value,
      type: filterType.value as QuestionType || undefined,
      keyword: searchKeyword.value || undefined
    })
    questions.value = result.records || []
    total.value = result.total || 0
  } catch (error) {
    ElMessage.error('获取题目列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchQuestions()
}

const resetSearch = () => {
  filterType.value = ''
  searchKeyword.value = ''
  currentPage.value = 1
  fetchQuestions()
}

// 分页
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchQuestions()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchQuestions()
}

// 选择
const handleSelectionChange = (selection: Question[]) => {
  selectedIds.value = selection.map(q => q.id!).filter(id => id)
}

// 返回
const goBack = () => {
  router.push('/questions')
}

// 颜色
const getBankColor = (id: number) => {
  const colors = [
    'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
  ]
  return colors[id % colors.length]
}

// 类型
const getTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    single: '单选题', multiple: '多选题', judge: '判断题', essay: '简答题'
  }
  return labels[type] || type
}

const getTypeTagType = (type: string) => {
  const types: Record<string, any> = {
    single: 'primary', multiple: 'success', judge: 'warning', essay: 'info'
  }
  return types[type] || 'default'
}

const truncateText = (text: string, max: number) => {
  if (!text) return ''
  return text.length > max ? text.substring(0, max) + '...' : text
}

const getAnswerPlaceholder = () => {
  switch (questionForm.type) {
    case 'single': return '输入正确选项，如：A'
    case 'multiple': return '输入正确选项，多个用分号分隔，如：A;B;C'
    case 'judge': return '输入：正确 或 错误'
    case 'essay': return '输入参考答案'
    default: return '请输入正确答案'
  }
}

// 添加题目
const showAddDialog = () => {
  isEditQuestion.value = false
  Object.assign(questionForm, {
    id: 0,
    type: 'single',
    content: '',
    options: ['', ''],
    correctAnswer: '',
    analysis: ''
  })
  questionDialogVisible.value = true
}

// 编辑题目
const editQuestion = (question: Question) => {
  isEditQuestion.value = true
  Object.assign(questionForm, {
    id: question.id,
    type: question.type,
    content: question.content,
    options: question.options && question.options.length > 0 ? [...question.options] : ['', ''],
    correctAnswer: question.correctAnswer || '',
    analysis: question.analysis || ''
  })
  questionDialogVisible.value = true
}

// 添加选项
const addOption = () => {
  if (questionForm.options.length < 8) {
    questionForm.options.push('')
  }
}

// 删除选项
const removeOption = (index: number) => {
  if (questionForm.options.length > 2) {
    questionForm.options.splice(index, 1)
  }
}

// 提交题目
const submitQuestion = async () => {
  if (!questionFormRef.value) return

  try {
    await questionFormRef.value.validate()
    submitting.value = true

    const data = { ...questionForm } as any

    if (isEditQuestion.value) {
      await questionApi.updateQuestion(questionForm.id, data)
      ElMessage.success('修改成功')
    } else {
      await questionApi.createQuestion(bankId.value, data)
      ElMessage.success('添加成功')
    }

    questionDialogVisible.value = false
    fetchQuestions()
    fetchStats()
  } catch (error: any) {
    if (error.message) ElMessage.error(error.message)
  } finally {
    submitting.value = false
  }
}

// 删除题目
const deleteQuestion = async (question: Question) => {
  try {
    await ElMessageBox.confirm('确定要删除这道题目吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await questionApi.deleteQuestion(question.id!)
    ElMessage.success('删除成功')
    fetchQuestions()
    fetchStats()
  } catch (error: any) {
    if (error !== 'cancel' && error.message) ElMessage.error(error.message)
  }
}

// 批量删除
const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 道题目吗？`, '批量删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await questionApi.batchDeleteQuestions(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    fetchQuestions()
    fetchStats()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

// 查看题目
const viewQuestion = (question: Question) => {
  viewingQuestion.value = question
  viewDialogVisible.value = true
}

const editFromView = () => {
  if (viewingQuestion.value) {
    viewDialogVisible.value = false
    editQuestion(viewingQuestion.value)
  }
}

// 导入
const showImportDialog = () => {
  selectedFile.value = null
  importDialogVisible.value = true
}

const handleFileChange = (file: any) => {
  if (file && file.raw) selectedFile.value = file.raw
}

const confirmImport = async () => {
  if (!selectedFile.value) return

  importing.value = true
  try {
    const result = await bankApi.importQuestionsToBank(bankId.value, selectedFile.value, 'append') as any
    const count = result?.importResult?.successCount || result?.importedCount || 0
    ElMessage.success(`导入成功，共导入 ${count} 道题目`)
    importDialogVisible.value = false
    fetchQuestions()
    fetchStats()
  } catch (error: any) {
    ElMessage.error('导入失败：' + (error.message || '未知错误'))
  } finally {
    importing.value = false
  }
}

// 下载导入模板
const downloadTemplate = () => {
  const headers = [
    '试题类型', '题目', '答案', '解析', '选项1', '选项2', '选项3', '选项4', '选项5', '选项6'
  ]

  const exampleData = [
    ['单选题', '1+1等于多少？', 'B', '基础数学运算', '1', '2', '3', '4', '', ''],
    ['多选题', '以下哪些是编程语言？', 'A,B,D', 'HTML是标记语言，不是编程语言', 'Java', 'Python', 'HTML', 'JavaScript', '', ''],
    ['判断题', '地球是圆的', '正确', '地球是一个椭球体', '正确', '错误', '', '', '', ''],
    ['简答题', '请简述什么是面向对象编程？', '面向对象编程是一种编程范式，它使用对象来设计软件。', '理解OOP的基本概念', '', '', '', '', '', ''],
    ['单选题', '（示例：7个选项）哪个是正确的？', 'G', '选项可以超过6个', '选项A', '选项B', '选项C', '选项D', '选项E', '选项F', '选项G']
  ]

  const workbook = XLSX.utils.book_new()
  const worksheet = XLSX.utils.aoa_to_sheet([headers, ...exampleData])

  // 设置列宽
  worksheet['!cols'] = [
    { wch: 10 },  // 试题类型
    { wch: 40 },  // 题目
    { wch: 15 },  // 答案
    { wch: 30 },  // 解析
    { wch: 12 },  // 选项1
    { wch: 12 },  // 选项2
    { wch: 12 },  // 选项3
    { wch: 12 },  // 选项4
    { wch: 12 },  // 选项5
    { wch: 12 },  // 选项6
    { wch: 12 }   // 选项7（如果有）
  ]

  XLSX.utils.book_append_sheet(workbook, worksheet, '题目模板')
  XLSX.writeFile(workbook, '题库导入模板.xlsx')
  ElMessage.success('模板下载成功')
}

onMounted(() => {
  fetchBank()
  fetchStats()
  fetchQuestions()
})
</script>

<style scoped>
.bank-detail-container {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.back-btn {
  margin-bottom: 12px;
  font-size: 14px;
  color: #909399;
}

.back-btn:hover {
  color: #409eff;
}

.bank-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.bank-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.bank-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 4px 0;
}

.bank-desc {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.header-right {
  display: flex;
  gap: 12px;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #ebeef5;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-icon.total { background: #e8f4ff; color: #409eff; }
.stat-icon.single { background: #e8f5e9; color: #67c23a; }
.stat-icon.multiple { background: #fff3e0; color: #e6a23c; }
.stat-icon.judge { background: #fce4ec; color: #f56c6c; }

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

/* 筛选 */
.filter-card {
  margin-bottom: 24px;
  border-radius: 12px;
}

.filter-row {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-right {
  margin-left: auto;
}

/* 表格 */
.question-card {
  border-radius: 12px;
}

.question-content {
  padding: 8px 0;
}

.question-text {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.answer-text {
  color: #67c23a;
  font-weight: 500;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

/* 详情对话框 */
.question-detail {
  padding: 10px 0;
}

.detail-row {
  margin-bottom: 16px;
  display: flex;
}

.detail-label {
  width: 80px;
  flex-shrink: 0;
  color: #909399;
  font-size: 14px;
}

.detail-content {
  flex: 1;
  line-height: 1.6;
  color: #303133;
}

.options-list {
  line-height: 2;
}

.answer-highlight {
  color: #67c23a;
  font-weight: 600;
}

/* 上传 */
.upload-area {
  width: 100%;
}

.upload-area :deep(.el-upload) {
  width: 100%;
}

.upload-area :deep(.el-upload-dragger) {
  width: 100%;
  height: 150px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .header-right {
    width: 100%;
  }

  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-right {
    margin-left: 0;
  }
}
</style>
