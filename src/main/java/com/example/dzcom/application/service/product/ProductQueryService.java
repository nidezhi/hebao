package com.example.dzcom.application.service.product;

import com.example.dzcom.application.assembler.product.ProductViewAssembler;
import com.example.dzcom.application.dto.product.ProductView;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.enums.product.ProductTradeStatus;
import com.example.dzcom.domain.enums.product.ProductType;
import com.example.dzcom.domain.model.product.Product;
import com.example.dzcom.domain.repository.product.ProductSearchCriteria;
import com.example.dzcom.domain.repository.product.ProductInvestmentProfileStore;
import com.example.dzcom.domain.repository.product.ProductStore;
import com.example.dzcom.domain.repository.product.ProductAttributeStore;
import com.example.dzcom.domain.repository.product.ProductThemeRelationStore;
import com.example.dzcom.domain.repository.market.MarketQuoteStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/** 产品目录公开查询用例。 */
@Service
@RequiredArgsConstructor
public class ProductQueryService {
    private static final Set<String> SORT_FIELDS =
        Set.of("createdAt", "productName", "productCode", "riskLevel", "listingDate");

    private final ProductStore store;
    private final ProductAttributeStore attributes;
    private final ProductInvestmentProfileStore investmentProfiles;
    private final ProductThemeRelationStore themeRelations;
    private final MarketQuoteStore quotes;
    private final ProductViewAssembler assembler;

    /**
     * 查询产品详情并一次性装配其有效扩展属性。
     *
     * @param bizId 业务对象的唯一标识
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    @Transactional(readOnly = true)
    public ProductView detail(String bizId) {
        Product product = requiredProduct(bizId);
        return assembler.assembleInvestmentDetail(
            product,
            attributes.findByProductBizId(bizId),
            investmentProfiles.findByProductBizId(bizId),
            themeRelations.findByProductBizId(bizId),
            quotes.findLatest(product.getBizId(), "1D", null)
        );
    }

    /**
     * 按白名单字段分页查询产品，阻断任意排序字段进入 Mapper XML。
     *
     * @param keyword 模糊查询关键字
     * @param productType productType 参数
     * @param tradeStatus tradeStatus 参数
     * @param riskLevel riskLevel 参数
     * @param currency currency 参数
     * @param pageQuery 分页查询参数
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    @Transactional(readOnly = true)
    public PageResult<ProductView> list(String keyword, ProductType productType,
                                        ProductTradeStatus tradeStatus, Integer riskLevel,
                                        String currency, PageQuery pageQuery) {
        String sort = pageQuery.safeSort(SORT_FIELDS, "createdAt");
        PageResult<Product> page = store.search(new ProductSearchCriteria(
            keyword, productType, tradeStatus, riskLevel, currency,
            pageQuery.page(), pageQuery.size(), sort, "asc".equals(pageQuery.direction())
        ));
        return PageResult.<ProductView>builder()
            .items(page.items().stream()
                .map(product -> assembler.assembleSummary(
                    product,
                    investmentProfiles.findByProductBizId(product.getBizId()),
                    quotes.findLatest(product.getBizId(), "1D", null)
                ))
                .toList())
            .total(page.total())
            .page(page.page())
            .size(page.size())
            .totalPages(page.totalPages())
            .build();
    }

    /**
     * 获取必需的业务对象，不存在时终止当前流程。
     *
     * @param bizId 业务对象的唯一标识
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    private Product requiredProduct(String bizId) {
        return store.findByBizId(bizId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "产品不存在"));
    }
}
