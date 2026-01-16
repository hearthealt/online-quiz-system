// 小程序状态管理 - 基于 Vuex 的简化版本
export class MiniStore {
  constructor() {
    this.state = {
      // 用户状态
      user: {
        isLoggedIn: false,
        userInfo: null,
        token: null
      },

      // 题目状态
      questions: {
        currentQuestion: null,
        questionList: [],
        currentIndex: 0,
        categories: [],
        loading: false
      },

      // 答题状态
      quiz: {
        answers: new Map(),
        startTime: null,
        endTime: null,
        mode: 'practice', // practice 或 exam
        sessionId: null,
        timeLeft: 0,
        isSubmitted: false
      },

      // 应用状态
      app: {
        networkStatus: 'online',
        theme: 'light',
        loading: false,
        error: null
      }
    }

    this.listeners = []
    this.cache = new Map()
  }

  // 获取状态
  getState(path) {
    if (!path) return this.state

    return path.split('.').reduce((obj, key) => obj && obj[key], this.state)
  }

  // 更新状态
  setState(path, value) {
    const keys = path.split('.')
    const lastKey = keys.pop()
    const target = keys.reduce((obj, key) => {
      if (!obj[key]) obj[key] = {}
      return obj[key]
    }, this.state)

    target[lastKey] = value
    this.notify()
  }

  // 合并状态
  mergeState(path, value) {
    const current = this.getState(path) || {}
    this.setState(path, { ...current, ...value })
  }

  // 订阅状态变化
  subscribe(listener) {
    this.listeners.push(listener)
    return () => {
      const index = this.listeners.indexOf(listener)
      if (index > -1) {
        this.listeners.splice(index, 1)
      }
    }
  }

  // 通知所有监听器
  notify() {
    this.listeners.forEach(listener => listener(this.state))
  }

  // 缓存管理
  setCache(key, data, ttl = 5 * 60 * 1000) {
    this.cache.set(key, {
      data,
      timestamp: Date.now(),
      ttl
    })
  }

  getCache(key) {
    const cached = this.cache.get(key)
    if (!cached) return null

    if (Date.now() - cached.timestamp > cached.ttl) {
      this.cache.delete(key)
      return null
    }

    return cached.data
  }

  clearCache(prefix) {
    if (prefix) {
      Array.from(this.cache.keys()).forEach(key => {
        if (key.startsWith(prefix)) {
          this.cache.delete(key)
        }
      })
    } else {
      this.cache.clear()
    }
  }

  // 持久化存储
  persist() {
    try {
      const persistData = {
        user: this.state.user,
        app: {
          theme: this.state.app.theme
        }
      }
      uni.setStorageSync('app_state', JSON.stringify(persistData))
    } catch (error) {
      console.error('状态持久化失败:', error)
    }
  }

  // 恢复持久化状态
  restore() {
    try {
      const saved = uni.getStorageSync('app_state')
      if (saved) {
        const persistData = JSON.parse(saved)
        this.mergeState('user', persistData.user || {})
        this.mergeState('app', persistData.app || {})
      }
    } catch (error) {
      console.error('状态恢复失败:', error)
    }
  }

