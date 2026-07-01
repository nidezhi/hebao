# Skill 更新待办清单

生成日期：2026-07-01

## 1. 使用说明

本清单用于后续评审，不代表已经实施。任何条目进入业务系统前，都需要单独任务确认，并遵守后端开发铁律、API 契约规则和数据质量门禁。

## 2. 优先级清单

| ID | 优先级 | 建议动作 | 目标对象 | 解决的问题 | 验收方式 |
| --- | --- | --- | --- | --- | --- |
| DQ-SKILL-001 | P0 | 新增诊断型 Skill | `CORE_DATA_QUALITY_DIAGNOSTIC_SKILL` | 当前缺少统一“数据是否支撑核心功能”的诊断资产 | 能输出 `PASS/REVIEW/BLOCK`、阻断原因、证据和治理动作 |
| DQ-SKILL-002 | P0 | 新增报告前置门禁 Skill 或本地规则资产 | `REPORT_READINESS_GATE_SKILL` | 避免数据不足时继续调用报告、Prompt 治理和闭环模型 | 数据不足时只输出数据缺口，不调用投资建议模型 |
| DQ-SKILL-003 | P1 | 新增治理方案 Skill | `DATA_GAP_REMEDIATION_PLANNER_SKILL` | 数据问题发现后缺少结构化更新方案 | 每条建议包含影响功能、目标资产、验收方式、风险和人工确认要求 |
| DQ-SKILL-004 | P1 | 新增闭环证据核对 Skill | `CLOSED_LOOP_EVIDENCE_RECONCILIATION_SKILL` | 闭环步骤状态和实际产物可能断链 | 输出缺失产物、断链步骤和前端可展示动作 |
| DQ-SKILL-005 | P1 | 新增模型调用守卫 Skill | `AI_COST_AND_FAILURE_GUARD_SKILL` | 高成本、高失败、重复调用缺少统一治理建议 | 输出熔断、降频、缓存、模型切换或人工审核建议 |
| DQ-SKILL-006 | P2 | 升级行情字段映射审查能力 | `MARKET_DATA_TOOL_SKILL` 或 `COLLECTOR_FIELD_MAPPING_REVIEW_SKILL` | 确定性采集器字段映射变更缺少 AI 辅助审查资产 | 输出关键字段缺失、字段类型风险和质量评分影响 |
| DQ-SKILL-007 | P2 | 给 Prompt 治理输入追加数据质量摘要 | `PROMPT_GOVERNANCE_CORE` | Prompt 优化可能只看报告和反馈，忽略输入数据质量 | Prompt 候选评分能说明是否受数据质量限制 |
| DQ-SKILL-008 | P2 | 给投教 Skill 增加阻断解释场景 | `INVESTOR_EDUCATION_COMPANION_SKILL` | 用户需要理解为什么报告/交易被阻断 | 输出非投资建议式解释和风险提示 |

## 3. 建议落地顺序

1. 先做 `CORE_DATA_QUALITY_DIAGNOSTIC_SKILL`，因为它是后续所有治理建议的事实入口。
2. 再做 `REPORT_READINESS_GATE_SKILL`，因为它直接决定是否允许继续调用报告模型和闭环。
3. 然后做 `DATA_GAP_REMEDIATION_PLANNER_SKILL`，把诊断结果转成可排期任务。
4. 之后补 `CLOSED_LOOP_EVIDENCE_RECONCILIATION_SKILL` 和 `AI_COST_AND_FAILURE_GUARD_SKILL`，增强闭环可观测性与成本治理。
5. 最后升级既有行情、Prompt 和投教 Skill，使它们消费统一诊断结果。

## 4. 不建议做的事

| 不建议动作 | 原因 |
| --- | --- |
| 让大模型直接写入产品、行情、资讯或交易表 | 会重新回到“模型替代数据工程”的错误路线 |
| 为了跑通报告降低质量门禁 | 会让低质量数据变成看似确定的投资建议 |
| 把所有诊断输出塞进 raw JSON 给前端 | 违背当前前端结构化体验和契约收紧方向 |
| 让治理 Skill 自动启用新 Prompt 或模型 | 正式启用必须保留人工确认或灰度开关 |
| 把数据缺口解释成市场观点 | 缺数据就是缺数据，不能变成投资判断 |

## 5. 每条 Skill 的最小验收模板

```text
Skill Code:
Skill Version:
场景:
输入 schema:
输出 schema:
评估策略:
是否允许写业务表: 否
是否允许输出投资建议: 否
是否需要人工确认: 是
阻断原因字段:
证据字段:
前端展示字段:
回归验证:
```

## 6. 与核心功能的对应关系

| 核心功能 | 依赖的数据支撑 | 对应建议 Skill |
| --- | --- | --- |
| 投资报告 | 产品池、行情、资讯、质量快照、Prompt、模型绑定 | `CORE_DATA_QUALITY_DIAGNOSTIC_SKILL`、`REPORT_READINESS_GATE_SKILL` |
| Prompt 治理 | 报告质量、回测、反馈、数据缺口 | `DATA_GAP_REMEDIATION_PLANNER_SKILL`、升级 `PROMPT_GOVERNANCE_CORE` |
| Mock 交易 | 报告门禁、产品可交易性、行情、现金、风控 | `CLOSED_LOOP_EVIDENCE_RECONCILIATION_SKILL` |
| 回测反馈 | Mock 订单、估值、收益曲线、用户反馈 | `CLOSED_LOOP_EVIDENCE_RECONCILIATION_SKILL` |
| 驾驶舱展示 | 闭环步骤、阻断原因、产物 ID、数据缺口 | `CORE_DATA_QUALITY_DIAGNOSTIC_SKILL`、`DATA_GAP_REMEDIATION_PLANNER_SKILL` |
| 成本治理 | 模型调用审计、失败率、耗时、预算 | `AI_COST_AND_FAILURE_GUARD_SKILL` |
