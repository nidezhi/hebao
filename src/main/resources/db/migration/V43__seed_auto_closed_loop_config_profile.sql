INSERT INTO aiw_system_config
(biz_id, config_group, config_key, environment, value_type, config_value, description, status, version, created_at, updated_at)
VALUES
('41000000-0000-0000-0000-000000000010', 'AUTO_INVESTMENT_CLOSED_LOOP_PROFILE', 'default-auto-mock', 'DEFAULT',
 'JSON', JSON_OBJECT(
     'profileCode', 'default-auto-mock',
     'profileName', '默认 AI Mock 闭环方案',
     'automationLevel', 'FULL_MOCK',
     'mockPortfolioBizId', '',
     'mockUserBizId', '21000000-0000-0000-0000-000000000002',
     'mockPortfolioName', '全自动闭环模拟组合',
     'initialCash', '100000',
     'promptCode', 'investment-plan-from-report',
     'promptVersion', 'auto-v1',
     'promptScenario', 'INVESTMENT_PLAN',
     'modelType', 'INVESTMENT_ANALYSIS'
 ), '自动投资闭环配置方案；手动触发闭环时选择，运行事件会保存本次参数快照。', 'ENABLED', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE
value_type = VALUES(value_type),
description = VALUES(description),
status = VALUES(status),
updated_at = CURRENT_TIMESTAMP(3);
