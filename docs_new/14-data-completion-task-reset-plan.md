# 高质量数据补全任务重置方案

## 1. 文档目的

本文档记录 2026-06-24 对投资平台定时任务、数据源和默认 AI 报告生成链路的重置方案。

本次重置目标：

- 停止把低质量兜底数据伪装成正式投资输入。
- 初始化一组高质量、可配置、可前端查看的定时任务。
- 让核心数据补全优先围绕 L1/L2/L3 数据源。
- 增加自动投资报告生成任务。
- 默认挂 OpenAI 兼容模型，并允许前端通过模型配置接口调整。

## 2. 迁移入口

迁移文件：

```text
src/main/resources/db/migration/V17__reset_quality_tasks_and_openai_default.sql
```

执行内容：

1. 停用既有 `aiw_investment_task_definition` 中的历史任务。
2. 初始化高质量数据源注册和健康占位。
3. 初始化新的可配置任务定义。
4. 将 `openai-compatible-analysis` 调整为自动报告默认模型。

## 3. 数据源初始化

| sourceCode | 等级 | 类型 | 状态 | 用途 |
| --- | --- | --- | --- | --- |
| `CSRC` | L1 | REGULATORY | 启用 | 监管政策、处罚、制度和官方公告 |
| `SSE` | L1 | ANNOUNCEMENT | 启用 | 上交所公告和产品披露 |
| `SZSE` | L1 | ANNOUNCEMENT | 启用 | 深交所公告和产品披露 |
| `CNINFO` | L1 | ANNOUNCEMENT | 启用 | 巨潮资讯公告和披露文件 |
| `CHINA_WEALTH` | L2 | MARKET | 启用 | 银行理财产品、净值和产品公开信息 |
| `EASTMONEY` | L4 | MARKET | 启用 | 行情和资讯补充，需要交叉验证 |
| `WIND` | L3 | MARKET | 禁用 | 专业供应商占位，等待授权 |
| `CHOICE` | L3 | MARKET | 禁用 | 专业供应商占位，等待授权 |

原则：

- L1/L2 数据进入正式数据补全链路。
- L3 供应商需要授权后启用。
- L4 只能补充验证，不能单独支撑正式投资建议。
- L5 兜底数据只允许本地演示，不进入正式报告。

## 4. 任务重置结果

| taskCode | taskType | 默认启用 | 说明 |
| --- | --- | --- | --- |
| `l1-regulatory-disclosure-collection` | `REGULATORY_DISCLOSURE_COLLECTION` | 是 | L1 监管披露专用采集，端点和字段路径前端可配置 |
| `l1-exchange-announcement-collection` | `EXCHANGE_ANNOUNCEMENT_COLLECTION` | 是 | L1 交易所/巨潮公告专用采集，端点和字段路径前端可配置 |
| `l2-wealth-product-nav-refresh` | `WEALTH_PRODUCT_NAV_REFRESH` | 是 | L2 理财产品和净值披露专用采集，同步产品池并写入净值行情 |
| `cn-mainland-market-momentum-scan` | `MARKET_MOMENTUM_SCAN` | 是 | 中国大陆核心主题动量扫描 |
| `cn-mainland-hot-theme-return` | `HOT_THEME_RETURN` | 是 | 中国大陆核心主题收益快照 |
| `cn-mainland-news-heat-aggregation` | `NEWS_HEAT_AGGREGATION` | 是 | 资讯热度和资讯-主题-产品证据链 |
| `auto-openai-investment-report-generation` | `AUTO_INVESTMENT_REPORT_GENERATION` | 是 | 自动生成投资报告，默认 OpenAI 兼容模型 |

前端可以继续通过以下接口查看、调整和触发：

```text
POST /api/investment/tasks/definitions
POST /api/investment/tasks/definitions/save
POST /api/investment/tasks/trigger
POST /api/investment/tasks/executions/list
```

## 5. 任务参数关键约定

### 5.1 禁止兜底伪装正式数据

`INVESTMENT_NEWS_COLLECTION` 新增参数：

```json
{
  "fallbackEnabled": "false"
}
```

当外部源无数据且 `fallbackEnabled=false` 时，任务不会写入兜底资讯，只返回“外部资讯源无有效数据”的执行摘要。

### 5.2 官方专用采集参数

