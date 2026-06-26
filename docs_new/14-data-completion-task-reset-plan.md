# 高质量数据补全任务重置方案

## 1. 文档目的

本文档记录 2026-06-24 到 2026-06-27 对投资平台定时任务、数据源和默认 AI 报告生成链路的重置方案。

本次重置目标：

- 停止把低质量兜底数据伪装成正式投资输入。
- 初始化一组高质量、可配置、可前端查看的定时任务。
- 让核心数据补全优先围绕 L1/L2/L3 数据源。
- 增加自动投资报告生成任务。
- 增加自动 Prompt 治理任务，让报告、反馈和评估形成可复盘链路。
- 增加自动投资闭环总编排任务，让 Mock 投资闭环可以全自动运行。
- 默认挂 OpenAI 兼容模型，并允许前端通过模型配置接口调整。
- 增加模型挂靠配置，让数据源发现、报告生成、闭环编排和 Prompt 治理都能通过独立接口绑定模型。
- V23 后数据源发现由大模型按采集方向直接整理生成来源、采集计划、字段映射和质量策略；Skill 按数据收集类型维护。

## 2. 迁移入口

核心迁移文件：

```text
src/main/resources/db/migration/V17__reset_quality_tasks_and_openai_default.sql
src/main/resources/db/migration/V21__ai_model_binding_and_data_source_discovery.sql
src/main/resources/db/migration/V22__ai_skills_and_pure_closed_loop_tasks.sql
src/main/resources/db/migration/V23__llm_collection_skills_and_closed_loop_tasks.sql
```

执行内容：

1. 停用既有 `aiw_investment_task_definition` 中的历史任务。
2. 初始化高质量数据源注册和健康占位。
3. 初始化新的可配置任务定义。
4. 将 `openai-compatible-analysis` 调整为自动报告默认模型。
5. 本地清库脚本不注入业务样本，产品、行情、资讯、报告和 Mock 结果均由定时任务和用户动作产生。
6. V23 停用单一 `ai-data-source-discovery` 默认入口，生成六条 LLM 采集方向任务。
7. V23 初始化详细 Skill 模版，并把模型实例挂靠到数据采集、报告、方案、Prompt 和复盘审计 Skill。

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
| `llm-data-collection-multi-source` | `AI_DATA_SOURCE_DISCOVERY` | 是 | LLM 多源数据采集规划，生成来源、采集计划、字段映射和质量策略 |
| `llm-official-disclosure-collection` | `AI_DATA_SOURCE_DISCOVERY` | 是 | LLM 官方披露采集规划，重点公告、交易所和发行机构披露 |
| `llm-product-nav-collection` | `AI_DATA_SOURCE_DISCOVERY` | 是 | LLM 产品净值采集规划，重点产品池 upsert 和净值入行情表 |
| `llm-market-quote-collection` | `AI_DATA_SOURCE_DISCOVERY` | 是 | LLM 行情估值采集规划，重点行情、指数、估值和历史回溯 |
| `llm-news-research-collection` | `AI_DATA_SOURCE_DISCOVERY` | 是 | LLM 资讯研报采集规划，重点新闻、研报、机构观点和交叉验证 |
| `llm-regulatory-collection` | `AI_DATA_SOURCE_DISCOVERY` | 是 | LLM 监管政策采集规划，重点政策、处罚、风险提示和审计路由 |
| `cn-mainland-market-momentum-scan` | `MARKET_MOMENTUM_SCAN` | 是 | 中国大陆核心主题动量扫描 |
| `cn-mainland-hot-theme-return` | `HOT_THEME_RETURN` | 是 | 中国大陆核心主题收益快照 |
| `cn-mainland-news-heat-aggregation` | `NEWS_HEAT_AGGREGATION` | 是 | 资讯热度和资讯-主题-产品证据链 |
| `auto-openai-investment-report-generation` | `AUTO_INVESTMENT_REPORT_GENERATION` | 是 | 自动生成投资报告，默认 OpenAI 兼容模型 |
| `auto-prompt-governance` | `AUTO_PROMPT_GOVERNANCE` | 是 | 自动维护报告转方案 Prompt 基线，并基于真实报告和反馈生成评估记录 |
| `auto-investment-closed-loop-orchestration` | `AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION` | 是 | 自动采集、报告、Prompt/模型候选、Mock 交易、回测和反馈总编排 |

