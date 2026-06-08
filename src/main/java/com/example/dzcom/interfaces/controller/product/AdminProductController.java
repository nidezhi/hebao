package com.example.dzcom.interfaces.controller.product;

import com.example.dzcom.application.command.market.SaveMarketQuoteCommand;
import com.example.dzcom.application.command.product.CreateProductCommand;
import com.example.dzcom.application.command.product.SaveProductAttributeCommand;
import com.example.dzcom.application.command.product.UpdateProductCommand;
import com.example.dzcom.application.dto.market.MarketQuoteView;
import com.example.dzcom.application.dto.product.ProductView;
import com.example.dzcom.application.service.market.MarketQuoteApplicationService;
import com.example.dzcom.application.service.product.ProductApplicationService;
import com.example.dzcom.common.result.Result;
import com.example.dzcom.interfaces.request.market.SaveMarketQuoteRequest;
import com.example.dzcom.interfaces.request.product.CreateProductRequest;
import com.example.dzcom.interfaces.request.product.ProductAttributeRequest;
import com.example.dzcom.interfaces.request.product.ProductStatusRequest;
import com.example.dzcom.interfaces.request.product.UpdateProductRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 管理端产品目录和行情维护接口；权限在应用服务中再次校验。 */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Tag(name = "管理端产品与行情")
public class AdminProductController {
    private final ProductApplicationService products;
    private final MarketQuoteApplicationService quotes;

    @PostMapping
    @Operation(summary = "创建产品")
    public Result<ProductView> create(@Valid @RequestBody CreateProductRequest request) {
        return Result.success(products.create(CreateProductCommand.builder()
            .productCode(request.productCode())
            .productName(request.productName())
            .productType(request.productType())
            .marketCode(request.marketCode())
            .currency(request.currency())
            .riskLevel(request.riskLevel())
            .minInvestAmount(request.minInvestAmount())
            .amountStep(request.amountStep())
            .quantityStep(request.quantityStep())
            .feeRate(request.feeRate())
            .listingDate(request.listingDate())
            .delistingDate(request.delistingDate())
            .description(request.description())
            .build()));
    }

    @PutMapping("/{bizId}")
    @Operation(summary = "更新产品可变资料")
    public Result<ProductView> update(@PathVariable String bizId,
                                      @Valid @RequestBody UpdateProductRequest request) {
        return Result.success(products.update(bizId, UpdateProductCommand.builder()
            .productName(request.productName())
            .riskLevel(request.riskLevel())
            .minInvestAmount(request.minInvestAmount())
            .amountStep(request.amountStep())
            .quantityStep(request.quantityStep())
            .feeRate(request.feeRate())
            .listingDate(request.listingDate())
            .delistingDate(request.delistingDate())
            .description(request.description())
            .build()));
    }

    @PatchMapping("/{bizId}/status")
    @Operation(summary = "变更产品交易状态")
    public Result<ProductView> status(@PathVariable String bizId,
                                      @Valid @RequestBody ProductStatusRequest request) {
        return Result.success(products.changeStatus(bizId, request.status()));
    }

    @PutMapping("/{bizId}/attributes")
    @Operation(summary = "新增或覆盖产品扩展属性")
    public Result<ProductView> attribute(@PathVariable String bizId,
                                         @Valid @RequestBody ProductAttributeRequest request) {
        return Result.success(products.saveAttribute(bizId, SaveProductAttributeCommand.builder()
            .key(request.key())
            .valueType(request.valueType())
            .jsonValue(request.jsonValue())
            .effectiveDate(request.effectiveDate())
            .sourceCode(request.sourceCode())
            .build()));
    }

    @PostMapping("/{bizId}/quotes")
    @Operation(summary = "写入或修正产品行情点")
    public Result<MarketQuoteView> quote(@PathVariable String bizId,
                                         @Valid @RequestBody SaveMarketQuoteRequest request) {
        return Result.success(quotes.save(SaveMarketQuoteCommand.builder()
            .productBizId(bizId)
            .sourceCode(request.sourceCode())
            .interval(request.interval())
            .quoteTime(request.quoteTime())
            .openPrice(request.openPrice())
            .highPrice(request.highPrice())
            .lowPrice(request.lowPrice())
            .closePrice(request.closePrice())
            .previousClosePrice(request.previousClosePrice())
            .volume(request.volume())
            .turnoverAmount(request.turnoverAmount())
            .status(request.status())
            .build()));
    }

    @DeleteMapping("/{bizId}")
    @Operation(summary = "逻辑删除产品")
    public Result<Void> delete(@PathVariable String bizId) {
        products.delete(bizId);
        return Result.success();
    }
}
