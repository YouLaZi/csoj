package com.oj.cs.controller;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oj.cs.annotation.AuthCheck;
import com.oj.cs.aop.PerformanceMonitorInterceptor;
import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.constant.UserConstant;

import lombok.extern.slf4j.Slf4j;

/** 系统监控控制器 提供系统状态和性能监控功能 */
@RestController
@RequestMapping("/system/monitor")
@Slf4j
public class SystemMonitorController {

  /** 获取系统状态信息 仅管理员可访问 */
  @GetMapping("/status")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Map<String, Object>> getSystemStatus() {
    Map<String, Object> systemInfo = new HashMap<>();

    try {
      // 获取运行时信息
      RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
      systemInfo.put("uptime", runtimeBean.getUptime());
      systemInfo.put("startTime", runtimeBean.getStartTime());
      systemInfo.put("jvmName", runtimeBean.getVmName());
      systemInfo.put("jvmVersion", runtimeBean.getVmVersion());

      // 获取内存信息
      MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
      long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
      long heapMax = memoryBean.getHeapMemoryUsage().getMax();
      long nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed();

      Map<String, Object> memoryInfo = new HashMap<>();
      memoryInfo.put("heapUsed", formatBytes(heapUsed));
      memoryInfo.put("heapMax", formatBytes(heapMax));
      memoryInfo.put("heapUsedMB", heapUsed / 1024 / 1024);
      memoryInfo.put("heapMaxMB", heapMax / 1024 / 1024);
      memoryInfo.put(
          "heapUsagePercent", String.format("%.2f%%", (double) heapUsed / heapMax * 100));
      memoryInfo.put("nonHeapUsed", formatBytes(nonHeapUsed));
      memoryInfo.put("nonHeapUsedMB", nonHeapUsed / 1024 / 1024);
      systemInfo.put("memory", memoryInfo);

      // 获取操作系统信息
      OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
      Map<String, Object> osInfo = new HashMap<>();
      osInfo.put("name", osBean.getName());
      osInfo.put("version", osBean.getVersion());
      osInfo.put("arch", osBean.getArch());
      osInfo.put("availableProcessors", osBean.getAvailableProcessors());
      osInfo.put("systemLoadAverage", osBean.getSystemLoadAverage());
      systemInfo.put("operatingSystem", osInfo);

      // 获取线程信息
      Map<String, Object> threadInfo = new HashMap<>();
      threadInfo.put("activeCount", Thread.activeCount());
      threadInfo.put("currentThread", Thread.currentThread().getName());
      systemInfo.put("thread", threadInfo);

      // 添加时间戳
      systemInfo.put("timestamp", System.currentTimeMillis());

    } catch (Exception e) {
      log.error("获取系统状态信息失败", e);
      return ResultUtils.error(500, "获取系统状态信息失败: " + e.getMessage());
    }

    return ResultUtils.success(systemInfo);
  }

  /** 获取性能统计信息 仅管理员可访问 */
  @GetMapping("/performance")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<String> getPerformanceStatistics() {
    try {
      String statistics = PerformanceMonitorInterceptor.getStatistics();
      return ResultUtils.success(statistics);
    } catch (Exception e) {
      log.error("获取性能统计信息失败", e);
      return ResultUtils.error(500, "获取性能统计信息失败: " + e.getMessage());
    }
  }

  /** 清空性能统计信息 仅管理员可访问 */
  @PostMapping("/performance/clear")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<String> clearPerformanceStatistics() {
    try {
      PerformanceMonitorInterceptor.clearStatistics();
      log.info("性能统计信息已清空");
      return ResultUtils.success("性能统计信息已清空");
    } catch (Exception e) {
      log.error("清空性能统计信息失败", e);
      return ResultUtils.error(500, "清空性能统计信息失败: " + e.getMessage());
    }
  }

  /** 触发垃圾回收 仅管理员可访问 */
  @PostMapping("/gc")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Map<String, Object>> triggerGarbageCollection() {
    try {
      // 获取GC前的内存信息
      MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
      long beforeHeapUsed = memoryBean.getHeapMemoryUsage().getUsed();

      // 触发垃圾回收
      long startTime = System.currentTimeMillis();
      System.gc();
      long endTime = System.currentTimeMillis();

      // 等待一小段时间让GC完成
      Thread.sleep(100);

      // 获取GC后的内存信息
      long afterHeapUsed = memoryBean.getHeapMemoryUsage().getUsed();
      long freedMemory = beforeHeapUsed - afterHeapUsed;

      Map<String, Object> gcInfo = new HashMap<>();
      gcInfo.put("beforeHeapUsedMB", beforeHeapUsed / 1024 / 1024);
      gcInfo.put("afterHeapUsedMB", afterHeapUsed / 1024 / 1024);
      gcInfo.put("freedMemoryMB", freedMemory / 1024 / 1024);
      gcInfo.put("gcDurationMs", endTime - startTime);
      gcInfo.put("timestamp", System.currentTimeMillis());

      log.info("手动触发垃圾回收完成，释放内存: {}MB", freedMemory / 1024 / 1024);
      return ResultUtils.success(gcInfo);

    } catch (Exception e) {
      log.error("触发垃圾回收失败", e);
      return ResultUtils.error(500, "触发垃圾回收失败: " + e.getMessage());
    }
  }

  /** 获取应用健康检查信息 */
  @GetMapping("/health")
  public BaseResponse<Map<String, Object>> getHealthCheck() {
    Map<String, Object> healthInfo = new HashMap<>();

    try {
      // 基本健康状态
      healthInfo.put("status", "UP");
      healthInfo.put("timestamp", System.currentTimeMillis());

      // 内存健康检查
      MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
      long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
      long heapMax = memoryBean.getHeapMemoryUsage().getMax();
      double heapUsagePercent = (double) heapUsed / heapMax * 100;

      Map<String, Object> memoryHealth = new HashMap<>();
      memoryHealth.put("status", heapUsagePercent < 90 ? "HEALTHY" : "WARNING");
      memoryHealth.put("usagePercent", String.format("%.2f%%", heapUsagePercent));
      healthInfo.put("memory", memoryHealth);

      // 系统负载检查
      OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
      double systemLoad = osBean.getSystemLoadAverage();
      Map<String, Object> loadHealth = new HashMap<>();
      if (systemLoad >= 0) {
        loadHealth.put("status", systemLoad < 0.8 ? "HEALTHY" : "WARNING");
        loadHealth.put("loadAverage", systemLoad);
      } else {
        loadHealth.put("status", "UNKNOWN");
        loadHealth.put("loadAverage", "N/A");
      }
      healthInfo.put("systemLoad", loadHealth);

    } catch (Exception e) {
      log.error("健康检查失败", e);
      healthInfo.put("status", "DOWN");
      healthInfo.put("error", e.getMessage());
    }

    return ResultUtils.success(healthInfo);
  }

  /** 格式化字节数 */
  private String formatBytes(long bytes) {
    if (bytes < 1024) {
      return bytes + " B";
    } else if (bytes < 1024 * 1024) {
      return String.format("%.2f KB", (double) bytes / 1024);
    } else if (bytes < 1024 * 1024 * 1024) {
      return String.format("%.2f MB", (double) bytes / 1024 / 1024);
    } else {
      return String.format("%.2f GB", (double) bytes / 1024 / 1024 / 1024);
    }
  }
}
