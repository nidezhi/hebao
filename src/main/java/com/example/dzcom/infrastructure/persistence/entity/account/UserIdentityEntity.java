package com.example.dzcom.infrastructure.persistence.entity.account;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 用户登录标识持久化实体，用于保存用户名、邮箱、手机号等可认证标识。
 *
 * <p>{@code normalizedValue} 用于登录查询和唯一性判断，
 * {@code identityValue} 只用于经过权限控制后的展示。</p>
 */
@Schema(description = "持久化：用户登录标识表（aiw_user_identity）")
@TableName("aiw_user_identity")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserIdentityEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    @Schema(description = "业务唯一标识")
    private String bizId;
    @Schema(description = "所属用户业务标识")
    private String userBizId;
    @Schema(description = "标识类型")
    private String identityType;
    @Schema(description = "展示值（受权限控制）")
    private String identityValue;
    @Schema(description = "标准化值（用于唯一性与登录查询）")
    private String normalizedValue;
    @Schema(description = "是否已验证")
    private boolean verified;
    @Schema(description = "验证时间（北京时间）")
    private LocalDateTime verifiedAt;
    @Schema(description = "状态编码")
    private int status;
    @Schema(description = "创建时间（北京时间）")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间（北京时间）")
    private LocalDateTime updatedAt;
    @TableField("is_deleted")
    @Schema(description = "逻辑删除标记（0/1）")
    private int deleted;
}
