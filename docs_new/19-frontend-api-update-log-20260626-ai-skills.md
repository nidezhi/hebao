# 2026-06-26 前端接口更新说明：AI Skills 与纯净闭环

## 1. 更新目标

本轮后端将旧的 RSS/fallback/手工 endpoint 默认数据源方案从自动闭环主链路中移除。2026-06-27 继续调整为“LLM 按采集方向直接整理生成数据源”，改为：

```text
AI Skill 维护
  -> 模型实例绑定 Skill
  -> LLM 方向化数据源发现任务
  -> 自动报告生成
  -> Prompt 治理
  -> Mock 交易
  -> 回测反馈
  -> 驾驶舱复盘
```

旧专用采集器仍保留为“人工审核后的执行原语”，但不再是默认主方案。

## 2. 新增接口

### 2.1 AI Skill 管理

#### `POST /api/ai/skills/save`

请求：

```json
{
  "skillCode": "DATA_COLLECTION_PRODUCT_NAV",
  "skillVersion": "v1",
  "skillName": "理财产品与净值采集Skill",
  "skillType": "DATA_SOURCE_DISCOVERY",
  "status": "ACTIVE",
  "instructionContent": "专注整理银行理财、公募基金、ETF等产品主档和净值来源...",
  "inputSchema": "{\"type\":\"object\"}",
  "outputSchema": "{\"type\":\"object\"}",
  "evaluationPolicy": "{\"manualReviewRequired\":true}",
  "description": "产品池、风险等级、净值行情方向采集模版"
}
```

响应核心字段：

```json
{
  "bizId": "skill-biz-id",
  "skillCode": "DATA_COLLECTION_PRODUCT_NAV",
  "skillVersion": "v1",
  "skillType": "DATA_SOURCE_DISCOVERY",
  "status": "ACTIVE",
  "instructionContent": "...",
  "inputSchema": "{...}",
  "outputSchema": "{...}",
  "evaluationPolicy": "{...}"
}
```

#### `POST /api/ai/skills/list`

请求：

```json
{
  "skillCode": "",
  "skillType": "DATA_SOURCE_DISCOVERY",
  "status": "ACTIVE",
  "keyword": "数据源",
  "page": 1,
  "size": 20,
  "sort": "updatedAt",
  "direction": "desc"
}
```

#### `POST /api/ai/skills/detail`

请求：

```json
{
  "bizId": "skill-biz-id"
}
```

#### `POST /api/ai/skills/status`

请求：

```json
{
  "bizId": "skill-biz-id",
  "status": "RETIRED"
}
```

### 2.2 模型 Skill 绑定

#### `POST /api/ai/model-skills/save`

请求：

```json
{
  "modelBizId": "10000000-0000-0000-0000-000000000002",
  "skillBizId": "23000000-0000-0000-0000-000000000001",
  "scenarioCode": "DATA_SOURCE_DISCOVERY",
  "priority": 10,
  "enabled": true,
  "config": "{\"candidateLimit\":8,\"autoApply\":false}",
  "description": "OpenAI兼容模型挂靠数据源发现Skill"
}
```

#### `POST /api/ai/model-skills/list`

请求：

```json
{
  "modelBizId": "",
  "modelCode": "openai-compatible-analysis",
  "skillCode": "DATA_COLLECTION_PRODUCT_NAV",
  "scenarioCode": "DATA_SOURCE_DISCOVERY",
  "enabled": true,
  "page": 1,
  "size": 20,
  "sort": "priority",
  "direction": "asc"
}
```

#### `POST /api/ai/model-skills/detail`

请求：

```json
{
  "bizId": "binding-biz-id"
}
```

#### `POST /api/ai/model-skills/by-model`

请求：

```json
{
  "modelBizId": "10000000-0000-0000-0000-000000000002"
}
```

### 2.3 数据源发现

`POST /api/admin/data-sources/discover` 请求新增 `collectionDirection` 和 `skillCode`：

```json
{
  "marketScope": "CN_MAINLAND",
  "assetClass": "BANK_WMP,FUND,ETF",
  "dataTypes": "MARKET_QUOTE",
  "topicKeywords": "理财产品,基金净值,ETF净值,产品风险等级",
  "collectionDirection": "PRODUCT_NAV",
  "skillCode": "DATA_COLLECTION_PRODUCT_NAV",
  "preferredTrustLevels": "L1,L2,L3",
  "candidateLimit": 8,
  "environment": "DEFAULT",
  "includeDisabledCandidates": true
}
```

响应新增：

```json
{
  "collectionDirection": "PRODUCT_NAV",
  "skillCode": "DATA_COLLECTION_PRODUCT_NAV",
  "skillVersion": "v1",
  "skillInstruction": "专注整理银行理财、公募基金、ETF等产品主档和净值来源...",
  "candidates": [
    {
      "sourceCode": "MODEL_GENERATED_SOURCE",
      "sourceType": "MARKET",
      "trustLevel": "L2",
      "recommendedTaskType": "WEALTH_PRODUCT_NAV_REFRESH",
      "suggestedParameters": {},
      "fieldMappings": {},
      "collectionPlan": "AI整理出的采集计划、接口说明、限制和样例",
      "qualityPolicy": "AI整理出的质量校验规则",
      "requiresReview": true
    }
  ]
}
```

前端需要在数据源发现页展示：

