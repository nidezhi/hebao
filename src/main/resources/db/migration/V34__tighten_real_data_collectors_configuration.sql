-- ============================================================================
-- V34 收紧第一批真实采集器参数与低成本闭环门禁
-- 说明：
--   1. 产品池可在未配置远端 provider 时按授权代码清单建立最小主档。
--   2. 行情、资讯必须依赖真实 providerBaseUrl，不再允许大模型伪装采集。
--   3. 主闭环仍默认关闭定时，只允许手动验证；质量门禁明确使用真实数据质量分。
-- ============================================================================

UPDATE aiw_investment_task_definition
SET parameters = JSON_SET(
        parameters,
        '$.providerBaseUrl',
        '',
        '$.allowConfigCodeUniverse',
        'true',
        '$.description',
        '产品代码清单来自任务配置或授权数据源；未配置providerBaseUrl时仅建立产品主档，不生成行情和资讯。'
    ),
    description = '确定性真实产品池同步。可按授权代码清单建立产品主档；远端 provider 仅用于补充名称等元数据。',
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_code = 'real-product-universe-sync';

UPDATE aiw_investment_task_definition
SET parameters = JSON_SET(
        parameters,
        '$.providerBaseUrl',
        '',
        '$.requireProviderBaseUrl',
        'true',
        '$.minQuoteCount',
        '1'
    ),
    description = '确定性真实行情/净值同步。必须配置 AKShare/AKTools 或授权行情源 providerBaseUrl，未配置时只记录质量缺口。',
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_code = 'real-market-quote-sync';

UPDATE aiw_investment_task_definition
SET parameters = JSON_SET(
        parameters,
        '$.providerBaseUrl',
        '',
        '$.requireProviderBaseUrl',
        'true',
        '$.minNewsCount',
        '20'
    ),
    description = '确定性真实资讯同步。必须配置 AKShare/AKTools 或授权资讯源 providerBaseUrl，未配置时只记录质量缺口。',
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_code = 'real-news-sync';

UPDATE aiw_investment_task_definition
SET parameters = JSON_SET(
        parameters,
        '$.minRealDataQualityScore',
        '0.60'
    ),
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_code = 'real-data-quality-snapshot';

UPDATE aiw_investment_task_definition
SET enabled = 0,
    parameters = JSON_SET(
        parameters,
        '$.requireStructuredCoreData',
        'true',
        '$.minStructuredProductCount',
        '1',
        '$.minStructuredNewsCount',
        '1',
        '$.minStructuredQuoteCount',
        '1',
        '$.minRealDataQualityScore',
        '0.60',
        '$.dataTaskCodes',
        'real-product-universe-sync,real-market-quote-sync,real-news-sync,real-data-quality-snapshot,cn-mainland-market-momentum-scan,cn-mainland-hot-theme-return,cn-mainland-news-heat-aggregation'
    ),
    description = '自动投资闭环总编排任务。默认关闭定时，手动验证；真实核心数据质量达标后才允许进入AI报告和Mock闭环。',
    updated_at = CURRENT_TIMESTAMP(3)
WHERE task_code = 'auto-investment-closed-loop-orchestration';
