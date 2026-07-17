const express = require('express')
const history = require('connect-history-api-fallback')
const path = require('path')
const { createProxyMiddleware } = require('http-proxy-middleware')

const PORT = process.env.PORT || 8089
// Railway 内部通信：Spring Boot 服务的地址（格式：http://<service-name>.railway.internal:<port>）
// 本地开发默认 fallback 到 localhost:8088
const API_TARGET = process.env.API_URL || 'http://localhost:8088'

const app = express()

// 把 /invigilation 开头的请求转发到 Spring Boot 后端
app.use(
  createProxyMiddleware({
    target: API_TARGET,
    changeOrigin: true,
    pathFilter: '/invigilation',
    proxyTimeout: 60000,
    timeout: 60000
  })
)

// 静态文件 & SPA 回退
app.use(history())
app.use(express.static(path.join(__dirname, 'public')))

app.listen(PORT, '0.0.0.0', () => {
  console.log(`服务器启动成功，访问 http://localhost:${PORT}`)
  console.log('局域网访问：http://<本机IP>:' + PORT)
})
