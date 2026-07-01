# Mock 闭环失败诊断与优化方案

生成日期：2026-07-01

## 1. 边界

本文件只记录最近 Mock 闭环数据诊断、原因归因和优化建议，不修改业务代码、数据库迁移、配置或前端。

本次诊断入口固定为：

- `docs_new/00-current-handoff.md`
- `data-analysis/`
- 本地只读 SQL 查询结果

## 2. 当前数据基线

本地库：`dz_database`

截至本次只读诊断：

| 指标 | 当前值 | 结论 |
| --- | ---: | --- |
| 产品 | `8` | 已覆盖默认三组主题的最小产品池 |
| 行情 | `88` | 最近行情时间为 `2026-06-29 15:00:00` |
| 新闻 | `26` | 最近新闻发布时间为 `2026-06-30 00:00:00` |
| 数据质量快照 | `103` | 最新质量快照时间为 `2026-07-01 22:49:38.164` |
| 投资报告 | `27` | 最新报告时间为 `2026-07-01 22:49:38.210` |
| 闭环运行 | `57` | 最近 3 天成功和失败并存 |
| 任务执行 | `758` | 自动任务样本足够支持归因 |

最新闭环中的 `REAL_DATA_QUALITY_SNAPSHOT` 输出：

```text
真实数据质量快照完成: quality=0.9375, products=8, quoteReady=8, recentNews=15
```

结论：最近 Mock 闭环失败的主因已经不是产品、行情、质量快照完全缺失。数据资产已能支撑进入报告和 Mock 前置检查，但在“报告计划结构 -> Mock 执行”之间出现断裂。

## 3. 最近失败归因

### 3.1 P0：本地库 `idempotency_key` 字段仍未扩容

证据：

```text
aiw_order.idempotency_key = varchar(128)
```

但当前 handoff 口径已经是：

```text
V49__expand_order_idempotency_key.sql 将 aiw_order.idempotency_key 扩为 VARCHAR(512)
```

最近失败：

```text
2026-07-01 21:23:49
Data too long for column 'idempotency_key'
```

影响：

- `CLR-20260701-e4e1ba62` 在报告质量门禁通过后，进入 Mock 下单时数据库截断失败。
- 这是 schema 漂移，不是投资数据质量问题。
- 当前本地库 `chat_snapshot` 和 `aiw_ai_model_call_audit` 已存在，但 `idempotency_key` 仍停在 `128`，说明局部迁移状态不一致。

建议：

- 后续真正实施前，先做 schema preflight：检查 `chat_snapshot`、`aiw_ai_model_call_audit`、`aiw_order.idempotency_key >= 512`。
- schema preflight 不通过时，闭环应在 `SCHEMA_PREFLIGHT` 或 `SAFETY_GUARD` 阶段结构化阻断，不应继续执行到订单落库。
- 本文件只记录建议，不执行迁移。

### 3.2 P0：报告计划字段与 Mock 执行器可解析字段不一致

最近 3 天最主要阻断：

```text
Mock 计划无法执行: 报告未给出可执行的参考配置金额
```

计数：

```text
最近 3 天该原因阻断 3 次
```

典型报告 `34ea2712-3248-4466-bd0f-996a4c7abe4a`：

```json
{
  "planType": "SMALL_EXPLORATORY_BUY",
  "actionType": "BUY",
  "orderSizing": {
    "referenceTradeAmount": 5000,
    "referenceAllocationRate": 0.05,
    "maxSingleTradeAmountLimit": 10000
  },
  "targetWeights": [],
  "selectedProduct": {
    "productBizId": "bf492788-3333-479e-a07b-15b7539194da",
    "productCode": "515980"
  },
  "portfolioConsideration": {
    "cashBalance": 100000,
    "cashSufficient": true
  }
}
```

业务上这份报告已经给出了：

- `actionType=BUY`
- `selectedProduct.productBizId`
- `orderSizing.referenceTradeAmount=5000`
- `cashBalance=100000`

但闭环仍判断“未给出可执行的参考配置金额”。这说明 Mock 执行器或风控规则没有把 `orderSizing.referenceTradeAmount` 识别为可执行金额，或只接受少数顶层字段。

影响：

