package com.example.dzcom.domain.repository.product;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.product.Product;

import java.util.Optional;

/**
 * 产品中心仓储端口。
 *
 * <p>领域和应用层仅依赖该契约。产品属性通过产品业务 ID 显式读取，
 * 不使用持久化级联集合，避免扩展属性生命周期反向污染产品聚合。</p>
 */
public interface ProductStore {
    /**
     * 创建或保存对应的业务数据。
     *
     * @param product product 参数
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    Product save(Product product);

    /**
     * 根据指定条件查询业务数据。
     *
     * @param bizId 业务对象的唯一标识
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    Optional<Product> findByBizId(String bizId);

    /**
     * 执行 exists by market and code 处理。
     *
     * @param marketCode marketCode 参数
     * @param productCode productCode 参数
     * @return 满足条件时返回 true，否则返回 false
     * @author dz
     * @date 2026-06-14
     */
    boolean existsByMarketAndCode(String marketCode, String productCode);

    /**
     * 根据查询条件获取业务数据列表。
     *
     * @param criteria 查询筛选条件
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    PageResult<Product> search(ProductSearchCriteria criteria);

}
