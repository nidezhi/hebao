package com.example.dzcom.interfaces.dto.response.account;

import com.example.dzcom.application.service.account.PermissionCodes;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/** 接口层权限目录响应。 */
@Builder
@Schema(description = "权限目录响应，用于前端结构化权限选择器")
public record PermissionCatalogResponse(
    @Schema(description = "权限编码", example = "account:user:read") String permissionCode,
    @Schema(description = "展示名称", example = "查看用户") String displayName,
    @Schema(description = "权限分组", example = "账户与用户") String groupName,
    @Schema(description = "权限说明") String description,
    @Schema(description = "风险等级", example = "LOW") String riskLevel,
    @Schema(description = "权限状态", example = "ACTIVE") String status
) {

    /**
     * 将权限注册表条目转换为接口响应。
     *
     * @param source 权限注册表条目
     * @return 接口层权限目录响应
     * @author dz
     * @date 2026-06-28
     */
    public static PermissionCatalogResponse from(PermissionCodes.PermissionDescriptor source) {
        return PermissionCatalogResponse.builder()
            .permissionCode(source.permissionCode())
            .displayName(source.displayName())
            .groupName(source.groupName())
            .description(source.description())
            .riskLevel(source.riskLevel())
            .status(source.status())
            .build();
    }
}
