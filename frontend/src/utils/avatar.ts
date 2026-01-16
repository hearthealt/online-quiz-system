/**
 * 获取用户头像 URL
 * 如果用户没有设置头像，返回默认生成的头像
 */
export const getAvatarUrl = (avatar?: string | null, username?: string): string => {
  if (avatar && avatar.trim()) {
    return avatar
  }

  // 使用 DiceBear API 生成默认头像
  const seed = username || 'default'
  return `https://api.dicebear.com/7.x/avataaars/svg?seed=${encodeURIComponent(seed)}`
}

/**
 * 获取用户显示名称
 */
export const getDisplayName = (nickname?: string | null, username?: string): string => {
  return nickname && nickname.trim() ? nickname : username || '未知用户'
}

/**
 * 获取用户名首字母（用于头像占位符）
 */
export const getUserInitial = (nickname?: string | null, username?: string): string => {
  const displayName = getDisplayName(nickname, username)
  return displayName.charAt(0).toUpperCase()
}