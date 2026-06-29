package com.example.dzcom.domain.repository.portfolio;

import com.example.dzcom.domain.model.portfolio.OrderEvent;

import java.util.List;

/** 模拟订单事件仓储端口。 */
public interface OrderEventStore {
    /**
     * 追加保存订单状态事件。
     *
     * @param event 订单状态事件
     * @return 保存后的订单状态事件
     * @author dz
     * @date 2026-06-23
     */
    OrderEvent save(OrderEvent event);

    /**
     * 查询订单生命周期事件。
     *
     * @param orderBizId 订单业务唯一标识
     * @return 按发生时间升序排列的订单事件
     * @author dz
     * @date 2026-06-23
     */
    List<OrderEvent> findByOrderBizId(String orderBizId);

    /**
     * 查询模拟组合最近订单事件。
     *
     * @param portfolioBizId 组合业务唯一标识
     * @param limit 事件数量上限
     * @return 按发生时间倒序排列的订单事件
     * @author dz
     * @date 2026-06-28
     */
    List<OrderEvent> findRecentByPortfolioBizId(String portfolioBizId, int limit);
}
