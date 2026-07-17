// 项目入口文件（启动项目）

// 导入需要的工具
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import '@/styles/theme.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { useUserStore } from '@/stores/user' //导入用户状态管理

// 1. 创建vue实例
const app = createApp(App)
const pinia = createPinia()

// 2. 全局Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 3.先挂载Pinia
app.use(pinia)

// 初始化用户Store（加载LocalStorage中的登录状态）
try {
  const userStore = useUserStore()
  userStore.initUser() // 从本地存储读取登录信息
  console.log('用户Store初始化完成：', {
    username: userStore.username,
    role: userStore.role,
    isLogin: userStore.isLogin
  })
} catch (error) {
  console.error('用户Store初始化失败：', error)
}

// 4. 挂载组件库和路由
app.use(ElementPlus)
app.use(router)

// 5. 挂载到页面
app.mount('#app')

// 6. 路由守卫（制定跳转规则）
router.beforeEach((to, _, next) => {
  const userStore = useUserStore()
  if (to.path !== '/login' && !userStore.isLogin) {
    next('/login')
  } else {
    if (to.path.startsWith('/admin') && userStore.role !== 'admin') {
      next('/teacher')
    } else if (to.path.startsWith('/teacher') && userStore.role !== 'teacher') {
      next('/admin')
    } else {
      next()
    }
  }
})
