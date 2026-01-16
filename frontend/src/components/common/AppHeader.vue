<template>
  <el-header class="app-header">
    <div class="header-content">
      <!-- Logo和标题 -->
      <div class="logo-section">
        <router-link to="/home" class="logo-link">
          <h1 class="app-title">在线答题系统</h1>
        </router-link>
      </div>

      <!-- 导航菜单 -->
      <el-menu
        :default-active="activeIndex"
        class="header-menu"
        mode="horizontal"
        @select="handleSelect"
      >
        <el-menu-item index="/home">首页</el-menu-item>
        <el-menu-item index="/banks">开始答题</el-menu-item>
        <el-menu-item index="/questions">题库</el-menu-item>
        <el-menu-item index="/ranking">排行榜</el-menu-item>
        <el-sub-menu index="user" v-if="userStore.isLoggedIn">
          <template #title>个人中心</template>
          <el-menu-item index="/favorites">我的收藏</el-menu-item>
          <el-menu-item index="/wrong-questions">错题本</el-menu-item>
          <el-menu-item index="/statistics">统计分析</el-menu-item>
          <el-menu-item index="/profile">个人信息</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="admin" v-if="userStore.isLoggedIn && userStore.isAdmin">
          <template #title>管理后台</template>
          <el-menu-item index="/admin/permissions">权限管理</el-menu-item>
          <el-menu-item index="/admin/sessions">答题记录</el-menu-item>
        </el-sub-menu>
      </el-menu>

      <!-- 用户操作区 -->
      <div class="user-section">
        <template v-if="userStore.isLoggedIn">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.nickname?.charAt(0) || userStore.userInfo?.username?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
              <el-icon><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="contact" divided>联系管理员</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button-group>
            <el-button @click="showContactDialog = true">联系管理员</el-button>
            <el-button @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" @click="$router.push('/register')">注册</el-button>
          </el-button-group>
        </template>
      </div>
    </div>
    
    <!-- 联系管理员弹窗 -->
    <ContactAdminDialog v-model="showContactDialog" />
  </el-header>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import ContactAdminDialog from './ContactAdminDialog.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 联系管理员弹窗状态
const showContactDialog = ref(false)

// 当前激活的菜单项
const activeIndex = computed(() => route.path)

// 菜单选择处理
const handleSelect = (key: string) => {
  if (key.startsWith('/')) {
    router.push(key)
  }
}

// 用户操作处理
const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'contact':
      showContactDialog.value = true
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await userStore.logout()
        ElMessage.success('退出成功')
        router.push('/home')
      } catch (error) {
        // 用户取消操作
      }
      break
  }
}
</script>

<style scoped>
.app-header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 20px;
}

.logo-section {
  flex-shrink: 0;
}

.logo-link {
  text-decoration: none;
  color: inherit;
}

.app-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
}

.header-menu {
  flex: 1;
  margin: 0 40px;
  border-bottom: none;
}

.header-menu .el-menu-item,
.header-menu .el-sub-menu {
  border-bottom: 2px solid transparent;
}

.header-menu .el-menu-item.is-active {
  border-bottom-color: #409eff;
  color: #409eff;
}

.user-section {
  flex-shrink: 0;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  margin: 0 8px;
  font-size: 14px;
  color: #606266;
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 10px;
  }
  
  .header-menu {
    margin: 0 20px;
  }
  
  .username {
    display: none;
  }
}
</style>
