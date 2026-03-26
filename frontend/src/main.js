import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'
import pinia from './stores'
import { initAccessibility } from './accessibility/focus'
import { useAccessibilityStore } from './stores/accessibility'
import './styles/index.css'

const app = createApp(App)

app.use(pinia)
app.use(router)
app.use(ElementPlus)

const accessibilityStore = useAccessibilityStore(pinia)
accessibilityStore.init()
initAccessibility(() => accessibilityStore.focusHighlight)

app.mount('#app')
