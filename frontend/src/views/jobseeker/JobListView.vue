<template>
  <section class="job-browse-page" aria-label="岗位浏览页面">
    <el-card shadow="never" class="hero-card">
      <template #header>
        <h2>岗位列表</h2>
      </template>

      <el-form :model="filters" label-position="top" aria-label="岗位搜索筛选表单" @keyup.enter="handleSearch">
        <el-row :gutter="12">
          <el-col :xs="24" :md="8">
            <el-form-item label="关键字" for="keyword">
              <el-input id="keyword" v-model="filters.keyword" placeholder="请输入岗位名称或技能关键字" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="5">
            <el-form-item label="城市" for="city">
              <el-input id="city" v-model="filters.city" placeholder="如：上海" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="5">
            <el-form-item label="工作方式" for="workMode">
              <el-select id="workMode" v-model="filters.workMode" placeholder="全部" clearable>
                <el-option label="线下办公" value="OFFLINE" />
                <el-option label="远程办公" value="REMOTE" />
                <el-option label="混合办公" value="HYBRID" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="6">
            <el-form-item label="残疾适配类型" for="disabilitySupportType">
              <el-input
                id="disabilitySupportType"
                v-model="filters.disabilitySupportType"
                placeholder="如：听力障碍"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" aria-label="按条件搜索岗位" @click="handleSearch">搜索岗位</el-button>
          <el-button @click="handleReset">重置筛选</el-button>
        </el-form-item>
      </el-form>

      <el-table
        v-loading="loading"
        :data="jobs"
        border
        empty-text="暂无符合条件的岗位，请调整筛选条件后重试"
        aria-label="岗位列表结果"
      >
        <el-table-column prop="title" label="岗位名称" min-width="200" />
        <el-table-column prop="enterpriseName" label="企业名称" min-width="180" />
        <el-table-column prop="city" label="城市" width="100" />
        <el-table-column label="工作方式" width="120">
          <template #default="{ row }">{{ workModeText(row.workMode) }}</template>
        </el-table-column>
        <el-table-column label="薪资范围" min-width="140">
          <template #default="{ row }">{{ row.salaryMin }} - {{ row.salaryMax }} 元</template>
        </el-table-column>
        <el-table-column prop="disabilitySupportType" label="适配残疾类型" min-width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :aria-label="`查看岗位 ${row.title} 详情`" @click="goDetail(row.id)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-pagination" role="navigation" aria-label="岗位分页">
        <el-pagination
          background
          layout="total, prev, pager, next, sizes"
          :total="total"
          :page-size="pageSize"
          :current-page="pageNum"
          :page-sizes="[5, 10, 20]"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPublishedJobs } from '../../api/jobs'

const router = useRouter()
const loading = ref(false)
const jobs = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const filters = reactive({
  keyword: '',
  city: '',
  workMode: '',
  disabilitySupportType: ''
})

function workModeText(mode) {
  return {
    OFFLINE: '线下办公',
    REMOTE: '远程办公',
    HYBRID: '混合办公'
  }[mode] || mode
}

async function loadJobs() {
  loading.value = true
  try {
    const response = await getPublishedJobs({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: filters.keyword || undefined,
      city: filters.city || undefined,
      workMode: filters.workMode || undefined,
      disabilitySupportType: filters.disabilitySupportType || undefined
    })
    jobs.value = response.data?.records || []
    total.value = Number(response.data?.total || 0)
  } catch (error) {
    ElMessage.error('加载岗位列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadJobs()
}

function handleReset() {
  filters.keyword = ''
  filters.city = ''
  filters.workMode = ''
  filters.disabilitySupportType = ''
  pageNum.value = 1
  loadJobs()
}

function handleSizeChange(size) {
  pageSize.value = size
  pageNum.value = 1
  loadJobs()
}

function handlePageChange(page) {
  pageNum.value = page
  loadJobs()
}

function goDetail(id) {
  router.push(`/jobs/${id}`)
}

onMounted(() => {
  loadJobs()
})
</script>
