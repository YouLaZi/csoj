package com.oj.cs.constant;

/** 文件常量 */
public interface FileConstant {

  /**
   * COS 访问地址（已弃用 - 改用本地存储）
   *
   * @deprecated 使用本地文件存储替代
   */
  @Deprecated String COS_HOST = "https://cs.oj.com";

  /** 本地文件访问地址前缀 */
  String LOCAL_FILE_HOST = "/api/files";
}
