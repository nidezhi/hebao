package com.example.dzcom.application.dto.account;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

import java.time.LocalDateTime;

/** 返回给客户端的用户偏好视图。 */
@Builder
public record PreferenceView(String key, String valueType, JsonNode value, LocalDateTime updatedAt) {
}
