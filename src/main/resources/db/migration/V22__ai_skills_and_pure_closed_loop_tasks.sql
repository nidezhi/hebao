-- ============================================================================
-- V22 AI Skills 与纯净闭环任务重整
-- 设计说明：
--   1. 数据源发现、Prompt 治理等大模型交互能力以 Skill 版本落库。
--   2. 模型实例通过 aiw_ai_model_skill_binding 挂靠 Skill，复盘时可定位具体模型与 Skill 版本。
--   3. 旧 RSS/fallback/手工 endpoint 方案退出默认主动闭环，只保留为人工配置后的执行原语。
-- ============================================================================

CREATE TABLE aiw_ai_skill (
    biz_id CHAR(36) NOT NULL COMMENT 'Skill业务唯一标识',
    skill_code VARCHAR(64) NOT NULL COMMENT '跨版本稳定Skill编码',
    skill_version VARCHAR(32) NOT NULL COMMENT 'Skill版本',
    skill_name VARCHAR(128) NOT NULL COMMENT 'Skill展示名称',
    skill_type VARCHAR(64) NOT NULL COMMENT 'Skill类型：DATA_SOURCE_DISCOVERY、PROMPT_GOVERNANCE等',
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT、VALIDATING、ACTIVE、RETIRED、ARCHIVED',
    instruction_content TEXT NOT NULL COMMENT '给大模型的Skill指令内容',
    input_schema JSON NULL COMMENT '输入JSON Schema',
    output_schema JSON NULL COMMENT '输出JSON Schema',
    evaluation_policy JSON NULL COMMENT '评估策略JSON',
    description VARCHAR(512) NULL COMMENT 'Skill说明',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    created_by VARCHAR(64) NULL COMMENT '创建人',
    updated_by VARCHAR(64) NULL COMMENT '更新人',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_ai_skill_code_version (skill_code, skill_version),
    KEY idx_ai_skill_type_status (skill_type, status, updated_at),
    KEY idx_ai_skill_code_status (skill_code, status, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI Skill版本化资产';

CREATE TABLE aiw_ai_model_skill_binding (
    biz_id CHAR(36) NOT NULL COMMENT '绑定业务唯一标识',
    model_biz_id CHAR(36) NOT NULL COMMENT '模型业务唯一标识快照',
    model_code VARCHAR(64) NOT NULL COMMENT '模型稳定编码快照',
    model_version VARCHAR(32) NOT NULL COMMENT '模型版本快照',
    skill_biz_id CHAR(36) NOT NULL COMMENT 'Skill业务唯一标识快照',
    skill_code VARCHAR(64) NOT NULL COMMENT 'Skill稳定编码快照',
    skill_version VARCHAR(32) NOT NULL COMMENT 'Skill版本快照',
    scenario_code VARCHAR(64) NOT NULL COMMENT '业务场景编码',
    priority INT NOT NULL DEFAULT 100 COMMENT '优先级，数值越小越优先',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    config JSON NULL COMMENT '场景级绑定配置',
    description VARCHAR(512) NULL COMMENT '绑定说明',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    created_by VARCHAR(64) NULL COMMENT '创建人',
    updated_by VARCHAR(64) NULL COMMENT '更新人',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_ai_model_skill_scenario (model_biz_id, skill_biz_id, scenario_code),
    KEY idx_ai_model_skill_model (model_biz_id, enabled, priority),
    KEY idx_ai_model_skill_skill (skill_code, skill_version),
    KEY idx_ai_model_skill_scenario (scenario_code, enabled, priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模型实例与Skill版本绑定';

INSERT INTO aiw_ai_skill
(biz_id, skill_code, skill_version, skill_name, skill_type, status, instruction_content,
 input_schema, output_schema, evaluation_policy, description, created_at, updated_at, created_by, updated_by)
VALUES
('23000000-0000-0000-0000-000000000001', 'DATA_SOURCE_DISCOVERY_CORE', 'v1',
 '核心数据源发现Skill', 'DATA_SOURCE_DISCOVERY', 'ACTIVE',
 '围绕投资理财平台发现可信数据源。优先官方监管、交易所、产品披露和授权专业供应商；输出必须标注来源等级、可用性、字段映射、授权要求、置信度和人工审核要求；禁止把兜底样本或单一低质量媒体源作为正式投资依据。',
 JSON_OBJECT(
   'type', 'object',
   'required', JSON_ARRAY('marketScope', 'dataTypes'),
   'properties', JSON_OBJECT(
     'marketScope', JSON_OBJECT('type', 'string'),
     'assetClass', JSON_OBJECT('type', 'string'),
     'dataTypes', JSON_OBJECT('type', 'array'),
     'preferredTrustLevels', JSON_OBJECT('type', 'array'),
     'candidateLimit', JSON_OBJECT('type', 'integer')
   )
 ),
 JSON_OBJECT(
   'type', 'object',
   'required', JSON_ARRAY('candidates', 'reviewPolicy'),
   'properties', JSON_OBJECT(
     'candidates', JSON_OBJECT('type', 'array'),
     'reviewPolicy', JSON_OBJECT('type', 'string')
   )
 ),
 JSON_OBJECT('minConfidence', 0.70, 'officialFirst', true, 'manualReviewRequired', true),
 '数据源发现默认Skill，前端可复制新版本调整来源偏好和输出约束。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'V22', 'V22'),
('23000000-0000-0000-0000-000000000002', 'PROMPT_GOVERNANCE_CORE', 'v1',
 '核心Prompt治理Skill', 'PROMPT_GOVERNANCE', 'ACTIVE',
 '根据投资报告、数据质量门禁、Mock交易结果、回测和用户反馈生成Prompt候选或改进建议。不得自动启用新Prompt；必须输出变更原因、适用场景、风险边界、回滚说明和评分依据。',
 JSON_OBJECT(
   'type', 'object',
   'required', JSON_ARRAY('reports', 'feedback', 'backtests'),
   'properties', JSON_OBJECT(
     'reports', JSON_OBJECT('type', 'array'),
     'feedback', JSON_OBJECT('type', 'array'),
     'backtests', JSON_OBJECT('type', 'array')
   )
 ),
 JSON_OBJECT(
   'type', 'object',
   'required', JSON_ARRAY('promptCandidates', 'scores', 'activationPolicy'),
   'properties', JSON_OBJECT(
     'promptCandidates', JSON_OBJECT('type', 'array'),
     'scores', JSON_OBJECT('type', 'array'),
     'activationPolicy', JSON_OBJECT('type', 'string')
   )
 ),
 JSON_OBJECT('autoActivation', false, 'minScoreForReview', 0.70, 'rollbackRequired', true),
 'Prompt治理默认Skill，复盘结论为Prompt不佳时优先调整该Skill或复制新版本。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'V22', 'V22')
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
updated_by = 'V22';

INSERT INTO aiw_ai_model_skill_binding
(biz_id, model_biz_id, model_code, model_version, skill_biz_id, skill_code, skill_version,
 scenario_code, priority, enabled, config, description, created_at, updated_at, created_by, updated_by)
VALUES
('24000000-0000-0000-0000-000000000001',
 '10000000-0000-0000-0000-000000000002', 'openai-compatible-analysis', 'mock-v1',
 '23000000-0000-0000-0000-000000000001', 'DATA_SOURCE_DISCOVERY_CORE', 'v1',
 'DATA_SOURCE_DISCOVERY', 10, 1,
 JSON_OBJECT('candidateLimit', 8, 'manualReviewRequired', true, 'autoApply', false),
 '默认 OpenAI 兼容模型挂靠数据源发现 Skill。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'V22', 'V22'),
('24000000-0000-0000-0000-000000000002',
 '10000000-0000-0000-0000-000000000002', 'openai-compatible-analysis', 'mock-v1',
 '23000000-0000-0000-0000-000000000002', 'PROMPT_GOVERNANCE_CORE', 'v1',
 'PROMPT_GOVERNANCE', 20, 1,
 JSON_OBJECT('autoActivation', false, 'candidateLimit', 3),
 '默认 OpenAI 兼容模型挂靠 Prompt 治理 Skill。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'V22', 'V22')
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
updated_by = 'V22';

INSERT INTO aiw_investment_task_definition
(biz_id, task_code, task_type, cron, zone, enabled, parameters, description, created_at, updated_at)
VALUES
('17000000-0000-0000-0000-000000000210', 'ai-data-source-discovery',
 'AI_DATA_SOURCE_DISCOVERY', '0 0 */6 * * *', 'Asia/Shanghai', 1,
 JSON_OBJECT(
   'environment', 'DEFAULT',
   'marketScope', 'CN_MAINLAND',
   'assetClass', 'MULTI_ASSET',
   'dataTypes', 'MARKET_QUOTE,NEWS,ANNOUNCEMENT,RESEARCH,REGULATORY',
   'preferredTrustLevels', 'L1,L2,L3,L4',
   'candidateLimit', '8',
   'includeDisabledCandidates', 'true'
 ),
 'AI 数据源发现任务。通过模型挂靠和 Skill 生成候选来源、字段映射和采集建议；不自动启用正式数据源。',
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
SET enabled = 0,
    description = CONCAT(description, '【V22废弃默认启用：旧手工endpoint/RSS方案退出主动闭环，仅保留为人工审核后的执行原语。】'),
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_code IN (
    'l1-regulatory-disclosure-collection',
    'l1-exchange-announcement-collection',
    'l2-wealth-product-nav-refresh'
);

UPDATE aiw_investment_task_definition
SET parameters = JSON_SET(
        parameters,
        '$.dataTaskCodes',
        'ai-data-source-discovery,cn-mainland-market-momentum-scan,cn-mainland-hot-theme-return,cn-mainland-news-heat-aggregation',
        '$.maxReportsForMock',
        '20'
    ),
    description = '自动投资闭环总编排任务。默认先执行 AI 数据源发现与审计，再生成报告、Prompt候选、Mock交易、回测和反馈；正式启用新数据源、Prompt、模型或真实交易仍需前端确认或灰度开关。',
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_code = 'auto-investment-closed-loop-orchestration';
