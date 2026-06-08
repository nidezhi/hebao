package com.example.dzcom.infrastructure.persistence.repository.product;

import com.example.dzcom.infrastructure.persistence.entity.product.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/** Spring Data 产品主表仓储。 */
public interface JpaProductRepository extends JpaRepository<ProductEntity, String> {
    Optional<ProductEntity> findByBizIdAndDeleted(String bizId, int deleted);

    boolean existsByMarketCodeAndProductCodeAndDeleted(String marketCode, String productCode, int deleted);

    @Query(
        value = """
            select p from ProductEntity p
            where p.deleted = 0
              and (:keyword is null
                   or lower(p.productName) like lower(concat('%', :keyword, '%'))
                   or lower(p.productCode) like lower(concat('%', :keyword, '%'))
                   or lower(p.productNo) like lower(concat('%', :keyword, '%')))
              and (:productType is null or p.productType = :productType)
              and (:tradeStatus is null or p.tradeStatus = :tradeStatus)
              and (:riskLevel is null or p.riskLevel = :riskLevel)
              and (:currency is null or p.currency = :currency)
            """,
        countQuery = """
            select count(p) from ProductEntity p
            where p.deleted = 0
              and (:keyword is null
                   or lower(p.productName) like lower(concat('%', :keyword, '%'))
                   or lower(p.productCode) like lower(concat('%', :keyword, '%'))
                   or lower(p.productNo) like lower(concat('%', :keyword, '%')))
              and (:productType is null or p.productType = :productType)
              and (:tradeStatus is null or p.tradeStatus = :tradeStatus)
              and (:riskLevel is null or p.riskLevel = :riskLevel)
              and (:currency is null or p.currency = :currency)
            """
    )
    Page<ProductEntity> search(String keyword, String productType, Integer tradeStatus,
                               Integer riskLevel, String currency, Pageable pageable);
}
