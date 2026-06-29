package com.example.dzcom.infrastructure.persistence.entity.system;

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

/** 系统配置持久化实体。 */
@Schema(description = "系统配置持久化实体")
@TableName("aiw_system_config")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SystemConfigEntity {
    /** 配置业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "配置业务唯一标识")
    private String bizId;
    @Schema(description = "配置分组编码", example = "AUTO_INVESTMENT_CLOSED_LOOP")
    private String configGroup;
    @Schema(description = "配置键名", example = "mockUserBizId")
    private String configKey;
    @Schema(description = "配置生效环境", example = "default")
    private String environment;
    @Schema(description = "配置值类型：STRING/NUMBER/BOOLEAN/JSON")
    private String valueType;
    @Schema(description = "配置值 JSON 字符串")
    private String configValue;
    @Schema(description = "配置用途说明")
    private String description;
    @Schema(description = "配置状态：ACTIVE/INACTIVE")
    private String status;
    @Schema(description = "配置版本号")
    private Integer version;
    @Schema(description = "记录创建时间，北京时间")
    private LocalDateTime createdAt;
    @Schema(description = "记录更新时间，北京时间")
    private LocalDateTime updatedAt;
}
