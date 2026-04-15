<template>
  <section class="user-feedback-page" aria-label="联系管理员">
    <el-card shadow="never" class="hero-card">
      <template #header>
        <div class="section-header">
          <h2>联系管理员</h2>
          <p class="section-desc">
            求职者与企业可在此向平台管理员反馈问题、咨询事项；若您已被列入黑名单，还可选择「黑名单申诉」申请解除。
          </p>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <el-form-item label="反馈类型" prop="category">
          <el-radio-group v-model="form.category">
            <el-radio label="GENERAL">一般咨询 / 事项反馈</el-radio>
            <el-radio label="BLACKLIST_APPEAL" :disabled="!authStore.isBlacklisted">黑名单申诉</el-radio>
          </el-radio-group>
          <p v-if="!authStore.isBlacklisted" class="field-hint">
            「黑名单申诉」仅适用于<strong>已被列入黑名单</strong>的账号；您当前未在黑名单中，请使用「一般咨询」反馈问题。
          </p>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="150" show-word-limit placeholder="简要概括您的诉求" />
        </el-form-item>
        <el-form-item label="正文" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            maxlength="2000"
            show-word-limit
            placeholder="请详细说明情况、申诉理由或希望管理员协助的内容"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提交反馈</el-button>
        </el-form-item>
      </el-form>

      <el-divider content-position="left">我的反馈记录</el-divider>
      <el-table v-loading="listLoading" :data="mineRows" border empty-text="暂无记录" size="small">
        <el-table-column prop="id" label="工单号" width="88" />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">{{ categoryText(row.category) }}</template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">{{ statusText(row.status) }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" min-width="160" />
        <el-table-column prop="adminRemark" label="处理说明" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.adminRemark || '-' }}</template>
        </el-table-column>
      </el-table>
      <div class="pager-wrap">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[5, 10, 20]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadMine"
          @size-change="() => loadMine(1)"
        />
      </div>
    </el-card>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyUserFeedback, submitUserFeedback } from '../../api/userFeedback'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()
const formRef = ref()
const submitting = ref(false)
const listLoading = ref(false)
const mineRows = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const form = reactive({
  category: 'GENERAL',
  title: '',
  content: ''
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入正文', trigger: 'blur' }]
}

watch(
  () => authStore.isBlacklisted,
  (bl) => {
    if (!bl && form.category === 'BLACKLIST_APPEAL') {
      form.category = 'GENERAL'
    }
  }
)

function categoryText(c) {
  if (c === 'BLACKLIST_APPEAL') {
    return '黑名单申诉'
  }
  return '一般咨询'
}

function statusText(s) {
  if (s === 'RESOLVED') {
    return '已处理'
  }
  if (s === 'PENDING') {
    return '待处理'
  }
  return s || '-'
}

async function loadMine(p) {
  listLoading.value = true
  try {
    if (p) {
      page.value = p
    }
    const res = await getMyUserFeedback({ page: page.value, size: size.value })
    const body = res.data || {}
    mineRows.value = body.records || []
    total.value = Number(body.total || 0)
  } finally {
    listLoading.value = false
  }
}

async function handleSubmit() {
  const ok = await formRef.value?.validate().catch(() => false)
  if (!ok) {
    return
  }
  submitting.value = true
  try {
    await submitUserFeedback({
      category: form.category,
      title: form.title.trim(),
      content: form.content.trim()
    })
    ElMessage.success('提交成功')
    form.title = ''
    form.content = ''
    await loadMine(1)
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  if (authStore.token) {
    await authStore.fetchCurrentUser().catch(() => {})
  }
  loadMine(1)
})
</script>

<style scoped>
.user-feedback-page {
  width: 100%;
  max-width: min(var(--jp-content-max), 100%);
  margin: 0 auto;
}

.hero-card {
  border-radius: 12px;
}

.section-header h2 {
  margin: 0 0 8px;
  font-size: 1.25rem;
}

.section-desc {
  margin: 0;
  font-size: 0.875rem;
  color: var(--el-text-color-secondary);
  line-height: 1.55;
}

.field-hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}

.pager-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
