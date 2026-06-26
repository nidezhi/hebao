# DZCOM 前端生成蓝图

## 1. 文档目标

本文档用于指导 `dzcom_web` 前端按步骤生成投资平台驾驶舱和全部业务页面。

目标不是生成一组“菜单管理页”，而是生成一个可操作、可解释、可复盘、可配置的投资工作台。前端必须把后端复杂闭环组织成清晰的用户路径：

```text
数据源
  -> 采集编排
  -> 产品池 / 行情 / 资讯
  -> 投资报告
  -> Prompt / 模型候选
  -> Mock 交易
  -> 回测 / 反馈
  -> 风控审计
  -> 自动闭环驾驶舱
```

本文档可直接作为后续前端生成任务的参考输入。

## 2. 当前前端基线

当前前端仓库：

```text
/Users/daniel/work/code_source/IdeaProjects/dzcom_web
```

已初始化技术栈：

| 类型 | 当前选择 |
| --- | --- |
| 构建 | Vite |
| 框架 | Vue 3 |
| 语言 | TypeScript |
| 路由 | Vue Router |
| 状态 | Pinia |
| UI | Ant Design Vue |
| 图表 | ECharts |
| 请求 | Axios |
| 图标 | `@ant-design/icons-vue` |

已存在核心目录：

```text
src/
  shared/api/
  shared/components/business/
  shared/dictionaries/
  entities/
  pages/
  layouts/
  router/
  stores/
```

已存在页面入口：

| 路由 | 页面 |
| --- | --- |
| `/overview` | Overview 投资驾驶舱 |
| `/data-quality` | Data Quality 数据质量 |
| `/data-ingestion` | Data Ingestion 采集编排 |
| `/product-risk` | Product & Risk 产品风险 |
| `/report-studio` | Report Studio 投资报告 |
| `/prompt-lab` | Prompt Lab 实验室 |
| `/simulation` | Simulation 模拟交易 |
| `/review-loop` | Review Loop 复盘闭环 |
| `/risk-audit` | Risk Audit 风控审计 |
| `/config-center` | Config Center 配置中心 |

当前多数业务页面仍是占位，后续按本文档逐步生成。

## 3. 前端开发铁律

### 3.1 接口铁律

- 所有业务请求使用 `POST + JSON body`。
- 页面组件不得直接写接口路径。
- 接口路径统一维护在 `src/shared/api/endpoints.ts`。
- 每个业务域必须有：
  - `entities/<domain>/model.ts`
  - `entities/<domain>/api.ts`
  - `entities/<domain>/adapter.ts`
  - `entities/<domain>/dictionary.ts`
- 后端统一响应格式：

```ts
interface ApiResult<T> {
  code: number
  message: string
  data: T
}
```

- 分页响应格式：

```ts
interface PageResult<T> {
  items: T[]
  total: number
  page: number
  size: number
  totalPages: number
}
```

### 3.2 UI 铁律

- 第一屏必须是实际工作台，不做欢迎页。
- 页面要像投资操作舱，不像传统后台 CRUD 菜单。
- 图表、状态流、时间线、矩阵、审计流优先于纯表格。
- 表格只用于批量扫描，详情必须有结构化解释。
- 低质量数据必须显眼展示，不得包装成投资建议。
- Mock 收益必须标记为模拟结果。
- 风控拒绝必须展示原因、等级、关联对象和审计入口。
- Prompt、模型、真实交易启用必须展示人工确认或灰度闸门。

### 3.3 业务铁律

- 数据质量先于 AI 输出。
- 产品风险先于 Mock 交易。
- 报告不是终点，必须能进入 Prompt、Mock、回测、反馈。
- 自动闭环可以执行 Mock，不能自动启用新 Prompt、新模型或真实交易。
- 缺失真实数据源端点时，前端必须展示“未配置真实端点，不写兜底数据”。

## 4. 生成阶段总览

前端生成按 8 个阶段推进，每个阶段必须能独立运行、独立验收。

| 阶段 | 名称 | 目标 | 产物 |
| --- | --- | --- | --- |
| S0 | 框架与契约补齐 | 补齐 API、字典、错误、公共组件 | `shared/*`、`entities/*` |
| S1 | 自动闭环驾驶舱 | 首屏展示自动闭环、质量、风险、Mock、待处理 | `pages/overview` |
| S2 | 数据质量与采集 | 配置数据源、任务、端点、字段映射、执行记录 | `data-quality`、`data-ingestion` |
| S3 | 产品与行情风险 | 产品池、画像、行情、主题关系、Mock 门禁 | `product-risk` |
| S4 | 报告工作台 | 报告列表、详情、质量门禁、图表、报告转 Mock | `report-studio` |
| S5 | Prompt 与模型实验室 | Prompt 版本、预览、评估、模型候选和启用闸门 | `prompt-lab` |
| S6 | 模拟交易 | 组合、订单、持仓、估值、收益曲线、再平衡 | `simulation` |
| S7 | 复盘与审计 | 回测、反馈、Prompt 评估、风控审计 | `review-loop`、`risk-audit` |

## 5. 业务域分类

### 5.1 框架域

| 业务类型 | 目录 | 说明 |
| --- | --- | --- |
| 认证会话 | `entities/auth`、`stores/auth` | 登录、登出、当前用户 |
| 用户权限 | `entities/user`、`entities/role` | 用户、角色、权限、风险等级 |
| HTTP 基础设施 | `shared/api` | Axios、错误、Result 解包、端点 |
| 字典状态 | `shared/dictionaries` | 全局下拉、Tag、状态颜色 |
| 页面壳 | `shared/components/business` | 页面标题、风险提示、指标条、状态空白 |

### 5.2 投资业务域

| 业务类型 | 目录 | 页面 | 核心对象 |
| --- | --- | --- | --- |
| 数据源治理 | `entities/data-source` | Data Quality | DataSource、Health、QualitySnapshot |
| 采集编排 | `entities/task` | Data Ingestion | TaskDefinition、Execution、Article、Snapshot |
| 产品风险 | `entities/product` | Product & Risk | Product、Profile、ThemeRelation |
| 行情 | `entities/market-quote` 或 `entities/product` | Product & Risk | MarketQuote |
| 投资报告 | `entities/report` | Report Studio | InvestmentAnalysisReport |
| Prompt | `entities/prompt` | Prompt Lab | Template、Variable、OutputSchema |
| 模型 | `entities/ai-model` | Prompt Lab / Config Center | AiModel |
| Mock 组合 | `entities/portfolio` | Simulation | Portfolio、Position、Order、Valuation |
| 回测 | `entities/backtest` | Review Loop | BacktestResult |
| 反馈评估 | `entities/feedback` | Review Loop | Feedback、PromptEvaluation |
| 风控审计 | `entities/risk` | Risk Audit | RiskCheck |
| 自动闭环 | `entities/closed-loop` | Overview | ClosedLoopRun、ClosedLoopStep |

## 6. 闭环节点字典

### 6.1 主链路节点

| 节点编码 | 展示名 | 归属页面 | 可点击目标 |
| --- | --- | --- | --- |
| `DATA_SOURCE` | 数据源 | Data Quality | 数据源详情 |
| `INGESTION_TASK` | 采集编排 | Data Ingestion | 任务详情 / 执行记录 |
| `PRODUCT_POOL` | 产品池 | Product & Risk | 产品详情 |
| `MARKET_QUOTE` | 行情净值 | Product & Risk | 行情曲线 |
| `NEWS_HEAT` | 资讯热度 | Data Ingestion / Overview | 资讯列表 |
| `REPORT` | 投资报告 | Report Studio | 报告详情 |
| `PROMPT` | Prompt | Prompt Lab | Prompt 详情 |
| `MODEL_CANDIDATE` | 模型候选 | Prompt Lab | 模型详情 |
| `MOCK_TRADE` | Mock 交易 | Simulation | 订单 / 组合 |
| `BACKTEST` | 回测 | Review Loop | 回测详情 |
| `FEEDBACK` | 反馈 | Review Loop | 反馈详情 |
| `RISK_AUDIT` | 风控审计 | Risk Audit | 审计详情 |

