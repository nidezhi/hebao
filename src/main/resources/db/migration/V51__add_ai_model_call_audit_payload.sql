ALTER TABLE aiw_ai_model_call_audit
    ADD COLUMN request_payload LONGTEXT NULL COMMENT '脱敏后的完整输入内容' AFTER response_preview,
    ADD COLUMN response_payload LONGTEXT NULL COMMENT '脱敏后的完整输出内容' AFTER request_payload;
