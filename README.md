# 监考管理系统

> 全功能高校监考管理平台 — 前端 Vue 3 + TypeScript，后端 Spring Boot + MySQL

<div align="center">

[在线体验](https://invigilation-system-sdut.up.railway.app/) | [技术栈](#技术栈) | [项目架构](#项目架构) | [核心功能](#核心功能)

</div>

---

## 技术栈

| 分类 | 技术 |
|------|------|
| **前端框架** | Vue 3 + TypeScript（严格模式） |
| **构建工具** | Vite |
| **UI 组件库** | Element Plus |
| **状态管理** | Pinia |
| **路由** | Vue Router |
| **HTTP 客户端** | Axios |
| **数据可视化** | ECharts |
| **实时通信** | WebSocket |
| **测试** | Vitest |
| **代码规范** | ESLint + Prettier |
| **后端** | Spring Boot 3 + MyBatis + MySQL |
| **部署** | Docker 多阶段构建 + Railway CI/CD |

---

## 项目架构

```
┌─────────────────────────────────────────────────┐
│                 用户浏览器                         │
└────────────────────┬────────────────────────────┘
                     │ HTTPS
┌────────────────────▼────────────────────────────┐
│          Express 代理服务器 (Node.js)              │
│  ┌─────────────────────────────────────────────┐ │
│  │  静态资源: Vue 构建产物 (./public/)           │ │
│  │  动态请求: 代理转发至 Spring Boot 后端        │ │
│  └─────────────────────────────────────────────┘ │
└────────────────────┬────────────────────────────┘
                     │ /invigilation/api/*
┌────────────────────▼────────────────────────────┐
│           Spring Boot 后端 (Java 21)              │
│  ┌─────────────────────────────────────────────┐ │
│  │  业务层: 登录认证 / 教师管理 / 排考调度        │ │
│  │         冲突检测 / 学期管理 / 通知推送         │ │
│  │  数据层: MyBatis + MySQL                     │ │
│  │  通信层: WebSocket                           │ │
│  └─────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────┘
```

### 前端目录结构

```
invigilation-system/src/
├── api/              # API 接口封装
│   ├── admin.ts      # 管理员端接口
│   └── teacher.ts    # 教师端接口
├── components/       # 通用组件
│   └── ImportDialog.vue   # Excel 导入组件
├── composables/      # 组合式函数
│   └── useWebSocket.ts    # WebSocket 封装
├── layouts/          # 布局组件
│   ├── AdminLayout.vue    # 管理员布局
│   └── TeacherLayout.vue  # 教师布局
├── router/           # 路由配置
│   └── index.ts      # 路由表 + 导航守卫
├── stores/           # Pinia 状态管理
│   ├── user.ts       # 用户状态
│   ├── theme.ts      # 主题切换
│   └── semester.ts   # 学期状态
├── utils/            # 工具函数
│   ├── request.ts    # Axios 封装
│   └── export.ts     # Excel 导出
├── views/            # 页面组件
│   ├── admin/        # 管理员端页面
│   │   ├── Dashboard.vue          # 控制台
│   │   ├── TeacherManage.vue      # 教师管理
│   │   ├── ExamManage.vue         # 考试管理
│   │   ├── InvigilationConfig.vue # 监考配置
│   │   └── SemesterManage.vue     # 学期管理
│   └── teacher/      # 教师端页面
│       ├── MyInvigilation.vue     # 我的监考
│       ├── InvigilationQuery.vue  # 监考查询
│       └── Profile.vue           # 个人信息
```

---

## 核心功能

### 1. 双端权限路由体系

- Vue Router 导航守卫 + Pinia 状态管理实现**管理员/教师两套视图隔离**
- 路由表通过 `meta.requiresAuth` 和 `meta.role` 标记权限
- 未登录自动拦截跳转登录页，防止 URL 直接访问越权

### 2. JWT 无感认证

- Axios 请求拦截器从 localStorage 读取 Token 并自动注入 `Authorization` 头
- 响应拦截器统一处理 401 状态码，自动清除用户状态并重定向
- 前端完全无感完成身份验证与过期处理

### 3. 组件化设计

- **ECharts 图表组件**：4 种图表（饼图/折线图/排行/柱状图）通过 Props 接收数据，与业务逻辑解耦
- **ImportDialog 导入组件**：接收 `import-api` Prop，传入不同 API 接口即可复用
- 主题切换：Pinia Store + CSS 变量，所有组件自动响应深色/浅色切换

### 4. 智能冲突检测

- 监考安排表单实时校验：300ms 防抖调用后端冲突检测接口
- 自动检测**教师时间冲突**和**教室占用冲突**
- 冲突结果以警告列表实时展示，提交前二次确认弹窗

### 5. WebSocket 实时通知

- 封装 `useWebSocket` Composable，连接时携带角色和用户名参数
- 教师提交冲突申请 → 服务端主动推送至管理员端
- 管理员审批 → 即时回推审批结果至教师端

### 6. 数据可视化

- 仪表盘展示：监考概况统计、状态分布、月趋势、教师工作量排行
- 4 个 ECharts 组件均可复用、可配置
- 支持一键深色主题切换

### 7. 工程化

- TypeScript 严格模式全覆盖，杜绝隐式 any
- ESLint + Prettier 统一代码风格
- Vitest 编写核心工具函数单元测试
- Docker 多阶段构建 + Railway CI/CD 自动部署

---

## 快速开始

```bash
# 克隆项目
git clone https://github.com/your-username/invigilation.git
cd invigilation

# 安装前端依赖
cd invigilation-system
pnpm install

# 启动开发服务器
pnpm dev

# 构建生产版本
pnpm build
```

---

## 部署

项目通过 Docker 多阶段构建部署至 Railway，Push 到 GitHub 后自动触发 CI/CD 流水线。

- **前端服务**：`Dockerfile.frontend` — 构建 Vue 项目 + Express 服务器
- **后端服务**：`invigilation/Dockerfile` — Maven 构建 + Spring Boot 运行

---

## 在线体验

[https://invigilation-system-sdut.up.railway.app/](https://invigilation-system-sdut.up.railway.app/)
教师端:
账号：T001
密码：123456
管理员端：
账号：admin
密码：YZYyzy111

