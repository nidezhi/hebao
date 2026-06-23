-- ============================================================================
-- V14 数据源治理
-- 设计说明：
--   1. 数据源质量成为投资报告、Mock 交易和前端看板的基础输入。
--   2. 健康状态与质量快照分离，避免覆盖历史质量证据。
--   3. 禁止数据库外键，跨域只保存 source_code 等稳定业务编码。
-- ============================================================================

CREATE TABLE aiw_data_source (
    biz_id CHAR(36) NOT NULL COMMENT '数据源业务唯一标识',
    source_code VARCHAR(64) NOT NULL COMMENT '数据源稳定编码',
    source_name VARCHAR(128) NOT NULL COMMENT '数据源展示名称',
    source_type VARCHAR(32) NOT NULL COMMENT '数据源类型：MARKET、NEWS、ANNOUNCEMENT、RESEARCH、REGULATORY等',
    trust_level VARCHAR(8) NOT NULL COMMENT '来源等级：L1-L5',
    base_url VARCHAR(512) NULL COMMENT '数据源入口地址或供应商网关',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用采集或展示',
    fetch_frequency VARCHAR(64) NULL COMMENT '采集频率说明或cron表达式',
    owner VARCHAR(64) NULL COMMENT '数据源负责人或维护方',
    description VARCHAR(512) NULL COMMENT '数据源用途说明',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    created_by VARCHAR(64) NULL COMMENT '创建操作者业务ID或系统标识',
    updated_by VARCHAR(64) NULL COMMENT '最后更新操作者业务ID或系统标识',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_aiw_data_source_code (source_code),
    KEY idx_aiw_data_source_type_enabled (source_type, enabled),
    KEY idx_aiw_data_source_trust (trust_level, enabled)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='数据源注册表';

CREATE TABLE aiw_data_source_health (
    biz_id CHAR(36) NOT NULL COMMENT '数据源健康状态业务唯一标识',
    source_code VARCHAR(64) NOT NULL COMMENT '数据源稳定编码',
    last_success_at DATETIME(3) NULL COMMENT '最近成功采集时间',
    last_failure_at DATETIME(3) NULL COMMENT '最近失败时间',
    success_rate DECIMAL(10,4) NOT NULL DEFAULT 0 COMMENT '近期成功率，0-1',
    avg_latency_ms INT UNSIGNED NULL COMMENT '平均响应耗时毫秒',
    failure_reason VARCHAR(512) NULL COMMENT '最近失败原因摘要',
    sample_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '最近窗口样本数量',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_aiw_data_source_health_code (source_code),
    KEY idx_aiw_data_source_health_success (success_rate, updated_at),
    KEY idx_aiw_data_source_health_failure (last_failure_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='数据源当前健康状态';

CREATE TABLE aiw_data_quality_snapshot (
    biz_id CHAR(36) NOT NULL COMMENT '数据质量快照业务唯一标识',
    source_code VARCHAR(64) NOT NULL COMMENT '数据源稳定编码',
    data_type VARCHAR(32) NOT NULL COMMENT '数据类型：MARKET_QUOTE、NEWS、ANNOUNCEMENT、RESEARCH等',
    quality_score DECIMAL(10,4) NOT NULL DEFAULT 0 COMMENT '综合质量分，0-1',
    missing_rate DECIMAL(10,4) NOT NULL DEFAULT 0 COMMENT '缺失率，0-1',
    duplicate_rate DECIMAL(10,4) NOT NULL DEFAULT 0 COMMENT '重复率，0-1',
    freshness_score DECIMAL(10,4) NOT NULL DEFAULT 0 COMMENT '新鲜度分，0-1',
    sample_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '参与评估样本数',
    snapshot_time DATETIME(3) NOT NULL COMMENT '质量快照时间',
    detail JSON NULL COMMENT '质量评估上下文和解释',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (biz_id),
    KEY idx_aiw_data_quality_source_time (source_code, snapshot_time),
    KEY idx_aiw_data_quality_type_score (data_type, quality_score, snapshot_time)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='数据源质量历史快照';
