
## dzcom 项目（已重置 README）

此 README 已重置为项目首页说明文档。团队新的、重要的开发规范已集中到代码仓库内：

- 主要规范（中文）存放在：`src/main/java/com/example/dzcom/docs_new/DevSkillsGuidelines.java`，请将其作为权威参考（包含 DDD、代码规范与 AI 代码生成规范）。

快速查看该规范：在 IDE 中打开文件 `src/main/java/com/example/dzcom/docs_new/DevSkillsGuidelines.java`。

---

## 快速开始

前置条件：

- JDK 17+
- Maven 3.6+

构建：

```bash
cd /Users/daniel/IdeaProjects/dzcom
./mvnw clean package
```

运行（开发模式）：

```bash
./mvnw spring-boot:run
```

运行 Jar：

```bash
java -jar target/dzcom-0.0.1-SNAPSHOT.jar
```

如需指定 Spring Profile：

```bash
java -jar target/dzcom-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

---

## 项目结构（简要）

代码按 DDD 分层组织：`application`、`domain`、`infrastructure`、`interfaces`。完整目录请查看源码树。

重要文件：

- `src/main/java/com/example/dzcom/DzcomApplication.java` - 启动类
- `src/main/java/com/example/dzcom/docs_new/DevSkillsGuidelines.java` - 团队开发规范（权威参考）

---

## 关于规范和 AI 生成代码

团队要求：

- 所有新代码遵循 `DevSkillsGuidelines.java` 中的规范。
- 任何 AI 生成的代码必须在 PR 中说明使用的提示（prompt）、模型与人工审核步骤，并附带相应测试。

---

## 测试

运行测试：

```bash
./mvnw test
```

---

## 贡献与变更规范文档

如需更新规范：提交 PR 修改 `src/main/java/com/example/dzcom/docs_new/DevSkillsGuidelines.java`，并在 PR 描述中说明变更理由与迁移步骤。

---

**最后更新**: 2026-05-27
