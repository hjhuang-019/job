<template>
  <span v-if="showRich" class="enterprise-apply-msg">
    {{ parts.prefix }}请到「<RouterLink class="enterprise-apply-msg-link" :to="applicationsPath">投递记录</RouterLink>」查看并处理。
  </span>
  <span v-else-if="adminFeedbackLinkSplit" class="enterprise-apply-msg">
    <span>{{ adminFeedbackLinkSplit.before }}「</span>
    <RouterLink class="enterprise-apply-msg-link" to="/admin/feedback">用户反馈</RouterLink>
    <span>」{{ adminFeedbackLinkSplit.after }}</span>
  </span>
  <span v-else>{{ row.content }}</span>
</template>

<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { splitEnterpriseApplyReceivedMessage } from '../utils/enterpriseApplyMessage'

const props = defineProps({
  row: {
    type: Object,
    required: true
  },
  userRole: {
    type: String,
    required: true
  }
})

const parts = computed(() => splitEnterpriseApplyReceivedMessage(props.row?.content || ''))

const applicationsPath = computed(() => `/enterprise/jobs/${props.row.relatedJobId}/applications`)

/** 与后端 UserFeedbackServiceImpl 通知文案「请到「用户反馈」菜单处理。」一致 */
const FEEDBACK_MENU_MARKER = '「用户反馈」'

const adminFeedbackLinkSplit = computed(() => {
  if (props.userRole !== 'ADMIN' || props.row?.messageType !== 'USER_FEEDBACK') {
    return null
  }
  const c = String(props.row?.content || '')
  const i = c.indexOf(FEEDBACK_MENU_MARKER)
  if (i < 0) {
    return null
  }
  return {
    before: c.slice(0, i),
    after: c.slice(i + FEEDBACK_MENU_MARKER.length)
  }
})

const showRich = computed(
  () =>
    props.userRole === 'ENTERPRISE' &&
    props.row?.messageType === 'APPLY_RECEIVED' &&
    props.row?.relatedJobId != null &&
    parts.value.matched
)
</script>

<style scoped>
.enterprise-apply-msg-link {
  font-weight: 600;
  text-decoration: underline;
}
</style>
