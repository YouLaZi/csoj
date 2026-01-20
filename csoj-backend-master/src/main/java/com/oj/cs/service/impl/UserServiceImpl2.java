package com.oj.cs.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.oj.cs.constant.PermissionConstant;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.enums.UserRoleEnum;
import com.oj.cs.model.vo.UserPermissionVO;

/** 用户服务实现类扩展 提供权限相关功能 */
@Service
public class UserServiceImpl2 {

  /**
   * 获取用户权限
   *
   * @param user 用户
   * @return 用户权限信息
   */
  public UserPermissionVO getUserPermission(User user) {
    if (user == null) {
      return null;
    }

    UserPermissionVO userPermissionVO = new UserPermissionVO();
    userPermissionVO.setUserId(user.getId());
    userPermissionVO.setUserRole(user.getUserRole());

    // 根据角色分配权限
    List<String> permissions = new ArrayList<>();
    String userRole = user.getUserRole();

    // 所有用户都有的基础权限
    permissions.add(PermissionConstant.USER_READ);
    permissions.add(PermissionConstant.QUESTION_READ);
    permissions.add(PermissionConstant.POST_READ);
    permissions.add(PermissionConstant.POINTS_READ);

    // 普通用户权限
    if (UserRoleEnum.USER.getValue().equals(userRole)) {
      permissions.add(PermissionConstant.USER_EDIT); // 编辑自己的信息
      permissions.add(PermissionConstant.SUBMIT_CREATE); // 提交题目
      permissions.add(PermissionConstant.POST_EDIT); // 编辑自己的帖子
    }

    // 管理员权限
    if (UserRoleEnum.ADMIN.getValue().equals(userRole)) {
      // 管理员拥有所有权限
      permissions.add(PermissionConstant.USER_EDIT);
      permissions.add(PermissionConstant.USER_DELETE);
      permissions.add(PermissionConstant.QUESTION_EDIT);
      permissions.add(PermissionConstant.QUESTION_DELETE);
      permissions.add(PermissionConstant.SUBMIT_READ);
      permissions.add(PermissionConstant.SUBMIT_CREATE);
      permissions.add(PermissionConstant.POST_EDIT);
      permissions.add(PermissionConstant.POST_DELETE);
      permissions.add(PermissionConstant.ADMIN_ACCESS);
      permissions.add(PermissionConstant.POINTS_EDIT);
    }

    userPermissionVO.setPermissions(permissions);
    return userPermissionVO;
  }
}
