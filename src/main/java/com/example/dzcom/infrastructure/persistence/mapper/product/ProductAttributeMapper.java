package com.example.dzcom.infrastructure.persistence.mapper.product;

import com.example.dzcom.infrastructure.persistence.entity.product.ProductAttributeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/** 产品属性 MyBatis Mapper。 */
@Mapper
public interface ProductAttributeMapper {
    /** 根据业务标识查询产品属性。 */
    ProductAttributeEntity selectById(@Param("bizId") String bizId);

    /** 根据产品、属性键和生效日期查询属性。 */
    ProductAttributeEntity selectOne(@Param("productBizId") String productBizId,
                                     @Param("key") String key,
                                     @Param("effectiveDate") LocalDate effectiveDate,
                                     @Param("includeDeleted") boolean includeDeleted);

    /** 查询产品全部未删除属性。 */
    List<ProductAttributeEntity> selectByProductBizId(@Param("productBizId") String productBizId);

    /** 新增或更新产品属性。 */
    int save(ProductAttributeEntity entity);
}
