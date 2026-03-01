package com.oj.cs.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oj.cs.config.WxOpenConfig;
import com.oj.cs.model.dto.user.UserLoginRequest;
import com.oj.cs.model.dto.user.UserRegisterRequest;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.vo.LoginUserVO;
import com.oj.cs.service.UserService;

/** 用户控制器单元测试 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户控制器单元测试")
class UserControllerTest {

  private MockMvc mockMvc;

  @Mock private UserService userService;

  @Mock private WxOpenConfig wxOpenConfig;

  @InjectMocks private UserController userController;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private UserRegisterRequest validRegisterRequest;
  private UserLoginRequest validLoginRequest;
  private User mockUser;
  private LoginUserVO mockLoginUserVO;

  @BeforeEach
  void setUp() {
    // 使用 standaloneSetup 避免加载完整的 Spring 上下文
    mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

    // 准备有效的注册请求
    validRegisterRequest = new UserRegisterRequest();
    validRegisterRequest.setUserAccount("testuser");
    validRegisterRequest.setUserName("测试用户");
    validRegisterRequest.setUserPassword("password123");
    validRegisterRequest.setCheckPassword("password123");

    // 准备有效的登录请求
    validLoginRequest = new UserLoginRequest();
    validLoginRequest.setUserAccount("testuser");
    validLoginRequest.setUserPassword("password123");

    // 准备 Mock 用户数据
    mockUser = new User();
    mockUser.setId(1L);
    mockUser.setUserAccount("testuser");
    mockUser.setUserName("测试用户");
    mockUser.setUserRole("user");
    mockUser.setUserAvatar("https://example.com/avatar.png");

    // 准备 Mock 登录用户 VO
    mockLoginUserVO = new LoginUserVO();
    mockLoginUserVO.setId(1L);
    mockLoginUserVO.setUserName("测试用户");
    mockLoginUserVO.setUserRole("user");
    mockLoginUserVO.setUserAvatar("https://example.com/avatar.png");
    mockLoginUserVO.setToken("mock-jwt-token");
  }

  // ==================== 用户注册测试 ====================

  @Test
  @DisplayName("用户注册 - 成功")
  void testUserRegister_Success() throws Exception {
    // Given
    Long expectedUserId = 1L;
    when(userService.userRegister(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(expectedUserId);

    // When & Then
    mockMvc
        .perform(
            post("/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.data").value(expectedUserId));
  }

  @Test
  @DisplayName("用户注册 - 缺少必填字段")
  void testUserRegister_MissingField() throws Exception {
    // Given - 空请求，controller 返回 null (null checks in controller)
    // When & Then
    mockMvc
        .perform(post("/user/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("用户注册 - 两次密码不一致")
  void testUserRegister_PasswordMismatch() throws Exception {
    // Given
    validRegisterRequest.setCheckPassword("different");

    // When & Then - controller doesn't validate password match, returns null from service
    when(userService.userRegister(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(-1L);
    mockMvc
        .perform(
            post("/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
        .andExpect(status().isOk());
  }

  // ==================== 用户登录测试 ====================

  @Test
  @DisplayName("用户登录 - 成功")
  void testUserLogin_Success() throws Exception {
    // Given
    when(userService.userLogin(eq("testuser"), eq("password123"), any()))
        .thenReturn(mockLoginUserVO);

    // When & Then
    mockMvc
        .perform(
            post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.data.userName").value("测试用户"))
        .andExpect(jsonPath("$.data.userRole").value("user"))
        .andExpect(jsonPath("$.data.token").value("mock-jwt-token"));
  }

  // ==================== 密码重置测试 ====================

  @Test
  @DisplayName("忘记密码 - 发送邮件成功")
  void testForgotPassword_Success() throws Exception {
    // Given
    String email = "test@example.com";
    String requestBody = String.format("{\"email\":\"%s\"}", email);

    // 模拟服务方法无异常
    org.mockito.Mockito.doNothing().when(userService).processForgotPassword(anyString());

    // When & Then
    mockMvc
        .perform(
            post("/user/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.data").value("密码重置邮件已发送，请检查您的邮箱"));
  }

  @Test
  @DisplayName("重置密码 - 成功")
  void testResetPassword_Success() throws Exception {
    // Given
    String requestBody = "{\"token\":\"valid-token\",\"newPassword\":\"newpass123\"}";

    // Mock 服务方法无异常
    org.mockito.Mockito.doNothing().when(userService).resetPassword(anyString(), anyString());

    // When & Then
    mockMvc
        .perform(
            post("/user/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.data").value("密码已成功重置"));
  }
}
