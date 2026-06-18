package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 保存 AI 模型配置请求。 */
@Schema(description = "保存 AI 模型配置请求")
public record SaveAiModelRequest(
    @NotBlank
    @Schema(description = "模型稳定编码", example = "investment-analysis")
    String modelCode,
    @NotBlank
    @Schema(description = "模型版本", example = "v1")
    String modelVersion,
    @NotBlank
    @Schema(description = "模型名称", example = "投资分析模型")
    String modelName,
    @NotBlank
    @Schema(description = "模型类型：SIGNAL/RISK/RECOMMENDATION/NLP/ANALYSIS", example = "ANALYSIS")
    String modelType,
    @Schema(description = "模型提供方", example = "LOCAL_RULE")
    String provider,
    @Schema(description = "模型制品、提示词或配置地址")
    String artifactUri,
    @Schema(description = "脱敏后的模型参数 JSON 字符串")
    String modelConfig,
    @Schema(description = "离线评估指标 JSON 字符串")
    String metrics,
    @Schema(description = "状态：DRAFT/VALIDATING/ACTIVE/INACTIVE/ARCHIVED", example = "ACTIVE")
    String status
) {
}
