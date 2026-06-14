package com.example.dzcom.domain.repository.product;

import com.example.dzcom.domain.model.product.ProductAttribute;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** 产品属性仓储端口。 */
public interface ProductAttributeStore {
    /** 保存产品属性。 */
    ProductAttribute save(ProductAttribute attribute);

    /** 根据产品、属性键和生效日期查询属性。 */
    Optional<ProductAttribute> find(String productBizId, String key, LocalDate effectiveDate,
                                    boolean includeDeleted);

    /** 查询产品全部有效属性。 */
    List<ProductAttribute> findByProductBizId(String productBizId);
}
