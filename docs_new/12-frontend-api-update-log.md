# 前端接口更新记录

## 1. 文档目的

本文档专门记录后端新增或修改的前端接口，作为前端备用索引。

详细字段展开仍以 `docs_new/10-frontend-interface-changes.md` 为准；本文档只记录每轮新增、变更、废弃和前端接入注意事项。

## 2. 记录规则

每次后端接口发生变化，必须记录：

1. 接口路径。
2. 新增、变更或废弃类型。
3. 请求 JSON。
4. 响应核心 JSON。
5. 前端页面用途。
6. 前端开关、降级或隐藏逻辑。

## 3. 2026-06-23：模拟组合基础接口

### 3.1 新增接口总览

| 接口 | 类型 | 前端用途 |
| --- | --- | --- |
| `POST /api/mock/portfolios/create` | 新增 | 创建当前用户的模拟组合，生成初始现金估值 |
| `POST /api/mock/portfolios/mine` | 新增 | 查询我的模拟组合列表，展示总资产、现金、收益等摘要 |
| `POST /api/mock/portfolios/detail` | 新增 | 查询模拟组合详情，展示估值和当前持仓 |
| `POST /api/mock/portfolios/orders/buy` | 新增 | 按产品最新行情模拟买入，生成订单、成交、持仓和新估值 |
| `POST /api/mock/portfolios/orders/sell` | 新增 | 按产品最新行情模拟卖出，生成订单、成交、持仓扣减和新估值 |
| `POST /api/mock/portfolios/orders/cancel` | 新增 | 撤销当前用户的非终态模拟订单，即时成交终态订单会返回拒绝 |
| `POST /api/mock/portfolios/orders/events` | 新增 | 查询当前用户模拟订单生命周期事件和交易审计 |
| `POST /api/mock/portfolios/orders/buy-from-report` | 新增 | 根据投资分析报告自动生成模拟买入 |
| `POST /api/mock/portfolios/rebalance/execute` | 新增 | 按目标权重执行模拟再平衡，返回调仓订单集合和最终组合 |
| `POST /api/mock/portfolios/valuations/refresh` | 新增 | 按持仓和最新行情刷新组合估值 |
| `POST /api/mock/portfolios/performance/curve` | 新增 | 查询组合收益曲线、累计收益率和最大回撤 |
| `POST /api/admin/data-sources/save` | 新增 | 保存数据源注册信息 |
| `POST /api/admin/data-sources/health/save` | 新增 | 保存数据源健康状态 |
| `POST /api/admin/data-sources/quality/save` | 新增 | 保存数据质量快照 |
| `POST /api/admin/data-sources/list` | 新增 | 查询数据源看板列表 |
| `POST /api/admin/data-sources/quality/list` | 新增 | 查询数据质量快照历史 |
| `POST /api/ai/prompts/save` | 新增 | 保存 Prompt 模板版本、变量定义和输出 Schema |
| `POST /api/ai/prompts/list` | 新增 | 查询 Prompt 版本列表 |
| `POST /api/ai/prompts/detail` | 新增 | 查询 Prompt 模板详情 |
| `POST /api/ai/prompts/status` | 新增 | 变更 Prompt 生命周期状态 |
| `POST /api/ai/prompts/preview` | 新增 | 本地预览 Prompt 渲染结果 |
| `POST /api/backtests/save` | 新增 | 保存外部或后台计算的回测结果 |
| `POST /api/backtests/generate-from-portfolio` | 新增 | 从 Mock 组合估值曲线生成回测摘要 |
| `POST /api/backtests/detail` | 新增 | 查询回测详情 |
| `POST /api/backtests/list` | 新增 | 查询回测结果列表 |
| `POST /api/ai/feedback/save` | 新增 | 保存用户采纳、拒绝、观察或忽略反馈 |
| `POST /api/ai/feedback/detail` | 新增 | 查询反馈详情 |
| `POST /api/ai/feedback/list` | 新增 | 查询反馈历史 |
| `POST /api/ai/prompt-evaluations/save` | 新增 | 保存 Prompt 评估 |
| `POST /api/ai/prompt-evaluations/detail` | 新增 | 查询 Prompt 评估详情 |
| `POST /api/ai/prompt-evaluations/list` | 新增 | 查询 Prompt 评估列表 |
| `POST /api/risk/checks/list` | 新增 | 查询风控检查和 Mock 交易拦截原因 |

### 3.1.0 Prompt 治理接口

`POST /api/ai/prompts/save`

请求：

