package com.example.dzcom.infrastructure.persistence.entity.account;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/** 用户展示资料持久化实体，不承载登录标识、凭据和高敏感身份信息。 */
@TableName("aiw_user_profile")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserProfileEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    private String bizId;
    private String userBizId;
    private String nickname;
    private String avatarUrl;
    private String locale;
    private String timezone;
    private String profileExt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableField("is_deleted")
    private int deleted;
}
