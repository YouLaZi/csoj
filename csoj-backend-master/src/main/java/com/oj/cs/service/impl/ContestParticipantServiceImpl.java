package com.oj.cs.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.exception.ThrowUtils;
import com.oj.cs.model.entity.Contest;
import com.oj.cs.model.entity.ContestParticipant;
import com.oj.cs.service.ContestParticipantService;
import com.oj.cs.service.ContestService;

import lombok.extern.slf4j.Slf4j;

/** 比赛参与者服务实现 */
@Service
@Slf4j
public class ContestParticipantServiceImpl
    extends ServiceImpl<com.oj.cs.mapper.ContestParticipantMapper, ContestParticipant>
    implements ContestParticipantService {

  @Resource private ContestService contestService;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long joinContest(Long contestId, Long userId) {
    // 参数校验
    if (contestId == null || userId == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 检查比赛是否存在
    Contest contest = contestService.getById(contestId);
    ThrowUtils.throwIf(contest == null, ErrorCode.NOT_FOUND_ERROR, "比赛不存在");

    // 检查比赛状态
    if (!"ONGOING".equals(contest.getStatus())) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "比赛未开始或已结束");
    }

    // 检查是否已参加
    ContestParticipant existing =
        this.getOne(
            new QueryWrapper<ContestParticipant>()
                .eq("contest_id", contestId)
                .eq("user_id", userId));
    if (existing != null) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "已参加此比赛");
    }

    // 创建参与记录
    ContestParticipant participant = new ContestParticipant();
    participant.setContestId(contestId);
    participant.setUserId(userId);
    participant.setJoinTime(new Date());
    participant.setTotalScore(0);
    participant.setPassedCount(0);
    participant.setTotalTime(0L);

    boolean result = this.save(participant);
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

    // 更新比赛参与人数
    contest.setParticipantCount(contest.getParticipantCount() + 1);
    contestService.updateById(contest);

    return participant.getId();
  }

  @Override
  public Boolean hasJoined(Long contestId, Long userId) {
    if (contestId == null || userId == null) {
      return false;
    }

    Long count =
        this.count(
            new QueryWrapper<ContestParticipant>()
                .eq("contest_id", contestId)
                .eq("user_id", userId));
    return count > 0;
  }

  @Override
  public Integer getParticipantCount(Long contestId) {
    if (contestId == null) {
      return 0;
    }

    return (int) this.count(new QueryWrapper<ContestParticipant>().eq("contest_id", contestId));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateParticipantStats(Long contestId, Long userId, Integer score, Long timeUsed) {
    // 参数校验
    if (contestId == null || userId == null) {
      return;
    }

    // 查找参与记录
    ContestParticipant participant =
        this.getOne(
            new QueryWrapper<ContestParticipant>()
                .eq("contest_id", contestId)
                .eq("user_id", userId));

    if (participant == null) {
      return;
    }

    // 更新统计信息
    participant.setTotalScore(participant.getTotalScore() + score);
    participant.setTotalTime(participant.getTotalTime() + timeUsed);
    participant.setLastSubmitTime(System.currentTimeMillis());

    // 如果得分大于0，说明通过了一题
    if (score != null && score > 0) {
      participant.setPassedCount(participant.getPassedCount() + 1);
    }

    this.updateById(participant);
  }

  @Override
  public List<ContestParticipant> getContestParticipants(Long contestId) {
    if (contestId == null) {
      return new ArrayList<>();
    }

    return this.list(
        new QueryWrapper<ContestParticipant>()
            .eq("contest_id", contestId)
            .orderByDesc("total_score")
            .orderByAsc("total_time"));
  }
}
