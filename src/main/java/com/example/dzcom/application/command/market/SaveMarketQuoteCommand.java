package com.example.dzcom.application.command.market;

import com.example.dzcom.domain.enums.market.QuoteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 行情采集或管理接口写入一个标准 OHLCV 行情点的用例输入。 */
@Builder
@Schema(description = "保存标准 OHLCV 行情点的应用层命令")
public record SaveMarketQuoteCommand(
    @Schema(description = "行情所属产品业务标识")
    String productBizId,
    @Schema(description = "行情数据来源编码")
    String sourceCode,
    @Schema(description = "行情周期，如 1m、5m、1d")
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
    @Schema(description = "行情有效状态")
    QuoteStatus status
) {
}
