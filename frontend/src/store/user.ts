import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types/index'
import * as authApi from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // 状态
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))

  // 计算属性
  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const userInfo = computed(() => user.value)
  const isAdmin = computed(() => user.value?.role === 'admin')
  const userPermissions = computed(() => user.value?.permissions || [])

  // 登录
  const login = async (loginData: { username: string; password: string; rememberMe?: boolean }) => {
    try {
      const result = await authApi.login(loginData)
      
      // 保存token和用户信息
      token.value = result.token
      user.value = result.user
      
      // 持久化存储
      localStorage.setItem('token', result.token)
      localStorage.setItem('user', JSON.stringify(result.user))
      
      return result
    } catch (error) {
      throw error
    }
  }

  // 注册
  const register = async (registerData: {
    username: string
    password: string
    confirmPassword: string
    nickname?: string
    email?: string
  }) => {
    try {
      const result = await authApi.register(registerData)
      return result
    } catch (error) {
      throw error
    }
  }

  // 带验证码的注册
  const registerWithVerification = async (registerData: {
    username: string
    password: string
    nickname?: string
    email: string
    verificationCode: string
  }) => {
    try {
      const result = await authApi.registerWithVerification(registerData)
      return result
    } catch (error) {
      throw error
    }
  }

  // 登出
  const logout = async () => {
    try {
      await authApi.logout()
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      // 清除本地状态
      token.value = null
      user.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    if (!token.value) return null
    
    try {
      const userInfo = await authApi.getProfile()
      user.value = userInfo
      localStorage.setItem('user', JSON.stringify(userInfo))
      return userInfo
    } catch (error) {
      // 如果获取用户信息失败，清除登录状态
      logout()
      throw error
    }
  }

  // 更新用户信息
  const updateUserInfo = async (updateData: Partial<User>) => {
    try {
      const result = await authApi.updateProfile(updateData)
      user.value = { ...user.value, ...result }
      localStorage.setItem('user', JSON.stringify(user.value))
      return result
    } catch (error) {
      throw error
    }
  }

  // 初始化用户状态（从localStorage恢复）
  const initUserState = () => {
    const savedUser = localStorage.getItem('user')
    if (savedUser && token.value) {
      try {
        user.value = JSON.parse(savedUser)
      } catch (error) {
        console.error('解析用户信息失败:', error)
        localStorage.removeItem('user')
      }
    }
  }

  // 检查用户名是否存在
  const checkUsername = async (username: string) => {
    try {
      return await authApi.checkUsername(username)
    } catch (error) {
      throw error
    }
  }

  // 检查邮箱是否已注册
  const checkEmail = async (email: string) => {
    try {
      return await authApi.checkEmail(email)
    } catch (error) {
      throw error
    }
  }

  // 权限检查
  const hasPermission = (permission: string) => {
    if (!user.value) return false
    if (user.value.role === 'admin') return true
    return user.value.permissions?.includes(permission) || false
  }

  // 检查是否有题目管理权限
  const hasQuestionManagePermission = () => {
    return hasPermission('question:create') ||
           hasPermission('question:edit') ||
           hasPermission('question:delete') ||
           hasPermission('question:upload') ||
           hasPermission('question:batch')
  }

  return {
    // 状态
    user,
    token,
    
    // 计算属性
    isLoggedIn,
    userInfo,
    isAdmin,
    userPermissions,
    
    // 方法
    login,
    register,
    registerWithVerification,
    logout,
    fetchUserInfo,
    updateUserInfo,
    initUserState,
    checkUsername,
    checkEmail,
    hasPermission,
    hasQuestionManagePermission
  }
})
