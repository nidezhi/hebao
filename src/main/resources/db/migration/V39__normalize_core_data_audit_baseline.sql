-- ============================================================================
-- V39 核心数据审计基线修复
-- 说明：
--   1. 全市场投资报告的 theme_code 应为空值，不应保存为空字符串。
--   2. 陈旧 RUNNING 闭环运行会污染驾驶舱统计；超过 30 分钟未完成的运行标记为失败，
--      并补一条 RUNNING_TIMEOUT 步骤，方便前端结构化展示。
-- ============================================================================

UPDATE aiw_investment_analysis_report
SET theme_code = NULL
WHERE theme_code = '';

INSERT INTO aiw_closed_loop_step
(biz_id, run_biz_id, step_code, step_name, step_order, step_status,
 input_summary, output_summary, failure_reason, started_at, completed_at, created_at, updated_at)
SELECT
    UUID(),
    r.biz_id,
    'RUNNING_TIMEOUT',
    '运行超时保护',
    998,
    'FAILED',
    JSON_OBJECT(
        'startedAt', DATE_FORMAT(r.started_at, '%Y-%m-%dT%H:%i:%s.%f'),
        'timeoutMinutes', 30
    ),
    NULL,
    CONCAT(
        '自动闭环运行超时: startedAt=', DATE_FORMAT(r.started_at, '%Y-%m-%d %H:%i:%s.%f'),
        ', timeoutMinutes=30, 当前运行可能因远程模型、Kafka消费或采集子任务未返回而卡住，请查看任务执行记录和应用日志'
    ),
    CURRENT_TIMESTAMP(3),
    CURRENT_TIMESTAMP(3),
    CURRENT_TIMESTAMP(3),
    CURRENT_TIMESTAMP(3)
FROM aiw_closed_loop_run r
LEFT JOIN aiw_closed_loop_step existed
  ON existed.run_biz_id = r.biz_id
 AND existed.step_code = 'RUNNING_TIMEOUT'
WHERE r.run_status = 'RUNNING'
  AND r.started_at < DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 30 MINUTE)
  AND existed.biz_id IS NULL;

UPDATE aiw_closed_loop_run
SET run_status = 'FAILED',
    gate_result = 'BLOCK',
    failure_reason = CONCAT(
        '自动闭环运行超时: startedAt=', DATE_FORMAT(started_at, '%Y-%m-%d %H:%i:%s.%f'),
        ', timeoutMinutes=30, 当前运行可能因远程模型、Kafka消费或采集子任务未返回而卡住，请查看任务执行记录和应用日志'
    ),
    summary = JSON_OBJECT(
        'displaySeverity', 'ERROR',
        'blocking', true,
        'failureReason', CONCAT(
            '自动闭环运行超时: startedAt=', DATE_FORMAT(started_at, '%Y-%m-%d %H:%i:%s.%f'),
            ', timeoutMinutes=30'
        )
    ),
    completed_at = COALESCE(completed_at, CURRENT_TIMESTAMP(3)),
    updated_at = CURRENT_TIMESTAMP(3)
WHERE run_status = 'RUNNING'
  AND started_at < DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 30 MINUTE);
