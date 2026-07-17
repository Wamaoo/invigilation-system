<!-- 项目根组件 -->
<script setup lang="ts">
// 初始化用户状态（防止刷新页面登录信息丢失）
import { useUserStore } from '@/stores/user'
useUserStore().initUser()
// 初始化主题
import { useThemeStore } from '@/stores/theme'
useThemeStore()
</script>

<template>
  <!-- 显示当前跳转的页面 -->
  <router-view />
</template>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
</style>
