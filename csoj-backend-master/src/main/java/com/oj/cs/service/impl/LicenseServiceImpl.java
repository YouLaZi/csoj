package com.oj.cs.service.impl;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;

import jakarta.annotation.Resource;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.mapper.LicenseMapper;
import com.oj.cs.mapper.UserMapper;
import com.oj.cs.model.dto.license.LicenseRequest;
import com.oj.cs.model.entity.License;
import com.oj.cs.model.entity.Question;
import com.oj.cs.model.entity.User;
import com.oj.cs.service.LicenseService;
import com.oj.cs.service.QuestionService;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/** 许可证服务实现 */
@Service
@Slf4j
public class LicenseServiceImpl extends ServiceImpl<LicenseMapper, License>
    implements LicenseService {

  @Resource private UserMapper userMapper;

  @Resource private QuestionService questionService;

  private static final String SECRET_SALT = "CSOJ_LICENSE_SECRET_2026";
  private static final SecureRandom random = new SecureRandom();

  @Override
  @Transactional(rollbackFor = Exception.class)
  public String generateLicense(LicenseRequest request) {
    if (request == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 生成许可证密钥
    String licenseKey = generateLicenseKey(request);

    // 创建许可证记录
    License license = new License();
    license.setLicenseKey(licenseKey);
    license.setLicenseType(request.getLicenseType());
    license.setSchoolName(request.getSchoolName());
    license.setContactPerson(request.getContactPerson());
    license.setContactEmail(request.getContactEmail());
    license.setContactPhone(request.getContactPhone());
    license.setMaxUsers(
        request.getMaxUsers() != null
            ? request.getMaxUsers()
            : getDefaultMaxUsers(request.getLicenseType()));
    license.setCurrentUsers(0);
    license.setMaxQuestions(
        request.getMaxQuestions() != null
            ? request.getMaxQuestions()
            : getDefaultMaxQuestions(request.getLicenseType()));
    license.setStartDate(request.getStartDate() != null ? request.getStartDate() : new Date());
    license.setEndDate(
        request.getEndDate() != null ? request.getEndDate() : DateUtil.offsetDay(new Date(), 30));
    license.setIsActive(0);
    license.setCreateTime(new Date());
    license.setUpdateTime(new Date());

    // 设置功能权限
    Map<String, Boolean> features = new HashMap<>();
    features.put(
        "contest", request.getFeatures() != null && request.getFeatures().contains("contest"));
    features.put(
        "plagiarism",
        request.getFeatures() != null && request.getFeatures().contains("plagiarism"));
    features.put(
        "statistics",
        request.getFeatures() != null && request.getFeatures().contains("statistics"));
    features.put(
        "export", request.getFeatures() != null && request.getFeatures().contains("export"));
    license.setFeatures(JSONUtil.toJsonStr(features));
    license.setEnableContest(features.get("contest") ? 1 : 0);
    license.setEnablePlagiarism(features.get("plagiarism") ? 1 : 0);

    this.save(license);
    log.info("生成许可证: {}, 类型: {}", licenseKey, request.getLicenseType());

    return licenseKey;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean activateLicense(String licenseKey) {
    if (StrUtil.isBlank(licenseKey)) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "许可证密钥不能为空");
    }

    License license = this.baseMapper.getByLicenseKey(licenseKey);
    if (license == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "许可证不存在");
    }

    // 检查许可证是否已激活
    if (license.getIsActive() == 1) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "许可证已被激活");
    }

    // 检查许可证是否已过期
    if (license.getEndDate().before(new Date())) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "许可证已过期");
    }

    // 激活许可证
    license.setIsActive(1);
    license.setActivationTime(new Date());
    license.setLastCheckTime(new Date());
    license.setUpdateTime(new Date());
    Boolean result = this.updateById(license);

    log.info("激活许可证: {}, 学校: {}", licenseKey, license.getSchoolName());
    return result;
  }

  @Override
  public Boolean validateLicense() {
    License license = getCurrentLicense();
    if (license == null) {
      return false;
    }

    // 检查是否激活
    if (license.getIsActive() != 1) {
      return false;
    }

    // 检查是否过期
    if (license.getEndDate().before(new Date())) {
      // 标记为已过期
      license.setIsActive(2);
      license.setUpdateTime(new Date());
      this.updateById(license);
      return false;
    }

    return true;
  }

  @Override
  public License getCurrentLicense() {
    // 获取已激活的许可证
    QueryWrapper<License> wrapper = new QueryWrapper<>();
    wrapper.eq("isActive", 1);
    wrapper.orderByDesc("createTime");
    wrapper.last("LIMIT 1");
    return this.getOne(wrapper);
  }

  @Override
  @Scheduled(cron = "0 0 */6 * * ?") // 每6小时检查一次
  public void checkExpiredLicenses() {
    log.info("开始检查过期许可证...");
    List<License> licenses = this.baseMapper.getActiveLicenses();
    Date now = new Date();
    int expiredCount = 0;

    for (License license : licenses) {
      if (license.getEndDate().before(now)) {
        license.setIsActive(2);
        license.setUpdateTime(now);
        this.updateById(license);
        expiredCount++;
        log.warn("许可证已过期: {}, 学校: {}", license.getLicenseKey(), license.getSchoolName());
      }

      // 更新最后检查时间
      license.setLastCheckTime(now);
      this.updateById(license);
    }

    log.info("许可证检查完成，过期数量: {}", expiredCount);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean updateUsageStatistics() {
    License license = getCurrentLicense();
    if (license == null) {
      return false;
    }

    // 更新用户数
    QueryWrapper<User> userWrapper = new QueryWrapper<>();
    userWrapper.eq("userRole", "user");
    Long userCount = userMapper.selectCount(userWrapper);
    license.setCurrentUsers(userCount.intValue());

    license.setLastCheckTime(new Date());
    license.setUpdateTime(new Date());
    return this.updateById(license);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean extendLicense(Long licenseId, Integer extendDays) {
    if (licenseId == null || licenseId <= 0 || extendDays == null || extendDays <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    License license = this.getById(licenseId);
    if (license == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "许可证不存在");
    }

    // 延长到期时间
    Date newEndDate = DateUtil.offsetDay(license.getEndDate(), extendDays);
    license.setEndDate(newEndDate);
    license.setUpdateTime(new Date());

    // 如果已过期，重新激活
    if (license.getIsActive() == 2) {
      license.setIsActive(1);
    }

    Boolean result = this.updateById(license);
    log.info("续期许可证: {}, 延长天数: {}", licenseId, extendDays);
    return result;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean upgradeLicense(Long licenseId, String newLicenseType) {
    if (licenseId == null || licenseId <= 0 || StrUtil.isBlank(newLicenseType)) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    License license = this.getById(licenseId);
    if (license == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "许可证不存在");
    }

    // 升级许可证类型
    license.setLicenseType(newLicenseType);
    license.setMaxUsers(getDefaultMaxUsers(newLicenseType));
    license.setMaxQuestions(getDefaultMaxQuestions(newLicenseType));
    license.setUpdateTime(new Date());

    Boolean result = this.updateById(license);
    log.info("升级许可证: {}, 新类型: {}", licenseId, newLicenseType);
    return result;
  }

  @Override
  public LicenseUsageStats getUsageStats() {
    LicenseUsageStats stats = new LicenseUsageStats();
    License license = getCurrentLicense();

    if (license == null) {
      stats.setCanCreateContest(false);
      stats.setCanUsePlagiarism(false);
      return stats;
    }

    // 统计用户数
    QueryWrapper<User> userWrapper = new QueryWrapper<>();
    userWrapper.eq("userRole", "user");
    stats.setTotalUsers(userMapper.selectCount(userWrapper).intValue());

    // 统计题目数
    QueryWrapper<Question> questionWrapper = new QueryWrapper<>();
    stats.setTotalQuestions((int) questionService.count(questionWrapper));

    // 功能权限
    stats.setCanCreateContest(
        license.getEnableContest() != null && license.getEnableContest() == 1);
    stats.setCanUsePlagiarism(
        license.getEnablePlagiarism() != null && license.getEnablePlagiarism() == 1);

    // 计算剩余天数
    long daysUntilExpiry = DateUtil.betweenDay(new Date(), license.getEndDate(), false);
    stats.setDaysUntilExpiry((int) daysUntilExpiry);

    return stats;
  }

  /** 生成许可证密钥 */
  private String generateLicenseKey(LicenseRequest request) {
    try {
      String rawData =
          request.getSchoolName()
              + request.getLicenseType()
              + System.currentTimeMillis()
              + SECRET_SALT
              + random.nextInt(10000);

      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] hash = md.digest(rawData.getBytes("UTF-8"));

      // 转换为十六进制字符串并取前32位
      StringBuilder hexString = new StringBuilder();
      for (int i = 0; i < Math.min(16, hash.length); i++) {
        String hex = Integer.toHexString(0xff & hash[i]);
        if (hex.length() == 1) hexString.append('0');
        hexString.append(hex);
      }

      // 格式化为 XXXX-XXXX-XXXX-XXXX-XXXX-XXXX-XXXX-XXXX
      String key = hexString.toString().toUpperCase();
      return formatLicenseKey(key);
    } catch (Exception e) {
      log.error("生成许可证密钥失败", e);
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成许可证失败");
    }
  }

  /** 格式化许可证密钥 */
  private String formatLicenseKey(String key) {
    StringBuilder formatted = new StringBuilder();
    for (int i = 0; i < key.length(); i += 4) {
      if (i > 0) {
        formatted.append("-");
      }
      formatted.append(key.substring(i, Math.min(i + 4, key.length())));
    }
    return formatted.toString();
  }

  /** 获取默认最大用户数 */
  private Integer getDefaultMaxUsers(String licenseType) {
    switch (licenseType) {
      case "TRIAL":
        return 50;
      case "STANDARD":
        return 200;
      case "PREMIUM":
        return 500;
      case "ENTERPRISE":
        return 9999;
      default:
        return 100;
    }
  }

  /** 获取默认最大题目数 */
  private Integer getDefaultMaxQuestions(String licenseType) {
    switch (licenseType) {
      case "TRIAL":
        return 100;
      case "STANDARD":
        return 500;
      case "PREMIUM":
        return 2000;
      case "ENTERPRISE":
        return 99999;
      default:
        return 500;
    }
  }
}
