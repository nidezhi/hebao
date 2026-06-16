package com.example.dzcom.application.service.task;

/** 投资任务触发事件发布端口。 */
public interface InvestmentTaskTriggerPort {
    /** 发布一次投资任务触发事件并返回事件 ID。 */
    String publish(InvestmentTaskEvent event);
}
