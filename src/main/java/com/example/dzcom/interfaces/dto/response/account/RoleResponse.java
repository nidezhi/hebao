package com.example.dzcom.interfaces.dto.response.account;

import com.example.dzcom.application.dto.account.RoleView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

/** 接口层角色响应。 */
@Builder
@Schema(description = "角色响应，包含角色定义、权限集合和启用状态")
public record RoleResponse(
    @Schema(description = "角色编码", example = "USER") String roleCode,
    @Schema(description = "角色名称", example = "普通用户") String roleName,
    @Schema(description = "角色说明") String description,
    @Schema(description = "角色类型", example = "CUSTOM") String roleType,
    @Schema(description = "是否启用", example = "true") boolean enabled,
    @Schema(description = "权限编码集合") Set<String> permissions,
    @Schema(description = "创建时间") LocalDateTime createdAt,
    @Schema(description = "最后更新时间") LocalDateTime updatedAt
) {

    /**
     * 将应用层角色视图转换为接口响应。
     *
     * @param source 应用层角色视图
     * @return 接口层角色响应
     * @author dz
     * @date 2026-06-15
     */
    public static RoleResponse from(RoleView source) {
        return RoleResponse.builder()
            .roleCode(source.roleCode())
            .roleName(source.roleName())
            .description(source.description())
            .roleType(source.roleType())
            .enabled(source.enabled())
            .permissions(source.permissions())
            .createdAt(source.createdAt())
            .updatedAt(source.updatedAt())
            .build();
    }
}
