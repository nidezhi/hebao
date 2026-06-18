package com.example.dzcom.application.dto.account;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** 返回给客户端的用户偏好视图。 */
@Builder
@Schema(description = "用户偏好应用层视图")
public record PreferenceView(
    @Schema(description = "用户偏好稳定键")
	String key,
    @Schema(description = "偏好值类型")
	String valueType,
    @Schema(description = "偏好 JSON 值")
	JsonNode value,
    @Schema(description = "偏好最后更新时间，北京时间")
	LocalDateTime updatedAt
) {
}
