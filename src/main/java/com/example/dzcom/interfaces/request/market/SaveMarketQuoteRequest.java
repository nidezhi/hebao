package com.example.dzcom.interfaces.request.market;

import com.example.dzcom.domain.enums.market.QuoteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 标准 OHLCV 行情点写入请求。 */
@Schema(description = "管理端写入或修正标准 OHLCV 行情点的请求")
public record SaveMarketQuoteRequest(
    @Schema(description = "产品业务标识", example = "prd_01Hxxxx")
    @NotBlank String productBizId,
    @Schema(description = "行情数据源编码", example = "EXCHANGE_A")
    @NotBlank @Size(max = 64) String sourceCode,
    @Schema(description = "行情周期", example = "1D")
    @NotBlank @Size(max = 16) String interval,
    @Schema(description = "行情时间", example = "2026-06-15T00:00:00")
    @NotNull LocalDateTime quoteTime,
    @Schema(description = "开盘价", example = "198.50000000")
    @DecimalMin("0") @Digits(integer = 16, fraction = 8) BigDecimal openPrice,
    @Schema(description = "最高价", example = "201.00000000")
    @DecimalMin("0") @Digits(integer = 16, fraction = 8) BigDecimal highPrice,
    @Schema(description = "最低价", example = "197.80000000")
    @DecimalMin("0") @Digits(integer = 16, fraction = 8) BigDecimal lowPrice,
    @Schema(description = "收盘价", example = "200.10000000")
    @NotNull @DecimalMin("0") @Digits(integer = 16, fraction = 8) BigDecimal closePrice,
    @Schema(description = "前一周期收盘价", example = "198.30000000")
    @DecimalMin("0") @Digits(integer = 16, fraction = 8) BigDecimal previousClosePrice,
    @Schema(description = "成交量", example = "1234567.00000000")
    @DecimalMin("0") @Digits(integer = 20, fraction = 8) BigDecimal volume,
    @Schema(description = "成交额", example = "246913400.00000000")
    @DecimalMin("0") @Digits(integer = 20, fraction = 8) BigDecimal turnoverAmount,
    @Schema(description = "行情数据质量状态；为空时使用服务端默认值")
    QuoteStatus status
) {
}
