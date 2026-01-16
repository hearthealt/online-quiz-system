<template>
  <AppLayout>
    <div class="bank-manage-page">
      <!-- 顶部区域 -->
      <div class="page-header">
        <div class="header-info">
          <h1 class="page-title">题库管理</h1>
          <p class="page-desc">管理所有题库，点击可查看和编辑题目</p>
        </div>
        <div class="header-actions">
          <el-button @click="showImportDialog" v-if="canUpload">
            <el-icon><Upload /></el-icon>
            导入题库
          </el-button>
          <el-button type="primary" @click="showCreateDialog" v-if="canCreate">
            <el-icon><Plus /></el-icon>
            新建题库
          </el-button>
        </div>
      </div>

      <!-- 搜索和统计 -->
      <div class="toolbar">
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索题库名称..."
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
        <div class="stats-box">
          <div class="stat-item">
            <span class="stat-num">{{ total }}</span>
            <span class="stat-label">题库总数</span>
          </div>
        </div>
      </div>

      <!-- 题库网格 -->
      <div class="banks-container" v-loading="loading">
        <div class="banks-grid">
          <div
            v-for="bank in banks"
            :key="bank.id"
            class="bank-card"
            @click="viewBankDetail(bank)"
          >
            <!-- 卡片顶部 -->
            <div class="card-top">
              <div class="bank-icon" :style="{ background: getBankColor(bank.id!) }">
                <el-icon :size="26"><FolderOpened /></el-icon>
              </div>
              <el-dropdown
                @click.stop
                trigger="click"
                v-if="canEdit || canDelete"
              >
                <el-button link class="more-btn" @click.stop>
                  <el-icon :size="18"><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="editBank(bank)" v-if="canEdit">
                      <el-icon><Edit /></el-icon>
                      编辑
                    </el-dropdown-item>
                    <el-dropdown-item @click="deleteBank(bank)" v-if="canDelete">
                      <el-icon color="#f56c6c"><Delete /></el-icon>
                      <span style="color: #f56c6c">删除</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>

            <!-- 卡片内容 -->
            <div class="card-content">
              <h3 class="bank-name">{{ bank.name }}</h3>
              <p class="bank-desc">{{ bank.description || '暂无描述' }}</p>
            </div>

            <!-- 卡片底部 -->
            <div class="card-bottom">
              <div class="bank-meta">
                <span class="meta-item">
                  <el-icon><Document /></el-icon>
                  {{ bank.questionCount || 0 }} 题
                </span>
                <el-tag :type="bank.status === 1 ? 'success' : 'info'" size="small" effect="light">
                  {{ bank.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </div>
              <el-button type="primary" size="small" text class="view-btn">
                查看详情
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <el-empty
          v-if="!loading && banks.length === 0"
          description="暂无题库"
          :image-size="140"
        >
          <el-button type="primary" @click="showCreateDialog" v-if="canCreate">
            创建第一个题库
          </el-button>
        </el-empty>
      </div>

      <!-- 分页 -->
      <div class="pagination-box" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[12, 24, 36, 48]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑题库' : '新建题库'"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="bankForm"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="bankForm.name" placeholder="请输入题库名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="bankForm.description" type="textarea" :rows="3" placeholder="请输入描述（选填）" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="状态" v-if="isEdit">
          <el-switch v-model="bankForm.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="导入题库"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-alert type="info" :closable="false" style="margin-bottom: 20px">
        <template #title>
          <div style="line-height: 1.8;">
            <p><strong>格式：</strong>Excel (.xlsx/.xls)</p>
            <p><strong>规则：</strong>文件名作为题库名称，同名题库会覆盖</p>
            <el-button type="primary" size="small" text @click="downloadTemplate">
              <el-icon><Download /></el-icon>
              下载模板
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
        class="upload-box"
      >
        <el-icon class="el-icon--upload" :size="48"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖拽文件到此处，或<em>点击上传</em></div>
      </el-upload>

      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmImport" :loading="importing" :disabled="!selectedFile">
          开始导入
        </el-button>
      </template>
    </el-dialog>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  Plus, Upload, Search, FolderOpened, MoreFilled,
  Edit, Delete, Document, Download, UploadFilled, ArrowRight
} from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import AppLayout from '@/components/common/AppLayout.vue'
import { useUserStore } from '@/store/user'
import * as bankApi from '@/api/bank'
import type { QuestionBank } from '@/types/index'

const router = useRouter()
const userStore = useUserStore()

// 权限
const canCreate = computed(() => userStore.hasPermission('question:create'))
const canEdit = computed(() => userStore.hasPermission('question:edit'))
const canDelete = computed(() => userStore.hasPermission('question:delete'))
const canUpload = computed(() => userStore.hasPermission('question:upload'))

// 列表
const loading = ref(false)
const banks = ref<QuestionBank[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const searchKeyword = ref('')

// 表单
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const submitting = ref(false)
const bankForm = reactive({ id: 0, name: '', description: '', status: 1 })
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入题库名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在2-50个字符', trigger: 'blur' }
  ]
}

// 导入
const importDialogVisible = ref(false)
const uploadRef = ref()
const selectedFile = ref<File | null>(null)
const importing = ref(false)

// 获取题库颜色
const getBankColor = (id: number) => {
  const colors = [
    'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
    'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)',
    'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)'
  ]
  return colors[id % colors.length]
}

