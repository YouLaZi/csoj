package com.oj.cs.utils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.oj.cs.common.ErrorCode;
import com.oj.cs.exception.BusinessException;

/** 代码安全工具类 用于检测和过滤恶意代码 */
public class CodeSecurityUtils {

  /** 危险关键词列表 */
  private static final List<String> DANGEROUS_KEYWORDS =
      Arrays.asList(
          // 文件操作
          "File",
          "FileWriter",
          "FileReader",
          "FileInputStream",
          "FileOutputStream",
          "Files",
          "Paths",
          "Path",
          "RandomAccessFile",
          // 网络操作
          "Socket",
          "ServerSocket",
          "URL",
          "URLConnection",
          "HttpURLConnection",
          "InetAddress",
          "DatagramSocket",
          "MulticastSocket",
          // 系统操作
          "Runtime",
          "Process",
          "ProcessBuilder",
          "System.exit",
          "System.gc",
          "System.runFinalization",
          // 反射操作
          "Class.forName",
          "Method.invoke",
          "Constructor.newInstance",
          "Field.set",
          "Field.get",
          // 线程操作
          "Thread",
          "ThreadGroup",
          "Runnable",
          "Callable",
          "ExecutorService",
          "ThreadPoolExecutor",
          "ScheduledExecutorService",
          // 安全管理
          "SecurityManager",
          "AccessController",
          "PrivilegedAction",
          // 类加载
          "ClassLoader",
          "URLClassLoader",
          "defineClass",
          // JNI
          "native",
          "System.loadLibrary",
          "System.load");

  /** 危险包名列表 */
  private static final List<String> DANGEROUS_PACKAGES =
      Arrays.asList(
          "java.io",
          "java.net",
          "java.nio",
          "java.lang.reflect",
          "java.lang.management",
          "java.security",
          "javax.script",
          "sun.",
          "com.sun.",
          "jdk.internal");

  /** 危险正则表达式模式 */
  private static final List<Pattern> DANGEROUS_PATTERNS =
      Arrays.asList(
          // 执行系统命令
          Pattern.compile("Runtime\\.getRuntime\\(\\)\\.exec", Pattern.CASE_INSENSITIVE),
          Pattern.compile("ProcessBuilder\\.start\\(\\)", Pattern.CASE_INSENSITIVE),
          // 文件操作
          Pattern.compile("new\\s+File\\s*\\(", Pattern.CASE_INSENSITIVE),
          Pattern.compile("Files\\.(delete|write|copy|move)", Pattern.CASE_INSENSITIVE),
          // 网络操作
          Pattern.compile("new\\s+Socket\\s*\\(", Pattern.CASE_INSENSITIVE),
          Pattern.compile("new\\s+URL\\s*\\(", Pattern.CASE_INSENSITIVE),
          // 反射操作
          Pattern.compile("Class\\.forName\\s*\\(", Pattern.CASE_INSENSITIVE),
          Pattern.compile("\\.getDeclaredMethod\\s*\\(", Pattern.CASE_INSENSITIVE),
          // 系统退出
          Pattern.compile("System\\.exit\\s*\\(", Pattern.CASE_INSENSITIVE));

  /**
   * 检查代码是否包含危险操作
   *
   * @param code 用户提交的代码
   * @throws BusinessException 如果代码包含危险操作
   */
  public static void checkCodeSecurity(String code) {
    if (StringUtils.isBlank(code)) {
      return;
    }

    // 移除注释和字符串字面量，避免误判
    String cleanCode = removeCommentsAndStrings(code);

    // 检查危险关键词
    for (String keyword : DANGEROUS_KEYWORDS) {
      if (cleanCode.contains(keyword)) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码包含危险操作: " + keyword + "，请检查您的代码");
      }
    }

    // 检查危险包导入
    for (String packageName : DANGEROUS_PACKAGES) {
      if (cleanCode.contains("import " + packageName)) {
        throw new BusinessException(
            ErrorCode.PARAMS_ERROR, "代码包含危险包导入: " + packageName + "，请检查您的代码");
      }
    }

    // 检查危险模式
    for (Pattern pattern : DANGEROUS_PATTERNS) {
      if (pattern.matcher(cleanCode).find()) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码包含危险操作模式，请检查您的代码");
      }
    }
  }

  /**
   * 移除代码中的注释和字符串字面量 简化版本，用于基本的安全检查
   *
   * @param code 原始代码
   * @return 清理后的代码
   */
  private static String removeCommentsAndStrings(String code) {
    // 移除单行注释
    code = code.replaceAll("//.*?\\n", "\\n");
    // 移除多行注释
    code = code.replaceAll("/\\*.*?\\*/", "");
    // 移除字符串字面量
    code = code.replaceAll("\".*?\"", "\"\"");
    code = code.replaceAll("'.*?'", "''");
    return code;
  }

  /**
   * 检查编程语言是否被支持
   *
   * @param language 编程语言
   * @return 是否支持
   */
  public static boolean isSupportedLanguage(String language) {
    if (StringUtils.isBlank(language)) {
      return false;
    }

    List<String> supportedLanguages =
        Arrays.asList("java", "python", "cpp", "c", "javascript", "go", "csharp");

    return supportedLanguages.contains(language.toLowerCase());
  }

  /**
   * 验证代码长度
   *
   * @param code 代码内容
   * @param maxLength 最大长度
   */
  public static void validateCodeLength(String code, int maxLength) {
    if (StringUtils.isNotBlank(code) && code.length() > maxLength) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码长度超过限制，最大允许 " + maxLength + " 个字符");
    }
  }
}
