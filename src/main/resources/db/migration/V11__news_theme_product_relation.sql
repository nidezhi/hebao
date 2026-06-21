-- ============================================================
-- V11 新闻、主题和产品显式关联
-- 1. 将资讯热度任务中的关键词命中结果落库，便于解释主题热度来源。
-- 2. 保留产品代码关联，后续接入产品主数据后可补齐产品业务 ID。
-- 3. 保存来源质量、关键词命中数和关联分数，支持投资分析评估输入质量。
-- ============================================================

CREATE TABLE aiw_news_article_relation (
    biz_id CHAR(36) NOT NULL COMMENT '关联记录业务ID',
    article_biz_id CHAR(36) NOT NULL COMMENT '资讯业务ID',
    theme_code VARCHAR(64) NOT NULL COMMENT '投资主题稳定编码',
    theme_name VARCHAR(128) NOT NULL COMMENT '投资主题展示名称',
    product_code VARCHAR(64) NOT NULL DEFAULT '' COMMENT '关联产品代码；空字符串表示主题级关联',
    relation_type VARCHAR(32) NOT NULL COMMENT '关联类型：KEYWORD_MATCH/MANUAL/MODEL_EXTRACTED',
    matched_keywords JSON NOT NULL COMMENT '命中的关键词集合',
    source_quality_score DECIMAL(10, 4) NOT NULL DEFAULT 0 COMMENT '数据源质量分',
    relation_score DECIMAL(10, 4) NOT NULL DEFAULT 0 COMMENT '综合关联分',
    evidence VARCHAR(1024) NULL COMMENT '关联证据摘要',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_news_theme_product_relation (
        article_biz_id,
        theme_code,
        product_code,
        relation_type
    ),
    KEY idx_news_relation_theme (theme_code, relation_score),
    KEY idx_news_relation_article (article_biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资讯、投资主题和产品显式关联';
