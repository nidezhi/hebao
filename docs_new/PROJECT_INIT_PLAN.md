# AI理财平台 - 项目初始化方案

## 📋 方案概述

本文档定义了AI理财平台的项目初始化方案，包括技术选型、依赖管理、工具类封装、认证授权体系等基础架构设计。

**核心原则**: 磨刀不误砍柴工 - 先搭建完善的基础设施，再开展业务开发

---

## 🎯 技术栈选型

### 1. 核心框架
- **Spring Boot 4.0.5** - 主框架
- **Java 17** - JDK版本
- **DDD架构** - 领域驱动设计分层

### 2. 数据库与持久化
- **MySQL 8.0+** - 主数据库
- **MyBatis-Plus 3.5.5** - ORM框架（简化CRUD）
- **Flyway** - 数据库迁移工具
- **Redis** - 缓存与会话存储
- **Druid** - 数据库连接池（监控+安全）

### 3. API文档
- **SpringDoc OpenAPI 3** - OpenAPI 3.0规范
- **Knife4j 4.5.0** - 增强Swagger UI（国产优化版）

### 4. 工具类库
- **Hutool 5.8.25** - Java工具类集合（推荐，比fastjson更安全）
- **FastJSON2 2.0.45** - JSON处理（阿里新版，修复安全漏洞）
- **Apache POI 5.2.5** - Excel导入导出
- **EasyExcel 3.3.3** - 阿里Excel工具（性能更好，内存友好）
- **Guava 33.0.0** - Google核心库
- **Apache Commons Lang3** - 常用工具

### 5. 安全与认证
- **JWT (jjwt 0.12.5)** - Token生成与验证
- **Spring Security Crypto** - 密码加密
- **Cookie + Redis** - 会话管理方案

### 6. 其他
- **Lombok** - 简化代码
- **MapStruct** - 对象映射（高性能）
- **Validation** - 参数校验

---

## 📦 Maven依赖配置

### pom.xml 关键依赖

```xml
<properties>
    <java.version>17</java.version>
    <mybatis-plus.version>3.5.5</mybatis-plus.version>
    <hutool.version>5.8.25</hutool.version>
    <fastjson2.version>2.0.45</fastjson2.version>
    <easyexcel.version>3.3.3</easyexcel.version>
    <knife4j.version>4.5.0</knife4j.version>
    <jwt.version>0.12.5</jwt.version>
    <druid.version>1.2.20</druid.version>
    <mapstruct.version>1.5.5.Final</mapstruct.version>
</properties>

<dependencies>
    <!-- ========== Web & MVC ========== -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- ========== API Documentation ========== -->
    <dependency>
        <groupId>com.github.xiaoymin</groupId>
        <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
        <version>${knife4j.version}</version>
    </dependency>

    <!-- ========== Database ========== -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot4-starter</artifactId>
        <version>${mybatis-plus.version}</version>
    </dependency>
    
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-mysql</artifactId>
    </dependency>
    
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid-spring-boot-4-starter</artifactId>
        <version>${druid.version}</version>
    </dependency>

    <!-- ========== Redis ========== -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>

    <!-- ========== Utils ========== -->
    <dependency>
        <groupId>cn.hutool</groupId>
        <artifactId>hutool-all</artifactId>
        <version>${hutool.version}</version>
    </dependency>
    
    <dependency>
        <groupId>com.alibaba.fastjson2</groupId>
        <artifactId>fastjson2</artifactId>
        <version>${fastjson2.version}</version>
    </dependency>
    
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>easyexcel</artifactId>
        <version>${easyexcel.version}</version>
    </dependency>
    
    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
        <version>33.0.0-jre</version>
    </dependency>
    
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
    </dependency>

    <!-- ========== JWT ========== -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>${jwt.version}</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>${jwt.version}</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>${jwt.version}</version>
        <scope>runtime</scope>
    </dependency>

    <!-- ========== Lombok & MapStruct ========== -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>${mapstruct.version}</version>
    </dependency>

    <!-- ========== Dev Tools ========== -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>
</dependencies>
```

---

## 🏗️ 项目目录结构

