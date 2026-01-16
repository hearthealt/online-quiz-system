import request from '@/utils/request'
import type { ContactRequest } from '@/types/index'

/**
 * 发送联系邮件
 */
export const sendContactEmail = (data: ContactRequest): Promise<void> => {
  return request.post('/contact/send', data)
}

/**
 * 获取管理员邮箱
 */
export const getAdminEmail = (): Promise<string> => {
  return request.get('/contact/admin-email')
}
