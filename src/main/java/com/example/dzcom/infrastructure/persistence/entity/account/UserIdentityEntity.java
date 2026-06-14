package com.example.dzcom.infrastructure.persistence.entity.account;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 用户登录标识持久化实体，用于保存用户名、邮箱、手机号等可认证标识。
 *
 * <p>{@code normalizedValue} 用于登录查询和唯一性判断，
 * {@code identityValue} 只用于经过权限控制后的展示。</p>
 */
@TableName("aiw_user_identity")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserIdentityEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    private String bizId;
    private String userBizId;
    private String identityType;
    private String identityValue;
    private String normalizedValue;
    private boolean verified;
    private LocalDateTime verifiedAt;
    private int status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableField("is_deleted")
    private int deleted;
}
