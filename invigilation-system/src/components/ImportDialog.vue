<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import * as XLSX from 'xlsx'

const props = defineProps<{
  title: string
  /** 导入 API 函数，接收已映射好英文字段的数据 */
  importApi: (data: any[]) => Promise<any>
  /** 将 Excel 解析出的中文列名数据映射为后端需要的英文字段格式 */
  transform: (rawData: any[]) => any[]
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const dialogVisible = ref(false)
const previewData = ref<any[]>([])
const previewColumns = ref<string[]>([])
const fileName = ref('')
const importing = ref(false)

const open = () => {
  previewData.value = []
  previewColumns.value = []
  fileName.value = ''
  dialogVisible.value = true
}

const handleFileChange = (file: File) => {
  fileName.value = file.name
  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const data = e.target?.result as ArrayBuffer
      const workbook = XLSX.read(data, { type: 'array' })
      const firstSheet = workbook.Sheets[workbook.SheetNames[0]!]!
      const json = XLSX.utils.sheet_to_json(firstSheet, { defval: '' })
      if (!json || json.length === 0) {
        ElMessage.warning('文件为空或格式不正确')
        return
      }
      previewColumns.value = Object.keys(json[0] as object)
      previewData.value = json as any[]
      ElMessage.success(`成功解析 ${json.length} 条数据`)
    } catch {
      ElMessage.error('文件解析失败，请检查格式')
    }
  }
  reader.readAsArrayBuffer(file)
  return false
}

const confirmImport = async () => {
  if (previewData.value.length === 0) {
    ElMessage.warning('请先选择文件')
    return
  }
  importing.value = true
  try {
    const mappedData = props.transform(previewData.value)
    const result = await props.importApi(mappedData)
    ElMessage.success(
      typeof result === 'string'
        ? result
        : `成功导入 ${mappedData.length} 条数据`
    )
    dialogVisible.value = false
    emit('success')
  } catch (error: any) {
    ElMessage.error(error?.msg || '导入失败，请检查数据格式')
  } finally {
    importing.value = false
  }
}

defineExpose({ open })
</script>

<template>
  <el-dialog
    :title="'导入' + title"
    v-model="dialogVisible"
    width="800px"
    :close-on-click-modal="false"
  >
    <div style="margin-bottom: 16px">
      <p
        style="
          color: var(--text-secondary);
          margin-bottom: 8px;
          font-size: 13px;
        "
      >
        请先导出数据模板，在 Excel 中填写内容后导入。仅支持 .xlsx 格式。
      </p>
      <el-upload
        drag
        accept=".xlsx,.xls"
        :auto-upload="false"
        :show-file-list="false"
        :on-change="(f: any) => handleFileChange(f.raw!)"
        style="text-align: center"
      >
        <el-icon style="font-size: 48px; color: #409eff"
          ><UploadFilled
        /></el-icon>
        <div style="margin-top: 8px; color: var(--text-primary)">
          {{ fileName || '点击或拖拽 Excel 文件到此区域' }}
        </div>
      </el-upload>
    </div>

    <div v-if="previewData.length > 0">
      <p
        style="font-weight: 600; color: var(--text-primary); margin-bottom: 8px"
      >
        数据预览（共 {{ previewData.length }} 条）
      </p>
      <el-table
        :data="previewData.slice(0, 20)"
        border
        max-height="350px"
        style="width: 100%"
        size="small"
      >
        <el-table-column
          v-for="col in previewColumns"
          :key="col"
          :prop="col"
          :label="col"
          min-width="100"
        >
          <template #default="scope">
            {{ scope.row[col] }}
          </template>
        </el-table-column>
      </el-table>
      <p
        v-if="previewData.length > 20"
        style="color: var(--text-secondary); font-size: 12px; margin-top: 4px"
      >
        仅显示前 20 条，实际导入全部 {{ previewData.length }} 条
      </p>
    </div>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button
        type="primary"
        :loading="importing"
        :disabled="previewData.length === 0"
        @click="confirmImport"
      >
        确认导入
      </el-button>
    </template>
  </el-dialog>
</template>
