import request from '@/utils/request'
import type { QuizSession } from '@/types/index'

// 答题会话列表返回类型
export interface SessionListResult {
  records: QuizSession[]
  total: number
  current: number
  size: number
}

// 获取答题会话列表（管理员）
export const getSessionList = (params: {
  current?: number
  size?: number
  userId?: number
  bankId?: number
  mode?: string
  status?: string
  deleted?: number
}): Promise<SessionListResult> => {
  return request.get('/admin/sessions', { params })
}
