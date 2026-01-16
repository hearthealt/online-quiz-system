/**
 * Token工具类
 */

// 解析JWT token payload
export function parseJwt(token: string) {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => {
          return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
        })
        .join('')
    )
    return JSON.parse(jsonPayload)
  } catch (error) {
    console.error('Token解析失败:', error)
    return null
  }
}

// 检查token是否过期
export function isTokenExpired(token: string): boolean {
  try {
    const payload = parseJwt(token)
    if (!payload || !payload.exp) {
      return true
    }
    
    // JWT的exp是以秒为单位的时间戳
    const currentTime = Math.floor(Date.now() / 1000)
    return payload.exp < currentTime
  } catch (error) {
    console.error('Token过期检查失败:', error)
    return true
  }
}

// 获取token剩余时间（秒）
export function getTokenRemainingTime(token: string): number {
  try {
    const payload = parseJwt(token)
    if (!payload || !payload.exp) {
      return 0
    }
    
    const currentTime = Math.floor(Date.now() / 1000)
    return Math.max(0, payload.exp - currentTime)
  } catch (error) {
    console.error('获取token剩余时间失败:', error)
    return 0
  }
}

// 格式化剩余时间
export function formatRemainingTime(seconds: number): string {
  if (seconds <= 0) return '已过期'
  
  const days = Math.floor(seconds / (24 * 60 * 60))
  const hours = Math.floor((seconds % (24 * 60 * 60)) / (60 * 60))
  const minutes = Math.floor((seconds % (60 * 60)) / 60)
  
  if (days > 0) {
    return `${days}天${hours}小时`
  } else if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else {
    return `${minutes}分钟`
  }
}

// 检查并清理过期token
export function checkAndCleanExpiredToken(): boolean {
  const token = localStorage.getItem('token')
  if (!token) {
    return false
  }
  
  if (isTokenExpired(token)) {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    return false
  }
  
  return true
}
