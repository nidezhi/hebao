package com.example.dzcom.interfaces.dto.response.task;

import com.example.dzcom.application.dto.task.InvestmentTaskDefinitionView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Map;

/** 投资任务配置响应。 */
@Builder
@Schema(description = "投资任务配置响应")
public record InvestmentTaskDefinitionResponse(
    @Schema(description = "任务编码", example = "investment-news-collection")
    String code,
    @Schema(description = "任务类型", example = "INVESTMENT_NEWS_COLLECTION")
    String type,
    @Schema(description = "Spring Cron 表达式", example = "0 0/30 * * * ?")
    String cron,
    @Schema(description = "Cron 时区", example = "Asia/Shanghai")
    String zone,
    @Schema(description = "是否启用", example = "true")
    boolean enabled,
    @Schema(description = "任务参数；由不同任务处理器解释")
    Map<String, String> parameters
) {
    /** 将应用层配置视图转换为接口响应。 */
    public static InvestmentTaskDefinitionResponse from(InvestmentTaskDefinitionView view) {
        return InvestmentTaskDefinitionResponse.builder()
            .code(view.code())
            .type(view.type())
            .cron(view.cron())
            .zone(view.zone())
            .enabled(view.enabled())
            .parameters(view.parameters())
            .build();
    }
}
