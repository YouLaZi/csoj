package com.oj.cs.oauth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import com.oj.cs.constant.OAuthPlatformEnum;
import com.oj.cs.oauth.provider.*;

import lombok.extern.slf4j.Slf4j;

/** OAuth Provider 工厂 根据平台类型获取对应的 Provider */
@Slf4j
@Component
public class OAuthProviderFactory {

  @Resource private GitHubOAuthProvider gitHubOAuthProvider;

  @Resource private GiteeOAuthProvider giteeOAuthProvider;

  @Resource private QQOAuthProvider qqOAuthProvider;

  @Resource private WeChatOAuthProvider weChatOAuthProvider;

  private final Map<String, OAuthProvider> providerMap = new HashMap<>();

  /** 初始化 Provider 映射 */
  private void initProviderMap() {
    if (providerMap.isEmpty()) {
      providerMap.put(OAuthPlatformEnum.GITHUB.getCode(), gitHubOAuthProvider);
      providerMap.put(OAuthPlatformEnum.GITEE.getCode(), giteeOAuthProvider);
      providerMap.put(OAuthPlatformEnum.QQ.getCode(), qqOAuthProvider);
      providerMap.put(OAuthPlatformEnum.WECHAT.getCode(), weChatOAuthProvider);
    }
  }

  /**
   * 根据平台名称获取 Provider
   *
   * @param platform 平台名称（不区分大小写）
   * @return OAuthProvider
   */
  public OAuthProvider getProvider(String platform) {
    initProviderMap();

    OAuthPlatformEnum platformEnum = OAuthPlatformEnum.getByCode(platform);
    if (platformEnum == null) {
      platformEnum = OAuthPlatformEnum.getByPathName(platform);
    }

    if (platformEnum == null) {
      throw new IllegalArgumentException("不支持的OAuth平台: " + platform);
    }

    OAuthProvider provider = providerMap.get(platformEnum.getCode());
    if (provider == null) {
      throw new IllegalArgumentException("未找到OAuth Provider: " + platform);
    }

    return provider;
  }

  /** 获取所有已启用的 Provider */
  public List<OAuthProvider> getEnabledProviders() {
    initProviderMap();
    return providerMap.values().stream().filter(OAuthProvider::isEnabled).toList();
  }

  /** 检查平台是否启用 */
  public boolean isPlatformEnabled(String platform) {
    try {
      OAuthProvider provider = getProvider(platform);
      return provider.isEnabled();
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
