package com.example.dzcom.domain.model.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** AI 模型版本、提供方、配置、评估指标和生命周期状态领域对象。 */
@Builder
@Schema(description = "AI 模型及版本注册领域对象")
public record AiModel(
    @Schema(description = "模型业务唯一标识")
    String bizId,
    @Schema(description = "跨版本稳定的模型编码")
    String modelCode,
    @Schema(description = "模型版本号")
    String modelVersion,
    @Schema(description = "模型展示名称")
    String modelName,
    @Schema(description = "模型业务类型")
    String modelType,
    @Schema(description = "模型提供方或运行平台")
    String provider,
    @Schema(description = "模型制品、提示词或配置地址")
    String artifactUri,
    @Schema(description = "脱敏后的模型配置 JSON 字符串")
    String modelConfig,
    @Schema(description = "离线评估指标 JSON 字符串")
    String metrics,
    @Schema(description = "生命周期状态")
    String status,
    @Schema(description = "首次正式启用时间，北京时间")
    LocalDateTime activatedAt,
    @Schema(description = "停用或归档时间，北京时间")
    LocalDateTime retiredAt,
    @Schema(description = "记录创建时间，北京时间")
    LocalDateTime createdAt,
    @Schema(description = "记录最后更新时间，北京时间")
    LocalDateTime updatedAt
) {
}