### 6.2 自动闭环步骤节点

来自 `AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION`。

| stepCode | 展示名 | 终态 | UI 展示 |
| --- | --- | --- | --- |
| `SAFETY_GUARD` | 自动化安全边界 | `SUCCEEDED` | 顶部安全闸门 |
| `DATA_COLLECTION` | 真实数据采集 | `SUCCEEDED/BLOCKED/FAILED` | 采集节点 |
| `REPORT_GENERATION` | 自动报告生成 | `SUCCEEDED/BLOCKED/FAILED` | 报告节点 |
| `QUALITY_GATE` | 数据质量与报告门禁 | `SUCCEEDED/BLOCKED` | 质量闸门 |
| `PROMPT_CANDIDATE` | Prompt 候选与评分 | `SUCCEEDED/SKIPPED/BLOCKED` | Prompt 节点 |
| `MODEL_CANDIDATE` | 模型候选与评分 | `SUCCEEDED/SKIPPED` | 模型节点 |
| `MOCK_TRADE` | 自动 Mock 交易 | `SUCCEEDED/BLOCKED/FAILED` | Mock 节点 |
| `BACKTEST_FEEDBACK` | 回测与反馈沉淀 | `SUCCEEDED/FAILED` | 复盘节点 |
| `PROMPT_ACTIVATION_GUARD` | Prompt 正式启用闸门 | `SKIPPED` | 人工确认提示 |
| `MODEL_ACTIVATION_GUARD` | 模型正式启用闸门 | `SKIPPED` | 灰度开关提示 |
| `REAL_TRADE_GUARD` | 真实交易闸门 | `SKIPPED` | 真实交易禁用提示 |
| `UNEXPECTED_FAILURE` | 闭环异常兜底 | `FAILED` | 异常节点 |

### 6.3 闭环运行状态

| 值 | 文案 | 颜色 | 前端动作 |
| --- | --- | --- | --- |
| `RUNNING` | 运行中 | processing | 展示进度骨架和最近步骤 |
| `SUCCEEDED` | 已完成 | success | 展示报告、组合、回测入口 |
| `PARTIAL` | 部分完成 | warning | 展示已完成节点和阻断节点 |
| `BLOCKED` | 已阻断 | error | 高亮阻断原因和补救动作 |
| `FAILED` | 系统失败 | error | 展示失败原因和重试入口 |

## 7. 全局字典

所有字典集中维护在 `src/shared/dictionaries/status.ts` 和各 `entities/*/dictionary.ts`。

### 7.1 数据质量字典

| 字典 | 值 | 文案 | 颜色 |
| --- | --- | --- | --- |
| `qualityLevel` | `HIGH` | 高质量 | success |
| `qualityLevel` | `MEDIUM` | 可用 | processing |
| `qualityLevel` | `LOW` | 低质量 | warning |
| `qualityLevel` | `DEMO_ONLY` | 仅演示 | default |

### 7.2 数据源字典

| 字典 | 值 | 文案 |
| --- | --- | --- |
| `trustLevel` | `L1` | 官方监管/交易所 |
| `trustLevel` | `L2` | 官方产品/净值 |
| `trustLevel` | `L3` | 授权供应商 |
| `trustLevel` | `L4` | 主流财经媒体 |
| `trustLevel` | `L5` | 演示/兜底 |

| 字典 | 值 |
| --- | --- |
| `sourceType` | `REGULATORY` |
| `sourceType` | `ANNOUNCEMENT` |
| `sourceType` | `MARKET` |
| `sourceType` | `NEWS` |
| `sourceType` | `RESEARCH` |

### 7.3 任务类型字典

| 值 | 文案 | 关键参数 |
| --- | --- | --- |
| `REGULATORY_DISCLOSURE_COLLECTION` | 监管披露采集 | endpoints、itemsPath、titlePath |
| `EXCHANGE_ANNOUNCEMENT_COLLECTION` | 交易所/巨潮公告采集 | endpoints、externalIdPath、publishTimePath |
| `WEALTH_PRODUCT_NAV_REFRESH` | 理财产品净值刷新 | extraFieldPaths、productMarketCode、quoteInterval |
| `MARKET_MOMENTUM_SCAN` | 主题动量扫描 | windowMinutes、themes |
| `HOT_THEME_RETURN` | 热门主题收益 | windowMinutes、themes |
| `NEWS_HEAT_AGGREGATION` | 资讯热度聚合 | themes、themeProducts |
| `AUTO_INVESTMENT_REPORT_GENERATION` | 自动投资报告 | providerCode、modelCode、lookbackDays |
| `AUTO_PROMPT_GOVERNANCE` | 自动 Prompt 治理 | promptCode、promptVersion、reportSampleSize |
| `AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION` | 自动闭环总编排 | dataTaskCodes、reportTaskCode、mockUserBizId、安全闸门 |
| `DATA_SOURCE_DISCOVERY` | 数据源 AI 发现 | marketScope、assetClass、dataTypes、preferredTrustLevels |

### 7.4 任务执行状态

| 值 | 文案 | 颜色 |
| --- | --- | --- |
| `RUNNING` | 运行中 | processing |
| `SUCCEEDED` | 成功 | success |
| `FAILED` | 失败 | error |

### 7.5 报告状态与可信等级

| 字典 | 值 | 文案 | 颜色 |
| --- | --- | --- | --- |
| `reportStatus` | `SUCCEEDED` | 成功 | success |
| `reportStatus` | `SUCCESS` | 成功 | success |
| `reportStatus` | `FAILED` | 失败 | error |
| `confidenceLevel` | `HIGH_CONFIDENCE` | 高可信 | success |
| `confidenceLevel` | `MEDIUM_CONFIDENCE` | 中可信 | processing |
| `confidenceLevel` | `LOW_CONFIDENCE` | 低可信 | warning |
| `confidenceLevel` | `UNUSABLE` | 不可用 | error |

### 7.6 Prompt 状态

| 值 | 文案 | 颜色 |
| --- | --- | --- |
| `DRAFT` | 草稿 | default |
| `VALIDATING` | 校验中 | processing |
| `ACTIVE` | 生效 | success |
| `RETIRED` | 退役 | default |

### 7.7 模型状态

| 值 | 文案 | 颜色 | 前端动作 |
| --- | --- | --- | --- |
| `DRAFT` | 草稿 | default | 可编辑 |
| `VALIDATING` | 校验中 | processing | 可查看评分 |
| `ACTIVE` | 生效 | success | 当前可用 |
| `INACTIVE` | 停用 | warning | 可重新启用 |
| `ARCHIVED` | 归档 | default | 只读 |

自动闭环生成的模型候选必须以 `DRAFT` 展示，并标注“需要人工确认或灰度开关”。

### 7.8 产品字典

| 字典 | 值 | 文案 |
| --- | --- | --- |
| `productType` | `STOCK` | 股票 |
| `productType` | `FUND` | 基金 |
| `productType` | `BOND` | 债券 |
| `productType` | `ETF` | ETF |
| `productType` | `BANK_WMP` | 银行理财 |
| `productType` | `GOLD` | 黄金 |
| `productType` | `REIT` | REIT |

| 字典 | 值 | 文案 | 颜色 |
| --- | --- | --- | --- |
| `tradeStatus` | `TRADABLE` | 可交易 | success |
| `tradeStatus` | `SUSPENDED` | 暂停交易 | warning |
| `tradeStatus` | `DISABLED` | 已禁用 | default |

### 7.9 Mock 交易字典

| 字典 | 值 | 文案 |
| --- | --- | --- |
| `portfolioStatus` | `0` | 关闭 |
| `portfolioStatus` | `1` | 正常 |
| `portfolioStatus` | `2` | 冻结 |
| `orderSide` | `BUY` | 买入 |
| `orderSide` | `SELL` | 卖出 |
| `orderStatus` | `PENDING` | 待处理 |
| `orderStatus` | `FILLED` | 已成交 |
| `orderStatus` | `CANCELLED` | 已撤销 |
| `orderStatus` | `REJECTED` | 已拒绝 |

