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
    Optional<MarketQuoteEntity> findByProductBizIdAndSourceCodeAndQuoteIntervalAndQuoteTime(
        String productBizId, String sourceCode, String quoteInterval, LocalDateTime quoteTime);

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
