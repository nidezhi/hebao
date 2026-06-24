-- ============================================================================
-- DZCOM 本地/开发环境业务数据重置与高质量演示数据初始化
--
-- 用途：
--   1. 清空投资平台业务数据，保留 Flyway 历史、角色权限等系统基线。
--   2. 注入前端重构和后端闭环联调所需的稳定演示数据。
--   3. 初始化优秀、可配置、可审计的任务、数据源、报告、Prompt、Mock 交易和反馈链路。
--
-- 安全边界：
--   1. 仅允许在数据库名为 dz_database 的本地/开发库执行。
--   2. 禁止作为 Flyway 迁移自动执行。
--   3. 执行前请确认连接目标：SELECT DATABASE();
-- ============================================================================

CREATE TEMPORARY TABLE dzcom_reset_guard (
    database_name VARCHAR(64) NOT NULL
) ENGINE=MEMORY;

INSERT INTO dzcom_reset_guard (database_name)
SELECT CASE
           WHEN DATABASE() = 'dz_database' THEN DATABASE()
           ELSE NULL
       END;

DROP TEMPORARY TABLE dzcom_reset_guard;

START TRANSACTION;

-- 1. 清空业务流水和闭环数据。保留 flyway_schema_history、角色、权限和非敏感系统配置。
DELETE FROM aiw_ai_prompt_evaluation;
DELETE FROM aiw_investment_feedback;
DELETE FROM aiw_backtest_result;
DELETE FROM aiw_ai_recommendation;
DELETE FROM aiw_ai_signal;
DELETE FROM aiw_investment_analysis_report;
DELETE FROM aiw_ai_prompt_output_schema;
DELETE FROM aiw_ai_prompt_variable;
DELETE FROM aiw_ai_prompt_template;
DELETE FROM aiw_order_event;
DELETE FROM aiw_trade_execution;
DELETE FROM aiw_order;
DELETE FROM aiw_position;
DELETE FROM aiw_portfolio_valuation;
DELETE FROM aiw_portfolio;
DELETE FROM aiw_risk_check;
DELETE FROM aiw_notification;
DELETE FROM aiw_audit_log;
DELETE FROM aiw_outbox_event;
DELETE FROM aiw_news_article_relation;
DELETE FROM aiw_news_target;
DELETE FROM aiw_news_article;
DELETE FROM aiw_investment_theme_snapshot;
DELETE FROM aiw_scheduled_task_execution;
DELETE FROM aiw_market_quote;
DELETE FROM aiw_product_theme_relation;
DELETE FROM aiw_product_investment_profile;
DELETE FROM aiw_product_attribute;
DELETE FROM aiw_product;
DELETE FROM aiw_data_quality_snapshot;
DELETE FROM aiw_data_source_health;
DELETE FROM aiw_data_source;
DELETE FROM aiw_investment_task_definition;
DELETE FROM aiw_user_preference;
DELETE FROM aiw_user_role;
DELETE FROM aiw_user_risk_profile;
DELETE FROM aiw_user_profile;
DELETE FROM aiw_user_credential;
DELETE FROM aiw_user_identity;
DELETE FROM aiw_user;
DELETE FROM aiw_risk_rule;
DELETE FROM aiw_ai_model;

