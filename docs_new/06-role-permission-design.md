# DZCOM 角色与权限模型设计

本文档定义 DZCOM 角色与权限模型的设计方案、数据结构和实施基线。当前方案已通过 `V6__role_and_permission_model.sql`、独立 Mapper XML、仓储实现、授权服务和管理接口落地。

## 1. 设计背景

当前账户域已经存在 `aiw_user_role`，用于保存用户角色分配：

- `user_biz_id` 表示被授权用户。
- `role_code` 表示分配的角色编码。
- `scope_code` 表示授权作用域。
- `effective_from`、`effective_to` 表示授权有效期。

该表只表达“哪个用户拥有什么角色”，没有角色名称、角色状态、角色说明和权限集合。历史代码曾直接判断 `ADMIN` 等角色编码，现已切换为稳定权限编码鉴权。

## 2. 核心决策

采用基于角色的访问控制模型 RBAC，形成以下三层关系：

1. `aiw_role`：角色定义。
2. `aiw_user_role`：用户与角色的分配关系，沿用现有表。
3. `aiw_role_permission`：角色与权限的映射关系。

本次所称“用户角色权限表”在物理模型中命名为 `aiw_role_permission`。权限由用户持有的有效角色间接获得，不直接建立 `user_biz_id + permission_code` 的授权表。

这样设计的原因：

- 避免每个用户重复保存相同权限。
- 修改角色权限时只维护角色映射，不批量更新用户数据。
- 用户角色分配、角色定义和角色权限各自具有独立生命周期。
- 可以继续使用现有 `aiw_user_role`，减少账户注册和登录链路的迁移范围。
- 后续如确实需要用户级例外授权，应单独设计授权覆盖模型，不能污染角色权限表。

## 3. 领域边界

### 3.1 角色

角色是权限集合的业务载体。角色编码是系统内稳定标识，角色名称和说明可以修改，但角色编码创建后不可修改。

首批系统角色建议为：

| 角色编码 | 角色名称 | 主要职责 |
| --- | --- | --- |
| `USER` | 普通用户 | 使用个人账户、产品查询、组合与订单能力 |
| `ADVISOR` | 投顾人员 | 查看授权客户信息并提供投资辅助服务 |
| `RISK` | 风控人员 | 查看风险检查结果并执行授权的风控操作 |
| `AUDITOR` | 审计人员 | 查询审计记录，不默认具有业务写权限 |
| `ADMIN` | 系统管理员 | 管理用户、角色和系统配置 |

`ADMIN` 不应在代码中被解释为无条件绕过全部权限。管理员是否拥有某项权限仍由 `aiw_role_permission` 决定，避免新增高风险接口时自动向管理员开放。

### 3.2 权限

权限表示一个可独立授权的业务动作，使用稳定字符串编码。权限编码建议采用：

```text
<领域>:<资源>:<动作>
```

示例：

| 权限编码 | 含义 |
| --- | --- |
| `account:user:read` | 查询用户信息 |
| `account:user:update-status` | 修改用户状态 |
| `account:role:manage` | 管理角色定义 |
| `account:role:assign` | 给用户分配或撤销角色 |
| `product:catalog:read` | 查询产品目录 |
| `product:catalog:manage` | 管理产品目录 |
| `market:quote:write` | 写入或修正行情 |
| `audit:log:read` | 查询审计日志 |

权限编码必须由应用代码中的集中注册表或枚举约束，禁止 Controller、数据库脚本和业务服务各自创建不同字符串。

当前实现不新建独立权限定义表。权限来源于应用内 `PermissionCodes` 注册表，权限编码随代码评审与版本发布管理。若后续需要动态权限目录、多语言名称或菜单绑定，再单独评审 `aiw_permission`。

### 3.3 作用域

继续沿用 `aiw_user_role.scope_code`：

- `GLOBAL`：全局生效。
- 其他值：表示未来的机构、租户、业务线或资源域。

本阶段不在 `aiw_role_permission` 重复保存作用域。权限定义角色能做什么，用户角色分配决定该角色在哪里生效。

## 4. 数据关系

```text
aiw_user
    1
    |
    | user_biz_id
    N
aiw_user_role
    N
    |
    | role_code
    1
aiw_role
    1
    |
    | role_code
    N
aiw_role_permission
```

所有关联均使用业务标识进行逻辑关联，不建立数据库外键。

## 5. 表结构方案

### 5.1 角色定义表 `aiw_role`

职责：保存角色的稳定定义、启停状态和审计信息。

