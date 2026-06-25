-- ============================================================================
-- DZCOM 本地/开发环境业务数据清空与真实采集启动基线
--
-- 用途：
--   1. 清空投资平台业务结果数据，保留/重建本地联调用户。
--   2. 初始化真实采集所需的数据源、可配置定时任务、模型和 Prompt 基线。
--   3. 不注入任何伪造产品、行情、资讯、报告、Mock 交易、回测或反馈。
--
-- 安全边界：
--   1. 仅允许在数据库名为 dz_database 的本地/开发库执行。
--   2. 禁止作为 Flyway 迁移自动执行。
--   3. 执行后业务数据应由定时任务和真实端点逐步写入。
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

-- 1. 清空业务结果和采集产物。保留 Flyway、角色权限表结构，后面重建联调用户和系统配置。
DELETE FROM aiw_ai_prompt_evaluation;
DELETE FROM aiw_investment_feedback;
DELETE FROM aiw_backtest_result;
DELETE FROM aiw_ai_recommendation;
DELETE FROM aiw_ai_signal;
DELETE FROM aiw_investment_analysis_report;
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
DELETE FROM aiw_closed_loop_step;
DELETE FROM aiw_closed_loop_run;
DELETE FROM aiw_market_quote;
DELETE FROM aiw_product_theme_relation;
DELETE FROM aiw_product_investment_profile;
DELETE FROM aiw_product_attribute;
DELETE FROM aiw_product;
DELETE FROM aiw_data_quality_snapshot;

-- Prompt 模板属于可配置资产，先清空后重建一份系统基线，不作为业务结果。
DELETE FROM aiw_ai_prompt_output_schema;
DELETE FROM aiw_ai_prompt_variable;
DELETE FROM aiw_ai_prompt_template;

-- 数据源、任务、模型是系统配置，执行脚本时以最新真实采集基线重建。
DELETE FROM aiw_data_source_health;
DELETE FROM aiw_data_source;
DELETE FROM aiw_investment_task_definition;
DELETE FROM aiw_ai_model;

-- 本地联调账号允许 Mock，但需要干净重建，避免残留旧偏好和会话状态影响验收。
DELETE FROM aiw_user_preference;
DELETE FROM aiw_user_role;
DELETE FROM aiw_user_risk_profile;
DELETE FROM aiw_user_profile;
DELETE FROM aiw_user_credential;
DELETE FROM aiw_user_identity;
DELETE FROM aiw_user;

-- 2. 初始化本地联调用户。默认密码：Demo@123456。
INSERT INTO aiw_user
(biz_id, user_no, status, version, registered_at, last_login_at, created_at, updated_at, created_by, updated_by, is_deleted, deleted_at)
VALUES
('21000000-0000-0000-0000-000000000001', 'U-DEMO-ADMIN', 1, 0, CURRENT_TIMESTAMP(3), NULL, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'REAL_INGESTION_RESET', 'REAL_INGESTION_RESET', 0, NULL),
('21000000-0000-0000-0000-000000000002', 'U-DEMO-INVESTOR', 1, 0, CURRENT_TIMESTAMP(3), NULL, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'REAL_INGESTION_RESET', 'REAL_INGESTION_RESET', 0, NULL);

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
('21000000-0000-0000-0004-000000000001', '21000000-0000-0000-0000-000000000001', 1, 5, 'local-reset-v1', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), JSON_OBJECT('purpose', 'admin-test'), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
('21000000-0000-0000-0004-000000000002', '21000000-0000-0000-0000-000000000002', 1, 3, 'local-reset-v1', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), JSON_OBJECT('investmentHorizon', '6-12M', 'lossTolerance', 'MEDIUM'), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

INSERT INTO aiw_user_role
(biz_id, user_biz_id, role_code, scope_code, effective_from, effective_to, created_at, created_by, is_deleted)
VALUES
('21000000-0000-0000-0005-000000000001', '21000000-0000-0000-0000-000000000001', 'ADMIN', 'GLOBAL', CURRENT_TIMESTAMP(3), NULL, CURRENT_TIMESTAMP(3), 'REAL_INGESTION_RESET', 0),
('21000000-0000-0000-0005-000000000002', '21000000-0000-0000-0000-000000000002', 'USER', 'GLOBAL', CURRENT_TIMESTAMP(3), NULL, CURRENT_TIMESTAMP(3), 'REAL_INGESTION_RESET', 0);

