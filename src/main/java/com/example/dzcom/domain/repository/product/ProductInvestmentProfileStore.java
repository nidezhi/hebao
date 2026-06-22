package com.example.dzcom.domain.repository.product;

import com.example.dzcom.domain.model.product.ProductInvestmentProfile;

import java.util.Optional;

/** 产品投资风险和交易画像仓储端口。 */
public interface ProductInvestmentProfileStore {

    /**
     * 保存产品投资画像；同一产品只保留一条当前画像。
     *
     * @param profile 产品投资风险和交易画像
     * @return 保存后的产品投资画像
     * @author dz
     * @date 2026-06-22
     */
    ProductInvestmentProfile save(ProductInvestmentProfile profile);

    /**
     * 根据产品业务标识查询投资画像。
     *
     * @param productBizId 产品业务唯一标识
     * @return 产品投资画像；不存在时返回空
     * @author dz
     * @date 2026-06-22
     */
    Optional<ProductInvestmentProfile> findByProductBizId(String productBizId);
}
