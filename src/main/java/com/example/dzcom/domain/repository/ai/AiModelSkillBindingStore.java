package com.example.dzcom.domain.repository.ai;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.ai.AiModelSkillBinding;

import java.util.List;
import java.util.Optional;

/** AI 模型 Skill 绑定仓储端口。 */
public interface AiModelSkillBindingStore {
    /** 保存模型 Skill 绑定。 */
    AiModelSkillBinding save(AiModelSkillBinding binding);

    /** 根据业务 ID 查询绑定。 */
    Optional<AiModelSkillBinding> findByBizId(String bizId);

    /** 根据模型、Skill 和场景查询绑定。 */
    Optional<AiModelSkillBinding> findByModelSkillAndScenario(
        String modelBizId,
        String skillBizId,
        String scenarioCode
    );

    /** 查询模型下启用的 Skill 绑定。 */
    List<AiModelSkillBinding> findEnabledByModelBizId(String modelBizId);

    /** 分页查询模型 Skill 绑定。 */
    PageResult<AiModelSkillBinding> search(AiModelSkillBindingSearchCriteria criteria);
}
