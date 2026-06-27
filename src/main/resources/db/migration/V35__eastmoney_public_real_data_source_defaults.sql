-- ============================================================================
-- V35 默认真实采集源切换为东方财富公开源
-- 说明：
--   1. 未配置 providerBaseUrl 时，行情使用东方财富公开 K 线接口。
--   2. 未配置 providerBaseUrl 时，资讯使用东方财富公开财经新闻页面。
--   3. 仍保留 providerBaseUrl 覆盖能力，后续可切 AKShare/AKTools 或授权数据商。
-- ============================================================================

UPDATE aiw_investment_task_definition
SET parameters = JSON_SET(
        parameters,
        '$.sourceCode',
        'EASTMONEY_PUBLIC',
        '$.providerBaseUrl',
        ''
    ),
    description = '确定性真实产品池同步。默认按授权代码清单建立产品主档；可配置providerBaseUrl补充元数据。',
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_code = 'real-product-universe-sync';

UPDATE aiw_investment_task_definition
SET parameters = JSON_SET(
        parameters,
        '$.sourceCode',
        'EASTMONEY_PUBLIC',
        '$.providerBaseUrl',
        '',
        '$.requireProviderBaseUrl',
        'false'
    ),
    description = '确定性真实行情/净值同步。默认使用东方财富公开K线接口；可配置providerBaseUrl切换到AKShare/AKTools或授权行情源。',
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_code = 'real-market-quote-sync';

UPDATE aiw_investment_task_definition
SET parameters = JSON_SET(
        parameters,
        '$.sourceCode',
        'EASTMONEY_NEWS',
        '$.providerBaseUrl',
        '',
        '$.requireProviderBaseUrl',
        'false'
    ),
    description = '确定性真实资讯同步。默认使用东方财富公开财经新闻页面；可配置providerBaseUrl切换到AKShare/AKTools或授权资讯源。',
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_code = 'real-news-sync';
