package com.oj.cs.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.config.OAuthProperties;
import com.oj.cs.constant.OAuthPlatformEnum;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.mapper.UserMapper;
import com.oj.cs.mapper.UserOAuthMapper;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.entity.UserOAuth;
import com.oj.cs.model.enums.UserRoleEnum;
import com.oj.cs.model.vo.LoginUserVO;
import com.oj.cs.model.vo.OAuthBindingVO;
import com.oj.cs.model.vo.OAuthLoginVO;
import com.oj.cs.oauth.OAuthProvider;
import com.oj.cs.oauth.OAuthProviderFactory;
import com.oj.cs.oauth.OAuthUserInfo;
import com.oj.cs.service.OAuthService;

import lombok.extern.slf4j.Slf4j;

/** OAuth 服务实现 */
@Slf4j
@Service
public class OAuthServiceImpl extends ServiceImpl<UserOAuthMapper, UserOAuth>
    implements OAuthService {

  private static final String OAUTH_STATE_PREFIX = "oauth_state:";
  private static final long STATE_EXPIRATION = 300; // 5分钟

  @Resource private OAuthProperties oauthProperties;

  @Resource private OAuthProviderFactory providerFactory;

  @Resource private UserMapper userMapper;

  @Resource private StringRedisTemplate stringRedisTemplate;

  @Override
  public String generateState(String platform, HttpServletRequest request) {
    String state = UUID.randomUUID().toString().replace("-", "");
    String stateKey = OAUTH_STATE_PREFIX + state;
    String stateValue = platform.toUpperCase() + ":" + System.currentTimeMillis();
    stringRedisTemplate.opsForValue().set(stateKey, stateValue, STATE_EXPIRATION, TimeUnit.SECONDS);
    return state;
  }

  @Override
  public boolean verifyState(String state, String platform, HttpServletRequest request) {
    if (StringUtils.isBlank(state)) {
      return false;
    }
    String stateKey = OAUTH_STATE_PREFIX + state;
    String stateValue = stringRedisTemplate.opsForValue().get(stateKey);

    if (stateValue == null || !stateValue.startsWith(platform.toUpperCase() + ":")) {
      log.warn("OAuth state 验证失败: state={}, expected platform={}", state, platform);
      return false;
    }

    // 验证通过后删除 state
    stringRedisTemplate.delete(stateKey);
    return true;
  }

  @Override
  public String getRedirectUri(String platform) {
    return oauthProperties.getCallbackUrl(platform);
  }

  @Override
  public OAuthLoginVO processOAuthLogin(
      String platform, String code, String state, HttpServletRequest request) {
    OAuthLoginVO result = new OAuthLoginVO();
    result.setPlatform(platform);

    try {
      // 验证 state（如果提供）
      if (StringUtils.isNotBlank(state) && !verifyState(state, platform, request)) {
        result.setSuccess(false);
        result.setMessage("授权验证失败，请重试");
        return result;
      }

      // 获取 Provider
      OAuthProvider provider = providerFactory.getProvider(platform);
      if (!provider.isEnabled()) {
        result.setSuccess(false);
        result.setMessage("该平台登录未启用");
        return result;
      }

      // 获取 access_token
      String redirectUri = getRedirectUri(platform);
      String accessToken = provider.getAccessToken(code, redirectUri);
      if (StringUtils.isBlank(accessToken)) {
        result.setSuccess(false);
        result.setMessage("获取授权令牌失败");
        return result;
      }

      // 获取用户信息
      OAuthUserInfo userInfo = provider.getUserInfo(accessToken);
      if (userInfo == null) {
        result.setSuccess(false);
        result.setMessage("获取用户信息失败");
        return result;
      }

      // 查找或创建用户
      LoginUserVO loginUserVO = findOrCreateUser(userInfo, platform);

      result.setSuccess(true);
      result.setToken(loginUserVO.getToken());
      result.setUserInfo(loginUserVO);
      result.setNickname(userInfo.getNickname());
      result.setAvatar(userInfo.getAvatar());

      return result;

    } catch (Exception e) {
      log.error("OAuth登录处理异常: platform={}", platform, e);
      result.setSuccess(false);
      result.setMessage("登录失败：" + e.getMessage());
      return result;
    }
  }

  @Override
  public boolean bindOAuthAccount(String platform, String code, HttpServletRequest request) {
    // 暂不实现绑定功能
    throw new BusinessException(ErrorCode.OPERATION_ERROR, "绑定功能暂未开放");
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean unbindOAuthAccount(String platform, Long userId) {
    QueryWrapper<UserOAuth> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("platform", platform.toUpperCase());
    queryWrapper.eq("userId", userId);

    return this.remove(queryWrapper);
  }

  @Override
  public List<OAuthBindingVO> getOAuthBindings(Long userId) {
    QueryWrapper<UserOAuth> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("userId", userId);
    List<UserOAuth> userOAuthList = this.list(queryWrapper);

    List<OAuthBindingVO> result = new ArrayList<>();
    for (UserOAuth userOAuth : userOAuthList) {
      OAuthBindingVO vo = new OAuthBindingVO();
      vo.setId(userOAuth.getId());
      vo.setPlatform(userOAuth.getPlatform());

      OAuthPlatformEnum platformEnum = OAuthPlatformEnum.getByCode(userOAuth.getPlatform());
      if (platformEnum != null) {
        vo.setPlatformName(platformEnum.getName());
      }

      vo.setNickname(userOAuth.getNickname());
      vo.setAvatar(userOAuth.getAvatar());
      vo.setBindTime(userOAuth.getBindTime());
      vo.setEnabled(true);
      result.add(vo);
    }

    return result;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public LoginUserVO findOrCreateUser(OAuthUserInfo userInfo, String platform) {
    // 查找是否已有绑定记录
    QueryWrapper<UserOAuth> oauthQuery = new QueryWrapper<>();
    oauthQuery.eq("platform", platform.toUpperCase());
    oauthQuery.eq("platformUserId", userInfo.getPlatformUserId());
    UserOAuth existingOAuth = this.getOne(oauthQuery);

    User user;
    if (existingOAuth != null) {
      // 已绑定，获取用户
      user = userMapper.selectById(existingOAuth.getUserId());
      if (user == null) {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "关联用户不存在");
      }
    } else {
      // 未绑定，创建新用户
      user = new User();
      user.setUserAccount(generateUniqueAccount(platform, userInfo.getPlatformUserId()));
      user.setUserName(generateUniqueUserName(userInfo.getNickname()));
      user.setUserAvatar(userInfo.getAvatar());
      user.setUserProfile("");
      user.setUserRole(UserRoleEnum.USER.getValue());
      // 随机密码（OAuth用户不需要密码登录）
      String randomPassword = UUID.randomUUID().toString();

      int insertResult = userMapper.insert(user);
      if (insertResult <= 0) {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建用户失败");
      }

      // 创建绑定记录
      UserOAuth userOAuth = new UserOAuth();
      userOAuth.setUserId(user.getId());
      userOAuth.setPlatform(platform.toUpperCase());
      userOAuth.setPlatformUserId(userInfo.getPlatformUserId());
      userOAuth.setOpenId(userInfo.getOpenId());
      userOAuth.setUnionId(userInfo.getUnionId());
      userOAuth.setNickname(userInfo.getNickname());
      userOAuth.setAvatar(userInfo.getAvatar());
      userOAuth.setBindTime(new Date());
      userOAuth.setUpdateTime(new Date());
      this.save(userOAuth);

      log.info(
          "OAuth创建新用户: userId={}, platform={}, platformUserId={}",
          user.getId(),
          platform,
          userInfo.getPlatformUserId());
    }

    // 检查用户状态
    if (UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
      throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "该用户已被封禁");
    }

    // 生成 Token
    String token = UUID.randomUUID().toString();
    stringRedisTemplate
        .opsForValue()
        .set("login_token:" + token, user.getId().toString(), 24, TimeUnit.HOURS);

    // 构建 LoginUserVO
    LoginUserVO loginUserVO = new LoginUserVO();
    BeanUtils.copyProperties(user, loginUserVO);
    loginUserVO.setToken(token);

    return loginUserVO;
  }

  /** 生成唯一账号 */
  private String generateUniqueAccount(String platform, String platformUserId) {
    String prefix = platform.toLowerCase();
    String suffix = platformUserId.length() > 10 ? platformUserId.substring(0, 10) : platformUserId;
    return prefix + "_" + suffix + "_" + System.currentTimeMillis() % 10000;
  }

  /** 生成唯一用户名 */
  private String generateUniqueUserName(String nickname) {
    if (StringUtils.isBlank(nickname)) {
      return "用户" + System.currentTimeMillis() % 10000;
    }
    // 截取前10个字符
    String name = nickname.length() > 10 ? nickname.substring(0, 10) : nickname;
    // 移除特殊字符
    name = name.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "");
    if (StringUtils.isBlank(name)) {
      name = "用户" + System.currentTimeMillis() % 10000;
    }
    return name;
  }
}