```sql
CREATE TABLE aiw_role (
    biz_id CHAR(36) NOT NULL COMMENT '角色业务ID，UUID字符串',
    role_code VARCHAR(64) NOT NULL COMMENT '稳定角色编码，创建后不可修改',
    role_name VARCHAR(128) NOT NULL COMMENT '角色显示名称',
    description VARCHAR(512) NULL COMMENT '角色职责和授权边界说明',
    role_type VARCHAR(16) NOT NULL DEFAULT 'CUSTOM'
        COMMENT '角色类型：SYSTEM-系统内置，CUSTOM-自定义',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '角色状态：0-停用，1-启用',
    version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
    created_by VARCHAR(64) NULL COMMENT '创建操作者业务ID或系统标识',
    updated_by VARCHAR(64) NULL COMMENT '最后更新操作者业务ID或系统标识',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    deleted_at DATETIME(3) NULL COMMENT '逻辑删除时间',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_aiw_role_code (role_code),
    KEY idx_aiw_role_status (status, is_deleted, created_at),
    KEY idx_aiw_role_name (role_name)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='角色定义表';
```

字段约束：

- `role_code` 全局唯一，统一使用大写字母、数字和下划线。
- `SYSTEM` 角色禁止物理删除，原则上也禁止修改角色编码。
- 停用角色后，其所有用户分配立即失去授权效果，但历史分配记录保留。
- 逻辑删除前必须确认不存在未删除的用户角色分配和角色权限映射。
- 更新时通过 Mapper XML 显式校验并递增 `version`。

### 5.2 角色权限映射表 `aiw_role_permission`

职责：保存一个角色具有的权限编码，是角色与权限之间的多对多映射记录。

```sql
CREATE TABLE aiw_role_permission (
    biz_id CHAR(36) NOT NULL COMMENT '角色权限映射业务ID，UUID字符串',
    role_code VARCHAR(64) NOT NULL COMMENT '角色编码，逻辑关联aiw_role.role_code',
    permission_code VARCHAR(128) NOT NULL COMMENT '权限编码，格式为领域:资源:动作',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '授权创建时间',
    created_by VARCHAR(64) NULL COMMENT '授权操作者业务ID或系统标识',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    deleted_at DATETIME(3) NULL COMMENT '撤销授权时间',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_aiw_role_permission (role_code, permission_code),
    KEY idx_aiw_role_permission_lookup (role_code, is_deleted),
    KEY idx_aiw_role_permission_reverse (permission_code, is_deleted)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='角色权限映射表';
```

字段约束：

- 相同角色和权限只允许存在一条记录。
- 重新授予已逻辑删除的权限时恢复原记录，不创建重复数据。
- `permission_code` 必须存在于应用权限注册表中。
- 角色停用或删除时不级联删除权限映射，由应用服务显式处理。
- 该表不保存 `user_biz_id`、角色名称、菜单信息或接口 URL。

### 5.3 现有用户角色表 `aiw_user_role`

现有表继续作为用户角色分配表，建议实施阶段补充以下规则：

- `role_code` 必须对应一个未删除角色。
- 新分配角色时，角色必须处于启用状态。
- 有效角色必须同时满足：
  - 用户角色记录未删除。
  - `effective_from` 不晚于当前时间。
  - `effective_to` 为空或晚于当前时间。
  - 对应角色未删除且状态为启用。
- 查询用户权限时，使用有效用户角色关联有效角色权限。
- 建议将表注释调整为“用户角色分配表”，避免与角色定义表混淆。

## 6. 权限计算

用户最终权限集合按以下步骤计算：

1. 查询用户全部有效 `aiw_user_role`。
2. 过滤不存在、停用或已删除的 `aiw_role`。
3. 查询有效角色对应的未删除 `aiw_role_permission`。
4. 使用 Stream 对权限编码去重，形成不可变权限集合。
5. 将角色编码、权限编码和作用域写入会话快照。

权限判断必须使用权限编码，不再以 `hasRole("ADMIN")` 作为业务接口的最终授权依据。角色判断只适用于角色管理等确实以角色身份为规则的场景。

## 7. 会话与缓存

当前 Redis 会话保存角色和权限集合：

```text
SessionData
- userBizId
- credentialVersion
- roleCodes
- permissionCodes
- scopes
- authorizationVersion
```

角色、角色权限或用户角色发生变化后，旧会话不能继续使用旧权限。推荐方案：

- 登录时写入有效角色和权限快照。
- 分配角色、撤销角色、修改角色权限或停用角色后，直接撤销受影响用户的全部会话。
- 兼容升级前的旧会话数据；旧会话缺少权限集合时重新计算当前授权。

禁止只依赖长时间缓存而不提供失效机制。

## 8. 应用与仓储边界

后续实现必须按开发铁律拆分：

