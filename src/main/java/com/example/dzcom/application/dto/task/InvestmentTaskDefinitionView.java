package com.example.dzcom.application.dto.task;

import lombok.Builder;

import java.util.Map;

/** 投资任务配置视图。 */
@Builder
public record InvestmentTaskDefinitionView(
    String code,
    String type,
    String cron,
    String zone,
    boolean enabled,
    Map<String, String> parameters
) {
}
