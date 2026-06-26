-- 修复 V17 早期默认模型密钥引用不一致的问题。
-- 本地/开发默认模型的配置文件使用 OPENAI_MOCK_API_KEY；真实生产 Key 可由前端另建模型版本或修改 secretRef 后同步配置。
UPDATE aiw_ai_model
SET model_config = JSON_SET(model_config, '$.secretRef', 'OPENAI_MOCK_API_KEY'),
    updated_at = CURRENT_TIMESTAMP(3)
WHERE model_code = 'openai-compatible-analysis'
  AND JSON_UNQUOTE(JSON_EXTRACT(model_config, '$.secretRef')) = 'OPENAI_API_KEY';
