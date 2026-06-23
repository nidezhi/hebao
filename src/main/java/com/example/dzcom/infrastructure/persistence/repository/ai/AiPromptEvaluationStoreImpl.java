package com.example.dzcom.infrastructure.persistence.repository.ai;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.ai.AiPromptEvaluation;
import com.example.dzcom.domain.repository.ai.AiPromptEvaluationSearchCriteria;
import com.example.dzcom.domain.repository.ai.AiPromptEvaluationStore;
import com.example.dzcom.infrastructure.persistence.entity.ai.AiPromptEvaluationEntity;
import com.example.dzcom.infrastructure.persistence.mapper.ai.AiPromptEvaluationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** AI Prompt 评估仓储实现。 */
@Repository
@RequiredArgsConstructor
public class AiPromptEvaluationStoreImpl implements AiPromptEvaluationStore {
    private final AiPromptEvaluationMapper mapper;

    /** 保存 Prompt 评估。 */
    @Override
    public AiPromptEvaluation save(AiPromptEvaluation evaluation) {
        mapper.insert(toEntity(evaluation));
        return evaluation;
    }

    /** 按业务 ID 查询 Prompt 评估。 */
    @Override
    public Optional<AiPromptEvaluation> findByBizId(String bizId) {
        return Optional.ofNullable(mapper.selectByBizId(bizId)).map(this::toDomain);
    }

    /** 分页查询 Prompt 评估。 */
    @Override
    public PageResult<AiPromptEvaluation> search(AiPromptEvaluationSearchCriteria criteria) {
        int offset = (criteria.page() - 1) * criteria.size();
        List<AiPromptEvaluation> items = mapper.search(criteria, offset, resolveSortColumn(criteria.sort()))
            .stream()
            .map(this::toDomain)
            .toList();
        long total = mapper.count(criteria);
        return PageResult.<AiPromptEvaluation>builder()
            .items(items)
            .total(total)
            .page(criteria.page())
            .size(criteria.size())
            .totalPages((int) Math.ceil((double) total / criteria.size()))
            .build();
    }

    /** 将接口排序字段转换为固定数据库列。 */
    private String resolveSortColumn(String sort) {
        return switch (sort) {
            case "promptCode" -> "e.prompt_code";
            case "promptVersion" -> "e.prompt_version";
            case "scenario" -> "e.scenario";
            case "score" -> "e.score";
            case "reviewStatus" -> "e.review_status";
            default -> "e.evaluated_at";
        };
    }

    /** 将领域对象转换为持久化实体。 */
    private AiPromptEvaluationEntity toEntity(AiPromptEvaluation evaluation) {
        return AiPromptEvaluationEntity.builder()
            .bizId(evaluation.bizId())
            .promptBizId(evaluation.promptBizId())
            .promptCode(evaluation.promptCode())
            .promptVersion(evaluation.promptVersion())
            .scenario(evaluation.scenario())
            .backtestBizId(evaluation.backtestBizId())
            .feedbackBizId(evaluation.feedbackBizId())
            .score(evaluation.score())
            .scoreDetail(evaluation.scoreDetail())
            .reviewStatus(evaluation.reviewStatus())
            .evaluatorType(evaluation.evaluatorType())
            .evaluatorBizId(evaluation.evaluatorBizId())
            .evaluatedAt(evaluation.evaluatedAt())
            .createdAt(evaluation.createdAt())
            .build();
    }

    /** 将持久化实体转换为领域对象。 */
    private AiPromptEvaluation toDomain(AiPromptEvaluationEntity entity) {
        return AiPromptEvaluation.builder()
            .bizId(entity.getBizId())
            .promptBizId(entity.getPromptBizId())
            .promptCode(entity.getPromptCode())
            .promptVersion(entity.getPromptVersion())
            .scenario(entity.getScenario())
            .backtestBizId(entity.getBacktestBizId())
            .feedbackBizId(entity.getFeedbackBizId())
            .score(entity.getScore())
            .scoreDetail(entity.getScoreDetail())
            .reviewStatus(entity.getReviewStatus())
            .evaluatorType(entity.getEvaluatorType())
            .evaluatorBizId(entity.getEvaluatorBizId())
            .evaluatedAt(entity.getEvaluatedAt())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
