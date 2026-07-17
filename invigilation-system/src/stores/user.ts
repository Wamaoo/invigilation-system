//pinia管理全局用户状态（登录、登出、用户名、角色），并通过LocalStorage持久化
// 相当于 "全局记事本"，存登录后的用户名、角色等，所有页面都能访问
import { defineStore } from 'pinia'
//定义用户状态类型
export interface UserState {
  username: string
  role: 'admin' | 'teacher' | ''
  token: string
  isLogin: boolean
}
// 创建名为user的Store
export const useUserStore = defineStore('user', {
  //初始化时直接从LocalStorage加载，无需等initUser
  state: (): UserState => ({
    username: localStorage.getItem('username') || '',
    role: (localStorage.getItem('userRole') as 'admin' | 'teacher' | '') || '',
    token: localStorage.getItem('token') || '',
    isLogin: !!localStorage.getItem('username') // 有用户名就是已登录
  }),
  actions: {
    //登录：更新状态 + 持久化到 localStorage
    login(username: string, role: 'admin' | 'teacher', token: string) {
      this.username = username
      this.role = role
      this.token = token
      this.isLogin = true
      localStorage.setItem('userRole', role) //存到浏览器本地存储
      localStorage.setItem('username', username)
      localStorage.setItem('token', token)
    },
    //退出：清空状态 + 清除 localStorage（只删除用户相关 key，保留主题等设置）
    logout() {
      this.username = ''
      this.role = ''
      this.token = ''
      this.isLogin = false
      localStorage.removeItem('username')
      localStorage.removeItem('userRole')
      localStorage.removeItem('token')
    },
    //初始化：刷新页面后恢复登录状态
    initUser() {
      const role = localStorage.getItem('userRole') as
        'admin' | 'teacher' | null
      const username = localStorage.getItem('username')
      const token = localStorage.getItem('token')
      if (role && username && token) {
        this.username = username
        this.role = role
        this.token = token
        this.isLogin = true
      }
    }
  }
})
