# 投资平台闭环改进方案

## 1. 文档目的

本文档重新定义 DZCOM 投资平台的建设目标和闭环路线。

当前系统已经具备账户、产品、行情仓储、配置任务、新闻采集、主题快照、AI 模型配置和投资分析雏形，但距离“可参考的投资辅助平台”仍有明显差距：

- 数据源质量不足，兜底资讯和空样本占比过高。
- 产品、风险、主题、资产类别和可交易规则没有形成稳定投资架构。
- 行情和资讯没有稳定接入官方或专业数据源。
- AI 分析报告缺少高质量输入，不能直接产出有参考价值的投资方案。
- 提示词没有成为可版本化、可前端配置、可评估成长的业务资产。
- Mock 交易、持仓、收益、回测和风控审计没有闭环。

本方案的目标是将项目从“演示型 AI 报告”调整为“可信数据驱动的投资辅助平台”。

## 2. 总体目标

目标平台需要满足四个核心要求：

| 核心要求 | 业务含义 | 系统落点 |
| --- | --- | --- |
| 优秀的产品、风险、投资架构 | 产品池可信，风险画像明确，投资动作有边界 | 产品主档、风险评级、主题映射、资产类别、可交易规则 |
| 高质量数据来源 | 行情、资讯、公告、研报和监管数据可追溯、可评分 | 数据源注册、采集任务、质量评分、数据缺口告警 |
| 不断成长，AI 驱动，前端可控 | 提示词、模型、报告和反馈可版本化演进 | Prompt 版本、模型配置、报告评估、前端配置和回滚 |
| 全程自动化优化成长 | 数据、报告、方案、Mock 交易、回测和风控形成闭环 | 策略执行、模拟交易、收益回测、风控审计、反馈学习 |

## 3. 闭环主链路

```text
产品池
  -> 产品风险与主题映射
  -> 高质量行情 / 资讯 / 公告 / 研报 / 监管数据
  -> 数据清洗、去重、归因、质量评分
  -> 主题快照、产品快照、市场趋势快照
  -> 投资报告
  -> 报告维度提示词版本
  -> 大模型生成具体投资方案
  -> Mock 组合、订单、持仓、成交和估值
  -> 收益、回撤、波动、命中率和风险审计
  -> 前端反馈、人工采纳、拒绝和原因记录
  -> 提示词、模型和策略版本优化
```

禁止跳过“产品池、数据质量、Mock 交易、回测和审计”直接生成正式投资方案。

## 4. 现状问题和修正原则

### 4.1 数据问题

当前问题：

- RSS/Atom 和兜底资讯只能用于链路演示，无法支撑投资决策。
- 大量主题快照样本为空或有效样本覆盖率不足。
- 新闻与产品的关系主要依赖关键词，缺少官方公告、行业分类和产品成分映射。

修正原则：

- 低质量数据不进入正式投资方案，只进入数据缺口报告。
- 每个数据源必须有来源等级、质量分、更新频率、失败率和最近成功采集时间。
- 前端必须能看到数据质量，不允许隐藏样本不足问题。

### 4.2 产品问题

当前问题：

- 产品只停留在基础目录，缺少投资语义。
- 主题和产品只是字符串配置，没有正式关系。
- 产品风险、资产类别、交易规则、费用、流动性没有支撑 Mock 交易。

修正原则：

- 产品必须成为投资分析的核心，不允许只用主题关键词生成投资方案。
- 产品必须具备资产类别、风险等级、交易状态、最小交易单位、估值规则和费用规则。
- 主题、行业、指数、产品成分必须显式建模。

### 4.3 AI 问题

当前问题：

- OpenAI 兼容 Provider 当前只是 Mock，没有真实调用 API。
- 即使接入真实大模型，低质量输入也会生成低价值输出。
- Prompt 没有版本、评估、回滚和前端配置能力。

修正原则：

- 正式大模型调用暂缓。
- Prompt 必须先成为可版本化数据资产。
- AI 输出必须受 JSON Schema、数据质量阈值、风控规则和 Mock 回测约束。

### 4.4 交易模拟问题

当前问题：

- 没有 Mock 账户、组合、持仓、订单、成交和估值闭环。
- 投资报告不能转化为可验证的模拟方案。

