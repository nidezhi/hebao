# DZCOM 当前业务线总览

> 2026-06-26 更新：投资数据源主链路已调整为 AI 数据源发现和 Skill 治理。本文中 RSS/Atom、fallback 和手工 endpoint 相关内容仅表示历史实现或可选执行原语，不再作为默认自动闭环方案。

## 1. 文档目的

本文档从业务视角整理当前仓库已经存在的业务实体、接口、应用服务、数据库表、串联方式、数据流转和预期输出。

投资平台后续重构和闭环建设以 `docs_new/11-investment-platform-closed-loop-plan.md` 为准。当前文档描述“现状”，闭环方案描述“目标状态、阶段计划和验收标准”。

阅读时需要区分三种状态：

| 状态 | 含义 |
| --- | --- |
| 已实现 | 已有 Controller、Application Service、Domain、Repository、Mapper XML 和可调用接口 |
| 部分实现 | 已有部分领域能力或数据库结构，但业务链路尚未闭环 |
| 仅有表结构 | Flyway 已创建表，但当前没有完整 Java 业务实现和接口 |

当前真正形成闭环的业务线包括：

1. 账户注册、登录、会话和用户管理。
2. 三层 RBAC 角色权限管理。
3. 产品目录和产品属性管理。
4. 市场行情应用服务和仓储能力。
5. 配置驱动投资定时任务。
6. 投资资讯、主题收益、动量和资讯热度汇总。
7. AI 模型注册和配置管理。
8. AI Prompt 模板版本、变量、输出 Schema 和本地预览管理。
9. 可插拔投资分析报告。

投资组合已经具备模拟组合创建、列表、详情、买入、卖出、撤单边界、订单事件、报告转买入、目标权重再平衡、估值刷新和收益曲线能力；风控审计已具备关键 Mock 拦截记录和查询接口；回测结果、AI 方案反馈和 Prompt 评估已具备保存、查询和复盘接口。

## 2. 总体架构串联

```text
前端或调用方
  -> interfaces/controller
  -> interfaces/request DTO
  -> application/service
  -> domain/model + domain/repository
  -> infrastructure/persistence/repository/*Impl
  -> infrastructure/persistence/mapper
  -> resources/mapper/*.xml
  -> MySQL
```

异步投资任务链路：

```text
数据库任务配置
  -> 动态 Cron 调度器
  -> Kafka 任务事件
  -> Kafka 消费者
  -> 任务执行编排服务
  -> 独立 InvestmentTaskHandler
  -> 新闻、行情快照、执行记录
  -> 投资分析 Provider
  -> 投资分析报告
```

## 3. 账户与身份业务线

### 3.1 业务状态

已实现。

### 3.2 核心业务实体

| 领域对象 | 对应表 | 业务职责 |
| --- | --- | --- |
| `User` | `aiw_user` | 用户主账户、用户编号、状态、登录时间和逻辑删除 |
| `LoginIdentity` | `aiw_login_identity` | 用户名、邮箱、手机号等登录标识 |
| `UserCredential` | `aiw_user_credential` | 密码摘要、失败次数、锁定和过期信息 |
| `UserProfile` | `aiw_user_profile` | 昵称、头像、语言、时区和展示资料 |
| `UserRiskProfile` | `aiw_user_risk_profile` | KYC 状态、风险等级和测评信息 |
| `UserPreference` | `aiw_user_preference` | 用户个性化配置和通知偏好 |

### 3.3 主要应用服务

| 应用服务 | 作用 |
| --- | --- |
| `AccountRegistrationService` | 注册用户并协调用户、身份、凭据、资料、风险画像和初始角色 |
| `AuthenticationApplicationService` | 登录校验、凭据锁定、成功登录记录和会话创建 |
| `UserApplicationService` | 管理端用户创建、更新、状态、KYC、风险等级和删除 |
| `UserQueryService` | 用户详情和分页查询 |
| `UserPreferenceApplicationService` | 用户偏好查询、保存和删除 |
| `SessionService` | Redis 会话创建、查询和撤销 |

### 3.4 注册数据流

```text
POST /api/auth/register
  -> RegisterRequest
  -> AccountRegistrationService
  -> 标准化用户名、邮箱和手机号
  -> 校验登录标识唯一性
  -> 生成密码摘要
  -> 保存 User
  -> 保存 LoginIdentity
  -> 保存 UserCredential
  -> 保存 UserProfile
  -> 保存 UserRiskProfile
  -> 分配默认角色
  -> 返回 UserResponse
```

预期输出：

- 用户业务 ID 和用户编号。
- 账户状态。
- 登录身份。
- 用户资料、KYC 和风险信息。
- 初始角色和权限。

### 3.5 登录数据流

```text
POST /api/auth/login
  -> LoginRequest
  -> AuthenticationApplicationService
  -> 识别登录标识类型
  -> 查询 LoginIdentity
  -> 查询 UserCredential
  -> 校验账户状态和临时锁定
  -> 校验密码摘要
  -> 失败：增加失败次数，必要时锁定
  -> 成功：清零失败次数，更新最近登录时间
  -> 创建 Redis 会话
  -> 返回会话和用户信息
```

### 3.6 账户接口

主要 Controller：

- `AuthenticationController`
- `CurrentUserController`
- `AdminUserController`

主要接口能力：

- 注册。
- 登录。
- 退出。
- 当前用户资料查询和更新。
- 修改密码。
- 用户偏好查询、设置和删除。
- 管理端用户列表、详情、创建、更新、状态变更、KYC、风险等级和逻辑删除。

## 4. 角色权限业务线

### 4.1 业务状态

已实现。

### 4.2 三层 RBAC 实体

