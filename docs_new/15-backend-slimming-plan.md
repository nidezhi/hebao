# 后端持续瘦身方案

## 1. 目标

本文档作为 DZCOM 后端持续瘦身基线。瘦身目标不是“少代码优先”，而是：

- 保留当前投资闭环主链路。
- 删除已退出默认方案、无代码引用、会误导前端或数据库理解的历史能力。
- 对仍有执行价值但不在默认链路的能力，标记为“执行原语”，不直接删除。
- 将大类拆分控制在可验证的小步迁移中，避免边瘦身边破坏闭环。

当前主链路：

```text
LLM 方向化数据源发现
  -> 候选数据源沉淀
  -> 任务/报告/Prompt/模型配置
  -> 投资报告
  -> Mock 交易
  -> 回测、反馈、风控审计
  -> Skill / Prompt / 模型复盘优化
```

## 2. 瘦身判定规则

### 2.1 可直接删除

满足全部条件即可删除：

- 不在当前默认任务、闭环编排和前端主页面中使用。
- 没有应用层、接口层、测试或配置引用。
- 已被新的主链路替代。
- 删除后 `./mvnw test`、契约测试和开发铁律扫描通过。

### 2.2 只能退役，不直接删

满足任一条件时不能直接删：

- 仍被 LLM 候选作为 `recommendedTaskType` 推荐。
- 仍承担真实数据落库、产品 upsert、行情入库、审计查询等执行能力。
- 数据库表有当前实体、Mapper、仓储或接口引用。
- 前端重构文档仍将其作为可配置高级执行原语。

### 2.3 数据库瘦身规则

数据库瘦身只通过新增 Flyway 迁移，不修改历史迁移。

优先级：

1. `DROP TABLE` 已确认无代码引用、无主链路含义的占位表。
2. 禁用或归档旧默认任务配置。
3. 再考虑字段级瘦身。

不做：

- 不删除账户、产品、行情、报告、Prompt、Skill、Mock、回测、反馈、风控审计主表。
- 不删除 `aiw_outbox_event`、`aiw_audit_log` 等架构能力表，除非明确完成替代方案。
- 不物理删除业务数据清库脚本之外的正式业务数据。

## 3. 本轮已执行瘦身

### 3.1 删除 RSS/fallback 资讯采集链路

删除原因：

- `INVESTMENT_NEWS_COLLECTION` 已退出默认闭环。
- RSS/fallback 方案与当前“LLM 按采集方向整理生成数据源”主方案冲突。
- 代码引用只剩自身处理器和测试。
- 新闻/研报方向已由 `llm-news-research-collection` + `DATA_COLLECTION_NEWS_RESEARCH` 承担发现和治理。

已删除：

| 文件 | 说明 |
| --- | --- |
| `InvestmentNewsCollectionTaskHandler.java` | RSS/Atom 与 fallback 资讯采集处理器 |
| `InvestmentNewsFeedClient.java` | RSS/Atom 客户端端口 |
| `RssInvestmentNewsFeedClient.java` | RSS/Atom XML 解析实现 |
| `InvestmentNewsCollectionTaskHandlerTest.java` | 已删除链路测试 |

同步清理：

- OpenAPI 示例中的 `INVESTMENT_NEWS_COLLECTION` 改为 `AI_DATA_SOURCE_DISCOVERY`。
- OpenAPI 示例中的 `OFFICIAL_RSS` 改为 `CNINFO`。

### 3.2 删除未引用通用工具类

删除原因：

- 未被当前代码引用。
- 与项目当前 DDD 分层无直接关系。
- 保留会鼓励绕过应用服务和结构化组件。

已删除：

| 文件 | 说明 |
| --- | --- |
| `ExcelUtil.java` | 未引用 Excel 工具 |
| `JsonUtil.java` | 未引用 JSON 工具 |
| `RedisUtil.java` | 未引用 Redis 工具 |

### 3.3 退役旧 AI 信号与建议占位表

新增迁移：

```text
src/main/resources/db/migration/V24__slim_deprecated_ai_signal_recommendation_tables.sql
```

退役表：

| 表 | 原定位 | 替代链路 |
| --- | --- | --- |
| `aiw_ai_signal` | 早期 AI 信号占位 | `aiw_investment_analysis_report`、Prompt 快照、Mock 交易、风控审计 |
| `aiw_ai_recommendation` | 早期 AI 建议占位 | 投资报告、Mock 订单、反馈、回测、Prompt 评估 |

删除原因：

- 没有 Entity、Mapper、Repository、Service、Controller 引用。
- 当前闭环已经形成更完整的报告、方案、Mock、回测和反馈链路。
- 保留会让前端误以为还存在独立“信号/建议”业务线。

## 4. 本轮明确不删除

