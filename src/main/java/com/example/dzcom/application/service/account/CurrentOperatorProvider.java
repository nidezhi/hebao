package com.example.dzcom.application.service.account;

public interface CurrentOperatorProvider {
    /**
     * 获取必需的业务对象，不存在时终止当前流程。
     *
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    CurrentOperator required();

    /**
     * 在指定系统操作者上下文中执行业务逻辑。
     *
     * <p>默认实现用于不支持上下文切换的 Provider。基础设施实现会绑定
     * ThreadLocal，使定时任务可以在明确的模拟用户身份下执行 Mock 闭环。</p>
     *
     * @param operator 系统指定操作者
     * @param action 待执行逻辑
     * @param <T> 返回值类型
     * @return 执行结果
     * @author dz
     * @date 2026-06-25
     */
    default <T> T callAs(CurrentOperator operator, java.util.function.Supplier<T> action) {
        return action.get();
    }
}
