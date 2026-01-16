<template>
  <div class="not-found-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="floating-shape shape-1"></div>
      <div class="floating-shape shape-2"></div>
      <div class="floating-shape shape-3"></div>
      <div class="floating-shape shape-4"></div>
    </div>

    <div class="not-found-content">
      <!-- 404 数字 -->
      <div class="error-code-container">
        <div class="error-code">4</div>
        <div class="error-code">0</div>
        <div class="error-code">4</div>
      </div>
      
      <!-- 标题和描述 -->
      <div class="error-info">
        <h1 class="error-title">页面走丢了</h1>
        <p class="error-description">
          抱歉，您访问的页面不存在或已被移除<br>
          让我们帮您找到正确的方向
        </p>
      </div>

      <!-- 操作按钮 -->
      <div class="error-actions">
        <el-button type="primary" size="large" @click="goHome" class="primary-btn">
          <el-icon><House /></el-icon>
          返回首页
        </el-button>
        <el-button size="large" @click="goBack" class="secondary-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回上页
        </el-button>
      </div>

      <!-- 快速导航 -->
      <div class="quick-nav">
        <p class="nav-title">或者试试这些页面：</p>
        <div class="nav-links">
          <el-button text @click="goToQuiz" class="nav-link">
            <el-icon><Edit /></el-icon>
            开始答题
          </el-button>
          <el-button text @click="goToProfile" class="nav-link">
            <el-icon><User /></el-icon>
            个人中心
          </el-button>
          <el-button text @click="goToHistory" class="nav-link">
            <el-icon><Clock /></el-icon>
            答题历史
          </el-button>
        </div>
      </div>
    </div>

    <!-- 插图 -->
    <div class="error-illustration">
      <div class="illustration-container">
        <div class="lost-robot">
          <div class="robot-head">
            <div class="robot-eyes">
              <div class="eye left-eye"></div>
              <div class="eye right-eye"></div>
            </div>
            <div class="robot-mouth"></div>
          </div>
          <div class="robot-body">
            <div class="robot-screen">
              <div class="screen-content">404</div>
            </div>
          </div>
          <div class="robot-arms">
            <div class="arm left-arm"></div>
            <div class="arm right-arm"></div>
          </div>
        </div>
        <div class="search-dots">
          <div class="dot dot-1"></div>
          <div class="dot dot-2"></div>
          <div class="dot dot-3"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { House, ArrowLeft, Edit, User, Clock } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.back()
}

const goToQuiz = () => {
  router.push('/banks')
}

const goToProfile = () => {
  router.push('/profile')
}

const goToHistory = () => {
  router.push('/history')
}
</script>

<style scoped>
.not-found-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.floating-shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 80px;
  height: 80px;
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 120px;
  height: 120px;
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.shape-3 {
  width: 60px;
  height: 60px;
  top: 80%;
  left: 20%;
  animation-delay: 4s;
}

.shape-4 {
  width: 100px;
  height: 100px;
  top: 30%;
  right: 30%;
  animation-delay: 1s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

.not-found-content {
  text-align: center;
  color: white;
  max-width: 500px;
  margin-right: 40px;
  position: relative;
  z-index: 2;
}

/* 404 数字样式 */
.error-code-container {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 32px;
  perspective: 1000px;
}

.error-code {
  font-size: 120px;
  font-weight: 700;
  line-height: 1;
  background: linear-gradient(45deg, #fff, #f0f9ff);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: bounce 2s ease-in-out infinite;
  transform-style: preserve-3d;
}

.error-code:nth-child(1) { animation-delay: 0s; }
.error-code:nth-child(2) { animation-delay: 0.2s; }
.error-code:nth-child(3) { animation-delay: 0.4s; }

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% { transform: translateY(0) rotateY(0deg); }
  40% { transform: translateY(-20px) rotateY(180deg); }
  60% { transform: translateY(-10px) rotateY(180deg); }
}

.error-info {
  margin-bottom: 40px;
}

.error-title {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 16px;
  background: linear-gradient(45deg, #fff, #e0f2fe);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.error-description {
  font-size: 18px;
  line-height: 1.6;
  opacity: 0.9;
  margin: 0;
}

/* 按钮样式 */
.error-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 40px;
}

.primary-btn {
  background: linear-gradient(45deg, #3b82f6, #1d4ed8);
  border: none;
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.4);
  transition: all 0.3s ease;
}

.primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.6);
}

