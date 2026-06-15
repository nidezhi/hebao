package com.example.dzcom.interfaces.dto.response.market;

import com.example.dzcom.application.dto.market.MarketQuoteView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 接口层标准行情响应。 */
@Builder
@Schema(description = "标准 OHLCV 行情响应")
public record MarketQuoteResponse(
    @Schema(description = "行情业务标识", example = "mq_01Hxxxx") String bizId,
    @Schema(description = "产品业务标识", example = "prd_01Hxxxx") String productBizId,
    @Schema(description = "数据源编码", example = "EXCHANGE_A") String sourceCode,
    @Schema(description = "行情周期", example = "1D") String interval,
    @Schema(description = "行情时间") LocalDateTime quoteTime,
    @Schema(description = "开盘价") BigDecimal openPrice,
    @Schema(description = "最高价") BigDecimal highPrice,
    @Schema(description = "最低价") BigDecimal lowPrice,
    @Schema(description = "收盘价") BigDecimal closePrice,
    @Schema(description = "前一周期收盘价") BigDecimal previousClosePrice,
    @Schema(description = "成交量") BigDecimal volume,
    @Schema(description = "成交额") BigDecimal turnoverAmount,
    @Schema(description = "数据质量状态", example = "VALID") String status,
    @Schema(description = "接收时间") LocalDateTime receivedAt
) {

    /**
     * 将应用层行情视图转换为接口响应。
     *
     * @param source 应用层行情视图
     * @return 接口层行情响应
     * @author dz
     * @date 2026-06-15
     */
    public static MarketQuoteResponse from(MarketQuoteView source) {
        return MarketQuoteResponse.builder()
            .bizId(source.bizId())
            .productBizId(source.productBizId())
            .sourceCode(source.sourceCode())
            .interval(source.interval())
            .quoteTime(source.quoteTime())
            .openPrice(source.openPrice())
            .highPrice(source.highPrice())
            .lowPrice(source.lowPrice())
            .closePrice(source.closePrice())
            .previousClosePrice(source.previousClosePrice())
            .volume(source.volume())
            .turnoverAmount(source.turnoverAmount())
            .status(source.status() == null ? null : source.status().name())
            .receivedAt(source.receivedAt())
            .build();
    }
}
