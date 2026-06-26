# 投资任务与投资分析业务说明

> 2026-06-26 更新：本文中 RSS/Atom、fallbackArticles 和手工 endpoints 章节保留为历史能力说明，不再代表默认闭环方案。当前默认数据源入口是 `AI_DATA_SOURCE_DISCOVERY` + 模型 Skill；旧采集器默认停用，fallback 默认不写入正式资讯。

## 1. 文档目标

本文档面向后端开发、前端联调和业务理解，说明当前投资任务与投资分析模块的代码结构、业务流程、接口契约、定时任务、初始化方式和数据落库逻辑。

当前模块覆盖三类能力：

- 可配置投资定时任务：任务定义落库，支持接口查询、保存、手动触发和动态调度刷新。
- 中国大陆投资主题汇总：收益、动量、资讯热度快照默认只处理 `CN_MAINLAND` 市场范围。
- 可插拔投资分析报告：通过 `InvestmentAnalysisProvider` 扩展分析能力，当前内置 `LOCAL_RULE` 本地规则分析器，后续可替换为真实大模型。

## 2. 总体业务链路

### 2.1 定时任务链路

```text
应用启动
  -> InvestmentTaskDefinitionInitializer
  -> 将 YAML/环境变量中的默认任务写入 aiw_investment_task_definition（仅缺失时初始化）
  -> ConfigurableInvestmentTaskScheduler 读取数据库启用任务
  -> 根据 cron + zone 动态注册调度
  -> 到点触发 InvestmentTaskManagementService.trigger
  -> InvestmentTaskTriggerPort 发布 Kafka 事件
  -> InvestmentTaskEventConsumer 消费 Kafka 事件
  -> InvestmentTaskExecutionService 记录 RUNNING 并匹配 InvestmentTaskHandler
  -> 执行具体任务处理器
  -> 写入资讯、快照、执行记录
```

### 2.2 手动触发链路

```text
前端调用 /api/investment/tasks/trigger
  -> InvestmentTaskController
  -> InvestmentTaskManagementService.trigger
  -> 从 aiw_investment_task_definition 查询 taskCode
  -> 合并数据库默认参数和本次 overrides
  -> 发布 Kafka 事件
  -> 后续执行链路与定时触发相同
```

### 2.3 投资分析链路

```text
前端调用 /api/investment/analysis/generate
  -> InvestmentAnalysisController
  -> InvestmentAnalysisApplicationService.generate
  -> 根据 modelCode 查询 aiw_ai_model 中最近启用的 ACTIVE 版本
  -> 解析 model_config，并通过 secretRef 注入外部 API Key
  -> 根据模型表 provider 匹配 InvestmentAnalysisProvider
  -> Provider 读取 aiw_investment_theme_snapshot 和近期资讯
  -> 生成投资信息汇总、趋势、投资方案、模拟收益、图表数据
  -> 写入 aiw_investment_analysis_report
  -> 返回报告响应
```

## 3. 核心代码地图

### 3.1 接口层

| 代码路径 | 作用 |
| --- | --- |
| `interfaces/controller/ai/AiModelController.java` | AI 模型注册、配置、状态和归档接口 |
| `interfaces/controller/task/InvestmentTaskController.java` | 投资任务配置、触发、执行记录、资讯和快照接口 |
| `interfaces/controller/ai/InvestmentAnalysisController.java` | 投资分析报告生成和查询接口 |
| `interfaces/request/task/*` | 投资任务接口请求 DTO |
| `interfaces/request/ai/*` | 投资分析接口请求 DTO |
| `interfaces/dto/response/task/*` | 投资任务接口响应 DTO |
| `interfaces/dto/response/ai/*` | 投资分析接口响应 DTO |

接口层只负责协议适配，不直接暴露 Domain、Entity 或应用层分页对象。

### 3.2 应用层

| 代码路径 | 作用 |
| --- | --- |
| `application/service/task/InvestmentTaskManagementService.java` | 任务配置查询保存、触发、执行记录和结果查询编排 |
| `application/service/task/InvestmentTaskExecutionService.java` | Kafka 事件执行编排、幂等、执行记录状态维护 |
| `application/service/task/InvestmentTaskHandler.java` | 每种任务处理器的统一接口 |
| `application/service/task/TaskParameterParser.java` | 任务参数解析，包含 `CN_MAINLAND` 默认市场范围 |
| `application/service/ai/AiModelApplicationService.java` | AI 模型注册、配置和状态管理用例 |
| `application/service/ai/InvestmentAnalysisApplicationService.java` | 投资分析报告生成和分页查询用例 |
| `application/service/ai/InvestmentAnalysisProvider.java` | 可插拔分析提供方端口 |

### 3.3 领域层

| 代码路径 | 作用 |
| --- | --- |
| `domain/model/task/InvestmentTaskDefinition.java` | 落库定时任务定义 |
| `domain/model/task/ScheduledTaskExecution.java` | 任务执行记录 |
| `domain/model/task/NewsArticle.java` | 投资资讯 |
| `domain/model/task/InvestmentThemeSnapshot.java` | 投资主题快照，包含收益、动量、热度和 `marketScope` |
| `domain/model/ai/AiModel.java` | AI 模型及版本注册信息 |
| `domain/model/ai/InvestmentAnalysisReport.java` | 投资分析报告 |
| `domain/repository/task/*Store.java` | 任务相关仓储端口 |
| `domain/repository/ai/InvestmentAnalysisReportStore.java` | 投资分析报告仓储端口 |

