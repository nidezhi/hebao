# 投资平台完整业务说明书

## 1. 平台目标

DZCOM 投资平台的目标是形成一个可复盘、可配置、可自动成长的投资辅助闭环：

```text
可信数据源
  -> 采集和治理编排
  -> 投资报告
  -> Prompt 与 AI Skill
  -> 投资方案和 Mock 交易
  -> 回测、反馈、风控审计
  -> 驾驶舱展示
  -> 数据源、Prompt、模型和 Skill 持续优化
```

核心边界：

- AI 可以自动发现候选、生成报告、生成 Prompt/模型候选和评分。
- Mock 投资闭环可以全自动执行。
- 正式启用新数据源、新 Prompt、新模型和真实交易必须保留人工确认或灰度开关。
- 数据质量不足时只能生成数据缺口和风险提示，不能伪装成确定投资建议。

## 2. 数据源治理

### 2.1 当前主方案

当前默认主方案不再依赖手工维护 RSS、fallback 或 endpoint 字段路径，而是：

```text
多条 AI_DATA_SOURCE_DISCOVERY 方向化定时任务
  -> DATA_SOURCE_DISCOVERY 模型挂靠配置
  -> DATA_COLLECTION_* Skill
  -> 大模型直接整理生成候选数据源、采集计划、字段映射、质量策略和审核策略
  -> 定时任务沉淀候选到数据源池但默认不启用
  -> 前端审核后启用数据源或生成专用采集任务
```

默认方向化任务：

| taskCode | collectionDirection | skillCode |
| --- | --- | --- |
| `llm-data-collection-multi-source` | `MULTI_SOURCE` | `DATA_COLLECTION_MULTI_SOURCE` |
| `llm-official-disclosure-collection` | `OFFICIAL_DISCLOSURE` | `DATA_COLLECTION_OFFICIAL_DISCLOSURE` |
| `llm-product-nav-collection` | `PRODUCT_NAV` | `DATA_COLLECTION_PRODUCT_NAV` |
| `llm-market-quote-collection` | `MARKET_QUOTE` | `DATA_COLLECTION_MARKET_QUOTE` |
| `llm-news-research-collection` | `NEWS_RESEARCH` | `DATA_COLLECTION_NEWS_RESEARCH` |
| `llm-regulatory-collection` | `REGULATORY` | `DATA_COLLECTION_REGULATORY` |

### 2.2 数据源等级

| 等级 | 说明 | 默认用途 |
| --- | --- | --- |
| `L1` | 官方监管、交易所、披露平台 | 报告核心输入 |
| `L2` | 官方产品、净值、基金/理财披露 | 产品池和行情核心输入 |
| `L3` | 授权专业供应商 | 增强行情、研报、资金流 |
| `L4` | 主流财经媒体 | 热度和事件补充 |
| `L5` | fallback/演示数据 | 只允许本地链路验证 |

### 2.3 弃案逻辑

| 弃案 | 处理 |
| --- | --- |
| RSS/fallback 作为默认数据源方案 | 移出自动闭环默认链路 |
| 手工 endpoint 字段路径作为主配置 | 降级为候选审核后的执行原语 |
| 无外部数据时写入 fallback 正式资讯 | 禁止进入正式报告 |
| 候选数据源自动启用 | 禁止，需前端确认或灰度开关 |

## 3. AI 模型、Skill 与 Prompt

### 3.1 模型挂靠配置

`aiw_ai_model_binding` 负责按业务场景选择模型：

| 场景 | 用途 |
| --- | --- |
| `DATA_SOURCE_DISCOVERY` | 数据源发现 |
| `AUTO_REPORT_GENERATION` | 自动报告 |
| `AUTO_CLOSED_LOOP_ORCHESTRATION` | 闭环编排 |
| `PROMPT_GOVERNANCE` | Prompt 治理 |

### 3.2 AI Skill

`aiw_ai_skill` 保存可版本化的大模型能力说明：

| 字段 | 说明 |
| --- | --- |
| `skillCode` | 稳定 Skill 编码 |
| `skillVersion` | Skill 版本 |
| `skillType` | 数据源发现、Prompt 治理、质量审计等 |
| `instructionContent` | 给模型的核心指令 |
| `inputSchema` | 输入约束 |
| `outputSchema` | 输出约束 |
| `evaluationPolicy` | 评分和启用策略 |

`aiw_ai_model_skill_binding` 把模型实例绑定到 Skill 版本。复盘时如果发现：

- 数据源不佳：调整对应 `DATA_COLLECTION_*` Skill 或复制新版本。
- Prompt 不佳：调整 `PROMPT_EVOLUTION_CORE` 或复制新版本。
- 模型不佳：调整模型配置、模型 Skill 绑定或候选模型状态。

### 3.3 Prompt

Prompt 仍作为业务资产维护：

- `AiPromptTemplate`
- `AiPromptVariable`
- `AiPromptOutputSchema`
- `AiPromptEvaluation`

Skill 负责指导“如何生成和评估 Prompt 候选”，Prompt 模板负责实际渲染给投资方案模型。

## 4. 定时闭环编排

默认闭环任务：

```text
auto-investment-closed-loop-orchestration
```

默认步骤：

