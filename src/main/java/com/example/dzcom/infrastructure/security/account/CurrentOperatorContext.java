package com.example.dzcom.infrastructure.security.account;

import com.example.dzcom.common.exception.BusinessException;
import com.example.dzcom.application.service.account.CurrentOperator;
import com.example.dzcom.application.service.account.CurrentOperatorProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class CurrentOperatorContext implements CurrentOperatorProvider {
    private static final ThreadLocal<CurrentOperator> HOLDER = new ThreadLocal<>();

    public void set(CurrentOperator operator) {
        HOLDER.set(operator);
    }

    public void clear() {
        HOLDER.remove();
    }

    @Override
    public CurrentOperator required() {
        CurrentOperator operator = HOLDER.get();
        if (operator == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "未登录或登录已过期");
        }
        return operator;
    }
}
