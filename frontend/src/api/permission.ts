import request from '@/utils/request'
import type { User, UserQueryParams, UpdatePermissionsParams, UpdateUserStatusParams, ResetPasswordParams, PageResult } from '@/types/index'

/**
 * 获取用户列表（分页）
 */
export const getUserList = (params: UserQueryParams): Promise<PageResult<User>> => {
  return request.get<PageResult<User>>('/admin/users', { params }) as unknown as Promise<PageResult<User>>
}

/**
 * 更新用户权限
 */
export const updateUserPermissions = (data: UpdatePermissionsParams) => {
  return request.put('/admin/users/permissions', data)
}

/**
 * 更新用户状态
 */
export const updateUserStatus = (data: UpdateUserStatusParams) => {
  return request.put('/admin/users/status', data)
}

/**
 * 重置用户密码
 */
export const resetUserPassword = (data: ResetPasswordParams) => {
  return request.put('/admin/users/reset-password', data)
}

