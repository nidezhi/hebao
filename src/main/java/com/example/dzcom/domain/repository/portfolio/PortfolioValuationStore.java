package com.example.dzcom.domain.repository.portfolio;

import com.example.dzcom.domain.model.portfolio.PortfolioValuation;

import java.util.List;
import java.util.Optional;

/** 模拟组合估值快照仓储端口。 */
public interface PortfolioValuationStore {
    /**
     * 保存组合估值快照。
     *
     * @param valuation 组合估值快照
     * @return 保存后的估值快照
     * @author dz
     * @date 2026-06-23
     */
    PortfolioValuation save(PortfolioValuation valuation);

    /**
     * 查询组合最新估值快照。
     *
     * @param portfolioBizId 组合业务唯一标识
     * @return 最新估值快照
     * @author dz
     * @date 2026-06-23
     */
    Optional<PortfolioValuation> findLatestByPortfolioBizId(String portfolioBizId);

    /**
     * 查询组合首个估值快照。
     *
     * @param portfolioBizId 组合业务唯一标识
     * @return 首个估值快照
     * @author dz
     * @date 2026-06-23
     */
    Optional<PortfolioValuation> findFirstByPortfolioBizId(String portfolioBizId);

    /**
     * 查询组合估值曲线。
     *
     * @param portfolioBizId 组合业务唯一标识
     * @param limit 返回数量上限
     * @return 按估值时间升序排列的估值快照
     * @author dz
     * @date 2026-06-23
     */
    List<PortfolioValuation> findHistoryByPortfolioBizId(String portfolioBizId, int limit);
}
