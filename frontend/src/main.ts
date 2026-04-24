import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router'
import App from './App.vue'
import { useAuthStore } from './stores/auth'
import './assets/main.css'

async function bootstrap() {
  const app = createApp(App)
  const pinia = createPinia()
  const auth = useAuthStore(pinia)

  await auth.init()

  app.use(pinia)
  app.use(router)
  app.use(ElementPlus)
  app.mount('#app')
}

void bootstrap()
