package com.example.dzcom.interfaces.controller.product;

import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.service.market.MarketQuoteApplicationService;
import com.example.dzcom.application.service.product.ProductQueryService;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.result.Result;
import com.example.dzcom.interfaces.dto.response.common.PageResponse;
import com.example.dzcom.interfaces.dto.response.market.MarketQuoteResponse;
import com.example.dzcom.interfaces.dto.response.product.ProductResponse;
import com.example.dzcom.interfaces.request.market.LatestMarketQuoteRequest;
import com.example.dzcom.interfaces.request.market.MarketQuoteHistoryRequest;
import com.example.dzcom.interfaces.request.product.ProductBizIdRequest;
import com.example.dzcom.interfaces.request.product.ProductListRequest;
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

import java.util.List;

/**
 * 面向用户的产品目录与基础行情查询接口。
 *
 * <p>该接口只读且不要求登录，便于产品展示和行情浏览。后续若产品可见性需要按渠道、
 * 地区或用户资格过滤，应在应用查询用例中显式加入策略，不能在 Controller 临时判断。</p>
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "产品与行情", description = "面向客户端的产品目录、产品详情和行情查询接口")
public class ProductController {
    private final ProductQueryService products;
    private final MarketQuoteApplicationService quotes;

    /**
     * 根据筛选条件分页查询产品目录。
     *
     * @param request 产品筛选、分页和排序请求
     * @return 产品分页结果
     * @throws BusinessException 当分页参数或排序规则不合法时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询产品目录", description = "根据筛选条件分页查询产品目录。分页默认 page=1,size=20, sort=createdAt,direction=desc。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回接口层产品分页响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数或分页规则不合法"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<PageResponse<ProductResponse>> list(@Valid @RequestBody ProductListRequest request) {
        var result = products.list(
            request.keyword(),
            request.productType(),
            request.tradeStatus(),
            request.riskLevel(),
            request.currency(),
            new PageQuery(
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size(),
                request.sort() == null ? "createdAt" : request.sort(),
                request.direction() == null ? "desc" : request.direction()
            )
        );
        return Result.success(PageResponse.from(result, ProductResponse::from));
    }

    /**
     * 根据产品业务标识查询产品详情。
     *
     * @param request 产品业务标识请求
     * @return 产品详细信息
     * @throws BusinessException 当产品不存在时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/detail")
    @Operation(summary = "查询产品详情", description = "根据产品业务标识查询产品的详细信息（生命周期、交易参数、说明、扩展属性等）。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回产品响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "404", description = "产品不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<ProductResponse> detail(@Valid @RequestBody ProductBizIdRequest request) {
        return Result.success(ProductResponse.from(products.detail(request.bizId())));
    }

    /**
     * 查询指定产品和行情周期的最新有效行情。
     *
     * @param request 产品标识、行情周期和可选数据源
     * @return 最新有效行情
     * @throws BusinessException 当产品或行情不存在时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/quotes/latest")
    @Operation(summary = "查询产品最新有效行情", description = "查询指定产品和周期的最新有效行情。若 interval 为空默认使用 1D。可指定数据源 sourceCode。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回最新行情响应", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "404", description = "产品或行情不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<MarketQuoteResponse> latestQuote(@Valid @RequestBody LatestMarketQuoteRequest request) {
        return Result.success(MarketQuoteResponse.from(quotes.latest(
            request.productBizId(),
            request.interval() == null ? "1D" : request.interval(),
            request.sourceCode()
        )));
    }

    /**
     * 查询指定产品在时间区间内的历史行情。
     *
     * @param request 产品标识、行情周期、时间区间和结果上限
     * @return 按时间排列的历史行情列表
     * @throws BusinessException 当时间区间或查询上限不合法时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/quotes/history")
    @Operation(summary = "查询产品历史行情", description = "查询指定时间区间内按时间升序排列的历史行情，limit 最大 1000（默认 500）。from/to 必须同时提供且 from <= to。")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功，返回历史行情数组", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "时间区间或查询上限不合法"),
        @ApiResponse(responseCode = "404", description = "产品不存在"),
        @ApiResponse(responseCode = "500", description = "系统错误")
    })
    public Result<List<MarketQuoteResponse>> quoteHistory(
        @Valid @RequestBody MarketQuoteHistoryRequest request
    ) {
        return Result.success(quotes.history(
            request.productBizId(),
            request.interval() == null ? "1D" : request.interval(),
            request.sourceCode(),
            request.from(),
            request.to(),
            request.limit() == null ? 500 : request.limit()
        ).stream().map(MarketQuoteResponse::from).toList());
    }
}
