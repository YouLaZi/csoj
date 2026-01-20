package com.oj.cs.controller;

import jakarta.annotation.Resource;

import org.springframework.web.bind.annotation.*;

import com.oj.cs.annotation.AuthCheck;
import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.constant.UserConstant;
import com.oj.cs.model.dto.license.LicenseRequest;
import com.oj.cs.model.entity.License;
import com.oj.cs.service.LicenseService;

import lombok.extern.slf4j.Slf4j;

/** 许可证管理接口（仅管理员） */
@RestController
@RequestMapping("/license")
@Slf4j
public class LicenseController {

  @Resource private LicenseService licenseService;

  /** 生成许可证（管理员） */
  @PostMapping("/generate")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<String> generateLicense(@RequestBody LicenseRequest request) {
    String licenseKey = licenseService.generateLicense(request);
    return ResultUtils.success(licenseKey);
  }

  /** 激活许可证 */
  @PostMapping("/activate")
  public BaseResponse<Boolean> activateLicense(@RequestParam String licenseKey) {
    Boolean result = licenseService.activateLicense(licenseKey);
    return ResultUtils.success(result);
  }

  /** 验证许可证状态 */
  @GetMapping("/validate")
  public BaseResponse<Boolean> validateLicense() {
    Boolean result = licenseService.validateLicense();
    return ResultUtils.success(result);
  }

  /** 获取当前许可证信息 */
  @GetMapping("/current")
  public BaseResponse<License> getCurrentLicense() {
    License license = licenseService.getCurrentLicense();
    return ResultUtils.success(license);
  }

  /** 获取许可证使用统计 */
  @GetMapping("/stats")
  public BaseResponse<LicenseService.LicenseUsageStats> getUsageStats() {
    LicenseService.LicenseUsageStats stats = licenseService.getUsageStats();
    return ResultUtils.success(stats);
  }

  /** 续期许可证（管理员） */
  @PostMapping("/extend")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Boolean> extendLicense(
      @RequestParam Long licenseId, @RequestParam Integer extendDays) {
    Boolean result = licenseService.extendLicense(licenseId, extendDays);
    return ResultUtils.success(result);
  }

  /** 升级许可证（管理员） */
  @PostMapping("/upgrade")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Boolean> upgradeLicense(
      @RequestParam Long licenseId, @RequestParam String newLicenseType) {
    Boolean result = licenseService.upgradeLicense(licenseId, newLicenseType);
    return ResultUtils.success(result);
  }

  /** 更新使用统计（管理员） */
  @PostMapping("/stats/update")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Boolean> updateUsageStats() {
    Boolean result = licenseService.updateUsageStatistics();
    return ResultUtils.success(result);
  }
}
