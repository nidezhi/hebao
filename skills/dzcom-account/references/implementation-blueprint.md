# 账户域实现蓝图

## 1. 包结构

```text
com.example.dzcom
├── domain/account/
│   ├── model/
│   ├── valueobject/
│   ├── enums/
│   ├── repository/
│   └── service/
├── application/account/
│   ├── command/
│   ├── query/
│   ├── service/
│   └── assembler/
├── interfaces/account/
│   ├── controller/
│   ├── request/
│   └── response/
└── infrastructure/account/
    ├── persistence/
    │   ├── entity/
    │   ├── repository/
    │   └── mapper/
    ├── security/
    └── session/
```

## 2. 应用服务

### AuthenticationApplicationService

- `register`
- `login`
- `logout`
- `getCurrentUser`

### UserApplicationService

- `createUser`
- `updateCurrentProfile`
- `updateLoginIdentity`
- `changePassword`
- `changeStatus`
- `changeKycStatus`
- `changeRiskLevel`
- `assignRole`
- `removeRole`
- `deleteUser`

### UserQueryService

- `getCurrentUser`
- `getUserDetail`
- `listUsers`

### UserPreferenceApplicationService

- `listPreferences`
- `setPreference`
- `deletePreference`

命令和查询较少时可先放在上述服务中，类膨胀后再拆 Handler，不提前引入 CQRS 框架。

## 3. 通用 service 层

通用层只提供跨用例稳定复用的能力：

- `IdGenerator`：生成 UUID 业务 ID 和并发安全的用户编号。
- `PasswordHasher`：提供 `hash` 和 `matches`，基础设施首版使用 BCrypt。
- `SessionService`：创建、解析、撤销单个或用户全部会话。
- `CurrentOperatorProvider`：提供当前用户、会话和角色。
- `ClockProvider`：统一时间来源，便于测试。
- `PageQuery` 与 `PageResult`：隔离 Spring Data 分页类型。
- `IdentityNormalizer`：按用户名、邮箱、手机号类型进行标准化。

禁止创建 `BaseCrudService<T>`、依赖 JPA Entity 的通用业务 Service 和万能动态查询 Service。

## 4. 仓储接口

- `UserRepository`：用户主体保存、按业务 ID 查询、状态筛选和分页查询。
- `UserIdentityRepository`：按类型和标准化值查询、唯一性判断、查询用户全部标识。
- `UserCredentialRepository`：读取和保存指定用户的密码凭据。
- `UserProfileRepository`：读取和保存用户资料。
- `UserRiskProfileRepository`：读取和保存 KYC 与风险画像。
- `UserRoleRepository`：查询、分配和撤销角色。
- `UserPreferenceRepository`：查询、upsert 和软删除偏好。

仓储查询默认排除 `is_deleted = 1`。查询已删除数据必须使用明确命名的专用方法。

## 5. 持久化映射

| Entity | 数据表 |
| --- | --- |
| `UserEntity` | `aiw_user` |
| `UserIdentityEntity` | `aiw_user_identity` |
| `UserCredentialEntity` | `aiw_user_credential` |
| `UserProfileEntity` | `aiw_user_profile` |
| `UserRiskProfileEntity` | `aiw_user_risk_profile` |
| `UserRoleEntity` | `aiw_user_role` |
| `UserPreferenceEntity` | `aiw_user_preference` |

- Entity 之间不使用 `@ManyToOne`、`@OneToMany`、`CascadeType` 或 `orphanRemoval`。
- 逻辑关联只映射为 `String userBizId` 等标量字段。
- 领域对象与 Entity 的映射器位于基础设施层，不使用反射式 Bean Copy。
- 查询响应由应用层 assembler 组装，不把 Entity 暴露给 Controller。
- `aiw_user.version` 映射为 JPA `@Version`。

## 6. 关键用例事务

### 注册

1. 标准化全部登录标识。
2. 通过仓储预检查唯一性。
3. 创建用户主体、标识、凭据、资料、风险画像和默认角色。
4. 在同一个数据库事务中显式调用各仓储保存。
5. 唯一索引冲突统一映射为账号已存在错误。

### 登录

1. 标准化账号并查询 `aiw_user_identity`。
2. 查询用户主体和密码凭据。
3. 检查删除、状态、标识状态、临时锁定和凭据有效期。
4. 使用恒定时间密码比较。
5. 失败时更新失败次数或锁定时间；成功时清零失败次数并记录最近登录。
6. 数据库事务提交后创建 Redis 会话。

### 改密、禁用和删除

- 改密递增 `credential_version`，提交后撤销全部旧会话。
- 禁用或锁定用户后撤销全部会话。
- 删除显式软删除账户域全部从属记录，不依赖级联。
- 关键操作写入审计日志或事务发件箱。

## 7. 会话方案

- Cookie 名为 `DZCOM_SESSION`。
- Cookie 设置 `HttpOnly=true`、`Path=/`、`SameSite=Lax`；线上环境设置 `Secure=true`。
- 会话令牌至少 256 bit 随机值，Redis 只保存令牌摘要。
- Redis Key：`dzcom:account:session:{tokenHash}`。
- 用户会话集合：`dzcom:account:user-sessions:{userBizId}`。
- 会话保存用户业务 ID、角色、凭据版本和过期时间。
- 默认有效期 7 天，必须外部配置。

## 8. 安全要求

- 登录按标准化账号和来源地址限流。
- Cookie 鉴权的写接口校验 `Origin` 或专用 CSRF Token。
- 登录失败统一提示，禁止泄露账号是否存在。
- 列表接口中的邮箱和手机号按角色脱敏。
- 日志、审计和事件禁止包含密码、完整令牌、完整手机号和完整邮箱。
