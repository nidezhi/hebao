package com.example.dzcom.infrastructure.config.account;

import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.service.account.CurrentOperator;
import com.example.dzcom.application.service.account.CurrentOperatorProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class CurrentOperatorContext implements CurrentOperatorProvider {
    private static final ThreadLocal<CurrentOperator> HOLDER = new ThreadLocal<>();

    /**
     * 执行 set 处理。
     *
     * @param operator 当前操作人标识
     * @author dz
     * @date 2026-06-14
     */
    public void set(CurrentOperator operator) {
        HOLDER.set(operator);
    }

    /**
     * 执行 clear 处理。
     *
     * @author dz
     * @date 2026-06-14
     */
    public void clear() {
        HOLDER.remove();
    }

    /**
     * 获取必需的业务对象，不存在时终止当前流程。
     *
     * @return 查询到的业务数据
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    @Override
    public CurrentOperator required() {
        CurrentOperator operator = HOLDER.get();
        if (operator == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "未登录或登录已过期");
        }
        return operator;
    }
}
