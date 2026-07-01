package com.example.dzcom.domain.repository.ai;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.ai.AiModelCallAudit;

import java.util.Optional;

/** AI 模型调用审计仓储，屏蔽持久化实现并暴露按调用/业务查询能力。 */
public interface AiModelCallAuditStore {
    /**
     * 保存或更新审计记录。
     *
     * @param audit 模型调用审计领域对象
     * @return 保存后的审计对象
     */
    AiModelCallAudit save(AiModelCallAudit audit);

    /**
     * 根据审计记录业务标识查询详情。
     *
     * @param bizId 审计记录业务唯一标识
     * @return 存在时返回审计详情
     */
    Optional<AiModelCallAudit> findByBizId(String bizId);

    /**
     * 根据模型调用追踪标识查询详情。
     *
     * @param callId 单次模型调用追踪标识
     * @return 存在时返回审计详情
     */
    Optional<AiModelCallAudit> findByCallId(String callId);

    /**
     * 分页查询审计记录。
     *
     * @param criteria 查询条件、分页和排序信息
     * @return 审计分页结果
     */
    PageResult<AiModelCallAudit> search(AiModelCallAuditSearchCriteria criteria);
}
