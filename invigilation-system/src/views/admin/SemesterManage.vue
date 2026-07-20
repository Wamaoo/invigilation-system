<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addSemester, editSemester, deleteSemester, setCurrentSemester,
  type SemesterItem
} from '@/api/admin'
import { useSemesterStore } from '@/stores/semester'

const semesterStore = useSemesterStore()
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref<SemesterItem>({ id: 0, name: '', startDate: '', endDate: '', isCurrent: 0 })

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'name', label: '学期名称', width: 160 },
  { prop: 'startDate', label: '开始日期', width: 140 },
  { prop: 'endDate', label: '结束日期', width: 140 },
]

async function loadData() {
  loading.value = true
  await semesterStore.fetchList()
  loading.value = false
}

function openAdd() {
  isEdit.value = false
  form.value = { id: 0, name: '', startDate: '', endDate: '', isCurrent: 0 }
  dialogVisible.value = true
}

function openEdit(row: SemesterItem) {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleDelete(row: SemesterItem) {
  try {
    await ElMessageBox.confirm('确定删除该学期吗？', '提示', { type: 'warning' })
    await deleteSemester(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch { /* cancelled */ }
}

async function handleSubmit() {
  if (!form.value.name || !form.value.startDate || !form.value.endDate) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    if (isEdit.value) {
      await editSemester(form.value)
      ElMessage.success('修改成功')
    } else {
      await addSemester(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleSetCurrent(row: SemesterItem) {
  if (row.isCurrent === 1) return
  try {
    await setCurrentSemester(row.id)
    await semesterStore.fetchCurrent()
    ElMessage.success('已切换当前学期为 ' + row.name)
    loadData()
  } catch {
    ElMessage.error('切换失败')
  }
}

onMounted(() => loadData())
</script>

<template>
  <div>
    <div style="display: flex; justify-content: space-between; margin-bottom: 16px">
      <h2 style="margin: 0; color: var(--text-primary)">学期管理</h2>
      <el-button type="primary" @click="openAdd">新增学期</el-button>
    </div>

    <el-table :data="semesterStore.list" border v-loading="loading">
      <el-table-column v-for="col in columns" :key="col.prop" v-bind="col" />
      <el-table-column prop="isCurrent" label="是否当前" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isCurrent === 1 ? 'success' : 'info'" size="small">
            {{ row.isCurrent === 1 ? '当前学期' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button
            size="small"
            type="success"
            :disabled="row.isCurrent === 1"
            @click="handleSetCurrent(row)"
          >
            设为当前
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑学期' : '新增学期'" width="460px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="学期名称">
          <el-input v-model="form.name" placeholder="如 2026-2027-1" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="form.startDate" type="date" placeholder="选择开始日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="form.endDate" type="date" placeholder="选择结束日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
