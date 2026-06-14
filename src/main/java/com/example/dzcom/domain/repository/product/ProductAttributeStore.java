package com.example.dzcom.domain.repository.product;

import com.example.dzcom.domain.model.product.ProductAttribute;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductAttributeStore {
    ProductAttribute save(ProductAttribute attribute);

    Optional<ProductAttribute> find(String productBizId, String key, LocalDate effectiveDate,
                                    boolean includeDeleted);

    List<ProductAttribute> findByProductBizId(String productBizId);
}
