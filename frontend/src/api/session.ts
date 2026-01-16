import request from '@/utils/request'
import type { QuizSession, PageResult, AnswerMode, SessionDetail, Question, UserAnswer } from '@/types/index'

// 开始会话返回类型
export interface StartSessionResult {
  session: QuizSession
  questions: Question[]
  answers: UserAnswer[]
  startIndex: number
}

// 开始答题会话
export const startSession = (data: {
  bankId: number
  mode: AnswerMode
}): Promise<StartSessionResult> => {
  return request.post('/sessions/start', data)
}

// 提交单题答案（练习模式）
export const submitAnswer = (sessionId: string, data: {
  questionId: number
  questionIndex: number
  userAnswer: string
}): Promise<{
  isCorrect: boolean
  correctAnswer: string
  analysis: string
}> => {
  return request.post(`/sessions/${sessionId}/answer`, data)
}

// 提交考试（考试模式）
export const submitExam = (sessionId: string, answers: Array<{
  questionId: number
  questionIndex: number
  userAnswer: string
}>): Promise<{
  correctCount: number
  totalQuestions: number
  accuracy: number
  results: Array<{
    questionId: number
    isCorrect: boolean
    correctAnswer: string
    userAnswer: string
  }>
}> => {
  return request.post(`/sessions/${sessionId}/submit`, { answers })
}

// 重置会话（重新开始）
export const resetSession = (bankId: number, mode: string): Promise<void> => {
  return request.post('/sessions/reset', { bankId, mode })
}

// 获取会话详情
export const getSession = (sessionId: string): Promise<QuizSession> => {
  return request.get(`/sessions/${sessionId}`)
}

// 获取会话完整详情（包含题目和答案）
export const getSessionDetail = (sessionId: string): Promise<SessionDetail> => {
  return request.get(`/sessions/${sessionId}/detail`)
}

// 获取用户进行中的会话
export const getOngoingSessions = (): Promise<QuizSession[]> => {
  return request.get('/sessions/ongoing')
}

// 获取用户的答题会话列表
export const getUserSessions = (params: {
  current?: number
  size?: number
  mode?: AnswerMode
  status?: 'ongoing' | 'completed' | 'abandoned'
  bankId?: number
}): Promise<PageResult<QuizSession>> => {
  return request.get('/sessions', { params })
}

// 获取最近的答题会话
export const getRecentSessions = (limit: number = 10): Promise<QuizSession[]> => {
  return request.get('/sessions/recent', { params: { limit } })
}

// 删除答题会话
export const deleteSession = (sessionId: number): Promise<void> => {
  return request.delete(`/sessions/${sessionId}`)
}

// 放弃会话
export const abandonSession = (sessionId: number): Promise<void> => {
  return request.put(`/sessions/${sessionId}/abandon`)
}