```json
{
  "promptCode": "INVESTMENT_PLAN",
  "promptVersion": "v1",
  "scenario": "INVESTMENT_PLAN",
  "templateName": "投资方案生成 Prompt",
  "templateContent": "请基于报告摘要 ${reportSummary} 和质量门禁 ${dataQualityGate} 输出投资方案。",
  "status": "DRAFT",
  "description": "用于投资报告通过质量门禁后的方案生成",
  "variables": [
    {
      "variableName": "reportSummary",
      "sourcePath": "report.summary",
      "required": true,
      "description": "投资报告摘要"
    },
    {
      "variableName": "dataQualityGate",
      "sourcePath": "report.dataQualityGate",
      "required": true,
      "description": "数据质量门禁 JSON"
    }
  ],
  "outputSchemas": [
    {
      "schemaVersion": "v1",
      "schemaJson": "{\"type\":\"object\",\"required\":[\"planType\",\"riskLevel\"],\"properties\":{\"planType\":{\"type\":\"string\"},\"riskLevel\":{\"type\":\"string\"}}}"
    }
  ]
}
```

响应核心字段：

```json
{
  "bizId": "prompt-biz-id",
  "promptCode": "INVESTMENT_PLAN",
  "promptVersion": "v1",
  "scenario": "INVESTMENT_PLAN",
  "templateName": "投资方案生成 Prompt",
  "status": "DRAFT",
  "variables": [
    {
      "variableName": "reportSummary",
      "sourcePath": "report.summary",
      "required": true
    }
  ],
  "outputSchemas": [
    {
      "schemaVersion": "v1",
      "schemaJson": "{\"type\":\"object\"}"
    }
  ]
}
```

`POST /api/ai/prompts/list`

请求：

```json
{
  "promptCode": "INVESTMENT_PLAN",
  "scenario": "INVESTMENT_PLAN",
  "status": "ACTIVE",
  "page": 1,
  "size": 20,
  "sort": "updatedAt",
  "direction": "desc"
}
```

`POST /api/ai/prompts/preview`

请求：

```json
{
  "promptBizId": "prompt-biz-id",
  "variables": {
    "reportSummary": "AI主题热度上升，但数据质量中等。",
    "dataQualityGate": "{\"passed\":true,\"dataQualityScore\":0.72}"
  }
}
```

响应核心字段：

```json
{
  "promptBizId": "prompt-biz-id",
  "promptCode": "INVESTMENT_PLAN",
  "promptVersion": "v1",
  "scenario": "INVESTMENT_PLAN",
  "renderedPrompt": "请基于报告摘要 AI主题热度上升，但数据质量中等。 和质量门禁 {\"passed\":true,\"dataQualityScore\":0.72} 输出投资方案。",
  "missingVariables": [],
  "readyForModel": false,
  "displayMessage": "Prompt当前不是ACTIVE状态，仅允许后台预览"
}
```

前端注意：

- `readyForModel=false` 不是错误，表示模板未启用或缺少必填变量。
- `status=ACTIVE` 且 `missingVariables=[]` 时，后续才允许进入模型调用链路。
- `status=RETIRED` 的版本仍可查看和预览，用于回滚前对比，但不应作为默认生成入口。

### 3.1.0.1 风控审计查询接口

`POST /api/risk/checks/list`

请求：

```json
{
  "businessType": "ORDER",
  "businessBizId": "portfolio-biz-id",
  "userBizId": "user-biz-id",
  "checkResult": "REJECT",
  "riskLevel": "HIGH",
  "reasonCode": "INSUFFICIENT_CASH",
  "page": 1,
  "size": 20,
  "sort": "checkedAt",
  "direction": "desc"
}
```

响应核心字段：

```json
{
  "items": [
    {
      "bizId": "risk-check-biz-id",
      "businessType": "ORDER",
      "businessBizId": "portfolio-biz-id",
      "userBizId": "user-biz-id",
      "ruleCode": "MOCK_CASH_BALANCE",
      "checkResult": "REJECT",
      "riskLevel": "HIGH",
      "reasonCode": "INSUFFICIENT_CASH",
      "detail": "{\"cashBalance\":1000,\"requiredCash\":5000}",
      "checkedAt": "2026-06-23T23:30:00"
    }
  ],
  "total": 1,
  "page": 1,
  "size": 20,
  "totalPages": 1
}
```

前端注意：

