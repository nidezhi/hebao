# dzcom

DZCOM 当前处于“重新建立业务基线”的阶段。仓库保留基础工程、通用组件、数据库迁移脚手架和项目文档，历史用户业务实现已清空，不再作为后续开发模板。

## 当前基线

- 技术框架：Spring Boot 4 + Java 17
- 存储基线：MySQL + Redis + Flyway + JPA
- 文档基线：`docs_new/*.md`
- 项目 skill：`skills/dzcom-project`

## 必读文档

- [开发规范](docs_new/01-development-guidelines.md)
- [项目需求](docs_new/02-project-requirements.md)
- [技术架构](docs_new/03-technical-architecture.md)
- [初版数据库](docs_new/04-initial-database-design.md)
- [开发节奏](docs_new/05-development-rhythm.md)

## 当前仓库状态

- 旧用户业务纵切已移除。
- 当前代码更偏向基础工程骨架，而不是完整业务实现。
- 后续开发按有界上下文逐步重建，不直接恢复旧示例代码。

## 本地验证

```bash
./mvnw test
```
