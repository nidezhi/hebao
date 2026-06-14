package com.example.dzcom.interfaces.controller.product;

import com.example.dzcom.application.dto.market.MarketQuoteView;
import com.example.dzcom.application.dto.product.ProductView;
import com.example.dzcom.application.service.market.MarketQuoteApplicationService;
import com.example.dzcom.application.service.product.ProductQueryService;
import com.example.dzcom.common.page.PageQuery;
import com.example.dzcom.common.page.PageResult;
import com.example.dzcom.common.result.Result;
import com.example.dzcom.interfaces.request.market.LatestMarketQuoteRequest;
import com.example.dzcom.interfaces.request.market.MarketQuoteHistoryRequest;
import com.example.dzcom.interfaces.request.product.ProductBizIdRequest;
import com.example.dzcom.interfaces.request.product.ProductListRequest;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "产品与行情")
public class ProductController {
    private final ProductQueryService products;
    private final MarketQuoteApplicationService quotes;

    /**
     * 根据筛选条件分页查询产品目录。
     *
     * @param request 产品筛选、分页和排序请求
     * @return 产品分页结果
     * @throws com.example.dzcom.common.exception.BusinessException 当分页参数或排序规则不合法时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询产品目录")
    public Result<PageResult<ProductView>> list(@Valid @RequestBody ProductListRequest request) {
        return Result.success(products.list(
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
        ));
    }

    /**
     * 根据产品业务标识查询产品详情。
     *
     * @param request 产品业务标识请求
     * @return 产品详细信息
     * @throws com.example.dzcom.common.exception.BusinessException 当产品不存在时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/detail")
    @Operation(summary = "查询产品详情")
    public Result<ProductView> detail(@Valid @RequestBody ProductBizIdRequest request) {
        return Result.success(products.detail(request.bizId()));
    }

    /**
     * 查询指定产品和行情周期的最新有效行情。
     *
     * @param request 产品标识、行情周期和可选数据源
     * @return 最新有效行情
     * @throws com.example.dzcom.common.exception.BusinessException 当产品或行情不存在时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/quotes/latest")
    @Operation(summary = "查询产品最新有效行情")
    public Result<MarketQuoteView> latestQuote(@Valid @RequestBody LatestMarketQuoteRequest request) {
        return Result.success(quotes.latest(
            request.productBizId(),
            request.interval() == null ? "1D" : request.interval(),
            request.sourceCode()
        ));
    }

    /**
     * 查询指定产品在时间区间内的历史行情。
     *
     * @param request 产品标识、行情周期、时间区间和结果上限
     * @return 按时间排列的历史行情列表
     * @throws com.example.dzcom.common.exception.BusinessException 当时间区间或查询上限不合法时抛出
     * @author dz
     * @date 2026-06-14
     */
    @PostMapping("/quotes/history")
    @Operation(summary = "查询产品历史行情")
    public Result<List<MarketQuoteView>> quoteHistory(@Valid @RequestBody MarketQuoteHistoryRequest request) {
        return Result.success(quotes.history(
            request.productBizId(),
            request.interval() == null ? "1D" : request.interval(),
            request.sourceCode(),
            request.from(),
            request.to(),
            request.limit() == null ? 500 : request.limit()
        ));
    }
}
