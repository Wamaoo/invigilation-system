<!-- 管理员的侧边栏+头部布局 -->
<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { useSemesterStore } from '@/stores/semester'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { House, User, Document, Calendar, Lock, Moon, Sunny, Setting } from '@element-plus/icons-vue'
import { updateAdminPassword, setCurrentSemester, type SemesterItem } from '@/api/admin'
import { useWebSocket } from '@/composables/useWebSocket'

const userStore = useUserStore()
const themeStore = useThemeStore()
const semesterStore = useSemesterStore()
const router = useRouter()
const username = userStore.username
const themeIcon = computed(() => themeStore.isDark ? Sunny : Moon)

// WebSocket 实时通知
const { connect: wsConnect, disconnect: wsDisconnect } = useWebSocket()
onMounted(async () => {
  if (userStore.isLogin) {
    wsConnect()
    await semesterStore.fetchCurrent()
    await semesterStore.fetchList()
  }
})
onUnmounted(() => wsDisconnect())

// 学期切换
const switchingSemester = ref(false)
const handleSemesterSwitch = async (semester: SemesterItem) => {
  if (semester.isCurrent === 1) return
  switchingSemester.value = true
  try {
    await setCurrentSemester(semester.id)
    await semesterStore.fetchCurrent()
    await semesterStore.fetchList()
    ElMessage.success('已切换到 ' + semester.name)
    // 刷新当前页面
    router.go(0)
  } catch {
    ElMessage.error('切换失败')
  }
  switchingSemester.value = false
}

// 退出登录
const logout = () => {
  wsDisconnect()
  try {
    userStore.logout()
    router.push('/login')
  } catch (err) {
    console.log('退出登录报错:', err)
  }
}

// 修改密码
const pwdDialogVisible = ref(false)
const pwdFormRef = ref()
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdLoading = ref(false)
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_: any, value: string, callback: Function) => {
        if (value !== pwdForm.value.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else { callback() }
      }, trigger: 'blur'
    }
  ]
}

const submitPassword = async () => {
  try {
    await pwdFormRef.value.validate()
    pwdLoading.value = true
    await updateAdminPassword({
      username: username || 'admin',
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword
    })
    ElMessage.success('密码修改成功')
    pwdDialogVisible.value = false
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch { /* 验证失败或接口报错 */ }
  pwdLoading.value = false
}
</script>

<template>
  <el-container style="height: 100vh">
    <el-aside width="200px" class="admin-sidebar">
      <div class="sidebar-header">
        <h2>监考管理系统</h2>
        <p class="sidebar-subtitle">管理员端</p>
      </div>
      <!-- 侧边栏菜单（点击跳转对应页面） -->
      <el-menu
        :default-active="$route.path"
        class="admin-menu"
        :background-color="'var(--admin-sidebar-bg)'"
        :text-color="'var(--admin-sidebar-text)'"
        :active-text-color="'var(--admin-sidebar-active)'"
        router
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><House /></el-icon>
          <span>控制台</span>
        </el-menu-item>
        <el-menu-item index="/admin/teacher-manage">
          <el-icon><User /></el-icon>
          <span>教师管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/exam-manage">
          <el-icon><Document /></el-icon>
          <span>考试管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/invigilation-config">
          <el-icon><Calendar /></el-icon>
          <span>监考配置</span>
        </el-menu-item>
        <el-menu-item index="/admin/semester-manage">
          <el-icon><Setting /></el-icon>
          <span>学期管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <!-- 右侧内容区 -->
    <el-container>
      <el-header
        class="admin-header"
      >
        <div style="display: flex; align-items: center; gap: 12px">
          <span>欢迎您，{{ username || '管理员' }}</span>
          <el-select
            v-model="semesterStore.current"
            value-key="id"
            placeholder="选择学期"
            size="small"
            style="width: 170px"
            :loading="switchingSemester"
            @change="handleSemesterSwitch"
          >
            <el-option
              v-for="s in semesterStore.list"
              :key="s.id"
              :label="s.name + (s.isCurrent === 1 ? ' (当前)' : '')"
              :value="s"
            />
          </el-select>
          <el-button text size="small" @click="pwdDialogVisible = true">
            <el-icon style="margin-right: 4px"><Lock /></el-icon>修改密码
          </el-button>
        </div>
        <div>
          <el-button text size="small" @click="themeStore.toggle" style="margin-right: 8px">
            <el-icon><component :is="themeIcon" /></el-icon>
          </el-button>
          <el-button type="text" @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <!-- 主内容区（显示子页面，比如教师管理、考试管理） -->
      <el-main style="padding: 20px; overflow-y: auto">
        <router-view />
      </el-main>
    </el-container>
  </el-container>

  <!-- 修改密码弹窗 -->
  <el-dialog v-model="pwdDialogVisible" title="修改密码" width="400px">
    <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="100px">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码"></el-input>
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码（至少6位）"></el-input>
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码"></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="pwdDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitPassword" :loading="pwdLoading">确定</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.admin-sidebar {
  background-color: var(--admin-sidebar-bg);
  color: var(--admin-sidebar-text);
  transition: background-color 0.3s;
}
.sidebar-header {
  text-align: center;
  padding: 20px;
  border-bottom: 1px solid var(--admin-sidebar-border);
  transition: border-color 0.3s;
}
.sidebar-header h2 {
  margin: 0;
  color: var(--admin-sidebar-text);
}
.sidebar-subtitle {
  font-size: 12px;
  opacity: 0.7;
  margin: 0;
  color: var(--admin-sidebar-text);
}
.admin-menu {
  border-right: none !important;
  transition: background-color 0.3s;
}
.admin-header {
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