```
src/main/java/com/example/dzcom/
├── DzcomApplication.java                    # 启动类
├── common/                                  # 【新增】通用模块
│   ├── annotation/                          # 自定义注解
│   │   ├── RequireLogin.java               # 需要登录注解
│   │   └── IgnoreLogin.java                # 忽略登录注解
│   ├── constant/                            # 常量定义
│   │   ├── RedisKeyConstant.java           # Redis键前缀
│   │   └── CookieConstant.java             # Cookie常量
│   ├── enums/                               # 枚举
│   │   ├── ResultCode.java                 # 响应码
│   │   └── UserStatus.java                 # 用户状态
│   ├── exception/                           # 异常处理
│   │   ├── BusinessException.java          # 业务异常
│   │   └── GlobalExceptionHandler.java     # 全局异常处理器
│   ├── result/                              # 统一响应
│   │   └── Result.java                     # 统一返回结果
│   └── utils/                               # 工具类
│       ├── CookieUtil.java                 # Cookie工具
│       ├── JwtUtil.java                    # JWT工具
│       ├── RedisUtil.java                  # Redis工具
│       ├── JsonUtil.java                   # JSON工具
│       ├── ExcelUtil.java                  # Excel工具
│       └── SecurityUtil.java               # 安全工具
├── config/                                  # 【新增】配置类
│   ├── Knife4jConfig.java                  # Knife4j配置
│   ├── MybatisPlusConfig.java              # MyBatis-Plus配置
│   ├── RedisConfig.java                    # Redis配置
│   ├── WebMvcConfig.java                   # Web MVC配置
│   └── CorsConfig.java                     # 跨域配置
├── interceptor/                             # 【新增】拦截器
│   └── LoginInterceptor.java               # 登录拦截器
├── context/                                 # 【新增】上下文
│   └── UserContext.java                    # 用户上下文（ThreadLocal）
├── application/                             # 应用层
│   ├── dto/                                # 数据传输对象
│   ├── service/                            # 应用服务
│   └── schedule/                           # 定时任务
├── domain/                                  # 领域层
│   ├── model/                              # 领域模型
│   ├── repository/                         # 仓储接口
│   └── service/                            # 领域服务
├── infrastructure/                          # 基础设施层
│   ├── config/                             # 基础设施配置
│   ├── dao/                                # 数据访问
│   │   ├── entity/                         # 实体类
│   │   ├── mapper/                         # MyBatis Mapper
│   │   └── repository/                     # 仓储实现
│   └── utils/                              # 基础设施工具
└── interfaces/                              # 接口层
    ├── controller/                          # 控制器
    │   ├── AuthController.java             # 认证控制器
    │   └── UserController.java             # 用户控制器
    └── vo/                                  # 视图对象
        ├── request/                         # 请求VO
        └── response/                        # 响应VO
```

---

## 🔐 Cookie登录验证方案

### 方案设计

采用 **Cookie + Redis + JWT** 的混合方案：

1. **登录流程**:
   - 用户提交账号密码 → 验证通过
   - 生成JWT Token（包含用户ID、过期时间）
   - 将Token存入Redis（key: `auth:token:{userId}`, value: token, TTL: 7天）
   - 将Token写入HttpOnly Cookie（防止XSS攻击）

2. **验证流程**:
   - 拦截器从Cookie读取Token
   - 验证JWT签名和有效期
   - 查询Redis确认Token未失效
   - 解析用户信息存入ThreadLocal
   - Controller可直接获取当前用户

3. **优势**:
   - ✅ HttpOnly Cookie防XSS
   - ✅ Redis支持主动登出
   - ✅ JWT无状态，便于扩展
   - ✅ ThreadLocal线程隔离，安全获取用户信息

### 核心代码实现

#### 1. 统一响应结果 (Result.java)

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
            .code(200)
            .message("success")
            .data(data)
            .build();
    }
    
    public static <T> Result<T> error(Integer code, String message) {
        return Result.<T>builder()
            .code(code)
            .message(message)
            .build();
    }
}
```

#### 2. Cookie工具类 (CookieUtil.java)

```java
@Component
public class CookieUtil {
    
