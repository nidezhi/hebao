package com.example.dzcom.interfaces.request.market;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 查询产品最新有效行情的请求。
 */
public record LatestMarketQuoteRequest(
    @NotBlank String productBizId,
    @Size(max = 16) String interval,
    @Size(max = 64) String sourceCode
) {
}
