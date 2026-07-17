// axios封装，统一处理接口请求
import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const instance = axios.create({
  baseURL: window.location.origin + '/invigilation', // 后端接口前缀
  timeout: 15000,
  headers: { 'Content-Type': 'application/json;charset=utf-8' } //请求头
})

// 请求拦截器（发请求前的操作）
instance.interceptors.request.use(
  (config) => {
    // 自动携带 JWT Token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = 'Bearer ' + token
    }
    return config
  },
  (error) => {
    ElMessage.error('请求发送失败：' + error.message)
    return Promise.reject(error)
  }
)

// 响应拦截器（接收后端返回后的操作）
instance.interceptors.response.use(
  (response) => {
    const res = response.data
    // 后端Result格式是 {code:200, msg:"成功", data:...}
    if (res.code !== 200) {
      ElMessage.error(res.msg || '操作失败')
      return Promise.reject(res)
    }
    // 返回后端Result里的data（这才是实际数据）
    return res.data
  },
  (error) => {
    // 401：token 过期或未登录，跳转到登录页
    if (error.response?.status === 401) {
      localStorage.removeItem('username')
      localStorage.removeItem('userRole')
      localStorage.removeItem('token')
      window.location.href = '/login'
      return Promise.reject(error)
    }
    ElMessage.error(
      '服务器错误：' + (error.response?.data?.msg || error.message)
    )
    return Promise.reject(error)
  }
)

// 覆写类型：拦截器已将响应解包为 T，而非 AxiosResponse<T>
const request = instance as {
  get<T = any>(url: string, config?: object): Promise<T>
  post<T = any>(url: string, data?: any, config?: object): Promise<T>
  put<T = any>(url: string, data?: any, config?: object): Promise<T>
  delete<T = any>(url: string, config?: object): Promise<T>
}

export default request
