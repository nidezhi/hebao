# 前端重构方案

## 1. 文档目的

本文档用于指导 DZCOM 投资平台前端重构。

当前后端已经具备产品画像、数据源治理、投资分析报告、Prompt 版本、Mock 交易、风控审计、回测、反馈和 Prompt 评估等前端可见接口。前端重构目标不是继续堆叠菜单管理页，而是将这些后台能力组织成一个可操作、可解释、可复盘的投资工作台。

重构必须服务于项目核心目标：

| 核心目标 | 前端表达 |
| --- | --- |
| 优秀的产品、风险、投资架构 | 产品风险画像、资产类别、Mock 可交易状态、风险等级和主题关系清晰可见 |
| 高质量数据来源 | 数据源健康、质量分、可信等级、缺口原因和降级状态在关键页面直接展示 |
| 不断成长，AI 驱动，前端可控 | Prompt 版本、变量、输出 Schema、预览、状态流和评估记录可管理 |
| 全程自动化优化成长 | 报告、Prompt、Mock 交易、回测、风控审计和反馈形成可追踪链路 |

## 2. 重构原则

### 2.1 产品原则

- 第一屏必须是实际工作台，不做空泛首页、营销页或单纯菜单入口。
- 后台复杂能力要前端可见，但最终用户路径要简洁：看数据质量、看报告、做模拟、看回测、给反馈。
- 低质量数据必须明确降级展示，不允许包装成确定性投资建议。
- 所有投资动作必须能追踪到数据来源、报告、Prompt、风控、Mock 订单、回测和反馈。
- 管理能力要服务闭环，不能变成孤立 CRUD。

### 2.2 UI 原则

- 整体采用“投资驾驶舱 + 工作台”结构，而不是传统左侧菜单堆页面。
- 可以有视觉张力，但不能牺牲信息密度和可操作性。
- 图表、矩阵、时间线、状态流和审计流是核心表达方式。
- 卡片只用于独立对象，例如产品、报告、订单、回测记录；不要卡片套卡片。
- 页面区块使用全宽工作区或分栏布局，不做漂浮装饰区块。
- 按钮优先使用图标 + tooltip，常见操作使用熟悉图标。
- 表格只承载批量扫描，不承担全部信息表达。
- 所有状态必须有明确颜色和文案：正常、降级、待复核、拒绝、失败、不可 Mock、低质量。

### 2.3 前端开发铁律

- 业务接口统一通过 API client 调用，页面组件不得直接散写请求路径。
- DTO、领域视图模型和表单模型分层，不在页面里临时拼接复杂后端结构。
- 字典集中管理，筛选项、表单下拉、Tag、图例和文案共用同一份定义。
- 所有接口按后端约束使用 `POST + JSON body`，不使用业务 query/path 参数。
- 数据质量门禁必须成为 UI 状态，不得只在详情 JSON 中隐藏。
- 风控拒绝、403 越权、数据不足、产品不可 Mock、现金不足等失败必须有明确前端状态。
- 投资相关页面必须展示风险提示，不得将 Mock 收益表达为真实收益承诺。
- 页面组件按业务域组织，避免一个 `components` 目录承载所有内容。
- 图表数据在 adapter 层转换，组件只消费稳定的展示模型。
- 前端重构过程中每个阶段都要能独立运行和验收。

## 3. 信息架构

推荐顶层工作区：

| 工作区 | 目标 | 主要接口 |
| --- | --- | --- |
| Overview 投资驾驶舱 | 查看全局健康、机会、风险和待处理事项 | 数据源、报告、Mock 组合、风控审计、回测摘要 |
| Data Quality 数据质量 | 查看数据源健康、质量分和缺口 | `/api/admin/data-sources/list`、`/api/admin/data-sources/quality/list` |
| Product & Risk 产品风险 | 管理产品画像、风险等级、主题关系和 Mock 门禁 | `/api/products/detail`、`/api/admin/products/investment-profile/save` |
| Report Studio 投资报告 | 生成、查看和解释投资报告 | `/api/investment/analysis/generate`、`/api/investment/analysis/reports/list` |
| Prompt Lab Prompt 实验室 | 管理 Prompt 版本、变量、Schema 和预览 | `/api/ai/prompts/*` |
| Simulation 模拟交易 | Mock 组合、订单、持仓、估值、调仓和收益曲线 | `/api/mock/portfolios/*` |
| Review Loop 复盘闭环 | 回测、反馈、Prompt 评估和采纳拒绝 | `/api/backtests/*`、`/api/ai/feedback/*`、`/api/ai/prompt-evaluations/*` |
| Risk Audit 风控审计 | 查看全链路拦截原因 | `/api/risk/checks/list` |

