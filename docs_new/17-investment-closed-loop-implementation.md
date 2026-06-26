# 投资平台闭环实现评估与前端对接说明

## 1. 结论

截至 2026-06-25，后端已经具备从数据源治理、采集编排、投资报告、Prompt 治理、模型候选、Mock 交易、回测反馈到驾驶舱展示的数据闭环支撑，并新增了全自动闭环总编排任务。

闭环不是“全部自动产生真实数据”的状态，而是“后端链路、任务、表结构、接口、质量门禁和自动编排已经打通”。清库后不会注入假业务数据，产品、行情、资讯、报告、Mock、回测和反馈需要通过真实采集任务、前端配置、用户操作和模型配置逐步产生。

自动化边界：

- Mock 投资闭环可以由 `AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION` 全自动执行。
- 真实数据采集可以由可配置任务全自动执行，端点和字段路径由前端或环境配置维护。
- Prompt/模型优化可以自动产出候选和评分。
- 正式启用新 Prompt、新模型、真实交易不会自动执行，必须保留人工确认或灰度开关。

当前状态判断：

| 闭环节点 | 实现状态 | 说明 |
| --- | --- | --- |
| 数据源治理 | 已实现 | 数据源注册、健康状态、质量快照可前端配置和查询 |
| 采集编排 | 已实现 | 可配置任务、手动触发、执行记录、L1/L2 专用采集器已具备 |
| 产品池与行情 | 已实现基础闭环 | `WEALTH_PRODUCT_NAV_REFRESH` 可 upsert 理财产品并写入净值行情 |
| 投资报告 | 已实现 | 本地规则/OpenAI 兼容 Provider、自动报告任务、质量门禁已具备 |
| Prompt 治理 | 已实现 | Prompt 模板、变量、Schema、预览、状态和自动治理任务已具备 |
| 模型候选 | 已实现 | 自动闭环可生成 DRAFT 模型候选和评分指标，默认不自动 ACTIVE |
| 模拟交易 | 已实现 | 买入、卖出、撤单、报告转买入、再平衡、估值和收益曲线已具备 |
| 风控审计 | 已实现 | 数据质量、产品不可 Mock、现金不足等拦截可落审计并查询 |
| 复盘优化 | 已实现基础闭环 | 回测、反馈、Prompt 评估和自动评估已具备 |
| 闭环运行审计 | 已实现 | 每轮自动闭环和步骤输入、输出、阻断原因可前端查询 |
| 驾驶舱展示 | 已具备接口支撑 | 前端可直接查询闭环运行记录，并聚合多个业务接口完成驾驶舱 |

## 2. 核心闭环链路

```text
数据源配置
  -> 定时任务编排
  -> 官方/授权端点采集
  -> 产品池、行情、资讯、公告、质量快照
  -> 投资报告和质量门禁
  -> Prompt 版本与预览
  -> Mock 组合、订单、持仓、估值
  -> 回测、反馈、Prompt 评估
  -> 闭环运行和步骤审计
  -> 前端驾驶舱聚合展示
  -> 配置优化、Prompt 版本调整、模型切换和任务重跑
```

## 3. 数据源 -> 采集编排

### 3.1 数据源治理接口

| 能力 | 接口 |
| --- | --- |
| 保存数据源 | `POST /api/admin/data-sources/save` |
| AI 发现数据源候选 | `POST /api/admin/data-sources/discover` |
| 保存健康状态 | `POST /api/admin/data-sources/health/save` |
| 保存质量快照 | `POST /api/admin/data-sources/quality/save` |
| 查询数据源看板 | `POST /api/admin/data-sources/list` |
| 查询质量趋势 | `POST /api/admin/data-sources/quality/list` |

前端可展示：

- 来源等级：`L1`、`L2`、`L3`、`L4`。
- 启用状态、采集频率、负责人、来源说明。
- 最近成功/失败、成功率、样本数、失败原因。
- 质量分、缺失率、重复率、新鲜度趋势。

### 3.2 采集任务接口

| 能力 | 接口 |
| --- | --- |
| 查询任务定义 | `POST /api/investment/tasks/definitions` |
| 保存任务定义 | `POST /api/investment/tasks/definitions/save` |
| 模型挂靠配置 | `POST /api/ai/model-bindings/list`、`/save`、`/detail` |
| 手动触发任务 | `POST /api/investment/tasks/trigger` |
| 查询执行记录 | `POST /api/investment/tasks/executions/list` |
| 查询采集资讯 | `POST /api/investment/tasks/articles/list` |
| 查询资讯-主题-产品关系 | `POST /api/investment/tasks/article-relations/list` |
| 查询主题快照 | `POST /api/investment/tasks/snapshots/list` |

已初始化任务：

