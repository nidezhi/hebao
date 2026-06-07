package com.example.dzcom.domain.repository.account;

import com.example.dzcom.domain.enums.account.AccountStatus;
import com.example.dzcom.domain.enums.account.KycStatus;

public record UserSearchCriteria(
    String keyword,
    AccountStatus status,
    KycStatus kycStatus,
    Integer riskLevel,
    int page,
    int size,
    String sort,
    boolean ascending
) {
}
