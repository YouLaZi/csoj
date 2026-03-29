package com.oj.cs.oauth.provider;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.oj.cs.config.OAuthProperties;
import com.oj.cs.constant.OAuthPlatformEnum;
import com.oj.cs.oauth.OAuthProvider;
import com.oj.cs.oauth.OAuthUserInfo;

import lombok.extern.slf4j.Slf4j;

/** QQ OAuth 提供者 注意：QQ登录需要先获取access_token，再获取openId，最后获取用户信息 */
@Slf4j
@Component
public class QQOAuthProvider implements OAuthProvider {

  private static final String AUTHORIZE_URL = "https://graph.qq.com/oauth2.0/authorize";
  private static final String TOKEN_URL = "https://graph.qq.com/oauth2.0/token";
  private static final String OPENID_URL = "https://graph.qq.com/oauth2.0/me";
  private static final String USER_INFO_URL = "https://graph.qq.com/user/get_user_info";

  @Resource private OAuthProperties oauthProperties;

  private final RestTemplate restTemplate = new RestTemplate();

  @Override
  public String getPlatform() {
    return OAuthPlatformEnum.QQ.getCode();
  }

  @Override
  public String buildAuthorizationUrl(String state, String redirectUri) {
    OAuthProperties.QQConfig config = oauthProperties.getQq();
    try {
      return String.format(
          "%s?client_id=%s&redirect_uri=%s&scope=%s&response_type=code&state=%s",
          AUTHORIZE_URL,
          config.getClientId(),
          URLEncoder.encode(redirectUri, "UTF-8"),
          URLEncoder.encode(config.getScope(), "UTF-8"),
          state);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("UTF-8 encoding not supported", e);
    }
  }

  @Override
  public String getAccessToken(String code, String redirectUri) {
    OAuthProperties.QQConfig config = oauthProperties.getQq();
    try {
      String url =
          String.format(
              "%s?grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s",
              TOKEN_URL,
              config.getClientId(),
              config.getClientSecret(),
              code,
              URLEncoder.encode(redirectUri, "UTF-8"));

      // QQ返回的是query string格式
      String response = restTemplate.getForObject(url, String.class);
      if (response != null && response.contains("access_token")) {
        // 解析 access_token=xxx&expires_in=xxx&refresh_token=xxx
        String[] parts = response.split("&");
        for (String part : parts) {
          if (part.startsWith("access_token=")) {
            return part.substring("access_token=".length());
          }
        }
      }
      log.error("QQ获取access_token失败: {}", response);
      return null;
    } catch (Exception e) {
      log.error("QQ获取access_token异常", e);
      return null;
    }
  }

  /** 获取QQ的openId */
  public String getOpenId(String accessToken) {
    String url = OPENID_URL + "?access_token=" + accessToken + "&fmt=json";
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> response = restTemplate.getForObject(url, Map.class);
      if (response != null && response.containsKey("openid")) {
        return (String) response.get("openid");
      }
      log.error("QQ获取openId失败: {}", response);
      return null;
    } catch (Exception e) {
      log.error("QQ获取openId异常", e);
      return null;
    }
  }

  @Override
  public OAuthUserInfo getUserInfo(String accessToken) {
    // QQ需要先获取openId
    String openId = getOpenId(accessToken);
    if (openId == null) {
      return null;
    }

    OAuthProperties.QQConfig config = oauthProperties.getQq();
    String url =
        String.format(
            "%s?access_token=%s&oauth_consumer_key=%s&openid=%s",
            USER_INFO_URL, accessToken, config.getClientId(), openId);

    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> response = restTemplate.getForObject(url, Map.class);

      if (response == null || (response.containsKey("ret") && (Integer) response.get("ret") != 0)) {
        log.error("QQ获取用户信息失败: {}", response);
        return null;
      }

      OAuthUserInfo userInfo = new OAuthUserInfo();
      userInfo.setPlatform(getPlatform());
      userInfo.setPlatformUserId(openId);
      userInfo.setOpenId(openId);
      userInfo.setNickname((String) response.get("nickname"));
      // QQ头像：figureurl_qq_2 (100x100), figureurl_qq_1 (40x40)
      String avatar = (String) response.get("figureurl_qq_2");
      if (avatar == null) {
        avatar = (String) response.get("figureurl_qq_1");
      }
      userInfo.setAvatar(avatar);
      userInfo.setRawJson(response.toString());

      return userInfo;
    } catch (Exception e) {
      log.error("QQ获取用户信息异常", e);
      return null;
    }
  }

  @Override
  public boolean isEnabled() {
    OAuthProperties.QQConfig config = oauthProperties.getQq();
    return config.isEnabled() && config.getClientId() != null && !config.getClientId().isEmpty();
  }
}
