# 23 Frontend Page Audit 2026-06-28

生成日期：2026-06-28

本文记录整合后第一次前端逐页审计结论。审计口径以后端业务目标和 `02-frontend-laws.md` 为准，只读页面、路由和直接引用的实体 API/类型；本轮未修改业务代码。

## 最新结论区

### 审计范围

- 入口与用户权限：`/`、`/login`、`/account`、`/users`、`/roles`。
- 核心业务闭环：`/overview`、`/data-quality`、`/data-ingestion`、`/product-risk`、`/report-studio`、`/prompt-lab`、`/simulation`、`/review-loop`、`/risk-audit`。
- 配置中心：`/config-center`、`/config-center/data-sources`、`/config-center/data-source-discovery`、`/config-center/tasks`、`/config-center/products`、`/config-center/prompts`、`/config-center/models`、`/config-center/ai-skills`、`/config-center/model-skills`、`/config-center/actions`。
- 辅助页面：`/development-rules`、`/ui-system`、`/standby/api`。

### 总体结论

- 页面覆盖：主业务闭环页面已覆盖后端核心节点，配置中心也已覆盖大部分写操作落位。
- 功能实现：大多数页面调用真实 `entities/*/api.ts`，不是静态 mock 页面；业务主链路可从治理、采集、报告、模拟交易、复盘、风控审计串起来。
- 铁律通过项：报告页、模拟交易页、模型 Skill 绑定页已较好满足对象选择器和结构化体验要求。
- 主要风险：若干配置页仍存在原始 JSON textarea、自由文本编码或硬编码默认值。
- 后端缺口：权限 catalog、复盘结构化契约、Prompt 预览变量 schema 和运行模型绑定页面已补齐；暂无未关闭的 P1/P2 后端悬空 gap。
- 前端整改：登录默认密码、创建用户默认密码、角色权限手填、Account 偏好 JSON、Simulation 订单事件弱类型、feedback/backtest API 签名、Data Quality 的 `sourceType -> dataType` 映射、旧规范页文案和 API Standby 旧阶段文案已整改；高级配置与证据 adapter 的弱类型后续按模块逐项处理。

### 逐页审计表

