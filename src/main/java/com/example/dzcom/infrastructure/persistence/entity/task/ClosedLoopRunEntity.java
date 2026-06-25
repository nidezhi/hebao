package com.example.dzcom.infrastructure.persistence.entity.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 自动投资闭环运行持久化实体。 */
@Schema(description = "自动投资闭环运行持久化实体")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClosedLoopRunEntity {
    @Schema(description = "闭环运行业务唯一标识")
    private String bizId;
    @Schema(description = "闭环运行编号")
    private String runNo;
    @Schema(description = "来源任务编码")
    private String taskCode;
    @Schema(description = "触发来源")
    private String triggerSource;
    @Schema(description = "运行状态")
    private String runStatus;
    @Schema(description = "自动化等级")
    private String automationLevel;
    @Schema(description = "市场范围")
    private String marketScope;
    @Schema(description = "主题编码")
    private String themeCode;
    @Schema(description = "自动 Mock 用户业务标识")
    private String mockUserBizId;
    @Schema(description = "Mock 组合业务标识")
    private String portfolioBizId;
    @Schema(description = "报告业务标识")
    private String reportBizId;
    @Schema(description = "Prompt 业务标识")
    private String promptBizId;
    @Schema(description = "Prompt 编码")
    private String promptCode;
    @Schema(description = "Prompt 版本")
    private String promptVersion;
    @Schema(description = "回测业务标识")
    private String backtestBizId;
    @Schema(description = "质量分")
    private BigDecimal qualityScore;
    @Schema(description = "门禁结果")
    private String gateResult;
    @Schema(description = "运行摘要 JSON")
    private String summary;
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
