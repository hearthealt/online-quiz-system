<template>
  <el-dialog
    v-model="visible"
    title="联系管理员"
    width="600px"
    :close-on-click-modal="false"
    :close-on-press-escape="true"
    @close="handleClose"
  >
    <div class="contact-content">
      <p class="contact-subtitle">遇到问题或需要帮助？请联系我们</p>

      <el-form
        ref="contactFormRef"
        :model="contactForm"
        :rules="contactRules"
        class="contact-form"
        size="large"
        @submit.prevent="handleSubmit"
      >
        <el-form-item prop="type">
          <el-select
            v-model="contactForm.type"
            placeholder="请选择问题类型"
            style="width: 100%"
            @change="handleTypeChange"
          >
            <el-option label="密码问题" value="password" />
            <el-option label="账号问题" value="account" />
            <el-option label="功能问题" value="feature" />
            <el-option label="其他问题" value="other" />
          </el-select>
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="contactForm.email"
            type="email"
            placeholder="请输入您的邮箱地址"
            prefix-icon="Message"
            clearable
          />
        </el-form-item>

        <el-form-item prop="username">
          <el-input
            v-model="contactForm.username"
            placeholder="请输入您的用户名（可选）"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="subject">
          <el-input
            v-model="contactForm.subject"
            placeholder="请输入问题标题"
            prefix-icon="Document"
            clearable
          />
        </el-form-item>

        <el-form-item prop="description">
          <el-input
            v-model="contactForm.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述您遇到的问题..."
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <div class="contact-tips">
            <el-alert
              :title="getAlertTitle()"
              :description="getAlertDescription()"
              type="info"
              :closable="false"
              show-icon
            />
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="submit-button"
            :loading="loading"
            @click="handleSubmit"
          >
            {{ loading ? '发送中...' : '发送邮件' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="contact-info">
        <div class="contact-methods">
          <div class="contact-method">
            <el-icon><Message /></el-icon>
            <span>邮箱：{{ adminEmail }}</span>
          </div>
          <div class="contact-method">
            <el-icon><Timer /></el-icon>
            <span>响应时间：24小时内回复</span>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, defineProps, defineEmits, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Message, Clock, Timer } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { ContactRequest } from '@/types/index'
import { sendContactEmail, getAdminEmail } from '@/api/contact'

// 定义props和emits
const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

// 弹窗显示状态
const visible = ref(props.modelValue)

// 管理员邮箱
const adminEmail = ref('admin@quiz-system.com')

// 表单引用
const contactFormRef = ref<FormInstance>()

// 表单数据
const contactForm = reactive<ContactRequest>({
  type: '' as ContactRequest['type'],
  email: '',
  username: '',
  subject: '',
  description: ''
})

// 加载状态
const loading = ref(false)

// 表单验证规则
const contactRules: FormRules = {
  type: [
    { required: true, message: '请选择问题类型', trigger: 'change' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  subject: [
    { required: true, message: '请输入问题标题', trigger: 'blur' },
    { min: 5, max: 100, message: '标题长度应为5-100个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请详细描述问题', trigger: 'blur' }
  ]
}

// 处理问题类型变化
const handleTypeChange = (value: string) => {
  // 根据问题类型自动设置标题
  const typeTitles = {
    password: '密码相关问题',
    account: '账号相关问题',
    feature: '功能使用问题',
    other: '其他问题'
  }
  
  if (!contactForm.subject || contactForm.subject === '') {
    contactForm.subject = typeTitles[value as keyof typeof typeTitles] || ''
  }
}

// 获取提示标题
const getAlertTitle = () => {
  const titles = {
    password: '密码问题',
    account: '账号问题',
    feature: '功能问题',
    other: '其他问题'
  }
  return titles[contactForm.type as keyof typeof titles] || '问题反馈'
}

// 获取提示描述
const getAlertDescription = () => {
  const descriptions = {
    password: '如果您忘记了密码或需要修改密码，请提供您的用户名和注册邮箱，我们会在24小时内为您处理。',
    account: '如果您遇到账号登录、注册或其他账号相关问题，请详细描述问题，我们会尽快为您解决。',
    feature: '如果您在使用系统功能时遇到问题，请详细描述操作步骤和遇到的问题，我们会为您提供帮助。',
    other: '如果您有其他问题或建议，请详细描述，我们会认真处理您的反馈。'
  }
  return descriptions[contactForm.type as keyof typeof descriptions] || '请详细描述您遇到的问题，我们会尽快为您处理。'
}

// 处理关闭弹窗
const handleClose = () => {
  emit('update:modelValue', false)
}

// 监听props变化
watch(() => props.modelValue, (newValue) => {
  visible.value = newValue
})

// 监听visible变化
watch(visible, (newValue) => {
  emit('update:modelValue', newValue)
})

// 获取管理员邮箱
const fetchAdminEmail = async () => {
  try {
    const email = await getAdminEmail()
    adminEmail.value = email
  } catch (error) {
    console.error('获取管理员邮箱失败:', error)
    // 保持默认值
  }
}

// 组件挂载时获取管理员邮箱
onMounted(() => {
  fetchAdminEmail()
})

// 处理提交
const handleSubmit = async () => {
  if (!contactFormRef.value) return

  try {
    // 表单验证
    await contactFormRef.value.validate()
    
    loading.value = true
    
    // 调用后端API发送邮件
    await sendContactEmail(contactForm)
    
    ElMessage.success('邮件发送成功！我们会在24小时内回复您。')
    
    // 重置表单
    contactFormRef.value.resetFields()
    
    // 关闭弹窗
    handleClose()
    
  } catch (error: any) {
    console.error('发送邮件失败:', error)
    ElMessage.error(error.message || '发送失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.contact-content {
  padding: 0;
}

.contact-subtitle {
  color: #909399;
  font-size: 14px;
  margin-bottom: 16px;
}

.contact-tips {
  margin-bottom: 12px;
}

.submit-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
}

.contact-info {
  border-top: 1px solid #ebeef5;
  padding-top: 16px;
}

.contact-methods {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.contact-method {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

.contact-method .el-icon {
  color: #409eff;
}
</style>
