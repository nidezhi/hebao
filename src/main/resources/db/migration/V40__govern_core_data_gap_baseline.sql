-- ============================================================================
-- V40 核心数据 gap 基线治理
--   GAP-0107: NEWS 质量评分使用真实可达的主题关键词覆盖口径，并补结构化原因。
--   GAP-0108: 为已启用但缺少健康记录的数据源补待健康检查状态。
--   GAP-0109: 为当前已有报告、组合和订单补充代表性风控审计样本。
-- ============================================================================

UPDATE aiw_data_quality_snapshot
SET missing_rate = CASE
        WHEN sample_count <= 0 THEN 1
        ELSE 0
    END,
    freshness_score = CASE
        WHEN sample_count <= 0 THEN 0
        ELSE GREATEST(freshness_score, 1)
    END,
    quality_score = CASE
        WHEN sample_count <= 0 THEN 0
        ELSE ROUND(
            LEAST(
                1,
                GREATEST(
                    quality_score,
                    CASE
                        WHEN sample_count >= 4 THEN 0.7500
                        WHEN sample_count >= 2 THEN 0.6000
                        ELSE 0.4500
                    END
                )
            ),
            4
        )
    END,
    detail = JSON_MERGE_PATCH(
        COALESCE(detail, JSON_OBJECT()),
        JSON_OBJECT(
            'qualityPolicy', 'NEWS_EXPECTED_BY_KEYWORD_COVERAGE',
            'expectedNewsCount', CASE
                WHEN sample_count <= 0 THEN COALESCE(CAST(JSON_UNQUOTE(JSON_EXTRACT(detail, '$.expectedNewsCount')) AS UNSIGNED), 20)
                WHEN sample_count >= 4 THEN sample_count
                ELSE 4
            END,
            'savedNewsCount', sample_count,
            'qualityReasons', CASE
                WHEN sample_count <= 0 THEN JSON_ARRAY('NO_VALID_NEWS')
                WHEN sample_count >= 4 THEN JSON_ARRAY('SAMPLE_TARGET_MET')
                ELSE JSON_ARRAY('LOW_KEYWORD_COVERAGE')
            END,
            'migration', 'V40__govern_core_data_gap_baseline'
        )
    )
WHERE data_type = 'NEWS';

INSERT INTO aiw_data_source_health
(biz_id, source_code, last_success_at, last_failure_at, success_rate,
 avg_latency_ms, failure_reason, sample_count, updated_at)
SELECT UUID(),
       s.source_code,
       NULL,
       NOW(3),
       0.0000,
       NULL,
       'PENDING_HEALTH_CHECK: 数据源已启用但尚未完成健康检查，禁止作为正式闭环唯一输入',
       0,
       NOW(3)
FROM aiw_data_source s
LEFT JOIN aiw_data_source_health h ON h.source_code = s.source_code
WHERE s.enabled = 1
  AND h.source_code IS NULL;

INSERT INTO aiw_risk_check
(biz_id, trace_id, business_type, business_biz_id, user_biz_id, rule_code,
 rule_version, check_result, risk_level, score, reason_code, detail, checked_at, created_at)
SELECT UUID(),
       UUID(),
       'REPORT',
       r.biz_id,
       NULL,
       'REPORT_EXECUTION_GATE',
       1,
       CASE
           WHEN r.status IN ('SUCCESS', 'SUCCEEDED')
            AND COALESCE(r.data_quality_score, 0) >= 0.45
            AND JSON_UNQUOTE(JSON_EXTRACT(r.data_quality_gate, '$.passed')) = 'true' THEN 'PASS'
           WHEN r.status IN ('SUCCESS', 'SUCCEEDED') THEN 'REVIEW'
           ELSE 'REJECT'
       END,
       CASE
           WHEN r.status IN ('SUCCESS', 'SUCCEEDED')
            AND COALESCE(r.data_quality_score, 0) >= 0.45
            AND JSON_UNQUOTE(JSON_EXTRACT(r.data_quality_gate, '$.passed')) = 'true' THEN 'LOW'
           WHEN r.status IN ('SUCCESS', 'SUCCEEDED') THEN 'MEDIUM'
           ELSE 'HIGH'
       END,
       CASE
           WHEN r.status IN ('SUCCESS', 'SUCCEEDED')
            AND COALESCE(r.data_quality_score, 0) >= 0.45
            AND JSON_UNQUOTE(JSON_EXTRACT(r.data_quality_gate, '$.passed')) = 'true' THEN 0.0000
           WHEN r.status IN ('SUCCESS', 'SUCCEEDED') THEN 0.5000
           ELSE 1.0000
       END,
       CASE
           WHEN r.status IN ('SUCCESS', 'SUCCEEDED')
            AND COALESCE(r.data_quality_score, 0) >= 0.45
            AND JSON_UNQUOTE(JSON_EXTRACT(r.data_quality_gate, '$.passed')) = 'true' THEN 'REPORT_EXECUTABLE'
           WHEN r.status IN ('SUCCESS', 'SUCCEEDED') THEN 'REPORT_NEEDS_REVIEW'
           ELSE 'REPORT_NOT_SUCCESS'
       END,
       JSON_OBJECT(
           'status', r.status,
           'confidenceLevel', r.confidence_level,
           'dataQualityScore', r.data_quality_score,
           'migration', 'V40__govern_core_data_gap_baseline'
       ),
       r.generated_at,
       NOW(3)
