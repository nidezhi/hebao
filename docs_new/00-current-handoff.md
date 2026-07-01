# 00 Current Handoff

生成日期：2026-06-28

本文是 dzcom 后端仓库与 dzcom_web 前端仓库的唯一默认交接入口。后续任务开始前默认先读本文，再按本文指向读取少量相关文档和目标文件。

## 最新结论区

### 当前协作口径

- **最高优先级铁律：任何新生成、修改、补齐的代码，必须先遵守对应仓库开发铁律；后端严格遵守 `03-backend-laws.md`，前端严格遵守 `02-frontend-laws.md`。铁律优先级高于功能推进、速度、临时绕过、个人习惯和历史旧实现；如不能满足，先登记 gap/更新契约，不得继续堆代码。**
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
- 首轮联调基线已完成，后端主闭环、前端主页面和真实数据均可支撑当前产品闭环；页面审计见 `23-frontend-page-audit-20260628.md`，剩余问题看 `06-backend-gap-list.md`。
- 自动闭环当前口径：系统默认项在 `AUTO_INVESTMENT_CLOSED_LOOP`，方案列表在 `AUTO_INVESTMENT_CLOSED_LOOP_PROFILE`，定时任务以 `scheduledConfigProfileCode` 为权威方案；前端入口为 `/config-center/system-configs`、`/config-center/tasks`、`/overview`、`/simulation`、`/report-studio`、`/review-loop`。
- 自动闭环近期核心状态：每天三次定时任务已生效；默认 Mock 资金组合为 10W CNY；报告生成会携带组合上下文和候选产品；Mock 执行支持调仓、HOLD/SKIP 无动作记录、现金不足业务阻断和探索买入样本。
- 大模型调用审计中心已建立：`aiw_ai_model_call_audit` 与 `/api/ai/model-call-audits/list|detail` 持久化模型、Prompt、Skill、业务关联、输入输出摘要、耗时和失败上下文；前端入口 `/ai-call-audits`。
- 最新铁律整改已完成：本次新增模型调用审计代码改为 MyBatis-Plus `selectPage` 分页和统一分页插件，不再使用 `last("limit")`；新增/改造后端方法已补业务语义 Javadoc；前端审计页使用真实记录派生的可搜索选择器，JSON 只作为辅助调试区。
- 持续进化分析层已建立：新增 `POST /api/analytics/investment-evolution/summary`，按最近样本归集闭环成功率、Mock 收益/回撤/换手代理、风控拒绝原因、模型调用稳定性、反馈/回测样本和模型/Prompt/Skill A/B 归因；样本不足时返回 `sampleStatus/limitations`，不伪造长期结论。
- 前端新增 `/investment-evolution` 统一分析页，入口在“投资闭环”菜单；页面只消费真实分析接口和 adapter 后的结构化 ViewModel。
- 后端启动故障已修复：模型调用审计接入后 `MockOpenAiCompatibleInvestmentAnalysisProvider` 和 `OpenAiCompatibleJsonCompletionClient` 存在测试兼容构造器与生产构造器，Spring 曾误走无参构造路径；现已显式标记生产构造器并补启动回归测试。
- `/ai-call-audits` 页面已升级为紧凑型模型调用审计工作台：筛选区按主筛选 + 更多高级筛选收纳；列表按状态图标、模型层、调用层、业务归因、Prompt/Skill 分级展示；主 UI 优先展示中文业务/场景/任务 label，原始枚举只作为追踪字段或次要信息保留。
- 自动闭环报告子任务失败原因已收紧：父闭环不再出现 `自动报告任务失败: null`，任务执行审计会对空 message 异常兜底为异常类型，父节点会带上 `taskCode/status/eventId/reason`；Java 26 下 MyBatis `BoundSql.sql` final-field mutation warning 已在 Maven 测试/启动参数中加入推荐开关。
- IDEA 直接启动补齐：Maven `spring-boot:run`/测试已带 `--enable-final-field-mutation=ALL-UNNAMED`，但 IDEA 绿色按钮不会继承 Maven 插件参数；新增共享运行配置 `.run/DzcomApplication local.run.xml`，默认 `local` profile，并带 `--sun-misc-unsafe-memory-access=allow --enable-final-field-mutation=ALL-UNNAMED`。
- Mock 订单幂等键越界改为数据库字段扩容解决：新增 `V49__expand_order_idempotency_key.sql` 将 `aiw_order.idempotency_key` 扩为 `VARCHAR(512)`，Service 层只 trim 首尾空白并保留自动闭环可读追踪原文，不再压缩或哈希改写。
- 自动闭环诊断优化已落地：闭环新增 `SCHEMA_PREFLIGHT` 数据库结构预检，提前阻断 `aiw_order.idempotency_key` 未扩容、报告 `chat_snapshot` 缺失、模型调用审计表缺失等本地 schema 漂移；新增 `MOCK_PLAN_NORMALIZATION` 步骤记录模型报告被解析出的动作、金额、产品和目标权重；报告转 Mock 买入兼容 `orderSizing.referenceTradeAmount`、`plannedTradeAmount`、`selectedProduct.productBizId` 等真实库内字段；默认方案恢复 `promptTaskCode=auto-prompt-governance`，Prompt/模型正式自动启用默认关闭；真实数据质量快照新增 `sampleStatus=PASS/REVIEW/BLOCK`。
- 自动闭环 Mock 执行契约继续收紧：`MOCK_PLAN_NORMALIZATION` 支持 `orderSuggestion.referenceAmount`、`orderReference.referenceAmount` 等金额别名，并能用 `targetWeights[0].targetWeight * portfolio.totalAsset` 推导参考金额；BUY/REBALANCE 缺少可执行金额或产品时在归一化节点直接 `BLOCKED`，不再继续打到 Mock 交易层；报告转 Mock 买入同步支持目标权重推导和 `targetWeights[0].productBizId`。
- UI 基线已确认并三轮修正：参考阿里云控制台的全宽 `50px` 顶栏、约 `204px` 左侧菜单、宽广工作面、黑灰主色、紧凑筛选、标签页与表格主导；左侧菜单父级约 `40px`、子级约 `34px` 且内缩分层；DZAI icon 已切换为自有黑灰标识。
- 右侧内容区已采用默认 mini density：正文 `12px`、次级说明 `11px`、页面标题约 `20px`、小节标题约 `14px`；按钮/输入默认约 `28px`，表格行约 `36px`，卡片 padding 和段落间距同步收紧。左侧菜单比例保持不变。
- 前端 JSON 展示和 Overview 滚动已收紧：共享 `JsonPreview` 改为短摘要 + 通用完整 JSON 弹窗，Overview 的“机会雷达/风险审计流”改用结构化短描述并让长列表在卡片内滚动；主内容区增加底部留白，避免页面底部被视口滚动裁掉。
- AI Call Audit 输入输出完整查看已修复：外层 `JsonPreview` 保持一行短预览；后端新增 `request_payload/response_payload` 完整审计字段，详情接口返回完整输入输出，列表查询排除大字段；前端“查看完整”弹窗优先读取完整 payload。旧审计记录因历史入库时已截断无法还原，新记录可完整查看。
- 顶栏无意义入口已收敛：删除无后续动作的“工作台/文档/费用/工单”装饰入口，保留品牌、真实全局搜索、用户状态和登录登出；全局搜索基于现有路由生成结果，可点击跳转真实页面。
- Handoff 自身瘦身已执行：本文最新区只保留入口索引、当前有效状态和最近验证摘要；详细历史默认不读。

