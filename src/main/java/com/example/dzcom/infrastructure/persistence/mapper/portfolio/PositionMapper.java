package com.example.dzcom.infrastructure.persistence.mapper.portfolio;

import com.example.dzcom.infrastructure.persistence.entity.portfolio.PositionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 模拟组合持仓 MyBatis Mapper。 */
@Mapper
public interface PositionMapper {
    /** 新增或更新当前持仓。 */
    int save(PositionEntity entity);

    /** 查询指定组合、产品和方向的当前持仓。 */
    PositionEntity selectByDimension(
        @Param("portfolioBizId") String portfolioBizId,
        @Param("productBizId") String productBizId,
        @Param("positionSide") String positionSide
    );

    /** 查询组合当前有效持仓。 */
    List<PositionEntity> selectByPortfolioBizId(@Param("portfolioBizId") String portfolioBizId);
}
