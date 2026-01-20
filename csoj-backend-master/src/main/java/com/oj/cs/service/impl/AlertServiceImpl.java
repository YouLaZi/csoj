package com.oj.cs.service.impl;

import java.util.Date;

import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.cs.mapper.AlertMapper;
import com.oj.cs.model.entity.Alert;
import com.oj.cs.service.AlertService;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/** 异常告警服务实现 */
@Service
@Slf4j
public class AlertServiceImpl extends ServiceImpl<AlertMapper, Alert> implements AlertService {

  @Resource private AlertMapper alertMapper;

  @Autowired(required = false)
  private JavaMailSender mailSender;

  /** 告警接收邮箱（可从配置读取） */
  private static final String ALERT_EMAIL = "admin@csoj.edu";

  @Override
  public boolean createAlert(Alert alert) {
    alert.setCreateTime(new Date());
    alert.setUpdateTime(new Date());
    alert.setIsProcessed(0);
    alert.setIsNotified(0);
    alert.setNotifyCount(0);
    int result = alertMapper.insert(alert);

    // 异步发送通知
    if (result > 0) {
      sendAlertNotification(alert);
    }

    return result > 0;
  }

  @Async
  @Override
  public void sendAlertNotification(Alert alert) {
    try {
      // 根据严重级别决定通知方式
      if ("critical".equals(alert.getSeverity()) || "high".equals(alert.getSeverity())) {
        // 发送邮件通知
        if (mailSender != null) {
          sendEmailAlert(alert);
        }

        // 记录通知日志
        log.warn("发送告警通知: title={}, severity={}", alert.getTitle(), alert.getSeverity());
      }

      // 更新通知状态
      alert.setIsNotified(1);
      alert.setNotifyCount(alert.getNotifyCount() + 1);
      alertMapper.updateById(alert);

    } catch (Exception e) {
      log.error("发送告警通知失败: alertId={}", alert.getId(), e);
    }
  }

  /** 发送邮件告警 */
  private void sendEmailAlert(Alert alert) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(ALERT_EMAIL);
      message.setSubject("[CSOJ告警] " + alert.getTitle());
      message.setText(buildEmailContent(alert));
      mailSender.send(message);
      log.info("邮件告警发送成功: alertId={}", alert.getId());
    } catch (Exception e) {
      log.error("邮件告警发送失败: alertId={}", alert.getId(), e);
    }
  }

  /** 构建邮件内容 */
  private String buildEmailContent(Alert alert) {
    StringBuilder sb = new StringBuilder();
    sb.append("告警详情:\n\n");
    sb.append("标题: ").append(alert.getTitle()).append("\n");
    sb.append("类型: ").append(alert.getAlertType()).append("\n");
    sb.append("级别: ").append(alert.getSeverity()).append("\n");
    sb.append("来源: ").append(alert.getSource()).append("\n");
    sb.append("路径: ").append(alert.getRequestPath()).append("\n");
    sb.append("时间: ").append(alert.getCreateTime()).append("\n");
    sb.append("\n内容:\n").append(alert.getContent()).append("\n");

    if (StrUtil.isNotBlank(alert.getStackTrace())) {
      // 只显示部分堆栈信息
      String stack = alert.getStackTrace();
      if (stack.length() > 1000) {
        stack = stack.substring(0, 1000) + "...";
      }
      sb.append("\n堆栈信息:\n").append(stack);
    }

    return sb.toString();
  }

  @Override
  public boolean processAlert(Long alertId, String processNote, Long processedBy) {
    Alert alert = alertMapper.selectById(alertId);
    if (alert == null) {
      return false;
    }

    alert.setIsProcessed(1);
    alert.setProcessedTime(new Date());
    alert.setProcessedBy(processedBy);
    alert.setProcessNote(processNote);
    alert.setUpdateTime(new Date());

    return alertMapper.updateById(alert) > 0;
  }

  @Override
  public Alert getAlertById(Long alertId) {
    return alertMapper.selectById(alertId);
  }

  @Override
  public void createExceptionAlert(
      String title,
      String alertType,
      String severity,
      String content,
      String stackTrace,
      String source,
      String requestPath,
      Long userId) {
    Alert alert = new Alert();
    alert.setTitle(title);
    alert.setAlertType(alertType);
    alert.setSeverity(severity);
    alert.setContent(content);
    alert.setStackTrace(stackTrace);
    alert.setSource(source);
    alert.setRequestPath(requestPath);
    alert.setUserId(userId);

    createAlert(alert);
    log.error("创建异常告警: title={}, severity={}, path={}", title, severity, requestPath);
  }
}
