# 数据质量验证路由 Skill 汇总建议

生成日期：2026-07-01

## 1. 适用范围

本文件汇总一个建议中的“数据质量验证路由”。该路由的主要职责是验证核心数据是否足以支撑投资闭环，发现数据质量问题，并提出可评审的更新方案。

本文件只提供参考意见，不是 Flyway 迁移，不是运行时代码，不是已生效配置。

## 2. 依据

本轮参考的有效事实：

- `docs_new/00-current-handoff.md`：当前默认交接入口。
- `docs_new/01-product-contract.md`：产品目标、核心边界和闭环验收口径。
- `docs_new/06-backend-gap-list.md`：仍需持续关注的高级配置 JSON、契约类型、真实路由冒烟等缺口。
- `docs_new/17-investment-closed-loop-implementation.md`：自动闭环当前节点和接口支撑。
- `docs_new/21-ai-investment-loop-diagnosis-and-recovery-plan.md`：历史诊断中“大模型承担采集职责过重”的根因。
- `docs_new/22-deterministic-real-data-collector-plan.md`：确定性采集、质量快照和前置门禁策略。
- `src/main/resources/db/migration/V22__ai_skills_and_pure_closed_loop_tasks.sql`：现有 `DATA_SOURCE_DISCOVERY_CORE`、`PROMPT_GOVERNANCE_CORE`。
- `src/main/resources/db/migration/V28__ai_structured_core_data_collection.sql`：现有 `AI_STRUCTURED_DATA_COLLECTION_CORE`。
- `src/main/resources/db/migration/V32__qwen_domestic_investment_skills.sql`：现有千问国内投资能力模型与 `MARKET_DATA_TOOL_SKILL` 等 Skill。

## 3. 核心结论

当前不建议把“数据质量验证路由”做成新的业务写入链路。更合适的定位是治理路由：

```text
读取核心数据与审计证据
  -> 校验产品、行情、资讯、质量快照、报告、Mock、回测和模型调用
  -> 识别缺口类型和阻断节点
  -> 输出 Skill / Prompt / 节点 / 模型 / 采集器调整建议
  -> 形成前端可展示或人工评审的治理包
```

路由输出可以作为后续人工评审、前端配置、迁移设计和任务排期的依据，但不能直接改变核心业务数据。

## 4. 建议路由节点

| 节点编码 | 节点名称 | 输入 | 输出 | 是否允许写业务表 |
| --- | --- | --- | --- | --- |
| `DQ_SAFETY_GUARD` | 安全边界检查 | 当前环境、自动化开关、预算、人工确认策略 | 本次允许执行的分析范围 | 否 |
| `DQ_BASELINE_COUNTS` | 核心数据基线统计 | 产品、行情、资讯、质量快照、报告、闭环、风控、模型审计计数 | 基础覆盖率和空样本风险 | 否 |
| `DQ_ASSET_READINESS` | 产品与行情就绪度 | 产品池、行情新鲜度、产品画像、可 Mock 交易规则 | 产品/行情缺口清单 | 否 |
| `DQ_NEWS_EVIDENCE_READINESS` | 资讯与证据就绪度 | 资讯、公告、监管、研报、来源等级、主题关系 | 证据链缺口清单 | 否 |
| `DQ_REPORT_GATE_REPLAY` | 报告门禁复核 | 最近报告、数据质量分、质量门禁、报告类型、候选窗口 | 报告是否可进入 Mock 的复核原因 | 否 |
| `DQ_CLOSED_LOOP_EVIDENCE` | 闭环证据核对 | 闭环运行、步骤、任务执行、Mock 订单、回测、反馈 | 缺失产物和断链节点 | 否 |
| `DQ_AI_CALL_COST_STABILITY` | 模型调用稳定性检查 | 模型调用审计、失败原因、耗时、token 或成本估计 | 成本和失败熔断建议 | 否 |
| `DQ_REMEDIATION_PLAN` | 治理方案生成 | 前面节点的结构化诊断结果 | Skill、Prompt、节点、模型、采集器更新建议 | 否 |
| `DQ_REVIEW_PACKET` | 人工评审包 | 治理方案、证据、风险等级、验收 SQL/API | 可评审 Markdown/JSON 摘要 | 否 |