### 3.4 基础设施层

| 代码路径 | 作用 |
| --- | --- |
| `infrastructure/task/InvestmentTaskDefinitionInitializer.java` | 启动时初始化缺失的任务定义 |
| `infrastructure/task/ConfigurableInvestmentTaskScheduler.java` | 从数据库读取启用任务并动态注册 Cron |
| `infrastructure/task/InvestmentTaskEventPublisher.java` | Kafka 任务事件发布 |
| `infrastructure/task/InvestmentTaskEventConsumer.java` | Kafka 任务事件消费 |
| `infrastructure/task/RssInvestmentNewsFeedClient.java` | RSS/Atom 投资资讯采集，发布时间统一转北京时间 |
| `infrastructure/ai/LocalRuleInvestmentAnalysisProvider.java` | 默认本地规则分析器 |
| `infrastructure/common/SystemClockProvider.java` | 统一业务时钟，使用 `Asia/Shanghai` |
| `infrastructure/persistence/entity/**` | 持久化实体 |
| `infrastructure/persistence/mapper/**` | MyBatis Mapper Java 接口 |
| `resources/mapper/**` | MyBatis Mapper XML SQL |

## 4. 数据库表说明

### 4.1 `aiw_ai_model`

AI 模型及版本注册表，是大模型配置、版本和状态的管理入口。

| 字段 | 说明 |
| --- | --- |
| `biz_id` | 模型业务 ID |
| `model_code` | 模型稳定编码 |
| `model_version` | 模型版本 |
| `model_name` | 模型名称 |
| `model_type` | 模型类型，如 `SIGNAL`、`RISK`、`RECOMMENDATION`、`NLP`、`ANALYSIS` |
| `provider` | 模型提供方，如 `LOCAL_RULE`、`OPENAI`、私有模型平台编码 |
| `artifact_uri` | 模型制品、提示词或配置地址 |
| `model_config` | 脱敏后的模型参数 JSON |
| `metrics` | 离线评估指标 JSON |
| `status` | `DRAFT`、`VALIDATING`、`ACTIVE`、`INACTIVE`、`ARCHIVED` |
| `activated_at` | 启用时间，北京时间 |
| `retired_at` | 停用时间，北京时间 |
| `created_at` | 创建时间，北京时间 |
| `updated_at` | 更新时间，北京时间 |

说明：

- 唯一约束为 `model_code + model_version`。
- 删除采用归档方式，将状态置为 `ARCHIVED`。
- 投资分析报告中的 `provider_code` 和 `model_code` 应能对应这里的模型配置。
- `model_config` 只保存 `baseUrl`、远端模型名、`secretRef`、超时、温度和
  `mockEnabled` 等普通参数，禁止保存 API Key 明文。

### 4.2 `aiw_investment_task_definition`

任务定义表，是当前定时任务运行时配置来源。

| 字段 | 说明 |
| --- | --- |
| `biz_id` | 任务配置业务 ID |
| `task_code` | 稳定任务编码，接口触发时使用 |
| `task_type` | 任务处理器类型，用于匹配 `InvestmentTaskHandler.supports` |
| `cron` | Spring Cron 表达式 |
| `zone` | Cron 时区，默认 `Asia/Shanghai` |
| `enabled` | 是否启用 |
| `parameters` | JSON 任务参数 |
| `description` | 配置说明 |
| `created_at` | 创建时间，北京时间 |
| `updated_at` | 更新时间，北京时间 |

运行时逻辑：

- 启动时 `InvestmentTaskDefinitionInitializer` 只初始化缺失任务。
- 如果库里已有同 `task_code` 配置，启动时不会被 YAML 覆盖。
- 接口保存配置后，当前节点会立即刷新动态调度。

### 4.3 `aiw_scheduled_task_execution`

任务执行审计表。

| 字段 | 说明 |
| --- | --- |
| `biz_id` | 执行记录业务 ID |
| `task_code` | 任务编码 |
| `task_type` | 任务类型 |
| `trigger_source` | 触发来源：`SCHEDULE`、`MANUAL`、`RETRY` |
| `status` | `RUNNING`、`SUCCEEDED`、`FAILED` |
| `event_id` | Kafka 事件幂等 ID，全局唯一 |
| `result_summary` | 成功结果摘要 |
| `failure_reason` | 失败原因摘要 |
| `started_at` | 开始时间，北京时间 |
| `completed_at` | 完成时间，北京时间 |
| `created_at` | 创建时间，北京时间 |

### 4.4 `aiw_news_article`

投资资讯表，保存 RSS/Atom 采集结果。

关键字段包括：

- `external_id`：外部资讯 ID 或摘要生成 ID。
- `article_type`：资讯类型。
- `source_code`：来源编码。
- `title`、`summary`、`content`：资讯文本。
- `source_url`：原文地址。
- `language_code`：语言编码。
- `publish_time`：发布时间，北京时间。
- `collected_at`：采集时间，北京时间。

### 4.5 `aiw_investment_theme_snapshot`