| 领域对象 | 对应表 | 业务职责 |
| --- | --- | --- |
| `Role` | `aiw_role` | 定义角色编码、名称、状态和系统角色标记 |
| `UserRole` | `aiw_user_role` | 给用户分配角色，并保存生效时间范围 |
| `RolePermission` | `aiw_role_permission` | 将稳定权限编码映射到角色 |

### 4.3 串联方式

```text
用户会话
  -> 当前用户业务 ID
  -> 查询有效 UserRole
  -> 查询有效 Role
  -> 查询 RolePermission
  -> 汇总稳定 PermissionCodes
  -> Controller 或应用服务执行权限校验
```

### 4.4 关键规则

- ADMIN 角色不能隐式绕过权限校验。
- 权限判断必须使用 `PermissionCodes` 中的稳定编码。
- 修改角色权限或用户角色后必须撤销受影响用户会话。
- 不允许删除最后一个有效角色管理员。

### 4.5 预期输出

- 角色列表和权限集合。
- 用户当前角色集合。
- 用户最终有效权限集合。
- 角色变更后的会话失效结果。

## 5. 产品目录业务线

### 5.1 业务状态

已实现。

### 5.2 核心实体

| 领域对象 | 对应表 | 业务职责 |
| --- | --- | --- |
| `Product` | `aiw_product` | 产品代码、名称、类型、风险等级、交易状态和生命周期 |
| `ProductAttribute` | `aiw_product_attribute` | 产品可扩展属性、值类型和属性值 |
| `ProductInvestmentProfile` | `aiw_product_investment_profile` | 产品资产类别、风险摘要、波动等级、流动性等级、最大回撤、适配风险等级和 Mock 交易开关 |
| `ProductThemeRelation` | `aiw_product_theme_relation` | 产品与主题、行业、指数、资产类别的显式关系，用于解释资讯命中、投资报告和后续模拟交易 |

支持的产品类型由 `ProductType` 定义，交易状态由 `ProductTradeStatus` 定义。

### 5.3 数据流

```text
管理端创建或更新产品
  -> Product Request DTO
  -> ProductApplicationService
  -> 校验产品编码、风险等级、日期和状态
  -> 创建或更新 Product
  -> 保存 ProductAttribute 集合
  -> ProductStoreImpl / ProductAttributeStoreImpl
  -> MyBatis Mapper XML
  -> 返回 ProductResponse

管理端保存产品投资画像
  -> SaveProductInvestmentProfileRequest
  -> ProductApplicationService.saveInvestmentProfile
  -> 校验产品存在、资产类别、波动等级、流动性等级、风险等级和质量分
  -> 保存 ProductInvestmentProfile
  -> 删除并重建 ProductThemeRelation 集合
  -> 返回带 investmentProfile 和 themeRelations 的 ProductResponse

前端查询产品详情
  -> ProductQueryService.detail
  -> 查询 Product、ProductAttribute、ProductInvestmentProfile、ProductThemeRelation
  -> ProductViewAssembler.assembleInvestmentDetail
  -> ProductResponse
```

### 5.4 预期输出

- 产品基础信息。
- 产品风险等级和交易状态。
- 上市、退市日期。
- 产品扩展属性集合。
- 产品分页列表和详情。
- 产品投资画像 `investmentProfile`，包括资产类别、风险摘要、波动等级、流动性等级、最大回撤、适配风险等级和 Mock 交易开关。
- 产品主题关系 `themeRelations`，包括主题、行业、指数和资产类别关系。
- 前端可根据 `mockTradable` 决定是否展示 Mock 交易入口。
- 前端可根据 `dataQualityScore` 展示产品画像质量，低质量产品只能进入观察和补数流程。

### 5.5 前端接口

| 接口 | 说明 |
| --- | --- |
| `POST /api/admin/products/create` | 创建产品基础信息 |
| `POST /api/admin/products/update` | 更新产品基础信息 |
| `POST /api/admin/products/attributes/save` | 新增或覆盖产品扩展属性 |
| `POST /api/admin/products/investment-profile/save` | 保存产品投资画像和主题关系 |
| `POST /api/products/detail` | 查询产品详情，返回基础信息、扩展属性、投资画像和主题关系 |

### 5.6 阶段 1 投资闭环位置

产品池和风险架构是投资分析、Prompt 版本、Mock 交易和风控审计的前置条件。任何投资方案如果无法说明使用了哪些产品、产品风险是什么、是否允许 Mock 交易、和哪些主题或指数有关，都不能进入正式投资建议展示。

## 6. 模拟组合业务线

### 6.1 业务状态

已实现第三阶段：当前用户可以创建模拟组合、查询我的模拟组合列表、查询模拟组合详情，并可以按最新行情执行模拟金额买入、数量卖出、撤单边界校验和目标权重再平衡。买入、卖出和再平衡会生成模拟订单、模拟成交、当前持仓、新估值快照和前端可查订单事件。

### 6.2 核心实体

| 领域对象 | 对应表 | 业务职责 |
| --- | --- | --- |
| `Portfolio` | `aiw_portfolio` | 模拟组合主数据，当前使用 `portfolioType=SIMULATION` |
| `PortfolioValuation` | `aiw_portfolio_valuation` | 组合估值快照，创建组合时写入初始现金快照 |
| `Position` | `aiw_position` | 当前持仓聚合数据，模拟买入和卖出成交后写入或更新 |
| `MockOrder` | `aiw_order` | 模拟订单，当前支持 `BUY`、`SELL` 和 `CANCELLED` 边界 |
| `TradeExecution` | `aiw_trade_execution` | 模拟成交，当前买入和卖出订单立即按最新收盘价成交 |
| `OrderEvent` | `aiw_order_event` | 订单追加式状态事件，用于前端查询订单状态追踪和交易审计 |

### 6.3 数据流

