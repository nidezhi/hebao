package com.example.dzcom.infrastructure.persistence.mapper.ai;

import com.example.dzcom.domain.repository.ai.AiModelSkillBindingSearchCriteria;
import com.example.dzcom.infrastructure.persistence.entity.ai.AiModelSkillBindingEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** AI 模型 Skill 绑定 MyBatis Mapper。 */
@Mapper
public interface AiModelSkillBindingMapper {
    /** 保存模型 Skill 绑定。 */
    int save(AiModelSkillBindingEntity entity);

    /** 根据业务 ID 查询绑定。 */
    AiModelSkillBindingEntity selectByBizId(@Param("bizId") String bizId);

    /** 根据模型、Skill 和场景查询绑定。 */
    AiModelSkillBindingEntity selectByModelSkillAndScenario(@Param("modelBizId") String modelBizId,
                                                            @Param("skillBizId") String skillBizId,
                                                            @Param("scenarioCode") String scenarioCode);

    /** 查询模型下启用的 Skill 绑定。 */
    List<AiModelSkillBindingEntity> selectEnabledByModelBizId(@Param("modelBizId") String modelBizId);

    /** 分页查询模型 Skill 绑定。 */
    List<AiModelSkillBindingEntity> search(@Param("criteria") AiModelSkillBindingSearchCriteria criteria,
                                           @Param("offset") int offset,
                                           @Param("sortColumn") String sortColumn);

    /** 统计绑定数量。 */
    long count(@Param("criteria") AiModelSkillBindingSearchCriteria criteria);
}
