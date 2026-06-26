-- 优化自动闭环的可观测性和默认报告候选窗口。
-- 1. BLOCKED 是质量、风控或安全边界的正常阻断结果，不应混同为系统失败。
-- 2. 自动报告可能一次生成多份主题报告，默认只看最近 1 份会误丢同批次的合格报告。
ALTER TABLE aiw_scheduled_task_execution
    MODIFY status VARCHAR(16) NOT NULL COMMENT '状态：RUNNING、SUCCEEDED、BLOCKED、FAILED';

UPDATE aiw_investment_task_definition
SET parameters = JSON_SET(parameters, '$.maxReportsForMock', '20'),
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_code = 'auto-investment-closed-loop-orchestration'
  AND JSON_UNQUOTE(JSON_EXTRACT(parameters, '$.maxReportsForMock')) = '1';
