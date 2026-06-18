package com.example.dzcom.domain.model.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

/** 单个产品在指定行情窗口内的起止价格和收益表现。 */
@Builder
@Schema(description = "主题产品窗口收益表现领域对象")
public record ThemeProductPerformance(
    @Schema(description = "产品业务唯一标识")
    String productBizId,
    @Schema(description = "产品代码")
    String productCode,
    @Schema(description = "产品名称")
    String productName,
    @Schema(description = "窗口起点价格")
    BigDecimal startPrice,
    @Schema(description = "窗口终点价格")
    BigDecimal endPrice,
    @Schema(description = "窗口收益率，小数形式")
    BigDecimal returnRate
) {
}
