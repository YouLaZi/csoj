package com.oj.cs.service;

import com.oj.cs.model.entity.Alert;

/** 异常告警服务 */
public interface AlertService {

  /**
   * 创建告警
   *
   * @param alert 告警信息
   * @return 是否创建成功
   */
  boolean createAlert(Alert alert);

  /**
   * 发送告警通知
   *
   * @param alert 告警信息
   */
  void sendAlertNotification(Alert alert);

  /**
   * 处理告警
   *
   * @param alertId 告警ID
   * @param processNote 处理备注
   * @param processedBy 处理人ID
   * @return 是否处理成功
   */
  boolean processAlert(Long alertId, String processNote, Long processedBy);

  /**
   * 根据ID查询告警
   *
   * @param alertId 告警ID
   * @return 告警信息
   */
  Alert getAlertById(Long alertId);

  /**
   * 根据异常信息创建告警
   *
   * @param title 告警标题
   * @param alertType 告警类型
   * @param severity 告警级别
   * @param content 内容
   * @param stackTrace 异常堆栈
   * @param source 来源
   * @param requestPath 请求路径
   * @param userId 用户ID
   */
  void createExceptionAlert(
      String title,
      String alertType,
      String severity,
      String content,
      String stackTrace,
      String source,
      String requestPath,
      Long userId);
}
