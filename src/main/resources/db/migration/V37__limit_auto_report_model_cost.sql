-- ============================================================================
-- V37 自动报告降载与模型调用成本保护
-- 说明：
--   1. 自动报告任务默认最多生成 1 份主题报告，避免闭环一次触发多次远程模型。
--   2. 闭环任务保留空 themeCodes，表示默认只生成市场级报告。
--   3. 如需批量主题报告，由前端显式配置 themeCodes 与 maxThemeReports。
-- ============================================================================

UPDATE aiw_investment_task_definition
SET parameters = JSON_SET(
        COALESCE(parameters, JSON_OBJECT()),
        '$.maxThemeReports',
        '1'
    ),
    description = '自动投资报告生成任务。默认最多生成1份报告以控制远程模型成本；前端可显式配置themeCodes与maxThemeReports批量生成。',
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_code = 'auto-openai-investment-report-generation';

UPDATE aiw_investment_task_definition
SET parameters = JSON_SET(
        COALESCE(parameters, JSON_OBJECT()),
        '$.themeCodes',
        '',
        '$.maxThemeReports',
        '1'
    ),
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_code = 'auto-investment-closed-loop-orchestration';
