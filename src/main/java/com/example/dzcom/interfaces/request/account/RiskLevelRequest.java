package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/** 管理端风险承受等级变更请求，允许值为 1 至 5。 */
@Builder
public record RiskLevelRequest(
    @NotBlank String bizId,
    @Min(1) @Max(5) int riskLevel
) {
}
