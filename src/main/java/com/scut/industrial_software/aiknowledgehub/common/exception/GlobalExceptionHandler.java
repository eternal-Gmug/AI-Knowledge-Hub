package com.scut.industrial_software.aiknowledgehub.common.exception;

import com.scut.industrial_software.aiknowledgehub.common.api.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 *
 * <p>约定：业务/参数/未知异常统一返回 HTTP 200 + {@link ApiResponse#fail(int, String)}，
 * 由响应体中的 {@code code} 字段区分成功与失败；认证失败的 401 由 Spring Security
 * 在过滤器链中单独处理（见 Step 1）。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    /** 处理 {@code @RequestBody} 校验失败（MethodArgumentNotValidException）与表单绑定校验失败（BindException）。 */
    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(ErrorCode.BAD_REQUEST.getMessage());
        log.warn("参数校验失败: {}", message);
        return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), message);
    }

    /** 处理 {@code @RequestParam}/{@code @PathVariable} 上的约束校验失败。 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getMessage())
                .orElse(ErrorCode.BAD_REQUEST.getMessage());
        log.warn("参数校验失败: {}", message);
        return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), message);
    }

    /** 处理请求体无法解析（如 JSON 格式错误）。 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return ApiResponse.fail(ErrorCode.BAD_REQUEST.getCode(), "请求体格式错误");
    }

    /** 兜底异常，避免内部细节泄露给客户端。 */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return ApiResponse.fail(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage());
    }
}
