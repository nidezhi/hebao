-- ============================================================================
-- V29 主闭环优先结构化真实数据采集
-- 说明：
--   数据源发现只负责候选来源治理，不会直接填充报告质量门禁需要的资讯、
--   产品和行情核心表。主闭环默认改为先执行 AI 结构化核心数据采集，再做
--   主题快照和报告生成，避免出现 reviewPolicy 成功但报告质量仍不足的假闭环。
-- ============================================================================

UPDATE aiw_investment_task_definition
SET parameters = JSON_SET(
        COALESCE(parameters, JSON_OBJECT()),
        '$.requireStructuredCoreData', 'true',
        '$.minStructuredNewsCount', '1',
        '$.minStructuredQuoteCount', '1',
        '$.dataTaskCodes',
        'llm-structured-core-data-collection,cn-mainland-market-momentum-scan,cn-mainland-hot-theme-return,cn-mainland-news-heat-aggregation'
    ),
    description = '自动投资闭环总编排任务。默认先调用 AI 结构化真实数据采集写入资讯、产品和行情核心表，再执行主题快照、报告、Prompt候选、模型候选、Mock交易、回测和反馈；数据源发现作为后台治理任务独立运行。',
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_code = 'auto-investment-closed-loop-orchestration';
