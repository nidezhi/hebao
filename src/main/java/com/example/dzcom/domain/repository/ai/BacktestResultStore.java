package com.example.dzcom.domain.repository.ai;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.ai.BacktestResult;

import java.util.Optional;

/** 回测结果仓储端口。 */
public interface BacktestResultStore {
    /** 保存回测结果。 */
    BacktestResult save(BacktestResult result);

    /** 按业务 ID 查询回测结果。 */
    Optional<BacktestResult> findByBizId(String bizId);

    /** 分页查询回测结果。 */
    PageResult<BacktestResult> search(BacktestResultSearchCriteria criteria);
}
