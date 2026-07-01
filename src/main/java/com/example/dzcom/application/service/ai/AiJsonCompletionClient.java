package com.example.dzcom.application.service.ai;

import com.example.dzcom.application.dto.ai.AiModelCallAuditContext;
import com.example.dzcom.application.dto.ai.AiModelRuntimeConfig;

/** AI JSON 补全客户端端口，用于非投资报告类的大模型结构化调用。 */
public interface AiJsonCompletionClient {
    /**
     * 判断当前客户端是否支持指定模型 Provider。
     *
     * @param providerCode 模型 Provider 编码
     * @return 支持时返回 true
     * @author dz
     * @date 2026-06-27
     */
    boolean supports(String providerCode);

    /**
     * 调用大模型并要求返回 JSON 对象文本。
     *
     * @param operationCode 操作编码，用于错误定位和审计
     * @param systemPrompt 系统提示词
     * @param userPrompt 用户提示词
     * @param modelConfig 模型运行时配置
     * @return 模型返回的 JSON 对象文本
     * @author dz
     * @date 2026-06-27
     */
    String completeJson(
        String operationCode,
        String systemPrompt,
        String userPrompt,
        AiModelRuntimeConfig modelConfig
    );

    /**
     * 调用大模型并携带业务审计上下文。
     *
     * @param operationCode 操作编码，用于错误定位和审计
     * @param systemPrompt 系统提示词
     * @param userPrompt 用户提示词
     * @param modelConfig 模型运行时配置
     * @param auditContext 调用关联的业务上下文
     * @return 模型返回的 JSON 对象文本
     * @author dz
     * @date 2026-07-01
     */
    default String completeJson(
        String operationCode,
        String systemPrompt,
        String userPrompt,
        AiModelRuntimeConfig modelConfig,
        AiModelCallAuditContext auditContext
    ) {
        return completeJson(operationCode, systemPrompt, userPrompt, modelConfig);
    }
}
