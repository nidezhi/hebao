package com.example.dzcom.infrastructure.persistence.entity.account;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/** 用户角色分配持久化实体，只保存角色结果，不承载权限定义。 */
@TableName("aiw_user_role")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserRoleEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    private String bizId;
    private String userBizId;
    private String roleCode;
    private String scopeCode;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private LocalDateTime createdAt;
    private String createdBy;
    @TableField("is_deleted")
    private int deleted;
}
