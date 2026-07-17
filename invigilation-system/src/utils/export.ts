import * as XLSX from 'xlsx'
import { saveAs } from 'file-saver'
import { ElMessage } from 'element-plus'

export function exportToExcel<T>(
  data: T[],
  sheetName: string,
  fileName: string
) {
  if (data.length === 0) {
    ElMessage.warning('没有数据可导出')
    return
  }
  const ws = XLSX.utils.json_to_sheet(data)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, sheetName)
  const wbout = XLSX.write(wb, { bookType: 'xlsx', type: 'array' })
  saveAs(new Blob([wbout], { type: 'application/octet-stream' }), fileName)
  ElMessage.success('导出成功！')
}
