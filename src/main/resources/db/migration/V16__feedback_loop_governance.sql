-- ============================================================================
-- V16 回测、反馈与 Prompt 评估闭环
-- 设计说明：
--   1. aiw_backtest_result 已在 V5 创建，本迁移只补反馈和 Prompt 评估表。
--   2. 用户采纳/拒绝、回测结果、Prompt 版本之间用业务 ID 关联，不使用数据库外键。
--   3. Prompt 评估可由回测、用户反馈或人工复核生成，作为后续版本优化依据。
-- ============================================================================

CREATE TABLE aiw_investment_feedback (
    biz_id CHAR(36) NOT NULL COMMENT '反馈业务唯一标识',
    user_biz_id CHAR(36) NULL COMMENT '反馈用户业务标识，系统反馈可为空',
    target_type VARCHAR(32) NOT NULL COMMENT '反馈目标类型：REPORT、RECOMMENDATION、MOCK_ORDER、BACKTEST等',
    target_biz_id CHAR(36) NOT NULL COMMENT '反馈目标业务标识',
    report_biz_id CHAR(36) NULL COMMENT '关联投资报告业务标识',
    prompt_biz_id CHAR(36) NULL COMMENT '关联Prompt模板业务标识',
    prompt_code VARCHAR(64) NULL COMMENT 'Prompt稳定编码快照',
    prompt_version VARCHAR(32) NULL COMMENT 'Prompt版本快照',
    backtest_biz_id CHAR(36) NULL COMMENT '关联回测结果业务标识',
    feedback_action VARCHAR(16) NOT NULL COMMENT '反馈动作：ADOPT、REJECT、WATCH、IGNORE',
    reason_code VARCHAR(64) NULL COMMENT '机器可读原因编码',
    comment_text VARCHAR(1024) NULL COMMENT '用户或人工复核备注',
    metadata JSON NULL COMMENT '反馈上下文、前端场景和脱敏补充信息',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '反馈创建时间',
    PRIMARY KEY (biz_id),
    KEY idx_feedback_target (target_type, target_biz_id, created_at),
    KEY idx_feedback_user (user_biz_id, created_at),
    KEY idx_feedback_prompt (prompt_code, prompt_version, created_at),
    KEY idx_feedback_backtest (backtest_biz_id, created_at),
    KEY idx_feedback_action (feedback_action, created_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='投资方案、报告、Mock结果和回测结果的用户反馈';

CREATE TABLE aiw_ai_prompt_evaluation (
    biz_id CHAR(36) NOT NULL COMMENT 'Prompt评估业务唯一标识',
    prompt_biz_id CHAR(36) NULL COMMENT 'Prompt模板业务标识',
    prompt_code VARCHAR(64) NOT NULL COMMENT 'Prompt稳定编码快照',
    prompt_version VARCHAR(32) NOT NULL COMMENT 'Prompt版本快照',
    scenario VARCHAR(64) NOT NULL COMMENT 'Prompt使用场景快照',
    backtest_biz_id CHAR(36) NULL COMMENT '关联回测结果业务标识',
    feedback_biz_id CHAR(36) NULL COMMENT '关联反馈业务标识',
    score DECIMAL(10,4) NOT NULL COMMENT '综合评分，0-1',
    score_detail JSON NULL COMMENT '评分详情，如回测收益、回撤、采纳动作和人工理由',
    review_status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '复核状态：PENDING、APPROVED、REJECTED、ARCHIVED',
    evaluator_type VARCHAR(16) NOT NULL DEFAULT 'SYSTEM' COMMENT '评估来源：SYSTEM、USER、ADMIN、JOB',
    evaluator_biz_id CHAR(36) NULL COMMENT '评估人或任务业务标识',
    evaluated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '评估时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '记录创建时间',
    PRIMARY KEY (biz_id),
    KEY idx_prompt_eval_prompt (prompt_code, prompt_version, evaluated_at),
    KEY idx_prompt_eval_backtest (backtest_biz_id),
    KEY idx_prompt_eval_feedback (feedback_biz_id),
    KEY idx_prompt_eval_status (review_status, evaluated_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='AI Prompt版本评估和回测反馈反哺记录';
