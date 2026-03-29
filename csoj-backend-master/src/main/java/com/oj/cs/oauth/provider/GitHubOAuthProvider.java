package com.oj.cs.oauth.provider;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import jakarta.annotation.Resource;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.oj.cs.config.OAuthProperties;
import com.oj.cs.constant.OAuthPlatformEnum;
import com.oj.cs.oauth.OAuthProvider;
import com.oj.cs.oauth.OAuthUserInfo;

import lombok.extern.slf4j.Slf4j;

/** GitHub OAuth 提供者 */
@Slf4j
@Component
public class GitHubOAuthProvider implements OAuthProvider {

  private static final String AUTHORIZE_URL = "https://github.com/login/oauth/authorize";
  private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";
  private static final String USER_INFO_URL = "https://api.github.com/user";

  @Resource private OAuthProperties oauthProperties;

  private final RestTemplate restTemplate = new RestTemplate();

  @Override
  public String getPlatform() {
    return OAuthPlatformEnum.GITHUB.getCode();
  }

  @Override
  public String buildAuthorizationUrl(String state, String redirectUri) {
    OAuthProperties.GitHubConfig config = oauthProperties.getGithub();
    try {
      return String.format(
          "%s?client_id=%s&redirect_uri=%s&scope=%s&state=%s",
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
    OAuthProperties.GitHubConfig config = oauthProperties.getGithub();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("client_id", config.getClientId());
    params.add("client_secret", config.getClientSecret());
    params.add("code", code);
    params.add("redirect_uri", redirectUri);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> response = restTemplate.postForObject(TOKEN_URL, request, Map.class);
      if (response != null && response.containsKey("access_token")) {
        return (String) response.get("access_token");
      }
      log.error("GitHub获取access_token失败: {}", response);
      return null;
    } catch (Exception e) {
      log.error("GitHub获取access_token异常", e);
      return null;
    }
  }

  @Override
  public OAuthUserInfo getUserInfo(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

    HttpEntity<Void> request = new HttpEntity<>(headers);

    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> response =
          restTemplate.exchange(USER_INFO_URL, HttpMethod.GET, request, Map.class).getBody();

      if (response == null) {
        return null;
      }

      OAuthUserInfo userInfo = new OAuthUserInfo();
      userInfo.setPlatform(getPlatform());
      userInfo.setPlatformUserId(String.valueOf(response.get("id")));
      userInfo.setNickname((String) response.get("login"));
      userInfo.setAvatar((String) response.get("avatar_url"));
      userInfo.setEmail((String) response.get("email"));
      userInfo.setRawJson(response.toString());

      return userInfo;
    } catch (Exception e) {
      log.error("GitHub获取用户信息异常", e);
      return null;
    }
  }

  @Override
  public boolean isEnabled() {
    OAuthProperties.GitHubConfig config = oauthProperties.getGithub();
    return config.isEnabled() && config.getClientId() != null && !config.getClientId().isEmpty();
  }
}
