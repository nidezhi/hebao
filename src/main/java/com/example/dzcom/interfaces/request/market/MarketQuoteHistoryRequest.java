package com.example.dzcom.interfaces.request.market;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 查询产品历史行情区间的请求。
 */
public record MarketQuoteHistoryRequest(
    @NotBlank String productBizId,
    @Size(max = 16) String interval,
    @Size(max = 64) String sourceCode,
    @NotNull LocalDateTime from,
    @NotNull LocalDateTime to,
    @Min(1) @Max(5000) Integer limit
) {
}
