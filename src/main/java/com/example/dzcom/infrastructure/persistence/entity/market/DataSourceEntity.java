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

import java.time.LocalDateTime;

/** 数据源注册持久化实体。 */
@Schema(description = "数据源注册持久化实体")
@TableName("aiw_data_source")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DataSourceEntity {
    /** 数据源业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "数据源业务唯一标识")
    private String bizId;
    /** 数据源稳定编码。 */
    @Schema(description = "数据源稳定编码")
    private String sourceCode;
    /** 数据源展示名称。 */
    @Schema(description = "数据源展示名称")
    private String sourceName;
    /** 数据源类型。 */
    @Schema(description = "数据源类型")
    private String sourceType;
    /** 来源等级。 */
    @Schema(description = "来源等级")
    private String trustLevel;
    /** 数据源入口地址。 */
    @Schema(description = "数据源入口地址")
    private String baseUrl;
    /** 是否启用。 */
    @Schema(description = "是否启用")
    private boolean enabled;
    /** 采集频率。 */
    @Schema(description = "采集频率")
    private String fetchFrequency;
    /** 负责人。 */
    @Schema(description = "负责人")
    private String owner;
    /** 说明。 */
    @Schema(description = "说明")
    private String description;
    /** 创建时间。 */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    /** 更新时间。 */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
    /** 创建人。 */
    @Schema(description = "创建人")
    private String createdBy;
    /** 更新人。 */
    @Schema(description = "更新人")
    private String updatedBy;
}
