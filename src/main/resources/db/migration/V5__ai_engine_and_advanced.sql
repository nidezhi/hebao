-- =====================================================
-- AI Wealth Platform - Phase 3: AI Engine & Advanced Features
-- Phase 3: AI分析与高级特性表
-- Execution Order: 5
-- Depends on: V1 (aiw_user, aiw_product)
-- =====================================================

-- 1. AI推荐信号表 (aiw_ai_signal)
CREATE TABLE IF NOT EXISTS aiw_ai_signal (
    biz_id VARCHAR(64) PRIMARY KEY COMMENT '信号业务ID',
    signal_type VARCHAR(32) NOT NULL COMMENT '信号类型 (RECOMMEND/FACTOR/SENTIMENT)',
    product_biz_id VARCHAR(64) NOT NULL COMMENT '产品业务ID',
    signal_value DECIMAL(10,4) COMMENT '信号值',
    confidence DECIMAL(3,2) COMMENT '置信度 (0-1)',
    model_version VARCHAR(32) NOT NULL COMMENT '模型版本',
    factors JSON COMMENT '因子详情',
    explanation TEXT COMMENT '可解释说明',
    valid_from DATETIME NOT NULL COMMENT '生效时间',
    valid_to DATETIME COMMENT '失效时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    
    INDEX idx_product_valid (product_biz_id, valid_from, valid_to),
    INDEX idx_signal_type (signal_type),
    FOREIGN KEY (product_biz_id) REFERENCES aiw_product(biz_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI推荐信号表';

-- 2. 策略回测结果表 (aiw_backtest_result)
CREATE TABLE IF NOT EXISTS aiw_backtest_result (
    biz_id VARCHAR(64) PRIMARY KEY COMMENT '回测业务ID',
    user_biz_id VARCHAR(64) NOT NULL COMMENT '用户业务ID',
    strategy_name VARCHAR(128) NOT NULL COMMENT '策略名称',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    initial_capital DECIMAL(18,2) NOT NULL COMMENT '初始资金',
    final_capital DECIMAL(18,2) NOT NULL COMMENT '最终资金',
    total_return DECIMAL(6,4) COMMENT '总收益率',
    sharpe_ratio DECIMAL(6,4) COMMENT '夏普比率',
    max_drawdown DECIMAL(6,4) COMMENT '最大回撤',
    win_rate DECIMAL(5,4) COMMENT '胜率',
    params JSON COMMENT '策略参数',
    result_data LONGTEXT COMMENT '详细结果数据',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    
    INDEX idx_user_created (user_biz_id, created_at),
    FOREIGN KEY (user_biz_id) REFERENCES aiw_user(biz_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='策略回测结果表';

-- 3. 产品元数据表 (aiw_product_metadata)
CREATE TABLE IF NOT EXISTS aiw_product_metadata (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键',
    biz_id VARCHAR(64) NOT NULL COMMENT '产品业务ID',
    metadata_key VARCHAR(64) NOT NULL COMMENT '元数据键 (如pe_ratio, market_cap)',
    metadata_value VARCHAR(512) NOT NULL COMMENT '元数据值',
    data_date DATE NOT NULL COMMENT '数据日期',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    
    INDEX idx_biz_id (biz_id),
    INDEX idx_biz_key_date (biz_id, metadata_key, data_date),
    FOREIGN KEY (biz_id) REFERENCES aiw_product(biz_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品元数据表';

-- 4. 系统参数表 (aiw_system_config)
CREATE TABLE IF NOT EXISTS aiw_system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键',
    config_key VARCHAR(64) UNIQUE NOT NULL COMMENT '配置键',
    config_value TEXT NOT NULL COMMENT '配置值',
    config_group VARCHAR(32) COMMENT '配置分组',
    description VARCHAR(256) COMMENT '描述',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    
    INDEX idx_config_group (config_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数表';
