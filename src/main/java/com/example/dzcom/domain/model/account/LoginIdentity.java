package com.example.dzcom.domain.model.account;

import com.example.dzcom.domain.enums.account.IdentityType;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 登录标识领域对象，封装标识类型、原始展示值和用于唯一查询的标准化值。
 */
@Schema(description = "登录标识领域对象，包含原始值与标准化后的值")
@Builder(toBuilder = true)
public record LoginIdentity(
    @Schema(description = "标识业务标识") String bizId,
    @Schema(description = "所属用户业务标识") String userBizId,
    @Schema(description = "标识类型，例如 USERNAME/EMAIL/PHONE") IdentityType type,
    @Schema(description = "原始展示值") String value,
    @Schema(description = "用于唯一性查询的标准化值") String normalizedValue,
    @Schema(description = "是否已确认") boolean verified,
    @Schema(description = "是否激活") boolean active,
    @Schema(description = "创建时间（北京时间）") LocalDateTime createdAt,
    @Schema(description = "逻辑删除标记（0/1）") int deleted
) {
}
