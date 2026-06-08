# dzcom

DZCOM 当前处于“重新建立业务基线”的阶段。仓库保留基础工程、通用组件、数据库迁移脚手架和项目文档，历史用户业务实现已清空，不再作为后续开发模板。

## 当前基线

- 技术框架：Spring Boot 4 + Java 17
- 存储基线：MySQL + Redis + Flyway + JPA
- 文档基线：`docs_new/*.md`
- 项目 skill：`skills/dzcom-project`

## 权威文档

- [开发规范](docs_new/01-development-guidelines.md)
- [项目需求](docs_new/02-project-requirements.md)
- [技术架构](docs_new/03-technical-architecture.md)
- [初版数据库](docs_new/04-initial-database-design.md)
- [开发节奏](docs_new/05-development-rhythm.md)

以上五份文档是项目当前唯一的业务与工程权威来源：

- 开发规范决定代码质量、分层、安全、测试和 AI 协作要求。
- 项目需求决定产品目标、业务范围、MVP 和阶段边界。
- 技术架构决定模块边界、包结构、技术选型和演进方向。
- 初版数据库决定数据模型、表关系、索引和迁移基线。
- 开发节奏决定迭代顺序、任务拆分和完成标准。

发生冲突时，先修正文档形成一致决策，再修改代码。历史实现、旧说明和数据库脚本中的注释不能覆盖上述文档。

## 项目期待

DZCOM 要建设为一个可持续演进的 AI 理财平台后端，而不是一组围绕数据库表生成的 CRUD 示例。项目应做到：

- 用清晰的业务域承载用户、产品、市场、组合、订单、AI、风控和审计能力。
- 先完成可靠的模块化单体和 MVP 主链路，再依据真实规模拆分服务。
- AI 输出必须可解释、可追溯，默认作为投资建议，不直接驱动真实资金操作。
- 交易、资产、身份和风控链路必须具备安全、审计、幂等和异常处理能力。
- 每次按一个明确用例交付完整链路，并保持需求、架构、数据库、代码和测试一致。

## 当前仓库状态

- 账户域已具备注册、认证、用户资料、偏好和风险画像基础能力。
- 产品中心已具备产品目录管理、扩展属性、分页查询和详情查询。
- 市场数据域已具备 OHLCV 行情写入修正、最新行情和历史行情查询。
- 组合、订单、AI、风控、通知和审计仍按 `docs_new/05-development-rhythm.md`
  规定的迭代顺序逐步实现，不直接恢复旧示例代码。

## 本地验证

本地启动前，在项目根目录创建 Git 已忽略的
`config/application-secrets.yaml`：

```yaml
spring:
  datasource:
    username: root
    password: 本机数据库密码
```

也可以通过 `DB_USERNAME`、`DB_PASSWORD` 和 `DB_URL` 环境变量覆盖。

```bash
./mvnw test
```

使用 `local` 或 `dev` profile 启动后，可访问：

- Knife4j：`http://localhost:8511/doc.html`
- Swagger UI：`http://localhost:8511/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8511/v3/api-docs`
