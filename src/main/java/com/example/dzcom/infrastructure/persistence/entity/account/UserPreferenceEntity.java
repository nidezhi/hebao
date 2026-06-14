package com.example.dzcom.infrastructure.persistence.entity.account;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/** 用户偏好持久化实体，偏好键受应用白名单约束，值以 JSON 保存。 */
@TableName("aiw_user_preference")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserPreferenceEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    private String bizId;
    private String userBizId;
    private String preferenceKey;
    private String valueType;
    private String preferenceValue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableField("is_deleted")
    private int deleted;
}
