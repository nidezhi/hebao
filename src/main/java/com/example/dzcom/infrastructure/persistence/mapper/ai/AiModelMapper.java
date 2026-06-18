package com.example.dzcom.infrastructure.persistence.mapper.ai;

import com.example.dzcom.domain.repository.ai.AiModelSearchCriteria;
import com.example.dzcom.infrastructure.persistence.entity.ai.AiModelEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** AI 模型 MyBatis Mapper。 */
@Mapper
public interface AiModelMapper {
    /** 根据业务 ID 查询模型。 */
    AiModelEntity selectByBizId(@Param("bizId") String bizId);

    /** 根据模型编码和版本查询模型。 */
    AiModelEntity selectByCodeAndVersion(@Param("modelCode") String modelCode,
                                         @Param("modelVersion") String modelVersion);

    /** 根据模型编码查询最近启用的 ACTIVE 版本。 */
    AiModelEntity selectActiveByCode(@Param("modelCode") String modelCode);

    /** 新增或更新模型。 */
    int save(AiModelEntity entity);

    /** 分页查询模型。 */
    List<AiModelEntity> search(@Param("criteria") AiModelSearchCriteria criteria,
                               @Param("offset") int offset,
                               @Param("sortColumn") String sortColumn);

    /** 统计模型数量。 */
    long count(@Param("criteria") AiModelSearchCriteria criteria);
}
