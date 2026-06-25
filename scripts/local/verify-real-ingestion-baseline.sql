-- DZCOM 本地/开发环境真实采集启动基线只读验收脚本。

SELECT 'users' AS metric, COUNT(*) AS value FROM aiw_user
UNION ALL SELECT 'data_sources', COUNT(*) FROM aiw_data_source
UNION ALL SELECT 'data_source_health', COUNT(*) FROM aiw_data_source_health
UNION ALL SELECT 'task_definitions', COUNT(*) FROM aiw_investment_task_definition
UNION ALL SELECT 'ai_models', COUNT(*) FROM aiw_ai_model
UNION ALL SELECT 'prompt_templates', COUNT(*) FROM aiw_ai_prompt_template
UNION ALL SELECT 'products_should_be_zero', COUNT(*) FROM aiw_product
UNION ALL SELECT 'quotes_should_be_zero', COUNT(*) FROM aiw_market_quote
UNION ALL SELECT 'news_should_be_zero', COUNT(*) FROM aiw_news_article
UNION ALL SELECT 'reports_should_be_zero', COUNT(*) FROM aiw_investment_analysis_report
UNION ALL SELECT 'portfolios_should_be_zero', COUNT(*) FROM aiw_portfolio
UNION ALL SELECT 'orders_should_be_zero', COUNT(*) FROM aiw_order
UNION ALL SELECT 'risk_checks_should_be_zero', COUNT(*) FROM aiw_risk_check
UNION ALL SELECT 'backtests_should_be_zero', COUNT(*) FROM aiw_backtest_result
UNION ALL SELECT 'feedback_should_be_zero', COUNT(*) FROM aiw_investment_feedback
UNION ALL SELECT 'prompt_evaluations_should_be_zero', COUNT(*) FROM aiw_ai_prompt_evaluation;

SELECT task_code, task_type, enabled, cron
FROM aiw_investment_task_definition
ORDER BY task_code;

SELECT h.source_code, s.enabled, h.success_rate, h.sample_count, h.failure_reason
FROM aiw_data_source_health h
JOIN aiw_data_source s ON s.source_code = h.source_code
ORDER BY h.source_code;
