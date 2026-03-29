package com.oj.cs.oauth.provider;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import com.oj.cs.config.OAuthProperties;
import com.oj.cs.config.WxOpenConfig;
import com.oj.cs.constant.OAuthPlatformEnum;
import com.oj.cs.oauth.OAuthProvider;
import com.oj.cs.oauth.OAuthUserInfo;

import lombok.extern.slf4j.Slf4j;

/** 微信开放平台 OAuth 提供者 用于网站应用扫码登录 */
@Slf4j
@Component
public class WeChatOAuthProvider implements OAuthProvider {

  private static final String AUTHORIZE_URL = "https://open.weixin.qq.com/connect/qrconnect";
  private static final String TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token";
  private static final String USER_INFO_URL = "https://api.weixin.qq.com/sns/userinfo";

  @Resource private OAuthProperties oauthProperties;

  @Resource private WxOpenConfig wxOpenConfig;

  @Override
  public String getPlatform() {
    return OAuthPlatformEnum.WECHAT.getCode();
  }

  @Override
  public String buildAuthorizationUrl(String state, String redirectUri) {
    String appId = getEffectiveAppId();
    try {
      return String.format(
          "%s?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_login&state=%s#wechat_redirect",
          AUTHORIZE_URL, appId, URLEncoder.encode(redirectUri, "UTF-8"), state);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("UTF-8 encoding not supported", e);
    }
  }

  @Override
  public String getAccessToken(String code, String redirectUri) {
    Map<String, Object> response = getAccessTokenResponse(code);
    return response != null ? (String) response.get("access_token") : null;
  }

  @Override
  public OAuthUserInfo getUserInfo(String accessToken) {
    log.warn("微信登录请使用getUserInfoWithTokenResponse方法");
    return null;
  }

  /** 获取access_token响应（包含openId和unionId） */
  public Map<String, Object> getAccessTokenResponse(String code) {
    String appId = getEffectiveAppId();
    String appSecret = getEffectiveAppSecret();

    String url =
        String.format(
            "%s?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
            TOKEN_URL, appId, appSecret, code);

    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> response =
          new org.springframework.web.client.RestTemplate().getForObject(url, Map.class);

      if (response != null && response.containsKey("access_token")) {
        return response;
      }
      log.error("微信获取access_token失败: {}", response);
      return null;
    } catch (Exception e) {
      log.error("微信获取access_token异常", e);
      return null;
    }
  }

  /** 获取微信用户信息（包含unionId） */
  public OAuthUserInfo getUserInfoWithTokenResponse(Map<String, Object> tokenResponse) {
    if (tokenResponse == null) {
      return null;
    }

    String accessToken = (String) tokenResponse.get("access_token");
    String openId = (String) tokenResponse.get("openid");

    String url = String.format("%s?access_token=%s&openid=%s", USER_INFO_URL, accessToken, openId);

    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> response =
          new org.springframework.web.client.RestTemplate().getForObject(url, Map.class);

      if (response == null || response.containsKey("errcode")) {
        log.error("微信获取用户信息失败: {}", response);
        return null;
      }

      OAuthUserInfo userInfo = new OAuthUserInfo();
      userInfo.setPlatform(getPlatform());
      userInfo.setPlatformUserId((String) response.get("openid"));
      userInfo.setOpenId((String) response.get("openid"));
      userInfo.setUnionId((String) response.get("unionid"));
      userInfo.setNickname((String) response.get("nickname"));
      userInfo.setAvatar((String) response.get("headimgurl"));
      userInfo.setRawJson(response.toString());

      return userInfo;
    } catch (Exception e) {
      log.error("微信获取用户信息异常", e);
      return null;
    }
  }

  private String getEffectiveAppId() {
    OAuthProperties.WeChatConfig wechatConfig = oauthProperties.getWechat();
    if (wechatConfig.getClientId() != null && !wechatConfig.getClientId().isEmpty()) {
      return wechatConfig.getClientId();
    }
    return wxOpenConfig.getAppId();
  }

  private String getEffectiveAppSecret() {
    OAuthProperties.WeChatConfig wechatConfig = oauthProperties.getWechat();
    if (wechatConfig.getClientSecret() != null && !wechatConfig.getClientSecret().isEmpty()) {
      return wechatConfig.getClientSecret();
    }
    return wxOpenConfig.getAppSecret();
  }

  @Override
  public boolean isEnabled() {
    OAuthProperties.WeChatConfig config = oauthProperties.getWechat();
    boolean oauthEnabled =
        config.isEnabled() && config.getClientId() != null && !config.getClientId().isEmpty();
    boolean wxEnabled =
        wxOpenConfig.getAppId() != null
            && !wxOpenConfig.getAppId().isEmpty()
            && !"xxx".equals(wxOpenConfig.getAppId());
    return oauthEnabled || wxEnabled;
  }
}