### 7.10 回测与反馈字典

| 字典 | 值 | 文案 | 颜色 |
| --- | --- | --- | --- |
| `backtestStatus` | `PENDING` | 待运行 | default |
| `backtestStatus` | `RUNNING` | 运行中 | processing |
| `backtestStatus` | `SUCCEEDED` | 成功 | success |
| `backtestStatus` | `FAILED` | 失败 | error |
| `backtestStatus` | `CANCELLED` | 已取消 | default |
| `feedbackAction` | `ADOPT` | 采纳 | success |
| `feedbackAction` | `REJECT` | 拒绝 | error |
| `feedbackAction` | `WATCH` | 观察 | warning |
| `feedbackAction` | `IGNORE` | 忽略 | default |

### 7.11 风控字典

| 字典 | 值 | 文案 | 颜色 |
| --- | --- | --- | --- |
| `riskCheckResult` | `PASS` | 通过 | success |
| `riskCheckResult` | `WARN` | 预警 | warning |
| `riskCheckResult` | `REJECT` | 拒绝 | error |
| `riskLevel` | `LOW` | 低 | success |
| `riskLevel` | `MEDIUM` | 中 | warning |
| `riskLevel` | `HIGH` | 高 | error |

常见 `reasonCode`：

| reasonCode | 文案 |
| --- | --- |
| `LOW_DATA_QUALITY` | 数据质量不足 |
| `PRODUCT_NOT_MOCK_TRADABLE` | 产品不可 Mock |
| `INSUFFICIENT_CASH` | 现金不足 |
| `INSUFFICIENT_POSITION` | 持仓不足 |
| `DATA_GATE_BLOCKED` | 数据门禁阻断 |
| `DATA_GAP_REPORT` | 数据缺口报告 |
| `NO_EXECUTABLE_AMOUNT` | 无可执行配置金额 |
| `INVALID_TOTAL_ASSET` | 组合资产无效 |

## 8. 需要补齐的前端文件

### 8.1 endpoints.ts 必补

在 `src/shared/api/endpoints.ts` 增加：

```ts
closedLoop: {
  runs: '/api/investment/closed-loop/runs/list',
  detail: '/api/investment/closed-loop/runs/detail',
}
```

### 8.2 新增实体 closed-loop

新增：

```text
src/entities/closed-loop/
  model.ts
  api.ts
  adapter.ts
  dictionary.ts
```

`model.ts`：

```ts
import type { PageQuery } from '@/shared/api/types'

export type ClosedLoopRunStatus = 'RUNNING' | 'SUCCEEDED' | 'PARTIAL' | 'BLOCKED' | 'FAILED' | string
export type ClosedLoopStepStatus = 'SUCCEEDED' | 'SKIPPED' | 'BLOCKED' | 'FAILED' | string
export type ClosedLoopGateResult = 'PENDING' | 'PASS' | 'BLOCK' | string

export interface ClosedLoopStepDto {
  bizId: string
  stepCode: string
  stepName: string
  stepOrder: number
  stepStatus: ClosedLoopStepStatus
  inputSummary?: string
  outputSummary?: string
  failureReason?: string
  startedAt?: string
  completedAt?: string
}

export interface ClosedLoopRunDto {
  bizId: string
  runNo: string
  taskCode: string
  triggerSource?: string
  runStatus: ClosedLoopRunStatus
  automationLevel?: string
  marketScope?: string
  themeCode?: string
  mockUserBizId?: string
  portfolioBizId?: string
  reportBizId?: string
  promptBizId?: string
  promptCode?: string
  promptVersion?: string
  backtestBizId?: string
  qualityScore?: number
  gateResult?: ClosedLoopGateResult
  summary?: string
  failureReason?: string
  startedAt?: string
  completedAt?: string
  steps?: ClosedLoopStepDto[]
}

export interface ClosedLoopRunListRequest extends PageQuery {
  taskCode?: string
  runStatus?: ClosedLoopRunStatus
  automationLevel?: string
  marketScope?: string
  themeCode?: string
  mockUserBizId?: string
  startedFrom?: string
  startedTo?: string
}

export interface ClosedLoopRunDetailRequest {
  bizId: string
}
```

`api.ts`：

```ts
import { endpoints } from '@/shared/api/endpoints'
import { postJson } from '@/shared/api/httpClient'
import type { PageResult } from '@/shared/api/types'
import type { ClosedLoopRunDetailRequest, ClosedLoopRunDto, ClosedLoopRunListRequest } from './model'

export const listClosedLoopRuns = (data: ClosedLoopRunListRequest) =>
  postJson<PageResult<ClosedLoopRunDto>, ClosedLoopRunListRequest>(endpoints.closedLoop.runs, data)

export const getClosedLoopRunDetail = (data: ClosedLoopRunDetailRequest) =>
  postJson<ClosedLoopRunDto, ClosedLoopRunDetailRequest>(endpoints.closedLoop.detail, data)
```

`dictionary.ts`：

```ts
export const closedLoopRunStatusOptions = [
  { label: '运行中', value: 'RUNNING', color: 'processing' },
  { label: '已完成', value: 'SUCCEEDED', color: 'success' },
  { label: '部分完成', value: 'PARTIAL', color: 'warning' },
  { label: '已阻断', value: 'BLOCKED', color: 'error' },
  { label: '系统失败', value: 'FAILED', color: 'error' },
] as const

export const closedLoopStepStatusOptions = [
  { label: '成功', value: 'SUCCEEDED', color: 'success' },
  { label: '跳过', value: 'SKIPPED', color: 'default' },
  { label: '阻断', value: 'BLOCKED', color: 'error' },
  { label: '失败', value: 'FAILED', color: 'error' },
] as const
```

### 8.3 任务字典必补

`src/entities/task/dictionary.ts` 的 `ingestionTaskTypeOptions` 必须补齐：

```ts
{ label: '主题动量扫描', value: 'MARKET_MOMENTUM_SCAN' },
{ label: '热门主题收益', value: 'HOT_THEME_RETURN' },
{ label: '资讯热度聚合', value: 'NEWS_HEAT_AGGREGATION' },
{ label: '自动 Prompt 治理', value: 'AUTO_PROMPT_GOVERNANCE' },
{ label: '自动闭环总编排', value: 'AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION' },
```

## 9. 接口文档

### 9.1 认证接口

#### 登录

```text
POST /api/auth/login
```

请求：

```json
{
  "account": "demo_admin",
  "password": "Demo@123456"
}
```

响应核心：

```json
{
  "bizId": "user-biz-id",
  "username": "demo_admin",
  "nickname": "演示管理员",
  "roles": ["ADMIN"],
  "permissions": ["..."]
}
```

#### 当前用户

```text
POST /api/auth/me
```

请求：

```json
{}
```

前端用途：

- 初始化 Pinia auth store。
- 控制菜单权限和按钮显隐。
- 401 时跳转登录。

### 9.2 数据源治理接口

#### 查询数据源

```text
POST /api/admin/data-sources/list
```

请求：

```json
{
  "keyword": "CNINFO",
  "sourceType": "ANNOUNCEMENT",
  "trustLevel": "L1",
  "enabled": true,
  "page": 1,
  "size": 20,
  "sort": "updatedAt",
  "direction": "desc"
}
```

响应核心：

```json
{
  "items": [
    {
      "bizId": "source-biz-id",
      "sourceCode": "CNINFO",
      "sourceName": "巨潮资讯",
      "sourceType": "ANNOUNCEMENT",
      "trustLevel": "L1",
      "baseUrl": "https://www.cninfo.com.cn",
      "enabled": true,
      "fetchFrequency": "0 */20 * * * *",
      "health": {
        "lastSuccessAt": "2026-06-25T10:00:00",
        "successRate": 0.98,
        "failureReason": null,
        "sampleCount": 100
      },
      "latestQuality": {
        "qualityScore": 0.86,
        "missingRate": 0.04,
        "duplicateRate": 0.01,
        "freshnessScore": 0.92
      },
      "qualityLevel": "HIGH",
      "displayMessage": "数据源质量较高"
    }
  ],
  "total": 1
}
```

