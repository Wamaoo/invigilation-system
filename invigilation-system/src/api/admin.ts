import request from '@/utils/request'

// ====================================
// 数据概览
// ====================================
export interface OverviewData {
  teacherCount: number
  examCount: number
  invigilationCount: number
  conflictCount: number
}

export interface RecentInvigilationItem {
  examName: string
  classroom: string
  teacherNames: string
  examTime: string
  status: string
}

export const getOverview = () =>
  request.get<OverviewData>('/api/admin/dashboard/overview')

// ====================================
// 仪表盘图表数据
// ====================================
export interface ChartData {
  statusData: { name: string; value: number }[]
  monthLabels: string[]
  monthValues: number[]
  workloadTop10: { teacherName: string; count: number }[]
  departmentData: { department: string; count: number }[]
}

export const fetchChartsData = () =>
  request.get<ChartData>('/api/admin/dashboard/charts')

export const getRecentInvigilation = (limit = 3) =>
  request.get<RecentInvigilationItem[]>(
    '/api/admin/dashboard/recent-invigilation',
    {
      params: { limit }
    }
  )

// ====================================
// 考试科目管理
// ====================================
export interface ExamForm {
  subjectId: string
  examName: string
  grade: string
  major: string
  examType: string
  semesterId?: string
}

export interface ExamSearchParams {
  examName?: string
  major?: string
  examType?: string
  semesterId?: string
  page?: number
  size?: number
}

export const getExamList = (params?: ExamSearchParams) =>
  request.get<{ list: ExamForm[]; total: number }>('/api/admin/exam/list', { params })

/** 获取下一个可用的科目ID（自动生成） */
export const getExamNextId = () =>
  request.get<string>('/api/admin/exam/next-id')

export const addExam = (data: ExamForm) =>
  request.post('/api/admin/exam/add', data)

export const editExam = (data: ExamForm) =>
  request.put('/api/admin/exam/edit', data)

export const deleteExam = (subjectId: string) =>
  request.delete(`/api/admin/exam/delete/${subjectId}`)

export const importExams = (data: any[]) =>
  request.post<string>('/api/admin/exam/import', data)

// ====================================
// 教师管理
// ====================================
export interface TeacherForm {
  teacherId: string
  teacherName: string
  department: string
  phone: string
  password?: string
}

export interface TeacherSearchParams {
  teacherName?: string
  department?: string
  page?: number
  size?: number
}

export const getTeacherList = (params?: TeacherSearchParams) =>
  request.get<{ list: TeacherForm[]; total: number }>('/api/admin/teacher/list', { params })

export const addTeacher = (data: TeacherForm) =>
  request.post('/api/admin/teacher/add', data)

export const editTeacher = (data: TeacherForm) =>
  request.put('/api/admin/teacher/edit', data)

export const deleteTeacher = (teacherId: string) =>
  request.delete(`/api/admin/teacher/delete/${teacherId}`)

export const getTeacherById = (teacherId: string) =>
  request.get('/api/admin/teacher/get-by-id', { params: { teacherId } })

export const importTeachers = (data: any[]) =>
  request.post<string>('/api/admin/teacher/import', data)

// ====================================
// 系统用户
// ====================================
export interface AddUserParams {
  username: string
  password: string
  role: string
}

export const addUser = (data: AddUserParams) =>
  request.post('/api/admin/user/add', data)

// ====================================
// 监考安排管理
// ====================================
export interface InvigilationForm {
  arrangeId: string
  subjectId?: string
  examName: string
  classroom: string
  examDate: string
  startTime: string
  endTime: string
  requiredTeachers?: number
  teacherIds: string
  teacherNames: string
  /** 后端计算字段: examDate + " " + startTime + "-" + endTime */
  examTime?: string
  examDuration: string
  semesterId?: string
  status: string
  remark?: string
}

export interface InvigilationSearchParams {
  examName?: string
  classroom?: string
  startDate?: string
  endDate?: string
  semesterId?: string
  page?: number
  size?: number
}

export const getInvigilationList = (params?: InvigilationSearchParams) =>
  request.get<{ list: InvigilationForm[]; total: number }>('/api/admin/invigilation/list', { params })

/** 获取下一个可用的安排ID（自动生成） */
export const getInvigilationNextId = () =>
  request.get<string>('/api/admin/invigilation/next-id')

export const addInvigilation = (data: InvigilationForm) =>
  request.post('/api/admin/invigilation/add', data)

export const editInvigilation = (data: InvigilationForm) =>
  request.put('/api/admin/invigilation/edit', data)

export const deleteInvigilation = (arrangeId: string) =>
  request.delete(`/api/admin/invigilation/delete/${arrangeId}`)

export const importInvigilation = (data: any[]) =>
  request.post<string>('/api/admin/invigilation/import', data)

export const updateInvigilationStatus = (data: {
  arrangeId: string
  status: string
}) => request.post('/api/admin/invigilation/update-status', data)

// ====================================
// 冲突检测
// ====================================
export interface ConflictInfo {
  type: string       // "teacher" | "classroom"
  detail: string
  conflictArrangeId: string
  teacherId?: string
  teacherName?: string
}

export const checkInvigilationConflicts = (params: {
  examDate: string
  startTime: string
  endTime: string
  classroom: string
  teacherIds: string
  excludeArrangeId?: string
}) => request.get<ConflictInfo[]>('/api/admin/invigilation/check-conflicts', { params })

// ====================================
// 自动排课推荐
// ====================================
export const autoSuggestTeachers = (params: {
  examDate: string
  startTime: string
  endTime: string
  requiredCount: number
  excludeArrangeId?: string
  subjectId?: string
}) => request.get<string[]>('/api/admin/invigilation/auto-suggest', { params })

// ====================================
// 冲突申请管理
// ====================================
export interface ConflictApplyItem {
  id: number
  teacherId: string
  teacherName: string
  arrangeId: string
  reason: string
  fileUrl?: string
  createTime: string
  status: number // 0-待审核 1-已同意 2-已拒绝
}

export const getConflictApplyList = (status: number) =>
  request.get<ConflictApplyItem[]>('/api/admin/conflict/apply/list', {
    params: { status }
  })

// ====================================
// 管理员密码修改
// ====================================
export const updateAdminPassword = (data: { username: string; oldPassword: string; newPassword: string }) =>
  request.post('/api/admin/update-password', data)

export const handleConflictApply = (
  id: number,
  status: number,
  arrangeId?: string,
  teacherId?: string,
  teacherName?: string
) =>
  request.put('/api/admin/conflict/handle', null, {
    params: { id, status, arrangeId, teacherId, teacherName }
  })

// ====================================
// 学期管理
// ====================================
export interface SemesterItem {
  id: number
  name: string
  startDate: string
  endDate: string
  isCurrent: number
}

export const getSemesterList = () =>
  request.get<SemesterItem[]>('/api/admin/semester/list')

export const getCurrentSemester = () =>
  request.get<SemesterItem>('/api/admin/semester/current')

export const addSemester = (data: SemesterItem) =>
  request.post('/api/admin/semester/add', data)

export const editSemester = (data: SemesterItem) =>
  request.put('/api/admin/semester/edit', data)

export const deleteSemester = (id: number) =>
  request.delete(`/api/admin/semester/delete/${id}`)

export const setCurrentSemester = (id: number) =>
  request.put(`/api/admin/semester/set-current/${id}`)
