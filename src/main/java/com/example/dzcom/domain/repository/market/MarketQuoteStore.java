package com.example.dzcom.domain.repository.market;

import com.example.dzcom.domain.model.market.MarketQuote;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 市场行情仓储端口。
 */
public interface MarketQuoteStore {
    /**
     * 保存或修正唯一行情点。 唯一键是产品、数据源、周期和行情时间；重复采集不得产生两条互相矛盾的数据。
     *
     * @param quote quote 参数
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    MarketQuote savePoint(MarketQuote quote);

    /**
     * 根据指定条件查询业务数据。
     *
     * @param productBizId 业务对象的唯一标识
     * @param interval interval 参数
     * @param sourceCode sourceCode 参数
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    Optional<MarketQuote> findLatest(String productBizId, String interval, String sourceCode);

    /**
     * 根据查询条件获取业务数据列表。
     *
     * @param productBizId 业务对象的唯一标识
     * @param interval interval 参数
     * @param sourceCode sourceCode 参数
     * @param from from 参数
     * @param to to 参数
     * @param limit 结果数量限制
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    List<MarketQuote> findHistory(String productBizId, String interval, String sourceCode,
                                  LocalDateTime from, LocalDateTime to, int limit);
}