```text
前端创建模拟组合
  -> CreateMockPortfolioRequest
  -> MockPortfolioApplicationService.create
  -> 校验当前用户、组合名称、币种、初始现金
  -> 保存 Portfolio(SIMULATION)
  -> 保存 PortfolioValuation(MOCK_INITIAL_CASH)
  -> 返回 MockPortfolioResponse

前端查询我的模拟组合
  -> MockPortfolioListRequest
  -> MockPortfolioApplicationService.listMine
  -> 按当前用户和 SIMULATION 类型分页查询 Portfolio
  -> 查询每个组合最新 PortfolioValuation
  -> 返回 PageResponse<MockPortfolioResponse>

前端查询模拟组合详情
  -> MockPortfolioDetailRequest
  -> MockPortfolioApplicationService.detail
  -> 校验组合属于当前用户
  -> 查询最新估值和当前持仓
  -> 返回 MockPortfolioResponse

前端执行模拟买入
  -> ExecuteMockBuyRequest
  -> MockPortfolioApplicationService.buy
  -> 校验组合属于当前用户、产品可交易、产品画像允许 Mock、最新行情存在、现金足够
  -> 保存 MockOrder(FILLED)
  -> 保存 TradeExecution
  -> 保存 OrderEvent(FILLED)
  -> 更新 Position
  -> 保存 PortfolioValuation(MOCK_BUY_FILLED)
  -> 返回 MockOrderExecutionResponse

前端执行模拟卖出
  -> ExecuteMockSellRequest
  -> MockPortfolioApplicationService.sell
  -> 校验组合属于当前用户、产品可交易、产品画像允许 Mock、最新行情存在、可用持仓足够
  -> 保存 MockOrder(SELL/FILLED)
  -> 保存 TradeExecution
  -> 保存 OrderEvent(FILLED)
  -> 按平均成本法扣减 Position
  -> 保存 PortfolioValuation(MOCK_SELL_FILLED)
  -> 返回 MockOrderExecutionResponse

前端撤销模拟订单
  -> CancelMockOrderRequest
  -> MockPortfolioApplicationService.cancelOrder
  -> 校验订单属于当前用户
  -> 如果订单为 FILLED、CANCELLED、REJECTED、FAILED 终态则拒绝
  -> 非终态订单保存为 CANCELLED
  -> 保存 OrderEvent(CANCELLED)
  -> 返回 MockOrderResponse

前端查询模拟订单事件
  -> MockOrderEventsRequest
  -> MockPortfolioApplicationService.orderEvents
  -> 校验订单属于当前用户
  -> 查询 aiw_order_event
  -> 返回 List<OrderEventResponse>

前端根据投资分析报告执行模拟买入
  -> ExecuteMockPlanFromReportRequest
  -> MockPortfolioApplicationService.buyFromReport
  -> 查询 InvestmentAnalysisReport
  -> 校验报告成功、可信等级、数据质量门禁和投资方案类型
  -> 读取 investmentPlan.referenceAllocationAmount
  -> 使用请求 productBizId 或按 report.themeCode 反查 ProductThemeRelation
  -> 复用模拟买入链路生成订单、成交、持仓和估值

前端刷新模拟组合估值
  -> MockPortfolioDetailRequest
  -> MockPortfolioApplicationService.refreshValuation
  -> 查询当前持仓
  -> 按每个持仓产品最新 1D 行情计算持仓市值
  -> 保存 PortfolioValuation(MOCK_MARK_TO_MARKET)
  -> 返回刷新后的 MockPortfolioResponse

前端查询收益曲线
  -> MockPortfolioPerformanceRequest
  -> MockPortfolioApplicationService.performance
  -> 查询 PortfolioValuation 历史
  -> 计算 latestReturnRate 和 maxDrawdown
  -> 返回 MockPortfolioPerformanceResponse

前端执行模拟再平衡
  -> ExecuteMockRebalanceRequest
  -> MockPortfolioApplicationService.rebalance
  -> 校验目标权重不重复、每项在 0 到 1 之间、总和不超过 1
  -> 当前持仓未出现在目标集合时按目标权重 0 处理
  -> 先卖出超配持仓，再买入低配产品
  -> 返回 MockRebalanceExecutionResponse
```

### 6.4 前端接口

| 接口 | 说明 |
| --- | --- |
| `POST /api/mock/portfolios/create` | 创建当前用户模拟组合，并写入初始现金估值 |
| `POST /api/mock/portfolios/mine` | 分页查询当前用户模拟组合列表 |
| `POST /api/mock/portfolios/detail` | 查询模拟组合详情、最新估值和当前持仓 |
| `POST /api/mock/portfolios/orders/buy` | 执行模拟金额买入，返回订单、成交和更新后的组合 |
| `POST /api/mock/portfolios/orders/sell` | 执行模拟数量卖出，返回订单、成交和更新后的组合 |
| `POST /api/mock/portfolios/orders/cancel` | 撤销非终态模拟订单；即时成交订单会明确拒绝 |
| `POST /api/mock/portfolios/orders/events` | 查询当前用户订单生命周期事件和交易审计 |
| `POST /api/mock/portfolios/orders/buy-from-report` | 根据投资分析报告自动生成模拟买入 |
| `POST /api/mock/portfolios/rebalance/execute` | 按目标权重执行模拟再平衡，返回调仓订单和最终组合 |
| `POST /api/mock/portfolios/valuations/refresh` | 按最新行情刷新模拟组合估值 |
| `POST /api/mock/portfolios/performance/curve` | 查询模拟组合收益曲线和最大回撤 |

### 6.5 当前边界

- 不触发真实交易。
- 当前支持即时成交买入、即时成交卖出、非终态撤单边界和目标权重再平衡；不支持部分成交、排队成交和真实渠道撤单。
- 当前成交价取产品最新 `1D` 行情收盘价。
- 当前必须依赖产品投资画像 `mockTradable=true` 和 `dataQualityScore >= 0.45`。
- 当前能力已覆盖“手动 Mock 买入/卖出 -> 订单 -> 成交 -> 持仓 -> 估值 -> 订单事件审计”、“投资分析报告 -> 自动 Mock 买入”、“目标权重 -> 再平衡订单”和“估值历史 -> 收益曲线/最大回撤”，下一步需要接风控规则命中审计和回测。