| 页面 | 功能实现 | 铁律结论 | 主要发现 | 类型 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| `/` Home | 已实现 | 警告 | 有静态指标如 `12 Skills Online`、主链路数量；入口页不影响核心交易，但不应暗示真实运行数据 | 前端整改 | P3 |
| `/login` | 已实现 | 通过 | 已移除表单硬编码 `demo_admin` / `Demo@123456` 和预填提示，登录仍调用真实 auth API | 已整改 | P3 |
| `/account` | 已实现 | 通过 | 个人偏好已按后端白名单 `language/timezone/theme/market/notification/dashboard` 渲染选择器、开关和字段级对象表单；未知历史偏好不开放编辑，主流程不再使用 raw JSON textarea/`JSON.stringify` | 已整改 | P3 |
| `/users` | 已实现 | 警告 | 新建用户默认密码硬编码已移除；表单类型仍偏弱 | 前端整改 | P2 |
| `/roles` | 已实现 | 通过 | 用户授权使用选择器；权限配置已改为后端 catalog 驱动的分组 checkbox，不再手填权限编码 | 已整改 | P3 |
| `/overview` | 已实现 | 通过 | 使用真实 API 聚合闭环、Skill、数据源、报告、产品、组合、风控、回测；JSON 仅在详情证据中辅助 | 可接受 | P3 |
| `/data-quality` | 已实现 | 通过 | 真实数据质量页已结构化；详情趋势查询已改为使用后端 `latestQuality.dataType`，不再把 `sourceType` 当 `dataType` | 已整改 | P3 |
| `/data-ingestion` | 已实现 | 警告 | `mockUserBizId` 有用户选择器；任务参数、source/theme/product code 多为自由输入，高级配置可暂容忍 | 前端整改 + 后端模板能力 | P2 |
| `/product-risk` | 已实现 | 通过 | 产品选择来自列表，风险画像、行情、风控上下文结构化；产品级风控记录不足时依赖后端样本 | 可接受 | P3 |
| `/report-studio` | 已实现 | 通过 | 主题生成已使用真实 `theme-options` 搜索下拉；报告、门禁、计划、图表、证据结构化；JSON 仅调试折叠 | 可接受 | P3 |
| `/prompt-lab` | 已实现 | 通过 | Prompt 列表/模型/Skill/评估真实；预览变量已按后端变量 schema 渲染字段级输入，支持示例值，未再使用 raw JSON textarea | 已整改 | P3 |
| `/simulation` | 已实现 | 通过 | 产品、报告、订单、再平衡目标均用选择器；后端新增按组合查询结构化订单事件接口，前端订单事件和收益曲线已对齐明确 DTO，不再用 `Record<string, unknown>` 多 key 兜底作为主流程 | 已整改 | P3 |
| `/review-loop` | 已实现 | 通过 | 已接入 `review-loop/metadata` 结构化契约；回测策略、基准、原因码、评估场景走后端字典，Prompt 版本走真实列表选择器，参数/指标/metadata/scoreDetail 改为字段级输入 | 已整改 | P3 |
| `/risk-audit` | 已实现 | 通过 | 风控列表、原因聚合、详情结构化；detail JSON 作为证据辅助可接受 | 可接受 | P3 |
| `/config-center` | 已实现 | 通过 | 配置域入口和真实统计可达；无核心写操作绕过 | 可接受 | P3 |
| `/config-center/data-sources` | 已实现 | 警告 | 数据源、健康、质量维护真实；质量 detail、Prompt snapshot、步骤摘要仍依赖 JSON 证据；默认源类型/质量值需谨慎 | 高级配置可暂容忍 | P2 |
| `/config-center/data-source-discovery` | 已实现 | 通过 | AI 治理中枢读取 Skill、模型绑定、Prompt、评估、任务并给出待办；无手填 BizId | 可接受 | P3 |
| `/config-center/tasks` | 已实现 | 警告 | 任务类型/方向化任务编码/Skill 有选择器；高级参数 JSON、marketScope/dataTypes 等仍自由输入 | 后端模板能力 + 前端整改 | P2 |
| `/config-center/products` | 已实现 | 警告 | 产品管理写操作落位；画像主题关系 JSON、属性编码/值、行情 source/status 是自由字段 | 高级配置可暂容忍 | P2 |
| `/config-center/prompts` | 已实现 | 警告 | Prompt CRUD 和状态变更真实；变量、输出 Schema 仍是 JSON textarea | 后端 schema 能力 + 前端整改 | P2 |
| `/config-center/models` | 已实现 | 警告 | 模型 CRUD、状态、归档真实且高风险确认；模型配置/指标仍是 JSON textarea | 高级配置可暂容忍 | P2 |
| `/config-center/ai-skills` | 已实现 | 警告 | Skill CRUD、状态、复制版本真实；input/output schema、evaluation policy 仍是 JSON textarea | 高级配置可暂容忍 | P2 |
| `/config-center/model-skills` | 已实现 | 通过 | `modelBizId`、`skillBizId` 均通过可搜索对象选择器提交；场景用字典，版本漂移有提示 | 可接受 | P3 |
| `/config-center/actions` | 已实现 | 通过 | 仅做写操作归属矩阵和导航，不提供万能执行表单；符合操作落位原则 | 可接受 | P3 |
| `/development-rules` | 已实现 | 通过 | 页面文案已对齐当前共享文档口径，明确 `dzcom/docs_new` 为事实源和上下文瘦身要求 | 已整改 | P3 |
| `/ui-system` | 已实现 | 警告 | 规范展示页存在静态视觉样例；作为规范页可接受，但不应作为业务事实 | 可接受 | P3 |
| `/standby/api` | 已实现 | 通过 | 文案已更新为真实 API 已接入后的接口契约工作台，不再称没有业务 API/DTO | 已整改 | P3 |

### 后端 gap 摘要

