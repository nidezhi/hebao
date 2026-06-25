package com.example.dzcom.interfaces.request.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 自动投资闭环运行详情请求。 */
@Schema(description = "自动投资闭环运行详情请求")
public record ClosedLoopRunDetailRequest(
    @NotBlank(message = "闭环运行业务ID不能为空")
    @Schema(description = "闭环运行业务唯一标识")
    String bizId
) {
}
