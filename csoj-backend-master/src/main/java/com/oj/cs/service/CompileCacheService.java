package com.oj.cs.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import cn.hutool.crypto.digest.DigestUtil;
import lombok.extern.slf4j.Slf4j;

/** 编译缓存服务 使用本地文件系统存储编译结果 */
@Service
@Slf4j
public class CompileCacheService {

  private static final String CACHE_DIR =
      System.getProperty("java.io.tmpdir") + "/oj-compile-cache";
  private static final int MAX_ENTRY_SIZE = 10 * 1024 * 1024; // 10MB
  private static final int DEFAULT_EXPIRE_HOURS = 24;

  private final Map<String, CacheEntry> cacheIndex = new ConcurrentHashMap<>();

  public CompileCacheService() {
    File cacheDirFile = new File(CACHE_DIR);
    if (!cacheDirFile.exists()) {
      cacheDirFile.mkdirs();
      log.info("创建编译缓存目录: {}", CACHE_DIR);
    }

    // 创建语言子目录
    String[] languages = {"java", "python", "cpp", "c", "go", "javascript", "csharp"};
    for (String lang : languages) {
      File langDir = new File(CACHE_DIR, lang);
      if (!langDir.exists()) {
        langDir.mkdirs();
      }
    }
    // 定时清理过期缓存（每小时执行一次）
    Timer timer = new Timer("CompileCacheCleanup", true);
    timer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            cleanupExpiredCache();
          }
        },
        3600 * 1000,
        3600 * 1000);
  }

  /** 计算代码哈希 */
  public String computeHash(String code, String language) {
    String normalizedCode = normalizeCode(code);
    String input = language + ":" + normalizedCode;
    return DigestUtil.sha256Hex(input);
  }

  /** 标准化代码（移除空白差异） */
  private String normalizeCode(String code) {
    if (code == null) return "";
    return code.trim().replaceAll("\\s+", " ");
  }

  /** 获取编译结果 */
  public Optional<CompileResult> getCompileResult(String code, String language) {
    String cacheKey = computeHash(code, language);
    CacheEntry entry = cacheIndex.get(cacheKey);

    if (entry == null || entry.isExpired()) {
      cacheIndex.remove(cacheKey);
      return Optional.empty();
    }

    try {
      Path filePath = Paths.get(entry.getFilePath());
      if (!Files.exists(filePath)) {
        cacheIndex.remove(cacheKey);
        return Optional.empty();
      }

      byte[] bytecode = Files.readAllBytes(filePath);

      CompileResult result = new CompileResult();
      result.setCacheKey(cacheKey);
      result.setBytecode(bytecode);
      result.setCreateTime(entry.getCreateTime());
      result.setSize(bytecode.length);
      result.setLanguage(language);

      log.debug("缓存命中: {}, size: {} bytes", cacheKey, bytecode.length);
      return Optional.of(result);
    } catch (IOException e) {
      log.error("读取缓存文件失败: {}", e);
      return Optional.empty();
    }
  }

  /** 存储编译结果 */
  public void putCompileResult(String code, String language, byte[] bytecode) {
    if (bytecode == null || bytecode.length == 0) {
      return;
    }

    // 检查大小限制
    if (bytecode.length > MAX_ENTRY_SIZE) {
      log.warn("编译结果过大，不缓存: {} bytes", bytecode.length);
      return;
    }

    String cacheKey = computeHash(code, language);
    String fileName = cacheKey + ".bin";
    String langDir = CACHE_DIR + "/" + language;
    String filePath = langDir + "/" + fileName;

    try {
      // 确保目录存在
      File dir = new File(langDir);
      if (!dir.exists()) {
        dir.mkdirs();
      }

      // 写入文件
      Files.write(Paths.get(filePath), bytecode);

      // 更新索引
      CacheEntry entry = new CacheEntry();
      entry.setCacheKey(cacheKey);
      entry.setFilePath(filePath);
      entry.setCreateTime(System.currentTimeMillis());
      entry.setExpireTime(System.currentTimeMillis() + DEFAULT_EXPIRE_HOURS * 3600 * 1000L);
      entry.setSize(bytecode.length);
      entry.setLanguage(language);

      cacheIndex.put(cacheKey, entry);

      log.info("编译结果已缓存: {}, size: {} bytes", cacheKey, bytecode.length);
    } catch (IOException e) {
      log.error("写入缓存文件失败: {}", e);
    }
  }

  /** 清理过期缓存 */
  public void cleanupExpiredCache() {
    List<String> expiredKeys = new ArrayList<>();

    cacheIndex.forEach(
        (key, entry) -> {
          if (entry.isExpired()) {
            expiredKeys.add(key);
            try {
              Files.deleteIfExists(Paths.get(entry.getFilePath()));
            } catch (IOException e) {
              log.warn("删除过期缓存文件失败: {}", e);
            }
          }
        });

    for (String key : expiredKeys) {
      cacheIndex.remove(key);
    }

    if (!expiredKeys.isEmpty()) {
      log.info("清理了 {} 个过期缓存", expiredKeys.size());
    }
  }

  /** 获取缓存统计 */
  public CacheStats getStats() {
    CacheStats stats = new CacheStats();
    stats.setTotalEntries(cacheIndex.size());

    long totalSize = 0;
    for (CacheEntry entry : cacheIndex.values()) {
      totalSize += entry.getSize();
    }
    stats.setTotalSize(totalSize);

    // 按语言统计
    Map<String, Integer> byLanguage = new HashMap<>();
    for (CacheEntry entry : cacheIndex.values()) {
      String lang = entry.getLanguage();
      byLanguage.put(lang, byLanguage.getOrDefault(lang, 0) + 1);
    }
    stats.setByLanguage(byLanguage);

    return stats;
  }

  /** 清空所有缓存 */
  public void clearAll() {
    // 清理文件
    cacheIndex.forEach(
        (key, entry) -> {
          try {
            Files.deleteIfExists(Paths.get(entry.getFilePath()));
          } catch (IOException e) {
            log.warn("删除缓存文件失败: {}", e);
          }
        });

    cacheIndex.clear();
    log.info("所有编译缓存已清空");
  }

  // ===================== 内部类 =====================

  /** 缓存条目 */
  @lombok.Data
  private static class CacheEntry {
    private String cacheKey;
    private String filePath;
    private long createTime;
    private long expireTime;
    private long size;
    private String language;

    public boolean isExpired() {
      return System.currentTimeMillis() > expireTime;
    }
  }

  /** 编译结果 */
  @lombok.Data
  public static class CompileResult {
    private String cacheKey;
    private byte[] bytecode;
    private long createTime;
    private long size;
    private String language;
  }

  /** 缓存统计 */
  @lombok.Data
  public static class CacheStats {
    private int totalEntries;
    private long totalSize;
    private Map<String, Integer> byLanguage;
  }
}
