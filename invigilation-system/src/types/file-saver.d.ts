//文件导出库(导出Excel)
declare module 'file-saver' {
  export function saveAs(blob: Blob, filename: string): void
}
