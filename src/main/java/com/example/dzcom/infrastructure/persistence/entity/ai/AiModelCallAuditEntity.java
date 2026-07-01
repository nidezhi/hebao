package com.example.dzcom.infrastructure.persistence.entity.ai;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** AI 模型调用审计持久化实体。 */
@Schema(description = "AI 模型调用审计持久化实体")
@TableName("aiw_ai_model_call_audit")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiModelCallAuditEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    private String bizId;
    private String callId;
    private String operationCode;
    private String callStatus;
    private String providerCode;
    private String modelCode;
    private String modelVersion;
    private String remoteModel;
    private String endpoint;
    private String httpMethod;
    private Integer httpStatus;
    private Long durationMs;
    private String systemPromptHash;
    private String userPromptHash;
    private String responseHash;
    private String requestPreview;
    private String responsePreview;
    private String requestPayload;
    private String responsePayload;
    private String inputSummary;
    private String outputSummary;
    private String businessType;
    private String businessBizId;
    private String businessLabel;
    private String taskCode;
    private String eventId;
    private String runBizId;
    private String runNo;
    private String reportBizId;
    private String promptBizId;
    private String promptCode;
    private String promptVersion;
    private String skillBizId;
    private String skillCode;
    private String skillVersion;
    private String modelSkillBindingBizId;
    private String scenarioCode;
    private String environment;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
