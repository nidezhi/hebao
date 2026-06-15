package com.example.dzcom.application.service.task;

/** 单一投资任务处理器契约。 */
public interface InvestmentTaskHandler {
    /** 判断当前处理器是否支持指定稳定任务类型。 */
    boolean supports(String taskType);

    /** 执行任务并返回可审计结果摘要。 */
    String execute(InvestmentTaskEvent event);
}
