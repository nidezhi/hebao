package com.example.dzcom.interfaces.request.product;

import com.example.dzcom.domain.enums.product.ProductTradeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 独立的产品交易状态变更请求，避免通用更新绕过生命周期规则。 */
@Schema(description = "产品交易状态变更请求")
public record ProductStatusRequest(
    @Schema(description = "产品业务标识", example = "prd_01Hxxxx")
    @NotBlank String bizId,
    @Schema(description = "目标交易状态")
    @NotNull ProductTradeStatus status
) {
}