投资主题快照表，保存收益、动量、资讯热度。

| 字段 | 说明 |
| --- | --- |
| `biz_id` | 快照业务 ID |
| `task_code` | 来源任务编码 |
| `snapshot_type` | `RETURN`、`MOMENTUM`、`NEWS_HEAT` |
| `theme_code` | 主题编码 |
| `theme_name` | 主题名称 |
| `market_scope` | 市场范围，默认 `CN_MAINLAND` |
| `window_minutes` | 统计窗口分钟数 |
| `sample_count` | 样本数量 |
| `return_rate` | 窗口收益率 |
| `momentum_score` | 动量分数 |
| `heat_score` | 资讯热度分数 |
| `top_product_biz_id` | 窗口内表现最好的产品业务 ID |
| `metrics` | JSON 指标明细 |
| `snapshot_time` | 快照时间，北京时间 |
| `created_at` | 创建时间，北京时间 |

### 4.6 `aiw_investment_analysis_report`

投资分析报告表。

| 字段 | 说明 |
| --- | --- |
| `biz_id` | 报告业务 ID |
| `request_id` | 本次分析请求 ID |
| `provider_code` | 分析提供方编码，如 `LOCAL_RULE` |
| `model_code` | 模型编码或分析器版本 |
| `market_scope` | 市场范围，默认 `CN_MAINLAND` |
| `theme_code` | 投资主题编码 |
| `theme_name` | 投资主题名称 |
| `status` | `SUCCEEDED`、`FAILED` |
| `investment_summary` | 投资信息汇总 JSON |
| `trend` | 趋势分析 JSON |
| `investment_plan` | 投资方案 JSON |
| `simulated_return` | 模拟收益 JSON |
| `chart_payload` | 前端图表数据 JSON |
| `prompt_snapshot` | 脱敏输入快照 |
| `failure_reason` | 失败原因摘要 |
| `generated_at` | 生成时间，北京时间 |
| `created_at` | 创建时间，北京时间 |

## 5. 定时任务类型

### 5.1 `INVESTMENT_NEWS_COLLECTION`

采集配置的 RSS/Atom 投资资讯。

处理器：

```text
application/service/task/InvestmentNewsCollectionTaskHandler.java
```

主要参数：

| 参数 | 说明 | 示例 |
| --- | --- | --- |
| `feedUrls` | RSS/Atom 地址，多个地址用逗号分隔 | `https://example.com/rss.xml` |
| `sourceCode` | 来源编码 | `OFFICIAL_RSS` |
| `languageCode` | 语言编码 | `zh-CN` |
| `maxItems` | 单次最多采集条数 | `50` |
| `fallbackArticles` | 外部 feed 无数据或不可用时入库的兜底资讯，格式为 `主题|标题|摘要;主题2|标题2|摘要2` | `AI人工智能|人工智能主题关注度提升|算力方向持续受到关注` |
| `fallbackEnabled` | 是否允许写入兜底资讯；正式 L1/L2 任务必须为 `false` | `false` |

输出：

- 写入 `aiw_news_article`。
- 执行结果写入 `aiw_scheduled_task_execution`。

容错逻辑：

- 每个 feed 独立采集，一个 feed 失败不会中断其它 feed。
- 如果本次没有采集到任何外部资讯，并且配置了 `fallbackArticles`，会将兜底资讯保存为 `sourceCode` 对应来源下的资讯。
- 当 `fallbackEnabled=false` 时，即使配置了 `fallbackArticles` 也不会写入兜底资讯。
- L1/L2 正式数据补全任务应使用下面的专用采集类型，不再依赖 RSS 兜底。

### 5.2 官方披露专用采集任务

处理器：

```text
application/service/task/OfficialDisclosureCollectionTaskHandler.java
```

支持任务类型：

| 任务类型 | 默认来源 | 入库类型 | 质量类型 |
| --- | --- | --- | --- |
| `REGULATORY_DISCLOSURE_COLLECTION` | `CSRC` | `REGULATORY` | `REGULATORY` |
| `EXCHANGE_ANNOUNCEMENT_COLLECTION` | `CNINFO` | `ANNOUNCEMENT` | `ANNOUNCEMENT` |
| `WEALTH_PRODUCT_NAV_REFRESH` | `CHINA_WEALTH` | `WEALTH_NAV` | `MARKET_QUOTE` |

主要参数：

| 参数 | 说明 | 示例 |
| --- | --- | --- |
| `endpoints` | 专用端点配置，格式 `名称=url|JSON;名称2=url|HTML` | `cninfo=https://example.com/api|JSON` |
| `responseFormat` | 默认响应格式，`JSON` 或 `HTML` | `JSON` |
| `itemsPath` | JSON 列表路径；空值表示根节点 | `data.items` |
| `externalIdPath` | 外部 ID 字段路径 | `announcementId` |
| `titlePath` | 标题字段路径 | `announcementTitle` |
| `summaryPath` | 摘要字段路径 | `summary` |
| `contentPath` | 正文字段路径 | `content` |
| `urlPath` | 原文链接字段路径 | `url` |
| `publishTimePath` | 发布时间字段路径 | `publishTime` |
| `extraFieldPaths` | 额外字段映射，格式 `业务字段=JSON路径;业务字段2=JSON路径2` | `productCode=code;nav=unitNav` |
| `includeKeywords` | 可选关键词过滤，逗号分隔 | `监管,处罚,风险` |
| `sourceCode` | 数据源编码 | `CNINFO` |
| `articleType` | 入库内容类型 | `ANNOUNCEMENT` |
| `maxItems` | 单次最多保存条数 | `100` |
| `timeoutSeconds` | 单端点超时时间 | `20` |
| `freshnessHours` | 新鲜度评估窗口 | `72` |

