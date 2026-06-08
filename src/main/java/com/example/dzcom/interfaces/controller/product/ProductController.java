package com.example.dzcom.interfaces.controller.product;

import com.example.dzcom.application.dto.market.MarketQuoteView;
import com.example.dzcom.application.dto.product.ProductView;
import com.example.dzcom.application.service.market.MarketQuoteApplicationService;
import com.example.dzcom.application.service.product.ProductQueryService;
import com.example.dzcom.common.page.PageQuery;
import com.example.dzcom.common.page.PageResult;
import com.example.dzcom.common.result.Result;
import com.example.dzcom.domain.enums.product.ProductTradeStatus;
import com.example.dzcom.domain.enums.product.ProductType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
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

    @GetMapping
    @Operation(summary = "分页查询产品目录")
    public Result<PageResult<ProductView>> list(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) ProductType productType,
        @RequestParam(required = false) ProductTradeStatus tradeStatus,
        @RequestParam(required = false) Integer riskLevel,
        @RequestParam(required = false) String currency,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sort,
        @RequestParam(defaultValue = "desc") String direction
    ) {
        return Result.success(products.list(keyword, productType, tradeStatus, riskLevel, currency,
            new PageQuery(page, size, sort, direction)));
    }

    @GetMapping("/{bizId}")
    @Operation(summary = "查询产品详情")
    public Result<ProductView> detail(@PathVariable String bizId) {
        return Result.success(products.detail(bizId));
    }

    @GetMapping("/{bizId}/quotes/latest")
    @Operation(summary = "查询产品最新有效行情")
    public Result<MarketQuoteView> latestQuote(
        @PathVariable String bizId,
        @RequestParam(defaultValue = "1D") String interval,
        @RequestParam(required = false) String sourceCode
    ) {
        return Result.success(quotes.latest(bizId, interval, sourceCode));
    }

    @GetMapping("/{bizId}/quotes")
    @Operation(summary = "查询产品历史行情")
    public Result<List<MarketQuoteView>> quoteHistory(
        @PathVariable String bizId,
        @RequestParam(defaultValue = "1D") String interval,
        @RequestParam(required = false) String sourceCode,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
        @RequestParam(defaultValue = "500") int limit
    ) {
        return Result.success(quotes.history(bizId, interval, sourceCode, from, to, limit));
    }
}
