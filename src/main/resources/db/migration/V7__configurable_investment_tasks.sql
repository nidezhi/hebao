-- ============================================================
-- V7 配置驱动投资任务
-- 1. 任务定义由应用配置生成，执行记录用于审计和失败追踪。
-- 2. 热门投资方向的收益、动量和资讯热度统一保存为快照。
-- ============================================================

CREATE TABLE aiw_scheduled_task_execution (
    biz_id CHAR(36) NOT NULL COMMENT '任务执行业务ID',
    task_code VARCHAR(64) NOT NULL COMMENT '稳定任务编码',
    task_type VARCHAR(64) NOT NULL COMMENT '任务类型',
    trigger_source VARCHAR(16) NOT NULL COMMENT '触发来源：SCHEDULE、MANUAL、RETRY',
    status VARCHAR(16) NOT NULL COMMENT '状态：RUNNING、SUCCEEDED、FAILED',
    event_id CHAR(36) NOT NULL COMMENT 'Kafka事件幂等标识',
    result_summary VARCHAR(1024) NULL COMMENT '执行结果摘要',
    failure_reason VARCHAR(2048) NULL COMMENT '失败原因摘要',
    started_at DATETIME(3) NOT NULL COMMENT '开始时间',
    completed_at DATETIME(3) NULL COMMENT '完成时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_scheduled_task_event (event_id),
    KEY idx_scheduled_task_code_time (task_code, started_at),
    KEY idx_scheduled_task_status_time (status, started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配置驱动定时任务执行记录';

CREATE TABLE aiw_investment_theme_snapshot (
    biz_id CHAR(36) NOT NULL COMMENT '主题快照业务ID',
    task_code VARCHAR(64) NOT NULL COMMENT '来源任务编码',
    snapshot_type VARCHAR(32) NOT NULL COMMENT '快照类型：RETURN、MOMENTUM、NEWS_HEAT',
    theme_code VARCHAR(64) NOT NULL COMMENT '投资主题稳定编码',
    theme_name VARCHAR(128) NOT NULL COMMENT '投资主题名称',
    window_minutes INT UNSIGNED NOT NULL COMMENT '统计回看窗口分钟数',
    sample_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '参与统计的样本数',
    return_rate DECIMAL(16,8) NULL COMMENT '窗口平均收益率',
    momentum_score DECIMAL(16,8) NULL COMMENT '动量评分',
    heat_score DECIMAL(16,8) NULL COMMENT '资讯热度评分',
    top_product_biz_id CHAR(36) NULL COMMENT '窗口表现最佳产品业务ID',
    metrics JSON NOT NULL COMMENT '可解释指标和样本明细',
    snapshot_time DATETIME(3) NOT NULL COMMENT '快照时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
    PRIMARY KEY (biz_id),
    KEY idx_theme_snapshot_lookup (snapshot_type, theme_code, snapshot_time),
    KEY idx_theme_snapshot_task (task_code, snapshot_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热门投资方向实时收益、动量和资讯热度快照';

