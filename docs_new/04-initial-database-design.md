# DZCOM 初版数据库设计

本文档定义 DZCOM 当前唯一有效的数据库基线。实现业务、编写 MyBatis Mapper XML 和评审迁移脚本时，必须同时以本文档和 `src/main/resources/db/migration/` 下的 V1 至 V6 脚本为准。

## 1. 设计目标

- 按账户、产品行情、组合订单、风控审计、AI 系统五个领域拆分表结构。
- 领域之间只通过稳定的 `biz_id` 进行逻辑引用，不建立数据库外键。
- 避免级联更新和级联删除，让每个领域独立维护生命周期。
- 高频查询使用明确索引，不为低频或不确定场景提前堆叠索引。
- 关键状态变化可追溯，AI、风控和订单结果保留版本或快照。
- 使用扩展表、JSON 配置和通用目标标识承接可预见变化，但不把核心字段全部塞入 JSON。

## 2. 强制约束

### 2.1 主键与标识

- `id BIGINT UNSIGNED AUTO_INCREMENT` 仅用于数据库内部定位，不出现在外部 API。
- `biz_id CHAR(36)` 是业务唯一标识，必须建立唯一索引。
- 跨表引用只保存对方 `biz_id`，字段命名为 `<对象>_biz_id`。
- 禁止 `FOREIGN KEY`、`REFERENCES`、`ON DELETE CASCADE` 和 `ON UPDATE CASCADE`。
- 逻辑引用字段应根据实际查询路径建立普通索引或联合索引。

### 2.2 数据一致性

- 单领域内需要原子完成的写操作使用本地事务。
- 跨领域一致性使用显式应用用例、领域事件和 `aiw_outbox_event`，不得依赖数据库级联。
- 删除或禁用主对象时，由应用服务显式处理从属数据和会话。
- 定时一致性检查用于发现无主逻辑引用、发布失败事件和异常状态。

### 2.3 字段与审计

- 时间统一使用 `DATETIME(3)`，应用层使用北京时间存储和传输。
- 字符集和排序规则统一使用 `utf8mb4`、`utf8mb4_unicode_ci`。
- 金额、价格、数量使用 `DECIMAL`，禁止使用浮点类型。
- 状态字段使用稳定字符串编码，Java 侧映射为枚举。
- 需要并发控制的聚合使用 `version` 乐观锁字段。
- 密码、令牌、密钥和完整敏感数据禁止写入日志、事件载荷或通用配置。
- 软删除只用于确实需要恢复或审计的主数据；流水、快照和审计记录采用只追加策略。

## 3. 迁移基线

| 迁移文件 | 领域 | 核心职责 |
| --- | --- | --- |
| `V1__account_domain.sql` | 账户 | 用户生命周期、登录标识、凭据、资料、风险档案、角色、偏好 |
| `V2__product_and_market_domain.sql` | 产品与行情 | 产品主数据、扩展属性、行情、资讯及资讯目标 |
| `V3__portfolio_and_order_domain.sql` | 组合与订单 | 组合、估值、持仓、订单、成交、订单事件 |
| `V4__risk_notification_and_audit_domain.sql` | 风控、通知、审计 | 风控规则、检查结果、消息通知、审计日志 |
| `V5__ai_and_system_domain.sql` | AI 与系统 | 模型注册、信号、建议、回测、系统配置、事务发件箱 |
| `V6__role_and_permission_model.sql` | 账户权限 | 角色定义、角色权限映射和系统角色权限初始化 |

这些脚本共同组成新项目的初始基线。基线进入共享或生产环境后禁止原地修改，后续变更必须新增更高版本迁移。

## 4. 账户域

| 表名 | 职责 | 关键约束 |
| --- | --- | --- |
| `aiw_user` | 用户稳定身份和生命周期 | `biz_id`、`user_no` 唯一，状态与软删除独立 |
| `aiw_user_identity` | 用户名、邮箱、手机号、OAuth 等登录标识 | `identity_type + normalized_value` 唯一 |
| `aiw_user_credential` | 密码哈希、算法版本、失败次数和锁定信息 | 一个用户一种凭据类型一条当前记录 |
| `aiw_user_profile` | 昵称、头像、地区等可变资料 | 每个用户一条资料 |
| `aiw_user_risk_profile` | KYC 和投资风险承受能力 | 每个用户一条当前风险档案 |
| `aiw_user_role` | 用户角色分配 | `user_biz_id + role_code` 唯一 |
| `aiw_role` | 角色定义 | `role_code` 唯一，支持启停和系统角色标记 |
| `aiw_role_permission` | 角色权限映射 | `role_code + permission_code` 唯一 |
| `aiw_user_preference` | 用户偏好键值 | `user_biz_id + preference_key` 唯一 |

