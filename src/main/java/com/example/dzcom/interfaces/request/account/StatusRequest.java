package com.example.dzcom.interfaces.request.account;

import com.example.dzcom.domain.enums.account.AccountStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

/** 管理端账户状态变更请求。 */
@Schema(description = "管理端账户状态变更请求")
@Builder
public record StatusRequest(
    @Schema(description = "用户业务标识", example = "usr_01Hxxx") @NotBlank String bizId,
    @Schema(description = "目标账户状态") @NotNull AccountStatus status
) {
}
