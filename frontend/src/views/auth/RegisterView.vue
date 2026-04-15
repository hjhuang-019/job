<template>
  <section class="auth-page" aria-label="注册页面">
    <div class="auth-shell">
      <aside class="auth-intro">
        <p class="auth-badge">Create Account</p>
        <h1>加入残疾人就业服务平台</h1>
        <p class="auth-description">
          完成账号注册后，即可根据身份进入对应工作台。求职者可在线投递岗位，企业可发布职位并管理招聘流程。
        </p>

        <div class="intro-grid">
          <div class="intro-card">
            <h3>求职者注册后</h3>
            <ul>
              <li>完善个人资料与求职意向</li>
              <li>维护多份简历并在线投递</li>
              <li>查看投递反馈和系统通知</li>
            </ul>
          </div>
          <div class="intro-card">
            <h3>企业注册后</h3>
            <ul>
              <li>完善企业信息并提交认证</li>
              <li>发布岗位并维护招聘状态</li>
              <li>查看应聘者投递与消息提醒</li>
            </ul>
          </div>
        </div>
      </aside>

      <el-card shadow="never" class="auth-card">
        <div class="auth-card-header">
          <div>
            <p class="auth-card-caption">Registration</p>
            <h2>创建账号</h2>
            <p>请选择注册身份，并填写基础信息。</p>
          </div>
          <div class="auth-switch">
            <RouterLink class="auth-link" to="/login">去登录</RouterLink>
            <span class="auth-switch-current">注册</span>
          </div>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          @submit.prevent="handleSubmit"
        >
          <el-form-item label="注册身份" prop="role" for="role">
            <el-radio-group id="role" v-model="form.role" aria-label="注册身份选择" class="role-group">
              <el-radio-button value="JOB_SEEKER">求职者</el-radio-button>
              <el-radio-button value="ENTERPRISE">企业</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-row :gutter="14">
            <el-col :xs="24" :md="12">
              <el-form-item label="用户名" prop="username" for="username">
                <el-input
                  id="username"
                  v-model="form.username"
                  name="username"
                  autocomplete="username"
                  placeholder="请输入4-20位用户名"
                  size="large"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="真实姓名" prop="realName" for="realName">
                <el-input
                  id="realName"
                  v-model="form.realName"
                  name="realName"
                  autocomplete="name"
                  placeholder="请输入真实姓名"
                  size="large"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item
            v-if="form.role === 'ENTERPRISE'"
            label="企业名称"
            prop="enterpriseName"
            for="enterpriseName"
          >
            <el-input
              id="enterpriseName"
              v-model="form.enterpriseName"
              name="enterpriseName"
              placeholder="请输入企业全称"
              size="large"
            />
          </el-form-item>

          <el-row :gutter="14">
            <el-col :xs="24" :md="12">
              <el-form-item label="手机号" prop="phone" for="phone">
                <el-input
                  id="phone"
                  v-model="form.phone"
                  name="phone"
                  autocomplete="tel"
                  placeholder="请输入手机号"
                  size="large"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="邮箱" prop="email" for="email">
                <el-input
                  id="email"
                  v-model="form.email"
                  name="email"
                  autocomplete="email"
                  placeholder="请输入邮箱"
                  size="large"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="14">
            <el-col :xs="24" :md="12">
              <el-form-item label="密码" prop="password" for="password">
                <el-input
                  id="password"
                  v-model="form.password"
                  name="password"
                  type="password"
                  show-password
                  autocomplete="new-password"
                  placeholder="请输入6-20位密码"
                  size="large"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="确认密码" prop="confirmPassword" for="confirmPassword">
                <el-input
                  id="confirmPassword"
                  v-model="form.confirmPassword"
                  name="confirmPassword"
                  type="password"
                  show-password
                  autocomplete="new-password"
                  placeholder="请再次输入密码"
                  size="large"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item>
            <el-button
              type="primary"
              class="auth-submit"
              :loading="loading"
              native-type="submit"
              size="large"
            >
              完成注册
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </section>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  role: 'JOB_SEEKER',
  username: '',
  realName: '',
  enterpriseName: '',
  phone: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
    return
  }
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
    return
  }
  callback()
}

const rules = computed(() => ({
  role: [{ required: true, message: '请选择注册身份', trigger: 'change' }],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度需在4到20位之间', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  enterpriseName:
    form.role === 'ENTERPRISE'
      ? [{ required: true, message: '请输入企业名称', trigger: 'blur' }]
      : [],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需在6到20位之间', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
}))

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  const payload = {
    username: form.username?.trim(),
    realName: form.realName?.trim(),
    phone: form.phone?.trim(),
    email: form.email?.trim(),
    password: form.password,
    confirmPassword: form.confirmPassword
  }

  if (form.role === 'ENTERPRISE') {
    payload.enterpriseName = form.enterpriseName?.trim()
  }

  loading.value = true
  try {
    await authStore.registerByRole(form.role, payload)
    ElMessage.success('注册成功，请登录')
    await router.push('/login')
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
    radial-gradient(circle at top right, rgba(37, 99, 235, 0.2), transparent 34%),
    radial-gradient(circle at bottom left, rgba(56, 189, 248, 0.14), transparent 30%),
    linear-gradient(135deg, #eff6ff 0%, #f8fafc 58%, #eef2ff 100%);
}

.auth-shell {
  width: min(1240px, 100%);
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(380px, 500px);
  gap: 32px;
  align-items: stretch;
}

.auth-intro {
  color: #0f172a;
  padding: 40px 12px 40px 8px;
}

.auth-badge {
  display: inline-flex;
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
  font-size: clamp(30px, 4vw, 46px);
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
  background: rgba(255, 255, 255, 0.94);
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

.auth-link {
  color: #2563eb;
  font-weight: 600;
}

.role-group {
  display: inline-flex;
}

.auth-submit {
  width: 100%;
  border-radius: 14px;
  height: 48px;
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
