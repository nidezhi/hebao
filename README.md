# AI 理财平台 (dzcom)

基于 DDD 架构的 AI 驱动理财平台，提供智能投资组合管理、市场数据分析与个性化理财建议。

## 📋 项目概述

**技术栈**: Spring Boot 4.0.5 + Java 17 + MySQL + Redis + MyBatis-Plus  
**架构模式**: 领域驱动设计 (DDD) 分层架构  
**认证方式**: Cookie + JWT + Redis 混合方案

### 核心功能
- 🔐 **用户认证体系**: HttpOnly Cookie + JWT Token + Redis 会话管理
- 📊 **投资组合管理**: 创建、查询、管理个人投资组合
- 📈 **市场数据集成**: 实时市场行情与历史数据分析
- 🤖 **AI 智能推荐**: 基于用户风险偏好的个性化理财建议
- 📱 **API 文档**: Knife4j 增强的 Swagger UI

---

## 🏗️ 项目结构

```
src/main/java/com/example/dzcom/
├── common/                          # 通用模块
│   ├── annotation/                  # 自定义注解 (@RequireLogin, @IgnoreLogin)
│   ├── constant/                    # 常量定义 (RedisKey, Cookie)
│   ├── enums/                       # 枚举 (ResultCode, UserStatus)
│   ├── exception/                   # 异常处理 (BusinessException, GlobalExceptionHandler)
│   ├── result/                      # 统一响应 (Result<T>)
│   └── utils/                       # 工具类 (CookieUtil, JwtUtil, RedisUtil, etc.)
├── config/                          # 配置类
│   ├── Knife4jConfig.java          # API 文档配置
│   ├── MybatisPlusConfig.java      # MyBatis-Plus 分页配置
│   ├── RedisConfig.java            # Redis 序列化配置
│   ├── WebMvcConfig.java           # 拦截器配置
│   └── CorsConfig.java             # 跨域配置
├── context/                         # 上下文
│   └── UserContext.java            # 用户上下文 (ThreadLocal)
├── interceptor/                     # 拦截器
│   └── LoginInterceptor.java       # 登录验证拦截器
├── application/                     # 应用层 (用例编排)
│   ├── dto/                        # 数据传输对象
│   ├── service/                    # 应用服务接口与实现
│   └── schedule/                   # 定时任务
├── domain/                          # 领域层 (核心业务逻辑)
│   ├── model/                      # 领域模型 (实体、值对象、聚合根)
│   ├── repository/                 # 仓储接口
│   └── service/                    # 领域服务
├── infrastructure/                  # 基础设施层 (技术实现)
│   ├── dao/                        # 数据访问
│   │   ├── entity/                 # 数据库实体
│   │   ├── mapper/                 # MyBatis Mapper
│   │   └── repository/             # 仓储实现
│   └── utils/                      # 基础设施工具
└── interfaces/                      # 接口层 (对外 API)
    ├── controller/                  # REST 控制器
    └── vo/                          # 视图对象 (Request/Response)
```

---

## 🚀 快速开始

### 前置条件

- **JDK**: 17+
- **Maven**: 3.6+
- **MySQL**: 8.0+
- **Redis**: 6.0+

### 环境准备

#### 1. 创建数据库

```bash
mysql -u root -p -e "CREATE DATABASE ai_wealth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

#### 2. 启动 Redis

```bash
# macOS
brew services start redis

# Linux
sudo systemctl start redis

# Docker
docker run -d -p 6379:6379 redis:latest
```

#### 3. 配置环境变量

编辑 `src/main/resources/application-dev.yaml`，修改数据库和 Redis 配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_wealth?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```

### 构建与运行

#### 方式一：Maven 直接运行（推荐开发使用）

```bash
./mvnw spring-boot:run
```

#### 方式二：打包后运行

```bash
# 编译打包
./mvnw clean package -DskipTests

# 运行 JAR
java -jar target/dzcom-0.0.1-SNAPSHOT.jar

# 指定 Profile
java -jar target/dzcom-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### 访问服务

- **应用地址**: http://localhost:8080
- **API 文档**: http://localhost:8080/doc.html (Knife4j)
- **Druid 监控**: http://localhost:8080/druid/index.html
- **Swagger UI**: http://localhost:8080/swagger-ui.html

---

## 🔐 认证与授权

### 认证流程

1. **登录**: POST `/api/auth/login` → 验证用户名密码 → 生成 JWT Token → 写入 HttpOnly Cookie + Redis
2. **验证**: 请求携带 Cookie → 拦截器提取 Token → 验证 JWT + Redis → 设置 UserContext (ThreadLocal)
3. **登出**: POST `/api/auth/logout` → 删除 Redis Token + 清除 Cookie

### 安全特性

- ✅ **HttpOnly Cookie**: 防止 XSS 攻击窃取 Token
- ✅ **JWT 签名验证**: 确保 Token 未被篡改
- ✅ **Redis 会话管理**: 支持主动登出与 Token 失效
- ✅ **BCrypt 密码加密**: 安全的密码存储
- ✅ **ThreadLocal 隔离**: 线程安全的用户信息传递

### 使用示例

```java
@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {
    
    @PostMapping
    public Result<String> createPortfolio(@Valid @RequestBody CreatePortfolioRequest request) {
        // 直接从 ThreadLocal 获取当前用户 ID
        String userId = UserContext.getCurrentUserId();
        
        String portfolioId = portfolioService.create(userId, request);
        return Result.success(portfolioId);
    }
}
```

---

## 📝 API 文档

### 认证相关接口

| 方法 | 路径 | 说明 | 需要登录 |
|------|------|------|----------|
| POST | `/api/auth/login` | 用户登录 | ❌ |
| POST | `/api/auth/logout` | 用户登出 | ✅ |
| GET | `/api/auth/current` | 获取当前用户信息 | ✅ |

### 测试登录接口

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test@123456"
  }' \
  -c cookies.txt

# 后续请求自动携带 Cookie
curl http://localhost:8080/api/auth/current \
  -b cookies.txt
```

