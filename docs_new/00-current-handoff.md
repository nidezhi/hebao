# 00 Current Handoff

生成日期：2026-06-28

本文是 dzcom 后端仓库与 dzcom_web 前端仓库的唯一默认交接入口。后续任务开始前默认先读本文，再按本文指向读取少量相关文档和目标文件。

## 最新结论区

### 当前协作口径

- 两个 Git 仓库保持独立：后端 `dzcom`，前端 `dzcom_web`。
- 共享文档归档在 `dzcom/docs_new`。前端 `dzcom_web` 不再保留 md 文档，后续以后端 `dzcom/docs_new` 为准。
- 前端历史 mock/handoff 已从前端仓库删除；如需历史，以后端 `docs_new` 已归档内容为准。
- 后续不依赖聊天历史维持项目事实；完成项必须沉淀回本文或对应 contract/gap 文档。

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

### 当前完成状态

- 已建立统一协作文档体系。
- 已明确前端不得绕过真实接口体验，后端不足进入 gap list。
- 已明确每个功能按 contract -> backend -> frontend -> smoke -> handoff 的顺序推进。
- 已删除 `dzcom_web` 下所有 `.md` 文档；前端仓库不再作为文档事实源。
- 首轮前后端联调基线已完成：按后端业务目标校验后端实现、API 闭环和前端页面覆盖。
- 本轮已补齐产品列表/详情 DTO 中前端工作台需要的真实摘要字段：`latestNav`、`latestQuoteTime`、`sourceCode`、`dataQualityScore`。
- 本轮已新增并应用 `V38__align_active_model_skill_binding_versions.sql`，将启用的模型 Skill 绑定对齐到当前 ACTIVE 模型版本。
- 本轮已新增并应用 `V40__govern_core_data_gap_baseline.sql`，关闭核心数据 gap：NEWS 质量、数据源健康覆盖、风控审计样本。

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

- 后端：`./mvnw -q test` 通过。
- 前端：`node node_modules/vue-tsc/bin/vue-tsc.js -b` 通过。
- 前端：`node node_modules/vite/bin/vite.js build` 通过；仅有既有 chunk size warning。
- 数据核验：`aiw_ai_model_skill_binding` 与 ACTIVE 模型版本漂移数为 `0`。
- 数据核验：主题选择器真实数据可返回 `AI人工智能`、`半导体`、`黄金`。
- 核心数据审计：产品 `8`、产品画像 `8`、行情 `80`、资讯 `10`、主题快照 `597`、报告 `17`、闭环运行 `42`、订单 `5`、回测 `4`、反馈 `4`。
- 核心数据优化：已新增并应用 `V39__normalize_core_data_audit_baseline.sql`，报告空字符串 `theme_code` 归零，陈旧 `RUNNING` 闭环归零。
- 核心数据优化：已新增并应用 `V40__govern_core_data_gap_baseline.sql`。NEWS 真实质量快照最高从 `0.2500` 提升到 `0.7500`，平均 `0.6875`；启用数据源健康覆盖从 `4/330` 提升到 `330/330`；风控检查从 `1` 条提升到 `25` 条，覆盖 `REPORT/ORDER/PORTFOLIO` 和 `PASS/REVIEW/REJECT`。
- 本轮新增只读审计脚本：`scripts/local/audit-core-data-gaps.sql`，用于后续低成本复查 GAP-0107/0108/0109。

### 下一步默认动作

用户给出一个具体功能或页面后，按以下方式开始：

1. 只读取本文。
2. 读取该任务相关的 laws/contract/gap 摘要。
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
