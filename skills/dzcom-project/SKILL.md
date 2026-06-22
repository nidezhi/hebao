---
name: dzcom-project
description: DZCOM 项目专用交付技能。规划、实现、重构、评审或扩展本项目时使用，确保开发遵循 docs_new 中的开发规范、项目需求、技术架构、初版数据库设计和开发节奏。
---

# DZCOM 项目技能

## 使用目标

修改 `dzcom` 代码前必须使用本技能。将 `docs_new` 下的 Markdown 文档视为项目权威基线；除非新需求明确要求，否则不得恢复已移除的旧用户业务实现。

当前项目的投资平台目标是：监控投资理财产品、行情、资讯、公告、研报和监管数据，通过数据质量评估、产品风险架构、AI 提示词版本和 Mock 交易闭环，形成可解释、可前端展示、可持续成长的投资辅助平台。禁止把低质量样本包装成“正式投资分析”。

## 开始流程

1. 阅读 [references/doc-map.md](references/doc-map.md)。
2. 只打开当前任务相关的 `../../docs_new/` 文档。
3. 将任务归属到一个有界上下文和一个交付阶段。
4. 每次只实现一条完整的业务链路。
5. 需求、架构、数据库或开发节奏发生变化时，在同一轮修改对应文档。
6. 投资分析、AI、数据源、产品、交易或风控相关任务，必须先阅读 `../../docs_new/11-investment-platform-closed-loop-plan.md`，确认当前变更落在哪个闭环节点。

## 工作规则

- 代码必须先按 DDD 领域层级划分为 `domain`、`application`、`infrastructure`、`interfaces` 和 `common`。
- 每个领域层级内部再按结构职责和业务域组织，例如 `domain/model/account`、`application/service/account`、`infrastructure/persistence/entity/account`、`interfaces/controller/account`。
- 禁止丢失领域层级直接使用顶层 `entity/account`、`service/account`、`controller/account`，也禁止同一职责存在多套目录。
- 类、公共方法、关键字段、业务规则和复杂流程必须提供详尽、准确的中文注释。
- 数据对象和参数较多的对象创建优先使用 Builder，禁止长参数构造器在业务代码中扩散。
- 集合筛选、转换、分组、去重和汇总优先使用 Stream；有副作用、事务顺序或提前退出要求时保留清晰的命令式代码。
- 默认使用 MyBatis Mapper XML + Flyway；每个持久化实体必须具有独立 Mapper Java 接口和同名 XML。
- 禁止使用 JPA、EntityManager、JPQL、QueryWrapper、BaseMapper 内置 CRUD 或在 Java 中编写 SQL。
- 当前仓库是新的业务基线，业务模块必须依据权威文档重新实现。
- 投资平台开发必须遵循“可信数据先于 AI 输出”的原则。没有达到质量阈值的数据，只能输出数据缺口、风险提示和补数任务，不能输出看似确定的投资方案。
- 任何投资分析改动都必须具备前端可见出口，包括列表、详情、图表数据、提示词预览、质量评分、审计记录或 Mock 交易结果。
- 投资报告不是终点。每次报告必须能够进入提示词版本、投资方案、Mock 组合、收益回测、风控审计和反馈学习链路。
- 正式大模型调用暂缓；在产品池、数据质量、提示词版本和 Mock 交易闭环未达标前，只允许使用本地规则或 Mock Provider 做链路验证，并必须标记数据可信等级。

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
- 投资平台闭环重构、数据源治理、产品风险架构、AI 提示词版本、Mock 交易和自动化成长：
  阅读 `../../docs_new/11-investment-platform-closed-loop-plan.md`。

## 交付要求

- 每个变更集优先只处理一个有界上下文或一个用例。
- 业务边界变化时同步更新代码、测试和文档。
- 结束任务前运行范围最小且有效的验证。
- 发现代码与文档冲突时，先修正项目基线再继续实现。
- 投资相关交付必须说明当前闭环位置、前端展示方式、数据质量阈值、失败降级方式和下一步自动化反馈入口。

## 参考入口

- `references/doc-map.md`：用于判断当前任务需要读取哪些项目文档。
