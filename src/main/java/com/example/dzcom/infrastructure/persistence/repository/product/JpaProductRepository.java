package com.example.dzcom.infrastructure.persistence.repository.product;

import com.example.dzcom.infrastructure.persistence.entity.product.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/** Spring Data 产品主表仓储。 */
public interface JpaProductRepository extends JpaRepository<ProductEntity, String> {
    /**
     * 执行 find by biz id and deleted 处理。
     *
     * @param bizId 业务对象的唯一标识
     * @param deleted deleted 参数
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    Optional<ProductEntity> findByBizIdAndDeleted(String bizId, int deleted);

    /**
     * 执行 exists by market code and product code and deleted 处理。
     *
     * @param marketCode marketCode 参数
     * @param productCode productCode 参数
     * @param deleted deleted 参数
     * @return 满足条件时返回 true，否则返回 false
     * @author dz
     * @date 2026-06-14
     */
    boolean existsByMarketCodeAndProductCodeAndDeleted(String marketCode, String productCode, int deleted);

    /**
     * 根据查询条件获取业务数据列表。
     *
     * @param keyword 模糊查询关键字
     * @param productType productType 参数
     * @param tradeStatus tradeStatus 参数
     * @param riskLevel riskLevel 参数
     * @param currency currency 参数
     * @param pageable pageable 参数
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
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