- 使用的模型：`modelCode/providerCode/environment`
- 使用的采集方向：`collectionDirection`
- 使用的 Skill：`skillCode/skillVersion`
- Skill 指令摘要：`skillInstruction`
- 候选来源：`candidates`
- 采集计划：`collectionPlan`
- 质量策略：`qualityPolicy`
- 审核策略：`reviewPolicy`
- Prompt 预览：`promptPreview`

## 3. 变更接口

### `POST /api/ai/models/detail`

响应新增 `skills[]`：

```json
{
  "bizId": "model-biz-id",
  "modelCode": "openai-compatible-analysis",
  "modelVersion": "mock-v1",
  "skills": [
    {
      "skillCode": "DATA_COLLECTION_PRODUCT_NAV",
      "skillVersion": "v1",
      "scenarioCode": "DATA_SOURCE_DISCOVERY",
      "priority": 10,
      "enabled": true
    }
  ]
}
```

## 4. 新增任务类型

### `AI_DATA_SOURCE_DISCOVERY`

默认任务编码：

```text
llm-data-collection-multi-source
llm-official-disclosure-collection
llm-product-nav-collection
llm-market-quote-collection
llm-news-research-collection
llm-regulatory-collection
```

参数：

| 参数 | 控件 | 默认值 |
| --- | --- | --- |
| `environment` | 下拉 | `DEFAULT` |
| `marketScope` | 下拉 | `CN_MAINLAND` |
| `assetClass` | 下拉 | `MULTI_ASSET` |
| `dataTypes` | 多选 | `MARKET_QUOTE,NEWS,ANNOUNCEMENT,RESEARCH,REGULATORY` |
| `collectionDirection` | 下拉 | `MULTI_SOURCE` |
| `skillCode` | 下拉/搜索 | `DATA_COLLECTION_MULTI_SOURCE` |
| `preferredTrustLevels` | 多选 | `L1,L2,L3,L4` |
| `candidateLimit` | 数字输入 | `8` |
| `includeDisabledCandidates` | 开关 | `true` |
| `autoRegisterCandidates` | 开关 | `true` |
| `autoEnableCandidates` | 开关 | `false` |

任务行为：

- `autoRegisterCandidates=true` 时，任务会把模型候选沉淀到数据源池。
- `autoEnableCandidates=false` 是默认安全边界，新候选不会自动启用。
- 已存在数据源不会因为候选沉淀被改成停用。
- `/api/admin/data-sources/discover` 接口只返回候选，不自动保存。

## 5. 字典

| 字典 | 值 |
| --- | --- |
| `skillType` | `DATA_SOURCE_DISCOVERY`、`PROMPT_GOVERNANCE`、`REPORT_ANALYSIS`、`QUALITY_AUDIT`、`MODEL_FEEDBACK` |
| `skillStatus` | `DRAFT`、`VALIDATING`、`ACTIVE`、`RETIRED`、`ARCHIVED` |
| `scenarioCode` | `DATA_SOURCE_DISCOVERY`、`PROMPT_GOVERNANCE`、`AUTO_REPORT_GENERATION`、`AUTO_CLOSED_LOOP_ORCHESTRATION` |
| `taskType` 新增 | `AI_DATA_SOURCE_DISCOVERY` |
| `taskExecutionStatus` | `RUNNING`、`SUCCEEDED`、`BLOCKED`、`FAILED` |
| `collectionDirection` | `MULTI_SOURCE`、`OFFICIAL_DISCLOSURE`、`PRODUCT_NAV`、`MARKET_QUOTE`、`NEWS_RESEARCH`、`REGULATORY` |
| `dataCollectionSkillCode` | `DATA_COLLECTION_MULTI_SOURCE`、`DATA_COLLECTION_OFFICIAL_DISCLOSURE`、`DATA_COLLECTION_PRODUCT_NAV`、`DATA_COLLECTION_MARKET_QUOTE`、`DATA_COLLECTION_NEWS_RESEARCH`、`DATA_COLLECTION_REGULATORY` |

## 6. 废弃默认入口

| 旧入口 | 当前处理 |
| --- | --- |
| `INVESTMENT_NEWS_COLLECTION` 作为闭环默认资讯采集 | 移出默认闭环；fallback 仅用于本地链路验证 |
| `REGULATORY_DISCLOSURE_COLLECTION` 默认启用 | 默认停用，仅作为候选审核后的执行原语 |
| `EXCHANGE_ANNOUNCEMENT_COLLECTION` 默认启用 | 默认停用，仅作为候选审核后的执行原语 |
| `WEALTH_PRODUCT_NAV_REFRESH` 默认启用 | 默认停用，仅作为候选审核后的执行原语 |
| 闭环 `maxReportsForMock=1` | 改为 `20` |
| 单一 `ai-data-source-discovery` 默认入口 | 默认停用，改为六条 `llm-*` 方向化任务 |

## 7. 页面调整建议

前端新增或调整：

1. AI Skill 工作台：列表、详情、编辑、状态变更、复制新版本。
2. 模型详情页：新增 Skill 绑定区。
3. 模型 Skill 绑定页：按模型、Skill、场景维护绑定。
4. 数据源发现页：展示采集方向、模型、Skill、候选、字段映射、采集计划、质量策略、Prompt 预览和审核策略。
5. 定时任务页：新增 `AI_DATA_SOURCE_DISCOVERY` 参数表单，支持 `collectionDirection`、`skillCode`、`autoRegisterCandidates`、`autoEnableCandidates`。
6. 驾驶舱：把数据源发现作为闭环前置治理步骤，而不是直接展示旧 endpoint 采集成功。
