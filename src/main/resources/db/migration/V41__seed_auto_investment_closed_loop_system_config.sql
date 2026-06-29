INSERT INTO aiw_system_config
(biz_id, config_group, config_key, environment, value_type, config_value, description, status, version, created_at, updated_at)
VALUES
('41000000-0000-0000-0000-000000000001', 'AUTO_INVESTMENT_CLOSED_LOOP', 'automationLevel', 'DEFAULT',
 'STRING', JSON_QUOTE('FULL_MOCK'), '自动投资闭环默认自动化等级。任务参数 automationLevel 可覆盖。', 'ENABLED', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('41000000-0000-0000-0000-000000000002', 'AUTO_INVESTMENT_CLOSED_LOOP', 'mockUserBizId', 'DEFAULT',
 'STRING', JSON_QUOTE('21000000-0000-0000-0000-000000000002'), '自动投资闭环默认模拟交易用户。任务参数 mockUserBizId 可覆盖。', 'ENABLED', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('41000000-0000-0000-0000-000000000003', 'AUTO_INVESTMENT_CLOSED_LOOP', 'mockPortfolioName', 'DEFAULT',
 'STRING', JSON_QUOTE('全自动闭环模拟组合'), '自动投资闭环默认 AI 模拟资金池名称。任务参数 mockPortfolioName 可覆盖。', 'ENABLED', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('41000000-0000-0000-0000-000000000004', 'AUTO_INVESTMENT_CLOSED_LOOP', 'initialCash', 'DEFAULT',
 'NUMBER', JSON_EXTRACT('100000', '$'), '自动投资闭环默认 AI 模拟资金池初始现金。任务参数 initialCash 可覆盖。', 'ENABLED', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('41000000-0000-0000-0000-000000000005', 'AUTO_INVESTMENT_CLOSED_LOOP', 'promptCode', 'DEFAULT',
 'STRING', JSON_QUOTE('investment-plan-from-report'), '自动投资闭环默认 Prompt 编码。任务参数 promptCode 可覆盖。', 'ENABLED', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('41000000-0000-0000-0000-000000000006', 'AUTO_INVESTMENT_CLOSED_LOOP', 'promptVersion', 'DEFAULT',
 'STRING', JSON_QUOTE('auto-v1'), '自动投资闭环默认 Prompt 版本。任务参数 promptVersion 可覆盖。', 'ENABLED', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('41000000-0000-0000-0000-000000000007', 'AUTO_INVESTMENT_CLOSED_LOOP', 'promptScenario', 'DEFAULT',
 'STRING', JSON_QUOTE('INVESTMENT_PLAN'), '自动投资闭环默认 Prompt 场景。任务参数 promptScenario 可覆盖。', 'ENABLED', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('41000000-0000-0000-0000-000000000008', 'AUTO_INVESTMENT_CLOSED_LOOP', 'modelType', 'DEFAULT',
 'STRING', JSON_QUOTE('INVESTMENT_ANALYSIS'), '自动投资闭环默认候选模型类型。', 'ENABLED', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE
value_type = VALUES(value_type),
config_value = VALUES(config_value),
description = VALUES(description),
status = VALUES(status),
updated_at = CURRENT_TIMESTAMP(3);

UPDATE aiw_investment_task_definition
SET parameters = JSON_REMOVE(
    parameters,
    '$.automationLevel',
    '$.mockUserBizId',
    '$.mockPortfolioName',
    '$.initialCash',
    '$.promptCode',
    '$.promptVersion',
    '$.promptScenario'
)
WHERE task_code = 'auto-investment-closed-loop-orchestration'
  AND parameters IS NOT NULL;
