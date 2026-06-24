-- ============================================================================
-- V17 重置高质量数据补全任务和默认 OpenAI 分析模型
-- 设计说明：
--   1. 将既有定时任务统一停用，再初始化一组可前端配置的高质量数据任务。
--   2. 注册 L1/L2/L3 优先的数据源，避免继续把兜底数据包装成正式数据。
--   3. 自动报告生成任务默认使用 OpenAI 兼容模型；开发环境可通过 mockEnabled 保持本地验证。
-- ============================================================================

UPDATE aiw_investment_task_definition
SET enabled = 0,
    description = LEFT(CONCAT(COALESCE(description, ''), '；V17重置后停用，等待新任务替代'), 512),
    updated_at = CURRENT_TIMESTAMP(3);

INSERT INTO aiw_data_source
(biz_id, source_code, source_name, source_type, trust_level, base_url,
 enabled, fetch_frequency, owner, description, created_at, updated_at, created_by, updated_by)
VALUES
('17000000-0000-0000-0000-000000000001', 'CSRC', '中国证监会', 'REGULATORY', 'L1',
 'https://www.csrc.gov.cn', 1, '0 */30 * * * *', 'SYSTEM',
 '监管政策、处罚、市场制度和官方公告，高可信来源。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM'),
('17000000-0000-0000-0000-000000000002', 'SSE', '上海证券交易所', 'ANNOUNCEMENT', 'L1',
 'https://www.sse.com.cn', 1, '0 */20 * * * *', 'SYSTEM',
 '上交所公告、产品披露和交易所公开信息。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM'),
('17000000-0000-0000-0000-000000000003', 'SZSE', '深圳证券交易所', 'ANNOUNCEMENT', 'L1',
 'https://www.szse.cn', 1, '0 */20 * * * *', 'SYSTEM',
 '深交所公告、产品披露和交易所公开信息。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM'),
('17000000-0000-0000-0000-000000000004', 'CNINFO', '巨潮资讯', 'ANNOUNCEMENT', 'L1',
 'https://www.cninfo.com.cn', 1, '0 */15 * * * *', 'SYSTEM',
 '上市公司公告和披露文件，适合补充公告与产品事件。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM'),
('17000000-0000-0000-0000-000000000005', 'CHINA_WEALTH', '中国理财网', 'MARKET', 'L2',
 'https://www.chinawealth.com.cn', 1, '0 0 */2 * * *', 'SYSTEM',
 '银行理财产品、净值和产品公开信息。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM'),
('17000000-0000-0000-0000-000000000006', 'EASTMONEY', '东方财富', 'MARKET', 'L4',
 'https://www.eastmoney.com', 1, '0 */10 * * * *', 'SYSTEM',
 '行情、资讯和产品补充来源，需要与 L1/L2 数据交叉验证。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM'),
('17000000-0000-0000-0000-000000000007', 'WIND', 'Wind 金融终端', 'MARKET', 'L3',
 'vendor://wind', 0, '按供应商授权配置', 'SYSTEM',
 '专业行情、研报和资金流供应商，占位数据源；启用前需要配置授权。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM'),
