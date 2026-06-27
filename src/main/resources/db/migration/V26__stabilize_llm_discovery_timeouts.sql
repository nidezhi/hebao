-- ============================================================================
-- V26 稳定 LLM 数据源发现任务
-- 说明：
--   数据源发现任务需要模型整理数据源、字段映射、采集计划和质量策略。
--   在远程中转模型下，一次生成 8 个候选容易超过 90 秒。
--   本迁移降低默认候选规模，并给 OpenAI-compatible 模型增加输出上限和更宽的超时。
-- ============================================================================

UPDATE aiw_investment_task_definition
SET parameters = JSON_SET(parameters, '$.candidateLimit', '4'),
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

UPDATE aiw_ai_model
SET model_config = JSON_SET(
        COALESCE(model_config, JSON_OBJECT()),
        '$.timeoutSeconds', 180,
        '$.maxTokens', 3000
    ),
    updated_at = CURRENT_TIMESTAMP(3)
WHERE model_code = 'openai-compatible-analysis'
  AND provider = 'OPENAI_COMPATIBLE'
  AND status = 'ACTIVE';