-- 2. 初始化演示用户。默认密码：Demo@123456。
INSERT INTO aiw_user
(biz_id, user_no, status, version, registered_at, last_login_at, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at)
VALUES
('21000000-0000-0000-0000-000000000001', 'U-DEMO-ADMIN', 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SEED', 'SEED', 0, NULL),
('21000000-0000-0000-0000-000000000002', 'U-DEMO-INVESTOR', 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SEED', 'SEED', 0, NULL);

INSERT INTO aiw_user_identity
(biz_id, user_biz_id, identity_type, identity_value, normalized_value, verified, verified_at, status, created_at, updated_at, is_deleted)
VALUES
('21000000-0000-0000-0001-000000000001', '21000000-0000-0000-0000-000000000001', 'USERNAME', 'demo_admin', 'demo_admin', 1, CURRENT_TIMESTAMP(3), 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
('21000000-0000-0000-0001-000000000002', '21000000-0000-0000-0000-000000000001', 'EMAIL', 'demo.admin@dzcom.local', 'demo.admin@dzcom.local', 1, CURRENT_TIMESTAMP(3), 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
('21000000-0000-0000-0001-000000000003', '21000000-0000-0000-0000-000000000002', 'USERNAME', 'demo_investor', 'demo_investor', 1, CURRENT_TIMESTAMP(3), 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
('21000000-0000-0000-0001-000000000004', '21000000-0000-0000-0000-000000000002', 'EMAIL', 'demo.investor@dzcom.local', 'demo.investor@dzcom.local', 1, CURRENT_TIMESTAMP(3), 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO aiw_user_credential
(biz_id, user_biz_id, credential_type, secret_hash, hash_algorithm, credential_version, expires_at, changed_at, failed_attempts, locked_until, created_at, updated_at, is_deleted)
VALUES
('21000000-0000-0000-0002-000000000001', '21000000-0000-0000-0000-000000000001', 'PASSWORD', '$2a$12$gxgjXCIIfZnUlEU57dEqveVStiZPqXBodFTwqQ4kObMVmI.XihPZS', 'BCRYPT', 1, NULL, CURRENT_TIMESTAMP(3), 0, NULL, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
('21000000-0000-0000-0002-000000000002', '21000000-0000-0000-0000-000000000002', 'PASSWORD', '$2a$12$gxgjXCIIfZnUlEU57dEqveVStiZPqXBodFTwqQ4kObMVmI.XihPZS', 'BCRYPT', 1, NULL, CURRENT_TIMESTAMP(3), 0, NULL, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO aiw_user_profile
(biz_id, user_biz_id, nickname, avatar_url, locale, timezone, profile_ext, created_at, updated_at, is_deleted)
VALUES
('21000000-0000-0000-0003-000000000001', '21000000-0000-0000-0000-000000000001', '演示管理员', NULL, 'zh-CN', 'Asia/Shanghai', JSON_OBJECT('workspace', 'investment-admin'), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
('21000000-0000-0000-0003-000000000002', '21000000-0000-0000-0000-000000000002', '稳健型演示投资人', NULL, 'zh-CN', 'Asia/Shanghai', JSON_OBJECT('workspace', 'investor-console'), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO aiw_user_risk_profile
(biz_id, user_biz_id, kyc_status, risk_level, assessment_version, assessed_at, kyc_reviewed_at, ext_data, created_at, updated_at, is_deleted)
VALUES
('21000000-0000-0000-0004-000000000001', '21000000-0000-0000-0000-000000000001', 1, 5, 'seed-admin-v1', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), JSON_OBJECT('purpose', 'admin-test'), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
('21000000-0000-0000-0004-000000000002', '21000000-0000-0000-0000-000000000002', 1, 3, 'seed-investor-v1', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), JSON_OBJECT('investmentHorizon', '6-12M', 'lossTolerance', 'MEDIUM'), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO aiw_user_role
(biz_id, user_biz_id, role_code, scope_code, effective_from, effective_to, created_at, created_by, is_deleted)
VALUES
('21000000-0000-0000-0005-000000000001', '21000000-0000-0000-0000-000000000001', 'ADMIN', 'GLOBAL', CURRENT_TIMESTAMP(3), NULL, CURRENT_TIMESTAMP(3), 'SEED', 0),
('21000000-0000-0000-0005-000000000002', '21000000-0000-0000-0000-000000000002', 'USER', 'GLOBAL', CURRENT_TIMESTAMP(3), NULL, CURRENT_TIMESTAMP(3), 'SEED', 0);

INSERT INTO aiw_user_preference
(biz_id, user_biz_id, preference_key, value_type, preference_value, created_at, updated_at, is_deleted)
VALUES
('21000000-0000-0000-0006-000000000001', '21000000-0000-0000-0000-000000000002', 'investment.watchThemes', 'JSON', JSON_ARRAY('AI_CN', 'SEMICONDUCTOR_CN', 'GOLD_CN'), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
('21000000-0000-0000-0006-000000000002', '21000000-0000-0000-0000-000000000002', 'investment.riskNoticeConfirmed', 'BOOLEAN', JSON_EXTRACT('true', '$'), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

-- 3. 初始化高质量数据源、健康状态和质量快照。
INSERT INTO aiw_data_source
(biz_id, source_code, source_name, source_type, trust_level, base_url, enabled, fetch_frequency, owner, description, created_at, updated_at, created_by, updated_by)
VALUES
('17000000-0000-0000-0000-000000000001', 'CSRC', '中国证监会', 'REGULATORY', 'L1', 'https://www.csrc.gov.cn', 1, '0 */30 * * * *', 'SYSTEM', '监管政策、处罚、市场制度和官方公告，高可信来源。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SEED', 'SEED'),
('17000000-0000-0000-0000-000000000002', 'SSE', '上海证券交易所', 'ANNOUNCEMENT', 'L1', 'https://www.sse.com.cn', 1, '0 */20 * * * *', 'SYSTEM', '上交所公告、ETF披露和交易所公开信息。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SEED', 'SEED'),
('17000000-0000-0000-0000-000000000003', 'SZSE', '深圳证券交易所', 'ANNOUNCEMENT', 'L1', 'https://www.szse.cn', 1, '0 */20 * * * *', 'SYSTEM', '深交所公告、ETF披露和交易所公开信息。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SEED', 'SEED'),
('17000000-0000-0000-0000-000000000004', 'CNINFO', '巨潮资讯', 'ANNOUNCEMENT', 'L1', 'https://www.cninfo.com.cn', 1, '0 */15 * * * *', 'SYSTEM', '上市公司公告和披露文件。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SEED', 'SEED'),
('17000000-0000-0000-0000-000000000005', 'CHINA_WEALTH', '中国理财网', 'MARKET', 'L2', 'https://www.chinawealth.com.cn', 1, '0 0 */2 * * *', 'SYSTEM', '银行理财产品、净值和产品公开信息。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SEED', 'SEED'),
('17000000-0000-0000-0000-000000000006', 'EASTMONEY', '东方财富', 'MARKET', 'L4', 'https://www.eastmoney.com', 1, '0 */10 * * * *', 'SYSTEM', '行情、资讯和产品补充来源，需要与 L1/L2 数据交叉验证。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SEED', 'SEED'),
('17000000-0000-0000-0000-000000000007', 'WIND', 'Wind 金融终端', 'MARKET', 'L3', 'vendor://wind', 0, '按供应商授权配置', 'SYSTEM', '专业行情、研报和资金流供应商，占位数据源；启用前需要配置授权。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SEED', 'SEED'),
('17000000-0000-0000-0000-000000000008', 'CHOICE', 'Choice 金融终端', 'MARKET', 'L3', 'vendor://choice', 0, '按供应商授权配置', 'SYSTEM', '专业行情和产品数据供应商，占位数据源；启用前需要配置授权。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SEED', 'SEED');

INSERT INTO aiw_data_source_health
(biz_id, source_code, last_success_at, last_failure_at, success_rate, avg_latency_ms, failure_reason, sample_count, updated_at)
VALUES
('17000000-0000-0000-0000-000000000101', 'CSRC', CURRENT_TIMESTAMP(3), NULL, 0.9800, 320, NULL, 18, CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000102', 'SSE', CURRENT_TIMESTAMP(3), NULL, 0.9700, 280, NULL, 35, CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000103', 'SZSE', CURRENT_TIMESTAMP(3), NULL, 0.9700, 295, NULL, 31, CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000104', 'CNINFO', CURRENT_TIMESTAMP(3), NULL, 0.9600, 410, NULL, 46, CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000105', 'CHINA_WEALTH', CURRENT_TIMESTAMP(3), NULL, 0.9400, 520, NULL, 12, CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000106', 'EASTMONEY', CURRENT_TIMESTAMP(3), NULL, 0.9000, 260, NULL, 64, CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000107', 'WIND', NULL, NULL, 0.0000, NULL, '供应商授权未启用', 0, CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000108', 'CHOICE', NULL, NULL, 0.0000, NULL, '供应商授权未启用', 0, CURRENT_TIMESTAMP(3));

INSERT INTO aiw_data_quality_snapshot
(biz_id, source_code, data_type, quality_score, missing_rate, duplicate_rate, freshness_score, sample_count, snapshot_time, detail, created_at)
VALUES
(UUID(), 'CSRC', 'REGULATORY', 0.9600, 0.0100, 0.0000, 0.9900, 18, CURRENT_TIMESTAMP(3), JSON_OBJECT('trustLevel', 'L1', 'gate', 'PASS'), CURRENT_TIMESTAMP(3)),
(UUID(), 'SSE', 'MARKET_QUOTE', 0.9300, 0.0200, 0.0000, 0.9700, 25, CURRENT_TIMESTAMP(3), JSON_OBJECT('trustLevel', 'L1', 'gate', 'PASS'), CURRENT_TIMESTAMP(3)),
(UUID(), 'SZSE', 'MARKET_QUOTE', 0.9300, 0.0200, 0.0000, 0.9700, 25, CURRENT_TIMESTAMP(3), JSON_OBJECT('trustLevel', 'L1', 'gate', 'PASS'), CURRENT_TIMESTAMP(3)),
(UUID(), 'CNINFO', 'ANNOUNCEMENT', 0.9200, 0.0300, 0.0100, 0.9500, 46, CURRENT_TIMESTAMP(3), JSON_OBJECT('trustLevel', 'L1', 'gate', 'PASS'), CURRENT_TIMESTAMP(3)),
(UUID(), 'CHINA_WEALTH', 'WEALTH_NAV', 0.9000, 0.0400, 0.0000, 0.9200, 12, CURRENT_TIMESTAMP(3), JSON_OBJECT('trustLevel', 'L2', 'gate', 'PASS'), CURRENT_TIMESTAMP(3)),
(UUID(), 'EASTMONEY', 'NEWS', 0.7600, 0.0800, 0.0500, 0.8800, 64, CURRENT_TIMESTAMP(3), JSON_OBJECT('trustLevel', 'L4', 'gate', 'CROSS_CHECK_REQUIRED'), CURRENT_TIMESTAMP(3));

-- 4. 初始化可配置任务。专用采集器默认不写兜底数据，端点由前端配置。
INSERT INTO aiw_investment_task_definition
(biz_id, task_code, task_type, cron, zone, enabled, parameters, description, created_at, updated_at)
VALUES
('17000000-0000-0000-0000-000000000201', 'l1-regulatory-disclosure-collection', 'REGULATORY_DISCLOSURE_COLLECTION', '0 */30 * * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT('endpoints', '', 'responseFormat', 'JSON', 'itemsPath', '', 'externalIdPath', 'id', 'titlePath', 'title', 'summaryPath', 'summary', 'contentPath', 'content', 'urlPath', 'url', 'publishTimePath', 'publishTime', 'extraFieldPaths', '', 'sourceCode', 'CSRC', 'articleType', 'REGULATORY', 'languageCode', 'zh-CN', 'maxItems', '80', 'timeoutSeconds', '20', 'freshnessHours', '72'),
 'L1监管披露专用采集任务。默认不写兜底数据，前端配置 endpoints 与 JSON 字段路径后启用真实采集。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000202', 'l1-exchange-announcement-collection', 'EXCHANGE_ANNOUNCEMENT_COLLECTION', '0 */20 * * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT('endpoints', '', 'responseFormat', 'JSON', 'itemsPath', '', 'externalIdPath', 'id', 'titlePath', 'title', 'summaryPath', 'summary', 'contentPath', 'content', 'urlPath', 'url', 'publishTimePath', 'publishTime', 'extraFieldPaths', '', 'sourceCode', 'CNINFO', 'articleType', 'ANNOUNCEMENT', 'languageCode', 'zh-CN', 'maxItems', '100', 'timeoutSeconds', '20', 'freshnessHours', '72'),
 'L1交易所和巨潮公告专用采集任务。默认不写兜底数据，前端配置 endpoints 与 JSON 字段路径后启用真实采集。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000203', 'l2-wealth-product-nav-refresh', 'WEALTH_PRODUCT_NAV_REFRESH', '0 0 */2 * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT('endpoints', '', 'responseFormat', 'JSON', 'itemsPath', '', 'externalIdPath', 'id', 'titlePath', 'productName', 'summaryPath', 'summary', 'contentPath', 'content', 'urlPath', 'url', 'publishTimePath', 'publishTime', 'extraFieldPaths', 'productCode=productCode;productName=productName;nav=nav;previousNav=previousNav;assetSize=assetSize;riskLevel=riskLevel', 'sourceCode', 'CHINA_WEALTH', 'articleType', 'WEALTH_NAV', 'languageCode', 'zh-CN', 'maxItems', '100', 'timeoutSeconds', '20', 'freshnessHours', '168', 'productMarketCode', 'BANK_WMP', 'productCurrency', 'CNY', 'quoteInterval', '1D', 'defaultRiskLevel', '2'),
 'L2银行理财产品和净值专用采集任务。产品池 upsert 后写入净值行情表。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000204', 'cn-mainland-market-momentum-scan', 'MARKET_MOMENTUM_SCAN', '0 */5 * * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT('windowMinutes', '60', 'marketScope', 'CN_MAINLAND', 'themes', 'AI人工智能=159819,588000,515980;半导体=512480,159995,688981;黄金=518880,159934', 'themeMarketScopes', 'AI人工智能=CN_MAINLAND;半导体=CN_MAINLAND;黄金=CN_MAINLAND'),
 '中国大陆核心主题动量扫描，依赖高质量行情数据。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000205', 'cn-mainland-hot-theme-return', 'HOT_THEME_RETURN', '30 */10 * * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT('windowMinutes', '1440', 'marketScope', 'CN_MAINLAND', 'themes', 'AI人工智能=159819,588000,515980;半导体=512480,159995,688981;黄金=518880,159934', 'themeMarketScopes', 'AI人工智能=CN_MAINLAND;半导体=CN_MAINLAND;黄金=CN_MAINLAND'),
 '中国大陆核心主题日内/日级收益快照。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000206', 'cn-mainland-news-heat-aggregation', 'NEWS_HEAT_AGGREGATION', '45 */10 * * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT('windowMinutes', '1440', 'marketScope', 'CN_MAINLAND', 'themes', 'AI人工智能=AI,人工智能,算力,大模型;半导体=半导体,芯片,集成电路,晶圆;黄金=黄金,金价,贵金属,避险', 'themeProducts', 'AI人工智能=159819,588000,515980;半导体=512480,159995,688981;黄金=518880,159934', 'themeMarketScopes', 'AI人工智能=CN_MAINLAND;半导体=CN_MAINLAND;黄金=CN_MAINLAND'),
 '高质量资讯入库后的主题热度聚合，生成资讯-主题-产品证据链。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000207', 'auto-openai-investment-report-generation', 'AUTO_INVESTMENT_REPORT_GENERATION', '0 5 */1 * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT('providerCode', 'OPENAI_COMPATIBLE', 'modelCode', 'openai-compatible-analysis', 'marketScope', 'CN_MAINLAND', 'lookbackDays', '30', 'initialCapital', '100000', 'themes', 'AI人工智能=159819,588000,515980;半导体=512480,159995,688981;黄金=518880,159934'),
 '自动投资报告生成任务。默认 OpenAI 兼容模型，前端可配置模型、主题、Cron 和资金参数。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3));

-- 5. 初始化模型配置。默认 OpenAI 兼容模型仍保持 mockEnabled=true。
INSERT INTO aiw_ai_model
(biz_id, model_code, model_version, model_name, model_type, provider, artifact_uri, model_config, metrics, status, activated_at, retired_at, created_at, updated_at)
VALUES
('10000000-0000-0000-0000-000000000001', 'local-rule-analysis', 'v1', '本地规则投资分析模型', 'ANALYSIS', 'LOCAL_RULE', 'classpath:local-rule-v1',
 JSON_OBJECT('model', 'local-rule-v1', 'timeoutSeconds', 30, 'temperature', 0, 'mockEnabled', false),
 JSON_OBJECT('configurationType', 'LOCAL_RULE', 'initializedBy', 'reset-and-seed'), 'ACTIVE', CURRENT_TIMESTAMP(3), NULL, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('10000000-0000-0000-0000-000000000002', 'openai-compatible-analysis', 'default-v1', 'OpenAI 默认投资分析模型', 'ANALYSIS', 'OPENAI_COMPATIBLE', 'https://api.openai.com/v1',
 JSON_OBJECT('baseUrl', 'https://api.openai.com/v1', 'model', 'gpt-4.1-mini', 'secretRef', 'OPENAI_API_KEY', 'timeoutSeconds', 90, 'temperature', 0.2, 'mockEnabled', true),
 JSON_OBJECT('configurationType', 'OPENAI_DEFAULT', 'protocol', 'OPENAI_COMPATIBLE', 'frontConfigurable', true, 'defaultForAutoReport', true, 'mockEnabledDefault', true), 'ACTIVE', CURRENT_TIMESTAMP(3), NULL, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3));

-- 6. 初始化产品池、投资画像和主题关系。
INSERT INTO aiw_product
(biz_id, product_no, product_code, product_name, product_type, market_code, currency, trade_status, risk_level, min_invest_amount, amount_step, quantity_step, fee_rate, listing_date, delisting_date, description, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at)
VALUES
('22000000-0000-0000-0000-000000000001', 'PROD-CN-AI-159819', '159819', '人工智能ETF', 'ETF', 'SZSE', 'CNY', 1, 4, 100.0000, 1.0000, 100.00000000, 0.00030000, '2020-09-01', NULL, '跟踪人工智能主题的场内ETF，用于AI主题样本。', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SEED', 'SEED', 0, NULL),
('22000000-0000-0000-0000-000000000002', 'PROD-CN-AI-588000', '588000', '科创50ETF', 'ETF', 'SSE', 'CNY', 1, 4, 100.0000, 1.0000, 100.00000000, 0.00030000, '2020-11-16', NULL, '科创成长代表ETF，AI与硬科技权重较高。', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SEED', 'SEED', 0, NULL),
('22000000-0000-0000-0000-000000000003', 'PROD-CN-AI-515980', '515980', '中证人工智能ETF', 'ETF', 'SSE', 'CNY', 1, 4, 100.0000, 1.0000, 100.00000000, 0.00030000, '2019-12-09', NULL, '人工智能产业链样本ETF。', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SEED', 'SEED', 0, NULL),
('22000000-0000-0000-0000-000000000004', 'PROD-CN-SEMI-512480', '512480', '半导体ETF', 'ETF', 'SSE', 'CNY', 1, 5, 100.0000, 1.0000, 100.00000000, 0.00030000, '2019-05-08', NULL, '半导体产业链高波动ETF。', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SEED', 'SEED', 0, NULL),
('22000000-0000-0000-0000-000000000005', 'PROD-CN-SEMI-159995', '159995', '芯片ETF', 'ETF', 'SZSE', 'CNY', 1, 5, 100.0000, 1.0000, 100.00000000, 0.00030000, '2020-02-10', NULL, '芯片产业主题ETF。', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SEED', 'SEED', 0, NULL),
('22000000-0000-0000-0000-000000000006', 'PROD-CN-SEMI-688981', '688981', '中芯国际', 'STOCK', 'SSE', 'CNY', 1, 5, 100.0000, 1.0000, 100.00000000, 0.00050000, '2020-07-16', NULL, '半导体制造代表性股票，仅用于高风险样本。', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SEED', 'SEED', 0, NULL),
('22000000-0000-0000-0000-000000000007', 'PROD-CN-GOLD-518880', '518880', '黄金ETF', 'ETF', 'SSE', 'CNY', 1, 3, 100.0000, 1.0000, 100.00000000, 0.00020000, '2013-07-29', NULL, '黄金资产配置代表ETF。', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SEED', 'SEED', 0, NULL),
('22000000-0000-0000-0000-000000000008', 'PROD-CN-GOLD-159934', '159934', '黄金ETF基金', 'ETF', 'SZSE', 'CNY', 1, 3, 100.0000, 1.0000, 100.00000000, 0.00020000, '2013-12-16', NULL, '深市黄金ETF样本。', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SEED', 'SEED', 0, NULL),
('22000000-0000-0000-0000-000000000009', 'PROD-WMP-STEADY-001', 'WMP-STEADY-001', '稳健优选月月开理财', 'BANK_WMP', 'BANK_WMP', 'CNY', 1, 2, 1000.0000, 100.0000, 0.01000000, 0.00000000, '2025-01-01', NULL, '低波动开放式银行理财样本，净值来源中国理财网。', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SEED', 'SEED', 0, NULL),
('22000000-0000-0000-0000-000000000010', 'PROD-WMP-CASH-001', 'WMP-CASH-001', '现金管理类理财', 'BANK_WMP', 'BANK_WMP', 'CNY', 1, 1, 1.0000, 1.0000, 0.01000000, 0.00000000, '2025-01-01', NULL, '现金管理类理财样本，用于低风险和现金替代演示。', 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SEED', 'SEED', 0, NULL);

INSERT INTO aiw_product_investment_profile
(biz_id, product_biz_id, asset_class, risk_summary, volatility_level, liquidity_level, max_drawdown, suitable_risk_level, mock_tradable, min_holding_days, trading_notes, data_quality_score, created_at, updated_at)
VALUES
(UUID(), '22000000-0000-0000-0000-000000000001', 'ETF', '主题波动较高，适合中高风险承受能力用户。', 'HIGH', 'HIGH', 0.28000000, 4, 1, 20, '场内ETF，Mock交易按最新收盘价撮合。', 0.9000, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000002', 'ETF', '科创成长波动较高，需控制仓位。', 'HIGH', 'HIGH', 0.30000000, 4, 1, 20, '场内ETF，Mock交易按最新收盘价撮合。', 0.9100, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000003', 'ETF', '人工智能产业ETF，适合作为卫星仓位。', 'HIGH', 'HIGH', 0.26000000, 4, 1, 20, '场内ETF，Mock交易按最新收盘价撮合。', 0.9000, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000004', 'ETF', '半导体行业景气波动大，不适合低风险用户。', 'HIGH', 'HIGH', 0.36000000, 5, 1, 30, '高波动主题，Mock下单需通过风险等级检查。', 0.8800, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000005', 'ETF', '芯片主题高弹性高回撤。', 'HIGH', 'HIGH', 0.34000000, 5, 1, 30, '高波动主题，Mock下单需通过风险等级检查。', 0.8800, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000006', 'STOCK', '单一股票风险集中，仅用于高风险样本。', 'HIGH', 'MEDIUM', 0.42000000, 5, 0, 30, '产品不可Mock交易，用于风控拦截演示。', 0.7600, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000007', 'GOLD', '黄金资产可降低组合相关性，仍存在价格波动。', 'MEDIUM', 'HIGH', 0.15000000, 3, 1, 10, '避险资产样本，适合组合对冲。', 0.9200, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000008', 'GOLD', '黄金资产可降低组合相关性，仍存在价格波动。', 'MEDIUM', 'HIGH', 0.15000000, 3, 1, 10, '避险资产样本，适合组合对冲。', 0.9200, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000009', 'BANK_WMP', '低波动净值型理财，适合稳健仓位。', 'LOW', 'MEDIUM', 0.02000000, 2, 1, 30, '按净值申赎，Mock交易按最新净值成交。', 0.9000, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000010', 'BANK_WMP', '现金管理类产品，流动性较好。', 'LOW', 'HIGH', 0.00500000, 1, 1, 1, '按净值申赎，Mock交易按最新净值成交。', 0.8900, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3));

INSERT INTO aiw_product_theme_relation
(biz_id, product_biz_id, relation_type, relation_code, relation_name, relation_weight, source_code, evidence, created_at, updated_at)
VALUES
(UUID(), '22000000-0000-0000-0000-000000000001', 'THEME', 'AI_CN', 'AI人工智能', 1.0000, 'SEED', '主题产品池初始化。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000002', 'THEME', 'AI_CN', 'AI人工智能', 0.7000, 'SEED', '科创成长暴露。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000003', 'THEME', 'AI_CN', 'AI人工智能', 1.0000, 'SEED', '主题产品池初始化。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000004', 'THEME', 'SEMICONDUCTOR_CN', '半导体', 1.0000, 'SEED', '主题产品池初始化。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000005', 'THEME', 'SEMICONDUCTOR_CN', '半导体', 1.0000, 'SEED', '主题产品池初始化。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000006', 'THEME', 'SEMICONDUCTOR_CN', '半导体', 0.9000, 'SEED', '半导体制造样本。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000007', 'THEME', 'GOLD_CN', '黄金', 1.0000, 'SEED', '黄金资产样本。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000008', 'THEME', 'GOLD_CN', '黄金', 1.0000, 'SEED', '黄金资产样本。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000009', 'ASSET_CLASS', 'BANK_WMP', '银行理财', 1.0000, 'CHINA_WEALTH', '中国理财网净值产品样本。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000010', 'ASSET_CLASS', 'BANK_WMP', '银行理财', 1.0000, 'CHINA_WEALTH', '中国理财网净值产品样本。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3));

-- 7. 初始化行情和理财净值，供趋势、报告和 Mock 交易使用。
INSERT INTO aiw_market_quote
(biz_id, product_biz_id, source_code, quote_interval, quote_time, open_price, high_price, low_price, close_price, previous_close_price, volume, turnover_amount, quote_status, received_at, created_at)
VALUES
(UUID(), '22000000-0000-0000-0000-000000000001', 'SZSE', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), '15:00:00'), 0.84200000, 0.85500000, 0.83600000, 0.85000000, 0.83800000, 22100000, 18785000, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000001', 'SZSE', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), '15:00:00'), 0.85100000, 0.86400000, 0.84900000, 0.86200000, 0.85000000, 23200000, 19998400, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000001', 'SZSE', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '15:00:00'), 0.86300000, 0.87900000, 0.85800000, 0.87500000, 0.86200000, 24500000, 21437500, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000001', 'SZSE', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '15:00:00'), 0.87600000, 0.89000000, 0.87200000, 0.88400000, 0.87500000, 23800000, 21039200, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000001', 'SZSE', '1D', TIMESTAMP(CURRENT_DATE, '15:00:00'), 0.88600000, 0.90200000, 0.88000000, 0.89800000, 0.88400000, 26600000, 23886800, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000002', 'SSE', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), '15:00:00'), 0.97200000, 0.98100000, 0.96500000, 0.97800000, 0.96800000, 31000000, 30318000, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000002', 'SSE', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), '15:00:00'), 0.98000000, 0.99600000, 0.97600000, 0.99200000, 0.97800000, 32500000, 32240000, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000002', 'SSE', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '15:00:00'), 0.99500000, 1.01200000, 0.99000000, 1.00600000, 0.99200000, 33100000, 33398600, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000002', 'SSE', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '15:00:00'), 1.00400000, 1.01800000, 0.99800000, 1.01100000, 1.00600000, 31800000, 32149800, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000002', 'SSE', '1D', TIMESTAMP(CURRENT_DATE, '15:00:00'), 1.01300000, 1.03000000, 1.00900000, 1.02600000, 1.01100000, 34200000, 35089200, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000003', 'SSE', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), '15:00:00'), 0.70100000, 0.71200000, 0.69800000, 0.70800000, 0.70000000, 18500000, 13098000, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000003', 'SSE', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), '15:00:00'), 0.70900000, 0.72400000, 0.70700000, 0.72000000, 0.70800000, 19400000, 13968000, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000003', 'SSE', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '15:00:00'), 0.72100000, 0.73900000, 0.71800000, 0.73500000, 0.72000000, 20700000, 15214500, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000003', 'SSE', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '15:00:00'), 0.73400000, 0.74600000, 0.72800000, 0.74100000, 0.73500000, 20200000, 14968200, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000003', 'SSE', '1D', TIMESTAMP(CURRENT_DATE, '15:00:00'), 0.74200000, 0.75800000, 0.73800000, 0.75300000, 0.74100000, 21900000, 16490700, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000004', 'SSE', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), '15:00:00'), 0.82100000, 0.83000000, 0.80600000, 0.81200000, 0.82500000, 40100000, 32561200, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000004', 'SSE', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), '15:00:00'), 0.81300000, 0.82800000, 0.80900000, 0.82500000, 0.81200000, 38900000, 32092500, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000004', 'SSE', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '15:00:00'), 0.82700000, 0.84200000, 0.82000000, 0.83600000, 0.82500000, 39700000, 33189200, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000004', 'SSE', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '15:00:00'), 0.83400000, 0.84600000, 0.81800000, 0.82100000, 0.83600000, 42000000, 34482000, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000004', 'SSE', '1D', TIMESTAMP(CURRENT_DATE, '15:00:00'), 0.82300000, 0.84100000, 0.81900000, 0.83800000, 0.82100000, 43600000, 36536800, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000005', 'SZSE', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), '15:00:00'), 0.92800000, 0.94000000, 0.91500000, 0.92000000, 0.93200000, 36500000, 33580000, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000005', 'SZSE', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), '15:00:00'), 0.92300000, 0.94400000, 0.91900000, 0.93800000, 0.92000000, 37100000, 34799800, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000005', 'SZSE', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '15:00:00'), 0.94000000, 0.95600000, 0.93400000, 0.94800000, 0.93800000, 38000000, 36024000, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000005', 'SZSE', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '15:00:00'), 0.94700000, 0.95400000, 0.93000000, 0.93600000, 0.94800000, 38600000, 36129600, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000005', 'SZSE', '1D', TIMESTAMP(CURRENT_DATE, '15:00:00'), 0.93800000, 0.96200000, 0.93600000, 0.95500000, 0.93600000, 39800000, 38009000, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000006', 'SSE', '1D', TIMESTAMP(CURRENT_DATE, '15:00:00'), 50.12000000, 51.20000000, 49.68000000, 50.88000000, 49.95000000, 11200000, 569856000, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000007', 'SSE', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), '15:00:00'), 5.28200000, 5.30200000, 5.27000000, 5.29400000, 5.28100000, 15800000, 83645200, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000007', 'SSE', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), '15:00:00'), 5.29600000, 5.31800000, 5.29000000, 5.31200000, 5.29400000, 16300000, 86585600, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000007', 'SSE', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '15:00:00'), 5.31400000, 5.34200000, 5.30500000, 5.33700000, 5.31200000, 17000000, 90729000, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000007', 'SSE', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '15:00:00'), 5.33900000, 5.36000000, 5.32800000, 5.34800000, 5.33700000, 16600000, 88776800, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000007', 'SSE', '1D', TIMESTAMP(CURRENT_DATE, '15:00:00'), 5.35000000, 5.38200000, 5.34100000, 5.37400000, 5.34800000, 17200000, 92432800, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000008', 'SZSE', '1D', TIMESTAMP(CURRENT_DATE, '15:00:00'), 5.21300000, 5.24500000, 5.20600000, 5.23800000, 5.21100000, 9600000, 50284800, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000009', 'CHINA_WEALTH', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '21:00:00'), 1.02010000, 1.02010000, 1.02010000, 1.02010000, 1.01980000, NULL, NULL, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000009', 'CHINA_WEALTH', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '21:00:00'), 1.02040000, 1.02040000, 1.02040000, 1.02040000, 1.02010000, NULL, NULL, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000009', 'CHINA_WEALTH', '1D', TIMESTAMP(CURRENT_DATE, '21:00:00'), 1.02080000, 1.02080000, 1.02080000, 1.02080000, 1.02040000, NULL, NULL, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000010', 'CHINA_WEALTH', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '21:00:00'), 1.00480000, 1.00480000, 1.00480000, 1.00480000, 1.00470000, NULL, NULL, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000010', 'CHINA_WEALTH', '1D', TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '21:00:00'), 1.00490000, 1.00490000, 1.00490000, 1.00490000, 1.00480000, NULL, NULL, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '22000000-0000-0000-0000-000000000010', 'CHINA_WEALTH', '1D', TIMESTAMP(CURRENT_DATE, '21:00:00'), 1.00500000, 1.00500000, 1.00500000, 1.00500000, 1.00490000, NULL, NULL, 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3));

-- 8. 初始化官方公告、资讯和主题证据链。
INSERT INTO aiw_news_article
(biz_id, external_id, article_type, title, summary, content, source_code, source_url, language_code, sentiment_score, publish_time, collected_at, created_at, is_deleted)
VALUES
('23000000-0000-0000-0000-000000000001', 'CSRC-SEED-001', 'REGULATORY', '证监会发布资本市场高质量发展配套政策', '政策强调提升上市公司质量、支持科技创新和长期资金入市。', '本条为本地演示种子数据，用于监管披露链路和质量门禁演示。', 'CSRC', 'https://www.csrc.gov.cn/', 'zh-CN', 0.420000, DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 8 HOUR), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
('23000000-0000-0000-0000-000000000002', 'CNINFO-SEED-001', 'ANNOUNCEMENT', '人工智能产业链公司披露算力订单进展', '公告显示算力基础设施需求保持增长，交付节奏仍需观察。', '本条为本地演示种子数据，用于公告关联和AI主题证据链演示。', 'CNINFO', 'https://www.cninfo.com.cn/', 'zh-CN', 0.560000, DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 6 HOUR), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
('23000000-0000-0000-0000-000000000003', 'SSE-SEED-001', 'ANNOUNCEMENT', '科创板半导体公司发布技术研发进展公告', '公告显示先进制程相关投入增加，但商业化仍存在周期波动。', '本条为本地演示种子数据，用于半导体主题证据链演示。', 'SSE', 'https://www.sse.com.cn/', 'zh-CN', 0.310000, DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 5 HOUR), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
('23000000-0000-0000-0000-000000000004', 'CHINA-WEALTH-SEED-001', 'WEALTH_NAV', '稳健优选月月开理财披露最新单位净值', '产品最新单位净值为1.0208，近三日净值小幅增长。', '本条为本地演示种子数据，用于银行理财净值采集与产品池upsert演示。', 'CHINA_WEALTH', 'https://www.chinawealth.com.cn/', 'zh-CN', 0.180000, DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 2 HOUR), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
('23000000-0000-0000-0000-000000000005', 'EM-SEED-001', 'NEWS', '黄金资产受避险情绪和实际利率变化影响走强', '多源市场信息显示黄金ETF获得资金关注，但单一媒体仅作为弱信号。', '本条为本地演示种子数据，用于L4资讯交叉验证提示。', 'EASTMONEY', 'https://www.eastmoney.com/', 'zh-CN', 0.350000, DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 3 HOUR), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO aiw_news_article_relation
(biz_id, article_biz_id, theme_code, theme_name, product_code, relation_type, matched_keywords, source_quality_score, relation_score, evidence, created_at)
VALUES
(UUID(), '23000000-0000-0000-0000-000000000002', 'AI_CN', 'AI人工智能', '159819', 'KEYWORD_MATCH', JSON_ARRAY('人工智能', '算力'), 0.9200, 0.8800, 'CNINFO公告命中AI和算力关键词。', CURRENT_TIMESTAMP(3)),
(UUID(), '23000000-0000-0000-0000-000000000002', 'AI_CN', 'AI人工智能', '588000', 'KEYWORD_MATCH', JSON_ARRAY('人工智能', '算力'), 0.9200, 0.7200, '科创成长产品与硬科技暴露相关。', CURRENT_TIMESTAMP(3)),
(UUID(), '23000000-0000-0000-0000-000000000003', 'SEMICONDUCTOR_CN', '半导体', '512480', 'KEYWORD_MATCH', JSON_ARRAY('半导体', '研发'), 0.9300, 0.8400, '上交所公告命中半导体研发关键词。', CURRENT_TIMESTAMP(3)),
(UUID(), '23000000-0000-0000-0000-000000000004', 'BANK_WMP', '银行理财', 'WMP-STEADY-001', 'MANUAL', JSON_ARRAY('净值', '理财'), 0.9000, 0.9500, '中国理财网净值披露映射到理财产品。', CURRENT_TIMESTAMP(3)),
(UUID(), '23000000-0000-0000-0000-000000000005', 'GOLD_CN', '黄金', '518880', 'KEYWORD_MATCH', JSON_ARRAY('黄金', '避险'), 0.7600, 0.6900, 'L4资讯仅作为黄金主题弱信号。', CURRENT_TIMESTAMP(3));

-- 9. 初始化主题快照。
INSERT INTO aiw_investment_theme_snapshot
(biz_id, task_code, snapshot_type, theme_code, theme_name, market_scope, window_minutes, sample_count, return_rate, momentum_score, heat_score, top_product_biz_id, metrics, snapshot_time, created_at)
VALUES
(UUID(), 'cn-mainland-hot-theme-return', 'RETURN', 'AI_CN', 'AI人工智能', 'CN_MAINLAND', 1440, 15, 0.03850000, NULL, NULL, '22000000-0000-0000-0000-000000000001', JSON_OBJECT('dataQualityScore', 0.9000, 'sourceCount', 3, 'sampleProducts', JSON_ARRAY('159819', '588000', '515980')), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), 'cn-mainland-market-momentum-scan', 'MOMENTUM', 'AI_CN', 'AI人工智能', 'CN_MAINLAND', 60, 9, NULL, 0.82000000, NULL, '22000000-0000-0000-0000-000000000002', JSON_OBJECT('dataQualityScore', 0.8900, 'trend', 'UP'), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), 'cn-mainland-news-heat-aggregation', 'NEWS_HEAT', 'AI_CN', 'AI人工智能', 'CN_MAINLAND', 1440, 2, NULL, NULL, 0.78000000, '22000000-0000-0000-0000-000000000001', JSON_OBJECT('dataQualityScore', 0.8800, 'sourceCodes', JSON_ARRAY('CNINFO', 'CSRC')), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), 'cn-mainland-hot-theme-return', 'RETURN', 'SEMICONDUCTOR_CN', '半导体', 'CN_MAINLAND', 1440, 11, 0.01880000, NULL, NULL, '22000000-0000-0000-0000-000000000005', JSON_OBJECT('dataQualityScore', 0.8600, 'sourceCount', 2, 'risk', 'HIGH_VOLATILITY'), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), 'cn-mainland-news-heat-aggregation', 'NEWS_HEAT', 'GOLD_CN', '黄金', 'CN_MAINLAND', 1440, 1, NULL, NULL, 0.52000000, '22000000-0000-0000-0000-000000000007', JSON_OBJECT('dataQualityScore', 0.7200, 'sourceCodes', JSON_ARRAY('EASTMONEY'), 'gate', 'WEAK_SIGNAL'), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3));