    /**
     * 设置Cookie
     */
    public static void setCookie(HttpServletResponse response, 
                                  String name, 
                                  String value, 
                                  int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
            .httpOnly(true)              // 防止XSS
            .secure(false)               // 生产环境改为true (HTTPS)
            .path("/")
            .maxAge(Duration.ofSeconds(maxAge))
            .sameSite("Lax")             // CSRF防护
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
    
    /**
     * 删除Cookie
     */
    public static void deleteCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
            .httpOnly(true)
            .path("/")
            .maxAge(0)
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
    
    /**
     * 从Request获取Cookie值
     */
    public static String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
```

#### 3. JWT工具类 (JwtUtil.java)

```java
@Component
public class JwtUtil {
    
    @Value("${jwt.secret:aiWealthSecretKey2026}")
    private String secret;
    
    @Value("${jwt.expiration:604800}") // 7天
    private Long expiration;
    
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * 生成Token
     */
    public String generateToken(String userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);
        
        return Jwts.builder()
            .subject(userId)
            .claim("username", username)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact();
    }
    
    /**
     * 解析Token
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
    
    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 从Token获取用户ID
     */
    public String getUserIdFromToken(String token) {
        return parseToken(token).getSubject();
    }
}
```

#### 4. 用户上下文 (UserContext.java)

```java
public class UserContext {
    
    private static final ThreadLocal<UserInfo> USER_THREAD_LOCAL = new ThreadLocal<>();
    
    @Data
    @Builder
    public static class UserInfo {
        private String userId;
        private String username;
        private String userNo;
    }
    
    /**
     * 设置当前用户信息
     */
    public static void setCurrentUser(UserInfo userInfo) {
        USER_THREAD_LOCAL.set(userInfo);
    }
    
    /**
     * 获取当前用户信息
     */
    public static UserInfo getCurrentUser() {
        return USER_THREAD_LOCAL.get();
    }
    
    /**
     * 获取当前用户ID
     */
    public static String getCurrentUserId() {
        UserInfo userInfo = USER_THREAD_LOCAL.get();
        return userInfo != null ? userInfo.getUserId() : null;
    }
    
