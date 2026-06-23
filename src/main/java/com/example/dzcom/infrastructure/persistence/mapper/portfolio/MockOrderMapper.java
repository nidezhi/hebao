package com.example.dzcom.infrastructure.persistence.mapper.portfolio;

import com.example.dzcom.infrastructure.persistence.entity.portfolio.MockOrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 模拟订单 MyBatis Mapper。 */
@Mapper
public interface MockOrderMapper {
    /** 新增或更新模拟订单。 */
    int save(MockOrderEntity entity);

    /** 按用户和幂等键查询未删除订单。 */
    MockOrderEntity selectByUserAndIdempotencyKey(
        @Param("userBizId") String userBizId,
        @Param("idempotencyKey") String idempotencyKey
    );
}