-- 10. 初始化 Prompt、投资报告、Mock 交易、回测、风控和反馈闭环。
INSERT INTO aiw_ai_prompt_template
(biz_id, prompt_code, prompt_version, scenario, template_name, template_content, status, description, created_at, updated_at, created_by, updated_by)
VALUES
('24000000-0000-0000-0000-000000000001', 'INVESTMENT_PLAN_FROM_REPORT', 'v1', 'INVESTMENT_PLAN', '基于可信报告生成投资方案', '请基于投资报告 ${reportSummary}、数据质量 ${dataQualityGate}、用户风险等级 ${userRiskLevel} 和可Mock产品 ${mockTradableProducts}，输出结构化投资方案。低质量数据必须输出数据缺口，不得输出确定性收益承诺。', 'ACTIVE', '演示用默认Prompt，前端可复制为新版本。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SEED', 'SEED');

INSERT INTO aiw_ai_prompt_variable
(biz_id, prompt_biz_id, variable_name, source_path, required, description, created_at)
VALUES
(UUID(), '24000000-0000-0000-0000-000000000001', 'reportSummary', 'report.investmentSummary', 1, '投资报告摘要和关键证据。', CURRENT_TIMESTAMP(3)),
(UUID(), '24000000-0000-0000-0000-000000000001', 'dataQualityGate', 'report.dataQualityGate', 1, '数据质量门禁结果。', CURRENT_TIMESTAMP(3)),
(UUID(), '24000000-0000-0000-0000-000000000001', 'userRiskLevel', 'user.riskProfile.riskLevel', 1, '用户风险承受等级。', CURRENT_TIMESTAMP(3)),
(UUID(), '24000000-0000-0000-0000-000000000001', 'mockTradableProducts', 'productPool.mockTradableProducts', 1, '可Mock交易产品池。', CURRENT_TIMESTAMP(3));