| 对象 | 原因 |
| --- | --- |
| `OfficialDisclosureCollectionTaskHandler` | 虽默认停用，但仍是 LLM 候选审核后的执行原语 |
| `HttpOfficialDisclosureClient` | 支撑官方披露、公告、产品净值类结构化采集 |
| `WEALTH_PRODUCT_NAV_REFRESH` | 已支持产品池 upsert 和净值入行情表 |
| `REGULATORY_DISCLOSURE_COLLECTION` | 可作为监管来源候选审核后的执行器 |
| `EXCHANGE_ANNOUNCEMENT_COLLECTION` | 可作为公告来源候选审核后的执行器 |
| `aiw_risk_check` | 当前风控审计查询接口正在使用 |
| `aiw_risk_rule` | 风控规则版本化预留，后续可接入规则配置 |
| `aiw_notification` | 通知能力尚未接入，不影响核心闭环，暂不删 |
| `aiw_audit_log` | 全局审计预留，和投资风控审计互补，暂不删 |
| `aiw_system_config` | 非敏感配置预留，后续可承接系统配置 |
| `aiw_outbox_event` | 事务发件箱预留，不做本轮删除 |

## 5. 下一批代码瘦身路线

### 5.1 Mock 组合服务拆分

当前 `MockPortfolioApplicationService` 约 1590 行，是最大胖点。

拆分顺序：

| 新组件 | 职责 |
| --- | --- |
| `MockTradeRiskGuard` | 产品可 Mock、现金、持仓、质量门禁、风险匹配 |
| `MockPortfolioValuationService` | 初始估值、成交后估值、刷新估值、收益曲线 |
| `MockOrderExecutionService` | 买入、卖出、成交生成 |
| `MockRebalanceService` | 目标权重校验和调仓执行 |
| `MockOrderEventAppender` | 订单事件统一写入 |

验收：

- `MockPortfolioApplicationService` 降到 600 行以内。
- Mock 买入、卖出、撤单、再平衡、报告转计划测试全部通过。
- Controller 不新增业务规则。

### 5.2 投资分析 Provider 拆分

当前 `LocalRuleInvestmentAnalysisProvider` 同时做数据上下文、质量门禁、报告组装和图表组装。

拆分顺序：

| 新组件 | 职责 |
| --- | --- |
| `InvestmentAnalysisContextLoader` | 查询快照、资讯、产品关系 |
| `InvestmentDataQualityGateService` | 数据质量门禁和降级原因 |
| `InvestmentReportPayloadBuilder` | summary/trend/plan/simulatedReturn/chartPayload |
| `InvestmentPromptSnapshotBuilder` | promptSnapshot 和输入快照脱敏 |

验收：

- Provider 降到 400 行以内。
- 报告质量门禁结果与现有测试一致。

### 5.3 数据源治理服务拆分

当前 `DataSourceGovernanceApplicationService` 因 LLM 数据源发现扩展到约 850 行。

拆分顺序：

| 新组件 | 职责 |
| --- | --- |
| `DataSourceDiscoverySkillResolver` | 按采集方向选择 Skill |
| `DataSourceDiscoveryPromptBuilder` | 构建系统提示词和用户提示词 |
| `DataSourceDiscoveryModelParser` | 解析模型 JSON 候选 |
| `DiscoveredDataSourceRegistrar` | 候选沉淀、启用边界、已有数据源保护 |

验收：

- 应用服务保留保存、查询和编排入口。
- 发现候选字段、任务沉淀行为和质量策略不变。

### 5.4 闭环服务拆分

`InvestmentClosedLoopApplicationService` 可拆：

| 新组件 | 职责 |
| --- | --- |
| `BacktestApplicationService` | 回测保存、列表、详情、组合生成回测 |
| `InvestmentFeedbackApplicationService` | 反馈保存、详情、列表 |
| `PromptEvaluationApplicationService` | Prompt 评估保存、列表、详情 |
| `ClosedLoopVisibilityGuard` | 用户可见性校验 |

### 5.5 测试夹具瘦身

建立：

```text
src/test/java/com/example/dzcom/testsupport/
```

优先抽取：

- `FixedIdGenerator`
- `FixedClockProvider`
- `InMemoryPortfolioStore`
- `InMemoryBacktestStore`
- `PassThroughOperatorProvider`

## 6. 下一批数据库瘦身候选

| 对象 | 当前判断 | 后续动作 |
| --- | --- | --- |
| `aiw_notification` | 当前无代码引用 | 前端通知能力确认不要后可退役 |
| `aiw_audit_log` | 当前无代码引用 | 若统一使用 `aiw_risk_check` 和闭环步骤审计，可退役 |
| `aiw_system_config` | 当前无代码引用 | 若模型/任务配置完全走专用表，可退役 |
| `aiw_outbox_event` | 当前无代码引用 | 若短期不做领域事件可靠发布，可退役 |
| `aiw_risk_rule` | 当前无代码引用 | 若风控规则继续由代码内置，可退役或降级为后续规则中心 |

执行前必须再次跑：

```text
rg -n "<table_or_domain_name>" src/main/java src/main/resources src/test/java docs_new
```

## 7. 每轮瘦身验收清单

- `./mvnw -q -DskipTests compile`
- `./mvnw -q -Dtest=DocumentationContractTest test`
- `./mvnw -q test`
- 开发铁律扫描：

```text
rg -n "@GetMapping|@PutMapping|@DeleteMapping|@PatchMapping|@RequestParam|@PathVariable|@Select\\(|@Insert\\(|@Update\\(|@Delete\\(|QueryWrapper|BaseMapper|JpaRepository|@Entity\\b|JdbcTemplate|NamedParameterJdbcTemplate" -S src/main/java
```

- `git diff --check`
- 文档同步更新 `docs_new/12`、`docs_new/15`，投资闭环影响同步更新 `docs_new/20`。
