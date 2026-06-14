package com.example.dzcom.infrastructure.config.account;

import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.domain.enums.account.IdentityType;
import com.example.dzcom.domain.service.account.IdentityNormalizer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class DefaultIdentityNormalizer implements IdentityNormalizer {
    /**
     * 执行 detect type 处理。
     *
     * @param account account 参数
     * @return 方法执行后的结果
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
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

    /**
     * 规范化输入值并返回统一格式。
     *
     * @param type 数据类型
     * @param value 待处理的数据值
     * @return 方法执行后的结果
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
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
