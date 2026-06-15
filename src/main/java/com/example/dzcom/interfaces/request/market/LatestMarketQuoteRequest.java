package com.example.dzcom.interfaces.request.market;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 查询产品最新有效行情的请求。
 */
@Schema(description = "查询产品最新有效行情的请求")
public record LatestMarketQuoteRequest(
    @Schema(description = "产品业务标识", example = "prd_01Hxxxx")
    @NotBlank String productBizId,
    @Schema(description = "行情周期；为空时默认 1D", example = "1D")
    @Size(max = 16) String interval,
    @Schema(description = "行情数据源编码；为空时由服务端选择", example = "EXCHANGE_A")
    @Size(max = 64) String sourceCode
) {
}
