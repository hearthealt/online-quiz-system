<template>
  <div class="file-upload">
    <el-upload
      ref="uploadRef"
      class="upload-component"
      :action="uploadAction"
      :before-upload="beforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-progress="handleProgress"
      :on-change="handleChange"
      :on-remove="handleRemove"
      v-model:file-list="fileList"
      :multiple="multiple"
      :accept="accept"
      :disabled="uploading"
      :show-file-list="showFileList"
      :drag="drag"
      :list-type="listType"
      :auto-upload="false"
    >
      <template #trigger>
        <el-button :loading="uploading" :disabled="disabled">
          <el-icon><Upload /></el-icon>
          {{ buttonText }}
        </el-button>
      </template>

      <template #tip v-if="tip">
        <div class="el-upload__tip">{{ tip }}</div>
      </template>

      <template v-if="drag" #default>
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处或<em>点击上传</em>
        </div>
      </template>
    </el-upload>

    <el-button
      v-if="!autoUpload && fileList.length > 0"
      type="primary"
      :loading="uploading"
      @click="submitUpload"
      style="margin-top: 10px"
    >
      确认上传
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, ElUpload } from 'element-plus'
import { Upload, UploadFilled } from '@element-plus/icons-vue'
import { uploadFile, uploadAvatar, uploadQuestionFile, uploadBatchFiles } from '@/api/upload'
import type { UploadResponse, BatchUploadResponse } from '@/types/index'
import type { UploadProps, UploadUserFile } from 'element-plus'

interface Props {
  // 上传类型
  uploadType?: 'avatar' | 'question' | 'file'
  // 文件分类
  category?: string
  // 是否支持多选
  multiple?: boolean
  // 接受的文件类型
  accept?: string
  // 是否显示文件列表
  showFileList?: boolean
  // 是否拖拽上传
  drag?: boolean
  // 列表类型
  listType?: 'text' | 'picture' | 'picture-card'
  // 按钮文本
  buttonText?: string
  // 提示文本
  tip?: string
  // 是否禁用
  disabled?: boolean
  // 是否自动上传
  autoUpload?: boolean
  // 最大文件大小(MB)
  maxSize?: number
  // 允许的文件类型
  allowedTypes?: string[]
}

const props = withDefaults(defineProps<Props>(), {
  uploadType: 'file',
  category: 'common',
  multiple: false,
  showFileList: true,
  drag: false,
  listType: 'text',
  buttonText: '选择文件',
  disabled: false,
  autoUpload: true,
  maxSize: 10
})

const emit = defineEmits<{
  success: [response: UploadResponse]
  error: [error: any]
  progress: [percentage: number]
}>()

const uploadRef = ref<InstanceType<typeof ElUpload>>()
const uploading = ref(false)
const fileList = ref<UploadUserFile[]>([])

// 虚拟上传地址，实际使用API
const uploadAction = computed(() => '#')

// 文件上传前的钩子
const beforeUpload: UploadProps['beforeUpload'] = (rawFile) => {
  // 检查文件大小
  if (rawFile.size / 1024 / 1024 > props.maxSize) {
    ElMessage.error(`文件大小不能超过${props.maxSize}MB`)
    return false
  }

  // 检查文件类型
  if (props.allowedTypes && props.allowedTypes.length > 0) {
    const fileType = rawFile.type
    const fileName = rawFile.name.toLowerCase()
    const fileExt = fileName.substring(fileName.lastIndexOf('.') + 1)

    const isValidType = props.allowedTypes.some(type =>
      fileType.includes(type) || fileName.endsWith(`.${type}`)
    )

    if (!isValidType) {
      ElMessage.error(`只支持${props.allowedTypes.join('、')}格式的文件`)
      return false
    }
  }

  if (props.autoUpload) {
    handleUpload(rawFile)
    return false // 阻止默认上传
  }

  return false // 阻止默认上传行为，通过onChange处理
}

