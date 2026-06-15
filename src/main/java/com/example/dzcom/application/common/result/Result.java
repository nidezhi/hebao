package com.example.dzcom.application.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一接口返回结果。
 *
 * @param <T> 业务数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一接口返回包装")
public class Result<T> {

    @Schema(description = "业务响应码，与 HTTP 状态语义保持一致", example = "200")
    private Integer code;

    @Schema(description = "响应消息；成功时通常为 success，失败时为可读错误信息", example = "success")
    private String message;

    @Schema(description = "业务响应数据；无返回数据时为 null")
    private T data;
    
    /**
     * 成功响应
     *
     * @param data data 参数
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
            .code(200)
            .message("success")
            .data(data)
            .build();
    }
    
    /**
     * 成功响应（无数据）
     *
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    public static <T> Result<T> success() {
        return success(null);
    }
    
    /**
     * 失败响应
     *
     * @param code code 参数
     * @param message message 参数
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    public static <T> Result<T> error(Integer code, String message) {
        return Result.<T>builder()
            .code(code)
            .message(message)
            .build();
    }
    
    /**
     * 失败响应（使用默认错误码）
     *
     * @param message message 参数
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}
