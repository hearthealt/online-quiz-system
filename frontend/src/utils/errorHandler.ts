import { ElMessage, ElNotification, ElMessageBox } from 'element-plus'

// 错误类型枚举
export enum ErrorType {
  NETWORK = 'NETWORK',
  BUSINESS = 'BUSINESS',
  VALIDATION = 'VALIDATION',
  PERMISSION = 'PERMISSION',
  SYSTEM = 'SYSTEM'
}

// 错误级别枚举
export enum ErrorLevel {
  INFO = 'info',
  WARNING = 'warning',
  ERROR = 'error',
  CRITICAL = 'critical'
}

// 错误信息接口
export interface ErrorInfo {
  type: ErrorType
  level: ErrorLevel
  code?: string | number
  message: string
  details?: any
  timestamp: number
  url?: string
  userId?: string
  requestId?: string
}

// 错误处理配置
interface ErrorHandlerConfig {
  showMessage?: boolean
  showNotification?: boolean
  logToConsole?: boolean
  reportToServer?: boolean
  retryable?: boolean
}

/**
 * 统一错误处理器
 */
class ErrorHandler {
  private errorLog: ErrorInfo[] = []
  private maxLogSize = 100

  /**
   * 处理错误
   */
  handle(error: any, config: ErrorHandlerConfig = {}) {
    const errorInfo = this.parseError(error)

    // 记录错误日志
    this.logError(errorInfo)

    // 显示错误信息
    this.showError(errorInfo, config)

    // 上报错误
    if (config.reportToServer !== false) {
      this.reportError(errorInfo)
    }

    return errorInfo
  }

  /**
   * 解析错误信息
   */
  private parseError(error: any): ErrorInfo {
    const timestamp = Date.now()
    let errorInfo: ErrorInfo = {
      type: ErrorType.SYSTEM,
      level: ErrorLevel.ERROR,
      message: '未知错误',
      timestamp,
      url: window.location.href
    }

    if (error?.response) {
      // HTTP错误
      const { status, data } = error.response

      errorInfo.code = status
      errorInfo.message = data?.message || this.getDefaultHttpErrorMessage(status)
      errorInfo.details = data
      errorInfo.requestId = error.config?.headers?.['X-Request-ID']

      switch (status) {
        case 400:
          errorInfo.type = ErrorType.VALIDATION
          errorInfo.level = ErrorLevel.WARNING
          break
        case 401:
          errorInfo.type = ErrorType.PERMISSION
          errorInfo.level = ErrorLevel.WARNING
          break
        case 403:
          errorInfo.type = ErrorType.PERMISSION
          errorInfo.level = ErrorLevel.ERROR
          break
        case 404:
          errorInfo.type = ErrorType.BUSINESS
          errorInfo.level = ErrorLevel.WARNING
          break
        case 500:
        case 502:
        case 503:
          errorInfo.type = ErrorType.SYSTEM
          errorInfo.level = ErrorLevel.CRITICAL
          break
        default:
          errorInfo.type = ErrorType.BUSINESS
          break
      }
    } else if (error?.code) {
      // 业务错误
      errorInfo.type = ErrorType.BUSINESS
      errorInfo.code = error.code
      errorInfo.message = error.message || '业务处理失败'
      errorInfo.details = error.details
    } else if (error?.message) {
      // 网络错误或其他错误
      if (error.message.includes('Network Error') || error.message.includes('timeout')) {
        errorInfo.type = ErrorType.NETWORK
        errorInfo.message = '网络连接失败，请检查网络设置'
      } else {
        errorInfo.message = error.message
      }
    } else if (typeof error === 'string') {
      errorInfo.message = error
    }

    return errorInfo
  }

  /**
   * 获取默认HTTP错误消息
   */
  private getDefaultHttpErrorMessage(status: number): string {
    const messages: Record<number, string> = {
      400: '请求参数错误',
      401: '未登录或登录已过期',
      403: '权限不足，无法访问',
      404: '请求的资源不存在',
      405: '请求方法不被允许',
      408: '请求超时',
      409: '请求冲突',
      422: '请求参数验证失败',
      429: '请求过于频繁，请稍后重试',
      500: '服务器内部错误',
      502: '网关错误',
      503: '服务暂不可用',
      504: '网关超时'
    }

    return messages[status] || `请求失败 (${status})`
  }

  /**
   * 显示错误信息
   */
  private showError(errorInfo: ErrorInfo, config: ErrorHandlerConfig) {
    const { showMessage = true, showNotification = false } = config

    if (!showMessage && !showNotification) return

    const message = errorInfo.message
    const type = this.getElementUIType(errorInfo.level)

    if (showNotification) {
      ElNotification({
        title: this.getErrorTitle(errorInfo.type),
        message,
        type,
        duration: this.getNotificationDuration(errorInfo.level),
        showClose: true
      })
    } else if (showMessage) {
      ElMessage({
        message,
        type,
        duration: this.getMessageDuration(errorInfo.level),
        showClose: errorInfo.level === ErrorLevel.CRITICAL
      })
    }
  }

  /**
   * 获取错误标题
   */
  private getErrorTitle(type: ErrorType): string {
    const titles: Record<ErrorType, string> = {
      [ErrorType.NETWORK]: '网络错误',
      [ErrorType.BUSINESS]: '操作失败',
      [ErrorType.VALIDATION]: '参数错误',
      [ErrorType.PERMISSION]: '权限错误',
      [ErrorType.SYSTEM]: '系统错误'
    }
    return titles[type]
  }

