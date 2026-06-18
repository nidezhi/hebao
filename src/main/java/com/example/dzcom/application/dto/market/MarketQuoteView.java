package com.example.dzcom.application.dto.market;

import com.example.dzcom.domain.enums.market.QuoteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 面向 API 的标准行情视图。 */
@Builder
@Schema(description = "标准 OHLCV 行情应用层视图")
public record MarketQuoteView(
    @Schema(description = "行情业务唯一标识")
    String bizId,
    @Schema(description = "行情所属产品业务标识")
    String productBizId,
    @Schema(description = "行情数据来源编码")
    String sourceCode,
    @Schema(description = "行情周期")
    String interval,
    @Schema(description = "行情时间，北京时间")
    LocalDateTime quoteTime,
    @Schema(description = "开盘价")
    BigDecimal openPrice,
    @Schema(description = "最高价")
    BigDecimal highPrice,
    @Schema(description = "最低价")
    BigDecimal lowPrice,
    @Schema(description = "收盘价或最新价")
    BigDecimal closePrice,
    @Schema(description = "前收盘价")
    BigDecimal previousClosePrice,
    @Schema(description = "成交量")
    BigDecimal volume,
    @Schema(description = "成交金额")
    BigDecimal turnoverAmount,
    @Schema(description = "行情状态")
    QuoteStatus status,
    @Schema(description = "平台接收时间，北京时间")
    LocalDateTime receivedAt
) {
}
