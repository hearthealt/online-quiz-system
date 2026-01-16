import request from '@/utils/request'
import type {
  SystemStats,
  UserStats,
  RankingItem,
  DetailedStats,
  AnswerTrendItem,
  TypeStatsItem
} from '@/types/index'

// 获取系统统计数据
export const getSystemStats = (): Promise<SystemStats> => {
  return request.get('/statistics/system')
}

// 获取用户统计数据
export const getUserStats = (): Promise<UserStats> => {
  return request.get('/statistics/user')
}

// 获取排行榜数据
export const getRankingData = (params?: {
  type?: string
  period?: string
}): Promise<RankingItem[]> => {
  return request.get('/statistics/ranking', { params })
}

// 获取详细统计数据
export const getDetailedStats = (): Promise<DetailedStats> => {
  return request.get('/statistics/detailed')
}

// 获取答题趋势数据
export const getAnswerTrend = (params: {
  period: string
}): Promise<AnswerTrendItem[]> => {
  return request.get('/statistics/trend', { params })
}

// 获取题型统计数据
export const getTypeStats = (): Promise<TypeStatsItem[]> => {
  return request.get('/statistics/type')
}

// 导出统计报告
export const exportStatisticsReport = (params: {
  format: string
}): Promise<Blob> => {
  return request.get('/statistics/export', {
    params,
    responseType: 'blob'
  })
}
