package com.example.dzcom.domain.repository.product;

import com.example.dzcom.common.page.PageResult;
import com.example.dzcom.domain.model.product.Product;
import com.example.dzcom.domain.model.product.ProductAttribute;

import java.util.List;
import java.util.Optional;

/**
 * 产品中心仓储端口。
 *
 * <p>领域和应用层仅依赖该契约。产品属性通过产品业务 ID 显式读取，
 * 不使用 JPA 级联集合，避免扩展属性生命周期反向污染产品聚合。</p>
 */
public interface ProductStore {
    Product save(Product product);

    Optional<Product> findByBizId(String bizId);

    boolean existsByMarketAndCode(String marketCode, String productCode);

    PageResult<Product> search(ProductSearchCriteria criteria);

    ProductAttribute saveAttribute(ProductAttribute attribute);

    Optional<ProductAttribute> findAttribute(String productBizId, String key,
                                             java.time.LocalDate effectiveDate, boolean includeDeleted);

    List<ProductAttribute> findAttributes(String productBizId);
}
