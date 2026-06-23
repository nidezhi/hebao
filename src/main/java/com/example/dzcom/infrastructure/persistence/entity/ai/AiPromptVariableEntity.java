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

/** AI Prompt 变量持久化实体。 */
@Schema(description = "AI Prompt 变量持久化实体")
@TableName("aiw_ai_prompt_variable")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiPromptVariableEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "变量业务唯一标识")
    private String bizId;
    @Schema(description = "Prompt模板业务唯一标识")
    private String promptBizId;
    @Schema(description = "变量名称")
    private String variableName;
    @Schema(description = "变量默认来源路径")
    private String sourcePath;
    @Schema(description = "是否必填")
    private boolean required;
    @Schema(description = "变量说明")
    private String description;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
