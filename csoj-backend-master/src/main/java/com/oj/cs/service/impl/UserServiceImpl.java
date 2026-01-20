package com.oj.cs.service.impl;

import static com.oj.cs.constant.UserConstant.USER_LOGIN_STATE;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.constant.CommonConstant;
import com.oj.cs.constant.PermissionConstant;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.mapper.QuestionSubmitMapper;
import com.oj.cs.mapper.UserMapper;
import com.oj.cs.mapper.UserPointsMapper;
import com.oj.cs.model.dto.user.UserQueryRequest;
import com.oj.cs.model.entity.QuestionSubmit;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.entity.UserPoints;
import com.oj.cs.model.enums.QuestionSubmitStatusEnum;
import com.oj.cs.model.enums.UserRoleEnum;
import com.oj.cs.model.vo.LoginUserVO;
import com.oj.cs.model.vo.UserPermissionVO;
import com.oj.cs.model.vo.UserVO;
import com.oj.cs.service.UserService;
import com.oj.cs.utils.PasswordUtil;
import com.oj.cs.utils.SqlUtils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;

/** 用户服务实现 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

  @Resource private StringRedisTemplate stringRedisTemplate; // 注入StringRedisTemplate

  @Resource private QuestionSubmitMapper questionSubmitMapper;

  @Resource private UserPointsMapper userPointsMapper;

  /** 盐值，混淆密码 */
  private static final String SALT = "admin";

  @Override
  public long userRegister(
      String userAccount, String userName, String userPassword, String checkPassword) {
    // 1. 校验
    if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
    }
    if (userAccount.length() < 4) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
    }
    if (userPassword.length() < 8 || checkPassword.length() < 8) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
    }
    // 密码和校验密码相同
    if (!userPassword.equals(checkPassword)) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
    }
    synchronized (userAccount.intern()) {
      // 账户不能重复
      QueryWrapper<User> queryWrapper = new QueryWrapper<>();
      queryWrapper.eq("userAccount", userAccount);
      long count = this.baseMapper.selectCount(queryWrapper);
      if (count > 0) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
      }
      // 2. 加密（使用 BCrypt）
      String encryptPassword = PasswordUtil.hash(userPassword);
      // 3. 插入数据
      User user = new User();
      user.setUserAccount(userAccount);
      user.setUserPassword(encryptPassword);
      user.setUserName(userName);
      boolean saveResult = this.save(user);
      if (!saveResult) {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
      }
      return user.getId();
    }
  }

  @Override
  public LoginUserVO userLogin(
      String userAccount, String userPassword, HttpServletRequest request) {
    // 1. 校验
    if (StringUtils.isAnyBlank(userAccount, userPassword)) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
    }
    if (userAccount.length() < 4) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
    }
    if (userPassword.length() < 8) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
    }

    // 2. 查询用户
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("userAccount", userAccount);
    User user = this.baseMapper.selectOne(queryWrapper);

    // 用户不存在
    if (user == null) {
      log.info("user login failed, userAccount not found: {}", userAccount);
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
    }

    // 3. 验证密码（支持 BCrypt 和旧 MD5 兼容）
    String storedPassword = user.getUserPassword();
    boolean passwordMatch = false;
    boolean needUpgrade = false;

    if (PasswordUtil.isBCryptHash(storedPassword)) {
      // 使用 BCrypt 验证
      passwordMatch = PasswordUtil.verify(userPassword, storedPassword);
    } else {
      // 使用旧的 MD5 验证（兼容现有用户）
      String oldEncryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
      passwordMatch = oldEncryptPassword.equals(storedPassword);
      needUpgrade = true; // 标记需要升级密码
      log.info("用户 {} 使用旧 MD5 密码登录，验证成功后将升级", userAccount);
    }

    if (!passwordMatch) {
      log.info("user login failed, password incorrect for userAccount: {}", userAccount);
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
    }

    // 4. 如果使用旧密码，自动升级为 BCrypt
    if (needUpgrade) {
      try {
        String newEncryptPassword = PasswordUtil.hash(userPassword);
        user.setUserPassword(newEncryptPassword);
        this.updateById(user);
        log.info("用户 {} 密码已从 MD5 升级为 BCrypt", userAccount);
      } catch (Exception e) {
        log.error("密码升级失败，但登录成功: userAccount={}", userAccount, e);
        // 升级失败不影响登录
      }
    }

    // 5. 生成Token
    String token = UUID.randomUUID().toString();
    // 将token存入Redis，有效期设置为24小时，可以根据实际需求调整
    stringRedisTemplate
        .opsForValue()
        .set("login_token:" + token, user.getId().toString(), 24, TimeUnit.HOURS);

    // 6. 记录用户的登录态
    request.getSession().setAttribute(USER_LOGIN_STATE, user);
    LoginUserVO loginUserVO = this.getLoginUserVO(user);
    loginUserVO.setToken(token); // 设置token到LoginUserVO
    return loginUserVO;
  }

  @Override
  public LoginUserVO userLoginByMpOpen(
      WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request) {
    String unionId = wxOAuth2UserInfo.getUnionId();
    String mpOpenId = wxOAuth2UserInfo.getOpenid();
    // 单机锁
    synchronized (unionId.intern()) {
      // 查询用户是否已存在
      QueryWrapper<User> queryWrapper = new QueryWrapper<>();
      queryWrapper.eq("unionId", unionId);
      User user = this.getOne(queryWrapper);
      // 被封号，禁止登录
      if (user != null && UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
        throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "该用户已被封，禁止登录");
      }
      // 用户不存在则创建
      if (user == null) {
        user = new User();
        user.setUnionId(unionId);
        user.setMpOpenId(mpOpenId);
        user.setUserAvatar(wxOAuth2UserInfo.getHeadImgUrl());
        user.setUserName(wxOAuth2UserInfo.getNickname());
        boolean result = this.save(user);
        if (!result) {
          throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
        }
      }
      // 记录用户的登录态
      request.getSession().setAttribute(USER_LOGIN_STATE, user);
      return getLoginUserVO(user);
    }
  }

  /**
   * 获取当前登录用户
   *
   * @param request
   * @return
   */
  @Override
  public User getLoginUser(HttpServletRequest request) {
    // 先判断是否已登录
    Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
    User currentUser = (User) userObj;
    if ((currentUser == null || currentUser.getId() == null)) {
      // 新增：支持前后端分离，允许通过 Authorization Bearer token 自动登录
      String authHeader = request.getHeader("Authorization");
      if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        String userIdStr = stringRedisTemplate.opsForValue().get("login_token:" + token);
        if (userIdStr != null) {
          long userId = Long.parseLong(userIdStr);
          currentUser = this.getById(userId);
          if (currentUser != null) {
            // 自动设置 session，兼容原有逻辑
            request.getSession().setAttribute(USER_LOGIN_STATE, currentUser);
          }
        }
      }
    }
    if (currentUser == null || currentUser.getId() == null) {
      throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
    }
    // 从数据库查询（追求性能的话可以注释，直接走缓存）
    long userId = currentUser.getId();
    currentUser = this.getById(userId);
    if (currentUser == null) {
      throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
    }
    return currentUser;
  }

  /**
   * 获取当前登录用户（允许未登录）
   *
   * @param request
   * @return
   */
  @Override
  public User getLoginUserPermitNull(HttpServletRequest request) {
    // 先判断是否已登录
    Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
    User currentUser = (User) userObj;
    if (currentUser == null || currentUser.getId() == null) {
      return null;
    }
    // 从数据库查询（追求性能的话可以注释，直接走缓存）
    long userId = currentUser.getId();
    return this.getById(userId);
  }

  /**
   * 是否为管理员
   *
   * @param request
   * @return
   */
  @Override
  public boolean isAdmin(HttpServletRequest request) {
    // 仅管理员可查询
    Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
    User user = (User) userObj;
    return isAdmin(user);
  }

  @Override
  public boolean isAdmin(User user) {
    return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
  }

  /**
   * 用户注销
   *
   * @param request
   */
  @Override
  public boolean userLogout(HttpServletRequest request) {
    if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
    }
    // 移除登录态
    request.getSession().removeAttribute(USER_LOGIN_STATE);
    return true;
  }

  @Override
  public LoginUserVO getLoginUserVO(User user) {
    if (user == null) {
      return null;
    }
    LoginUserVO loginUserVO = new LoginUserVO();
    BeanUtils.copyProperties(user, loginUserVO);

    // 查询用户统计数据
    Long userId = user.getId();

    // 查询提交次数
    QueryWrapper<QuestionSubmit> submitQueryWrapper = new QueryWrapper<>();
    submitQueryWrapper.eq("userId", userId);
    long submissionCount = questionSubmitMapper.selectCount(submitQueryWrapper);
    loginUserVO.setSubmissionCount((int) submissionCount);

    // 查询已解决题目数量（状态为成功的不重复题目数）
    QueryWrapper<QuestionSubmit> solvedQueryWrapper = new QueryWrapper<>();
    solvedQueryWrapper
        .eq("userId", userId)
        .eq("status", QuestionSubmitStatusEnum.SUCCEED.getValue())
        .select("questionId");
    List<QuestionSubmit> solvedSubmissions = questionSubmitMapper.selectList(solvedQueryWrapper);
    long solvedCount =
        solvedSubmissions.stream().map(QuestionSubmit::getQuestionId).distinct().count();
    loginUserVO.setSolvedCount((int) solvedCount);

    // 查询用户积分
    QueryWrapper<UserPoints> pointsQueryWrapper = new QueryWrapper<>();
    pointsQueryWrapper.eq("userId", userId);
    UserPoints userPoints = userPointsMapper.selectOne(pointsQueryWrapper);
    int points =
        (userPoints != null && userPoints.getTotalPoints() != null)
            ? userPoints.getTotalPoints()
            : 0;
    loginUserVO.setPoints(points);

    return loginUserVO;
  }

  @Override
  public UserVO getUserVO(User user) {
    if (user == null) {
      return null;
    }
    UserVO userVO = new UserVO();
    BeanUtils.copyProperties(user, userVO);
    return userVO;
  }

  @Override
  public List<UserVO> getUserVO(List<User> userList) {
    if (CollectionUtils.isEmpty(userList)) {
      return new ArrayList<>();
    }
    return userList.stream().map(this::getUserVO).collect(Collectors.toList());
  }

  @Override
  public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
    if (userQueryRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
    }
    Long id = userQueryRequest.getId();
    String unionId = userQueryRequest.getUnionId();
    String mpOpenId = userQueryRequest.getMpOpenId();
    String userName = userQueryRequest.getUserName();
    String userProfile = userQueryRequest.getUserProfile();
    String userRole = userQueryRequest.getUserRole();
    String sortField = userQueryRequest.getSortField();
    String sortOrder = userQueryRequest.getSortOrder();
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq(id != null, "id", id);
    queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
    queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
    queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
    queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
    queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
    queryWrapper.orderBy(
        SqlUtils.validSortField(sortField),
        sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
        sortField);
    return queryWrapper;
  }

  // 注意：确保User实体类中有email字段，并且数据库表user中也有对应的email列。
  // 如果没有，需要先在User.java实体类中添加email字段，并更新数据库表结构。
  // 例如，在User.java中添加：
  // private String email;
  // 并在数据库user表中添加email列。

  @Override
  public void processForgotPassword(String email) {
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("email", email); // 假设User实体有email字段
    // 使用getOne方法的重载版本，设置throwEx为false，避免多条记录时抛出异常
    // 或者使用list方法获取所有匹配记录，然后取第一条
    List<User> userList = this.list(queryWrapper);

    if (userList == null || userList.isEmpty()) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "该邮箱未注册");
    }

    // 取第一个匹配的用户
    User user = userList.get(0);

    // 如果有多个用户使用相同邮箱，记录警告日志
    if (userList.size() > 1) {
      log.warn("发现多个用户使用相同邮箱: {}, 用户数量: {}", email, userList.size());
    }

    String token = UUID.randomUUID().toString();
    // 将token存入Redis，有效期设置为1小时
    stringRedisTemplate
        .opsForValue()
        .set("reset_password_token:" + token, user.getId().toString(), 1, TimeUnit.HOURS);

    // 此处应调用邮件服务发送密码重置邮件，包含重置链接，例如: /reset-password?token=xxx
    log.info("向邮箱 {} 发送密码重置邮件，token: {}", email, token);

    // 使用Hutool发送邮件
    try {
      // 创建邮件客户端
      MailAccount account = new MailAccount();
      // 配置QQ邮箱（也可以使用163等其他国内邮箱）
      account.setHost("smtp.qq.com");
      account.setPort(465);
      account.setSslEnable(true);
      account.setAuth(true);
      account.setFrom("3179738838@qq.com"); // 发件人邮箱
      account.setUser("3179738838@qq.com"); // 邮箱账号
      account.setPass("qhcrpihhdkimdead"); // 邮箱授权码，非邮箱密码

      // 创建邮件内容
      String resetUrl = "http://localhost:8080/user/reset-password?token=" + token;
      String content =
          "<div style='padding:20px;'>"
              + "<h2>密码重置</h2>"
              + "<p>您好，您正在进行密码重置操作。</p>"
              + "<p>请点击下面的链接重置您的密码：</p>"
              + "<p><a href='"
              + resetUrl
              + "'>重置密码</a></p>"
              + "<p>如果链接无法点击，请复制以下地址到浏览器地址栏访问：</p>"
              + "<p>"
              + resetUrl
              + "</p>"
              + "<p>此链接有效期为1小时，请尽快操作。</p>"
              + "<p>如果您没有请求重置密码，请忽略此邮件。</p>"
              + "</div>";

      // 发送邮件
      MailUtil.send(account, CollUtil.newArrayList(email), "密码重置", content, true);
      log.info("密码重置邮件发送成功，邮箱：{}", email);
    } catch (Exception e) {
      log.error("发送密码重置邮件失败", e);
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "邮件发送失败，请稍后重试");
    }
  }

  @Override
  public void resetPassword(String token, String newPassword) {
    String userIdStr = stringRedisTemplate.opsForValue().get("reset_password_token:" + token);
    if (StringUtils.isBlank(userIdStr)) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效或已过期的重置链接");
    }

    long userId = Long.parseLong(userIdStr);
    User user = this.getById(userId);
    if (user == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
    }

    if (StringUtils.isBlank(newPassword) || newPassword.length() < 8) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码长度不能少于8位");
    }

    // 加密新密码（使用 BCrypt）
    String encryptPassword = PasswordUtil.hash(newPassword);
    user.setUserPassword(encryptPassword);
    boolean updateResult = this.updateById(user);

    if (!updateResult) {
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "密码重置失败");
    }

    // 密码重置成功后，删除Redis中的token
    stringRedisTemplate.delete("reset_password_token:" + token);
    log.info("用户 {} 的密码已重置", userId);
  }

  @Override
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
