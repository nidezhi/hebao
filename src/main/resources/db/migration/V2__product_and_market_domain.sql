-- ============================================================================
-- DZCOM 初始数据库 - V2 产品与市场数据域
-- 设计说明：
--   1. 产品目录只保存稳定产品属性；高频、时点型属性进入扩展属性或行情表。
--   2. 产品、行情、新闻之间只使用逻辑业务ID关联，不创建数据库外键。
--   3. 产品代码唯一性按市场维度控制，支持同代码出现在不同交易市场。
--   4. 行情表仅支撑MVP；数据规模扩大后可迁移到ClickHouse或时序数据库。
-- ============================================================================

-- 产品主表：统一描述股票、基金、债券、ETF及后续新增资产类型。
CREATE TABLE aiw_product (
    biz_id CHAR(36) NOT NULL COMMENT '产品业务ID，UUID字符串',
    product_no VARCHAR(32) NOT NULL COMMENT '平台内部产品编号，创建后不可变',
    product_code VARCHAR(64) NOT NULL COMMENT '市场产品代码或销售渠道产品代码',
    product_name VARCHAR(160) NOT NULL COMMENT '产品名称',
    product_type VARCHAR(32) NOT NULL COMMENT '产品类型：STOCK、FUND、BOND、ETF等',
    market_code VARCHAR(32) NOT NULL DEFAULT 'OTC' COMMENT '市场或交易所编码，如SSE、SZSE、OTC',
    currency VARCHAR(8) NOT NULL DEFAULT 'CNY' COMMENT '计价币种，使用ISO 4217编码',
    trade_status TINYINT NOT NULL DEFAULT 1 COMMENT '交易状态：0-停用，1-可交易，2-暂停交易',
    risk_level TINYINT NOT NULL DEFAULT 1 COMMENT '产品风险等级：1-低风险至5-高风险',
    min_invest_amount DECIMAL(20,4) NOT NULL DEFAULT 0 COMMENT '最小投资金额',
    amount_step DECIMAL(20,4) NOT NULL DEFAULT 0 COMMENT '金额递增步长；0表示不限制',
    quantity_step DECIMAL(20,8) NOT NULL DEFAULT 0 COMMENT '数量递增步长；0表示不限制',
    fee_rate DECIMAL(12,8) NOT NULL DEFAULT 0 COMMENT '默认费率；精确费用以后续费率规则为准',
    listing_date DATE NULL COMMENT '上市或首次可售日期',
    delisting_date DATE NULL COMMENT '退市或停止销售日期',
    description TEXT NULL COMMENT '产品简介，不保存高频变化数据',
    version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '记录最后更新时间',
    created_by VARCHAR(64) NULL COMMENT '创建操作者业务ID或系统标识',
    updated_by VARCHAR(64) NULL COMMENT '最后更新操作者业务ID或系统标识',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    deleted_at DATETIME(3) NULL COMMENT '逻辑删除时间',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_aiw_product_no (product_no),
    UNIQUE KEY uk_aiw_product_market_code (market_code, product_code),
    KEY idx_aiw_product_type_status (product_type, trade_status, is_deleted),
    KEY idx_aiw_product_risk_currency (risk_level, currency, is_deleted),
    KEY idx_aiw_product_name (product_name)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='产品目录主表；不创建跨表外键';

-- 产品扩展属性表：保存低频且尚未稳定为主表字段的产品属性。
CREATE TABLE aiw_product_attribute (
    biz_id CHAR(36) NOT NULL COMMENT '属性业务ID，UUID字符串',
    product_biz_id CHAR(36) NOT NULL COMMENT '产品业务ID；逻辑关联aiw_product.biz_id，无外键',
    attribute_key VARCHAR(64) NOT NULL COMMENT '属性键，例如fund_manager、issuer、industry_code',
    value_type VARCHAR(16) NOT NULL DEFAULT 'STRING'
        COMMENT '值类型：STRING、NUMBER、BOOLEAN、DATE、JSON',
    attribute_value JSON NOT NULL COMMENT '属性值；稳定且高频查询的属性应迁移为显式字段',
    effective_date DATE NOT NULL DEFAULT '1970-01-01' COMMENT '属性数据生效日期，1970-01-01表示当前长期有效',
    source_code VARCHAR(64) NULL COMMENT '数据来源编码',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '记录最后更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_aiw_product_attribute (product_biz_id, attribute_key, effective_date),
    KEY idx_aiw_product_attribute_key (attribute_key, effective_date, is_deleted),
    KEY idx_aiw_product_attribute_product (product_biz_id, is_deleted)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='产品扩展属性表；仅存放低频扩展字段';

-- 市场行情表：存储指定周期的OHLCV数据。
CREATE TABLE aiw_market_quote (
    biz_id CHAR(36) NOT NULL COMMENT '行情业务ID，UUID字符串',
    product_biz_id CHAR(36) NOT NULL COMMENT '产品业务ID；逻辑关联产品，无外键',
    source_code VARCHAR(64) NOT NULL COMMENT '行情数据源编码',
    quote_interval VARCHAR(16) NOT NULL COMMENT '行情周期：TICK、1M、5M、1D等',
    quote_time DATETIME(3) NOT NULL COMMENT '行情时间，统一以北京时间写入',
    open_price DECIMAL(24,8) NULL COMMENT '开盘价',
    high_price DECIMAL(24,8) NULL COMMENT '最高价',
    low_price DECIMAL(24,8) NULL COMMENT '最低价',
    close_price DECIMAL(24,8) NOT NULL COMMENT '收盘价或最新价',
    previous_close_price DECIMAL(24,8) NULL COMMENT '前收盘价',
    volume DECIMAL(28,8) NULL COMMENT '成交数量',
    turnover_amount DECIMAL(28,8) NULL COMMENT '成交金额',
    quote_status TINYINT NOT NULL DEFAULT 1 COMMENT '数据状态：0-无效，1-有效，2-修正',
    received_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '平台接收数据时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_aiw_market_quote_point
        (product_biz_id, source_code, quote_interval, quote_time),
    KEY idx_aiw_market_quote_product_time (product_biz_id, quote_interval, quote_time),
    KEY idx_aiw_market_quote_time (quote_time),
    KEY idx_aiw_market_quote_source_time (source_code, received_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='MVP市场行情表；无产品外键，后续可迁移至时序存储';

-- 新闻与公告主表：保存内容和来源，不把关联产品列表塞入JSON字段。
CREATE TABLE aiw_news_article (
    biz_id CHAR(36) NOT NULL COMMENT '新闻业务ID，UUID字符串',
    external_id VARCHAR(128) NULL COMMENT '数据源侧原始内容ID，用于去重',
    article_type VARCHAR(32) NOT NULL DEFAULT 'NEWS' COMMENT '内容类型：NEWS、ANNOUNCEMENT、RESEARCH',
    title VARCHAR(320) NOT NULL COMMENT '标题',
    summary TEXT NULL COMMENT '内容摘要',
    content LONGTEXT NULL COMMENT '正文；超大内容后续可迁移到对象存储',
    source_code VARCHAR(64) NOT NULL COMMENT '数据来源编码',
    source_url VARCHAR(1024) NULL COMMENT '原始内容地址',
    language_code VARCHAR(16) NOT NULL DEFAULT 'zh-CN' COMMENT '内容语言',
    sentiment_score DECIMAL(8,6) NULL COMMENT '情绪评分，建议范围-1至1',
    publish_time DATETIME(3) NOT NULL COMMENT '内容发布时间',
    collected_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '平台采集时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_aiw_news_source_external (source_code, external_id),
    KEY idx_aiw_news_publish (article_type, publish_time, is_deleted),
    KEY idx_aiw_news_source_time (source_code, publish_time)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='新闻、公告和研报主表';

-- 新闻关联目标表：使用通用目标类型支持产品、行业、市场等关联，不创建外键。
CREATE TABLE aiw_news_target (
    biz_id CHAR(36) NOT NULL COMMENT '新闻关联业务ID，UUID字符串',
    news_biz_id CHAR(36) NOT NULL COMMENT '新闻业务ID；逻辑关联aiw_news_article.biz_id，无外键',
    target_type VARCHAR(32) NOT NULL COMMENT '关联目标类型：PRODUCT、INDUSTRY、MARKET等',
    target_biz_id VARCHAR(64) NOT NULL COMMENT '关联目标业务ID或稳定编码',
    relevance_score DECIMAL(8,6) NULL COMMENT '关联度评分，建议范围0至1',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_aiw_news_target (news_biz_id, target_type, target_biz_id),
    KEY idx_aiw_news_target_lookup (target_type, target_biz_id, created_at),
    KEY idx_aiw_news_target_news (news_biz_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='新闻与业务目标的逻辑关联表，无数据库外键';