INSERT INTO aiw_ai_prompt_output_schema
(biz_id, prompt_biz_id, schema_version, schema_json, created_at)
VALUES
('24000000-0000-0000-0001-000000000001', '24000000-0000-0000-0000-000000000001', 'v1',
 JSON_OBJECT('type', 'object', 'required', JSON_ARRAY('planSummary', 'allocations', 'riskWarnings', 'mockTradePlan'), 'properties', JSON_OBJECT('planSummary', JSON_OBJECT('type', 'string'), 'allocations', JSON_OBJECT('type', 'array'), 'riskWarnings', JSON_OBJECT('type', 'array'), 'mockTradePlan', JSON_OBJECT('type', 'array'))), CURRENT_TIMESTAMP(3));

INSERT INTO aiw_investment_analysis_report
(biz_id, request_id, provider_code, model_code, market_scope, theme_code, theme_name, status, investment_summary, trend, investment_plan, simulated_return, chart_payload, prompt_snapshot, failure_reason, generated_at, created_at, confidence_level, data_quality_score, data_quality_gate)
VALUES
('25000000-0000-0000-0000-000000000001', '25000000-0000-0000-0001-000000000001', 'OPENAI_COMPATIBLE', 'openai-compatible-analysis', 'CN_MAINLAND', 'AI_CN', 'AI人工智能', 'SUCCEEDED',
 JSON_OBJECT('summary', 'AI主题由L1公告、交易所行情和产品画像共同支撑，短期趋势偏强，但建议作为卫星仓位。', 'evidenceCount', 7, 'sourceLevels', JSON_ARRAY('L1', 'L4')),
 JSON_OBJECT('direction', 'UP', 'strength', 0.7800, 'explain', '行情动量、公告热度和主题收益同时改善。'),
 JSON_OBJECT('action', 'WATCH_AND_MOCK_BUY', 'allocations', JSON_ARRAY(JSON_OBJECT('productCode', '159819', 'targetWeight', 0.1800), JSON_OBJECT('productCode', '588000', 'targetWeight', 0.1200), JSON_OBJECT('productCode', '518880', 'targetWeight', 0.0800), JSON_OBJECT('productCode', 'WMP-CASH-001', 'targetWeight', 0.2000)), 'riskNotice', '不构成投资建议，仅用于Mock验证。'),
 JSON_OBJECT('initialCapital', 100000, 'mockReturnRate', 0.0128, 'maxDrawdown', 0.0062, 'benchmarkReturnRate', 0.0076),
 JSON_OBJECT('series', JSON_ARRAY(JSON_OBJECT('date', DATE_FORMAT(DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), '%Y-%m-%d'), 'value', 100000), JSON_OBJECT('date', DATE_FORMAT(DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '%Y-%m-%d'), 'value', 100720), JSON_OBJECT('date', DATE_FORMAT(CURRENT_DATE, '%Y-%m-%d'), 'value', 101280)), 'bars', JSON_ARRAY(JSON_OBJECT('theme', 'AI人工智能', 'score', 0.82), JSON_OBJECT('theme', '半导体', 'score', 0.62), JSON_OBJECT('theme', '黄金', 'score', 0.52))),
 JSON_OBJECT('promptBizId', '24000000-0000-0000-0000-000000000001', 'promptCode', 'INVESTMENT_PLAN_FROM_REPORT', 'promptVersion', 'v1', 'modelCode', 'openai-compatible-analysis', 'mockEnabled', true),
 NULL, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'HIGH_CONFIDENCE', 0.8800,
 JSON_OBJECT('passed', true, 'coverage', 0.8300, 'sourceCount', 4, 'fallbackRatio', 0.0000, 'warnings', JSON_ARRAY('L4资讯仅作为交叉验证，不单独驱动配置建议。'))),
