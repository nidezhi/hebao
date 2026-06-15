package com.example.dzcom.interfaces.request.account;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 偏好写入请求。
 *
 * <p>value 直接接收 JSON 基础类型、数组或对象，不要求客户端二次转义。</p>
 */
@Schema(description = "设置用户偏好请求，value 可为 JSON 基础类型、数组或对象")
public record PreferenceRequest(
    @Schema(description = "偏好键，必须属于服务端允许的白名单", example = "theme")
    @NotBlank String key,
    @Schema(description = "偏好值，可直接提交字符串、数字、布尔值、数组或对象")
    @NotNull JsonNode value
) {
}
