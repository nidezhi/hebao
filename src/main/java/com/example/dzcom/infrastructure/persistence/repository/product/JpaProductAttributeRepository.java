package com.example.dzcom.infrastructure.persistence.repository.product;

import com.example.dzcom.infrastructure.persistence.entity.product.ProductAttributeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Spring Data 产品扩展属性仓储。 */
public interface JpaProductAttributeRepository extends JpaRepository<ProductAttributeEntity, String> {
    Optional<ProductAttributeEntity> findByProductBizIdAndAttributeKeyAndEffectiveDate(
        String productBizId, String attributeKey, LocalDate effectiveDate);

    Optional<ProductAttributeEntity> findByProductBizIdAndAttributeKeyAndEffectiveDateAndDeleted(
        String productBizId, String attributeKey, LocalDate effectiveDate, int deleted);

    List<ProductAttributeEntity> findAllByProductBizIdAndDeletedOrderByAttributeKeyAscEffectiveDateDesc(
        String productBizId, int deleted);
}
