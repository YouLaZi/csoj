package com.oj.cs.controller;

import java.util.Arrays;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.manager.LocalFileManager;
import com.oj.cs.model.dto.file.UploadFileRequest;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.enums.FileUploadBizEnum;
import com.oj.cs.service.UserService;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;

/** 文件接口 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

  @Resource private UserService userService;

  @Resource private LocalFileManager localFileManager;

  /**
   * 文件上传
   *
   * @param multipartFile
   * @param uploadFileRequest
   * @param request
   * @return
   */
  @PostMapping("/upload")
  public BaseResponse<String> uploadFile(
      @RequestPart("file") MultipartFile multipartFile,
      UploadFileRequest uploadFileRequest,
      HttpServletRequest request) {
    String biz = uploadFileRequest.getBiz();
    FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
    if (fileUploadBizEnum == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    validFile(multipartFile, fileUploadBizEnum);
    User loginUser = userService.getLoginUser(request);
    // 文件目录：根据业务、用户来划分
    String uuid = RandomStringUtils.randomAlphanumeric(8);
    // 防止路径穿越攻击：移除文件名中的路径分隔符和特殊字符
    String originalFilename = multipartFile.getOriginalFilename();
    if (originalFilename == null || originalFilename.isBlank()) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
    }
    String safeFilename = originalFilename.replaceAll("[\\\\/]", "_");
    String filename = uuid + "-" + safeFilename;
    String filepath =
        String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), filename);
    try {
      // 上传文件到本地存储
      String fileUrl = localFileManager.putObject(filepath, multipartFile);
      // 返回可访问地址
      return ResultUtils.success(fileUrl);
    } catch (Exception e) {
      log.error("file upload error, filepath = " + filepath, e);
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
    }
  }

  /**
   * 校验文件
   *
   * @param multipartFile
   * @param fileUploadBizEnum 业务类型
   */
  private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
    // 文件大小
    long fileSize = multipartFile.getSize();
    // 文件后缀
    String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
    final long ONE_M = 1024 * 1024L;
    if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
      if (fileSize > ONE_M) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
      }
      if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
      }
    }
  }
}
