package com.example.dzcom.interfaces.request.account;

import com.example.dzcom.domain.enums.account.KycStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

/** 管理端 KYC 状态变更请求。 */
@Schema(description = "管理端 KYC 状态变更请求")
@Builder
public record KycStatusRequest(
    @Schema(description = "用户业务标识", example = "usr_01Hxxx") @NotBlank String bizId,
    @Schema(description = "目标 KYC 状态") @NotNull KycStatus kycStatus
) {
}
