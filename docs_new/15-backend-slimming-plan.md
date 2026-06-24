# 后端代码减肥方案

## 1. 文档目的

本文档记录 2026-06-24 对后端代码臃肿度的快速检查结果和后续减肥建议。

本方案不要求立即执行，优先级低于高质量数据源、真实采集器、自动报告和前端重构。但如果继续扩展投资平台，建议按本文档逐步拆分，避免核心服务继续膨胀。

## 2. 快速扫描结果

按 Java 文件行数粗略扫描，当前较重的文件集中在：

| 文件 | 约行数 | 问题类型 |
| --- | ---: | --- |
| `MockPortfolioApplicationService.java` | 1537 | 交易、持仓、估值、风控、订单事件集中在一个应用服务 |
| `LocalRuleInvestmentAnalysisProvider.java` | 770 | 数据查询、质量门禁、报告组装、图表组装集中 |
| `InvestmentClosedLoopApplicationService.java` | 712 | 回测、反馈、Prompt 评估和权限校验集中 |
| `MockPortfolioApplicationServiceTest.java` | 560 | 内存仓储夹具过多，测试维护成本高 |
| `NewsHeatAggregationTaskHandler.java` | 464 | 热度统计、关联构建、质量评分集中 |
| `ProductApplicationService.java` | 439 | 产品主档、属性、投资画像维护集中 |
| `AiPromptApplicationService.java` | 406 | Prompt 模板、变量、Schema、预览集中 |
| `DataSourceGovernanceApplicationService.java` | 376 | 数据源、健康、质量快照编排集中 |

## 3. 优先减肥方向

### 3.1 Mock 组合服务拆分

当前问题：

- 买入、卖出、撤单、再平衡、估值、收益曲线和风控拒绝都在一个服务中。
- 交易规则变多后，单类继续增长风险很高。

建议拆分：

| 新组件 | 职责 |
| --- | --- |
| `MockOrderExecutionService` | 买入、卖出和成交生成 |
| `MockPortfolioValuationService` | 初始估值、成交后估值、刷新估值、收益曲线 |
| `MockRebalanceService` | 目标权重校验和调仓执行 |
| `MockTradeRiskGuard` | 产品可 Mock、现金、持仓、质量门禁等风控前置 |
| `MockOrderEventAppender` | 订单事件统一写入 |

优先级：高。

### 3.2 投资分析 Provider 拆分

当前问题：

- `LocalRuleInvestmentAnalysisProvider` 同时做数据上下文构建、质量门禁、投资计划、模拟收益和图表 payload。
- 后续真实模型、报告解析和 JSON Schema 校验会继续挤进同一层。

建议拆分：

| 新组件 | 职责 |
| --- | --- |
| `InvestmentAnalysisContextLoader` | 查询快照、资讯、产品关系 |
| `InvestmentDataQualityGateService` | 数据质量门禁和降级原因 |
| `InvestmentReportPayloadBuilder` | summary/trend/plan/simulatedReturn/chartPayload 组装 |
| `InvestmentPromptSnapshotBuilder` | promptSnapshot 和输入快照脱敏 |
| `OpenAiReportResponseParser` | 真实大模型 JSON 输出校验和转换 |

优先级：高。

### 3.3 闭环服务拆分

当前问题：

- 回测、反馈、Prompt 评估、自动评分和可见性校验集中在 `InvestmentClosedLoopApplicationService`。

建议拆分：

| 新组件 | 职责 |
| --- | --- |
| `BacktestApplicationService` | 回测保存、列表、详情、组合生成回测 |
| `InvestmentFeedbackApplicationService` | 反馈保存、详情、列表 |
| `PromptEvaluationApplicationService` | Prompt 评估保存、列表、详情 |
| `ClosedLoopVisibilityGuard` | 回测、反馈、评估的用户可见性校验 |
| `PromptEvaluationScoringService` | 反馈和回测评分逻辑 |

优先级：中高。

### 3.4 任务处理器抽公共质量评分

当前问题：

- 收益、动量、资讯热度都有质量分、覆盖率、样本数、波动等类似逻辑。

建议新增：

| 新组件 | 职责 |
| --- | --- |
| `TaskQualityScorer` | 样本分、覆盖率、质量等级 |
| `ThemeSnapshotMetricsWriter` | 快照 metrics JSON 统一序列化 |
| `ThemeConfigParser` | 主题、产品、市场范围解析增强 |

优先级：中。

### 3.5 测试夹具复用

当前问题：

- 多个测试类内置大量 `InMemory*Store`。
- 夹具重复导致测试文件变长，改接口时维护成本高。

建议：

- 建立 `src/test/java/com/example/dzcom/testsupport/`。
- 抽取 `InMemoryPortfolioStore`、`InMemoryBacktestStore`、`FixedIdGenerator`、`FixedClockProvider`。
- 控制每个业务测试类只保留场景数据，不保留完整仓储实现。

优先级：中。

## 4. 不建议立刻做的事情

- 不建议为了减肥引入新框架。
- 不建议拆 MyBatis XML 规范。
- 不建议把领域逻辑下沉到 Controller。
- 不建议把所有 TaskHandler 做成抽象继承层；优先提取小型组合服务。
- 不建议在真实数据源未接入前过度重构数据采集抽象。

## 5. 推荐执行顺序

1. 抽 `MockTradeRiskGuard` 和 `MockPortfolioValuationService`。
2. 抽 `InvestmentDataQualityGateService` 和 `InvestmentReportPayloadBuilder`。
3. 抽 `ClosedLoopVisibilityGuard`。
4. 抽 `testsupport` 内存仓储和固定时钟/ID。
5. 抽任务质量评分公共组件。
6. 最后再考虑更大粒度的应用服务拆分。

## 6. 验收标准

- 最大应用服务类控制在 500 行以内。
- 复杂 Provider 控制在 400 行以内。
- 测试类不内嵌超过 5 个内存仓储。
- 业务规则仍在 application/domain 层，Controller 不变胖。
- `./mvnw -q test` 继续通过。
- 所有接口契约保持 POST + RequestBody。
