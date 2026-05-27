# DZCOM 项目文档

## 📋 项目概述

**DZCOM** 是一个基于 Spring Boot 4.0.5 构建的企业级 Java 应用项目，采用 Java 17 开发。项目采用了**整洁架构（Clean Architecture）**设计模式，将代码按照职责分层组织，具有良好的可维护性和可扩展性。

- **项目名称**: dzcom
- **Group ID**: com.example
- **Artifact ID**: dzcom
- **版本**: 0.0.1-SNAPSHOT
- **Java 版本**: 17
- **Spring Boot 版本**: 4.0.5

---

## 🏗️ 技术栈

### 核心框架
- **Spring Boot 4.0.5** - 应用框架
- **Spring Web MVC** - Web 层框架
- **Spring JDBC** - 数据库访问
- **Spring Data Redis** - Redis 缓存支持
- **Spring Elasticsearch** - 搜索引擎集成
- **Spring Mail** - 邮件服务
- **Spring REST Client** - RESTful HTTP 客户端
- **Spring Web Services** - Web 服务支持

### 数据库支持
- **MySQL** - MySQL 数据库驱动 (mysql-connector-j)
- **SQL Server** - Microsoft SQL Server 驱动 (mssql-jdbc)
- **H2** - 内存数据库（用于测试/开发）

### API 文档
- **SpringDoc OpenAPI 3.0.2** - Swagger UI 集成，提供 API 文档和测试界面

### 开发工具
- **Spring Boot DevTools** - 开发时热重载工具
- **Maven** - 项目构建工具
- **JUnit 5** - 单元测试框架

---

## 📁 项目结构

项目采用**四层架构**设计，遵循领域驱动设计（DDD）原则：

```
dzcom/
├── src/main/java/com/example/dzcom/
│   ├── DzcomApplication.java              # 应用启动类
│   │
│   ├── application/                       # 应用层
│   │   ├── dto/                          # 数据传输对象
│   │   ├── schedule/                     # 定时任务
│   │   └── service/                      # 应用服务
│   │
│   ├── domain/                           # 领域层（核心业务逻辑）
│   │   ├── model/                        # 领域模型/实体
│   │   ├── repository/                   # 领域仓储接口
│   │   └── service/                      # 领域服务
│   │
│   ├── infrastructure/                   # 基础设施层
│   │   ├── config/                       # 配置类
│   │   ├── dao/                          # 数据访问对象
│   │   │   ├── entity/                   # 数据库实体
│   │   │   ├── mapper/                   # MyBatis Mapper 接口
│   │   │   └── repository/               # 仓储实现
│   │   └── utils/                        # 工具类
│   │
│   └── interfaces/                       # 接口层（用户界面）
│       ├── controller/                   # REST 控制器
│       └── vo/                           # 视图对象
│
├── src/main/resources/
│   ├── static/                           # 静态资源
│   ├── templates/                        # 模板文件
│   ├── application-dev.yaml             # 开发环境配置
│   ├── application-local.yaml           # 本地环境配置
│   └── application-online.yaml          # 生产环境配置
│
├── src/test/java/com/example/dzcom/
│   └── DzcomApplicationTests.java       # 应用测试类
│
├── pom.xml                               # Maven 配置文件
└── .mvn/wrapper/                         # Maven Wrapper
```

---

## 🏛️ 架构说明

