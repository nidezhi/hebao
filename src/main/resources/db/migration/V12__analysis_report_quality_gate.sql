-- ============================================================
-- V12 投资分析报告质量门禁
-- 1. 将可信度、数据质量分和质量门禁作为报告一等字段暴露给前端。
-- 2. 低质量数据不再隐藏在 JSON 内部，列表页即可展示报告是否可参考。
-- ============================================================

ALTER TABLE aiw_investment_analysis_report
    ADD COLUMN confidence_level VARCHAR(32) NOT NULL DEFAULT 'LOW_CONFIDENCE'
        COMMENT '报告可信等级：HIGH_CONFIDENCE/MEDIUM_CONFIDENCE/LOW_CONFIDENCE/UNUSABLE',
    ADD COLUMN data_quality_score DECIMAL(10, 4) NOT NULL DEFAULT 0
        COMMENT '报告输入数据质量分，0-1',
    ADD COLUMN data_quality_gate JSON NULL
        COMMENT '数据质量门禁结果，包含是否通过、降级原因和前端提示',
    ADD KEY idx_investment_analysis_quality (confidence_level, data_quality_score, generated_at);
