package com.example.dzcom.application.service.task;

/** 投资定时任务调度刷新端口。 */
public interface InvestmentTaskScheduleRefreshPort {
    /** 重新加载数据库中的启用任务并刷新 Cron 注册。 */
    void refreshSchedules();
}