## 7. 市场行情业务线

### 7.1 业务状态

部分实现。基础行情保存、最新行情、历史行情和主题收益查询已经具备
Application Service、Domain、Repository 和 Mapper XML，但当前没有独立市场行情
Controller，因此尚未形成可直接供前端调用的完整接口闭环。

### 7.2 核心实体

| 领域对象 | 对应表 | 业务职责 |
| --- | --- | --- |
| `MarketQuote` | `aiw_market_quote` | 保存产品价格、成交量、行情周期、来源和行情时间 |
| `ThemeProductPerformance` | 查询结果 | 表示产品在指定窗口内的起止价格和收益率 |

### 7.3 行情写入流

```text
内部调用或后续市场行情 Controller
  -> SaveMarketQuoteCommand
  -> MarketQuoteApplicationService
  -> 校验产品、价格、周期和来源
  -> MarketQuoteStore.savePoint
  -> aiw_market_quote
  -> 返回 MarketQuoteView
```

### 7.4 行情查询流

```text
产品业务 ID + 行情周期 + 来源
  -> 查询最新行情或历史区间
  -> Mapper XML 按行情时间排序
  -> 返回行情点集合
```

### 7.5 主题收益计算

```text
任务配置中的产品代码集合
  -> MarketQuoteStore.findPerformance
  -> 查询每个产品窗口起点和终点价格
  -> 计算 returnRate
  -> HotThemeReturnTaskHandler / MarketMomentumTaskHandler
```

预期输出：

- 最新行情。
- 历史行情序列。
- 产品窗口收益率。
- 主题平均收益和动量快照。

## 8. 配置驱动定时任务业务线

### 8.1 业务状态

已实现。

### 8.2 核心实体

| 领域对象 | 对应表 | 业务职责 |
| --- | --- | --- |
| `InvestmentTaskDefinition` | `aiw_investment_task_definition` | 保存任务编码、类型、Cron、时区、开关和参数 |
| `ScheduledTaskExecution` | `aiw_scheduled_task_execution` | 保存事件幂等键、执行状态、结果和失败原因 |
| `InvestmentTaskEvent` | Kafka 消息 | 在调度端和执行端之间传递任务信息 |

### 8.3 启动和调度流

```text
application-local/dev.yaml
  -> InvestmentTaskProperties
  -> InvestmentTaskDefinitionInitializer
  -> 初始化缺失或系统默认任务
  -> aiw_investment_task_definition
  -> ConfigurableInvestmentTaskScheduler
  -> 为 enabled=true 的任务注册 CronTrigger
```

数据库是运行时配置来源。接口保存任务后，当前节点立即取消旧调度并重新注册。

### 8.4 触发和执行流

```text
Cron 到点或手动触发
  -> InvestmentTaskManagementService.trigger
  -> 合并数据库默认参数和本次覆盖参数
  -> 生成 eventId
  -> Kafka topic
  -> InvestmentTaskEventConsumer
  -> InvestmentTaskExecutionService
  -> 根据 eventId 检查幂等
  -> 保存 RUNNING
  -> 匹配唯一 InvestmentTaskHandler
  -> 成功保存 SUCCEEDED 和结果摘要
  -> 失败保存 FAILED 和失败摘要，并记录完整堆栈
```

### 8.5 预期输出

- 可查询和可修改的任务配置。
- 手动触发事件 ID。
- 任务执行状态和失败原因。
- 任务产生的资讯和主题快照。

## 9. 投资资讯采集业务线

### 9.1 业务状态

已实现 RSS/Atom 采集、数据源独立容错、幂等保存和兜底资讯；本轮新增数据源治理后台接口，前端可展示数据源注册、健康状态、质量快照、质量等级和失败原因。

当前资讯采集仍是轻量级接入，目标不是直接替代专业金融数据供应商，而是先建立稳定的数据入库、质量评分和主题关联链路。后续接入财联社、Wind、同花顺 iFinD、东方财富、巨潮资讯、交易所公告或供应商 API 时，只需要扩展数据源客户端，后续热度统计、投资分析和图表接口可以复用。

数据源治理接口：

| 接口 | 说明 |
| --- | --- |
| `POST /api/admin/data-sources/save` | 保存数据源注册信息、来源等级、启用状态和负责人 |
| `POST /api/admin/data-sources/health/save` | 保存最近成功、失败、成功率、延迟和失败原因 |
| `POST /api/admin/data-sources/quality/save` | 保存数据质量快照 |
| `POST /api/admin/data-sources/list` | 查询数据源看板，返回健康状态、最新质量和前端提示文案 |
| `POST /api/admin/data-sources/quality/list` | 查询质量快照历史，用于趋势图 |

### 9.2 核心实体

| 领域对象 | 对应表 | 业务职责 |
| --- | --- | --- |
| `NewsArticle` | `aiw_news_article` | 保存新闻、公告或研报文本和来源信息 |
| `NewsArticleRelation` | `aiw_news_article_relation` | 保存新闻与主题、产品代码、命中关键词和关联分 |

### 9.3 数据来源分层

优质数据来源按可信度和投资可用性分层：

