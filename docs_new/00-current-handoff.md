# 00 Current Handoff

生成日期：2026-06-28

本文是 dzcom 后端仓库与 dzcom_web 前端仓库的唯一默认交接入口。后续任务开始前默认先读本文，再按本文指向读取少量相关文档和目标文件。

## 最新结论区

### 当前协作口径

- 两个 Git 仓库保持独立：后端 `dzcom`，前端 `dzcom_web`。
- 共享文档归档在 `dzcom/docs_new`。前端 `dzcom_web` 不再保留 md 文档，后续以后端 `dzcom/docs_new` 为准。
- 前端历史 mock/handoff 已从前端仓库删除；如需历史，以后端 `docs_new` 已归档内容为准。
- 后续不依赖聊天历史维持项目事实；完成项必须沉淀回本文或对应 contract/gap 文档。
- 本文只做默认入口和当前状态索引，不做无限流水账；新增结论前应优先合并旧摘要，最新结论区超过约 `150` 行或全文超过约 `350` 行时，必须先瘦身再追加。

### 仓库结构地图

后端 `dzcom`：

- 工程入口：`pom.xml`
- 文档入口：`docs_new`
- Controller：`src/main/java/com/example/dzcom/interfaces/controller`
- Request：`src/main/java/com/example/dzcom/interfaces/request`
- DTO/View：`src/main/java/com/example/dzcom/application/dto`
- Service：`src/main/java/com/example/dzcom/application/service`
- Domain：`src/main/java/com/example/dzcom/domain`
- Persistence：`src/main/java/com/example/dzcom/infrastructure/persistence`
- DB migration：`src/main/resources/db/migration`
- 后端测试：`src/test/java/com/example/dzcom`

前端 `dzcom_web`：

- 工程入口：`package.json`、`vite.config.ts`
- API 基建：`src/shared/api`、`src/api`
- 业务实体 API/类型/adapter：`src/entities/*`
- 页面：`src/pages`
- 路由：`src/router`
- 共享业务组件：`src/shared/components`
- 前端 md 文档：已删除；共享文档入口统一使用 `dzcom/docs_new`

### 当前文档体系

- `00-current-handoff.md`：当前唯一交接入口。
- `01-product-contract.md`：产品目标、闭环、核心对象关系。
- `02-frontend-laws.md`：前端开发铁律。
- `03-backend-laws.md`：后端开发铁律。
- `04-api-contract-rules.md`：接口契约规则。
- `05-cross-repo-change-checklist.md`：前后端联动变更清单。
- `06-backend-gap-list.md`：后端补齐清单。
- `99-context-slimming-rules.md`：对话瘦身规则。

### Handoff 自身瘦身口径

- 默认只读本文“最新结论区”；历史归档区只按明确章节读取。
- “当前完成状态”只保留仍影响后续决策的最近结论；阶段性明细应迁移到专题文档或历史归档。
- “本轮验证结果”只保留最近仍可复用的验证结论；旧验证流水按专题压缩。
- 不在本文长期保存完整接口清单、完整页面清单、完整 diff、完整日志或长篇排查过程。
- 每次结束任务时，优先用 1-3 条摘要更新本文；若需要更多细节，写入专题文档并在本文保留路径。

### 当前完成状态

- 统一协作机制已建立：共享文档以 `dzcom/docs_new` 为准，前端仓库不再保存 md 事实源；后续按 contract -> backend -> frontend -> smoke -> handoff 推进。
- 首轮联调基线已完成：后端主闭环、前端主页面和真实数据基线均可支撑当前产品闭环；详细页面审计见 `23-frontend-page-audit-20260628.md`。
- 近期关键闭环已完成：权限 catalog、Review Loop metadata、Prompt Lab 变量 schema、运行模型绑定、Simulation 订单事件、Product Risk 行情契约、Overview 闭环追溯、Prompt 候选产物、AI 模拟资金池、报告结构化展示。
- 自动闭环默认项已迁移到 `aiw_system_config`，配置分组 `AUTO_INVESTMENT_CLOSED_LOOP`；前端 `/config-center/system-configs` 提供结构化配置页，用户和 Prompt 使用真实选择器。
- 开发铁律已补充：Mapper 层非必要不得新增手写 SQL/XML SQL，优先 MyBatis-Plus `BaseMapper`、Service、LambdaQueryWrapper、分页和规范代码生成；见 `03-backend-laws.md`。
- Handoff 自身瘦身规则已生效：本文只做默认入口和当前状态索引，超过阈值先压缩再追加；详细规则见 `99-context-slimming-rules.md`。

