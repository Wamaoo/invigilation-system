// 路由配置（页面跳转规则）
// 相当于 “导航地图”，定义哪个路径对应哪个页面：
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import Login from '@/views/Login.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import TeacherLayout from '@/layouts/TeacherLayout.vue'

// 管理员子界面(侧边栏点击后显示的内容)
const adminRoutes: RouteRecordRaw[] = [
  { path: '/admin', redirect: '/admin/dashboard' },
  {
    path: 'dashboard',
    name: 'Dashboard',
    component: () => import('@/views/admin/Dashboard.vue')
  },
  {
    path: 'teacher-manage',
    name: 'TeacherManage',
    component: () => import('@/views/admin/TeacherManage.vue')
  },
  {
    path: 'exam-manage',
    name: 'ExamManage',
    component: () => import('@/views/admin/ExamManage.vue')
  },
  {
    path: 'invigilation-config',
    name: 'InvigilationConfig',
    component: () => import('@/views/admin/InvigilationConfig.vue')
  },
  {
    path: 'semester-manage',
    name: 'SemesterManage',
    component: () => import('@/views/admin/SemesterManage.vue')
  }
]

// 教师子界面
const teacherRoutes: RouteRecordRaw[] = [
  { path: '/teacher', redirect: '/teacher/my-invigilation' },
  {
    path: 'my-invigilation',
    name: 'MyInvigilation',
    component: () => import('@/views/teacher/MyInvigilation.vue')
  },
  {
    path: 'invigilation-query',
    name: 'InvigilationQuery',
    component: () => import('@/views/teacher/InvigilationQuery.vue')
  },
  {
    path: 'profile',
    name: 'Profile',
    component: () => import('@/views/teacher/Profile.vue')
  }
]

// 总路由规则
const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'Login', component: Login },
  {
    path: '/admin',
    name: 'Admin',
    component: AdminLayout,
    children: adminRoutes,
    meta: { requiresAuth: true, role: 'admin' } //需要登录,且角色是admin
  },
  {
    path: '/teacher',
    name: 'Teacher',
    component: TeacherLayout,
    children: teacherRoutes,
    meta: { requiresAuth: true, role: 'teacher' }
  },
  { path: '/:pathMatch(.*)*', redirect: '/login' } //找不到的路径就跳登录页
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})
export default router
