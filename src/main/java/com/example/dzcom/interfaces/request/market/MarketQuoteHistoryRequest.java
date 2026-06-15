package com.example.dzcom.interfaces.request.market;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 查询产品历史行情区间的请求。
 */
@Schema(description = "查询产品指定时间区间历史行情的请求")
public record MarketQuoteHistoryRequest(
    @Schema(description = "产品业务标识", example = "prd_01Hxxxx")
    @NotBlank String productBizId,
    @Schema(description = "行情周期；为空时默认 1D", example = "1D")
    @Size(max = 16) String interval,
    @Schema(description = "行情数据源编码；为空时由服务端选择", example = "EXCHANGE_A")
    @Size(max = 64) String sourceCode,
    @Schema(description = "查询开始时间，必须早于或等于结束时间", example = "2026-06-01T00:00:00")
    @NotNull LocalDateTime from,
    @Schema(description = "查询结束时间", example = "2026-06-15T23:59:59")
    @NotNull LocalDateTime to,
    @Schema(description = "最多返回条数，默认 500，最大 1000", example = "500")
    @Min(1) @Max(1000) Integer limit
) {
}
