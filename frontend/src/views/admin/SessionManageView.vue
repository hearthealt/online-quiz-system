<template>
  <AppLayout>
    <div class="session-manage-page">
      <div class="page-header">
        <h2>答题记录管理</h2>
      </div>

      <!-- 搜索筛选 -->
      <div class="filter-card">
        <el-form :inline="true" :model="filters" class="filter-form">
          <el-form-item label="用户ID">
            <el-input v-model="filters.userId" placeholder="请输入用户ID" clearable style="width: 120px" />
          </el-form-item>
          <el-form-item label="题库ID">
            <el-input v-model="filters.bankId" placeholder="请输入题库ID" clearable style="width: 120px" />
          </el-form-item>
          <el-form-item label="模式">
            <el-select v-model="filters.mode" placeholder="全部" clearable style="width: 120px">
              <el-option label="练习" value="practice" />
              <el-option label="考试" value="exam" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="filters.status" placeholder="全部" clearable style="width: 120px">
              <el-option label="进行中" value="ongoing" />
              <el-option label="已完成" value="completed" />
            </el-select>
          </el-form-item>
          <el-form-item label="删除状态">
            <el-select v-model="filters.deleted" placeholder="全部" clearable style="width: 120px">
              <el-option label="未删除" :value="0" />
              <el-option label="已删除" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 数据表格 -->
      <div class="table-card">
        <el-table :data="sessions" v-loading="loading" stripe border>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="sessionId" label="会话ID" width="280">
            <template #default="{ row }">
              <span class="session-id">{{ row.sessionId }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="username" label="用户" width="120" />
          <el-table-column prop="bankName" label="题库" min-width="150" />
          <el-table-column prop="mode" label="模式" width="80">
            <template #default="{ row }">
              <el-tag :type="row.mode === 'practice' ? 'success' : 'warning'" size="small">
                {{ row.mode === 'practice' ? '练习' : '考试' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="答题进度" width="120">
            <template #default="{ row }">
              {{ row.answeredQuestions }} / {{ row.totalQuestions }}
            </template>
          </el-table-column>
          <el-table-column label="正确率" width="100">
            <template #default="{ row }">
              <span v-if="row.answeredQuestions > 0">
                {{ ((row.correctAnswers / row.answeredQuestions) * 100).toFixed(1) }}%
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ongoing' ? 'primary' : 'info'" size="small">
                {{ row.status === 'ongoing' ? '进行中' : '已完成' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="deleted" label="删除状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.deleted === 1 ? 'danger' : 'success'" size="small">
                {{ row.deleted === 1 ? '已删除' : '正常' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="startTime" label="开始时间" width="170" />
          <el-table-column prop="endTime" label="结束时间" width="170">
            <template #default="{ row }">
              {{ row.endTime || '-' }}
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import AppLayout from '@/components/common/AppLayout.vue'
import type { QuizSession } from '@/types/index'
import * as adminApi from '@/api/admin'

const loading = ref(false)
const sessions = ref<QuizSession[]>([])

const filters = reactive({
  userId: '',
  bankId: '',
  mode: '',
  status: '',
  deleted: undefined as number | undefined
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params: any = {
      current: pagination.current,
      size: pagination.size
    }
    if (filters.userId) params.userId = Number(filters.userId)
    if (filters.bankId) params.bankId = Number(filters.bankId)
    if (filters.mode) params.mode = filters.mode
    if (filters.status) params.status = filters.status
    if (filters.deleted !== undefined) params.deleted = filters.deleted

    const result = await adminApi.getSessionList(params)
    sessions.value = result.records
    pagination.total = result.total
  } catch (error: any) {
    console.error('加载数据失败:', error)
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  filters.userId = ''
  filters.bankId = ''
  filters.mode = ''
  filters.status = ''
  filters.deleted = undefined
  pagination.current = 1
  loadData()
}

// 分页大小改变
const handleSizeChange = () => {
  pagination.current = 1
  loadData()
}

// 页码改变
const handleCurrentChange = () => {
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.session-manage-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.filter-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.table-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.session-id {
  font-family: monospace;
  font-size: 12px;
  color: #909399;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
