// API基础配置
// 注意：正式版小程序必须使用HTTPS，开发阶段可以临时使用HTTP
const BASE_URL = 'https://www.quiz-ai.online/api' // 生产环境地址
// const BASE_URL = 'http://localhost:9090/api' // 本地开发环境地址

// 请求拦截器
const request = (options) => {
  return new Promise((resolve, reject) => {
    // 获取token
    const token = uni.getStorageSync('token')
    
    // 请求配置
    const requestOptions = {
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
        ...options.header
      },
      success: (res) => {
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            resolve(res.data.data)
          } else {
            uni.showToast({
              title: res.data.message || '请求失败',
              icon: 'none',
              duration: 2000
            })
            reject(new Error(res.data.message || '请求失败'))
          }
        } else if (res.statusCode === 401) {
          // token过期，清除本地存储并跳转到登录页
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          uni.reLaunch({
            url: '/pages/login/login'
          })
          reject(new Error('登录已过期'))
        } else {
          uni.showToast({
            title: '网络错误',
            icon: 'none',
            duration: 2000
          })
          reject(new Error('网络错误'))
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '网络连接失败',
          icon: 'none',
          duration: 2000
        })
        reject(err)
      }
    }
    
    uni.request(requestOptions)
  })
}

// 用户相关API
export const authAPI = {
  // 用户登录
  login: (data) => {
    return request({
      url: '/auth/login',
      method: 'POST',
      data
    })
  },
  
  // 用户注册
  register: (data) => {
    return request({
      url: '/auth/register',
      method: 'POST',
      data
    })
  },
  
  // 发送邮箱验证码
  sendVerificationCode: (data) => {
    return request({
      url: '/auth/send-verification-code',
      method: 'POST',
      data
    })
  },
  
  // 带验证码的用户注册
  registerWithVerification: (data) => {
    return request({
      url: '/auth/register-with-verification',
      method: 'POST',
      data
    })
  },
  
  // 获取用户信息
  getProfile: () => {
    return request({
      url: '/auth/profile',
      method: 'GET'
    })
  },
  
  // 检查用户名是否存在
  checkUsername: (username) => {
    return request({
      url: '/auth/check-username',
      method: 'GET',
      data: { username }
    })
  },
  
  // 检查邮箱是否已注册
  checkEmail: (email) => {
    return request({
      url: '/auth/check-email',
      method: 'GET',
      data: { email }
    })
  },
  
  // 更新用户信息
  updateProfile: (data) => {
    return request({
      url: '/auth/profile',
      method: 'PUT',
      data
    })
  },
  
  // 用户登出
  logout: () => {
    return request({
      url: '/auth/logout',
      method: 'POST'
    })
  }
}

// 题库相关API
export const bankAPI = {
  // 获取题库列表
  getBanks: () => {
    return request({
      url: '/banks',
      method: 'GET'
    })
  },

  // 获取题库详情
  getBank: (id) => {
    return request({
      url: `/banks/${id}`,
      method: 'GET'
    })
  },

  // 获取题库的题目列表
  getBankQuestions: (bankId, params) => {
    return request({
      url: `/banks/${bankId}/questions`,
      method: 'GET',
      data: params
    })
  },

  // 获取题库的随机题目
  getBankRandomQuestions: (bankId, params) => {
    return request({
      url: `/banks/${bankId}/questions/random`,
      method: 'GET',
      data: params
    })
  }
}

// 题目相关API
export const questionAPI = {
  // 获取题目列表
  getQuestions: (params) => {
    return request({
      url: '/questions',
      method: 'GET',
      data: params
    })
  },

  // 获取题目详情
  getQuestion: (id) => {
    return request({
      url: `/questions/${id}`,
      method: 'GET'
    })
  },

  // 随机获取题目
  getRandomQuestions: (params) => {
    return request({
      url: '/questions/random',
      method: 'GET',
      data: params
    })
  },

  // 获取分类列表和题目数量
  getCategories: () => {
    return request({
      url: '/questions/categories',
      method: 'GET'
    })
  },

  // 根据ID批量获取题目
  getQuestionsByIds: (ids) => {
    return request({
      url: '/questions/batch',
      method: 'POST',
      data: { ids }
    })
  },

  // 获取分类统计
  getCategoryStats: () => {
    return request({
      url: '/questions/category-stats',
      method: 'GET'
    })
  }
}

// 答题相关API
export const answerAPI = {
  // 提交单个答案
  submitAnswer: (data) => {
    return request({
      url: '/answers',
      method: 'POST',
      data
    })
  },
  
  // 批量提交答案
  submitAnswers: (data) => {
    return request({
      url: '/answers/batch',
      method: 'POST',
      data
    })
  },
  
  // 获取答题历史
  getAnswerHistory: (params) => {
    return request({
      url: '/answers/history',
      method: 'GET',
      data: params
    })
  },
  
  // 获取答题统计
  getAnswerStats: () => {
    return request({
      url: '/answers/stats',
      method: 'GET'
    })
  },
  
  // 获取会话答题记录
  getSessionAnswers: (sessionId) => {
    return request({
      url: `/answers/session/${sessionId}`,
      method: 'GET'
    })
  }
}

