# 前端接口变更说明

> 2026-06-26 更新：旧 RSS/fallback/手工 endpoint 采集配置已退出默认主链路。前端应优先参考 `docs_new/19-frontend-api-update-log-20260626-ai-skills.md` 生成 AI Skill、模型 Skill 绑定和 AI 数据源发现页面。

## 1. 文档目的

本文档整理本轮投资任务、资讯热度、主题快照和投资分析相关接口变化。

投资平台后续闭环建设以 `docs_new/11-investment-platform-closed-loop-plan.md` 为准。当前接口文档只描述已暴露给前端的能力，不能替代产品池、数据源、Prompt 版本和 Mock 交易闭环。

按照开发铁律，任何后端统计和分析优化都必须能被前端展示、查询或生成图表。因此本文档重点说明：

1. 哪些接口发生变化。
2. 前端可以展示哪些字段。
3. JSON 字符串字段应如何展开。
4. 哪些字段可以用于列表、详情、筛选、排序和图表。

## 2. 接口变化总览

| 接口 | 变化类型 | 前端用途 |
| --- | --- | --- |
| `POST /api/investment/tasks/article-relations/list` | 新增 | 查询新闻、投资主题和产品代码的显式关联 |
| `POST /api/investment/tasks/snapshots/list` | 响应增强 | `metrics` 增加质量评估、样本覆盖、热度来源等可解释字段 |
| `POST /api/investment/tasks/definitions/save` | 参数约定增强 | `NEWS_HEAT_AGGREGATION` 支持 `themeProducts` 参数 |
| `POST /api/investment/tasks/definitions` | 响应内容增强 | 可看到 `themeProducts` 任务参数 |
| `POST /api/investment/tasks/trigger` | 参数约定增强 | 手动触发 `NEWS_HEAT_AGGREGATION` 时可临时覆盖 `themeProducts` |
| `POST /api/investment/analysis/generate` | 响应增强 | 分析报告增加数据质量、参考配置比例、压力/乐观模拟收益 |
| `POST /api/investment/analysis/reports/list` | 响应增强 | 分页查询报告时返回同样增强后的 JSON 字段 |
| `POST /api/admin/products/investment-profile/save` | 新增 | 后台维护产品投资画像和主题关系 |
| `POST /api/products/detail` | 响应增强 | 返回产品投资画像 `investmentProfile` 和主题关系 `themeRelations` |
| `POST /api/admin/data-sources/save` | 新增 | 后台维护数据源注册信息、来源等级和启用状态 |
| `POST /api/admin/data-sources/health/save` | 新增 | 后台或采集任务写入数据源健康状态 |
| `POST /api/admin/data-sources/quality/save` | 新增 | 后台或采集任务写入数据质量快照 |
| `POST /api/admin/data-sources/list` | 新增 | 数据源看板列表，展示健康状态和最新质量快照 |
| `POST /api/admin/data-sources/quality/list` | 新增 | 数据源质量趋势图和质量历史 |
| `POST /api/ai/prompts/save` | 新增 | 保存 Prompt 模板版本、变量定义和输出 Schema |
| `POST /api/ai/prompts/list` | 新增 | 分页查询 Prompt 版本，用于后台 Prompt 管理 |
| `POST /api/ai/prompts/detail` | 新增 | 查询 Prompt 模板、变量和输出 Schema 详情 |
| `POST /api/ai/prompts/status` | 新增 | 变更 Prompt 生命周期状态，支持启用、停用和回滚 |
| `POST /api/ai/prompts/preview` | 新增 | 本地渲染 Prompt 预览，校验必填变量但不调用真实模型 |
| `POST /api/backtests/save` | 新增 | 保存外部或后台计算的回测结果 |
| `POST /api/backtests/generate-from-portfolio` | 新增 | 从 Mock 组合估值曲线生成回测摘要 |
| `POST /api/backtests/detail` | 新增 | 查询回测详情 |
| `POST /api/backtests/list` | 新增 | 分页查询回测结果 |
| `POST /api/ai/feedback/save` | 新增 | 保存用户采纳、拒绝、观察或忽略反馈 |
| `POST /api/ai/feedback/detail` | 新增 | 查询反馈详情 |
| `POST /api/ai/feedback/list` | 新增 | 分页查询反馈历史 |
| `POST /api/ai/prompt-evaluations/save` | 新增 | 保存 Prompt 评估 |
| `POST /api/ai/prompt-evaluations/detail` | 新增 | 查询 Prompt 评估详情 |
| `POST /api/ai/prompt-evaluations/list` | 新增 | 分页查询 Prompt 评估 |
| `POST /api/mock/portfolios/create` | 新增 | 创建当前用户模拟组合并生成初始现金估值 |
| `POST /api/mock/portfolios/mine` | 新增 | 查询当前用户模拟组合列表 |
| `POST /api/mock/portfolios/detail` | 新增 | 查询模拟组合详情、最新估值和持仓 |
| `POST /api/mock/portfolios/orders/buy` | 新增 | 按最新行情模拟买入，返回订单、成交和更新后的组合 |
| `POST /api/mock/portfolios/orders/sell` | 新增 | 按最新行情模拟卖出，返回订单、成交和更新后的组合 |
| `POST /api/mock/portfolios/orders/cancel` | 新增 | 撤销非终态模拟订单，即时成交订单会明确拒绝 |
| `POST /api/mock/portfolios/orders/events` | 新增 | 查询模拟订单生命周期事件和交易审计 |
| `POST /api/mock/portfolios/orders/buy-from-report` | 新增 | 根据投资分析报告自动生成模拟买入 |
| `POST /api/mock/portfolios/rebalance/execute` | 新增 | 按目标权重执行模拟再平衡 |
| `POST /api/mock/portfolios/valuations/refresh` | 新增 | 按最新行情刷新模拟组合估值 |
| `POST /api/mock/portfolios/performance/curve` | 新增 | 查询模拟组合收益曲线和最大回撤 |
| `POST /api/risk/checks/list` | 新增 | 查询风控检查和 Mock 交易拦截原因 |

