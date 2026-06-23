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

/** 数据源质量快照持久化实体。 */
@Schema(description = "数据源质量快照持久化实体")
@TableName("aiw_data_quality_snapshot")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DataQualitySnapshotEntity {
    /** 数据质量快照业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "数据质量快照业务唯一标识")
    private String bizId;
    /** 数据源稳定编码。 */
    @Schema(description = "数据源稳定编码")
    private String sourceCode;
    /** 数据类型。 */
    @Schema(description = "数据类型")
    private String dataType;
    /** 综合质量分。 */
    @Schema(description = "综合质量分")
    private BigDecimal qualityScore;
    /** 缺失率。 */
    @Schema(description = "缺失率")
    private BigDecimal missingRate;
    /** 重复率。 */
    @Schema(description = "重复率")
    private BigDecimal duplicateRate;
    /** 新鲜度分。 */
    @Schema(description = "新鲜度分")
    private BigDecimal freshnessScore;
    /** 样本数。 */
    @Schema(description = "样本数")
    private int sampleCount;
    /** 快照时间。 */
    @Schema(description = "快照时间")
    private LocalDateTime snapshotTime;
    /** 质量评估上下文。 */
    @Schema(description = "质量评估上下文")
    private String detail;
    /** 创建时间。 */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
