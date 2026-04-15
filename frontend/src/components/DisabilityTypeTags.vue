<script setup>
import { computed } from 'vue'
import { parseDisabilityTypesFromStorage } from '../constants/disability'

const props = defineProps({
  /** 逗号/顿号分隔的存储串，或空 */
  value: {
    type: String,
    default: ''
  }
})

const tags = computed(() => parseDisabilityTypesFromStorage(props.value))
</script>

<template>
  <span class="disability-type-tags">
    <template v-if="tags.length">
      <el-tag v-for="t in tags" :key="t" size="small" class="dt-tag" type="info">{{ t }}</el-tag>
    </template>
    <span v-else class="dt-empty">-</span>
  </span>
</template>

<style scoped>
.disability-type-tags {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}

.dt-empty {
  color: var(--el-text-color-placeholder);
}

.dt-tag {
  margin: 0;
}
</style>
