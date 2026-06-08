package com.example.dzcom.common.exception;

import com.example.dzcom.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(Result.error(e.getCode(), e.getMessage()));
    }
    
    /**
     * 处理参数校验异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("参数校验失败");
        log.warn("参数校验异常: {}", message);
        return ResponseEntity.badRequest().body(Result.error(400, message));
    }
    
    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("参数绑定失败");
        log.warn("参数绑定异常: {}", message);
        return ResponseEntity.badRequest().body(Result.error(400, message));
    }

    /**
     * 处理领域对象对非法状态或非法数值的拒绝。
     *
     * <p>请求注解负责常规格式校验，领域校验负责跨字段和生命周期约束；
     * 两类错误都属于调用方可修正的 400 响应，不应被包装成系统异常。</p>
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Result<Void>> handleDomainValidationException(RuntimeException e) {
        log.warn("领域规则校验失败: {}", e.getMessage());
        return ResponseEntity.badRequest().body(Result.error(400, e.getMessage()));
    }

    /**
     * 数据库唯一约束是并发写入下的最终防线，冲突时返回稳定的 409，
     * 同时避免把 SQL 和索引细节暴露给调用方。
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Result<Void>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("数据唯一性或完整性冲突", e);
        return ResponseEntity.status(409).body(Result.error(409, "数据已存在或不满足完整性约束"));
    }
    
    /**
     * 处理静态资源未找到异常（如 favicon.ico）
     * 这类异常通常是浏览器自动请求导致的，不需要记录为错误
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
        // 仅在 debug 级别记录，避免日志污染
        log.debug("静态资源未找到: {}", e.getResourcePath());
        return ResponseEntity.status(404).body(Result.error(404, "资源不存在"));
    }
    
    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("系统异常: ", e);
        return ResponseEntity.internalServerError().body(Result.error(500, "系统错误，请联系管理员"));
    }
}