// 获取列表
const fetchBanks = async () => {
  loading.value = true
  try {
    const result = await bankApi.getBanks({
      current: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined
    })
    banks.value = result.records || result as any
    total.value = result.total || banks.value.length
  } catch (error) {
    console.error('获取题库列表失败:', error)
    ElMessage.error('获取题库列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchBanks()
}

const resetSearch = () => {
  searchKeyword.value = ''
  currentPage.value = 1
  fetchBanks()
}

const handlePageChange = () => fetchBanks()
const handleSizeChange = () => {
  currentPage.value = 1
  fetchBanks()
}

// 查看详情
const viewBankDetail = (bank: QuestionBank) => {
  router.push(`/questions/bank/${bank.id}`)
}

// 创建
const showCreateDialog = () => {
  isEdit.value = false
  Object.assign(bankForm, { id: 0, name: '', description: '', status: 1 })
  dialogVisible.value = true
}

// 编辑
const editBank = (bank: QuestionBank) => {
  isEdit.value = true
  Object.assign(bankForm, {
    id: bank.id,
    name: bank.name,
    description: bank.description || '',
    status: bank.status ?? 1
  })
  dialogVisible.value = true
}

// 提交
const submitForm = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true

    if (isEdit.value) {
      await bankApi.updateBank(bankForm.id, { description: bankForm.description, status: bankForm.status } as any)
      ElMessage.success('修改成功')
    } else {
      await bankApi.createBank({ name: bankForm.name, description: bankForm.description })
      ElMessage.success('创建成功')
    }

    dialogVisible.value = false
    fetchBanks()
  } catch (error: any) {
    if (error.message) ElMessage.error(error.message)
  } finally {
    submitting.value = false
  }
}

// 删除
const deleteBank = async (bank: QuestionBank) => {
  try {
    await ElMessageBox.confirm(`确定删除题库「${bank.name}」？将同时删除所有题目！`, '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    await bankApi.deleteBank(bank.id!)
    ElMessage.success('删除成功')
    fetchBanks()
  } catch (error: any) {
    if (error !== 'cancel' && error.message) ElMessage.error(error.message)
  }
}

// 导入
const showImportDialog = () => {
  selectedFile.value = null
  importDialogVisible.value = true
}

const handleFileChange = (file: any) => {
  if (file?.raw) selectedFile.value = file.raw
}

const confirmImport = async () => {
  if (!selectedFile.value) return
  importing.value = true
  try {
    const result = await bankApi.importQuestions(selectedFile.value, 'overwrite') as any
    const count = result?.importResult?.successCount || result?.importedCount || 0
    ElMessage.success(`导入成功，共 ${count} 道题目`)
    importDialogVisible.value = false
    fetchBanks()
  } catch (error: any) {
    ElMessage.error('导入失败：' + (error.message || '未知错误'))
  } finally {
    importing.value = false
  }
}

// 下载模板
const downloadTemplate = () => {
  const headers = ['试题类型', '题目', '答案', '解析', '选项1', '选项2', '选项3', '选项4', '选项5', '选项6']
  const data = [
    ['单选题', '1+1等于多少？', 'B', '基础运算', '1', '2', '3', '4', '', ''],
    ['多选题', '以下哪些是编程语言？', 'A,B,D', 'HTML是标记语言', 'Java', 'Python', 'HTML', 'JavaScript', '', ''],
    ['判断题', '地球是圆的', '正确', '地球是椭球体', '正确', '错误', '', '', '', ''],
    ['简答题', '什么是面向对象编程？', 'OOP是一种编程范式', '理解OOP概念', '', '', '', '', '', '']
  ]
  const ws = XLSX.utils.aoa_to_sheet([headers, ...data])
  ws['!cols'] = [{ wch: 10 }, { wch: 40 }, { wch: 15 }, { wch: 30 }, { wch: 12 }, { wch: 12 }, { wch: 12 }, { wch: 12 }, { wch: 12 }, { wch: 12 }]
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '题目模板')
  XLSX.writeFile(wb, '题库导入模板.xlsx')
  ElMessage.success('模板下载成功')
}

onMounted(() => fetchBanks())
</script>

<style scoped>
.bank-manage-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 24px;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 6px 0;
}

.page-desc {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.search-box {
  display: flex;
  gap: 12px;
}

.search-input {
  width: 280px;
}

.stats-box {
  display: flex;
  gap: 24px;
}

.stat-item {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.stat-num {
  font-size: 24px;
  font-weight: 700;
  color: #667eea;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

/* 题库网格 */
.banks-container {
  min-height: 400px;
}

.banks-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.bank-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #ebeef5;
}

.bank-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
  border-color: #667eea;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.bank-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.more-btn {
  padding: 6px;
  color: #909399;
}

.more-btn:hover {
  color: #667eea;
  background: #f0f5ff;
  border-radius: 6px;
}

.card-content {
  margin-bottom: 16px;
}

.bank-name {
  font-size: 17px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bank-desc {
  font-size: 14px;
  color: #909399;
  margin: 0;
  height: 40px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.bank-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  font-size: 14px;
}

.view-btn {
  font-weight: 500;
}

/* 分页 */
.pagination-box {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

/* 上传 */
.upload-box {
  width: 100%;
}

.upload-box :deep(.el-upload) {
  width: 100%;
}

.upload-box :deep(.el-upload-dragger) {
  width: 100%;
  height: 160px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

/* 响应式 */
@media (max-width: 768px) {
  .bank-manage-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .header-actions {
    width: 100%;
  }

  .header-actions .el-button {
    flex: 1;
  }

  .toolbar {
    flex-direction: column;
    gap: 16px;
  }

  .search-box {
    width: 100%;
    flex-wrap: wrap;
  }

  .search-input {
    width: 100%;
  }

  .banks-grid {
    grid-template-columns: 1fr;
  }
}
</style>