修正原则：

- 投资方案必须能生成 Mock 交易计划。
- Mock 交易必须能形成持仓、估值、收益和回撤。
- 每个方案必须可回测、可审计、可复盘。

## 5. 目标业务域

| 业务域 | 核心职责 | 当前状态 | 目标状态 |
| --- | --- | --- | --- |
| 产品池 | 产品主档、风险、资产类别、交易规则 | 基础产品目录 | 投资语义完整 |
| 数据源治理 | 数据源注册、质量评分、采集审计 | 部分 RSS/兜底 | 官方和专业数据源优先 |
| 行情中心 | 实时/历史行情、净值、收益率 | 仓储能力 | 稳定采集和查询接口 |
| 资讯公告 | 新闻、公告、研报、监管数据 | 资讯表和关联表 | 多源分层和质量门禁 |
| 主题归因 | 主题、行业、指数、产品关系 | 参数配置 | 显式关系表和版本 |
| 投资报告 | 趋势、风险、摘要、图表 | 本地规则雏形 | 数据质量驱动报告 |
| Prompt 资产 | 提示词模板、版本、变量和输出 Schema | 未落地 | 前端可配置和回滚 |
| AI 方案 | 调用模型生成投资方案 | Mock Provider | 质量达标后真实调用 |
| Mock 交易 | 账户、组合、订单、持仓、收益 | 未闭环 | 可模拟、可回测、可审计 |
| 风控审计 | 风险规则、命中、通知和复盘 | 表结构/规划 | 全链路强制约束 |

## 6. 高质量数据来源规划

### 6.1 数据源等级

| 等级 | 来源类型 | 示例 | 用途 | 质量策略 |
| --- | --- | --- | --- | --- |
| L1 | 官方监管和交易所 | 证监会、上交所、深交所、北交所、巨潮资讯 | 公告、监管、披露、产品事件 | 默认高可信 |
| L2 | 官方产品与净值披露 | 中国理财网、基金公司、银行理财公告 | 理财产品、基金、净值 | 高可信但需结构化 |
| L3 | 专业行情和资讯供应商 | Wind、同花顺 iFinD、Choice、财联社 | 行情、研报、资金流、新闻 | 需要供应商接入配置 |
| L4 | 主流财经媒体 | 东方财富、证券时报、中国证券报、上海证券报 | 新闻热度、事件跟踪 | 需要去重和交叉验证 |
| L5 | 兜底和演示数据 | fallbackArticles、本地 mock quote | 本地演示和链路验证 | 禁止进入正式方案 |

### 6.2 数据源实体目标

需要新增或完善的数据源对象：

| 对象 | 关键字段 |
| --- | --- |
| `DataSource` | sourceCode、sourceName、sourceType、trustLevel、baseUrl、enabled、fetchFrequency、owner |
| `DataSourceHealth` | sourceCode、lastSuccessAt、lastFailureAt、successRate、avgLatency、failureReason |
| `DataIngestionJob` | jobCode、sourceCode、dataType、status、startedAt、completedAt、sampleCount |
| `DataQualitySnapshot` | sourceCode、dataType、qualityScore、missingRate、duplicateRate、freshnessScore |

### 6.3 数据质量门禁

| 门禁 | 阈值 | 动作 |
| --- | --- | --- |
| 样本覆盖率 | `< 0.6` | 不生成正式投资方案，只生成数据缺口 |
| 数据质量分 | `< 0.45` | 报告标记 `LOW_CONFIDENCE` |
| 兜底数据占比 | `> 0.2` | 不允许输出积极配置建议 |
| 来源数量 | `< 2` | 新闻热度只能作为弱信号 |
| 行情新鲜度 | 超过配置周期 | 暂停交易模拟 |

## 7. 产品、风险和投资架构

### 7.1 产品池目标模型

| 模型 | 说明 |
| --- | --- |
| `Product` | 产品主档，表示股票、ETF、基金、债券、银行理财、黄金、REITs 等 |
| `ProductRiskProfile` | 产品风险、波动、最大回撤、流动性、适合风险等级 |
| `ProductTradingRule` | 最小金额、数量步长、费用、交易时间、是否允许 Mock 交易 |
| `ProductThemeRelation` | 产品与主题、行业、指数、资产类别的关系 |
| `ProductValuationSnapshot` | 产品估值、净值、价格、收益率快照 |

