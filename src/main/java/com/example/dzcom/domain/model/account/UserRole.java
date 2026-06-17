package com.example.dzcom.domain.model.account;

import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** 用户角色分配领域对象。 */
@Schema(description = "用户角色分配领域对象")
@Builder(toBuilder = true)
public record UserRole(
    @Schema(description = "业务标识") String bizId,
    @Schema(description = "用户业务标识") String userBizId,
    @Schema(description = "角色编码") String roleCode,
    @Schema(description = "作用域编码（可选）") String scopeCode,
    @Schema(description = "生效起始时间（北京时间）") LocalDateTime effectiveFrom,
    @Schema(description = "生效截止时间（北京时间）") LocalDateTime effectiveTo,
    @Schema(description = "创建者标识") String createdBy,
    @Schema(description = "逻辑删除标记（0/1）") int deleted
) {
}
