package com.example.dzcom.domain.repository.ai;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.ai.AiSkill;

import java.util.Optional;

/** AI Skill 仓储端口。 */
public interface AiSkillStore {
    /** 保存 Skill 版本。 */
    AiSkill save(AiSkill skill);

    /** 根据业务 ID 查询 Skill。 */
    Optional<AiSkill> findByBizId(String bizId);

    /** 根据编码和版本查询 Skill。 */
    Optional<AiSkill> findByCodeAndVersion(String skillCode, String skillVersion);

    /** 查询指定编码最近启用的 Skill 版本。 */
    Optional<AiSkill> findActiveByCode(String skillCode);

    /** 分页查询 Skill。 */
    PageResult<AiSkill> search(AiSkillSearchCriteria criteria);
}
