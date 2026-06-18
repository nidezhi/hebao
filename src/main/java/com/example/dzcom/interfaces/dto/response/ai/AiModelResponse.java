package com.example.dzcom.interfaces.dto.response.ai;

import com.example.dzcom.domain.model.ai.AiModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** AI 模型响应。 */
@Builder
@Schema(description = "AI 模型响应")
public record AiModelResponse(
    @Schema(description = "模型业务 ID") String bizId,
    @Schema(description = "模型编码") String modelCode,
    @Schema(description = "模型版本") String modelVersion,
    @Schema(description = "模型名称") String modelName,
    @Schema(description = "模型类型") String modelType,
    @Schema(description = "模型提供方") String provider,
    @Schema(description = "模型制品、提示词或配置地址") String artifactUri,
    @Schema(description = "脱敏后的模型参数 JSON 字符串") String modelConfig,
    @Schema(description = "离线评估指标 JSON 字符串") String metrics,
    @Schema(description = "状态") String status,
    @Schema(description = "启用时间") LocalDateTime activatedAt,
    @Schema(description = "停用时间") LocalDateTime retiredAt,
    @Schema(description = "创建时间") LocalDateTime createdAt,
    @Schema(description = "更新时间") LocalDateTime updatedAt
) {
    /** 将领域对象转换为接口响应。 */
    public static AiModelResponse from(AiModel model) {
        return AiModelResponse.builder()
            .bizId(model.bizId())
            .modelCode(model.modelCode())
            .modelVersion(model.modelVersion())
            .modelName(model.modelName())
            .modelType(model.modelType())
            .provider(model.provider())
            .artifactUri(model.artifactUri())
            .modelConfig(model.modelConfig())
            .metrics(model.metrics())
            .status(model.status())
            .activatedAt(model.activatedAt())
            .retiredAt(model.retiredAt())
            .createdAt(model.createdAt())
            .updatedAt(model.updatedAt())
            .build();
    }
}
