package com.example.dzcom.infrastructure.persistence.entity.account;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** 角色权限映射持久化实体。 */
@Schema(description = "持久化：角色权限映射表（aiw_role_permission）")
@TableName("aiw_role_permission")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RolePermissionEntity {
    /** 角色权限映射业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "业务唯一标识")
    private String bizId;
    /** 角色编码。 */
    @Schema(description = "角色编码")
    private String roleCode;
    /** 系统注册的权限编码。 */
    @Schema(description = "权限编码")
    private String permissionCode;
    /** 授权创建时间。 */
    @Schema(description = "授权创建时间（北京时间）")
    private LocalDateTime createdAt;
    /** 授权操作者。 */
    @Schema(description = "授权操作者标识")
    private String createdBy;
    /** 逻辑删除标记。 */
    @TableField("is_deleted")
    @Schema(description = "逻辑删除标记（0/1）")
    private int deleted;
    /** 权限撤销时间。 */
    @Schema(description = "权限撤销时间（北京时间）")
    private LocalDateTime deletedAt;
}