### 7.2 产品必须支持的投资语义

| 语义 | 用途 |
| --- | --- |
| 资产类别 | 股票、基金、ETF、银行理财、债券、黄金等 |
| 市场范围 | 中国大陆、港股、美股等；当前优先中国大陆 |
| 风险等级 | 用户适配和风控 |
| 流动性 | Mock 交易可执行性 |
| 收益来源 | 价格、净值、利息、分红 |
| 交易规则 | 订单生成和成交模拟 |

## 8. Prompt 版本化闭环

### 8.1 Prompt 不是代码常量

投资报告生成后的“提示词”必须作为业务数据管理，而不是写死在代码里。

目标对象：

| 对象 | 关键字段 |
| --- | --- |
| `AiPromptTemplate` | promptCode、promptVersion、scenario、status、templateContent |
| `AiPromptVariable` | promptCode、variableName、sourcePath、required、description |
| `AiPromptOutputSchema` | promptCode、schemaJson、schemaVersion |
| `AiPromptEvaluation` | promptCode、promptVersion、score、backtestResultId、reviewStatus |

### 8.2 Prompt 生命周期

```text
DRAFT
  -> VALIDATING
  -> ACTIVE
  -> RETIRED
```

### 8.3 前端可控能力

前端后台必须支持：

- 查看 Prompt 模板。
- 查看变量来源。
- 预览某次投资报告生成的最终 Prompt。
- 新建 Prompt 版本。
- 启用、停用、回滚 Prompt。
- 查看 Prompt 对应报告、方案、Mock 交易和回测结果。

## 9. 投资报告闭环

### 9.1 报告生成输入

| 输入 | 来源 |
| --- | --- |
| 产品池 | 产品主档、风险、交易规则 |
| 行情 | 历史价格、净值、收益率、波动 |
| 资讯公告 | 新闻、公告、研报、监管事件 |
| 主题归因 | 产品与主题、行业、指数关系 |
| 用户画像 | 风险等级、投资偏好、持仓情况 |
| 数据质量 | 来源质量、样本覆盖率、新鲜度 |

### 9.2 报告输出

| 输出 | 前端展示 |
| --- | --- |
| 投资信息汇总 | 摘要卡片 |
| 趋势判断 | 趋势标签和趋势图 |
| 数据质量 | 质量分和数据缺口 |
| 风险解释 | 风险提示和适配性 |
| Prompt 快照 | 后台可预览 |
| 投资方案草案 | 等待 AI 或规则生成 |
| 图表数据 | 收益、热度、动量和事件标记 |

### 9.3 报告降级策略

| 条件 | 输出 |
| --- | --- |
| 数据质量低 | 数据缺口报告 |
| 产品风险缺失 | 不输出配置比例 |
| 行情缺失 | 不做收益模拟 |
| 新闻单源 | 热度标记为弱信号 |
| 用户风险画像缺失 | 输出通用报告，不输出个性化建议 |

## 10. Mock 交易闭环

### 10.1 必须落地的对象

| 对象 | 说明 |
| --- | --- |
| `MockAccount` | 用户模拟账户 |
| `MockPortfolio` | 模拟组合 |
| `MockPosition` | 模拟持仓 |
| `MockOrder` | 模拟订单 |
| `MockTrade` | 模拟成交 |
| `MockValuationSnapshot` | 组合估值快照 |
| `MockPerformanceSnapshot` | 收益、回撤、波动和胜率 |
| `MockStrategyExecution` | 投资方案执行记录 |

### 10.2 Mock 交易链路

```text
投资报告
  -> 投资方案
  -> 风控预检
  -> Mock 订单
  -> Mock 成交
  -> 持仓变化
  -> 估值快照
  -> 收益回测
  -> 风控审计
  -> 反馈学习
```

### 10.3 Mock 交易验收

| 验收项 | 说明 |
| --- | --- |
| 订单可生成 | 投资方案能转换为买入、卖出、调仓订单 |
| 持仓可变化 | 成交后持仓数量、成本、现金变化正确 |
| 收益可计算 | 能计算日收益、累计收益、最大回撤 |
| 风险可拦截 | 超风险等级、超仓位、流动性不足必须拦截 |
| 审计可追踪 | 每个订单能回溯到报告、Prompt、模型和数据版本 |

