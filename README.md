# 心屿 AI

心屿 AI 是一个 AI 情绪陪伴与角色聊天 App。项目包含 Android 客户端、NestJS 后端、PostgreSQL/Prisma 数据库、Redis 缓存、AI Gateway、安全风控、角色记忆、情绪打卡、会员额度和管理后台。

当前版本重点保证产品边界清晰：

- 用户始终知道自己正在与 AI 互动，不是真人。
- 情绪支持角色只做陪伴、倾听、心理健康科普和压力梳理，不做医疗诊断或心理治疗。
- 恋爱陪伴角色只向成年人开放。
- 自伤、自杀、暴力、违法、严重危机场景会进入全局安全模式。
- 模型 API Key 只保存在后端，不进入 Android 客户端。

## 项目结构

```text
.
├── android/       # Kotlin + Jetpack Compose Android 客户端
├── server/        # NestJS + Prisma 后端 API
├── admin-web/     # Next.js + React + Tailwind 管理后台
├── docs/          # 测试与部署文档
└── docker-compose.yml
```

## Android 客户端

技术栈：

- Kotlin
- Jetpack Compose
- MVVM + Repository + UseCase
- Hilt
- Retrofit + OkHttp
- Room
- DataStore
- Navigation Compose

运行方式：

```bash
cd android
gradle testDebugUnitTest
```

也可以直接用 Android Studio 打开 `android/` 目录并运行 `app` 模块。

注意：仓库当前没有提交 Gradle Wrapper。如果本机没有全局 `gradle`，建议用 Android Studio 的 Gradle 面板运行。

Android 模拟器默认连接后端地址：

```text
http://10.0.2.2:3000/
```

真机调试时，需要把 `android/app/build.gradle.kts` 中的 `API_BASE_URL` 改成电脑局域网 IP。

## 后端 API

技术栈：

- Node.js
- NestJS
- PostgreSQL
- Prisma
- Redis
- JWT Auth
- OpenAI-compatible AI Gateway

本地启动：

```bash
cd server
cp .env.example .env
npm install
npx prisma generate
npx prisma migrate dev
npm run prisma:seed
npm run start:dev
```

Demo seed data is safe to publish and contains only fake data. It creates:
- Demo account: `demo@xinyu.local`
- Demo password: `Demo123456`
- A few official roles, one sample chat, one sample memory, and one mood log

主要模块：

- `AuthModule`：注册、登录、刷新 token、退出
- `UserModule`：用户资料、年龄校验、软删除
- `RoleModule`：官方角色、自定义角色
- `ChatModule`：会话、消息、AI 回复
- `SafetyModule`：风险分类、危机模式、安全事件
- `PromptModule`：角色 Prompt 组装
- `AiGatewayModule`：服务端模型调用封装
- `MemoryModule`：角色专属记忆、敏感记忆确认
- `MoodModule`：情绪打卡、周报摘要
- `SubscriptionModule`：会员额度、mock 升级
- `AdminModule`：用户、角色、风险事件、聊天日志、举报审查
- `AdminAuditLog`：记录后台角色状态变更、举报处理等关键操作

如果 `.env` 没有配置 `AI_API_KEY`，后端会使用本地 fallback 回复，便于离线联调。

## 管理后台

技术栈：

- Next.js
- React
- Tailwind CSS

本地启动：

```bash
cd admin-web
cp .env.example .env.local
npm install
npm run dev
```

默认访问地址：

```text
http://localhost:3001
```

后台通过登录后端账号获取 JWT，再调用 `/admin/*` 接口。开发环境可以在 `server/.env` 中设置：

```env
ADMIN_DEV_ALLOW_ALL="true"
```

生产环境请关闭该开关，并配置 `ADMIN_USER_IDS` 或 `ADMIN_EMAILS`。

## Docker 本地运行

```bash
docker compose up --build
```

默认服务：

- Backend API: `http://localhost:3000`
- Admin Web: `http://localhost:3001`
- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`

首次启动后建议进入 server 容器执行 `npx prisma migrate dev --name init`，或在本机先执行迁移。生产环境提交 migrations 后再使用 `migrate deploy`。

## Windows 开发环境

当前项目提供 3 个辅助脚本：

```powershell
powershell -ExecutionPolicy Bypass -File scripts\check-env.ps1
powershell -ExecutionPolicy Bypass -File scripts\setup-windows-dev.ps1
powershell -ExecutionPolicy Bypass -File scripts\compile-all.ps1
```

说明：

- `check-env.ps1`：检查 Node/npm、JDK、Gradle、Docker 和 Android SDK。
- `setup-windows-dev.ps1`：通过 winget 或 Chocolatey 安装 JDK 17、Node.js LTS、Gradle 和 Android Studio，需要管理员 PowerShell。
- `compile-all.ps1`：依次编译后端、管理后台和 Android，并运行已有测试。

## 测试

后端：

```bash
cd server
npm test
```

Android：

```bash
cd android
gradle testDebugUnitTest
```

更多说明见：

- `docs/TESTING.md`
- `docs/DEPLOYMENT.md`

## 安全与合规边界

- Android 客户端不保存模型 API Key。
- 聊天请求必须经过后端 SafetyService。
- 危机内容不会继续普通角色扮演。
- 未成年人不能使用虚拟男友、虚拟女友、虚拟伴侣等亲密关系角色。
- 敏感记忆必须用户单独确认后才能保存。
- 用户可以删除聊天记录、删除长期记忆、关闭记忆功能。
- 角色和系统不能使用情感操控话术诱导付费。

## 推荐下一步
1. 增加 Redis 级别的限流和风控冷却时间。
2. 对敏感字段做加密存储。
3. 给管理后台增加二次认证和操作审计页面。
4. 配置 CI/CD、Android release 签名和应用商店合规材料。
