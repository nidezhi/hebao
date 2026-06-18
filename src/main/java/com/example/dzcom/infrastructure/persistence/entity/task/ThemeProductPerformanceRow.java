package com.example.dzcom.infrastructure.persistence.entity.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** 单个产品在指定统计窗口内的行情收益查询结果行。 */
@Schema(description = "主题产品窗口收益查询结果行")
@Getter
@Setter
public class ThemeProductPerformanceRow {
    /** 产品业务唯一标识。 */
    @Schema(description = "产品业务唯一标识")
    private String productBizId;
    /** 产品稳定代码。 */
    @Schema(description = "产品代码")
    private String productCode;
    /** 产品展示名称。 */
    @Schema(description = "产品名称")
    private String productName;
    /** 统计窗口起点价格。 */
    @Schema(description = "窗口起点价格")
    private BigDecimal startPrice;
    /** 统计窗口终点价格。 */
    @Schema(description = "窗口终点价格")
    private BigDecimal endPrice;
    /** 根据起止价格计算的收益率。 */
    @Schema(description = "窗口收益率，小数形式")
    private BigDecimal returnRate;
}
