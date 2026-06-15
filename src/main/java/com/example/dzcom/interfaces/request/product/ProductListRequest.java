package com.example.dzcom.interfaces.request.product;

import com.example.dzcom.domain.enums.product.ProductTradeStatus;
import com.example.dzcom.domain.enums.product.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 产品目录分页查询请求，筛选条件和分页参数统一从请求体接收。
 */
@Schema(description = "产品目录分页查询请求，支持产品条件筛选、分页与排序")
public record ProductListRequest(
    @Schema(description = "产品编码或名称关键字", example = "Apple")
    String keyword,
    @Schema(description = "产品类型")
    ProductType productType,
    @Schema(description = "交易状态")
    ProductTradeStatus tradeStatus,
    @Schema(description = "风险等级，允许值 1-5", example = "3")
    @Min(1) @Max(5) Integer riskLevel,
    @Schema(description = "币种编码", example = "USD")
    String currency,
    @Schema(description = "页码；支持 0 兼容前端零基页码，后端会转换为 1", example = "1")
    @Min(0) Integer page,
    @Schema(description = "每页条数，允许值 1-100", example = "20")
    @Min(1) @Max(100) Integer size,
    @Schema(description = "排序字段，必须属于服务端白名单", example = "createdAt")
    String sort,
    @Schema(description = "排序方向，asc 或 desc", example = "desc")
    String direction
) {
}
