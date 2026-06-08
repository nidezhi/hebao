package com.example.dzcom.infrastructure.persistence.entity.account;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** 用户偏好持久化实体，偏好键受应用白名单约束，值以 JSON 保存。 */
@Entity
@Table(name = "aiw_user_preference")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserPreferenceEntity {
    @Id
    @Column(name = "biz_id", length = 36)
    private String bizId;
    @Column(name = "user_biz_id", nullable = false, length = 36)
    private String userBizId;
    @Column(name = "preference_key", nullable = false, length = 64)
    private String preferenceKey;
    @Column(name = "value_type", nullable = false, length = 16)
    private String valueType;
    @Column(name = "preference_value", nullable = false, columnDefinition = "json")
    private String preferenceValue;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT(1)")
    private int deleted;
}