导航建议：

- 顶部使用工作区切换，而不是深层左侧菜单。
- 二级视图使用 Tabs 或 segmented controls。
- 详情页使用右侧抽屉或分栏，不频繁跳转。
- 首页工作台直接展示核心闭环，不做“欢迎使用”类页面。

## 4. 核心页面方案

### 4.1 Overview 投资驾驶舱

第一屏布局：

- 顶部状态条：数据质量均分、可用数据源、可 Mock 产品、待复核 Prompt、风险拦截数、Mock 组合收益。
- 中部闭环流：`数据源 -> 产品画像 -> 投资报告 -> Prompt -> Mock 交易 -> 回测 -> 反馈`。
- 左侧机会面板：主题热度、报告可信等级、可操作报告。
- 右侧风险审计流：最近拦截、拦截原因、风险等级、关联对象。
- 底部资产曲线：当前用户 Mock 组合总资产、收益率、最大回撤。

交互要求：

- 点击闭环节点进入对应工作区。
- 低质量报告只能进入“查看缺口”和“补数任务”，不能展示 Mock 交易入口。
- 风控审计可直接跳到关联报告、订单、组合或回测。

### 4.2 Data Quality 数据质量

页面组成：

- 数据源健康矩阵：按 `sourceType` 和 `trustLevel` 展示启用、失败、降级。
- 质量趋势图：按 `qualityScore`、`sampleCount`、`missingRatio` 展示历史。
- 缺口列表：数据类型、来源、失败原因、最近成功时间。
- 数据源详情：基础信息、健康状态、质量快照历史。

核心状态：

- `L1/L2` 数据源异常应高亮。
- `L5` 兜底数据只能标记为演示或链路验证。
- `qualityLevel=LOW/DEMO_ONLY` 时，相关报告页面必须展示降级状态。

### 4.3 Product & Risk 产品风险

页面组成：

- 产品池列表：资产类别、风险等级、交易状态、数据质量、Mock 可交易。
- 产品画像详情：风险摘要、波动等级、流动性、最大回撤、适配风险等级。
- 主题关系图：主题、行业、指数、资产类别与产品关系权重。
- Mock 门禁面板：是否允许 Mock、不可 Mock 原因、最小持有天数、交易备注。

交互要求：

- `mockTradable=false` 时，所有 Mock 交易入口禁用并展示原因。
- `dataQualityScore` 低于阈值时，产品可查看但不可作为推荐主标的。
- 风险等级不匹配时，前端展示风控提示，不绕过后端校验。

### 4.4 Report Studio 投资报告

页面组成：

- 报告列表：主题、市场、可信等级、质量分、生成时间、允许动作。
- 报告详情：质量门禁、趋势、投资计划、模拟收益、图表证据、风险提示。
- 报告生成面板：市场范围、主题、回看天数、初始资金。
- 报告转动作：生成 Prompt、生成 Mock 买入、进入回测。

降级规则：

- `dataQualityGate.passed=false` 时隐藏生成 Prompt、Mock 交易和配置建议入口。
- `confidenceLevel=UNUSABLE` 时只展示数据缺口。
- `investmentPlan.planType=DATA_GAP_REPORT` 时展示补数建议，不展示配置金额。

### 4.5 Prompt Lab Prompt 实验室

页面组成：

- Prompt 版本列表：编码、版本、场景、状态、更新时间。
- 模板编辑区：模板内容、变量引用、缺失变量提示。
- 变量面板：变量名、来源路径、是否必填、描述。
- 输出 Schema 面板：Schema 版本、JSON Schema、启用状态。
- 预览区：本地渲染结果、缺失变量、是否可进入模型调用。

状态流：

```text
DRAFT -> VALIDATING -> ACTIVE -> RETIRED
```

交互要求：

- `ACTIVE` 版本作为默认调用候选。
- `RETIRED` 可查看和复制，但不作为默认生成入口。
- 预览不触发真实模型调用，只做变量替换和必填校验。
- Prompt 评估数据从 Review Loop 回流到版本详情。

### 4.6 Simulation 模拟交易

