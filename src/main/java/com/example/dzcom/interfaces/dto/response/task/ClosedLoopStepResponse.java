package com.example.dzcom.interfaces.dto.response.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.dzcom.application.dto.task.ClosedLoopStepView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** 自动投资闭环步骤响应。 */
@Builder
@Schema(description = "自动投资闭环步骤响应")
public record ClosedLoopStepResponse(
    @Schema(description = "步骤业务唯一标识") String bizId,
    @Schema(description = "步骤编码") String stepCode,
    @Schema(description = "步骤名称") String stepName,
    @Schema(description = "步骤顺序") int stepOrder,
    @Schema(description = "步骤状态：SUCCEEDED/SKIPPED/BLOCKED/FAILED") String stepStatus,
    @Schema(description = "输入摘要 JSON") String inputSummary,
    @Schema(description = "输出摘要 JSON") String outputSummary,
    @Schema(description = "展示级别：INFO/WARN/ERROR") String displaySeverity,
    @Schema(description = "是否阻断闭环") boolean blocking,
    @Schema(description = "是否建议作为前端重点提示") boolean userFacing,
    @Schema(description = "前端可直接展示的摘要文案") String displayMessage,
    @Schema(description = "失败或阻断原因") String failureReason,
    @Schema(description = "开始时间") LocalDateTime startedAt,
    @Schema(description = "完成时间") LocalDateTime completedAt
) {
    /** 将应用层视图转换为接口响应。 */
    public static ClosedLoopStepResponse from(ClosedLoopStepView view) {
        JSONObject input = parseObject(view.inputSummary());
        JSONObject output = parseObject(view.outputSummary());
        String severity = firstText(output, input, "displaySeverity");
        boolean blocking = firstBoolean(output, input, "blocking", isBlockingStatus(view.stepStatus()));
        boolean userFacing = firstBoolean(output, input, "userFacing", defaultUserFacing(view.stepCode(), view.stepStatus()));
        return ClosedLoopStepResponse.builder()
            .bizId(view.bizId())
            .stepCode(view.stepCode())
            .stepName(view.stepName())
            .stepOrder(view.stepOrder())
            .stepStatus(view.stepStatus())
            .inputSummary(view.inputSummary())
            .outputSummary(view.outputSummary())
            .displaySeverity(severity == null || severity.isBlank() ? defaultSeverity(view.stepStatus()) : severity)
            .blocking(blocking)
            .userFacing(userFacing)
            .displayMessage(displayMessage(view, output))
            .failureReason(view.failureReason())
            .startedAt(view.startedAt())
            .completedAt(view.completedAt())
            .build();
    }

    /** 尽量解析摘要 JSON，解析失败时保持兼容。 */
    private static JSONObject parseObject(String value) {
        if (value == null || value.isBlank()) {
            return new JSONObject();
        }
        try {
            return JSON.parseObject(value);
        } catch (Exception exception) {
            return new JSONObject();
        }
    }

    /** 按输出优先、输入兜底读取文本字段。 */
    private static String firstText(JSONObject output, JSONObject input, String key) {
        String value = output.getString(key);
        return value == null || value.isBlank() ? input.getString(key) : value;
    }

    /** 按输出优先、输入兜底读取布尔字段。 */
    private static boolean firstBoolean(JSONObject output, JSONObject input, String key, boolean defaultValue) {
        Boolean outputValue = output.getBoolean(key);
        if (outputValue != null) {
            return outputValue;
        }
        Boolean inputValue = input.getBoolean(key);
        return inputValue == null ? defaultValue : inputValue;
    }

    /** 阻断类步骤默认需要前端提示。 */
    private static boolean isBlockingStatus(String status) {
        return "BLOCKED".equals(status) || "FAILED".equals(status);
    }

    /** 安全边界是后台审计，不作为驾驶舱主告警。 */
    private static boolean defaultUserFacing(String stepCode, String status) {
        if ("SAFETY_GUARD".equals(stepCode)) {
            return false;
        }
        return isBlockingStatus(status);
    }

    /** 根据步骤状态给展示级别兜底。 */
    private static String defaultSeverity(String status) {
        if ("FAILED".equals(status) || "BLOCKED".equals(status)) {
            return "ERROR";
        }
        if ("SKIPPED".equals(status)) {
            return "WARN";
        }
        return "INFO";
    }

    /** 优先返回模型/闭环给出的摘要，其次返回失败原因。 */
    private static String displayMessage(ClosedLoopStepView view, JSONObject output) {
        String summary = output.getString("summary");
        if (summary != null && !summary.isBlank()) {
            return summary;
        }
        if (view.failureReason() != null && !view.failureReason().isBlank()) {
            return view.failureReason();
        }
        String effectivePolicy = output.getString("effectivePolicy");
        if (effectivePolicy != null && !effectivePolicy.isBlank()) {
            return effectivePolicy;
        }
        return view.stepName();
    }
}
