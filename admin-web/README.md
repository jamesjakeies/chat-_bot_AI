# 心屿 AI 管理后台

Next.js + React + Tailwind CSS 实现的运营后台。

## 功能

- 登录后端账号并保存 access token。
- 查看用户、角色、安全事件、聊天日志、举报。
- 管理角色状态：`ACTIVE`、`BLOCKED`、`ARCHIVED`。
- 处理举报状态：`PENDING`、`REVIEWED`、`RESOLVED`、`REJECTED`。
- 查看审计日志，追踪后台关键操作。
- 后端地址可在登录页填写，默认读取 `NEXT_PUBLIC_API_BASE_URL`。

## 本地运行

```bash
cd admin-web
cp .env.example .env.local
npm install
npm run dev
```

访问：

```text
http://localhost:3001
```

后端本地默认：

```text
http://localhost:3000
```

## Admin 权限

后端本地开发可使用：

```env
ADMIN_DEV_ALLOW_ALL="true"
```

生产环境请关闭，并配置：

```env
ADMIN_USER_IDS=""
ADMIN_EMAILS=""
```

## 注意

当前后台会展示聊天和安全事件内容，生产环境必须配合：

- HTTPS
- 管理员二次认证
- IP allowlist
- 操作审计
- 数据保留周期
- 字段级加密或 KMS