## 5. 建议新增或升级的 Skills

### 5.1 `CORE_DATA_QUALITY_DIAGNOSTIC_SKILL`

建议状态：新增。

Skill 类型：`DATA_QUALITY_DIAGNOSTIC`

职责：

- 汇总产品、行情、资讯、质量快照、报告、闭环步骤和风控审计。
- 判断核心功能是否具备最小数据支撑。
- 输出阻断原因、证据路径和治理优先级。

边界：

- 只能读诊断输入和审计摘要。
- 不生成投资建议。
- 不直接启用数据源、Prompt、模型或真实交易。

建议指令：

```text
你是 DZCOM 数据质量诊断 Skill。你只判断数据是否足以支撑投资闭环，不输出买卖建议。
必须分别检查产品覆盖、行情新鲜度、资讯证据、数据质量快照、报告门禁、Mock 可交易性、回测反馈和模型调用稳定性。
当样本不足或证据缺失时，输出 dataGaps 和 remediationActions，不得补写、猜测或美化数据。
```

输入 schema 建议：

```json
{
  "type": "object",
  "required": ["marketScope", "themes", "baseline", "qualitySnapshots", "reports", "closedLoopRuns"],
  "properties": {
    "marketScope": { "type": "string" },
    "themes": { "type": "array" },
    "baseline": { "type": "object" },
    "qualitySnapshots": { "type": "array" },
    "reports": { "type": "array" },
    "closedLoopRuns": { "type": "array" },
    "riskChecks": { "type": "array" },
    "modelCallAudits": { "type": "array" }
  }
}
```

输出 schema 建议：

```json
{
  "type": "object",
  "required": ["readiness", "blockingReasons", "dataGaps", "remediationActions"],
  "properties": {
    "readiness": { "type": "string", "enum": ["PASS", "REVIEW", "BLOCK"] },
    "blockingReasons": { "type": "array" },
    "dataGaps": { "type": "array" },
    "evidence": { "type": "array" },
    "remediationActions": { "type": "array" },
    "nextValidation": { "type": "array" }
  }
}
```

评估策略：

```json
{
  "noInvestmentAdvice": true,
  "requireEvidence": true,
  "rejectSyntheticData": true,
  "blockWhenCoreCountsZero": true,
  "manualReviewRequired": true
}
```

### 5.2 `DATA_GAP_REMEDIATION_PLANNER_SKILL`

建议状态：新增。

Skill 类型：`DATA_REMEDIATION_PLANNING`

职责：

- 把数据质量问题转成后续工作建议。
- 区分数据源、采集器、字段映射、质量评分、Prompt、模型绑定和前端展示问题。
- 输出低风险、可拆分、可验收的更新方案。

建议指令：

```text
你是 DZCOM 数据缺口治理方案 Skill。你根据诊断结果输出更新方案，而不是直接改数据。
每条方案必须包含问题、影响的核心功能、建议调整对象、验收方式、风险和是否需要人工确认。
```

重点输出字段：

- `gapCode`
- `affectedFeature`
- `rootCauseType`
- `recommendedChangeType`
- `targetAsset`
- `acceptance`
- `riskLevel`
- `manualApprovalRequired`

建议根因类型：

| 根因类型 | 说明 |
| --- | --- |
| `NO_PRODUCT_UNIVERSE` | 产品池不足 |
| `STALE_OR_MISSING_QUOTES` | 行情缺失或过旧 |
| `LOW_NEWS_EVIDENCE` | 资讯/公告/研报证据不足 |
| `QUALITY_SNAPSHOT_MISSING` | 缺少质量快照或质量口径不完整 |
| `REPORT_GATE_BLOCKED` | 报告门禁不通过 |
| `MOCK_TRADE_BLOCKED` | Mock 交易因产品、现金、行情或报告质量阻断 |
| `PROMPT_SCHEMA_DRIFT` | Prompt 输出结构与前端/服务端契约漂移 |
| `MODEL_BINDING_MISMATCH` | 场景模型或 Skill 绑定不匹配 |
| `AI_COST_FAILURE_RISK` | 模型调用成本、失败率或超时风险高 |

