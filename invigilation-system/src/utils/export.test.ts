import { describe, it, expect, vi, beforeEach } from 'vitest'

// Mock 外部依赖
vi.mock('element-plus', () => ({
  ElMessage: { warning: vi.fn(), success: vi.fn() },
}))
vi.mock('file-saver', () => ({
  saveAs: vi.fn(),
}))

const { exportToExcel } = await import('./export')
const { ElMessage } = await import('element-plus')
const { saveAs } = await import('file-saver')

beforeEach(() => {
  vi.clearAllMocks()
})

describe('exportToExcel', () => {
  it('空数据时弹出警告，不生成文件', () => {
    exportToExcel([], '测试', '空.xlsx')
    expect(ElMessage.warning).toHaveBeenCalledWith('没有数据可导出')
    expect(saveAs).not.toHaveBeenCalled()
  })

  it('有数据时生成 Excel 文件', () => {
    exportToExcel([{ name: '张三' }], '测试', '人员.xlsx')
    expect(ElMessage.success).toHaveBeenCalledWith('导出成功！')
    expect(saveAs).toHaveBeenCalled()
  })
})
