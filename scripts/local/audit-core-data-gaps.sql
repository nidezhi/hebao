-- Read-only audit for GAP-0107/0108/0109.

SELECT 'gap_0107_news_quality_summary' AS section,
       data_type,
       COUNT(*) AS snapshot_count,
       MIN(quality_score) AS min_quality,
       MAX(quality_score) AS max_quality,
       AVG(quality_score) AS avg_quality,
       SUM(sample_count) AS total_samples
FROM aiw_data_quality_snapshot
WHERE data_type = 'NEWS'
GROUP BY data_type;

SELECT 'gap_0107_latest_news_quality' AS section,
       source_code,
       data_type,
       quality_score,
       missing_rate,
       duplicate_rate,
       freshness_score,
       sample_count,
       snapshot_time,
       JSON_UNQUOTE(JSON_EXTRACT(detail, '$.taskCode')) AS task_code,
       JSON_UNQUOTE(JSON_EXTRACT(detail, '$.savedNewsCount')) AS saved_news_count,
       JSON_UNQUOTE(JSON_EXTRACT(detail, '$.expectedNewsCount')) AS expected_news_count
FROM aiw_data_quality_snapshot
WHERE data_type = 'NEWS'
ORDER BY snapshot_time DESC
LIMIT 12;

SELECT 'gap_0107_news_articles_by_source' AS section,
       source_code,
       COUNT(*) AS article_count,
       MIN(publish_time) AS min_publish_time,
       MAX(publish_time) AS max_publish_time,
       COUNT(DISTINCT external_id) AS external_id_count
FROM aiw_news_article
WHERE is_deleted = 0
GROUP BY source_code
ORDER BY article_count DESC, source_code
LIMIT 20;

SELECT 'gap_0107_news_relations_by_theme' AS section,
       theme_code,
       theme_name,
       COUNT(*) AS relation_count,
       COUNT(DISTINCT article_biz_id) AS article_count,
       MAX(source_quality_score) AS max_source_quality,
       AVG(relation_score) AS avg_relation_score
FROM aiw_news_article_relation
GROUP BY theme_code, theme_name
ORDER BY relation_count DESC, theme_code
LIMIT 20;

SELECT 'gap_0108_source_health_coverage' AS section,
       COUNT(*) AS source_count,
       SUM(CASE WHEN enabled = 1 THEN 1 ELSE 0 END) AS enabled_count,
       SUM(CASE WHEN h.source_code IS NOT NULL THEN 1 ELSE 0 END) AS health_count,
       SUM(CASE WHEN enabled = 1 AND h.source_code IS NULL THEN 1 ELSE 0 END) AS enabled_missing_health_count
FROM aiw_data_source s
LEFT JOIN aiw_data_source_health h ON h.source_code = s.source_code;

SELECT 'gap_0108_source_health_by_type' AS section,
       s.source_type,
       s.trust_level,
       COUNT(*) AS source_count,
       SUM(CASE WHEN s.enabled = 1 THEN 1 ELSE 0 END) AS enabled_count,
       SUM(CASE WHEN h.source_code IS NOT NULL THEN 1 ELSE 0 END) AS health_count,
       SUM(CASE WHEN s.enabled = 1 AND h.source_code IS NULL THEN 1 ELSE 0 END) AS enabled_missing_health_count
FROM aiw_data_source s
LEFT JOIN aiw_data_source_health h ON h.source_code = s.source_code
GROUP BY s.source_type, s.trust_level
ORDER BY enabled_missing_health_count DESC, source_count DESC
LIMIT 20;

SELECT 'gap_0108_missing_enabled_health_sample' AS section,
       s.source_code,
       s.source_name,
       s.source_type,
       s.trust_level,
       s.owner,
       s.updated_at
FROM aiw_data_source s
LEFT JOIN aiw_data_source_health h ON h.source_code = s.source_code
WHERE s.enabled = 1
  AND h.source_code IS NULL
ORDER BY s.updated_at DESC, s.source_code
LIMIT 30;

SELECT 'gap_0109_risk_summary' AS section,
       COUNT(*) AS risk_check_count,
       COUNT(DISTINCT business_type) AS business_type_count,
       COUNT(DISTINCT check_result) AS result_type_count
FROM aiw_risk_check;

SELECT 'gap_0109_risk_by_business_result' AS section,
       business_type,
       check_result,
       risk_level,
       COUNT(*) AS check_count,
       MAX(checked_at) AS latest_checked_at
FROM aiw_risk_check
GROUP BY business_type, check_result, risk_level
ORDER BY check_count DESC, business_type, check_result;

SELECT 'gap_0109_latest_risk_checks' AS section,
       biz_id,
       business_type,
       business_biz_id,
       user_biz_id,
       rule_code,
       rule_version,
       check_result,
       risk_level,
       score,
       reason_code,
       checked_at
FROM aiw_risk_check
ORDER BY checked_at DESC
LIMIT 20;
