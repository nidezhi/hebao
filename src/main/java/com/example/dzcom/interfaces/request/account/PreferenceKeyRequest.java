package com.example.dzcom.interfaces.request.account;

import jakarta.validation.constraints.NotBlank;

/**
 * 按偏好键删除本人偏好的请求。
 */
public record PreferenceKeyRequest(@NotBlank String key) {
}
