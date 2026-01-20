package com.oj.cs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oj.cs.model.dto.license.LicenseRequest;
import com.oj.cs.model.entity.License;

/** 许可证服务接口 */
public interface LicenseService extends IService<License> {

  /**
   * 生成许可证
   *
   * @param request 许可证请求
   * @return 许可证密钥
   */
  String generateLicense(LicenseRequest request);

  /**
   * 激活许可证
   *
   * @param licenseKey 许可证密钥
   * @return 是否激活成功
   */
  Boolean activateLicense(String licenseKey);

  /**
   * 验证许可证状态
   *
   * @return 是否有效
   */
  Boolean validateLicense();

  /** 获取当前激活的许可证信息 */
  License getCurrentLicense();

  /** 检查并更新过期许可证（定时任务调用） */
  void checkExpiredLicenses();

  /** 更新许可证使用统计 */
  Boolean updateUsageStatistics();

  /**
   * 续期许可证
   *
   * @param licenseId 许可证ID
   * @param extendDays 续期天数
   * @return 是否成功
   */
  Boolean extendLicense(Long licenseId, Integer extendDays);

  /**
   * 升级许可证
   *
   * @param licenseId 许可证ID
   * @param newLicenseType 新许可证类型
   * @return 是否成功
   */
  Boolean upgradeLicense(Long licenseId, String newLicenseType);

  /** 获取许可证使用统计 */
  LicenseUsageStats getUsageStats();

  /** 许可证使用统计 */
  class LicenseUsageStats {
    private Integer totalUsers;
    private Integer activeUsers;
    private Integer totalQuestions;
    private Integer publishedQuestions;
    private Integer totalContests;
    private Boolean canCreateContest;
    private Boolean canUsePlagiarism;
    private Integer daysUntilExpiry;

    // Getters and Setters
    public Integer getTotalUsers() {
      return totalUsers;
    }

    public void setTotalUsers(Integer totalUsers) {
      this.totalUsers = totalUsers;
    }

    public Integer getActiveUsers() {
      return activeUsers;
    }

    public void setActiveUsers(Integer activeUsers) {
      this.activeUsers = activeUsers;
    }

    public Integer getTotalQuestions() {
      return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
      this.totalQuestions = totalQuestions;
    }

    public Integer getPublishedQuestions() {
      return publishedQuestions;
    }

    public void setPublishedQuestions(Integer publishedQuestions) {
      this.publishedQuestions = publishedQuestions;
    }

    public Integer getTotalContests() {
      return totalContests;
    }

    public void setTotalContests(Integer totalContests) {
      this.totalContests = totalContests;
    }

    public Boolean getCanCreateContest() {
      return canCreateContest;
    }

    public void setCanCreateContest(Boolean canCreateContest) {
      this.canCreateContest = canCreateContest;
    }

    public Boolean getCanUsePlagiarism() {
      return canUsePlagiarism;
    }

    public void setCanUsePlagiarism(Boolean canUsePlagiarism) {
      this.canUsePlagiarism = canUsePlagiarism;
    }

    public Integer getDaysUntilExpiry() {
      return daysUntilExpiry;
    }

    public void setDaysUntilExpiry(Integer daysUntilExpiry) {
      this.daysUntilExpiry = daysUntilExpiry;
    }
  }
}
