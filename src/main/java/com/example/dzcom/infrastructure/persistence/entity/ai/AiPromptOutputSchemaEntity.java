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

/** AI Prompt 输出 Schema 持久化实体。 */
@Schema(description = "AI Prompt 输出 Schema 持久化实体")
@TableName("aiw_ai_prompt_output_schema")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiPromptOutputSchemaEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "输出Schema业务唯一标识")
    private String bizId;
    @Schema(description = "Prompt模板业务唯一标识")
    private String promptBizId;
    @Schema(description = "Schema版本号")
    private String schemaVersion;
    @Schema(description = "输出JSON Schema")
    private String schemaJson;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
