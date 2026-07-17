<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  getTeacherProfile,
  updateTeacherProfile,
  updatePassword
} from '@/api/teacher'

const userStore = useUserStore()

// ====== 个人信息 ======
const profileForm = ref({
  teacherId: '',
  teacherName: '',
  department: '',
  phone: ''
})
const profileLoading = ref(false)

// ====== 密码修改 ======
const pwdForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const pwdFormRef = ref<any>(null)
const pwdDialogVisible = ref(false)

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
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 加载个人信息
const loadProfile = async () => {
  const username = userStore.username
  if (!username) return
  profileLoading.value = true
  try {
    // 先从 sys_user 拿 username 作为 teacherId 查询教师信息
    const res = await getTeacherProfile(username)
    if (res) {
      profileForm.value.teacherId = res.teacherId || ''
      profileForm.value.teacherName = res.teacherName || ''
      profileForm.value.department = res.department || ''
      profileForm.value.phone = res.phone || ''
    }
  } catch {
    ElMessage.error('加载个人信息失败')
  } finally {
    profileLoading.value = false
  }
}

// 保存个人信息
const handleSaveProfile = async () => {
  if (!profileForm.value.teacherId) {
    ElMessage.warning('无法获取教师工号')
    return
  }
  try {
    await updateTeacherProfile(profileForm.value)
    ElMessage.success('个人信息更新成功！')
  } catch {
    ElMessage.error('更新失败')
  }
}

// 打开密码修改弹窗
const openPwdDialog = () => {
  pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  pwdDialogVisible.value = true
}

// 提交密码修改
const handleUpdatePassword = async () => {
  try {
    await pwdFormRef.value.validate()
    await updatePassword({
      username: userStore.username,
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword
    })
    ElMessage.success('密码修改成功！')
    pwdDialogVisible.value = false
  } catch (error: any) {
    if (error?.msg) {
      ElMessage.error(error.msg)
    } else if (typeof error === 'string') {
      // 校验失败时 error 是 string
    } else {
      ElMessage.error('密码修改失败，请检查原密码是否正确')
    }
  }
}

onMounted(loadProfile)
</script>

<template>
  <div>
    <h2 style="color: var(--text-primary); margin-bottom: 24px">个人信息</h2>

    <el-card shadow="hover" v-loading="profileLoading">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span style="font-weight: 600; color: var(--text-primary)">基本资料</span>
          <el-button type="primary" @click="openPwdDialog">修改密码</el-button>
        </div>
      </template>

      <el-form :model="profileForm" label-width="120px" style="max-width: 500px">
        <el-form-item label="工号">
          <el-input v-model="profileForm.teacherId" :disabled="true"></el-input>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="profileForm.teacherName" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="profileForm.department" placeholder="请输入部门"></el-input>
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="profileForm.phone" placeholder="请输入联系电话"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSaveProfile">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 密码修改弹窗 -->
    <el-dialog v-model="pwdDialogVisible" title="修改密码" width="450px">
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入原密码" show-password></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码（至少6位）" show-password></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdatePassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
