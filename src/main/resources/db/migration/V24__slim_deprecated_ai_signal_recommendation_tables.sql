-- ============================================================================
-- V24 数据库瘦身：退役旧 AI 信号与建议占位表
-- 设计说明：
--   1. aiw_ai_signal / aiw_ai_recommendation 是早期“信号->建议”占位模型。
--   2. 当前核心闭环已经由 aiw_investment_analysis_report、Prompt、Mock交易、
--      aiw_backtest_result、aiw_investment_feedback 和风控审计承载。
--   3. 代码层没有仓储、实体、Mapper 或接口引用这两张表，保留只会造成前端和数据库理解偏差。
-- ============================================================================

DROP TABLE IF EXISTS aiw_ai_recommendation;
DROP TABLE IF EXISTS aiw_ai_signal;