- Mock 买入、卖出、报告转交易、再平衡中的关键拒绝会写入风险检查记录。
- 当前已覆盖报告质量不足、数据门禁未通过、现金不足、持仓不足、产品不可 Mock、调仓目标非法等原因。
- `detail` 是脱敏 JSON 字符串，详情页可以展开显示；列表页优先展示 `ruleCode`、`riskLevel`、`reasonCode` 和 `checkedAt`。

### 3.1.1 数据源治理接口

`POST /api/admin/data-sources/save`

请求：

```json
{
  "sourceCode": "CNINFO",
  "sourceName": "巨潮资讯",
  "sourceType": "ANNOUNCEMENT",
  "trustLevel": "L1",
  "baseUrl": "https://www.cninfo.com.cn",
  "enabled": true,
  "fetchFrequency": "0 */30 * * * ?",
  "owner": "research-ops",
  "description": "上市公司公告与披露数据源"
}
```

`POST /api/admin/data-sources/list`

响应核心字段：

```json
{
  "items": [
    {
      "sourceCode": "CNINFO",
      "sourceName": "巨潮资讯",
      "sourceType": "ANNOUNCEMENT",
      "trustLevel": "L1",
      "enabled": true,
      "qualityLevel": "HIGH",
      "displayMessage": "数据源质量较高，可作为投资报告的重要输入。",
      "health": {
        "successRate": 0.98,
        "failureReason": null,
        "sampleCount": 1200
      },
      "latestQuality": {
        "dataType": "ANNOUNCEMENT",
        "qualityScore": 0.86,
        "missingRate": 0.02,
        "duplicateRate": 0.01,
        "freshnessScore": 0.90
      }
    }
  ]
}
```

前端注意：

- `trustLevel=L5` 的数据源只能展示为演示或兜底，不应进入正式投资方案入口。
- `qualityLevel=LOW/UNKNOWN/DISABLED/DEMO_ONLY` 时，投资报告和 Mock 交易入口应降级或隐藏。
- `POST /api/admin/data-sources/quality/list` 可用于绘制质量趋势图。

### 3.2 `POST /api/mock/portfolios/create`

请求：

```json
{
  "portfolioName": "AI主题观察组合",
  "baseCurrency": "CNY",
  "initialCash": 100000
}
```

响应核心字段：

```json
{
  "bizId": "portfolio-biz-id",
  "portfolioNo": "MPXXXXXXXXXX",
  "portfolioName": "AI主题观察组合",
  "portfolioType": "SIMULATION",
  "baseCurrency": "CNY",
  "status": 1,
  "latestValuation": {
    "valuationTime": "2026-06-23T10:00:00",
    "baseCurrency": "CNY",
    "totalAsset": 100000,
    "cashBalance": 100000,
    "positionValue": 0,
    "totalCost": 0,
    "unrealizedProfit": 0,
    "realizedProfit": 0,
    "totalReturnRate": 0,
    "sourceCode": "MOCK_INITIAL_CASH"
  },
  "positions": []
}
```

前端注意：

- 创建后可以直接跳转模拟组合详情页。
- `latestValuation.sourceCode=MOCK_INITIAL_CASH` 表示只是初始现金快照，还没有订单、成交和持仓。
- 当前阶段不触发真实交易，也不生成订单。

### 3.3 `POST /api/mock/portfolios/mine`

请求：

```json
{
  "page": 1,
  "size": 20,
  "sort": "createdAt",
  "direction": "desc"
}
```

响应核心字段：

```json
{
  "items": [
    {
      "bizId": "portfolio-biz-id",
      "portfolioNo": "MPXXXXXXXXXX",
      "portfolioName": "AI主题观察组合",
      "portfolioType": "SIMULATION",
      "baseCurrency": "CNY",
      "status": 1,
      "latestValuation": {
        "totalAsset": 100000,
        "cashBalance": 100000,
        "positionValue": 0,
        "totalReturnRate": 0
      },
      "positions": []
    }
  ],
  "total": 1,
  "page": 1,
  "size": 20,
  "totalPages": 1
}
```

前端注意：

- 列表页只展示 `latestValuation` 摘要，不展示持仓明细。
- `positions` 在列表页为空是预期行为。
- 可用 `totalAsset`、`cashBalance`、`positionValue`、`totalReturnRate` 绘制组合卡片。

### 3.4 `POST /api/mock/portfolios/detail`

请求：

```json
{
  "portfolioBizId": "portfolio-biz-id"
}
```

响应核心字段：