| taskCode | taskType | 作用 |
| --- | --- | --- |
| `llm-data-collection-multi-source` | `AI_DATA_SOURCE_DISCOVERY` | LLM 多源数据采集规划 |
| `llm-official-disclosure-collection` | `AI_DATA_SOURCE_DISCOVERY` | LLM 官方披露采集规划 |
| `llm-product-nav-collection` | `AI_DATA_SOURCE_DISCOVERY` | LLM 产品池和净值采集规划 |
| `llm-market-quote-collection` | `AI_DATA_SOURCE_DISCOVERY` | LLM 行情估值采集规划 |
| `llm-news-research-collection` | `AI_DATA_SOURCE_DISCOVERY` | LLM 新闻研报采集规划 |
| `llm-regulatory-collection` | `AI_DATA_SOURCE_DISCOVERY` | LLM 监管政策采集规划 |
| `cn-mainland-market-momentum-scan` | `MARKET_MOMENTUM_SCAN` | 主题动量扫描 |
| `cn-mainland-hot-theme-return` | `HOT_THEME_RETURN` | 主题收益快照 |
| `cn-mainland-news-heat-aggregation` | `NEWS_HEAT_AGGREGATION` | 资讯热度聚合 |
| `auto-openai-investment-report-generation` | `AUTO_INVESTMENT_REPORT_GENERATION` | 自动投资报告 |
| `auto-prompt-governance` | `AUTO_PROMPT_GOVERNANCE` | 自动 Prompt 治理 |
| `auto-investment-closed-loop-orchestration` | `AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION` | 自动采集、报告、Prompt/模型候选、Mock 交易、回测和反馈总编排 |

重要边界：

- LLM 方向化任务负责整理候选来源、采集计划、字段映射和质量策略。
- 定时任务默认把候选沉淀进数据源池，但不会自动启用新数据源。
- L1/L2 专用采集器仍保留为前端审核候选后的执行原语，没有端点时不会写兜底数据。
- 真实数据增长依赖真实模型配置、模型/网关可访问的数据能力，以及前端审核后启用的采集任务。

### 3.3 自动闭环编排

| 能力 | 接口或任务 |
| --- | --- |
| 全自动闭环执行 | `AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION` |
| 查询闭环运行列表 | `POST /api/investment/closed-loop/runs/list` |
| 查询闭环步骤详情 | `POST /api/investment/closed-loop/runs/detail` |

默认任务行为：

- 同步执行 `dataTaskCodes` 配置的真实采集/聚合任务。
- 执行 `reportTaskCode` 生成投资报告。
- 校验报告状态、可信等级、数据质量分、质量门禁和数据缺口报告类型。
- 执行 `promptTaskCode` 生成 Prompt 候选和评估。
- 自动生成 DRAFT 模型候选和评分指标。
- 在配置的 `mockUserBizId` 下创建或复用 Mock 组合，自动执行报告转 Mock 买入。
- 刷新估值、生成回测摘要，并以 `WATCH` 反馈进入复盘链路。
- 自动记录正式 Prompt 启用、模型启用和真实交易闸门步骤，但不会执行正式启用或真实交易。

关键参数：

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `automationLevel` | `FULL_MOCK` | 当前默认自动跑完整 Mock 闭环 |
| `allowAutoMockTrade` | `true` | 是否允许自动 Mock 交易 |
| `allowPromptCandidate` | `true` | 是否允许自动 Prompt 候选和评分 |
| `allowModelCandidate` | `true` | 是否允许自动 DRAFT 模型候选 |
| `allowAutoPromptActivation` | `false` | 当前只记录闸门，不自动启用 |
| `allowAutoModelActivation` | `false` | 当前只记录闸门，不自动 ACTIVE |
| `allowRealTrade` | `false` | 当前只记录闸门，不触发真实交易 |
| `minQualityScore` | `0.45` | 自动 Mock 前最低报告质量分 |
| `maxReportsForMock` | `20` | 报告候选窗口，避免多主题批量报告只检查最后一份 |

## 4. 产品池、行情与投资方案

### 4.1 产品与行情接口

| 能力 | 接口 |
| --- | --- |
| 查询产品池 | `POST /api/products/list` |
| 查询产品详情 | `POST /api/products/detail` |
| 查询最新行情 | `POST /api/products/quotes/latest` |
| 查询历史行情 | `POST /api/products/quotes/history` |
| 后台保存产品 | `POST /api/admin/products/create`、`POST /api/admin/products/update` |
| 后台保存产品画像 | `POST /api/admin/products/investment-profile/save` |
| 后台保存行情 | `POST /api/admin/products/quotes/save` |

`WEALTH_PRODUCT_NAV_REFRESH` 已支持：

- 基于真实结构化字段 upsert `BANK_WMP` 产品。
- 将 `nav` 写入 `aiw_market_quote` 的 `1D` 行情。
- 通过 `extraFieldPaths` 配置产品编码、产品名称、净值、前值、规模和风险等级路径。

