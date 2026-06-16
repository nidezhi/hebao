package com.example.dzcom.interfaces.dto.response.task;

import com.example.dzcom.application.dto.task.InvestmentTaskTriggerResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** 投资任务触发响应。 */
@Builder
@Schema(description = "投资任务触发响应")
public record InvestmentTaskTriggerResponse(
    @Schema(description = "Kafka 事件 ID，也是消费幂等键")
    String eventId,
    @Schema(description = "任务编码", example = "hot-theme-return")
    String taskCode,
    @Schema(description = "任务类型", example = "HOT_THEME_RETURN")
    String taskType,
    @Schema(description = "触发来源", example = "MANUAL")
    String triggerSource,
    @Schema(description = "触发时间", example = "2026-06-16T10:00:00")
    LocalDateTime triggeredAt
) {
    /** 将应用层触发结果转换为接口响应。 */
    public static InvestmentTaskTriggerResponse from(InvestmentTaskTriggerResult result) {
        return InvestmentTaskTriggerResponse.builder()
            .eventId(result.eventId())
            .taskCode(result.taskCode())
            .taskType(result.taskType())
            .triggerSource(result.triggerSource())
            .triggeredAt(result.triggeredAt())
            .build();
    }
}
