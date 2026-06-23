package com.example.dzcom.infrastructure.persistence.repository.ai;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.ai.InvestmentFeedback;
import com.example.dzcom.domain.repository.ai.InvestmentFeedbackSearchCriteria;
import com.example.dzcom.domain.repository.ai.InvestmentFeedbackStore;
import com.example.dzcom.infrastructure.persistence.entity.ai.InvestmentFeedbackEntity;
import com.example.dzcom.infrastructure.persistence.mapper.ai.InvestmentFeedbackMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** 投资闭环反馈仓储实现。 */
@Repository
@RequiredArgsConstructor
public class InvestmentFeedbackStoreImpl implements InvestmentFeedbackStore {
    private final InvestmentFeedbackMapper mapper;

    /** 保存投资反馈。 */
    @Override
    public InvestmentFeedback save(InvestmentFeedback feedback) {
        mapper.insert(toEntity(feedback));
        return feedback;
    }

    /** 按业务 ID 查询投资反馈。 */
    @Override
    public Optional<InvestmentFeedback> findByBizId(String bizId) {
        return Optional.ofNullable(mapper.selectByBizId(bizId)).map(this::toDomain);
    }

    /** 分页查询投资反馈。 */
    @Override
    public PageResult<InvestmentFeedback> search(InvestmentFeedbackSearchCriteria criteria) {
        int offset = (criteria.page() - 1) * criteria.size();
        List<InvestmentFeedback> items = mapper.search(criteria, offset, resolveSortColumn(criteria.sort()))
            .stream()
            .map(this::toDomain)
            .toList();
        long total = mapper.count(criteria);
        return PageResult.<InvestmentFeedback>builder()
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
            case "targetType" -> "f.target_type";
            case "feedbackAction" -> "f.feedback_action";
            case "promptCode" -> "f.prompt_code";
            case "promptVersion" -> "f.prompt_version";
            default -> "f.created_at";
        };
    }

    /** 将领域对象转换为持久化实体。 */
    private InvestmentFeedbackEntity toEntity(InvestmentFeedback feedback) {
        return InvestmentFeedbackEntity.builder()
            .bizId(feedback.bizId())
            .userBizId(feedback.userBizId())
            .targetType(feedback.targetType())
            .targetBizId(feedback.targetBizId())
            .reportBizId(feedback.reportBizId())
            .promptBizId(feedback.promptBizId())
            .promptCode(feedback.promptCode())
            .promptVersion(feedback.promptVersion())
            .backtestBizId(feedback.backtestBizId())
            .feedbackAction(feedback.feedbackAction())
            .reasonCode(feedback.reasonCode())
            .commentText(feedback.commentText())
            .metadata(feedback.metadata())
            .createdAt(feedback.createdAt())
            .build();
    }

    /** 将持久化实体转换为领域对象。 */
    private InvestmentFeedback toDomain(InvestmentFeedbackEntity entity) {
        return InvestmentFeedback.builder()
            .bizId(entity.getBizId())
            .userBizId(entity.getUserBizId())
            .targetType(entity.getTargetType())
            .targetBizId(entity.getTargetBizId())
            .reportBizId(entity.getReportBizId())
            .promptBizId(entity.getPromptBizId())
            .promptCode(entity.getPromptCode())
            .promptVersion(entity.getPromptVersion())
            .backtestBizId(entity.getBacktestBizId())
            .feedbackAction(entity.getFeedbackAction())
            .reasonCode(entity.getReasonCode())
            .commentText(entity.getCommentText())
            .metadata(entity.getMetadata())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
