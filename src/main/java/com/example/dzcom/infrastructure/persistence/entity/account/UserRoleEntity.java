package com.example.dzcom.infrastructure.persistence.entity.account;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** 用户角色分配持久化实体，只保存角色结果，不承载权限定义。 */
@Schema(description = "持久化：用户角色分配表（aiw_user_role）")
@TableName("aiw_user_role")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserRoleEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "业务唯一标识")
    private String bizId;
    @Schema(description = "所属用户业务标识")
    private String userBizId;
    @Schema(description = "角色编码")
    private String roleCode;
    @Schema(description = "作用域编码（可选）")
    private String scopeCode;
    @Schema(description = "生效起始时间（北京时间）")
    private LocalDateTime effectiveFrom;
    @Schema(description = "生效截止时间（北京时间）")
    private LocalDateTime effectiveTo;
    @Schema(description = "创建时间（北京时间）")
    private LocalDateTime createdAt;
    @Schema(description = "创建者标识")
    private String createdBy;
    @TableField("is_deleted")
    @Schema(description = "逻辑删除标记（0/1）")
    private int deleted;
}
