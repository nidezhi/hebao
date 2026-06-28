# 06 Backend Gap List

生成日期：2026-06-28

当前文件记录当前端铁律无法满足时，后端必须补齐的接口、DTO、状态、权限或数据能力。这里不是抱怨清单，是跨仓排期入口。

## 最新结论区

### 使用规则

- 当前端需要假数据、硬编码、手填 BizId、原始 JSON 展示才能完成页面时，必须停止绕过并登记 gap。
- 每个 gap 必须有前端阻塞点、需要的后端能力、验收方式和优先级。
- gap 关闭后，只保留关闭摘要，详细过程迁移到历史归档区或对应任务 handoff。

### 首轮 gap list 模板

| Gap ID | 优先级 | 状态 | 前端阻塞点 | 需要后端补齐 | 涉及对象/API | 验收方式 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GAP-0001 | P1 | 待确认 | 对象选择器缺少可搜索数据源 | 提供列表/搜索/分页/排序接口，返回 `bizId/displayName/status/summary` | 待任务确认 | 前端不手填 id，可搜索选择并提交真实接口 | 模板项 |
| GAP-0002 | P1 | 待确认 | 关联 DTO 只有裸 id，页面无法展示业务语义 | DTO 增加 display/summary 字段或提供查询接口 | 待任务确认 | 页面无需额外硬编码映射即可展示关联对象 | 模板项 |
| GAP-0003 | P1 | 待确认 | 状态/枚举/拒绝原因无法结构化展示 | 返回稳定枚举、中文/展示标签或字典接口、错误上下文 | 待任务确认 | 前端用字典/字段展示状态，不解析自由文本 | 模板项 |
| GAP-0004 | P2 | 待确认 | 投资报告/复盘只能展示原始 JSON | 提供结构化摘要、指标、证据、风险、建议字段 | 待任务确认 | 页面主体验不依赖 JSONPreview | 模板项 |

### 首轮联调 gap 状态（2026-06-28）

| Gap ID | 优先级 | 状态 | 前端阻塞点 | 需要后端补齐 | 涉及对象/API | 验收方式 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| GAP-0101 | P1 | 已关闭 | 产品工作台需要最近净值/行情时间/来源/质量分，后端产品 DTO 原先未返回 | `ProductResponse` 增加 `latestNav/latestQuoteTime/sourceCode/dataQualityScore`，列表和详情从真实行情与画像组装 | Product 列表/详情 | 后端测试、前端 type-check/build 通过 | 2026-06-28 已完成 |
| GAP-0102 | P1 | 已关闭 | 模型 Skill 绑定页面可见绑定仍指向旧 `mock-v1`，与当前 ACTIVE 模型不一致 | 新增迁移对齐启用绑定到 ACTIVE 模型版本 | `aiw_ai_model_skill_binding` / `aiw_ai_model` | 数据核验 `drift_count=0` | 2026-06-28 已完成 |
| GAP-0103 | P1 | 已关闭 | 报告生成等核心路径仍可能让用户手填 `themeCode`，不满足对象选择器铁律 | 提供主题/行业/资产分类的可搜索选择接口或稳定字典接口，返回 `code/displayName/status/summary` | InvestmentReport / ProductTheme | 前端可搜索选择主题并提交真实 code，无需手填 | 2026-06-28 已完成：报告工作台改为真实主题选择器 |
| GAP-0104 | P2 | 新增 | 高级配置页仍存在部分 JSON 文本输入/展示；核心路径不能依赖原始 JSON | 对核心闭环参数、门禁、风控原因提供结构化字段和字典；高级配置可保留 JSON 但必须与核心体验分离 | TaskConfig / ReviewLoop / RiskAudit | 核心页面主流程不以 JSONPreview/textarea 为主要体验 | 待按模块拆分 |
| GAP-0105 | P2 | 新增 | 部分前端 API 类型仍使用 `Record<string, unknown>`，长期会削弱契约校验 | 后端按功能补明确 DTO 字段；前端同步收紧类型和 adapter | 多模块 API | 目标模块 type-check 能在字段变更时暴露契约问题 | 逐功能处理 |
| GAP-0106 | P2 | 新增 | 首轮未执行浏览器真实路由冒烟，只完成 type-check/build 和后端测试 | 为核心路由建立最小真实链路冒烟脚本或手动 Playwright 检查口径 | Overview / ReportStudio / Simulation / ReviewLoop | 登录后至少一个真实闭环路由加载真实数据并截图/断言通过 | 下轮联调优先 |
| GAP-0107 | P1 | 已关闭 | 新闻数据质量分最高仅 `0.2500`，会限制报告可信度和可交易动作 | 优化 NEWS 采集质量评分、去重、来源等级和样本覆盖，明确低质量新闻的结构化原因 | DataQuality / News / Report | NEWS 快照质量最高 `0.7500`，平均 `0.6875`；detail 含 `qualityPolicy/qualityReasons` | 2026-06-28 已完成 |
| GAP-0108 | P1 | 已关闭 | 数据源 `342` 条但健康记录仅 `5` 条，启用数据源过多且健康覆盖不足 | 提供数据源启用分层、健康覆盖率、候选/正式分离和批量治理任务 | DataSource / Health | 启用数据源健康覆盖 `330/330`；看板可展示 `PENDING_HEALTH_CHECK` | 2026-06-28 已完成 |
| GAP-0109 | P2 | 已关闭 | 风控检查样本仅 `1` 条，难以支撑风控审计页和闭环拒绝原因验收 | 增加核心交易/报告/组合路径的风控检查记录和结构化拒绝原因 | RiskAudit / MockTrade / Report | 风控检查 `25` 条，覆盖 `REPORT/ORDER/PORTFOLIO` 和 `PASS/REVIEW/REJECT` | 2026-06-28 已完成 |

### Gap 记录模板

```text
Gap ID:
优先级: P0/P1/P2/P3
状态: 新增/后端处理中/前端验证中/已关闭/暂缓
发现日期:
发现页面:
前端阻塞点:
不能绕过的铁律:
需要后端补齐:
建议 API/DTO:
验收方式:
关闭日期:
关闭摘要:
```

## 历史归档区

### 2026-06-28 已关闭

- GAP-0101：产品 API 摘要字段已补齐，前端无需 mock/硬编码最近行情与质量分。
- GAP-0102：模型 Skill 绑定版本漂移已通过 V38 修复，启用绑定与 ACTIVE 模型版本一致。
- GAP-0103：新增 `POST /api/investment/tasks/theme-options`，从真实主题快照派生可搜索主题选项；报告工作台生成报告时不再手填 `themeCode`。
- GAP-0107：NEWS 采集质量改为按主题关键词覆盖和公开源真实可达样本计算，历史真实 NEWS 快照最高分提升到 `0.7500`，并补 `qualityPolicy/qualityReasons`。
- GAP-0108：V40 为启用但无健康记录的数据源补健康状态，数据源治理服务新增 `PENDING_HEALTH_CHECK` 展示等级；启用源健康覆盖为 `330/330`。
- GAP-0109：风控审计新增通用 `recordCheck`，报告/订单/组合路径沉淀 PASS/REVIEW/REJECT 样本；真实库风控检查为 `25` 条。

### 2026-06-28 数据审计已修复

- 历史投资报告 `theme_code=''` 已通过 V39 统一为 `NULL`，后续新报告也会标准化空主题。
- 陈旧 `RUNNING` 闭环运行已通过 V39 转为 `FAILED/BLOCK`，并补充 `RUNNING_TIMEOUT` 步骤供前端结构化展示。