```json
{
  "bizId": "portfolio-biz-id",
  "portfolioNo": "MPXXXXXXXXXX",
  "portfolioName": "AI主题观察组合",
  "portfolioType": "SIMULATION",
  "latestValuation": {
    "totalAsset": 100000,
    "cashBalance": 100000,
    "positionValue": 0,
    "unrealizedProfit": 0,
    "realizedProfit": 0,
    "totalReturnRate": 0
  },
  "positions": [
    {
      "bizId": "position-biz-id",
      "productBizId": "product-biz-id",
      "positionSide": "LONG",
      "quantity": 100,
      "availableQuantity": 100,
      "averageCost": 1.25,
      "costAmount": 125,
      "realizedProfit": 0,
      "lastTradeAt": "2026-06-23T10:00:00"
    }
  ]
}
```

前端注意：

- 手动模拟买入落地后，详情页 `positions` 会展示买入成交形成的当前持仓。
- 组合只允许当前登录用户访问，越权会返回 403。
- `portfolioType` 当前固定为 `SIMULATION`，不要和真实组合混用。

### 3.5 `POST /api/mock/portfolios/orders/buy`

请求：

```json
{
  "portfolioBizId": "portfolio-biz-id",
  "productBizId": "product-biz-id",
  "amount": 10000,
  "idempotencyKey": "front-uuid-001"
}
```

响应核心字段：

```json
{
  "order": {
    "bizId": "order-biz-id",
    "orderNo": "MOXXXXXXXXXX",
    "portfolioBizId": "portfolio-biz-id",
    "productBizId": "product-biz-id",
    "orderSide": "BUY",
    "orderType": "AMOUNT",
    "currency": "CNY",
    "requestedPrice": 1.25,
    "requestedAmount": 10000,
    "executedQuantity": 8000,
    "executedAmount": 10000,
    "feeAmount": 10,
    "status": "FILLED",
    "completedAt": "2026-06-23T10:00:00"
  },
  "execution": {
    "bizId": "execution-biz-id",
    "executionNo": "MEXXXXXXXXXX",
    "orderBizId": "order-biz-id",
    "executionPrice": 1.25,
    "executionQuantity": 8000,
    "executionAmount": 10000,
    "feeAmount": 10,
    "executedAt": "2026-06-23T10:00:00"
  },
  "portfolio": {
    "bizId": "portfolio-biz-id",
    "latestValuation": {
      "totalAsset": 99990,
      "cashBalance": 89990,
      "positionValue": 10000,
      "totalCost": 10000,
      "unrealizedProfit": 0,
      "totalReturnRate": -0.0001,
      "sourceCode": "MOCK_BUY_FILLED"
    },
    "positions": [
      {
        "productBizId": "product-biz-id",
        "positionSide": "LONG",
        "quantity": 8000,
        "availableQuantity": 8000,
        "averageCost": 1.25,
        "costAmount": 10000
      }
    ]
  }
}
```

前端注意：

- 该接口只做模拟买入，不触发真实交易。
- 产品必须在产品投资画像中 `mockTradable=true`，且 `dataQualityScore >= 0.45`。
- 产品必须存在最新 `1D` 行情，成交价取最新收盘价。
- 组合现金必须覆盖 `amount + feeAmount`。
- `idempotencyKey` 建议由前端生成 UUID；重复提交会返回同一笔订单和成交结果。

### 3.6 `POST /api/mock/portfolios/orders/sell`

请求：

```json
{
  "portfolioBizId": "portfolio-biz-id",
  "productBizId": "product-biz-id",
  "quantity": 4000,
  "idempotencyKey": "front-uuid-sell-001"
}
```

响应核心字段：

```json
{
  "order": {
    "orderSide": "SELL",
    "orderType": "MARKET",
    "requestedPrice": 1.35,
    "requestedQuantity": 4000,
    "requestedAmount": 5400,
    "executedQuantity": 4000,
    "executedAmount": 5400,
    "feeAmount": 5.4,
    "status": "FILLED"
  },
  "execution": {
    "executionPrice": 1.35,
    "executionQuantity": 4000,
    "executionAmount": 5400
  },
  "portfolio": {
    "latestValuation": {
      "cashBalance": 95384.6,
      "positionValue": 5400,
      "realizedProfit": 394.6,
      "sourceCode": "MOCK_SELL_FILLED"
    },
    "positions": []
  }
}
```

前端注意：

- 卖出按最新 `1D` 收盘价即时成交，不触发真实交易。
- 需要产品仍满足 `mockTradable=true` 和 `dataQualityScore >= 0.45`。
- 持仓不足或产品缺少行情时会返回 400。
- 清仓后该持仓不会出现在 `positions` 当前持仓列表中。

