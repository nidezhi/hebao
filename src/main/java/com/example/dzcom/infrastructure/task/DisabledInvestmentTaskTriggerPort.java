package com.example.dzcom.infrastructure.task;

import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.service.task.InvestmentTaskEvent;
import com.example.dzcom.application.service.task.InvestmentTaskTriggerPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** 投资任务禁用时的触发端口实现。 */
@Component
@ConditionalOnProperty(prefix = "investment.tasks", name = "enabled", havingValue = "false", matchIfMissing = true)
public class DisabledInvestmentTaskTriggerPort implements InvestmentTaskTriggerPort {
    /** 任务未启用时拒绝发布触发事件。 */
    @Override
    public String publish(InvestmentTaskEvent event) {
        throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "投资任务未启用");
    }
}
