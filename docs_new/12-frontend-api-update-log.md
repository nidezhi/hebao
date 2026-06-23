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
| `POST /api/mock/portfolios/orders/buy-from-report` | 新增 | 根据投资分析报告自动生成模拟买入 |
| `POST /api/mock/portfolios/valuations/refresh` | 新增 | 按持仓和最新行情刷新组合估值 |
| `POST /api/mock/portfolios/performance/curve` | 新增 | 查询组合收益曲线、累计收益率和最大回撤 |

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

### 3.6 `POST /api/mock/portfolios/orders/buy-from-report`

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

### 3.7 `POST /api/mock/portfolios/valuations/refresh`

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

### 3.8 `POST /api/mock/portfolios/performance/curve`

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
