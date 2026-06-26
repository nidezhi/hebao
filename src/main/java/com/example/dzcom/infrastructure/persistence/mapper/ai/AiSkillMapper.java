package com.example.dzcom.infrastructure.persistence.mapper.ai;

import com.example.dzcom.domain.repository.ai.AiSkillSearchCriteria;
import com.example.dzcom.infrastructure.persistence.entity.ai.AiSkillEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** AI Skill MyBatis Mapper。 */
@Mapper
public interface AiSkillMapper {
    /** 保存 Skill 版本。 */
    int save(AiSkillEntity entity);

    /** 根据业务 ID 查询 Skill。 */
    AiSkillEntity selectByBizId(@Param("bizId") String bizId);

    /** 根据编码和版本查询 Skill。 */
    AiSkillEntity selectByCodeAndVersion(@Param("skillCode") String skillCode,
                                         @Param("skillVersion") String skillVersion);

    /** 查询指定编码最近启用的 Skill 版本。 */
    AiSkillEntity selectActiveByCode(@Param("skillCode") String skillCode);

    /** 分页查询 Skill。 */
    List<AiSkillEntity> search(@Param("criteria") AiSkillSearchCriteria criteria,
                               @Param("offset") int offset,
                               @Param("sortColumn") String sortColumn);

    /** 统计 Skill 数量。 */
    long count(@Param("criteria") AiSkillSearchCriteria criteria);
}
