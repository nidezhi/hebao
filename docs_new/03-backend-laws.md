# 03 Backend Laws

生成日期：2026-06-28

本文是 dzcom 后端开发铁律。若与旧开发说明冲突，先更新本文或对应 contract，再改代码。

## 最新结论区

### API 契约

- API 契约必须稳定，字段含义明确，命名一致，避免同一概念多套字段。
- 错误码、错误消息和错误上下文必须可被前端结构化展示。
- 业务失败、权限失败、风控拒绝、数据不足、幂等冲突不能都压成普通 500。
- 涉及跨前后端变更时，必须同步更新 `00-current-handoff.md`、`04-api-contract-rules.md` 和必要的 gap list。

### 查询与选择器

- 所有前端对象选择器需要的实体必须提供列表/搜索/分页/排序能力。
- 选择器接口至少应返回 id、displayName 或 name/code、状态、摘要字段和必要过滤项。
- 不允许只提供详情接口，要求前端手填 id。
- 对权限受限对象，接口应返回空结果或明确 403 上下文，不泄漏敏感信息。

### DTO 与关联对象

- DTO 不能只返回裸 id；涉及关联对象时，应提供 display 字段、摘要字段，或提供可查询接口。
- 关联对象字段应稳定，例如 `productBizId` + `productName` + `productCode` + `productStatus`。
- 状态、枚举、权限、风控拒绝原因必须可被前端结构化展示。
- 大 JSON 字段若属于核心体验，后端应提供摘要、schema 或解析后的稳定结构。

### 数据、权限与幂等

- 数据库变更必须通过 Flyway migration，同步考虑默认值、索引、回滚风险和兼容旧数据。
- 创建、触发、执行类接口必须考虑幂等、重复提交和状态机边界。
- 权限边界必须在后端校验，前端展示不能替代后端授权。
- 高风险业务必须保留审计字段、操作人、操作时间、拒绝原因或失败上下文。

### Mapper 与 SQL 生成

- Mapper 层非必要不得新增手写 SQL、XML SQL 或生成重复 CRUD SQL；优先使用 MyBatis-Plus 的 `BaseMapper`、Service、LambdaQueryWrapper、分页和规范代码生成方式。
- 新增实体、Mapper、Service 时应遵循 MyBatis-Plus 代码生成规范，保持 Entity/Mapper/Repository/Service 分层一致，不为简单增删改查额外写 XML。
- 允许手写 SQL 的场景必须有明确理由：复杂聚合、跨表报表查询、批量 upsert、数据库特性、性能瓶颈、锁语义或 MyBatis-Plus 难以表达的查询。
- 手写 SQL 必须局部化在 Mapper/XML 层，方法名表达业务语义，参数/返回 DTO 明确，并补服务测试或 mapper 相关验证；不能把裸 `Map`、拼接 SQL 或前端展示字段漂移带到上层。
- Flyway migration 只负责 schema、索引、种子数据和必要数据修复；不得把可由 MyBatis-Plus 运行时代码完成的普通业务写入长期 SQL 脚本。

### 测试与验证

- 后端变更必须补对应服务测试、Controller 契约测试或关键链路测试。
- 修改 Request/DTO/Controller 时，必须评估前端类型、adapter 和页面影响。
- 完成后至少执行与变更相关的 Maven 验证；如无法执行，写明原因和剩余风险。

## 历史归档区

- 旧后端开发规范见 `01-development-guidelines.md`。
- 旧技术架构见 `03-technical-architecture.md`。
- 旧数据库说明见 `04-initial-database-design.md`。
- 后续默认只读本文最新结论区，除非任务明确要求回溯历史架构。
- 2026-06-29 补充 Mapper 层铁律：非必要不生成 SQL，优先 MyBatis-Plus 规范代码和运行时查询能力。
