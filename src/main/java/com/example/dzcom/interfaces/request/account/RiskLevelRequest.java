package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

/** 管理端风险承受等级变更请求，允许值为 1 至 5。 */
@Schema(description = "管理端风险等级变更请求（1-5）")
@Builder
public record RiskLevelRequest(
    @Schema(description = "用户业务标识", example = "usr_01Hxxx") @NotBlank String bizId,
    @Schema(description = "风险等级（1-5）") @Min(1) @Max(5) int riskLevel
) {
}
