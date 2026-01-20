package com.oj.cs.aop;

import java.util.Arrays;
import java.util.List;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.oj.cs.annotation.AuthCheck2;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.enums.UserRoleEnum;
import com.oj.cs.model.vo.UserPermissionVO;
import com.oj.cs.service.UserService;

/** 增强版权限校验 AOP */
@Aspect
@Component
public class AuthInterceptor2 {

  @Resource private UserService userService;

  /**
   * 执行拦截
   *
   * @param joinPoint
   * @param authCheck
   * @return
   */
  @Around("@annotation(authCheck)")
  public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck2 authCheck)
      throws Throwable {
    String mustRole = authCheck.mustRole();
    String[] mustPermissions = authCheck.mustPermissions();
    String logic = authCheck.logic();

    RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
    HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

    // 当前登录用户
    User loginUser = userService.getLoginUser(request);

    // 校验角色
    if (StringUtils.isNotBlank(mustRole)) {
      UserRoleEnum mustUserRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
      if (mustUserRoleEnum == null) {
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "角色不存在");
      }
      String userRole = loginUser.getUserRole();

      // 如果被封号，直接拒绝
      if (UserRoleEnum.BAN.getValue().equals(userRole)) {
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "账号已被封禁");
      }

      // 必须有管理员权限
      if (UserRoleEnum.ADMIN.getValue().equals(mustRole)) {
        if (!mustRole.equals(userRole)) {
          throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "需要管理员权限");
        }
      }
    }

    // 校验权限
    if (mustPermissions.length > 0) {
      // 获取用户权限列表
      UserPermissionVO userPermissionVO = userService.getUserPermission(loginUser);
      List<String> permissions = userPermissionVO.getPermissions();

      // 权限校验逻辑
      if ("ANY".equals(logic)) {
        // 任一权限满足即可
        boolean hasAnyPermission = Arrays.stream(mustPermissions).anyMatch(permissions::contains);
        if (!hasAnyPermission) {
          throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "权限不足");
        }
      } else {
        // 默认必须满足所有权限
        boolean hasAllPermissions = Arrays.stream(mustPermissions).allMatch(permissions::contains);
        if (!hasAllPermissions) {
          throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "权限不足");
        }
      }
    }

    // 通过权限校验，放行
    return joinPoint.proceed();
  }
}
