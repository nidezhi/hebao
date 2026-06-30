-- Reset the default automated closed-loop Mock pool and run the scheduler three times per day.
-- The reset is scoped to the configured default Mock portfolio only.

UPDATE aiw_investment_task_definition
SET cron = '0 30 9,13,20 * * *',
    zone = 'Asia/Shanghai',
    enabled = 1,
    description = '自动投资闭环总编排任务。每日 09:30、13:30、20:30 运行；自动采集、报告、Prompt候选、Mock交易、回测和反馈；默认不自动启用新Prompt/模型、不触发真实交易。',
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_code = 'auto-investment-closed-loop-orchestration';

UPDATE aiw_system_config
SET config_value = JSON_QUOTE('default-auto-mock'),
    status = 'ENABLED',
    updated_at = CURRENT_TIMESTAMP(3)
WHERE config_group = 'AUTO_INVESTMENT_CLOSED_LOOP'
  AND config_key = 'scheduledConfigProfileCode'
  AND environment = 'DEFAULT';

SET @default_mock_user_biz_id := '21000000-0000-0000-0000-000000000002';
SET @default_mock_portfolio_name := '全自动闭环模拟组合';
SET @default_initial_cash := 100000.00000000;

SELECT JSON_UNQUOTE(config_value)
INTO @configured_mock_portfolio_biz_id
FROM aiw_system_config
WHERE config_group = 'AUTO_INVESTMENT_CLOSED_LOOP'
  AND config_key = 'mockPortfolioBizId'
  AND environment = 'DEFAULT'
LIMIT 1;

SELECT p.biz_id
INTO @default_mock_portfolio_biz_id
FROM aiw_portfolio p
WHERE p.biz_id COLLATE utf8mb4_unicode_ci = @configured_mock_portfolio_biz_id COLLATE utf8mb4_unicode_ci
   OR (p.owner_user_biz_id COLLATE utf8mb4_unicode_ci = @default_mock_user_biz_id COLLATE utf8mb4_unicode_ci
       AND p.portfolio_name COLLATE utf8mb4_unicode_ci = @default_mock_portfolio_name COLLATE utf8mb4_unicode_ci
       AND p.is_deleted = 0)
ORDER BY CASE WHEN p.biz_id COLLATE utf8mb4_unicode_ci = @configured_mock_portfolio_biz_id COLLATE utf8mb4_unicode_ci THEN 0 ELSE 1 END,
         p.updated_at DESC
LIMIT 1;

SET @default_mock_portfolio_biz_id := COALESCE(@default_mock_portfolio_biz_id, UUID());

INSERT INTO aiw_portfolio
(biz_id, portfolio_no, owner_user_biz_id, portfolio_name, portfolio_type, base_currency, status, version,
 created_at, updated_at, created_by, updated_by, is_deleted)
VALUES
(@default_mock_portfolio_biz_id, 'MPAUTO00000000000001', @default_mock_user_biz_id, @default_mock_portfolio_name,
 'SIMULATION', 'CNY', 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'V46_RESET_DEFAULT_POOL', 'V46_RESET_DEFAULT_POOL', 0)
ON DUPLICATE KEY UPDATE
owner_user_biz_id = VALUES(owner_user_biz_id),
portfolio_name = VALUES(portfolio_name),
portfolio_type = 'SIMULATION',
base_currency = 'CNY',
status = 1,
updated_by = 'V46_RESET_DEFAULT_POOL',
is_deleted = 0,
deleted_at = NULL,
updated_at = CURRENT_TIMESTAMP(3);

CREATE TEMPORARY TABLE tmp_v46_default_mock_orders AS
SELECT biz_id
FROM aiw_order
WHERE portfolio_biz_id = @default_mock_portfolio_biz_id;

DELETE FROM aiw_order_event
WHERE order_biz_id IN (SELECT biz_id FROM tmp_v46_default_mock_orders);

DELETE FROM aiw_trade_execution
WHERE portfolio_biz_id = @default_mock_portfolio_biz_id
   OR order_biz_id IN (SELECT biz_id FROM tmp_v46_default_mock_orders);

DELETE FROM aiw_order
WHERE portfolio_biz_id = @default_mock_portfolio_biz_id;

DELETE FROM aiw_position
WHERE portfolio_biz_id = @default_mock_portfolio_biz_id;

DELETE FROM aiw_portfolio_valuation
WHERE portfolio_biz_id = @default_mock_portfolio_biz_id;

DROP TEMPORARY TABLE tmp_v46_default_mock_orders;

INSERT INTO aiw_portfolio_valuation
(biz_id, portfolio_biz_id, valuation_time, base_currency, total_asset, cash_balance, position_value,
 total_cost, unrealized_profit, realized_profit, total_return_rate, source_code, created_at)
VALUES
(UUID(), @default_mock_portfolio_biz_id, CURRENT_TIMESTAMP(3), 'CNY', @default_initial_cash, @default_initial_cash, 0,
 0, 0, 0, 0, 'V46_RESET_DEFAULT_POOL', CURRENT_TIMESTAMP(3));

UPDATE aiw_system_config
SET config_value = JSON_QUOTE(@default_mock_portfolio_biz_id),
    status = 'ENABLED',
    updated_at = CURRENT_TIMESTAMP(3)
WHERE config_group = 'AUTO_INVESTMENT_CLOSED_LOOP'
  AND config_key = 'mockPortfolioBizId'
  AND environment = 'DEFAULT';

UPDATE aiw_system_config
SET config_value = JSON_SET(
        COALESCE(config_value, JSON_OBJECT()),
        '$.mockPortfolioBizId', @default_mock_portfolio_biz_id,
        '$.mockUserBizId', @default_mock_user_biz_id,
        '$.mockPortfolioName', @default_mock_portfolio_name,
        '$.initialCash', CAST(@default_initial_cash AS CHAR)
    ),
    status = 'ENABLED',
    updated_at = CURRENT_TIMESTAMP(3)
WHERE config_group = 'AUTO_INVESTMENT_CLOSED_LOOP_PROFILE'
  AND config_key = 'default-auto-mock'
  AND environment = 'DEFAULT';