    /**
     * 清除用户信息
     */
    public static void clear() {
        USER_THREAD_LOCAL.remove();
    }
}
```

#### 5. 登录拦截器 (LoginInterceptor.java)

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {
    
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                             HttpServletResponse response, 
                             Object handler) throws Exception {
        
        // 检查是否有忽略登录的注解
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (handlerMethod.hasMethodAnnotation(IgnoreLogin.class)) {
                return true;
            }
        }
        
        // 从Cookie获取Token
        String token = CookieUtil.getCookieValue(request, "AUTH_TOKEN");
        
        if (StrUtil.isBlank(token)) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        
        // 验证JWT
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(401, "Token无效");
        }
        
        // 获取用户ID
        String userId = jwtUtil.getUserIdFromToken(token);
        
        // 检查Redis中Token是否存在
        String redisKey = RedisKeyConstant.AUTH_TOKEN_PREFIX + userId;
        String redisToken = redisTemplate.opsForValue().get(redisKey);
        
        if (StrUtil.isBlank(redisToken) || !redisToken.equals(token)) {
            throw new BusinessException(401, "登录已失效，请重新登录");
        }
        
        // 解析用户信息
        Claims claims = jwtUtil.parseToken(token);
        String username = claims.get("username", String.class);
        
        // 设置到ThreadLocal
        UserContext.setCurrentUser(
            UserContext.UserInfo.builder()
                .userId(userId)
                .username(username)
                .build()
        );
        
        log.debug("用户登录验证通过: userId={}, username={}", userId, username);
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, 
                                HttpServletResponse response, 
                                Object handler, 
                                Exception ex) throws Exception {
        // 清除ThreadLocal，防止内存泄漏
        UserContext.clear();
    }
}
```

#### 6. 自定义注解

```java
// RequireLogin.java - 标记需要登录的接口（默认所有接口都需要）
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireLogin {
}

// IgnoreLogin.java - 标记忽略登录的接口
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreLogin {
}
```

#### 7. WebMvc配置 (WebMvcConfig.java)

```java
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final LoginInterceptor loginInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
            .addPathPatterns("/api/**")           // 拦截所有API
            .excludePathPatterns(
                "/api/auth/login",                // 登录接口排除
                "/api/auth/register",             // 注册接口排除
                "/doc.html",                      // Knife4j文档
                "/swagger-ui/**",
                "/v3/api-docs/**"
            );
    }
}
```

#### 8. 认证控制器 (AuthController.java)

```java
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "用户登录、注册、登出")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    
    @PostMapping("/login")
    @IgnoreLogin
    @Operation(summary = "用户登录", description = "登录成功后Token写入Cookie")
    public Result<Void> login(@Valid @RequestBody LoginRequest request, 
                              HttpServletResponse response) {
        
        // 验证用户名密码
        User user = userService.authenticate(request.getUsername(), request.getPassword());
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getBizId(), user.getUsername());
        
        // 存入Redis (7天过期)
        String redisKey = RedisKeyConstant.AUTH_TOKEN_PREFIX + user.getBizId();
        redisTemplate.opsForValue().set(redisKey, token, 7, TimeUnit.DAYS);
        
        // 写入Cookie
        CookieUtil.setCookie(response, "AUTH_TOKEN", token, 7 * 24 * 3600);
        
        return Result.success(null);
    }
    
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "清除Token")
    public Result<Void> logout(HttpServletResponse response) {
        String userId = UserContext.getCurrentUserId();
        
        // 删除Redis中的Token
        String redisKey = RedisKeyConstant.AUTH_TOKEN_PREFIX + userId;
        redisTemplate.delete(redisKey);
        
        // 删除Cookie
        CookieUtil.deleteCookie(response, "AUTH_TOKEN");
        
        return Result.success(null);
    }
    
    @GetMapping("/current")
    @Operation(summary = "获取当前用户信息")
    public Result<UserInfoVO> getCurrentUser() {
        String userId = UserContext.getCurrentUserId();
        User user = userService.getById(userId);
        
        UserInfoVO vo = BeanUtil.copyProperties(user, UserInfoVO.class);
        return Result.success(vo);
    }
}
```

#### 9. Controller中使用示例

```java
@RestController
@RequestMapping("/api/portfolio")
@Tag(name = "投资组合管理")
@RequiredArgsConstructor
public class PortfolioController {
    
    private final PortfolioService portfolioService;
    
    @PostMapping
    @Operation(summary = "创建投资组合")
    public Result<String> createPortfolio(@Valid @RequestBody CreatePortfolioRequest request) {
        // 直接从ThreadLocal获取当前用户ID
        String userId = UserContext.getCurrentUserId();
        
        String portfolioId = portfolioService.create(userId, request);
        return Result.success(portfolioId);
    }
    
    @GetMapping("/list")
    @Operation(summary = "查询我的投资组合")
    public Result<List<PortfolioVO>> listPortfolios() {
        // 自动获取当前用户
        String userId = UserContext.getCurrentUserId();
        
        List<PortfolioVO> portfolios = portfolioService.listByUser(userId);
        return Result.success(portfolios);
    }
}
```

---

## 📝 Knife4j配置

### Knife4jConfig.java

```java
@Configuration
public class Knife4jConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("AI理财平台 API文档")
                .description("AI驱动的理财产品平台接口文档")
                .version("1.0.0")
                .contact(new Contact()
                    .name("技术团队")
                    .email("tech@aiwealth.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .components(new Components()
                .addSecuritySchemes("cookie-auth", 
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name("AUTH_TOKEN")));
    }
}
```

### application.yaml 配置

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
  group-configs:
    - group: 'default'
      paths-to-match: '/api/**'
      packages-to-scan: com.example.dzcom.interfaces.controller

knife4j:
  enable: true
  setting:
    language: zh_cn
    swagger-model-name: 实体类列表
```

访问地址: `http://localhost:8080/doc.html`

---

## 🛠️ 常用工具类封装

### 1. Redis工具类 (RedisUtil.java)

```java
@Component
@RequiredArgsConstructor
public class RedisUtil {
    
    private final StringRedisTemplate redisTemplate;
    
    /**
     * 设置字符串
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }
    
    /**
     * 获取字符串
     */
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 删除键
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }
    
    /**
     * 判断键是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
    
    /**
     * 设置Hash
     */
    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }
    
    /**
     * 获取Hash
     */
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }
}
```

### 2. JSON工具类 (JsonUtil.java)

```java
public class JsonUtil {
    
    /**
     * 对象转JSON字符串
     */
    public static String toJson(Object obj) {
        return JSON.toJSONString(obj);
    }
    
    /**
     * JSON字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }
    
    /**
     * JSON字符串转List
     */
    public static <T> List<T> fromJsonArray(String json, Class<T> clazz) {
        return JSON.parseArray(json, clazz);
    }
}
```

### 3. Excel工具类 (ExcelUtil.java)

```java
public class ExcelUtil {
    
    /**
     * 导出Excel
     */
    public static void exportExcel(HttpServletResponse response, 
                                    String fileName, 
                                    List<?> data, 
                                    Class<?> clazz) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8");
        response.setHeader("Content-disposition", 
            "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");
        
        EasyExcel.write(response.getOutputStream(), clazz)
            .sheet("Sheet1")
            .doWrite(data);
    }
    
    /**
     * 导入Excel
     */
    public static <T> List<T> importExcel(InputStream inputStream, Class<T> clazz) {
        return EasyExcel.read(inputStream, clazz, null)
            .sheet()
            .doReadSync();
    }
}
```

---

## ⚙️ 配置文件

### application-dev.yaml

```yaml
server:
  port: 8080

spring:
  application:
    name: ai-wealth-platform
  
  # 数据源配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ai_wealth?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: root
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
  
  # Redis配置
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      timeout: 3000ms
  
  # Flyway配置
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

# MyBatis-Plus配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: ASSIGN_UUID
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# JWT配置
jwt:
  secret: YWlXZWFsdGhTZWNyZXRLZXkyMDI2QEludmVzdA==  # Base64编码的密钥
  expiration: 604800  # 7天（秒）

# 日志配置
logging:
  level:
    com.example.dzcom: DEBUG
    com.baomidou.mybatisplus: DEBUG
```

---

## 📌 实施步骤

### Phase 1: 基础依赖与配置
1. ✅ 更新pom.xml添加依赖
2. ✅ 配置application.yaml
3. ✅ 创建目录结构
4. ✅ 配置Knife4j

### Phase 2: 工具类封装
1. ✅ 实现CookieUtil、JwtUtil、RedisUtil
2. ✅ 实现UserContext（ThreadLocal）
3. ✅ 实现统一响应Result
4. ✅ 实现全局异常处理

### Phase 3: 认证体系
1. ✅ 实现LoginInterceptor
2. ✅ 实现RequireLogin/IgnoreLogin注解
3. ✅ 配置WebMvc拦截器
4. ✅ 实现AuthController

### Phase 4: 测试验证
1. ✅ 测试登录接口
2. ✅ 测试Cookie自动携带
3. ✅ 测试拦截器验证
4. ✅ 测试ThreadLocal获取用户

---

## 🎁 额外建议

### 1. 安全加固
- 生产环境启用HTTPS，Cookie设置secure=true
- 密码使用BCrypt加密
- 添加验证码防止暴力破解
- 限制登录失败次数

### 2. 性能优化
- Redis集群部署
- 热点数据本地缓存（Caffeine）
- 数据库读写分离
- API响应缓存

### 3. 监控告警
- Druid监控SQL性能
- Actuator健康检查
- Prometheus + Grafana监控
- 链路追踪（SkyWalking）

### 4. 开发效率
- 统一代码风格（Checkstyle）
- Git Hook预检查
- 自动化测试覆盖率
- CI/CD流水线

---

**文档版本**: v1.0  
**创建日期**: 2026-05-30  
**维护者**: AI理财平台技术团队
