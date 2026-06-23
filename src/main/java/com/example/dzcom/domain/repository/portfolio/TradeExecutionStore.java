package com.example.dzcom.domain.repository.portfolio;

import com.example.dzcom.domain.model.portfolio.TradeExecution;

import java.util.Optional;

/** 模拟成交仓储端口。 */
public interface TradeExecutionStore {
    /**
     * 保存模拟成交。
     *
     * @param execution 模拟成交领域对象
     * @return 保存后的模拟成交
     * @author dz
     * @date 2026-06-23
     */
    TradeExecution save(TradeExecution execution);

    /**
     * 查询订单对应的第一笔成交。
     *
     * @param orderBizId 订单业务唯一标识
     * @return 模拟成交
     * @author dz
     * @date 2026-06-23
     */
    Optional<TradeExecution> findFirstByOrderBizId(String orderBizId);
}
