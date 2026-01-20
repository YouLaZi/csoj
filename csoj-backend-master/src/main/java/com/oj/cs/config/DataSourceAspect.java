package com.oj.cs.config;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.oj.cs.config.DataSourceContextHolder.DataSourceType;

/**
 * 数据源切换切面 根据方法名或注解自动选择主库或从库
 *
 * <p>规则： - 以 insert、update、delete、create 开头的方法 → 主库 - 以 select、query、get、find、list 开头的方法 → 从库 -
 * 使用 @ReadOnly 注解的方法 → 从库 - 使用 @ReadWrite 注解的方法 → 主库
 */
@Aspect
@Component
@Order(1) // 确保在事务切面之前执行
public class DataSourceAspect {

  @Around("execution(* com.oj.cs.service..*.*(..)) || execution(* com.oj.cs.mapper..*.*(..))")
  public Object around(ProceedingJoinPoint point) throws Throwable {
    MethodSignature signature = (MethodSignature) point.getSignature();
    Method method = signature.getMethod();

    // 检查方法上的注解
    if (method.isAnnotationPresent(ReadOnly.class)) {
      DataSourceContextHolder.setDataSourceType(DataSourceType.SLAVE);
    } else if (method.isAnnotationPresent(ReadWrite.class)) {
      DataSourceContextHolder.setDataSourceType(DataSourceType.MASTER);
    } else {
      // 根据方法名判断
      String methodName = method.getName();
      if (isReadMethod(methodName)) {
        DataSourceContextHolder.setDataSourceType(DataSourceType.SLAVE);
      } else {
        DataSourceContextHolder.setDataSourceType(DataSourceType.MASTER);
      }
    }

    try {
      return point.proceed();
    } finally {
      // 清除数据源类型，避免影响后续操作
      DataSourceContextHolder.clearDataSourceType();
    }
  }

  /** 判断是否为读方法 */
  private boolean isReadMethod(String methodName) {
    return methodName.startsWith("select")
        || methodName.startsWith("query")
        || methodName.startsWith("get")
        || methodName.startsWith("find")
        || methodName.startsWith("list")
        || methodName.startsWith("count")
        || methodName.startsWith("check")
        || methodName.startsWith("exist")
        || methodName.startsWith("has");
  }
}
