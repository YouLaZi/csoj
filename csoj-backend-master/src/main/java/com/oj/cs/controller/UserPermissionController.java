package com.oj.cs.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oj.cs.annotation.AuthCheck2;
import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.constant.PermissionConstant;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.vo.UserPermissionVO;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 用户权限控制器 */
@RestController
@RequestMapping("/user/permission")
@Slf4j
public class UserPermissionController {

  @Resource private UserService userService;

  /**
   * 获取当前登录用户的权限信息
   *
   * @param request 请求
   * @return 用户权限信息
   */
  @GetMapping("/current")
  public BaseResponse<UserPermissionVO> getCurrentUserPermission(HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    UserPermissionVO userPermissionVO = userService.getUserPermission(loginUser);
    return ResultUtils.success(userPermissionVO);
  }

  /**
   * 检查当前用户是否拥有指定权限
   *
   * @param permission 权限标识
   * @param request 请求
   * @return 是否拥有权限
   */
  @GetMapping("/check")
  public BaseResponse<Boolean> checkPermission(String permission, HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    UserPermissionVO userPermissionVO = userService.getUserPermission(loginUser);
    boolean hasPermission = userPermissionVO.getPermissions().contains(permission);
    return ResultUtils.success(hasPermission);
  }

  /**
   * 获取系统所有权限列表（仅管理员可访问）
   *
   * @param request 请求
   * @return 权限列表
   */
  @GetMapping("/list")
  @AuthCheck2(mustRole = "admin")
  public BaseResponse<String[]> getAllPermissions(HttpServletRequest request) {
    // 反射获取所有权限常量
    java.lang.reflect.Field[] fields = PermissionConstant.class.getDeclaredFields();
    String[] permissions = new String[fields.length];
    try {
      for (int i = 0; i < fields.length; i++) {
        fields[i].setAccessible(true);
        permissions[i] = (String) fields[i].get(null);
      }
    } catch (IllegalAccessException e) {
      log.error("获取权限列表失败", e);
    }
    return ResultUtils.success(permissions);
  }
}
