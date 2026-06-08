package com.example.dzcom.domain.model.product;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 产品低频扩展属性。
 *
 * <p>属性值保持 JSON 文本形式，以便完整保留 NUMBER、BOOLEAN、DATE 和对象等类型。
 * 高频过滤字段应升级为产品主表显式字段，不能长期依赖 JSON 扫描。</p>
 */
@Builder
public record ProductAttribute(
    String bizId,
    String productBizId,
    String key,
    String valueType,
    String jsonValue,
    LocalDate effectiveDate,
    String sourceCode,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    int deleted
) {
}
