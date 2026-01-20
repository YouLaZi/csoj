package com.oj.cs.exception;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.common.ResultUtils;

import lombok.extern.slf4j.Slf4j;

/** 全局异常处理器（增强版） 处理所有Controller抛出的异常，统一返回格式 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /** 业务异常（最常见） */
  @ExceptionHandler(BusinessException.class)
  public BaseResponse<?> businessExceptionHandler(BusinessException e, HttpServletRequest request) {
    log.warn("业务异常 [{}] {} - {}", request.getRequestURI(), e.getCode(), e.getMessage());
    return ResultUtils.error(e.getCode(), e.getMessage());
  }

  /** 参数验证失败（@Valid注解触发） */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public BaseResponse<?> methodArgumentNotValidExceptionHandler(
      MethodArgumentNotValidException e, HttpServletRequest request) {
    log.warn("参数验证失败 [{}]", request.getRequestURI());
    String message =
        e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .reduce((msg1, msg2) -> msg1 + "; " + msg2)
            .orElse("参数验证失败");
    return ResultUtils.error(ErrorCode.PARAMS_ERROR, message);
  }

  /** 参数绑定异常 */
  @ExceptionHandler(BindException.class)
  public BaseResponse<?> bindExceptionHandler(BindException e, HttpServletRequest request) {
    log.warn("参数绑定失败 [{}]", request.getRequestURI());
    String message =
        e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .reduce((msg1, msg2) -> msg1 + "; " + msg2)
            .orElse("参数绑定失败");
    return ResultUtils.error(ErrorCode.PARAMS_ERROR, message);
  }

  /** 约束验证异常（@Validated注解触发） */
  @ExceptionHandler(ConstraintViolationException.class)
  public BaseResponse<?> constraintViolationExceptionHandler(
      ConstraintViolationException e, HttpServletRequest request) {
    log.warn("约束验证失败 [{}]", request.getRequestURI());
    String message =
        e.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .reduce((msg1, msg2) -> msg1 + "; " + msg2)
            .orElse("约束验证失败");
    return ResultUtils.error(ErrorCode.PARAMS_ERROR, message);
  }

  /** 缺少请求参数异常 */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public BaseResponse<?> missingServletRequestParameterExceptionHandler(
      MissingServletRequestParameterException e, HttpServletRequest request) {
    log.warn("缺少请求参数 [{}] {}", request.getRequestURI(), e.getParameterName());
    return ResultUtils.error(ErrorCode.PARAMS_ERROR, "缺少必需参数: " + e.getParameterName());
  }

  /** 缺少路径变量异常 */
  @ExceptionHandler(MissingPathVariableException.class)
  public BaseResponse<?> missingPathVariableExceptionHandler(
      MissingPathVariableException e, HttpServletRequest request) {
    log.warn("缺少路径变量 [{}] {}", request.getRequestURI(), e.getVariableName());
    return ResultUtils.error(ErrorCode.PARAMS_ERROR, "缺少路径变量: " + e.getVariableName());
  }

  /** 请求方法不支持异常 */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public BaseResponse<?> httpRequestMethodNotSupportedExceptionHandler(
      HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
    log.warn("请求方法不支持 [{}] {}", request.getRequestURI(), e.getMethod());
    return ResultUtils.error(ErrorCode.PARAMS_ERROR, "不支持的请求方法: " + e.getMethod());
  }

  /** 媒体类型不支持异常 */
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public BaseResponse<?> httpMediaTypeNotSupportedExceptionHandler(
      HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
    log.warn("媒体类型不支持 [{}] {}", request.getRequestURI(), e.getContentType());
    return ResultUtils.error(ErrorCode.PARAMS_ERROR, "不支持的媒体类型: " + e.getContentType());
  }

  /** 消息不可读异常（JSON解析错误） */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public BaseResponse<?> httpMessageNotReadableExceptionHandler(
      HttpMessageNotReadableException e, HttpServletRequest request) {
    log.warn("消息解析失败 [{}]", request.getRequestURI());
    return ResultUtils.error(ErrorCode.PARAMS_ERROR, "请求体格式错误，请检查JSON格式");
  }

  /** SQL异常（数据库错误） */
  @ExceptionHandler(SQLException.class)
  public BaseResponse<?> sqlExceptionHandler(SQLException e, HttpServletRequest request) {
    log.error(
        "数据库异常 [{}] SQLState: {}, ErrorCode: {}",
        request.getRequestURI(),
        e.getSQLState(),
        e.getErrorCode(),
        e);
    // 生产环境不暴露具体错误信息
    return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "数据库操作失败，请稍后重试");
  }

  /** 超时异常 */
  @ExceptionHandler(TimeoutException.class)
  public BaseResponse<?> timeoutExceptionHandler(TimeoutException e, HttpServletRequest request) {
    log.error("请求超时 [{}]", request.getRequestURI());
    return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "请求超时，请稍后重试");
  }

  /** IO异常 */
  @ExceptionHandler(IOException.class)
  public BaseResponse<?> ioExceptionHandler(IOException e, HttpServletRequest request) {
    log.error("IO异常 [{}]", request.getRequestURI(), e);
    return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "文件操作失败");
  }

  /** 空指针异常（不应该发生，表示代码有bug） */
  @ExceptionHandler(NullPointerException.class)
  public BaseResponse<?> nullPointerExceptionHandler(
      NullPointerException e, HttpServletRequest request) {
    log.error("空指针异常 [{}]", request.getRequestURI(), e);
    return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统内部错误");
  }

  /** 非法参数异常 */
  @ExceptionHandler(IllegalArgumentException.class)
  public BaseResponse<?> illegalArgumentExceptionHandler(
      IllegalArgumentException e, HttpServletRequest request) {
    log.warn("非法参数 [{}] {}", request.getRequestURI(), e.getMessage());
    return ResultUtils.error(ErrorCode.PARAMS_ERROR, e.getMessage());
  }

  /** 非法状态异常 */
  @ExceptionHandler(IllegalStateException.class)
  public BaseResponse<?> illegalStateExceptionHandler(
      IllegalStateException e, HttpServletRequest request) {
    log.error("非法状态 [{}] {}", request.getRequestURI(), e.getMessage());
    return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统状态异常");
  }

  /** 运行时异常（兜底处理） */
  @ExceptionHandler(RuntimeException.class)
  public BaseResponse<?> runtimeExceptionHandler(RuntimeException e, HttpServletRequest request) {
    log.error("运行时异常 [{}]", request.getRequestURI(), e);
    return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误: " + e.getMessage());
  }

  /** 所有异常的最终兜底处理 */
  @ExceptionHandler(Exception.class)
  public BaseResponse<?> exceptionHandler(Exception e, HttpServletRequest request) {
    log.error("未知异常 [{}]", request.getRequestURI(), e);
    return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误，请联系管理员");
  }
}
