<template>
  <section class="admin-feedback-page" aria-label="用户反馈工单">
    <el-card shadow="never" class="hero-card">
      <template #header>
        <div class="section-header">
          <h2>用户反馈</h2>
          <el-button :loading="loading" @click="loadList(1)">刷新</el-button>
        </div>
      </template>

      <div class="toolbar">
        <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 140px" @change="loadList(1)">
          <el-option label="待处理" value="PENDING" />
          <el-option label="已处理" value="RESOLVED" />
        </el-select>
        <el-input
          v-model="keyword"
          clearable
          placeholder="标题 / 正文 / 用户名"
          style="max-width: 260px"
          @keyup.enter="loadList(1)"
        />
        <el-button type="primary" :loading="loading" @click="loadList(1)">搜索</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" border empty-text="暂无工单">
        <el-table-column prop="id" label="工单号" width="88" />
        <el-table-column prop="senderUsername" label="用户" width="120" />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">{{ categoryText(row.category) }}</template>
        </el-table-column>
        <el-table-column label="角色" width="96">
          <template #default="{ row }">{{ roleText(row.senderRole) }}</template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="140" show-overflow-tooltip />
        <el-table-column prop="content" label="正文" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="96">
          <template #default="{ row }">{{ row.status === 'RESOLVED' ? '已处理' : '待处理' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" min-width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" type="primary" link @click="openResolve(row)">处理</el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager-wrap">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="(p) => loadList(p)"
          @size-change="() => loadList(1)"
        />
      </div>
    </el-card>

    <el-dialog v-model="resolveVisible" title="处理反馈工单" width="520px" destroy-on-close @closed="onResolveClosed">
      <p v-if="resolveRow" class="resolve-meta">工单 #{{ resolveRow.id }} · {{ resolveRow.senderUsername }}</p>
      <el-form label-position="top">
        <el-form-item label="处理备注（将通知用户，必填）">
          <el-input v-model="resolveRemark" type="textarea" :rows="5" maxlength="500" show-word-limit placeholder="请填写处理说明、是否解除黑名单等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resolveVisible = false">取消</el-button>
        <el-button type="primary" :loading="resolveSubmitting" @click="confirmResolve">确认已处理</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminUserFeedback, resolveAdminUserFeedback } from '../../api/userFeedback'

const loading = ref(false)
const rows = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterStatus = ref('')
const keyword = ref('')

const resolveVisible = ref(false)
const resolveRow = ref(null)
const resolveRemark = ref('')
const resolveSubmitting = ref(false)

function categoryText(c) {
  if (c === 'BLACKLIST_APPEAL') {
    return '黑名单申诉'
  }
  return '一般咨询'
}

function roleText(role) {
  if (role === 'JOB_SEEKER') {
    return '求职者'
  }
  if (role === 'ENTERPRISE') {
    return '企业'
  }
  return role || '-'
}

async function loadList(p) {
  loading.value = true
  try {
    if (p) {
      page.value = p
    }
    const res = await getAdminUserFeedback({
      page: page.value,
      size: pageSize.value,
      status: filterStatus.value || undefined,
      keyword: keyword.value?.trim() || undefined
    })
    const body = res.data || {}
    rows.value = body.records || []
    total.value = Number(body.total || 0)
  } finally {
    loading.value = false
  }
}

function openResolve(row) {
  resolveRow.value = row
  resolveRemark.value = ''
  resolveVisible.value = true
}

function onResolveClosed() {
  resolveRow.value = null
  resolveRemark.value = ''
}

async function confirmResolve() {
  const remark = resolveRemark.value?.trim() || ''
  if (!remark) {
    ElMessage.warning('请填写处理备注')
    return
  }
  const id = resolveRow.value?.id
  if (!id) {
    return
  }
  resolveSubmitting.value = true
  try {
    await resolveAdminUserFeedback(id, { remark })
    ElMessage.success('已处理并通知用户')
    resolveVisible.value = false
    await loadList(page.value)
  } finally {
    resolveSubmitting.value = false
  }
}

onMounted(() => {
  loadList(1)
})
</script>

<style scoped>
.admin-feedback-page {
  width: 100%;
  max-width: min(var(--jp-content-max), 100%);
  margin: 0 auto;
}

.hero-card {
  border-radius: 12px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.section-header h2 {
  margin: 0;
  font-size: 1.25rem;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}

.pager-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.resolve-meta {
  margin: 0 0 12px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
</style>