本轮阶段 0 止损字段：

| 字段 | 接口 | 前端用途 |
| --- | --- | --- |
| `confidenceLevel` | `/generate`、`/reports/list` | 报告可信等级，列表页必须展示 |
| `dataQualityScore` | `/generate`、`/reports/list` | 报告输入数据质量分，列表页可排序 |
| `dataQualityGate` | `/generate`、`/reports/list` | 数据质量门禁详情，详情页展示降级原因和允许动作 |

## 3. 新增接口：保存产品投资画像

### 3.1 接口信息

```text
POST /api/admin/products/investment-profile/save
```

前端使用场景：

- 产品后台维护产品风险摘要、资产类别、波动等级和流动性等级。
- 配置产品是否允许进入 Mock 交易。
- 建立产品与主题、行业、指数和资产类别的显式关系。
- 为后续投资报告和 Mock 交易提供产品风险依据。

### 3.2 请求 JSON

```json
{
  "productBizId": "product-1",
  "assetClass": "ETF",
  "riskSummary": "跟踪人工智能主题，波动较高，适合中高风险用户观察配置。",
  "volatilityLevel": "HIGH",
  "liquidityLevel": "MEDIUM",
  "maxDrawdown": 0.18,
  "suitableRiskLevel": 4,
  "mockTradable": true,
  "minHoldingDays": 30,
  "tradingNotes": "仅用于模拟交易，不代表真实可交易。",
  "dataQualityScore": 0.75,
  "relations": [
    {
      "relationType": "THEME",
      "relationCode": "AI人工智能",
      "relationName": "AI人工智能",
      "relationWeight": 0.8,
      "sourceCode": "MANUAL",
      "evidence": "后台人工维护的主题关系"
    },
    {
      "relationType": "ASSET_CLASS",
      "relationCode": "ETF",
      "relationName": "ETF基金",
      "relationWeight": 1.0,
      "sourceCode": "MANUAL",
      "evidence": "产品资产类别"
    }
  ]
}
```

字段说明：