('25000000-0000-0000-0000-000000000002', '25000000-0000-0000-0001-000000000002', 'LOCAL_RULE', 'local-rule-analysis', 'CN_MAINLAND', 'GOLD_CN', '黄金', 'SUCCEEDED',
 JSON_OBJECT('summary', '黄金主题具备避险价值，但当前资讯来源偏少，报告只给出观察建议。', 'evidenceCount', 3, 'sourceLevels', JSON_ARRAY('L1', 'L4')),
 JSON_OBJECT('direction', 'NEUTRAL_UP', 'strength', 0.5200, 'explain', '行情温和走强，新闻热度为弱信号。'),
 JSON_OBJECT('action', 'WATCH', 'allocations', JSON_ARRAY(JSON_OBJECT('productCode', '518880', 'targetWeight', 0.0800)), 'riskNotice', '数据源数量不足，不建议扩大仓位。'),
 JSON_OBJECT('initialCapital', 100000, 'mockReturnRate', 0.0036, 'maxDrawdown', 0.0018, 'benchmarkReturnRate', 0.0025),
 JSON_OBJECT('series', JSON_ARRAY(JSON_OBJECT('date', DATE_FORMAT(CURRENT_DATE, '%Y-%m-%d'), 'value', 100360)), 'bars', JSON_ARRAY(JSON_OBJECT('theme', '黄金', 'score', 0.52))),
 JSON_OBJECT('promptBizId', '24000000-0000-0000-0000-000000000001', 'promptCode', 'INVESTMENT_PLAN_FROM_REPORT', 'promptVersion', 'v1', 'modelCode', 'local-rule-analysis'),
 NULL, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'MEDIUM_CONFIDENCE', 0.7200,
 JSON_OBJECT('passed', true, 'coverage', 0.6800, 'sourceCount', 2, 'fallbackRatio', 0.0000, 'warnings', JSON_ARRAY('新闻热度为弱信号。')));

