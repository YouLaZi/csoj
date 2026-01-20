package com.oj.cs.aspect;

import java.lang.reflect.Method;
import java.util.Date;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.oj.cs.annotation.AuditLog;
import com.oj.cs.model.entity.User;
import com.oj.cs.service.AuditLogService;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/** 操作审计日志切面 */
@Aspect
@Component
@Slf4j
public class AuditLogAspect {

  @Resource private AuditLogService auditLogService;

  /** 配置切入点 */
  @Pointcut("@annotation(com.oj.cs.annotation.AuditLog)")
  public void auditLogPointcut() {}

  /** 环绕通知 */
  @Around("auditLogPointcut()")
  public Object around(ProceedingJoinPoint point) throws Throwable {
    long beginTime = System.currentTimeMillis();
    Object result = null;
    Exception exception = null;

    try {
      // 执行方法
      result = point.proceed();
      return result;
    } catch (Exception e) {
      exception = e;
      throw e;
    } finally {
      long duration = System.currentTimeMillis() - beginTime;
      recordAuditLog(point, result, exception, duration);
    }
  }

  /** 记录审计日志 */
  private void recordAuditLog(
      ProceedingJoinPoint point, Object result, Exception exception, long duration) {
    try {
      // 获取RequestAttributes
      ServletRequestAttributes attributes =
          (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (attributes == null) {
        return;
      }

      HttpServletRequest request = attributes.getRequest();

      // 获取方法签名
      MethodSignature signature = (MethodSignature) point.getSignature();
      Method method = signature.getMethod();

      // 获取AuditLog注解
      AuditLog auditLogAnnotation = method.getAnnotation(AuditLog.class);
      if (auditLogAnnotation == null) {
        return;
      }

      // 获取当前登录用户（从请求属性中获取，避免重复查询）
      User user = (User) request.getAttribute("loginUser");

      // 构建审计日志实体
      com.oj.cs.model.entity.AuditLog auditLog = new com.oj.cs.model.entity.AuditLog();
      auditLog.setCreateTime(new Date());
      auditLog.setDuration(duration);

      // 设置用户信息
      if (user != null) {
        auditLog.setUserId(user.getId());
        auditLog.setUserAccount(user.getUserAccount());
        auditLog.setUserName(user.getUserName());
      }

      // 设置操作信息
      auditLog.setModule(
          StrUtil.isNotBlank(auditLogAnnotation.module())
              ? auditLogAnnotation.module()
              : getModuleFromRequest(request));
      auditLog.setOperationType(
          StrUtil.isNotBlank(auditLogAnnotation.operationType())
              ? auditLogAnnotation.operationType()
              : inferOperationType(request));
      auditLog.setDescription(auditLogAnnotation.description());

      // 设置请求信息
      auditLog.setMethod(request.getMethod() + " " + request.getRequestURI());

      // 设置参数（限制长度）
      Object[] args = point.getArgs();
      if (args != null && args.length > 0) {
        String paramsJson = JSONUtil.toJsonStr(args);
        // 限制参数长度，避免过长
        if (paramsJson.length() > 2000) {
          paramsJson = paramsJson.substring(0, 2000) + "...";
        }
        auditLog.setParams(paramsJson);
      }

      // 设置返回结果（限制长度）
      if (result != null) {
        String resultJson = JSONUtil.toJsonStr(result);
        if (resultJson.length() > 2000) {
          resultJson = resultJson.substring(0, 2000) + "...";
        }
        auditLog.setResult(resultJson);
      }

      // 设置状态和错误信息
      if (exception != null) {
        auditLog.setStatus("FAILURE");
        String errorMsg = exception.getMessage();
        if (errorMsg != null && errorMsg.length() > 500) {
          errorMsg = errorMsg.substring(0, 500) + "...";
        }
        auditLog.setErrorMsg(errorMsg);
      } else {
        auditLog.setStatus("SUCCESS");
      }

      // 设置客户端信息
      auditLog.setClientIp(getClientIp(request));
      auditLog.setUserAgent(request.getHeader("User-Agent"));

      // 异步保存日志
      auditLogService.saveAsync(auditLog);

    } catch (Exception e) {
      log.error("审计日志记录失败", e);
    }
  }

  /** 从请求推断操作类型 */
  private String inferOperationType(HttpServletRequest request) {
    String method = request.getMethod();
    String uri = request.getRequestURI();

    if (uri.contains("/login")) {
      return "LOGIN";
    } else if (uri.contains("/logout")) {
      return "LOGOUT";
    } else if (uri.contains("/import")) {
      return "IMPORT";
    } else if (uri.contains("/export")) {
      return "EXPORT";
    }

    switch (method) {
      case "POST":
        return uri.contains("/update") ? "UPDATE" : "CREATE";
      case "PUT":
        return "UPDATE";
      case "DELETE":
        return "DELETE";
      case "GET":
        return "QUERY";
      default:
        return "OTHER";
    }
  }

  /** 从请求获取模块名称 */
  private String getModuleFromRequest(HttpServletRequest request) {
    String uri = request.getRequestURI();
    if (uri.contains("/user")) {
      return "用户管理";
    } else if (uri.contains("/question")) {
      return "题目管理";
    } else if (uri.contains("/questionSubmit")) {
      return "判题管理";
    } else if (uri.contains("/announcement")) {
      return "公告管理";
    } else if (uri.contains("/assignment")) {
      return "作业管理";
    } else if (uri.contains("/contest")) {
      return "比赛管理";
    } else if (uri.contains("/file")) {
      return "文件管理";
    } else if (uri.contains("/system")) {
      return "系统管理";
    }
    return "其他";
  }

  /** 获取客户端IP */
  private String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("HTTP_CLIENT_IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("HTTP_X_FORWARDED_FOR");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    // 处理多个IP的情况
    if (ip != null && ip.contains(",")) {
      ip = ip.split(",")[0].trim();
    }
    return ip;
  }
}