| 层级 | 数据来源类型 | 建议来源 | 适用输出 | 当前接入状态 |
| --- | --- | --- | --- | --- |
| 一级 | 监管和交易所公告 | 证监会、上交所、深交所、北交所、巨潮资讯 | 政策变化、上市公司公告、行业监管信号 | 预留来源编码，待接入 API 或可解析源 |
| 二级 | 主流财经媒体 | 东方财富、证券时报、中国证券报、新华社财经 | 新闻热度、主题情绪、事件驱动 | 可通过 RSS/Atom 或后续 API 接入 |
| 三级 | 专业行情和研报供应商 | Wind、同花顺 iFinD、财联社、Choice | 机构研报、行业景气、资金流和专业标签 | 待采购或配置供应商接口 |
| 四级 | 项目兜底资讯 | `fallbackArticles` | 本地联调、断源兜底、演示数据 | 已实现，但质量分较低 |

当前代码中 `NewsHeatAggregationTaskHandler` 内置来源质量评分：

| 来源类型 | 质量分 |
| --- | --- |
| 监管、交易所、主流可信来源 | `1.00` |
| 普通 RSS/Atom 来源 | `0.70` |
| 兜底资讯 | `0.35` |

这意味着兜底资讯可以保证流程可跑通，但不会在热度评分中与真实可靠来源等权。

### 9.4 采集流

```text
INVESTMENT_NEWS_COLLECTION
  -> InvestmentNewsCollectionTaskHandler
  -> 解析 feedUrls、sourceCode、languageCode、maxItems
  -> 每个 feed 独立调用 RssInvestmentNewsFeedClient
  -> RSS/Atom XML 安全解析
  -> 外部发布时间转换为北京时间
  -> sourceCode + externalId 幂等保存
```

### 9.5 兜底流

```text
全部外部 feed 无有效数据
  -> 解析 fallbackArticles
  -> 格式：主题|标题|摘要;主题2|标题2|摘要2
  -> 构造稳定 externalId
  -> 保存中国大陆主题资讯
```

兜底资讯的作用是保证：

- 新闻列表接口有可查询数据。
- 资讯热度任务有关键词样本。
- 投资分析报告有近期新闻上下文。

### 9.6 新闻与主题、产品显式关联

资讯热度汇总任务会根据主题关键词将新闻与主题、产品代码建立显式关联：

```text
NEWS_HEAT_AGGREGATION
  -> 根据 themes 读取主题关键词
  -> 查询窗口内命中新闻
  -> 根据 themeProducts 生成产品代码关联
  -> 计算 matchedKeywords、sourceQualityScore、relationScore
  -> 保存 aiw_news_article_relation
  -> 保存 NEWS_HEAT 快照
```

关联表核心字段：

| 字段 | 说明 |
| --- | --- |
| `article_biz_id` | 关联的新闻业务 ID |
| `theme_code` / `theme_name` | 关联主题 |
| `product_code` | 关联产品代码；空字符串表示主题级关联 |
| `matched_keywords` | 命中的关键词集合 |
| `source_quality_score` | 来源质量分 |
| `relation_score` | 关键词命中、来源质量和时效综合得分 |
| `evidence` | 关联证据摘要，当前保存新闻标题 |

前端查询接口：

```text
POST /api/investment/tasks/article-relations/list
```

该接口按资讯业务 ID、主题编码、产品代码和关联类型分页查询关联记录。前端可展示命中关键词、来源质量分、综合关联分和证据摘要，用于解释资讯热度来源。

### 9.7 预期输出

- 新闻标题、摘要、正文、来源和原文地址。
- 发布时间和采集时间。
- 可按关键字、类型、来源、语言和时间查询的资讯分页。
- 新闻、主题和产品代码的显式关联数据。
- 来源质量和关联分，供热度统计和 AI 分析解释使用。

## 10. 投资主题汇总业务线

### 10.1 业务状态

已实现，默认只处理中国大陆市场 `CN_MAINLAND`。

### 10.2 核心实体

| 领域对象 | 对应表 | 业务职责 |
| --- | --- | --- |
| `InvestmentThemeSnapshot` | `aiw_investment_theme_snapshot` | 保存主题收益、动量、热度和解释指标 |

### 10.3 热门主题收益

处理器：

```text
HotThemeReturnTaskHandler
```

数据流：

```text
themes 中的产品代码
  -> 查询产品窗口起止行情
  -> 计算每个产品 returnRate
  -> 过滤无有效收益样本
  -> 计算主题有效样本平均收益率
  -> 计算覆盖率、波动、头部贡献和质量分
  -> 选择收益最高产品
  -> 保存 RETURN 快照
```

输出质量评估：

| 指标 | 含义 |
| --- | --- |
| `requestedProductCount` | 配置中要求统计的产品数量 |
| `validReturnCount` | 实际有起止行情并能计算收益的产品数量 |
| `coverageRate` | 有效样本覆盖率，越高越可信 |
| `volatility` | 主题内部产品收益平均绝对偏离 |
| `topContribution` | 头部产品对收益解释的集中度 |
| `qualityScore` | 覆盖率、样本数和波动稳定性综合评分 |
| `qualityLevel` | `HIGH` / `MEDIUM` / `LOW` |

### 10.4 市场动量

处理器：

```text
MarketMomentumTaskHandler
```

当前公式：

```text
平均收益率 = 有效产品收益率之和 / 有效产品数量
上涨广度 = 正收益产品数量 / 有效产品数量
质量分 = 覆盖率 * 0.55 + 样本分 * 0.25 + 稳定性分 * 0.20
动量分数 = 平均收益率 * 上涨广度 * 质量分
```

输出：

- `returnRate`
- `momentumScore`
- `topProductBizId`
- `metrics.positiveBreadth`
- `metrics.coverageRate`
- `metrics.volatility`
- `metrics.qualityScore`
- `metrics.qualityLevel`
- 产品收益样本明细

### 10.5 资讯热度

处理器：

```text
NewsHeatAggregationTaskHandler
```

数据流：