页面用途：

- Data Quality 顶部健康矩阵。
- Overview 数据健康摘要。
- Data Ingestion 任务配置时选择数据源。

#### 保存数据源

```text
POST /api/admin/data-sources/save
```

请求：

```json
{
  "sourceCode": "CNINFO",
  "sourceName": "巨潮资讯",
  "sourceType": "ANNOUNCEMENT",
  "trustLevel": "L1",
  "baseUrl": "https://www.cninfo.com.cn",
  "enabled": true,
  "fetchFrequency": "0 */20 * * * *",
  "owner": "research-ops",
  "description": "上市公司公告和披露文件"
}
```

#### 查询质量趋势

```text
POST /api/admin/data-sources/quality/list
```

请求：

```json
{
  "sourceCode": "CNINFO",
  "dataType": "ANNOUNCEMENT",
  "limit": 30
}
```

响应用途：

- 质量趋势折线图。
- 缺失率、重复率、新鲜度小图。

### 9.3 采集编排接口

#### 查询任务定义

```text
POST /api/investment/tasks/definitions
```

请求：

```json
{}
```

响应核心：

```json
[
  {
    "code": "auto-investment-closed-loop-orchestration",
    "type": "AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION",
    "cron": "0 40 */2 * * *",
    "zone": "Asia/Shanghai",
    "enabled": true,
    "parameters": {
      "automationLevel": "FULL_MOCK",
      "allowAutoMockTrade": "true",
      "allowRealTrade": "false"
    },
    "description": "自动投资闭环总编排任务"
  }
]
```

#### 保存任务定义

```text
POST /api/investment/tasks/definitions/save
```

请求：

```json
{
  "code": "l2-wealth-product-nav-refresh",
  "type": "WEALTH_PRODUCT_NAV_REFRESH",
  "cron": "0 0 */2 * * *",
  "zone": "Asia/Shanghai",
  "enabled": true,
  "parameters": {
    "endpoints": "中国理财网=https://example/api|JSON",
    "itemsPath": "data.list",
    "productCode": "productCode",
    "extraFieldPaths": "productCode=productCode;productName=productName;nav=nav;previousNav=previousNav;assetSize=assetSize;riskLevel=riskLevel",
    "quoteInterval": "1D"
  },
  "description": "理财产品和净值专用采集"
}
```

前端表单要求：

- 不给用户一个巨大的 JSON 文本框作为唯一入口。
- 按任务类型渲染字段：
  - 基础：code、type、cron、zone、enabled、description。
  - 端点：endpoints、responseFormat、timeoutSeconds、maxItems。
  - 字段路径：itemsPath、externalIdPath、titlePath、publishTimePath。
  - 理财扩展：productMarketCode、productCurrency、quoteInterval、defaultRiskLevel、extraFieldPaths。
  - 自动闭环：allowAutoMockTrade、allowPromptCandidate、allowModelCandidate、allowRealTrade。

#### 手动触发任务

```text
POST /api/investment/tasks/trigger
```

请求：

```json
{
  "taskCode": "auto-investment-closed-loop-orchestration",
  "parameters": {
    "allowAutoMockTrade": "true",
    "maxReportsForMock": "20"
  }
}
```

响应：

```json
{
  "eventId": "event-biz-id",
  "taskCode": "auto-investment-closed-loop-orchestration",
  "taskType": "AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION",
  "triggerSource": "MANUAL",
  "triggeredAt": "2026-06-25T10:00:00"
}
```

#### 查询执行记录

```text
POST /api/investment/tasks/executions/list
```

请求：

```json
{
  "taskCode": "auto-investment-closed-loop-orchestration",
  "taskType": "AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION",
  "status": "FAILED",
  "startedFrom": "2026-06-25T00:00:00",
  "startedTo": "2026-06-25T23:59:59",
  "page": 1,
  "size": 20,
  "sort": "startedAt",
  "direction": "desc"
}
```

### 9.4 自动闭环接口

#### 查询闭环运行

```text
POST /api/investment/closed-loop/runs/list
```

请求：

```json
{
  "taskCode": "auto-investment-closed-loop-orchestration",
  "runStatus": "BLOCKED",
  "automationLevel": "FULL_MOCK",
  "marketScope": "CN_MAINLAND",
  "themeCode": "",
  "mockUserBizId": "21000000-0000-0000-0000-000000000002",
  "startedFrom": "2026-06-25T00:00:00",
  "startedTo": "2026-06-25T23:59:59",
  "page": 1,
  "size": 20,
  "sort": "startedAt",
  "direction": "desc"
}
```

响应核心：

```json
{
  "items": [
    {
      "bizId": "run-biz-id",
      "runNo": "CLR-20260625-00000000",
      "taskCode": "auto-investment-closed-loop-orchestration",
      "runStatus": "BLOCKED",
      "automationLevel": "FULL_MOCK",
      "marketScope": "CN_MAINLAND",
      "reportBizId": "report-biz-id",
      "portfolioBizId": "portfolio-biz-id",
      "backtestBizId": "backtest-biz-id",
      "qualityScore": 0.32,
      "gateResult": "BLOCK",
      "summary": "{\"blockedReason\":\"没有找到满足质量门禁的最新投资报告\"}",
      "failureReason": "没有找到满足质量门禁的最新投资报告",
      "startedAt": "2026-06-25T10:00:00",
      "completedAt": "2026-06-25T10:01:00",
      "steps": []
    }
  ],
  "total": 1,
  "page": 1,
  "size": 20,
  "totalPages": 1
}
```

#### 查询闭环详情

```text
POST /api/investment/closed-loop/runs/detail
```

请求：

```json
{
  "bizId": "run-biz-id"
}
```

响应重点：

```json
{
  "bizId": "run-biz-id",
  "runNo": "CLR-20260625-00000000",
  "runStatus": "SUCCEEDED",
  "qualityScore": 0.76,
  "steps": [
    {
      "stepCode": "QUALITY_GATE",
      "stepName": "数据质量与报告门禁",
      "stepOrder": 40,
      "stepStatus": "SUCCEEDED",
      "inputSummary": "{\"minQualityScore\":0.45}",
      "outputSummary": "{\"reportBizId\":\"...\",\"qualityScore\":0.76}",
      "failureReason": null
    }
  ]
}
```

页面用途：

- Overview 中部闭环时间线。
- 运行详情抽屉。
- 失败/阻断原因解释。
- 直接跳转报告、组合、回测、Prompt、模型。

### 9.5 产品与行情接口

#### 查询产品列表

```text
POST /api/products/list
```

请求：

```json
{
  "keyword": "黄金",
  "productType": "ETF",
  "tradeStatus": "TRADABLE",
  "riskLevel": 3,
  "currency": "CNY",
  "page": 1,
  "size": 20,
  "sort": "updatedAt",
  "direction": "desc"
}
```

响应核心：

```json
{
  "items": [
    {
      "bizId": "product-biz-id",
      "productCode": "518880",
      "productName": "黄金ETF",
      "productType": "ETF",
      "marketCode": "CN_MAINLAND",
      "currency": "CNY",
      "tradeStatus": "TRADABLE",
      "riskLevel": 3,
      "dataQualityScore": 0.82,
      "latestNav": 5.12,
      "latestQuoteTime": "2026-06-25T15:00:00",
      "investmentProfile": {
        "assetClass": "GOLD",
        "mockTradable": true,
        "volatilityLevel": "MEDIUM",
        "liquidityLevel": "HIGH",
        "suitableRiskLevel": 3
      },
      "themeRelations": [
        {
          "relationType": "THEME",
          "relationCode": "GOLD",
          "relationName": "黄金",
          "relationWeight": 0.9
        }
      ]
    }
  ],
  "total": 1
}
```

#### 查询产品详情

```text
POST /api/products/detail
```

请求：

```json
{
  "bizId": "product-biz-id"
}
```

#### 查询最新行情

```text
POST /api/products/quotes/latest
```

请求：

