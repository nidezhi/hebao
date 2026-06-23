package com.example.dzcom.domain.repository.ai;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.ai.AiPromptEvaluation;

import java.util.Optional;

/** AI Prompt 评估仓储端口。 */
public interface AiPromptEvaluationStore {
    /** 保存 Prompt 评估。 */
    AiPromptEvaluation save(AiPromptEvaluation evaluation);

    /** 按业务 ID 查询 Prompt 评估。 */
    Optional<AiPromptEvaluation> findByBizId(String bizId);

    /** 分页查询 Prompt 评估。 */
    PageResult<AiPromptEvaluation> search(AiPromptEvaluationSearchCriteria criteria);
}
