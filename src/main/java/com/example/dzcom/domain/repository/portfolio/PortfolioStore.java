package com.example.dzcom.domain.repository.portfolio;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.portfolio.Portfolio;

import java.util.Optional;

/** 投资组合仓储端口。 */
public interface PortfolioStore {
    /**
     * 保存投资组合。
     *
     * @param portfolio 投资组合领域对象
     * @return 保存后的投资组合
     * @author dz
     * @date 2026-06-23
     */
    Portfolio save(Portfolio portfolio);

    /**
     * 根据业务标识查询未删除组合。
     *
     * @param bizId 组合业务唯一标识
     * @return 组合领域对象
     * @author dz
     * @date 2026-06-23
     */
    Optional<Portfolio> findByBizId(String bizId);

    /**
     * 分页查询当前用户的模拟组合。
     *
     * @param criteria 组合查询条件
     * @return 组合分页结果
     * @author dz
     * @date 2026-06-23
     */
    PageResult<Portfolio> search(PortfolioSearchCriteria criteria);
}
