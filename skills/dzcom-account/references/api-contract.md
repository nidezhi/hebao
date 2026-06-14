# 账户域接口契约

## 1. 通用约定

- 基础路径：`/api`
- 请求方法：所有接口统一使用 `POST`
- 请求参数：业务参数统一放入 JSON 请求体，禁止使用 URL 查询参数和路径变量
- 响应体：`Result<T>`
- 时间格式：ISO-8601
- 分页参数：`page` 从 1 开始，`size` 默认 20，最大 100
- 列表默认按 `createdAt DESC`
- 业务错误同时映射正确 HTTP 状态

## 2. 认证接口

### 注册

`POST /api/auth/register`

请求字段：

- `username`：必填，4 至 32 位
- `password`：必填，至少 8 位，必须包含字母和数字
- `email`：可选，合法邮箱
- `phone`：可选，标准化手机号

响应：

- `201 Created`
- 返回 `UserSummaryResponse`
- 注册成功后是否自动登录作为显式配置；首版默认不自动登录

### 登录

`POST /api/auth/login`

请求字段：

- `account`：用户名、邮箱或手机号
- `password`

行为：

- 校验用户未删除且状态正常。
- 创建不透明会话令牌并写入 Redis。
- 令牌通过 HttpOnly Cookie 返回。
- 失败统一返回“账号或密码错误”。

响应：

- `200 OK`
- 返回当前用户摘要，不在响应 JSON 中返回令牌

### 登出

`POST /api/auth/logout`

- 撤销当前会话。
- 清除 Cookie。
- 重复登出保持幂等。

### 当前用户

`POST /api/auth/me`

- 返回当前登录用户基础信息。
- 不返回 `passwordHash`、内部删除标记等字段。

## 3. 本人账户接口

### 更新本人资料

`POST /api/users/me/update`

允许修改：

- `email`
- `phone`

禁止修改：

- `username`
- `status`
- `kycStatus`
- `riskLevel`

### 修改密码

`POST /api/users/me/password`

请求字段：

- `currentPassword`
- `newPassword`

行为：

- 校验旧密码。
- 更新密码哈希。
- 撤销除当前请求外的全部会话；首版也可撤销全部会话并要求重新登录。

### 查询偏好

`POST /api/users/me/preferences/list`

### 设置偏好

`POST /api/users/me/preferences/set`

- 使用 upsert 语义。
- 请求体包含 `key` 和 `value`。
- 偏好键必须通过校验。

### 删除偏好

`POST /api/users/me/preferences/delete`

- 请求体包含 `key`。
- 使用逻辑删除。
- 重复删除保持幂等。

## 4. 管理端用户接口

管理端统一使用 `/api/admin/users`，必须经过管理权限校验。

### 用户列表

`POST /api/admin/users/list`

请求体筛选参数：

- `keyword`：匹配用户编号、用户名、邮箱或手机号
- `status`
- `kycStatus`
- `riskLevel`
- `page`
- `size`
- `sort`

响应：

- `PageResult<UserListItemResponse>`

### 用户详情

`POST /api/admin/users/detail`

- 请求体包含 `bizId`。

### 管理端创建用户

`POST /api/admin/users/create`

- 复用注册领域规则。
- 支持显式设置初始状态，但不能接收明文密码之外的密码哈希。

### 管理端更新用户

`POST /api/admin/users/update`

允许修改：

- `bizId`
- `email`
- `phone`

状态、KYC 和风险等级使用专用接口，不混入通用更新。

### 更新账户状态

`POST /api/admin/users/status`

请求：

```json
{
  "bizId": "用户业务标识",
  "status": "ACTIVE"
}
```

- `ACTIVE` 表示启用。
- `DISABLED` 表示禁用。
- 禁用后撤销该用户全部会话。

### 更新 KYC 状态

`POST /api/admin/users/kyc-status`

- 请求体包含 `bizId` 和 `kycStatus`。

### 更新风险等级

`POST /api/admin/users/risk-level`

- 请求体包含 `bizId` 和 `riskLevel`。

### 删除用户

`POST /api/admin/users/delete`

- 请求体包含 `bizId`。
- 软删除用户和偏好。
- 撤销全部会话。
- 重复删除返回成功或资源不存在，项目必须统一选择；首版建议幂等成功。

## 5. 建议响应对象

- `UserSummaryResponse`
- `CurrentUserResponse`
- `UserListItemResponse`
- `UserDetailResponse`
- `UserPreferenceResponse`
- `PageResult<T>`

所有响应对象显式列字段，禁止直接返回领域对象或 JPA Entity。

## 6. 建议错误码

| HTTP 状态 | 业务场景 |
| --- | --- |
| `400` | 参数或状态转换非法 |
| `401` | 未登录、会话失效、登录失败 |
| `403` | 用户禁用或无管理权限 |
| `404` | 用户不存在 |
| `409` | 用户名、邮箱或手机号冲突 |
| `429` | 登录或注册请求过于频繁 |
