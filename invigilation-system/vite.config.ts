import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path' //引入Node.js的path模块，用于处理路径

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()], // 保留原有Vue插件配置，无需改动
  //路径别名配置，解决@指向src目录的问题
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src') // 把@映射为项目根目录下的src文件夹绝对路径
    }
  },
  // 局域网访问配置：允许手机等设备连接
  server: {
    host: '0.0.0.0', // 监听所有网络接口，允许局域网访问
    port: 5173,
    // 开发模式下将 API 请求代理到 Spring Boot 后端
    proxy: {
      '/invigilation': {
        target: 'http://localhost:8088',
        changeOrigin: true
      }
    }
  }
})
