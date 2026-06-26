package com.example.dzcom.infrastructure.persistence.mapper.ai;

import com.example.dzcom.domain.repository.ai.AiModelBindingSearchCriteria;
import com.example.dzcom.infrastructure.persistence.entity.ai.AiModelBindingEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** AI 模型挂靠配置 MyBatis Mapper。 */
@Mapper
public interface AiModelBindingMapper {
    /** 新增或更新模型挂靠配置。 */
    int save(AiModelBindingEntity entity);

    /** 根据业务 ID 查询模型挂靠配置。 */
    AiModelBindingEntity selectByBizId(@Param("bizId") String bizId);

    /** 根据场景和环境查询模型挂靠配置。 */
    AiModelBindingEntity selectByScenarioAndEnvironment(
        @Param("scenarioCode") String scenarioCode,
        @Param("environment") String environment
    );

    /** 分页查询模型挂靠配置。 */
    List<AiModelBindingEntity> search(
        @Param("criteria") AiModelBindingSearchCriteria criteria,
        @Param("offset") int offset,
        @Param("sortColumn") String sortColumn
    );

    /** 统计模型挂靠配置数量。 */
    long count(@Param("criteria") AiModelBindingSearchCriteria criteria);
}
