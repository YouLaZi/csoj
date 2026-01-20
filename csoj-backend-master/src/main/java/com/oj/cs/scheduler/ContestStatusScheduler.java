package com.oj.cs.scheduler;

import jakarta.annotation.Resource;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.oj.cs.service.ContestService;

import lombok.extern.slf4j.Slf4j;

/** 比赛状态更新定时任务 */
@Component
@Slf4j
public class ContestStatusScheduler {

  @Resource private ContestService contestService;

  /** 每分钟更新一次比赛状态 将应该开始的比赛状态更新为 ONGOING 将应该结束的比赛状态更新为 ENDED */
  @Scheduled(cron = "0 * * * * ?")
  public void updateContestStatus() {
    try {
      contestService.updateContestStatus();
    } catch (Exception e) {
      log.error("更新比赛状态失败", e);
    }
  }
}
