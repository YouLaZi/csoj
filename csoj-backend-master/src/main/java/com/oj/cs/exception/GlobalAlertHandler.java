package com.oj.cs.exception;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.oj.cs.service.AlertService;

import lombok.extern.slf4j.Slf4j;

/** 全局异常告警处理器 */
@Component
@Slf4j
public class GlobalAlertHandler {

  @Resource private AlertService alertService;

  /**
   * 处理异常并发送告警
   *
   * @param e 异常对象
   * @param request HTTP请求
   * @param userId 用户ID
   */
  public void handleException(Throwable e, HttpServletRequest request, Long userId) {
    try {
      // 判断告警级别
      String severity = determineSeverity(e);

      // 只对高级别异常发送告警
      if ("critical".equals(severity) || "high".equals(severity)) {
        String title = buildAlertTitle(e);
        String alertType = determineAlertType(e);
        String content = buildAlertContent(e, request);
        String stackTrace = getStackTrace(e);
        String source = determineSource(e);
        String requestPath = request != null ? request.getRequestURI() : "unknown";

        alertService.createExceptionAlert(
            title, alertType, severity, content, stackTrace, source, requestPath, userId);
      }
    } catch (Exception ex) {
      // 防止告警处理本身引发异常
      log.error("创建异常告警失败", ex);
    }
  }

  /** 处理异常并发送告警（无用户ID） */
  public void handleException(Throwable e, HttpServletRequest request) {
    handleException(e, request, null);
  }

  /** 判断告警级别 */
  private String determineSeverity(Throwable e) {
    if (e instanceof OutOfMemoryError || e instanceof StackOverflowError) {
      return "critical";
    } else if (e instanceof BusinessException) {
      BusinessException be = (BusinessException) e;
      // 根据错误码判断严重性
      int code = be.getCode();
      if (code >= 50000) {
        return "high";
      } else if (code >= 40000) {
        return "medium";
      }
      return "low";
    } else if (e instanceof RuntimeException) {
      return "high";
    }
    return "medium";
  }

  /** 确定告警类型 */
  private String determineAlertType(Throwable e) {
    if (e instanceof OutOfMemoryError) {
      return "system";
    } else if (e instanceof BusinessException) {
      return "business";
    } else if (e instanceof NullPointerException) {
      return "bug";
    } else if (e instanceof IllegalArgumentException) {
      return "parameter";
    }
    return "error";
  }

  /** 构建告警标题 */
  private String buildAlertTitle(Throwable e) {
    String simpleName = e.getClass().getSimpleName();
    if (e instanceof BusinessException) {
      return ((BusinessException) e).getMessage();
    }
    return simpleName + ": " + e.getMessage();
  }

  /** 构建告警内容 */
  private String buildAlertContent(Throwable e, HttpServletRequest request) {
    StringBuilder sb = new StringBuilder();
    sb.append("异常类型: ").append(e.getClass().getName()).append("\n");
    sb.append("异常消息: ").append(e.getMessage()).append("\n");

    if (request != null) {
      sb.append("请求方法: ").append(request.getMethod()).append("\n");
      sb.append("请求路径: ").append(request.getRequestURI()).append("\n");
      String queryString = request.getQueryString();
      if (queryString != null) {
        sb.append("查询参数: ").append(queryString).append("\n");
      }
      String remoteAddr = request.getRemoteAddr();
      if (remoteAddr != null) {
        sb.append("客户端IP: ").append(remoteAddr).append("\n");
      }
    }

    return sb.toString();
  }

  /** 获取异常堆栈 */
  private String getStackTrace(Throwable e) {
    java.io.StringWriter sw = new java.io.StringWriter();
    java.io.PrintWriter pw = new java.io.PrintWriter(sw);
    e.printStackTrace(pw);
    return sw.toString();
  }

  /** 确定告警来源 */
  private String determineSource(Throwable e) {
    StackTraceElement[] stackTrace = e.getStackTrace();
    if (stackTrace != null && stackTrace.length > 0) {
      StackTraceElement element = stackTrace[0];
      return element.getClassName();
    }
    return "unknown";
  }
}
