-- ============================================================
-- V10 初始化通用 AI 模型配置
-- 1. 模型表只保存普通连接参数和 secretRef，不保存 API Key 明文。
-- 2. UUID 格式模拟 Key 由 application-local/dev.yaml 或环境变量提供。
-- 3. OpenAI 兼容模型当前启用 mockEnabled，不发起真实外部请求。
-- ============================================================

INSERT INTO aiw_ai_model
(biz_id, model_code, model_version, model_name, model_type, provider, artifact_uri,
 model_config, metrics, status, activated_at, retired_at, created_at, updated_at)
VALUES
('10000000-0000-0000-0000-000000000001',
 'local-rule-analysis', 'v1', '本地规则投资分析模型', 'ANALYSIS', 'LOCAL_RULE',
 'classpath:local-rule-v1',
 JSON_OBJECT(
     'model', 'local-rule-v1',
     'timeoutSeconds', 30,
     'temperature', 0,
     'mockEnabled', false
 ),
 JSON_OBJECT('configurationType', 'LOCAL_RULE', 'initializedBy', 'V10'),
 'ACTIVE', CURRENT_TIMESTAMP(3), NULL, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),

('10000000-0000-0000-0000-000000000002',
 'openai-compatible-analysis', 'mock-v1', 'OpenAI兼容投资分析模型', 'ANALYSIS',
 'OPENAI_COMPATIBLE', 'https://api.openai.com/v1',
 JSON_OBJECT(
     'baseUrl', 'https://api.openai.com/v1',
     'model', 'gpt-4.1-mini',
     'secretRef', 'OPENAI_MOCK_API_KEY',
     'timeoutSeconds', 60,
     'temperature', 0.2,
     'mockEnabled', true
 ),
 JSON_OBJECT('configurationType', 'MOCK', 'protocol', 'OPENAI_COMPATIBLE'),
 'ACTIVE', CURRENT_TIMESTAMP(3), NULL, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),

('10000000-0000-0000-0000-000000000003',
 'deepseek-compatible-analysis', 'mock-v1', 'DeepSeek兼容投资分析模型', 'ANALYSIS',
 'OPENAI_COMPATIBLE', 'https://api.deepseek.com/v1',
 JSON_OBJECT(
     'baseUrl', 'https://api.deepseek.com/v1',
     'model', 'deepseek-chat',
     'secretRef', 'DEEPSEEK_MOCK_API_KEY',
     'timeoutSeconds', 60,
     'temperature', 0.2,
     'mockEnabled', true
 ),
 JSON_OBJECT('configurationType', 'MOCK', 'protocol', 'OPENAI_COMPATIBLE'),
 'ACTIVE', CURRENT_TIMESTAMP(3), NULL, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3)),

('10000000-0000-0000-0000-000000000004',
 'qwen-compatible-analysis', 'mock-v1', '通义千问兼容投资分析模型', 'ANALYSIS',
 'OPENAI_COMPATIBLE', 'https://dashscope.aliyuncs.com/compatible-mode/v1',
 JSON_OBJECT(
     'baseUrl', 'https://dashscope.aliyuncs.com/compatible-mode/v1',
     'model', 'qwen-plus',
     'secretRef', 'QWEN_MOCK_API_KEY',
     'timeoutSeconds', 60,
     'temperature', 0.2,
     'mockEnabled', true
 ),
 JSON_OBJECT('configurationType', 'MOCK', 'protocol', 'OPENAI_COMPATIBLE'),
 'ACTIVE', CURRENT_TIMESTAMP(3), NULL, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3))
ON DUPLICATE KEY UPDATE
model_name = VALUES(model_name),
model_type = VALUES(model_type),
provider = VALUES(provider),
artifact_uri = VALUES(artifact_uri),
model_config = VALUES(model_config),
metrics = VALUES(metrics),
updated_at = CURRENT_TIMESTAMP(3);
