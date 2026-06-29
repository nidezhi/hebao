package com.example.dzcom.infrastructure.persistence.mapper.system;

import com.example.dzcom.domain.repository.system.SystemConfigSearchCriteria;
import com.example.dzcom.infrastructure.persistence.entity.system.SystemConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 系统配置 MyBatis Mapper。 */
@Mapper
public interface SystemConfigMapper {
    /** 新增或更新系统配置。 */
    int save(SystemConfigEntity entity);

    /** 查询指定环境下启用的配置。 */
    SystemConfigEntity selectEnabled(
        @Param("configGroup") String configGroup,
        @Param("configKey") String configKey,
        @Param("environment") String environment
    );

    /** 根据配置组、键名和环境查询配置。 */
    SystemConfigEntity selectByKey(
        @Param("configGroup") String configGroup,
        @Param("configKey") String configKey,
        @Param("environment") String environment
    );

    /** 分页查询系统配置。 */
    List<SystemConfigEntity> search(
        @Param("criteria") SystemConfigSearchCriteria criteria,
        @Param("offset") int offset,
        @Param("sortColumn") String sortColumn
    );

    /** 统计系统配置数量。 */
    long count(@Param("criteria") SystemConfigSearchCriteria criteria);
}
