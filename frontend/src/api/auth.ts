import request from '@/utils/request'
import type { LoginRequest, RegisterRequest, RegisterWithVerificationRequest, SendVerificationCodeRequest, User } from '@/types/index'

/**
 * 用户登录
 */
export const login = (data: LoginRequest): Promise<{ token: string; user: User }> => {
  return request.post('/auth/login', data)
}

/**
 * 用户注册
 */
export const register = (data: RegisterRequest): Promise<User> => {
  return request.post('/auth/register', data)
}

/**
 * 发送邮箱验证码
 */
export const sendVerificationCode = (data: SendVerificationCodeRequest): Promise<void> => {
  return request.post('/auth/send-verification-code', data)
}

/**
 * 带验证码的用户注册
 */
export const registerWithVerification = (data: RegisterWithVerificationRequest): Promise<User> => {
  return request.post('/auth/register-with-verification', data)
}

/**
 * 获取当前用户信息
 */
export const getProfile = (): Promise<User> => {
  return request.get('/auth/profile')
}

/**
 * 更新用户信息
 */
export const updateProfile = (data: Partial<User>): Promise<User> => {
  return request.put('/auth/profile', data)
}

/**
 * 检查用户名是否存在
 */
export const checkUsername = (username: string): Promise<boolean> => {
  return request.get('/auth/check-username', { params: { username } })
}

/**
 * 检查邮箱是否已注册
 */
export const checkEmail = (email: string): Promise<boolean> => {
  return request.get('/auth/check-email', { params: { email } })
}

/**
 * 用户登出
 */
export const logout = (): Promise<void> => {
  return request.post('/auth/logout')
}

/**
 * 修改密码
 */
export const updatePassword = (data: {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}): Promise<void> => {
  return request.put('/auth/password', data)
}