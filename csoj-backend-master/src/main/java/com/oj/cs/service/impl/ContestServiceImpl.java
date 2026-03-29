package com.oj.cs.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.exception.ThrowUtils;
import com.oj.cs.model.dto.contest.ContestAddRequest;
import com.oj.cs.model.dto.contest.ContestJoinRequest;
import com.oj.cs.model.dto.contest.ContestQueryRequest;
import com.oj.cs.model.dto.contest.ContestUpdateRequest;
import com.oj.cs.model.entity.Contest;
import com.oj.cs.model.entity.ContestParticipant;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.vo.ContestRankingVO;
import com.oj.cs.model.vo.ContestStatisticsVO;
import com.oj.cs.model.vo.ContestVO;
import com.oj.cs.service.*;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/** 比赛服务实现 */
@Service
@Slf4j
public class ContestServiceImpl extends ServiceImpl<com.oj.cs.mapper.ContestMapper, Contest>
    implements ContestService {

  @Resource private UserService userService;

  @Resource private ContestParticipantService contestParticipantService;

  @Resource private QuestionService questionService;

  @Resource private QuestionSubmitService questionSubmitService;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Long createContest(ContestAddRequest contestAddRequest, Long userId) {
    // 参数校验
    if (contestAddRequest == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    if (StrUtil.isBlank(contestAddRequest.getTitle())) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "比赛名称不能为空");
    }
    if (contestAddRequest.getStartTime() == null || contestAddRequest.getEndTime() == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "开始时间和结束时间不能为空");
    }
    if (contestAddRequest.getStartTime().after(contestAddRequest.getEndTime())) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "开始时间不能晚于结束时间");
    }
    if (contestAddRequest.getQuestionIds() == null
        || contestAddRequest.getQuestionIds().isEmpty()) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "比赛题目不能为空");
    }
    if ("PASSWORD".equals(contestAddRequest.getType())
        && StrUtil.isBlank(contestAddRequest.getPassword())) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码赛必须设置密码");
    }

    // 获取创建用户
    User user = userService.getById(userId);
    ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);

    // 创建比赛
    Contest contest = new Contest();
    contest.setTitle(contestAddRequest.getTitle());
    contest.setDescription(contestAddRequest.getDescription());
    contest.setType(
        StrUtil.isNotBlank(contestAddRequest.getType()) ? contestAddRequest.getType() : "PUBLIC");
    contest.setPassword(contestAddRequest.getPassword());
    contest.setQuestionIds(JSONUtil.toJsonStr(contestAddRequest.getQuestionIds()));
    contest.setStartTime(contestAddRequest.getStartTime());
    contest.setEndTime(contestAddRequest.getEndTime());
    contest.setUserId(userId);
    contest.setStatus("DRAFT");
    contest.setParticipantCount(0);
    contest.setEnableRanking(
        contestAddRequest.getEnableRanking() != null ? contestAddRequest.getEnableRanking() : true);
    contest.setShowRealTimeRanking(
        contestAddRequest.getShowRealTimeRanking() != null
            ? contestAddRequest.getShowRealTimeRanking()
            : true);
    contest.setRankingFreezeMinutes(
        contestAddRequest.getRankingFreezeMinutes() != null
            ? contestAddRequest.getRankingFreezeMinutes()
            : 0);

    boolean result = this.save(contest);
    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

    return contest.getId();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean updateContest(ContestUpdateRequest contestUpdateRequest, Long userId) {
    // 参数校验
    if (contestUpdateRequest == null || contestUpdateRequest.getId() == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 检查比赛是否存在
    Contest oldContest = this.getById(contestUpdateRequest.getId());
    ThrowUtils.throwIf(oldContest == null, ErrorCode.NOT_FOUND_ERROR);

    // 检查权限（只有创建者可以修改）
    if (!oldContest.getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }

    // 已开始的比赛只能修改结束时间
    if ("ONGOING".equals(oldContest.getStatus())) {
      Contest update = new Contest();
      update.setId(contestUpdateRequest.getId());
      if (contestUpdateRequest.getEndTime() != null) {
        if (contestUpdateRequest.getEndTime().before(new Date())) {
          throw new BusinessException(ErrorCode.PARAMS_ERROR, "结束时间不能早于当前时间");
        }
        update.setEndTime(contestUpdateRequest.getEndTime());
      }
      return this.updateById(update);
    }

    // 草稿状态可以修改所有字段
    Contest contest = new Contest();
    contest.setId(contestUpdateRequest.getId());

    if (StrUtil.isNotBlank(contestUpdateRequest.getTitle())) {
      contest.setTitle(contestUpdateRequest.getTitle());
    }
    if (contestUpdateRequest.getDescription() != null) {
      contest.setDescription(contestUpdateRequest.getDescription());
    }
    if (StrUtil.isNotBlank(contestUpdateRequest.getType())) {
      contest.setType(contestUpdateRequest.getType());
    }
    if (contestUpdateRequest.getPassword() != null) {
      contest.setPassword(contestUpdateRequest.getPassword());
    }
    if (contestUpdateRequest.getQuestionIds() != null) {
      contest.setQuestionIds(JSONUtil.toJsonStr(contestUpdateRequest.getQuestionIds()));
    }
    if (contestUpdateRequest.getStartTime() != null) {
      contest.setStartTime(contestUpdateRequest.getStartTime());
    }
    if (contestUpdateRequest.getEndTime() != null) {
      contest.setEndTime(contestUpdateRequest.getEndTime());
    }
    if (StrUtil.isNotBlank(contestUpdateRequest.getStatus())) {
      contest.setStatus(contestUpdateRequest.getStatus());
    }
    if (contestUpdateRequest.getEnableRanking() != null) {
      contest.setEnableRanking(contestUpdateRequest.getEnableRanking());
    }
    if (contestUpdateRequest.getShowRealTimeRanking() != null) {
      contest.setShowRealTimeRanking(contestUpdateRequest.getShowRealTimeRanking());
    }
    if (contestUpdateRequest.getRankingFreezeMinutes() != null) {
      contest.setRankingFreezeMinutes(contestUpdateRequest.getRankingFreezeMinutes());
    }

    return this.updateById(contest);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean deleteContest(Long id, Long userId) {
    // 参数校验
    if (id == null || id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 检查比赛是否存在
    Contest contest = this.getById(id);
    ThrowUtils.throwIf(contest == null, ErrorCode.NOT_FOUND_ERROR);

    // 检查权限
    if (!contest.getUserId().equals(userId)) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }

    // 已开始的比赛不能删除
    if ("ONGOING".equals(contest.getStatus())) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "已开始的比赛不能删除");
    }

    return this.removeById(id);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Boolean joinContest(ContestJoinRequest joinRequest, Long userId) {
    // 参数校验
    if (joinRequest == null || joinRequest.getContestId() == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 检查比赛是否存在
    Contest contest = this.getById(joinRequest.getContestId());
    ThrowUtils.throwIf(contest == null, ErrorCode.NOT_FOUND_ERROR, "比赛不存在");

    // 检查比赛状态
    if (!"ONGOING".equals(contest.getStatus())) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "比赛未开始或已结束");
    }

    // 检查比赛类型和密码
    if ("PRIVATE".equals(contest.getType())) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "私有比赛，无法加入");
    }
    if ("PASSWORD".equals(contest.getType())) {
      if (StrUtil.isBlank(joinRequest.getPassword())) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入比赛密码");
      }
      if (!contest.getPassword().equals(joinRequest.getPassword())) {
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
      }
    }

    // 参加比赛
    contestParticipantService.joinContest(joinRequest.getContestId(), userId);
    return true;
  }

  @Override
  public List<ContestRankingVO> getContestRanking(Long contestId, Long userId) {
    // 参数校验
    if (contestId == null || contestId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 检查比赛是否存在
    Contest contest = this.getById(contestId);
    ThrowUtils.throwIf(contest == null, ErrorCode.NOT_FOUND_ERROR);

    // 检查是否启用排行榜
    if (!contest.getEnableRanking()) {
      return new ArrayList<>();
    }

    // 检查是否封榜
    boolean isFrozen = false;
    if (contest.getRankingFreezeMinutes() != null && contest.getRankingFreezeMinutes() > 0) {
      long freezeTime = contest.getEndTime().getTime() - contest.getRankingFreezeMinutes() * 60000L;
      if (System.currentTimeMillis() > freezeTime) {
        isFrozen = true;
      }
    }

    // 获取参与者列表
    List<ContestParticipant> participants =
        contestParticipantService.getContestParticipants(contestId);

    // 转换为排行榜VO
    List<ContestRankingVO> rankingList = new ArrayList<>();
    int rank = 1;

    for (ContestParticipant participant : participants) {
      ContestRankingVO rankingVO = new ContestRankingVO();
      rankingVO.setRank(rank++);
      rankingVO.setUserId(participant.getUserId());

      User user = userService.getById(participant.getUserId());
      if (user != null) {
        rankingVO.setUserName(user.getUserName());
        rankingVO.setUserAccount(user.getUserAccount());
      }

      rankingVO.setTotalScore(participant.getTotalScore());
      rankingVO.setPassedCount(participant.getPassedCount());
      rankingVO.setTotalTime(participant.getTotalTime());

      // 如果封榜，隐藏得分信息（除了用户自己）
      if (isFrozen && !userId.equals(participant.getUserId())) {
        rankingVO.setTotalScore(null);
        rankingVO.setPassedCount(null);
        rankingVO.setTotalTime(null);
      }

      rankingList.add(rankingVO);
    }

    return rankingList;
  }

  @Override
  public ContestVO getContestVOById(Long id, Long userId) {
    // 参数校验
    if (id == null || id <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 查询比赛
    Contest contest = this.getById(id);
    ThrowUtils.throwIf(contest == null, ErrorCode.NOT_FOUND_ERROR);

    // 检查权限（私有比赛只有创建者可见）
    User user = userService.getById(userId);
    boolean isAdmin = userService.isAdmin(user);
    if ("PRIVATE".equals(contest.getType()) && !contest.getUserId().equals(userId) && !isAdmin) {
      throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }

    // 转换为VO
    ContestVO contestVO = getContestVO(contest);

    // 查询当前用户是否已参加
    if (userId != null) {
      boolean hasJoined = contestParticipantService.hasJoined(id, userId);
      contestVO.setHasJoined(hasJoined);
    }

    return contestVO;
  }

  @Override
  public Page<ContestVO> listContestByPage(
      long current, long size, ContestQueryRequest queryRequest, Long userId, boolean isAdmin) {
    // 构建查询条件
    QueryWrapper<Contest> queryWrapper = getQueryWrapper(queryRequest);

    // 非管理员只能看到公开的比赛或自己创建的比赛
    if (!isAdmin) {
      queryWrapper.and(wrapper -> wrapper.eq("type", "PUBLIC").or().eq("user_id", userId));
    }

    // 处理排序
    String sortField = queryRequest.getSortField();
    String sortOrder = queryRequest.getSortOrder();
    if (StrUtil.isNotBlank(sortField)) {
      if ("desc".equalsIgnoreCase(sortOrder)) {
        queryWrapper.orderByDesc(sortField);
      } else {
        queryWrapper.orderByAsc(sortField);
      }
    } else {
      queryWrapper.orderByDesc("startTime");
    }

    // 分页查询
    Page<Contest> contestPage = this.page(new Page<>(current, size), queryWrapper);

    // 转换为VO
    Page<ContestVO> contestVOPage = new Page<>(current, size, contestPage.getTotal());
    List<ContestVO> contestVOList = getContestVO(contestPage.getRecords());
    contestVOPage.setRecords(contestVOList);

    return contestVOPage;
  }

  @Override
  public ContestVO getContestVO(Contest contest) {
    if (contest == null) {
      return null;
    }

    ContestVO contestVO = new ContestVO();
    contestVO.setId(contest.getId());
    contestVO.setTitle(contest.getTitle());
    contestVO.setDescription(contest.getDescription());
    contestVO.setType(contest.getType());
    contestVO.setStartTime(contest.getStartTime());
    contestVO.setEndTime(contest.getEndTime());
    contestVO.setUserId(contest.getUserId());
    contestVO.setStatus(contest.getStatus());
    contestVO.setParticipantCount(contest.getParticipantCount());
    contestVO.setEnableRanking(contest.getEnableRanking());
    contestVO.setShowRealTimeRanking(contest.getShowRealTimeRanking());
    contestVO.setRankingFreezeMinutes(contest.getRankingFreezeMinutes());
    contestVO.setCreateTime(contest.getCreateTime());
    contestVO.setUpdateTime(contest.getUpdateTime());

    // 解析题目ID列表
    if (StrUtil.isNotBlank(contest.getQuestionIds())) {
      try {
        contestVO.setQuestionIds(JSONUtil.toList(contest.getQuestionIds(), Long.class));
      } catch (Exception e) {
        log.error("解析题目ID列表失败", e);
      }
    }

    // 获取创建用户信息
    User user = userService.getById(contest.getUserId());
    if (user != null) {
      contestVO.setUserName(user.getUserName());
    }

    // 计算持续时间
    if (contest.getStartTime() != null && contest.getEndTime() != null) {
      long duration = (contest.getEndTime().getTime() - contest.getStartTime().getTime()) / 60000;
      contestVO.setDurationMinutes(duration);
    }

    // 计算距离开始/结束时间
    Date now = new Date();
    if (contest.getStartTime().after(now)) {
      contestVO.setMinutesToStart((contest.getStartTime().getTime() - now.getTime()) / 60000);
    } else if (contest.getEndTime().after(now)) {
      contestVO.setMinutesToEnd((contest.getEndTime().getTime() - now.getTime()) / 60000);
    }

    // 判断是否封榜
    if (contest.getRankingFreezeMinutes() != null && contest.getRankingFreezeMinutes() > 0) {
      long freezeTime = contest.getEndTime().getTime() - contest.getRankingFreezeMinutes() * 60000;
      contestVO.setIsRankingFrozen(now.getTime() > freezeTime);
    } else {
      contestVO.setIsRankingFrozen(false);
    }

    return contestVO;
  }

  @Override
  public List<ContestVO> getContestVO(List<Contest> contestList) {
    if (contestList == null || contestList.isEmpty()) {
      return new ArrayList<>();
    }

    return contestList.stream().map(this::getContestVO).collect(Collectors.toList());
  }

  @Override
  public QueryWrapper<Contest> getQueryWrapper(ContestQueryRequest queryRequest) {
    QueryWrapper<Contest> queryWrapper = new QueryWrapper<>();

    if (queryRequest == null) {
      return queryWrapper;
    }

    // 标题模糊查询
    String title = queryRequest.getTitle();
    if (StrUtil.isNotBlank(title)) {
      queryWrapper.like("title", title);
    }

    // 比赛类型精确查询
    String type = queryRequest.getType();
    if (StrUtil.isNotBlank(type)) {
      queryWrapper.eq("type", type);
    }

    // 状态精确查询
    String status = queryRequest.getStatus();
    if (StrUtil.isNotBlank(status)) {
      queryWrapper.eq("status", status);
    }

    // 创建用户ID精确查询
    Long userId = queryRequest.getUserId();
    if (userId != null) {
      queryWrapper.eq("user_id", userId);
    }

    // 开始时间范围查询
    Date startTimeBegin = queryRequest.getStartTimeBegin();
    Date startTimeEnd = queryRequest.getStartTimeEnd();
    if (startTimeBegin != null) {
      queryWrapper.ge("startTime", startTimeBegin);
    }
    if (startTimeEnd != null) {
      queryWrapper.le("startTime", startTimeEnd);
    }

    return queryWrapper;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateContestStatus() {
    // 查询所有需要更新状态的比赛
    Date now = new Date();

    // 将应该开始的比赛状态更新为 ONGOING
    QueryWrapper<Contest> startWrapper = new QueryWrapper<>();
    startWrapper.eq("status", "DRAFT");
    startWrapper.le("startTime", now);
    startWrapper.ge("endTime", now);
    List<Contest> startContests = this.list(startWrapper);

    for (Contest contest : startContests) {
      contest.setStatus("ONGOING");
    }
    this.updateBatchById(startContests);

    // 将应该结束的比赛状态更新为 ENDED
    QueryWrapper<Contest> endWrapper = new QueryWrapper<>();
    endWrapper.eq("status", "ONGOING");
    endWrapper.lt("endTime", now);
    List<Contest> endContests = this.list(endWrapper);

    for (Contest contest : endContests) {
      contest.setStatus("ENDED");
    }
    this.updateBatchById(endContests);
  }

  @Override
  public ContestStatisticsVO getContestStatistics(Long contestId) {
    // 参数校验
    if (contestId == null || contestId <= 0) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 检查比赛是否存在
    Contest contest = this.getById(contestId);
    ThrowUtils.throwIf(contest == null, ErrorCode.NOT_FOUND_ERROR, "比赛不存在");

    // 创建统计对象
    ContestStatisticsVO statistics = new ContestStatisticsVO();
    statistics.setContestId(contestId);
    statistics.setContestTitle(contest.getTitle());

    // 获取参与人数
    Integer participantCount = contestParticipantService.getParticipantCount(contestId);
    statistics.setParticipantCount(participantCount);

    // 解析题目ID列表
    List<Long> questionIds = new ArrayList<>();
    if (StrUtil.isNotBlank(contest.getQuestionIds())) {
      try {
        questionIds = JSONUtil.toList(contest.getQuestionIds(), Long.class);
      } catch (Exception e) {
        log.error("解析题目ID列表失败", e);
      }
    }

    // 如果没有题目，返回基础统计
    if (questionIds.isEmpty()) {
      statistics.setQuestionStatisticsList(new ArrayList<>());
      return statistics;
    }

    // 获取所有参与者
    List<ContestParticipant> participants =
        contestParticipantService.getContestParticipants(contestId);
    List<Integer> scores = new ArrayList<>();
    for (ContestParticipant participant : participants) {
      scores.add(participant.getTotalScore());
    }

    // 计算得分统计
    if (!scores.isEmpty()) {
      statistics.setSubmitterCount(scores.size());
      statistics.setTotalSubmits(scores.stream().mapToInt(Integer::intValue).sum());
      statistics.setAverageScore(scores.stream().mapToInt(Integer::intValue).average().orElse(0.0));
      statistics.setMaxScore(scores.stream().max(Integer::compareTo).orElse(0));
      statistics.setMinScore(scores.stream().min(Integer::compareTo).orElse(0));

      // 计算中位数
      List<Integer> sortedScores = new ArrayList<>(scores);
      java.util.Collections.sort(sortedScores);
      int size = sortedScores.size();
      if (size % 2 == 0) {
        statistics.setMedianScore(
            (sortedScores.get(size / 2 - 1) + sortedScores.get(size / 2)) / 2);
      } else {
        statistics.setMedianScore(sortedScores.get(size / 2));
      }
    }

    // 统计每个题目的情况
    List<ContestStatisticsVO.QuestionStatistics> questionStatisticsList = new ArrayList<>();

    // 获取参与者的用户ID集合
    Set<Long> participantUserIds =
        participants.stream().map(ContestParticipant::getUserId).collect(Collectors.toSet());

    for (Long questionId : questionIds) {
      ContestStatisticsVO.QuestionStatistics questionStat =
          new ContestStatisticsVO.QuestionStatistics();
      questionStat.setQuestionId(questionId);

      // 获取题目信息
      com.oj.cs.model.entity.Question question = questionService.getById(questionId);
      if (question != null) {
        questionStat.setQuestionTitle(question.getTitle());
      }

      // 查询该题目在比赛期间的提交记录（只统计参与者的提交）
      QueryWrapper<com.oj.cs.model.entity.QuestionSubmit> submitWrapper = new QueryWrapper<>();
      submitWrapper.eq("questionId", questionId);
      submitWrapper.in("userId", participantUserIds);
      submitWrapper.ge("createTime", contest.getStartTime());
      submitWrapper.le("createTime", contest.getEndTime());

      List<com.oj.cs.model.entity.QuestionSubmit> submits =
          questionSubmitService.list(submitWrapper);

      if (!submits.isEmpty()) {
        // 总提交次数
        questionStat.setTotalSubmits(submits.size());

        // 提交人数（去重）
        Set<Long> submitters =
            submits.stream()
                .map(com.oj.cs.model.entity.QuestionSubmit::getUserId)
                .collect(Collectors.toSet());
        questionStat.setSubmitterCount(submitters.size());

        // 通过人数（status = 2 表示成功）
        long passedCount =
            submits.stream()
                .filter(submit -> submit.getStatus() != null && submit.getStatus() == 2)
                .map(com.oj.cs.model.entity.QuestionSubmit::getUserId)
                .distinct()
                .count();
        questionStat.setPassedCount((int) passedCount);

        // 通过率
        if (questionStat.getSubmitterCount() > 0) {
          questionStat.setPassRate((double) passedCount / questionStat.getSubmitterCount() * 100);
        } else {
          questionStat.setPassRate(0.0);
        }
      } else {
        questionStat.setTotalSubmits(0);
        questionStat.setSubmitterCount(0);
        questionStat.setPassedCount(0);
        questionStat.setPassRate(0.0);
      }

      questionStatisticsList.add(questionStat);
    }

    statistics.setQuestionStatisticsList(questionStatisticsList);

    return statistics;
  }
}
