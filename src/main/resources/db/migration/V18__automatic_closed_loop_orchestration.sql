-- ============================================================================
-- V18 自动投资闭环编排
-- 设计说明：
--   1. 自动化闭环必须可追踪，每轮运行和每个步骤都要保留状态、输入摘要和中断原因。
--   2. 默认只允许自动 Mock 交易和候选 Prompt/模型评估，不自动启用新 Prompt、新模型或真实交易。
--   3. 不创建数据库外键，跨模块只保存业务标识和稳定编码。
-- ============================================================================

CREATE TABLE aiw_closed_loop_run (
    biz_id CHAR(36) NOT NULL COMMENT '闭环运行业务唯一标识',
    run_no VARCHAR(40) NOT NULL COMMENT '闭环运行编号，面向前端展示',
    task_code VARCHAR(64) NOT NULL COMMENT '来源任务编码',
    trigger_source VARCHAR(16) NOT NULL COMMENT '触发来源：SCHEDULE/MANUAL/RETRY',
    run_status VARCHAR(16) NOT NULL COMMENT '运行状态：RUNNING/SUCCEEDED/PARTIAL/BLOCKED/FAILED',
    automation_level VARCHAR(32) NOT NULL COMMENT '自动化等级：DATA_ONLY/MOCK_ONLY/FULL_MOCK',
    market_scope VARCHAR(32) NOT NULL DEFAULT 'CN_MAINLAND' COMMENT '市场范围',
    theme_code VARCHAR(64) NULL COMMENT '本轮聚焦主题，空表示市场级',
    mock_user_biz_id CHAR(36) NULL COMMENT '自动 Mock 使用的用户业务标识',
    portfolio_biz_id CHAR(36) NULL COMMENT '本轮使用或创建的 Mock 组合业务标识',
    report_biz_id CHAR(36) NULL COMMENT '本轮生成或选用的报告业务标识',
    prompt_biz_id CHAR(36) NULL COMMENT '本轮使用或候选 Prompt 业务标识',
    prompt_code VARCHAR(64) NULL COMMENT 'Prompt 稳定编码快照',
    prompt_version VARCHAR(32) NULL COMMENT 'Prompt 版本快照',
    backtest_biz_id CHAR(36) NULL COMMENT '本轮生成的回测业务标识',
    quality_score DECIMAL(10,4) NULL COMMENT '本轮报告或数据质量分',
    gate_result VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '门禁结果：PENDING/PASS/BLOCK',
    summary JSON NULL COMMENT '运行摘要、关键业务标识和前端驾驶舱提示',
    failure_reason VARCHAR(1024) NULL COMMENT '失败或阻断原因摘要',
    started_at DATETIME(3) NOT NULL COMMENT '运行开始时间',
    completed_at DATETIME(3) NULL COMMENT '运行完成时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_closed_loop_run_no (run_no),
    KEY idx_closed_loop_run_status (run_status, started_at),
    KEY idx_closed_loop_run_task (task_code, started_at),
    KEY idx_closed_loop_run_theme (market_scope, theme_code, started_at),
    KEY idx_closed_loop_run_mock_user (mock_user_biz_id, started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自动投资闭环运行记录';

CREATE TABLE aiw_closed_loop_step (
    biz_id CHAR(36) NOT NULL COMMENT '闭环步骤业务唯一标识',
    run_biz_id CHAR(36) NOT NULL COMMENT '闭环运行业务标识',
    step_code VARCHAR(64) NOT NULL COMMENT '步骤编码',
    step_name VARCHAR(128) NOT NULL COMMENT '步骤展示名称',
    step_order INT NOT NULL COMMENT '步骤顺序',
    step_status VARCHAR(16) NOT NULL COMMENT '步骤状态：PENDING/RUNNING/SUCCEEDED/SKIPPED/BLOCKED/FAILED',
    input_summary JSON NULL COMMENT '步骤输入摘要，禁止保存敏感明文',
    output_summary JSON NULL COMMENT '步骤输出摘要，保存关键业务标识和计数',
    failure_reason VARCHAR(1024) NULL COMMENT '失败或阻断原因摘要',
    started_at DATETIME(3) NULL COMMENT '步骤开始时间',
    completed_at DATETIME(3) NULL COMMENT '步骤完成时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_closed_loop_step_run_code (run_biz_id, step_code),
    KEY idx_closed_loop_step_run_order (run_biz_id, step_order),
    KEY idx_closed_loop_step_status (step_status, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自动投资闭环步骤记录';

INSERT INTO aiw_investment_task_definition
(biz_id, task_code, task_type, cron, zone, enabled, parameters, description, created_at, updated_at)
VALUES
('17000000-0000-0000-0000-000000000209', 'auto-investment-closed-loop-orchestration',
 'AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION', '0 40 */2 * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT(
   'automationLevel', 'FULL_MOCK',
   'marketScope', 'CN_MAINLAND',
   'mockUserBizId', '21000000-0000-0000-0000-000000000002',
   'mockPortfolioName', '全自动闭环模拟组合',
   'initialCash', '100000',
   'minQualityScore', '0.45',
   'allowAutoMockTrade', 'true',
   'allowPromptCandidate', 'true',
   'allowModelCandidate', 'true',
   'allowAutoPromptActivation', 'false',
   'allowAutoModelActivation', 'false',
   'allowRealTrade', 'false',
   'dataTaskCodes', 'l1-regulatory-disclosure-collection,l1-exchange-announcement-collection,l2-wealth-product-nav-refresh,cn-mainland-market-momentum-scan,cn-mainland-hot-theme-return,cn-mainland-news-heat-aggregation',
   'reportTaskCode', 'auto-openai-investment-report-generation',
   'promptTaskCode', 'auto-prompt-governance',
   'modelCode', 'openai-compatible-analysis',
   'providerCode', 'OPENAI_COMPATIBLE',
   'lookbackDays', '30',
   'themeCodes', '',
   'maxReportsForMock', '1'
 ),
 '自动投资闭环总编排任务。自动采集、报告、Prompt候选、Mock交易、回测和反馈；默认不自动启用新Prompt/模型、不触发真实交易。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE
task_type = VALUES(task_type),
cron = VALUES(cron),
zone = VALUES(zone),
enabled = VALUES(enabled),
parameters = VALUES(parameters),
description = VALUES(description),
updated_at = CURRENT_TIMESTAMP(3);