---

## 🛠️ 开发规范

### 代码规范

所有代码必须遵循团队开发规范，详见：

📖 **[DevSkillsGuidelines.java](file:///Users/daniel/work/code_source/IdeaProjects/dzcom/src/main/java/com/example/dzcom/docs_new/DevSkillsGuidelines.java)**

包含：
- DDD 领域驱动设计规范
- 代码编写与命名规范
- AI 代码生成使用规范
- 安全与可观测性要求

### 关键规范摘要

#### 1. DDD 分层原则

- **Domain Layer**: 核心业务逻辑、领域模型、仓储接口
- **Application Layer**: 用例编排、事务管理、DTO 转换
- **Infrastructure Layer**: 数据持久化、外部服务调用
- **Interfaces Layer**: REST API、消息队列消费者

#### 2. 统一响应格式

所有接口返回统一的 `Result<T>` 对象：

```java
// 成功响应
return Result.success(data);

// 失败响应
return Result.error(400, "参数校验失败");
```

#### 3. 异常处理

- 业务异常抛出 `BusinessException`
- 全局异常处理器自动转换为统一响应
- 避免在 Controller 中捕获异常

#### 4. 日志规范

```java
log.info("用户登录成功: userId={}", userId);      // 关键操作
log.warn("Token 验证失败: token={}", token);      // 可恢复异常
log.error("系统异常: ", e);                        // 严重错误
```

#### 5. AI 代码生成规范

- ✅ 可用于生成样板代码、单元测试、文档草稿
- ❌ 禁止直接合并业务逻辑、安全相关代码
- 📝 PR 中必须注明 AI 使用情况（prompt、模型、人工审核步骤）

---

## 🧪 测试

### 运行测试

```bash
# 运行所有测试
./mvnw test

# 运行单个测试类
./mvnw test -Dtest=AuthControllerTest

# 生成测试报告
./mvnw surefire-report:report
```

### 测试策略

- **单元测试**: 领域模型、工具类（覆盖率 > 80%）
- **集成测试**: 仓储实现、服务层（数据库、Redis）
- **端到端测试**: 关键业务流程（登录、交易）

---

## 📦 依赖管理

### 核心依赖

| 依赖 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 4.0.5 | 主框架 |
| MyBatis-Plus | 3.5.9 | ORM 框架 |
| Hutool | 5.8.25 | Java 工具类 |
| FastJSON2 | 2.0.45 | JSON 处理 |
| JWT (jjwt) | 0.12.5 | Token 生成与验证 |
| Druid | 1.2.20 | 数据库连接池 |
| Knife4j | 4.5.0 | API 文档增强 |
| EasyExcel | 3.3.3 | Excel 导入导出 |

### 添加新依赖

在 `pom.xml` 的 `<dependencies>` 中添加，并执行：

```bash
./mvnw clean install -U  # 强制更新依赖
```

---

## 🔍 监控与调试

### Druid 监控

访问 http://localhost:8080/druid/index.html 查看：
- SQL 执行统计与性能分析
- 数据库连接池状态
- Web 应用监控

### 日志级别调整

编辑 `application-dev.yaml`：

```yaml
logging:
  level:
    com.example.dzcom: DEBUG          # 应用日志
    com.baomidou.mybatisplus: DEBUG   # MyBatis-Plus SQL 日志
    org.springframework.web: INFO     # Spring Web 日志
```

---

## 🤝 贡献指南

### 提交 PR 前检查清单

- [ ] 代码符合 `DevSkillsGuidelines.java` 规范
- [ ] 新增功能有对应的单元测试
- [ ] API 变更已更新 Swagger 注解
- [ ] 敏感信息未硬编码（密钥、密码）
- [ ] 日志不包含敏感数据
- [ ] 若使用 AI 生成代码，PR 描述中包含 AI 使用说明

### PR 模板

```markdown
## 变更说明
简要描述本次变更的目的和内容

## 变更类型
- [ ] Bug 修复
- [ ] 新功能
- [ ] 重构
- [ ] 文档更新

## 测试说明
描述如何测试本次变更，包括测试用例和测试结果

## AI 使用情况（如适用）
- 使用的模型: GPT-4 / Claude / 其他
- Prompt 摘要: ...
- 人工审核步骤: ...
```

---

## 📚 参考资料

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [DDD 实战指南](https://domain-driven-design.org/)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Knife4j 文档](https://doc.xiaominfo.com/)

---

## 📄 License

Apache License 2.0

---

**最后更新**: 2026-05-30  
**维护者**: AI 理财平台技术团队