### 最近验证结果

- 后端最近全量基线：`./mvnw -q test` 通过。
- 前端最近全量基线：`node node_modules/vue-tsc/bin/vue-tsc.js -b` 通过；`node node_modules/vite/bin/vite.js build` 通过，仅既有 chunk size warning。
- 自动闭环、配置、Mock、报告、Overview、复盘等专项验证均已在对应历史归档中留短摘要；默认不再读取旧验证流水。
- 模型调用审计中心最新整改验证：`./mvnw -q -Dtest=AiModelCallAuditApplicationServiceTest,ControllerRequestContractTest,OpenAiCompatibleJsonCompletionClientTest,MockOpenAiCompatibleInvestmentAnalysisProviderTest test` 通过；`/Users/daniel/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/bin/node node_modules/vue-tsc/bin/vue-tsc.js -b` 通过；同 Node 路径执行 `node_modules/vite/bin/vite.js build` 通过，仅既有 chunk size warning。
- 持续进化分析验证：`./mvnw -q -Dtest=InvestmentEvolutionAnalyticsApplicationServiceTest,ControllerRequestContractTest test` 通过；前端 bundled Node 执行 `vue-tsc -b` 与 `vite build` 通过，仅既有 chunk size warning；`diff --check` 通过。
- 后端启动故障验证：`./mvnw -q -Dtest=DzcomApplicationTests test` 通过，已断言核心 AI Bean 可由 Spring 装配；`./mvnw -q -Dtest=MockOpenAiCompatibleInvestmentAnalysisProviderTest,OpenAiCompatibleJsonCompletionClientTest,AiModelCallAuditApplicationServiceTest,InvestmentEvolutionAnalyticsApplicationServiceTest,ControllerRequestContractTest test` 通过；`git diff --check` 通过。`spring-boot:run` 已验证可启动并已停止，避免占用 IDEA 端口。
- 模型调用审计页验证：前端 bundled Node 执行 `vue-tsc -b` 与 `vite build` 通过，仅既有 chunk size warning；`/ai-call-audits` 路由冒烟通过，真实接口返回审计记录，筛选卡片高度约 `102px`，正文未裸展示 `INVESTMENT_REPORT/DEFAULT`，控制台无 error。
- 自动闭环失败原因验证：`./mvnw -q -Dtest=AutoInvestmentClosedLoopOrchestrationTaskHandlerTest,InvestmentTaskManagementServiceTest test` 通过；`./mvnw -q -DskipTests compile` 通过；`git diff --check` 通过。测试覆盖报告子任务抛出无 message 异常时，父任务记录为 `BLOCKED` 且失败原因包含任务编码、状态和异常类型，不再包含 `null`。
- IDEA 启动 warning 配置验证：新增 `.run/DzcomApplication local.run.xml` 后，`./mvnw -q -DskipTests compile` 与 `git diff --check` 通过；后续 IDEA 启动应选择共享配置 `DzcomApplication local`。
- Mock 订单幂等键扩容验证：`./mvnw -q -Dtest=MockPortfolioApplicationServiceTest,AutoInvestmentClosedLoopOrchestrationTaskHandlerTest test` 通过；`./mvnw -q -DskipTests compile` 通过；`git diff --check` 通过。测试覆盖长自动闭环幂等键按原文保存且长度超过 128，依赖 V49 字段扩容承载。
- 自动闭环诊断优化验证：`./mvnw -q -Dtest=MockPortfolioApplicationServiceTest,AutoInvestmentClosedLoopOrchestrationTaskHandlerTest test` 通过；`./mvnw -q -DskipTests compile` 通过；`git diff --check` 通过。测试覆盖 schema preflight 阻断、嵌套报告计划字段归一化、默认候选/启用边界和 Mock 买入真实字段解析。
- Mock 执行契约收紧验证：`./mvnw -q -Dtest=MockPortfolioApplicationServiceTest,AutoInvestmentClosedLoopOrchestrationTaskHandlerTest test` 通过；`./mvnw -q -DskipTests compile` 通过；`git diff --check` 通过。测试覆盖 `orderSuggestion.referenceAmount`、目标权重金额推导、归一化缺金额提前 `BLOCKED` 且不进入 `MOCK_TRADE`。
- 阿里云控制台 UI 三轮比例验证：前端 bundled Node 执行 `vue-tsc -b` 与 `vite build` 通过，仅既有 chunk size warning；`git diff --check` 通过；HTTP 冒烟 `/`、`/overview`、`/ai-call-audits`、`/config-center/system-configs`、`/ui-system` 均返回 200；浏览器测量确认全宽顶栏 `50px`、侧栏 `204px`、父菜单 `40px`、子菜单 `34px`、搜索/按钮 `32px`，主按钮与选中态为黑灰体系。
- 前端 mini density 验证：`/Users/daniel/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/bin/node node_modules/vue-tsc/bin/vue-tsc.js -b` 通过；同 Node 路径执行 `node_modules/vite/bin/vite.js build` 通过，仅既有 chunk size warning；`git diff --check` 通过。当前 `dzcom_web` Git 工作区显示 clean，`HEAD` 已包含 mini density token 与 CSS 规则。
- Overview JSON/滚动优化验证：前端 bundled Node 执行 `vue-tsc -b` 与 `vite build` 通过，仅既有 chunk size warning；`git diff --check` 通过；浏览器访问 `/overview` 在未登录态正确重定向 `/login?redirect=/overview` 且无 console error，真实数据路由视觉冒烟需登录后复核。
- AI Call Audit 完整 payload 验证：`./mvnw -q -Dtest=AiModelCallAuditApplicationServiceTest test` 与 `./mvnw -q -DskipTests compile` 通过；前端 bundled Node 执行 `vue-tsc -b` 与 `vite build` 通过，仅既有 chunk size warning；两仓 `git diff --check` 通过。真实审计抽屉视觉复核需用户登录态下打开 `/ai-call-audits` 后点击“查看完整”确认。
- 顶栏搜索验证：`vue-tsc -b` 与 `vite build` 通过；`/ui-system`、`/development-rules` HTTP 冒烟 200；浏览器验证输入“开发”出现 `/ui-system`、`/development-rules`、`/standby/api` 三个真实路由结果，点击“开发铁律”可跳转 `/development-rules`，顶栏不再出现“文档/费用/工单”装饰链接。
- 真实浏览器/API 冒烟依赖用户在 IDEA 启动后端服务；未启动服务时不把路由/API 冒烟写成通过。

