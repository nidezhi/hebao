package com.example.dzcom.application.common.exception;

import com.example.dzcom.application.common.enums.ResultCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final Integer code;
    private final HttpStatus httpStatus;
    
    /**
     * 创建并初始化 BusinessException 对象。
     *
     * @param message message 参数
     * @author dz
     * @date 2026-06-14
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }
    
    /**
     * 创建并初始化 BusinessException 对象。
     *
     * @param code code 参数
     * @param message message 参数
     * @author dz
     * @date 2026-06-14
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.httpStatus = HttpStatus.resolve(code) == null ? HttpStatus.BAD_REQUEST : HttpStatus.valueOf(code);
    }
    
    /**
     * 创建并初始化 BusinessException 对象。
     *
     * @param resultCode resultCode 参数
     * @author dz
     * @date 2026-06-14
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.httpStatus = HttpStatus.valueOf(resultCode.getCode());
    }

    /**
     * 创建并初始化 BusinessException 对象。
     *
     * @param httpStatus httpStatus 参数
     * @param message message 参数
     * @author dz
     * @date 2026-06-14
     */
    public BusinessException(HttpStatus httpStatus, String message) {
        super(message);
        this.code = httpStatus.value();
        this.httpStatus = httpStatus;
    }
}