// 会话相关API
export const sessionAPI = {
  // 开始答题会话 - 和Web端一致
  startSession: (data) => {
    return request({
      url: '/sessions/start',
      method: 'POST',
      data
    })
  },

  // 获取会话完整详情（包含题目和答案）- 用于继续答题
  getSessionDetail: (sessionId) => {
    return request({
      url: `/sessions/${sessionId}/detail`,
      method: 'GET'
    })
  },

  // 获取会话基本信息
  getSession: (sessionId) => {
    return request({
      url: `/sessions/${sessionId}`,
      method: 'GET'
    })
  },

  // 获取进行中的会话
  getOngoingSessions: () => {
    return request({
      url: '/sessions/ongoing',
      method: 'GET'
    })
  },

  // 重置会话
  resetSession: (bankId, mode) => {
    return request({
      url: '/sessions/reset',
      method: 'POST',
      data: { bankId, mode }
    })
  },

  // 提交单题答案（练习模式）
  submitAnswer: (sessionId, data) => {
    return request({
      url: `/sessions/${sessionId}/answer`,
      method: 'POST',
      data
    })
  },

  // 提交考试（考试模式，批量提交）
  submitExam: (sessionId, answers) => {
    return request({
      url: `/sessions/${sessionId}/submit`,
      method: 'POST',
      data: { answers }
    })
  }
}

// 收藏相关API
export const favoriteAPI = {
  // 收藏题目
  addFavorite: (questionId, bankId, notes) => {
    return request({
      url: '/favorites',
      method: 'POST',
      data: { questionId, bankId, notes }
    })
  },

  // 取消收藏（通过收藏ID）
  removeFavorite: (favoriteId) => {
    return request({
      url: `/favorites/${favoriteId}`,
      method: 'DELETE'
    })
  },

  // 通过题目ID取消收藏
  removeFavoriteByQuestionId: (questionId) => {
    return request({
      url: `/favorites/question/${questionId}`,
      method: 'DELETE'
    })
  },

  // 获取收藏列表
  getFavorites: (params) => {
    return request({
      url: '/favorites',
      method: 'GET',
      data: params
    })
  },

  // 检查是否已收藏
  checkFavorite: (questionId) => {
    return request({
      url: `/favorites/check/${questionId}`,
      method: 'GET'
    })
  },

  // 按题库分组获取收藏统计
  getStatsByBank: () => {
    return request({
      url: '/favorites/stats/by-bank',
      method: 'GET'
    })
  }
}

// 错题相关API
export const wrongQuestionAPI = {
  // 获取错题列表
  getWrongQuestions: (params) => {
    return request({
      url: '/wrong-questions',
      method: 'GET',
      data: params
    })
  },

  // 重做错题
  retryWrongQuestion: (questionId) => {
    return request({
      url: `/wrong-questions/retry/${questionId}`,
      method: 'POST'
    })
  },

  // 标记错题为已掌握
  markAsMastered: (questionId) => {
    return request({
      url: `/wrong-questions/${questionId}/mastered`,
      method: 'PUT'
    })
  },

  // 标记错题为未掌握
  markAsUnmastered: (questionId) => {
    return request({
      url: `/wrong-questions/${questionId}/unmastered`,
      method: 'PUT'
    })
  },

  // 获取错题统计
  getWrongQuestionStats: () => {
    return request({
      url: '/wrong-questions/stats',
      method: 'GET'
    })
  },

  // 获取按题库分组的错题统计
  getStatsByBank: () => {
    return request({
      url: '/wrong-questions/stats/by-bank',
      method: 'GET'
    })
  },

  // 获取错题总体统计
  getOverallStats: () => {
    return request({
      url: '/wrong-questions/stats/overall',
      method: 'GET'
    })
  }
}

// 统计相关API
export const statisticsAPI = {
  // 获取系统统计
  getSystemStats: () => {
    return request({
      url: '/statistics/system',
      method: 'GET'
    })
  },
  
  // 获取用户统计
  getUserStats: () => {
    return request({
      url: '/statistics/user',
      method: 'GET'
    })
  },
  
  // 获取今日学习数据
  getTodayStats: () => {
    return request({
      url: '/statistics/today',
      method: 'GET'
    })
  },
  
  // 获取学习进度
  getLearningProgress: () => {
    return request({
      url: '/statistics/progress',
      method: 'GET'
    })
  },
  
  // 获取学习历史
  getLearningHistory: (params) => {
    return request({
      url: '/statistics/history',
      method: 'GET',
      data: params
    })
  },
  
  // 获取学习趋势
  getLearningTrend: (params) => {
    return request({
      url: '/statistics/trend',
      method: 'GET',
      data: params
    })
  }
}

export default {
  authAPI,
  bankAPI,
  questionAPI,
  answerAPI,
  sessionAPI,
  favoriteAPI,
  wrongQuestionAPI,
  statisticsAPI
}
