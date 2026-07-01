package com.example.dzcom.interfaces.request.ai;

import jakarta.validation.constraints.NotBlank;

/** AI 模型调用审计详情请求。 */
public record AiModelCallAuditDetailRequest(
    @NotBlank(message = "bizId不能为空")
    String bizId
) {
}
