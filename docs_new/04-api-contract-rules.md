# 04 API Contract Rules

生成日期：2026-06-28

本文定义前后端共同遵守的 API 契约规则。它不是完整 API 清单；每个功能只在任务相关章节补充关键接口摘要。

## 最新结论区

### 基础规则

- 默认使用真实后端 API、真实鉴权、真实错误和真实空状态。
- 接口字段必须有业务含义，不允许仅为了页面临时拼装而引入含混字段。
- 前端页面不得直接依赖数据库字段名或裸 JSON 字符串结构。
- 同一业务对象的 id、display、status、summary 命名应跨接口一致。

### 推荐响应结构

后端当前存在统一 Result/PageResult 口径时，应保持一致，并确保前端能稳定解包：

```json
{
  "code": "SUCCESS",
  "message": "ok",
  "data": {}
}
```

分页列表应包含：

```json
{
  "items": [],
  "total": 0,
  "pageNo": 1,
  "pageSize": 20
}
```

实际字段以当前后端公共类型为准，变更公共响应结构必须作为跨仓变更处理。

### 对象选择器接口

凡前端需要选择对象并回传业务 id，后端应提供查询接口：

| 字段 | 要求 |
| --- | --- |
| `keyword` | 支持名称、编码或摘要搜索 |
| `pageNo/pageSize` | 支持分页 |
| `sort` | 支持稳定排序或默认排序 |
| `status` | 支持按生命周期筛选 |
| `bizId` | 稳定业务 id |
| `displayName` | 前端可直接展示的名称 |
| `summary` | 可选摘要，例如来源、风险等级、最近更新时间 |

### 错误契约

错误响应至少要让前端识别：

- 是否未登录：401。
- 是否无权限：403。
- 是否对象不存在：404。
- 是否状态冲突或幂等冲突：409。
- 是否数据不足、质量门禁失败、风控拒绝：业务错误码 + 结构化上下文。
- 是否系统异常：500，避免泄漏内部敏感细节。

### 契约变更流程

1. 先在本文或相关功能 contract 写接口摘要：路径、用途、请求、响应、状态、错误、前端使用点。
2. 后端实现 Controller/Request/DTO/Service/DB/test。
3. 前端更新 API client、model、adapter、dictionary。
4. 前端页面只消费 adapter 后的展示模型。
5. 验证后将结果写回 `00-current-handoff.md`。

### 当前关键接口摘要

#### Product 列表/详情摘要字段

- 前端使用点：产品风险、报告工作台、模拟交易、数据质量和对象选择器相关页面。
- 后端响应：`ProductResponse`。
- 本轮确认字段：
  - `bizId`：产品业务 id。
  - `productName` / `productCode` / `displayName` 等既有展示字段按当前 DTO 为准。
  - `latestNav`：最近 1D 行情或净值。
  - `latestQuoteTime`：最近 1D 行情时间。
  - `sourceCode`：最近行情数据源编码。
  - `dataQualityScore`：产品画像或行情可用性综合质量分，范围 `0-1`。
- 契约要求：前端不得用 mock 或硬编码补这些字段；后端从真实 `MarketQuote` 和 `ProductInvestmentProfile` 组装，缺数据时返回空值并由页面展示真实空状态。
- 验收状态：2026-06-28 已补齐并通过后端全量测试、前端 type-check/build。

#### Investment Theme 选择器

- 前端使用点：报告工作台生成投资报告时选择 `themeCode`。
- 后端接口：`POST /api/investment/tasks/theme-options`。
- 请求字段：
  - `keyword`：可选，匹配主题编码或主题名称。
  - `marketScope`：可选，默认 `CN_MAINLAND`。
  - `page/size`：分页参数。
- 响应字段：`themeCode`、`themeName`、`displayName`、`marketScope`、`summary`、`latestSnapshotType`、`latestSnapshotTime`、`sampleCount`、`returnRate`、`momentumScore`、`heatScore`。
- 契约要求：选择项必须来自真实 `InvestmentThemeSnapshot`，前端不得手填 `themeCode`；允许清空选择代表全市场分析。
- 验收状态：2026-06-28 已补齐，真实数据可返回 `AI人工智能`、`半导体`、`黄金`，后端全量测试和前端 type-check/build 通过。

## 历史归档区

- 旧前端接口变更见 `10-frontend-interface-changes.md`、`12-frontend-api-update-log.md`、`19-frontend-api-update-log-20260626-ai-skills.md`。
- 后续默认只读本文最新结论区；完整接口细节按当前任务读取 Controller、DTO、前端 api/model 文件。
