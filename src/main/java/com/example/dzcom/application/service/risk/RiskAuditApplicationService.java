package com.example.dzcom.application.service.risk;

import com.example.dzcom.application.common.json.Jsons;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.risk.RiskCheckView;
import com.example.dzcom.domain.model.risk.RiskCheck;
import com.example.dzcom.domain.repository.risk.RiskCheckSearchCriteria;
import com.example.dzcom.domain.repository.risk.RiskCheckStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** 风控审计应用服务，负责保存和查询前端可见的风险检查记录。 */
@Service
@RequiredArgsConstructor
public class RiskAuditApplicationService {
    private static final Set<String> SORTS =
        Set.of("checkedAt", "businessType", "checkResult", "riskLevel", "reasonCode");

    private final RiskCheckStore riskChecks;
    private final IdGenerator ids;
    private final ClockProvider clock;

    /**
     * 以独立事务记录风险拦截，避免被主业务异常回滚。
     *
     * @param userBizId 用户业务标识
     * @param businessType 被检查业务类型
     * @param businessBizId 被检查业务对象标识
     * @param ruleCode 规则编码
     * @param riskLevel 风险等级
     * @param reasonCode 原因编码
     * @param detail 脱敏检查详情
     * @author dz
     * @date 2026-06-23
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordReject(String userBizId, String businessType, String businessBizId,
                             String ruleCode, String riskLevel, String reasonCode,
                             Map<String, Object> detail) {
        LocalDateTime now = clock.now();
        riskChecks.save(RiskCheck.builder()
            .bizId(ids.newBizId())
            .traceId(ids.newBizId())
            .businessType(normalizeUpper(businessType, "UNKNOWN"))
            .businessBizId(normalizeBizId(businessBizId))
            .userBizId(userBizId)
            .ruleCode(normalizeUpper(ruleCode, "UNSPECIFIED_RULE"))
            .ruleVersion(1)
            .checkResult("REJECT")
            .riskLevel(normalizeUpper(riskLevel, "HIGH"))
            .score(BigDecimal.ONE)
            .reasonCode(normalizeUpper(reasonCode, "UNKNOWN_REJECT"))
            .detail(detail == null || detail.isEmpty() ? null : Jsons.toJson(detail))
            .checkedAt(now)
            .createdAt(now)
            .build());
    }

    /**
     * 分页查询风险检查记录。
     *
     * @param businessType 业务类型
     * @param businessBizId 业务对象标识
     * @param userBizId 用户业务标识
     * @param checkResult 检查结论
     * @param riskLevel 风险等级
     * @param reasonCode 原因编码
     * @param query 分页排序参数
     * @return 风险检查分页视图
     * @author dz
     * @date 2026-06-23
     */
    @Transactional(readOnly = true)
    public PageResult<RiskCheckView> list(String businessType, String businessBizId, String userBizId,
                                          String checkResult, String riskLevel, String reasonCode,
                                          PageQuery query) {
        PageResult<RiskCheck> page = riskChecks.search(new RiskCheckSearchCriteria(
            trimToNull(businessType),
            trimToNull(businessBizId),
            trimToNull(userBizId),
            trimToNull(checkResult),
            trimToNull(riskLevel),
            trimToNull(reasonCode),
            query.page(),
            query.size(),
            query.safeSort(SORTS, "checkedAt"),
            "asc".equals(query.direction())
        ));
        return PageResult.<RiskCheckView>builder()
            .items(page.items().stream().map(this::toView).toList())
            .total(page.total())
            .page(page.page())
            .size(page.size())
            .totalPages(page.totalPages())
            .build();
    }

    /** 将领域对象转换为应用层视图。 */
    private RiskCheckView toView(RiskCheck riskCheck) {
        return RiskCheckView.builder()
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
            .build();
    }

    /** 规范化可为空的业务标识。 */
    private String normalizeBizId(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? "00000000-0000-0000-0000-000000000000" : trimmed;
    }

    /** 转为大写编码。 */
    private String normalizeUpper(String value, String defaultValue) {
        String trimmed = trimToNull(value);
        return (trimmed == null ? defaultValue : trimmed).toUpperCase(Locale.ROOT);
    }

    /** 去除首尾空白，空字符串返回 null。 */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
