# DZCOM 技术架构基线

本文档定义当前项目的新技术架构基线，用于约束后续模块设计、目录组织、基础设施选择和系统演进路径。

## 1. 架构目标

- 以模块化单体为起点，降低初期协作与交付成本。
- 以有界上下文为核心组织代码，为后续服务拆分保留边界。
- 以稳定的数据结构和清晰的业务分层支撑后续 AI、风控和交易扩展。

## 2. 当前技术基线

- Java 17
- Spring Boot 4
- Spring Web MVC
- MyBatis-Plus 3.5
- Flyway
- MySQL 8
- Redis
- SpringDoc OpenAPI
- H2 用于基础测试上下文

说明：

- 当前仓库保留基础工程、配置、通用组件和迁移脚手架。
- 历史用户业务实现已清理，不再作为架构参考。

## 3. 架构形态

### 3.1 第一阶段

- 采用模块化单体。
- 所有能力仍在一个部署单元内交付。
- 通过包边界、领域模型和应用服务划分上下文。

### 3.2 后续演进

当以下条件成立时，再考虑服务拆分：

- 单一业务域开发节奏明显独立。
- 数据规模、流量或安全边界要求单独部署。
- 外部集成复杂度已经超出模块化单体维护成本。

## 4. 有界上下文划分

建议的业务上下文如下：

- `account`：用户、身份、认证、偏好、风险画像。
- `product`：产品目录、分类、产品元数据。
- `market`：行情快照、历史数据、数据源接入。
- `portfolio`：投资组合、持仓、估值。
- `order`：订单、执行、回执、流水。
- `ai`：推荐信号、分析结果、策略建议。
- `risk`：风控规则、命中记录、审计、通知。

## 5. 代码组织约束

新增业务模块先按 DDD 领域层级组织，再按结构职责和上下文拆分，示例：

```text
src/main/java/com/example/dzcom/
├── domain/
│   ├── model/account/
│   ├── repository/account/
│   ├── service/account/
│   └── enums/account/
├── application/
│   ├── service/account/
│   ├── command/account/
│   ├── dto/account/
│   └── assembler/account/
├── infrastructure/
│   ├── persistence/entity/account/
│   ├── persistence/repository/account/
│   ├── security/account/
│   └── session/account/
├── interfaces/
│   ├── controller/account/
│   ├── request/account/
│   └── response/account/
└── common/
```

规则：

- 顶层目录表达 DDD 领域层级，不得省略。
- `domain/model/<context>` 放领域对象和业务行为。
- `domain/repository/<context>` 定义仓储端口，`infrastructure/persistence/repository/<context>` 提供实现。
- `application/service/<context>` 负责编排用例和事务。
- `interfaces/controller/<context>` 与 `interfaces/request/<context>` 负责 API 协议适配。
- `common` 目录只存真正跨域复用的能力。

## 6. 数据与存储架构

### 6.1 关系型数据

- MySQL 作为核心事务库。
- 通过 Flyway 管理结构演进。
- 用户、产品、组合、订单、审计等强一致性数据优先落在 MySQL。
- 表间仅保存逻辑业务 ID，不建立数据库外键；跨领域一致性通过应用服务和领域事件维护。
- 持久化 Entity 使用标量逻辑 ID，禁止通过对象图或级联操作耦合不同领域。
- 每个持久化 Entity 对应独立 Mapper Java 接口和同名 Mapper XML，所有 SQL 统一维护在 XML。

### 6.2 缓存与会话

- Redis 用于缓存、会话、短期状态和轻量级分布式协调。
- 任何 Redis Key 必须有命名规则和过期策略。

### 6.3 测试数据库

- 默认使用 H2 启动最小测试上下文。
- 真正依赖方言或迁移行为的能力，需要补充更接近生产环境的集成验证。

## 7. API 架构

- 对外以 REST API 为主。
- 使用 OpenAPI 维护接口契约。
- 统一返回 `Result<T>`。
- 参数校验和异常处理作为全局基础设施能力提供。

## 8. 数据流与调用原则

### 8.1 同步链路

- 前端或调用方进入 Controller。
- Application Service 编排用例。
- Domain 完成业务判断和状态变化。
- Repository Interface 由 Infrastructure 实现。

### 8.2 异步链路

- 后续需要事件驱动时，优先围绕订单、行情、AI 信号和风控事件引入。
- 在没有明确异步收益前，不提前引入复杂消息架构。

## 9. 安全与审计架构要求

- 鉴权方案单独设计，不能隐式散落在业务代码中。
- 涉及资产、订单、身份和风控的能力必须有审计日志。
- 明文凭据不进入仓库，统一通过环境变量或部署平台配置注入。

## 10. 当前仓库状态说明

- 当前代码基线不包含可直接复用的业务模块。
- 后续开发从“文档基线 -> 数据结构 -> 垂直切片实现”重新推进。
- 若新实现与本文档冲突，以先更新本文档为前提。