输出：

- 写入 `aiw_news_article`，不写兜底数据。
- 写入 `aiw_data_source_health`，前端数据源看板可见。
- 写入 `aiw_data_quality_snapshot`，用于质量趋势和报告门禁。

说明：

- JSON 端点通过字段路径解析，适合接证监会、交易所、巨潮或供应商授权接口。
- HTML 端点只做保守链接抽取，适合临时过渡；正式结构化数据优先使用 JSON 或供应商接口。
- `WEALTH_PRODUCT_NAV_REFRESH` 会基于 `extraFieldPaths` 同步银行理财产品主档，并在存在 `nav` 时写入 `aiw_market_quote` 的 `1D` 净值行情。
- 理财任务额外支持 `productMarketCode`、`productCurrency`、`quoteInterval`、`defaultRiskLevel`，默认分别为 `BANK_WMP`、`CNY`、`1D`、`2`。
- 理财 `extraFieldPaths` 推荐字段：`productCode`、`productName`、`nav`、`previousNav`、`assetSize`、`riskLevel`。

### 5.3 `HOT_THEME_RETURN`

计算热门投资方向在窗口内的平均收益。

处理器：

```text
application/service/task/HotThemeReturnTaskHandler.java
```

主要参数：

| 参数 | 说明 | 示例 |
| --- | --- | --- |
| `windowMinutes` | 回看窗口分钟数 | `1440` |
| `marketScope` | 市场范围，默认 `CN_MAINLAND` | `CN_MAINLAND` |
| `themes` | 主题到产品代码映射 | `AI人工智能=159819,588000;黄金=518880` |
| `themeMarketScopes` | 可选，主题到市场范围映射 | `AI人工智能=CN_MAINLAND;黄金=CN_MAINLAND` |

输出：

- 写入 `aiw_investment_theme_snapshot`。
- `snapshot_type = RETURN`。
- 默认只处理 `CN_MAINLAND`。

### 5.4 `MARKET_MOMENTUM_SCAN`

计算主题动量。当前动量公式基于窗口平均收益和上涨广度。

处理器：

```text
application/service/task/MarketMomentumTaskHandler.java
```

主要参数与 `HOT_THEME_RETURN` 相同。

输出：

- 写入 `aiw_investment_theme_snapshot`。
- `snapshot_type = MOMENTUM`。
- `metrics` 保存上涨广度和样本明细。

### 5.5 `NEWS_HEAT_AGGREGATION`

按关键词统计主题资讯热度。

处理器：

```text
application/service/task/NewsHeatAggregationTaskHandler.java
```

主要参数：

| 参数 | 说明 | 示例 |
| --- | --- | --- |
| `windowMinutes` | 回看窗口分钟数 | `1440` |
| `marketScope` | 市场范围，默认 `CN_MAINLAND` | `CN_MAINLAND` |
| `themes` | 主题到关键词映射 | `AI人工智能=AI,人工智能,算力;黄金=黄金,金价` |
| `themeMarketScopes` | 可选，主题到市场范围映射 | `AI人工智能=CN_MAINLAND` |

输出：

- 写入 `aiw_investment_theme_snapshot`。
- `snapshot_type = NEWS_HEAT`。
- `heat_score` 当前等于命中资讯数量。

## 6. 任务参数解析规则

任务参数统一由 `TaskParameterParser` 解析。

### 6.1 主题格式

```text
主题名称=值1,值2;主题名称2=值3,值4
```

示例：

```text
AI人工智能=159819,588000,515980;半导体=512480,159995,688981;黄金=518880,159934
```

解析结果保持配置顺序。

### 6.2 市场范围

默认市场范围：

```text
CN_MAINLAND
```

如果不传 `marketScope`，系统按 `CN_MAINLAND` 处理。

如果配置了 `themeMarketScopes`，只保留匹配当前 `marketScope` 的主题；未声明主题默认视为 `CN_MAINLAND`。

## 7. 接口清单

所有接口统一使用 `POST`，响应统一包装为 `Result<T>`。

### 7.1 保存 AI 模型配置

```http
POST /api/ai/models/save
```

请求 DTO：

```text
interfaces/request/ai/SaveAiModelRequest.java
```

请求字段：

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `modelCode` | 是 | 模型稳定编码 |
| `modelVersion` | 是 | 模型版本 |
| `modelName` | 是 | 模型名称 |
| `modelType` | 是 | 模型类型 |
| `provider` | 否 | 模型提供方 |
| `artifactUri` | 否 | 模型制品、提示词或配置地址 |
| `modelConfig` | 否 | 脱敏后的模型参数 JSON 字符串 |
| `metrics` | 否 | 离线评估指标 JSON 字符串 |
| `status` | 否 | 状态，默认 `DRAFT` |

请求示例：