### 首轮联调基线（2026-06-28）

- 后端业务主链路已具备：产品/行情/数据源治理/投资任务/报告/Prompt/Skill/模型治理/模拟组合/模拟交易/回测反馈/风控审计/闭环运行。
- 前端主页面已覆盖：总览、数据质量、数据采集、产品风险、报告工作台、Prompt Lab、模拟交易、复盘闭环、风控审计、配置中心。
- 真实数据基线：报告 `17` 条且均成功；产品 `8` 个；行情 `80` 条；新闻 `10` 条；数据质量快照 `47` 条。
- 最新真实数据质量门禁：`REAL_DATA_GATE` 质量分 `0.8750`，`reportAllowed=true`。
- 运行模型基线：`openai-compatible-analysis@default-v1`，`mockEnabled=false`，远程模型 `gpt-5.5`，LLM discovery 任务保持禁用。
- 已发现并修正：启用的 `aiw_ai_model_skill_binding` 曾指向旧 `mock-v1`；V38 应用后 `drift_count=0`。
- 已发现并修正：前端产品工作台依赖产品行情/质量摘要字段，后端 `ProductResponse` 原先缺失；现列表和详情均返回真实最新 1D 行情与画像质量分。
- 已发现并修正：报告工作台曾手填 `themeCode`；现后端提供真实主题选择器 `POST /api/investment/tasks/theme-options`，前端生成报告改为可搜索下拉。
- 前端实现基线：核心路由和 API client/type/adapter 已存在；本轮未调整 UI。
- 剩余 gap 已写入 `06-backend-gap-list.md`，后续按具体功能逐项处理，不在对话中保留完整过程上下文。

### 本轮验证结果

- 后端最近全量基线：`./mvnw -q test` 通过。
- 前端最近全量基线：`node node_modules/vue-tsc/bin/vue-tsc.js -b` 通过；`node node_modules/vite/bin/vite.js build` 通过，仅既有 chunk size warning。
- 核心数据治理基线：NEWS 质量、数据源健康覆盖、风控样本已通过 V40 治理；复查脚本为 `scripts/local/audit-core-data-gaps.sql`。
- 自动闭环配置基线：`V41__seed_auto_investment_closed_loop_system_config.sql` 已写入默认配置；后端目标测试和全量测试通过。
- 真实浏览器/API 冒烟依赖用户在 IDEA 启动后端服务；未启动服务时不把路由/API 冒烟写成通过。

### 下一步默认动作

用户给出一个具体功能或页面后，按以下方式开始：

1. 只读取本文。
2. 读取该任务相关的 laws/contract/gap 摘要；若是前端页面整改，读取 `23-frontend-page-audit-20260628.md` 最新结论区。
3. 列出本次将读取的后端/前端目标文件。
4. 先更新 contract 或 gap，再进入代码修改。
5. 完成后把结论写回本文，并把完成过程压缩为摘要。

## 当前任务模板

每次任务在本文更新这一小段即可：

| 字段 | 内容 |
| --- | --- |
| 任务名称 | 待填写 |
| 涉及仓库 | `dzcom` / `dzcom_web` / 两者 |
| 需读文档 | 待填写 |
| 需改后端 | 待填写 |
| 需改前端 | 待填写 |
| 需验证 | 待填写 |
| 当前状态 | 待开始 / 进行中 / 已完成 / 阻塞 |
| 下一步 | 待填写 |

## 历史归档区

### 2026-06-28 初始化

