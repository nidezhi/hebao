package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 按偏好键删除本人偏好的请求。
 */
@Schema(description = "按偏好键删除本人偏好的请求")
public record PreferenceKeyRequest(@Schema(description = "偏好键", example = "ui.theme") @NotBlank String key) {
}
