package com.example.dzcom.domain.model.product;

import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 产品低频扩展属性。
 *
 * <p>属性值保持 JSON 文本形式，以便完整保留 NUMBER、BOOLEAN、DATE 和对象等类型。
 * 高频过滤字段应升级为产品主表显式字段，不能长期依赖 JSON 扫描。</p>
 */
@Schema(description = "产品扩展属性领域对象，jsonValue 为 JSON 文本")
@Builder
public record ProductAttribute(
    @Schema(description = "属性业务标识") String bizId,
    @Schema(description = "关联产品业务标识") String productBizId,
    @Schema(description = "属性键") String key,
    @Schema(description = "值类型") String valueType,
    @Schema(description = "JSON 文本值") String jsonValue,
    @Schema(description = "生效日期") LocalDate effectiveDate,
    @Schema(description = "来源编码") String sourceCode,
    @Schema(description = "创建时间（北京时间）") LocalDateTime createdAt,
    @Schema(description = "更新时间（北京时间）") LocalDateTime updatedAt,
    @Schema(description = "逻辑删除标记（0/1）") int deleted
) {
}