### 4.2 投资报告接口

| 能力 | 接口 |
| --- | --- |
| 手动生成报告 | `POST /api/investment/analysis/generate` |
| 查询报告列表 | `POST /api/investment/analysis/reports/list` |
| 自动生成报告 | `AUTO_INVESTMENT_REPORT_GENERATION` 任务 |

报告中已经包含前端闭环字段：

- `confidenceLevel`
- `dataQualityScore`
- `dataQualityGate`
- `investmentSummary`
- `trend`
- `investmentPlan`
- `simulatedReturn`
- `chartPayload`
- `promptSnapshot`

低质量输入的处理：

- 本地规则 Provider 会输出数据缺口报告。
- 数据质量不足时不输出积极配置建议。
- Mock 交易会重新校验报告状态、可信等级、质量分和质量门禁。

## 5. 提示词闭环

### 5.1 Prompt 接口

| 能力 | 接口 |
| --- | --- |
| 保存 Prompt 模板 | `POST /api/ai/prompts/save` |
| 查询 Prompt 列表 | `POST /api/ai/prompts/list` |
| 查询 Prompt 详情 | `POST /api/ai/prompts/detail` |
| 变更 Prompt 状态 | `POST /api/ai/prompts/status` |
| 本地预览 Prompt | `POST /api/ai/prompts/preview` |

Prompt 已是业务资产：

- 模板版本：`promptCode`、`promptVersion`。
- 使用场景：`scenario`。
- 变量定义：`variableName`、`sourcePath`、`required`。
- 输出约束：`schemaJson`、`schemaVersion`。
- 生命周期：`DRAFT`、`VALIDATING`、`ACTIVE`、`RETIRED`。

### 5.2 自动 Prompt 治理

`AUTO_PROMPT_GOVERNANCE` 已实现：

- 无 Prompt 基线时自动创建报告转方案 Prompt。
- 无真实报告时只返回等待真实报告，不写伪评估。
- 有报告后按报告状态、可信等级、数据质量分写入 Prompt 评估。

前端可在 Prompt 工作台展示：

- 当前生效版本。
- 变量缺失情况。
- 输出 Schema。
- 报告关联评估。
- 用户反馈和回测反哺评分。

## 6. 模拟交易闭环

### 6.1 Mock 组合接口

| 能力 | 接口 |
| --- | --- |
| 创建模拟组合 | `POST /api/mock/portfolios/create` |
| 查询我的组合 | `POST /api/mock/portfolios/mine` |
| 查询组合详情 | `POST /api/mock/portfolios/detail` |
| 模拟买入 | `POST /api/mock/portfolios/orders/buy` |
| 模拟卖出 | `POST /api/mock/portfolios/orders/sell` |
| 撤销订单 | `POST /api/mock/portfolios/orders/cancel` |
| 查询订单事件 | `POST /api/mock/portfolios/orders/events` |
| 报告转模拟买入 | `POST /api/mock/portfolios/orders/buy-from-report` |
| 执行再平衡 | `POST /api/mock/portfolios/rebalance/execute` |
| 刷新估值 | `POST /api/mock/portfolios/valuations/refresh` |
| 查询收益曲线 | `POST /api/mock/portfolios/performance/curve` |

已覆盖的交易边界：

- 产品不可交易会拒绝。
- 产品画像缺失或 `mockTradable=false` 会拒绝并落审计。
- 行情缺失会拒绝。
- 现金不足会拒绝并落审计。
- 持仓不足会拒绝。
- 已成交订单撤单会明确拒绝。
- 报告质量不足、数据缺口报告、无可执行金额会拒绝并落审计。
- 再平衡会先卖出超配，再买入低配。

### 6.2 风控审计接口

| 能力 | 接口 |
| --- | --- |
| 查询风险检查记录 | `POST /api/risk/checks/list` |

可前端展示的审计维度：

- `LOW_DATA_QUALITY`
- `PRODUCT_NOT_MOCK_TRADABLE`
- `INSUFFICIENT_CASH`
- `DATA_GATE_BLOCKED`
- `DATA_GAP_REPORT`
- `NO_EXECUTABLE_AMOUNT`
- `INVALID_TOTAL_ASSET`

## 7. 复盘优化闭环

### 7.1 回测接口

| 能力 | 接口 |
| --- | --- |
| 保存回测结果 | `POST /api/backtests/save` |
| 从 Mock 组合生成回测摘要 | `POST /api/backtests/generate-from-portfolio` |
| 查询回测详情 | `POST /api/backtests/detail` |
| 查询回测列表 | `POST /api/backtests/list` |

### 7.2 用户反馈接口

