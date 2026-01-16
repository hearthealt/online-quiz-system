// 本地存储工具类
export const storage = {
  // 设置存储
  set(key, value) {
    try {
      uni.setStorageSync(key, value)
      return true
    } catch (error) {
      console.error('存储失败:', error)
      return false
    }
  },
  
  // 获取存储
  get(key, defaultValue = null) {
    try {
      const value = uni.getStorageSync(key)
      return value !== '' ? value : defaultValue
    } catch (error) {
      console.error('获取存储失败:', error)
      return defaultValue
    }
  },
  
  // 删除存储
  remove(key) {
    try {
      uni.removeStorageSync(key)
      return true
    } catch (error) {
      console.error('删除存储失败:', error)
      return false
    }
  },
  
  // 清空存储
  clear() {
    try {
      uni.clearStorageSync()
      return true
    } catch (error) {
      console.error('清空存储失败:', error)
      return false
    }
  },
  
  // 检查是否存在
  has(key) {
    try {
      const value = uni.getStorageSync(key)
      return value !== ''
    } catch (error) {
      return false
    }
  }
}

// 用户信息存储
export const userStorage = {
  // 保存用户信息
  saveUser(userInfo) {
    return storage.set('userInfo', userInfo)
  },
  
  // 获取用户信息
  getUser() {
    return storage.get('userInfo', {})
  },
  
  // 清除用户信息
  clearUser() {
    storage.remove('userInfo')
    storage.remove('token')
  },
  
  // 保存token
  saveToken(token, expiresIn = 7 * 24 * 60 * 60 * 1000) { // 默认7天过期
    const success = storage.set('token', token)
    if (success) {
      // 计算过期时间
      const expiryTime = Date.now() + expiresIn
      storage.set('tokenExpiry', expiryTime)
    }
    return success
  },
  
  // 获取token
  getToken() {
    return storage.get('token', '')
  },
  
  // 检查token是否过期
  isTokenExpired() {
    const expiryTime = storage.get('tokenExpiry', 0)
    return expiryTime > 0 && Date.now() > expiryTime
  },
  
  // 检查是否已登录
  isLoggedIn() {
    return storage.has('token') && storage.has('userInfo') && !this.isTokenExpired()
  },
  
  // 清除所有登录信息
  clearLogin() {
    storage.remove('userInfo')
    storage.remove('token')
    storage.remove('tokenExpiry')
  }
}

// 答题会话存储
export const sessionStorage = {
  // 保存当前会话
  saveCurrentSession(session) {
    return storage.set('currentSession', session)
  },
  
  // 获取当前会话
  getCurrentSession() {
    return storage.get('currentSession', null)
  },
  
  // 清除当前会话
  clearCurrentSession() {
    storage.remove('currentSession')
  },
  
  // 保存答题进度
  saveProgress(progress) {
    return storage.set('quizProgress', progress)
  },
  
  // 获取答题进度
  getProgress() {
    return storage.get('quizProgress', {})
  },
  
  // 清除答题进度
  clearProgress() {
    storage.remove('quizProgress')
  }
}

export default {
  storage,
  userStorage,
  sessionStorage
}
