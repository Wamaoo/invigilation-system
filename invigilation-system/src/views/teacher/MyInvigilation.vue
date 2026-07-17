<script setup lang="ts">
import { ref, onMounted, watch, reactive } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheck, Download } from '@element-plus/icons-vue'
import {
  getMyInvigilation,
  submitConflictApply as sendConflictApply
} from '@/api/teacher'
import { getTeacherById } from '@/api/admin'

// 定义监考安排类型
interface MyInvigilation {
  arrangeId: string
  examName: string
  classroom: string
  examTime: string
  examDate: string
  startTime: string
  endTime: string
  examDuration: string
  remark: string
  teacherNames: string
  myName: string
}

// 冲突申请表单类型
interface ConflictApplyForm {
  teacherId: string
  teacherName: string
  arrangeId: string
  reason: string
  fileUrl: string
}

// 初始化变量
const userStore = useUserStore()
const router = useRouter()
const myInvigilationList = ref<MyInvigilation[]>([])
const loading = ref(false)
const isTeacher = ref(false)
const submitLoading = ref(false)

// 冲突申请弹窗相关
const applyDialogVisible = ref(false)
const applyFormRef = ref()
const applyForm = reactive<ConflictApplyForm>({
  teacherId: '',
  teacherName: '',
  arrangeId: '',
  reason: '',
  fileUrl: ''
})

// ====== 上传证明材料 ======
const uploadRef = ref()
const fileList = ref<any[]>([])
const uploadAction = `/invigilation/api/upload`
const uploadHeaders = { /* 无需额外认证头 */ }

const handleUploadSuccess = (res: any) => {
  if (res.code === 200 && res.data) {
    applyForm.fileUrl = res.data
    fileList.value = [{ name: '证明材料', url: res.data }]
    ElMessage.success('文件上传成功')
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

const handleUploadRemove = () => {
  applyForm.fileUrl = ''
  fileList.value = []
}

const handleUploadError = () => {
  ElMessage.error('文件上传失败，请重试')
}

const removeFile = () => {
  applyForm.fileUrl = ''
  fileList.value = []
  uploadRef.value?.clearFiles()
}

// 兼容两种用户名：T008（直接登录）/teacher08（解析）
const getTeacherId = (username: string): string => {
  if (username.startsWith('T')) {
    return username
  }
  const numPart = username.replace(/[^0-9]/g, '')
  return `T${numPart.padStart(3, '0')}`
}

// 获取监考数据（核心修复：添加类型断言，解决TS报错，删除绿色提示）
const getInvigilationData = async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录！')
    router.push('/login')
    return
  }
  if (userStore.role !== 'teacher') {
    isTeacher.value = false
    ElMessage.warning('仅教师可查看监考安排！')
    return
  }

  isTeacher.value = true
  loading.value = true
  try {
    const teacherId = getTeacherId(userStore.username)
    console.log('【我的监考】当前教师ID：', teacherId)

    const myInvigilationData = await getMyInvigilation(teacherId)

    console.log('【我的监考】后端返回数据：', myInvigilationData)
    myInvigilationList.value = myInvigilationData || []
  } catch (error: any) {
    console.error('【我的监考】请求失败：', error)
    ElMessage.error(`获取监考安排失败：${error.message || '网络异常'}`)
    myInvigilationList.value = []
  } finally {
    loading.value = false
  }
}

// 监听Store状态变化
watch(
  () => [userStore.isLogin, userStore.role, userStore.username],
  () => {
    getInvigilationData()
  },
  { immediate: true, deep: true }
)

// 页面挂载时强制刷新
onMounted(() => {
  setTimeout(() => {
    getInvigilationData()
  }, 500)
})

// 打开冲突申请弹窗
const openConflictApply = async (row: MyInvigilation) => {
  const teacherId = getTeacherId(userStore.username)
  applyForm.teacherId = teacherId
  applyForm.teacherName = row.myName || (await getTeacherById(teacherId))?.teacherName || '未知姓名'
  applyForm.arrangeId = row.arrangeId
  applyForm.reason = ''
  applyForm.fileUrl = ''
  applyDialogVisible.value = true
}

