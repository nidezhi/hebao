package com.example.dzcom.application.common.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一返回结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    
    /**
     * 响应码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
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
