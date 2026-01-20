package com.oj.cs.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.slf4j.Slf4j;

/** 文件访问控制器 用于提供本地文件的HTTP访问 */
@Slf4j
@Controller
public class FileServeController {

  @Value("${file.upload.base-path:./uploads}")
  private String baseUploadPath;

  /**
   * 获取文件 URL格式: /api/files/** (其中 ** 是文件相对路径)
   *
   * @param request 请求对象
   * @return 文件内容
   */
  @GetMapping("/api/files/**")
  public ResponseEntity<Resource> getFile(HttpServletRequest request) {
    // 获取相对路径
    String filepath = extractPath(request);

    if (filepath == null || filepath.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    try {
      Path filePath = Paths.get(baseUploadPath, filepath).normalize();

      // 安全检查：防止路径遍历攻击
      if (!filePath.startsWith(Paths.get(baseUploadPath).normalize())) {
        log.warn("非法文件访问请求: {}", filepath);
        return ResponseEntity.badRequest().build();
      }

      File file = filePath.toFile();
      if (!file.exists()) {
        log.warn("文件不存在: {}", filePath);
        return ResponseEntity.notFound().build();
      }

      // 获取文件MIME类型
      String contentType = Files.probeContentType(filePath);
      if (contentType == null) {
        contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
      }

      Resource resource = new FileSystemResource(file);

      return ResponseEntity.ok()
          .contentType(MediaType.parseMediaType(contentType))
          .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
          .body(resource);

    } catch (IOException e) {
      log.error("读取文件失败: {}", filepath, e);
      return ResponseEntity.status(500).build();
    }
  }

  /** 从请求中提取文件路径 */
  private String extractPath(HttpServletRequest request) {
    String requestUri = request.getRequestURI();
    String contextPath = request.getContextPath();
    String path = requestUri.substring(contextPath.length());

    // 移除前缀 "/api/files/"
    if (path.startsWith("/api/files/")) {
      return path.substring("/api/files/".length());
    } else if (path.equals("/api/files")) {
      return "";
    }
    return null;
  }
}
