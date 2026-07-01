package com.example.dzcom.interfaces.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;

/** AI 模型调用审计分页查询请求。 */
public record AiModelCallAuditListRequest(
    @Schema(description = "操作编码")
    String operationCode,
    @Schema(description = "调用状态")
    String callStatus,
    @Schema(description = "模型提供方")
    String providerCode,
    @Schema(description = "模型编码")
    String modelCode,
    @Schema(description = "模型版本")
    String modelVersion,
    @Schema(description = "业务类型")
    String businessType,
    @Schema(description = "业务对象标识")
    String businessBizId,
    @Schema(description = "任务编码")
    String taskCode,
    @Schema(description = "任务事件标识")
    String eventId,
    @Schema(description = "闭环运行标识")
    String runBizId,
    @Schema(description = "闭环运行号")
    String runNo,
    @Schema(description = "报告标识")
    String reportBizId,
    @Schema(description = "Prompt 编码")
    String promptCode,
    @Schema(description = "Skill 编码")
    String skillCode,
    @Schema(description = "业务场景")
    String scenarioCode,
    @Schema(description = "环境")
    String environment,
    Integer page,
    Integer size,
    String sort,
    String direction
) {
}