```text
主题关键词
  -> 查询回看窗口内新闻标题和摘要
  -> 识别命中关键词
  -> 计算来源质量分
  -> 计算新闻时效分
  -> relationScore = 关键词命中数 * 来源质量分 * 时效分
  -> heatScore = relationScore 汇总
  -> 保存新闻、主题、产品代码显式关联
  -> 保存 NEWS_HEAT 快照
```

输出质量评估：

| 指标 | 含义 |
| --- | --- |
| `articleCount` | 命中的新闻数量 |
| `uniqueSourceCount` | 去重来源数量 |
| `averageSourceQuality` | 平均来源质量 |
| `dataQualityScore` | 样本数、来源多样性和来源质量综合评分 |
| `qualityLevel` | `HIGH` / `MEDIUM` / `LOW` |
| `sampleArticles` | 最多十条样本新闻，便于前端解释热度来源 |

### 10.6 预期输出

- 主题收益榜。
- 主题动量榜。
- 主题资讯热度榜。
- 按时间绘制的收益、动量和热度趋势。
- 可供投资分析 Provider 使用的结构化样本。
- 可解释的统计质量分，避免把低质量样本误判为投资机会。

## 11. AI 模型管理业务线

### 11.1 业务状态

已实现模型注册、更新、查询、状态变更和归档。

### 11.2 核心实体

| 领域对象 | 对应表 | 业务职责 |
| --- | --- | --- |
| `AiModel` | `aiw_ai_model` | 保存模型编码、版本、提供方、配置、指标和生命周期 |

### 11.3 模型生命周期

```text
DRAFT
  -> VALIDATING
  -> ACTIVE
  -> INACTIVE
  -> ARCHIVED
```

状态说明：

| 状态 | 说明 |
| --- | --- |
| `DRAFT` | 草稿配置，尚未验证 |
| `VALIDATING` | 正在进行离线验证或接入验证 |
| `ACTIVE` | 可供业务选择使用 |
| `INACTIVE` | 暂停使用但保留配置 |
| `ARCHIVED` | 逻辑删除，不再参与新业务 |

### 11.4 ACTIVE 模型与 Provider 强关联

投资分析生成时，实际 Provider 不以接口传参为准，而是以 `aiw_ai_model` 中最近启用的 `ACTIVE` 模型为准：

```text
GenerateInvestmentAnalysisRequest.modelCode
  -> AiModelStore.findActiveByCode
  -> 读取 ACTIVE AiModel.provider
  -> 校验请求 providerCode 是否与模型 provider 一致
  -> AiModelRuntimeConfigResolver 注入 secretRef 对应 API Key
  -> providers.stream().filter(candidate.supports(model.provider()))
  -> InvestmentAnalysisProvider.analyze
```

这条规则解决两个问题：

- 业务侧只能选择已经激活的模型配置，不能绕过模型生命周期直接选择 Provider。
- `providerCode` 只做一致性校验，防止前端传错 Provider 后调用到错误的大模型实现。

### 11.5 数据流

```text
POST /api/ai/models/save
  -> SaveAiModelRequest
  -> AiModelApplicationService
  -> 按 modelCode + modelVersion 查询现有版本
  -> 校验状态
  -> 维护 activatedAt / retiredAt
  -> AiModelStoreImpl
  -> aiw_ai_model
  -> 返回 AiModelResponse
```

模型调用配置采用普通参数和密钥分离：

```text
aiw_ai_model.model_config
  -> baseUrl、model、secretRef、timeoutSeconds、temperature、mockEnabled

application-local/dev.yaml 或部署平台 Secret
  -> ai.secrets.values[secretRef]
  -> 实际 API Key
```

当前初始化了本地规则、OpenAI、DeepSeek 和通义千问兼容配置。后三者使用
UUID 格式模拟 API Key，并开启 `mockEnabled=true`，用于验证配置注入链路。

### 11.6 预期输出

- 模型版本清单。
- 提供方和制品地址。
- 脱敏模型配置。
- 离线评估指标。
- 模型当前生命周期状态。

## 12. 投资分析报告业务线

### 12.1 业务状态

已实现可插拔 Provider 架构和默认 `LOCAL_RULE` 实现。

### 12.2 核心实体

| 领域对象 | 对应表 | 业务职责 |
| --- | --- | --- |
| `InvestmentAnalysisReport` | `aiw_investment_analysis_report` | 保存完整分析输入、输出、模型和时间快照 |

### 12.3 Provider 串联

```text
GenerateInvestmentAnalysisRequest
  -> InvestmentAnalysisApplicationService
  -> 根据 modelCode 查询最近启用的 ACTIVE AiModel
  -> 解析 model_config.secretRef
  -> 从外部密钥配置注入 API Key
  -> 根据 AiModel.provider 查找 InvestmentAnalysisProvider
  -> provider.analyze(requestId, command, runtimeConfig)
  -> 保存 InvestmentAnalysisReport
```

新增真实大模型时只需要实现新的 `InvestmentAnalysisProvider`，Controller 和报告表结构保持稳定。

### 12.4 默认本地规则分析

`LocalRuleInvestmentAnalysisProvider` 的数据输入：

- 指定市场和主题的主题快照。
- 回看窗口内的近期新闻。
- 模拟收益初始资金。

公共计算：

- 平均收益率。
- 平均动量。
- 加权资讯热度。
- 快照质量分。
- 最近主题快照。
- 新闻数量。
- 参考配置比例。
- 模拟收益、压力情景和乐观情景。

### 12.5 报告输出

#### 报告质量门禁

报告现在有三个前端可见的一等字段：

| 字段 | 说明 |
| --- | --- |
| `confidenceLevel` | 报告可信等级：`HIGH_CONFIDENCE`、`MEDIUM_CONFIDENCE`、`LOW_CONFIDENCE`、`UNUSABLE` |
| `dataQualityScore` | 报告输入数据质量分，0-1 |
| `dataQualityGate` | 数据质量门禁 JSON，包含是否通过、降级原因、提示文案和允许动作 |