INSERT INTO aiw_portfolio
(biz_id, portfolio_no, owner_user_biz_id, portfolio_name, portfolio_type, base_currency, status, version, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at)
VALUES
('26000000-0000-0000-0000-000000000001', 'MOCK-AI-001', '21000000-0000-0000-0000-000000000002', 'AI主题稳健Mock组合', 'SIMULATION', 'CNY', 1, 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SEED', 'SEED', 0, NULL);

INSERT INTO aiw_portfolio_valuation
(biz_id, portfolio_biz_id, valuation_time, base_currency, total_asset, cash_balance, position_value, total_cost, unrealized_profit, realized_profit, total_return_rate, source_code, created_at)
VALUES
('26000000-0000-0000-0001-000000000001', '26000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP(3), 'CNY', 101280.00000000, 61240.00000000, 40040.00000000, 39560.00000000, 480.00000000, 800.00000000, 0.0128000000, 'SIMULATOR', CURRENT_TIMESTAMP(3));

INSERT INTO aiw_position
(biz_id, portfolio_biz_id, product_biz_id, position_side, quantity, available_quantity, average_cost, cost_amount, realized_profit, last_trade_at, version, created_at, updated_at, is_deleted)
VALUES
('26000000-0000-0000-0002-000000000001', '26000000-0000-0000-0000-000000000001', '22000000-0000-0000-0000-000000000001', 'LONG', 20000.00000000, 20000.00000000, 0.87400000, 17480.00000000, 0.00000000, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
('26000000-0000-0000-0002-000000000002', '26000000-0000-0000-0000-000000000001', '22000000-0000-0000-0000-000000000007', 'LONG', 4200.00000000, 4200.00000000, 5.31200000, 22310.40000000, 800.00000000, CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO aiw_order
(biz_id, order_no, idempotency_key, user_biz_id, portfolio_biz_id, product_biz_id, channel_code, order_side, order_type, currency, requested_price, requested_quantity, requested_amount, executed_quantity, executed_amount, fee_amount, status, external_order_id, reject_code, reject_message, submitted_at, completed_at, version, created_at, updated_at, created_by, is_deleted)
VALUES
('26000000-0000-0000-0003-000000000001', 'MOCK-ORDER-0001', 'seed-buy-ai-001', '21000000-0000-0000-0000-000000000002', '26000000-0000-0000-0000-000000000001', '22000000-0000-0000-0000-000000000001', 'SIMULATOR', 'BUY', 'MARKET', 'CNY', 0.89800000, 20000.00000000, 17960.00000000, 20000.00000000, 17960.00000000, 5.38800000, 'FILLED', 'SIM-EXEC-0001', NULL, NULL, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 1, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SEED', 0),
('26000000-0000-0000-0003-000000000002', 'MOCK-ORDER-0002', 'seed-reject-stock-001', '21000000-0000-0000-0000-000000000002', '26000000-0000-0000-0000-000000000001', '22000000-0000-0000-0000-000000000006', 'SIMULATOR', 'BUY', 'MARKET', 'CNY', 50.88000000, 100.00000000, 5088.00000000, 0.00000000, 0.00000000, 0.00000000, 'REJECTED', NULL, 'PRODUCT_NOT_MOCK_TRADABLE', '产品画像标记不可Mock交易', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'SEED', 0);

INSERT INTO aiw_trade_execution
(biz_id, execution_no, order_biz_id, user_biz_id, portfolio_biz_id, product_biz_id, channel_code, external_execution_id, execution_price, execution_quantity, execution_amount, fee_amount, executed_at, created_at)
VALUES
('26000000-0000-0000-0004-000000000001', 'MOCK-EXEC-0001', '26000000-0000-0000-0003-000000000001', '21000000-0000-0000-0000-000000000002', '26000000-0000-0000-0000-000000000001', '22000000-0000-0000-0000-000000000001', 'SIMULATOR', 'SIM-EXEC-0001', 0.89800000, 20000.00000000, 17960.00000000, 5.38800000, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3));

INSERT INTO aiw_order_event
(biz_id, order_biz_id, event_type, from_status, to_status, event_source, operator_biz_id, event_payload, occurred_at, created_at)
VALUES
(UUID(), '26000000-0000-0000-0003-000000000001', 'CREATED', NULL, 'CREATED', 'INTERNAL', 'SEED', JSON_OBJECT('reportBizId', '25000000-0000-0000-0000-000000000001'), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '26000000-0000-0000-0003-000000000001', 'FILLED', 'SUBMITTED', 'FILLED', 'CHANNEL', 'SIMULATOR', JSON_OBJECT('executionNo', 'MOCK-EXEC-0001'), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), '26000000-0000-0000-0003-000000000002', 'REJECTED', 'SUBMITTED', 'REJECTED', 'INTERNAL', 'RISK_ENGINE', JSON_OBJECT('reasonCode', 'PRODUCT_NOT_MOCK_TRADABLE'), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3));

INSERT INTO aiw_risk_rule
(biz_id, rule_code, rule_version, rule_name, rule_type, risk_level, priority, rule_config, status, effective_at, expired_at, created_by_biz_id, updated_by_biz_id, created_at, updated_at)
VALUES
('27000000-0000-0000-0000-000000000001', 'DATA_QUALITY_GATE', 1, '数据质量门禁', 'ORDER', 'HIGH', 10, JSON_OBJECT('minQualityScore', 0.60, 'minSourceCount', 2), 'ENABLED', CURRENT_TIMESTAMP(3), NULL, 'SEED', 'SEED', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('27000000-0000-0000-0000-000000000002', 'RISK_LEVEL_MATCH', 1, '用户与产品风险匹配', 'ORDER', 'HIGH', 20, JSON_OBJECT('rejectWhenProductRiskAboveUser', true), 'ENABLED', CURRENT_TIMESTAMP(3), NULL, 'SEED', 'SEED', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('27000000-0000-0000-0000-000000000003', 'PRODUCT_MOCK_TRADABLE', 1, '产品Mock交易开关', 'ORDER', 'HIGH', 30, JSON_OBJECT('requireMockTradable', true), 'ENABLED', CURRENT_TIMESTAMP(3), NULL, 'SEED', 'SEED', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('27000000-0000-0000-0000-000000000004', 'INSUFFICIENT_CASH', 1, '现金充足性检查', 'ORDER', 'MEDIUM', 40, JSON_OBJECT('includeFee', true), 'ENABLED', CURRENT_TIMESTAMP(3), NULL, 'SEED', 'SEED', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3));

INSERT INTO aiw_risk_check
(biz_id, trace_id, business_type, business_biz_id, user_biz_id, rule_code, rule_version, check_result, risk_level, score, reason_code, detail, checked_at, created_at)
VALUES
(UUID(), 'seed-trace-report-ai', 'REPORT', '25000000-0000-0000-0000-000000000001', '21000000-0000-0000-0000-000000000002', 'DATA_QUALITY_GATE', 1, 'PASS', 'LOW', 0.8800, 'QUALITY_PASS', JSON_OBJECT('dataQualityScore', 0.88, 'sourceCount', 4), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), 'seed-trace-order-ai', 'ORDER', '26000000-0000-0000-0003-000000000001', '21000000-0000-0000-0000-000000000002', 'RISK_LEVEL_MATCH', 1, 'PASS', 'LOW', 0.7000, 'RISK_MATCHED', JSON_OBJECT('userRiskLevel', 3, 'productRiskLevel', 4, 'allowSatellitePosition', true), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), 'seed-trace-order-reject', 'ORDER', '26000000-0000-0000-0003-000000000002', '21000000-0000-0000-0000-000000000002', 'PRODUCT_MOCK_TRADABLE', 1, 'REJECT', 'HIGH', 0.9500, 'PRODUCT_NOT_MOCK_TRADABLE', JSON_OBJECT('productCode', '688981', 'mockTradable', false), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
(UUID(), 'seed-trace-order-cash', 'ORDER', '26000000-0000-0000-0003-000000000002', '21000000-0000-0000-0000-000000000002', 'INSUFFICIENT_CASH', 1, 'REVIEW', 'MEDIUM', 0.6200, 'CASH_BUFFER_LOW', JSON_OBJECT('cashBalance', 61240, 'requestedAmount', 5088, 'postTradeCashBufferRate', 0.5615), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3));

INSERT INTO aiw_backtest_result
(biz_id, owner_user_biz_id, strategy_code, strategy_version, start_date, end_date, initial_capital, benchmark_code, parameters, metrics, result_uri, status, failure_reason, started_at, completed_at, created_at, updated_at)
VALUES
('28000000-0000-0000-0000-000000000001', '21000000-0000-0000-0000-000000000002', 'AI_THEME_SATELLITE_MOCK', 'v1', DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY), CURRENT_DATE, 100000.00000000, '000300',
 JSON_OBJECT('reportBizId', '25000000-0000-0000-0000-000000000001', 'promptCode', 'INVESTMENT_PLAN_FROM_REPORT', 'rebalance', 'weekly'),
 JSON_OBJECT('totalReturnRate', 0.0128, 'maxDrawdown', 0.0062, 'volatility', 0.1180, 'winRate', 0.5800, 'sampleDays', 30),
 NULL, 'SUCCEEDED', NULL, DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL 1 MINUTE), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3));

