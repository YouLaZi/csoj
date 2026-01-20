package com.oj.cs.aop;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.extern.slf4j.Slf4j;

/** 性能监控拦截器 用于监控接口响应时间和系统性能 */
@Aspect
@Component
@Slf4j
public class PerformanceMonitorInterceptor {

  /** 接口调用次数统计 */
  private static final ConcurrentHashMap<String, AtomicLong> API_CALL_COUNT =
      new ConcurrentHashMap<>();

  /** 接口总响应时间统计 */
  private static final ConcurrentHashMap<String, AtomicLong> API_TOTAL_TIME =
      new ConcurrentHashMap<>();

  /** 慢接口阈值（毫秒） */
  private static final long SLOW_API_THRESHOLD = 3000;

  /** 判题接口阈值（毫秒） */
  private static final long JUDGE_API_THRESHOLD = 10000;

  /** 监控所有Controller方法 */
  @Around("execution(* com.oj.cs.controller.*.*(..))")
  public Object monitorController(ProceedingJoinPoint joinPoint) throws Throwable {
    return monitorMethod(joinPoint, "Controller");
  }

  /** 监控判题服务方法 */
  @Around("execution(* com.oj.cs.judge.*.*(..))")
  public Object monitorJudgeService(ProceedingJoinPoint joinPoint) throws Throwable {
    return monitorMethod(joinPoint, "Judge");
  }

  /** 监控代码沙箱方法 */
  @Around("execution(* com.oj.cs.judge.codesandbox.*.*(..))")
  public Object monitorCodeSandbox(ProceedingJoinPoint joinPoint) throws Throwable {
    return monitorMethod(joinPoint, "CodeSandbox");
  }

  /** 通用方法监控 */
  private Object monitorMethod(ProceedingJoinPoint joinPoint, String type) throws Throwable {
    long startTime = System.currentTimeMillis();
    String methodName =
        joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
    String requestInfo = getRequestInfo();

    try {
      Object result = joinPoint.proceed();
      long endTime = System.currentTimeMillis();
      long executionTime = endTime - startTime;

      // 更新统计信息
      updateStatistics(methodName, executionTime);

      // 记录性能日志
      logPerformance(type, methodName, executionTime, requestInfo, true);

      return result;
    } catch (Throwable throwable) {
      long endTime = System.currentTimeMillis();
      long executionTime = endTime - startTime;

      // 记录异常性能日志
      logPerformance(type, methodName, executionTime, requestInfo, false);

      throw throwable;
    }
  }

  /** 更新统计信息 */
  private void updateStatistics(String methodName, long executionTime) {
    API_CALL_COUNT.computeIfAbsent(methodName, k -> new AtomicLong(0)).incrementAndGet();
    API_TOTAL_TIME.computeIfAbsent(methodName, k -> new AtomicLong(0)).addAndGet(executionTime);
  }

  /** 记录性能日志 */
  private void logPerformance(
      String type, String methodName, long executionTime, String requestInfo, boolean success) {
    // 判断是否为慢接口
    boolean isSlow = false;
    if ("Judge".equals(type) || "CodeSandbox".equals(type)) {
      isSlow = executionTime > JUDGE_API_THRESHOLD;
    } else {
      isSlow = executionTime > SLOW_API_THRESHOLD;
    }

    String status = success ? "SUCCESS" : "ERROR";
    String logMessage =
        String.format(
            "[%s] %s - %s - %dms%s%s",
            type,
            methodName,
            status,
            executionTime,
            isSlow ? " [SLOW]" : "",
            requestInfo != null ? " - " + requestInfo : "");

    if (isSlow || !success) {
      log.warn(logMessage);
    } else {
      log.info(logMessage);
    }
  }

  /** 获取请求信息 */
  private String getRequestInfo() {
    try {
      ServletRequestAttributes attributes =
          (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (attributes != null) {
        HttpServletRequest request = attributes.getRequest();
        return String.format("%s %s", request.getMethod(), request.getRequestURI());
      }
    } catch (Exception e) {
      // 忽略异常，可能在非Web环境中调用
    }
    return null;
  }

  /** 获取接口统计信息 */
  public static String getStatistics() {
    StringBuilder sb = new StringBuilder();
    sb.append("\n=== API Performance Statistics ===\n");

    API_CALL_COUNT.forEach(
        (method, count) -> {
          AtomicLong totalTime = API_TOTAL_TIME.get(method);
          if (totalTime != null) {
            long avgTime = totalTime.get() / count.get();
            sb.append(String.format("%s: calls=%d, avgTime=%dms\n", method, count.get(), avgTime));
          }
        });

    return sb.toString();
  }

  /** 清空统计信息 */
  public static void clearStatistics() {
    API_CALL_COUNT.clear();
    API_TOTAL_TIME.clear();
  }
}