页面组成：

- 我的 Mock 组合：总资产、现金、持仓市值、累计收益、最大回撤。
- 组合详情：资产曲线、收益率曲线、持仓列表、现金、估值来源。
- 订单工作区：买入、卖出、撤单、订单事件时间线。
- 再平衡面板：目标权重编辑、预估交易、执行结果。
- 报告转买入：从报告建议金额生成 Mock 买入。

交互要求：

- 即时成交订单进入终态后，撤单按钮禁用。
- 卖出数量不能超过可用持仓。
- 现金不足、产品不可 Mock、行情缺失、目标权重非法必须展示后端风控原因。
- 组合收益必须标记为 Mock 结果，不得暗示真实收益。

### 4.7 Review Loop 复盘闭环

页面组成：

- 回测结果列表：策略编码、版本、状态、区间、指标摘要。
- 回测详情：参数、指标、收益、回撤、波动、来源组合。
- 反馈列表：目标类型、采纳/拒绝/观察/忽略、原因、关联报告/Prompt/回测。
- Prompt 评估列表：评分、场景、复核状态、关联回测和反馈。
- 复盘链路图：报告 -> Prompt -> Mock 订单 -> 回测 -> 反馈 -> Prompt 评分。

交互要求：

- 回测 `status` 可省略，后端默认 `PENDING`。
- 从 Mock 组合生成回测时，前端传入的参数会回写到 `parameters.clientParameters`。
- 反馈携带 `promptCode` 和 `promptVersion` 时，后端会自动生成 `FEEDBACK_LOOP` 评估。
- 403 越权统一展示为“当前记录不可见或已无权限访问”。

### 4.8 Risk Audit 风控审计

页面组成：

- 风控检查列表：结果、风险等级、原因编码、目标类型、目标 ID、创建时间。
- 拦截原因聚合：质量不足、风险不匹配、现金不足、产品不可 Mock、目标权重非法。
- 关联对象入口：报告、产品、订单、组合、回测。

前端用途：

- 每次 Mock 交易失败都能找到审计记录。
- Overview 首页展示最新高风险拦截。
- 产品、报告、组合详情页展示关联风控记录。

## 5. API Client 和前端目录建议

当前后端工作区未发现前端工程文件；以下结构按已初始化前端工程落地。

推荐目录：

```text
src/
  app/
    routes/
    providers/
  shared/
    api/
    dictionaries/
    components/
    charts/
    errors/
    utils/
  entities/
    data-source/
    product/
    report/
    prompt/
    portfolio/
    risk/
    backtest/
    feedback/
  features/
    generate-report/
    edit-prompt/
    execute-mock-order/
    rebalance-portfolio/
    submit-feedback/
  pages/
    overview/
    data-quality/
    product-risk/
    report-studio/
    prompt-lab/
    simulation/
    review-loop/
    risk-audit/
```

API client 分层：

| 层 | 职责 |
| --- | --- |
| `shared/api/httpClient` | 认证、错误处理、POST JSON、Result 解包 |
| `shared/api/endpoints` | 统一维护接口路径 |
| `entities/*/api` | 业务域接口函数 |
| `entities/*/model` | DTO、ViewModel、adapter |
| `features/*` | 业务动作和表单状态 |
| `pages/*` | 页面编排，不直接处理后端细节 |

统一错误处理：

| HTTP/业务状态 | 前端表现 |
| --- | --- |
| `400` | 表单字段或操作参数错误 |
| `401` | 跳登录或刷新登录态 |
| `403` | 当前记录不可见、权限不足或关联数据不属于当前用户 |
| `404` | 记录不存在或已删除 |
| 风控拒绝 | 展示 `reasonCode`、`riskLevel`、`message` 和关联审计入口 |

## 6. 字典和下拉项

所有下拉项必须集中在字典模块，页面不得硬编码。

### 6.1 数据源字典

| 字典 | 值 |
| --- | --- |
| `sourceType` | `MARKET`、`NEWS`、`ANNOUNCEMENT`、`RESEARCH`、`REGULATORY`、`FALLBACK` |
| `trustLevel` | `L1`、`L2`、`L3`、`L4`、`L5` |
| `dataType` | `MARKET_QUOTE`、`NEWS`、`ANNOUNCEMENT`、`RESEARCH`、`REGULATORY` |
| `qualityLevel` | `HIGH`、`MEDIUM`、`LOW`、`UNKNOWN`、`DISABLED`、`DEMO_ONLY` |