| 字段 | 类型 | 是否必填 | 说明 |
| --- | --- | --- | --- |
| `productBizId` | string | 是 | 产品业务 ID |
| `assetClass` | string | 是 | 资产类别：`STOCK`、`ETF`、`FUND`、`BOND`、`BANK_WMP`、`GOLD`、`REIT` 等 |
| `riskSummary` | string | 否 | 风险摘要，产品详情页直接展示 |
| `volatilityLevel` | string | 否 | 波动等级：`LOW`、`MEDIUM`、`HIGH` |
| `liquidityLevel` | string | 否 | 流动性等级：`LOW`、`MEDIUM`、`HIGH` |
| `maxDrawdown` | number | 否 | 最大回撤，小数形式 |
| `suitableRiskLevel` | number | 否 | 适配用户风险等级，1-5 |
| `mockTradable` | boolean | 否 | 是否允许进入 Mock 交易 |
| `minHoldingDays` | number | 否 | 建议最短持有天数 |
| `tradingNotes` | string | 否 | 交易约束说明 |
| `dataQualityScore` | number | 否 | 产品画像质量分，0-1 |
| `relations` | array | 否 | 产品与主题、行业、指数、资产类别的关系集合 |

`relations` 字段说明：

| 字段 | 类型 | 是否必填 | 说明 |
| --- | --- | --- | --- |
| `relationType` | string | 是 | `THEME`、`INDUSTRY`、`INDEX`、`ASSET_CLASS` |
| `relationCode` | string | 是 | 关系稳定编码 |
| `relationName` | string | 是 | 前端展示名称 |
| `relationWeight` | number | 否 | 关系权重，0-1 |
| `sourceCode` | string | 否 | 来源编码，默认 `MANUAL` |
| `evidence` | string | 否 | 关系证据摘要 |

### 3.3 响应变化：产品详情

`POST /api/products/detail` 和保存接口都会返回 `ProductResponse`，新增：

```json
{
  "investmentProfile": {
    "assetClass": "ETF",
    "riskSummary": "跟踪人工智能主题，波动较高，适合中高风险用户观察配置。",
    "volatilityLevel": "HIGH",
    "liquidityLevel": "MEDIUM",
    "maxDrawdown": 0.18,
    "suitableRiskLevel": 4,
    "mockTradable": true,
    "minHoldingDays": 30,
    "tradingNotes": "仅用于模拟交易，不代表真实可交易。",
    "dataQualityScore": 0.75
  },
  "themeRelations": [
    {
      "relationType": "THEME",
      "relationCode": "AI人工智能",
      "relationName": "AI人工智能",
      "relationWeight": 0.8,
      "sourceCode": "MANUAL",
      "evidence": "后台人工维护的主题关系"
    }
  ]
}
```

前端要求：

- 产品详情页必须展示 `investmentProfile`。
- `mockTradable=false` 时隐藏 Mock 交易入口。
- `dataQualityScore < 0.45` 时展示“产品画像质量不足”。
- `themeRelations` 用标签或分组列表展示，不能只展示原始 JSON。

### 3.4 字段展开说明

`investmentProfile` 是产品进入投资分析和 Mock 交易前必须展示的风险画像，不是后台内部配置。

| 字段 | 类型 | 前端展示建议 |
| --- | --- | --- |
| `assetClass` | string | 资产类别标签，用于产品筛选、资产分布图和组合占比图 |
| `riskSummary` | string | 产品详情页风险说明，建议放在风险区域首行 |
| `volatilityLevel` | string | 波动等级标签，`HIGH` 需要高亮风险 |
| `liquidityLevel` | string | 流动性等级标签，用于判断赎回或模拟卖出限制 |
| `maxDrawdown` | number | 最大回撤，可展示为百分比 |
| `suitableRiskLevel` | number | 适配用户风险等级，后续和用户画像匹配 |
| `mockTradable` | boolean | Mock 交易入口开关，`false` 时禁止创建模拟持仓 |
| `minHoldingDays` | number | 最短建议持有天数，Mock 交易和前端提示共同使用 |
| `tradingNotes` | string | 交易限制、费用、赎回或人工确认说明 |
| `dataQualityScore` | number | 产品画像质量分，低于阈值时只允许展示风险提示 |

`themeRelations` 是产品与投资主题、行业、指数和资产类别的显式关系。

| 字段 | 类型 | 前端展示建议 |
| --- | --- | --- |
| `relationType` | string | 分组标题，区分主题、行业、指数、资产类别 |
| `relationCode` | string | 稳定筛选值，可用于跳转主题页或图表筛选 |
| `relationName` | string | 前端展示名称 |
| `relationWeight` | number | 关系权重，可展示为进度条或排序依据 |
| `sourceCode` | string | 来源标签，区分人工维护、官方来源或后续模型抽取 |
| `evidence` | string | 关系证据，详情页可折叠展示 |

