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

/** 角色定义持久化实体。 */
@Schema(description = "持久化：角色定义表实体（aiw_role）")
@TableName("aiw_role")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RoleEntity {
    /** 角色业务唯一标识。 */
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "业务唯一标识")
    private String bizId;
    /** 稳定角色编码。 */
    @Schema(description = "角色编码")
    private String roleCode;
    /** 角色显示名称。 */
    @Schema(description = "角色显示名称")
    private String roleName;
    /** 角色职责和授权边界说明。 */
    @Schema(description = "角色说明")
    private String description;
    /** 角色类型：SYSTEM 或 CUSTOM。 */
    @Schema(description = "角色类型：SYSTEM 或 CUSTOM")
    private String roleType;
    /** 启用状态：0-停用，1-启用。 */
    @Schema(description = "启用状态：0-停用，1-启用")
    private int status;
    /** 乐观锁版本号。 */
    @Schema(description = "乐观锁版本号")
    private int version;
    /** 创建时间。 */
    @Schema(description = "创建时间（北京时间）")
    private LocalDateTime createdAt;
    /** 最后更新时间。 */
    @Schema(description = "最后更新时间（北京时间）")
    private LocalDateTime updatedAt;
    /** 创建操作者。 */
    @Schema(description = "创建操作者标识")
    private String createdBy;
    /** 最后更新操作者。 */
    @Schema(description = "最后更新操作者标识")
    private String updatedBy;
    /** 逻辑删除标记。 */
    @TableField("is_deleted")
    @Schema(description = "逻辑删除标记（0/1）")
    private int deleted;
    /** 逻辑删除时间。 */
    @Schema(description = "删除时间（北京时间）")
    private LocalDateTime deletedAt;
}
