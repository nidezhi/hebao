package com.example.dzcom.domain.repository.ai;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.ai.AiPromptOutputSchema;
import com.example.dzcom.domain.model.ai.AiPromptTemplate;
import com.example.dzcom.domain.model.ai.AiPromptVariable;

import java.util.List;
import java.util.Optional;

/** AI Prompt 版本化仓储端口。 */
public interface AiPromptStore {
    /** 保存 Prompt 模板。 */
    AiPromptTemplate saveTemplate(AiPromptTemplate template);

    /** 替换 Prompt 变量定义。 */
    void replaceVariables(String promptBizId, List<AiPromptVariable> variables);

    /** 替换 Prompt 输出 Schema。 */
    void replaceOutputSchemas(String promptBizId, List<AiPromptOutputSchema> schemas);

    /** 按业务 ID 查询 Prompt 模板。 */
    Optional<AiPromptTemplate> findTemplateByBizId(String bizId);

    /** 按编码和版本查询 Prompt 模板。 */
    Optional<AiPromptTemplate> findTemplateByCodeAndVersion(String promptCode, String promptVersion);

    /** 查询 Prompt 变量定义。 */
    List<AiPromptVariable> findVariables(String promptBizId);

    /** 查询 Prompt 输出 Schema。 */
    List<AiPromptOutputSchema> findOutputSchemas(String promptBizId);

    /** 分页查询 Prompt 模板。 */
    PageResult<AiPromptTemplate> search(AiPromptSearchCriteria criteria);
}