前端可以继续通过以下接口查看、调整和触发：

```text
POST /api/investment/tasks/definitions
POST /api/investment/tasks/definitions/save
POST /api/investment/tasks/trigger
POST /api/investment/tasks/executions/list
POST /api/ai/model-bindings/list
POST /api/ai/model-bindings/save
POST /api/admin/data-sources/discover
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

### 5.2 旧官方专用采集参数

`REGULATORY_DISCLOSURE_COLLECTION`、`EXCHANGE_ANNOUNCEMENT_COLLECTION`、`WEALTH_PRODUCT_NAV_REFRESH`
共享以下参数：

V23 后这些任务不再默认启用，只作为“LLM 候选被前端审核后”的执行原语。默认闭环入口已经切换为 `AI_DATA_SOURCE_DISCOVERY` 多方向任务。

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

### 5.4 自动 Prompt 治理

任务类型：

```text
AUTO_PROMPT_GOVERNANCE
```

核心参数：

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `promptCode` | `investment-plan-from-report` | 报告转投资方案 Prompt 稳定编码 |
| `promptVersion` | `auto-v1` | 自动治理基线版本 |
| `scenario` | `INVESTMENT_PLAN` | Prompt 使用场景 |
| `reportSampleSize` | `20` | 每次复盘最近报告数量 |

任务行为：

- 若 Prompt 基线不存在，自动创建模板、变量和输出 Schema。
- 若暂无真实报告，只返回等待报告的执行摘要，不写伪评估。
- 若已有报告，按报告状态、可信等级和数据质量分写入 `aiw_ai_prompt_evaluation`。
- 用户采纳/拒绝反馈仍由反馈接口写入，并会触发反馈维度 Prompt 评估。

### 5.5 AI 数据源发现

任务类型：

```text
AI_DATA_SOURCE_DISCOVERY
```

核心参数：

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `environment` | `DEFAULT` | 模型挂靠配置环境 |
| `marketScope` | `CN_MAINLAND` | 市场范围 |
| `assetClass` | `MULTI_ASSET` | 资产类别 |
| `dataTypes` | `MARKET_QUOTE,NEWS,ANNOUNCEMENT,RESEARCH,REGULATORY` | 需要发现的数据类型 |
| `preferredTrustLevels` | `L1,L2,L3,L4` | 候选来源等级 |
| `candidateLimit` | `8` | 候选数量上限 |
| `includeDisabledCandidates` | `true` | 是否包含需授权或暂不可用候选 |

任务行为：

- 使用 `DATA_SOURCE_DISCOVERY` 模型挂靠配置和方向化 Skill 生成候选。
- 输出来源等级、字段映射、推荐任务类型、采集计划、质量策略、置信度和审核策略。
- 定时任务默认 `autoRegisterCandidates=true`，会把候选沉淀到数据源池；默认 `autoEnableCandidates=false`，不会自动启用正式数据源。
- 已存在的数据源不会因为候选沉淀被改成停用状态。
- 接口 `/api/admin/data-sources/discover` 只返回候选，不自动保存；自动沉淀只发生在定时任务处理器中。

方向化参数：

| 参数 | 示例 | 说明 |
| --- | --- | --- |
| `collectionDirection` | `PRODUCT_NAV` | 采集方向：`MULTI_SOURCE`、`OFFICIAL_DISCLOSURE`、`NEWS_RESEARCH`、`PRODUCT_NAV`、`MARKET_QUOTE`、`REGULATORY` |
| `skillCode` | `DATA_COLLECTION_PRODUCT_NAV` | 指定 Skill；空值按采集方向自动选择 |
| `autoRegisterCandidates` | `true` | 是否把模型候选沉淀到数据源池 |
| `autoEnableCandidates` | `false` | 是否允许自动启用新候选，默认关闭 |

默认 Skill 模版：

| Skill | 类型 | 用途 |
| --- | --- | --- |
| `DATA_COLLECTION_MULTI_SOURCE` | `DATA_SOURCE_DISCOVERY` | 多源投资数据采集规划 |
| `DATA_COLLECTION_OFFICIAL_DISCLOSURE` | `DATA_SOURCE_DISCOVERY` | 官方披露、交易所、发行机构公告 |
| `DATA_COLLECTION_NEWS_RESEARCH` | `DATA_SOURCE_DISCOVERY` | 新闻、研报、机构观点和事件热度 |
| `DATA_COLLECTION_PRODUCT_NAV` | `DATA_SOURCE_DISCOVERY` | 产品池、风险等级、净值行情 |
| `DATA_COLLECTION_MARKET_QUOTE` | `DATA_SOURCE_DISCOVERY` | 行情、指数、估值和历史回溯 |
| `DATA_COLLECTION_REGULATORY` | `DATA_SOURCE_DISCOVERY` | 监管政策、处罚、风险提示 |
| `REPORT_GENERATION_CORE` | `REPORT_ANALYSIS` | 投资报告生成和降级 |
| `INVESTMENT_PLAN_GENERATION_CORE` | `REPORT_ANALYSIS` | 报告转 Mock 投资方案 |
| `PROMPT_EVOLUTION_CORE` | `PROMPT_GOVERNANCE` | Prompt 候选、评分和进化 |
| `CLOSED_LOOP_REVIEW_AUDIT_CORE` | `QUALITY_AUDIT` | 闭环复盘、审计和改进路由 |

### 5.6 自动投资闭环总编排

任务类型：

```text
AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION
```

核心参数：

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `automationLevel` | `FULL_MOCK` | 自动化等级，当前默认跑完整 Mock 闭环 |
| `dataTaskCodes` | LLM采集/聚合任务列表 | 本轮先同步执行的数据源发现、候选沉淀和聚合任务 |
| `reportTaskCode` | `auto-openai-investment-report-generation` | 自动报告任务编码 |
| `promptTaskCode` | `auto-prompt-governance` | 自动 Prompt 治理任务编码 |
| `mockUserBizId` | 本地 demo 用户 | 自动 Mock 使用的用户业务标识 |
| `mockPortfolioName` | `全自动闭环模拟组合` | 自动创建或复用的 Mock 组合名称 |
| `initialCash` | `100000` | 自动 Mock 组合初始现金 |
| `minQualityScore` | `0.45` | 报告进入 Mock 前的最低质量分 |
| `maxReportsForMock` | `20` | 报告进入 Mock 前最多向前检查的候选报告数量 |
| `allowAutoMockTrade` | `true` | 是否允许自动 Mock 交易 |
| `allowPromptCandidate` | `true` | 是否允许自动 Prompt 候选和评分 |
| `allowModelCandidate` | `true` | 是否允许自动 DRAFT 模型候选 |
| `allowAutoPromptActivation` | `false` | 正式启用 Prompt 闸门，当前不自动启用 |
| `allowAutoModelActivation` | `false` | 正式启用模型闸门，当前不自动 ACTIVE |
| `allowRealTrade` | `false` | 真实交易闸门，当前不触发真实交易 |

任务行为：

- 子任务失败会阻断闭环，并写入闭环步骤记录。
- 默认数据源入口为六条 `llm-*` 方向任务，旧 RSS/手工 endpoint 专用采集任务不再默认参与主动闭环。
- 报告必须满足状态成功、可信等级可用、质量分达标、质量门禁通过、不是数据缺口报告。
- 如果最近报告不达标，闭环会在 `maxReportsForMock` 窗口内继续查找合格报告，并把每份候选报告的阻断原因写入闭环步骤审计。
- 自动 Mock 会写组合、订单、成交、估值、回测和反馈。
- Prompt/模型只自动产出候选与评分，不自动正式启用。
- 真实交易只记录闸门步骤，不调用任何真实交易接口。

## 6. 默认 OpenAI 模型

默认模型：

```text
modelCode = openai-compatible-analysis
provider = OPENAI_COMPATIBLE
remote model = gpt-4.1-mini
secretRef = OPENAI_MOCK_API_KEY
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
- 当 `mockEnabled=false` 且外部密钥按当前模型 `secretRef` 配置完成时，后端会调用 OpenAI Chat Completions 兼容接口。

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

