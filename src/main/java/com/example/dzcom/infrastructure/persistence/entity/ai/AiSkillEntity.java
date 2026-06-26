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

/** AI Skill 版本化持久化实体。 */
@Schema(description = "AI Skill 版本化持久化实体")
@TableName("aiw_ai_skill")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiSkillEntity {
    /** Skill 业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "Skill 业务唯一标识")
    private String bizId;
    /** 跨版本稳定 Skill 编码。 */
    @Schema(description = "跨版本稳定 Skill 编码")
    private String skillCode;
    /** Skill 版本号。 */
    @Schema(description = "Skill 版本号")
    private String skillVersion;
    /** Skill 展示名称。 */
    @Schema(description = "Skill 展示名称")
    private String skillName;
    /** Skill 类型。 */
    @Schema(description = "Skill 类型")
    private String skillType;
    /** 生命周期状态。 */
    @Schema(description = "生命周期状态")
    private String status;
    /** 给大模型的 Skill 指令内容。 */
    @Schema(description = "给大模型的 Skill 指令内容")
    private String instructionContent;
    /** 输入 JSON Schema。 */
    @Schema(description = "输入 JSON Schema")
    private String inputSchema;
    /** 输出 JSON Schema。 */
    @Schema(description = "输出 JSON Schema")
    private String outputSchema;
    /** 评估策略 JSON。 */
    @Schema(description = "评估策略 JSON")
    private String evaluationPolicy;
    /** Skill 说明。 */
    @Schema(description = "Skill 说明")
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
