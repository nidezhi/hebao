package com.example.dzcom.infrastructure.persistence.entity.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 自动投资闭环步骤持久化实体。 */
@Schema(description = "自动投资闭环步骤持久化实体")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClosedLoopStepEntity {
    @Schema(description = "步骤业务唯一标识")
    private String bizId;
    @Schema(description = "闭环运行业务标识")
    private String runBizId;
    @Schema(description = "步骤编码")
    private String stepCode;
    @Schema(description = "步骤名称")
    private String stepName;
    @Schema(description = "步骤顺序")
    private int stepOrder;
    @Schema(description = "步骤状态")
    private String stepStatus;
    @Schema(description = "输入摘要 JSON")
    private String inputSummary;
    @Schema(description = "输出摘要 JSON")
    private String outputSummary;
    @Schema(description = "失败或阻断原因")
    private String failureReason;
    @Schema(description = "开始时间")
    private LocalDateTime startedAt;
    @Schema(description = "完成时间")
    private LocalDateTime completedAt;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