```json
{
  "productBizId": "product-biz-id",
  "quoteInterval": "1D",
  "sourceCode": "CHINA_WEALTH"
}
```

#### 查询历史行情

```text
POST /api/products/quotes/history
```

请求：

```json
{
  "productBizId": "product-biz-id",
  "quoteInterval": "1D",
  "sourceCode": "CHINA_WEALTH",
  "limit": 120
}
```

### 9.6 投资报告接口

#### 查询报告列表

```text
POST /api/investment/analysis/reports/list
```

请求：

```json
{
  "marketScope": "CN_MAINLAND",
  "themeCode": "GOLD",
  "providerCode": "OPENAI_COMPATIBLE",
  "status": "SUCCEEDED",
  "page": 1,
  "size": 20,
  "sort": "generatedAt",
  "direction": "desc"
}
```

响应核心：

```json
{
  "items": [
    {
      "bizId": "report-biz-id",
      "providerCode": "OPENAI_COMPATIBLE",
      "modelCode": "openai-compatible-analysis",
      "marketScope": "CN_MAINLAND",
      "themeCode": "GOLD",
      "themeName": "黄金",
      "status": "SUCCEEDED",
      "confidenceLevel": "MEDIUM_CONFIDENCE",
      "dataQualityScore": 0.76,
      "dataQualityGate": "{\"passed\":true,\"qualityLevel\":\"MEDIUM\"}",
      "investmentSummary": "{\"summary\":\"...\"}",
      "trend": "{\"direction\":\"UP\"}",
      "investmentPlan": "{\"planType\":\"REFERENCE_ALLOCATION\",\"referenceAllocationAmount\":10000}",
      "simulatedReturn": "{\"expectedReturn\":0.03}",
      "chartPayload": "{\"series\":[]}",
      "promptSnapshot": "{\"promptCode\":\"investment-plan-from-report\"}",
      "generatedAt": "2026-06-25T10:00:00"
    }
  ],
  "total": 1
}
```

注意：

- 后端字段名为 `dataQualityScore`，当前前端 `report/model.ts` 若使用 `qualityScore`，adapter 必须兼容。
- `dataQualityGate`、`investmentSummary`、`trend`、`investmentPlan`、`simulatedReturn`、`chartPayload`、`promptSnapshot` 可能是 JSON 字符串，adapter 负责解析。
- 解析失败时前端不能崩溃，展示“原始 JSON 不可解析”。

#### 手动生成报告

```text
POST /api/investment/analysis/generate
```

请求：

```json
{
  "providerCode": "OPENAI_COMPATIBLE",
  "modelCode": "openai-compatible-analysis",
  "marketScope": "CN_MAINLAND",
  "themeCode": "GOLD",
  "lookbackDays": 30,
  "initialCapital": 100000
}
```

### 9.7 Prompt 接口

#### 查询 Prompt 列表

```text
POST /api/ai/prompts/list
```

请求：

```json
{
  "promptCode": "investment-plan-from-report",
  "scenario": "INVESTMENT_PLAN",
  "status": "ACTIVE",
  "page": 1,
  "size": 20,
  "sort": "updatedAt",
  "direction": "desc"
}
```

#### 保存 Prompt

```text
POST /api/ai/prompts/save
```

请求：

```json
{
  "promptCode": "investment-plan-from-report",
  "promptVersion": "auto-v2",
  "scenario": "INVESTMENT_PLAN",
  "templateName": "报告转投资方案 Prompt",
  "templateContent": "请基于 ${investmentReport} 输出结构化方案。",
  "status": "DRAFT",
  "description": "自动治理候选版本",
  "variables": [
    {
      "variableName": "investmentReport",
      "sourcePath": "report",
      "required": true,
      "description": "投资报告完整 JSON"
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

#### 预览 Prompt

```text
POST /api/ai/prompts/preview
```

请求：

```json
{
  "promptBizId": "prompt-biz-id",
  "variables": {
    "investmentReport": "{\"reportBizId\":\"...\"}",
    "dataQualityGate": "{\"passed\":true}"
  }
}
```

### 9.8 模型接口

#### 查询模型列表

```text
POST /api/ai/models/list
```

请求：

```json
{
  "modelCode": "openai-compatible-analysis",
  "modelType": "INVESTMENT_ANALYSIS",
  "provider": "OPENAI_COMPATIBLE",
  "status": "DRAFT",
  "page": 1,
  "size": 20,
  "sort": "updatedAt",
  "direction": "desc"
}
```

自动闭环候选模型展示规则：

- `status=DRAFT`。
- `artifactUri` 可能为 `closed-loop://<runBizId>`。
- `metrics` JSON 中展示 `score`、`dataQualityScore`、`confidenceLevel`。
- 只能显示“提交人工确认/灰度启用”，不能自动 ACTIVE。

#### 保存模型

```text
POST /api/ai/models/save
```

请求：

```json
{
  "modelCode": "openai-compatible-analysis",
  "modelVersion": "candidate-20260625100000",
  "modelName": "自动闭环候选模型",
  "modelType": "INVESTMENT_ANALYSIS",
  "provider": "OPENAI_COMPATIBLE",
  "artifactUri": "closed-loop://run-biz-id",
  "modelConfig": "{\"mockEnabled\":true}",
  "metrics": "{\"score\":0.78}",
  "status": "DRAFT"
}
```

#### 变更模型状态

```text
POST /api/ai/models/status
```

请求：

```json
{
  "bizId": "model-biz-id",
  "status": "ACTIVE"
}
```

前端必须二次确认：

- 是否已通过人工复核。
- 是否灰度范围明确。
- 是否确认不会触发真实交易。

### 9.9 Mock 组合接口

#### 查询我的组合

```text
POST /api/mock/portfolios/mine
```

请求：

```json
{
  "page": 1,
  "size": 20,
  "sort": "createdAt",
  "direction": "desc"
}
```

#### 创建组合

```text
POST /api/mock/portfolios/create
```

请求：

```json
{
  "portfolioName": "我的模拟组合",
  "baseCurrency": "CNY",
  "initialCash": 100000
}
```

#### 报告转买入

```text
POST /api/mock/portfolios/orders/buy-from-report
```

请求：

```json
{
  "portfolioBizId": "portfolio-biz-id",
  "reportBizId": "report-biz-id",
  "productBizId": "product-biz-id",
  "idempotencyKey": "REPORT-report-biz-id-product-biz-id"
}
```

前端动作：

- 只有报告质量门禁通过才展示按钮。
- 如果失败，跳 Risk Audit 查询同 target 的审计记录。

#### 买入

```text
POST /api/mock/portfolios/orders/buy
```

请求：

```json
{
  "portfolioBizId": "portfolio-biz-id",
  "productBizId": "product-biz-id",
  "amount": 10000,
  "idempotencyKey": "BUY-product-amount-timestamp"
}
```

#### 卖出

```text
POST /api/mock/portfolios/orders/sell
```

请求：

```json
{
  "portfolioBizId": "portfolio-biz-id",
  "productBizId": "product-biz-id",
  "quantity": 100,
  "idempotencyKey": "SELL-product-quantity-timestamp"
}
```

#### 再平衡

```text
POST /api/mock/portfolios/rebalance/execute
```

请求：

```json
{
  "portfolioBizId": "portfolio-biz-id",
  "targets": [
    {
      "productBizId": "product-a",
      "targetWeight": 0.4
    },
    {
      "productBizId": "product-b",
      "targetWeight": 0.3
    }
  ],
  "minTradeAmount": 100,
  "idempotencyKey": "REBALANCE-20260625"
}
```

### 9.10 回测、反馈、评估接口

#### 从组合生成回测

```text
POST /api/backtests/generate-from-portfolio
```

请求：

```json
{
  "portfolioBizId": "portfolio-biz-id",
  "strategyCode": "AUTO_CLOSED_LOOP_MOCK",
  "strategyVersion": "CLR-20260625-00000000",
  "benchmarkCode": "CSI300",
  "parameters": "{\"source\":\"FRONTEND\"}",
  "limit": 100
}
```

#### 查询回测列表

