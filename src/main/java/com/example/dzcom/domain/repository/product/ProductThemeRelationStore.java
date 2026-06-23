package com.example.dzcom.domain.repository.product;

import com.example.dzcom.domain.model.product.ProductThemeRelation;

import java.util.List;

/** 产品主题、行业、指数和资产类别关系仓储端口。 */
public interface ProductThemeRelationStore {

    /**
     * 替换指定产品的全部主题关系。
     *
     * @param productBizId 产品业务唯一标识
     * @param relations 新的主题关系集合
     * @author dz
     * @date 2026-06-22
     */
    void replaceByProductBizId(String productBizId, List<ProductThemeRelation> relations);

    /**
     * 根据产品业务标识查询全部主题关系。
     *
     * @param productBizId 产品业务唯一标识
     * @return 产品主题、行业、指数和资产类别关系集合
     * @author dz
     * @date 2026-06-22
     */
    List<ProductThemeRelation> findByProductBizId(String productBizId);

    /**
     * 根据关系类型和关系编码反查产品关系。
     *
     * @param relationType 关系类型
     * @param relationCode 关系编码
     * @return 产品主题关系集合
     * @author dz
     * @date 2026-06-23
     */
    List<ProductThemeRelation> findByRelation(String relationType, String relationCode);
}
