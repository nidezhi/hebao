package com.example.dzcom.interfaces.request.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 产品扩展属性写入请求。
 *
 * <p>{@code jsonValue} 必须是合法 JSON 文本；字符串值需要包含 JSON 引号，
 * 例如 {@code "张三"}，而数字可直接传 {@code 12.5}。</p>
 */
@Schema(description = "产品扩展属性写入请求，jsonValue 必须为合法 JSON 文本")
public record ProductAttributeRequest(
    @Schema(description = "产品业务标识", example = "prd_01Hxxxx")
    @NotBlank String bizId,
    @Schema(description = "属性键", example = "issuer")
    @NotBlank @Size(max = 64) String key,
    @Schema(description = "值类型", example = "string")
    @NotBlank @Size(max = 16) String valueType,
    @Schema(description = "JSON 格式属性值；字符串值需要包含 JSON 引号", example = "\"Apple Inc.\"")
    @NotBlank String jsonValue,
    @Schema(description = "属性生效日期", example = "2026-06-15")
    LocalDate effectiveDate,
    @Schema(description = "属性来源编码", example = "MANUAL")
    @Size(max = 64) String sourceCode
) {
}
