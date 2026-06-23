package com.example.dzcom.domain.repository.ai;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.ai.InvestmentFeedback;

import java.util.Optional;

/** 投资闭环反馈仓储端口。 */
public interface InvestmentFeedbackStore {
    /** 保存投资反馈。 */
    InvestmentFeedback save(InvestmentFeedback feedback);

    /** 按业务 ID 查询投资反馈。 */
    Optional<InvestmentFeedback> findByBizId(String bizId);

    /** 分页查询投资反馈。 */
    PageResult<InvestmentFeedback> search(InvestmentFeedbackSearchCriteria criteria);
}
