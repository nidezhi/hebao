-- ============================================================================
-- V33 第一批确定性真实数据采集任务与低成本安全调度
-- 说明：
--   1. AI 数据源发现、AI 结构化采集、自动报告、Prompt 治理、闭环编排默认关闭定时。
--   2. 新增产品池、行情、资讯、真实数据质量快照四个确定性采集任务。
--   3. 主闭环改为先执行真实数据任务，只有质量门禁通过后才允许模型报告和 Mock 闭环。
-- ============================================================================

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

INSERT INTO aiw_investment_task_definition
(biz_id, task_code, task_type, cron, zone, enabled, parameters, description, created_at, updated_at)
VALUES
('33000000-0000-0000-0000-000000000001', 'real-product-universe-sync',
 'REAL_PRODUCT_UNIVERSE_SYNC', '0 30 8 * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT(
   'sourceCode', 'AKSHARE',
   'providerBaseUrl', '',
   'marketScope', 'CN_MAINLAND',
   'defaultCurrency', 'CNY',
   'defaultRiskLevel', '3',
   'lookbackDays', '10',
   'timeoutSeconds', '12',
   'themes', 'AI人工智能=159819,588000,515980;半导体=512480,159995,688981;黄金=518880,159934'
 ),
 '确定性真实产品池同步。产品代码来自任务配置或授权数据源，不调用大模型。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('33000000-0000-0000-0000-000000000002', 'real-market-quote-sync',
 'REAL_MARKET_QUOTE_SYNC', '0 30 17 * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT(
   'sourceCode', 'AKSHARE',
   'providerBaseUrl', '',
   'marketScope', 'CN_MAINLAND',
   'quoteInterval', '1D',
   'lookbackDays', '10',
   'timeoutSeconds', '20',
   'themes', 'AI人工智能=159819,588000,515980;半导体=512480,159995,688981;黄金=518880,159934'
 ),
 '确定性真实行情/净值同步。需要配置 AKShare/AKTools 或授权行情源 providerBaseUrl。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('33000000-0000-0000-0000-000000000003', 'real-news-sync',
 'REAL_NEWS_SYNC', '0 0 */2 * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT(
   'sourceCode', 'AKSHARE_NEWS',
   'providerBaseUrl', '',
   'marketScope', 'CN_MAINLAND',
   'keywords', 'AI,人工智能,算力,大模型,半导体,芯片,集成电路,黄金,金价,贵金属',
   'lookbackDays', '3',
   'maxItems', '100',
   'timeoutSeconds', '20',
   'themes', 'AI人工智能=159819,588000,515980;半导体=512480,159995,688981;黄金=518880,159934'
 ),
 '确定性真实资讯同步。需要配置 AKShare/AKTools 或授权资讯源 providerBaseUrl。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('33000000-0000-0000-0000-000000000004', 'real-data-quality-snapshot',
 'REAL_DATA_QUALITY_SNAPSHOT', '0 10 * * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT(
   'marketScope', 'CN_MAINLAND',
   'freshnessHours', '72',
   'minNewsCount', '20',
   'keywords', 'AI,人工智能,算力,大模型,半导体,芯片,集成电路,黄金,金价,贵金属',
   'themes', 'AI人工智能=159819,588000,515980;半导体=512480,159995,688981;黄金=518880,159934'
 ),
 '真实核心数据质量快照。报告和闭环必须先看该门禁。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE
task_type = VALUES(task_type),
cron = VALUES(cron),
zone = VALUES(zone),
enabled = VALUES(enabled),
parameters = VALUES(parameters),
description = VALUES(description),
updated_at = CURRENT_TIMESTAMP(3);

UPDATE aiw_investment_task_definition
SET cron = '0 20 * * * *',
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_code = 'cn-mainland-market-momentum-scan';

UPDATE aiw_investment_task_definition
SET cron = '0 25 * * * *',
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_code = 'cn-mainland-hot-theme-return';

UPDATE aiw_investment_task_definition
SET cron = '0 30 * * * *',
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_code = 'cn-mainland-news-heat-aggregation';

UPDATE aiw_investment_task_definition
SET cron = '0 30 20 * * *',
    enabled = 0,
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_code = 'auto-openai-investment-report-generation';

UPDATE aiw_investment_task_definition
SET cron = '0 0 21 * * *',
    enabled = 0,
    parameters = JSON_SET(
        parameters,
        '$.dataTaskCodes',
        'real-product-universe-sync,real-market-quote-sync,real-news-sync,real-data-quality-snapshot,cn-mainland-market-momentum-scan,cn-mainland-hot-theme-return,cn-mainland-news-heat-aggregation',
        '$.requireStructuredCoreData',
        'true',
        '$.minStructuredNewsCount',
        '1',
        '$.minStructuredQuoteCount',
        '1'
    ),
    description = '自动投资闭环总编排任务。默认关闭定时；手动验证时先执行真实数据采集和质量门禁，再生成报告、Prompt候选、Mock交易、回测和反馈。',
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_code = 'auto-investment-closed-loop-orchestration';
