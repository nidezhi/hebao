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

import java.time.LocalDateTime;

/** AI Prompt 模板持久化实体。 */
@Schema(description = "AI Prompt 模板持久化实体")
@TableName("aiw_ai_prompt_template")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiPromptTemplateEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "Prompt模板业务唯一标识")
    private String bizId;
    @Schema(description = "Prompt跨版本稳定编码")
    private String promptCode;
    @Schema(description = "Prompt版本号")
    private String promptVersion;
    @Schema(description = "使用场景")
    private String scenario;
    @Schema(description = "模板展示名称")
    private String templateName;
    @Schema(description = "模板内容")
    private String templateContent;
    @Schema(description = "生命周期状态")
    private String status;
    @Schema(description = "模板说明")
    private String description;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
    @Schema(description = "创建操作者")
    private String createdBy;
    @Schema(description = "更新操作者")
    private String updatedBy;
}
