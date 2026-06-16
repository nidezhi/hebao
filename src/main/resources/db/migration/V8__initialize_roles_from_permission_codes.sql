-- ============================================================================
-- DZCOM V8 - 基于 PermissionCodes 初始化系统角色权限
-- 目标：
--   1. 以 application.service.account.PermissionCodes 为唯一权限编码来源。
--   2. 幂等补齐系统角色和默认角色权限，避免回改已发布的 V6 迁移。
--   3. 不新增代码未注册的权限编码，不在 SQL 中发明动态权限。
-- 兼容：MySQL 8.0+
-- ============================================================================

INSERT INTO aiw_role
(biz_id, role_code, role_name, description, role_type, status, version,
 created_at, updated_at, created_by, updated_by, is_deleted, deleted_at)
VALUES
('00000000-0000-0000-0000-000000000101', 'USER', '普通用户',
 '个人账户基础角色；不默认授予后台管理权限', 'SYSTEM', 1, 0,
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM', 0, NULL),
('00000000-0000-0000-0000-000000000102', 'ADVISOR', '投顾人员',
 '查看授权客户信息并提供投资辅助服务', 'SYSTEM', 1, 0,
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM', 0, NULL),
('00000000-0000-0000-0000-000000000103', 'RISK', '风控人员',
 '查看用户信息并维护 KYC、风险等级和账户状态', 'SYSTEM', 1, 0,
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM', 0, NULL),
('00000000-0000-0000-0000-000000000104', 'AUDITOR', '审计人员',
 '只读查看用户与角色授权，不默认具有业务写权限', 'SYSTEM', 1, 0,
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM', 0, NULL),
('00000000-0000-0000-0000-000000000105', 'ADMIN', '系统管理员',
 '系统管理角色；具体能力由角色权限映射决定', 'SYSTEM', 1, 0,
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM', 0, NULL)
ON DUPLICATE KEY UPDATE
role_name = VALUES(role_name),
description = VALUES(description),
role_type = VALUES(role_type),
status = VALUES(status),
updated_at = CURRENT_TIMESTAMP(3),
updated_by = 'SYSTEM',
is_deleted = 0,
deleted_at = NULL;

INSERT INTO aiw_role_permission
(biz_id, role_code, permission_code, created_at, created_by, is_deleted, deleted_at)
VALUES
-- ADMIN：拥有 PermissionCodes 当前注册的全部权限。
('00000000-0000-0000-0008-000000000001', 'ADMIN', 'account:user:read',
 CURRENT_TIMESTAMP(3), 'SYSTEM', 0, NULL),
('00000000-0000-0000-0008-000000000002', 'ADMIN', 'account:user:create',
 CURRENT_TIMESTAMP(3), 'SYSTEM', 0, NULL),
('00000000-0000-0000-0008-000000000003', 'ADMIN', 'account:user:update',
 CURRENT_TIMESTAMP(3), 'SYSTEM', 0, NULL),
('00000000-0000-0000-0008-000000000004', 'ADMIN', 'account:user:update-status',
 CURRENT_TIMESTAMP(3), 'SYSTEM', 0, NULL),
('00000000-0000-0000-0008-000000000005', 'ADMIN', 'account:user:update-kyc',
 CURRENT_TIMESTAMP(3), 'SYSTEM', 0, NULL),
('00000000-0000-0000-0008-000000000006', 'ADMIN', 'account:user:update-risk',
 CURRENT_TIMESTAMP(3), 'SYSTEM', 0, NULL),
('00000000-0000-0000-0008-000000000007', 'ADMIN', 'account:user:delete',
 CURRENT_TIMESTAMP(3), 'SYSTEM', 0, NULL),
('00000000-0000-0000-0008-000000000008', 'ADMIN', 'account:role:read',
 CURRENT_TIMESTAMP(3), 'SYSTEM', 0, NULL),
('00000000-0000-0000-0008-000000000009', 'ADMIN', 'account:role:manage',
 CURRENT_TIMESTAMP(3), 'SYSTEM', 0, NULL),
('00000000-0000-0000-0008-000000000010', 'ADMIN', 'account:role:assign',
 CURRENT_TIMESTAMP(3), 'SYSTEM', 0, NULL),
('00000000-0000-0000-0008-000000000011', 'ADMIN', 'product:catalog:manage',
 CURRENT_TIMESTAMP(3), 'SYSTEM', 0, NULL),
('00000000-0000-0000-0008-000000000012', 'ADMIN', 'market:quote:write',
 CURRENT_TIMESTAMP(3), 'SYSTEM', 0, NULL),

-- ADVISOR：投顾只读查看客户信息，不默认获得用户修改和角色管理权限。
('00000000-0000-0000-0008-000000000013', 'ADVISOR', 'account:user:read',
 CURRENT_TIMESTAMP(3), 'SYSTEM', 0, NULL),

-- RISK：风控可读用户信息，并维护 KYC、风险等级和账户状态。
('00000000-0000-0000-0008-000000000014', 'RISK', 'account:user:read',
 CURRENT_TIMESTAMP(3), 'SYSTEM', 0, NULL),
('00000000-0000-0000-0008-000000000015', 'RISK', 'account:user:update-status',
 CURRENT_TIMESTAMP(3), 'SYSTEM', 0, NULL),
('00000000-0000-0000-0008-000000000016', 'RISK', 'account:user:update-kyc',
 CURRENT_TIMESTAMP(3), 'SYSTEM', 0, NULL),
('00000000-0000-0000-0008-000000000017', 'RISK', 'account:user:update-risk',
 CURRENT_TIMESTAMP(3), 'SYSTEM', 0, NULL),

-- AUDITOR：当前代码未注册 audit:log:read，先授予账户与角色只读权限。
('00000000-0000-0000-0008-000000000018', 'AUDITOR', 'account:user:read',
 CURRENT_TIMESTAMP(3), 'SYSTEM', 0, NULL),
('00000000-0000-0000-0008-000000000019', 'AUDITOR', 'account:role:read',
 CURRENT_TIMESTAMP(3), 'SYSTEM', 0, NULL)
ON DUPLICATE KEY UPDATE
created_by = 'SYSTEM',
is_deleted = 0,
deleted_at = NULL;