- 数据质量和报告质量都通过了，Mock 交易仍无法执行。
- 失败不是产品不可 Mock，也不是现金不足，而是投资计划 JSON 结构漂移。

建议：

- 定义统一的 `ExecutableMockPlan` 契约，要求报告或 Prompt 输出稳定字段：
  - `actionType`
  - `selectedProduct.productBizId`
  - `executableAmount`
  - `targetWeights[].productBizId`
  - `targetWeights[].targetWeight`
  - `cashReserveAfterPlan`
  - `maxSingleTradeAmount`
  - `riskControls[]`
- 在报告 Prompt 中把 `orderSizing.referenceTradeAmount`、`plannedTradeAmount`、`referenceAllocationAmount` 等别名统一映射到顶层 `executableAmount`。
- 新增建议节点 `MOCK_PLAN_NORMALIZATION`，在 `QUALITY_GATE` 后、`MOCK_TRADE` 前执行字段归一化和字段级阻断说明。

### 3.3 P1：默认闭环方案中 Prompt 治理任务为空

最近步骤显示：

```text
PROMPT_CANDIDATE = SKIPPED
failure_reason = 未配置 Prompt 治理任务
```

计数：

```text
最近 3 天跳过 7 次
```

影响：

- 报告计划字段已经出现漂移，但 Prompt 治理没有参与纠偏。
- 模型候选仍在生成，Prompt 候选却缺席，闭环进化会偏向模型而不是修正输出契约。

建议：

- `default-auto-mock` 方案应恢复或显式配置 `promptTaskCode=auto-prompt-governance`。
- Prompt 治理输入必须包含 `NO_EXECUTABLE_AMOUNT`、`idempotency_key` schema 漂移、Mock 执行字段缺失等失败样本。
- Prompt 评估分应增加“Mock 可执行契约分”，不是只看报告质量和文本质量。

### 3.4 P1：配置口径出现安全边界漂移

最近 `PROFILE_SNAPSHOT` 显示：

```text
allowAutoPromptActivation=true
allowAutoModelActivation=true
promptTaskCode=""
dataTaskCodes=["real-data-quality-snapshot"]
```

这和当前 handoff 中“正式启用新 Prompt、新模型必须保留人工确认或灰度开关”的边界存在张力。

影响：

- 方案声称允许自动启用 Prompt/模型，但 Prompt 治理任务为空。
- 安全策略说明与实际任务编排不一致，前端和审计可能难以判断真实行为。

建议：

- `SAFETY_GUARD` 输出应把“允许候选生成”和“允许正式启用”拆开。
- 默认方案建议恢复为：
  - `allowPromptCandidate=true`
  - `allowModelCandidate=true`
  - `allowAutoPromptActivation=false`
  - `allowAutoModelActivation=false`
  - `allowRealTrade=false`
- 如果确实要自动启用，必须新增灰度条件、回滚条件和人工确认记录。

### 3.5 P1：现金不足曾导致连续失败，但当前已恢复

历史失败：

```text
2026-06-29 21:20:56 模拟组合现金不足
2026-06-29 21:40:08 模拟组合现金不足
2026-06-30 00:27:38 模拟组合现金不足
```

风控证据：

```json
{
  "ruleCode": "MOCK_CASH_BALANCE",
  "reasonCode": "INSUFFICIENT_CASH",
  "cashBalance": 0.0,
  "requiredCash": 20000.0
}
```

当前自动 Mock 组合：

```text
portfolioBizId=0e826d40-d827-46c9-b08f-5a9576b76616
totalAsset=100000
cashBalance=100000
positionValue=0
```

结论：

- 现金不足是历史真实失败原因。
- 当前默认组合已恢复到 10W 现金空仓，不是最新 2026-07-01 阻断主因。

建议：

- 对定时基线方案，明确“每轮复用组合”还是“每日重置组合”。
- 若复用组合，应在报告计划中优先走 `REBALANCE`，不能每轮都按现金充足假设 `BUY`。
- 若定时任务目标是生成稳定样本，应在 `MOCK_PORTFOLIO_CONTEXT` 固化 `cashBalance/positions/maxSingleTradeAmount`，并要求报告输出严格基于该上下文。

