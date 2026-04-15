import { createRouter, createWebHistory } from 'vue-router'
import pinia from '../stores'
import { useAuthStore } from '../stores/auth'

import MainLayout from '../layout/MainLayout.vue'
import HomeView from '../views/home/HomeView.vue'
import LoginView from '../views/auth/LoginView.vue'
import RegisterView from '../views/auth/RegisterView.vue'
import JobSeekerProfileView from '../views/jobseeker/JobSeekerProfileView.vue'
import JobSeekerVerifyView from '../views/jobseeker/JobSeekerVerifyView.vue'
import ResumeListView from '../views/jobseeker/ResumeListView.vue'
import ResumeEditView from '../views/jobseeker/ResumeEditView.vue'
import JobListView from '../views/jobseeker/JobListView.vue'
import JobDetailView from '../views/jobseeker/JobDetailView.vue'
import MyApplicationsView from '../views/jobseeker/MyApplicationsView.vue'
import EnterpriseProfileView from '../views/enterprise/EnterpriseProfileView.vue'
import EnterpriseVerifyView from '../views/enterprise/EnterpriseVerifyView.vue'
import JobPublishView from '../views/enterprise/JobPublishView.vue'
import JobManageView from '../views/enterprise/JobManageView.vue'
import JobEditView from '../views/enterprise/JobEditView.vue'
import JobApplicationsView from '../views/enterprise/JobApplicationsView.vue'
import MessageCenterView from '../views/MessageCenterView.vue'
import AdminAuditView from '../views/admin/AdminAuditView.vue'
import AdminStatisticsView from '../views/admin/AdminStatisticsView.vue'
import AdminFeedbackView from '../views/admin/AdminFeedbackView.vue'
import UserFeedbackView from '../views/feedback/UserFeedbackView.vue'

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
        },
        {
          path: 'jobseeker/profile',
          name: 'jobSeekerProfile',
          component: JobSeekerProfileView,
          meta: {
            title: '求职者个人中心',
            requiresAuth: true,
            roles: ['JOB_SEEKER']
          }
        },
        {
          path: 'jobseeker/verify',
          name: 'jobSeekerVerify',
          component: JobSeekerVerifyView,
          meta: {
            title: '求职者认证',
            requiresAuth: true,
            roles: ['JOB_SEEKER']
          }
        },
        {
          path: 'jobseeker/resumes',
          name: 'resumeList',
          component: ResumeListView,
          meta: {
            title: '简历列表',
            requiresAuth: true,
            roles: ['JOB_SEEKER']
          }
        },
        {
          path: 'jobseeker/resumes/new',
          name: 'resumeCreate',
          component: ResumeEditView,
          meta: {
            title: '新增简历',
            requiresAuth: true,
            roles: ['JOB_SEEKER']
          }
        },
        {
          path: 'jobseeker/resumes/:id/edit',
          name: 'resumeEdit',
          component: ResumeEditView,
          meta: {
            title: '编辑简历',
            requiresAuth: true,
            roles: ['JOB_SEEKER']
          }
        },
        {
          path: 'jobs',
          name: 'jobList',
          component: JobListView,
          meta: {
            title: '岗位浏览',
            requiresAuth: true,
            roles: ['JOB_SEEKER']
          }
        },
        {
          path: 'jobs/:id',
          name: 'jobDetail',
          component: JobDetailView,
          meta: {
            title: '岗位详情',
            requiresAuth: true,
            roles: ['JOB_SEEKER']
          }
        },
        {
          path: 'applications/my',
          name: 'myApplications',
          component: MyApplicationsView,
          meta: {
            title: '我的投递',
            requiresAuth: true,
            roles: ['JOB_SEEKER']
          }
        },
        {
          path: 'recommend/jobs',
          redirect: { name: 'jobList' }
        },
        {
          path: 'messages',
          name: 'messageCenter',
          component: MessageCenterView,
          meta: {
            title: '消息中心',
            requiresAuth: true
          }
        },
        {
          path: 'enterprise/profile',
          name: 'enterpriseProfile',
          component: EnterpriseProfileView,
          meta: {
            title: '企业中心',
            requiresAuth: true,
            roles: ['ENTERPRISE']
          }
        },
        {
          path: 'enterprise/verify',
          name: 'enterpriseVerify',
          component: EnterpriseVerifyView,
          meta: {
            title: '企业认证',
            requiresAuth: true,
            roles: ['ENTERPRISE']
          }
        },
        {
          path: 'enterprise/jobs/publish',
          name: 'jobPublish',
          component: JobPublishView,
          meta: {
            title: '发布岗位',
            requiresAuth: true,
            roles: ['ENTERPRISE']
          }
        },
        {
          path: 'enterprise/jobs',
          name: 'jobManage',
          component: JobManageView,
          meta: {
            title: '岗位管理',
            requiresAuth: true,
            roles: ['ENTERPRISE']
          }
        },
        {
          path: 'enterprise/jobs/:id/edit',
          name: 'jobEdit',
          component: JobEditView,
          meta: {
            title: '编辑岗位',
            requiresAuth: true,
            roles: ['ENTERPRISE']
          }
        },
        {
          path: 'enterprise/jobs/:jobId/applications',
          name: 'jobApplications',
          component: JobApplicationsView,
          meta: {
            title: '岗位投递记录',
            requiresAuth: true,
            roles: ['ENTERPRISE']
          }
        },
        {
          path: 'feedback/contact-admin',
          name: 'userFeedbackContact',
          component: UserFeedbackView,
          meta: {
            title: '联系管理员',
            requiresAuth: true,
            roles: ['JOB_SEEKER', 'ENTERPRISE']
          }
        },
        {
          path: 'admin/audits',
          name: 'adminAudits',
          component: AdminAuditView,
          meta: {
            title: '认证审核',
            requiresAuth: true,
            roles: ['ADMIN']
          }
        },
        {
          path: 'admin/users',
          redirect: { name: 'adminStatistics' },
          meta: {
            requiresAuth: true,
            roles: ['ADMIN']
          }
        },
        {
          path: 'admin/feedback',
          name: 'adminFeedback',
          component: AdminFeedbackView,
          meta: {
            title: '用户反馈',
            requiresAuth: true,
            roles: ['ADMIN']
          }
        },
        {
          path: 'admin/statistics',
          name: 'adminStatistics',
          component: AdminStatisticsView,
          meta: {
            title: '平台统计',
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
  const storedToken = localStorage.getItem('job-platform-token') || ''
  const storedUserRaw = localStorage.getItem('job-platform-user') || ''

  if (!authStore.token && storedToken) {
    authStore.setToken(storedToken)
  }

  if (!authStore.userInfo && storedUserRaw) {
    try {
      authStore.setUserInfo(JSON.parse(storedUserRaw))
    } catch (error) {
      authStore.setUserInfo(null)
    }
  }

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