## 4. 新增接口：资讯主题产品关联查询

### 4.1 接口信息

```text
POST /api/investment/tasks/article-relations/list
```

前端使用场景：

- 在资讯热度详情页展示某个主题热度由哪些新闻支撑。
- 在产品详情页展示某个产品被哪些主题新闻命中。
- 在投资分析报告中点击新闻热度，展开命中关键词和证据。
- 给图表事件点提供可解释弹窗。

### 4.2 请求 JSON

```json
{
  "articleBizId": "article-1",
  "themeCode": "AI人工智能",
  "productCode": "159819",
  "relationType": "KEYWORD_MATCH",
  "page": 1,
  "size": 20,
  "sort": "relationScore",
  "direction": "desc"
}
```

字段说明：

| 字段 | 类型 | 是否必填 | 说明 |
| --- | --- | --- | --- |
| `articleBizId` | string | 否 | 资讯业务 ID，来自资讯列表 `bizId` |
| `themeCode` | string | 否 | 投资主题编码 |
| `productCode` | string | 否 | 产品代码；为空时查询主题级和产品级关联 |
| `relationType` | string | 否 | 关联类型：`KEYWORD_MATCH`、`MANUAL`、`MODEL_EXTRACTED` |
| `page` | number | 否 | 页码，默认 `1` |
| `size` | number | 否 | 每页条数，默认 `20` |
| `sort` | string | 否 | 排序字段：`createdAt`、`relationScore`、`sourceQualityScore`、`themeCode`、`productCode` |
| `direction` | string | 否 | 排序方向：`asc`、`desc`，默认 `desc` |

### 4.3 响应 JSON

```json
{
  "code": "SUCCESS",
  "message": "成功",
  "data": {
    "items": [
      {
        "bizId": "relation-1",
        "articleBizId": "article-1",
        "themeCode": "AI人工智能",
        "themeName": "AI人工智能",
        "productCode": "159819",
        "relationType": "KEYWORD_MATCH",
        "matchedKeywords": ["AI", "人工智能", "算力"],
        "sourceQualityScore": 1.0,
        "relationScore": 3.0,
        "evidence": "AI 算力产业链投资热度提升",
        "createdAt": "2026-06-21T10:00:00"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 20,
    "totalPages": 1
  }
}
```

响应字段说明：

| 字段 | 类型 | 前端展示建议 |
| --- | --- | --- |
| `bizId` | string | 关联记录唯一 ID |
| `articleBizId` | string | 可用于跳转或回查新闻详情 |
| `themeCode` | string | 主题筛选和分组 |
| `themeName` | string | 主题展示名称 |
| `productCode` | string | 产品代码；空字符串表示主题级关联 |
| `relationType` | string | 显示关联来源，如关键词匹配 |
| `matchedKeywords` | string[] | 标签形式展示命中关键词 |
| `sourceQualityScore` | number | 来源质量，可做评分条 |
| `relationScore` | number | 关联强度，可用于排序 |
| `evidence` | string | 证据摘要，当前主要是新闻标题 |
| `createdAt` | datetime | 关联生成时间，北京时间 |

## 5. 变更接口：投资方向快照分页

### 5.1 接口信息

```text
POST /api/investment/tasks/snapshots/list
```

### 5.2 请求 JSON

```json
{
  "taskCode": "news-heat-aggregation",
  "snapshotType": "NEWS_HEAT",
  "themeCode": "AI人工智能",
  "marketScope": "CN_MAINLAND",
  "snapshotFrom": "2026-06-21T00:00:00",
  "snapshotTo": "2026-06-21T23:59:59",
  "page": 1,
  "size": 20,
  "sort": "snapshotTime",
  "direction": "desc"
}
```

### 5.3 响应核心字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `snapshotType` | string | `RETURN`、`MOMENTUM`、`NEWS_HEAT` |
| `returnRate` | number | 收益快照或动量快照的平均收益率 |
| `momentumScore` | number | 动量分 |
| `heatScore` | number | 加权资讯热度分 |
| `metrics` | string | JSON 字符串，前端必须按 `snapshotType` 展开 |

### 5.4 `metrics`：RETURN 结构

