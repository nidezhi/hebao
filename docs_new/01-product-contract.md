# 01 Product Contract

生成日期：2026-06-28

本文描述 dzcom 与 dzcom_web 共同遵守的产品契约。详细历史方案可按需查阅 `20-investment-platform-business-specification.md`、`21-ai-investment-loop-diagnosis-and-recovery-plan.md`、`22-deterministic-real-data-collector-plan.md`，默认不全量读取历史。

## 最新结论区

### 产品目标

DZCOM 是一个 AI 投资业务驾驶舱，目标是形成可配置、可复盘、可审计、可逐步自动成长的投资辅助闭环：

```text
可信数据源
  -> 采集与数据治理
  -> 投资报告
  -> Prompt / Skill / 模型治理
  -> Mock 组合与模拟交易
  -> 回测、反馈、风控审计
  -> 驾驶舱展示
  -> 数据源、Prompt、模型和 Skill 持续优化
```

### 核心边界

- AI 可以发现候选、生成报告、生成 Prompt/模型候选和评分。
- Mock 投资闭环可以自动执行。
- 正式启用新数据源、新 Prompt、新模型和真实交易必须保留人工确认或灰度开关。
- 数据质量不足时只能展示数据缺口、风险提示和下一步动作，不能伪装成确定投资建议。
- 前端体验必须围绕真实对象、真实状态、真实错误和真实空状态组织，不用假数据填平后端缺口。

### 核心对象关系

| 对象 | 主要职责 | 前后端契约要求 |
| --- | --- | --- |
| User / Role / Permission | 登录、权限、审计边界 | 前端展示权限状态；后端返回可解释的 401/403 上下文 |
| Product | 投资产品池、风险画像、行情归属 | 提供可搜索选择器能力，返回 display 字段 |
| DataSource | 数据源候选、审核、启用、健康状态 | 候选与启用分层；状态和拒绝原因结构化 |
| MarketQuote / News / Disclosure | 报告输入数据资产 | 不允许 fallback 冒充正式数据 |
| InvestmentTask | 采集、治理、报告、闭环任务 | 任务参数、状态、失败原因可结构化展示 |
| InvestmentReport | 投资分析、质量门禁、证据链 | 报告不是原始 JSON；应有指标、摘要、证据、风险 |
| AiPrompt / AiSkill / AiModel | AI 能力治理资产 | 生命周期、版本、评分、启用状态可审计 |
| MockPortfolio / MockOrder | 模拟交易与调仓 | 仅 Mock 自动化；真实交易不进入自动闭环 |
| Backtest / Feedback / RiskAudit | 复盘、反馈、风控审计 | 原因、结论、动作建议结构化展示 |
| ClosedLoopRun / ClosedLoopStep | 闭环运行证据 | 时间线、步骤状态、输入输出摘要可追踪 |

### 当前业务闭环验收口径

一个闭环功能只有同时满足以下条件，才算完成：

- 后端有明确 API 契约、DTO、状态、错误上下文和测试。
- 前端有 API client、类型、adapter 和结构化页面体验。
- 用户需要选择并回传的对象 id 均来自对象选择器或可搜索下拉。
- 数据不足、权限不足、风控拒绝、执行失败都能在 UI 结构化展示。
- 至少跑通一个真实链路冒烟，不用手填 id、硬编码、假数据或原始 JSON 绕过。

## 历史归档区

- 历史完整业务说明见 `20-investment-platform-business-specification.md`。
- AI 闭环诊断与回正方案见 `21-ai-investment-loop-diagnosis-and-recovery-plan.md`。
- 真实数据采集回正方案见 `22-deterministic-real-data-collector-plan.md`。