INSERT INTO aiw_user_preference
(biz_id, user_biz_id, preference_key, value_type, preference_value, created_at, updated_at, is_deleted)
VALUES
('21000000-0000-0000-0006-000000000001', '21000000-0000-0000-0000-000000000002', 'investment.watchThemes', 'JSON', JSON_ARRAY('AI_CN', 'SEMICONDUCTOR_CN', 'GOLD_CN'), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0),
('21000000-0000-0000-0006-000000000002', '21000000-0000-0000-0000-000000000002', 'investment.riskNoticeConfirmed', 'BOOLEAN', JSON_EXTRACT('true', '$'), CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 0);

-- 3. 初始化高质量数据源注册。健康状态明确标记为等待真实采集，不伪装成功。
INSERT INTO aiw_data_source
(biz_id, source_code, source_name, source_type, trust_level, base_url, enabled, fetch_frequency, owner, description, created_at, updated_at, created_by, updated_by)
VALUES
('17000000-0000-0000-0000-000000000001', 'CSRC', '中国证监会', 'REGULATORY', 'L1', 'https://www.csrc.gov.cn', 1, '0 */30 * * * *', 'SYSTEM', '监管政策、处罚、市场制度和官方公告，高可信来源；端点由任务参数配置。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'REAL_INGESTION_RESET', 'REAL_INGESTION_RESET'),
('17000000-0000-0000-0000-000000000002', 'SSE', '上海证券交易所', 'ANNOUNCEMENT', 'L1', 'https://www.sse.com.cn', 1, '0 */20 * * * *', 'SYSTEM', '上交所公告、产品披露和交易所公开信息。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'REAL_INGESTION_RESET', 'REAL_INGESTION_RESET'),
('17000000-0000-0000-0000-000000000003', 'SZSE', '深圳证券交易所', 'ANNOUNCEMENT', 'L1', 'https://www.szse.cn', 1, '0 */20 * * * *', 'SYSTEM', '深交所公告、产品披露和交易所公开信息。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'REAL_INGESTION_RESET', 'REAL_INGESTION_RESET'),
('17000000-0000-0000-0000-000000000004', 'CNINFO', '巨潮资讯', 'ANNOUNCEMENT', 'L1', 'https://www.cninfo.com.cn', 1, '0 */15 * * * *', 'SYSTEM', '上市公司公告和披露文件，适合补充公告与产品事件。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'REAL_INGESTION_RESET', 'REAL_INGESTION_RESET'),
('17000000-0000-0000-0000-000000000005', 'CHINA_WEALTH', '中国理财网', 'MARKET', 'L2', 'https://www.chinawealth.com.cn', 1, '0 0 */2 * * *', 'SYSTEM', '银行理财产品、净值和产品公开信息；需配置真实结构化端点。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'REAL_INGESTION_RESET', 'REAL_INGESTION_RESET'),
('17000000-0000-0000-0000-000000000006', 'EASTMONEY', '东方财富', 'MARKET', 'L4', 'https://www.eastmoney.com', 1, '0 */10 * * * *', 'SYSTEM', '行情、资讯和产品补充来源，需要与 L1/L2 数据交叉验证。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'REAL_INGESTION_RESET', 'REAL_INGESTION_RESET'),
('17000000-0000-0000-0000-000000000007', 'WIND', 'Wind 金融终端', 'MARKET', 'L3', 'vendor://wind', 0, '按供应商授权配置', 'SYSTEM', '专业行情、研报和资金流供应商，占位数据源；启用前需要配置授权。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'REAL_INGESTION_RESET', 'REAL_INGESTION_RESET'),
('17000000-0000-0000-0000-000000000008', 'CHOICE', 'Choice 金融终端', 'MARKET', 'L3', 'vendor://choice', 0, '按供应商授权配置', 'SYSTEM', '专业行情和产品数据供应商，占位数据源；启用前需要配置授权。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'REAL_INGESTION_RESET', 'REAL_INGESTION_RESET');

INSERT INTO aiw_data_source_health
(biz_id, source_code, last_success_at, last_failure_at, success_rate, avg_latency_ms, failure_reason, sample_count, updated_at)
VALUES
('17000000-0000-0000-0000-000000000101', 'CSRC', NULL, NULL, 0, NULL, '等待首次真实采集', 0, CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000102', 'SSE', NULL, NULL, 0, NULL, '等待首次真实采集', 0, CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000103', 'SZSE', NULL, NULL, 0, NULL, '等待首次真实采集', 0, CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000104', 'CNINFO', NULL, NULL, 0, NULL, '等待首次真实采集', 0, CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000105', 'CHINA_WEALTH', NULL, NULL, 0, NULL, '等待首次真实采集', 0, CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000106', 'EASTMONEY', NULL, NULL, 0, NULL, '等待首次真实采集', 0, CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000107', 'WIND', NULL, NULL, 0, NULL, '供应商授权未启用', 0, CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000108', 'CHOICE', NULL, NULL, 0, NULL, '供应商授权未启用', 0, CURRENT_TIMESTAMP(3));

-- 4. 初始化真实采集和自动闭环任务。端点为空时任务会记录缺口，不写假数据。
INSERT INTO aiw_investment_task_definition
(biz_id, task_code, task_type, cron, zone, enabled, parameters, description, created_at, updated_at)
VALUES
('17000000-0000-0000-0000-000000000201', 'l1-regulatory-disclosure-collection', 'REGULATORY_DISCLOSURE_COLLECTION', '0 */30 * * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT('endpoints', '', 'responseFormat', 'JSON', 'itemsPath', '', 'externalIdPath', 'id', 'titlePath', 'title', 'summaryPath', 'summary', 'contentPath', 'content', 'urlPath', 'url', 'publishTimePath', 'publishTime', 'extraFieldPaths', '', 'sourceCode', 'CSRC', 'articleType', 'REGULATORY', 'languageCode', 'zh-CN', 'maxItems', '80', 'timeoutSeconds', '20', 'freshnessHours', '72'),
 'L1监管披露专用采集任务。端点和字段路径由前端配置；无有效数据时只记录健康失败，不写兜底数据。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000202', 'l1-exchange-announcement-collection', 'EXCHANGE_ANNOUNCEMENT_COLLECTION', '0 */20 * * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT('endpoints', '', 'responseFormat', 'JSON', 'itemsPath', '', 'externalIdPath', 'id', 'titlePath', 'title', 'summaryPath', 'summary', 'contentPath', 'content', 'urlPath', 'url', 'publishTimePath', 'publishTime', 'extraFieldPaths', '', 'sourceCode', 'CNINFO', 'articleType', 'ANNOUNCEMENT', 'languageCode', 'zh-CN', 'maxItems', '100', 'timeoutSeconds', '20', 'freshnessHours', '72'),
 'L1交易所和巨潮公告专用采集任务。端点和字段路径由前端配置；无有效数据时只记录健康失败。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000203', 'l2-wealth-product-nav-refresh', 'WEALTH_PRODUCT_NAV_REFRESH', '0 0 */2 * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT('endpoints', '', 'responseFormat', 'JSON', 'itemsPath', '', 'externalIdPath', 'id', 'titlePath', 'productName', 'summaryPath', 'summary', 'contentPath', 'content', 'urlPath', 'url', 'publishTimePath', 'publishTime', 'extraFieldPaths', 'productCode=productCode;productName=productName;nav=nav;previousNav=previousNav;assetSize=assetSize;riskLevel=riskLevel', 'sourceCode', 'CHINA_WEALTH', 'articleType', 'WEALTH_NAV', 'languageCode', 'zh-CN', 'maxItems', '100', 'timeoutSeconds', '20', 'freshnessHours', '168', 'productMarketCode', 'BANK_WMP', 'productCurrency', 'CNY', 'quoteInterval', '1D', 'defaultRiskLevel', '2'),
 'L2银行理财产品和净值专用采集任务。真实端点返回后 upsert 产品池并写入净值行情表。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000204', 'cn-mainland-market-momentum-scan', 'MARKET_MOMENTUM_SCAN', '0 */5 * * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT('windowMinutes', '60', 'marketScope', 'CN_MAINLAND', 'themes', 'AI人工智能=159819,588000,515980;半导体=512480,159995,688981;黄金=518880,159934', 'themeMarketScopes', 'AI人工智能=CN_MAINLAND;半导体=CN_MAINLAND;黄金=CN_MAINLAND'),
 '中国大陆核心主题动量扫描。依赖已采集行情；无行情时产出空样本快照或缺口状态。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000205', 'cn-mainland-hot-theme-return', 'HOT_THEME_RETURN', '30 */10 * * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT('windowMinutes', '1440', 'marketScope', 'CN_MAINLAND', 'themes', 'AI人工智能=159819,588000,515980;半导体=512480,159995,688981;黄金=518880,159934', 'themeMarketScopes', 'AI人工智能=CN_MAINLAND;半导体=CN_MAINLAND;黄金=CN_MAINLAND'),
 '中国大陆核心主题日内/日级收益快照。只使用已采集行情。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000206', 'cn-mainland-news-heat-aggregation', 'NEWS_HEAT_AGGREGATION', '45 */10 * * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT('windowMinutes', '1440', 'marketScope', 'CN_MAINLAND', 'themes', 'AI人工智能=AI,人工智能,算力,大模型;半导体=半导体,芯片,集成电路,晶圆;黄金=黄金,金价,贵金属,避险', 'themeProducts', 'AI人工智能=159819,588000,515980;半导体=512480,159995,688981;黄金=518880,159934', 'themeMarketScopes', 'AI人工智能=CN_MAINLAND;半导体=CN_MAINLAND;黄金=CN_MAINLAND'),
 '高质量资讯入库后的主题热度聚合，生成资讯-主题-产品证据链。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000207', 'auto-openai-investment-report-generation', 'AUTO_INVESTMENT_REPORT_GENERATION', '0 5 */1 * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT('providerCode', 'OPENAI_COMPATIBLE', 'modelCode', 'openai-compatible-analysis', 'marketScope', 'CN_MAINLAND', 'lookbackDays', '30', 'initialCapital', '100000', 'themes', 'AI人工智能=159819,588000,515980;半导体=512480,159995,688981;黄金=518880,159934'),
 '自动投资报告生成任务。默认 OpenAI 兼容模型，低质量输入只能生成缺口/风险报告。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000208', 'auto-prompt-governance', 'AUTO_PROMPT_GOVERNANCE', '0 20 */2 * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT('promptCode', 'investment-plan-from-report', 'promptVersion', 'auto-v1', 'scenario', 'INVESTMENT_PLAN', 'reportSampleSize', '20'),
 '自动 Prompt 治理任务。维护报告转方案 Prompt 基线，并基于真实报告和反馈形成评估记录。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('17000000-0000-0000-0000-000000000209', 'auto-investment-closed-loop-orchestration', 'AUTO_INVESTMENT_CLOSED_LOOP_ORCHESTRATION', '0 40 */2 * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT(
   'automationLevel', 'FULL_MOCK',
   'marketScope', 'CN_MAINLAND',
   'mockUserBizId', '21000000-0000-0000-0000-000000000002',
   'mockPortfolioName', '全自动闭环模拟组合',
   'initialCash', '100000',
   'minQualityScore', '0.45',
   'allowAutoMockTrade', 'true',
   'allowPromptCandidate', 'true',
   'allowModelCandidate', 'true',
   'allowAutoPromptActivation', 'false',
   'allowAutoModelActivation', 'false',
   'allowRealTrade', 'false',
   'dataTaskCodes', 'l1-regulatory-disclosure-collection,l1-exchange-announcement-collection,l2-wealth-product-nav-refresh,cn-mainland-market-momentum-scan,cn-mainland-hot-theme-return,cn-mainland-news-heat-aggregation',
   'reportTaskCode', 'auto-openai-investment-report-generation',
   'promptTaskCode', 'auto-prompt-governance',
   'modelCode', 'openai-compatible-analysis',
   'providerCode', 'OPENAI_COMPATIBLE',
   'lookbackDays', '30',
   'themeCodes', '',
   'maxReportsForMock', '1'
 ),
 '自动投资闭环总编排任务。自动采集、报告、Prompt候选、模型候选、Mock交易、回测和反馈；默认不自动启用新Prompt/模型、不触发真实交易。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3));

-- 5. 初始化模型配置。默认 OpenAI 兼容模型仍为 mockEnabled=true，由前端配置真实 Key 后再关闭。
INSERT INTO aiw_ai_model
(biz_id, model_code, model_version, model_name, model_type, provider, artifact_uri, model_config, metrics, status, activated_at, retired_at, created_at, updated_at)
VALUES
('10000000-0000-0000-0000-000000000001', 'local-rule-analysis', 'v1', '本地规则投资分析模型', 'ANALYSIS', 'LOCAL_RULE', 'classpath:local-rule-v1',
 JSON_OBJECT('model', 'local-rule-v1', 'timeoutSeconds', 30, 'temperature', 0, 'mockEnabled', false),
 JSON_OBJECT('configurationType', 'LOCAL_RULE', 'initializedBy', 'real-ingestion-reset'), 'ACTIVE', CURRENT_TIMESTAMP(3), NULL, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),
('10000000-0000-0000-0000-000000000002', 'openai-compatible-analysis', 'default-v1', 'OpenAI 默认投资分析模型', 'ANALYSIS', 'OPENAI_COMPATIBLE', 'https://api.openai.com/v1',
 JSON_OBJECT('baseUrl', 'https://api.openai.com/v1', 'model', 'gpt-4.1-mini', 'secretRef', 'OPENAI_API_KEY', 'timeoutSeconds', 90, 'temperature', 0.2, 'mockEnabled', true),
 JSON_OBJECT('configurationType', 'OPENAI_DEFAULT', 'protocol', 'OPENAI_COMPATIBLE', 'frontConfigurable', true, 'defaultForAutoReport', true, 'mockEnabledDefault', true), 'ACTIVE', CURRENT_TIMESTAMP(3), NULL, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3));

-- 6. 初始化 Prompt 基线。它是系统配置，不是某次业务报告产物。
INSERT INTO aiw_ai_prompt_template
(biz_id, prompt_code, prompt_version, scenario, template_name, template_content, status, description, created_at, updated_at, created_by, updated_by)
VALUES
('18000000-0000-0000-0000-000000000001', 'investment-plan-from-report', 'auto-v1', 'INVESTMENT_PLAN', '自动投资报告转方案 Prompt',
 '你是投资辅助平台的方案生成模型。请只基于 ${investmentReport}、${dataQualityGate}、${riskBoundary} 输出结构化方案。若数据质量不足、产品风险画像缺失、用户风险不匹配或行情不新鲜，必须返回 dataGap 与 riskNotice，不得输出积极配置建议。输出必须符合 ${outputSchema}。',
 'ACTIVE', '真实采集闭环启动基线；前端可复制新版本调整。', CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'REAL_INGESTION_RESET', 'REAL_INGESTION_RESET');

INSERT INTO aiw_ai_prompt_variable
(biz_id, prompt_biz_id, variable_name, source_path, required, description, created_at)
VALUES
('18000000-0000-0000-0001-000000000001', '18000000-0000-0000-0000-000000000001', 'investmentReport', 'report', 1, '投资报告完整 JSON', CURRENT_TIMESTAMP(3)),
('18000000-0000-0000-0001-000000000002', '18000000-0000-0000-0000-000000000001', 'dataQualityGate', 'report.dataQualityGate', 1, '数据质量门禁和降级原因', CURRENT_TIMESTAMP(3)),
('18000000-0000-0000-0001-000000000003', '18000000-0000-0000-0000-000000000001', 'riskBoundary', 'user.riskProfile', 1, '用户风险等级、产品适配和 Mock 交易边界', CURRENT_TIMESTAMP(3)),
('18000000-0000-0000-0001-000000000004', '18000000-0000-0000-0000-000000000001', 'outputSchema', 'prompt.outputSchema', 1, '投资方案输出 JSON Schema', CURRENT_TIMESTAMP(3));

INSERT INTO aiw_ai_prompt_output_schema
(biz_id, prompt_biz_id, schema_version, schema_json, created_at)
VALUES
('18000000-0000-0000-0002-000000000001', '18000000-0000-0000-0000-000000000001', 'v1',
 JSON_OBJECT(
   'type', 'object',
   'required', JSON_ARRAY('summary', 'actions', 'riskNotice', 'dataGap'),
   'properties', JSON_OBJECT(
     'summary', JSON_OBJECT('type', 'string'),
     'actions', JSON_OBJECT('type', 'array'),
     'riskNotice', JSON_OBJECT('type', 'array'),
     'dataGap', JSON_OBJECT('type', 'array')
   )
 ), CURRENT_TIMESTAMP(3));

COMMIT;

SELECT 'users' AS metric, COUNT(*) AS value FROM aiw_user
UNION ALL SELECT 'data_sources', COUNT(*) FROM aiw_data_source
UNION ALL SELECT 'task_definitions', COUNT(*) FROM aiw_investment_task_definition
UNION ALL SELECT 'ai_models', COUNT(*) FROM aiw_ai_model
UNION ALL SELECT 'prompt_templates', COUNT(*) FROM aiw_ai_prompt_template
UNION ALL SELECT 'products_should_be_zero', COUNT(*) FROM aiw_product
UNION ALL SELECT 'quotes_should_be_zero', COUNT(*) FROM aiw_market_quote
UNION ALL SELECT 'news_should_be_zero', COUNT(*) FROM aiw_news_article
UNION ALL SELECT 'reports_should_be_zero', COUNT(*) FROM aiw_investment_analysis_report
UNION ALL SELECT 'portfolios_should_be_zero', COUNT(*) FROM aiw_portfolio
UNION ALL SELECT 'orders_should_be_zero', COUNT(*) FROM aiw_order
UNION ALL SELECT 'feedback_should_be_zero', COUNT(*) FROM aiw_investment_feedback;

SELECT task_code, task_type, enabled
FROM aiw_investment_task_definition
ORDER BY task_code;

SELECT h.source_code, s.enabled, h.success_rate, h.failure_reason
FROM aiw_data_source_health h
JOIN aiw_data_source s ON s.source_code = h.source_code
ORDER BY h.source_code;
