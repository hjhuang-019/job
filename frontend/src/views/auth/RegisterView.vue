<template>
  <section class="auth-page">
    <el-card shadow="never" class="auth-card">
      <template #header>
        <div class="auth-card-header">
          <div>
            <h2>用户注册</h2>
            <p>请选择注册身份并填写基础信息。</p>
          </div>
          <RouterLink class="auth-link" to="/login">已有账号？去登录</RouterLink>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleSubmit"
      >
        <el-form-item label="注册身份" prop="role" for="role">
          <el-radio-group id="role" v-model="form.role" aria-label="注册身份选择">
            <el-radio value="JOB_SEEKER">求职者</el-radio>
            <el-radio value="ENTERPRISE">企业</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="用户名" prop="username" for="username">
          <el-input
            id="username"
            v-model="form.username"
            name="username"
            autocomplete="username"
            placeholder="请输入4-20位用户名"
          />
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName" for="realName">
          <el-input
            id="realName"
            v-model="form.realName"
            name="realName"
            autocomplete="name"
            placeholder="请输入真实姓名"
          />
        </el-form-item>

        <el-form-item v-if="form.role === 'ENTERPRISE'" label="企业名称" prop="enterpriseName" for="enterpriseName">
          <el-input
            id="enterpriseName"
            v-model="form.enterpriseName"
            name="enterpriseName"
            placeholder="请输入企业全称"
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone" for="phone">
          <el-input
            id="phone"
            v-model="form.phone"
            name="phone"
            autocomplete="tel"
            placeholder="请输入手机号"
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email" for="email">
          <el-input
            id="email"
            v-model="form.email"
            name="email"
            autocomplete="email"
            placeholder="请输入邮箱"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password" for="password">
          <el-input
            id="password"
            v-model="form.password"
            name="password"
            type="password"
            show-password
            autocomplete="new-password"
            placeholder="请输入6-20位密码"
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword" for="confirmPassword">
          <el-input
            id="confirmPassword"
            v-model="form.confirmPassword"
            name="confirmPassword"
            type="password"
            show-password
            autocomplete="new-password"
            placeholder="请再次输入密码"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="auth-submit"
            :loading="loading"
            native-type="submit"
            @click="handleSubmit"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
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
    username: form.username,
    realName: form.realName,
    phone: form.phone,
    email: form.email,
    password: form.password,
    confirmPassword: form.confirmPassword
  }

  if (form.role === 'ENTERPRISE') {
    payload.enterpriseName = form.enterpriseName
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
