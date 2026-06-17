package com.example.dzcom.domain.model.account;

import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** 角色定义领域对象，承载角色状态和审计信息。 */
@Schema(description = "角色领域对象")
@Builder(toBuilder = true)
public record Role(
    @Schema(description = "角色业务标识") String bizId,
    @Schema(description = "角色编码") String roleCode,
    @Schema(description = "角色名称") String roleName,
    @Schema(description = "角色说明") String description,
    @Schema(description = "角色类型") String roleType,
    @Schema(description = "状态编码（1=启用,0=停用）") int status,
    @Schema(description = "乐观锁版本") int version,
    @Schema(description = "创建时间（北京时间）") LocalDateTime createdAt,
    @Schema(description = "更新时间（北京时间）") LocalDateTime updatedAt,
    @Schema(description = "创建者标识") String createdBy,
    @Schema(description = "最后修改者标识") String updatedBy,
    @Schema(description = "逻辑删除标记（0/1）") int deleted,
    @Schema(description = "删除时间（北京时间）") LocalDateTime deletedAt
) {
    /** 判断角色是否处于可授权状态。 */
    public boolean active() {
        return status == 1 && deleted == 0;
    }

    /** 判断角色是否为系统内置角色。 */
    public boolean systemRole() {
        return "SYSTEM".equals(roleType);
    }
}
