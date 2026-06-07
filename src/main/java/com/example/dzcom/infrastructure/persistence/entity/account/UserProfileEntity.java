package com.example.dzcom.infrastructure.persistence.entity.account;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** 用户展示资料持久化实体，不承载登录标识、凭据和高敏感身份信息。 */
@Entity
@Table(name = "aiw_user_profile")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserProfileEntity {
    @Id
    @Column(name = "biz_id", length = 36)
    private String bizId;
    @Column(name = "user_biz_id", nullable = false, length = 36)
    private String userBizId;
    @Column(length = 64)
    private String nickname;
    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;
    @Column(nullable = false, length = 16)
    private String locale;
    @Column(nullable = false, length = 64)
    private String timezone;
    @Column(name = "profile_ext", columnDefinition = "json")
    private String profileExt;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;
}
