import { createRouter, createWebHistory } from 'vue-router'
import pinia from '../stores'
import { useAuthStore } from '../stores/auth'

import MainLayout from '../layout/MainLayout.vue'
import HomeView from '../views/home/HomeView.vue'
import LoginView from '../views/auth/LoginView.vue'
import RegisterView from '../views/auth/RegisterView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: {
        title: '登录',
        guestOnly: true
      }
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
      meta: {
        title: '注册',
        guestOnly: true
      }
    },
    {
      path: '/',
      component: MainLayout,
      children: [
        {
          path: '',
          redirect: '/home'
        },
        {
          path: 'home',
          name: 'jobSeekerHome',
          component: HomeView,
          meta: {
            title: '求职者首页',
            requiresAuth: true,
            roles: ['JOB_SEEKER']
          }
        },
        {
          path: 'enterprise/home',
          name: 'enterpriseHome',
          component: HomeView,
          meta: {
            title: '企业首页',
            requiresAuth: true,
            roles: ['ENTERPRISE']
          }
        },
        {
          path: 'admin/home',
          name: 'adminHome',
          component: HomeView,
          meta: {
            title: '管理员首页',
            requiresAuth: true,
            roles: ['ADMIN']
          }
        }
      ]
    }
  ],
  scrollBehavior() {
    return {
      top: 0
    }
  }
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore(pinia)

  if (!authStore.initialized && authStore.token) {
    try {
      await authStore.fetchCurrentUser()
    } catch (error) {
      if (to.meta?.requiresAuth) {
        return {
          path: '/login',
          query: {
            redirect: to.fullPath
          }
        }
      }
    }
  } else if (!authStore.initialized) {
    authStore.markInitialized()
  }

  if (to.meta?.guestOnly && authStore.isLoggedIn) {
    return authStore.getHomePathByRole()
  }

  if (to.meta?.requiresAuth && !authStore.isLoggedIn) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath
      }
    }
  }

  const allowRoles = to.meta?.roles
  if (allowRoles?.length && !allowRoles.includes(authStore.userRole)) {
    return authStore.getHomePathByRole()
  }

  return true
})

router.afterEach((to) => {
  document.title = `${to.meta?.title || '首页'} - 残疾人就业 Web 平台`
})

export default router