  /**
   * 转换错误级别为Element UI类型
   */
  private getElementUIType(level: ErrorLevel): 'success' | 'warning' | 'info' | 'error' {
    switch (level) {
      case ErrorLevel.INFO:
        return 'info'
      case ErrorLevel.WARNING:
        return 'warning'
      case ErrorLevel.ERROR:
      case ErrorLevel.CRITICAL:
        return 'error'
      default:
        return 'error'
    }
  }

  /**
   * 获取消息显示持续时间
   */
  private getMessageDuration(level: ErrorLevel): number {
    switch (level) {
      case ErrorLevel.INFO:
        return 3000
      case ErrorLevel.WARNING:
        return 4000
      case ErrorLevel.ERROR:
        return 5000
      case ErrorLevel.CRITICAL:
        return 0 // 不自动关闭
      default:
        return 4000
    }
  }

  /**
   * 获取通知显示持续时间
   */
  private getNotificationDuration(level: ErrorLevel): number {
    switch (level) {
      case ErrorLevel.INFO:
        return 4000
      case ErrorLevel.WARNING:
        return 6000
      case ErrorLevel.ERROR:
        return 8000
      case ErrorLevel.CRITICAL:
        return 0 // 不自动关闭
      default:
        return 6000
    }
  }

  /**
   * 记录错误日志
   */
  private logError(errorInfo: ErrorInfo) {
    // 添加到错误日志
    this.errorLog.unshift(errorInfo)

    // 限制日志大小
    if (this.errorLog.length > this.maxLogSize) {
      this.errorLog = this.errorLog.slice(0, this.maxLogSize)
    }

    // 控制台输出
    const logMethod = errorInfo.level === ErrorLevel.CRITICAL ? 'error' : 'warn'
    console[logMethod]('Frontend Error:', {
      type: errorInfo.type,
      level: errorInfo.level,
      code: errorInfo.code,
      message: errorInfo.message,
      details: errorInfo.details,
      url: errorInfo.url,
      timestamp: new Date(errorInfo.timestamp).toISOString()
    })
  }

  /**
   * 上报错误到服务器
   */
  private async reportError(errorInfo: ErrorInfo) {
    // 只上报系统错误和关键错误
    if (errorInfo.level !== ErrorLevel.CRITICAL && errorInfo.type !== ErrorType.SYSTEM) {
      return
    }

    try {
      // 这里可以调用错误上报API
      await fetch('/api/errors/report', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          ...errorInfo,
          userAgent: navigator.userAgent,
          url: window.location.href,
          stack: errorInfo.details?.stack
        })
      })
    } catch (err) {
      console.warn('Error reporting failed:', err)
    }
  }

  /**
   * 显示确认对话框处理错误
   */
  async showErrorDialog(
    errorInfo: ErrorInfo,
    options?: {
      title?: string
      confirmText?: string
      cancelText?: string
      showCancel?: boolean
    }
  ): Promise<boolean> {
    const {
      title = this.getErrorTitle(errorInfo.type),
      confirmText = '确定',
      cancelText = '取消',
      showCancel = false
    } = options || {}

    try {
      await ElMessageBox.confirm(
        errorInfo.message,
        title,
        {
          confirmButtonText: confirmText,
          cancelButtonText: cancelText,
          showCancelButton: showCancel,
          type: this.getElementUIType(errorInfo.level)
        }
      )
      return true
    } catch {
      return false
    }
  }

  /**
   * 获取错误日志
   */
  getErrorLog(): ErrorInfo[] {
    return [...this.errorLog]
  }

  /**
   * 清除错误日志
   */
  clearErrorLog() {
    this.errorLog = []
  }

  /**
   * 获取错误统计
   */
  getErrorStats() {
    const stats = {
      total: this.errorLog.length,
      byType: {} as Record<ErrorType, number>,
      byLevel: {} as Record<ErrorLevel, number>
    }

    this.errorLog.forEach(error => {
      stats.byType[error.type] = (stats.byType[error.type] || 0) + 1
      stats.byLevel[error.level] = (stats.byLevel[error.level] || 0) + 1
    })

    return stats
  }
}

// 导出单例实例
export const errorHandler = new ErrorHandler()

// 导出便捷方法
export const handleError = errorHandler.handle.bind(errorHandler)
export const showErrorDialog = errorHandler.showErrorDialog.bind(errorHandler)
export const getErrorLog = errorHandler.getErrorLog.bind(errorHandler)
export const clearErrorLog = errorHandler.clearErrorLog.bind(errorHandler)
export const getErrorStats = errorHandler.getErrorStats.bind(errorHandler)

// Vue全局错误处理器
export function setupGlobalErrorHandler(app: any) {
  app.config.errorHandler = (error: any, instance: any, info: string) => {
    console.error('Vue Error:', error, info)

    errorHandler.handle(error, {
      showMessage: true,
      logToConsole: true,
      reportToServer: true
    })
  }

  // 捕获未处理的Promise错误
  window.addEventListener('unhandledrejection', (event) => {
    console.error('Unhandled Promise Rejection:', event.reason)

    errorHandler.handle(event.reason, {
      showMessage: true,
      logToConsole: true,
      reportToServer: true
    })
  })

  // 捕获全局JavaScript错误
  window.addEventListener('error', (event) => {
    console.error('Global Error:', event.error)

    errorHandler.handle(event.error, {
      showMessage: true,
      logToConsole: true,
      reportToServer: true
    })
  })
}