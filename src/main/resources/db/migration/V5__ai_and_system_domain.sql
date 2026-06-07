-- ============================================================
-- V5 AI 与系统支撑领域
-- 设计约束：
-- 1. AI结果保存模型版本和输入输出快照，保证可解释、可追溯。
-- 2. 通用目标使用 target_type + target_biz_id，避免与业务表产生数据库耦合。
-- 3. 事务发件箱用于可靠发布领域事件，不依赖跨表外键或级联操作。
-- ============================================================

CREATE TABLE aiw_ai_model (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '数据库内部主键，不对外暴露',
    biz_id CHAR(36) NOT NULL COMMENT '模型版本业务唯一标识',
    model_code VARCHAR(64) NOT NULL COMMENT '模型稳定编码',
    model_version VARCHAR(32) NOT NULL COMMENT '模型版本号',
    model_name VARCHAR(128) NOT NULL COMMENT '模型名称',
    model_type VARCHAR(32) NOT NULL COMMENT '模型类型：SIGNAL、RISK、RECOMMENDATION、NLP等',
    provider VARCHAR(64) NULL COMMENT '模型提供方或运行平台',
    artifact_uri VARCHAR(512) NULL COMMENT '模型制品、提示词或配置的存储地址',
    model_config JSON NULL COMMENT '脱敏后的模型参数和运行配置',
    metrics JSON NULL COMMENT '离线评估指标及验证摘要',
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT、VALIDATING、ACTIVE、INACTIVE、ARCHIVED',
    activated_at DATETIME(3) NULL COMMENT '正式启用时间',
    retired_at DATETIME(3) NULL COMMENT '停止使用时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_model_biz_id (biz_id),
    UNIQUE KEY uk_ai_model_code_version (model_code, model_version),
    KEY idx_ai_model_type_status (model_type, status),
    KEY idx_ai_model_status_time (status, activated_at, retired_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模型及版本注册信息';

CREATE TABLE aiw_ai_signal (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '数据库内部主键，不对外暴露',
    biz_id CHAR(36) NOT NULL COMMENT 'AI信号业务唯一标识',
    model_code VARCHAR(64) NOT NULL COMMENT '生成信号的模型编码快照',
    model_version VARCHAR(32) NOT NULL COMMENT '生成信号的模型版本快照',
    signal_type VARCHAR(32) NOT NULL COMMENT '信号类型：BUY、SELL、HOLD、RISK_ALERT、TREND等',
    target_type VARCHAR(32) NOT NULL COMMENT '目标类型：PRODUCT、PORTFOLIO、MARKET等',
    target_biz_id CHAR(36) NOT NULL COMMENT '目标对象逻辑业务标识',
    signal_value DECIMAL(20,8) NULL COMMENT '可量化的信号值',
    confidence DECIMAL(7,6) NULL COMMENT '置信度，范围通常为0至1',
    explanation TEXT NULL COMMENT '面向用户或审核人员的解释文本',
    factors JSON NULL COMMENT '主要影响因子、特征贡献及证据摘要',
    input_snapshot JSON NULL COMMENT '生成信号时的脱敏输入快照',
    generated_at DATETIME(3) NOT NULL COMMENT '信号生成时间',
    valid_until DATETIME(3) NULL COMMENT '信号有效截止时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_signal_biz_id (biz_id),
    KEY idx_ai_signal_target (target_type, target_biz_id, generated_at),
    KEY idx_ai_signal_model (model_code, model_version, generated_at),
    KEY idx_ai_signal_type_time (signal_type, generated_at),
    KEY idx_ai_signal_validity (valid_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='可追溯的AI分析信号';

CREATE TABLE aiw_ai_recommendation (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '数据库内部主键，不对外暴露',
    biz_id CHAR(36) NOT NULL COMMENT 'AI建议业务唯一标识',
    user_biz_id CHAR(36) NULL COMMENT '目标用户逻辑业务标识，公共建议可为空',
    portfolio_biz_id CHAR(36) NULL COMMENT '目标组合逻辑业务标识',
    signal_biz_id CHAR(36) NULL COMMENT '来源AI信号逻辑业务标识',
    recommendation_type VARCHAR(32) NOT NULL COMMENT '建议类型：ALLOCATION、PRODUCT、RISK、REBALANCE等',
    title VARCHAR(256) NOT NULL COMMENT '建议标题',
    content TEXT NOT NULL COMMENT '建议正文',
    action_payload JSON NULL COMMENT '建议操作、目标比例及扩展参数',
    risk_notice TEXT NOT NULL COMMENT '风险提示，不得为空',
    status VARCHAR(16) NOT NULL DEFAULT 'GENERATED' COMMENT '状态：GENERATED、PUBLISHED、ACCEPTED、REJECTED、EXPIRED',
    generated_at DATETIME(3) NOT NULL COMMENT '建议生成时间',
    valid_until DATETIME(3) NULL COMMENT '建议有效截止时间',
    acted_at DATETIME(3) NULL COMMENT '用户接受或拒绝时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_recommendation_biz_id (biz_id),
    KEY idx_ai_recommendation_user (user_biz_id, status, generated_at),
    KEY idx_ai_recommendation_portfolio (portfolio_biz_id, generated_at),
    KEY idx_ai_recommendation_signal (signal_biz_id),
    KEY idx_ai_recommendation_validity (status, valid_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI生成的投资分析建议，不构成承诺或保证';

CREATE TABLE aiw_backtest_result (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '数据库内部主键，不对外暴露',
    biz_id CHAR(36) NOT NULL COMMENT '回测任务业务唯一标识',
    owner_user_biz_id CHAR(36) NULL COMMENT '任务发起用户逻辑业务标识，系统任务可为空',
    strategy_code VARCHAR(64) NOT NULL COMMENT '策略稳定编码',
    strategy_version VARCHAR(32) NOT NULL COMMENT '策略版本快照',
    start_date DATE NOT NULL COMMENT '回测区间开始日期',
    end_date DATE NOT NULL COMMENT '回测区间结束日期',
    initial_capital DECIMAL(24,8) NOT NULL COMMENT '初始资金',
    benchmark_code VARCHAR(64) NULL COMMENT '基准指数或比较对象编码',
    parameters JSON NOT NULL COMMENT '回测参数快照',
    metrics JSON NULL COMMENT '收益、回撤、波动率等结果指标',
    result_uri VARCHAR(512) NULL COMMENT '明细结果或报告存储地址',
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING、RUNNING、SUCCEEDED、FAILED、CANCELLED',
    failure_reason VARCHAR(1024) NULL COMMENT '失败原因摘要',
    started_at DATETIME(3) NULL COMMENT '任务开始时间',
    completed_at DATETIME(3) NULL COMMENT '任务完成时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '任务创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_backtest_result_biz_id (biz_id),
    KEY idx_backtest_owner (owner_user_biz_id, created_at),
    KEY idx_backtest_strategy (strategy_code, strategy_version, created_at),
    KEY idx_backtest_status (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='策略回测任务及结果摘要';

CREATE TABLE aiw_system_config (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '数据库内部主键，不对外暴露',
    biz_id CHAR(36) NOT NULL COMMENT '配置业务唯一标识',
    config_group VARCHAR(64) NOT NULL COMMENT '配置分组',
    config_key VARCHAR(128) NOT NULL COMMENT '配置键',
    environment VARCHAR(32) NOT NULL DEFAULT 'DEFAULT' COMMENT '生效环境：DEFAULT、DEV、TEST、PROD等',
    value_type VARCHAR(16) NOT NULL COMMENT '值类型：STRING、NUMBER、BOOLEAN、JSON',
    config_value JSON NOT NULL COMMENT '配置值，敏感信息必须使用外部密钥服务引用',
    description VARCHAR(512) NULL COMMENT '配置用途说明',
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED、DISABLED',
    version INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_system_config_biz_id (biz_id),
    UNIQUE KEY uk_system_config_key_env (config_group, config_key, environment),
    KEY idx_system_config_status (status, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='非敏感系统配置及外部配置引用';

CREATE TABLE aiw_outbox_event (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '数据库内部主键，不对外暴露',
    biz_id CHAR(36) NOT NULL COMMENT '事件业务唯一标识，同时作为消息幂等键',
    aggregate_type VARCHAR(32) NOT NULL COMMENT '事件来源聚合类型',
    aggregate_biz_id CHAR(36) NOT NULL COMMENT '事件来源聚合逻辑业务标识',
    event_type VARCHAR(128) NOT NULL COMMENT '稳定的领域事件类型',
    event_version INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '事件结构版本',
    payload JSON NOT NULL COMMENT '领域事件载荷，禁止包含口令和完整敏感信息',
    headers JSON NULL COMMENT '追踪标识、租户及消息头扩展信息',
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '发布状态：PENDING、PUBLISHING、PUBLISHED、FAILED',
    retry_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '发布重试次数',
    next_retry_at DATETIME(3) NULL COMMENT '下一次允许重试时间',
    occurred_at DATETIME(3) NOT NULL COMMENT '领域事件实际发生时间',
    published_at DATETIME(3) NULL COMMENT '成功发布时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_outbox_event_biz_id (biz_id),
    KEY idx_outbox_dispatch (status, next_retry_at, created_at),
    KEY idx_outbox_aggregate (aggregate_type, aggregate_biz_id, occurred_at),
    KEY idx_outbox_event_type (event_type, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='可靠发布领域事件的事务发件箱';