| 能力 | 接口 |
| --- | --- |
| 保存反馈 | `POST /api/ai/feedback/save` |
| 查询反馈详情 | `POST /api/ai/feedback/detail` |
| 查询反馈列表 | `POST /api/ai/feedback/list` |

反馈动作：

- `ADOPT`
- `REJECT`
- `WATCH`
- `IGNORE`

携带 Prompt 信息时，后端会自动生成 Prompt 评估。

### 7.3 Prompt 评估接口

| 能力 | 接口 |
| --- | --- |
| 保存 Prompt 评估 | `POST /api/ai/prompt-evaluations/save` |
| 查询 Prompt 评估详情 | `POST /api/ai/prompt-evaluations/detail` |
| 查询 Prompt 评估列表 | `POST /api/ai/prompt-evaluations/list` |

评估来源：

- 自动 Prompt 治理任务。
- 用户反馈自动评估。
- 回测结果反哺。
- 人工复核录入。

## 8. 驾驶舱展示方案

当前后端没有单独的 `/api/dashboard/*` 聚合接口，但已有接口可以支撑前端驾驶舱第一屏。

建议前端驾驶舱聚合以下数据：

| 驾驶舱区域 | 数据接口 |
| --- | --- |
| 数据源健康 | `/api/admin/data-sources/list`、`/api/admin/data-sources/discover` |
| 采集任务运行 | `/api/investment/tasks/definitions`、`/api/investment/tasks/executions/list` |
| 数据质量趋势 | `/api/admin/data-sources/quality/list` |
| 产品池和行情 | `/api/products/list`、`/api/products/quotes/latest`、`/api/products/quotes/history` |
| 资讯热度和主题快照 | `/api/investment/tasks/articles/list`、`/api/investment/tasks/snapshots/list` |
| 投资报告 | `/api/investment/analysis/reports/list` |
| Prompt/模型候选 | `/api/ai/prompts/list`、`/api/ai/prompt-evaluations/list`、`/api/ai/models/list`、`/api/ai/skills/list`、`/api/ai/model-skills/list` |
| Mock 资产 | `/api/mock/portfolios/mine`、`/api/mock/portfolios/performance/curve` |
| 风控拦截 | `/api/risk/checks/list` |
| 回测反馈 | `/api/backtests/list`、`/api/ai/feedback/list` |
| 自动闭环运行 | `/api/investment/closed-loop/runs/list`、`/api/investment/closed-loop/runs/detail` |

驾驶舱建议展示为：

- 顶部全局状态：数据源健康、今日任务、报告质量、风控拦截。
- 左侧主图：主题收益、动量、资讯热度。
- 中部主列表：最新报告和可执行/不可执行原因。
- 右侧操作队列：待配置端点、失败任务、低质量报告、Prompt 待复核。
- 底部闭环复盘：Mock 收益曲线、回测评分、用户采纳/拒绝趋势。

## 9. 当前缺口和下一步

### 9.1 必须明确的边界

1. 真实数据源启用仍需前端确认。
   后端默认通过模型和 Skill 发现候选来源、字段映射和任务建议；候选不会自动成为正式数据源。

2. 驾驶舱目前没有单一聚合接口。
   前端可以先聚合现有接口完成驾驶舱；后续如性能或体验需要，可补 `DashboardApplicationService` 和 `/api/investment/dashboard/overview`。

3. 自动优化已支持候选和评分，但不是完全自治正式启用。
   当前已经能记录报告、Prompt、模型候选、Mock、回测、反馈和评分；正式启用新 Prompt、新模型、真实交易仍要求人工确认或灰度开关。

### 9.2 建议后续增强

| 优先级 | 增强项 | 原因 |
| --- | --- | --- |
| P0 | 配置真实官方/授权数据端点模板 | 让清库后的业务数据真正增长 |
| P0 | AI Skill 管理页 | 数据源不佳/Prompt不佳时能调整对应 Skill 并关联模型 |
| P0 | 前端驾驶舱聚合页 | 把已有接口串成可视化闭环 |
| P1 | 后端驾驶舱聚合接口 | 降低前端多接口并发和拼装复杂度 |
| P1 | 自动 Prompt 新版本候选生成 | 当前已有 Prompt 基线和评估，后续可扩展为自动复制候选版本 |
| P1 | 数据源字段模板库 | 降低前端配置 JSON/HTML 字段路径成本 |
| P2 | 供应商行情同步任务 | 补齐 ETF、基金、指数等高质量行情 |

## 10. 前端闭环开发顺序

建议按以下顺序重构前端：

1. 数据源和任务工作台。
2. 产品池、行情和质量看板。
3. 投资报告和 Prompt 工作台。
4. Mock 组合、交易、估值和收益曲线。
5. 回测、反馈、Prompt 评估。
6. 投资驾驶舱聚合页。

第一版前端可以不等待后端 Dashboard 聚合接口，直接使用本文件列出的接口完成驾驶舱展示。
