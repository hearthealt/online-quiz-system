import request from '@/utils/request'
import type { QuestionBank, PageResult, ImportResult } from '@/types/index'

// 获取题库列表
export const getBanks = (params?: {
  current?: number
  size?: number
  keyword?: string
}): Promise<PageResult<QuestionBank>> => {
  return request.get('/banks', { params })
}

// 获取所有题库（不分页）
export const getAllBanks = (): Promise<QuestionBank[]> => {
  return request.get('/banks')
}

// 获取题库详情
export const getBank = (id: number): Promise<QuestionBank> => {
  return request.get(`/banks/${id}`)
}

// 创建题库
export const createBank = (data: {
  name: string
  description?: string
}): Promise<QuestionBank> => {
  return request.post('/banks', data)
}

// 更新题库
export const updateBank = (id: number, data: {
  name?: string
  description?: string
}): Promise<QuestionBank> => {
  return request.put(`/banks/${id}`, data)
}

// 删除题库
export const deleteBank = (id: number): Promise<void> => {
  return request.delete(`/banks/${id}`)
}

// 导入题目到题库（直接上传文件）
export const importQuestions = (file: File, mode: string = 'overwrite'): Promise<ImportResult> => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('mode', mode)
  return request.post('/banks/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 300000 // 5分钟超时，适合大数据量导入
  })
}

// 导入题目到指定题库
export const importQuestionsToBank = (bankId: number, file: File, mode: string = 'append'): Promise<ImportResult> => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('mode', mode)
  return request.post(`/banks/${bankId}/import`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 300000
  })
}

// 获取题库统计信息
export const getBankStats = (id: number): Promise<{
  totalQuestions: number
  singleCount: number
  multipleCount: number
  judgeCount: number
  essayCount: number
}> => {
  return request.get(`/banks/${id}/stats`)
}
