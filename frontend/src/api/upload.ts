import request from '@/utils/request'
import type { UploadResponse, BatchUploadResponse } from '@/types/index'

/**
 * 上传头像
 */
export const uploadAvatar = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)

  return request<UploadResponse>({
    url: '/upload/avatar',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传题库文件
 */
export const uploadQuestionFile = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)

  return request<UploadResponse>({
    url: '/upload/questions',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传普通文件
 */
export const uploadFile = (file: File, category: string = 'common') => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('category', category)

  return request<UploadResponse>({
    url: '/upload/file',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 批量上传文件
 */
export const uploadBatchFiles = (files: File[], category: string = 'common') => {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  formData.append('category', category)

  return request<BatchUploadResponse>({
    url: '/upload/batch',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除文件
 */
export const deleteFile = (fileUrl: string) => {
  return request<void>({
    url: '/upload/file',
    method: 'DELETE',
    params: { url: fileUrl }
  })
}