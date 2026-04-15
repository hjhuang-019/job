package com.example.jobplatform.common;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException exception) {
        return Result.fail(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldError() != null
                ? exception.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        return Result.fail(400, message);
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException exception) {
        String message = exception.getBindingResult().getFieldError() != null
                ? exception.getBindingResult().getFieldError().getDefaultMessage()
                : "参数绑定失败";
        return Result.fail(400, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        log.warn("Request body parse failed", exception);
        return Result.fail(400, "请求体 JSON 解析失败，请确认 pageContext 为对象、priorTurns 为对象数组。详情见服务端日志。");
    }

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public Result<Void> handleHttpMessageNotWritableException(HttpMessageNotWritableException exception) {
        log.error("Response body write failed", exception);
        return Result.fail(500, "响应序列化失败（多为返回数据含无法转 JSON 的字段），请查看服务端日志。");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        log.warn("Path/query type mismatch: {}", exception.getMessage());
        return Result.fail(400, "路径或查询参数类型不正确");
    }

    /**
     * Spring MVC 在没有任何 Controller 匹配该路径时，会尝试按静态资源解析并抛出此异常。
     * 常见于：后端未重新编译/重启，仍运行旧版本（无新接口映射）。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNoResourceFoundException(NoResourceFoundException exception, HttpServletRequest request) {
        log.warn("No handler for {} {}", request.getMethod(), request.getRequestURI());
        return Result.fail(404, "接口不存在或未发布到当前进程，请重新编译并重启后端后再试。");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        log.warn("Database constraint violation", exception);
        String message = resolveConstraintMessage(exception);
        return Result.fail(400, message);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception, HttpServletRequest request) {
        log.error("Request failed: {}", request.getRequestURI(), exception);
        Throwable root = NestedExceptionUtils.getMostSpecificCause(exception);
        String rootName = root.getClass().getSimpleName();
        String rootMsg = root.getMessage();
        String suffix = rootName;
        if (rootMsg != null && !rootMsg.isBlank()) {
            String shortMsg = rootMsg.length() > 160 ? rootMsg.substring(0, 160) + "…" : rootMsg;
            suffix = rootName + "：" + shortMsg;
        }
        return Result.fail(500, "服务器内部异常（" + suffix + "）。完整堆栈见控制台/日志。");
    }

    private String resolveConstraintMessage(DataIntegrityViolationException exception) {
        Throwable root = exception;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        String detail = root.getMessage() != null ? root.getMessage().toLowerCase() : "";

        if (detail.contains("uk_sys_user_username")) {
            return "用户名已存在";
        }
        if (detail.contains("uk_sys_user_phone")) {
            return "手机号已存在";
        }
        if (detail.contains("uk_job_seeker_profile_user_id")) {
            return "求职者资料已存在，请勿重复创建";
        }
        if (detail.contains("uk_enterprise_profile_user_id")) {
            return "企业资料已存在，请勿重复创建";
        }
        if (detail.contains("accept_remote")) {
            return "求职者初始资料创建失败，请稍后重试";
        }
        return "数据已存在或不符合约束条件";
    }
}
