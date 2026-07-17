import { ref, onUnmounted } from 'vue'
import { ElNotification } from 'element-plus'
import { useUserStore } from '@/stores/user'

interface WsMessage {
  title: string
  message: string
  targetRole: string
  timestamp: number
}

export function useWebSocket() {
  const userStore = useUserStore()
  let ws: WebSocket | null = null
  let reconnectTimer: ReturnType<typeof setTimeout> | null = null
  const connected = ref(false)

  function connect() {
    if (!userStore.isLogin || !userStore.username) return

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = 'localhost:8088'
    const url = `${protocol}//${host}/invigilation/ws/notifications?role=${userStore.role}&username=${userStore.username}`

    try {
      ws = new WebSocket(url)

      ws.onopen = () => {
        connected.value = true
        console.log('[WS] 已连接')
      }

      ws.onmessage = (event) => {
        try {
          const data: WsMessage = JSON.parse(event.data)
          showNotification(data)
        } catch {
          console.warn('[WS] 消息解析失败:', event.data)
        }
      }

      ws.onclose = () => {
        connected.value = false
        console.log('[WS] 已断开')
        // 5 秒后自动重连
        reconnectTimer = setTimeout(() => connect(), 5000)
      }

      ws.onerror = () => {
        ws?.close()
      }
    } catch (e) {
      console.warn('[WS] 连接失败:', e)
    }
  }

  function disconnect() {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    if (ws) {
      ws.onclose = null // 阻止触发重连
      ws.close()
      ws = null
    }
    connected.value = false
  }

  function showNotification(data: WsMessage) {
    const isAdmin = userStore.role === 'admin'
    const isTeacher = userStore.role === 'teacher'
    // 只显示发给自己角色的通知
    if (data.targetRole === 'admin' && !isAdmin) return
    if (data.targetRole === 'teacher' && !isTeacher) return

    ElNotification({
      title: data.title,
      message: data.message,
      type: data.targetRole === 'admin' ? 'warning' : 'success',
      duration: 6000,
      position: 'top-right'
    })
  }

  // 组件卸载时断开
  onUnmounted(() => disconnect())

  return { connect, disconnect, connected }
}
