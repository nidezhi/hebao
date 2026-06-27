-- ============================================================================
-- V25 扩展失败原因字段
-- 说明：
--   自动闭环接入远程大模型后，失败原因可能包含 provider 返回摘要、门禁上下文
--   或较长的异常信息。统一将 failure_reason 从短 VARCHAR 扩展为 TEXT，
--   避免任务审计落库时因信息过长导致整轮闭环失败。
-- ============================================================================

ALTER TABLE aiw_backtest_result
    MODIFY COLUMN failure_reason TEXT NULL COMMENT '失败原因详情';

ALTER TABLE aiw_scheduled_task_execution
    MODIFY COLUMN failure_reason TEXT NULL COMMENT '失败原因详情';

ALTER TABLE aiw_investment_analysis_report
    MODIFY COLUMN failure_reason TEXT NULL COMMENT '失败原因详情';

ALTER TABLE aiw_data_source_health
    MODIFY COLUMN failure_reason TEXT NULL COMMENT '最近失败原因详情';

ALTER TABLE aiw_closed_loop_run
    MODIFY COLUMN failure_reason TEXT NULL COMMENT '失败或阻断原因详情';

ALTER TABLE aiw_closed_loop_step
    MODIFY COLUMN failure_reason TEXT NULL COMMENT '失败或阻断原因详情';