1. `SAFETY_GUARD`：记录真实交易、自动启用 Prompt、自动启用模型等安全边界。
2. `DATA_COLLECTION`：执行多条 LLM 采集方向任务和市场/热度聚合任务。
3. `REPORT_GENERATION`：执行自动投资报告任务。
4. `QUALITY_GATE`：检查报告质量、可信等级、数据缺口和候选窗口。
5. `PROMPT_CANDIDATE`：执行 Prompt 治理任务。
6. `MODEL_CANDIDATE`：生成 DRAFT 模型候选和评分。
7. `MOCK_TRADE`：执行 Mock 买入/卖出/调仓。
8. `BACKTEST_FEEDBACK`：生成回测和反馈。
9. `PROMPT_ACTIVATION_GUARD`：记录 Prompt 正式启用闸门。
10. `MODEL_ACTIVATION_GUARD`：记录模型正式启用闸门。
11. `REAL_TRADE_GUARD`：记录真实交易闸门。

默认 `dataTaskCodes`：

```text
llm-data-collection-multi-source,
llm-official-disclosure-collection,
llm-product-nav-collection,
llm-market-quote-collection,
llm-news-research-collection,
llm-regulatory-collection,
cn-mainland-market-momentum-scan,
cn-mainland-hot-theme-return,
cn-mainland-news-heat-aggregation
```

## 5. 投资报告

报告生成输入：

- 产品池、风险画像、可交易规则。
- 行情、净值、收益、动量。
- 资讯、公告、研报、监管事件。
- 数据源质量、健康状态、样本覆盖率。
- Prompt 模板和模型配置。

报告门禁：

| 门禁 | 不通过时 |
| --- | --- |
| 报告状态不是 `SUCCEEDED` | 阻断 Mock |
| `confidenceLevel` 不可用 | 阻断 Mock |
| `dataQualityScore < minQualityScore` | 阻断 Mock |
| `qualityGatePassed=false` | 阻断 Mock |
| `reportType=DATA_GAP` | 阻断 Mock |

`maxReportsForMock=20`，最近报告不合格时会继续检查候选窗口，并把阻断原因写入闭环步骤。

## 6. Mock 交易和风控

Mock 交易支持：

- 创建组合。
- 报告转 Mock 买入。
- 卖出。
- 撤单边界。
- 再平衡。
- 估值刷新。
- 收益曲线。

风控拦截会写入前端可查审计：

- 质量不足。
- 产品不可 Mock。
- 风险等级不匹配。
- 现金不足。
- 持仓不足。
- 数据缺口报告。
- 无可执行金额。

## 7. 回测与反馈

回测和反馈用于形成复盘闭环：

```text
Mock 组合
  -> 回测结果
  -> 用户采纳/拒绝/观望/忽略
  -> Prompt 评估
  -> 模型候选评分
  -> Skill 调整建议
```

当前自动化边界：

- 自动写回测。
- 自动写反馈摘要。
- 自动写 Prompt 评估。
- 自动产出模型候选。
- 不自动启用新 Prompt 或模型。

## 8. 驾驶舱展示

驾驶舱第一屏建议展示：

| 区域 | 接口 |
| --- | --- |
| 数据源发现 | `/api/admin/data-sources/discover` |
| 数据源健康 | `/api/admin/data-sources/list`、`/api/admin/data-sources/quality/list` |
| 任务运行 | `/api/investment/tasks/definitions`、`/api/investment/tasks/executions/list` |
| 投资报告 | `/api/investment/analysis/reports/list` |
| Prompt/Skill | `/api/ai/prompts/list`、`/api/ai/skills/list`、`/api/ai/model-skills/list` |
| Mock 组合 | `/api/mock/portfolios/mine`、`/api/mock/portfolios/performance/curve` |
| 风控审计 | `/api/risk/checks/list` |
| 回测反馈 | `/api/backtests/list`、`/api/ai/feedback/list` |
| 自动闭环 | `/api/investment/closed-loop/runs/list`、`/api/investment/closed-loop/runs/detail` |

## 9. 前端建设顺序

1. AI Skill 工作台。
2. 模型 Skill 绑定页。
3. 数据源发现和候选审核页。
4. 定时任务配置页补 `AI_DATA_SOURCE_DISCOVERY`。
5. 投资报告和 Prompt 工作台。
6. Mock 组合和回测反馈页。
7. 自动闭环驾驶舱。

## 10. 当前可执行状态

已经具备：

- Skill 落库和前端接口。
- 模型实例关联 Skill。
- AI 数据源发现接口和方向化定时任务。
- 自动闭环默认链路切到多条 LLM 数据采集方向任务。
- 定时任务可把模型候选沉淀到数据源池，默认不自动启用。
- 旧 RSS/fallback/手工 endpoint 方案退出默认主链路。
- 报告、Prompt、模型候选、Mock、回测、反馈、审计可复盘。

仍需谨慎：

- AI 数据源发现当前负责整理候选来源、采集计划和质量策略；真实网页/API 抓取能力取决于模型或后续审核生成的专用采集任务。
- 正式数据源启用、Prompt 启用、模型启用和真实交易仍需前端确认或灰度开关。
- 后续可补驾驶舱聚合接口，降低前端多接口拼装成本。