- GAP-0110：已关闭。角色权限配置新增权限 catalog/list API，返回权限编码、名称、分组、说明、风险等级和状态，前端已改分组 checkbox。
- GAP-0111：已关闭。Review Loop 已新增结构化 metadata 契约，前端主流程不再使用 raw JSON textarea 或手填 Prompt Code/Version。
- GAP-0112：已关闭。Prompt 变量响应新增预览输入 schema，Prompt Lab 预览变量改为结构化变量编辑器。
- GAP-0113：已关闭。运行模型绑定保留为独立配置页 `/config-center/model-bindings`，与模型 Skill 绑定分工清晰。
- GAP-0104/GAP-0105 继续有效：高级 JSON 配置和剩余 `Record<string, unknown>` 应按模块收紧，不在核心路径长期存在；`/simulation` 核心订单事件、收益曲线和 feedback/backtest 保存/详情/生成 API 签名已整改。

### 前端整改摘要

- 登录页和用户创建页的硬编码默认密码已移除。
- `/data-quality` 中数据源展开时 `sourceType` 与后端 `dataType` 的映射已修正。
- `/development-rules`、`/standby/api` 的旧阶段文案已对齐当前 `docs_new` 口径。
- 优先收紧高级配置与证据 adapter 里的剩余 `Record<string, unknown>`；`/simulation`、`/review-loop` 主表单、`/prompt-lab` 预览主流程、feedback/backtest API 签名已完成结构化整改。
- 配置页 JSON textarea 暂归为高级配置，后续按具体 schema 逐步替换；Account 偏好主流程已完成结构化整改。

### 下次默认读取入口

继续逐页整改时默认只读：

1. `00-current-handoff.md`
2. 本文最新结论区
3. `02-frontend-laws.md`
4. `06-backend-gap-list.md` 对应 gap 行
5. 当前目标页面、实体 API/model/adapter、必要后端 Controller/DTO

## 历史归档区

- 本文创建于 2026-06-28，作为整合后第一次前端页面审计快照。后续关闭某个页面问题时，只在最新结论区更新摘要，详细过程不继续塞入对话上下文。
- 2026-06-28：关闭 `/review-loop` P1 铁律问题。后端新增 `POST /api/review-loop/metadata`，前端改为策略/Prompt/原因/场景选择器和字段级输入，旧 JSON textarea 与手填 Prompt Code/Version 从主流程移除。
- 2026-06-28：关闭 `/prompt-lab` P2 预览变量问题。后端 Prompt 变量响应新增预览输入 schema，前端改为字段级变量表单和示例填充按钮，旧 raw JSON textarea 从主流程移除。
- 2026-06-28：关闭运行模型绑定页面覆盖问题。前端新增 `/config-center/model-bindings`、`entities/ai-model-binding` 和配置入口，后端 `AiModelBindingController` 纳入统一契约测试。
- 2026-06-28：关闭低风险前端漂移项。`/data-quality` 详情趋势使用 `latestQuality.dataType`，`/development-rules` 和 `/standby/api` 文案对齐当前共享文档和真实 API 接入口径。
- 2026-06-28：关闭 `/account` 偏好 JSON P3 问题。前端按后端偏好白名单渲染结构化控件，`dashboard` 使用字段级对象表单，旧 Key/Value textarea 主路径移除；验证通过前端 type-check/build 和 `/account` 路由冒烟。
- 2026-06-28：关闭 `/simulation` 订单事件弱类型 P2 问题。后端新增 `POST /api/mock/portfolios/orders/events/by-portfolio`，返回组合订单事件和订单摘要字段；前端收益曲线改读 `valuations` 响应，订单事件改用明确 DTO，旧 raw 订单事件折叠区移除。验证通过后端测试、前端 type-check/build 和 `/simulation` 路由冒烟。
- 2026-06-28：继续收紧 GAP-0105。前端 feedback/backtest 保存、详情、生成接口改用后端 Request DTO 对应类型；Review Loop 表单 `status/feedbackAction` 收窄到枚举联合类型。验证通过前端 type-check/build 和 `/review-loop` 路由冒烟。
