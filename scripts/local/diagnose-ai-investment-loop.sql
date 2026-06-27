SELECT 'core_counts' AS section,
       (SELECT COUNT(*) FROM aiw_product WHERE is_deleted = 0) AS products,
       (SELECT COUNT(*) FROM aiw_market_quote) AS quotes,
       (SELECT COUNT(*) FROM aiw_news_article WHERE is_deleted = 0) AS news,
       (SELECT COUNT(*) FROM aiw_data_source) AS data_sources,
       (SELECT COUNT(*) FROM aiw_data_quality_snapshot) AS quality_snapshots,
       (SELECT COUNT(*) FROM aiw_investment_analysis_report) AS reports,
       (SELECT COUNT(*) FROM aiw_closed_loop_run) AS closed_loop_runs,
       (SELECT COUNT(*) FROM aiw_scheduled_task_execution) AS task_executions;

SELECT 'recent_business_data' AS section,
       (SELECT MAX(updated_at) FROM aiw_product WHERE is_deleted = 0) AS latest_product_update,
       (SELECT MAX(quote_time) FROM aiw_market_quote) AS latest_quote_time,
       (SELECT MAX(publish_time) FROM aiw_news_article WHERE is_deleted = 0) AS latest_news_publish_time,
       (SELECT MAX(snapshot_time) FROM aiw_data_quality_snapshot) AS latest_quality_snapshot_time,
       (SELECT MAX(generated_at) FROM aiw_investment_analysis_report) AS latest_report_time;

SELECT 'task_status_by_type' AS section,
       task_type,
       status,
       COUNT(*) AS count,
       MIN(started_at) AS first_started_at,
       MAX(started_at) AS last_started_at,
       ROUND(AVG(TIMESTAMPDIFF(SECOND, started_at, COALESCE(completed_at, NOW(3)))), 2) AS avg_seconds
FROM aiw_scheduled_task_execution
GROUP BY task_type, status
ORDER BY task_type, status;

SELECT 'recent_failed_or_blocked_tasks' AS section,
       task_code,
       task_type,
       status,
       started_at,
       completed_at,
       LEFT(COALESCE(failure_reason, result_summary, ''), 800) AS reason
FROM aiw_scheduled_task_execution
WHERE status <> 'SUCCEEDED'
ORDER BY started_at DESC
LIMIT 40;

SELECT 'closed_loop_status' AS section,
       run_status,
       gate_result,
       COUNT(*) AS count,
       MIN(started_at) AS first_started_at,
       MAX(started_at) AS last_started_at,
       ROUND(AVG(COALESCE(quality_score, 0)), 4) AS avg_quality
FROM aiw_closed_loop_run
GROUP BY run_status, gate_result
ORDER BY count DESC;

SELECT 'closed_loop_step_status' AS section,
       step_code,
       step_status,
       COUNT(*) AS count,
       LEFT(MAX(COALESCE(failure_reason, '')), 800) AS sample_reason
FROM aiw_closed_loop_step
GROUP BY step_code, step_status
ORDER BY step_code, step_status;

SELECT 'recent_closed_loop_steps' AS section,
       r.run_no,
       r.run_status,
       s.step_order,
       s.step_code,
       s.step_status,
       LEFT(COALESCE(s.failure_reason, JSON_EXTRACT(s.output_summary, '$')), 800) AS detail,
       s.updated_at
FROM aiw_closed_loop_step s
JOIN aiw_closed_loop_run r ON r.biz_id = s.run_biz_id
ORDER BY s.updated_at DESC
LIMIT 60;

SELECT 'report_quality' AS section,
       status,
       confidence_level,
       COUNT(*) AS count,
       ROUND(AVG(data_quality_score), 4) AS avg_quality,
       MIN(data_quality_score) AS min_quality,
       MAX(data_quality_score) AS max_quality
FROM aiw_investment_analysis_report
GROUP BY status, confidence_level
ORDER BY status, confidence_level;

SELECT 'recent_reports' AS section,
       biz_id,
       status,
       provider_code,
       model_code,
       confidence_level,
       data_quality_score,
       JSON_EXTRACT(data_quality_gate, '$.passed') AS gate_passed,
       JSON_EXTRACT(data_quality_gate, '$.reasons') AS gate_reasons,
       LEFT(COALESCE(failure_reason, ''), 800) AS failure_reason,
       generated_at
FROM aiw_investment_analysis_report
ORDER BY generated_at DESC
LIMIT 30;

SELECT 'data_quality_by_type' AS section,
       data_type,
       source_code,
       COUNT(*) AS count,
       ROUND(AVG(quality_score), 4) AS avg_quality,
       ROUND(AVG(missing_rate), 4) AS avg_missing,
       ROUND(AVG(freshness_score), 4) AS avg_freshness,
       SUM(sample_count) AS total_samples,
       MAX(snapshot_time) AS latest_snapshot_time
FROM aiw_data_quality_snapshot
GROUP BY data_type, source_code
ORDER BY latest_snapshot_time DESC, avg_quality ASC;

SELECT 'data_sources' AS section,
       source_code,
       source_name,
       source_type,
       trust_level,
       enabled,
       fetch_frequency,
       owner,
       LEFT(description, 500) AS description,
       updated_at
FROM aiw_data_source
ORDER BY updated_at DESC
LIMIT 50;

SELECT 'structured_collection_summaries' AS section,
       task_code,
       status,
       started_at,
       completed_at,
       LEFT(COALESCE(result_summary, failure_reason, ''), 1200) AS summary
FROM aiw_scheduled_task_execution
WHERE task_type = 'AI_STRUCTURED_DATA_COLLECTION'
ORDER BY started_at DESC
LIMIT 20;

SELECT 'llm_discovery_summaries' AS section,
       task_code,
       status,
       started_at,
       completed_at,
       LEFT(COALESCE(result_summary, failure_reason, ''), 1200) AS summary
FROM aiw_scheduled_task_execution
WHERE task_type = 'AI_DATA_SOURCE_DISCOVERY'
ORDER BY started_at DESC
LIMIT 20;

SELECT 'active_task_definitions' AS section,
       task_code,
       task_type,
       cron,
       enabled,
       LEFT(CAST(parameters AS CHAR), 1200) AS parameters
FROM aiw_investment_task_definition
WHERE enabled = 1
ORDER BY task_type, task_code;
