package com.example.dzcom.domain.repository.system;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.system.SystemConfig;

import java.util.Optional;

/** 系统配置仓储。 */
public interface SystemConfigStore {
    /** 查询指定环境下启用的配置。 */
    Optional<SystemConfig> findEnabled(String configGroup, String configKey, String environment);

    /** 根据配置组、键名和环境查询配置。 */
    Optional<SystemConfig> findByKey(String configGroup, String configKey, String environment);

    /** 保存系统配置。 */
    SystemConfig save(SystemConfig config);

    /** 分页查询系统配置。 */
    PageResult<SystemConfig> search(SystemConfigSearchCriteria criteria);
}
