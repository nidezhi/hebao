package com.example.dzcom.infrastructure.persistence.mapper.portfolio;

import com.example.dzcom.infrastructure.persistence.entity.portfolio.OrderEventEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 模拟订单事件 MyBatis Mapper。 */
@Mapper
public interface OrderEventMapper {
    /** 新增模拟订单事件。 */
    int insert(OrderEventEntity entity);

    /** 查询订单事件。 */
    List<OrderEventEntity> selectByOrderBizId(@Param("orderBizId") String orderBizId);
}
