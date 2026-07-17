<script setup lang="ts">
import { computed, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { useRouter } from 'vue-router'
import { Ticket, Search, User, Moon, Sunny } from '@element-plus/icons-vue'
import { useWebSocket } from '@/composables/useWebSocket'

const userStore = useUserStore()
const themeStore = useThemeStore()
const router = useRouter()
const username = userStore.username
const themeIcon = computed(() => themeStore.isDark ? Sunny : Moon)

// WebSocket 实时通知
const { connect: wsConnect, disconnect: wsDisconnect } = useWebSocket()
onMounted(() => { if (userStore.isLogin) wsConnect() })
onUnmounted(() => wsDisconnect())

const logout = () => {
  wsDisconnect()
  try {
    userStore.logout()
    router.push('/login')
  } catch (err) {
    console.log('退出登录报错:', err)
  }
}
</script>

<template>
  <el-container style="height: 100vh">
    <el-aside width="200px" class="teacher-sidebar">
      <div class="sidebar-header">
        <h2>监考管理系统</h2>
        <p class="sidebar-subtitle">教师端</p>
      </div>
      <el-menu
        :default-active="$route.path"
        class="teacher-menu"
        :background-color="'var(--teacher-sidebar-bg)'"
        :text-color="'var(--teacher-sidebar-text)'"
        :active-text-color="'var(--teacher-sidebar-active)'"
        router
      >
        <el-menu-item index="/teacher/my-invigilation">
          <el-icon><Ticket /></el-icon>
          <span>我的监考</span>
        </el-menu-item>
        <el-menu-item index="/teacher/invigilation-query">
          <el-icon><Search /></el-icon>
          <span>监考查询</span>
        </el-menu-item>
        <el-menu-item index="/teacher/profile">
          <el-icon><User /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="teacher-header">
        <div>欢迎您，{{ username || '教师' }}</div>
        <div>
          <el-button text size="small" @click="themeStore.toggle" style="margin-right: 8px">
            <el-icon><component :is="themeIcon" /></el-icon>
          </el-button>
          <el-button type="text" @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main style="padding: 20px; overflow-y: auto">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.teacher-sidebar {
  background-color: var(--teacher-sidebar-bg);
  color: var(--teacher-sidebar-text);
  transition: background-color 0.3s;
}
.sidebar-header {
  text-align: center;
  padding: 20px;
  border-bottom: 1px solid var(--teacher-sidebar-border);
  transition: border-color 0.3s;
}
.sidebar-header h2 {
  margin: 0;
  color: var(--teacher-sidebar-text);
}
.sidebar-subtitle {
  font-size: 12px;
  opacity: 0.7;
  margin: 0;
  color: var(--teacher-sidebar-text);
}
.teacher-menu {
  border-right: none !important;
  transition: background-color 0.3s;
}
.teacher-header {
  background-color: var(--header-bg) !important;
  border-bottom: 1px solid var(--header-border) !important;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  transition: background-color 0.3s, border-color 0.3s;
  color: var(--text-primary);
}
</style>