完成前后端统一协作文档与上下文瘦身机制建设。未移动仓库，未修改业务代码。`dzcom/docs_new` 成为共享事实入口，前端 `mock` 与散落 md 作为历史归档来源。

### 2026-06-28 前端 md 清理

已删除 `dzcom_web` 下所有 `.md` 文档，包括根目录说明、`docs` 说明和 `mock` 历史文档。后续产品契约、前后端开发铁律、handoff、API 契约、gap list、验收流程全部以后端 `dzcom/docs_new` 为准。

### 2026-06-28 首轮前后端联调基线

按后端业务目标优先完成第一次跨仓比对。后端主闭环和前端主页面均已覆盖，真实数据可支撑当前驾驶舱；本轮补齐产品 API 摘要字段，并用 V38 修复模型 Skill 绑定版本漂移。完整过程不再依赖聊天历史，后续默认读取本文最新结论区和 `06-backend-gap-list.md`。

### 2026-06-28 GAP-0103 报告主题选择器

按铁律关闭报告生成手填 `themeCode` 缺口。后端新增从真实 `InvestmentThemeSnapshot` 派生的主题选项接口，前端报告工作台改为可搜索主题下拉；清空选择代表全市场分析。验证：后端全量测试、前端 type-check/build、真实主题数据查询均通过。

### 2026-06-28 核心数据审计与 V39

完成一次真实数据库核心数据审计。结论：主业务目标已可跑通，数据从产品、行情、资讯、主题快照到报告、闭环、Mock 交易、回测反馈均有真实记录支撑；中高置信报告 `7` 条且质量门禁通过，低置信报告 `10` 条会被门禁降级。发现并修复两处数据语义污染：历史报告 `theme_code=''` 统一改为 `NULL`，超过 30 分钟的陈旧 `RUNNING` 闭环运行标记为 `FAILED/BLOCK` 并补 `RUNNING_TIMEOUT` 步骤。剩余数据治理缺口已写入 `06-backend-gap-list.md`。

### 2026-06-28 GAP-0107/0108/0109 核心数据治理

按真实数据闭环完成核心数据 gap 优化。NEWS 采集质量改为按主题关键词覆盖和公开源真实可达样本计算，并在 detail 中沉淀 `qualityPolicy/qualityReasons`；数据源看板新增 `PENDING_HEALTH_CHECK` 结构化状态，V40 为启用但无健康记录的数据源补健康行；风控审计新增通用 `recordCheck`，核心报告、订单、组合路径会沉淀 PASS/REVIEW/REJECT 样本。验证：`./mvnw -q test` 通过；`scripts/local/audit-core-data-gaps.sql` 复查通过。

### 2026-06-28 前端逐页审计

按前端铁律逐页审计入口、用户权限、核心业务闭环和配置中心页面。结论：主链路页面和真实 API 调用基本覆盖；报告、模拟交易、风险审计、模型 Skill 绑定达标较好；登录/用户默认密码、角色权限手填、Review Loop 原始 JSON、Prompt Lab 预览变量 JSON、旧规范页文案和部分弱类型契约需要整改。详细矩阵见 `23-frontend-page-audit-20260628.md`，后续默认不重读所有页面。

### 2026-06-28 GAP-0110 权限 Catalog

关闭账号权限组 P1 缺口。后端从 `PermissionCodes` 注册表提供 `POST /api/admin/roles/permissions/catalog`，返回 `permissionCode/displayName/groupName/description/riskLevel/status`；前端 `/roles` 改为分组 checkbox 配置权限，不再手填权限编码。同时移除 `/login` 账号密码预填和 `/users` 新建用户默认密码。验证：后端测试、前端 type-check/build、`/roles` 路由冒烟均通过。

### 2026-06-28 GAP-0111 Review Loop 结构化契约

关闭复盘闭环 P1 缺口。后端新增 `POST /api/review-loop/metadata`，返回回测策略、基准、反馈原因码、Prompt 评估场景和字段 schema；前端 `/review-loop` 使用该契约渲染选择器和字段级输入，Prompt 版本来自真实 Prompt 列表，保存时仅将结构化字段薄适配为既有 `parameters/metrics/metadata/scoreDetail` JSON 字符串。验证：后端测试、前端 type-check/build、`/review-loop` 路由冒烟均通过。

