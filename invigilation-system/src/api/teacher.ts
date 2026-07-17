import request from '@/utils/request'

// ====================================
// 登录
// ====================================
export interface LoginParams {
  username: string
  password: string
  role: 'admin' | 'teacher'
}

export interface LoginResult {
  username: string
  role: string
  token: string
}

export const login = (data: LoginParams) =>
  request.post<LoginResult>('/api/login', data)

// ====================================
// 教师信息
// ====================================
export interface TeacherInfo {
  teacherId: string
  teacherName: string
  department?: string
  phone?: string
}

export const getTeacherInfo = (teacherId: string) =>
  request.get('/api/admin/teacher/get-by-id', { params: { teacherId } })

// ====================================
// 我的监考
// ====================================
export interface MyInvigilationItem {
  arrangeId: string
  examName: string
  classroom: string
  examTime: string
  examDate: string
  startTime: string
  endTime: string
  examDuration: string
  remark: string
  teacherNames: string
  myName: string
}

export const getMyInvigilation = (teacherId: string) =>
  request.get<MyInvigilationItem[]>('/api/teacher/invigilation/my', {
    params: { teacherId }
  })

// ====================================
// 监考查询
// ====================================
export interface InvigilationQueryItem {
  arrangeId: string
  examName: string
  classroom: string
  teacherNames: string
  examTime: string
  examDuration: string
  status: string
}

export interface InvigilationQueryResult {
  list: InvigilationQueryItem[]
  total: number
}

export interface InvigilationQueryParams {
  examName?: string
  classroom?: string
  startDate?: string
  endDate?: string
  page?: number
  size?: number
}

export const queryInvigilation = (params?: InvigilationQueryParams) =>
  request.get<InvigilationQueryResult>('/api/teacher/invigilation/list', {
    params
  })

// ====================================
// 冲突申请
// ====================================
export interface ConflictApplyForm {
  teacherId: string
  teacherName: string
  arrangeId: string
  reason: string
}

export const submitConflictApply = (data: ConflictApplyForm) =>
  request.post('/api/teacher/conflict/apply', data)

// ====================================
// 个人信息 & 密码修改
// ====================================
export interface TeacherProfile {
  teacherId: string
  teacherName: string
  department: string
  phone: string
}

export const getTeacherProfile = (teacherId: string) =>
  request.get<TeacherProfile>('/api/teacher/info/get-by-id', { params: { teacherId } })
export interface UpdateProfileParams {
  teacherId: string
  teacherName: string
  department: string
  phone: string
}

export const updateTeacherProfile = (data: UpdateProfileParams) =>
  request.put('/api/teacher/info/update', data)

export interface UpdatePasswordParams {
  username: string
  oldPassword: string
  newPassword: string
}

export const updatePassword = (data: UpdatePasswordParams) =>
  request.post('/api/teacher/info/update-password', data)
