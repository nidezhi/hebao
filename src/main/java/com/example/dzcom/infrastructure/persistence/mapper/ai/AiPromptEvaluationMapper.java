package com.example.dzcom.infrastructure.persistence.mapper.ai;

import com.example.dzcom.domain.repository.ai.AiPromptEvaluationSearchCriteria;
import com.example.dzcom.infrastructure.persistence.entity.ai.AiPromptEvaluationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** AI Prompt 评估 MyBatis Mapper。 */
@Mapper
public interface AiPromptEvaluationMapper {
    int insert(AiPromptEvaluationEntity entity);

    AiPromptEvaluationEntity selectByBizId(@Param("bizId") String bizId);

    List<AiPromptEvaluationEntity> search(
        @Param("criteria") AiPromptEvaluationSearchCriteria criteria,
        @Param("offset") int offset,
        @Param("sortColumn") String sortColumn
    );

    long count(@Param("criteria") AiPromptEvaluationSearchCriteria criteria);
}
