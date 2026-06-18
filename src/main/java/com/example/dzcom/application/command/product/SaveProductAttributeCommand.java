package com.example.dzcom.application.command.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

/** 新增或覆盖同一产品、属性键和生效日期的低频扩展属性。 */
@Builder
@Schema(description = "保存产品扩展属性的应用层命令")
public record SaveProductAttributeCommand(
    @Schema(description = "产品扩展属性稳定键")
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
