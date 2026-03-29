package com.oj.cs.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.constant.OAuthPlatformEnum;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.model.vo.OAuthBindingVO;
import com.oj.cs.model.vo.OAuthLoginVO;
import com.oj.cs.oauth.OAuthProvider;
import com.oj.cs.oauth.OAuthProviderFactory;
import com.oj.cs.service.OAuthService;

import lombok.extern.slf4j.Slf4j;

/** OAuth 第三方登录控制器 */
@Slf4j
@RestController
@RequestMapping("/oauth")
public class OAuthController {

  @Resource private OAuthService oAuthService;

  @Resource private OAuthProviderFactory providerFactory;

  /** 获取授权URL */
  @GetMapping("/authorize")
  public BaseResponse<Map<String, Object>> getAuthorizeUrl(
      @RequestParam String platform, HttpServletRequest request) {

    OAuthPlatformEnum platformEnum = OAuthPlatformEnum.getByCode(platform);
    if (platformEnum == null) {
      platformEnum = OAuthPlatformEnum.getByPathName(platform);
    }
    if (platformEnum == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的平台: " + platform);
    }

    OAuthProvider provider = providerFactory.getProvider(platformEnum.getCode());
    if (!provider.isEnabled()) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "该平台登录未启用");
    }

    // 生成 state 参数
    String state = oAuthService.generateState(platformEnum.getCode(), request);
    String redirectUri = oAuthService.getRedirectUri(platformEnum.getCode());
    String authorizeUrl = provider.buildAuthorizationUrl(state, redirectUri);

    Map<String, Object> result = new HashMap<>();
    result.put("authorizeUrl", authorizeUrl);
    result.put("state", state);
    result.put("platform", platformEnum.getCode());

    return ResultUtils.success(result);
  }

  /** OAuth 回调处理 */
  @GetMapping("/callback/{platform}")
  public BaseResponse<OAuthLoginVO> oauthCallback(
      @PathVariable String platform,
      @RequestParam(required = false) String code,
      @RequestParam(required = false) String state,
      HttpServletRequest request) {

    if (StringUtils.isBlank(code)) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "授权码不能为空");
    }

    OAuthPlatformEnum platformEnum = OAuthPlatformEnum.getByPathName(platform);
    if (platformEnum == null) {
      platformEnum = OAuthPlatformEnum.getByCode(platform);
    }
    if (platformEnum == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的平台: " + platform);
    }

    OAuthLoginVO result =
        oAuthService.processOAuthLogin(platformEnum.getCode(), code, state, request);

    return ResultUtils.success(result);
  }

  /** 获取已绑定的第三方账号 */
  @GetMapping("/bindings")
  public BaseResponse<List<OAuthBindingVO>> getOAuthBindings(@RequestParam Long userId) {
    List<OAuthBindingVO> bindings = oAuthService.getOAuthBindings(userId);
    return ResultUtils.success(bindings);
  }

  /** 解绑第三方账号 */
  @DeleteMapping("/unbind")
  public BaseResponse<Boolean> unbindOAuth(
      @RequestParam String platform, @RequestParam Long userId) {
    boolean result = oAuthService.unbindOAuthAccount(platform, userId);
    return ResultUtils.success(result);
  }

  /** 获取支持的OAuth平台列表 */
  @GetMapping("/platforms")
  public BaseResponse<List<Map<String, Object>>> getSupportedPlatforms() {
    List<Map<String, Object>> platforms = new ArrayList<>();

    for (OAuthPlatformEnum platform : OAuthPlatformEnum.values()) {
      try {
        OAuthProvider provider = providerFactory.getProvider(platform.getCode());
        Map<String, Object> platformInfo = new HashMap<>();
        platformInfo.put("code", platform.getCode());
        platformInfo.put("name", platform.getName());
        platformInfo.put("pathName", platform.getPathName());
        platformInfo.put("enabled", provider.isEnabled());
        platforms.add(platformInfo);
      } catch (Exception e) {
        log.warn("获取平台信息失败: {}", platform.getCode(), e);
      }
    }

    return ResultUtils.success(platforms);
  }
}
