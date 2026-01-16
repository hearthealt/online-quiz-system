import request from '@/utils/request'
import type { AnswerRequest, UserAnswer, AnswerStats, PageResult } from '@/types/index'

// 提交单个答案
export const submitAnswer = (data: AnswerRequest): Promise<UserAnswer> => {
  return request.post('/answers', data)
}

// 批量提交答案
export const submitAnswers = (data: AnswerRequest[]): Promise<UserAnswer[]> => {
  return request.post('/answers/batch', data)
}

// 获取答题历史
export const getAnswerHistory = (params?: {
  current?: number
  size?: number
}): Promise<PageResult<UserAnswer>> => {
  return request.get('/answers/history', { params })
}

// 获取答题统计
export const getAnswerStats = (): Promise<AnswerStats> => {
  return request.get('/answers/stats')
}

// 获取会话答题记录
export const getSessionAnswers = (sessionId: string): Promise<UserAnswer[]> => {
  return request.get(`/answers/session/${sessionId}`)
}