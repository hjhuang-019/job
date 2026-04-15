<template>
  <section class="job-browse-page" aria-label="岗位浏览页面">
    <el-card shadow="never" class="hero-card">
      <template #header>
        <div class="job-browse-header">
          <h2>岗位浏览</h2>
          <p class="job-browse-hint">
            列表已按与您个人中心「残疾类型」的匹配程度排序，分值不在页面展示。下方「适合招收的残疾类型」可多选，岗位说明中<strong>包含任一</strong>所选类型即可出现在结果中。
          </p>
        </div>
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
            <el-form-item label="适合招收的残疾类型" for="disabilityTypeFilter">
              <el-select
                id="disabilityTypeFilter"
                v-model="filters.disabilityTypeFilterList"
                multiple
                filterable
                clearable
                placeholder="可多选，满足其一即匹配"
                class="job-filter-disability-select"
              >
                <el-option
                  v-for="opt in DISABILITY_TYPE_OPTIONS"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" aria-label="按条件搜索岗位" @click="handleSearch">搜索岗位</el-button>
          <el-button @click="handleReset">重置筛选</el-button>
          <el-button
            :disabled="loading || jobs.length === 0"
            aria-label="语音播报当前页全部岗位信息"
            @click="handleSpeakAllJobs"
          >
            播报本页岗位
          </el-button>
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
        <el-table-column label="适合招收的残疾类型" min-width="220">
          <template #default="{ row }">
            <DisabilityTypeTags :value="row.disabilitySupportType || ''" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link :aria-label="`播报岗位 ${row.title} 信息`" @click="speakSingleJob(row)">播报信息</el-button>
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
import { onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPublishedJobs } from '../../api/jobs'
import { DISABILITY_TYPE_OPTIONS, parseDisabilityTypesFromStorage, serializeDisabilityTypesForStorage } from '../../constants/disability'
import { speakText, isSpeechSupported } from '../../accessibility/speech'
import { useAccessibilityStore } from '../../stores/accessibility'
import { registerJobSeekerVoicePage } from '../../voice/jobSeekerVoiceRegistry'
import DisabilityTypeTags from '../../components/DisabilityTypeTags.vue'

const route = useRoute()
const router = useRouter()
const accessibilityStore = useAccessibilityStore()
const loading = ref(false)
const jobs = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const filters = reactive({
  keyword: '',
  city: '',
  workMode: '',
  disabilityTypeFilterList: []
})

function workModeText(mode) {
  return {
    OFFLINE: '线下办公',
    REMOTE: '远程办公',
    HYBRID: '混合办公'
  }[mode] || mode
}

function canSpeak() {
  return isSpeechSupported() && accessibilityStore.voiceEnabled
}

function speakWithSettings(text, options = {}) {
  if (!canSpeak()) {
    return false
  }
  return speakText(text, {
    ...options,
    rate: accessibilityStore.voiceRate,
    volume: accessibilityStore.voiceVolume
  })
}

function formatJobSpeech(row, index) {
  return `第 ${index + 1} 条：岗位 ${row.title || '未命名'}，企业 ${row.enterpriseName || '未知企业'}，城市 ${row.city || '未填写'}，工作方式 ${workModeText(row.workMode) || '未填写'}，薪资 ${row.salaryMin || 0} 到 ${row.salaryMax || 0} 元，适配 ${row.disabilitySupportType || '未说明'}`
}

function formatSingleJobSpeech(row) {
  return `岗位 ${row.title || '未命名'}，企业 ${row.enterpriseName || '未知企业'}，城市 ${row.city || '未填写'}，工作方式 ${workModeText(row.workMode) || '未填写'}，薪资 ${row.salaryMin || 0} 到 ${row.salaryMax || 0} 元，适配 ${row.disabilitySupportType || '未说明'}`
}

function speakJobsOnPage(mode = 'preview') {
  if (!canSpeak()) {
    return
  }
  if (!jobs.value.length) {
    speakWithSettings('岗位列表暂无符合条件的数据，请调整筛选条件后再试', { force: true })
    return
  }

  const summary = `岗位列表共 ${total.value} 条，当前第 ${pageNum.value} 页，每页 ${pageSize.value} 条，本页 ${jobs.value.length} 条`
  const records = mode === 'all' ? jobs.value : jobs.value.slice(0, 10)
  const details = records.map((row, index) => formatJobSpeech(row, index)).join('；')
  const tailTip = mode !== 'all' && jobs.value.length > 10 ? '。如需完整播报，请点击播报本页岗位按钮' : ''
  speakWithSettings(`${summary}。${details}${tailTip}`, {
    force: true,
    minIntervalMs: 600
  })
}