### 5.3 `COLLECTOR_FIELD_MAPPING_REVIEW_SKILL`

建议状态：升级现有 `MARKET_DATA_TOOL_SKILL` 或新增审查型 Skill。

Skill 类型：`COLLECTOR_MAPPING_REVIEW`

职责：

- 只审查确定性采集器的字段映射建议。
- 识别 `product_code`、`quote_time`、`close_price`、`source_url` 等关键字段缺失。
- 给出采集器配置更新建议。

边界：

- 不直接调用外部行情 API。
- 不直接写 `aiw_product`、`aiw_market_quote`、`aiw_news_article`。
- 不把模型输出当作行情真值。

适配现有资产：

- 与 `MARKET_DATA_TOOL_SKILL` 形成分工：前者偏字段映射与质量解释，本 Skill 偏“配置审查和落库前校验建议”。

### 5.4 `REPORT_READINESS_GATE_SKILL`

建议状态：新增。

Skill 类型：`REPORT_QUALITY_GATE_REVIEW`

职责：

- 复核报告生成前置条件。
- 判断是否应该调用报告模型、Prompt 治理和 Mock 闭环。
- 在不满足条件时输出“为什么不应调用模型”的解释和补数建议。

建议门禁：

| 指标 | 建议阈值 |
| --- | --- |
| 产品覆盖 | 配置产品覆盖率 `>= 80%` |
| 行情覆盖 | 有效行情产品覆盖率 `>= 80%` |
| 行情新鲜度 | 最新行情不超过 `3` 个自然日，节假日可配置放宽 |
| 行情连续性 | 每个核心产品至少 `2` 个交易日行情 |
| 资讯覆盖 | 近 `72` 小时资讯不少于 `20` 条 |
| 主题资讯覆盖 | 每个主题不少于 `3` 条 |
| 质量分 | 总质量分 `>= 0.60` |

### 5.5 `CLOSED_LOOP_EVIDENCE_RECONCILIATION_SKILL`

建议状态：新增。

Skill 类型：`CLOSED_LOOP_EVIDENCE_RECONCILIATION`

职责：

- 核对闭环步骤是否都有可追溯产物。
- 检查报告、Prompt、模型候选、Mock 订单、回测、反馈、风控审计之间是否断链。
- 输出前端驾驶舱应展示的断链节点和下一步动作。

重点检查：

- `REPORT_GENERATION` 是否生成可查询报告。
- `QUALITY_GATE` 是否给出结构化阻断原因。
- `PROMPT_CANDIDATE` 是否绑定报告和评估。
- `MOCK_TRADE` 是否关联组合、订单和风险审计。
- `BACKTEST_FEEDBACK` 是否关联回测与反馈样本。
- 模型调用审计是否包含业务关联、Prompt、Skill 和失败上下文。

### 5.6 `AI_COST_AND_FAILURE_GUARD_SKILL`

建议状态：新增。

Skill 类型：`AI_OPERATION_GUARD`

职责：

- 读取模型调用审计摘要。
- 识别高失败率、高耗时、重复输入、高成本任务。
- 建议熔断、降频、缓存、模型切换或手动审核。

边界：

- 不修改任务启停状态。
- 不修改模型配置。
- 只输出建议和理由。

## 6. 建议 Prompt 资产

### 6.1 数据质量诊断 Prompt

建议 `promptCode`：`data-quality-diagnostic-summary`

场景：`DATA_QUALITY_DIAGNOSTIC`

目标：把结构化统计转成可审计的诊断摘要。

关键约束：

- 只能引用输入中的证据。
- 不能输出投资建议。
- 必须说明“能否支撑报告、Mock 交易、回测和驾驶舱展示”。

### 6.2 数据缺口治理 Prompt

建议 `promptCode`：`data-gap-remediation-plan`

场景：`DATA_REMEDIATION_PLANNING`

目标：把阻断原因转为后续工作建议。

关键约束：

