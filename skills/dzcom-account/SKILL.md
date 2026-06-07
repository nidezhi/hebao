---
name: dzcom-account
description: DZCOM 用户与账户域开发技能。实现或评审用户注册、登录、登出、当前用户、用户列表、详情、更新、启用、禁用、软删除、偏好设置、密码与会话管理，以及账户域通用应用服务时使用。
---

# DZCOM 用户与账户域

## 使用前提

先使用 `$dzcom-project` 获取项目全局约束，再使用本技能处理账户域任务。以下文件是本技能的详细依据：

- [业务边界](references/domain-scope.md)
- [接口契约](references/api-contract.md)
- [实现蓝图](references/implementation-blueprint.md)
- [数据规则与验收](references/data-and-testing.md)

## 核心决策

- 账户域上下文名称固定为 `account`。
- 使用 Spring Data JPA + Flyway，不恢复旧用户示例代码。
- 采用领域模型与 JPA Entity 分离的结构。
- 用户主体、登录标识、凭据、资料、风险画像、角色和偏好分别持久化，不恢复单表用户模型。
- JPA Entity 之间不建立级联关系，只保存标量 `biz_id`；应用服务显式协调多个仓储。
- 登录采用 HttpOnly Cookie + Redis 不透明会话令牌，首版不使用 JWT。
- 删除用户是软删除，同时撤销全部会话并显式逻辑删除账户域从属数据。
- 用户只能修改本人基础资料和密码；状态、KYC、风险等级由管理或风控用例修改。
- 不创建通用 `BaseCrudService<T>`。通用 service 层只提供分页、ID、时钟、密码、会话、当前操作者等跨用例能力。

## 开发流程

1. 读取 `V1__account_domain.sql` 中的全部账户域表。
2. 按 [数据规则与验收](references/data-and-testing.md) 确认表职责、标准化和事务边界。
3. 按 [实现蓝图](references/implementation-blueprint.md) 建立领域层、应用层、接口层和基础设施层。
4. 按 [接口契约](references/api-contract.md) 逐条实现用例，不从 Controller 直接操作仓储。
5. 先完成注册、登录、当前用户，再完成管理端列表和状态管理。
6. 为领域规则写单元测试，为仓储、认证和 API 写集成测试。
7. 同步更新 OpenAPI 和项目权威文档。

## 强制规则

- 密码只以哈希形式持久化，禁止日志输出密码、会话令牌和完整个人敏感信息。
- 所有查询默认排除 `is_deleted = 1`。
- 状态变更、密码变更和删除操作必须撤销相关会话。
- 分页大小最大为 100，排序字段使用白名单。
- 注册唯一性先做业务校验，最终以数据库唯一约束兜底。
- 登录账号必须通过 `aiw_user_identity.identity_type + normalized_value` 定位，不扫描用户主表。
- 禁止使用 `@ManyToOne`、`@OneToMany` 或数据库级联处理账户数据。
- Controller 只负责协议转换、校验和响应；事务位于应用服务。
- API 错误必须同时返回正确 HTTP 状态和统一 `Result<T>` 响应体。
- 登录失败返回统一提示，不能暴露账号是否存在。

## 完成标准

- `./mvnw test` 通过。
- 账户域领域规则有单元测试。
- 注册、登录、列表、状态变更和删除有集成测试。
- Flyway 迁移可在空库执行。
- 不存在旧用户实现的复制代码。
- 文档、数据库、代码和 OpenAPI 契约一致。
