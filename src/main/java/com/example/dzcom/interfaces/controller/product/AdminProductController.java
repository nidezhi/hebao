package com.example.dzcom.interfaces.controller.product;

import com.example.dzcom.application.command.market.SaveMarketQuoteCommand;
import com.example.dzcom.application.command.product.CreateProductCommand;
import com.example.dzcom.application.command.product.SaveProductAttributeCommand;
import com.example.dzcom.application.command.product.UpdateProductCommand;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.service.market.MarketQuoteApplicationService;
import com.example.dzcom.application.service.product.ProductApplicationService;
import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.interfaces.dto.response.market.MarketQuoteResponse;
import com.example.dzcom.interfaces.dto.response.product.ProductResponse;
import com.example.dzcom.interfaces.request.market.SaveMarketQuoteRequest;
import com.example.dzcom.interfaces.request.product.CreateProductRequest;
import com.example.dzcom.interfaces.request.product.ProductAttributeRequest;
import com.example.dzcom.interfaces.request.product.ProductBizIdRequest;
import com.example.dzcom.interfaces.request.product.ProductStatusRequest;
import com.example.dzcom.interfaces.request.product.UpdateProductRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端产品目录和行情维护接口；权限在应用服务中再次校验。
 */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Tag(name = "管理端产品与行情", description = "管理产品资料、交易状态、扩展属性和行情数据的管理端接口")
public class AdminProductController {
    private final ProductApplicationService products;
    private final MarketQuoteApplicationService quotes;


    /**
     * 创建产品并初始化产品交易资料。
     *
     * @param request 产品基础资料和交易参数
     * @return 创建后的产品信息
     * @throws BusinessException 当产品编码冲突或参数不符合规则时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/create")
    @Operation(summary = "创建产品", description = "创建产品并初始化交易参数。请求中 productCode/marketCode/currency 等字段有严格格式与数值校验。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "创建成功，返回产品响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败或数值不合法"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无产品创建权限"),
        @ApiResponse(responseCode = "409", description = "产品编码冲突"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        return Result.success(ProductResponse.from(products.create(CreateProductCommand.builder()
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
                .build())));
    }

    /**
     * 更新指定产品的可变资料和交易参数。
     *
     * @param request 产品业务标识和待更新资料
     * @return 更新后的产品信息
     * @throws BusinessException 当产品不存在或参数不符合规则时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/update")
    @Operation(summary = "更新产品可变资料", description = "更新产品允许变更的资料与交易参数（名称、风险等级、步进、费用、上下架时间、说明等）。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "更新成功，返回产品响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数或业务规则不合法"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无产品更新权限"),
        @ApiResponse(responseCode = "404", description = "产品不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<ProductResponse> update(@Valid @RequestBody UpdateProductRequest request) {
        return Result.success(ProductResponse.from(products.update(
            request.bizId(), UpdateProductCommand.builder()
                .productName(request.productName())
                .riskLevel(request.riskLevel())
                .minInvestAmount(request.minInvestAmount())
                .amountStep(request.amountStep())
                .quantityStep(request.quantityStep())
                .feeRate(request.feeRate())
                .listingDate(request.listingDate())
                .delistingDate(request.delistingDate())
                .description(request.description())
                .build())));
    }

    /**
     * 变更指定产品的交易状态。
     *
     * @param request 产品业务标识和目标交易状态
     * @return 状态变更后的产品信息
     * @throws BusinessException 当产品不存在或状态转换不合法时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/status")
    @Operation(summary = "变更产品交易状态", description = "变更产品的交易可用状态（DISABLED/TRADABLE/SUSPENDED）。请使用此独立接口以确保生命周期规则。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回变更后的产品响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数或状态转换不合法"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无产品状态变更权限"),
        @ApiResponse(responseCode = "404", description = "产品不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<ProductResponse> status(@Valid @RequestBody ProductStatusRequest request) {
        return Result.success(ProductResponse.from(
            products.changeStatus(request.bizId(), request.status())));
    }

    /**
     * 新增或覆盖指定产品的扩展属性。
     *
     * @param request 产品业务标识和扩展属性内容
     * @return 保存属性后的产品信息
     * @throws BusinessException 当产品不存在或属性值不合法时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/attributes/save")
    @Operation(summary = "新增或覆盖产品扩展属性", description = "向产品写入扩展属性，jsonValue 必须是合法的 JSON 文本（字符串需包含引号）。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回包含新属性的产品响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败或 jsonValue 非法"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无产品属性维护权限"),
        @ApiResponse(responseCode = "404", description = "产品不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<ProductResponse> attribute(@Valid @RequestBody ProductAttributeRequest request) {
        return Result.success(ProductResponse.from(products.saveAttribute(
            request.bizId(), SaveProductAttributeCommand.builder()
                .key(request.key())
                .valueType(request.valueType())
                .jsonValue(request.jsonValue())
                .effectiveDate(request.effectiveDate())
                .sourceCode(request.sourceCode())
                .build())));
    }

    /**
     * 写入或修正指定产品的行情点。
     *
     * @param request 产品业务标识和标准行情数据
     * @return 保存后的行情信息
     * @throws BusinessException 当产品不存在或行情数据不合法时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/quotes/save")
    @Operation(summary = "写入或修正产品行情点", description = "写入单条 OHLCV 行情点。用于修正或补写历史/实时行情数据。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回保存后的行情响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败或行情数据不合法"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无行情维护权限"),
        @ApiResponse(responseCode = "404", description = "产品不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<MarketQuoteResponse> quote(@Valid @RequestBody SaveMarketQuoteRequest request) {
        return Result.success(MarketQuoteResponse.from(quotes.save(SaveMarketQuoteCommand.builder()
                .productBizId(request.productBizId())
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
                .build())));
    }

    /**
     * 逻辑删除指定产品。
     *
     * @param request 产品业务标识请求
     * @return 无业务数据的成功结果
     * @throws BusinessException 当产品不存在或不允许删除时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/delete")
    @Operation(summary = "逻辑删除产品", description = "对指定产品执行逻辑删除。删除后常规产品查询不再返回该产品。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "删除成功（Result<Void>）", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数不合法或不允许删除"),
        @ApiResponse(responseCode = "401", description = "未登录或会话失效"),
        @ApiResponse(responseCode = "403", description = "无产品删除权限"),
        @ApiResponse(responseCode = "404", description = "产品不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<Void> delete(@Valid @RequestBody ProductBizIdRequest request) {
        products.delete(request.bizId());
        return Result.success();
    }
}
