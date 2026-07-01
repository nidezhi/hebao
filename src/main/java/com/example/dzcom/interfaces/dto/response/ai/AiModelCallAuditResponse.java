package com.example.dzcom.interfaces.dto.response.ai;

import com.example.dzcom.application.dto.ai.AiModelCallAuditView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** AI 模型调用审计响应，提供稳定展示字段和脱敏输入输出预览。 */
@Builder
@Schema(description = "AI 模型调用审计响应")
public record AiModelCallAuditResponse(
    @Schema(description = "审计记录业务唯一标识")
    String bizId,
    @Schema(description = "单次模型调用追踪标识")
    String callId,
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
    @Schema(description = "供应商侧模型名称")
    String remoteModel,
    @Schema(description = "脱敏后的调用端点")
    String endpoint,
    @Schema(description = "HTTP 方法")
    String httpMethod,
    @Schema(description = "远端 HTTP 状态")
    Integer httpStatus,
    @Schema(description = "调用耗时毫秒")
    Long durationMs,
    @Schema(description = "系统提示词 Hash")
    String systemPromptHash,
    @Schema(description = "用户提示词 Hash")
    String userPromptHash,
    @Schema(description = "模型输出 Hash")
    String responseHash,
    @Schema(description = "脱敏截断后的输入预览")
    String requestPreview,
    @Schema(description = "脱敏截断后的输出预览")
    String responsePreview,
    @Schema(description = "结构化输入摘要 JSON")
    String inputSummary,
    @Schema(description = "结构化输出摘要 JSON")
    String outputSummary,
    @Schema(description = "模型展示文本")
    String modelDisplay,
    @Schema(description = "业务对象展示文本")
    String businessDisplay,
    @Schema(description = "Prompt 展示文本")
    String promptDisplay,
    @Schema(description = "Skill 展示文本")
    String skillDisplay,
    @Schema(description = "耗时展示文本")
    String durationDisplay,
    @Schema(description = "失败原因展示文本")
    String failureDisplay,
    @Schema(description = "关联业务类型")
    String businessType,
    @Schema(description = "关联业务对象标识")
    String businessBizId,
    @Schema(description = "关联业务对象展示名称")
    String businessLabel,
    @Schema(description = "任务编码")
    String taskCode,
    @Schema(description = "任务事件标识")
    String eventId,
    @Schema(description = "闭环运行标识")
    String runBizId,
    @Schema(description = "闭环运行号")
    String runNo,
    @Schema(description = "投资报告标识")
    String reportBizId,
    @Schema(description = "Prompt 标识")
    String promptBizId,
    @Schema(description = "Prompt 编码")
    String promptCode,
    @Schema(description = "Prompt 版本")
    String promptVersion,
    @Schema(description = "AI Skill 标识")
    String skillBizId,
    @Schema(description = "AI Skill 编码")
    String skillCode,
    @Schema(description = "AI Skill 版本")
    String skillVersion,
    @Schema(description = "模型 Skill 绑定标识")
    String modelSkillBindingBizId,
    @Schema(description = "业务场景编码")
    String scenarioCode,
    @Schema(description = "运行环境")
    String environment,
    @Schema(description = "错误编码")
    String errorCode,
    @Schema(description = "错误消息")
    String errorMessage,
    @Schema(description = "创建时间")
    LocalDateTime createdAt,
    @Schema(description = "更新时间")
    LocalDateTime updatedAt
) {
    /**
     * 从应用视图转换为接口响应。
     *
     * @param view 应用层审计视图
     * @return 接口响应 DTO
     */
    public static AiModelCallAuditResponse from(AiModelCallAuditView view) {
        return AiModelCallAuditResponse.builder()
            .bizId(view.bizId())
            .callId(view.callId())
            .operationCode(view.operationCode())
            .callStatus(view.callStatus())
            .providerCode(view.providerCode())
            .modelCode(view.modelCode())
            .modelVersion(view.modelVersion())
            .remoteModel(view.remoteModel())
            .endpoint(view.endpoint())
            .httpMethod(view.httpMethod())
            .httpStatus(view.httpStatus())
            .durationMs(view.durationMs())
            .systemPromptHash(view.systemPromptHash())
            .userPromptHash(view.userPromptHash())
            .responseHash(view.responseHash())
            .requestPreview(view.requestPreview())
            .responsePreview(view.responsePreview())
            .inputSummary(view.inputSummary())
            .outputSummary(view.outputSummary())
            .modelDisplay(view.modelDisplay())
            .businessDisplay(view.businessDisplay())
            .promptDisplay(view.promptDisplay())
            .skillDisplay(view.skillDisplay())
            .durationDisplay(view.durationDisplay())
            .failureDisplay(view.failureDisplay())
            .businessType(view.businessType())
            .businessBizId(view.businessBizId())
            .businessLabel(view.businessLabel())
            .taskCode(view.taskCode())
            .eventId(view.eventId())
            .runBizId(view.runBizId())
            .runNo(view.runNo())
            .reportBizId(view.reportBizId())
            .promptBizId(view.promptBizId())
            .promptCode(view.promptCode())
            .promptVersion(view.promptVersion())
            .skillBizId(view.skillBizId())
            .skillCode(view.skillCode())
            .skillVersion(view.skillVersion())
            .modelSkillBindingBizId(view.modelSkillBindingBizId())
            .scenarioCode(view.scenarioCode())
            .environment(view.environment())
            .errorCode(view.errorCode())
            .errorMessage(view.errorMessage())
            .createdAt(view.createdAt())
            .updatedAt(view.updatedAt())
            .build();
    }
}
