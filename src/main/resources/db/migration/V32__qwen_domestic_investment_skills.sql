-- ============================================================================
-- V32 千问国内投资能力模型与 Skill 模板
-- 说明：
--   1. 国内投资数据的联网整理、研报解读、宏观分析、用户陪伴优先挂靠千问。
--   2. 行情/K线/净值仍由 AKShare、Tushare 或授权 API 确定性采集落库。
--   3. 本迁移只注入模型配置模板和 Skill 资产，不自动开启真实交易或自动闭环。
-- ============================================================================

INSERT INTO aiw_ai_model
(biz_id, model_code, model_version, model_name, model_type, provider, artifact_uri,
 model_config, metrics, status, activated_at, retired_at, created_at, updated_at)
VALUES
('32000000-0000-0000-0000-000000000001',
 'qwen-domestic-investment', 'default-v1', '通义千问国内投资能力模型', 'ANALYSIS',
 'OPENAI_COMPATIBLE', 'https://dashscope.aliyuncs.com/compatible-mode/v1',
 JSON_OBJECT(
     'baseUrl', 'https://dashscope.aliyuncs.com/compatible-mode/v1',
     'model', 'qwen-plus',
     'secretRef', 'QWEN_API_KEY',
     'timeoutSeconds', 90,
     'maxTokens', 4000,
     'temperature', 0.2,
     'mockEnabled', false
 ),
 JSON_OBJECT(
     'configurationType', 'REMOTE',
     'protocol', 'OPENAI_COMPATIBLE',
     'provider', 'DASHSCOPE',
     'primaryUse', 'DOMESTIC_INVESTMENT_INTELLIGENCE'
 ),
 'ACTIVE', CURRENT_TIMESTAMP(3), NULL, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE
model_name = VALUES(model_name),
model_type = VALUES(model_type),
provider = VALUES(provider),
artifact_uri = VALUES(artifact_uri),
model_config = VALUES(model_config),
metrics = VALUES(metrics),
status = VALUES(status),
activated_at = VALUES(activated_at),
updated_at = CURRENT_TIMESTAMP(3);

INSERT INTO aiw_ai_model_binding
(biz_id, scenario_code, scenario_name, model_code, provider_code, environment, enabled, config, description,
 created_at, updated_at, created_by, updated_by)
VALUES
('32000000-0000-0000-0000-000000000101', 'DOMESTIC_DATA_INTELLIGENCE', '国内投资数据智能整理',
 'qwen-domestic-investment', 'OPENAI_COMPATIBLE', 'DEFAULT', 1,
 JSON_OBJECT('sourcePolicy', 'TRACEABLE_ONLY', 'autoWriteCoreTables', false, 'candidateLimit', 5),
 '国内投资数据来源整理、字段映射建议和采集失败解释；不直接写核心行情/资讯表。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'V32', 'V32'),
('32000000-0000-0000-0000-000000000102', 'RESEARCH_REPORT_READING', '研报与公告解读',
 'qwen-domestic-investment', 'OPENAI_COMPATIBLE', 'DEFAULT', 1,
 JSON_OBJECT('requireSourceUrl', true, 'outputFormat', 'json_object'),
 '解读巨潮公告、财报和授权研报，提炼财务指标、风险提示和经营变化。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'V32', 'V32'),
('32000000-0000-0000-0000-000000000103', 'MACRO_ANALYSIS', '宏观数据影响分析',
 'qwen-domestic-investment', 'OPENAI_COMPATIBLE', 'DEFAULT', 1,
 JSON_OBJECT('requireIndicatorEvidence', true, 'outputFormat', 'json_object'),
 '分析 CPI、利率、收益率、流动性等宏观因子对市场风格和风险偏好的影响。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'V32', 'V32'),
('32000000-0000-0000-0000-000000000104', 'INVESTOR_EDUCATION_COMPANION', '用户陪伴与投教',
 'qwen-domestic-investment', 'OPENAI_COMPATIBLE', 'DEFAULT', 1,
 JSON_OBJECT('noReturnPromise', true, 'riskReminderRequired', true),
 '面向前端用户解释投资概念、模拟盘操作、报告结论和风险边界。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'V32', 'V32')
ON DUPLICATE KEY UPDATE
scenario_name = VALUES(scenario_name),
model_code = VALUES(model_code),
provider_code = VALUES(provider_code),
enabled = VALUES(enabled),
config = VALUES(config),
description = VALUES(description),
updated_at = CURRENT_TIMESTAMP(3),
updated_by = 'V32';

INSERT INTO aiw_ai_skill
(biz_id, skill_code, skill_version, skill_name, skill_type, status, instruction_content,
 input_schema, output_schema, evaluation_policy, description, created_at, updated_at, created_by, updated_by)
VALUES
('32000000-0000-0000-0000-000000000201', 'MARKET_DATA_TOOL_SKILL', 'v1',
 '行情数据工具 Skill', 'DATA_TOOL', 'ACTIVE',
 '对接 AKShare、Tushare 或授权行情 API，整理实时股价、日线K线、ETF净值和大盘指数的字段映射、采集计划和质量解释。不得凭记忆生成行情；没有工具或接口返回值时必须输出数据缺口。实际写入 aiw_product 和 aiw_market_quote 必须由确定性采集器完成。',
 JSON_OBJECT('type', 'object', 'required', JSON_ARRAY('marketScope', 'symbols', 'provider')),
 JSON_OBJECT('type', 'object', 'required', JSON_ARRAY('fieldMappings', 'qualityPolicy', 'dataGaps')),
 JSON_OBJECT('traceableOnly', true, 'noSyntheticQuote', true, 'coreTableWriter', 'DETERMINISTIC_COLLECTOR'),
 '行情数据 Skill，服务于 AKShare/Tushare 采集器配置和质量解释。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'V32', 'V32'),
('32000000-0000-0000-0000-000000000202', 'RESEARCH_REPORT_READING_SKILL', 'v1',
 '研报解读 Skill', 'RESEARCH_ANALYSIS', 'ACTIVE',
 '对接巨潮资讯网公告、财报或授权研报库，提炼公司核心财务指标、经营变化、风险提示、行业趋势和可引用证据。必须保留原文 URL、公告编号或研报来源；不得直接输出买卖建议。',
 JSON_OBJECT('type', 'object', 'required', JSON_ARRAY('sourceUrl', 'documentType')),
 JSON_OBJECT('type', 'object', 'required', JSON_ARRAY('financialHighlights', 'riskFactors', 'evidence')),
 JSON_OBJECT('requireSourceUrl', true, 'noDirectTradeAdvice', true),
 '研报、财报、公告解读 Skill。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'V32', 'V32'),
('32000000-0000-0000-0000-000000000203', 'MACRO_ANALYSIS_SKILL', 'v1',
 '宏观分析 Skill', 'MACRO_ANALYSIS', 'ACTIVE',
 '对接 FRED 等宏观数据库，分析 CPI、利率、国债收益率、美元指数、流动性等指标对市场风格、风险偏好和股市的可能影响。宏观结论只能作为背景解释，不能覆盖产品级数据和风险门禁。',
 JSON_OBJECT('type', 'object', 'required', JSON_ARRAY('indicators', 'lookbackWindow')),
 JSON_OBJECT('type', 'object', 'required', JSON_ARRAY('macroSummary', 'marketImpact', 'riskScenarios')),
 JSON_OBJECT('evidenceRequired', true, 'backgroundOnly', true),
 '宏观因子解释 Skill，第一批以 FRED 为海外宏观来源，国内宏观后续扩展央行/统计局等来源。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'V32', 'V32'),
('32000000-0000-0000-0000-000000000204', 'INVESTOR_EDUCATION_COMPANION_SKILL', 'v1',
 '用户陪伴与投教 Skill', 'INVESTOR_EDUCATION', 'ACTIVE',
 '面向新手用户解释投资概念、模拟盘操作、报告中的风险等级、回撤、波动率、仓位和止损等概念。不得承诺收益，不得诱导真实交易；当用户风险等级不匹配时必须提示风险。',
 JSON_OBJECT('type', 'object', 'required', JSON_ARRAY('userQuestion')),
 JSON_OBJECT('type', 'object', 'required', JSON_ARRAY('answer', 'riskReminder')),
 JSON_OBJECT('noReturnPromise', true, 'riskReminderRequired', true, 'educationOnly', true),
 '前端用户陪伴和投教 Skill。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'V32', 'V32')
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
updated_by = 'V32';

INSERT INTO aiw_ai_model_skill_binding
(biz_id, model_biz_id, model_code, model_version, skill_biz_id, skill_code, skill_version,
 scenario_code, priority, enabled, config, description, created_at, updated_at, created_by, updated_by)
VALUES
('32000000-0000-0000-0000-000000000301',
 '32000000-0000-0000-0000-000000000001', 'qwen-domestic-investment', 'default-v1',
 '32000000-0000-0000-0000-000000000201', 'MARKET_DATA_TOOL_SKILL', 'v1',
 'DOMESTIC_DATA_INTELLIGENCE', 10, 1,
 JSON_OBJECT('autoWriteCoreTables', false, 'providerCandidates', JSON_ARRAY('AKSHARE', 'TUSHARE')),
 '千问挂靠行情数据工具 Skill，仅生成配置/解释，核心行情由确定性采集器写入。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'V32', 'V32'),
('32000000-0000-0000-0000-000000000302',
 '32000000-0000-0000-0000-000000000001', 'qwen-domestic-investment', 'default-v1',
 '32000000-0000-0000-0000-000000000202', 'RESEARCH_REPORT_READING_SKILL', 'v1',
 'RESEARCH_REPORT_READING', 10, 1,
 JSON_OBJECT('requireSourceUrl', true),
 '千问挂靠研报解读 Skill。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'V32', 'V32'),
('32000000-0000-0000-0000-000000000303',
 '32000000-0000-0000-0000-000000000001', 'qwen-domestic-investment', 'default-v1',
 '32000000-0000-0000-0000-000000000203', 'MACRO_ANALYSIS_SKILL', 'v1',
 'MACRO_ANALYSIS', 10, 1,
 JSON_OBJECT('primaryMacroSource', 'FRED', 'backgroundOnly', true),
 '千问挂靠宏观分析 Skill。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'V32', 'V32'),
('32000000-0000-0000-0000-000000000304',
 '32000000-0000-0000-0000-000000000001', 'qwen-domestic-investment', 'default-v1',
 '32000000-0000-0000-0000-000000000204', 'INVESTOR_EDUCATION_COMPANION_SKILL', 'v1',
 'INVESTOR_EDUCATION_COMPANION', 10, 1,
 JSON_OBJECT('educationOnly', true, 'riskReminderRequired', true),
 '千问挂靠用户陪伴与投教 Skill。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'V32', 'V32')
ON DUPLICATE KEY UPDATE
model_code = VALUES(model_code),
model_version = VALUES(model_version),
skill_code = VALUES(skill_code),
skill_version = VALUES(skill_version),
priority = VALUES(priority),
enabled = VALUES(enabled),
config = VALUES(config),
description = VALUES(description),
updated_at = CURRENT_TIMESTAMP(3),
updated_by = 'V32';
