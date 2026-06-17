-- ============================================================
-- V9 定时任务落库配置与可插拔投资分析
-- 1. 定时任务定义落库，支持接口配置并保留环境变量启动同步入口。
-- 2. 投资分析报告保存大模型输出快照，供前端生成图表。
-- 3. 投资主题快照补充市场范围，仅汇总中国大陆投资方案。
-- ============================================================

ALTER TABLE aiw_investment_theme_snapshot
    ADD COLUMN market_scope VARCHAR(32) NOT NULL DEFAULT 'CN_MAINLAND' COMMENT '市场范围：CN_MAINLAND 等' AFTER theme_name,
    ADD KEY idx_theme_snapshot_market (market_scope, snapshot_type, snapshot_time);

CREATE TABLE aiw_investment_task_definition (
    biz_id CHAR(36) NOT NULL COMMENT '任务配置业务ID',
    task_code VARCHAR(64) NOT NULL COMMENT '稳定任务编码',
    task_type VARCHAR(64) NOT NULL COMMENT '任务处理器类型',
    cron VARCHAR(64) NOT NULL COMMENT 'Spring Cron 表达式',
    zone VARCHAR(64) NOT NULL DEFAULT 'Asia/Shanghai' COMMENT 'Cron 时区',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    parameters JSON NOT NULL COMMENT '任务参数集合',
    description VARCHAR(512) NULL COMMENT '配置说明',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_investment_task_code (task_code),
    KEY idx_investment_task_type_enabled (task_type, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='可配置投资定时任务定义';

CREATE TABLE aiw_investment_analysis_report (
    biz_id CHAR(36) NOT NULL COMMENT '分析报告业务ID',
    request_id CHAR(36) NOT NULL COMMENT '本次分析请求ID',
    provider_code VARCHAR(64) NOT NULL COMMENT '分析提供方编码',
    model_code VARCHAR(64) NOT NULL COMMENT '模型编码或本地分析器编码',
    market_scope VARCHAR(32) NOT NULL DEFAULT 'CN_MAINLAND' COMMENT '市场范围：CN_MAINLAND 等',
    theme_code VARCHAR(64) NULL COMMENT '投资主题编码',
    theme_name VARCHAR(128) NULL COMMENT '投资主题名称',
    status VARCHAR(16) NOT NULL COMMENT '状态：SUCCEEDED、FAILED',
    investment_summary JSON NOT NULL COMMENT '投资信息汇总',
    trend JSON NOT NULL COMMENT '趋势分析',
    investment_plan JSON NOT NULL COMMENT '投资方案',
    simulated_return JSON NOT NULL COMMENT '模拟收益',
    chart_payload JSON NOT NULL COMMENT '前端图表数据',
    prompt_snapshot JSON NULL COMMENT '脱敏提示词和输入快照',
    failure_reason VARCHAR(2048) NULL COMMENT '失败原因摘要',
    generated_at DATETIME(3) NOT NULL COMMENT '生成时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_investment_analysis_request (request_id),
    KEY idx_investment_analysis_theme (market_scope, theme_code, generated_at),
    KEY idx_investment_analysis_provider (provider_code, model_code, generated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='可插拔大模型投资分析报告';