## 8. 弃案逻辑与保留边界

本轮后以下逻辑退出默认主动闭环：

| 旧逻辑 | 当前状态 | 处理方式 |
| --- | --- | --- |
| RSS/fallback 资讯作为默认数据补充 | 废弃 | `INVESTMENT_NEWS_COLLECTION` 不再作为闭环默认数据源任务；fallback 只能用于本地链路验证 |
| 手工 `endpoints/itemsPath/titlePath` 作为默认来源方案 | 废弃默认入口 | `REGULATORY_DISCLOSURE_COLLECTION`、`EXCHANGE_ANNOUNCEMENT_COLLECTION`、`WEALTH_PRODUCT_NAV_REFRESH` 默认停用，只保留为候选审核后的执行原语 |
| `maxReportsForMock=1` | 废弃 | 默认改为 `20`，避免多报告批量生成时误判无合格报告 |
| 数据源候选直接启用 | 禁止 | AI 发现只输出候选，正式启用需要前端保存或灰度开关 |

保留旧采集器类的原因：

- 它们仍可作为前端确认后的执行原语。
- 删除历史迁移或处理器会影响已部署环境和任务审计。
- 真正废弃的是“默认主方案”和“无审核启用”，不是所有采集执行能力。

## 9. 仍需补齐的高质量数据处理器

