-- Emergency cost guard: pause expensive AI scheduled jobs.
-- Manual trigger remains available through /api/investment/tasks/trigger.
UPDATE aiw_investment_task_definition
SET enabled = 0,
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_type IN (
    'AI_DATA_SOURCE_DISCOVERY',
    'AI_STRUCTURED_DATA_COLLECTION',
    'AUTO_INVESTMENT_REPORT_GENERATION',
    'AUTO_PROMPT_GOVERNANCE',
    'AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION'
);

UPDATE aiw_scheduled_task_execution
SET status = 'FAILED',
    completed_at = COALESCE(completed_at, CURRENT_TIMESTAMP(3)),
    failure_reason = COALESCE(NULLIF(failure_reason, ''), '人工止血：AI闭环定时任务成本异常，暂停后重新设计预算和采集链路')
WHERE status = 'RUNNING'
  AND task_type IN (
      'AI_DATA_SOURCE_DISCOVERY',
      'AI_STRUCTURED_DATA_COLLECTION',
      'AUTO_INVESTMENT_REPORT_GENERATION',
      'AUTO_PROMPT_GOVERNANCE',
      'AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION'
  );

SELECT task_code, task_type, enabled, updated_at
FROM aiw_investment_task_definition
WHERE task_type IN (
    'AI_DATA_SOURCE_DISCOVERY',
    'AI_STRUCTURED_DATA_COLLECTION',
    'AUTO_INVESTMENT_REPORT_GENERATION',
    'AUTO_PROMPT_GOVERNANCE',
    'AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION'
)
ORDER BY task_type, task_code;
