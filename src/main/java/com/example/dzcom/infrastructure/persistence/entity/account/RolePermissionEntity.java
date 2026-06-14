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

import java.time.LocalDateTime;

/** 角色权限映射持久化实体。 */
@TableName("aiw_role_permission")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RolePermissionEntity {
    /** 角色权限映射业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    private String bizId;
    /** 角色编码。 */
    private String roleCode;
    /** 系统注册的权限编码。 */
    private String permissionCode;
    /** 授权创建时间。 */
    private LocalDateTime createdAt;
    /** 授权操作者。 */
    private String createdBy;
    /** 逻辑删除标记。 */
    @TableField("is_deleted")
    private int deleted;
    /** 权限撤销时间。 */
    private LocalDateTime deletedAt;
}