### 下一步默认动作

用户给出一个具体功能或页面后，按以下方式开始：

1. 只读取本文。
2. 第一时间读取并声明本次适用的 `02-frontend-laws.md` / `03-backend-laws.md` 相关条款；铁律不满足时先补 gap 或 contract。
3. 读取该任务相关的 contract/gap 摘要；若是前端页面整改，读取 `23-frontend-page-audit-20260628.md` 最新结论区。
4. 列出本次将读取的后端/前端目标文件。
5. 先更新 contract 或 gap，再进入代码修改。
6. 完成后把结论写回本文，并把完成过程压缩为摘要。

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

### 2026-06-29 自动闭环配置方案

完成“配置、Mock、闭环流程”第一阶段边界拆分。后端在 `aiw_system_config` 增加 `AUTO_INVESTMENT_CLOSED_LOOP_PROFILE` 方案分组，任务触发时按“任务定义参数 -> 方案参数 -> 手动参数”合并，并把方案快照写入运行事件；前端 `/config-center/system-configs` 增加方案列表/结构化编辑，`/config-center/tasks` 触发自动闭环时必须选择方案。验证：后端目标测试、前端 type-check/build 通过。

### 2026-06-29 自动闭环定时默认方案

补齐定时任务口径：手工触发可选方案，Cron/SCHEDULE 触发固定读取 `AUTO_INVESTMENT_CLOSED_LOOP.scheduledConfigProfileCode` 作为最权威方案，并把方案快照写入事件；前端系统配置页新增“定时任务权威方案”下拉，任务页手工触发只提交所选 `configProfileCode`。新增 `V44__add_scheduled_auto_closed_loop_profile_config.sql`，验证：后端目标测试、前端 type-check/build 通过。

### 2026-06-30 自动闭环方案高级化

方案从扁平参数升级为结构化策略：基础、执行、门禁、Mock、安全阀、Prompt/回测分组编辑；后端配置服务兼容旧扁平 JSON 和新嵌套 JSON，并在闭环运行中新增 `PROFILE_SNAPSHOT` 步骤保存方案证据。新增 `V45__upgrade_auto_closed_loop_default_profile.sql`，验证：后端目标测试、前端 type-check/build 通过。
