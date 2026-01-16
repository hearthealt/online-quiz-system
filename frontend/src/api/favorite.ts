import request from '@/utils/request'
import type { Favorite, FavoriteWithDetail, PageResult } from '@/types/index'

// 获取收藏列表（带题目详情）
export const getFavorites = (params: {
  current?: number
  size?: number
  keyword?: string
}): Promise<PageResult<FavoriteWithDetail>> => {
  return request.get('/favorites', { params })
}

// 添加收藏
export const addFavorite = (questionId: number, bankId?: number, notes?: string): Promise<Favorite> => {
  return request.post('/favorites', { questionId, bankId, notes })
}

// 取消收藏
export const removeFavorite = (favoriteId: number): Promise<void> => {
  return request.delete(`/favorites/${favoriteId}`)
}

// 通过题目ID取消收藏
export const removeFavoriteByQuestionId = (questionId: number): Promise<void> => {
  return request.delete(`/favorites/question/${questionId}`)
}

// 批量取消收藏
export const batchRemoveFavorites = (favoriteIds: number[]): Promise<void> => {
  return request.delete('/favorites/batch', { data: { ids: favoriteIds } })
}

// 更新收藏备注
export const updateFavoriteNotes = (favoriteId: number, notes: string): Promise<Favorite> => {
  return request.put(`/favorites/${favoriteId}/notes`, notes, {
    headers: {
      'Content-Type': 'text/plain'
    }
  })
}

// 检查题目是否已收藏
export const checkFavoriteStatus = (questionId: number): Promise<boolean> => {
  return request.get(`/favorites/check/${questionId}`)
}

// 获取收藏的题目ID列表
export const getFavoriteQuestionIds = (): Promise<number[]> => {
  return request.get('/favorites/question-ids')
}

// 获取收藏数量
export const getFavoriteCount = (): Promise<number> => {
  return request.get('/favorites/count')
}

// 按题库分组获取收藏统计
export const getFavoriteStatsByBank = (): Promise<{ bankId: number, bankName: string, count: number }[]> => {
  return request.get('/favorites/stats/by-bank')
}
