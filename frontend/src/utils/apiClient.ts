import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

// 请求配置接口
interface RequestConfig extends AxiosRequestConfig {
  retry?: number // 重试次数
  retryDelay?: number // 重试延迟
  showError?: boolean // 是否显示错误消息
  showLoading?: boolean // 是否显示加载状态
}

// 响应数据接口
interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 创建优化的请求实例
class ApiClient {
  private instance: AxiosInstance
  private requestInterceptors: number[] = []
  private responseInterceptors: number[] = []

  constructor() {
    this.instance = axios.create({
      baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json'
      }
    })

    this.setupInterceptors()
  }

  private setupInterceptors() {
    // 请求拦截器
    const requestInterceptor = this.instance.interceptors.request.use(
      (config) => {
        const userStore = useUserStore()

        // 添加认证token
        if (userStore.token) {
          config.headers.Authorization = `Bearer ${userStore.token}`
        }

        // 添加请求ID用于调试
        config.headers['X-Request-ID'] = this.generateRequestId()

        return config
      },
      (error) => {
        return Promise.reject(error)
      }
    )

    // 响应拦截器
    const responseInterceptor = this.instance.interceptors.response.use(
      (response: AxiosResponse<ApiResponse>) => {
        const { data } = response

        // 统一处理业务错误
        if (data.code !== 200) {
          const error = new Error(data.message || '请求失败')
          ;(error as any).code = data.code
          throw error
        }

        return data.data
      },
      async (error) => {
        const config = error.config as RequestConfig

        // 处理网络错误
        if (!error.response) {
          if (this.shouldRetry(config)) {
            return this.retryRequest(config)
          }
          ElMessage.error('网络连接失败，请检查网络设置')
          return Promise.reject(error)
        }

        const { status } = error.response

        switch (status) {
          case 401:
            this.handleUnauthorized()
            break
          case 403:
            ElMessage.error('权限不足，无法访问该资源')
            break
          case 404:
            ElMessage.error('请求的资源不存在')
            break
          case 500:
            ElMessage.error('服务器内部错误，请稍后重试')
            break
          default:
            if (config.showError !== false) {
              const message = error.response?.data?.message || '请求失败'
              ElMessage.error(message)
            }
        }

        return Promise.reject(error)
      }
    )

    this.requestInterceptors.push(requestInterceptor)
    this.responseInterceptors.push(responseInterceptor)
  }

  private generateRequestId(): string {
    return Date.now().toString(36) + Math.random().toString(36).substr(2)
  }

  private shouldRetry(config: RequestConfig): boolean {
    const retry = config.retry || 0
    const method = config.method?.toLowerCase()

    // 只对GET请求进行重试，且重试次数小于限制
    return method === 'get' && retry < 3
  }

  private async retryRequest(config: RequestConfig): Promise<any> {
    const retryCount = (config.retry || 0) + 1
    const retryDelay = config.retryDelay || 1000 * retryCount

    console.warn(`请求重试 ${retryCount}/3: ${config.url}`)

    // 等待重试延迟
    await new Promise(resolve => setTimeout(resolve, retryDelay))

    // 更新重试次数
    config.retry = retryCount

    return this.instance.request(config)
  }

  private handleUnauthorized() {
    const userStore = useUserStore()

    ElMessage.error('登录已过期，请重新登录')

    // 清除用户状态
    userStore.logout()

    // 跳转到登录页
    const currentPath = window.location.pathname
    if (currentPath !== '/login') {
      window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`
    }
  }

  // GET请求
  get<T = any>(url: string, config?: RequestConfig): Promise<T> {
    return this.instance.get(url, config)
  }

  // POST请求
  post<T = any>(url: string, data?: any, config?: RequestConfig): Promise<T> {
    return this.instance.post(url, data, config)
  }

  // PUT请求
  put<T = any>(url: string, data?: any, config?: RequestConfig): Promise<T> {
    return this.instance.put(url, data, config)
  }

  // DELETE请求
  delete<T = any>(url: string, config?: RequestConfig): Promise<T> {
    return this.instance.delete(url, config)
  }

  // 文件上传
  upload<T = any>(
    url: string,
    file: File,
    options?: {
      onProgress?: (progress: number) => void
      timeout?: number
    }
  ): Promise<T> {
    const formData = new FormData()
    formData.append('file', file)

    return this.instance.post(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      timeout: options?.timeout || 60000,
      onUploadProgress: (progressEvent) => {
        if (options?.onProgress && progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          options.onProgress(progress)
        }
      }
    })
  }

  // 批量请求
  async batch<T = any>(requests: Array<() => Promise<T>>): Promise<T[]> {
    try {
      return await Promise.all(requests.map(request => request()))
    } catch (error) {
      console.error('批量请求失败:', error)
      throw error
    }
  }

  // 串行请求（按顺序执行）
  async sequence<T = any>(requests: Array<() => Promise<T>>): Promise<T[]> {
    const results: T[] = []

    for (const request of requests) {
      try {
        const result = await request()
        results.push(result)
      } catch (error) {
        console.error('串行请求失败:', error)
        throw error
      }
    }

    return results
  }

  // 请求缓存
  private cache = new Map<string, { data: any; timestamp: number; ttl: number }>()

  getCached<T = any>(
    url: string,
    config?: RequestConfig & { ttl?: number }
  ): Promise<T> {
    const cacheKey = `${url}?${JSON.stringify(config?.params || {})}`
    const cached = this.cache.get(cacheKey)
    const ttl = config?.ttl || 5 * 60 * 1000 // 默认5分钟

    if (cached && Date.now() - cached.timestamp < cached.ttl) {
      return Promise.resolve(cached.data)
    }

    return this.get<T>(url, config).then(data => {
      this.cache.set(cacheKey, {
        data,
        timestamp: Date.now(),
        ttl
      })
      return data
    })
  }

  // 清除缓存
  clearCache(prefix?: string) {
    if (prefix) {
      const keysToDelete = Array.from(this.cache.keys()).filter(key =>
        key.startsWith(prefix)
      )
      keysToDelete.forEach(key => this.cache.delete(key))
    } else {
      this.cache.clear()
    }
  }

  // 销毁实例
  destroy() {
    this.requestInterceptors.forEach(id => {
      this.instance.interceptors.request.eject(id)
    })
    this.responseInterceptors.forEach(id => {
      this.instance.interceptors.response.eject(id)
    })
    this.cache.clear()
  }
}

// 导出单例实例
export const apiClient = new ApiClient()

// 导出默认的request方法以保持兼容性
export default apiClient