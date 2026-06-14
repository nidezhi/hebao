package com.example.dzcom.interfaces.request.account;

import com.example.dzcom.domain.enums.account.AccountStatus;
import com.example.dzcom.domain.enums.account.KycStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 管理端用户分页查询请求，筛选条件和分页参数统一从请求体接收。
 */
public record AdminUserListRequest(
    String keyword,
    AccountStatus status,
    KycStatus kycStatus,
    @Min(1) @Max(5) Integer riskLevel,
    @Min(0) Integer page,
    @Min(1) @Max(100) Integer size,
    String sort,
    String direction
) {
}
