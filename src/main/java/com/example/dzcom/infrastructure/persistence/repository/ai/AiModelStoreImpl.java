package com.example.dzcom.infrastructure.persistence.repository.ai;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.ai.AiModel;
import com.example.dzcom.domain.repository.ai.AiModelSearchCriteria;
import com.example.dzcom.domain.repository.ai.AiModelStore;
import com.example.dzcom.infrastructure.persistence.entity.ai.AiModelEntity;
import com.example.dzcom.infrastructure.persistence.mapper.ai.AiModelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** AI 模型仓储实现。 */
@Repository
@RequiredArgsConstructor
public class AiModelStoreImpl implements AiModelStore {
    private final AiModelMapper mapper;

    /** 根据业务 ID 查询模型。 */
    @Override
    public Optional<AiModel> findByBizId(String bizId) {
        return Optional.ofNullable(mapper.selectByBizId(bizId)).map(this::toDomain);
    }

    /** 根据模型编码和版本查询模型。 */
    @Override
    public Optional<AiModel> findByCodeAndVersion(String modelCode, String modelVersion) {
        return Optional.ofNullable(mapper.selectByCodeAndVersion(modelCode, modelVersion))
            .map(this::toDomain);
    }

    /**
     * 根据模型编码查询最近启用的 ACTIVE 版本。
     *
     * @param modelCode 模型跨版本稳定编码
     * @return 最近启用的模型版本；不存在时返回空
     * @author dz
     * @date 2026-06-18
     */
    @Override
    public Optional<AiModel> findActiveByCode(String modelCode) {
        return Optional.ofNullable(mapper.selectActiveByCode(modelCode))
            .map(this::toDomain);
    }

    /** 新增或更新模型配置。 */
    @Override
    public AiModel save(AiModel model) {
        mapper.save(toEntity(model));
        return model;
    }

    /** 分页查询模型。 */
    @Override
    public PageResult<AiModel> search(AiModelSearchCriteria criteria) {
        int offset = (criteria.page() - 1) * criteria.size();
        List<AiModel> items = mapper.search(criteria, offset, resolveSortColumn(criteria.sort()))
            .stream()
            .map(this::toDomain)
            .toList();
        long total = mapper.count(criteria);
        return PageResult.<AiModel>builder()
            .items(items)
            .total(total)
            .page(criteria.page())
            .size(criteria.size())
            .totalPages((int) Math.ceil((double) total / criteria.size()))
            .build();
    }

    /** 将接口排序字段转换为固定数据库列。 */
    private String resolveSortColumn(String sort) {
        return switch (sort) {
            case "modelCode" -> "m.model_code";
            case "modelVersion" -> "m.model_version";
            case "modelType" -> "m.model_type";
            case "provider" -> "m.provider";
            case "status" -> "m.status";
            case "activatedAt" -> "m.activated_at";
            default -> "m.updated_at";
        };
    }

    /** 将领域对象转换为持久化实体。 */
    private AiModelEntity toEntity(AiModel model) {
        return AiModelEntity.builder()
            .bizId(model.bizId())
            .modelCode(model.modelCode())
            .modelVersion(model.modelVersion())
            .modelName(model.modelName())
            .modelType(model.modelType())
            .provider(model.provider())
            .artifactUri(model.artifactUri())
            .modelConfig(model.modelConfig())
            .metrics(model.metrics())
            .status(model.status())
            .activatedAt(model.activatedAt())
            .retiredAt(model.retiredAt())
            .createdAt(model.createdAt())
            .updatedAt(model.updatedAt())
            .build();
    }

    /** 将持久化实体转换为领域对象。 */
    private AiModel toDomain(AiModelEntity entity) {
        return AiModel.builder()
            .bizId(entity.getBizId())
            .modelCode(entity.getModelCode())
            .modelVersion(entity.getModelVersion())
            .modelName(entity.getModelName())
            .modelType(entity.getModelType())
            .provider(entity.getProvider())
            .artifactUri(entity.getArtifactUri())
            .modelConfig(entity.getModelConfig())
            .metrics(entity.getMetrics())
            .status(entity.getStatus())
            .activatedAt(entity.getActivatedAt())
            .retiredAt(entity.getRetiredAt())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
