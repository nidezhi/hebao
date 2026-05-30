-- =====================================================
-- AI Wealth Platform - Phase 1: Core Tables (MVP)
-- Phase 1: 核心表（用户、产品、投资组合）
-- Execution Order: 1
-- =====================================================

-- 1. 用户基础表 (aiw_user)
CREATE TABLE IF NOT EXISTS aiw_user (
    biz_id VARCHAR(64) PRIMARY KEY COMMENT '用户业务ID (UUID)',
    user_no VARCHAR(32) UNIQUE NOT NULL COMMENT '用户编号 (业务可读)',
    username VARCHAR(64) NOT NULL COMMENT '用户名',
    email VARCHAR(128) UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    password_hash VARCHAR(256) NOT NULL COMMENT '密码哈希',
    kyc_status TINYINT DEFAULT 0 COMMENT 'KYC状态 (0:未认证, 1:已认证, 2:审核中)',
    risk_level TINYINT DEFAULT 1 COMMENT '风险承受能力等级 (1-5)',
    status TINYINT DEFAULT 1 COMMENT '账户状态 (0:禁用, 1:正常)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(64) COMMENT '创建人',
    updated_by VARCHAR(64) COMMENT '更新人',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除 (0:否, 1:是)',
    
    INDEX idx_user_no (user_no),
    INDEX idx_email (email),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户基础表';

-- 2. 用户偏好表 (aiw_user_preference)
CREATE TABLE IF NOT EXISTS aiw_user_preference (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键',
    biz_id VARCHAR(64) NOT NULL COMMENT '用户业务ID',
    preference_key VARCHAR(64) NOT NULL COMMENT '偏好键',
    preference_value TEXT NOT NULL COMMENT '偏好值',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    
    INDEX idx_biz_id (biz_id),
    FOREIGN KEY (biz_id) REFERENCES aiw_user(biz_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户偏好表';

-- 3. 产品基础表 (aiw_product)
CREATE TABLE IF NOT EXISTS aiw_product (
    biz_id VARCHAR(64) PRIMARY KEY COMMENT '产品业务ID (UUID)',
    product_code VARCHAR(32) UNIQUE NOT NULL COMMENT '产品代码 (如股票代码)',
    product_name VARCHAR(128) NOT NULL COMMENT '产品名称',
    product_type VARCHAR(32) NOT NULL COMMENT '产品类型 (stock/fund/bond/etf/option/future)',
    exchange VARCHAR(32) COMMENT '交易所',
    currency VARCHAR(8) DEFAULT 'CNY' COMMENT '币种 (CNY/USD/HKD)',
    min_invest_amount DECIMAL(18,2) DEFAULT 0 COMMENT '最小投资金额',
    fee_rate DECIMAL(6,4) DEFAULT 0 COMMENT '费率',
    status TINYINT DEFAULT 1 COMMENT '产品状态 (0:下架, 1:上架)',
    listing_date DATE COMMENT '上市日期',
    description TEXT COMMENT '产品描述',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    
    INDEX idx_product_code (product_code),
    INDEX idx_product_type (product_type),
    INDEX idx_exchange (exchange)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品基础表';
