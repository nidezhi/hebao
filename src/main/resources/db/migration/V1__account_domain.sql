-- ============================================================================
-- DZCOM 初始数据库 - V1 账户域
-- 目标：
--   1. 将用户主体、登录标识、密码凭据、个人资料、风险画像拆分，降低字段耦合。
--   2. 支持用户名、邮箱、手机号以及未来 OAuth 等多种登录标识。
--   3. 不创建数据库外键；跨表一致性由账户域应用服务、事务和巡检任务负责。
--   4. 所有逻辑关联字段均建立必要索引，保证常用查询性能。
-- 兼容：MySQL 8.0+
-- ============================================================================

-- 用户主体表：只保存稳定身份、生命周期状态和审计信息。
CREATE TABLE aiw_user (
    biz_id CHAR(36) NOT NULL COMMENT '用户业务ID，UUID字符串；跨模块仅传递此ID，不建立外键',
    user_no VARCHAR(32) NOT NULL COMMENT '用户编号，面向运营和客服展示，创建后不可变',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '账户状态：0-禁用，1-正常，2-锁定',
    version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号，防止并发更新覆盖',
    registered_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '注册完成时间',
    last_login_at DATETIME(3) NULL COMMENT '最近一次成功登录时间，仅用于展示和安全分析',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '记录最后更新时间',
    created_by VARCHAR(64) NULL COMMENT '创建操作者业务ID或系统标识',
    updated_by VARCHAR(64) NULL COMMENT '最后更新操作者业务ID或系统标识',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    deleted_at DATETIME(3) NULL COMMENT '逻辑删除时间',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_aiw_user_user_no (user_no),
    KEY idx_aiw_user_status_created (status, is_deleted, created_at),
    KEY idx_aiw_user_last_login (last_login_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='用户主体表；不保存登录名、密码、联系方式和风险画像';

-- 用户登录标识表：一个用户可按类型拥有用户名、邮箱、手机号或第三方身份。
CREATE TABLE aiw_user_identity (
    biz_id CHAR(36) NOT NULL COMMENT '登录标识业务ID，UUID字符串',
    user_biz_id CHAR(36) NOT NULL COMMENT '所属用户业务ID；逻辑关联aiw_user.biz_id，无外键',
    identity_type VARCHAR(32) NOT NULL COMMENT '标识类型：USERNAME、EMAIL、PHONE、OAUTH_*',
    identity_value VARCHAR(256) NOT NULL COMMENT '原始标识值，用于必要的展示；敏感值按接口权限脱敏',
    normalized_value VARCHAR(256) NOT NULL COMMENT '标准化标识值，用于登录查询和唯一性判断',
    verified TINYINT NOT NULL DEFAULT 0 COMMENT '验证状态：0-未验证，1-已验证',
    verified_at DATETIME(3) NULL COMMENT '完成验证的时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '标识状态：0-停用，1-可用',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '记录最后更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_aiw_user_identity_type_value (identity_type, normalized_value),
    UNIQUE KEY uk_aiw_user_identity_user_type (user_biz_id, identity_type),
    KEY idx_aiw_user_identity_user_status (user_biz_id, status, is_deleted)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='用户登录标识表；通过逻辑业务ID关联用户主体';

-- 用户凭据表：隔离密码等认证秘密，便于未来支持密码算法升级和其他凭据类型。
CREATE TABLE aiw_user_credential (
    biz_id CHAR(36) NOT NULL COMMENT '凭据业务ID，UUID字符串',
    user_biz_id CHAR(36) NOT NULL COMMENT '所属用户业务ID；逻辑关联aiw_user.biz_id，无外键',
    credential_type VARCHAR(32) NOT NULL DEFAULT 'PASSWORD'
        COMMENT '凭据类型：PASSWORD；为未来PIN、PASSKEY等预留',
    secret_hash VARCHAR(512) NOT NULL COMMENT '凭据哈希；禁止保存明文、禁止写入日志',
    hash_algorithm VARCHAR(32) NOT NULL DEFAULT 'BCRYPT' COMMENT '哈希算法标识',
    credential_version INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '凭据版本，改密后递增，用于会话失效判断',
    expires_at DATETIME(3) NULL COMMENT '凭据过期时间；NULL表示不强制过期',
    changed_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '最近一次凭据变更时间',
    failed_attempts INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '连续认证失败次数',
    locked_until DATETIME(3) NULL COMMENT '凭据临时锁定截止时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '记录最后更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_aiw_user_credential_user_type (user_biz_id, credential_type),
    KEY idx_aiw_user_credential_lock (user_biz_id, locked_until, is_deleted)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='用户认证凭据表；与用户主体隔离存储';

-- 用户资料表：保存非认证用途的展示和本地化信息。
CREATE TABLE aiw_user_profile (
    biz_id CHAR(36) NOT NULL COMMENT '用户资料业务ID，UUID字符串',
    user_biz_id CHAR(36) NOT NULL COMMENT '所属用户业务ID；逻辑关联aiw_user.biz_id，无外键',
    nickname VARCHAR(64) NULL COMMENT '用户昵称',
    avatar_url VARCHAR(512) NULL COMMENT '头像资源地址',
    locale VARCHAR(16) NOT NULL DEFAULT 'zh-CN' COMMENT '语言和地区标识',
    timezone VARCHAR(64) NOT NULL DEFAULT 'Asia/Shanghai' COMMENT '用户时区',
    profile_ext JSON NULL COMMENT '低频扩展资料；禁止存放密码、证件号等高敏感信息',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '记录最后更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_aiw_user_profile_user (user_biz_id),
    KEY idx_aiw_user_profile_nickname (nickname)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='用户展示资料表';

-- 用户风险画像表：与账户主体解耦，后续可独立演进KYC和风险测评流程。
CREATE TABLE aiw_user_risk_profile (
    biz_id CHAR(36) NOT NULL COMMENT '风险画像业务ID，UUID字符串',
    user_biz_id CHAR(36) NOT NULL COMMENT '所属用户业务ID；逻辑关联aiw_user.biz_id，无外键',
    kyc_status TINYINT NOT NULL DEFAULT 0 COMMENT 'KYC状态：0-未认证，1-已认证，2-审核中，3-拒绝',
    risk_level TINYINT NOT NULL DEFAULT 1 COMMENT '风险承受等级：1-保守至5-进取',
    assessment_version VARCHAR(32) NULL COMMENT '最近一次风险测评问卷或模型版本',
    assessed_at DATETIME(3) NULL COMMENT '最近一次风险测评完成时间',
    kyc_reviewed_at DATETIME(3) NULL COMMENT '最近一次KYC审核完成时间',
    ext_data JSON NULL COMMENT '风险画像扩展数据；字段变稳定后应迁移为显式列',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '记录最后更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_aiw_user_risk_profile_user (user_biz_id),
    KEY idx_aiw_user_risk_status (kyc_status, risk_level, is_deleted)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='用户KYC与风险画像表';

-- 用户角色表：只保存角色分配，不绑定具体权限模型，便于后续接入独立权限上下文。
CREATE TABLE aiw_user_role (
    biz_id CHAR(36) NOT NULL COMMENT '角色分配业务ID，UUID字符串',
    user_biz_id CHAR(36) NOT NULL COMMENT '所属用户业务ID；逻辑关联aiw_user.biz_id，无外键',
    role_code VARCHAR(64) NOT NULL COMMENT '角色编码：USER、ADVISOR、RISK、ADMIN等',
    scope_code VARCHAR(64) NOT NULL DEFAULT 'GLOBAL' COMMENT '角色作用域编码，首版默认GLOBAL',
    effective_from DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '角色生效时间',
    effective_to DATETIME(3) NULL COMMENT '角色失效时间；NULL表示长期有效',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
    created_by VARCHAR(64) NULL COMMENT '分配角色的操作者业务ID或系统标识',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_aiw_user_role_assignment (user_biz_id, role_code, scope_code),
    KEY idx_aiw_user_role_role (role_code, effective_to, is_deleted)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='用户角色分配表；不建立数据库外键';

-- 用户偏好表：保存可配置偏好，稳定业务字段不得长期堆积在JSON中。
CREATE TABLE aiw_user_preference (
    biz_id CHAR(36) NOT NULL COMMENT '偏好业务ID，UUID字符串',
    user_biz_id CHAR(36) NOT NULL COMMENT '所属用户业务ID；逻辑关联aiw_user.biz_id，无外键',
    preference_key VARCHAR(64) NOT NULL COMMENT '偏好键，必须由应用白名单或命名规则约束',
    value_type VARCHAR(16) NOT NULL DEFAULT 'STRING'
        COMMENT '值类型：STRING、NUMBER、BOOLEAN、JSON',
    preference_value JSON NOT NULL COMMENT '偏好值，统一使用JSON存储基础类型或小型对象',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '记录最后更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_aiw_user_preference_key (user_biz_id, preference_key),
    KEY idx_aiw_user_preference_user (user_biz_id, is_deleted, updated_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='用户偏好表；通过逻辑业务ID关联用户主体';
