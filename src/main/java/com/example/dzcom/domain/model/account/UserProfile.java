package com.example.dzcom.domain.model.account;

import lombok.Builder;

/** 用户展示资料领域对象。 */
@Builder(toBuilder = true)
public record UserProfile(
    String bizId,
    String userBizId,
    String nickname,
    String avatarUrl,
    String locale,
    String timezone,
    boolean deleted
) {
}
