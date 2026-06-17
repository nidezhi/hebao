package com.example.dzcom.domain.model.account;

import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** 用户偏好领域对象，值以合法 JSON 字符串表达。 */
@Schema(description = "用户偏好领域对象")
@Builder(toBuilder = true)
public record UserPreference(
    @Schema(description = "偏好业务标识") String bizId,
    @Schema(description = "所属用户业务标识") String userBizId,
    @Schema(description = "偏好键") String key,
    @Schema(description = "值类型") String valueType,
    @Schema(description = "JSON 文本值") String jsonValue,
    @Schema(description = "更新时间（北京时间）") LocalDateTime updatedAt,
    @Schema(description = "逻辑删除标记（0/1）") int deleted
) {
}
