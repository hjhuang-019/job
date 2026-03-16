import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    siteName: '残疾人就业 Web 平台'
  })
})
