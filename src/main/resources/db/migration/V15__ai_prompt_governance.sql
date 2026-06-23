-- ============================================================================
-- V15 AI Prompt 版本化治理
-- 设计说明：
--   1. Prompt 是可配置、可预览、可回滚的数据资产，不再只是代码常量。
--   2. 模板、变量和输出 Schema 分表保存，前端可分别展示和维护。
--   3. 状态流转先覆盖 DRAFT、VALIDATING、ACTIVE、RETIRED，真实模型调用仍暂缓。
-- ============================================================================

CREATE TABLE aiw_ai_prompt_template (
    biz_id CHAR(36) NOT NULL COMMENT 'Prompt模板业务唯一标识',
    prompt_code VARCHAR(64) NOT NULL COMMENT 'Prompt跨版本稳定编码',
    prompt_version VARCHAR(32) NOT NULL COMMENT 'Prompt版本号',
    scenario VARCHAR(64) NOT NULL COMMENT '使用场景：INVESTMENT_REPORT、INVESTMENT_PLAN等',
    template_name VARCHAR(128) NOT NULL COMMENT '模板展示名称',
    template_content TEXT NOT NULL COMMENT '模板内容，变量使用 ${variableName} 占位',
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT、VALIDATING、ACTIVE、RETIRED',
    description VARCHAR(512) NULL COMMENT '模板说明',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    created_by VARCHAR(64) NULL COMMENT '创建操作者业务ID或系统标识',
    updated_by VARCHAR(64) NULL COMMENT '最后更新操作者业务ID或系统标识',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_aiw_prompt_code_version (prompt_code, prompt_version),
    KEY idx_aiw_prompt_scenario_status (scenario, status),
    KEY idx_aiw_prompt_status_updated (status, updated_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='AI Prompt模板版本表';

CREATE TABLE aiw_ai_prompt_variable (
    biz_id CHAR(36) NOT NULL COMMENT 'Prompt变量业务唯一标识',
    prompt_biz_id CHAR(36) NOT NULL COMMENT 'Prompt模板业务唯一标识',
    variable_name VARCHAR(64) NOT NULL COMMENT '变量名称，不包含占位符符号',
    source_path VARCHAR(256) NULL COMMENT '变量默认来源路径，如 report.dataQualityGate',
    required TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否必填',
    description VARCHAR(512) NULL COMMENT '变量说明',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_aiw_prompt_variable_name (prompt_biz_id, variable_name),
    KEY idx_aiw_prompt_variable_prompt (prompt_biz_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='AI Prompt变量定义表';

CREATE TABLE aiw_ai_prompt_output_schema (
    biz_id CHAR(36) NOT NULL COMMENT 'Prompt输出Schema业务唯一标识',
    prompt_biz_id CHAR(36) NOT NULL COMMENT 'Prompt模板业务唯一标识',
    schema_version VARCHAR(32) NOT NULL COMMENT 'Schema版本号',
    schema_json JSON NOT NULL COMMENT '输出JSON Schema',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (biz_id),
    UNIQUE KEY uk_aiw_prompt_schema_version (prompt_biz_id, schema_version),
    KEY idx_aiw_prompt_schema_prompt (prompt_biz_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='AI Prompt输出Schema表';