('17000000-0000-0000-0000-000000000008', 'CHOICE', 'Choice 金融终端', 'MARKET', 'L3',
 'vendor://choice', 0, '按供应商授权配置', 'SYSTEM',
 '专业行情和产品数据供应商，占位数据源；启用前需要配置授权。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
source_name = VALUES(source_name),
source_type = VALUES(source_type),
trust_level = VALUES(trust_level),
base_url = VALUES(base_url),
enabled = VALUES(enabled),
fetch_frequency = VALUES(fetch_frequency),
owner = VALUES(owner),
description = VALUES(description),
updated_at = CURRENT_TIMESTAMP(3),
updated_by = 'SYSTEM';

INSERT INTO aiw_data_source_health
(biz_id, source_code, last_success_at, last_failure_at, success_rate,
 avg_latency_ms, failure_reason, sample_count, updated_at)
VALUES
('17000000-0000-0000-0000-000000000101', 'CSRC', NULL, NULL, 0, NULL, '等待首次采集', 0, CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000102', 'SSE', NULL, NULL, 0, NULL, '等待首次采集', 0, CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000103', 'SZSE', NULL, NULL, 0, NULL, '等待首次采集', 0, CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000104', 'CNINFO', NULL, NULL, 0, NULL, '等待首次采集', 0, CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000105', 'CHINA_WEALTH', NULL, NULL, 0, NULL, '等待首次采集', 0, CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000106', 'EASTMONEY', NULL, NULL, 0, NULL, '等待首次采集', 0, CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000107', 'WIND', NULL, NULL, 0, NULL, '供应商授权未启用', 0, CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000108', 'CHOICE', NULL, NULL, 0, NULL, '供应商授权未启用', 0, CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE
failure_reason = VALUES(failure_reason),
updated_at = CURRENT_TIMESTAMP(3);

INSERT INTO aiw_investment_task_definition
(biz_id, task_code, task_type, cron, zone, enabled, parameters, description, created_at, updated_at)
VALUES
('17000000-0000-0000-0000-000000000201', 'l1-regulatory-news-collection', 'INVESTMENT_NEWS_COLLECTION',
 '0 */30 * * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT(
   'feedUrls', '',
   'sourceCode', 'CSRC',
   'languageCode', 'zh-CN',
   'maxItems', '80',
   'fallbackEnabled', 'false'
 ),
 'L1监管资讯采集任务。默认不写兜底数据，前端可配置 feedUrls 或接入专用采集器。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000202', 'l1-exchange-announcement-collection', 'INVESTMENT_NEWS_COLLECTION',
 '0 */20 * * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT(
   'feedUrls', '',
   'sourceCode', 'CNINFO',
   'languageCode', 'zh-CN',
   'maxItems', '100',
   'fallbackEnabled', 'false'
 ),
 'L1交易所和公告采集任务。默认不写兜底数据，后续接交易所/巨潮专用解析器。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000203', 'l2-wealth-product-refresh', 'INVESTMENT_NEWS_COLLECTION',
 '0 0 */2 * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT(
   'feedUrls', '',
   'sourceCode', 'CHINA_WEALTH',
   'languageCode', 'zh-CN',
   'maxItems', '100',
   'fallbackEnabled', 'false'
 ),
 'L2银行理财产品和净值补全任务。当前复用资讯采集入口，后续替换为产品专用处理器。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000204', 'cn-mainland-market-momentum-scan', 'MARKET_MOMENTUM_SCAN',
 '0 */5 * * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT(
   'windowMinutes', '60',
   'marketScope', 'CN_MAINLAND',
   'themes', 'AI人工智能=159819,588000,515980;半导体=512480,159995,688981;黄金=518880,159934',
   'themeMarketScopes', 'AI人工智能=CN_MAINLAND;半导体=CN_MAINLAND;黄金=CN_MAINLAND'
 ),
 '中国大陆核心主题动量扫描，依赖高质量行情数据。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000205', 'cn-mainland-hot-theme-return', 'HOT_THEME_RETURN',
 '30 */10 * * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT(
   'windowMinutes', '1440',
   'marketScope', 'CN_MAINLAND',
   'themes', 'AI人工智能=159819,588000,515980;半导体=512480,159995,688981;黄金=518880,159934',
   'themeMarketScopes', 'AI人工智能=CN_MAINLAND;半导体=CN_MAINLAND;黄金=CN_MAINLAND'
 ),
 '中国大陆核心主题日内/日级收益快照。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000206', 'cn-mainland-news-heat-aggregation', 'NEWS_HEAT_AGGREGATION',
 '45 */10 * * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT(
   'windowMinutes', '1440',
   'marketScope', 'CN_MAINLAND',
   'themes', 'AI人工智能=AI,人工智能,算力,大模型;半导体=半导体,芯片,集成电路,晶圆;黄金=黄金,金价,贵金属,避险',
   'themeProducts', 'AI人工智能=159819,588000,515980;半导体=512480,159995,688981;黄金=518880,159934',
   'themeMarketScopes', 'AI人工智能=CN_MAINLAND;半导体=CN_MAINLAND;黄金=CN_MAINLAND'
 ),
 '高质量资讯入库后的主题热度聚合，生成资讯-主题-产品证据链。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000207', 'auto-openai-investment-report-generation', 'AUTO_INVESTMENT_REPORT_GENERATION',
 '0 5 */1 * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT(
   'providerCode', 'OPENAI_COMPATIBLE',
   'modelCode', 'openai-compatible-analysis',
   'marketScope', 'CN_MAINLAND',
   'lookbackDays', '30',
   'initialCapital', '100000',
   'themes', 'AI人工智能=159819,588000,515980;半导体=512480,159995,688981;黄金=518880,159934'
 ),
 '自动投资报告生成任务。默认 OpenAI 兼容模型，前端可配置模型、主题、Cron 和资金参数。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE
task_type = VALUES(task_type),
cron = VALUES(cron),
zone = VALUES(zone),
enabled = VALUES(enabled),
parameters = VALUES(parameters),
description = VALUES(description),
updated_at = CURRENT_TIMESTAMP(3);

UPDATE aiw_ai_model
SET model_version = 'default-v1',
    model_name = 'OpenAI 默认投资分析模型',
    provider = 'OPENAI_COMPATIBLE',
    artifact_uri = 'https://api.openai.com/v1',
    model_config = JSON_OBJECT(
        'baseUrl', 'https://api.openai.com/v1',
        'model', 'gpt-4.1-mini',
        'secretRef', 'OPENAI_API_KEY',
        'timeoutSeconds', 90,
        'temperature', 0.2,
        'mockEnabled', true
    ),
    metrics = JSON_OBJECT(
        'configurationType', 'OPENAI_DEFAULT',
        'protocol', 'OPENAI_COMPATIBLE',
        'frontConfigurable', true,
        'defaultForAutoReport', true,
        'mockEnabledDefault', true
    ),
    status = 'ACTIVE',
    activated_at = CURRENT_TIMESTAMP(3),
    retired_at = NULL,
    updated_at = CURRENT_TIMESTAMP(3)
WHERE model_code = 'openai-compatible-analysis';
