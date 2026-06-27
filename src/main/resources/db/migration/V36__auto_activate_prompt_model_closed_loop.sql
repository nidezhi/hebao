-- ============================================================================
-- V36 自动闭环启用 Prompt/模型闸门，真实交易继续关闭
-- 说明：
--   1. 手动触发闭环时默认执行自动报告，不设置 skipReportTask。
--   2. Prompt/模型候选通过闭环审计后允许自动启用，减少前端“已跳过”节点。
--   3. 真实交易仍保持关闭，只允许 Mock 交易自动化。
-- ============================================================================

UPDATE aiw_investment_task_definition
SET parameters = JSON_REMOVE(
        JSON_SET(
            COALESCE(parameters, JSON_OBJECT()),
            '$.allowAutoPromptActivation',
            'true',
            '$.allowAutoModelActivation',
            'true',
            '$.allowRealTrade',
            'false'
        ),
        '$.skipReportTask'
    ),
    description = '自动投资闭环总编排任务。默认关闭定时；手动验证时执行真实数据采集、自动报告、Prompt/模型候选、Prompt/模型自动启用、Mock交易、回测和反馈；真实交易始终关闭。',
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_code = 'auto-investment-closed-loop-orchestration';
