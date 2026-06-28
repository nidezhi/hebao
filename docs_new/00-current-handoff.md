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
- 初始阶段未修改业务代码。

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
