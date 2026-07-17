<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import type { TeacherForm } from '@/api/admin'
import {
  getTeacherList as fetchTeacherList,
  addTeacher,
  editTeacher,
  deleteTeacher as removeTeacher,
  addUser,
  importTeachers
} from '@/api/admin'
import { exportToExcel } from '@/utils/export'
import ImportDialog from '@/components/ImportDialog.vue'

// 搜索表单
const searchForm = ref({
  teacherName: '',
  department: ''
})

// 教师列表数据（对接后端）
const teacherList = ref<TeacherForm[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 弹窗相关
const teacherDialogVisible = ref(false)
const isEdit = ref(false)
const teacherFormRef = ref<FormInstance | null>(null)
const teacherForm = ref({
  teacherId: '',
  teacherName: '',
  department: '',
  phone: '',
  password: ''
})

// 表单校验规则
const teacherRules = ref({
  teacherId: [{ required: true, message: '请输入教师工号', trigger: 'blur' }],
  teacherName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  department: [
    { required: true, message: '请选择所属部门', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, message: '密码长度不少于6位', trigger: 'blur' }
  ]
})

// 编辑时不需要密码校验
const editRules = ref({
  teacherId: [{ required: true, message: '请输入教师工号', trigger: 'blur' }],
  teacherName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  department: [{ required: true, message: '请选择所属部门', trigger: 'change' }],
  phone: [{ required: true, message: '请输入联系方式', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
})

// 根据是否编辑切换校验规则
const currentRules = computed(() => isEdit.value ? editRules.value : teacherRules.value)


// 页面加载时获取教师列表
onMounted(() => {
  getTeacherList()
})

// 调用后端接口：获取教师列表
const getTeacherList = async () => {
  loading.value = true
  try {
    const res = await fetchTeacherList({
      ...searchForm.value,
      page: currentPage.value,
      size: pageSize.value
    })
    teacherList.value = res.list
    total.value = res.total
  } catch (error) {
    ElMessage.error('获取教师列表失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = { teacherName: '', department: '' }
  currentPage.value = 1
  getTeacherList()
}

// 分页
const handleSizeChange = (val: number) => {
  pageSize.value = val
  getTeacherList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  getTeacherList()
}

// 弹窗操作
const openAddTeacherDialog = () => {
  isEdit.value = false
  teacherForm.value = {
    teacherId: '',
    teacherName: '',
    department: '',
    phone: '',
    password: ''
  }
  teacherDialogVisible.value = true
}

const openEditTeacherDialog = (row: TeacherForm) => {
  isEdit.value = true
  teacherForm.value = { ...row, password: '' } // 编辑时不显示密码
  teacherDialogVisible.value = true
}

// 提交表单（新增/编辑）
const submitTeacherForm = async () => {
  try {
    await teacherFormRef.value?.validate()
    if (isEdit.value) {
      // 编辑教师信息
      const { teacherId, teacherName, department, phone } = teacherForm.value
      await editTeacher({ teacherId, teacherName, department, phone })
      ElMessage.success('编辑成功！')
    } else {
      // 新增教师：同时添加sys_user账号
      const teacherData = { ...teacherForm.value }
      // 1. 新增teacher_info
      await addTeacher(teacherData)
      // 2. 新增sys_user（用户名=教师工号，密码=初始密码，角色=teacher）
      await addUser({
        username: teacherData.teacherId,
        password: teacherData.password,
        role: 'teacher'
      })
      ElMessage.success('新增成功！')
    }
    teacherDialogVisible.value = false
    getTeacherList()
  } catch (error) {
    ElMessage.error('提交失败，请检查表单！')
  }
}

// 删除教师
const deleteTeacher = async (teacherId: string) => {
  try {
    await ElMessageBox.confirm('确认删除该教师吗？删除后无法恢复。', '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await removeTeacher(teacherId)
    ElMessage.success('删除成功！')
    getTeacherList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败！')
    }
  }
}

// 导入对话框引用
const importDialogRef = ref<InstanceType<typeof ImportDialog> | null>(null)

// Excel 中文列名 → 后端的英文字段（密码统一设为 123456）
const transformTeachers = (raw: any[]) =>
  raw.map((row: any) => ({
    teacherId: row['教师工号'] || '',
    teacherName: row['姓名'] || '',
    department: row['所属部门'] || '',
    phone: row['联系方式'] || ''
  }))

// 导出教师数据
const exportTeacherData = () => {
  const exportData = teacherList.value.map((item) => ({
    教师工号: item.teacherId,
    姓名: item.teacherName,
    所属部门: item.department,
    联系方式: item.phone
  }))
  exportToExcel(exportData, '教师信息', '教师信息表.xlsx')
}
</script>
<template>
  <div>
    <!-- 标题+按钮 -->
    <div
      style="
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
      "
    >
      <h2 style="color: var(--text-primary)">教师信息管理</h2>
      <div>
        <el-button type="primary" @click="openAddTeacherDialog"
          >新增教师</el-button
        >
        <el-button type="success" style="margin-left: 10px" @click="importDialogRef?.open()">导入数据</el-button>
        <el-button type="info" style="margin-left: 10px" @click="exportTeacherData">导出数据</el-button>
      </div>
    </div>
    <!-- 搜索表单 -->
    <el-form :model="searchForm" inline style="margin-bottom: 20px">
      <el-form-item label="教师姓名">
        <el-input
          v-model="searchForm.teacherName"
          placeholder="请输入姓名"
        ></el-input>
      </el-form-item>
      <el-form-item label="所属部门">
        <el-select
          v-model="searchForm.department"
          placeholder="请选择部门"
          style="width: 150px"
        >
          <el-option label="计算机学院" value="计算机学院"></el-option>
          <el-option label="电子信息学院" value="电子信息学院"></el-option>
          <el-option label="自动化学院" value="自动化学院"></el-option>
          <el-option label="电子工程学院" value="电子工程学院"></el-option>
          <el-option label="数学学院" value="数学学院"></el-option>
          <el-option label="外语学院" value="外语学院"></el-option>
        </el-select>
      </el-form-item>
      <el-button type="primary" @click="getTeacherList">查询</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </el-form>
    <!-- 教师列表表格 -->
    <el-table :data="teacherList" border style="width: 100%" v-loading="loading">
      <el-table-column prop="teacherId" label="教师工号" />
      <el-table-column prop="teacherName" label="姓名" />
      <el-table-column prop="department" label="所属部门" />
      <el-table-column prop="phone" label="联系方式" />
      <el-table-column prop="operation" label="操作" width="180">
        <template #default="scope">
          <el-button type="text" @click="openEditTeacherDialog(scope.row)"
            >编辑</el-button
          >
          <el-button
            type="text"
            style="color: #ef4444"
            @click="deleteTeacher(scope.row.teacherId)"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="currentPage"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      style="margin-top: 20px; text-align: right"
    ></el-pagination>

    <!-- 新增/编辑教师弹窗 -->
    <el-dialog
      :title="isEdit ? '编辑教师' : '新增教师'"
      v-model="teacherDialogVisible"
      width="500px"
    >
      <el-form
        :model="teacherForm"
        :rules="currentRules"
        ref="teacherFormRef"
        label-width="100px"
      >
        <el-form-item label="教师工号" prop="teacherId">
          <el-input
            v-model="teacherForm.teacherId"
            :disabled="isEdit"
            placeholder="请输入教师工号"
          ></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="teacherName">
          <el-input
            v-model="teacherForm.teacherName"
            placeholder="请输入姓名"
          ></el-input>
        </el-form-item>
        <el-form-item label="所属部门" prop="department">
          <el-select v-model="teacherForm.department" placeholder="请选择部门">
            <el-option label="计算机学院" value="计算机学院"></el-option>
            <el-option label="电子信息学院" value="电子信息学院"></el-option>
            <el-option label="自动化学院" value="自动化学院"></el-option>
            <el-option label="电子工程学院" value="电子工程学院"></el-option>
            <el-option label="数学学院" value="数学学院"></el-option>
            <el-option label="外语学院" value="外语学院"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="联系方式" prop="phone">
          <el-input
            v-model="teacherForm.phone"
            placeholder="请输入手机号"
          ></el-input>
        </el-form-item>
        <el-form-item label="初始密码" prop="password" v-if="!isEdit">
          <el-input
            v-model="teacherForm.password"
            type="password"
            placeholder="请输入初始密码（默认123456）"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="teacherDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTeacherForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 导入弹窗 -->
    <ImportDialog ref="importDialogRef" title="教师数据" :import-api="importTeachers" :transform="transformTeachers" @success="getTeacherList" />
  </div>
</template>
