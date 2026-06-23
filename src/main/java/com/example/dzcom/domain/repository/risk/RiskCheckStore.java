package com.example.dzcom.domain.repository.risk;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.risk.RiskCheck;

/** 风险检查结果仓储端口。 */
public interface RiskCheckStore {
    /** 保存风险检查结果。 */
    RiskCheck save(RiskCheck riskCheck);

    /** 分页查询风险检查结果。 */
    PageResult<RiskCheck> search(RiskCheckSearchCriteria criteria);
}
