package com.example.dzcom.domain.repository.ai;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.ai.AiModel;

import java.util.Optional;

/** AI 模型仓储端口。 */
public interface AiModelStore {
    /** 根据业务 ID 查询模型。 */
    Optional<AiModel> findByBizId(String bizId);

    /** 根据模型编码和版本查询模型。 */
    Optional<AiModel> findByCodeAndVersion(String modelCode, String modelVersion);

    /** 根据模型编码查询最近启用的 ACTIVE 版本。 */
    Optional<AiModel> findActiveByCode(String modelCode);

    /** 新增或更新模型配置。 */
    AiModel save(AiModel model);

    /** 分页查询模型。 */
    PageResult<AiModel> search(AiModelSearchCriteria criteria);
}