INSERT INTO aiw_investment_feedback
(biz_id, user_biz_id, target_type, target_biz_id, report_biz_id, prompt_biz_id, prompt_code, prompt_version, backtest_biz_id, feedback_action, reason_code, comment_text, metadata, created_at)
VALUES
('28000000-0000-0000-0001-000000000001', '21000000-0000-0000-0000-000000000002', 'REPORT', '25000000-0000-0000-0000-000000000001', '25000000-0000-0000-0000-000000000001', '24000000-0000-0000-0000-000000000001', 'INVESTMENT_PLAN_FROM_REPORT', 'v1', '28000000-0000-0000-0000-000000000001', 'WATCH', 'NEED_MORE_DAYS', '报告证据链完整，但希望观察更多交易日后再采纳。', JSON_OBJECT('frontScene', 'report-detail', 'decision', 'watch'), CURRENT_TIMESTAMP(3)),
('28000000-0000-0000-0001-000000000002', '21000000-0000-0000-0000-000000000002', 'MOCK_ORDER', '26000000-0000-0000-0003-000000000002', '25000000-0000-0000-0000-000000000001', '24000000-0000-0000-0000-000000000001', 'INVESTMENT_PLAN_FROM_REPORT', 'v1', NULL, 'REJECT', 'PRODUCT_NOT_MOCK_TRADABLE', '单一股票不进入Mock交易，拒绝正确。', JSON_OBJECT('frontScene', 'risk-audit', 'decision', 'reject-confirmed'), CURRENT_TIMESTAMP(3));

