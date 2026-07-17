<!-- 教师端监考查询 -->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { queryInvigilation } from '@/api/teacher'
import { exportToExcel } from '@/utils/export'
import { formatDateToYMD } from '@/utils/invigilation'

// 定义监考查询结果类型（和后端字段对齐）
interface InvigilationItem {
  arrangeId: string
  examName: string
  classroom: string
  teacherNames: string
  examTime: string
  examDuration: string
  status: string
}

// 搜索表单
const searchForm = ref({
  examName: '',
  examTime: undefined as Date | undefined,
  classroom: ''
})

// 列表数据（和管理员端一致）
const invigilationList = ref<InvigilationItem[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 页面加载时获取所有监考安排
onMounted(() => {
  getInvigilationList()
})

// 调用教师端的"监考查询"接口
const getInvigilationList = async () => {
  loading.value = true
  try {
    const dateStr = searchForm.value.examTime
      ? formatDateToYMD(searchForm.value.examTime)
      : ''
    const res = await queryInvigilation({
      examName: searchForm.value.examName,
      classroom: searchForm.value.classroom,
      startDate: dateStr || undefined,
      endDate: dateStr || undefined,
      page: currentPage.value,
      size: pageSize.value
    })
    invigilationList.value = res.list
    total.value = res.total
  } catch (error) {
    ElMessage.error('获取监考列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const searchInvigilation = () => {
  currentPage.value = 1
  getInvigilationList()
}

// 重置
const resetSearch = () => {
  searchForm.value = { examName: '', examTime: undefined, classroom: '' }
  currentPage.value = 1
  getInvigilationList()
}

// 分页
const handleSizeChange = (val: number) => {
  pageSize.value = val
  getInvigilationList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  getInvigilationList()
}

// 导出数据
const exportConfigData = () => {
  const exportData = invigilationList.value.map((item) => ({
    安排ID: item.arrangeId,
    考试科目: item.examName,
    考场: item.classroom,
    监考教师: item.teacherNames,
    考试时间: item.examTime,
    考试时长: item.examDuration,
    状态: item.status
  }))
  exportToExcel(exportData, '监考安排信息', '监考安排表.xlsx')
}
</script>
<template>
  <div>
    <!-- 标题 + 仅保留导出按钮 -->
    <div
      style="
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
      "
    >
      <h2 style="color: var(--text-primary)">监考信息查询</h2>
      <el-button type="info" @click="exportConfigData">导出数据</el-button>
    </div>

    <!-- 搜索表单（保留和管理员端一致的查询条件） -->
    <el-form :model="searchForm" inline style="margin-bottom: 20px">
      <el-form-item label="考试科目">
        <el-input
          v-model="searchForm.examName"
          placeholder="请输入科目名称"
        ></el-input>
      </el-form-item>
      <el-form-item label="考试时间">
        <el-date-picker
          v-model="searchForm.examTime"
          type="date"
          placeholder="选择考试日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="考场">
        <el-input
          v-model="searchForm.classroom"
          placeholder="请输入考场号"
        ></el-input>
      </el-form-item>
      <el-button type="primary" @click="searchInvigilation">查询</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </el-form>

    <!-- 表格：显示所有监考安排（去掉操作列） -->
    <el-table :data="invigilationList" border style="width: 100%" v-loading="loading">
      <el-table-column prop="arrangeId" label="安排ID" />
      <el-table-column prop="examName" label="考试科目" />
      <el-table-column prop="classroom" label="考场" />
      <el-table-column prop="teacherNames" label="监考教师" />
      <el-table-column prop="examTime" label="考试时间" />
      <el-table-column prop="examDuration" label="考试时长" />
      <el-table-column label="状态">
  <template #default="scope">
    <el-tag :type="scope.row.status === '已结束' ? 'info' : scope.row.status === '冲突预警' ? 'warning' : 'success'" effect="plain">
      {{ scope.row.status }}
    </el-tag>
  </template>
</el-table-column>
      <el-table-column label="备注" align="center">
        <template #default="scope">{{ scope.row.remark || '-' }}</template>
      </el-table-column>
    </el-table>

    <!-- 分页（和管理员端一致） -->
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
</template>
