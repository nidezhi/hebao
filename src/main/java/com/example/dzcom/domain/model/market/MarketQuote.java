package com.example.dzcom.domain.model.market;

import com.example.dzcom.domain.enums.market.QuoteStatus;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 单个产品在指定数据源、周期和时点的 OHLCV 行情。
 *
 * <p>同一业务键的数据由基础设施层执行覆盖式修正，领域对象负责保证价格区间、
 * 非负成交量以及时间完整性。行情是只追加/修正数据，不与产品聚合建立对象关系。</p>
 */
@Schema(description = "领域层行情对象（OHLCV）")
@Builder
public record MarketQuote(
    @Schema(description = "行情业务标识") String bizId,
    @Schema(description = "产品业务标识") String productBizId,
    @Schema(description = "数据源编码") String sourceCode,
    @Schema(description = "周期") String interval,
    @Schema(description = "行情时间（北京时间）") LocalDateTime quoteTime,
    @Schema(description = "开盘价") BigDecimal openPrice,
    @Schema(description = "最高价") BigDecimal highPrice,
    @Schema(description = "最低价") BigDecimal lowPrice,
    @Schema(description = "收盘价") BigDecimal closePrice,
    @Schema(description = "前一周期收盘价") BigDecimal previousClosePrice,
    @Schema(description = "成交量") BigDecimal volume,
    @Schema(description = "成交额") BigDecimal turnoverAmount,
    @Schema(description = "行情状态") QuoteStatus status,
    @Schema(description = "接收时间（北京时间）") LocalDateTime receivedAt,
    @Schema(description = "创建时间（北京时间）") LocalDateTime createdAt
) {
    public MarketQuote {
        if (productBizId == null || productBizId.isBlank()) {
            throw new IllegalArgumentException("行情产品业务标识不能为空");
        }
        if (sourceCode == null || sourceCode.isBlank()) {
            throw new IllegalArgumentException("行情数据源不能为空");
        }
        if (interval == null || interval.isBlank()) {
            throw new IllegalArgumentException("行情周期不能为空");
        }
        if (quoteTime == null || closePrice == null || closePrice.signum() < 0) {
            throw new IllegalArgumentException("行情时间和非负收盘价不能为空");
        }
        validateNonNegative("开盘价", openPrice);
        validateNonNegative("最高价", highPrice);
        validateNonNegative("最低价", lowPrice);
        validateNonNegative("前收盘价", previousClosePrice);
        validateNonNegative("成交数量", volume);
        validateNonNegative("成交金额", turnoverAmount);
        if (highPrice != null && lowPrice != null && highPrice.compareTo(lowPrice) < 0) {
            throw new IllegalArgumentException("最高价不能低于最低价");
        }
        validateWithinRange("开盘价", openPrice, lowPrice, highPrice);
        validateWithinRange("收盘价", closePrice, lowPrice, highPrice);
    }

    /**
     * 校验输入值是否满足业务约束。
     *
     * @param field field 参数
     * @param value 待处理的数据值
     * @throws IllegalArgumentException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    private static void validateNonNegative(String field, BigDecimal value) {
        if (value != null && value.signum() < 0) {
            throw new IllegalArgumentException(field + "不能为负数");
        }
    }

    /**
     * 校验输入值是否满足业务约束。
     *
     * @param field field 参数
     * @param value 待处理的数据值
     * @param low low 参数
     * @param high high 参数
     * @throws IllegalArgumentException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    private static void validateWithinRange(String field, BigDecimal value,
                                            BigDecimal low, BigDecimal high) {
        if (value != null && low != null && value.compareTo(low) < 0) {
            throw new IllegalArgumentException(field + "不能低于最低价");
        }
        if (value != null && high != null && value.compareTo(high) > 0) {
            throw new IllegalArgumentException(field + "不能高于最高价");
        }
    }
}