```text
POST /api/backtests/list
```

请求：

```json
{
  "strategyCode": "AUTO_CLOSED_LOOP_MOCK",
  "strategyVersion": "CLR-20260625-00000000",
  "status": "SUCCEEDED",
  "page": 1,
  "size": 20,
  "sort": "createdAt",
  "direction": "desc"
}
```

#### 保存反馈

```text
POST /api/ai/feedback/save
```

请求：

```json
{
  "targetType": "MOCK_PORTFOLIO",
  "targetBizId": "portfolio-biz-id",
  "reportBizId": "report-biz-id",
  "promptBizId": "prompt-biz-id",
  "promptCode": "investment-plan-from-report",
  "promptVersion": "auto-v1",
  "backtestBizId": "backtest-biz-id",
  "feedbackAction": "WATCH",
  "reasonCode": "AUTO_MOCK_EXECUTED",
  "commentText": "继续观察",
  "metadata": "{\"runBizId\":\"run-biz-id\"}"
}
```

#### 查询 Prompt 评估

```text
POST /api/ai/prompt-evaluations/list
```

请求：

```json
{
  "promptCode": "investment-plan-from-report",
  "promptVersion": "auto-v1",
  "scenario": "REPORT_PROMPT_GOVERNANCE",
  "reviewStatus": "PENDING",
  "page": 1,
  "size": 20,
  "sort": "evaluatedAt",
  "direction": "desc"
}
```

### 9.11 风控审计接口

```text
POST /api/risk/checks/list
```

请求：

```json
{
  "businessType": "REPORT",
  "businessBizId": "report-biz-id",
  "userBizId": "user-biz-id",
  "checkResult": "REJECT",
  "riskLevel": "HIGH",
  "reasonCode": "LOW_DATA_QUALITY",
  "page": 1,
  "size": 20,
  "sort": "checkedAt",
  "direction": "desc"
}
```

响应核心：

```json
{
  "items": [
    {
      "bizId": "risk-biz-id",
      "businessType": "REPORT",
      "businessBizId": "report-biz-id",
      "userBizId": "user-biz-id",
      "ruleCode": "REPORT_DATA_QUALITY",
      "checkResult": "REJECT",
      "riskLevel": "HIGH",
      "reasonCode": "LOW_DATA_QUALITY",
      "detail": "{\"dataQualityScore\":0.2}",
      "checkedAt": "2026-06-25T10:00:00"
    }
  ]
}
```

## 10. 页面生成方案

### 10.1 Overview 投资驾驶舱

目标：作为第一屏，展示系统是否正在健康地自动成长。

#### 数据来源

| 区域 | 接口 |
| --- | --- |
| 闭环运行 | `/api/investment/closed-loop/runs/list` |
| 闭环详情 | `/api/investment/closed-loop/runs/detail` |
| 数据源健康 | `/api/admin/data-sources/list` |
| 任务执行 | `/api/investment/tasks/executions/list` |
| 最新报告 | `/api/investment/analysis/reports/list` |
| Mock 组合 | `/api/mock/portfolios/mine` |
| 风控拦截 | `/api/risk/checks/list` |
| 回测 | `/api/backtests/list` |
| Prompt 评估 | `/api/ai/prompt-evaluations/list` |

#### 页面布局

```text
顶部：全局状态指标条
  - 今日闭环运行数
  - 成功率 / 阻断率
  - 平均数据质量分
  - 高风险拦截数

中部：自动闭环可视化时间线
  DATA_COLLECTION -> REPORT_GENERATION -> QUALITY_GATE -> PROMPT_CANDIDATE
  -> MODEL_CANDIDATE -> MOCK_TRADE -> BACKTEST_FEEDBACK -> GUARDS

左侧：机会雷达
  - 最新可执行报告
  - 热门主题快照
  - 可 Mock 产品数

右侧：风险和待处理队列
  - 低质量报告
  - 失败任务
  - Prompt 待复核
  - 模型候选待确认

底部：Mock 资产与回测摘要
```

#### 组件拆分

| 组件 | 职责 |
| --- | --- |
| `ClosedLoopTimeline.vue` | 展示步骤节点、状态、耗时、阻断原因 |
| `QualityGatePanel.vue` | 展示质量分、门禁、缺口 |
| `AutomationGuardPanel.vue` | 展示 Prompt/模型/真实交易闸门 |
| `ActionQueue.vue` | 展示待处理事项 |
| `RiskTicker.vue` | 展示最近风控拦截 |
| `MockPerformanceMiniChart.vue` | 展示组合收益小图 |

#### 视觉要求

- 第一屏必须直接看到闭环状态，不用点菜单。
- 中部时间线使用横向步骤条或 Sankey 风格流程图。
- `BLOCKED/FAILED` 节点红色，`SKIPPED` 灰色，`SUCCEEDED` 绿色。
- 每个节点 hover 展示 `inputSummary/outputSummary` 摘要。
- 点击节点打开详情抽屉。

#### 生成步骤

1. 新增 `entities/closed-loop`。
2. 在 `endpoints.ts` 增加闭环接口。
3. 生成 `ClosedLoopTimeline`。
4. Overview 首屏调用闭环列表。
5. 点击运行记录调用闭环详情。
6. 并行加载数据源、任务、报告、风控、回测摘要。
7. 用 adapter 计算 KPI。
8. 补空状态和错误状态。

#### 验收

- 无数据时显示“等待真实采集和自动闭环运行”，不展示假数据。
- 有 `BLOCKED` 运行时能看到阻断原因。
- 能从运行详情跳转报告、组合、回测。
- 真实交易闸门始终可见。

### 10.2 Data Quality 数据质量页

目标：让用户明确知道数据质量能否支撑报告和 Mock。

#### 页面结构

```text
质量总览
  - L1/L2 可用数据源
  - 最近成功采集
  - 平均质量分
  - 缺口数量

数据源健康矩阵
  行：sourceType
  列：trustLevel
  单元：数量、成功率、质量等级

数据源列表
  sourceCode / sourceName / trustLevel / enabled / successRate / qualityScore

质量趋势抽屉
  qualityScore / missingRate / duplicateRate / freshnessScore
```

#### 关键交互

- 点击数据源行打开详情抽屉。
- 点击质量趋势调用 `/quality/list`。
- L1/L2 失败时 Overview 也要显示待处理。
- `qualityScore < 0.45` 时标记“不能支撑自动 Mock”。

### 10.3 Data Ingestion 采集编排页

目标：配置和触发真实采集任务，不写兜底假数据。

#### 页面结构

```text
任务总览 tabs:
  - 全部任务
  - 采集任务
  - 自动报告
  - Prompt 治理
  - 自动闭环

任务表格:
  code / type / cron / enabled / latest status / action

任务详情抽屉:
  基础配置
  参数表单
  手动触发
  最近执行记录
```

#### 按任务类型生成表单

`REGULATORY_DISCLOSURE_COLLECTION`：

| 字段 | 控件 |
| --- | --- |
| `endpoints` | 可增删端点列表 |
| `responseFormat` | Select：JSON/HTML/RSS |
| `itemsPath` | Input |
| `externalIdPath` | Input |
| `titlePath` | Input |
| `publishTimePath` | Input |
| `includeKeywords` | Tags input |
| `maxItems` | InputNumber |

`WEALTH_PRODUCT_NAV_REFRESH`：

| 字段 | 控件 |
| --- | --- |
| `productMarketCode` | Select |
| `productCurrency` | Select |
| `quoteInterval` | Select |
| `defaultRiskLevel` | Select |
| `extraFieldPaths` | 字段映射编辑器 |

`AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION`：

| 字段 | 控件 |
| --- | --- |
| `automationLevel` | Segmented |
| `dataTaskCodes` | MultiSelect |
| `reportTaskCode` | Select |
| `promptTaskCode` | Select |
| `mockUserBizId` | Select / Input |
| `minQualityScore` | Slider + InputNumber |
| `allowAutoMockTrade` | Switch |
| `allowPromptCandidate` | Switch |
| `allowModelCandidate` | Switch |
| `allowAutoPromptActivation` | Disabled Switch + Guard Notice |
| `allowAutoModelActivation` | Disabled Switch + Guard Notice |
| `allowRealTrade` | Disabled Switch + Danger Notice |