INSERT INTO aiw_ai_prompt_evaluation
(biz_id, prompt_biz_id, prompt_code, prompt_version, scenario, backtest_biz_id, feedback_biz_id, score, score_detail, review_status, evaluator_type, evaluator_biz_id, evaluated_at, created_at)
VALUES
('28000000-0000-0000-0002-000000000001', '24000000-0000-0000-0000-000000000001', 'INVESTMENT_PLAN_FROM_REPORT', 'v1', 'INVESTMENT_PLAN', '28000000-0000-0000-0000-000000000001', '28000000-0000-0000-0001-000000000001', 0.8200, JSON_OBJECT('backtestReturn', 0.0128, 'maxDrawdown', 0.0062, 'feedbackAction', 'WATCH', 'qualityGatePass', true), 'PENDING', 'SYSTEM', NULL, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3));

INSERT INTO aiw_audit_log
(biz_id, trace_id, operator_type, operator_biz_id, action_code, resource_type, resource_biz_id, request_method, request_path, result_code, result_status, client_ip, user_agent, detail, occurred_at, created_at)
VALUES
(UUID(), 'seed-reset', 'JOB', NULL, 'LOCAL_BUSINESS_DATA_RESET_SEED', 'DATABASE', NULL, 'SQL', 'scripts/local/reset-and-seed-investment-demo.sql', 'SUCCESS', 'SUCCESS', NULL, 'LOCAL_SQL', JSON_OBJECT('businessDataReset', true, 'seedVersion', '2026-06-24'), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3));

COMMIT;

-- 11. 快速校验结果。
SELECT 'aiw_user' AS table_name, COUNT(*) AS row_count FROM aiw_user
UNION ALL SELECT 'aiw_product', COUNT(*) FROM aiw_product
UNION ALL SELECT 'aiw_market_quote', COUNT(*) FROM aiw_market_quote
UNION ALL SELECT 'aiw_news_article', COUNT(*) FROM aiw_news_article
UNION ALL SELECT 'aiw_investment_task_definition', COUNT(*) FROM aiw_investment_task_definition
UNION ALL SELECT 'aiw_investment_analysis_report', COUNT(*) FROM aiw_investment_analysis_report
UNION ALL SELECT 'aiw_portfolio', COUNT(*) FROM aiw_portfolio
UNION ALL SELECT 'aiw_risk_check', COUNT(*) FROM aiw_risk_check
UNION ALL SELECT 'aiw_backtest_result', COUNT(*) FROM aiw_backtest_result
UNION ALL SELECT 'aiw_investment_feedback', COUNT(*) FROM aiw_investment_feedback;
