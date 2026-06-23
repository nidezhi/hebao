package com.example.dzcom.infrastructure.persistence.entity.market;

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
import java.time.LocalDateTime;

/** 数据源健康状态持久化实体。 */
@Schema(description = "数据源健康状态持久化实体")
@TableName("aiw_data_source_health")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DataSourceHealthEntity {
    /** 健康状态业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "健康状态业务唯一标识")
    private String bizId;
    /** 数据源稳定编码。 */
    @Schema(description = "数据源稳定编码")
    private String sourceCode;
    /** 最近成功采集时间。 */
    @Schema(description = "最近成功采集时间")
    private LocalDateTime lastSuccessAt;
    /** 最近失败时间。 */
    @Schema(description = "最近失败时间")
    private LocalDateTime lastFailureAt;
    /** 成功率。 */
    @Schema(description = "成功率")
    private BigDecimal successRate;
    /** 平均响应耗时。 */
    @Schema(description = "平均响应耗时")
    private Integer avgLatencyMs;
    /** 最近失败原因。 */
    @Schema(description = "最近失败原因")
    private String failureReason;
    /** 样本数量。 */
    @Schema(description = "样本数量")
    private int sampleCount;
    /** 更新时间。 */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