当 `dataQualityGate.passed=false` 时，系统只输出数据缺口报告，不生成积极配置建议，不开放 Mock 交易动作。

#### 投资信息汇总

- 市场范围。
- 主题编码。
- 快照样本数量。
- 相关新闻数量。
- 平均收益率。
- 平均动量。
- 加权资讯热度。
- 数据质量分和质量等级。
- 最近快照时间。
- 近期新闻标题和摘要。

#### 趋势

- `UP` 或 `DOWN`。
- `NEUTRAL`。
- 平均收益率。
- 平均动量。
- 新闻热度。
- 加权热度分。
- 数据质量分。
- 回看天数。

#### 投资方案

- 方案类型。
- 建议动作。
- 参考配置比例。
- 参考配置金额。
- 再平衡规则。
- 数据质量等级。
- 风险提示。

投资方案仅用于辅助决策，不构成收益承诺，不自动执行交易。

#### 模拟收益

- 初始资金。
- 参考配置比例。
- 实际参与模拟的配置本金。
- 模拟收益金额。
- 模拟期末资金。
- 模拟收益率。
- 压力情景收益。
- 乐观情景收益。
- 模拟假设说明。

#### 图表数据

- 收益、动量和热度时间序列。
- 新闻事件时间点。
- 主题和来源信息。

## 13. 投资决策质量设计

这一节是后续优化投资分析输出时必须优先关注的三类输入。

### 13.1 数据来源

当前系统将数据源拆成三类输入：

| 输入 | 当前状态 | 后续增强方向 |
| --- | --- | --- |
| 新闻资讯 | RSS/Atom + 兜底资讯，已入库并支持主题关联 | 接入监管、交易所、巨潮、财联社、东方财富等稳定来源 |
| 行情数据 | 已有行情表、保存和查询服务，但缺少独立前端 Controller | 接入实时行情、分钟线、日线和复权收益 |
| 产品主数据 | 已有产品目录和产品属性 | 建立主题、产品、行业、指数成分的显式映射 |

优质数据的判断标准：

- 来源稳定：监管、交易所、官方公告和专业数据商优先。
- 内容可追溯：必须保留 `sourceCode`、`sourceUrl`、`externalId`。
- 时效明确：发布时间统一转换为北京时间。
- 可解释：新闻必须能追溯到主题、产品代码、命中关键词和证据摘要。
- 可降级：外部源失败时允许兜底，但兜底来源质量分低于真实来源。

### 13.2 输出质量评估

投资任务输出不再只看“有没有数据”，而是同时输出质量字段：

| 输出类型 | 核心质量字段 | 质量含义 |
| --- | --- | --- |
| 收益快照 | `coverageRate`、`volatility`、`topContribution`、`qualityScore` | 判断样本覆盖是否充足、主题是否被单一产品拉动 |
| 动量快照 | `positiveBreadth`、`coverageRate`、`volatility`、`qualityScore` | 判断上涨是否有广度，避免单点行情误导 |
| 资讯热度 | `uniqueSourceCount`、`averageSourceQuality`、`dataQualityScore` | 判断新闻热度是否来自多源、可信、近期资讯 |
| 分析报告 | `dataQualityScore`、`dataQualityLevel` | 决定建议仓位和是否提示补充数据源 |

质量等级规则：

| 等级 | 解释 | 投资分析动作 |
| --- | --- | --- |
| `HIGH` | 样本覆盖、来源质量和一致性较好 | 可以输出正常参考仓位 |
| `MEDIUM` | 数据可用但存在覆盖或来源不足 | 轻仓跟踪，等待趋势确认 |
| `LOW` | 数据不足或主要依赖兜底资讯 | 仅观察，不应生成激进配置建议 |

### 13.3 投资模拟方案

当前投资模拟方案是“主题级辅助决策”，不是自动交易策略。

模拟输入：

| 输入 | 来源 |
| --- | --- |
| 初始资金 | `GenerateInvestmentAnalysisRequest.initialCapital` |
| 平均收益率 | `RETURN` 和 `MOMENTUM` 快照中的有效收益样本 |
| 平均动量 | 动量快照 |
| 数据质量分 | 各类快照 metrics 中的质量字段 |
| 新闻热度 | `NEWS_HEAT` 快照和近期新闻 |

参考配置比例规则：

| 条件 | 参考配置比例 |
| --- | --- |
| 数据质量低于 `0.45` | `10%` 观察仓位 |
| 平均收益和平均动量均为正 | `30%` 分批配置 |
| 平均收益非负但动量不足 | `20%` 轻仓跟踪 |
| 平均收益为负 | `5%` 防守观察 |

模拟输出：

| 字段 | 说明 |
| --- | --- |
| `initialCapital` | 初始资金 |
| `allocationRate` | 参考配置比例 |
| `simulatedPrincipal` | 实际参与模拟的资金 |
| `estimatedProfit` | 基准情景模拟收益 |
| `estimatedFinalCapital` | 初始资金 + 模拟收益 |
| `stressLoss` | 压力情景，按平均收益率下调 5% 模拟 |
| `optimisticProfit` | 乐观情景，按平均收益率上调 3% 模拟 |
| `assumption` | 模拟假设，明确不代表未来收益 |

### 13.4 后续闭环目标

后续要把这条链路从“主题辅助分析”升级为“用户组合级投资建议”：

```text
用户风险画像
  -> 真实投资组合和持仓
  -> 产品风险等级和流动性
  -> 新闻、行情和主题快照
  -> AI 信号
  -> 推荐方案
  -> 回测验证
  -> 风控审计
  -> 用户确认或拒绝
```

## 14. 当前仅有表结构或尚未闭环的业务线

### 14.1 完整组合交易闭环

