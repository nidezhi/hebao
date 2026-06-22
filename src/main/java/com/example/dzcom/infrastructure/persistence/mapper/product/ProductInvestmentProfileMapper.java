package com.example.dzcom.infrastructure.persistence.mapper.product;

import com.example.dzcom.infrastructure.persistence.entity.product.ProductInvestmentProfileEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 产品投资画像 MyBatis Mapper。 */
@Mapper
public interface ProductInvestmentProfileMapper {

    /**
     * 新增或更新产品投资画像。
     *
     * @param entity 产品投资画像持久化实体
     * @return 影响行数
     * @author dz
     * @date 2026-06-22
     */
    int save(ProductInvestmentProfileEntity entity);

    /**
     * 根据产品业务标识查询投资画像。
     *
     * @param productBizId 产品业务唯一标识
     * @return 产品投资画像持久化实体；不存在时返回 null
     * @author dz
     * @date 2026-06-22
     */
    ProductInvestmentProfileEntity selectByProductBizId(@Param("productBizId") String productBizId);
}
