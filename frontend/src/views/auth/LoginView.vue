<template>
  <section class="auth-page" aria-label="登录页面">
    <div class="auth-shell">
      <aside class="auth-intro">
        <p class="auth-badge">Graduation Project</p>
        <h1>残疾人就业服务平台</h1>
        <p class="auth-description">
          面向求职者、企业与管理员的综合就业服务平台，支持用户注册登录、岗位发布、简历管理、消息通知与审核统计。
        </p>

        <div class="intro-grid">
          <div class="intro-card">
            <h3>平台能力</h3>
            <ul>
              <li>求职者可完善资料、管理简历并在线投递岗位</li>
              <li>企业可发布岗位、查看投递记录并处理招聘流程</li>
              <li>管理员可进行认证审核并查看平台运行统计</li>
            </ul>
          </div>
          <div class="intro-card">
            <h3>展示亮点</h3>
            <ul>
              <li>多角色协同，完整体现招聘业务闭环</li>
              <li>支持消息提醒与推荐岗位，增强交互体验</li>
            </ul>
          </div>
        </div>
      </aside>

      <el-card shadow="never" class="auth-card">
        <div class="auth-card-header">
          <div>
            <p class="auth-card-caption">Account Access</p>
            <h2>欢迎登录</h2>
            <p>请输入账号信息，进入平台工作台。</p>
          </div>
          <div class="auth-switch">
            <span class="auth-switch-current">登录</span>
            <RouterLink class="auth-link" to="/register">去注册</RouterLink>
          </div>
        </div>

        <div class="auth-role-tags">
          <el-tag type="primary" effect="light">求职者</el-tag>
          <el-tag type="success" effect="light">企业用户</el-tag>
          <el-tag type="warning" effect="light">管理员</el-tag>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          aria-label="登录表单"
          @submit.prevent="handleLogin"
        >
          <el-form-item label="用户名" prop="username" for="username">
            <el-input
              id="username"
              v-model="form.username"
              name="username"
              autocomplete="username"
              placeholder="请输入用户名"
              size="large"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password" for="password">
            <el-input
              id="password"
              v-model="form.password"
              name="password"
              type="password"
              show-password
              autocomplete="current-password"
              placeholder="请输入密码"
              size="large"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              class="auth-submit"
              :loading="loading"
              native-type="submit"
              aria-label="提交登录"
              size="large"
            >
              立即登录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="auth-footer-tip">
          <span>还没有账号？</span>
          <RouterLink class="auth-link" to="/register">立即创建账号</RouterLink>
        </div>
      </el-card>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../stores/auth'
import { isSpeechSupported, speakText } from '../../accessibility/speech'

const route = useRoute()
const authStore = useAuthStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度需在4到20位之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需在6到20位之间', trigger: 'blur' }
  ]
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  loading.value = true
  try {
    const loginPayload = {
      username: form.username?.trim(),
      password: form.password
    }
    const data = await authStore.login(loginPayload)
    ElMessage.success('登录成功')
    const accessibilityRaw = localStorage.getItem('job-platform-accessibility') || ''
    if (isSpeechSupported() && accessibilityRaw) {
      try {
        const accessibilitySettings = JSON.parse(accessibilityRaw)
        if (accessibilitySettings?.voiceEnabled) {
          const savedVolume = Number(accessibilitySettings.voiceVolume)
          speakText('登录成功', {
            force: true,
            rate: Number(accessibilitySettings.voiceRate) || 1,
            volume: savedVolume >= 0 && savedVolume <= 1 ? savedVolume : 1
          })
        }
      } catch (error) {
        // Ignore invalid local settings and keep login flow stable.
      }
    }
    const roleHomePath = authStore.getHomePathByRole(data?.userInfo?.role)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    const allowRedirect = redirect && redirect !== '/home' && redirect !== '/enterprise/home' && redirect !== '/admin/home'
    window.location.replace(allowRedirect ? redirect : roleHomePath)
  } catch (error) {
    // message is already shown by interceptor, this prevents uncaught promise errors in console.
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  padding: 40px 24px;
  background:
    radial-gradient(circle at top left, rgba(59, 130, 246, 0.22), transparent 34%),
    radial-gradient(circle at bottom right, rgba(14, 165, 233, 0.18), transparent 30%),
    linear-gradient(135deg, #eff6ff 0%, #f8fafc 55%, #eef2ff 100%);
}

.auth-shell {
  width: min(1240px, 100%);
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(360px, 460px);
  gap: 32px;
  align-items: stretch;
}

.auth-intro {
  color: #0f172a;
  padding: 40px 12px 40px 8px;
}

.auth-badge {
  display: inline-flex;
  align-items: center;
  margin: 0 0 18px;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.1);
  color: #2563eb;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.auth-intro h1 {
  margin: 0;
  font-size: clamp(32px, 4vw, 48px);
  line-height: 1.18;
}

.auth-description {
  max-width: 640px;
  margin: 20px 0 32px;
  color: #475569;
  font-size: 16px;
  line-height: 1.8;
}

.intro-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.intro-card {
  padding: 22px 24px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.06);
  backdrop-filter: blur(10px);
}

.intro-card h3 {
  margin: 0 0 14px;
  font-size: 18px;
}

.intro-card ul {
  margin: 0;
  padding-left: 18px;
  color: #475569;
  line-height: 1.8;
}

.auth-card {
  border: none;
  border-radius: 28px;
  padding: 8px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 60px rgba(30, 41, 59, 0.12);
}

.auth-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.auth-card-caption {
  margin: 0 0 10px;
  color: #2563eb;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.auth-card-header h2 {
  margin: 0 0 8px;
  font-size: 30px;
  color: #0f172a;
}

.auth-card-header p {
  margin: 0;
  color: #64748b;
}

.auth-switch {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.auth-switch-current {
  padding: 8px 14px;
  border-radius: 999px;
  background: #eff6ff;
  color: #2563eb;
  font-weight: 600;
}

.auth-role-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 18px;
}

.auth-submit {
  width: 100%;
  border-radius: 14px;
  height: 48px;
  font-weight: 600;
}

.auth-footer-tip {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 8px;
  color: #64748b;
}

.auth-link {
  color: #2563eb;
  font-weight: 600;
}

@media (max-width: 960px) {
  .auth-shell {
    grid-template-columns: 1fr;
  }

  .auth-intro {
    padding: 12px 0 0;
  }

  .intro-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .auth-page {
    padding: 20px 14px;
  }

  .auth-card-header {
    flex-direction: column;
  }
}
</style>
