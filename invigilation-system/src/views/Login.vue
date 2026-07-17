<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user' //用户状态
import { ElMessage } from 'element-plus'
import { login } from '@/api/teacher'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref<any>(null) //表单引用（用于校验）

// 登录表单数据
const loginForm = ref({
  username: '',
  password: '',
  role: 'teacher' as 'admin' | 'teacher'
})

// 表单校验规则
const loginRules = ref({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不少于6位', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择用户身份', trigger: 'change' }]
})

// 登录处理核心逻辑
const handleLogin = async () => {
  try {
    //1. 表单校验
    await loginFormRef.value.validate()

    //2. 发送登录请求
    const res = await login({
      username: loginForm.value.username,
      password: loginForm.value.password,
      role: loginForm.value.role
    })

    //3. 登录成功，更新用户状态（含 JWT token）
    userStore.login(res.username, res.role as 'admin' | 'teacher', res.token)

    //4. 提示并跳转
    ElMessage.success(`欢迎回来，${res.username}！`)
    if (res.role === 'admin') {
      router.push('/admin')
    } else {
      router.push('/teacher')
    }
  } catch (error: any) {
    if (error.msg) {
      ElMessage.error(error.msg)
    } else if (error.message) {
      ElMessage.error(`登录失败：${error.message}`)
    } else {
      ElMessage.error('用户名或密码错误，请检查后重试')
    }
  }
}
</script>
<template>
  <div class="login-page">
    <!-- 头部：校徽 + 系统名称 -->
    <header class="login-header">
      <div class="header-container">
        <img
          src="@/assets/logo.png"
          alt="山东理工大学校徽"
          class="school-logo"
        />
        <div class="system-title">
          <p>监考管理查询系统</p>
        </div>
      </div>
    </header>
    <!-- 主体：背景 + 登录框 -->
    <main class="login-main">
      <div class="school-bg"></div>
      <el-card class="login-card">
        <div style="text-align: center; margin-bottom: 20px">
          <h2 style="color: var(--text-primary); font-size: 20px; opacity: 0.6">
            请选择身份登录
          </h2>
        </div>
        <el-form
          :model="loginForm"
          :rules="loginRules"
          ref="loginFormRef"
          label-width="80px"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
            ></el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              show-password
              placeholder="请输入密码"
            ></el-input>
          </el-form-item>
          <el-form-item label="用户身份" prop="role">
            <el-radio-group v-model="loginForm.role">
              <el-radio label="admin">管理员</el-radio>
              <el-radio label="teacher">教师</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleLogin" style="width: 100%"
              >登录</el-button
            >
          </el-form-item>
        </el-form>
      </el-card>
    </main>
    <!-- 底部：版权信息 -->
    <footer class="login-footer">
      <div class="footer-bg">
        <img src="@/assets/foot.jpg" alt="底部背景" class="footer-bg-img" />
        <div class="footer-text">
          <div class="copyright">
            版权所有©山东理工大学 地址：山东省淄博市张店区新村西路266号 (255000)
            <br />
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
/* 整个页面布局 */
.login-page {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--login-bg);
  transition: background-color 0.3s;
}

/* 头部样式：校徽靠左，系统名称靠右（两端对齐） */
.login-header {
  height: 70px;
  background-color: var(--login-header-bg);
  border-bottom: 1px solid var(--login-header-border);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition:
    background-color 0.3s,
    border-color 0.3s;
}
.header-container {
  width: 1200px;
  height: 100%;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
.school-logo {
  height: 90px;
  width: auto;
  object-fit: contain;
}
.system-title p {
  font-size: 30px;
  color: #297eca;
  margin: 0;
  font-weight: 400;
  font-family: '方正字迹-龙吟体 简', 'Microsoft YaHei', sans-serif;
  letter-spacing: 2px;
}

/* 中间主体样式：背景+登录框 */
.login-main {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 100px;
  background: url('@/assets/school02.jpg') no-repeat center left;
  background-size: cover;
}
/* 登录框样式 */
.login-card {
  width: 400px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  background-color: var(--login-card-bg);
  transition: background-color 0.3s;
}

/* 底部样式：图片作为背景，文字居中 */
.login-footer {
  height: 55px;
  overflow: hidden;
}
.footer-bg {
  position: relative;
  width: 100%;
  height: 100%;
  background-color: var(--login-footer-bg);
  transition: background-color 0.3s;
}
/* 底部背景图：覆盖整个底部区域 */
.footer-bg-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.8;
}
/* 居中的文字区域 */
.footer-text {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 0 20px;
  color: var(--text-secondary);
  font-size: 12px;
  font-family: 'Microsoft YaHei', sans-serif;
}
.motto {
  font-size: 14px;
}
.copyright {
  text-align: center;
  line-height: 1.5;
}
</style>
