# 个人待办工具 — 设计方案

> 日期：2026-05-18 | 状态：已定稿

---

## 1. 技术栈

| 层 | 选择 |
|----|------|
| 前端 | Vue 3 + Vite + Pinia + Axios |
| 后端 | Spring Boot + Spring Security + MyBatis-Plus |
| 数据库 | MySQL |
| 认证 | JWT 双 Token（Access + Refresh） |
| 部署 | Nginx（前端静态文件 + API 反向代理）+ Spring Boot（后端服务） |

---

## 2. 整体架构

```
浏览器 → Nginx(:80)
           ├── /          → Vue 静态文件（index.html, js, css）
           └── /api/*     → 反向代理到 Spring Boot(:8080)
                              └── MySQL(:3306)
```

- 前端直接调用天气 API（和风天气等），不经过后端代理
- Nginx 托管 Vue 构建产物，处理 `/api/*` 转发

---

## 3. 布局设计

整体为**左右分栏仪表盘**风格：

- **左侧 50%**：日历面板
  - 月视图 7×6 网格，支持月份切换
  - 每格左上阳历日期，右上农历日期（节日替换为节日名称）
  - 待办事项以短句直接列在格子内，红色=高/黄色=中/绿色=低优先级圆点
  - 内容超出时显示 "+N 更多"
  - **单击格子**弹出放大镜浮层（卡内完整列出当日待办），弹出方向根据格子位置自适应（斜上/斜下，不出边界）

- **右侧 50%**：三栏纵向排列
  1. **天气 Widget**：定位图钉按钮（点击弹出城市搜索）→ 天气图标 → 多云转晴文字 → 温度/湿度 → 城市名 → 分隔线 → 未来三天横向排列
  2. **任务完成率**：左大右小分栏
     - 左侧：今日环形图 + 本周环形图左右排列，圈内显示分数和"今日完成"/"本周完成"，圈下显示"今日进度 已完成40%" / "本周总览 剩余任务12项"
     - 右侧：点击环形图切换详情列表，分已完成/待完成，超出可滚动
  3. **快速添加栏**：输入框 + 添加按钮，点击添加后弹出截止日期/分类/优先级选择

- **暗色/亮色模式**：CSS 变量驱动，localStorage 持久化，默认跟随系统主题

---

## 4. 数据库设计

### users（用户表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 主键 |
| username | VARCHAR(50) UNIQUE NOT NULL | 登录用户名 |
| password | VARCHAR(255) NOT NULL | BCrypt 加密 |
| avatar | VARCHAR(500) NULL | 头像 URL |
| created_at | DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP | 注册时间 |

### categories（分类表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 主键 |
| user_id | BIGINT FK NOT NULL | 所属用户 |
| name | VARCHAR(50) NOT NULL | 分类名 |
| color | VARCHAR(7) NULL | 分类色值（如 #6366f1） |
| created_at | DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### todos（待办表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 主键 |
| user_id | BIGINT FK NOT NULL | 所属用户 |
| title | VARCHAR(200) NOT NULL | 标题 |
| description | TEXT NULL | 详细描述 |
| completed | TINYINT(1) NOT NULL DEFAULT 0 | 0=未完成, 1=已完成 |
| priority | TINYINT NOT NULL DEFAULT 1 | 0=低(绿), 1=中(黄), 2=高(红) |
| due_date | DATE NULL | 截止日期 |
| category_id | BIGINT FK NULL | 所属分类 |
| created_at | DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

索引：`idx_user_completed_duedate (user_id, completed, due_date)`，`idx_user_category (user_id, category_id)`

### refresh_tokens（刷新令牌表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AUTO_INCREMENT | 主键 |
| user_id | BIGINT FK NOT NULL | 所属用户 |
| token | VARCHAR(500) UNIQUE NOT NULL | Refresh Token |
| expires_at | DATETIME NOT NULL | 过期时间 |
| created_at | DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |

索引：`idx_token (token)`，`idx_user_expires (user_id, expires_at)`

---

## 5. API 设计

统一响应格式：`{ "code": 200, "message": "success", "data": {...} }`

