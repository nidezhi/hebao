package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;

/** 管理端风险承受等级变更请求，允许值为 1 至 5。 */
@Builder
public record RiskLevelRequest(@Min(1) @Max(5) int riskLevel) {
}
