package com.example.dzcom.domain.service.account;

import com.example.dzcom.domain.enums.account.IdentityType;

public interface IdentityNormalizer {
    IdentityType detectType(String account);

    String normalize(IdentityType type, String value);
}