### 1. **应用层 (application)**
- **职责**: 协调领域对象完成具体的业务用例
- **dto/**: 定义数据传输对象，用于跨层或跨服务传递数据
- **schedule/**: 定义定时任务和调度逻辑
- **service/**: 应用服务，编排领域服务完成业务流程

### 2. **领域层 (domain)**
- **职责**: 核心业务逻辑，不依赖任何外部框架
- **model/**: 领域实体和业务对象
- **repository/**: 仓储接口定义（由基础设施层实现）
- **service/**: 领域服务，封装复杂业务规则

### 3. **基础设施层 (infrastructure)**
- **职责**: 提供技术实现，如数据库访问、外部服务调用等
- **config/**: Spring 配置类
- **dao/**: 数据访问实现
  - **entity/**: JPA/MyBatis 实体类（与数据库表映射）
  - **mapper/**: MyBatis Mapper 接口
  - **repository/**: 领域仓储接口的具体实现
- **utils/**: 通用工具类

### 4. **接口层 (interfaces)**
- **职责**: 对外暴露 API 接口
- **controller/**: RESTful API 控制器
- **vo/**: 视图对象，用于 API 请求/响应

---

## ⚙️ 环境配置

项目支持多环境配置，通过 Spring Profile 管理：

### 可用环境
- **local**: 本地开发环境
- **dev**: 开发服务器环境
- **online**: 生产环境

### 配置文件位置
- `src/main/resources/application-local.yaml`
- `src/main/resources/application-dev.yaml`
- `src/main/resources/application-online.yaml`

### 激活指定环境
```bash
# 方式1: 在 application.yaml 中配置
spring:
  profiles:
    active: dev

# 方式2: 启动时指定
java -jar dzcom.jar --spring.profiles.active=dev

# 方式3: 环境变量
export SPRING_PROFILES_ACTIVE=dev
```

---

## 🚀 快速开始

### 前置要求
- **JDK 17+**
- **Maven 3.6+**
- **数据库**（MySQL / SQL Server / H2，根据需求选择）
- **Redis**（如需缓存功能）
- **Elasticsearch**（如需搜索功能）

### 构建项目
```bash
# 进入项目目录
cd /Users/daniel/IdeaProjects/dzcom

# 编译打包
./mvnw clean package

# 或者使用系统 Maven
mvn clean package
```

### 运行应用
```bash
# 方式1: 使用 Maven 运行（开发模式）
./mvnw spring-boot:run

# 方式2: 运行 JAR 包
java -jar target/dzcom-0.0.1-SNAPSHOT.jar

# 方式3: 指定环境运行
java -jar target/dzcom-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### 访问应用
- **应用地址**: http://localhost:8080
- **API 文档**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

---

## 🔧 开发指南

### 添加新的业务功能

#### 1. 创建领域模型
在 `domain/model/` 中定义实体类：
```java
package com.example.dzcom.domain.model;

public class User {
    private Long id;
    private String username;
    private String email;
    // getters and setters
}
```

#### 2. 定义仓储接口
在 `domain/repository/` 中定义接口：
```java
package com.example.dzcom.domain.repository;

public interface UserRepository {
    User findById(Long id);
    User save(User user);
    void deleteById(Long id);
}
```

#### 3. 实现数据访问层
在 `infrastructure/dao/entity/` 创建 JPA 实体：
```java
package com.example.dzcom.infrastructure.dao.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    // getters and setters
}
```

在 `infrastructure/dao/repository/` 实现仓储：
```java
package com.example.dzcom.infrastructure.dao.repository;

@Repository
public class UserRepositoryImpl implements UserRepository {
    @Autowired
    private UserJpaRepository userJpaRepository;
    
    // 实现方法
}
```

#### 4. 创建应用服务
在 `application/service/` 中编排业务：
```java
package com.example.dzcom.application.service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    public User createUser(User user) {
        // 业务逻辑
        return userRepository.save(user);
    }
}
```

#### 5. 暴露 REST API
在 `interfaces/controller/` 中创建控制器：
```java
package com.example.dzcom.interfaces.controller;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.createUser(user));
    }
}
```

### 数据库配置示例

#### MySQL 配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dzcom?useSSL=false&serverTimezone=UTC
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

#### SQL Server 配置
```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=dzcom
    username: sa
    password: your_password
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
```

#### Redis 配置
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
```

#### Elasticsearch 配置
```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
```

---

## 🧪 测试

### 运行所有测试
```bash
./mvnw test
```

### 运行单个测试类
```bash
./mvnw test -Dtest=DzcomApplicationTests
```

### 编写测试
测试类位于 `src/test/java/com/example/dzcom/`，使用 JUnit 5 和 Spring Boot Test。

---

## 📦 部署

### 打包
```bash
./mvnw clean package -DskipTests
```

生成的 JAR 文件位于：`target/dzcom-0.0.1-SNAPSHOT.jar`

### 运行生产环境
```bash
java -jar target/dzcom-0.0.1-SNAPSHOT.jar --spring.profiles.active=online
```

### Docker 部署（可选）
创建 `Dockerfile`:
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/dzcom-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

构建和运行：
```bash
docker build -t dzcom .
docker run -p 8080:8080 dzcom
```

---

## 📝 API 文档

项目集成了 **SpringDoc OpenAPI**，启动应用后访问：

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API 规范**: http://localhost:8080/v3/api-docs

### 添加 API 文档注解
```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户相关操作")
public class UserController {
    
    @Operation(summary = "创建用户", description = "创建新用户")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功创建"),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // ...
    }
}
```

---

## 🔍 项目特性

### ✅ 已集成的功能
- [x] Spring Boot 4.0.5 最新框架
- [x] 多层架构设计（DDD）
- [x] RESTful API 支持
- [x] 多数据库支持（MySQL, SQL Server, H2）
- [x] Redis 缓存
- [x] Elasticsearch 搜索
- [x] 邮件服务
- [x] OpenAPI/Swagger 文档
- [x] 多环境配置
- [x] 热重载开发工具
- [x] 单元测试框架

### 📌 待开发的功能
- [ ] 安全认证（Spring Security）
- [ ] JWT Token 认证
- [ ] 全局异常处理
- [ ] 统一响应格式
- [ ] 日志配置
- [ ] 数据库迁移工具（Flyway/Liquibase）
- [ ] 监控和健康检查（Actuator）
- [ ] Docker 容器化
- [ ] CI/CD 配置

---

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 📄 许可证

本项目暂未指定许可证。

---

## 👥 开发团队

项目开发和维护团队。

---

## 📞 联系方式

如有问题或建议，请联系开发团队。

---

## 🔄 更新日志

### v0.0.1-SNAPSHOT (2026-05-27)
- 初始项目搭建
- 基础架构设计
- 集成 Spring Boot 4.0.5
- 配置多环境支持
- 集成 Swagger 文档

---

**最后更新**: 2026-05-27  
**文档版本**: 1.0.0