### 3.6 P2：数据质量快照新闻数低于配置目标但未阻断

当前任务配置：

```text
real-data-quality-snapshot.minNewsCount = 20
```

最近输出：

```text
recentNews=15
quality=0.9375
```

结论：

- 质量分整体较高，未阻断报告。
- 但新闻样本小于配置目标，应该至少进入 `REVIEW` 级解释，避免前端误以为证据完全充足。

建议：

- 数据质量输出增加 `sampleStatus`：
  - `PASS`: 达到配置目标。
  - `REVIEW`: 总分过线但某项样本不足。
  - `BLOCK`: 核心门禁不过。
- 报告 Prompt 应读取 `recentNews=15 < minNewsCount=20`，在风险说明中体现“证据样本偏薄”。

## 4. 当前不是主因的问题

| 问题 | 当前判断 | 证据 |
| --- | --- | --- |
| 产品池缺失 | 不是主因 | `products=8`，默认主题产品均存在 |
| 行情缺失 | 不是主因 | `quotes=88`，8 个产品都有最新行情 |
| 产品不可 Mock | 不是最新主因 | 8 个核心产品 `mock_tradable=1` |
| 报告质量不过 | 不是最新主因 | 最近多份报告 `HIGH_CONFIDENCE` 且 `gate_passed=true` |
| 当前现金不足 | 不是最新主因 | 当前默认组合现金 `100000`，空仓 |

## 5. 建议新增路由节点

| 节点 | 位置 | 目标 | 阻断示例 |
| --- | --- | --- | --- |
| `SCHEMA_PREFLIGHT` | `PROFILE_SNAPSHOT` 后 | 检查运行需要的表和字段是否与代码口径一致 | `ORDER_IDEMPOTENCY_KEY_TOO_SHORT` |
| `MOCK_PLAN_NORMALIZATION` | `QUALITY_GATE` 后 | 把报告计划归一化为 Mock 执行契约 | `NO_EXECUTABLE_AMOUNT` |
| `MOCK_PLAN_FIELD_AUDIT` | `MOCK_PLAN_NORMALIZATION` 后 | 输出字段级缺失原因，反馈给 Prompt 治理 | `MISSING_SELECTED_PRODUCT`、`MISSING_EXECUTABLE_AMOUNT` |
| `MOCK_PORTFOLIO_POLICY` | `MOCK_PORTFOLIO_CONTEXT` 后 | 判断复用组合、重置组合、现金不足和调仓优先级 | `CASH_POLICY_MISMATCH` |

## 6. 建议 Prompt / Skill 更新

### 6.1 Prompt：`investment-plan-from-report`

建议新增输出约束：

```json
{
  "actionType": "BUY|SELL|REBALANCE|HOLD|SKIP",
  "executableAmount": 5000,
  "selectedProduct": {
    "productBizId": "...",
    "productCode": "515980"
  },
  "targetWeights": [
    {
      "productBizId": "...",
      "targetWeight": 0.05
    }
  ],
  "cashReserveAfterPlan": 95000,
  "maxSingleTradeAmount": 10000,
  "notExecutableReason": null
}
```

强制规则：

- `BUY` 必须有 `selectedProduct.productBizId` 和 `executableAmount > 0`。
- `REBALANCE` 必须有非空 `targetWeights`，且总权重在 `0` 到 `1` 之间。
- `HOLD/SKIP` 必须写 `notExecutableReason`，不能让 Mock 执行器猜。
- 允许保留 `orderSizing`、`plannedTradeAmount` 等解释字段，但执行字段必须出现在顶层稳定契约。

### 6.2 Skill：`CLOSED_LOOP_EVIDENCE_RECONCILIATION_SKILL`

新增评估项：

- 报告计划是否包含 Mock 可执行字段。
- Mock 执行器是否能解析对应字段。
- 风控拒绝原因是否已反馈给 Prompt 治理。
- Schema preflight 是否通过。

### 6.3 Skill：`DATA_GAP_REMEDIATION_PLANNER_SKILL`

新增根因类型：

