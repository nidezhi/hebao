package com.example.dzcom.domain.repository.portfolio;

import com.example.dzcom.domain.model.portfolio.Position;

import java.util.List;

/** 模拟组合持仓仓储端口。 */
public interface PositionStore {
    /**
     * 保存或覆盖当前持仓。
     *
     * @param position 当前持仓领域对象
     * @return 保存后的持仓
     * @author dz
     * @date 2026-06-23
     */
    Position save(Position position);

    /**
     * 查询指定组合、产品和方向的当前持仓。
     *
     * @param portfolioBizId 组合业务唯一标识
     * @param productBizId 产品业务唯一标识
     * @param positionSide 持仓方向
     * @return 当前持仓
     * @author dz
     * @date 2026-06-23
     */
    java.util.Optional<Position> findByDimension(
        String portfolioBizId,
        String productBizId,
        String positionSide
    );

    /**
     * 查询组合当前有效持仓。
     *
     * @param portfolioBizId 组合业务唯一标识
     * @return 当前持仓集合
     * @author dz
     * @date 2026-06-23
     */
    List<Position> findByPortfolioBizId(String portfolioBizId);
}
