package com.example.dzcom.infrastructure.persistence.repository.product;

import com.example.dzcom.infrastructure.persistence.entity.product.ProductAttributeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Spring Data 产品扩展属性仓储。 */
public interface JpaProductAttributeRepository extends JpaRepository<ProductAttributeEntity, String> {
    /**
     * 执行 find by product biz id and attribute key and effective date 处理。
     *
     * @param productBizId 业务对象的唯一标识
     * @param attributeKey attributeKey 参数
     * @param effectiveDate effectiveDate 参数
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    Optional<ProductAttributeEntity> findByProductBizIdAndAttributeKeyAndEffectiveDate(
        String productBizId, String attributeKey, LocalDate effectiveDate);

    /**
     * 执行 find by product biz id and attribute key and effective date and deleted 处理。
     *
     * @param productBizId 业务对象的唯一标识
     * @param attributeKey attributeKey 参数
     * @param effectiveDate effectiveDate 参数
     * @param deleted deleted 参数
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    Optional<ProductAttributeEntity> findByProductBizIdAndAttributeKeyAndEffectiveDateAndDeleted(
        String productBizId, String attributeKey, LocalDate effectiveDate, int deleted);

    /**
     * 执行 find all by product biz id and deleted order by attribute key asc effective date desc 处理。
     *
     * @param productBizId 业务对象的唯一标识
     * @param deleted deleted 参数
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    List<ProductAttributeEntity> findAllByProductBizIdAndDeletedOrderByAttributeKeyAscEffectiveDateDesc(
        String productBizId, int deleted);
}