#### 验收

- 保存后任务定义刷新。
- 手动触发后能看到 eventId。
- 执行失败能看到 failureReason。
- 自动闭环任务能跳转 Overview 运行详情。

### 10.4 Product & Risk 产品风险页

目标：把产品池、风险画像、行情和 Mock 门禁做成投资对象详情。

#### 页面结构

```text
左侧：产品筛选和列表
右侧：产品详情
  - 基础信息
  - 风险画像
  - 行情 / 净值曲线
  - 主题关系
  - Mock 门禁
  - 关联风控审计
```

#### 列表字段

| 字段 | UI |
| --- | --- |
| productCode | 代码 |
| productName | 名称 |
| productType | Tag |
| tradeStatus | Tag |
| riskLevel | Risk badge |
| dataQualityScore | Progress |
| mockTradable | Switch-like readonly Tag |
| latestNav | 数值 |
| latestQuoteTime | 时间 |

#### 详情可视化

- 行情折线图：closePrice/nav。
- 风险雷达：波动、流动性、最大回撤、质量分。
- 主题关系权重条。
- Mock 门禁面板。

### 10.5 Report Studio 投资报告页

目标：让报告可解释、可追溯、可执行或明确不可执行。

#### 页面结构

```text
报告筛选:
  marketScope / themeCode / providerCode / status / confidenceLevel

报告列表:
  theme / status / confidence / quality / generatedAt / actions

报告详情:
  质量门禁
  投资摘要
  趋势图
  投资计划
  模拟收益
  Prompt 快照
  关联 Mock / 回测 / 风控
```

#### 动作显隐

| 条件 | 动作 |
| --- | --- |
| `status != SUCCEEDED/SUCCESS` | 禁用 Mock |
| `confidenceLevel=UNUSABLE` | 禁用 Mock 和 Prompt 执行 |
| `dataQualityGate.passed=false` | 禁用 Mock |
| `investmentPlan.planType=DATA_GAP_REPORT` | 禁用 Mock，显示补数 |
| `referenceAllocationAmount <= 0` | 禁用报告转买入 |

#### 图表

- 趋势方向：折线/面积图。
- 热度：柱状图。
- 模拟收益：收益曲线。
- 质量门禁：仪表盘或环形进度。

### 10.6 Prompt Lab 页面

目标：把 Prompt 和模型变成可控资产。

#### 页面结构

```text
顶部 tabs:
  - Prompt 模板
  - Prompt 预览
  - Prompt 评估
  - 模型配置
  - 模型候选

Prompt 模板:
  列表 + 详情编辑器 + 变量面板 + Schema 面板

Prompt 预览:
  变量输入 + renderedPrompt + missingVariables

模型候选:
  自动闭环候选模型列表 + 评分 + 来源报告 + 启用闸门
```

#### 编辑器要求

- 模板内容 textarea 要高亮 `${variable}`。
- 变量缺失要在旁边列出。
- Schema JSON 要格式化和校验。
- `ACTIVE` 状态修改必须二次确认。
- `RETIRED` 只读，可复制为新版本。

### 10.7 Simulation 模拟交易页

目标：完成 Mock 组合、订单、估值、再平衡和收益展示。

#### 页面结构

```text
组合列表
  - 总资产
  - 现金
  - 持仓市值
  - 累计收益

组合详情
  - 估值曲线
  - 持仓表
  - 订单时间线
  - 买入 / 卖出 / 再平衡

报告转买入
  - 选择报告
  - 选择组合
  - 预览配置金额
  - 执行 Mock
```

#### 风控失败处理

Mock 接口失败时：

1. 展示后端错误消息。
2. 自动按 `businessBizId` 查询 `/api/risk/checks/list`。
3. 展示最新 `REJECT` 审计。
4. 提供跳转到产品、报告、组合详情。

### 10.8 Review Loop 复盘闭环页

目标：回看策略结果，把反馈反哺 Prompt 和模型。

#### 页面结构

```text
左：回测列表
中：回测详情和指标
右：反馈面板
底：Prompt 评估列表
```

#### 指标展示

- totalReturnRate
- maxDrawdown
- volatility
- startAsset
- endAsset
- pointCount

#### 反馈表单

| 字段 | 控件 |
| --- | --- |
| targetType | Select |
| targetBizId | Object picker |
| reportBizId | Select |
| promptCode | Select |
| promptVersion | Select |
| backtestBizId | Select |
| feedbackAction | Segmented |
| reasonCode | Select |
| commentText | Textarea |

### 10.9 Risk Audit 风控审计页

目标：集中查看全链路拦截，不让失败藏在接口错误里。

#### 页面结构

```text
筛选条:
  businessType / businessBizId / userBizId / checkResult / riskLevel / reasonCode

拦截统计:
  HIGH 数量 / REJECT 数量 / 数据质量问题 / 现金问题 / 产品问题

审计表:
  ruleCode / checkResult / riskLevel / reasonCode / target / checkedAt

详情抽屉:
  detail JSON
  关联对象入口
  建议修复动作
```

## 11. 可视化组件库

建议新增：

```text
src/shared/components/visual/
  StatusTag.vue
  ScoreGauge.vue
  QualityBar.vue
  JsonPreview.vue
  EntityLink.vue
  TimelineFlow.vue
  RiskReasonTag.vue
  EmptyEvidence.vue
```

### 11.1 ScoreGauge

用途：

- 数据质量分。
- Prompt 评分。
- 模型候选评分。
- 成功率。

规则：

| 分数 | 状态 |
| --- | --- |
| `>= 0.75` | good |
| `>= 0.45` | usable |
| `< 0.45` | blocked |

### 11.2 JsonPreview

用途：

- `dataQualityGate`
- `investmentPlan`
- `chartPayload`
- `promptSnapshot`
- `inputSummary`
- `outputSummary`
- `risk.detail`

要求：

- 支持折叠。
- 支持复制。
- JSON parse 失败时显示原文。

### 11.3 EntityLink

根据目标类型跳转：

| targetType | 跳转 |
| --- | --- |
| `REPORT` | Report Studio detail |
| `MOCK_PORTFOLIO` | Simulation portfolio |
| `MOCK_ORDER` | Simulation order |
| `BACKTEST` | Review Loop backtest |
| `PROMPT` | Prompt Lab detail |
| `PRODUCT` | Product & Risk detail |

## 12. Adapter 规则

### 12.1 JSON 字符串解析

后端部分字段是 JSON 字符串，adapter 必须统一解析：

```ts
export const safeParseJson = <T = unknown>(value?: string): T | undefined => {
  if (!value) return undefined
  try {
    return JSON.parse(value) as T
  } catch {
    return undefined
  }
}
```

必须解析字段：

| 业务域 | 字段 |
| --- | --- |
| report | dataQualityGate、investmentSummary、trend、investmentPlan、simulatedReturn、chartPayload、promptSnapshot |
| closed-loop | summary、inputSummary、outputSummary |
| backtest | parameters、metrics |
| feedback | metadata |
| risk | detail |
| ai-model | modelConfig、metrics |

### 12.2 字段兼容

| 后端字段 | 前端兼容 |
| --- | --- |
| `dataQualityScore` | report adapter 映射到 `qualityScore` 或统一改 DTO |
| `SUCCESS` | 等同 `SUCCEEDED` |
| `checkedAt` | risk DTO 当前若使用 `createdAt`，adapter 需兼容 |
| `businessType/businessBizId` | risk DTO 当前若使用 `targetType/targetBizId`，adapter 需兼容 |

## 13. 配置中心建议

`/config-center` 不做孤立配置菜单，而做全局配置工作台。

分区：

| 分区 | 内容 |
| --- | --- |
| 数据端点模板 | 官方/授权 API endpoint 模板、字段路径模板 |
| 自动化开关 | allowAutoMockTrade、allowPromptCandidate、allowModelCandidate |
| 安全闸门 | Prompt 启用、模型启用、真实交易开关说明 |
| OpenAI 兼容配置 | baseUrl、model、secretRef、mockEnabled |
| 展示偏好 | 默认市场、默认主题、刷新间隔 |

