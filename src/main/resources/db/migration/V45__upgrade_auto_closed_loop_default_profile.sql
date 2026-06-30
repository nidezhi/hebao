UPDATE aiw_system_config
SET config_value = JSON_OBJECT(
    'profileCode', 'default-auto-mock',
    'profileName', '默认 AI Mock 闭环方案',
    'profileType', 'SCHEDULED_BASELINE',
    'riskLevel', 'LOW',
    'strategyNote', '默认定时闭环方案：采集、报告、候选、Mock交易、回测反馈全链路执行；真实交易保持关闭。',
    'automationLevel', 'FULL_MOCK',
    'mockPortfolioBizId', '',
    'mockUserBizId', '21000000-0000-0000-0000-000000000002',
    'mockPortfolioName', '全自动闭环模拟组合',
    'initialCash', '100000',
    'promptCode', 'investment-plan-from-report',
    'promptVersion', 'auto-v1',
    'promptScenario', 'INVESTMENT_PLAN',
    'modelType', 'INVESTMENT_ANALYSIS',
    'execution', JSON_OBJECT(
        'runMode', 'FULL_PIPELINE',
        'marketScope', 'CN_MAINLAND',
        'dataTaskCodes', JSON_ARRAY('real-data-quality-snapshot'),
        'reportTaskCode', 'auto-openai-investment-report-generation',
        'promptTaskCode', '',
        'skipReportTask', false,
        'allowPromptCandidate', true,
        'allowModelCandidate', true
    ),
    'qualityGate', JSON_OBJECT(
        'requireStructuredCoreData', false,
        'minQualityScore', '0.45',
        'maxReportsForMock', '20'
    ),
    'safety', JSON_OBJECT(
        'allowAutoMockTrade', true,
        'allowAutoPromptActivation', true,
        'allowAutoModelActivation', true,
        'allowRealTrade', false,
        'maxSingleTradeAmount', '10000'
    ),
    'backtest', JSON_OBJECT(
        'benchmarkCode', '',
        'valuationPointLimit', '100'
    )
),
description = '自动投资闭环高级配置方案；手动触发或定时任务选择后，运行事件保存完整方案快照。',
updated_at = CURRENT_TIMESTAMP(3)
WHERE config_group = 'AUTO_INVESTMENT_CLOSED_LOOP_PROFILE'
  AND config_key = 'default-auto-mock'
  AND environment = 'DEFAULT';