```json
{
  "modelCode": "investment-analysis",
  "modelVersion": "v1",
  "modelName": "投资分析模型",
  "modelType": "ANALYSIS",
  "provider": "LOCAL_RULE",
  "artifactUri": "classpath:local-rule-v1",
  "modelConfig": "{\"temperature\":0}",
  "metrics": "{\"sampleValidation\":\"passed\"}",
  "status": "ACTIVE"
}
```

### 7.2 分页查询 AI 模型

```http
POST /api/ai/models/list
```

请求字段：

| 字段 | 说明 |
| --- | --- |
| `modelCode` | 模型编码 |
| `modelType` | 模型类型 |
| `provider` | 提供方 |
| `status` | 状态 |
| `page` | 页码 |
| `size` | 每页条数 |
| `sort` | `updatedAt`、`modelCode`、`modelVersion`、`modelType`、`provider`、`status`、`activatedAt` |
| `direction` | `asc` 或 `desc` |

### 7.3 查询 AI 模型详情

```http
POST /api/ai/models/detail
```

请求字段：

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `bizId` | 是 | 模型业务 ID |

### 7.4 变更 AI 模型状态

```http
POST /api/ai/models/status
```

请求字段：

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `bizId` | 是 | 模型业务 ID |
| `status` | 是 | `DRAFT`、`VALIDATING`、`ACTIVE`、`INACTIVE`、`ARCHIVED` |

### 7.5 归档 AI 模型

```http
POST /api/ai/models/archive
```

请求字段：

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `bizId` | 是 | 模型业务 ID |

归档不是物理删除，会将状态置为 `ARCHIVED` 并记录停用时间。

### 7.6 查询任务配置

```http
POST /api/investment/tasks/definitions
```

请求体：无。

响应元素：

| 字段 | 说明 |
| --- | --- |
| `code` | 任务编码 |
| `type` | 任务类型 |
| `cron` | Cron 表达式 |
| `zone` | Cron 时区 |
| `enabled` | 是否启用 |
| `parameters` | 任务参数 |
| `description` | 配置说明 |

### 7.7 保存任务配置

```http
POST /api/investment/tasks/definitions/save
```

请求 DTO：

```text
interfaces/request/task/SaveInvestmentTaskDefinitionRequest.java
```

请求字段：

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `code` | 是 | 稳定任务编码 |
| `type` | 是 | 任务处理器类型 |
| `cron` | 是 | Spring Cron 表达式 |
| `zone` | 否 | Cron 时区，默认 `Asia/Shanghai` |
| `enabled` | 否 | 是否启用，默认 `true` |
| `parameters` | 否 | 任务参数 |
| `description` | 否 | 配置说明 |

请求示例：

```json
{
  "code": "hot-theme-return",
  "type": "HOT_THEME_RETURN",
  "cron": "30 */5 * * * *",
  "zone": "Asia/Shanghai",
  "enabled": true,
  "parameters": {
    "windowMinutes": "1440",
    "marketScope": "CN_MAINLAND",
    "themes": "AI人工智能=159819,588000,515980;黄金=518880,159934"
  },
  "description": "中国大陆热门投资方向收益汇总"
}
```

保存成功后：

- 写入或更新 `aiw_investment_task_definition`。
- 刷新当前节点动态 Cron。
- 多节点部署时，其他节点需要额外刷新机制或重启才能同步当前节点外的调度状态。

### 7.8 手动触发任务

```http
POST /api/investment/tasks/trigger
```

请求 DTO：

```text
interfaces/request/task/TriggerInvestmentTaskRequest.java
```

请求字段：

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `taskCode` | 是 | 数据库中存在的任务编码 |
| `parameters` | 否 | 本次触发覆盖参数，不修改数据库配置 |

请求示例：

```json
{
  "taskCode": "hot-theme-return",
  "parameters": {
    "windowMinutes": "60"
  }
}
```

响应字段：

| 字段 | 说明 |
| --- | --- |
| `eventId` | Kafka 事件 ID |
| `taskCode` | 任务编码 |
| `taskType` | 任务类型 |
| `triggerSource` | `MANUAL` |
| `triggeredAt` | 触发时间，北京时间 |

### 7.9 分页查询执行记录

```http
POST /api/investment/tasks/executions/list
```

请求字段：

| 字段 | 说明 |
| --- | --- |
| `taskCode` | 任务编码 |
| `taskType` | 任务类型 |
| `status` | `RUNNING`、`SUCCEEDED`、`FAILED` |
| `startedFrom` | 开始时间起点 |
| `startedTo` | 开始时间终点 |
| `page` | 页码，从 1 开始，0 兼容为 1 |
| `size` | 每页条数，1-100 |
| `sort` | `startedAt`、`completedAt`、`createdAt`、`taskCode`、`taskType`、`status` |
| `direction` | `asc` 或 `desc` |

用途：

- 前端展示任务运行状态。
- 查看失败原因。
- 排查 Kafka 消费是否重复或失败。

### 7.10 分页查询资讯

```http
POST /api/investment/tasks/articles/list
```

请求字段：

