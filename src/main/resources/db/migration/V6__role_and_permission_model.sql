-- ============================================================================
-- DZCOM V6 - 角色与权限模型
-- 目标：
--   1. 新增独立角色定义，现有 aiw_user_role 继续作为用户角色分配表。
--   2. 新增角色权限映射，以稳定权限编码替代 ADMIN 角色硬编码鉴权。
--   3. 不创建数据库外键，关联一致性由账户应用服务和巡检负责。
-- 兼容：MySQL 8.0+
-- ============================================================================

CREATE TABLE aiw_role (
    biz_id CHAR(36) NOT NULL COMMENT '角色业务ID，UUID字符串',
    role_code VARCHAR(64) NOT NULL COMMENT '稳定角色编码，创建后不可修改',
    role_name VARCHAR(128) NOT NULL COMMENT '角色显示名称',
    description VARCHAR(512) NULL COMMENT '角色职责和授权边界说明',
    role_type VARCHAR(16) NOT NULL DEFAULT 'CUSTOM' COMMENT '角色类型：SYSTEM、CUSTOM',
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

INSERT INTO aiw_role
(biz_id, role_code, role_name, description, role_type, status, version,
 created_at, updated_at, created_by, updated_by, is_deleted)
VALUES
('00000000-0000-0000-0000-000000000101', 'USER', '普通用户', '个人账户基础角色', 'SYSTEM', 1, 0,
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM', 0),
('00000000-0000-0000-0000-000000000102', 'ADVISOR', '投顾人员', '投资顾问和运营服务角色', 'SYSTEM', 1, 0,
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM', 0),
('00000000-0000-0000-0000-000000000103', 'RISK', '风控人员', '风险检查和风险处置角色', 'SYSTEM', 1, 0,
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM', 0),
('00000000-0000-0000-0000-000000000104', 'AUDITOR', '审计人员', '审计记录只读角色', 'SYSTEM', 1, 0,
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM', 0),
('00000000-0000-0000-0000-000000000105', 'ADMIN', '系统管理员', '系统管理角色，权限仍由角色权限映射决定', 'SYSTEM', 1, 0,
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM', 0);

INSERT INTO aiw_role_permission
(biz_id, role_code, permission_code, created_at, created_by, is_deleted)
VALUES
('00000000-0000-0000-0001-000000000001', 'ADMIN', 'account:user:read', CURRENT_TIMESTAMP(3), 'SYSTEM', 0),
('00000000-0000-0000-0001-000000000002', 'ADMIN', 'account:user:create', CURRENT_TIMESTAMP(3), 'SYSTEM', 0),
('00000000-0000-0000-0001-000000000003', 'ADMIN', 'account:user:update', CURRENT_TIMESTAMP(3), 'SYSTEM', 0),
('00000000-0000-0000-0001-000000000004', 'ADMIN', 'account:user:update-status', CURRENT_TIMESTAMP(3), 'SYSTEM', 0),
('00000000-0000-0000-0001-000000000005', 'ADMIN', 'account:user:update-kyc', CURRENT_TIMESTAMP(3), 'SYSTEM', 0),
('00000000-0000-0000-0001-000000000006', 'ADMIN', 'account:user:update-risk', CURRENT_TIMESTAMP(3), 'SYSTEM', 0),
('00000000-0000-0000-0001-000000000007', 'ADMIN', 'account:user:delete', CURRENT_TIMESTAMP(3), 'SYSTEM', 0),
('00000000-0000-0000-0001-000000000008', 'ADMIN', 'account:role:read', CURRENT_TIMESTAMP(3), 'SYSTEM', 0),
('00000000-0000-0000-0001-000000000009', 'ADMIN', 'account:role:manage', CURRENT_TIMESTAMP(3), 'SYSTEM', 0),
('00000000-0000-0000-0001-000000000010', 'ADMIN', 'account:role:assign', CURRENT_TIMESTAMP(3), 'SYSTEM', 0),
('00000000-0000-0000-0001-000000000011', 'ADMIN', 'product:catalog:manage', CURRENT_TIMESTAMP(3), 'SYSTEM', 0),
('00000000-0000-0000-0001-000000000012', 'ADMIN', 'market:quote:write', CURRENT_TIMESTAMP(3), 'SYSTEM', 0),
('00000000-0000-0000-0001-000000000013', 'ADVISOR', 'account:user:read', CURRENT_TIMESTAMP(3), 'SYSTEM', 0),
('00000000-0000-0000-0001-000000000014', 'RISK', 'account:user:read', CURRENT_TIMESTAMP(3), 'SYSTEM', 0),
('00000000-0000-0000-0001-000000000015', 'RISK', 'account:user:update-kyc', CURRENT_TIMESTAMP(3), 'SYSTEM', 0),
('00000000-0000-0000-0001-000000000016', 'RISK', 'account:user:update-risk', CURRENT_TIMESTAMP(3), 'SYSTEM', 0);
