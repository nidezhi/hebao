package com.example.dzcom.infrastructure.persistence.mapper.product;

import com.example.dzcom.infrastructure.persistence.entity.product.ProductThemeRelationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 产品主题关系 MyBatis Mapper。 */
@Mapper
public interface ProductThemeRelationMapper {

    /**
     * 删除指定产品的全部旧关系。
     *
     * @param productBizId 产品业务唯一标识
     * @return 影响行数
     * @author dz
     * @date 2026-06-22
     */
    int deleteByProductBizId(@Param("productBizId") String productBizId);

    /**
     * 批量新增产品主题关系。
     *
     * @param entities 产品主题关系持久化实体集合
     * @return 影响行数
     * @author dz
     * @date 2026-06-22
     */
    int insertBatch(List<ProductThemeRelationEntity> entities);

    /**
     * 根据产品业务标识查询全部主题关系。
     *
     * @param productBizId 产品业务唯一标识
     * @return 产品主题关系实体集合
     * @author dz
     * @date 2026-06-22
     */
    List<ProductThemeRelationEntity> selectByProductBizId(@Param("productBizId") String productBizId);

    /** 根据关系类型和关系编码反查产品关系。 */
    List<ProductThemeRelationEntity> selectByRelation(
        @Param("relationType") String relationType,
        @Param("relationCode") String relationCode
    );
}
