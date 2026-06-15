package com.example.dzcom.interfaces.dto.response.product;

import com.example.dzcom.application.dto.product.ProductAttributeView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

/** 接口层产品扩展属性响应。 */
@Builder
@Schema(description = "产品扩展属性响应")
public record ProductAttributeResponse(
    @Schema(description = "属性键", example = "issuer") String key,
    @Schema(description = "值类型", example = "string") String valueType,
    @Schema(description = "JSON 格式属性值") String jsonValue,
    @Schema(description = "属性生效日期") LocalDate effectiveDate,
    @Schema(description = "来源编码", example = "MANUAL_IMPORT") String sourceCode
) {

    /**
     * 将应用层产品属性视图转换为接口响应。
     *
     * @param source 应用层产品属性视图
     * @return 接口层产品属性响应
     * @author dz
     * @date 2026-06-15
     */
    public static ProductAttributeResponse from(ProductAttributeView source) {
        return ProductAttributeResponse.builder()
            .key(source.key())
            .valueType(source.valueType())
            .jsonValue(source.jsonValue())
            .effectiveDate(source.effectiveDate())
            .sourceCode(source.sourceCode())
            .build();
    }
}
