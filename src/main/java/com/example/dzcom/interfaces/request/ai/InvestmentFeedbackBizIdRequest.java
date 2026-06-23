package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 投资反馈业务 ID 请求。 */
@Schema(description = "投资反馈业务 ID 请求")
public record InvestmentFeedbackBizIdRequest(
    @NotBlank
    @Schema(description = "反馈业务唯一标识")
    String bizId
) {
}
