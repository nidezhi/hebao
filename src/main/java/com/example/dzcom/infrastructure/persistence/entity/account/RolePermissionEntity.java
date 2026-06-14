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
    @TableId(value = "biz_id", type = IdType.INPUT)
    private String bizId;
    private String roleCode;
    private String permissionCode;
    private LocalDateTime createdAt;
    private String createdBy;
    @TableField("is_deleted")
    private int deleted;
    private LocalDateTime deletedAt;
}
