package com.example.dzcom.domain.repository.portfolio;

import com.example.dzcom.domain.model.portfolio.MockOrder;

import java.util.Optional;

/** 模拟订单仓储端口。 */
public interface MockOrderStore {
    /**
     * 保存模拟订单。
     *
     * @param order 模拟订单领域对象
     * @return 保存后的模拟订单
     * @author dz
     * @date 2026-06-23
     */
    MockOrder save(MockOrder order);

    /**
     * 按用户和幂等键查询订单。
     *
     * @param userBizId 用户业务唯一标识
     * @param idempotencyKey 客户端幂等键
     * @return 已存在的模拟订单
     * @author dz
     * @date 2026-06-23
     */
    Optional<MockOrder> findByUserAndIdempotencyKey(String userBizId, String idempotencyKey);

    /**
     * 按订单业务标识查询模拟订单。
     *
     * @param orderBizId 订单业务唯一标识
     * @return 未删除的模拟订单
     * @author dz
     * @date 2026-06-23
     */
    Optional<MockOrder> findByBizId(String orderBizId);
}
