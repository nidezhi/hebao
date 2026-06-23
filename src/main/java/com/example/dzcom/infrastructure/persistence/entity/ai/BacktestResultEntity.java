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
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 回测结果持久化实体。 */
@Schema(description = "回测结果持久化实体")
@TableName("aiw_backtest_result")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BacktestResultEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "回测结果业务唯一标识")
    private String bizId;
    @Schema(description = "任务发起用户业务标识")
    private String ownerUserBizId;
    @Schema(description = "策略稳定编码")
    private String strategyCode;
    @Schema(description = "策略版本快照")
    private String strategyVersion;
    @Schema(description = "回测区间开始日期")
    private LocalDate startDate;
    @Schema(description = "回测区间结束日期")
    private LocalDate endDate;
    @Schema(description = "初始资金")
    private BigDecimal initialCapital;
    @Schema(description = "基准指数或比较对象编码")
    private String benchmarkCode;
    @Schema(description = "回测参数 JSON")
    private String parameters;
    @Schema(description = "回测指标 JSON")
    private String metrics;
    @Schema(description = "明细结果或报告存储地址")
    private String resultUri;
    @Schema(description = "回测状态")
    private String status;
    @Schema(description = "失败原因摘要")
    private String failureReason;
    @Schema(description = "任务开始时间")
    private LocalDateTime startedAt;
    @Schema(description = "任务完成时间")
    private LocalDateTime completedAt;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
