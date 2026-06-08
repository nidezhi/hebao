package com.example.dzcom.application.dto.product;

import lombok.Builder;

import java.time.LocalDate;

/** 对外展示的产品扩展属性，不暴露数据库主键和删除标记。 */
@Builder
public record ProductAttributeView(
    String key,
    String valueType,
    String jsonValue,
    LocalDate effectiveDate,
    String sourceCode
) {
}
