<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import type { InvigilationForm, ExamForm, TeacherForm } from '@/api/admin'
import {
  getInvigilationList,
  addInvigilation,
  editInvigilation,
  deleteInvigilation,
  getInvigilationNextId,
  getExamList,
  getTeacherList,
  importInvigilation,
  checkInvigilationConflicts,
  autoSuggestTeachers,
  type ConflictInfo
} from '@/api/admin'
import { exportToExcel } from '@/utils/export'
import ImportDialog from '@/components/ImportDialog.vue'
import { useSemesterStore } from '@/stores/semester'

const semesterStore = useSemesterStore()

// ====== 搜索表单 ======
const searchForm = ref({ examName: '', classroom: '', dateRange: null as [string, string] | null })

// ====== 监考安排列表 ======
const configList = ref<InvigilationForm[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const examOptions = ref<{ label: string; value: string }[]>([])
const teacherOptions = ref<{ label: string; value: string; name: string }[]>([])

// ====== 弹窗表单 ======
const configDialogVisible = ref(false)
const isEdit = ref(false)
const configFormRef = ref<FormInstance | null>(null)

const examDate = ref('')
const examStartTime = ref('')
const examEndTime = ref('')

const configForm = ref({
  arrangeId: '',
  subjectId: '',
  examName: '',
  classroom: '',
  teacherIds: '',
  teacherNames: '',
  examDate: '',
  startTime: '',
  endTime: '',
  examTime: '',
  examDuration: '',
  semesterId: '',
  requiredTeachers: 2,
  status: '已安排',
  remark: ''
})

// 选中的教师ID列表
const selectedTeacherIds = ref<string[]>([])

const configRules = ref({
  arrangeId: [{ required: true, message: '安排ID', trigger: 'blur' }],
  examName: [{ required: true, message: '请选择考试科目', trigger: 'change' }],
  classroom: [{ required: true, message: '请输入考场', trigger: 'blur' }],
  teacherIds: [{ required: true, message: '请选择监考教师', trigger: 'change' }],
  examDate: [{ required: true, message: '请选择考试日期' }],
  startTime: [{ required: true, message: '请选择开始时间' }],
  endTime: [{ required: true, message: '请选择结束时间' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
})

const selectedTeacherNames = computed(() => {
  return selectedTeacherIds.value
    .map((id) => {
      const t = teacherOptions.value.find((o) => o.value === id)
      return t ? t.name : ''
    })
    .filter(Boolean)
    .join(',')
})

const handleTeacherChange = (ids: string[]) => {
  selectedTeacherIds.value = ids
  configForm.value.teacherIds = ids.join(',')
  configForm.value.teacherNames = selectedTeacherNames.value
  autoCheckConflicts()
}

// ====== 自动计算考试时长 ======
const handleTimeChange = () => {
  if (examDate.value && examStartTime.value && examEndTime.value) {
    configForm.value.examDate = examDate.value
    configForm.value.startTime = examStartTime.value
    configForm.value.endTime = examEndTime.value
    configForm.value.examTime = `${examDate.value} ${examStartTime.value}-${examEndTime.value}`

    const [startH = 0, startM = 0] = examStartTime.value.split(':').map(Number)
    const [endH = 0, endM = 0] = examEndTime.value.split(':').map(Number)
    const diff = endH * 60 + endM - (startH * 60 + startM)
    if (diff > 0) {
      configForm.value.examDuration = `${diff}分钟`
    } else {
      configForm.value.examDuration = ''
      ElMessage.warning('结束时间必须晚于开始时间')
    }
  }
  autoCheckConflicts()
}

// ====== 获取列表 ======
onMounted(() => getConfigList())

const getConfigList = async () => {
  loading.value = true
  try {
    const params: any = {
      examName: searchForm.value.examName,
      classroom: searchForm.value.classroom,
      semesterId: semesterStore.current?.name ?? '',
      page: currentPage.value,
      size: pageSize.value
    }
    if (searchForm.value.dateRange) {
      params.startDate = searchForm.value.dateRange[0]
      params.endDate = searchForm.value.dateRange[1]
    }
    const res = await getInvigilationList(params)
    configList.value = res.list
    total.value = res.total
  } catch { ElMessage.error('获取监考安排列表失败') }
  finally { loading.value = false }
}

const resetSearch = () => {
  searchForm.value = { examName: '', classroom: '', dateRange: null }
  currentPage.value = 1
  getConfigList()
}

const handleSizeChange = (val: number) => { pageSize.value = val; getConfigList() }
const handleCurrentChange = (val: number) => { currentPage.value = val; getConfigList() }

// ====== 加载下拉数据 ======
const examDataMap = ref<Map<string, string>>(new Map()) // examName -> subjectId
const loadSelectorData = async () => {
  try {
    const res = await getExamList({ semesterId: semesterStore.current?.name ?? '' })
    examOptions.value = (res.list || []).map((e: ExamForm) => {
      examDataMap.value.set(e.examName, e.subjectId)
      return {
        label: `${e.subjectId} - ${e.examName}`,
        value: e.examName
      }
    })
  } catch { console.error('加载考试列表失败') }

  try {
    const res = await getTeacherList({})
    teacherOptions.value = (res.list || []).map((t: TeacherForm) => ({
      label: `${t.teacherId} - ${t.teacherName}`,
      value: t.teacherId,
      name: t.teacherName
    }))
  } catch { console.error('加载教师列表失败') }
}

// ====== 自动推荐教师 ======
const suggesting = ref(false)
const autoSuggest = async () => {
  if (!examDate.value || !examStartTime.value || !examEndTime.value) {
    ElMessage.warning('请先选择考试日期和时间')
    return
  }
  suggesting.value = true
  try {
    const ids = await autoSuggestTeachers({
      examDate: examDate.value,
      startTime: examStartTime.value,
      endTime: examEndTime.value,
      requiredCount: configForm.value.requiredTeachers || 2,
      excludeArrangeId: isEdit.value ? configForm.value.arrangeId : undefined,
      subjectId: examDataMap.value.get(configForm.value.examName) ?? undefined
    })
    if (ids.length > 0) {
      selectedTeacherIds.value = ids
      handleTeacherChange(ids)
      ElMessage.success(`已自动分配 ${ids.length} 位教师`)
    } else {
      ElMessage.warning('没有找到可用的教师')
    }
  } catch {
    ElMessage.error('自动分配失败')
  } finally {
    suggesting.value = false
  }
}

// ====== 打开新增弹窗 ======
const openAddConfigDialog = async () => {
  isEdit.value = false
  configForm.value = {
    arrangeId: '', subjectId: '', examName: '', classroom: '',
    teacherIds: '', teacherNames: '', examDate: '', startTime: '',
    endTime: '', examTime: '', examDuration: '', semesterId: semesterStore.current?.name ?? '',
    requiredTeachers: 2, status: '已安排', remark: ''
  }
  examDate.value = ''
  examStartTime.value = ''
  examEndTime.value = ''
  selectedTeacherIds.value = []

  await loadSelectorData()
  conflicts.value = []

  try {
    const id = await getInvigilationNextId()
    configForm.value.arrangeId = id
  } catch {
    ElMessage.warning('获取安排ID失败')
  }
  configDialogVisible.value = true
}

// ====== 打开编辑弹窗 ======
const openEditConfigDialog = async (row: InvigilationForm) => {
  isEdit.value = true
  configForm.value = { ...row, subjectId: row.subjectId ?? '', requiredTeachers: row.requiredTeachers ?? 2, examTime: row.examTime ?? '', semesterId: row.semesterId ?? '', remark: row.remark ?? '' }
  selectedTeacherIds.value = row.teacherIds ? row.teacherIds.split(',') : []

  if (row.examDate && row.startTime && row.endTime) {
    examDate.value = row.examDate
    examStartTime.value = row.startTime
    examEndTime.value = row.endTime
  } else if (row.examTime) {
    const match = row.examTime.match(/^(\S+)\s+(\S+)-(\S+)$/)
    if (match) {
      examDate.value = match[1] ?? ''
      examStartTime.value = match[2] ?? ''
      examEndTime.value = match[3] ?? ''
      configForm.value.examDate = examDate.value
      configForm.value.startTime = examStartTime.value
      configForm.value.endTime = examEndTime.value
    }
  }

  await loadSelectorData()
  conflicts.value = []
  configDialogVisible.value = true
}

// ====== 提交表单 ======
const submitConfigForm = async () => {
  try {
    await configFormRef.value?.validate()
    if (conflicts.value.length > 0) {
      await ElMessageBox.confirm('检测到冲突，确定要保存吗？', '冲突警告', {
        type: 'warning', confirmButtonText: '确定保存', cancelButtonText: '取消'
      })
    }
    if (isEdit.value) {
      await editInvigilation(configForm.value)
      ElMessage.success('编辑成功！')
    } else {
      await addInvigilation(configForm.value)
      ElMessage.success('新增成功！')
    }
    configDialogVisible.value = false
    conflicts.value = []
    getConfigList()
  } catch { ElMessage.error('提交失败，请检查表单！') }
}

// ====== 删除 ======
const deleteConfig = async (arrangeId: string) => {
  try {
    await ElMessageBox.confirm('确认删除该监考安排吗？删除后无法恢复。', '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await deleteInvigilation(arrangeId)
    ElMessage.success('删除成功！')
    getConfigList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败！')
    }
  }
}

// ====== 导入 ======
const importDialogRef = ref<InstanceType<typeof ImportDialog> | null>(null)

const transformInvigilation = (raw: any[]) =>
  raw.map((row: any) => ({
    arrangeId: row['安排ID'] || '',
    examName: row['考试科目'] || '',
    classroom: row['考场'] || '',
    teacherIds: row['监考教师ID'] || '',
    teacherNames: row['监考教师'] || '',
    examDate: row['考试日期'] || '',
    startTime: row['开始时间'] || '',
    endTime: row['结束时间'] || '',
    examDuration: row['考试时长'] || '',
    status: row['状态'] || '已安排',
    remark: row['备注'] || ''
  }))

// ====== 导出 ======
const exportConfigData = () => {
  const exportData = configList.value.map((item) => ({
    安排ID: item.arrangeId,
    考试科目: item.examName,
    考场: item.classroom,
    监考教师: item.teacherNames,
    考试日期: item.examDate || (item.examTime ? item.examTime.substring(0, 10) : ''),
    考试时间: item.examTime || '',
    考试时长: item.examDuration,
    状态: item.status,
    备注: item.remark || '无'
  }))
  exportToExcel(exportData, '监考安排信息', '监考安排表.xlsx')
}

const statusTagType = (status: string): string => {
  switch (status) {
    case '已安排': return 'success'
    case '冲突预警': return 'warning'
    case '已结束': return 'info'
    default: return ''
  }
}

// ====== 智能冲突检测 ======
const conflicts = ref<ConflictInfo[]>([])
const checkingConflicts = ref(false)
let conflictTimer: ReturnType<typeof setTimeout> | null = null

const autoCheckConflicts = () => {
  if (conflictTimer) clearTimeout(conflictTimer)
  if (!configForm.value.examDate || !configForm.value.startTime ||
      !configForm.value.endTime || !configForm.value.classroom) {
    conflicts.value = []
    return
  }
  conflictTimer = setTimeout(async () => {
    checkingConflicts.value = true
    try {
      const res = await checkInvigilationConflicts({
        examDate: configForm.value.examDate!,
        startTime: configForm.value.startTime!,
        endTime: configForm.value.endTime!,
        classroom: configForm.value.classroom,
        teacherIds: configForm.value.teacherIds,
        excludeArrangeId: isEdit.value ? configForm.value.arrangeId : undefined
      })
      conflicts.value = res
      if (res.length > 0) {
        configForm.value.status = '冲突预警'
      }
    } catch { /* 静默失败 */ }
    checkingConflicts.value = false
  }, 300)
}

watch(
  () => [configForm.value.examDate, configForm.value.startTime,
         configForm.value.endTime, configForm.value.classroom,
         configForm.value.teacherIds],
  () => { if (configDialogVisible.value) autoCheckConflicts() }
)
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px">
      <h2 style="color: var(--text-primary)">监考安排管理</h2>
      <div>
        <el-button type="primary" @click="openAddConfigDialog">新增安排</el-button>
        <el-button type="success" style="margin-left: 10px" @click="importDialogRef?.open()">导入数据</el-button>
        <el-button type="info" style="margin-left: 10px" @click="exportConfigData">导出数据</el-button>
      </div>
    </div>

    <el-form :model="searchForm" inline style="margin-bottom: 20px">
      <el-form-item label="考试科目">
        <el-input v-model="searchForm.examName" placeholder="请输入考试科目"></el-input>
      </el-form-item>
      <el-form-item label="考场">
        <el-input v-model="searchForm.classroom" placeholder="请输入考场"></el-input>
      </el-form-item>
      <el-form-item label="考试日期">
        <el-date-picker
          v-model="searchForm.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 240px"
        ></el-date-picker>
      </el-form-item>
      <el-button type="primary" @click="getConfigList">查询</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </el-form>

    <el-table :data="configList" border style="width: 100%" v-loading="loading">
      <el-table-column prop="arrangeId" label="安排ID" />
      <el-table-column prop="examName" label="考试科目" />
      <el-table-column prop="classroom" label="考场" />
      <el-table-column prop="teacherNames" label="监考教师" />
      <el-table-column prop="examTime" label="考试时间" />
      <el-table-column prop="examDuration" label="考试时长" />
      <el-table-column label="状态">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)" effect="plain">
            {{ scope.row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button type="text" @click="openEditConfigDialog(scope.row)">编辑</el-button>
          <el-button type="text" style="color: #ef4444" @click="deleteConfig(scope.row.arrangeId)">删除</el-button>
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

  <el-dialog
    :title="isEdit ? '编辑安排' : '新增安排'"
    v-model="configDialogVisible"
    width="750px"
    :close-on-click-modal="false"
  >
    <el-form :model="configForm" :rules="configRules" ref="configFormRef" label-width="120px">
      <el-form-item label="安排ID" prop="arrangeId">
        <el-input v-model="configForm.arrangeId" :disabled="true"></el-input>
      </el-form-item>

      <el-form-item label="考试科目" prop="examName">
        <el-select
          v-model="configForm.examName"
          placeholder="请选择考试科目"
          style="width: 100%"
          filterable
        >
          <el-option
            v-for="opt in examOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          ></el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="考场" prop="classroom">
        <el-input v-model="configForm.classroom" placeholder="请输入考场"></el-input>
      </el-form-item>

      <el-form-item label="所需人数" prop="requiredTeachers">
        <el-input-number v-model="configForm.requiredTeachers" :min="1" :max="10" />
        <el-button
          type="primary"
          size="small"
          style="margin-left: 12px"
          :loading="suggesting"
          @click="autoSuggest"
        >
          自动分配
        </el-button>
      </el-form-item>

      <el-form-item label="监考教师" prop="teacherIds">
        <el-select
          v-model="selectedTeacherIds"
          placeholder="请选择监考教师（可多选）"
          style="width: 100%"
          multiple
          filterable
          @change="handleTeacherChange"
        >
          <el-option
            v-for="t in teacherOptions"
            :key="t.value"
            :label="t.label"
            :value="t.value"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="监考教师姓名">
        <el-input v-model="configForm.teacherNames" :disabled="true" placeholder="选择教师后自动生成"></el-input>
      </el-form-item>

      <el-form-item label="考试日期" prop="examDate">
        <el-date-picker
          v-model="examDate"
          type="date"
          placeholder="选择日期"
          value-format="YYYY-MM-DD"
          style="width: 100%"
          @change="handleTimeChange"
        ></el-date-picker>
      </el-form-item>

      <el-form-item label="考试时间" prop="startTime">
        <el-space>
          <el-time-picker
            v-model="examStartTime"
            placeholder="开始时间"
            value-format="HH:mm"
            format="HH:mm"
            @change="handleTimeChange"
          />
          <span>至</span>
          <el-time-picker
            v-model="examEndTime"
            placeholder="结束时间"
            value-format="HH:mm"
            format="HH:mm"
            @change="handleTimeChange"
          />
        </el-space>
      </el-form-item>

      <el-form-item label="考试时长" prop="examDuration">
        <el-input v-model="configForm.examDuration" :disabled="true" placeholder="选择时间后自动计算"></el-input>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-select v-model="configForm.status" placeholder="请选择状态" style="width: 100%">
          <el-option label="已安排" value="已安排"></el-option>
          <el-option label="冲突预警" value="冲突预警"></el-option>
          <el-option label="已结束" value="已结束"></el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="备注" prop="remark">
        <el-input v-model="configForm.remark" type="textarea" placeholder="请输入备注"></el-input>
      </el-form-item>
    </el-form>

    <div v-if="checkingConflicts" style="margin-bottom: 16px; text-align: center; color: var(--text-secondary);">
      <el-icon class="is-loading"><Loading /></el-icon> 检测冲突中...
    </div>
    <div v-if="conflicts.length > 0" style="margin-bottom: 16px;">
      <el-alert
        v-for="(c, i) in conflicts" :key="i"
        :title="c.detail"
        type="warning"
        show-icon
        :closable="false"
        style="margin-bottom: 8px;"
      />
    </div>

    <template #footer>
      <el-button @click="configDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitConfigForm">确定</el-button>
    </template>
  </el-dialog>

  <ImportDialog ref="importDialogRef" title="监考安排数据" :import-api="importInvigilation" :transform="transformInvigilation" @success="getConfigList" />
</template>