| 字段 | 说明 |
| --- | --- |
| `keyword` | 标题或摘要关键字 |
| `articleType` | 资讯类型 |
| `sourceCode` | 来源编码 |
| `languageCode` | 语言编码 |
| `publishFrom` | 发布时间起点 |
| `publishTo` | 发布时间终点 |
| `page` | 页码 |
| `size` | 每页条数 |
| `sort` | `publishTime`、`collectedAt`、`createdAt`、`title`、`sourceCode` |
| `direction` | `asc` 或 `desc` |

### 7.11 分页查询主题快照

```http
POST /api/investment/tasks/snapshots/list
```

请求字段：

| 字段 | 说明 |
| --- | --- |
| `taskCode` | 来源任务编码 |
| `snapshotType` | `RETURN`、`MOMENTUM`、`NEWS_HEAT` |
| `themeCode` | 主题编码 |
| `marketScope` | 市场范围，默认 `CN_MAINLAND` |
| `snapshotFrom` | 快照时间起点 |
| `snapshotTo` | 快照时间终点 |
| `page` | 页码 |
| `size` | 每页条数 |
| `sort` | `snapshotTime`、`createdAt`、`taskCode`、`snapshotType`、`themeCode`、`returnRate`、`momentumScore`、`heatScore` |
| `direction` | `asc` 或 `desc` |

响应中包含：

- 收益率 `returnRate`。
- 动量分数 `momentumScore`。
- 热度分数 `heatScore`。
- 指标明细 JSON `metrics`。
- 市场范围 `marketScope`。

### 7.12 生成投资分析报告

```http
POST /api/investment/analysis/generate
```

请求 DTO：

```text
interfaces/request/ai/GenerateInvestmentAnalysisRequest.java
```

请求字段：

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `providerCode` | 否 | 可选一致性校验值；实际提供方由模型表决定 |
| `modelCode` | 否 | 模型稳定编码；默认 `local-rule-analysis` |
| `marketScope` | 否 | 市场范围，默认 `CN_MAINLAND` |
| `themeCode` | 否 | 投资主题编码，空则分析全部大陆主题 |
| `lookbackDays` | 否 | 回看天数，默认 30 |
| `initialCapital` | 否 | 模拟收益初始资金，默认 100000 |

请求示例：

```json
{
  "providerCode": "OPENAI_COMPATIBLE",
  "modelCode": "openai-compatible-analysis",
  "marketScope": "CN_MAINLAND",
  "themeCode": "AI人工智能",
  "lookbackDays": 30,
  "initialCapital": 100000
}
```

响应字段：

| 字段 | 说明 |
| --- | --- |
| `bizId` | 报告业务 ID |
| `requestId` | 请求 ID |
| `providerCode` | 分析提供方 |
| `modelCode` | 模型编码 |
| `marketScope` | 市场范围 |
| `themeCode` | 主题编码 |
| `themeName` | 主题名称 |
| `status` | 报告状态 |
| `investmentSummary` | 投资信息汇总 JSON |
| `trend` | 趋势分析 JSON |
| `investmentPlan` | 投资方案 JSON |
| `simulatedReturn` | 模拟收益 JSON |
| `chartPayload` | 前端图表数据 JSON |
| `failureReason` | 失败原因 |
| `generatedAt` | 生成时间，北京时间 |
| `createdAt` | 创建时间，北京时间 |

### 7.13 分页查询投资分析报告

```http
POST /api/investment/analysis/reports/list
```

请求字段：

| 字段 | 说明 |
| --- | --- |
| `marketScope` | 市场范围，默认 `CN_MAINLAND` |
| `themeCode` | 投资主题编码 |
| `providerCode` | 分析提供方 |
| `status` | `SUCCEEDED`、`FAILED` |
| `page` | 页码 |
| `size` | 每页条数 |
| `sort` | `generatedAt`、`createdAt`、`providerCode`、`modelCode`、`themeCode`、`status` |
| `direction` | `asc` 或 `desc` |

## 8. 投资分析输出结构

当前 `LOCAL_RULE` 默认输出 JSON 字符串。前端可以直接反序列化后绘图。

### 8.1 `investmentSummary`

包含：

- `marketScope`：市场范围。
- `themeCode`：主题编码。
- `sampleCount`：样本数量。
- `newsCount`：回看窗口内相关新闻数量。
- `averageReturn`：平均收益率。
- `latestSnapshotTime`：最近快照时间。
- `recentNews`：近期相关新闻摘要数组。

### 8.2 `trend`

包含：

- `direction`：`UP` 或 `DOWN`。
- `averageReturn`：平均收益率。
- `newsHeat`：相关新闻数量。
- `lookbackDays`：回看天数。

### 8.3 `investmentPlan`

包含：

- `planType`：当前为 `REFERENCE_ALLOCATION`。
- `suggestedAction`：参考动作。
- `riskNotice`：风险提示。

说明：投资方案只作为辅助建议，不自动执行交易，不承诺收益。

### 8.4 `simulatedReturn`

包含：

- `initialCapital`：初始资金。
- `estimatedProfit`：模拟收益。
- `estimatedFinalCapital`：模拟期末资金。
- `returnRate`：模拟收益率。

### 8.5 `chartPayload`

当前包含 `series` 数组，元素包括：

- `time`：快照时间。
- `snapshotType`：快照类型。
- `returnRate`：收益率。
- `momentumScore`：动量分数。
- `heatScore`：热度分数。

同时包含 `news` 数组，元素包括：

