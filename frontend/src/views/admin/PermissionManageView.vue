<template>
  <AppLayout>
    <div class="permission-manage-container">
      <div class="page-header">
        <h1 class="page-title">权限管理</h1>
        <div class="header-actions">
          <el-button type="primary" @click="refreshUsers">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>

      <!-- 搜索区域 -->
      <el-card class="search-card">
        <el-form :model="searchForm" :inline="true" class="search-form">
          <el-form-item label="用户名">
            <el-input
              v-model="searchForm.username"
              placeholder="请输入用户名"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input
              v-model="searchForm.nickname"
              placeholder="请输入昵称"
              clearable
              style="width: 200px"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="searchForm.status"
              placeholder="请选择状态"
              clearable
              style="width: 150px"
            >
              <el-option label="正常" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="resetSearch">
              <el-icon><RefreshLeft /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 用户列表 -->
      <el-card class="table-card">
        <el-table
          :data="users"
          v-loading="loading"
          stripe
          style="width: 100%"
          class="user-table"
        >
          <el-table-column label="头像" width="80" align="center">
            <template #default="{ row }">
              <el-avatar :src="row.avatar" :size="40">
                {{ row.nickname?.charAt(0) || row.username.charAt(0) }}
              </el-avatar>
            </template>
          </el-table-column>

          <el-table-column prop="username" label="用户名" min-width="80" />

          <el-table-column prop="nickname" label="昵称" min-width="80">
            <template #default="{ row }">
              {{ row.nickname || '-' }}
            </template>
          </el-table-column>

          <el-table-column prop="email" label="邮箱" min-width="200">
            <template #default="{ row }">
              {{ row.email || '-' }}
            </template>
          </el-table-column>

          <el-table-column prop="role" label="角色" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.role === 'admin' ? 'danger' : 'info'" size="small">
                {{ row.role === 'admin' ? '管理员' : '普通用户' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="权限" min-width="120">
            <template #default="{ row }">
              <div class="permissions">
                <template v-if="row.role === 'admin'">
                  <el-tag type="danger" size="small">全部权限</el-tag>
                </template>
                <template v-else>
                  <el-tag
                    v-for="permission in (row.permissions || [])"
                    :key="permission"
                    size="small"
                    class="permission-tag"
                  >
                    {{ getPermissionLabel(permission) }}
                  </el-tag>
                  <span v-if="!row.permissions || row.permissions.length === 0" class="no-permissions">
                    无权限
                  </span>
                </template>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="状态" width="70" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="lastLoginTime" label="最后登录" min-width="150">
            <template #default="{ row }">
              {{ row.lastLoginTime || '-' }}
            </template>
          </el-table-column>

          <el-table-column label="操作" width="240" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button 
                  v-if="row.role !== 'admin'" 
                  link type="primary" 
                  size="small" 
                  @click="editPermissions(row)"
                >
                  <el-icon><Edit /></el-icon>
                  编辑权限
                </el-button>
                <el-button 
                  v-if="row.role !== 'admin'"
                  :type="row.status === 1 ? 'warning' : 'success'"
                  link 
                  size="small" 
                  @click="toggleUserStatus(row)"
                >
                  <el-icon><Switch /></el-icon>
                  {{ row.status === 1 ? '禁用' : '启用' }}
                </el-button>
                <el-button 
                  v-if="row.role !== 'admin'"
                  type="danger"
                  link 
                  size="small" 
                  @click="resetPassword(row)"
                >
                  <el-icon><Key /></el-icon>
                  重置密码
                </el-button>
                <span v-else class="admin-note">管理员</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :page-sizes="[5, 10, 16, 20]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 编辑权限对话框 -->
    <el-dialog 
      v-model="editDialogVisible" 
      title="编辑用户权限" 
      width="600px"
      :close-on-click-modal="false"
      class="permission-dialog"
      :show-close="true"
      :close-on-press-escape="true"
      :modal="true"
      :append-to-body="true"
      :destroy-on-close="true"
    >
      <div class="permission-edit">
        <!-- 用户信息卡片 -->
        <div class="user-info-card">
          <div class="user-avatar-section">
            <el-avatar :src="currentUser?.avatar" :size="50" class="user-avatar">
              {{ currentUser?.nickname?.charAt(0) || currentUser?.username?.charAt(0) }}
            </el-avatar>
          </div>
          <div class="user-details">
            <h3 class="user-name">{{ currentUser?.nickname || currentUser?.username }}</h3>
            <p class="user-username">@{{ currentUser?.username }}</p>
            <div class="user-status">
              <el-tag :type="currentUser?.status === 1 ? 'success' : 'danger'" size="small">
                {{ currentUser?.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- 权限设置区域 -->
        <div class="permissions-section">
          <div class="section-header">
          <h3>权限设置</h3>
            <p class="section-desc">为当前用户分配题目管理相关权限</p>
          </div>
          
          <div class="permission-groups">
            <div class="permission-group">
              <div class="group-header">
              <h4>题目管理权限</h4>
                <el-button 
                  link 
                  type="primary" 
                  size="small" 
                  @click="selectAllPermissions"
                >
                  全选
                </el-button>
                <el-button 
                  link 
                  type="info" 
                  size="small" 
                  @click="clearAllPermissions"
                >
                  清空
                </el-button>
              </div>
              <div class="permission-list">
                <el-checkbox-group v-model="selectedPermissions" class="permission-checkboxes">
                  <div class="permission-item">
                    <el-checkbox value="question:create" class="permission-checkbox">
                      <div class="permission-content">
                        <div class="permission-title">创建题目</div>
                        <div class="permission-desc">可以创建新的题目</div>
                      </div>
                    </el-checkbox>
                  </div>
                  <div class="permission-item">
                    <el-checkbox value="question:edit" class="permission-checkbox">
                      <div class="permission-content">
                        <div class="permission-title">编辑题目</div>
                        <div class="permission-desc">可以修改现有题目</div>
                      </div>
                    </el-checkbox>
                  </div>
                  <div class="permission-item">
                    <el-checkbox value="question:delete" class="permission-checkbox">
                      <div class="permission-content">
                        <div class="permission-title">删除题目</div>
                        <div class="permission-desc">可以删除题目</div>
                      </div>
                    </el-checkbox>
                  </div>
                  <div class="permission-item">
                    <el-checkbox value="question:upload" class="permission-checkbox">
                      <div class="permission-content">
                        <div class="permission-title">上传题库</div>
                        <div class="permission-desc">可以批量上传题目</div>
                      </div>
                    </el-checkbox>
                  </div>
                  <div class="permission-item">
                    <el-checkbox value="question:batch" class="permission-checkbox">
                      <div class="permission-content">
                        <div class="permission-title">批量操作</div>
                        <div class="permission-desc">可以批量管理题目</div>
                      </div>
                    </el-checkbox>
                  </div>
              </el-checkbox-group>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <div class="footer-left">
            <span class="selected-count">
              已选择 {{ selectedPermissions.length }} 个权限
            </span>
          </div>
          <div class="footer-right">
            <el-button @click="editDialogVisible = false" size="large">
              取消
            </el-button>
            <el-button 
              type="primary" 
              @click="savePermissions" 
              :loading="saving"
              size="large"
            >
              保存权限
          </el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Edit, Search, RefreshLeft, Switch, Key } from '@element-plus/icons-vue'
import AppLayout from '@/components/common/AppLayout.vue'
import { useUserStore } from '@/store/user'
import type { User } from '@/types/index'
import { getUserList, updateUserPermissions, updateUserStatus, resetUserPassword } from '@/api/permission'
import type { UserQueryParams, PageResult } from '@/types/index'

const userStore = useUserStore()

// 数据状态
const loading = ref(false)
const saving = ref(false)
const users = ref<User[]>([])
const editDialogVisible = ref(false)
const currentUser = ref<User | null>(null)
const selectedPermissions = ref<string[]>([])

// 搜索表单
const searchForm = reactive<UserQueryParams>({
  username: '',
  nickname: '',
  status: undefined
})

// 分页信息
const pagination = reactive({
  current: 1,
  size: 8,
  total: 0
})

// 权限标签映射
const permissionLabels: Record<string, string> = {
  'question:create': '创建题目',
  'question:edit': '编辑题目',
  'question:delete': '删除题目',
  'question:upload': '上传题库',
  'question:batch': '批量操作'
}

// 获取权限标签
const getPermissionLabel = (permission: string) => {
  return permissionLabels[permission] || permission
}

// 获取用户列表
const fetchUsers = async () => {
  try {
    loading.value = true
    const params: UserQueryParams = {
      page: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    
    const result = await getUserList(params)
    users.value = result.records
    pagination.total = result.total
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 刷新用户列表
const refreshUsers = () => {
  pagination.current = 1
  fetchUsers()
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchUsers()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    username: '',
    nickname: '',
    status: undefined
  })
  pagination.current = 1
  fetchUsers()
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  fetchUsers()
}

// 当前页改变
const handleCurrentChange = (page: number) => {
  pagination.current = page
  fetchUsers()
}

// 编辑权限
const editPermissions = (user: User) => {
  currentUser.value = user
  selectedPermissions.value = user.permissions ? [...user.permissions] : []
  editDialogVisible.value = true
}

// 保存权限
const savePermissions = async () => {
  if (!currentUser.value) return

  try {
    saving.value = true
    await updateUserPermissions({
      userId: currentUser.value.id!,
      permissions: selectedPermissions.value
    })
    
    // 更新本地数据
    const user = users.value.find(u => u.id === currentUser.value!.id)
    if (user) {
      user.permissions = selectedPermissions.value.length > 0 ? [...selectedPermissions.value] : []
    }
    
    ElMessage.success('权限更新成功')
    editDialogVisible.value = false
  } catch (error) {
    ElMessage.error('权限更新失败')
  } finally {
    saving.value = false
  }
}

// 切换用户状态
const toggleUserStatus = async (user: User) => {
  const newStatus = user.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户 "${user.username}" 吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await updateUserStatus({
      userId: user.id!,
      status: newStatus
    })
    
    // 更新本地数据
    user.status = newStatus
    ElMessage.success(`用户${action}成功`)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`用户${action}失败`)
    }
  }
}

// 全选权限
const selectAllPermissions = () => {
  selectedPermissions.value = [
    'question:create',
    'question:edit',
    'question:delete',
    'question:upload',
    'question:batch'
  ]
}

// 清空权限
const clearAllPermissions = () => {
  selectedPermissions.value = []
}

// 重置密码
const resetPassword = async (user: User) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户 "${user.username}" 的密码吗？\n新密码将通过邮件发送给用户`,
      '确认重置密码',
      {
        confirmButtonText: '确定重置',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true
      }
    )
    
    await resetUserPassword({
      userId: user.id!
    })
    
    ElMessage.success('密码重置成功，新密码已发送到用户邮箱')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('密码重置失败')
    }
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.permission-manage-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.search-card {
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.search-form {
  margin-bottom: 0;
}

.table-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.user-table {
  border-radius: 8px;
  overflow: hidden;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.username {
  font-weight: 600;
  color: #303133;
}

.nickname {
  font-size: 12px;
  color: #909399;
}

.permissions {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.permission-tag {
  margin: 2px;
}

.no-permissions {
  color: #909399;
  font-size: 12px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 2px;
  flex-wrap: wrap;
}

.admin-note {
  color: #909399;
  font-size: 12px;
}

/* 权限对话框样式 */
.permission-dialog {
  .el-dialog {
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  }
  
  .el-dialog__header {
    padding: 20px 24px 16px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border-bottom: none;
    position: relative;
  }
  
  .el-dialog__title {
    font-size: 20px;
    font-weight: 600;
    color: white;
  }
  
  .el-dialog__headerbtn {
    top: 20px;
    right: 24px;
  }
  
  .el-dialog__close {
    color: white;
    font-size: 20px;
  }
  
  .el-dialog__close:hover {
    color: rgba(255, 255, 255, 0.8);
  }
  
  .el-dialog__body {
    padding: 0;
    background: #fafbfc;
  }
  
  .el-dialog__footer {
    padding: 20px 24px 24px;
    background: white;
    border-top: 1px solid #e8eaed;
  }
}

.permission-edit {
  padding: 0;
}

/* 用户信息卡片 */
.user-info-card {
  display: flex;
  align-items: center;
  padding: 20px 24px;
  background: white;
  margin: 0;
  border-bottom: 1px solid #e8eaed;
  position: relative;
}

.user-info-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
}

.user-avatar-section {
  margin-right: 20px;
  position: relative;
}

.user-avatar {
  border: 4px solid #fff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transition: transform 0.3s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
}

.user-details {
  flex: 1;
}

.user-name {
  margin: 0 0 6px 0;
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
  letter-spacing: -0.5px;
}

.user-username {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

.user-status {
  display: flex;
  align-items: center;
}

.user-status .el-tag {
  font-weight: 600;
  border-radius: 20px;
  padding: 4px 12px;
}

/* 权限设置区域 */
.permissions-section {
  background: white;
  margin: 0;
}

.section-header {
  padding: 24px 24px 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  border-bottom: 1px solid #e2e8f0;
}

.section-header h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: -0.5px;
}

.section-desc {
  margin: 0;
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.permission-groups {
  padding: 0;
}

.permission-group {
  border-bottom: none;
}

.group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px 16px;
  background: white;
  border-bottom: 1px solid #f1f5f9;
}

.group-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #334155;
  letter-spacing: -0.3px;
}

.group-header .el-button {
  margin-left: 12px;
  font-size: 13px;
  font-weight: 600;
  padding: 6px 16px;
  border-radius: 20px;
  transition: all 0.3s ease;
}

.group-header .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.permission-list {
  padding: 0 24px 24px;
}

.permission-checkboxes {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.permission-item {
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  background: white;
  position: relative;
}

.permission-item:hover {
  border-color: #667eea;
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

.permission-item:has(.el-checkbox__input.is-checked) {
  border-color: #667eea;
  background: linear-gradient(135deg, #f8faff 0%, #f0f4ff 100%);
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.2);
}

.permission-checkbox {
  width: 100%;
  margin: 0;
  padding: 16px 20px;
}

.permission-checkbox .el-checkbox__label {
  width: 100%;
  padding-left: 12px;
  font-size: 0;
}

.permission-checkbox .el-checkbox__input {
  margin-top: 2px;
}

.permission-checkbox .el-checkbox__input .el-checkbox__inner {
  width: 20px;
  height: 20px;
  border-radius: 6px;
  border: 2px solid #d1d5db;
  transition: all 0.3s ease;
}

.permission-checkbox .el-checkbox__input.is-checked .el-checkbox__inner {
  background-color: #667eea;
  border-color: #667eea;
}

.permission-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.permission-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  letter-spacing: -0.2px;
  flex-shrink: 0;
}

.permission-desc {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
  text-align: right;
  flex: 1;
}


/* 对话框底部 */
.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
}

.footer-left {
  display: flex;
  align-items: center;
}

.selected-count {
  font-size: 14px;
  color: #475569;
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  padding: 8px 16px;
  border-radius: 20px;
  border: 1px solid #cbd5e1;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.footer-right {
  display: flex;
  gap: 16px;
}

.footer-right .el-button {
  padding: 12px 24px;
  font-weight: 600;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.footer-right .el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.footer-right .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.footer-right .el-button:not(.el-button--primary) {
  background: white;
  border: 2px solid #e2e8f0;
  color: #64748b;
}

.footer-right .el-button:not(.el-button--primary):hover {
  border-color: #cbd5e1;
  color: #475569;
  transform: translateY(-1px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .permission-dialog .el-dialog {
    width: 95% !important;
    margin: 0 auto;
  }
  
  .user-info-card {
    flex-direction: column;
    text-align: center;
  }
  
  .user-avatar-section {
    margin-right: 0;
    margin-bottom: 12px;
  }
  
  .dialog-footer {
    flex-direction: column;
    gap: 16px;
  }
  
  .footer-right {
    width: 100%;
    justify-content: center;
  }
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 20px 0;
}
</style>
