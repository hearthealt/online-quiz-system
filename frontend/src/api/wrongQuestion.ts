import request from '@/utils/request'
import type { WrongQuestion, WrongQuestionWithDetail, PageResult, WrongQuestionStats } from '@/types/index'

// 获取错题列表（带题目详情，支持分页和按题库筛选）
export const getWrongQuestions = (params: {
  current?: number
  size?: number
  status?: number
  keyword?: string
  bankId?: number
}): Promise<PageResult<WrongQuestionWithDetail>> => {
  return request.get('/wrong-questions', { params })
}

// 获取错题详情
export const getWrongQuestion = (id: number): Promise<WrongQuestionWithDetail> => {
  return request.get(`/wrong-questions/${id}`)
}

// 标记错题为已掌握
export const markWrongQuestionAsMastered = (questionId: number): Promise<void> => {
  return request.put(`/wrong-questions/${questionId}/mastered`)
}

// 标记错题为未掌握
export const markWrongQuestionAsUnmastered = (questionId: number): Promise<void> => {
  return request.put(`/wrong-questions/${questionId}/unmastered`)
}

// 批量标记错题状态
export const batchUpdateWrongQuestionStatus = (questionIds: number[], status: number): Promise<void> => {
  return request.put('/wrong-questions/batch-status', { questionIds, status })
}

// 删除错题记录
export const deleteWrongQuestion = (questionId: number): Promise<void> => {
  return request.delete(`/wrong-questions/${questionId}`)
}

// 获取错题统计
export const getWrongQuestionStats = (): Promise<WrongQuestionStats> => {
  return request.get('/wrong-questions/stats')
}

// 获取错题数量
export const getWrongQuestionCount = (status?: number): Promise<number> => {
  return request.get('/wrong-questions/count', { params: { status } })
}

// 获取按题库分组的错题统计
export const getWrongQuestionStatsByBank = (): Promise<{
  bankId: number
  bankName: string
  total: number
  masteredCount: number
  unmasteredCount: number
}[]> => {
  return request.get('/wrong-questions/stats/by-bank')
}

// 获取错题总体统计
export const getWrongQuestionOverallStats = (): Promise<{
  total: number
  mastered: number
  unmastered: number
  accuracy: number
}> => {
  return request.get('/wrong-questions/stats/overall')
}
