import request from '@/utils/request'
import type { Question, PageResult, QuestionType } from '@/types/index'

// 分页查询题目
export const getQuestions = (params: {
  current?: number
  size?: number
  bankId?: number
  type?: QuestionType
  keyword?: string
}): Promise<PageResult<Question>> => {
  return request.get('/questions', { params })
}

// 获取题目详情
export const getQuestion = (id: number): Promise<Question> => {
  return request.get(`/questions/${id}`)
}

// 获取题库的所有题目
export const getQuestionsByBankId = (bankId: number): Promise<Question[]> => {
  return request.get(`/questions/bank/${bankId}`)
}

// 批量获取题目详情
export const getQuestionsByIds = (ids: number[]): Promise<Question[]> => {
  return request.post('/questions/batch', { ids }, {
    timeout: 60000 // 1分钟超时，适合批量获取
  })
}

// 创建题目
export const createQuestion = (bankId: number, data: Question): Promise<Question> => {
  return request.post(`/questions?bankId=${bankId}`, data)
}

// 更新题目
export const updateQuestion = (id: number, data: Question): Promise<Question> => {
  return request.put(`/questions/${id}`, data)
}

// 删除题目
export const deleteQuestion = (id: number): Promise<void> => {
  return request.delete(`/questions/${id}`)
}

// 批量启用题目
export const batchEnableQuestions = (ids: number[]): Promise<void> => {
  return request.put('/questions/batch/enable', { ids }, {
    timeout: 60000
  })
}

// 批量禁用题目
export const batchDisableQuestions = (ids: number[]): Promise<void> => {
  return request.put('/questions/batch/disable', { ids }, {
    timeout: 60000
  })
}

// 批量删除题目
export const batchDeleteQuestions = (ids: number[]): Promise<void> => {
  return request.delete('/questions/batch', {
    data: { ids },
    timeout: 60000
  })
}

// 导出题目
export const exportQuestions = (params: {
  bankId?: number
  type?: string
  keyword?: string
}): Promise<Blob> => {
  return request.get('/questions/export', {
    params,
    responseType: 'blob',
    timeout: 300000
  })
}
