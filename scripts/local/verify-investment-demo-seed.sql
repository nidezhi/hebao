-- DZCOM 本地/开发环境初始化数据只读验收脚本。

SELECT 'aiw_user' AS table_name, COUNT(*) AS row_count FROM aiw_user
UNION ALL SELECT 'aiw_product', COUNT(*) FROM aiw_product
UNION ALL SELECT 'aiw_market_quote', COUNT(*) FROM aiw_market_quote
UNION ALL SELECT 'aiw_news_article', COUNT(*) FROM aiw_news_article
UNION ALL SELECT 'aiw_news_article_relation', COUNT(*) FROM aiw_news_article_relation
UNION ALL SELECT 'aiw_investment_task_definition', COUNT(*) FROM aiw_investment_task_definition
UNION ALL SELECT 'aiw_investment_analysis_report', COUNT(*) FROM aiw_investment_analysis_report
UNION ALL SELECT 'aiw_ai_prompt_template', COUNT(*) FROM aiw_ai_prompt_template
UNION ALL SELECT 'aiw_portfolio', COUNT(*) FROM aiw_portfolio
UNION ALL SELECT 'aiw_order', COUNT(*) FROM aiw_order
UNION ALL SELECT 'aiw_risk_check', COUNT(*) FROM aiw_risk_check
UNION ALL SELECT 'aiw_backtest_result', COUNT(*) FROM aiw_backtest_result
UNION ALL SELECT 'aiw_investment_feedback', COUNT(*) FROM aiw_investment_feedback;

SELECT product_code, product_name, product_type, market_code, risk_level
FROM aiw_product
ORDER BY product_code;

SELECT task_code, task_type, enabled
FROM aiw_investment_task_definition
ORDER BY task_code;
