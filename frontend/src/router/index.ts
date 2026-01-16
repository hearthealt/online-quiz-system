import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { checkAndCleanExpiredToken } from '@/utils/token'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/home'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: {
        title: '用户登录',
        requiresAuth: false
      }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/auth/RegisterView.vue'),
      meta: {
        title: '用户注册',
        requiresAuth: false
      }
    },
    {
      path: '/home',
      name: 'Home',
      component: () => import('@/views/HomeView.vue'),
      meta: {
        title: '首页',
        requiresAuth: false
      }
    },
    {
      path: '/banks',
      name: 'Banks',
      component: () => import('@/views/bank/BankListView.vue'),
      meta: {
        title: '题库列表',
        requiresAuth: true
      }
    },
    {
      path: '/practice/:bankId',
      name: 'Practice',
      component: () => import('@/views/practice/PracticeView.vue'),
      meta: {
        title: '顺序练习',
        requiresAuth: true
      }
    },
    {
      path: '/exam/:bankId',
      name: 'Exam',
      component: () => import('@/views/exam/ExamView.vue'),
      meta: {
        title: '考试模式',
        requiresAuth: true
      }
    },
    {
      path: '/result/:sessionId',
      name: 'Result',
      component: () => import('@/views/result/ResultView.vue'),
      meta: {
        title: '答题结果',
        requiresAuth: true
      }
    },
    {
      path: '/questions',
      name: 'Questions',
      component: () => import('@/views/question/QuestionListView.vue'),
      meta: {
        title: '题库管理',
        requiresAuth: true
      }
    },
    {
      path: '/questions/bank/:bankId',
      name: 'BankDetail',
      component: () => import('@/views/question/BankDetailView.vue'),
      meta: {
        title: '题库详情',
        requiresAuth: true
      }
    },
    {
      path: '/favorites',
      name: 'Favorites',
      component: () => import('@/views/user/FavoritesView.vue'),
      meta: {
        title: '我的收藏',
        requiresAuth: true
      }
    },
    {
      path: '/wrong-questions',
      name: 'WrongQuestions',
      component: () => import('@/views/user/WrongQuestionsView.vue'),
      meta: {
        title: '错题本',
        requiresAuth: true
      }
    },
    {
      path: '/statistics',
      name: 'Statistics',
      component: () => import('@/views/user/StatisticsView.vue'),
      meta: {
        title: '统计分析',
        requiresAuth: true
      }
    },
    {
      path: '/ranking',
      name: 'Ranking',
      component: () => import('@/views/user/RankingView.vue'),
      meta: {
        title: '成绩排行榜',
        requiresAuth: true
      }
    },
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('@/views/user/ProfileView.vue'),
      meta: {
        title: '个人信息',
        requiresAuth: true
      }
    },
    {
      path: '/admin/permissions',
      name: 'PermissionManage',
      component: () => import('@/views/admin/PermissionManageView.vue'),
      meta: {
        title: '权限管理',
        requiresAuth: true,
        requiresAdmin: true
      }
    },
    {
      path: '/admin/sessions',
      name: 'SessionManage',
      component: () => import('@/views/admin/SessionManageView.vue'),
      meta: {
        title: '答题记录管理',
        requiresAuth: true,
        requiresAdmin: true
      }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/NotFoundView.vue'),
      meta: {
        title: '页面不存在'
      }
    }
  ]
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 在线答题系统`
  }

  // 如果有token但没有用户信息，尝试初始化用户状态
  if (!userStore.isLoggedIn && checkAndCleanExpiredToken()) {
    userStore.initUserState()
  }

  // 检查是否需要登录
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
  } else if ((to.path === '/login' || to.path === '/register') && userStore.isLoggedIn) {
    // 已登录用户访问登录/注册页面，重定向到首页
    next('/home')
  } else if (to.meta.requiresAdmin && !userStore.isAdmin) {
    // 需要管理员权限但用户不是管理员
    ElMessage.error('您没有权限访问此页面')
    next('/home')
  } else {
    next()
  }
})

export default router