## 11. 前端后台和用户端边界

### 11.1 后台复杂

后台面向运营、研究、管理员和开发：

| 页面 | 能力 |
| --- | --- |
| 数据源看板 | 来源状态、质量分、失败率、最近采集 |
| 产品池管理 | 产品、风险、交易规则、主题关系 |
| 主题归因管理 | 主题、关键词、产品映射、版本 |
| 投资报告后台 | 报告输入、输出、数据质量和缺口 |
| Prompt 管理 | 模板、变量、版本、预览、回滚 |
| Mock 交易后台 | 组合、订单、持仓、收益和回测 |
| 风控审计 | 风险命中、拦截原因、人工复核 |

### 11.2 用户端简洁

用户端只展示明确结果：

| 页面 | 展示 |
| --- | --- |
| 投资机会 | 方向、风险、数据质量、摘要 |
| 投资报告 | 趋势、依据、风险、建议 |
| 模拟方案 | 配置比例、模拟收益、压力情景 |
| 我的模拟组合 | 持仓、收益、回撤、调仓记录 |
| 风险提示 | 不适配原因和注意事项 |

## 12. 分阶段交付计划

### 阶段 0：止损与标记

目标：停止把低质量数据包装成正式投资方案。

交付：

- 报告增加 `confidenceLevel` 和 `dataQualityGate`。
- 数据不达标时只输出数据缺口。
- Mock Provider 明确标记为链路验证。
- 前端显示“低可信度”和“数据不足”状态。

当前落地状态：

| 能力 | 状态 | 前端出口 |
| --- | --- | --- |
| 报告可信等级 `confidenceLevel` | 已落地 | `/api/investment/analysis/generate`、`/api/investment/analysis/reports/list` |
| 报告数据质量分 `dataQualityScore` | 已落地 | 报告列表页和详情页 |
| 数据质量门禁 `dataQualityGate` | 已落地 | 报告详情页降级原因、允许动作和提示文案 |
| 低质量数据降级 | 已落地 | `investmentPlan.planType=DATA_GAP_REPORT` |
| 禁止低质量配置建议 | 已落地 | `referenceAllocationRate=0`、`allowedActions` 不含 `MOCK_TRADE` |
| 风控拦截审计查询 | 已落地 | `/api/risk/checks/list` |

`dataQualityGate` 结构：

```json
{
  "passed": false,
  "confidenceLevel": "LOW_CONFIDENCE",
  "dataQualityScore": 0.35,
  "snapshotCount": 3,
  "newsCount": 1,
  "fallbackNewsRatio": 0.5,
  "reasons": ["LOW_DATA_QUALITY"],
  "displayMessage": "数据质量不足，当前仅展示数据缺口，不生成投资配置建议。",
  "allowedActions": ["VIEW_REPORT", "SHOW_DATA_GAP"]
}
```

验收：

- 空样本不会输出积极配置建议。
- 兜底资讯占比过高时报告降级。

### 阶段 1：产品池和风险架构

目标：产品具备投资语义和 Mock 交易基础。

交付：

- 产品风险画像。
- 产品交易规则。
- 产品主题关系。
- 产品资产类别和市场范围。
- 产品后台管理接口。

当前落地状态：

| 能力 | 状态 | 前端出口 |
| --- | --- | --- |
| 产品投资风险和交易画像表 `aiw_product_investment_profile` | 已落地 | `/api/admin/products/investment-profile/save`、`/api/products/detail` |
| 产品主题关系表 `aiw_product_theme_relation` | 已落地 | `/api/admin/products/investment-profile/save`、`/api/products/detail` |
| 资产类别 `assetClass` | 已落地 | 产品详情 `investmentProfile.assetClass` |
| 风险摘要 `riskSummary` | 已落地 | 产品详情风险说明 |
| 波动等级和流动性等级 | 已落地 | 产品详情风险标签 |
| Mock 交易开关 `mockTradable` | 已落地 | 产品详情和后续 Mock 交易入口判断 |
| 主题、行业、指数、资产类别关系 | 已落地 | 产品详情 `themeRelations` |

前端必须展示：

