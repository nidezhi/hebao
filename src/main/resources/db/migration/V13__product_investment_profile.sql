-- ============================================================
-- V13 产品投资画像和主题关系
-- 1. 产品风险和交易画像成为一等数据，后续 Mock 交易和投资报告必须引用。
-- 2. 产品与主题、行业、资产类别建立显式关系，替代任务参数中的临时字符串映射。
-- ============================================================

CREATE TABLE aiw_product_investment_profile (
    biz_id CHAR(36) NOT NULL COMMENT '画像业务ID',
    product_biz_id CHAR(36) NOT NULL COMMENT '产品业务ID',
    asset_class VARCHAR(32) NOT NULL COMMENT '资产类别：STOCK/ETF/FUND/BOND/BANK_WMP/GOLD/REIT等',
    risk_summary VARCHAR(512) NULL COMMENT '风险摘要，前端产品详情页展示',
    volatility_level VARCHAR(16) NOT NULL DEFAULT 'MEDIUM' COMMENT '波动等级：LOW/MEDIUM/HIGH',
    liquidity_level VARCHAR(16) NOT NULL DEFAULT 'MEDIUM' COMMENT '流动性等级：LOW/MEDIUM/HIGH',
    max_drawdown DECIMAL(18, 8) NULL COMMENT '历史或估算最大回撤，小数形式',
    suitable_risk_level INT NOT NULL COMMENT '适配用户风险等级，1-5',
    mock_tradable TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否允许进入Mock交易',
    min_holding_days INT NOT NULL DEFAULT 0 COMMENT '建议最短持有天数',
    trading_notes VARCHAR(1024) NULL COMMENT '交易约束和注意事项',
    data_quality_score DECIMAL(10, 4) NOT NULL DEFAULT 0 COMMENT '画像数据质量分，0-1',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_product_investment_profile_product (product_biz_id),
    KEY idx_product_investment_profile_asset (asset_class, suitable_risk_level, mock_tradable)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品投资风险和交易画像';

CREATE TABLE aiw_product_theme_relation (
    biz_id CHAR(36) NOT NULL COMMENT '关系业务ID',
    product_biz_id CHAR(36) NOT NULL COMMENT '产品业务ID',
    relation_type VARCHAR(32) NOT NULL COMMENT '关系类型：THEME/INDUSTRY/INDEX/ASSET_CLASS',
    relation_code VARCHAR(64) NOT NULL COMMENT '关系稳定编码',
    relation_name VARCHAR(128) NOT NULL COMMENT '关系展示名称',
    relation_weight DECIMAL(10, 4) NOT NULL DEFAULT 1 COMMENT '关系权重，0-1',
    source_code VARCHAR(64) NOT NULL COMMENT '关系数据来源编码',
    evidence VARCHAR(1024) NULL COMMENT '关系证据摘要',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_product_theme_relation (product_biz_id, relation_type, relation_code),
    KEY idx_product_theme_relation_code (relation_type, relation_code, relation_weight)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品主题、行业、指数和资产类别显式关系';