### 认证

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/api/auth/register` | 注册 | 无 |
| POST | `/api/auth/login` | 登录，返回 Access + Refresh Token | 无 |
| POST | `/api/auth/refresh` | 刷新 Access Token | Refresh Token |
| POST | `/api/auth/logout` | 退出登录，失效 Refresh Token | Access Token |

### 待办

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| GET | `/api/todos` | 查询列表（?status=&priority=&category_id=&keyword=&sort=&page=&size=） | Access Token |
| POST | `/api/todos` | 创建待办 | Access Token |
| PUT | `/api/todos/{id}` | 更新待办 | Access Token |
| DELETE | `/api/todos/{id}` | 删除待办 | Access Token |
| PATCH | `/api/todos/{id}/toggle` | 切换完成状态 | Access Token |

### 分类

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| GET | `/api/categories` | 获取当前用户分类列表 | Access Token |
| POST | `/api/categories` | 创建分类 | Access Token |
| PUT | `/api/categories/{id}` | 更新分类 | Access Token |
| DELETE | `/api/categories/{id}` | 删除分类 | Access Token |

### 统计

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| GET | `/api/stats/daily` | 今日完成数/总数/完成率 | Access Token |
| GET | `/api/stats/weekly` | 本周完成数/总数/完成率/日趋势 | Access Token |

### 用户

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| GET | `/api/user/profile` | 获取个人信息 | Access Token |
| PUT | `/api/user/profile` | 更新个人信息 | Access Token |
| POST | `/api/user/avatar` | 上传头像（multipart） | Access Token |

---

## 6. 前端架构

### 路由

| 路径 | 组件 | 要求 |
|------|------|------|
| `/login` | LoginView | 未登录 |
| `/register` | RegisterView | 未登录 |
| `/` | DashboardView | 需登录（路由守卫） |
| `/*` | 重定向到 `/` | - |

### 组件树

```
App.vue
├── AppHeader.vue                # Logo + ThemeToggle + 用户头像/退出
├── LoginView.vue                # /login
├── RegisterView.vue             # /register
└── DashboardView.vue            # /
    ├── CalendarPanel.vue        # 左侧日历面板
    │   ├── MonthNav.vue         #   月份导航
    │   ├── CalendarGrid.vue     #   7×6 网格
    │   │   └── DayCell.vue      #     单格（阳历+农历/节日+事项色点）
    │   └── MagnifierPopup.vue   #   单击弹出浮层（自适应方向）
    └── SidePanel.vue            # 右侧面板
        ├── WeatherWidget.vue    #   天气（定位+今日+未来三天）
        │   └── CitySearchDialog.vue  # 城市搜索弹窗
        ├── StatsCard.vue        #   任务完成率
        │   ├── RingChart.vue    #     环形进度图（复用×2）
        │   └── TaskDetailList.vue    # 已完成/待完成（点击切换）
        └── QuickAddBar.vue      #   快速添加
            └── AddTodoDialog.vue     # 添加弹窗（日期/分类/优先级）
```

### 状态管理（Pinia）

- `useAuthStore`：用户信息、Access Token、Refresh Token、登录/登出/刷新
- `useThemeStore`：dark/light 主题，localStorage 持久化
- `useWeatherStore`：当前城市、天气数据
- 组件内部状态：日历月份、任务列表分页、统计数据

### Axios 拦截器

- 请求拦截：自动注入 `Authorization: Bearer <accessToken>`
- 响应拦截：401 时自动用 Refresh Token 换取新 Access Token，重试原请求；Refresh 也失败则跳登录页

---

## 7. 后端架构

### 包结构

```
com.todo
├── TodoApplication.java
├── config/          # SecurityConfig, WebConfig, MyBatisPlusConfig
├── controller/      # Auth, Todo, Category, Stats, User
├── service/         # 接口 + impl
├── mapper/          # MyBatis-Plus Mapper 接口
├── entity/          # User, Todo, Category, RefreshToken
├── dto/             # 请求/响应 DTO
├── security/        # JwtTokenProvider, JwtAuthFilter, UserDetailsServiceImpl
├── common/          # Result, GlobalExceptionHandler, BizException
└── util/            # FileUploadUtil
```

### 关键依赖

| 依赖 | 用途 |
|------|------|
| spring-boot-starter-web | MVC |
| spring-boot-starter-security | 认证鉴权 |
| mybatis-plus-boot-starter | ORM |
| mysql-connector-j | 数据库驱动 |
| jjwt (io.jsonwebtoken) | JWT |
| lombok | 减少样板代码 |
| spring-boot-starter-validation | 参数校验 |

### JWT 双 Token 流程

1. 登录 → 返回 Access Token（15min）+ Refresh Token（7天）
2. Access Token 过期 → 前端自动调用 `/api/auth/refresh` 换取新 Access Token
3. Refresh Token 过期 → 跳转登录页
4. 退出登录 → 删除数据库中的 Refresh Token

---

## 8. 功能一览

| 模块 | 功能 |
|------|------|
| 认证 | 注册、登录、双 Token 续期、退出 |
| 待办 CRUD | 创建、编辑、删除、切换完成、搜索筛选排序分页 |
| 分类 | 创建、编辑、删除分类，标记颜色 |
| 日历 | 月视图、农历/节日显示、优先级色点、+N 折叠、单击放大镜 |
| 统计 | 今日/本周环形进度、已完成/待完成列表 |
| 天气 | 今日+未来三天、城市搜索切换 |
| 主题 | 暗色/亮色切换，localStorage 持久化 |
| 用户 | 个人资料编辑、头像上传 |

---

## 9. 不在范围内

以下功能确定不做（可后续迭代）：
- 多用户协作
- 邮件/浏览器通知提醒
- 数据导出
- 拖拽排序
- 子任务
