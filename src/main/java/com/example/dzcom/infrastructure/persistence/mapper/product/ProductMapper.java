package com.example.dzcom.infrastructure.persistence.mapper.product;

import com.example.dzcom.domain.repository.product.ProductSearchCriteria;
import com.example.dzcom.infrastructure.persistence.entity.product.ProductEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 产品 MyBatis Mapper。 */
@Mapper
public interface ProductMapper {
    /** 根据业务标识查询产品。 */
    ProductEntity selectById(@Param("bizId") String bizId);

    /** 根据业务标识查询未删除产品。 */
    ProductEntity selectActiveByBizId(@Param("bizId") String bizId);

    /** 统计市场内指定代码的未删除产品数量。 */
    long countByMarketAndCode(@Param("marketCode") String marketCode,
                              @Param("productCode") String productCode);

    /** 根据市场和产品代码查询未删除产品。 */
    ProductEntity selectActiveByMarketAndCode(@Param("marketCode") String marketCode,
                                              @Param("productCode") String productCode);

    /** 根据筛选条件分页查询产品。 */
    List<ProductEntity> search(@Param("criteria") ProductSearchCriteria criteria,
                               @Param("offset") int offset,
                               @Param("sortColumn") String sortColumn);

    /** 统计符合筛选条件的产品数量。 */
    long count(@Param("criteria") ProductSearchCriteria criteria);

    /** 新增或更新产品。 */
    int save(ProductEntity entity);
}
