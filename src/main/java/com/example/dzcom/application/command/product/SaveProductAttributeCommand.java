package com.example.dzcom.application.command.product;

import lombok.Builder;

import java.time.LocalDate;

/** 新增或覆盖同一产品、属性键和生效日期的低频扩展属性。 */
@Builder
public record SaveProductAttributeCommand(
    String key,
    String valueType,
    String jsonValue,
    LocalDate effectiveDate,
    String sourceCode
) {
}
