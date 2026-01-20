package com.oj.cs.manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

/** 本地文件存储管理器 适用于校内机房部署，无需外部云存储服务 */
@Slf4j
@Component
public class LocalFileManager {

  /** 文件存储根目录 */
  @Value("${file.upload.base-path:./uploads}")
  private String baseUploadPath;

  /** 文件访问URL前缀 */
  @Value("${file.upload.url-prefix:/api/files}")
  private String urlPrefix;

  /**
   * 上传文件
   *
   * @param filepath 文件相对路径 (如: /avatar/user123/abc-xxx.jpg)
   * @param file 文件对象
   * @return 文件访问URL
   */
  public String putObject(String filepath, File file) {
    try {
      Path targetPath = Paths.get(baseUploadPath, filepath);
      // 创建父目录
      Files.createDirectories(targetPath.getParent());
      // 复制文件
      Files.copy(file.toPath(), targetPath);
      log.info("文件上传成功: {}", targetPath);
      return urlPrefix + filepath;
    } catch (IOException e) {
      log.error("文件上传失败: {}", filepath, e);
      throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
    }
  }

  /**
   * 上传文件
   *
   * @param filepath 文件相对路径
   * @param multipartFile MultipartFile对象
   * @return 文件访问URL
   */
  public String putObject(String filepath, MultipartFile multipartFile) {
    try {
      Path targetPath = Paths.get(baseUploadPath, filepath);
      // 创建父目录
      Files.createDirectories(targetPath.getParent());
      // 保存文件
      multipartFile.transferTo(targetPath.toFile());
      log.info("文件上传成功: {}", targetPath);
      return urlPrefix + filepath;
    } catch (IOException e) {
      log.error("文件上传失败: {}", filepath, e);
      throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
    }
  }

  /**
   * 删除文件
   *
   * @param filepath 文件相对路径
   */
  public void deleteObject(String filepath) {
    try {
      Path targetPath = Paths.get(baseUploadPath, filepath);
      Files.deleteIfExists(targetPath);
      log.info("文件删除成功: {}", targetPath);
    } catch (IOException e) {
      log.error("文件删除失败: {}", filepath, e);
    }
  }

  /**
   * 检查文件是否存在
   *
   * @param filepath 文件相对路径
   * @return 是否存在
   */
  public boolean exists(String filepath) {
    Path targetPath = Paths.get(baseUploadPath, filepath);
    return Files.exists(targetPath);
  }

  /**
   * 获取文件绝对路径
   *
   * @param filepath 文件相对路径
   * @return 绝对路径
   */
  public Path getAbsolutePath(String filepath) {
    return Paths.get(baseUploadPath, filepath).toAbsolutePath();
  }

  /**
   * 获取文件访问URL
   *
   * @param filepath 文件相对路径
   * @return 访问URL
   */
  public String getFileUrl(String filepath) {
    return urlPrefix + filepath;
  }
}
