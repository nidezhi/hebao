package com.example.dzcom.interfaces.request.product;

import com.example.dzcom.domain.enums.product.ProductTradeStatus;
import jakarta.validation.constraints.NotNull;

/** 独立的产品交易状态变更请求，避免通用更新绕过生命周期规则。 */
public record ProductStatusRequest(@NotNull ProductTradeStatus status) {
}
