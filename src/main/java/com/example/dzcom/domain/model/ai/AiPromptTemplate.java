package com.example.dzcom.domain.model.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** AI Prompt 模板版本领域对象。 */
@Builder
@Schema(description = "AI Prompt 模板版本领域对象")
public record AiPromptTemplate(
    @Schema(description = "Prompt模板业务唯一标识")
    String bizId,
    @Schema(description = "Prompt跨版本稳定编码")
    String promptCode,
    @Schema(description = "Prompt版本号")
    String promptVersion,
    @Schema(description = "使用场景")
    String scenario,
    @Schema(description = "模板展示名称")
    String templateName,
    @Schema(description = "模板内容")
    String templateContent,
    @Schema(description = "生命周期状态")
    String status,
    @Schema(description = "模板说明")
    String description,
    @Schema(description = "创建时间")
    LocalDateTime createdAt,
    @Schema(description = "更新时间")
    LocalDateTime updatedAt,
    @Schema(description = "创建操作者")
    String createdBy,
    @Schema(description = "更新操作者")
    String updatedBy
) {
}
