package com.example.dzcom.domain.model.account;

import lombok.Builder;

import java.time.LocalDateTime;

/** 用户偏好领域对象，值以合法 JSON 字符串表达。 */
@Builder(toBuilder = true)
public record UserPreference(
    String bizId,
    String userBizId,
    String key,
    String valueType,
    String jsonValue,
    LocalDateTime updatedAt,
    boolean deleted
) {
}
