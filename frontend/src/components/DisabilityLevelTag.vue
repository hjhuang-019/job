<script setup>
import { computed } from 'vue'
import { normalizeDisabilityLevelFromStorage } from '../constants/disability'

const props = defineProps({
  value: {
    type: String,
    default: ''
  }
})

const label = computed(() => normalizeDisabilityLevelFromStorage(props.value))
</script>

<template>
  <el-tag v-if="label" size="small" type="success" class="dl-tag">{{ label }}</el-tag>
  <span v-else class="dl-empty">-</span>
</template>

<style scoped>
.dl-empty {
  color: var(--el-text-color-placeholder);
}

.dl-tag {
  margin: 0;
}
</style>
