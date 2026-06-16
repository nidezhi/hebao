package com.example.dzcom.domain.repository.task;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.task.InvestmentThemeSnapshot;

/** 投资主题快照仓储端口。 */
public interface InvestmentThemeSnapshotStore {
    /** 保存一条主题分析快照。 */
    InvestmentThemeSnapshot save(InvestmentThemeSnapshot snapshot);

    /** 根据筛选条件分页查询主题快照。 */
    PageResult<InvestmentThemeSnapshot> search(InvestmentThemeSnapshotSearchCriteria criteria);
}
