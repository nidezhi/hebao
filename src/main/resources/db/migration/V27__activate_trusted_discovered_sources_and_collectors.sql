-- ============================================================================
-- V27 激活可信数据源候选并补齐闭环采集执行原语
-- 说明：
--   V23/V26 后默认任务可以通过模型发现数据源，但候选仍处于 disabled，
--   导致后续报告只能看到 NO_RECENT_NEWS / LOW_DATA_QUALITY。
--   本迁移只启用 L1-L3 可信候选，不生成兜底数据；真实产品、行情、新闻仍必须
--   通过专用采集任务或前端配置的真实端点入库。
-- ============================================================================

UPDATE aiw_investment_task_definition
SET parameters = JSON_SET(
        COALESCE(parameters, JSON_OBJECT()),
        '$.autoEnableCandidates', 'true',
        '$.autoEnableReviewRequiredCandidates', 'true',
        '$.autoEnableTrustLevels', 'L1,L2,L3'
    ),
    description = CONCAT(description, ' 已开启 L1-L3 可信候选自动启用；不自动启用低可信来源。'),
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_type = 'AI_DATA_SOURCE_DISCOVERY'
  AND task_code IN (
      'llm-data-collection-multi-source',
      'llm-official-disclosure-collection',
      'llm-news-research-collection',
      'llm-product-nav-collection',
      'llm-market-quote-collection',
      'llm-regulatory-collection'
  );

UPDATE aiw_data_source
SET enabled = 1,
    updated_by = 'SYSTEM_V27',
    updated_at = CURRENT_TIMESTAMP(3)
WHERE enabled = 0
  AND trust_level IN ('L1', 'L2', 'L3');

UPDATE aiw_investment_task_definition
SET parameters = JSON_SET(
        COALESCE(parameters, JSON_OBJECT()),
        '$.dataTaskCodes',
        'llm-data-collection-multi-source,llm-official-disclosure-collection,llm-product-nav-collection,llm-market-quote-collection,llm-news-research-collection,llm-regulatory-collection,l1-regulatory-disclosure-collection,l1-exchange-announcement-collection,l2-wealth-product-nav-refresh,cn-mainland-market-momentum-scan,cn-mainland-hot-theme-return,cn-mainland-news-heat-aggregation'
    ),
    description = '自动投资闭环总编排任务。默认先执行 LLM 数据源发现并启用 L1-L3 可信候选，再执行专用真实采集原语、主题快照、报告、Prompt候选、模型候选、Mock交易、回测和反馈；正式启用新 Prompt、新模型或真实交易仍需前端确认或灰度开关。',
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_code = 'auto-investment-closed-loop-orchestration';
