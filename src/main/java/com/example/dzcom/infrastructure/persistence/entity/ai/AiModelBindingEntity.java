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

/** AI 模型业务场景挂靠配置持久化实体。 */
@Schema(description = "AI 模型业务场景挂靠配置持久化实体")
@TableName("aiw_ai_model_binding")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiModelBindingEntity {
    /** 配置业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "配置业务唯一标识")
    private String bizId;
    /** 业务场景编码。 */
    @Schema(description = "业务场景编码", example = "DATA_SOURCE_DISCOVERY")
    private String scenarioCode;
    /** 业务场景展示名称。 */
    @Schema(description = "业务场景展示名称", example = "数据源AI发现")
    private String scenarioName;
    /** 模型稳定编码。 */
    @Schema(description = "模型稳定编码", example = "openai-compatible-analysis")
    private String modelCode;
    /** 模型提供方一致性校验编码。 */
    @Schema(description = "模型提供方一致性校验编码", example = "OPENAI_COMPATIBLE")
    private String providerCode;
    /** 生效环境。 */
    @Schema(description = "生效环境", example = "DEFAULT")
    private String environment;
    /** 是否启用。 */
    @Schema(description = "是否启用")
    private boolean enabled;
    /** 场景级配置 JSON。 */
    @Schema(description = "场景级配置 JSON")
    private String config;
    /** 配置说明。 */
    @Schema(description = "配置说明")
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
