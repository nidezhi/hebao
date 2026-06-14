package com.example.dzcom.infrastructure.persistence.repository.market;

import com.example.dzcom.infrastructure.persistence.entity.market.MarketQuoteEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Spring Data 行情仓储，查询只返回有效或修正状态的数据。 */
public interface JpaMarketQuoteRepository extends JpaRepository<MarketQuoteEntity, String> {
    /**
     * 执行 find by product biz id and source code and quote interval and quote time 处理。
     *
     * @param productBizId 业务对象的唯一标识
     * @param sourceCode sourceCode 参数
     * @param quoteInterval quoteInterval 参数
     * @param quoteTime quoteTime 参数
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    Optional<MarketQuoteEntity> findByProductBizIdAndSourceCodeAndQuoteIntervalAndQuoteTime(
        String productBizId, String sourceCode, String quoteInterval, LocalDateTime quoteTime);

    /**
     * 根据指定条件查询业务数据。
     *
     * @param productBizId 业务对象的唯一标识
     * @param interval interval 参数
     * @param sourceCode sourceCode 参数
     * @param pageable pageable 参数
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    @Query("""
        select q from MarketQuoteEntity q
        where q.productBizId = :productBizId
          and q.quoteInterval = :interval
          and q.quoteStatus in (1, 2)
          and (:sourceCode is null or q.sourceCode = :sourceCode)
        order by q.quoteTime desc
        """)
    List<MarketQuoteEntity> findLatest(String productBizId, String interval,
                                       String sourceCode, Pageable pageable);

    /**
     * 根据查询条件获取业务数据列表。
     *
     * @param productBizId 业务对象的唯一标识
     * @param interval interval 参数
     * @param sourceCode sourceCode 参数
     * @param from from 参数
     * @param to to 参数
     * @param pageable pageable 参数
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    @Query("""
        select q from MarketQuoteEntity q
        where q.productBizId = :productBizId
          and q.quoteInterval = :interval
          and q.quoteStatus in (1, 2)
          and (:sourceCode is null or q.sourceCode = :sourceCode)
          and q.quoteTime between :from and :to
        order by q.quoteTime asc
        """)
    List<MarketQuoteEntity> findHistory(String productBizId, String interval, String sourceCode,
                                        LocalDateTime from, LocalDateTime to, Pageable pageable);
}
