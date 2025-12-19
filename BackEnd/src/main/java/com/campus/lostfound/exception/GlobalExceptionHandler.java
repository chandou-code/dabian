package com.campus.lostfound.exception;

import com.campus.lostfound.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;

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
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return Result.error(e.getMessage());
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("参数验证异常", e);
        
        StringBuilder errorMsg = new StringBuilder("参数验证失败: ");
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        
        for (int i = 0; i < fieldErrors.size(); i++) {
            FieldError error = fieldErrors.get(i);
            errorMsg.append(error.getField())
                    .append(" ")
                    .append(error.getDefaultMessage());
            
            if (i < fieldErrors.size() - 1) {
                errorMsg.append("; ");
            }
        }
        
        return Result.error(errorMsg.toString());
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleBindException(BindException e) {
        log.error("参数绑定异常", e);
        
        StringBuilder errorMsg = new StringBuilder("参数绑定失败: ");
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        
        for (int i = 0; i < fieldErrors.size(); i++) {
            FieldError error = fieldErrors.get(i);
            errorMsg.append(error.getField())
                    .append(" ")
                    .append(error.getDefaultMessage());
            
            if (i < fieldErrors.size() - 1) {
                errorMsg.append("; ");
            }
        }
        
        return Result.error(errorMsg.toString());
    }

    /**
     * 处理参数约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("参数约束违反异常", e);
        
        StringBuilder errorMsg = new StringBuilder("参数验证失败: ");
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        
        int i = 0;
        for (ConstraintViolation<?> violation : violations) {
            errorMsg.append(violation.getPropertyPath().toString())
                    .append(" ")
                    .append(violation.getMessage());
            
            if (i < violations.size() - 1) {
                errorMsg.append("; ");
            }
            i++;
        }
        
        return Result.error(errorMsg.toString());
    }

    /**
     * 处理IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("非法参数异常", e);
        return Result.error("参数错误: " + e.getMessage());
    }

    /**
     * 处理RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常", e);
        return Result.error("系统内部错误: " + e.getMessage());
    }

    /**
     * 处理通用Exception
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error("系统繁忙，请稍后再试");
    }



    /**
     * 处理403禁止访问异常
     */
    @ExceptionHandler({SecurityException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Object> handleForbiddenException(SecurityException e) {
        log.error("禁止访问", e);
        return Result.error("权限不足");
    }

    /**
     * 处理404资源不存在异常
     */
    @ExceptionHandler({org.springframework.web.servlet.NoHandlerFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Object> handleNotFoundException(Exception e) {
        log.error("资源不存在", e);
        return Result.error("请求的资源不存在");
    }

    /**
     * 处理HTTP请求方法不支持异常
     */
    @ExceptionHandler({org.springframework.web.HttpRequestMethodNotSupportedException.class})
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Object> handleMethodNotSupportedException(Exception e) {
        log.error("请求方法不支持", e);
        return Result.error("请求方法不支持");
    }

    /**
     * 处理HTTP媒体类型不支持异常
     */
    @ExceptionHandler({org.springframework.web.HttpMediaTypeNotSupportedException.class})
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Result<Object> handleHttpMediaTypeNotSupportedException(Exception e) {
        log.error("不支持的媒体类型", e);
        return Result.error("不支持的媒体类型");
    }
}