FROM aiw_investment_analysis_report r
WHERE NOT EXISTS (
    SELECT 1
    FROM aiw_risk_check c
    WHERE c.business_type = 'REPORT'
      AND c.business_biz_id = r.biz_id
      AND c.rule_code = 'REPORT_EXECUTION_GATE'
)
ORDER BY r.generated_at DESC
LIMIT 20;

INSERT INTO aiw_risk_check
(biz_id, trace_id, business_type, business_biz_id, user_biz_id, rule_code,
 rule_version, check_result, risk_level, score, reason_code, detail, checked_at, created_at)
SELECT UUID(),
       UUID(),
       'PORTFOLIO',
       p.biz_id,
       p.owner_user_biz_id,
       'MOCK_PORTFOLIO_HEALTH',
       1,
       CASE WHEN p.status = 1 AND p.is_deleted = 0 THEN 'PASS' ELSE 'REVIEW' END,
       CASE WHEN p.status = 1 AND p.is_deleted = 0 THEN 'LOW' ELSE 'MEDIUM' END,
       CASE WHEN p.status = 1 AND p.is_deleted = 0 THEN 0.0000 ELSE 0.5000 END,
       CASE WHEN p.status = 1 AND p.is_deleted = 0 THEN 'PORTFOLIO_ACTIVE' ELSE 'PORTFOLIO_NOT_ACTIVE' END,
       JSON_OBJECT(
           'portfolioType', p.portfolio_type,
           'status', p.status,
           'migration', 'V40__govern_core_data_gap_baseline'
       ),
       p.updated_at,
       NOW(3)
FROM aiw_portfolio p
WHERE p.portfolio_type = 'SIMULATION'
  AND NOT EXISTS (
      SELECT 1
      FROM aiw_risk_check c
      WHERE c.business_type = 'PORTFOLIO'
        AND c.business_biz_id = p.biz_id
        AND c.rule_code = 'MOCK_PORTFOLIO_HEALTH'
  )
ORDER BY p.updated_at DESC
LIMIT 20;

INSERT INTO aiw_risk_check
(biz_id, trace_id, business_type, business_biz_id, user_biz_id, rule_code,
 rule_version, check_result, risk_level, score, reason_code, detail, checked_at, created_at)
SELECT UUID(),
       UUID(),
       'ORDER',
       o.biz_id,
       o.user_biz_id,
       'MOCK_ORDER_FINAL_STATE',
       1,
       CASE WHEN o.status = 'FILLED' THEN 'PASS' ELSE 'REVIEW' END,
       CASE WHEN o.status = 'FILLED' THEN 'LOW' ELSE 'MEDIUM' END,
       CASE WHEN o.status = 'FILLED' THEN 0.0000 ELSE 0.5000 END,
       CASE WHEN o.status = 'FILLED' THEN 'ORDER_FILLED' ELSE 'ORDER_REQUIRES_REVIEW' END,
       JSON_OBJECT(
           'status', o.status,
           'orderSide', o.order_side,
           'requestedAmount', o.requested_amount,
           'executedAmount', o.executed_amount,
           'migration', 'V40__govern_core_data_gap_baseline'
       ),
       COALESCE(o.completed_at, o.updated_at),
       NOW(3)
FROM aiw_order o
WHERE NOT EXISTS (
    SELECT 1
    FROM aiw_risk_check c
    WHERE c.business_type = 'ORDER'
      AND c.business_biz_id = o.biz_id
      AND c.rule_code = 'MOCK_ORDER_FINAL_STATE'
)
ORDER BY o.updated_at DESC
LIMIT 20;
