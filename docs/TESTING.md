# 测试说明

## 后端测试

后端使用 Jest + ts-jest。

```bash
cd server
npm install
npm test
```

当前已覆盖：

- `SafetyClassifier`：自伤危机、违法请求、未成年人亲密关系、依赖风险。
- `RolePromptBuilder`：AI 身份提示、角色风格、记忆拼装、安全边界。
- `subscription-limits`：免费版、月会员、高级会员额度配置。

建议后续继续补：

- `AuthService`：注册、登录、刷新 token。
- `ChatService`：发消息前额度检查、危机模式分支、普通模型回复分支。
- `MemoryService`：敏感记忆待确认、确认保存、拒绝保存、角色记忆隔离。
- `AdminService`：角色状态变更、安全事件查询、举报查询、举报状态处理和审计日志。

## Android 单元测试

Android 当前补了 JVM 单测，不需要启动模拟器。

当前仓库没有提交 Gradle Wrapper。没有全局 `gradle` 时，请用 Android Studio 的 Gradle 面板运行同名任务。

```bash
cd android
gradle testDebugUnitTest
```

Windows PowerShell：

```powershell
cd android
gradle testDebugUnitTest
```

当前已覆盖：

- `NetworkError.asMessage()`：关键错误提示是否可读。
- `CreateCustomRoleUseCase`：自定义角色创建是否委托 Repository 并返回角色。

建议后续继续补：

- `CreateRoleViewModel`：表单校验、创建成功、创建失败。
- `ChatViewModel`：发送消息、保存记忆、错误状态。
- `MemoryViewModel`：删除记忆、确认敏感记忆、关闭角色记忆。
- `MoodCheckInViewModel`：提交心情和周报刷新。
- `MembershipViewModel`：mock upgrade 和额度展示。

## 管理后台测试

管理后台当前以手动验收为主：

```bash
cd admin-web
npm install
npm run dev
```

建议手动验证：

- 使用后端账号登录后台。
- 用户列表能正确展示邮箱、昵称、会员等级和未成年人状态。
- 角色列表能切换 `ACTIVE / BLOCKED / ARCHIVED`，并在审计日志页看到操作记录。
- 安全事件页能展示风险类型、风险等级、处理动作。
- 聊天日志页能展示用户消息和 AI 消息。
- 举报页能展示举报原因和状态，并能更新为 `REVIEWED / RESOLVED / REJECTED`。
- 审计日志页能展示角色状态更新和举报处理记录。

后续可补：

- React Testing Library 组件测试。
- Playwright e2e 测试。
- Admin API mock 层测试。

## 手动验收清单

- 注册、登录、年龄校验后进入角色列表。
- 未成年人账号不能进入恋爱陪伴角色。
- 免费版只能使用 3 个基础角色，超出时后端返回额度错误。
- 聊天页显示“AI 角色，不是真人”。
- 发送自伤、自杀、暴力、违法文本时，进入安全危机回复，不继续角色扮演。
- 长按聊天消息可以复制、删除、举报、提交记忆。
- 敏感记忆进入记忆页待确认，用户可确认或拒绝。
- 情绪打卡可保存，月会员及以上可获取周报。
- 会员中心 mock upgrade 后额度变化。
- Admin API 可查看用户、角色、安全事件、聊天日志、举报和审计日志。