| 根因类型 | 说明 |
| --- | --- |
| `MOCK_PLAN_SCHEMA_DRIFT` | 报告计划字段和 Mock 执行契约不一致 |
| `LOCAL_SCHEMA_DRIFT` | 本地数据库字段与当前运行口径不一致 |
| `PROMPT_GOVERNANCE_DISABLED` | Prompt 治理任务缺席，无法自动修复输出契约 |
| `MOCK_CASH_POLICY_MISMATCH` | 组合现金策略与报告计划假设不一致 |

## 7. 优先级建议

| 优先级 | 建议 | 目的 |
| --- | --- | --- |
| P0 | 定义并使用 `ExecutableMockPlan` 稳定契约 | 消除“报告有金额但执行器识别不到”的断链 |
| P0 | 修正 `MOCK_PLAN_NORMALIZATION` 的 PASS 条件 | 归一化后若金额为空，必须 `BLOCK/REVIEW`，不能继续到 Mock 交易 |
| P1 | 对齐本地 schema：`idempotency_key` 应达到 `VARCHAR(512)` | 消除订单落库硬失败；23:41 复核中该项已通过 |
| P1 | 恢复 `promptTaskCode` 或明确关闭原因 | 让 Prompt 能根据 Mock 失败样本自我修正；23:41 复核中该项已恢复 |
| P1 | 明确默认方案是否允许自动启用 Prompt/模型 | 修正安全边界漂移；23:41 复核中开关已恢复为 false |
| P2 | 数据质量样本不足时输出 `REVIEW` | 避免总分过线掩盖新闻证据偏薄 |

## 8. 下一次验证清单

只读验证建议：

```sql
SELECT column_type
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'aiw_order'
  AND column_name = 'idempotency_key';
```

期望：

```text
varchar(512)
```

闭环验证建议：

- 最近一次 `QUALITY_GATE` 通过后，`MOCK_PLAN_NORMALIZATION` 输出非空可执行金额且状态为 `PASS`。
- `MOCK_TRADE` 不再因 `NO_EXECUTABLE_AMOUNT` 阻断。
- 若报告选择 `HOLD/SKIP`，必须有结构化 `notExecutableReason`。
- 若报告选择 `BUY`，必须能在订单表看到一条 `FILLED` 或结构化 `REJECTED` 订单。
- 风控审计中 `REPORT_EXECUTABLE_AMOUNT` 不再连续出现 `NO_EXECUTABLE_AMOUNT`。

## 9. 总结

最近 Mock 无法闭环主要是两类问题叠加：

1. 运行环境 schema 漂移：`aiw_order.idempotency_key` 仍是 `VARCHAR(128)`，导致可执行调仓在落订单时失败。
2. 报告计划契约漂移：报告把可执行金额放在 `orderSizing.referenceTradeAmount`、`orderReference.referenceAmount` 等非统一字段，Mock 执行器无法稳定识别。

数据质量本身已经比早期明显改善，当前优化重点应从“补基础数据”转为“稳定报告到 Mock 交易的执行契约、schema preflight 和 Prompt 治理反馈”。

## 10. 23:41 附件日志复核

用户补充日志对应运行：

```text
runNo=CLR-20260701-56170c05
reportBizId=151fb4b5-f858-495c-bc0b-496004d06836
triggerSource=MANUAL
```

### 10.1 已改善项

这次运行相比前一轮诊断已有进步：

| 项 | 23:41 复核结果 | 说明 |
| --- | --- | --- |
| `SCHEMA_PREFLIGHT` | `SUCCEEDED` | `aiw_order.idempotency_key.length=512`，`chat_snapshot` 和 `aiw_ai_model_call_audit` 均存在 |
| Prompt 治理 | `PROMPT_CANDIDATE=SUCCEEDED` | `promptTaskCode=auto-prompt-governance` 已恢复并生成评估 |
| 安全开关 | 已恢复 | `allowAutoPromptActivation=false`、`allowAutoModelActivation=false` |
| 数据质量 | `sampleStatus=REVIEW` | 总分 `0.9375`，但 `recentNews=15`，样本状态已能表达偏薄 |
| 报告质量 | 通过 | `HIGH_CONFIDENCE`，`dataQualityScore=0.7631` |

因此当前最新阻断不再是 schema 漂移或 Prompt 治理缺席。

### 10.2 最新阻断点

闭环步骤显示：

