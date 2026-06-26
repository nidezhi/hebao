-- AI 模型挂靠配置与数据源发现默认场景。
-- 1. 模型不再只散落在任务参数中，前端可以按业务场景统一配置。
-- 2. 数据源发现由大模型产出候选建议，但不自动启用正式数据源。
CREATE TABLE aiw_ai_model_binding (
    biz_id CHAR(36) NOT NULL COMMENT '模型挂靠配置业务唯一标识',
    scenario_code VARCHAR(64) NOT NULL COMMENT '业务场景编码：DATA_SOURCE_DISCOVERY、AUTO_REPORT_GENERATION等',
    scenario_name VARCHAR(128) NOT NULL COMMENT '业务场景展示名称',
    model_code VARCHAR(64) NOT NULL COMMENT '挂靠模型稳定编码',
    provider_code VARCHAR(64) NULL COMMENT '模型提供方一致性校验编码',
    environment VARCHAR(32) NOT NULL DEFAULT 'DEFAULT' COMMENT '生效环境：DEFAULT、DEV、TEST、PROD等',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用该场景挂靠',
    config JSON NULL COMMENT '场景级模型参数，例如温度、候选数量、输出约束',
    description VARCHAR(512) NULL COMMENT '配置说明',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    created_by VARCHAR(64) NULL COMMENT '创建操作者业务ID或系统标识',
    updated_by VARCHAR(64) NULL COMMENT '最后更新操作者业务ID或系统标识',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_ai_model_binding_scenario_env (scenario_code, environment),
    KEY idx_ai_model_binding_model (model_code, enabled),
    KEY idx_ai_model_binding_enabled (enabled, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模型业务场景挂靠配置';

INSERT INTO aiw_ai_model_binding
(biz_id, scenario_code, scenario_name, model_code, provider_code, environment, enabled, config, description,
 created_at, updated_at, created_by, updated_by)
VALUES
('22000000-0000-0000-0000-000000000001', 'DATA_SOURCE_DISCOVERY', '数据源AI发现', 'openai-compatible-analysis',
 'OPENAI_COMPATIBLE', 'DEFAULT', 1,
 JSON_OBJECT('candidateLimit', 8, 'minTrustLevel', 'L4', 'requireOfficialSourceFirst', true, 'autoEnable', false),
 '用于按市场、资产类别和数据类型生成数据源候选、字段映射和采集建议；候选需前端审核后保存。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'V21', 'V21'),
('22000000-0000-0000-0000-000000000002', 'AUTO_REPORT_GENERATION', '自动投资报告生成', 'openai-compatible-analysis',
 'OPENAI_COMPATIBLE', 'DEFAULT', 1,
 JSON_OBJECT('lookbackDays', 30, 'responseFormat', 'json_object'),
 '用于自动投资报告生成任务的默认模型挂靠。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'V21', 'V21'),
('22000000-0000-0000-0000-000000000003', 'AUTO_CLOSED_LOOP_ORCHESTRATION', '自动投资闭环编排', 'openai-compatible-analysis',
 'OPENAI_COMPATIBLE', 'DEFAULT', 1,
 JSON_OBJECT('automationLevel', 'FULL_MOCK', 'allowRealTrade', false),
 '用于自动闭环编排中的模型候选、报告和反馈优化挂靠。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'V21', 'V21'),
('22000000-0000-0000-0000-000000000004', 'PROMPT_GOVERNANCE', 'Prompt自动治理', 'openai-compatible-analysis',
 'OPENAI_COMPATIBLE', 'DEFAULT', 1,
 JSON_OBJECT('candidateLimit', 3, 'autoActivation', false),
 '用于 Prompt 候选生成、评分和复盘，不自动正式启用。',
 CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3), 'V21', 'V21')
ON DUPLICATE KEY UPDATE
scenario_name = VALUES(scenario_name),
model_code = VALUES(model_code),
provider_code = VALUES(provider_code),
enabled = VALUES(enabled),
config = VALUES(config),
description = VALUES(description),
updated_at = CURRENT_TIMESTAMP(3),
updated_by = 'V21';
