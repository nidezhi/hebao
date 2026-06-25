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

    /**
     * 在指定操作者上下文中执行业务逻辑，并在结束后恢复原上下文。
     *
     * @param operator 临时操作者
     * @param action 待执行逻辑
     * @param <T> 返回值类型
     * @return 执行结果
     * @author dz
     * @date 2026-06-25
     */
    @Override
    public <T> T callAs(CurrentOperator operator, java.util.function.Supplier<T> action) {
        CurrentOperator previous = HOLDER.get();
        HOLDER.set(operator);
        try {
            return action.get();
        } finally {
            if (previous == null) {
                HOLDER.remove();
            } else {
                HOLDER.set(previous);
            }
        }
    }
}