### 6.2 产品和风险字典

| 字典 | 值 |
| --- | --- |
| `assetClass` | `STOCK`、`ETF`、`FUND`、`BOND`、`BANK_WMP`、`GOLD`、`REIT`、`CASH`、`OTHER` |
| `volatilityLevel` | `LOW`、`MEDIUM`、`HIGH` |
| `liquidityLevel` | `LOW`、`MEDIUM`、`HIGH` |
| `riskLevel` | `1` 保守、`2` 稳健、`3` 平衡、`4` 成长、`5` 进取 |
| `relationType` | `THEME`、`INDUSTRY`、`INDEX`、`ASSET_CLASS` |

### 6.3 Prompt 和 AI 字典

| 字典 | 值 |
| --- | --- |
| `promptScenario` | `INVESTMENT_REPORT`、`INVESTMENT_PLAN`、`RISK_AUDIT`、`BACKTEST_FEEDBACK`、`FEEDBACK_LOOP` |
| `promptStatus` | `DRAFT`、`VALIDATING`、`ACTIVE`、`RETIRED` |
| `reviewStatus` | `PENDING`、`APPROVED`、`REJECTED`、`ARCHIVED` |
| `targetType` | `REPORT`、`RECOMMENDATION`、`MOCK_ORDER`、`MOCK_PORTFOLIO`、`BACKTEST`、`PROMPT` |

### 6.4 Mock 交易和回测字典

| 字典 | 值 |
| --- | --- |
| `orderSide` | `BUY`、`SELL` |
| `orderStatus` | `SUBMITTED`、`FILLED`、`CANCELLED`、`REJECTED`、`FAILED` |
| `backtestStatus` | `PENDING`、`RUNNING`、`SUCCEEDED`、`FAILED`、`CANCELLED` |
| `feedbackAction` | `ADOPT`、`REJECT`、`WATCH`、`IGNORE` |
| `riskCheckResult` | `PASS`、`REVIEW`、`REJECT`、`ERROR` |
| `riskAuditLevel` | `LOW`、`MEDIUM`、`HIGH`、`CRITICAL` |

## 7. 视觉组件清单

必须优先建设的组件：

| 组件 | 用途 |
| --- | --- |
| `QualityBadge` | 数据质量、可信等级、报告置信度 |
| `RiskBadge` | 风险等级、风控结果、审计严重度 |
| `StatusPill` | Prompt、订单、回测、数据源状态 |
| `MetricStrip` | 顶部核心指标条 |
| `EvidenceTimeline` | 报告证据、订单事件、风控审计 |
| `ClosedLoopFlow` | 报告到反馈的复盘链路 |
| `PortfolioCurve` | Mock 组合资产和收益曲线 |
| `DrawdownPanel` | 最大回撤、波动、压力情景 |
| `PromptPreviewPane` | Prompt 渲染结果和缺失变量 |
| `AuditDrawer` | 风控详情侧边抽屉 |

图表建议：

- 数据源健康：矩阵图、趋势线。
- 投资报告：收益/动量/热度组合图。
- 产品关系：关系图或权重条。
- Mock 组合：资产曲线、收益曲线、持仓占比。
- 回测：收益、最大回撤、波动指标。
- 复盘：链路时间线。

## 8. 阶段拆分

### 阶段 1：基础骨架和字典

交付：

- API client。
- 字典模块。
- 统一错误处理。
- 页面路由和顶层工作区。
- 基础 UI 状态组件。

验收：

- 所有接口路径集中维护。
- 所有下拉项来自字典。
- 403、400、风控拒绝有统一显示方式。

### 阶段 2：Overview 和数据质量

交付：

- 投资驾驶舱第一屏。
- 数据源健康矩阵。
- 数据质量趋势和缺口列表。
- 首页风险审计流。

验收：

- 用户进入系统即可看到数据是否可信。
- 低质量状态能影响报告和 Mock 入口展示。

### 阶段 3：产品风险和报告工作台

交付：

- 产品画像列表和详情。
- 投资报告列表、生成和详情。
- 报告质量门禁和降级展示。
- 报告图表证据。

验收：

- 数据质量不足时不能进入投资动作。
- 报告详情能解释趋势、质量和风险。

### 阶段 4：Prompt Lab

交付：

