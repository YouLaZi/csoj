package com.oj.cs.service;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import com.oj.cs.model.vo.LoginUserVO;
import com.oj.cs.model.vo.OAuthBindingVO;
import com.oj.cs.model.vo.OAuthLoginVO;
import com.oj.cs.oauth.OAuthUserInfo;

/** OAuth 服务接口 */
public interface OAuthService {

  /**
   * 生成 State 参数（CSRF防护）
   *
   * @param platform 平台类型
   * @param request HTTP请求
   * @return state参数
   */
  String generateState(String platform, HttpServletRequest request);

  /**
   * 验证 State 参数
   *
   * @param state state参数
   * @param platform 平台类型
   * @param request HTTP请求
   * @return 是否有效
   */
  boolean verifyState(String state, String platform, HttpServletRequest request);

  /**
   * 获取平台的回调URL
   *
   * @param platform 平台类型
   * @return 回调URL
   */
  String getRedirectUri(String platform);

  /**
   * 处理 OAuth 登录
   *
   * @param platform 平台类型
   * @param code 授权码
   * @param state state参数（可选）
   * @param request HTTP请求
   * @return 登录结果
   */
  OAuthLoginVO processOAuthLogin(
      String platform, String code, String state, HttpServletRequest request);

  /**
   * 绑定第三方账号到当前用户
   *
   * @param platform 平台类型
   * @param code 授权码
   * @param request HTTP请求
   * @return 是否成功
   */
  boolean bindOAuthAccount(String platform, String code, HttpServletRequest request);

  /**
   * 解绑第三方账号
   *
   * @param platform 平台类型
   * @param userId 用户ID
   * @return 是否成功
   */
  boolean unbindOAuthAccount(String platform, Long userId);

  /**
   * 获取用户已绑定的第三方账号
   *
   * @param userId 用户ID
   * @return 绑定列表
   */
  List<OAuthBindingVO> getOAuthBindings(Long userId);

  /**
   * 根据第三方信息查找或创建用户
   *
   * @param userInfo 第三方用户信息
   * @param platform 平台类型
   * @return 登录用户VO
   */
  LoginUserVO findOrCreateUser(OAuthUserInfo userInfo, String platform);
}
