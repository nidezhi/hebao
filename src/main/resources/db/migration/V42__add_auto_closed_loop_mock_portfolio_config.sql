INSERT INTO aiw_system_config
(biz_id, config_group, config_key, environment, value_type, config_value, description, status, version, created_at, updated_at)
VALUES
('41000000-0000-0000-0000-000000000009', 'AUTO_INVESTMENT_CLOSED_LOOP', 'mockPortfolioBizId', 'DEFAULT',
 'STRING', JSON_QUOTE(''), '自动投资闭环默认模拟组合 BizId。为空时按用户和名称查找或创建资金池；任务参数 mockPortfolioBizId 可覆盖。', 'ENABLED', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE
value_type = VALUES(value_type),
description = VALUES(description),
status = VALUES(status),
updated_at = CURRENT_TIMESTAMP(3);