### 3.7 `POST /api/mock/portfolios/orders/cancel`

请求：

```json
{
  "orderBizId": "order-biz-id",
  "cancelReason": "用户撤销模拟调仓"
}
```

响应核心字段：

```json
{
  "bizId": "order-biz-id",
  "orderNo": "MOXXXXXXXXXX",
  "status": "CANCELLED",
  "completedAt": "2026-06-23T10:10:00"
}
```

前端注意：

- 只能撤销当前用户自己的订单。
- 当前买入、卖出和再平衡腿都是即时成交，`FILLED` 订单已处于终态，会返回 400，前端应展示“订单已成交，不能撤销”。
- 该接口用于把撤单边界先暴露给前端，后续部分成交或排队订单可以复用同一入口。

### 3.8 `POST /api/mock/portfolios/rebalance/execute`

请求：

```json
{
  "portfolioBizId": "portfolio-biz-id",
  "targets": [
    {
      "productBizId": "product-a",
      "targetWeight": 0.5
    },
    {
      "productBizId": "product-b",
      "targetWeight": 0.3
    }
  ],
  "minTradeAmount": 100,
  "idempotencyKey": "front-uuid-rebalance-001"
}
```

响应核心字段：

```json
{
  "executions": [
    {
      "order": {
        "orderSide": "SELL",
        "status": "FILLED"
      },
      "execution": {
        "executionAmount": 12000
      }
    },
    {
      "order": {
        "orderSide": "BUY",
        "status": "FILLED"
      },
      "execution": {
        "executionAmount": 8000
      }
    }
  ],
  "portfolio": {
    "bizId": "portfolio-biz-id",
    "latestValuation": {
      "sourceCode": "MOCK_BUY_FILLED"
    },
    "positions": []
  }
}
```

前端注意：

- `targets.targetWeight` 是 0 到 1 的小数，目标权重总和不能超过 1，剩余部分保留现金。
- 未出现在目标集合中的已有持仓按目标 0 处理，后端会先卖出再买入。
- `minTradeAmount` 用于过滤过小调仓腿。
- `idempotencyKey` 建议传稳定 UUID，后端会扩展成每条调仓腿的幂等键。

### 3.9 `POST /api/mock/portfolios/orders/events`

请求：

```json
{
  "orderBizId": "order-biz-id"
}
```

响应核心字段：

```json
[
  {
    "bizId": "event-biz-id",
    "orderBizId": "order-biz-id",
    "eventType": "FILLED",
    "fromStatus": null,
    "toStatus": "FILLED",
    "eventSource": "INTERNAL",
    "operatorBizId": "user-biz-id",
    "eventPayload": "{\"orderSide\":\"BUY\",\"executionAmount\":10000}",
    "occurredAt": "2026-06-23T10:00:00"
  }
]
```

前端注意：

- 只能查询当前用户自己的订单事件，越权返回 403。
- 买入、卖出和再平衡订单腿成交后会生成 `FILLED` 事件。
- 非终态订单撤销成功后会生成 `CANCELLED` 事件。
- `eventPayload` 是脱敏 JSON 字符串，前端可解析后展示成交金额、费用、产品和组合等上下文。

### 3.10 `POST /api/mock/portfolios/orders/buy-from-report`

请求：

```json
{
  "portfolioBizId": "portfolio-biz-id",
  "reportBizId": "analysis-report-biz-id",
  "productBizId": "optional-product-biz-id",
  "idempotencyKey": "front-uuid-002"
}
```

响应：

```json
{
  "order": {
    "orderSide": "BUY",
    "orderType": "AMOUNT",
    "requestedAmount": 30000,
    "status": "FILLED"
  },
  "execution": {
    "executionPrice": 1.25,
    "executionQuantity": 24000,
    "executionAmount": 30000
  },
  "portfolio": {
    "latestValuation": {
      "sourceCode": "MOCK_BUY_FILLED"
    },
    "positions": []
  }
}
```

前端注意：

- 后端读取报告 `investmentPlan.referenceAllocationAmount` 作为买入金额。
- `productBizId` 可不传；为空时后端根据报告 `themeCode` 查产品主题关系，选择权重最高的产品。
- 报告 `dataQualityGate.passed=false`、`confidenceLevel=UNUSABLE`、`investmentPlan.planType=DATA_GAP_REPORT` 时会拒绝执行。
- 响应结构与 `/orders/buy` 一致，前端可以复用订单成交结果页。

