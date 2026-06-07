package com.example.dzcom.interfaces.request.account;

import com.example.dzcom.domain.enums.account.KycStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/** 管理端 KYC 状态变更请求。 */
@Builder
public record KycStatusRequest(@NotNull KycStatus kycStatus) {
}
