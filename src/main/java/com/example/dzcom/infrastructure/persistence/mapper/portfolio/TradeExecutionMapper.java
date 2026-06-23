package com.example.dzcom.infrastructure.persistence.mapper.portfolio;

import com.example.dzcom.infrastructure.persistence.entity.portfolio.TradeExecutionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 模拟成交 MyBatis Mapper。 */
@Mapper
public interface TradeExecutionMapper {
    /** 新增模拟成交。 */
    int insert(TradeExecutionEntity entity);

    /** 查询订单第一笔成交。 */
    TradeExecutionEntity selectFirstByOrderBizId(@Param("orderBizId") String orderBizId);
}
