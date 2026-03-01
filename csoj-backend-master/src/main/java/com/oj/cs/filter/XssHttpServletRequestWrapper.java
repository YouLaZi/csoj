package com.oj.cs.filter;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/** XSS 请求包装器 对请求参数进行 XSS 过滤 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

  public XssHttpServletRequestWrapper(HttpServletRequest request) {
    super(request);
  }

  @Override
  public String getParameter(String name) {
    String value = super.getParameter(name);
    return cleanXss(value);
  }

  @Override
  public String[] getParameterValues(String name) {
    String[] values = super.getParameterValues(name);
    if (values == null) {
      return null;
    }

    String[] cleanValues = new String[values.length];
    for (int i = 0; i < values.length; i++) {
      cleanValues[i] = cleanXss(values[i]);
    }
    return cleanValues;
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    Map<String, String[]> originalMap = super.getParameterMap();
    Map<String, String[]> cleanMap = new HashMap<>();

    for (Map.Entry<String, String[]> entry : originalMap.entrySet()) {
      String[] values = entry.getValue();
      String[] cleanValues = new String[values.length];
      for (int i = 0; i < values.length; i++) {
        cleanValues[i] = cleanXss(values[i]);
      }
      cleanMap.put(entry.getKey(), cleanValues);
    }

    return cleanMap;
  }

  @Override
  public String getHeader(String name) {
    String value = super.getHeader(name);
    // 跳过不需要 XSS 过滤的请求头（如 Origin、Referer、Content-Type 等）
    if (name != null
        && (name.equalsIgnoreCase("Origin")
            || name.equalsIgnoreCase("Referer")
            || name.equalsIgnoreCase("Content-Type")
            || name.equalsIgnoreCase("Authorization")
            || name.equalsIgnoreCase("Accept")
            || name.equalsIgnoreCase("Accept-Language")
            || name.equalsIgnoreCase("Accept-Encoding")
            || name.equalsIgnoreCase("User-Agent")
            || name.equalsIgnoreCase("X-Requested-With")
            || name.equalsIgnoreCase("X-CSRF-Token"))) {
      return value;
    }
    return cleanXss(value);
  }

  /**
   * 清除 XSS 攻击代码
   *
   * @param value 待清理的字符串
   * @return 清理后的字符串
   */
  private String cleanXss(String value) {
    if (value == null || value.isEmpty()) {
      return value;
    }

    // HTML 标签过滤
    value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");

    // JavaScript 事件过滤
    value = value.replaceAll("(?i)javascript:", "");
    value = value.replaceAll("(?i)vbscript:", "");
    value = value.replaceAll("(?i)onload", "");
    value = value.replaceAll("(?i)onerror", "");
    value = value.replaceAll("(?i)onclick", "");
    value = value.replaceAll("(?i)onmouseover", "");
    value = value.replaceAll("(?i)onfocus", "");
    value = value.replaceAll("(?i)onblur", "");

    // 特殊字符过滤
    value = value.replaceAll("\"", "&quot;");
    value = value.replaceAll("'", "&#x27;");
    value = value.replaceAll("/", "&#x2F;");

    // SQL 注入基础过滤（防止通过 XSS 进行 SQL 注入）
    value =
        value.replaceAll(
            "(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute)", "");

    return value;
  }
}