- `time`：发布时间。
- `title`：资讯标题。
- `sourceCode`：来源编码。

前端可按 `snapshotType` 拆分多条折线，也可以组合展示收益、动量、热度和新闻事件点。

## 9. 可插拔大模型扩展方式

扩展真实大模型时，不需要改 Controller。

### 9.1 当前初始化的模型

Flyway `V10__initialize_mock_ai_models.sql` 会初始化：

| modelCode | provider | 远端模型 | 当前模式 |
| --- | --- | --- | --- |
| `local-rule-analysis` | `LOCAL_RULE` | `local-rule-v1` | 本地规则 |
| `openai-compatible-analysis` | `OPENAI_COMPATIBLE` | `gpt-4.1-mini` | Mock |
| `deepseek-compatible-analysis` | `OPENAI_COMPATIBLE` | `deepseek-chat` | Mock |
| `qwen-compatible-analysis` | `OPENAI_COMPATIBLE` | `qwen-plus` | Mock |

OpenAI 兼容模型当前配置 `mockEnabled=true`，会验证模型选择、JSON 配置解析和
API Key 注入，然后复用本地规则生成报告，不发起真实外部网络请求。

### 9.2 API Key 存放方式

模型表中的配置示例：

```json
{
  "baseUrl": "https://api.openai.com/v1",
  "model": "gpt-4.1-mini",
  "secretRef": "OPENAI_MOCK_API_KEY",
  "timeoutSeconds": 60,
  "temperature": 0.2,
  "mockEnabled": true
}
```

本地和开发环境的 UUID 模拟 Key 位于：

```yaml
ai:
  secrets:
    values:
      OPENAI_MOCK_API_KEY: ${OPENAI_MOCK_API_KEY:83ec6f52-567b-40a7-87ad-8b60df196d6c}
      DEEPSEEK_MOCK_API_KEY: ${DEEPSEEK_MOCK_API_KEY:b86ba542-3460-4932-8685-d113a095851e}
      QWEN_MOCK_API_KEY: ${QWEN_MOCK_API_KEY:6020210c-4639-41d6-a2ec-c44c4be9cd5b}
```

这些 UUID 只是模拟 Key，不具备真实供应商调用能力。生产配置不提供默认值，
必须通过环境变量、Kubernetes Secret、Vault 或外部私密配置注入。

运行时链路：

```text
modelCode
  -> 查询 ACTIVE AiModel
  -> 读取 model_config.secretRef
  -> AiSecretResolver
  -> ai.secrets.values[secretRef]
  -> AiModelRuntimeConfig
  -> InvestmentAnalysisProvider
```

API Key 不会进入模型表、接口响应、分析报告或日志。

新增一个类实现：

```text
application/service/ai/InvestmentAnalysisProvider.java
```

示例结构：

```java
@Component
public class OpenAiInvestmentAnalysisProvider implements InvestmentAnalysisProvider {
    @Override
    public boolean supports(String providerCode) {
        return "OPENAI".equals(providerCode);
    }

    @Override
    public InvestmentAnalysisReport analyze(String requestId, GenerateInvestmentAnalysisCommand command) {
        // 1. 查询快照、资讯、产品、行情等上下文
        // 2. 组织脱敏输入
        // 3. 调用大模型
        // 4. 校验输出 JSON 结构
        // 5. 构造 InvestmentAnalysisReport
    }
}
```

实现要求：

- `supports` 必须只匹配自己的 `providerCode`。
- 输出必须包含五块结构：投资信息汇总、趋势、投资方案、模拟收益、图表数据。
- 失败时必须保留失败摘要和完整异常堆栈日志。
- 不得让大模型输出直接触发真实交易。
- 大模型输入应脱敏，`promptSnapshot` 只能保存可审计的脱敏输入。

## 10. 初始化和运行

### 10.1 数据库迁移

需要执行 Flyway 迁移到最新版本。

本次新增关键迁移：

```text
src/main/resources/db/migration/V9__configurable_tasks_and_ai_analysis.sql
src/main/resources/db/migration/V10__initialize_mock_ai_models.sql
```

新增或修改：

- `aiw_investment_task_definition`
- `aiw_investment_analysis_report`
- `aiw_investment_theme_snapshot.market_scope`

### 10.2 默认任务初始化

默认任务配置在：

```text
src/main/resources/application-local.yaml
src/main/resources/application-dev.yaml
```

应用启动后：

- 如果 `aiw_investment_task_definition` 不存在某个 `task_code`，会插入默认配置。
- 如果数据库已有该 `task_code`，不会覆盖。

### 10.3 Kafka

本地 Kafka 通过项目根目录 `compose.yaml` 启动：

```bash
docker compose up -d kafka
```

默认应用配置连接：

```text
localhost:9092
```

项目 Compose 默认暴露：

```text
localhost:19092
```

如果使用 Compose Kafka，需要设置：

```text
KAFKA_BOOTSTRAP_SERVERS=localhost:19092
```

### 10.4 时间约定

项目数据库时间统一使用北京时间：

- 应用业务时钟：`SystemClockProvider` 使用 `Asia/Shanghai`。
- 数据库连接会话：Druid 初始化执行 `SET time_zone = '+08:00'`。
- RSS/Atom 外部时间：解析后转换为北京时间。
- 数据库字段使用 `DATETIME(3)`。

