package com.example.dzcom.infrastructure.persistence.repository.risk;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.risk.RiskCheck;
import com.example.dzcom.domain.repository.risk.RiskCheckSearchCriteria;
import com.example.dzcom.domain.repository.risk.RiskCheckStore;
import com.example.dzcom.infrastructure.persistence.entity.risk.RiskCheckEntity;
import com.example.dzcom.infrastructure.persistence.mapper.risk.RiskCheckMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/** 风险检查结果仓储实现。 */
@Repository
@RequiredArgsConstructor
public class RiskCheckStoreImpl implements RiskCheckStore {
    private final RiskCheckMapper mapper;

    /** 保存风险检查结果。 */
    @Override
    public RiskCheck save(RiskCheck riskCheck) {
        mapper.insert(toEntity(riskCheck));
        return riskCheck;
    }

    /** 分页查询风险检查结果。 */
    @Override
    public PageResult<RiskCheck> search(RiskCheckSearchCriteria criteria) {
        int offset = (criteria.page() - 1) * criteria.size();
        List<RiskCheck> items = mapper.search(criteria, offset, resolveSortColumn(criteria.sort())).stream()
            .map(this::toDomain)
            .toList();
        long total = mapper.count(criteria);
        return PageResult.<RiskCheck>builder()
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
            case "businessType" -> "r.business_type";
            case "checkResult" -> "r.check_result";
            case "riskLevel" -> "r.risk_level";
            case "reasonCode" -> "r.reason_code";
            default -> "r.checked_at";
        };
    }

    /** 将领域对象转换为持久化实体。 */
    private RiskCheckEntity toEntity(RiskCheck riskCheck) {
        return RiskCheckEntity.builder()
            .bizId(riskCheck.bizId())
            .traceId(riskCheck.traceId())
            .businessType(riskCheck.businessType())
            .businessBizId(riskCheck.businessBizId())
            .userBizId(riskCheck.userBizId())
            .ruleCode(riskCheck.ruleCode())
            .ruleVersion(riskCheck.ruleVersion())
            .checkResult(riskCheck.checkResult())
            .riskLevel(riskCheck.riskLevel())
            .score(riskCheck.score())
            .reasonCode(riskCheck.reasonCode())
            .detail(riskCheck.detail())
            .checkedAt(riskCheck.checkedAt())
            .createdAt(riskCheck.createdAt())
            .build();
    }

    /** 将持久化实体转换为领域对象。 */
    private RiskCheck toDomain(RiskCheckEntity entity) {
        return RiskCheck.builder()
            .bizId(entity.getBizId())
            .traceId(entity.getTraceId())
            .businessType(entity.getBusinessType())
            .businessBizId(entity.getBusinessBizId())
            .userBizId(entity.getUserBizId())
            .ruleCode(entity.getRuleCode())
            .ruleVersion(entity.getRuleVersion())
            .checkResult(entity.getCheckResult())
            .riskLevel(entity.getRiskLevel())
            .score(entity.getScore())
            .reasonCode(entity.getReasonCode())
            .detail(entity.getDetail())
            .checkedAt(entity.getCheckedAt())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