- Prompt 版本列表。
- 模板编辑、变量管理、Schema 管理。
- Prompt 预览。
- 状态变更。

验收：

- 前端可配置 Prompt。
- Prompt 预览不触发真实模型调用。
- `ACTIVE`、`RETIRED` 等状态在 UI 中清晰可见。

### 阶段 5：Simulation 模拟交易

交付：

- Mock 组合列表和详情。
- 买入、卖出、撤单、订单事件。
- 再平衡。
- 估值刷新和收益曲线。

验收：

- Mock 交易形成订单、成交、持仓和估值。
- 失败动作能展示风控审计原因。

### 阶段 6：Review Loop 复盘闭环

交付：

- 回测列表和详情。
- 从 Mock 组合生成回测摘要。
- 反馈保存、列表和详情。
- Prompt 评估列表和详情。
- 闭环链路图。

验收：

- 报告、Prompt、Mock 结果、回测、反馈可追踪。
- 用户采纳/拒绝能反哺 Prompt 评估。

## 9. 页面到接口映射

| 页面 | 接口 |
| --- | --- |
| Overview | 数据源列表、报告列表、Mock 组合列表、收益曲线、风控审计列表、回测列表 |
| Data Quality | `/api/admin/data-sources/list`、`/api/admin/data-sources/quality/list` |
| Product & Risk | `/api/products/detail`、`/api/admin/products/investment-profile/save` |
| Report Studio | `/api/investment/analysis/generate`、`/api/investment/analysis/reports/list` |
| Prompt Lab | `/api/ai/prompts/save`、`/api/ai/prompts/list`、`/api/ai/prompts/detail`、`/api/ai/prompts/status`、`/api/ai/prompts/preview` |
| Simulation | `/api/mock/portfolios/create`、`/api/mock/portfolios/mine`、`/api/mock/portfolios/detail`、`/api/mock/portfolios/orders/buy`、`/api/mock/portfolios/orders/sell`、`/api/mock/portfolios/orders/cancel`、`/api/mock/portfolios/rebalance/execute`、`/api/mock/portfolios/performance/curve` |
| Review Loop | `/api/backtests/save`、`/api/backtests/generate-from-portfolio`、`/api/backtests/detail`、`/api/backtests/list`、`/api/ai/feedback/save`、`/api/ai/feedback/list`、`/api/ai/prompt-evaluations/list` |
| Risk Audit | `/api/risk/checks/list` |

## 10. 验收清单

### 10.1 功能验收

- 第一屏不是菜单页，而是可操作投资驾驶舱。
- 数据质量、产品风险、报告、Prompt、Mock 交易、回测、反馈均有入口。
- 下拉项全部来自字典模块。
- 低质量数据不会展示为可执行投资方案。
- Mock 交易失败能看到明确风控原因。
- Prompt 可以版本化、预览、启停和回看评估。
- 用户反馈能进入复盘链路。

### 10.2 UI 验收

- 页面没有大面积空白管理表格。
- 图表、状态流、审计流和矩阵承担主要表达。
- 移动端和桌面端文本不溢出、不重叠。
- 关键按钮有图标和 tooltip。
- 色彩区分状态，但不过度单色化。
- 风险提示和 Mock 标识始终清晰。

### 10.3 技术验收

- API client、DTO、adapter、dictionary、page 分层清晰。
- 页面不直接写接口路径。
- POST JSON 契约统一。
- 业务错误有统一处理。
- 图表组件只消费 ViewModel。
- 每个阶段都能独立运行、独立验收。

## 11. 当前后端支撑状态

当前后端已经支撑本前端重构方案的主要链路：

- 数据源治理：已支持注册、健康、质量快照和列表。
- 产品风险：已支持产品画像、主题关系和 Mock 门禁。
- 投资报告：已支持质量门禁、降级报告、趋势、模拟收益和图表数据。
- Prompt 治理：已支持版本、变量、Schema、状态和预览。
- Mock 交易：已支持组合、买入、卖出、撤单、订单事件、再平衡、估值和收益曲线。
- 风控审计：已支持关键拦截查询。
- 回测反馈：已支持回测保存、Mock 组合生成回测、反馈保存、Prompt 评估和可见性校验。

仍需后续阶段继续推进：

- 真实高质量外部数据源接入。
- 真实 OpenAI 兼容 Provider 调用。
- AI 投资方案结构化输出表。
- 更完整的角色权限和后台运营权限分级。