```json
{
  "requestedProductCount": 3,
  "validReturnCount": 3,
  "coverageRate": 1.0,
  "volatility": 0.0123,
  "topContribution": 0.45,
  "qualityScore": 0.86,
  "qualityLevel": "HIGH",
  "performances": [
    {
      "productBizId": "product-1",
      "productCode": "159819",
      "startPrice": 1.2,
      "endPrice": 1.26,
      "returnRate": 0.05
    }
  ]
}
```

前端展示建议：

- `coverageRate` 展示为样本覆盖率。
- `qualityLevel` 展示为质量标签。
- `performances` 展示产品收益明细表。
- `returnRate` 绘制收益趋势线。

### 5.5 `metrics`：MOMENTUM 结构

```json
{
  "requestedProductCount": 3,
  "validReturnCount": 3,
  "positiveCount": 2,
  "positiveBreadth": 0.6667,
  "coverageRate": 1.0,
  "volatility": 0.014,
  "qualityScore": 0.82,
  "qualityLevel": "HIGH",
  "performances": [
    {
      "productBizId": "product-1",
      "productCode": "159819",
      "returnRate": 0.05
    }
  ]
}
```

前端展示建议：

- `positiveBreadth` 展示上涨广度。
- `momentumScore` 绘制动量趋势线。
- `volatility` 展示主题内部分化程度。

### 5.6 `metrics`：NEWS_HEAT 结构

```json
{
  "themeName": "AI人工智能",
  "keywords": ["AI", "人工智能", "算力"],
  "articleCount": 12,
  "uniqueSourceCount": 4,
  "averageSourceQuality": 0.9,
  "dataQualityScore": 0.78,
  "qualityLevel": "HIGH",
  "heatScore": 18.5,
  "sampleArticles": [
    {
      "articleBizId": "article-1",
      "title": "AI 算力产业链投资热度提升",
      "sourceCode": "CN_MAINLAND_NEWS",
      "publishTime": "2026-06-21T10:00:00"
    }
  ]
}
```

前端展示建议：

- `heatScore` 绘制资讯热度趋势线。
- `qualityLevel` 标识热度可信度。
- `sampleArticles` 做热度来源弹窗。
- 点击 `articleBizId` 后可结合 `/articles/list` 和 `/article-relations/list` 展示完整新闻和关联证据。

## 6. 变更接口：任务配置保存和查询

### 6.1 影响接口

```text
POST /api/investment/tasks/definitions
POST /api/investment/tasks/definitions/save
POST /api/investment/tasks/trigger
```

### 6.2 `NEWS_HEAT_AGGREGATION` 参数新增约定

```json
{
  "code": "news-heat-aggregation",
  "type": "NEWS_HEAT_AGGREGATION",
  "cron": "45 */10 * * * *",
  "zone": "Asia/Shanghai",
  "enabled": true,
  "parameters": {
    "windowMinutes": "1440",
    "marketScope": "CN_MAINLAND",
    "themes": "AI人工智能=AI,人工智能,算力;半导体=半导体,芯片,集成电路;黄金=黄金,金价,贵金属",
    "themeProducts": "AI人工智能=159819,588000,515980;半导体=512480,159995,688981;黄金=518880,159934"
  },
  "description": "中国大陆主题资讯热度汇总"
}
```

字段说明：

| 参数 | 说明 |
| --- | --- |
| `themes` | 主题与关键词映射，用于查新闻 |
| `themeProducts` | 主题与产品代码映射，用于生成新闻-主题-产品关联 |
| `marketScope` | 市场范围，当前默认 `CN_MAINLAND` |
| `windowMinutes` | 统计回看窗口分钟数 |

## 7. 变更接口：生成投资分析报告

### 7.1 接口信息

```text
POST /api/investment/analysis/generate
```

### 7.2 请求 JSON

```json
{
  "providerCode": "LOCAL_RULE",
  "modelCode": "local-rule-analysis",
  "marketScope": "CN_MAINLAND",
  "themeCode": "AI人工智能",
  "lookbackDays": 30,
  "initialCapital": 100000
}
```

说明：

- 实际 Provider 由 `ACTIVE` 状态的 AI 模型配置决定。
- `providerCode` 只做一致性校验，不能绕过模型表直接选择 Provider。
- `initialCapital` 用于模拟收益，不会触发真实交易。

## 8. 变更接口：投资分析报告列表

### 8.1 接口信息

```text
POST /api/investment/analysis/reports/list
```

