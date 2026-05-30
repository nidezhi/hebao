package com.example.dzcom.docs_new;

/**
 * 高可用开发规范（包含领域驱动开发规范、代码规范与 AI 代码生成规范）
 *
 * 目的
 * 本文件以可编译的 JavaDoc 形式，集中记录团队的高可用开发规范（Domain-Driven
 * Design、代码开发规范、以及 AI 代码生成使用规范）。作为团队内部的权威参考，
 * 后续所有开发、评审与 AI 使用流程应以此为准。
 *
 * 目录概览
 * 1) 原则与目标
 * 2) 领域驱动（DDD）开发规范
 * 3) 代码开发规范
 * 4) 认证与安全规范（本项目特定）
 * 5) API 设计规范（本项目特定）
 * 6) AI 代码生成规范（边界、幻觉、冗余等限制与缓解策略）
 * 7) 工具链与校验要求
 * 8) PR 与评审检查清单
 * 9) 快速参考（Do / Don't）
 * 10) 参考资料
 *
 * 1) 原则与目标
 * - 高可用性：设计上考虑故障隔离、自动降级与快速恢复；避免单点故障（SPOF）。
 * - 可观测性：关键业务须可度量（日志、指标、Tracing、告警）。
 * - 可维护性：代码易读、易测、变更小而安全。
 * - 领域清晰：业务规则应集中在领域层，避免业务逻辑泄露到界面或基础设施层。
 * - 对 AI 生成代码保持最小信任：任何 AI 输出必须经过人工审查与测试才能合并。
 *
 * 2) 领域驱动（DDD）开发规范
 * - 统一语言（Ubiquitous Language）
 *   - 为每个有界上下文（Bounded Context）定义并记录统一语言，类名、方法名、测试与文档
 *     尽量使用领域术语，减少翻译成本。
 * - 有界上下文与集成
 *   - 明确上下文边界，绘制上下文依赖图。服务间优先采用异步事件集成以增强弹性。
 * - 聚合（Aggregate）与一致性
 *   - 聚合为事务一致性边界；聚合应保持精小（经验规则：字段与行为集中且不过度膨胀）。
 *   - 跨聚合使用领域事件实现最终一致性与解耦。
 * - 域服务与应用服务的职责
 *   - 涉及多个聚合或外部系统的复杂业务放在域服务或事件处理器中；应用服务负责用例编排、事务
 *     与边界转换，但不承担核心业务规则。
 * - 仓储与持久化
 *   - 仓储接口位于域层，具体实现位于基础设施层。避免将 ORM 或持久化细节泄露到领域对象中，必要时
 *     使用映射器（mapper）。
 * - 防腐层（Anti-Corruption Layer）
 *   - 与遗留系统或第三方集成时，建立防腐层进行模型翻译与保护。
 * - 领域事件
 *   - 事件必须是不可变的、可版本化且仅承载必要数据，便于演进与兼容。
 * - 值对象
 *   - 对于不可变概念（如金额、邮箱、坐标等）使用值对象，在构造时保证验证并覆盖 equals/hashCode。
 *
 * 3) 代码开发规范
 * - 项目结构
 *   - 建议分层：common、config、context、interceptor、domain、application、interfaces、infrastructure。
 *     包结构应与有界上下文相映射，便于责任划分。
 * - 命名与可读性
 *   - 使用描述性命名，避免缩写；方法名应表达意图。函数应短小、职责单一，超过一屏考虑拆分。
 * - 设计原则
 *   - 遵循 SOLID，优先使用组合而非继承提升可替换性与可测试性。
 * - 错误处理
 *   - 及时失败（fail-fast），区分可恢复/不可恢复错误，避免吞掉异常，需记录并合理转换。
 *   - 业务异常统一使用 BusinessException，由 GlobalExceptionHandler 处理。
 * - 日志与可观测性
 *   - 记录关键状态变更（INFO 成功、WARN 可恢复异常、ERROR 严重错误），携带关联 ID；优先结构化日志（JSON），
 *     且不在日志中暴露敏感信息。
 *   - 禁止在日志中打印密码、Token、密钥等敏感信息。
 * - 配置
 *   - 将环境相关配置外置（环境变量或集中配置中心）。启动时校验关键配置，发现严重错误应 fail-fast。
 *   - 不同环境使用不同的 Profile（local、dev、online）。
 * - 安全与凭据
 *   - 禁止将密钥/凭据提交到仓库，使用密钥管理服务或 CI 提供的 secret 机制；对外部资源采用最小权限原则。
 *   - 密码必须使用 BCrypt 加密存储（SecurityUtil.encodePassword）。
 * - 依赖管理
 *   - 依赖应精简、可审计；优先稳定且广泛使用的库，定期跟进安全漏洞修复（CVE）。
 *   - Spring Boot 4.x 兼容性注意：使用 mybatis-plus-boot-starter 而非 spring-boot4-starter。
 * - 测试策略
 *   - 分层测试：领域单元测试、仓储/服务集成测试、关键流程 e2e 测试。测试应尽量确定性，避免易抖动的外网调用。
 *   - 测试金字塔：大量快速单测、较少集成测试、极少端到端测试。
 * - 代码评审与 PR
 *   - 控制 PR 大小（推荐 <400 行），PR 描述包含变更原因、风险与测试说明；评审重点：领域一致性、测试覆盖、可观测性、
 *     错误处理与安全影响。
 * - 格式化与静态检查
 *   - 统一格式化工具（例如 google-java-format）、静态检查与漏洞扫描（Checkstyle、SpotBugs、ErrorProne），并在 CI 强制执行。
 *
 * 4) 认证与安全规范（本项目特定）
 * - Cookie + JWT + Redis 认证方案
 *   - Token 存储在 HttpOnly Cookie 中，防止 XSS 攻击
 *   - JWT 用于无状态验证，Redis 用于会话管理（支持主动登出）
 *   - Token 有效期：7 天，可根据安全需求调整
 * - 登录拦截器
 *   - 默认拦截所有 /api/** 路径
 *   - 公开接口使用 @IgnoreLogin 注解标记（如登录、注册）
 *   - 拦截器验证顺序：Cookie 提取 → JWT 签名验证 → Redis 存在性检查 → UserContext 设置
 * - 用户上下文（UserContext）
 *   - 使用 ThreadLocal 存储当前用户信息，确保线程安全
 *   - 必须在拦截器的 afterCompletion 中清除，防止内存泄漏
 *   - Controller 中通过 UserContext.getCurrentUserId() 获取用户 ID
 * - 密码安全
 *   - 使用 BCrypt 算法加密密码（SecurityUtil.encodePassword）
 *   - 验证密码时使用 SecurityUtil.matches(rawPassword, encodedPassword)
 *   - 禁止明文存储或传输密码
 * - CSRF 防护
 *   - Cookie 设置 sameSite="Lax"
 *   - 生产环境启用 HTTPS，Cookie 设置 secure=true
 * - SQL 注入防护
 *   - 使用 MyBatis-Plus 参数化查询，禁止拼接 SQL
 *   - 动态查询使用 LambdaQueryWrapper
 *
 * 5) API 设计规范（本项目特定）
 * - 统一响应格式
 *   - 所有接口返回 Result<T> 对象，包含 code、message、data 三个字段
 *   - 成功：Result.success(data) 或 Result.success()
 *   - 失败：Result.error(code, message) 或 Result.error(message)
 * - RESTful 规范
 *   - GET: 查询资源（幂等）
 *   - POST: 创建资源
 *   - PUT: 全量更新资源
 *   - PATCH: 部分更新资源
 *   - DELETE: 删除资源
 * - 路径命名
 *   - 使用复数名词：/api/portfolios、/api/users
 *   - 子资源：/api/portfolios/{id}/orders
 *   - 避免动词：使用 HTTP 方法表达动作
 * - 参数校验
 *   - 使用 @Valid 或 @Validated 注解触发校验
 *   - Request VO 中使用 JSR-303 注解（@NotBlank、@Email、@Min 等）
 *   - 校验失败由 GlobalExceptionHandler 统一处理
 * - Swagger 文档
 *   - 所有 Controller 添加 @Tag 注解
 *   - 所有接口方法添加 @Operation 注解，包含 summary 和 description
 *   - Request/Response VO 添加 @Schema 注解说明字段含义
 * - 分页查询
 *   - 使用 MyBatis-Plus 分页插件
 *   - 返回 Page<T> 对象，包含 total、pages、current、size 等信息
 *
 * 6) AI 代码生成规范（关键限制与缓解策略）
 * 总体原则：将 AI 视作辅助工具，而非自动决策者。所有 AI 生成内容必须由人为复核并通过测试。
 *
 * - 可用场景
 *   - 生成样板代码（DTO、Mapper、基础测试模版）、编写文档草稿、建议重构方案与生成单元测试用例。
 *   - 严禁直接合并 AI 生成的业务逻辑、权限或安全相关代码，必须人工审核。
 * - Prompt 规范
 *   - 提供精确上下文（相关接口、领域规则、包路径、平台限制、期望测试）并拆分为小任务（一次一类或一方法）。
 *   - 要求 AI 同时输出实现、注释与对应单元测试用例。
 * - 边界与强制要求
 *   - 对于生成的领域逻辑：必须有完整单元测试；对基础设施相关代码（DB、HTTP），至少要有一条集成测试。
 *   - 禁止生成全局隐藏单例、未受控线程或无界缓存等潜在资源泄露点，若确有需求必须书面设计并评审。
 *   - 对于生成的 SQL/查询，必须采用参数化或 ORM，审查注入风险与性能。
 * - 幻觉（hallucination）防护
 *   - 将 AI 提供的事实类输出（类名、接口、表结构、配置信息）视为建议，须与代码库与数据库模式核对。
 *   - 要求 AI 生成代码能在本地编译并通过测试后才允许提交 PR。
 *   - 在 PR 中注明 AI 使用来源、模型与提示（prompt）摘要，保证可追溯。
 * - 冗余与膨胀控制
 *   - 生成代码应尽量精简，避免重复工具类或复制粘贴的实现。若产生重复，应抽象为共享工具并补充测试。
 *   - 禁止一次性生成与任务无关的大量文件。每个生成任务应聚焦单一关注点。
 * - 许可与知识产权
 *   - 使用 AI 时需遵循公司许可与 IP 政策，避免直接复制外部受限代码或大段内容。必要时记录来源与许可约束。
 * - 安全与敏感信息
 *   - 禁止在对外 AI 提示中包含任何密钥、凭据或私有代码片段；对日志和问题单中保存的提示内容做脱敏处理。
 * - 责任归属
 *   - AI 生成的变更必须指定人类负责人（Owner），负责人需验证正确性、测试覆盖与安全。
 * - 持续改进
 *   - 统计 AI 使用指标（生成 PR 数、引入缺陷、节省时间、评审成本），并据此持续优化流程与限制。
 *
 * 7) 工具链与校验要求
 * - 格式化/静态检查：google-java-format、Checkstyle、SpotBugs、ErrorProne。
 * - 依赖与安全扫描：Dependabot、Snyk 或企业白名单工具。
 * - CI 流水线门禁：编译、测试、格式化、静态分析、许可检查与 AI 元数据检查（PR 模板），必须全部通过才允许合并。
 * - 合约测试：跨服务通信场景可使用 Pact 等合约测试工具。
 * - 可观测性：关键流程必须上报指标与 Trace，缺失关键指标将作为评审失败项。
 *
 * 8) PR 与评审检查清单（简要）
 * - 变更是否使用统一语言并放在正确的有界上下文包中？
 * - 领域规则是否在领域层实现并有单元测试覆盖？
 * - 外部边界（DB/HTTP）是否有集成测试与错误场景处理？
 * - 日志是否有意义且不包含敏感信息？是否包含关联 ID？
 * - PR 范围是否合理、变更是否可回滚？迁移影响是否清晰？
 * - 若使用 AI 生成，PR 是否包含 AI 摘要（prompt、模型、审查步骤）？
 * - 是否评估并测试了性能与安全影响？
 * - 认证相关变更是否经过安全审查？
 * - API 变更是否更新了 Swagger 文档？
 *
 * 9) 快速参考（Do / Don't）
 * DO:
 * - 将业务规则保持在领域层并编写单元测试。
 * - 使用描述性命名与小函数，保持代码可读。
 * - 为每个修复与新功能补充测试。
 * - 启动时校验关键配置。
 * - 将 AI 用于草稿、样板与测试，但必须人工复核。
 * - 使用 UserContext.getCurrentUserId() 获取当前用户。
 * - 使用 Result.success/error 统一响应格式。
 * - 使用 @IgnoreLogin 标记公开接口。
 * DON'T:
 * - 将密钥/凭据提交到版本库。
 * - 未经审查直接信任 AI 生成代码。
 * - 允许聚合演化为上帝对象；发现复杂度时及时拆分。
 * - 忽视关键流程的可观测性与告警。
 * - 在日志中打印密码、Token 等敏感信息。
 * - 直接操作 HttpServletRequest 获取用户信息（应使用 UserContext）。
 * - 在 Controller 中捕获异常（应由 GlobalExceptionHandler 统一处理）。
 *
 * 10) 参考资料（建议阅读）
 * - Eric Evans，《Domain-Driven Design》
 * - Vaughn Vernon，《Implementing Domain-Driven Design》
 * - Robert C. Martin，《Clean Code》《Clean Architecture》
 * - Google Java Style Guide
 * - OWASP Top 10
 * - Spring Boot 官方文档
 * - MyBatis-Plus 官方文档
 *
 * 变更流程
 * - 对本规范的修改通过 PR 提交，说明修改原因与迁移步骤，审查通过后合并。
 */
public final class DevSkillsGuidelines {
    // 文档类，禁止实例化
    private DevSkillsGuidelines() {
    }
}