function handleSpeakAllJobs() {
  if (!canSpeak()) {
    ElMessage.info('请先在顶部开启语音播报')
    return
  }
  speakJobsOnPage('all')
}

function speakSingleJob(row) {
  if (!canSpeak()) {
    ElMessage.info('请先在顶部开启语音播报')
    return
  }
  speakWithSettings(formatSingleJobSpeech(row), { force: true })
}

async function loadJobs(announceMode = 'preview') {
  loading.value = true
  try {
    const response = await getPublishedJobs({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: filters.keyword || undefined,
      city: filters.city || undefined,
      workMode: filters.workMode || undefined,
      disabilitySupportType: serializeDisabilityTypesForStorage(filters.disabilityTypeFilterList) || undefined
    })
    jobs.value = response.data?.records || []
    total.value = Number(response.data?.total || 0)
    speakJobsOnPage(announceMode)
  } catch (error) {
    ElMessage.error('加载岗位列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadJobs('preview')
}

function handleReset() {
  filters.keyword = ''
  filters.city = ''
  filters.workMode = ''
  filters.disabilityTypeFilterList = []
  pageNum.value = 1
  loadJobs('preview')
}

function handleSizeChange(size) {
  pageSize.value = size
  pageNum.value = 1
  loadJobs('preview')
}

function handlePageChange(page) {
  pageNum.value = page
  loadJobs('preview')
}

function goDetail(id) {
  router.push(`/jobs/${id}`)
}

function applyQueryFilters() {
  const q = route.query || {}
  if (q.keyword != null && String(q.keyword).trim() !== '') {
    filters.keyword = String(q.keyword)
  }
  if (q.city != null && String(q.city).trim() !== '') {
    filters.city = String(q.city)
  }
  if (q.workMode != null && String(q.workMode).trim() !== '') {
    filters.workMode = String(q.workMode)
  }
  if (q.disabilitySupportType != null && String(q.disabilitySupportType).trim() !== '') {
    filters.disabilityTypeFilterList = parseDisabilityTypesFromStorage(String(q.disabilitySupportType))
  }
}

function onVoiceInterpret(vo) {
  const f = vo.jobFilters
  if (!f || typeof f !== 'object') {
    return
  }
  let changed = false
  if (f.keyword != null && String(f.keyword).trim() !== '') {
    filters.keyword = String(f.keyword).trim()
    changed = true
  }
  if (f.city != null && String(f.city).trim() !== '') {
    filters.city = String(f.city).trim()
    changed = true
  }
  if (f.workMode != null && String(f.workMode).trim() !== '') {
    filters.workMode = String(f.workMode).trim()
    changed = true
  }
  if (f.disabilitySupportType != null && String(f.disabilitySupportType).trim() !== '') {
    filters.disabilityTypeFilterList = parseDisabilityTypesFromStorage(String(f.disabilitySupportType).trim())
    changed = true
  }
  const intent = String(vo.intent || '')
  if (changed || intent === 'SET_JOB_FILTERS' || intent === 'SUGGEST_SEARCH') {
    handleSearch()
  }
}

let unregisterVoicePage = () => {}

onMounted(() => {
  applyQueryFilters()
  unregisterVoicePage = registerJobSeekerVoicePage(route.name, {
    getPageContext: () => ({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      filters: { ...filters },
      jobs: jobs.value.map((row, index) => ({
        index: index + 1,
        id: row.id,
        title: row.title,
        enterpriseName: row.enterpriseName,
        city: row.city
      })),
      total: total.value
    }),
    onInterpret: onVoiceInterpret
  })
  loadJobs('preview')
})

onUnmounted(() => {
  unregisterVoicePage()
})

watch(
  () => route.query,
  () => {
    applyQueryFilters()
    pageNum.value = 1
    loadJobs('preview')
  }
)
</script>

<style scoped>
.job-browse-header h2 {
  margin: 0 0 8px;
}

.job-browse-hint {
  margin: 0;
  font-size: 13px;
  line-height: 1.55;
  color: var(--el-text-color-secondary);
}

.job-filter-disability-select {
  width: 100%;
}

.job-filter-disability-select :deep(.el-select__tags) {
  flex-wrap: wrap;
}
</style>