```text
MOCK_PLAN_NORMALIZATION = SUCCEEDED
output_summary.referenceTradeAmount = null
MOCK_TRADE = BLOCKED
reason = Mock 计划无法执行: 报告未给出可执行的参考配置金额
```

风险审计显示：

```json
{
  "ruleCode": "REPORT_EXECUTABLE_AMOUNT",
  "checkResult": "REJECT",
  "reasonCode": "NO_EXECUTABLE_AMOUNT",
  "supportedAmountFields": "referenceAllocationAmount,executableAmount,plannedTradeAmount,referenceTradeAmount,orderSizing.*"
}
```

报告计划实际包含：

```json
{
  "actionType": "BUY",
  "targetWeights": [
    {
      "productBizId": "2b08333f-6fb8-4890-854a-77aab9ecdb47",
      "targetWeight": 0.05
    }
  ],
  "orderSuggestion": {
    "side": "BUY",
    "referenceAmount": 5000,
    "referenceAllocationRate": 0.05,
    "cashAfterReferenceTrade": 95000
  },
  "selectedProduct": {
    "productBizId": "2b08333f-6fb8-4890-854a-77aab9ecdb47",
    "productCode": "159819"
  }
}
```

根因更新：

- Prompt 已输出可执行意图、产品和金额，但金额字段落在 `orderSuggestion.referenceAmount`。
- 当前风控支持字段中没有 `orderSuggestion.referenceAmount`。
- `MOCK_PLAN_NORMALIZATION` 虽然识别出 `actionType=BUY`、`productBizId`、`targetWeightCount=1`，但没有识别金额，仍错误标记为 `PASS`。

### 10.3 最新 P0 建议

1. `MOCK_PLAN_NORMALIZATION` 支持金额字段别名：
   - `executableAmount`
   - `plannedTradeAmount`
   - `referenceTradeAmount`
   - `referenceAllocationAmount`
   - `orderSizing.referenceTradeAmount`
   - `orderReference.referenceAmount`
   - `orderSuggestion.referenceAmount`
2. 对 `targetWeights` 提供金额推导：
   - 若 `targetWeight=0.05` 且 `totalAsset=100000`，可推导 `executableAmount=5000`。
   - 推导金额必须受 `cashBalance` 和 `maxSingleTradeAmount` 约束。
3. 修正归一化状态：
   - `BUY` 或 `REBALANCE` 且最终金额为空时，`normalizationStatus` 不得为 `PASS`。
   - 应输出 `BLOCK` 或 `REVIEW`，并把缺失字段写入 `missingFields`。
4. Prompt 评估增加 Mock 执行契约扣分：
   - 本次 Prompt 评估分 `0.9044`，但实际 Mock 不可执行。
   - 评分应把 `NO_EXECUTABLE_AMOUNT` 作为硬性扣分或 `REJECT` 条件。

### 10.4 更新后的当前结论

当前最近一次运行已经证明：

- 数据质量可以支撑报告生成。
- schema preflight 已能识别并通过关键字段。
- Prompt 治理已恢复。
- Mock 失败剩余核心问题是“执行金额字段契约和归一化规则不一致”。

下一步不应继续补基础数据，而应集中处理 `ExecutableMockPlan` 字段契约、金额别名归一化和 Prompt 评估闭环。

### 10.5 2026-07-01 代码落地摘要

已完成本诊断 P0 的后端落地：

- `MOCK_PLAN_NORMALIZATION` 支持 `orderSuggestion.referenceAmount`、`orderReference.referenceAmount` 等金额别名。
- 报告只有 `targetWeights[0].targetWeight` 时，可结合组合 `totalAsset/cashBalance` 推导可执行参考金额。
- BUY/REBALANCE 缺少可执行金额或产品时，归一化节点直接 `BLOCKED`，不再继续进入 `MOCK_TRADE`。
- `buyFromReport` 同步支持目标权重金额推导和 `targetWeights[0].productBizId`，避免编排层能识别、交易层不能执行。

验证：`./mvnw -q -Dtest=MockPortfolioApplicationServiceTest,AutoInvestmentClosedLoopOrchestrationTaskHandlerTest test`、`./mvnw -q -DskipTests compile`、`git diff --check` 均通过。