`REGULATORY_DISCLOSURE_COLLECTION`、`EXCHANGE_ANNOUNCEMENT_COLLECTION`、`WEALTH_PRODUCT_NAV_REFRESH`
共享以下参数：

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `endpoints` | 空 | 专用端点，格式 `名称=url|JSON;名称2=url|HTML` |
| `responseFormat` | `JSON` | 默认响应格式 |
| `itemsPath` | 空 | JSON 列表路径，空表示根节点 |
| `externalIdPath` | `id` | 外部 ID 字段路径 |
| `titlePath` | `title` / `productName` | 标题或产品名称字段路径 |
| `summaryPath` | `summary` | 摘要字段路径 |
| `contentPath` | `content` | 正文字段路径 |
| `urlPath` | `url` | 原文链接字段路径 |
| `publishTimePath` | `publishTime` | 发布时间字段路径 |
| `extraFieldPaths` | 空 / 理财默认映射 | 额外字段映射，理财任务用于产品池和净值行情 |
| `includeKeywords` | 空 | 关键词过滤，逗号分隔 |
| `maxItems` | `80` / `100` | 单次最多保存条数 |
| `timeoutSeconds` | `20` | 单端点超时 |
| `freshnessHours` | `72` / `168` | 新鲜度评分窗口 |

采集成功后会写入资讯/公告表、数据源健康和质量快照；端点未配置或无有效数据时不写入兜底数据。

`WEALTH_PRODUCT_NAV_REFRESH` 额外参数：

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `productMarketCode` | `BANK_WMP` | 银行理财产品市场/渠道编码 |
| `productCurrency` | `CNY` | 产品币种 |
| `quoteInterval` | `1D` | 净值行情周期 |
| `defaultRiskLevel` | `2` | 数据源未给风险等级时的默认产品风险 |

理财任务默认额外字段映射：

```text
productCode=productCode;productName=productName;nav=nav;previousNav=previousNav;assetSize=assetSize;riskLevel=riskLevel
```

### 5.3 自动报告生成

任务类型：

```text
AUTO_INVESTMENT_REPORT_GENERATION
```

核心参数：

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `providerCode` | `OPENAI_COMPATIBLE` | Provider 一致性校验 |
| `modelCode` | `openai-compatible-analysis` | 默认模型编码 |
| `marketScope` | `CN_MAINLAND` | 市场范围 |
| `lookbackDays` | `30` | 回看窗口 |
| `initialCapital` | `100000` | 模拟收益初始资金 |
| `themes` | 核心主题映射 | 用于生成主题报告 |
| `themeCodes` | 空 | 显式主题编码列表，优先级高于 `themes` |

## 6. 默认 OpenAI 模型

默认模型：

```text
modelCode = openai-compatible-analysis
provider = OPENAI_COMPATIBLE
remote model = gpt-4.1-mini
secretRef = OPENAI_API_KEY
```

默认配置仍保留：

```json
{
  "mockEnabled": true
}
```

原因：

- 本地和开发环境不应默认依赖真实外部网络与真实 API Key。
- 前端可通过 `/api/ai/models/save` 或 `/api/ai/models/status` 调整模型配置。
- 当 `mockEnabled=false` 且外部密钥 `OPENAI_API_KEY` 配置完成时，后端会调用 OpenAI Chat Completions 兼容接口。

## 7. 真实大模型调用边界

`OPENAI_COMPATIBLE` Provider 当前能力：

- `mockEnabled=true`：复用本地规则报告，标记为 OpenAI 兼容模型输出，不发起外部请求。
- `mockEnabled=false`：调用 `${baseUrl}/chat/completions`。
- 请求使用 `response_format={"type":"json_object"}`。
- 模型必须返回 JSON 对象，并包含：
  - `investmentSummary`
  - `trend`
  - `investmentPlan`
  - `simulatedReturn`
  - `chartPayload`
  - `promptSnapshot`
- 输出缺字段或 JSON 非法时，后端拒绝落库，不伪造成功报告。

## 8. 仍需补齐的高质量数据处理器

本次已补齐第一版可配置专用采集器，不再只是 RSS 占位。

后续建议补齐：

- `REGULATORY_DISCLOSURE_COLLECTION`：补官方接口字段模板和分页游标。
- `EXCHANGE_ANNOUNCEMENT_COLLECTION`：补上交所、深交所、巨潮的内置端点模板和公告分类映射。
- `WEALTH_PRODUCT_NAV_REFRESH`：已支持产品池 upsert 和净值入行情表；后续补中国理财网官方字段模板、分页游标和产品属性明细。
- `VENDOR_MARKET_QUOTE_SYNC`：Wind/Choice 等授权供应商行情同步。
- `DATA_SOURCE_QUALITY_AUDIT`：周期性读取采集结果并写质量快照。

## 9. 验收标准

- `/api/investment/tasks/definitions` 能看到新的 V17 任务。
- 历史旧任务默认停用。
- L1/L2 数据源在 `/api/admin/data-sources/list` 可见。
- 无外部源时，L1/L2 任务不会写入 fallback 资讯。
- 自动报告任务可手动触发并生成报告。
- 前端可配置 OpenAI 模型和自动报告任务参数。
- `mockEnabled=false` 时，如果密钥缺失或模型输出非法，报告不会落库为成功。