本次已补齐第一版可配置专用采集器，不再只是 RSS 占位。

后续建议补齐：

- `REGULATORY_DISCLOSURE_COLLECTION`：补官方接口字段模板和分页游标。
- `EXCHANGE_ANNOUNCEMENT_COLLECTION`：补上交所、深交所、巨潮的内置端点模板和公告分类映射。
- `WEALTH_PRODUCT_NAV_REFRESH`：已支持产品池 upsert 和净值入行情表；后续补中国理财网官方字段模板、分页游标和产品属性明细。
- `VENDOR_MARKET_QUOTE_SYNC`：Wind/Choice 等授权供应商行情同步。
- `DATA_SOURCE_QUALITY_AUDIT`：周期性读取采集结果并写质量快照。

## 10. 验收标准

- `/api/investment/tasks/definitions` 能看到 `AI_DATA_SOURCE_DISCOVERY`。
- 旧 RSS/手工 endpoint 专用任务默认停用，不进入自动闭环默认 `dataTaskCodes`。
- L1/L2 数据源在 `/api/admin/data-sources/list` 可见。
- 无外部源时，L1/L2 任务不会写入 fallback 资讯。
- 自动报告任务可手动触发并生成报告。
- 自动 Prompt 治理任务可手动触发；无报告时只返回等待真实报告，不写伪评估。
- 自动闭环总编排任务可手动触发；质量不足时应写 `BLOCKED` 运行和 `QUALITY_GATE` 阻断步骤。
- `/api/investment/closed-loop/runs/list` 和 `/runs/detail` 能查询闭环运行与步骤审计。
- 前端可配置 OpenAI 模型和自动报告任务参数。
- 前端可配置 AI Skill 和模型 Skill 绑定。
- `mockEnabled=false` 时，如果密钥缺失或模型输出非法，报告不会落库为成功。
- 清库后产品、行情、资讯、报告、Mock、回测和反馈表应为 0，后续只能由真实采集任务和用户动作增长。
