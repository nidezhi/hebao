package com.example.dzcom.application.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

/** 对外展示的产品扩展属性，不暴露数据库主键和删除标记。 */
@Builder
@Schema(description = "产品扩展属性应用层视图")
public record ProductAttributeView(
    @Schema(description = "扩展属性稳定键")
    String key,
    @Schema(description = "属性值类型")
    String valueType,
    @Schema(description = "属性 JSON 值")
    String jsonValue,
    @Schema(description = "属性生效日期")
    LocalDate effectiveDate,
    @Schema(description = "属性数据来源编码")
    String sourceCode
) {
}