// 提交冲突申请
const submitConflictApply = async () => {
  if (!applyFormRef.value || submitLoading.value) return
  try {
    submitLoading.value = true
    await applyFormRef.value.validate()

    await sendConflictApply(applyForm)

    ElMessage.success('申请提交成功，等待管理员审核')
    applyDialogVisible.value = false
    applyForm.reason = ''
    getInvigilationData()
  } catch (error: any) {
    console.error('提交申请失败：', error.message || error)
    ElMessage.error('申请提交失败，请稍后重试')
  } finally {
    submitLoading.value = false
  }
}
</script>

<template>
  <div>
    <h2 style="margin-bottom: 20px; color: var(--text-primary)">我的监考安排</h2>

    <!-- 未登录/非教师提示 -->
    <div
      v-if="!isTeacher"
      style="text-align: center; padding: 20px; color: red"
    >
      请以教师身份登录后查看
    </div>

    <!-- 监考列表 -->
    <el-table
      v-else
      :data="myInvigilationList"
      border
      style="width: 100%"
      v-loading="loading"
    >
      <el-table-column prop="arrangeId" label="安排ID" />
      <el-table-column prop="examName" label="考试科目" />
      <el-table-column prop="classroom" label="考场" />
      <el-table-column prop="examTime" label="考试时间" />
      <el-table-column prop="examDuration" label="考试时长" />
      <el-table-column prop="teacherNames" label="同场监考教师" />
      <el-table-column prop="remark" label="备注" />
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button type="text" @click="openConflictApply(scope.row)"
            >冲突申请</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <div
      class="empty-state"
      v-if="isTeacher && !loading && myInvigilationList.length === 0"
    >
      <el-icon :size="72" color="#38bdf8" style="margin-bottom: 20px;">
        <CircleCheck />
      </el-icon>
      <h3 style="color: var(--text-primary); margin: 0 0 12px 0; font-size: 22px; font-weight: 600;">
        你已完成全部监考任务
      </h3>
      <p style="color: var(--text-secondary); font-size: 15px; margin: 0; line-height: 1.6;">
        所有监考安排已结束，辛苦了！
      </p>
    </div>

    <!-- 冲突申请弹窗 -->
    <el-dialog v-model="applyDialogVisible" title="冲突申请" width="400px">
      <el-form :model="applyForm" ref="applyFormRef" label-width="80px">
        <el-form-item label="工号">
          <el-input v-model="applyForm.teacherId" disabled />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="applyForm.teacherName" disabled />
        </el-form-item>
        <el-form-item label="安排ID">
          <el-input v-model="applyForm.arrangeId" disabled />
        </el-form-item>
        <el-form-item
          label="申请原因"
          prop="reason"
          :rules="[
            { required: true, message: '请填写申请原因', trigger: 'blur' }
          ]"
        >
          <el-input v-model="applyForm.reason" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="证明材料">
          <el-upload
            ref="uploadRef"
            :action="uploadAction"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-remove="handleUploadRemove"
            :on-error="handleUploadError"
            :limit="1"
            :file-list="fileList"
            accept=".jpg,.jpeg,.png,.gif,.bmp,.pdf,.doc,.docx,.zip,.rar"
          >
            <el-button type="primary" size="small">选择文件</el-button>
            <template #tip>
              <span style="font-size: 12px; color: var(--text-secondary)">
                支持 jpg/png/pdf/doc/zip 等格式，单个文件不超过 50MB
              </span>
            </template>
          </el-upload>
          <div v-if="applyForm.fileUrl" style="margin-top: 8px">
            <el-link :href="applyForm.fileUrl" target="_blank" type="primary" :underline="false">
              <el-icon style="margin-right: 4px"><Download /></el-icon>
              查看已上传材料
            </el-link>
            <el-button text size="small" type="danger" @click="removeFile" style="margin-left: 8px">
              删除
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="submitConflictApply"
          :loading="submitLoading"
          >提交申请</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.empty-state {
  margin-top: 40px;
  text-align: center;
  padding: 60px 40px;
  background: var(--empty-state-bg);
  border-radius: 16px;
  border: 2px dashed var(--empty-state-border);
  transition: background 0.3s, border-color 0.3s;
}
</style>
