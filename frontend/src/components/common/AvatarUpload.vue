<template>
  <div class="avatar-upload">
    <el-upload
      class="avatar-uploader"
      :action="uploadAction"
      :before-upload="beforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      :show-file-list="false"
      :disabled="uploading"
    >
      <div class="avatar-container">
        <el-image
          v-if="avatarUrl"
          :src="avatarUrl"
          class="avatar"
          fit="cover"
          :preview-src-list="[avatarUrl]"
          :initial-index="0"
          :z-index="9999"
        />
        <div v-else class="avatar-placeholder">
          <el-icon class="avatar-uploader-icon">
            <Plus />
          </el-icon>
          <div class="upload-text">上传头像</div>
        </div>

        <div class="avatar-overlay" v-if="!uploading">
          <el-icon><Camera /></el-icon>
          <span>更换头像</span>
        </div>

        <div class="avatar-loading" v-if="uploading">
          <el-icon class="is-loading"><Loading /></el-icon>
        </div>
      </div>
    </el-upload>

    <div class="avatar-tips">
      <p>支持 JPG、PNG、GIF、WEBP 格式</p>
      <p>文件大小不超过 5MB</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Camera, Loading } from '@element-plus/icons-vue'
import { uploadAvatar } from '@/api/upload'
import type { UploadResponse } from '@/types/index'
import type { UploadProps } from 'element-plus'

interface Props {
  modelValue?: string
  size?: number
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  size: 120,
  disabled: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  success: [response: UploadResponse]
  error: [error: any]
}>()

const uploading = ref(false)

// 虚拟上传地址
const uploadAction = computed(() => '#')

// 当前头像URL
const avatarUrl = computed(() => props.modelValue)

// 上传前验证
const beforeUpload: UploadProps['beforeUpload'] = async (rawFile) => {
  const isImage = rawFile.type.startsWith('image/')
  const isLt5M = rawFile.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片格式的文件!')
    return false
  }

  if (!isLt5M) {
    ElMessage.error('上传头像图片大小不能超过 5MB!')
    return false
  }

  // 开始上传
  uploading.value = true
  try {
    const response = await uploadAvatar(rawFile) as unknown as UploadResponse
    handleSuccess(response)
  } catch (error: any) {
    handleError(error)
  }

  return false // 阻止默认上传行为
}

// 上传成功处理
const handleSuccess = (response: UploadResponse) => {
  uploading.value = false
  emit('update:modelValue', response.url)
  emit('success', response)
  ElMessage.success('头像上传成功!')
}

// 上传失败处理
const handleError = (error: any) => {
  uploading.value = false
  emit('error', error)
  ElMessage.error(error.message || '头像上传失败!')
}
</script>

<style scoped>
.avatar-upload {
  text-align: center;
}

.avatar-uploader {
  display: inline-block;
}

.avatar-container {
  position: relative;
  width: v-bind('props.size + "px"');
  height: v-bind('props.size + "px"');
  border: 2px dashed #d9d9d9;
  border-radius: 50%;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.3s;
}

.avatar-container:hover {
  border-color: var(--el-color-primary);
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #8c939d;
}

.avatar-uploader-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 14px;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  border-radius: 50%;
}

.avatar-container:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay .el-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.avatar-overlay span {
  font-size: 12px;
}

.avatar-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.avatar-loading .el-icon {
  font-size: 24px;
  color: var(--el-color-primary);
}

.avatar-tips {
  margin-top: 10px;
  color: #999;
  font-size: 12px;
}

.avatar-tips p {
  margin: 2px 0;
}
</style>