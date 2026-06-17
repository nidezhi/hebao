package com.example.dzcom.domain.repository.task;

import com.example.dzcom.domain.model.task.InvestmentTaskDefinition;

import java.util.List;
import java.util.Optional;

/** 投资定时任务定义仓储端口。 */
public interface InvestmentTaskDefinitionStore {
    /** 查询全部任务定义。 */
    List<InvestmentTaskDefinition> findAll();

    /** 根据稳定任务编码查询任务定义。 */
    Optional<InvestmentTaskDefinition> findByCode(String taskCode);

    /** 新增或更新任务定义。 */
    InvestmentTaskDefinition save(InvestmentTaskDefinition definition);
}
