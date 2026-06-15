package com.example.dzcom.interfaces.dto.response.account;

import com.example.dzcom.application.dto.account.PreferenceView;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/** 接口层用户偏好响应。 */
@Builder
@Schema(description = "用户偏好响应")
public record PreferenceResponse(
    @Schema(description = "偏好键", example = "ui.theme") String key,
    @Schema(description = "值类型", example = "string") String valueType,
    @Schema(description = "任意 JSON 偏好值") JsonNode value,
    @Schema(description = "更新时间") LocalDateTime updatedAt
) {

    /**
     * 将应用层偏好视图转换为接口响应。
     *
     * @param source 应用层偏好视图
     * @return 接口层偏好响应
     * @author dz
     * @date 2026-06-15
     */
    public static PreferenceResponse from(PreferenceView source) {
        return PreferenceResponse.builder()
            .key(source.key())
            .valueType(source.valueType())
            .value(source.value())
            .updatedAt(source.updatedAt())
            .build();
    }
}
