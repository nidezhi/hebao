package com.example.dzcom.infrastructure.persistence.mapper.portfolio;

import com.example.dzcom.infrastructure.persistence.entity.portfolio.PortfolioValuationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 模拟组合估值快照 MyBatis Mapper。 */
@Mapper
public interface PortfolioValuationMapper {
    /** 新增估值快照。 */
    int insert(PortfolioValuationEntity entity);

    /** 查询组合最新估值快照。 */
    PortfolioValuationEntity selectLatestByPortfolioBizId(@Param("portfolioBizId") String portfolioBizId);

    /** 查询组合首个估值快照。 */
    PortfolioValuationEntity selectFirstByPortfolioBizId(@Param("portfolioBizId") String portfolioBizId);

    /** 查询组合估值曲线。 */
    List<PortfolioValuationEntity> selectHistoryByPortfolioBizId(
        @Param("portfolioBizId") String portfolioBizId,
        @Param("limit") int limit
    );
}
