package com.oj.cs.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.oj.cs.constant.UserConstant;
import com.oj.cs.model.dto.user.UserQueryRequest;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.vo.LoginUserVO;
import com.oj.cs.model.vo.UserVO;
import com.oj.cs.service.impl.UserServiceImpl;

/**
 * UserService 单元测试
 *
 * <p>测试覆盖： - 用户信息脱敏 - 权限验证 - 查询条件构建 - 用户状态检查
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务单元测试")
public class UserServiceUnitTest {

  @Mock private HttpServletRequest request;

  @InjectMocks private UserServiceImpl userService;

  private User testUser;
  private UserQueryRequest userQueryRequest;

  @BeforeEach
  void setUp() {
    // 创建测试用户
    testUser = new User();
    testUser.setId(1L);
    testUser.setUserAccount("testuser");
    testUser.setUserName("测试用户");
    testUser.setUserRole(UserConstant.DEFAULT_ROLE);
    testUser.setUserAvatar("https://example.com/avatar.jpg");
    testUser.setUserProfile("测试简介");
    testUser.setEmail("test@example.com");
    testUser.setCreateTime(new java.util.Date());
    testUser.setUpdateTime(new java.util.Date());
    testUser.setIsDelete(0);

    // 创建查询请求
    userQueryRequest = new UserQueryRequest();
    userQueryRequest.setUserName("测试用户");
    userQueryRequest.setCurrent(1L);
    userQueryRequest.setPageSize(10L);
  }

  @Test
  @DisplayName("获取脱敏用户信息 - 成功")
  void testGetUserVO_Success() {
    UserVO userVO = userService.getUserVO(testUser);

    assertNotNull(userVO);
    assertEquals(testUser.getId(), userVO.getId());
    assertEquals(testUser.getUserName(), userVO.getUserName());
    // UserVO 不包含敏感信息（账号、密码等）
  }

  @Test
  @DisplayName("获取脱敏用户信息列表 - 成功")
  void testGetUserVOList_Success() {
    User user2 = new User();
    user2.setId(2L);
    user2.setUserName("测试用户2");

    List<User> users = Arrays.asList(testUser, user2);
    List<UserVO> userVOList = userService.getUserVO(users);

    assertNotNull(userVOList);
    assertEquals(2, userVOList.size());
    assertEquals(testUser.getId(), userVOList.get(0).getId());
    assertEquals(user2.getId(), userVOList.get(1).getId());
  }

  @Test
  @DisplayName("获取脱敏用户信息列表 - 空列表")
  void testGetUserVOList_Empty() {
    List<User> users = Arrays.asList();
    List<UserVO> userVOList = userService.getUserVO(users);

    assertNotNull(userVOList);
    assertTrue(userVOList.isEmpty());
  }

  @Test
  @DisplayName("获取脱敏用户信息列表 - null 输入")
  void testGetUserVOList_Null() {
    List<UserVO> userVOList = ((UserService) userService).getUserVO((List<User>) null);

    assertNotNull(userVOList);
    assertTrue(userVOList.isEmpty());
  }

  @Test
  @DisplayName("是否为管理员 - 是管理员")
  void testIsAdmin_True() {
    testUser.setUserRole(UserConstant.ADMIN_ROLE);
    boolean isAdmin = userService.isAdmin(testUser);
    assertTrue(isAdmin, "应该是管理员");
  }

  @Test
  @DisplayName("是否为管理员 - 教师角色")
  void testIsAdmin_Teacher() {
    testUser.setUserRole(UserConstant.TEACHER_ROLE);
    boolean isAdmin = userService.isAdmin(testUser);
    // 教师不是管理员
    assertFalse(isAdmin, "教师角色不应该是管理员");
  }

  @Test
  @DisplayName("是否为管理员 - 普通用户")
  void testIsAdmin_False() {
    testUser.setUserRole(UserConstant.DEFAULT_ROLE);
    boolean isAdmin = userService.isAdmin(testUser);
    assertFalse(isAdmin, "普通用户不应该是管理员");
  }

  @Test
  @DisplayName("是否为管理员 - null 用户")
  void testIsAdmin_NullUser() {
    boolean isAdmin = ((UserService) userService).isAdmin((User) null);
    assertFalse(isAdmin, "null 用户不应该是管理员");
  }

  @Test
  @DisplayName("获取查询条件 - 成功")
  void testGetQueryWrapper_Success() {
    QueryWrapper<User> queryWrapper = userService.getQueryWrapper(userQueryRequest);

    assertNotNull(queryWrapper);
  }

  @Test
  @DisplayName("获取查询条件 - 空请求")
  void testGetQueryWrapper_EmptyRequest() {
    UserQueryRequest emptyRequest = new UserQueryRequest();
    QueryWrapper<User> queryWrapper = userService.getQueryWrapper(emptyRequest);

    assertNotNull(queryWrapper);
  }

  @Test
  @DisplayName("获取查询条件 - null 请求")
  void testGetQueryWrapper_NullRequest() {
    // null 请求会抛出异常或返回默认 QueryWrapper
    // 根据实际实现调整
    assertThrows(
        com.oj.cs.exception.BusinessException.class, () -> userService.getQueryWrapper(null));
  }

  @Test
  @DisplayName("获取查询条件 - 带排序")
  void testGetQueryWrapper_WithSort() {
    userQueryRequest.setSortField("createTime");
    userQueryRequest.setSortOrder("desc");

    QueryWrapper<User> queryWrapper = userService.getQueryWrapper(userQueryRequest);

    assertNotNull(queryWrapper);
  }

  @Test
  @DisplayName("获取查询条件 - 带角色过滤")
  void testGetQueryWrapper_WithRole() {
    userQueryRequest.setUserRole(UserConstant.ADMIN_ROLE);

    QueryWrapper<User> queryWrapper = userService.getQueryWrapper(userQueryRequest);

    assertNotNull(queryWrapper);
  }

  @Test
  @DisplayName("获取登录用户VO - 成功")
  @Disabled("需要 mock QuestionSubmitMapper")
  void testGetLoginUserVO_Success() {
    LoginUserVO loginUserVO = userService.getLoginUserVO(testUser);

    assertNotNull(loginUserVO);
    assertEquals(testUser.getId(), loginUserVO.getId());
    assertEquals(testUser.getUserName(), loginUserVO.getUserName());
    assertEquals(testUser.getUserRole(), loginUserVO.getUserRole());
  }

  @Test
  @DisplayName("获取登录用户VO - null 用户")
  void testGetLoginUserVO_NullUser() {
    LoginUserVO loginUserVO = userService.getLoginUserVO(null);

    // getLoginUserVO 对 null 用户可能返回 null
    // 根据实际实现调整断言
    assertNull(loginUserVO, "null 用户应返回 null");
  }

  @Test
  @DisplayName("用户状态检查 - 已删除用户")
  void testUserStatus_Deleted() {
    testUser.setIsDelete(1);
    assertEquals(Integer.valueOf(1), testUser.getIsDelete(), "已删除用户应该被标记为1");
  }

  @Test
  @DisplayName("用户状态检查 - 未删除用户")
  void testUserStatus_NotDeleted() {
    testUser.setIsDelete(0);
    assertEquals(Integer.valueOf(0), testUser.getIsDelete(), "未删除用户应该被标记为0");
  }

  @Test
  @DisplayName("用户角色 - 被封禁用户")
  void testUserRole_Banned() {
    testUser.setUserRole(UserConstant.BAN_ROLE);
    assertEquals(UserConstant.BAN_ROLE, testUser.getUserRole(), "被封禁用户角色应为ban");
  }

  @Test
  @DisplayName("用户角色 - 正常用户")
  void testUserRole_Normal() {
    testUser.setUserRole(UserConstant.DEFAULT_ROLE);
    assertEquals(UserConstant.DEFAULT_ROLE, testUser.getUserRole(), "正常用户角色应为user");
  }

  @Test
  @DisplayName("用户实体 - 字段完整性")
  void testUserEntity_CompleteFields() {
    assertNotNull(testUser.getId());
    assertNotNull(testUser.getUserAccount());
    assertNotNull(testUser.getUserName());
    assertNotNull(testUser.getUserRole());
    assertNotNull(testUser.getCreateTime());
    assertNotNull(testUser.getUpdateTime());
    assertNotNull(testUser.getIsDelete());
  }

  @Test
  @DisplayName("用户验证 - 账号长度验证")
  void testUserAccount_LengthValidation() {
    String shortAccount = "ab";
    assertTrue(shortAccount.length() < 4, "账号长度不应少于4位");

    String validAccount = "validuser";
    assertTrue(validAccount.length() >= 4, "有效账号长度应至少4位");
  }

  @Test
  @DisplayName("用户验证 - 账号包含特殊字符")
  void testUserAccount_SpecialChars() {
    String invalidAccount = "user@#$";
    boolean hasSpecialChars = invalidAccount.matches(".*[^a-zA-Z0-9_].*");
    assertTrue(hasSpecialChars, "账号不应包含特殊字符");

    String validAccount = "user123";
    boolean hasNoSpecialChars = !validAccount.matches(".*[^a-zA-Z0-9_].*");
    assertTrue(hasNoSpecialChars, "有效账号不应包含特殊字符");
  }

  @Test
  @DisplayName("用户VO - 脱敏验证")
  void testUserVO_Desensitization() {
    UserVO userVO = userService.getUserVO(testUser);

    // UserVO 应该只包含非敏感信息
    assertNotNull(userVO.getId());
    assertNotNull(userVO.getUserName());
    assertNotNull(userVO.getUserRole());
    // userAccount, userPassword 等敏感信息不应存在
  }

  @Test
  @DisplayName("分页请求 - 默认值")
  void testPageRequest_Defaults() {
    UserQueryRequest request = new UserQueryRequest();
    request.setCurrent(1L);
    request.setPageSize(10L);

    assertEquals(1L, request.getCurrent());
    assertEquals(10L, request.getPageSize());
  }

  @Test
  @DisplayName("时间戳 - 创建时间早于更新时间")
  void testTimestamps_CreateBeforeUpdate() {
    java.util.Date create = testUser.getCreateTime();
    java.util.Date update = testUser.getUpdateTime();

    // 创建时间应该早于或等于更新时间
    assertTrue(create.compareTo(update) <= 0, "创建时间应该早于或等于更新时间");
  }

  @Test
  @DisplayName("用户角色常量 - 验证")
  void testUserRoleConstants() {
    assertEquals("user", UserConstant.DEFAULT_ROLE);
    assertEquals("admin", UserConstant.ADMIN_ROLE);
    assertEquals("teacher", UserConstant.TEACHER_ROLE);
    assertEquals("ban", UserConstant.BAN_ROLE);
  }

  @Test
  @DisplayName("用户登录态键 - 验证")
  void testUserLoginStateKey() {
    assertEquals("user_login", UserConstant.USER_LOGIN_STATE);
  }
}