| 领域对象 | 仓储接口 | 仓储实现 | Mapper Java | Mapper XML |
| --- | --- | --- | --- | --- |
| `Role` | `RoleStore` | `RoleStoreImpl` | `RoleMapper` | `RoleMapper.xml` |
| `RolePermission` | `RolePermissionStore` | `RolePermissionStoreImpl` | `RolePermissionMapper` | `RolePermissionMapper.xml` |
| `UserRole` | `UserRoleStore` | `UserRoleStoreImpl` | `UserRoleMapper` | `UserRoleMapper.xml` |

约束：

- 一个领域对象对应一个仓储接口、一个实现类、一个 Mapper Java 和一个同名 XML。
- SQL 全部写入 Mapper XML。
- StoreImpl 负责持久化 Entity 与领域对象之间的 Builder 转换。
- Mapper 只执行数据库操作，不组装领域对象、不判断授权规则。
- 角色创建、权限分配、用户授权和会话失效由应用服务编排事务。
- 集合映射、去重和权限汇总使用 Stream。

## 9. 建议用例

首期角色权限管理建议提供以下应用用例：

| 用例 | 所需权限 | 关键规则 |
| --- | --- | --- |
| 创建角色 | `account:role:manage` | 角色编码唯一且创建后不可修改 |
| 修改角色 | `account:role:manage` | 系统角色限制修改敏感字段 |
| 启用或停用角色 | `account:role:manage` | 停用后立即使相关授权失效 |
| 配置角色权限 | `account:role:manage` | 权限编码必须来自注册表 |
| 给用户分配角色 | `account:role:assign` | 角色必须存在并启用 |
| 撤销用户角色 | `account:role:assign` | 操作必须幂等 |
| 查询用户有效权限 | 当前用户或管理权限 | 不返回已失效授权 |

所有写操作必须记录操作者、操作对象、变更前后结果和时间，并进入审计链路。

## 10. 安全约束

- 禁止客户端提交任意权限编码后直接落库。
- 禁止通过角色名称、菜单名称或 URL 进行权限判断。
- 禁止使用 `ADMIN` 隐式绕过权限校验。
- 禁止删除最后一个具备角色管理权限的有效管理员授权。
- 角色权限变更必须使相关会话或授权缓存失效。
- 权限不足统一返回 HTTP 403 和项目统一错误结构。
- Controller 只接收请求和返回结果，授权规则由拦截器或应用服务统一处理。
- 未识别异常继续由全局异常处理器返回系统错误。

## 11. 查询与索引场景

主要查询路径：

1. 按 `role_code` 查询角色定义。
2. 分页查询启用或停用角色。
3. 按 `user_biz_id` 查询用户有效角色。
4. 按 `role_code` 查询角色全部有效权限。
5. 按 `permission_code` 反查具有该权限的角色，用于权限影响分析。

索引只服务以上明确查询。实施后必须使用真实 SQL 检查执行计划，禁止为低选择性字段盲目增加单列索引。

## 12. 迁移与兼容方案

已经通过新的 Flyway 迁移文件实施，未修改已存在的 `V1__account_domain.sql`：

1. 创建 `aiw_role`。
2. 创建 `aiw_role_permission`。
3. 初始化 `USER`、`ADVISOR`、`RISK`、`AUDITOR`、`ADMIN` 系统角色。
4. 按当前接口行为初始化最小权限集合。
5. 校验现有 `aiw_user_role.role_code` 均能匹配角色定义。
6. 上线新的权限计算和会话失效逻辑。
7. 将现有仅判断 `ADMIN` 的接口逐项迁移为权限编码判断。

迁移脚本建议命名为：

```text
V6__role_and_permission_model.sql
```

回滚不通过删除历史迁移完成。若上线后需要停止使用新模型，应通过后续迁移和功能开关回退鉴权读取路径。

## 13. 测试与验收

实施阶段至少覆盖：

- 角色编码唯一性。
- 已停用或已删除角色不能分配。
- 角色权限重复授予时不产生重复记录。
- 逻辑删除的角色权限可以恢复。
- 用户角色有效期计算。
- 多角色权限合并与去重。
- 角色停用、权限撤销和用户角色撤销后的会话失效。
- 普通用户不能管理角色或给自己提权。
- 最后一个角色管理员保护。
- Mapper Java 方法与 Mapper XML statement 一一对应。
- MySQL 8 Flyway 迁移执行和关键 SQL 集成测试。

## 14. 暂不实施

以下能力不进入首期：

- 用户级允许或拒绝权限覆盖。
- 资源实例级 ACL。
- 菜单、按钮和前端路由配置。
- 多租户角色继承。
- 角色层级和权限继承。
- 动态表达式、ABAC 和策略脚本。
- 独立权限定义表。

这些能力必须基于真实业务需求单独设计，禁止提前把通用授权平台复杂度引入账户域。
