-- ============================================================================
-- V38 对齐启用的模型 Skill 绑定到当前 ACTIVE 模型版本
-- 说明：
--   首次前后端联调发现 aiw_ai_model_skill_binding 中部分启用绑定仍指向旧 mock-v1，
--   而运行时场景模型挂靠按 model_code 解析 ACTIVE 模型。这里把治理展示和运行事实对齐，
--   避免前端模型 Skill 页面出现“可用绑定”但实际版本漂移的误导。
-- ============================================================================

UPDATE aiw_ai_model_skill_binding b
JOIN aiw_ai_model m
  ON m.model_code = b.model_code
 AND m.status = 'ACTIVE'
SET b.model_biz_id = m.biz_id,
    b.model_version = m.model_version,
    b.updated_at = CURRENT_TIMESTAMP(3)
WHERE b.enabled = 1
  AND NOT (b.model_biz_id <=> m.biz_id AND b.model_version <=> m.model_version);
