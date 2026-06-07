package com.example.dzcom.common.exception;

import com.example.dzcom.common.enums.ResultCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final Integer code;
    private final HttpStatus httpStatus;
    
    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }
    
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.httpStatus = HttpStatus.resolve(code) == null ? HttpStatus.BAD_REQUEST : HttpStatus.valueOf(code);
    }
    
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.httpStatus = HttpStatus.valueOf(resultCode.getCode());
    }

    public BusinessException(HttpStatus httpStatus, String message) {
        super(message);
        this.code = httpStatus.value();
        this.httpStatus = httpStatus;
    }
}
