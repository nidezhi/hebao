package com.example.dzcom.interfaces.request.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 产品扩展属性写入请求。
 *
 * <p>{@code jsonValue} 必须是合法 JSON 文本；字符串值需要包含 JSON 引号，
 * 例如 {@code "张三"}，而数字可直接传 {@code 12.5}。</p>
 */
public record ProductAttributeRequest(
    @NotBlank String bizId,
    @NotBlank @Size(max = 64) String key,
    @NotBlank @Size(max = 16) String valueType,
    @NotBlank String jsonValue,
    LocalDate effectiveDate,
    @Size(max = 64) String sourceCode
) {
}
