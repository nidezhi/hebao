SET @chat_snapshot_column_exists = (
    SELECT COUNT(1)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'aiw_investment_analysis_report'
      AND column_name = 'chat_snapshot'
);

SET @add_chat_snapshot_sql = IF(
    @chat_snapshot_column_exists = 0,
    'ALTER TABLE aiw_investment_analysis_report ADD COLUMN chat_snapshot JSON NULL COMMENT ''脱敏后的模型对话快照'' AFTER prompt_snapshot',
    'SELECT 1'
);

PREPARE add_chat_snapshot_stmt FROM @add_chat_snapshot_sql;
EXECUTE add_chat_snapshot_stmt;
DEALLOCATE PREPARE add_chat_snapshot_stmt;
