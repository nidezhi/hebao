-- =====================================================
-- AI Wealth Platform - Phase 2: Risk & Compliance
-- Phase 2: 风控与合规表
-- Execution Order: 4
-- Depends on: V1 (aiw_user)
-- =====================================================

-- 1. 风控规则表 (aiw_risk_rule)
CREATE TABLE IF NOT EXISTS aiw_risk_rule (
    biz_id VARCHAR(64) PRIMARY KEY COMMENT '规则业务ID',
    rule_code VARCHAR(32) UNIQUE NOT NULL COMMENT '规则编码',
    rule_name VARCHAR(128) NOT NULL COMMENT '规则名称',
    rule_type VARCHAR(32) NOT NULL COMMENT '规则类型 (LIMIT/BLACKLIST/AML)',
    rule_config JSON NOT NULL COMMENT '规则配置',
    priority INT DEFAULT 0 COMMENT '优先级',
    status TINYINT DEFAULT 1 COMMENT '状态 (0:禁用, 1:启用)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    
    INDEX idx_rule_code (rule_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控规则表';

-- 2. 风控检查记录表 (aiw_risk_check_log)
CREATE TABLE IF NOT EXISTS aiw_risk_check_log (
    biz_id VARCHAR(64) PRIMARY KEY COMMENT '检查记录ID',
    business_type VARCHAR(32) NOT NULL COMMENT '业务类型 (ORDER/WITHDRAW)',
    business_biz_id VARCHAR(64) NOT NULL COMMENT '业务ID (如订单ID)',
    user_biz_id VARCHAR(64) NOT NULL COMMENT '用户业务ID',
    rule_biz_id VARCHAR(64) COMMENT '规则业务ID',
    check_result TINYINT NOT NULL COMMENT '检查结果 (0:拒绝, 1:通过, 2:警告)',
    risk_level TINYINT COMMENT '风险等级 (1-5)',
    check_detail TEXT COMMENT '检查详情',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_business (business_type, business_biz_id),
    INDEX idx_user_created (user_biz_id, created_at),
    FOREIGN KEY (user_biz_id) REFERENCES aiw_user(biz_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控检查记录表';

-- 3. 审计日志表 (aiw_audit_log)
CREATE TABLE IF NOT EXISTS aiw_audit_log (
    biz_id VARCHAR(64) PRIMARY KEY COMMENT '审计ID',
    user_biz_id VARCHAR(64) COMMENT '用户业务ID',
    action_type VARCHAR(64) NOT NULL COMMENT '操作类型',
    resource_type VARCHAR(64) COMMENT '资源类型',
    resource_biz_id VARCHAR(64) COMMENT '资源业务ID',
    request_data JSON COMMENT '请求数据',
    response_data JSON COMMENT '响应数据',
    ip_address VARCHAR(64) COMMENT 'IP地址',
    user_agent VARCHAR(256) COMMENT '用户代理',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_user_action (user_biz_id, action_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';
