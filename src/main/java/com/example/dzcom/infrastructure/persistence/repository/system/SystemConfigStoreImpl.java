package com.example.dzcom.infrastructure.persistence.repository.system;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.system.SystemConfig;
import com.example.dzcom.domain.repository.system.SystemConfigStore;
import com.example.dzcom.domain.repository.system.SystemConfigSearchCriteria;
import com.example.dzcom.infrastructure.persistence.entity.system.SystemConfigEntity;
import com.example.dzcom.infrastructure.persistence.mapper.system.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** 系统配置仓储实现。 */
@Repository
@RequiredArgsConstructor
public class SystemConfigStoreImpl implements SystemConfigStore {
    private final SystemConfigMapper mapper;

    /** 查询指定环境下启用的配置。 */
    @Override
    public Optional<SystemConfig> findEnabled(String configGroup, String configKey, String environment) {
        return Optional.ofNullable(mapper.selectEnabled(configGroup, configKey, environment))
            .map(this::toDomain);
    }

    /** 根据配置组、键名和环境查询配置。 */
    @Override
    public Optional<SystemConfig> findByKey(String configGroup, String configKey, String environment) {
        return Optional.ofNullable(mapper.selectByKey(configGroup, configKey, environment))
            .map(this::toDomain);
    }

    /** 保存系统配置。 */
    @Override
    public SystemConfig save(SystemConfig config) {
        mapper.save(toEntity(config));
        return config;
    }

    /** 分页查询系统配置。 */
    @Override
    public PageResult<SystemConfig> search(SystemConfigSearchCriteria criteria) {
        int offset = (criteria.page() - 1) * criteria.size();
        List<SystemConfig> items = mapper.search(criteria, offset, resolveSortColumn(criteria.sort())).stream()
            .map(this::toDomain)
            .toList();
        long total = mapper.count(criteria);
        return PageResult.<SystemConfig>builder()
            .items(items)
            .total(total)
            .page(criteria.page())
            .size(criteria.size())
            .totalPages((int) Math.ceil((double) total / criteria.size()))
            .build();
    }

    private String resolveSortColumn(String sort) {
        return switch (sort) {
            case "configGroup" -> "c.config_group";
            case "configKey" -> "c.config_key";
            case "environment" -> "c.environment";
            case "valueType" -> "c.value_type";
            case "status" -> "c.status";
            default -> "c.updated_at";
        };
    }

    private SystemConfigEntity toEntity(SystemConfig config) {
        return SystemConfigEntity.builder()
            .bizId(config.bizId())
            .configGroup(config.configGroup())
            .configKey(config.configKey())
            .environment(config.environment())
            .valueType(config.valueType())
            .configValue(config.configValue())
            .description(config.description())
            .status(config.status())
            .version(config.version())
            .createdAt(config.createdAt())
            .updatedAt(config.updatedAt())
            .build();
    }

    private SystemConfig toDomain(SystemConfigEntity entity) {
        return SystemConfig.builder()
            .bizId(entity.getBizId())
            .configGroup(entity.getConfigGroup())
            .configKey(entity.getConfigKey())
            .environment(entity.getEnvironment())
            .valueType(entity.getValueType())
            .configValue(entity.getConfigValue())
            .description(entity.getDescription())
            .status(entity.getStatus())
            .version(entity.getVersion())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