  // Actions
  actions = {
    // 用户相关
    async login(credentials) {
      this.setState('app.loading', true)
      this.setState('app.error', null)

      try {
        const response = await this.request('/auth/login', {
          method: 'POST',
          data: credentials
        })

        this.mergeState('user', {
          isLoggedIn: true,
          userInfo: response.user,
          token: response.token
        })

        // 保存token到本地
        uni.setStorageSync('token', response.token)
        uni.setStorageSync('userInfo', JSON.stringify(response.user))

        this.persist()
        return response
      } catch (error) {
        this.setState('app.error', error.message)
        throw error
      } finally {
        this.setState('app.loading', false)
      }
    },

    logout() {
      this.setState('user', {
        isLoggedIn: false,
        userInfo: null,
        token: null
      })

      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
      this.clearCache()
      this.persist()
    },

    // 题目相关
    async fetchQuestions(params = {}) {
      const cacheKey = `questions_${JSON.stringify(params)}`
      const cached = this.getCache(cacheKey)

      if (cached) {
        this.setState('questions.questionList', cached)
        return cached
      }

      this.setState('questions.loading', true)

      try {
        const questions = await this.request('/questions/random', {
          method: 'GET',
          data: params
        })

        this.setState('questions.questionList', questions)
        this.setState('questions.currentIndex', 0)
        this.setState('questions.currentQuestion', questions[0] || null)

        this.setCache(cacheKey, questions)
        return questions
      } catch (error) {
        this.setState('app.error', error.message)
        throw error
      } finally {
        this.setState('questions.loading', false)
      }
    },

    setCurrentQuestion(index) {
      const questions = this.getState('questions.questionList')
      if (index >= 0 && index < questions.length) {
        this.setState('questions.currentIndex', index)
        this.setState('questions.currentQuestion', questions[index])
      }
    },

    // 答题相关
    startQuiz(mode = 'practice') {
      const sessionId = Date.now().toString()
      this.mergeState('quiz', {
        mode,
        sessionId,
        startTime: Date.now(),
        endTime: null,
        answers: new Map(),
        isSubmitted: false
      })
    },

    saveAnswer(questionId, answer) {
      const answers = this.getState('quiz.answers')
      answers.set(questionId, answer)
      this.setState('quiz.answers', answers)

      // 自动保存到本地
      this.saveQuizProgress()
    },

    async submitQuiz() {
      const quiz = this.getState('quiz')
      const questions = this.getState('questions.questionList')

      this.setState('quiz.endTime', Date.now())
      this.setState('app.loading', true)

      try {
        const answersArray = Array.from(quiz.answers.entries()).map(([questionId, answer]) => ({
          questionId: parseInt(questionId),
          userAnswer: answer,
          sessionId: quiz.sessionId,
          mode: quiz.mode
        }))

        const result = await this.request('/answers/submit-batch', {
          method: 'POST',
          data: answersArray
        })

        this.setState('quiz.isSubmitted', true)

        // 清除进度保存
        uni.removeStorageSync('quiz_progress')

        return result
      } catch (error) {
        this.setState('app.error', error.message)
        throw error
      } finally {
        this.setState('app.loading', false)
      }
    },

    saveQuizProgress() {
      try {
        const quiz = this.getState('quiz')
        const progress = {
          sessionId: quiz.sessionId,
          mode: quiz.mode,
          startTime: quiz.startTime,
          answers: Array.from(quiz.answers.entries()),
          currentIndex: this.getState('questions.currentIndex')
        }
        uni.setStorageSync('quiz_progress', JSON.stringify(progress))
      } catch (error) {
        console.error('保存答题进度失败:', error)
      }
    },

    loadQuizProgress() {
      try {
        const saved = uni.getStorageSync('quiz_progress')
        if (saved) {
          const progress = JSON.parse(saved)
          this.mergeState('quiz', {
            sessionId: progress.sessionId,
            mode: progress.mode,
            startTime: progress.startTime,
            answers: new Map(progress.answers)
          })
          this.setState('questions.currentIndex', progress.currentIndex || 0)
          return true
        }
      } catch (error) {
        console.error('加载答题进度失败:', error)
      }
      return false
    }
  }

  // 网络请求封装
  async request(url, options = {}) {
    const token = this.getState('user.token')

    const config = {
      url: `${this.baseURL}${url}`,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
        ...options.header
      },
      timeout: options.timeout || 30000
    }

    return new Promise((resolve, reject) => {
      uni.request({
        ...config,
        success: (res) => {
          if (res.statusCode === 200) {
            if (res.data.code === 200) {
              resolve(res.data.data)
            } else {
              reject(new Error(res.data.message || '请求失败'))
            }
          } else if (res.statusCode === 401) {
            // 未授权，跳转登录
            this.actions.logout()
            uni.reLaunch({ url: '/pages/login/login' })
            reject(new Error('登录已过期'))
          } else {
            reject(new Error(`请求失败 (${res.statusCode})`))
          }
        },
        fail: (error) => {
          console.error('网络请求失败:', error)
          reject(new Error('网络连接失败'))
        }
      })
    })
  }

  // 设置基础URL
  get baseURL() {
    // #ifdef MP-WEIXIN
    return 'https://your-api-domain.com/api'
    // #endif

    // #ifdef H5
    return '/api'
    // #endif

    // #ifdef APP-PLUS
    return 'https://your-api-domain.com/api'
    // #endif
  }
}

// 创建全局store实例
export const store = new MiniStore()

// 恢复持久化状态
store.restore()

// 导出便捷方法
export const getState = store.getState.bind(store)
export const setState = store.setState.bind(store)
export const mergeState = store.mergeState.bind(store)
export const subscribe = store.subscribe.bind(store)