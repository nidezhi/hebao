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

/** 角色定义持久化实体。 */
@TableName("aiw_role")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RoleEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    private String bizId;
    private String roleCode;
    private String roleName;
    private String description;
    private String roleType;
    private int status;
    private int version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    @TableField("is_deleted")
    private int deleted;
    private LocalDateTime deletedAt;
}