账户信息拆表的目的不是追求表数量，而是隔离凭据、登录标识、个人资料和风险数据的生命周期。注册、改密、禁用和删除由账户应用服务在同一用例中显式协调这些表。

角色定义、权限映射、会话授权和接口接入规则见
[`06-role-permission-design.md`](06-role-permission-design.md)。

## 5. 产品与行情域

| 表名 | 职责 |
| --- | --- |
| `aiw_product` | 产品稳定主数据、交易属性和生命周期 |
| `aiw_product_attribute` | 低频扩展属性及生效日期 |
| `aiw_market_quote` | 产品行情快照或时间序列 |
| `aiw_news_article` | 新闻、公告和舆情内容 |
| `aiw_news_target` | 资讯与产品、市场或行业对象的逻辑关联 |

行情按 `product_biz_id + quote_time` 查询。数据规模增长后可迁移到 ClickHouse 或时序数据库，业务接口不应依赖 MySQL 特有实现。

## 6. 组合与订单域

| 表名 | 职责 |
| --- | --- |
| `aiw_portfolio` | 组合主数据和生命周期 |
| `aiw_portfolio_valuation` | 组合资产、现金、收益等估值快照 |
| `aiw_position` | 组合内产品持仓 |
| `aiw_order` | 订单意图、幂等键、状态和乐观锁 |
| `aiw_trade_execution` | 订单成交明细 |
| `aiw_order_event` | 订单状态变化和处理记录 |

组合主表不保存持续变化的汇总资产，估值进入快照表。订单和成交分离，部分成交不覆盖订单原始意图。

## 7. 风控、通知与审计域

| 表名 | 职责 |
| --- | --- |
| `aiw_risk_rule` | 可版本化的风控规则和配置 |
| `aiw_risk_check` | 规则执行结果、版本快照和解释 |
| `aiw_notification` | 多渠道消息、发送重试和阅读状态 |
| `aiw_audit_log` | 只追加的关键操作审计记录 |

风控检查保留规则编码和版本快照，不直接依赖当前规则行。审计记录禁止保存密码、令牌、证件全文等敏感数据。

## 8. AI 与系统域

| 表名 | 职责 |
| --- | --- |
| `aiw_ai_model` | 模型编码、版本、配置和评估指标 |
| `aiw_ai_signal` | 模型生成的可解释信号 |
| `aiw_ai_recommendation` | 面向用户或组合的分析建议与风险提示 |
| `aiw_backtest_result` | 回测任务、参数和结果摘要 |
| `aiw_system_config` | 非敏感配置或外部密钥引用 |
| `aiw_outbox_event` | 与业务事务同库提交的待发布领域事件 |

AI 结果必须记录模型版本、生成时间和解释信息。AI 建议属于辅助分析，不直接代替用户下单，也不构成收益承诺。

## 9. 索引规范

- 唯一性由唯一索引兜底，应用层预检查不能替代数据库约束。
- 联合索引字段顺序必须匹配主要过滤条件和排序方式。
- 状态扫描任务通常使用 `status + next_retry_at + created_at`。
- 时间序列通常使用 `对象 biz_id + 时间`。
- 不为低选择性单字段盲目建索引；新增索引需说明对应 SQL。
- 上线前使用真实查询验证执行计划，避免重复索引和全表扫描。

## 10. 开发与验收

- 空库必须能按 V1 至 V6 顺序完整执行。
- 迁移脚本不得出现数据库外键和级联定义。
- 持久化 Entity 只保存标量逻辑 ID，不使用跨领域对象图或级联操作。
- 每个持久化 Entity 必须具有独立 Mapper Java 接口和同名 Mapper XML，SQL 不得写入 Java。
- Repository 查询默认处理软删除条件，历史查询使用明确命名。
- 每次结构变更同步更新本文档、对应领域技能、实体映射和集成测试。
