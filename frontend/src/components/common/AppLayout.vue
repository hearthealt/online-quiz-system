<template>
  <el-container class="app-layout">
    <AppHeader />
    <el-main class="app-main">
      <slot />
    </el-main>
    <el-footer class="app-footer">
      <div class="footer-content">
        <p>&copy; 2024 在线答题系统. All rights reserved.</p>
      </div>
    </el-footer>
    
    <!-- Token监控组件 -->
    <TokenMonitor />
  </el-container>
</template>

<script setup lang="ts">
import AppHeader from './AppHeader.vue'
import TokenMonitor from './TokenMonitor.vue'
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
}

.app-main {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.app-footer {
  background-color: #fff;
  border-top: 1px solid #e4e7ed;
  padding: 20px 0;
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}
</style>