### 2026-06-28 GAP-0112 Prompt Lab 预览变量

关闭 Prompt Lab P2 缺口。后端 `AiPromptVariableResponse` 增加 `previewValueType/previewDefaultValue/previewExampleValue`，由变量名和来源路径派生预览输入契约；前端 `/prompt-lab` 按变量定义渲染字段级预览表单，支持填入示例值，预览请求收紧为 `Record<string,string>`。验证：后端测试、前端 type-check/build、`/prompt-lab` 路由冒烟均通过。

### 2026-06-28 GAP-0113 运行模型绑定

关闭运行模型绑定 P2 缺口。后端 `AiModelBindingController` 纳入统一 Controller 契约测试并补齐 Swagger 响应说明；前端新增 `entities/ai-model-binding`、`/config-center/model-bindings` 路由和配置中心入口，使用 ACTIVE 模型选择器维护运行绑定。验证：后端测试、前端 type-check/build、`/config-center/model-bindings` 路由冒烟均通过。

### 2026-06-28 低风险前端漂移整改

关闭页面审计中的三项前端漂移。`/data-quality` 详情趋势查询使用后端返回的 `latestQuality.dataType`，不再把数据源类型当数据类型；`/development-rules` 和 `/standby/api` 更新为当前共享文档和真实 API 接入口径。验证：前端 type-check/build、三个目标路由冒烟均通过。

### 2026-06-28 Account 偏好结构化

关闭 `/account` 偏好 JSON P3 问题。后端偏好接口保持 JsonNode 扩展契约和白名单键，前端将 `language/timezone/theme/market/notification/dashboard` 渲染为选择器、开关和字段级对象表单；未知历史偏好不开放编辑。验证：前端 type-check/build 和 `/account` 路由冒烟通过。

### 2026-06-28 Simulation 订单事件契约

部分收紧 GAP-0105。后端新增按组合查询最近订单事件接口，返回订单状态、产品、方向、金额和数量等结构化摘要字段；前端 `/simulation` 改用 `PortfolioOrderEventDto` 与 `MockPortfolioPerformanceDto`，收益曲线读取 `valuations`，订单事件表不再依赖 `Record<string, unknown>` 或 raw JSON 折叠区。验证：后端测试、前端 type-check/build 和 `/simulation` 路由冒烟通过。

### 2026-06-28 Feedback/Backtest 类型收紧

继续推进 GAP-0105。前端 feedback、Prompt evaluation、backtest 保存/详情/生成 API 改用明确 Request DTO；Review Loop 表单状态字段收窄到 `BacktestStatus` 和 `FeedbackAction`，让契约漂移能被 type-check 捕获。验证：前端 type-check/build 和 `/review-loop` 路由冒烟通过。

### 2026-06-28 前端侧栏导航优化

响应配置项入口过深的问题。`BasicLayout` 侧栏改为按业务使用频率分组：投资闭环、数据与资产、AI 配置、权限与规范、开发规范；配置总览保留，但产品与行情、数据源、任务、Prompt、模型、AI Skill、模型 Skill 绑定、运行模型绑定和业务操作管理均可从侧栏直达。验证：前端 type-check/build 和 `/overview` 菜单冒烟通过。

### 2026-06-29 系统配置页

配合后端通用配置表 `aiw_system_config`，前端新增 `/config-center/system-configs`。自动闭环默认项不再通过 YAML 手改，页面提供环境选择、启停、自动化等级、模型类型、资金池名称、初始现金、AI 资金池用户选择器和默认 Prompt 选择器；保存时仍按 `AUTO_INVESTMENT_CLOSED_LOOP` 分组写入 `automationLevel/mockUserBizId/mockPortfolioName/initialCash/promptCode/promptVersion/promptScenario/modelType`。验证：前端 type-check/build 通过；后端真实服务冒烟待 IDEA 启动后执行。

