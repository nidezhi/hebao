package com.example.dzcom.interfaces.request.account;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/** 设置用户偏好的请求，值允许 JSON 基础类型或小型对象。 */
@Builder
public record PreferenceRequest(@NotBlank String key, @NotNull JsonNode value) {
}
