package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 回测结果业务 ID 请求。 */
@Schema(description = "回测结果业务 ID 请求")
public record BacktestBizIdRequest(
    @NotBlank
    @Schema(description = "回测结果业务唯一标识")
    String bizId
) {
}
