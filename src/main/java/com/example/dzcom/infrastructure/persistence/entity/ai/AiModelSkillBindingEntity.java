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

/** AI 模型实例与 Skill 版本绑定持久化实体。 */
@Schema(description = "AI 模型实例与 Skill 版本绑定持久化实体")
@TableName("aiw_ai_model_skill_binding")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiModelSkillBindingEntity {
    /** 绑定业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "绑定业务唯一标识")
    private String bizId;
    /** 模型业务唯一标识。 */
    @Schema(description = "模型业务唯一标识")
    private String modelBizId;
    /** 模型稳定编码。 */
    @Schema(description = "模型稳定编码")
    private String modelCode;
    /** 模型版本。 */
    @Schema(description = "模型版本")
    private String modelVersion;
    /** Skill 业务唯一标识。 */
    @Schema(description = "Skill 业务唯一标识")
    private String skillBizId;
    /** Skill 稳定编码。 */
    @Schema(description = "Skill 稳定编码")
    private String skillCode;
    /** Skill 版本。 */
    @Schema(description = "Skill 版本")
    private String skillVersion;
    /** 业务场景编码。 */
    @Schema(description = "业务场景编码")
    private String scenarioCode;
    /** 优先级。 */
    @Schema(description = "优先级")
    private Integer priority;
    /** 是否启用。 */
    @Schema(description = "是否启用")
    private boolean enabled;
    /** 场景级绑定配置 JSON。 */
    @Schema(description = "场景级绑定配置 JSON")
    private String config;
    /** 绑定说明。 */
    @Schema(description = "绑定说明")
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