历史数据如果已经按 UTC 写入，不会自动转换；需要单独按表和字段执行数据修正。

## 11. 默认配置示例

本地和开发环境默认投资主题：

```yaml
investment:
  tasks:
    enabled: true
    topic: dzcom.investment.task.trigger.v1
    definitions:
      - code: hot-theme-return
        type: HOT_THEME_RETURN
        cron: 30 */5 * * * *
        zone: Asia/Shanghai
        parameters:
          windowMinutes: "1440"
          marketScope: CN_MAINLAND
          themes: AI人工智能=159819,588000,515980;半导体=512480,159995,688981;黄金=518880,159934
```

说明：

- `enabled=false` 可关闭调度和 Kafka 消费。
- `topic` 可通过 `INVESTMENT_TASK_TOPIC` 覆盖。
- Cron 可通过环境变量覆盖默认值。
- 任务真正运行时以数据库配置为准。

## 12. 前端接入建议

### 12.1 任务管理页面

建议页面能力：

- 查询任务配置：`/api/investment/tasks/definitions`
- 编辑任务配置：`/api/investment/tasks/definitions/save`
- 手动触发任务：`/api/investment/tasks/trigger`
- 查看执行记录：`/api/investment/tasks/executions/list`

重点字段：

- `enabled`：是否启用。
- `cron`：调度表达式。
- `parameters`：JSON 参数编辑器。
- `status`：执行状态。
- `failureReason`：失败原因。

### 12.2 投资主题看板

建议使用：

- `/api/investment/tasks/snapshots/list`

展示内容：

- 收益榜：`snapshotType=RETURN`，按 `returnRate` 排序。
- 动量榜：`snapshotType=MOMENTUM`，按 `momentumScore` 排序。
- 热度榜：`snapshotType=NEWS_HEAT`，按 `heatScore` 排序。
- 趋势图：按 `snapshotTime` 聚合。

### 12.3 投资分析报告页

建议使用：

- 生成报告：`/api/investment/analysis/generate`
- 查询历史报告：`/api/investment/analysis/reports/list`

页面可拆成：

- 投资信息汇总卡片：读取 `investmentSummary`。
- 趋势图：读取 `trend` 和 `chartPayload.series`。
- 投资方案：读取 `investmentPlan`。
- 模拟收益图：读取 `simulatedReturn`。

## 13. 常见问题

### 13.1 为什么接口保存任务后只刷新当前节点？

当前是模块化单体实现，`ConfigurableInvestmentTaskScheduler.refreshSchedules` 只作用于当前 JVM。多实例部署时，需要增加配置变更事件或管理接口广播，让其他节点也刷新。

### 13.2 为什么分析报告可能没有图表数据？

`LOCAL_RULE` 分析器依赖 `aiw_investment_theme_snapshot`。如果收益、动量、热度任务还没有成功执行，样本为空，报告仍会生成，但 `chartPayload.series` 为空。

### 13.3 为什么任务配置在 YAML 中改了但没有覆盖数据库？

设计上数据库配置是运行时生效来源。YAML 和环境变量只用于首次初始化缺失任务，避免重启覆盖前端或管理员通过接口保存的配置。

### 13.4 如何只汇总中国大陆？

默认 `marketScope=CN_MAINLAND`。任务参数不传市场范围时也按 `CN_MAINLAND` 处理。查询快照和生成报告时不传 `marketScope` 也默认只查中国大陆。

### 13.5 已有历史 UTC 时间怎么办？

本次改动只影响后续写入。历史数据如果已经按 UTC 存在 `DATETIME` 字段，需要按实际表和字段执行一次修正，例如对确认写错的字段加 8 小时。修正前必须先备份并确认哪些字段确实按 UTC 写入。

## 14. 验证命令

后端基础验证：

```bash
./mvnw test -q
```

本地 Kafka 验证：

```bash
docker compose up -d kafka
docker compose ps kafka
```

数据库迁移验证：

```sql
select task_code, task_type, cron, zone, enabled, parameters
from aiw_investment_task_definition
order by task_code;
```

北京时间验证：

```sql
select now(), @@session.time_zone;
```

期望：

- `@@session.time_zone` 为 `+08:00`。
- 应用写入的业务时间与北京时间一致。

## 15. 2026-06-24 高质量数据任务重置

本轮新增 `V17__reset_quality_tasks_and_openai_default.sql`，用于重置任务基线：

- 历史任务统一停用。
- 初始化 L1/L2/L3 优先的数据源注册。
- 初始化 L1 监管、L1 公告、L2 理财产品、主题收益、市场动量、资讯热度和自动报告生成任务。
- `INVESTMENT_NEWS_COLLECTION` 支持 `fallbackEnabled=false`，避免无外部数据时写入兜底资讯。
- 新增 `AUTO_INVESTMENT_REPORT_GENERATION` 处理器，默认使用 `openai-compatible-analysis` 生成报告。
- `openai-compatible-analysis` 成为默认分析模型；`mockEnabled=true` 时不发起外部请求，`mockEnabled=false` 且密钥存在时调用 OpenAI 兼容接口。

详细方案见 `docs_new/14-data-completion-task-reset-plan.md`。