注意：

- `allowRealTrade` 当前只能展示为禁用态。
- `allowAutoPromptActivation`、`allowAutoModelActivation` 当前只能展示为需要人工确认。

## 14. 生成实施顺序

### Step 0：基础契约补齐

生成内容：

- `entities/closed-loop/*`
- `shared/api/endpoints.ts` 增加 closedLoop。
- 补齐 task 字典。
- 补 `JsonPreview`、`StatusTag`、`ScoreGauge`。
- 补 `safeParseJson`。

验收：

- `npm run type-check` 通过。
- 没有页面直接写接口路径。
- 所有新增状态从字典读取。

### Step 1：Overview 驾驶舱

生成内容：

- 替换 `pages/overview/OverviewPage.vue` 占位。
- 新增 `ClosedLoopTimeline`。
- 新增 `AutomationGuardPanel`。
- 新增 `ActionQueue`。

验收：

- 首屏可见闭环状态。
- 能打开闭环详情抽屉。
- 无数据时不造假。

### Step 2：Data Quality

生成内容：

- 数据源健康矩阵。
- 数据源列表。
- 质量趋势抽屉。

验收：

- L1/L2 状态突出。
- 质量低时报告入口能感知降级。

### Step 3：Data Ingestion

生成内容：

- 任务定义列表。
- 任务类型参数表单。
- 手动触发。
- 执行记录。

验收：

- 自动闭环任务参数可查看和保存。
- 端点为空显示明确提示。
- 手动触发后能看执行记录。

### Step 4：Product & Risk

生成内容：

- 产品列表和详情。
- 行情曲线。
- 风险画像。
- Mock 门禁。

验收：

- 不可 Mock 产品禁用交易入口。
- 风险和数据质量清晰可见。

### Step 5：Report Studio

生成内容：

- 报告列表。
- 报告详情。
- JSON 图表解析。
- 报告转 Mock 动作。

验收：

- 低质量报告不能触发 Mock。
- 数据缺口报告只展示补数建议。

### Step 6：Prompt Lab

生成内容：

- Prompt 列表和详情。
- Prompt 编辑器。
- Prompt 预览。
- Prompt 评估。
- 模型候选。

验收：

- 模型候选不可直接自动启用。
- ACTIVE/RETIRED 行为正确。

### Step 7：Simulation

生成内容：

- 组合列表。
- 组合详情。
- 买入、卖出、撤单。
- 再平衡。
- 收益曲线。

验收：

- Mock 标识明确。
- 失败能跳风控审计。

### Step 8：Review Loop + Risk Audit

生成内容：

- 回测列表和详情。
- 反馈表单。
- Prompt 评估列表。
- 风控审计表和详情。

验收：

- 反馈能关联报告、Prompt、回测。
- 风控 detail JSON 可展开。

## 15. 页面完成标准

每个页面完成必须满足：

- 有 loading。
- 有 empty。
- 有 error。
- 有权限/401 处理。
- 有业务错误提示。
- 有至少一个真实接口联调。
- 有字段字典。
- 有风险提示。
- 有移动端不溢出的布局。
- 有可点击详情或抽屉。
- 没有假业务数据。

## 16. 视觉设计标准

### 16.1 整体风格

- 投资操作舱。
- 深浅结合，但主内容不应过暗。
- 信息密度高，但层次清楚。
- 避免营销页 hero。
- 避免大面积装饰渐变。
- 避免页面只是一张表。

### 16.2 推荐布局

| 场景 | 布局 |
| --- | --- |
| Overview | 顶部指标 + 中部闭环流 + 两侧队列 + 底部图表 |
| 列表详情 | 左列表 + 右详情 / 抽屉 |
| 配置表单 | 基础配置 + 参数配置 + 预览 + 执行记录 |
| 图表页 | 图表在上，表格在下 |
| 审计页 | 聚合指标 + 筛选 + 审计流 |

### 16.3 颜色语义

| 状态 | 颜色语义 |
| --- | --- |
| 成功 / 可执行 | green |
| 运行中 / 中可信 | blue |
| 待复核 / 观察 | amber |
| 阻断 / 高风险 | red |
| 跳过 / 归档 | gray |

颜色必须用于状态，不做纯装饰。

## 17. 自动化边界展示

前端必须在 Overview、Data Ingestion、Prompt Lab、Config Center 展示：

```text
Mock 投资闭环：允许自动执行
真实数据采集：允许自动执行
Prompt 候选：允许自动生成和评分
模型候选：允许自动生成 DRAFT 和评分
Prompt 正式启用：需要人工确认或灰度开关
模型正式启用：需要人工确认或灰度开关
真实交易：当前不自动执行
```

任何“启用 Prompt / 启用模型 / 真实交易”的按钮必须：

1. 默认隐藏或禁用。
2. 展示安全说明。
3. 二次确认。
4. 记录操作者和原因。

## 18. 数据为空时的文案

| 场景 | 文案 |
| --- | --- |
| 无数据源 | 尚未配置真实数据源，请先在数据质量页配置 L1/L2 来源。 |
| 无任务执行 | 暂无任务执行记录，可在采集编排页手动触发。 |
| 无报告 | 暂无投资报告，等待真实采集任务产出有效数据。 |
| 报告低质量 | 当前报告质量不足，只能查看数据缺口，不能执行 Mock。 |
| 无 Mock 组合 | 暂无模拟组合，可创建组合或等待自动闭环创建。 |
| 无回测 | 暂无回测结果，可从 Mock 组合生成回测摘要。 |
| 无风控审计 | 暂无风控拦截记录。 |
| 无闭环运行 | 暂无自动闭环运行记录，可触发自动闭环任务。 |

## 19. 最终验收清单

### 19.1 功能验收

- Overview 能展示自动闭环运行和步骤。
- Data Quality 能展示数据源健康和质量趋势。
- Data Ingestion 能配置任务并手动触发。
- Product & Risk 能展示产品风险和行情。
- Report Studio 能展示报告质量和报告详情。
- Prompt Lab 能管理 Prompt、预览、评估和模型候选。
- Simulation 能完成 Mock 交易和收益曲线。
- Review Loop 能生成回测和保存反馈。
- Risk Audit 能查询风控拦截。

### 19.2 正确性验收

- 所有请求都是 POST JSON。
- 字段来自 DTO 和 adapter，不在页面临时拼。
- 所有下拉来自字典。
- 低质量报告不能执行 Mock。
- 产品不可 Mock 时按钮禁用。
- 真实交易不可自动执行。
- 自动候选模型不自动 ACTIVE。
- 风控拒绝可以查到审计。

### 19.3 UI 验收

- 第一屏不是菜单页。
- 页面不是单纯表格堆叠。
- 闭环流可视化清晰。
- 关键状态颜色一致。
- 文本不溢出。
- 移动端可读。
- 图表不遮挡内容。
- Mock 和风险提示始终可见。

### 19.4 工程验收

执行：

```bash
npm run type-check
npm run build
```

检查：

- 没有 TypeScript 错误。
- 没有未使用的大块 mock 数据。
- 没有业务页面直接写接口路径。
- 没有散落的状态文字和颜色。
- 没有绕过 `httpClient` 的请求。

## 20. 后续增强

| 优先级 | 增强 | 说明 |
| --- | --- | --- |
| P0 | 生成 Overview | 先把自动闭环跑起来看见 |
| P0 | 补 closed-loop entity | 当前前端缺自动闭环 API |
| P0 | 补任务字典和参数表单 | 支撑可配置真实采集 |
| P1 | 后端 Dashboard 聚合接口 | 降低 Overview 多接口并发 |
| P1 | 数据源字段模板库 | 降低端点配置难度 |
| P1 | Prompt 候选版本自动复制 | 让评分进一步推动 Prompt 版本成长 |
| P2 | 更细角色权限 | 区分运营、研究、复核、管理员 |
