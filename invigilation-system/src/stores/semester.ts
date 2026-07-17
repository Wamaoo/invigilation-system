import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCurrentSemester, getSemesterList, type SemesterItem } from '@/api/admin'

export const useSemesterStore = defineStore('semester', () => {
  const current = ref<SemesterItem | null>(null)
  const list = ref<SemesterItem[]>([])

  async function fetchCurrent() {
    try {
      current.value = await getCurrentSemester()
    } catch { /* ignore */ }
  }

  async function fetchList() {
    try {
      list.value = await getSemesterList()
    } catch { /* ignore */ }
  }

  return { current, list, fetchCurrent, fetchList }
})