### 3.11 `POST /api/mock/portfolios/valuations/refresh`

请求：

```json
{
  "portfolioBizId": "portfolio-biz-id"
}
```

响应：

```json
{
  "bizId": "portfolio-biz-id",
  "latestValuation": {
    "valuationTime": "2026-06-23T10:30:00",
    "totalAsset": 101200,
    "cashBalance": 69990,
    "positionValue": 31210,
    "totalCost": 30000,
    "unrealizedProfit": 1210,
    "realizedProfit": 0,
    "totalReturnRate": 0.012,
    "sourceCode": "MOCK_MARK_TO_MARKET"
  },
  "positions": []
}
```

前端注意：

- 该接口会生成一条新的估值快照。
- 估值使用每个持仓产品最新 `1D` 收盘价。
- 任一持仓产品缺少最新行情时，后端拒绝刷新，前端应提示“持仓行情不足”。

### 3.12 `POST /api/mock/portfolios/performance/curve`

请求：

```json
{
  "portfolioBizId": "portfolio-biz-id",
  "limit": 120
}
```

响应：

```json
{
  "portfolioBizId": "portfolio-biz-id",
  "latestReturnRate": 0.012,
  "maxDrawdown": 0.035,
  "pointCount": 20,
  "valuations": [
    {
      "valuationTime": "2026-06-23T10:00:00",
      "totalAsset": 100000,
      "cashBalance": 100000,
      "positionValue": 0,
      "totalReturnRate": 0,
      "sourceCode": "MOCK_INITIAL_CASH"
    },
    {
      "valuationTime": "2026-06-23T10:30:00",
      "totalAsset": 101200,
      "cashBalance": 69990,
      "positionValue": 31210,
      "totalReturnRate": 0.012,
      "sourceCode": "MOCK_MARK_TO_MARKET"
    }
  ]
}
```

前端注意：

- `valuations.totalAsset` 用于资产曲线。
- `valuations.totalReturnRate` 用于收益率曲线。
- `maxDrawdown` 用于风险卡片。
- `limit` 最大 500，默认 120。

## 4. 回测、反馈和 Prompt 评估闭环接口补充

### 4.1 回测结果接口

接口：

```text
POST /api/backtests/save
POST /api/backtests/generate-from-portfolio
POST /api/backtests/detail
POST /api/backtests/list
```

前端注意：

- `/api/backtests/save` 的 `status` 可省略，后端默认 `PENDING`。
- `/api/backtests/save` 传入已有 `bizId` 时，后端会校验该记录归属当前用户，禁止接管其他用户回测。
- `/api/backtests/generate-from-portfolio` 只允许使用当前用户自己的 Mock 组合。
- `/api/backtests/generate-from-portfolio` 的 `parameters` 可传 JSON，后端会写入响应 `parameters.clientParameters`。
- `/api/backtests/detail` 只允许查看当前用户可见记录；越权返回 `403`。

### 4.2 投资反馈接口

接口：

```text
POST /api/ai/feedback/save
POST /api/ai/feedback/detail
POST /api/ai/feedback/list
```

前端注意：

- `feedbackAction` 下拉项：`ADOPT`、`REJECT`、`WATCH`、`IGNORE`。
- `targetType` 下拉项：`REPORT`、`RECOMMENDATION`、`MOCK_ORDER`、`MOCK_PORTFOLIO`、`BACKTEST`、`PROMPT`。
- 保存反馈时携带 `backtestBizId` 会触发回测归属校验；越权返回 `403`。
- 携带 `promptCode` 和 `promptVersion` 时，后端自动生成 `FEEDBACK_LOOP` 场景 Prompt 评估。
- `/api/ai/feedback/detail` 只允许查看当前用户自己的反馈。

### 4.3 Prompt 评估接口

接口：

```text
POST /api/ai/prompt-evaluations/save
POST /api/ai/prompt-evaluations/detail
POST /api/ai/prompt-evaluations/list
```

前端注意：

- `reviewStatus` 可省略，后端默认 `PENDING`。
- `reviewStatus` 下拉项：`PENDING`、`APPROVED`、`REJECTED`、`ARCHIVED`。
- `score` 范围为 0 到 1。
- 保存评估时携带 `backtestBizId` 或 `feedbackBizId` 会触发可见性校验。
- `/api/ai/prompt-evaluations/detail` 只允许评估人本人、关联反馈所有者或关联回测所有者查看；越权返回 `403`。