- 产品详情页展示 `investmentProfile`。
- 产品详情页展示 `themeRelations`。
- `mockTradable=false` 时，不显示 Mock 买入或加入模拟组合入口。
- `dataQualityScore` 低时，提示“产品画像质量不足”。

验收：

- 任一投资方案都能说明涉及哪些产品、产品风险和交易约束。

### 阶段 2：数据源治理

目标：建立可信数据源体系。

交付：

- 数据源注册表：已落地，接口为 `/api/admin/data-sources/save`、`/api/admin/data-sources/list`。
- 数据源健康状态：已落地，接口为 `/api/admin/data-sources/health/save`，列表响应返回 `health`。
- 数据质量快照：已落地，接口为 `/api/admin/data-sources/quality/save`、`/api/admin/data-sources/quality/list`，列表响应返回 `latestQuality`、`qualityLevel`、`displayMessage`。
- 采集任务审计。
- 官方和专业来源接入预留接口。

验收：

- 前端可看到每个数据源质量和失败原因；当前已通过数据源看板接口落地。
- 数据源失败不会静默吞掉。

### 阶段 3：行情和资讯质量提升

目标：让快照数据具备参考价值。

交付：

- 行情采集接口和查询接口。
- 公告、研报、新闻多源采集。
- 新闻、产品、主题、公告显式关联。
- 快照质量门禁。

验收：

- 主题收益快照有效样本覆盖率达到阈值。
- 新闻热度至少两个来源交叉验证。

### 阶段 4：Prompt 版本化

目标：让提示词成为可成长资产。

交付：

- Prompt 模板表：已落地，表为 `aiw_ai_prompt_template`。
- Prompt 变量定义：已落地，表为 `aiw_ai_prompt_variable`。
- Prompt 输出 Schema：已落地，表为 `aiw_ai_prompt_output_schema`。
- Prompt 版本管理接口：已落地，接口为 `/api/ai/prompts/save`、`/api/ai/prompts/list`、`/api/ai/prompts/detail`、`/api/ai/prompts/status`。
- Prompt 预览接口：已落地，接口为 `/api/ai/prompts/preview`，仅做本地变量替换和必填校验，不触发真实模型调用。
- Prompt 评估和回测反哺：已落地，接口为 `/api/ai/prompt-evaluations/save`、`/api/ai/prompt-evaluations/list`、`/api/ai/feedback/save`、`/api/backtests/generate-from-portfolio`。

验收：

- 前端已可查看 Prompt 模板、变量、输出 Schema 和本地渲染后的最终 Prompt。
- Prompt 已可通过状态变更完成启用、停用和回滚候选管理。
- 报告、Prompt、Mock 结果、回测结果和用户反馈之间已具备可保存、可查询、可评分的复盘链路；真实模型调用结果表仍待接入真实 Provider 后细化。

### 阶段 5：Mock 交易闭环

目标：投资方案可被验证。

交付：

- Mock 账户。
- Mock 组合：基础创建、列表、详情已落地，接口为 `/api/mock/portfolios/create`、`/api/mock/portfolios/mine`、`/api/mock/portfolios/detail`。
- Mock 订单：金额买入已落地，接口为 `/api/mock/portfolios/orders/buy`；数量卖出已落地，接口为 `/api/mock/portfolios/orders/sell`；撤单边界已落地，接口为 `/api/mock/portfolios/orders/cancel`；订单事件审计已落地，接口为 `/api/mock/portfolios/orders/events`；报告转模拟买入已落地，接口为 `/api/mock/portfolios/orders/buy-from-report`；目标权重再平衡已落地，接口为 `/api/mock/portfolios/rebalance/execute`。
- Mock 成交：买入和卖出订单按最新行情立即成交。
- Mock 持仓：买入成交后更新多头持仓，卖出成交后按平均成本法扣减持仓。
- 估值和收益快照：买入成交后生成新估值快照；估值刷新和收益曲线已落地，接口为 `/api/mock/portfolios/valuations/refresh`、`/api/mock/portfolios/performance/curve`。
- 回测结果。

验收：

