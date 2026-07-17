import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const STORAGE_KEY = 'theme-dark'

  const isDark = ref(localStorage.getItem(STORAGE_KEY) === 'true')

  function applyTheme(val: boolean) {
    if (val) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  // 初始化
  applyTheme(isDark.value)

  // 持久化 + 自动同步
  watch(isDark, (val) => {
    localStorage.setItem(STORAGE_KEY, String(val))
    applyTheme(val)
  })

  function toggle() {
    isDark.value = !isDark.value
  }

  return { isDark, toggle }
})
