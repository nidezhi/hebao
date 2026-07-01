# 数据质量验证路由总览

生成日期：2026-07-01

## 1. 路由目标

数据质量验证路由的目标不是生成投资结论，而是回答一个更基础的问题：

```text
当前数据是否足以支撑核心功能继续执行？
```

核心功能包括：

- 投资报告生成。
- Prompt / Skill / 模型治理。
- Mock 组合与模拟交易。
- 回测、反馈和风控审计。
- 驾驶舱展示和闭环追溯。

只要数据不足，路由就应输出数据缺口和更新方案，而不是继续调用投资建议模型。

## 2. 当前事实判断

根据 `00-current-handoff.md`，当前系统已经具备：

- 自动闭环配置方案和定时方案。
- 模型调用审计中心。
- 持续进化分析接口和前端页面。
- 数据质量、报告、Mock、回测、反馈、风控等基础闭环。
- 一批已入库的 AI Skill 和模型挂靠配置。

因此下一步重点不是继续扩大业务代码，而是把数据质量验证变成可复用的分析资产：

- 可复核。
- 可审计。
- 可解释。
- 可变成后续 Skill / Prompt / 节点 / 模型调整的依据。

## 3. 最小检查矩阵

| 检查对象 | 通过目标 | 阻断影响 | 建议输出 |
| --- | --- | --- | --- |
| 产品池 | 核心主题产品覆盖率达标 | 报告和 Mock 无可分析标的 | 产品缺口、主题缺口、采集器建议 |
| 行情/净值 | 最新行情新鲜、连续、可追溯 | 报告质量低，Mock 交易阻断 | 行情缺口、字段映射风险、数据源建议 |
| 资讯/公告/研报 | 近 72 小时证据和主题覆盖达标 | 新闻热度和事件归因失真 | 证据缺口、来源等级、去重建议 |
| 数据质量快照 | 质量分、缺失率、重复率、新鲜度齐全 | 门禁无法结构化判断 | 质量规则补齐建议 |
| 投资报告 | 状态成功、质量门禁通过、非数据缺口报告 | Mock 和回测不应继续 | 报告阻断原因和补数建议 |
| Mock 交易 | 产品可交易、现金、行情、风险规则满足 | 自动闭环只能停在报告后 | 风控原因和交易前置条件 |
| 回测反馈 | 有订单、估值、收益曲线和反馈样本 | Prompt/模型优化无有效样本 | 反馈样本缺口和评估建议 |
| 模型调用审计 | 失败率、耗时、成本在预算内 | 自动化烧模型或反复失败 | 熔断、降频、缓存、模型切换建议 |

## 4. 推荐输出格式

数据质量路由建议统一输出以下结构，便于前端展示和后续人工评审：

```json
{
  "routeStatus": "PASS|REVIEW|BLOCK",
  "coreFeatureReadiness": [
    {
      "feature": "INVESTMENT_REPORT",
      "status": "PASS|REVIEW|BLOCK",
      "blockingReasons": [],
      "evidence": [],
      "nextActions": []
    }
  ],
  "skillRecommendations": [],
  "promptRecommendations": [],
  "nodeRecommendations": [],
  "modelRecommendations": [],
  "acceptanceChecks": []
}
```

## 5. 建议验收方式

### 5.1 数据验收

建议后续单独沉淀只读 SQL，用于验证：

- 产品数量和主题覆盖。
- 近 3 天行情覆盖。
- 近 72 小时资讯覆盖。
- 最新质量快照状态。
- 最近报告质量门禁结果。
- 最近闭环步骤状态和断链节点。
- 模型调用失败率、耗时和成本。

### 5.2 API 验收

建议使用现有接口组合验证：

- `POST /api/admin/data-sources/list`
- `POST /api/admin/data-sources/quality/list`
- `POST /api/products/list`
- `POST /api/products/quotes/latest`
- `POST /api/investment/analysis/reports/list`
- `POST /api/investment/closed-loop/runs/list`
- `POST /api/investment/closed-loop/runs/detail`
- `POST /api/risk/checks/list`
- `POST /api/ai/model-call-audits/list`
- `POST /api/analytics/investment-evolution/summary`

### 5.3 文档验收

每次形成治理结论时，至少应包含：

- 发现的问题。
- 影响的核心功能。
- 证据来源。
- 建议调整的 Skill、Prompt、节点、模型或采集器。
- 不建议调整业务代码的理由。
- 后续如果要实施，需要的人审或灰度条件。

## 6. 当前建议优先级

1. 建立 `CORE_DATA_QUALITY_DIAGNOSTIC_SKILL`，统一诊断口径。
2. 建立 `REPORT_READINESS_GATE_SKILL` 或等价本地规则资产，避免数据不足时继续烧模型。
3. 建立 `DATA_GAP_REMEDIATION_PLANNER_SKILL`，把诊断结果变成可排期方案。
4. 建立 `CLOSED_LOOP_EVIDENCE_RECONCILIATION_SKILL`，核对闭环步骤和产物证据。
5. 建立 `AI_COST_AND_FAILURE_GUARD_SKILL`，治理模型调用失败和成本。

## 7. 禁止越界

本路由不允许：

- 修改业务代码。
- 写入核心业务表。
- 新增 Flyway 迁移。
- 降低质量门禁。
- 用模型输出替代确定性采集。
- 自动启用新 Prompt、新模型或真实交易。
- 把数据缺口包装成投资建议。