- 当前已完成模拟组合容器、初始现金估值、手动模拟买入、手动模拟卖出、撤单边界、订单事件审计、目标权重再平衡、报告转模拟买入、估值刷新和收益曲线，前端可以展示“我的模拟组合”、组合详情、订单成交结果、订单状态追踪、持仓变化、调仓结果、累计收益率和最大回撤。
- 一个投资方案或调仓目标能生成订单、成交、持仓和收益曲线。
- 关键风控拦截已有审计记录和查询接口，覆盖报告质量不足、数据门禁未通过、现金不足、持仓不足、产品不可 Mock、调仓目标非法等原因。

### 阶段 6：AI 方案和自动优化

目标：在可信数据和 Mock 交易基础上接入真实模型。

交付：

- 真实 OpenAI 兼容 Provider。
- Prompt + JSON Schema 约束输出。
- AI 投资方案表。
- 推荐采纳、拒绝和反馈。
- 回测结果反哺 Prompt 评分。
- 回测结果：已落地，支持外部结果保存、Mock 组合估值生成摘要、详情和列表。
- 推荐采纳、拒绝和反馈：已落地，支持反馈保存、详情、列表，并自动生成 Prompt 评估记录。

验收：

- AI 输出必须通过 Schema 校验。
- 方案必须通过风控和 Mock 回测后才能展示给用户。

## 13. 里程碑闭环验收

| 里程碑 | 闭环标准 |
| --- | --- |
| M1 产品可信 | 产品、风险、主题、交易规则可查可配 |
| M2 数据可信 | 数据源质量、采集审计、样本覆盖可前端展示 |
| M3 报告可信 | 报告能解释数据来源、质量和缺口 |
| M4 Prompt 可成长 | Prompt 可版本、预览、启用、回滚、评估 |
| M5 交易可验证 | 方案能生成 Mock 交易和收益回测 |
| M6 AI 可控 | AI 输出受 Schema、风控、回测和审计约束 |

## 14. 当前立即执行的优先级

1. 暂停正式大模型调用开发。
2. 给现有报告增加数据质量门禁和降级输出。
3. 补产品风险画像、交易规则和主题关系。
4. 补数据源注册、质量快照和采集审计。
5. 补 Mock 账户、组合、订单、持仓、成交和估值。
6. 建 Prompt 模板、变量、输出 Schema 和版本管理。
7. 补回测、风控审计、Prompt 评估和用户采纳/拒绝反馈。
8. 最后再接真实 OpenAI 兼容 Provider。

## 15. 开发铁律补充

投资平台相关开发必须满足：

- 没有高质量数据，不输出正式投资建议。
- 没有产品风险和交易规则，不生成配置比例。
- 没有 Mock 交易和回测，不宣称策略有效。
- 没有 Prompt 版本，不允许不可追踪的大模型输出。
- 没有前端可见出口，后端改进视为未完成。
- 每个投资方案必须能追溯到数据源、产品、报告、Prompt、模型、风控和 Mock 结果。

## 16. 2026-06-24 数据补全和自动报告更新

本轮已执行高质量数据补全任务重置：

- 通过 `V17__reset_quality_tasks_and_openai_default.sql` 停用历史任务并初始化新任务。
- 新增 L1/L2/L3 数据源注册和健康占位。
- 新增自动投资报告生成任务 `AUTO_INVESTMENT_REPORT_GENERATION`。
- 默认分析模型切换为 `openai-compatible-analysis`。
- OpenAI 兼容 Provider 支持 `mockEnabled=false` 时调用真实 OpenAI 兼容接口。
- L1/L2 采集任务默认 `fallbackEnabled=false`，禁止将兜底样本写作正式数据。
- 新增第一版官方专用采集器，支持 `REGULATORY_DISCLOSURE_COLLECTION`、`EXCHANGE_ANNOUNCEMENT_COLLECTION`、`WEALTH_PRODUCT_NAV_REFRESH`。
- 专用采集器支持前端配置端点、JSON/HTML 格式、字段路径、关键词过滤、超时和新鲜度窗口，并自动写入数据源健康和质量快照。
- `WEALTH_PRODUCT_NAV_REFRESH` 已支持基于结构化字段同步银行理财产品主档，并将 `nav` 写入 `aiw_market_quote` 形成净值行情。

仍需继续补齐：官方接口内置模板、理财产品属性明细、Wind/Choice 授权行情和周期性数据质量审计。
