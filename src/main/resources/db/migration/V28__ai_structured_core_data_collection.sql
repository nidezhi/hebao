-- ============================================================================
-- V28 AI 结构化核心数据采集
-- 说明：
--   1. 数据源发现只负责发现候选来源，不能填充报告质量门禁需要的核心业务表。
--   2. 新增 AI_STRUCTURED_DATA_COLLECTION 任务，要求远程模型返回可追溯 sourceUrl 的
--      资讯、产品、行情 JSON；应用层严格校验后写入核心表。
--   3. 不降低质量门禁，不生成 mock/fallback 业务数据；模型无法联网或无法给出处
--      时应返回 dataGaps 并由任务审计暴露。
-- ============================================================================

INSERT INTO aiw_ai_skill
(biz_id, skill_code, skill_version, skill_name, skill_type, status, instruction_content,
 input_schema, output_schema, evaluation_policy, description, created_at, updated_at, created_by, updated_by)
VALUES
('28000000-0000-0000-0000-000000000001', 'AI_STRUCTURED_DATA_COLLECTION_CORE', 'v1',
 'AI结构化核心数据采集Skill', 'DATA_COLLECTION', 'ACTIVE',
 '围绕投资理财闭环采集可追溯的结构化真实数据。必须优先覆盖任务 themes 中配置的产品代码和主题关键词；必须输出 newsArticles、products、quotes、dataGaps 四个字段；每条业务数据必须有 sourceCode、sourceUrl 和时间字段；模型或中转不具备联网检索能力时必须返回空数组与 dataGaps，不得凭记忆或模拟数据补全。',
 JSON_OBJECT(
   'type', 'object',
   'required', JSON_ARRAY('marketScope', 'themes', 'dataTypes'),
   'properties', JSON_OBJECT(
     'marketScope', JSON_OBJECT('type', 'string'),
     'themes', JSON_OBJECT('type', 'object'),
     'topicKeywords', JSON_OBJECT('type', 'string'),
     'maxNews', JSON_OBJECT('type', 'integer'),
     'maxProducts', JSON_OBJECT('type', 'integer'),
     'maxQuotes', JSON_OBJECT('type', 'integer')
   )
 ),
 JSON_OBJECT(
   'type', 'object',
   'required', JSON_ARRAY('newsArticles', 'products', 'quotes', 'dataGaps'),
   'properties', JSON_OBJECT(
     'newsArticles', JSON_OBJECT('type', 'array'),
     'products', JSON_OBJECT('type', 'array'),
     'quotes', JSON_OBJECT('type', 'array'),
     'dataGaps', JSON_OBJECT('type', 'array')
   )
 ),
 JSON_OBJECT(
   'requireSourceUrl', true,
   'rejectMockFallback', true,
   'preferRecentHours', 72,
   'mustCoverConfiguredThemeProducts', true,
   'autoActivation', true
 ),
 'AI 直接整理并输出可入库的核心数据；复盘结论为数据源不佳时优先调整该 Skill。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'V28', 'V28')
ON DUPLICATE KEY UPDATE
skill_name = VALUES(skill_name),
skill_type = VALUES(skill_type),
status = VALUES(status),
instruction_content = VALUES(instruction_content),
input_schema = VALUES(input_schema),
output_schema = VALUES(output_schema),
evaluation_policy = VALUES(evaluation_policy),
description = VALUES(description),
updated_at = CURRENT_TIMESTAMP(3),
updated_by = 'V28';

INSERT INTO aiw_investment_task_definition
(biz_id, task_code, task_type, cron, zone, enabled, parameters, description, created_at, updated_at)
VALUES
('17000000-0000-0000-0000-000000000280', 'llm-structured-core-data-collection',
 'AI_STRUCTURED_DATA_COLLECTION', '0 5 */2 * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT(
   'environment', 'DEFAULT',
   'scenarioCode', 'DATA_SOURCE_DISCOVERY',
   'skillCode', 'AI_STRUCTURED_DATA_COLLECTION_CORE',
   'marketScope', 'CN_MAINLAND',
   'assetClass', 'MULTI_ASSET',
   'dataTypes', 'MARKET_QUOTE,NEWS,ANNOUNCEMENT,RESEARCH,REGULATORY',
   'topicKeywords', 'AI人工智能,半导体,黄金,基金净值,ETF净值,监管政策,财经新闻',
   'maxNews', '18',
   'maxProducts', '12',
   'maxQuotes', '36',
   'themes', 'AI人工智能=159819,588000,515980;半导体=512480,159995,688981;黄金=518880,159934'
 ),
 'AI 结构化核心数据采集任务。调用远程模型整理可追溯资讯、产品和行情，并严格校验后落库；不写入 mock/fallback 业务数据。',
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
SET parameters = JSON_SET(
        COALESCE(parameters, JSON_OBJECT()),
        '$.dataTaskCodes',
        'llm-data-collection-multi-source,llm-official-disclosure-collection,llm-product-nav-collection,llm-market-quote-collection,llm-news-research-collection,llm-regulatory-collection,llm-structured-core-data-collection,l1-regulatory-disclosure-collection,l1-exchange-announcement-collection,l2-wealth-product-nav-refresh,cn-mainland-market-momentum-scan,cn-mainland-hot-theme-return,cn-mainland-news-heat-aggregation'
    ),
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_code = 'auto-investment-closed-loop-orchestration';
