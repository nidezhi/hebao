INSERT INTO aiw_system_config
(biz_id, config_group, config_key, environment, value_type, config_value, description, status, version, created_at, updated_at)
VALUES
('41000000-0000-0000-0000-000000000011', 'AUTO_INVESTMENT_CLOSED_LOOP', 'scheduledConfigProfileCode', 'DEFAULT',
 'STRING', JSON_QUOTE('default-auto-mock'), '自动投资闭环定时任务默认配置方案编码；SCHEDULE 触发时作为最权威方案。', 'ENABLED', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE
value_type = VALUES(value_type),
description = VALUES(description),
status = VALUES(status),
updated_at = CURRENT_TIMESTAMP(3);

UPDATE aiw_investment_task_definition
SET parameters = JSON_REMOVE(
    parameters,
    '$.configProfileCode',
    '$.configProfileSnapshot'
)
WHERE task_code = 'auto-investment-closed-loop-orchestration'
  AND parameters IS NOT NULL;
