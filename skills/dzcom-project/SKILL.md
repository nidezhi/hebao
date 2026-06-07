---
name: dzcom-project
description: DZCOM 项目专用交付技能。规划、实现、重构、评审或扩展本项目时使用，确保开发遵循 docs_new 中的开发规范、项目需求、技术架构、初版数据库设计和开发节奏。
---

# DZCOM 项目技能

## 使用目标

修改 `dzcom` 代码前必须使用本技能。将 `docs_new` 下的 Markdown 文档视为项目权威基线；除非新需求明确要求，否则不得恢复已移除的旧用户业务实现。

## 开始流程

1. 阅读 [references/doc-map.md](references/doc-map.md)。
2. 只打开当前任务相关的 `../../docs_new/` 文档。
3. 将任务归属到一个有界上下文和一个交付阶段。
4. 每次只实现一条完整的业务链路。
5. 需求、架构、数据库或开发节奏发生变化时，在同一轮修改对应文档。

## 工作规则

- 将业务规则放在 `domain/<context>`。
- 将用例编排放在 `application/<context>`。
- 将 HTTP 接口和传输对象放在 `interfaces/<context>`。
- 将持久化和外部集成放在 `infrastructure/<context>`。
- 默认使用 Spring Data JPA + Flyway；变更持久化方案前必须先更新架构文档。
- 当前仓库是新的业务基线，业务模块必须依据权威文档重新实现。

## 文档选择

- 用户、认证、会话和用户偏好开发：
  同时使用 `$dzcom-account`。
- 编码规范、分层、测试、安全和 AI 使用边界：
  阅读 `../../docs_new/01-development-guidelines.md`。
- 产品范围、MVP、角色和业务域职责：
  阅读 `../../docs_new/02-project-requirements.md`。
- 模块设计、包结构、存储和集成边界：
  阅读 `../../docs_new/03-technical-architecture.md`。
- 表结构、关系、索引和迁移计划：
  阅读 `../../docs_new/04-initial-database-design.md`。
- 迭代顺序、任务拆分和完成标准：
  阅读 `../../docs_new/05-development-rhythm.md`。

## 交付要求

- 每个变更集优先只处理一个有界上下文或一个用例。
- 业务边界变化时同步更新代码、测试和文档。
- 结束任务前运行范围最小且有效的验证。
- 发现代码与文档冲突时，先修正项目基线再继续实现。

## 参考入口

- `references/doc-map.md`：用于判断当前任务需要读取哪些项目文档。