状态：模拟组合创建、列表、详情、买入订单、卖出订单、撤单边界、订单事件审计、再平衡、成交、持仓变化、估值刷新和收益曲线已经在第 6 章落地；风控规则命中审计和回测仍未形成完整 Java 闭环。

目标实体：

- 投资组合。
- 持仓。
- 组合估值。
- 组合收益。

预计串联：

```text
用户
  -> 投资组合
  -> 持仓
  -> 市场行情
  -> 组合估值和收益
  -> AI 分析和再平衡建议
```

### 14.2 订单与交易

状态：仅有数据库结构或未形成完整 Java 闭环。

预计实体：

- 订单。
- 成交回执。
- 资金或产品流水。

预计输出：

- 订单状态。
- 成交数量和价格。
- 失败原因。
- 持仓变化。

### 14.3 AI 信号与推荐

状态：`aiw_ai_signal`、`aiw_ai_recommendation` 表已存在，完整应用服务和接口尚未闭环。

预计串联：

```text
行情、新闻、组合和风险画像
  -> AI 模型
  -> AI Signal
  -> AI Recommendation
  -> 用户查看、接受或拒绝
```

### 14.4 策略回测

状态：`aiw_backtest_result` 表已存在，执行引擎和接口尚未闭环。

预计输出：

- 累计收益。
- 最大回撤。
- 波动率。
- 基准比较。
- 回测明细。

### 14.5 风控、通知和审计

状态：数据库结构已规划或部分存在，完整业务接口尚未全部闭环。

预计串联：

```text
用户、订单、组合、AI 建议
  -> 风控规则检查
  -> 风险命中记录
  -> 通知
  -> 审计日志
```

## 15. 当前业务线依赖关系

```text
账户与权限
  ├── 产品管理
  ├── 行情管理
  ├── 任务配置管理
  └── AI 模型管理

产品管理
  -> 市场行情
  -> 主题收益与动量

投资资讯
  -> 资讯热度
  -> 投资分析报告

市场行情
  -> 主题收益
  -> 市场动量
  -> 投资分析报告

AI 模型管理
  -> InvestmentAnalysisProvider 选择
  -> 投资分析报告
```

## 16. 当前主要接口分组

| 接口分组 | Controller | 主要输出 |
| --- | --- | --- |
| 认证 | `AuthenticationController` | 用户和会话 |
| 当前用户 | `CurrentUserController` | 当前用户资料和偏好 |
| 用户管理 | `AdminUserController` | 用户列表、详情和状态 |
| 角色权限 | `AdminRoleController` | 角色、权限和用户角色 |
| 产品目录 | `ProductController`、`AdminProductController` | 产品列表、详情和管理结果 |
| 模拟组合 | `MockPortfolioController` | 当前用户模拟组合、最新估值和持仓 |
| 市场行情 | 当前无独立 Controller | 已有应用服务和仓储，尚未对前端形成完整接口 |
| 投资任务 | `InvestmentTaskController` | 配置、触发、执行记录、新闻和快照 |
| AI 模型 | `AiModelController` | 模型版本、配置和状态 |
| 投资分析 | `InvestmentAnalysisController` | 分析报告和图表数据 |

投资任务接口中，本轮新增或增强的前端可见能力：

| 接口 | 用途 |
| --- | --- |
| `/api/investment/tasks/article-relations/list` | 查询新闻、主题和产品代码的显式关联 |
| `/api/investment/tasks/snapshots/list` | 查询收益、动量和热度快照；`metrics` 已展开为质量指标和样本说明 |
| `/api/investment/tasks/definitions` | 查询任务配置；可查看 `themeProducts` 参数 |
| `/api/investment/tasks/definitions/save` | 保存任务配置；可维护 `themeProducts` 参数 |
| `/api/investment/tasks/trigger` | 手动触发任务；可临时覆盖 `themeProducts` |

投资分析接口中，本轮增强的前端可见能力：

| 接口 | 用途 |
| --- | --- |
| `/api/investment/analysis/generate` | 生成含数据质量、参考仓位、情景收益和图表数据的分析报告 |
| `/api/investment/analysis/reports/list` | 查询历史分析报告，响应结构与生成接口一致 |

模拟组合接口中，本轮新增的前端可见能力：

| 接口 | 用途 |
| --- | --- |
| `/api/mock/portfolios/create` | 创建当前用户模拟组合，并生成初始现金估值 |
| `/api/mock/portfolios/mine` | 查询我的模拟组合列表，展示最新估值摘要 |
| `/api/mock/portfolios/detail` | 查询模拟组合详情、最新估值和当前持仓 |

完整 JSON 展开说明见 `docs_new/10-frontend-interface-changes.md` 和 `docs_new/12-frontend-api-update-log.md`。

## 17. 阅读代码建议

理解一条业务线时按以下顺序阅读：

1. 先看 `interfaces/controller`，确认对外能力。
2. 看对应 `interfaces/request` 和 `interfaces/dto/response`，理解输入输出。
3. 看 `application/service`，理解业务编排和事务。
4. 看 `domain/model`，理解业务实体和状态。
5. 看 `domain/repository`，理解持久化契约。
6. 看 `infrastructure/persistence/repository/*Impl`，理解转换方式。
7. 看 Mapper Java 和 Mapper XML，确认真实 SQL。
8. 看 Flyway migration，确认表结构、索引和唯一约束。
9. 看测试，确认核心规则和接口契约。

## 18. 后续建设优先级建议

1. 接入稳定的中国大陆新闻数据源，逐步减少兜底资讯依赖。
2. 完善行情数据接入，补齐真实分钟线、日线和复权收益。
3. 建立主题、产品、行业和指数成分的正式映射表。
4. 完善主题快照和投资分析的集成测试。
5. 落地投资组合与持仓业务线。
6. 将投资分析扩展到用户风险画像和真实组合。
7. 落地 AI 信号、推荐、回测和风控审计闭环。
