-- =====================================================
-- AI Wealth Platform - Phase 2: Market Data & Notifications
-- Phase 2: 市场数据与通知表
-- Execution Order: 3
-- Depends on: V1 (aiw_product)
-- =====================================================

-- 1. 行情数据表 (aiw_market_quote)
-- 注: 实际生产环境建议使用时序数据库(ClickHouse/TimescaleDB)
CREATE TABLE IF NOT EXISTS aiw_market_quote (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键',
    product_biz_id VARCHAR(64) NOT NULL COMMENT '产品业务ID',
    quote_time DATETIME NOT NULL COMMENT '行情时间',
    open_price DECIMAL(18,4) COMMENT '开盘价',
    high_price DECIMAL(18,4) COMMENT '最高价',
    low_price DECIMAL(18,4) COMMENT '最低价',
    close_price DECIMAL(18,4) COMMENT '收盘价',
    volume BIGINT COMMENT '成交量',
    amount DECIMAL(18,2) COMMENT '成交额',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_product_time (product_biz_id, quote_time),
    INDEX idx_quote_time (quote_time),
    FOREIGN KEY (product_biz_id) REFERENCES aiw_product(biz_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行情数据表';

-- 2. 新闻公告表 (aiw_news_article)
CREATE TABLE IF NOT EXISTS aiw_news_article (
    biz_id VARCHAR(64) PRIMARY KEY COMMENT '新闻业务ID',
    title VARCHAR(256) NOT NULL COMMENT '标题',
    content LONGTEXT COMMENT '内容',
    source VARCHAR(128) COMMENT '来源',
    publish_time DATETIME NOT NULL COMMENT '发布时间',
    related_products JSON COMMENT '相关产品ID列表',
    sentiment_score DECIMAL(3,2) COMMENT '情绪评分 (-1到1)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    
    INDEX idx_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新闻公告表';

-- 3. 通知消息表 (aiw_notification)
CREATE TABLE IF NOT EXISTS aiw_notification (
    biz_id VARCHAR(64) PRIMARY KEY COMMENT '通知业务ID',
    user_biz_id VARCHAR(64) NOT NULL COMMENT '用户业务ID',
    notification_type VARCHAR(32) NOT NULL COMMENT '通知类型 (ORDER/RISK/SYSTEM)',
    title VARCHAR(256) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读 (0:未读, 1:已读)',
    read_at DATETIME COMMENT '阅读时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    
    INDEX idx_user_read (user_biz_id, is_read),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_biz_id) REFERENCES aiw_user(biz_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知消息表';
