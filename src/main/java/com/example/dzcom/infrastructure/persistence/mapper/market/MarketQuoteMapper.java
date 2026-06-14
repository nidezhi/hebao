package com.example.dzcom.infrastructure.persistence.mapper.market;

import com.example.dzcom.infrastructure.persistence.entity.market.MarketQuoteEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/** 行情 MyBatis Mapper。 */
@Mapper
public interface MarketQuoteMapper {
    /** 根据行情业务唯一键查询行情点。 */
    MarketQuoteEntity selectByUniqueKey(@Param("productBizId") String productBizId,
                                        @Param("sourceCode") String sourceCode,
                                        @Param("interval") String interval,
                                        @Param("quoteTime") LocalDateTime quoteTime);

    /** 查询指定产品、周期和可选数据源的最新有效行情。 */
    MarketQuoteEntity selectLatest(@Param("productBizId") String productBizId,
                                   @Param("interval") String interval,
                                   @Param("sourceCode") String sourceCode);

    /** 查询指定时间范围内的有效行情历史。 */
    List<MarketQuoteEntity> selectHistory(@Param("productBizId") String productBizId,
                                          @Param("interval") String interval,
                                          @Param("sourceCode") String sourceCode,
                                          @Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to,
                                          @Param("limit") int limit);

    /** 新增或更新行情点。 */
    int save(MarketQuoteEntity entity);
}
