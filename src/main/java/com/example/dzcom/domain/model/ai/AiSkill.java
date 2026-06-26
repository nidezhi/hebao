package com.example.dzcom.domain.model.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** AI Skill 版本化领域对象，用于沉淀数据源发现、Prompt 治理等可复盘的大模型能力说明。 */
@Builder(toBuilder = true)
@Schema(description = "AI Skill 版本化领域对象")
public record AiSkill(
    @Schema(description = "Skill 业务唯一标识")
    String bizId,
    @Schema(description = "跨版本稳定 Skill 编码")
    String skillCode,
    @Schema(description = "Skill 版本号")
    String skillVersion,
    @Schema(description = "Skill 展示名称")
    String skillName,
    @Schema(description = "Skill 类型：DATA_SOURCE_DISCOVERY/PROMPT_GOVERNANCE等")
    String skillType,
    @Schema(description = "生命周期状态：DRAFT/VALIDATING/ACTIVE/RETIRED/ARCHIVED")
    String status,
    @Schema(description = "给大模型的 Skill 指令内容")
    String instructionContent,
    @Schema(description = "输入 JSON Schema")
    String inputSchema,
    @Schema(description = "输出 JSON Schema")
    String outputSchema,
    @Schema(description = "评估策略 JSON")
    String evaluationPolicy,
    @Schema(description = "Skill 说明")
    String description,
    @Schema(description = "创建时间，北京时间")
    LocalDateTime createdAt,
    @Schema(description = "更新时间，北京时间")
    LocalDateTime updatedAt,
    @Schema(description = "创建人")
    String createdBy,
    @Schema(description = "更新人")
    String updatedBy
) {
}
