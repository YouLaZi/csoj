package com.oj.cs.service;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.cs.model.dto.user.UserQueryRequest;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.vo.LoginUserVO;
import com.oj.cs.model.vo.UserPermissionVO;
import com.oj.cs.model.vo.UserVO;

import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;

/** 用户服务 */
public interface UserService extends IService<User> {

  /**
   * 用户注册
   *
   * @param userAccount 用户账户
   * @param userPassword 用户密码
   * @param checkPassword 校验密码
   * @return 新用户 id
   */
  long userRegister(String userAccount, String userName, String userPassword, String checkPassword);

  /**
   * 用户登录
   *
   * @param userAccount 用户账户
   * @param userPassword 用户密码
   * @param request
   * @return 脱敏后的用户信息
   */
  LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

  /**
   * 用户登录（微信开放平台）
   *
   * @param wxOAuth2UserInfo 从微信获取的用户信息
   * @param request
   * @return 脱敏后的用户信息
   */
  LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request);

  /**
   * 获取当前登录用户
   *
   * @param request
   * @return
   */
  User getLoginUser(HttpServletRequest request);

  /**
   * 获取当前登录用户（允许未登录）
   *
   * @param request
   * @return
   */
  User getLoginUserPermitNull(HttpServletRequest request);

  /**
   * 是否为管理员
   *
   * @param request
   * @return
   */
  boolean isAdmin(HttpServletRequest request);

  /**
   * 是否为管理员
   *
   * @param user
   * @return
   */
  boolean isAdmin(User user);

  /**
   * 用户注销
   *
   * @param request
   * @return
   */
  boolean userLogout(HttpServletRequest request);

  /**
   * 获取脱敏的已登录用户信息
   *
   * @return
   */
  LoginUserVO getLoginUserVO(User user);

  /**
   * 获取脱敏的用户信息
   *
   * @param user
   * @return
   */
  UserVO getUserVO(User user);

  /**
   * 获取脱敏的用户信息
   *
   * @param userList
   * @return
   */
  List<UserVO> getUserVO(List<User> userList);

  /**
   * 获取查询条件
   *
   * @param userQueryRequest
   * @return
   */
  QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

  /**
   * 获取用户权限信息
   *
   * @param user 用户
   * @return 用户权限信息
   */
  UserPermissionVO getUserPermission(User user);

  /**
   * 处理忘记密码请求，生成token并发送邮件
   *
   * @param email 用户邮箱
   */
  void processForgotPassword(String email);

  /**
   * 根据token重置密码
   *
   * @param token 密码重置token
   * @param newPassword 新密码
   */
  void resetPassword(String token, String newPassword);
}
