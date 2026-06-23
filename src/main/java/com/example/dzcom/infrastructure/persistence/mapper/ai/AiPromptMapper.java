package com.example.dzcom.infrastructure.persistence.mapper.ai;

import com.example.dzcom.domain.repository.ai.AiPromptSearchCriteria;
import com.example.dzcom.infrastructure.persistence.entity.ai.AiPromptOutputSchemaEntity;
import com.example.dzcom.infrastructure.persistence.entity.ai.AiPromptTemplateEntity;
import com.example.dzcom.infrastructure.persistence.entity.ai.AiPromptVariableEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** AI Prompt MyBatis Mapper。 */
@Mapper
public interface AiPromptMapper {
    int saveTemplate(AiPromptTemplateEntity entity);

    int deleteVariables(@Param("promptBizId") String promptBizId);

    int insertVariable(AiPromptVariableEntity entity);

    int deleteOutputSchemas(@Param("promptBizId") String promptBizId);

    int insertOutputSchema(AiPromptOutputSchemaEntity entity);

    AiPromptTemplateEntity selectTemplateByBizId(@Param("bizId") String bizId);

    AiPromptTemplateEntity selectTemplateByCodeAndVersion(
        @Param("promptCode") String promptCode,
        @Param("promptVersion") String promptVersion
    );

    List<AiPromptVariableEntity> selectVariables(@Param("promptBizId") String promptBizId);

    List<AiPromptOutputSchemaEntity> selectOutputSchemas(@Param("promptBizId") String promptBizId);

    List<AiPromptTemplateEntity> search(
        @Param("criteria") AiPromptSearchCriteria criteria,
        @Param("offset") int offset,
        @Param("sortColumn") String sortColumn
    );

    long count(@Param("criteria") AiPromptSearchCriteria criteria);
}
