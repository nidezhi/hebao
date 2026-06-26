package com.example.dzcom.domain.repository.ai;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.ai.AiModelBinding;

import java.util.Optional;

/** AI 模型挂靠配置仓储端口。 */
public interface AiModelBindingStore {
    /**
     * 保存模型挂靠配置。
     *
     * @param binding 模型挂靠配置
     * @return 保存后的配置
     * @author dz
     * @date 2026-06-26
     */
    AiModelBinding save(AiModelBinding binding);

    /**
     * 根据业务 ID 查询模型挂靠配置。
     *
     * @param bizId 业务唯一标识
     * @return 模型挂靠配置
     * @author dz
     * @date 2026-06-26
     */
    Optional<AiModelBinding> findByBizId(String bizId);

    /**
     * 根据场景和环境查询模型挂靠配置。
     *
     * @param scenarioCode 场景编码
     * @param environment 生效环境
     * @return 模型挂靠配置
     * @author dz
     * @date 2026-06-26
     */
    Optional<AiModelBinding> findByScenarioAndEnvironment(String scenarioCode, String environment);

    /**
     * 分页查询模型挂靠配置。
     *
     * @param criteria 查询条件
     * @return 分页结果
     * @author dz
     * @date 2026-06-26
     */
    PageResult<AiModelBinding> search(AiModelBindingSearchCriteria criteria);
}
