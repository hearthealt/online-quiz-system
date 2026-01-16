// 小程序性能优化工具
export class PerformanceOptimizer {
  constructor() {
    this.imageCache = new Map()
    this.requestCache = new Map()
    this.lazyLoadObserver = null
    this.performanceData = {
      pageLoadTimes: [],
      requestTimes: [],
      imageLoadTimes: []
    }
  }

  // 初始化性能监控
  init() {
    this.setupNetworkMonitor()
    this.setupImageLazyLoad()
    this.setupPerformanceMonitor()
  }

  // 网络状态监控
  setupNetworkMonitor() {
    uni.onNetworkStatusChange((res) => {
      console.log('网络状态变化:', res)

      if (!res.isConnected) {
        uni.showToast({
          title: '网络连接断开',
          icon: 'none'
        })
      } else if (res.networkType === '2g' || res.networkType === '3g') {
        // 弱网环境优化
        this.enableWeakNetworkMode()
      }
    })
  }

  // 弱网环境优化
  enableWeakNetworkMode() {
    console.log('启用弱网优化模式')

    // 减少并发请求数量
    this.maxConcurrentRequests = 2

    // 启用更积极的缓存策略
    this.aggressiveCache = true

    // 压缩图片质量
    this.imageQuality = 50
  }

  // 图片懒加载设置
  setupImageLazyLoad() {
    // 小程序使用 intersection observer 实现懒加载
    this.lazyLoadObserver = uni.createIntersectionObserver({
      rootMargin: '50px'
    })
  }

  // 懒加载图片
  lazyLoadImage(selector, callback) {
    if (!this.lazyLoadObserver) return

    this.lazyLoadObserver.observe(selector, (res) => {
      if (res.intersectionRatio > 0) {
        callback && callback()
        this.lazyLoadObserver.unobserve(selector)
      }
    })
  }

  // 性能监控
  setupPerformanceMonitor() {
    // 页面加载时间监控
    const originalOnLoad = Page.prototype.onLoad
    Page.prototype.onLoad = function(options) {
      const startTime = Date.now()

      const originalOnReady = this.onReady
      this.onReady = function() {
        const loadTime = Date.now() - startTime
        performanceOptimizer.recordPageLoadTime(loadTime)

        originalOnReady && originalOnReady.call(this)
      }

      originalOnLoad && originalOnLoad.call(this, options)
    }
  }

  // 记录页面加载时间
  recordPageLoadTime(time) {
    this.performanceData.pageLoadTimes.push({
      time,
      timestamp: Date.now()
    })

    // 保持最近100条记录
    if (this.performanceData.pageLoadTimes.length > 100) {
      this.performanceData.pageLoadTimes.shift()
    }

    console.log(`页面加载时间: ${time}ms`)
  }

  // 优化的网络请求
  async optimizedRequest(options) {
    const cacheKey = `${options.url}_${JSON.stringify(options.data || {})}`

    // 检查缓存
    if (this.aggressiveCache && this.requestCache.has(cacheKey)) {
      const cached = this.requestCache.get(cacheKey)
      if (Date.now() - cached.timestamp < cached.ttl) {
        return cached.data
      }
    }

    const startTime = Date.now()

    try {
      const result = await this.makeRequest(options)
      const requestTime = Date.now() - startTime

      // 记录请求时间
      this.performanceData.requestTimes.push({
        url: options.url,
        time: requestTime,
        timestamp: Date.now()
      })

      // 缓存结果
      this.requestCache.set(cacheKey, {
        data: result,
        timestamp: Date.now(),
        ttl: options.cacheTTL || 5 * 60 * 1000 // 默认5分钟
      })

      return result
    } catch (error) {
      console.error('请求失败:', error)
      throw error
    }
  }

  // 发起网络请求
  makeRequest(options) {
    return new Promise((resolve, reject) => {
      uni.request({
        ...options,
        success: (res) => {
          if (res.statusCode === 200) {
            resolve(res.data)
          } else {
            reject(new Error(`HTTP ${res.statusCode}`))
          }
        },
        fail: reject
      })
    })
  }

  // 图片预加载
  preloadImages(urls) {
    const promises = urls.map(url => this.preloadImage(url))
    return Promise.allSettled(promises)
  }

  preloadImage(url) {
    if (this.imageCache.has(url)) {
      return Promise.resolve(url)
    }

    return new Promise((resolve, reject) => {
      const startTime = Date.now()

      uni.getImageInfo({
        src: url,
        success: (res) => {
          const loadTime = Date.now() - startTime

          this.imageCache.set(url, {
            width: res.width,
            height: res.height,
            path: res.path,
            timestamp: Date.now()
          })

          this.performanceData.imageLoadTimes.push({
            url,
            time: loadTime,
            timestamp: Date.now()
          })

          resolve(url)
        },
        fail: reject
      })
    })
  }

