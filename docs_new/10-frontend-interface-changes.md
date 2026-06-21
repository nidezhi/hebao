# 前端接口变更说明

## 1. 文档目的

本文档整理本轮投资任务、资讯热度、主题快照和投资分析相关接口变化。

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

## 3. 新增接口：资讯主题产品关联查询

### 3.1 接口信息

```text
POST /api/investment/tasks/article-relations/list
```

前端使用场景：

- 在资讯热度详情页展示某个主题热度由哪些新闻支撑。
- 在产品详情页展示某个产品被哪些主题新闻命中。
- 在投资分析报告中点击新闻热度，展开命中关键词和证据。
- 给图表事件点提供可解释弹窗。

### 3.2 请求 JSON

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

### 3.3 响应 JSON

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

## 4. 变更接口：投资方向快照分页

### 4.1 接口信息

```text
POST /api/investment/tasks/snapshots/list
```

### 4.2 请求 JSON

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

### 4.3 响应核心字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `snapshotType` | string | `RETURN`、`MOMENTUM`、`NEWS_HEAT` |
| `returnRate` | number | 收益快照或动量快照的平均收益率 |
| `momentumScore` | number | 动量分 |
| `heatScore` | number | 加权资讯热度分 |
| `metrics` | string | JSON 字符串，前端必须按 `snapshotType` 展开 |

### 4.4 `metrics`：RETURN 结构

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

### 4.5 `metrics`：MOMENTUM 结构

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

### 4.6 `metrics`：NEWS_HEAT 结构

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

## 5. 变更接口：任务配置保存和查询

### 5.1 影响接口

```text
POST /api/investment/tasks/definitions
POST /api/investment/tasks/definitions/save
POST /api/investment/tasks/trigger
```

### 5.2 `NEWS_HEAT_AGGREGATION` 参数新增约定

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

## 6. 变更接口：生成投资分析报告

### 6.1 接口信息

```text
POST /api/investment/analysis/generate
```

### 6.2 请求 JSON

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

## 7. 变更接口：投资分析报告列表

### 7.1 接口信息

```text
POST /api/investment/analysis/reports/list
```

该接口返回的报告结构与 `/generate` 一致，适合前端做历史报告列表和详情页。

## 8. 投资分析报告 JSON 展开说明

### 8.1 `investmentSummary`

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

### 8.2 `trend`

```json
{
  "direction": "UP",
  "averageReturn": 0.035,
  "averageMomentum": 0.018,
  "newsHeat": 12,
  "weightedHeatScore": 18.5,
  "dataQualityScore": 0.78,
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

### 8.3 `investmentPlan`

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

前端展示建议：

- `referenceAllocationRate` 展示为建议仓位。
- `referenceAllocationAmount` 展示为模拟配置金额。
- `riskNotice` 必须在报告详情页展示。

### 8.4 `simulatedReturn`

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
  "assumption": "按回看窗口平均收益率模拟，仅反映历史样本，不代表未来收益。"
}
```

前端展示建议：

- 用三栏展示基准、压力、乐观情景。
- `estimatedFinalCapital` 是初始资金加模拟收益，不是实际账户资产。
- `assumption` 必须展示，避免用户误解为收益承诺。

### 8.5 `chartPayload`

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

## 9. 前端页面建议

| 页面 | 建议接口 |
| --- | --- |
| 投资任务配置页 | `/definitions`、`/definitions/save` |
| 任务执行记录页 | `/executions/list` |
| 新闻列表页 | `/articles/list` |
| 新闻关联证据页 | `/article-relations/list` |
| 主题快照榜单页 | `/snapshots/list` |
| 投资分析生成页 | `/analysis/generate` |
| 投资分析历史页 | `/analysis/reports/list` |

## 10. 本轮接口结论

本轮后端优化已经具备前端可见出口：

- 统计质量通过 `metrics` 暴露。
- 新闻和主题、产品关联通过 `/article-relations/list` 暴露。
- 投资模拟方案通过 `investmentPlan` 和 `simulatedReturn` 暴露。
- 图表数据通过 `chartPayload` 暴露。
- 数据质量和风险提示在投资分析报告中可直接展示。
