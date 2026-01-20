package com.oj.cs.controller;

import java.util.List;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.cs.annotation.AuditLog;
import com.oj.cs.annotation.AuthCheck;
import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.DeleteRequest;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.config.WxOpenConfig;
import com.oj.cs.constant.UserConstant;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.exception.ThrowUtils;
import com.oj.cs.model.dto.user.EmailRequest;
import com.oj.cs.model.dto.user.ResetPasswordRequest;
import com.oj.cs.model.dto.user.UserAddRequest;
import com.oj.cs.model.dto.user.UserLoginRequest;
import com.oj.cs.model.dto.user.UserQueryRequest;
import com.oj.cs.model.dto.user.UserRegisterRequest;
import com.oj.cs.model.dto.user.UserUpdateMyRequest;
import com.oj.cs.model.dto.user.UserUpdateRequest;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.vo.LoginUserVO;
import com.oj.cs.model.vo.UserVO;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;

/** 用户接口 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

  @Resource private UserService userService;

  @Resource private WxOpenConfig wxOpenConfig;

  // region 登录相关

  /**
   * 请求密码重置
   *
   * @param emailRequest 包含用户邮箱的请求体
   * @return 操作结果
   */
  @PostMapping("/forgot-password")
  public BaseResponse<String> forgotPassword(@RequestBody EmailRequest emailRequest) {
    if (emailRequest == null || StringUtils.isBlank(emailRequest.getEmail())) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱不能为空");
    }
    String email = emailRequest.getEmail();
    userService.processForgotPassword(email);
    return ResultUtils.success("密码重置邮件已发送，请检查您的邮箱");
  }

  /**
   * 重置密码
   *
   * @param resetPasswordRequest 包含token和新密码的请求体
   * @return 操作结果
   */
  @PostMapping("/reset-password")
  public BaseResponse<String> resetPassword(
      @RequestBody ResetPasswordRequest resetPasswordRequest) {
    if (resetPasswordRequest == null
        || StringUtils.isAnyBlank(
            resetPasswordRequest.getToken(), resetPasswordRequest.getNewPassword())) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "token和新密码不能为空");
    }
    userService.resetPassword(
        resetPasswordRequest.getToken(), resetPasswordRequest.getNewPassword());
    return ResultUtils.success("密码已成功重置");
  }

  /**
   * 用户注册
   *
   * @param userRegisterRequest
   * @return
   */
  @PostMapping("/register")
  public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
    if (userRegisterRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    String userAccount = userRegisterRequest.getUserAccount();
    String userPassword = userRegisterRequest.getUserPassword();
    String checkPassword = userRegisterRequest.getCheckPassword();
    String userName = userRegisterRequest.getUserName();
    if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
      return null;
    }
    long result = userService.userRegister(userAccount, userName, userPassword, checkPassword);
    return ResultUtils.success(result);
  }

  /**
   * 用户登录
   *
   * @param userLoginRequest
   * @param request
   * @return
   */
  @PostMapping("/login")
  @AuditLog(module = "用户管理", operationType = "LOGIN", description = "用户登录")
  public BaseResponse<LoginUserVO> userLogin(
      @RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
    if (userLoginRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    String userAccount = userLoginRequest.getUserAccount();
    String userPassword = userLoginRequest.getUserPassword();
    if (StringUtils.isAnyBlank(userAccount, userPassword)) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
    return ResultUtils.success(loginUserVO);
  }

  /** 用户登录（微信开放平台） */
  @GetMapping("/login/wx_open")
  public BaseResponse<LoginUserVO> userLoginByWxOpen(
      HttpServletRequest request, HttpServletResponse response, @RequestParam("code") String code) {
    WxOAuth2AccessToken accessToken;
    try {
      WxMpService wxService = wxOpenConfig.getWxMpService();
      accessToken = wxService.getOAuth2Service().getAccessToken(code);
      WxOAuth2UserInfo userInfo = wxService.getOAuth2Service().getUserInfo(accessToken, code);
      String unionId = userInfo.getUnionId();
      String mpOpenId = userInfo.getOpenid();
      if (StringUtils.isAnyBlank(unionId, mpOpenId)) {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败，系统错误");
      }
      return ResultUtils.success(userService.userLoginByMpOpen(userInfo, request));
    } catch (Exception e) {
      log.error("userLoginByWxOpen error", e);
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败，系统错误");
    }
  }

  /**
   * 用户注销
   *
   * @param request
   * @return
   */
  @PostMapping("/logout")
  @AuditLog(module = "用户管理", operationType = "LOGOUT", description = "用户注销")
  public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
    if (request == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    boolean result = userService.userLogout(request);
    return ResultUtils.success(result);
  }

  /**
   * 获取当前登录用户
   *
   * @param request
   * @return
   */
  @GetMapping("/get/login")
  public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
    User user = userService.getLoginUser(request);
    return ResultUtils.success(userService.getLoginUserVO(user));
  }

  // endregion

  // region 增删改查

  /**
   * 创建用户
   *
   * @param userAddRequest
   * @param request
   * @return
   */
  @PostMapping("/add")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  @AuditLog(module = "用户管理", operationType = "CREATE", description = "创建用户")
  public BaseResponse<Long> addUser(
      @RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
    if (userAddRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User user = new User();
    BeanUtils.copyProperties(userAddRequest, user);
    boolean result = userService.save(user);
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    return ResultUtils.success(user.getId());
  }

  /**
   * 删除用户
   *
   * @param deleteRequest
   * @param request
   * @return
   */
  @PostMapping("/delete")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  @AuditLog(module = "用户管理", operationType = "DELETE", description = "删除用户")
  public BaseResponse<Boolean> deleteUser(
      @RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
    if (deleteRequest == null || deleteRequest.getId() <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    boolean b = userService.removeById(deleteRequest.getId());
    return ResultUtils.success(b);
  }

  /**
   * 更新用户
   *
   * @param userUpdateRequest
   * @param request
   * @return
   */
  @PostMapping("/update")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  @AuditLog(module = "用户管理", operationType = "UPDATE", description = "更新用户")
  public BaseResponse<Boolean> updateUser(
      @RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
    if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User user = new User();
    BeanUtils.copyProperties(userUpdateRequest, user);
    boolean result = userService.updateById(user);
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    return ResultUtils.success(true);
  }

  /**
   * 根据 id 获取用户（仅管理员）
   *
   * @param id
   * @param request
   * @return
   */
  @GetMapping("/get")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
    if (id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User user = userService.getById(id);
    ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
    return ResultUtils.success(user);
  }

  /**
   * 根据 id 获取包装类
   *
   * @param id
   * @param request
   * @return
   */
  @GetMapping("/get/vo")
  public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
    BaseResponse<User> response = getUserById(id, request);
    User user = response.getData();
    return ResultUtils.success(userService.getUserVO(user));
  }

  /**
   * 分页获取用户列表（仅管理员）
   *
   * @param userQueryRequest
   * @param request
   * @return
   */
  @PostMapping("/list/page")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Page<User>> listUserByPage(
      @RequestBody UserQueryRequest userQueryRequest, HttpServletRequest request) {
    long current = userQueryRequest.getCurrent();
    long size = userQueryRequest.getPageSize();
    Page<User> userPage =
        userService.page(new Page<>(current, size), userService.getQueryWrapper(userQueryRequest));
    return ResultUtils.success(userPage);
  }

  /**
   * 分页获取用户封装列表
   *
   * @param userQueryRequest
   * @param request
   * @return
   */
  @PostMapping("/list/page/vo")
  public BaseResponse<Page<UserVO>> listUserVOByPage(
      @RequestBody UserQueryRequest userQueryRequest, HttpServletRequest request) {
    if (userQueryRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    long current = userQueryRequest.getCurrent();
    long size = userQueryRequest.getPageSize();
    // 限制爬虫
    ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
    Page<User> userPage =
        userService.page(new Page<>(current, size), userService.getQueryWrapper(userQueryRequest));
    Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
    List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
    userVOPage.setRecords(userVO);
    return ResultUtils.success(userVOPage);
  }

  /**
   * 批量导入学生（Excel）
   *
   * @param file Excel 文件
   * @param request
   * @return
   */
  @PostMapping("/import/students")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  @AuditLog(module = "用户管理", operationType = "IMPORT", description = "批量导入学生")
  public BaseResponse<ImportResultVO> importStudents(
      @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
      HttpServletRequest request) {
    if (file == null || file.isEmpty()) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传文件不能为空");
    }

    // 验证文件类型
    String filename = file.getOriginalFilename();
    if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持 .xlsx 或 .xls 格式的 Excel 文件");
    }

    try {
      // 使用 EasyExcel 读取文件
      com.oj.cs.listener.StudentImportListener listener =
          new com.oj.cs.listener.StudentImportListener(userService);

      com.alibaba.excel.EasyExcel.read(
              file.getInputStream(), com.oj.cs.model.dto.user.StudentImportDTO.class, listener)
          .sheet()
          .doRead();

      com.oj.cs.listener.StudentImportListener.ImportResult result = listener.getResult();

      ImportResultVO resultVO = new ImportResultVO();
      resultVO.setTotalCount(result.getTotalCount());
      resultVO.setSuccessCount(result.getSuccessCount());
      resultVO.setFailCount(result.getFailCount());
      resultVO.setErrorMessages(result.getErrorMessages());

      return ResultUtils.success(resultVO);
    } catch (Exception e) {
      log.error("导入学生数据失败", e);
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "导入失败: " + e.getMessage());
    }
  }

  /**
   * 下载学生导入模板
   *
   * @param response
   */
  @GetMapping("/import/template")
  public void downloadImportTemplate(HttpServletResponse response) {
    try {
      response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
      response.setCharacterEncoding("utf-8");
      String fileName = java.net.URLEncoder.encode("学生导入模板", "UTF-8").replaceAll("\\+", "%20");
      response.setHeader(
          "Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

      // 使用 EasyExcel 生成模板
      com.alibaba.excel.EasyExcel.write(
              response.getOutputStream(), com.oj.cs.model.dto.user.StudentImportDTO.class)
          .sheet("学生信息")
          .doWrite(new java.util.ArrayList<>());

    } catch (Exception e) {
      log.error("下载导入模板失败", e);
    }
  }

  /** 学生导入结果 VO */
  @lombok.Data
  public static class ImportResultVO {
    /** 总数 */
    private Integer totalCount;

    /** 成功数 */
    private Integer successCount;

    /** 失败数 */
    private Integer failCount;

    /** 错误信息列表 */
    private java.util.List<String> errorMessages;
  }

  // endregion

  /**
   * 更新个人信息
   *
   * @param userUpdateMyRequest
   * @param request
   * @return
   */
  @PostMapping("/update/my")
  public BaseResponse<Boolean> updateMyUser(
      @RequestBody UserUpdateMyRequest userUpdateMyRequest, HttpServletRequest request) {
    if (userUpdateMyRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    User loginUser = userService.getLoginUser(request);
    User user = new User();
    BeanUtils.copyProperties(userUpdateMyRequest, user);
    user.setId(loginUser.getId());
    boolean result = userService.updateById(user);
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    return ResultUtils.success(true);
  }
}