- 每条建议都要有验收方式。
- 区分“数据补齐”“Prompt 调整”“Skill 调整”“模型绑定调整”“节点编排调整”“前端展示调整”。
- 不生成迁移 SQL 和业务代码。

### 6.3 闭环证据核对 Prompt

建议 `promptCode`：`closed-loop-evidence-reconciliation`

场景：`CLOSED_LOOP_EVIDENCE_RECONCILIATION`

目标：说明闭环断在哪个节点，缺少哪个产物。

关键约束：

- 必须输出 `stepCode`、`artifactType`、`missingEvidence`、`userVisibleImpact`、`nextAction`。
- 对 `RUNNING` 卡死、失败原因为空、产物 ID 缺失等情况给出结构化建议。

## 7. 建议模型挂靠

| 场景 | 推荐模型 | 原因 | 约束 |
| --- | --- | --- | --- |
| `DATA_QUALITY_DIAGNOSTIC` | 本地规则优先，必要时千问/兼容模型辅助总结 | 大部分诊断应由确定性统计完成 | 模型只做解释，不做事实计算 |
| `DATA_REMEDIATION_PLANNING` | `qwen-domestic-investment` 或低温 OpenAI 兼容模型 | 中文金融语境和治理建议更重要 | `temperature <= 0.2`，输出 JSON |
| `COLLECTOR_MAPPING_REVIEW` | `qwen-domestic-investment` | 国内数据源字段理解更合适 | 不直接落库 |
| `REPORT_READINESS_GATE` | 本地规则 | 门禁必须确定性 | 不建议依赖大模型判断 PASS/BLOCK |
| `CLOSED_LOOP_EVIDENCE_RECONCILIATION` | 本地规则 + 模型摘要 | 断链判断确定性，说明可由模型润色 | 阻断状态以规则为准 |
| `AI_COST_AND_FAILURE_GUARD` | 本地规则 | 成本和失败率是数值规则 | 模型只总结建议 |

## 8. 与现有 Skill 的关系

| 现有 Skill | 保留方式 | 建议补充 |
| --- | --- | --- |
| `DATA_SOURCE_DISCOVERY_CORE` | 保留，负责候选数据源发现 | 输出候选后交给数据质量路由审查，不自动启用 |
| `PROMPT_GOVERNANCE_CORE` | 保留，负责 Prompt 候选和评分 | 治理输入必须包含数据质量诊断摘要 |
| `AI_STRUCTURED_DATA_COLLECTION_CORE` | 降级为实验或辅助整理 | 不作为核心真实采集入口，输出缺口优先 |
| `MARKET_DATA_TOOL_SKILL` | 保留，负责行情工具和字段映射说明 | 增加字段映射审查 Skill 或版本 |
| `RESEARCH_REPORT_READING_SKILL` | 保留，负责公告/研报摘要 | 输出必须挂接来源和证据类型 |
| `MACRO_ANALYSIS_SKILL` | 保留，作为背景解释 | 不覆盖产品级数据和质量门禁 |
| `INVESTOR_EDUCATION_COMPANION_SKILL` | 保留，用于用户解释 | 可以解释“为什么系统阻断报告或交易” |

## 9. 前端与审计展示建议

数据质量验证路由最终应能支撑前端展示：

- 当前是否可以生成报告。
- 当前是否可以进入 Mock 交易。
- 当前阻断在哪个闭环节点。
- 缺哪些数据资产。
- 推荐先调整哪个 Skill、Prompt、模型绑定或采集器配置。
- 每条建议的验收 SQL/API 或页面验证方式。

优先展示结构化字段，不依赖原始 JSON。

## 10. 验收口径

这些建议被真正实施前，至少应满足：

- 不修改业务核心表数据。
- 不降低质量门禁。
- 不让模型直接写产品、行情、资讯和交易数据。
- 每个 Skill 都有输入 schema、输出 schema 和评估策略。
- 每个路由节点都有可追踪输入、输出和阻断原因。
- 每条治理建议都有人工确认或灰度开关。
- 前端能展示结构化结果，而不是只展示 raw JSON。
