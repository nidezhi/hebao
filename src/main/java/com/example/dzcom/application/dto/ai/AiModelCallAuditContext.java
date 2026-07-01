package com.example.dzcom.application.dto.ai;

import lombok.Builder;

import java.util.Map;

/**
 * 模型调用关联的业务上下文。
 *
 * <p>调用方应尽量填充业务类型、展示名称、Prompt、Skill、任务和闭环运行信息。
 * {@code inputSummary} 仅用于保存可扩展摘要，不应替代上层稳定字段。</p>
 */
@Builder(toBuilder = true)
public record AiModelCallAuditContext(
    String businessType,
    String businessBizId,
    String businessLabel,
    String taskCode,
    String eventId,
    String runBizId,
    String runNo,
    String reportBizId,
    String promptBizId,
    String promptCode,
    String promptVersion,
    String skillBizId,
    String skillCode,
    String skillVersion,
    String modelSkillBindingBizId,
    String scenarioCode,
    String environment,
    Map<String, Object> inputSummary
) {
    /**
     * 构造空审计上下文。
     *
     * @return 不带业务关联的空上下文，用于兼容尚未接入业务归因的调用点
     */
    public static AiModelCallAuditContext empty() {
        return AiModelCallAuditContext.builder().build();
    }
}