### 2026-06-28 Product Risk 行情契约修复

修复截图反馈的 `/product-risk` 运行时报错。根因是前端查询行情历史使用旧字段 `quoteInterval` 且缺少后端必填 `from/to`；现已改为 `MarketQuoteHistoryRequest` 明确 DTO，并默认查询近 180 天 `1D` 行情。同步收紧产品行情 latest/history/save API 类型，产品配置页保存行情改用后端真实字段 `interval/quoteTime/closePrice`。验证：前端 type-check/build 通过，`/product-risk` 冒烟无 `from` 校验错误。

### 2026-06-28 Overview 闭环实例选择优化

修复 `/overview` 自动闭环时间线默认实例只选中列表项、未立即拉取详情的问题。页面加载后会对默认运行实例调用详情接口，时间线显示真实步骤证据；右上角实例选择器改成可搜索的结构化运行卡片，选中态显示“运行号 · 状态 · 时间”，下拉显示质量、门禁和范围摘要。验证：前端 type-check/build 通过，`/overview` 冒烟确认默认实例和下拉内容均正常。

### 2026-06-29 Overview 闭环节点追溯

修复用户截图反馈的四个完成节点“状态完成但页面无对应实例信息”问题。根因：后端已为 Mock、模型、回测反馈步骤沉淀核心产物 ID，但前端 Overview 只展示状态和 JSON，没有把产物 ID 转成结构化跳转；Prompt 候选步骤缺少 promptCode/promptVersion 输出字段。现已在 Overview 步骤抽屉新增闭环产物追溯，并让 `/report-studio`、`/simulation`、`/review-loop`、`/config-center/models`、`/config-center/prompts` 支持 query 自动定位对应实例；后端 Prompt 候选步骤同步输出 promptCode/promptVersion/scenario/reportBizId。验证：前端 type-check/build、后端目标测试和全量测试通过。

### 2026-06-28 自动报告空主题 NPE 修复

修复 Kafka 任务日志中的 `LocalRuleInvestmentAnalysisProvider.buildContext` 空指针：自动报告任务允许 `themeCodes=""` 生成市场级报告，此时 `GenerateInvestmentAnalysisCommand.themeCode()` 为 `null`。Provider 现将空主题视为全市场报告，新闻关键词使用空集合，不再调用 `themeCode.isBlank()`。新增单测覆盖 `themeCode=null` 市场级报告，后端全量测试通过，并已重启本地 `8511` 服务加载新 class。HTTP 验证确认空主题请求不再出现 NPE；另观察到 `local-rule-analysis` 直接 HTTP 生成会被既有模型配置校验 `baseUrl未配置` 阻断，后续如要开放本地规则模型直调需单独处理配置解析规则。

### 2026-06-28 OpenAI 投资分析 JSON 容错

修复自动报告任务中远端模型内容不是纯 JSON 时直接失败的问题。`MockOpenAiCompatibleInvestmentAnalysisProvider.mergeRemoteOutput` 现复用通用 JSON 客户端的容错策略：先去掉 Markdown 代码围栏，再判断 JSON 对象；若外层有说明文本，则提取首个括号平衡 JSON 对象并记录 warning。若仍无法解析，继续作为业务错误暴露，并记录截断 preview。新增单测覆盖“说明文字 + JSON + 收尾文字”的远端输出。验证：目标测试和后端全量测试通过，本地 `8511` 已重启加载新 class。

### 2026-06-29 自动闭环通用配置表

响应“YAML 太难维护”的要求，自动闭环默认配置不再放在 `application-local.yaml` / `application-dev.yaml`。后端复用既有通用表 `aiw_system_config`，新增系统配置读模型、仓储和读取服务，配置分组为 `AUTO_INVESTMENT_CLOSED_LOOP`；任务参数仍可临时覆盖，系统级维护以表数据为准，代码常量只作为兜底防崩。`V41__seed_auto_investment_closed_loop_system_config.sql` 种子写入默认配置，并从自动闭环任务定义 `parameters` 中移除默认型字段。验证：目标测试和后端全量测试通过。
