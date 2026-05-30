-- =====================================================
-- AI Wealth Platform - Phase 1: Portfolio & Orders
-- Phase 1: 投资组合与订单表（核心交易能力）
-- Execution Order: 2
-- Depends on: V1 (aiw_user, aiw_product)
-- =====================================================

-- 1. 投资组合表 (aiw_portfolio)
CREATE TABLE IF NOT EXISTS aiw_portfolio (
    biz_id VARCHAR(64) PRIMARY KEY COMMENT '组合业务ID (UUID)',
    user_biz_id VARCHAR(64) NOT NULL COMMENT '用户业务ID',
    portfolio_name VARCHAR(128) NOT NULL COMMENT '组合名称',
    total_asset DECIMAL(18,2) DEFAULT 0 COMMENT '总资产',
    available_cash DECIMAL(18,2) DEFAULT 0 COMMENT '可用现金',
    total_profit DECIMAL(18,2) DEFAULT 0 COMMENT '总盈亏',
    profit_rate DECIMAL(6,4) DEFAULT 0 COMMENT '收益率',
    status TINYINT DEFAULT 1 COMMENT '状态 (0:关闭, 1:活跃)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    
    INDEX idx_user_biz_id (user_biz_id),
    FOREIGN KEY (user_biz_id) REFERENCES aiw_user(biz_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投资组合表';

-- 2. 持仓明细表 (aiw_position)
CREATE TABLE IF NOT EXISTS aiw_position (
    biz_id VARCHAR(64) PRIMARY KEY COMMENT '持仓业务ID (UUID)',
    portfolio_biz_id VARCHAR(64) NOT NULL COMMENT '组合业务ID',
    product_biz_id VARCHAR(64) NOT NULL COMMENT '产品业务ID',
    quantity DECIMAL(18,4) NOT NULL COMMENT '持有数量',
    avg_cost DECIMAL(18,4) NOT NULL COMMENT '平均成本',
    current_price DECIMAL(18,4) COMMENT '当前价格',
    market_value DECIMAL(18,2) COMMENT '市值',
    unrealized_profit DECIMAL(18,2) COMMENT '未实现盈亏',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    
    INDEX idx_portfolio_product (portfolio_biz_id, product_biz_id),
    FOREIGN KEY (portfolio_biz_id) REFERENCES aiw_portfolio(biz_id) ON DELETE CASCADE,
    FOREIGN KEY (product_biz_id) REFERENCES aiw_product(biz_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='持仓明细表';

-- 3. 订单表 (aiw_order)
CREATE TABLE IF NOT EXISTS aiw_order (
    biz_id VARCHAR(64) PRIMARY KEY COMMENT '订单业务ID (UUID)',
    order_no VARCHAR(32) UNIQUE NOT NULL COMMENT '订单编号 (业务可读)',
    user_biz_id VARCHAR(64) NOT NULL COMMENT '用户业务ID',
    portfolio_biz_id VARCHAR(64) NOT NULL COMMENT '组合业务ID',
    product_biz_id VARCHAR(64) NOT NULL COMMENT '产品业务ID',
    order_type VARCHAR(16) NOT NULL COMMENT '订单类型 (BUY/SELL)',
    order_price DECIMAL(18,4) NOT NULL COMMENT '订单价格',
    quantity DECIMAL(18,4) NOT NULL COMMENT '数量',
    total_amount DECIMAL(18,2) NOT NULL COMMENT '总金额',
    status VARCHAR(16) DEFAULT 'PENDING' COMMENT '订单状态 (PENDING/FILLED/CANCELLED/FAILED)',
    executed_price DECIMAL(18,4) COMMENT '成交价格',
    executed_quantity DECIMAL(18,4) COMMENT '成交数量',
    executed_at DATETIME COMMENT '成交时间',
    broker_order_id VARCHAR(64) COMMENT '券商订单ID',
    remark VARCHAR(512) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    
    INDEX idx_order_no (order_no),
    INDEX idx_user_status (user_biz_id, status),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_biz_id) REFERENCES aiw_user(biz_id) ON DELETE CASCADE,
    FOREIGN KEY (portfolio_biz_id) REFERENCES aiw_portfolio(biz_id) ON DELETE CASCADE,
    FOREIGN KEY (product_biz_id) REFERENCES aiw_product(biz_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 4. 订单流水表 (aiw_order_log)
CREATE TABLE IF NOT EXISTS aiw_order_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键',
    order_biz_id VARCHAR(64) NOT NULL COMMENT '订单业务ID',
    log_type VARCHAR(32) NOT NULL COMMENT '日志类型 (CREATE/UPDATE/EXECUTE/CANCEL)',
    old_status VARCHAR(16) COMMENT '旧状态',
    new_status VARCHAR(16) COMMENT '新状态',
    log_content TEXT COMMENT '日志内容',
    operator VARCHAR(64) COMMENT '操作人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_order_created (order_biz_id, created_at),
    FOREIGN KEY (order_biz_id) REFERENCES aiw_order(biz_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单流水表';