.secondary-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  color: white;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.secondary-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
}

/* 快速导航 */
.quick-nav {
  text-align: center;
}

.nav-title {
  font-size: 16px;
  margin-bottom: 16px;
  opacity: 0.8;
}

.nav-links {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

.nav-link {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.nav-link:hover {
  color: white;
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-1px);
}

/* 插图样式 */
.error-illustration {
  max-width: 400px;
  position: relative;
  z-index: 2;
}

.illustration-container {
  position: relative;
  width: 300px;
  height: 300px;
  margin: 0 auto;
}

.lost-robot {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: robotFloat 3s ease-in-out infinite;
}

@keyframes robotFloat {
  0%, 100% { transform: translate(-50%, -50%) translateY(0px); }
  50% { transform: translate(-50%, -50%) translateY(-10px); }
}

.robot-head {
  width: 80px;
  height: 80px;
  background: linear-gradient(45deg, #4f46e5, #7c3aed);
  border-radius: 50%;
  position: relative;
  margin: 0 auto 10px;
  box-shadow: 0 4px 15px rgba(79, 70, 229, 0.3);
}

.robot-eyes {
  position: absolute;
  top: 25px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
}

.eye {
  width: 12px;
  height: 12px;
  background: white;
  border-radius: 50%;
  animation: blink 4s infinite;
}

@keyframes blink {
  0%, 90%, 100% { transform: scaleY(1); }
  95% { transform: scaleY(0.1); }
}

.robot-mouth {
  position: absolute;
  bottom: 15px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 10px;
  border: 2px solid white;
  border-top: none;
  border-radius: 0 0 20px 20px;
}

.robot-body {
  width: 100px;
  height: 80px;
  background: linear-gradient(45deg, #3b82f6, #1d4ed8);
  border-radius: 20px;
  position: relative;
  margin: 0 auto;
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.3);
}

.robot-screen {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 60px;
  height: 30px;
  background: #1f2937;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.screen-content {
  color: #10b981;
  font-family: 'Courier New', monospace;
  font-weight: bold;
  font-size: 14px;
  animation: screenFlicker 2s infinite;
}

@keyframes screenFlicker {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.robot-arms {
  position: absolute;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  width: 120px;
  height: 20px;
}

.arm {
  position: absolute;
  width: 40px;
  height: 8px;
  background: linear-gradient(45deg, #4f46e5, #7c3aed);
  border-radius: 4px;
  animation: armWave 2s ease-in-out infinite;
}

.left-arm {
  left: 0;
  animation-delay: 0s;
}

.right-arm {
  right: 0;
  animation-delay: 1s;
}

@keyframes armWave {
  0%, 100% { transform: rotate(0deg); }
  50% { transform: rotate(20deg); }
}

.search-dots {
  position: absolute;
  top: 20px;
  right: 20px;
  display: flex;
  gap: 4px;
}

.dot {
  width: 8px;
  height: 8px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 50%;
  animation: dotPulse 1.5s ease-in-out infinite;
}

.dot-1 { animation-delay: 0s; }
.dot-2 { animation-delay: 0.5s; }
.dot-3 { animation-delay: 1s; }

@keyframes dotPulse {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.2); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .not-found-container {
    flex-direction: column;
    text-align: center;
    padding: 20px;
  }

  .not-found-content {
    margin-right: 0;
    margin-bottom: 40px;
    order: 2;
  }

  .error-illustration {
    order: 1;
    margin-bottom: 20px;
  }

  .illustration-container {
    width: 250px;
    height: 250px;
  }

  .error-code {
    font-size: 80px;
  }

  .error-title {
    font-size: 28px;
  }

  .error-actions {
    flex-direction: column;
    align-items: center;
  }

  .error-actions .el-button {
    width: 200px;
  }

  .nav-links {
    flex-direction: column;
    align-items: center;
  }

  .nav-link {
    width: 150px;
  }
}

@media (max-width: 480px) {
  .error-code {
    font-size: 60px;
  }

  .error-title {
    font-size: 24px;
  }

  .illustration-container {
    width: 200px;
    height: 200px;
  }
}
</style>