  // 批量操作优化
  batchOperation(operations, batchSize = 5) {
    const batches = []
    for (let i = 0; i < operations.length; i += batchSize) {
      batches.push(operations.slice(i, i + batchSize))
    }

    return batches.reduce((promise, batch) => {
      return promise.then(results => {
        return Promise.all(batch).then(batchResults => {
          return results.concat(batchResults)
        })
      })
    }, Promise.resolve([]))
  }

  // 防抖函数
  debounce(func, wait) {
    let timeout
    return function executedFunction(...args) {
      const later = () => {
        clearTimeout(timeout)
        func(...args)
      }
      clearTimeout(timeout)
      timeout = setTimeout(later, wait)
    }
  }

  // 节流函数
  throttle(func, limit) {
    let inThrottle
    return function() {
      const args = arguments
      const context = this
      if (!inThrottle) {
        func.apply(context, args)
        inThrottle = true
        setTimeout(() => inThrottle = false, limit)
      }
    }
  }

  // 内存优化
  optimizeMemory() {
    // 清理过期缓存
    this.cleanExpiredCache()

    // 限制缓存大小
    this.limitCacheSize()

    // 触发垃圾回收（如果支持）
    if (typeof gc === 'function') {
      gc()
    }
  }

  cleanExpiredCache() {
    const now = Date.now()

    // 清理请求缓存
    for (const [key, value] of this.requestCache) {
      if (now - value.timestamp > value.ttl) {
        this.requestCache.delete(key)
      }
    }

    // 清理图片缓存（超过1小时的）
    for (const [key, value] of this.imageCache) {
      if (now - value.timestamp > 60 * 60 * 1000) {
        this.imageCache.delete(key)
      }
    }
  }

  limitCacheSize() {
    const maxCacheSize = 100

    if (this.requestCache.size > maxCacheSize) {
      const entries = Array.from(this.requestCache.entries())
      entries.sort((a, b) => a[1].timestamp - b[1].timestamp)

      // 删除最旧的一半
      const toDelete = entries.slice(0, Math.floor(entries.length / 2))
      toDelete.forEach(([key]) => this.requestCache.delete(key))
    }

    if (this.imageCache.size > maxCacheSize) {
      const entries = Array.from(this.imageCache.entries())
      entries.sort((a, b) => a[1].timestamp - b[1].timestamp)

      const toDelete = entries.slice(0, Math.floor(entries.length / 2))
      toDelete.forEach(([key]) => this.imageCache.delete(key))
    }
  }

  // 获取性能报告
  getPerformanceReport() {
    const report = {
      pageLoad: {
        count: this.performanceData.pageLoadTimes.length,
        average: this.calculateAverage(this.performanceData.pageLoadTimes.map(p => p.time)),
        median: this.calculateMedian(this.performanceData.pageLoadTimes.map(p => p.time))
      },
      requests: {
        count: this.performanceData.requestTimes.length,
        average: this.calculateAverage(this.performanceData.requestTimes.map(r => r.time)),
        median: this.calculateMedian(this.performanceData.requestTimes.map(r => r.time))
      },
      images: {
        count: this.performanceData.imageLoadTimes.length,
        average: this.calculateAverage(this.performanceData.imageLoadTimes.map(i => i.time)),
        median: this.calculateMedian(this.performanceData.imageLoadTimes.map(i => i.time))
      },
      cache: {
        requestCacheSize: this.requestCache.size,
        imageCacheSize: this.imageCache.size
      }
    }

    return report
  }

  calculateAverage(numbers) {
    if (numbers.length === 0) return 0
    return Math.round(numbers.reduce((sum, num) => sum + num, 0) / numbers.length)
  }

  calculateMedian(numbers) {
    if (numbers.length === 0) return 0
    const sorted = numbers.sort((a, b) => a - b)
    const middle = Math.floor(sorted.length / 2)
    return sorted.length % 2 === 0
      ? Math.round((sorted[middle - 1] + sorted[middle]) / 2)
      : sorted[middle]
  }

  // 销毁
  destroy() {
    if (this.lazyLoadObserver) {
      this.lazyLoadObserver.disconnect()
    }

    this.imageCache.clear()
    this.requestCache.clear()
    this.performanceData = {
      pageLoadTimes: [],
      requestTimes: [],
      imageLoadTimes: []
    }
  }
}

// 创建全局实例
export const performanceOptimizer = new PerformanceOptimizer()

// 自动初始化
performanceOptimizer.init()

// 定期内存清理
setInterval(() => {
  performanceOptimizer.optimizeMemory()
}, 5 * 60 * 1000) // 每5分钟清理一次