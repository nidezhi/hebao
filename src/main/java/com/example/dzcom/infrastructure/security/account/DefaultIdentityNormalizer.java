package com.example.dzcom.infrastructure.security.account;

import com.example.dzcom.common.exception.BusinessException;
import com.example.dzcom.domain.enums.account.IdentityType;
import com.example.dzcom.domain.service.account.IdentityNormalizer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class DefaultIdentityNormalizer implements IdentityNormalizer {
    @Override
    public IdentityType detectType(String account) {
        if (account == null || account.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "账号不能为空");
        }
        String value = account.trim();
        if (value.contains("@")) {
            return IdentityType.EMAIL;
        }
        if (value.matches("^\\+?[0-9]{7,15}$")) {
            return IdentityType.PHONE;
        }
        return IdentityType.USERNAME;
    }

    @Override
    public String normalize(IdentityType type, String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, type + " 不能为空");
        }
        String normalized = value.trim();
        return switch (type) {
            case EMAIL -> normalized.toLowerCase(Locale.ROOT);
            case PHONE -> normalized.startsWith("+") ? normalized : "+" + normalized;
            case USERNAME -> normalized.toLowerCase(Locale.ROOT);
        };
    }
}