该接口返回的报告结构与 `/generate` 一致，适合前端做历史报告列表和详情页。

## 9. 投资分析报告 JSON 展开说明

### 9.0 报告级止损字段

投资分析报告响应现在新增三个一等字段，不能只在 JSON 内部隐藏：

```json
{
  "confidenceLevel": "LOW_CONFIDENCE",
  "dataQualityScore": 0.35,
  "dataQualityGate": {
    "passed": false,
    "confidenceLevel": "LOW_CONFIDENCE",
    "dataQualityScore": 0.35,
    "snapshotCount": 3,
    "newsCount": 1,
    "fallbackNewsRatio": 0.5,
    "reasons": ["LOW_DATA_QUALITY", "FALLBACK_NEWS_RATIO_TOO_HIGH"],
    "displayMessage": "数据质量不足，当前仅展示数据缺口，不生成投资配置建议。",
    "allowedActions": ["VIEW_REPORT", "SHOW_DATA_GAP"]
  }
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `confidenceLevel` | string | `HIGH_CONFIDENCE`、`MEDIUM_CONFIDENCE`、`LOW_CONFIDENCE`、`UNUSABLE` |
| `dataQualityScore` | number | 报告输入质量分，0-1 |
| `dataQualityGate.passed` | boolean | 是否允许生成参考投资方案 |
| `dataQualityGate.snapshotCount` | number | 快照样本数量 |
| `dataQualityGate.newsCount` | number | 相关新闻数量 |
| `dataQualityGate.fallbackNewsRatio` | number | 兜底资讯占比 |
| `dataQualityGate.reasons` | string[] | 降级原因编码 |
| `dataQualityGate.displayMessage` | string | 前端直接展示的中文提示 |
| `dataQualityGate.allowedActions` | string[] | 允许动作；低质量时不包含 `GENERATE_PROMPT` 和 `MOCK_TRADE` |

前端要求：

- 报告列表页展示 `confidenceLevel` 和 `dataQualityScore`。
- 报告详情页展示 `dataQualityGate.displayMessage` 和 `reasons`。
- `dataQualityGate.passed=false` 时，隐藏生成 Prompt、生成 Mock 交易和配置建议入口。

### 9.1 `investmentSummary`

```json
{
  "marketScope": "CN_MAINLAND",
  "themeCode": "AI人工智能",
  "sampleCount": 30,
  "newsCount": 12,
  "averageReturn": 0.035,
  "averageMomentum": 0.018,
  "averageHeat": 18.5,
  "dataQualityScore": 0.78,
  "dataQualityLevel": "HIGH",
  "confidenceLevel": "HIGH_CONFIDENCE",
  "dataQualityPassed": true,
  "dataGapReasons": [],
  "latestSnapshotTime": "2026-06-21T10:00:00",
  "recentNews": [
    {
      "title": "AI 算力产业链投资热度提升",
      "summary": "人工智能应用带动算力需求",
      "publishTime": "2026-06-21T10:00:00",
      "sourceCode": "CN_MAINLAND_NEWS"
    }
  ]
}
```

前端展示建议：

- 顶部摘要卡展示 `averageReturn`、`averageMomentum`、`averageHeat`、`dataQualityLevel`。
- 新闻列表展示 `recentNews`。

### 9.2 `trend`

```json
{
  "direction": "UP",
  "averageReturn": 0.035,
  "averageMomentum": 0.018,
  "newsHeat": 12,
  "weightedHeatScore": 18.5,
  "dataQualityScore": 0.78,
  "confidenceLevel": "HIGH_CONFIDENCE",
  "dataQualityPassed": true,
  "lookbackDays": 30
}
```

字段说明：

| 字段 | 说明 |
| --- | --- |
| `direction` | 趋势方向：`UP`、`NEUTRAL`、`DOWN` |
| `averageReturn` | 平均收益率 |
| `averageMomentum` | 平均动量 |
| `newsHeat` | 新闻数量 |
| `weightedHeatScore` | 加权热度分 |
| `dataQualityScore` | 输入数据质量分 |
| `lookbackDays` | 回看天数 |

### 9.3 `investmentPlan`

```json
{
  "planType": "REFERENCE_ALLOCATION",
  "suggestedAction": "关注并分批配置",
  "referenceAllocationRate": 0.3,
  "referenceAllocationAmount": 30000,
  "dataQualityLevel": "HIGH",
  "rebalanceRule": "仅当收益、动量和资讯热度连续两个统计窗口同向时提高仓位。",
  "riskNotice": "AI 分析仅为投资辅助信息，不构成收益承诺或自动交易指令。"
}
```

低质量数据时，`investmentPlan` 会降级为数据缺口报告：

```json
{
  "planType": "DATA_GAP_REPORT",
  "suggestedAction": "数据质量不足，当前仅展示数据缺口，不生成投资配置建议。",
  "referenceAllocationRate": 0,
  "referenceAllocationAmount": 0,
  "dataQualityLevel": "LOW",
  "confidenceLevel": "LOW_CONFIDENCE",
  "dataGapReasons": ["LOW_DATA_QUALITY"],
  "rebalanceRule": "数据质量未达标，禁止生成调仓或配置建议。",
  "riskNotice": "当前报告仅用于暴露数据缺口，不构成投资建议。"
}
```

前端展示建议：

- `referenceAllocationRate` 展示为建议仓位。
- `referenceAllocationAmount` 展示为模拟配置金额。
- `riskNotice` 必须在报告详情页展示。

### 9.4 `simulatedReturn`

```json
{
  "initialCapital": 100000,
  "allocationRate": 0.3,
  "simulatedPrincipal": 30000,
  "estimatedProfit": 1050,
  "estimatedFinalCapital": 101050,
  "returnRate": 0.035,
  "stressLoss": -450,
  "optimisticProfit": 1950,
  "confidenceLevel": "HIGH_CONFIDENCE",
  "dataQualityPassed": true,
  "assumption": "按回看窗口平均收益率模拟，仅反映历史样本，不代表未来收益。"
}
```

前端展示建议：

- 用三栏展示基准、压力、乐观情景。
- `estimatedFinalCapital` 是初始资金加模拟收益，不是实际账户资产。
- `assumption` 必须展示，避免用户误解为收益承诺。

### 9.5 `chartPayload`

```json
{
  "series": [
    {
      "time": "2026-06-21T10:00:00",
      "snapshotType": "RETURN",
      "returnRate": 0.035,
      "momentumScore": 0,
      "heatScore": 0
    },
    {
      "time": "2026-06-21T10:10:00",
      "snapshotType": "NEWS_HEAT",
      "returnRate": 0,
      "momentumScore": 0,
      "heatScore": 18.5
    }
  ],
  "news": [
    {
      "time": "2026-06-21T10:00:00",
      "title": "AI 算力产业链投资热度提升",
      "sourceCode": "CN_MAINLAND_NEWS"
    }
  ]
}
```

前端展示建议：

- `series.returnRate` 绘制收益线。
- `series.momentumScore` 绘制动量线。
- `series.heatScore` 绘制资讯热度线。
- `news` 用作图表事件标记。

## 10. 前端页面建议

| 页面 | 建议接口 |
| --- | --- |
| 产品管理页 | `/api/admin/products/create`、`/api/admin/products/update`、`/api/admin/products/investment-profile/save`、`/api/products/detail` |
| 投资任务配置页 | `/definitions`、`/definitions/save` |
| 任务执行记录页 | `/executions/list` |
| 新闻列表页 | `/articles/list` |
| 新闻关联证据页 | `/article-relations/list` |
| 主题快照榜单页 | `/snapshots/list` |
| 投资分析生成页 | `/analysis/generate` |
| 投资分析历史页 | `/analysis/reports/list` |

## 11. 本轮接口结论

本轮后端优化已经具备前端可见出口：

- 产品投资画像通过 `/api/admin/products/investment-profile/save` 写入，通过 `/api/products/detail` 的 `investmentProfile` 和 `themeRelations` 展示。
- 统计质量通过 `metrics` 暴露。
- 新闻和主题、产品关联通过 `/article-relations/list` 暴露。
- 投资模拟方案通过 `investmentPlan` 和 `simulatedReturn` 暴露。
- 图表数据通过 `chartPayload` 暴露。
- 数据质量和风险提示在投资分析报告中可直接展示。

## 12. 回测、反馈和 Prompt 评估闭环接入说明

### 12.1 回测结果

接口：

```text
POST /api/backtests/save
POST /api/backtests/generate-from-portfolio
POST /api/backtests/detail
POST /api/backtests/list
```

前端注意：

- `/api/backtests/save` 的 `status` 可不传，后端默认保存为 `PENDING`。
- 传入 `bizId` 更新回测时，后端会校验该回测必须属于当前用户；否则返回 `403`。
- `/api/backtests/generate-from-portfolio` 会校验 Mock 组合归属当前用户；`parameters` 可为空，也可传 JSON。
- 生成回测摘要时，前端传入的 `parameters` 会写入响应 `parameters.clientParameters`，用于复盘当时的筛选、调仓或展示参数。
- `/detail` 只允许查看当前用户可见的回测结果；列表天然按当前用户过滤。

### 12.2 投资反馈

接口：

```text
POST /api/ai/feedback/save
POST /api/ai/feedback/detail
POST /api/ai/feedback/list
```

前端注意：

- `feedbackAction` 取值为 `ADOPT`、`REJECT`、`WATCH`、`IGNORE`。
- `targetType` 取值为 `REPORT`、`RECOMMENDATION`、`MOCK_ORDER`、`MOCK_PORTFOLIO`、`BACKTEST`、`PROMPT`。
- 保存反馈时如果携带 `backtestBizId`，后端会校验该回测属于当前用户。
- 携带 `promptCode` 和 `promptVersion` 时，后端会自动生成一条 `FEEDBACK_LOOP` 场景的 Prompt 评估记录。
- `/detail` 只允许查看当前用户自己的反馈记录；越权返回 `403`。

### 12.3 Prompt 评估

接口：

```text
POST /api/ai/prompt-evaluations/save
POST /api/ai/prompt-evaluations/detail
POST /api/ai/prompt-evaluations/list
```

前端注意：

- `reviewStatus` 可不传，后端默认 `PENDING`；允许值为 `PENDING`、`APPROVED`、`REJECTED`、`ARCHIVED`。
- `score` 为 0 到 1 的小数。
- 保存评估时，如果携带 `backtestBizId` 或 `feedbackBizId`，后端会校验关联数据对当前用户可见。
- `/detail` 只允许评估人本人、关联反馈所有者或关联回测所有者查看；越权返回 `403`。

## 13. 高质量数据任务和自动报告接入说明

本轮新增 V17 任务基线，前端任务配置页需要关注：

- 历史旧任务会被停用，新任务以 `l1-`、`l2-`、`cn-mainland-` 和 `auto-openai-` 开头。
- `INVESTMENT_NEWS_COLLECTION` 支持 `fallbackEnabled=false`，表示外部源无数据时不写入兜底资讯。
- 新增专用任务类型 `REGULATORY_DISCLOSURE_COLLECTION`、`EXCHANGE_ANNOUNCEMENT_COLLECTION`、`WEALTH_PRODUCT_NAV_REFRESH`。
- 专用任务配置页需要支持 `endpoints`、`responseFormat`、`itemsPath`、`externalIdPath`、`titlePath`、`summaryPath`、`contentPath`、`urlPath`、`publishTimePath`、`extraFieldPaths`、`includeKeywords`、`timeoutSeconds`、`freshnessHours`。
- `WEALTH_PRODUCT_NAV_REFRESH` 还需要支持 `productMarketCode`、`productCurrency`、`quoteInterval`、`defaultRiskLevel`，并提示 `extraFieldPaths` 中 `productCode/productName/nav` 是关键字段。
- 专用任务执行后会更新 `/api/admin/data-sources/list` 中的健康状态和最新质量快照。
- 新任务类型 `AUTO_INVESTMENT_REPORT_GENERATION` 用于自动生成投资报告。
- 自动报告任务默认参数：
  - `providerCode=OPENAI_COMPATIBLE`
  - `modelCode=openai-compatible-analysis`
  - `marketScope=CN_MAINLAND`
  - `lookbackDays=30`
  - `initialCapital=100000`
- OpenAI 模型仍通过 `/api/ai/models/save`、`/api/ai/models/list`、`/api/ai/models/status` 前端可配置。
- `openai-compatible-analysis` 默认 `secretRef=OPENAI_MOCK_API_KEY` 且 `mockEnabled=true`，不会调用外部网络；前端或运营配置为 `mockEnabled=false` 时，需要后端按当前模型 `secretRef` 配置同名密钥后才会真实调用。
