# 部署说明

## 环境要求

- Node.js 20+
- PostgreSQL 15+
- Redis 7+
- Java 17 + Android Studio，用于 Android 构建

## 后端环境变量

复制 `server/.env.example` 为 `server/.env`。

核心变量：

```env
APP_PORT=3000
DATABASE_URL="postgresql://postgres:postgres@localhost:5432/xinyu_ai?schema=public"
REDIS_URL="redis://localhost:6379"
JWT_SECRET="replace-with-access-secret"
JWT_REFRESH_SECRET="replace-with-refresh-secret"
ACCESS_TOKEN_TTL="15m"
REFRESH_TOKEN_TTL="30d"
AI_API_BASE_URL="https://api.openai.com/v1"
AI_API_KEY=""
AI_MODEL="gpt-4o-mini"
ADMIN_DEV_ALLOW_ALL="false"
ADMIN_USER_IDS=""
ADMIN_EMAILS=""
```

注意：

- `AI_API_KEY` 只能放在后端环境变量或密钥管理服务中，不能下发给 Android 客户端。
- 生产环境必须关闭 `ADMIN_DEV_ALLOW_ALL`。
- `JWT_SECRET` 和 `JWT_REFRESH_SECRET` 必须使用不同的高强度随机字符串。

## 管理后台环境变量

复制 `admin-web/.env.example` 为 `admin-web/.env.local`。

```env
NEXT_PUBLIC_API_BASE_URL="http://localhost:3000"
```

后台登录后会调用后端 `/auth/login` 获取 token，再访问 `/admin/*` 接口。生产环境建议给后台单独域名、HTTPS、二次认证和 IP allowlist。

## Docker Compose 本地部署

项目根目录提供 `docker-compose.yml`：

```bash
docker compose up --build
```

默认服务：

- Backend API: `http://localhost:3000`
- Admin Web: `http://localhost:3001`
- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`

当前仓库以 Prisma schema 为主，首次本地启动可执行开发迁移：

```bash
docker compose exec server npx prisma migrate dev --name init
```

生产环境建议提交 `server/prisma/migrations/` 后再使用 `npx prisma migrate deploy`。

健康检查：

```bash
curl http://localhost:3000/health
```

## 传统后端部署

```bash
cd server
npm install
npx prisma generate
npx prisma migrate deploy
npm run build
npm run start:prod
```

建议生产环境使用：

- Docker / Kubernetes
- systemd
- PM2

## 传统管理后台部署

```bash
cd admin-web
npm install
npm run build
npm run start
```

默认端口是 `3001`，可通过 `PORT` 环境变量调整。

## Android 发布构建

开发版：

```bash
cd android
gradle assembleDebug
```

正式版发布前需要补齐：

- release 签名配置
- 按环境区分 `API_BASE_URL`
- ProGuard/R8 规则复核
- 隐私政策、用户协议和 AI 明示确认
- 年龄分级与应用商店合规材料

## 运行日志与审计

后端当前已有：

- 全局异常过滤器：统一返回 `success=false` 和可读错误信息。
- `UsageLoggingInterceptor`：记录接口 action 到 `usage_logs`。
- `safety_events`：记录危机、违法、未成年人亲密关系等风险事件。
- `reports`：记录用户举报、处理状态、处理备注和处理人。
- `admin_audit_logs`：记录后台角色状态变更、举报处理等关键操作。
- Admin API：可查看用户、角色、安全事件、聊天日志、举报和审计日志。

后续可增强：

- 独立 `ai_request_logs` 表，细分模型调用成本和 token。
- 将 Admin 审计日志接入告警或 SIEM。
- Redis 限流日志和异常告警。

## 生产安全建议

- 所有 API 必须启用 HTTPS。
- 模型 Key 使用密钥管理服务或容器 secret。
- PostgreSQL 开启自动备份和 PITR。
- 对 `/chat/sessions/:id/messages` 增加 Redis 限流。
- Admin API 使用独立域名、IP allowlist、二次认证。
- 聊天、记忆、举报和安全事件设置数据保留周期。
- 敏感记忆、聊天正文和安全事件建议做字段级加密或 KMS 托管加密。
