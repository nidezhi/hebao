package com.example.dzcom.interfaces.request.account;

import com.example.dzcom.domain.enums.account.AccountStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/** 管理端账户状态变更请求。 */
@Builder
public record StatusRequest(
    @NotBlank String bizId,
    @NotNull AccountStatus status
) {
}
