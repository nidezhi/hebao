package com.example.dzcom.application.service.task;

/**
 * 投资任务被业务门禁阻断的异常。
 *
 * <p>该异常表示质量、风控、配置或自动化安全边界正常拦截，不代表系统故障。
 * 执行审计应记录为 BLOCKED，日志也应按业务阻断处理，避免定时任务和 Kafka
 * 消费日志被可预期的门禁结果刷成错误堆栈。</p>
 */
public class InvestmentTaskBlockedException extends RuntimeException {
    /**
     * 创建投资任务业务阻断异常。
     *
     * @param message 可展示给前端和执行审计的阻断原因
     * @author dz
     * @date 2026-06-26
     */
    public InvestmentTaskBlockedException(String message) {
        super(message);
    }
}