// 文件状态改变时的钩子
const handleChange = (file: any, fileList: any[]) => {
  // 更新文件列表，但不自动上传
  console.log('File changed:', file, fileList)
  // 如果启用了自动上传，立即上传文件
  if (props.autoUpload && file.status === 'ready') {
    handleUpload(file.raw)
  }
}

// 文件移除时的钩子
const handleRemove = (file: any, fileList: any[]) => {
  console.log('File removed:', file, fileList)
}

// 处理上传
const handleUpload = async (file: File) => {
  uploading.value = true
  try {
    let response: UploadResponse

    if (props.multiple && fileList.value.length > 1) {
      // 批量上传
      const files = fileList.value.map(item => item.raw!).filter(Boolean)
      const batchResponse = await uploadBatchFiles(files, props.category) as unknown as BatchUploadResponse
      // 取第一个成功的文件作为响应
      const successFile = batchResponse.files.find((f: any) => f.status === 'success')
      if (successFile) {
        response = {
          url: successFile.url!,
          relativePath: successFile.relativePath!,
          fileName: successFile.fileName
        }
      } else {
        throw new Error('批量上传失败')
      }
    } else {
      // 单文件上传
      switch (props.uploadType) {
        case 'avatar':
          response = (await uploadAvatar(file)).data
          break
        case 'question':
          response = (await uploadQuestionFile(file)).data
          break
        default:
          response = (await uploadFile(file, props.category)).data
      }
    }

    ElMessage.success('上传成功')
    emit('success', response)
    
    // 上传成功后清空文件列表
    if (props.autoUpload) {
      clearFiles()
    }
  } catch (error: any) {
    ElMessage.error(error.message || '上传失败')
    emit('error', error)
  } finally {
    uploading.value = false
  }
}

// 手动上传
const submitUpload = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请先选择文件')
    return
  }

  uploading.value = true
  try {
    if (props.multiple) {
      // 批量上传
      const files = fileList.value.map(item => item.raw!).filter(Boolean)
      const batchResponse = await uploadBatchFiles(files, props.category) as unknown as BatchUploadResponse
      
      // 检查是否有失败的文件
      const failedFiles = batchResponse.files.filter((f: any) => f.status === 'error')
      const successFiles = batchResponse.files.filter((f: any) => f.status === 'success')
      
      if (failedFiles.length > 0) {
        ElMessage.warning(`${successFiles.length}个文件上传成功，${failedFiles.length}个文件上传失败`)
      } else {
        ElMessage.success(`所有${successFiles.length}个文件上传成功`)
      }
      
      const response = {
        url: successFiles[0]?.url || '',
        relativePath: successFiles[0]?.relativePath || '',
        fileName: successFiles[0]?.fileName || '',
        category: props.category
      }
      console.log('FileUpload 批量上传响应:', response)
      emit('success', response)
      
      // 清空文件列表
      clearFiles()
    } else {
      // 单文件上传
      const file = fileList.value[0]?.raw
      if (file) {
        await handleUpload(file)
      }
    }
  } catch (error: any) {
    ElMessage.error(error.message || '上传失败')
    emit('error', error)
  } finally {
    uploading.value = false
  }
}

// 上传成功回调
const handleSuccess = (response: any, file: any) => {
  // 这个回调在我们的实现中不会被触发，因为我们阻止了默认上传
}

// 上传失败回调
const handleError = (error: any) => {
  ElMessage.error('上传失败')
  emit('error', error)
  uploading.value = false
}

// 上传进度回调
const handleProgress = (event: any) => {
  emit('progress', event.percent)
}

// 清空文件列表
const clearFiles = () => {
  uploadRef.value?.clearFiles()
  fileList.value = []
}

defineExpose({
  clearFiles,
  submitUpload
})
</script>

<style scoped>
.file-upload {
  width: 100%;
}

.upload-component {
  width: 100%;
}

.el-upload__tip {
  color: #999;
  font-size: 12px;
  margin-top: 8px;
}
</style>