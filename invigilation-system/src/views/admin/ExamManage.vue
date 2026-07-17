<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import type { ExamForm } from '@/api/admin'
import {
  getExamList as fetchExamList,
  addExam,
  editExam,
  deleteExam as removeExam,
  getExamNextId,
  importExams
} from '@/api/admin'
import { exportToExcel } from '@/utils/export'
import ImportDialog from '@/components/ImportDialog.vue'
import { useSemesterStore } from '@/stores/semester'

const semesterStore = useSemesterStore()

// 年级选项
const gradeOptions = ['大一', '大二', '大三', '大四']

// 搜索表单
const searchForm = ref({
  examName: '',
  major: '',
  examType: ''
})

// 考试列表数据
const examList = ref<ExamForm[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 弹窗相关
const examDialogVisible = ref(false)
const isEdit = ref(false)
const examFormRef = ref<FormInstance | null>(null)
const examForm = ref({
  subjectId: '',
  examName: '',
  grade: '',
  major: '',
  examType: '',
  semesterId: ''
})

// 表单校验规则
const examRules = ref({
  subjectId: [{ required: true, message: '科目ID', trigger: 'blur' }],
  examName: [{ required: true, message: '请输入科目名称', trigger: 'blur' }],
  grade: [{ required: true, message: '请选择考试年级', trigger: 'change' }],
  major: [{ required: true, message: '请选择考试专业', trigger: 'change' }],
  examType: [{ required: true, message: '请选择考试类型', trigger: 'change' }]
})


onMounted(() => {
  getExamList()
})

const getExamList = async () => {
  loading.value = true
  try {
    const res = await fetchExamList({
      ...searchForm.value,
      semesterId: semesterStore.current?.name ?? '',
      page: currentPage.value,
      size: pageSize.value
    })
    examList.value = res.list
    total.value = res.total
  } catch (error) {
    ElMessage.error('获取考试列表失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.value = { examName: '', major: '', examType: '' }
  currentPage.value = 1
  getExamList()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  getExamList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  getExamList()
}

// 打开新增弹窗 — 自动获取下一个ID
const openAddExamDialog = async () => {
  isEdit.value = false
  examForm.value = {
    subjectId: '',
    examName: '',
    grade: '',
    major: '',
    examType: '',
    semesterId: semesterStore.current?.name ?? ''
  }
  try {
    const id = await getExamNextId()
    examForm.value.subjectId = id
  } catch {
    ElMessage.warning('获取科目ID失败，请手动输入')
  }
  examDialogVisible.value = true
}

const openEditExamDialog = (row: ExamForm) => {
  isEdit.value = true
  examForm.value = { ...row, semesterId: row.semesterId ?? '' }
  examDialogVisible.value = true
}

const submitExamForm = async () => {
  try {
    await examFormRef.value?.validate()
    if (isEdit.value) {
      await editExam(examForm.value)
      ElMessage.success('编辑成功！')
    } else {
      await addExam(examForm.value)
      ElMessage.success('新增成功！')
    }
    examDialogVisible.value = false
    getExamList()
  } catch (error) {
    ElMessage.error('提交失败，请检查表单！')
  }
}

const deleteExam = async (subjectId: string) => {
  try {
    await ElMessageBox.confirm('确认删除该考试科目吗？删除后无法恢复。', '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await removeExam(subjectId)
    ElMessage.success('删除成功！')
    getExamList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败！')
    }
  }
}

// 导入对话框
const importDialogRef = ref<InstanceType<typeof ImportDialog> | null>(null)

const transformExams = (raw: any[]) =>
  raw.map((row: any) => ({
    subjectId: row['科目ID'] || '',
    examName: row['科目名称'] || '',
    grade: row['考试年级'] || '',
    major: row['考试专业'] || '',
    examType: row['考试类型'] || ''
  }))

// 导出考试数据
const exportExamData = () => {
  const exportData = examList.value.map((item) => ({
    科目ID: item.subjectId,
    科目名称: item.examName,
    考试年级: item.grade,
    考试专业: item.major,
    考试类型: item.examType
  }))
  exportToExcel(exportData, '考试科目信息', '考试科目表.xlsx')
}
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px">
      <h2 style="color: var(--text-primary)">考试科目管理</h2>
      <div>
        <el-button type="primary" @click="openAddExamDialog">新增考试</el-button>
        <el-button type="success" style="margin-left: 10px" @click="importDialogRef?.open()">导入数据</el-button>
        <el-button type="info" style="margin-left: 10px" @click="exportExamData">导出数据</el-button>
      </div>
    </div>

    <el-form :model="searchForm" inline style="margin-bottom: 20px">
      <el-form-item label="科目名称">
        <el-input v-model="searchForm.examName" placeholder="请输入科目名称"></el-input>
      </el-form-item>
      <el-form-item label="考试专业">
        <el-select v-model="searchForm.major" placeholder="请选择专业" style="width: 180px" clearable>
          <el-option label="计算机科学与技术" value="计算机科学与技术"></el-option>
          <el-option label="电子信息工程" value="电子信息工程"></el-option>
          <el-option label="电子工程" value="电子工程"></el-option>
          <el-option label="数学" value="数学"></el-option>
          <el-option label="外语" value="外语"></el-option>
          <el-option label="所有专业" value="所有专业"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="考试类型">
        <el-select v-model="searchForm.examType" placeholder="请选择类型" style="width: 120px" clearable>
          <el-option label="期末考试" value="期末考试"></el-option>
          <el-option label="期中考试" value="期中考试"></el-option>
          <el-option label="模拟考试" value="模拟考试"></el-option>
        </el-select>
      </el-form-item>
      <el-button type="primary" @click="getExamList">查询</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </el-form>

    <el-table :data="examList" border style="width: 100%" v-loading="loading">
      <el-table-column prop="subjectId" label="科目ID" />
      <el-table-column prop="examName" label="科目名称" />
      <el-table-column prop="grade" label="考试年级" />
      <el-table-column prop="major" label="考试专业" />
      <el-table-column prop="examType" label="考试类型" />
      <el-table-column prop="operation" label="操作" width="180">
        <template #default="scope">
          <el-button type="text" @click="openEditExamDialog(scope.row)">编辑</el-button>
          <el-button type="text" style="color: #ef4444" @click="deleteExam(scope.row.subjectId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

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
  </div>

  <!-- 新增/编辑弹窗 -->
  <el-dialog :title="isEdit ? '编辑考试' : '新增考试'" v-model="examDialogVisible" width="500px">
    <el-form :model="examForm" :rules="examRules" ref="examFormRef" label-width="100px">
      <el-form-item label="科目ID" prop="subjectId">
        <el-input v-model="examForm.subjectId" :disabled="true" placeholder="自动生成"></el-input>
      </el-form-item>
      <el-form-item label="科目名称" prop="examName">
        <el-input v-model="examForm.examName" placeholder="请输入科目名称"></el-input>
      </el-form-item>
      <el-form-item label="考试年级" prop="grade">
        <el-select v-model="examForm.grade" placeholder="请选择年级" style="width: 100%">
          <el-option v-for="g in gradeOptions" :key="g" :label="g" :value="g"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="考试专业" prop="major">
        <el-select v-model="examForm.major" placeholder="请选择专业" style="width: 100%">
          <el-option label="计算机科学与技术" value="计算机科学与技术"></el-option>
          <el-option label="电子信息工程" value="电子信息工程"></el-option>
          <el-option label="电子工程" value="电子工程"></el-option>
          <el-option label="数学" value="数学"></el-option>
          <el-option label="外语" value="外语"></el-option>
          <el-option label="所有专业" value="所有专业"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="考试类型" prop="examType">
        <el-select v-model="examForm.examType" placeholder="请选择类型" style="width: 100%">
          <el-option label="期末考试" value="期末考试"></el-option>
          <el-option label="期中考试" value="期中考试"></el-option>
          <el-option label="模拟考试" value="模拟考试"></el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="examDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitExamForm">确定</el-button>
    </template>
  </el-dialog>

  <ImportDialog ref="importDialogRef" title="考试数据" :import-api="importExams" :transform="transformExams" @success="getExamList" />
</template>
