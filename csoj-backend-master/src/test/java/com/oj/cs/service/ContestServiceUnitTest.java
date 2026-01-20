package com.oj.cs.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.oj.cs.model.dto.contest.ContestQueryRequest;
import com.oj.cs.model.entity.Contest;
import com.oj.cs.model.vo.ContestVO;
import com.oj.cs.service.impl.ContestServiceImpl;

/**
 * ContestService 单元测试
 *
 * <p>测试覆盖： - 查询条件构建 - VO转换 - 参数校验
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("比赛服务单元测试")
public class ContestServiceUnitTest {

  @InjectMocks private ContestServiceImpl contestService;

  @Mock private UserService userService;

  private Contest testContest;
  private ContestQueryRequest contestQueryRequest;

  @BeforeEach
  void setUp() {
    // 创建测试比赛
    testContest = new Contest();
    testContest.setId(1L);
    testContest.setTitle("算法竞赛");
    testContest.setDescription("测试比赛描述");
    testContest.setType("PUBLIC");
    testContest.setPassword("");
    testContest.setQuestionIds("[1,2,3]");
    testContest.setStartTime(new Date());
    testContest.setEndTime(new Date(System.currentTimeMillis() + 3600000));
    testContest.setUserId(1L);
    testContest.setStatus("DRAFT");
    testContest.setParticipantCount(0);
    testContest.setEnableRanking(true);
    testContest.setShowRealTimeRanking(true);
    testContest.setRankingFreezeMinutes(0);
    testContest.setCreateTime(new Date());
    testContest.setUpdateTime(new Date());

    // 创建查询请求
    contestQueryRequest = new ContestQueryRequest();
    contestQueryRequest.setTitle("算法");
    contestQueryRequest.setCurrent(1L);
    contestQueryRequest.setPageSize(10L);
  }

  // ==================== getQueryWrapper 测试 ====================

  @Test
  @DisplayName("获取查询条件 - 成功")
  void testGetQueryWrapper_Success() {
    QueryWrapper<Contest> queryWrapper = contestService.getQueryWrapper(contestQueryRequest);

    assertNotNull(queryWrapper);
  }

  @Test
  @DisplayName("获取查询条件 - 空请求")
  void testGetQueryWrapper_EmptyRequest() {
    ContestQueryRequest emptyRequest = new ContestQueryRequest();
    QueryWrapper<Contest> queryWrapper = contestService.getQueryWrapper(emptyRequest);

    assertNotNull(queryWrapper);
  }

  @Test
  @DisplayName("获取查询条件 - null请求")
  void testGetQueryWrapper_NullRequest() {
    QueryWrapper<Contest> queryWrapper = contestService.getQueryWrapper(null);

    assertNotNull(queryWrapper);
  }

  @Test
  @DisplayName("获取查询条件 - 带标题过滤")
  void testGetQueryWrapper_WithTitle() {
    contestQueryRequest.setTitle("算法竞赛");
    QueryWrapper<Contest> queryWrapper = contestService.getQueryWrapper(contestQueryRequest);

    assertNotNull(queryWrapper);
  }

  @Test
  @DisplayName("获取查询条件 - 带类型过滤")
  void testGetQueryWrapper_WithType() {
    contestQueryRequest.setType("PUBLIC");
    QueryWrapper<Contest> queryWrapper = contestService.getQueryWrapper(contestQueryRequest);

    assertNotNull(queryWrapper);
  }

  @Test
  @DisplayName("获取查询条件 - 带状态过滤")
  void testGetQueryWrapper_WithStatus() {
    contestQueryRequest.setStatus("ONGOING");
    QueryWrapper<Contest> queryWrapper = contestService.getQueryWrapper(contestQueryRequest);

    assertNotNull(queryWrapper);
  }

  @Test
  @DisplayName("获取查询条件 - 带用户ID过滤")
  void testGetQueryWrapper_WithUserId() {
    contestQueryRequest.setUserId(1L);
    QueryWrapper<Contest> queryWrapper = contestService.getQueryWrapper(contestQueryRequest);

    assertNotNull(queryWrapper);
  }

  @Test
  @DisplayName("获取查询条件 - 带时间范围过滤")
  void testGetQueryWrapper_WithTimeRange() {
    Date startTime = new Date();
    Date endTime = new Date(System.currentTimeMillis() + 86400000);
    contestQueryRequest.setStartTimeBegin(startTime);
    contestQueryRequest.setStartTimeEnd(endTime);

    QueryWrapper<Contest> queryWrapper = contestService.getQueryWrapper(contestQueryRequest);

    assertNotNull(queryWrapper);
  }

  // ==================== getContestVO 测试 ====================

  @Test
  @DisplayName("获取比赛VO - 成功")
  void testGetContestVO_Success() {
    ContestVO contestVO = contestService.getContestVO(testContest);

    assertNotNull(contestVO);
    assertEquals(testContest.getId(), contestVO.getId());
    assertEquals(testContest.getTitle(), contestVO.getTitle());
    assertEquals(testContest.getDescription(), contestVO.getDescription());
    assertEquals(testContest.getType(), contestVO.getType());
    assertNotNull(contestVO.getQuestionIds());
  }

  @Test
  @DisplayName("获取比赛VO - null输入")
  void testGetContestVO_NullInput() {
    // 需要显式转换来解决方法重载歧义
    ContestVO contestVO = ((ContestService) contestService).getContestVO((Contest) null);

    assertNull(contestVO);
  }

  @Test
  @DisplayName("获取比赛VO - 持续时间计算")
  void testGetContestVO_DurationCalculation() {
    testContest.setStartTime(new Date(System.currentTimeMillis() - 3600000));
    testContest.setEndTime(new Date());

    ContestVO contestVO = contestService.getContestVO(testContest);

    assertNotNull(contestVO);
    assertNotNull(contestVO.getDurationMinutes());
    assertEquals(Long.valueOf(60), contestVO.getDurationMinutes());
  }

  @Test
  @DisplayName("获取比赛VO - 未开始倒计时")
  void testGetContestVO_MinutesToStart() {
    testContest.setStartTime(new Date(System.currentTimeMillis() + 3600000));
    testContest.setEndTime(new Date(System.currentTimeMillis() + 7200000));

    ContestVO contestVO = contestService.getContestVO(testContest);

    assertNotNull(contestVO);
    assertNotNull(contestVO.getMinutesToStart());
  }

  @Test
  @DisplayName("获取比赛VO - 进行中倒计时")
  void testGetContestVO_MinutesToEnd() {
    testContest.setStartTime(new Date(System.currentTimeMillis() - 3600000));
    testContest.setEndTime(new Date(System.currentTimeMillis() + 3600000));

    ContestVO contestVO = contestService.getContestVO(testContest);

    assertNotNull(contestVO);
    assertNotNull(contestVO.getMinutesToEnd());
  }

  // ==================== getContestVO List 测试 ====================

  @Test
  @DisplayName("获取比赛VO列表 - 有数据")
  void testGetContestVOList_WithData() {
    List<Contest> contestList = Arrays.asList(testContest);

    List<ContestVO> contestVOList = contestService.getContestVO(contestList);

    assertNotNull(contestVOList);
    assertEquals(1, contestVOList.size());
    assertEquals(testContest.getId(), contestVOList.get(0).getId());
  }

  @Test
  @DisplayName("获取比赛VO列表 - 空列表")
  void testGetContestVOList_EmptyList() {
    List<Contest> contestList = Collections.emptyList();

    List<ContestVO> contestVOList = contestService.getContestVO(contestList);

    assertNotNull(contestVOList);
    assertTrue(contestVOList.isEmpty());
  }

  @Test
  @DisplayName("获取比赛VO列表 - null输入")
  void testGetContestVOList_NullInput() {
    // 需要显式转换来解决方法重载歧义
    List<ContestVO> contestVOList =
        ((ContestService) contestService).getContestVO((List<Contest>) null);

    assertNotNull(contestVOList);
    assertTrue(contestVOList.isEmpty());
  }

  // ==================== 比赛实体测试 ====================

  @Test
  @DisplayName("比赛实体 - 字段完整性")
  void testContestEntity_CompleteFields() {
    assertNotNull(testContest.getId());
    assertNotNull(testContest.getTitle());
    assertNotNull(testContest.getType());
    assertNotNull(testContest.getStartTime());
    assertNotNull(testContest.getEndTime());
    assertNotNull(testContest.getUserId());
    assertNotNull(testContest.getStatus());
    assertNotNull(testContest.getCreateTime());
    assertNotNull(testContest.getUpdateTime());
  }

  @Test
  @DisplayName("比赛实体 - 排行榜配置")
  void testContestEntity_RankingConfig() {
    assertTrue(testContest.getEnableRanking());
    assertTrue(testContest.getShowRealTimeRanking());
    assertEquals(Integer.valueOf(0), testContest.getRankingFreezeMinutes());
  }

  // ==================== 比赛类型测试 ====================

  @Test
  @DisplayName("比赛类型 - PUBLIC")
  void testContestType_Public() {
    testContest.setType("PUBLIC");

    ContestVO contestVO = contestService.getContestVO(testContest);

    assertEquals("PUBLIC", contestVO.getType());
  }

  @Test
  @DisplayName("比赛类型 - PRIVATE")
  void testContestType_Private() {
    testContest.setType("PRIVATE");

    ContestVO contestVO = contestService.getContestVO(testContest);

    assertEquals("PRIVATE", contestVO.getType());
  }

  @Test
  @DisplayName("比赛类型 - PASSWORD")
  void testContestType_Password() {
    testContest.setType("PASSWORD");
    testContest.setPassword("test123");

    ContestVO contestVO = contestService.getContestVO(testContest);

    assertEquals("PASSWORD", contestVO.getType());
    // 密码不应包含在VO中（脱敏）
  }

  // ==================== 比赛状态测试 ====================

  @Test
  @DisplayName("比赛状态 - DRAFT")
  void testContestStatus_Draft() {
    testContest.setStatus("DRAFT");

    ContestVO contestVO = contestService.getContestVO(testContest);

    assertEquals("DRAFT", contestVO.getStatus());
  }

  @Test
  @DisplayName("比赛状态 - ONGOING")
  void testContestStatus_Ongoing() {
    testContest.setStatus("ONGOING");

    ContestVO contestVO = contestService.getContestVO(testContest);

    assertEquals("ONGOING", contestVO.getStatus());
  }

  @Test
  @DisplayName("比赛状态 - ENDED")
  void testContestStatus_Ended() {
    testContest.setStatus("ENDED");

    ContestVO contestVO = contestService.getContestVO(testContest);

    assertEquals("ENDED", contestVO.getStatus());
  }

  // ==================== 封榜功能测试 ====================

  @Test
  @DisplayName("封榜功能 - 未封榜")
  void testRankingFrozen_NotFrozen() {
    testContest.setRankingFreezeMinutes(0);

    ContestVO contestVO = contestService.getContestVO(testContest);

    assertNotNull(contestVO);
    assertEquals(Boolean.FALSE, contestVO.getIsRankingFrozen());
  }

  @Test
  @DisplayName("封榜功能 - 已封榜")
  void testRankingFrozen_Frozen() {
    testContest.setRankingFreezeMinutes(10);
    testContest.setEndTime(new Date(System.currentTimeMillis() - 600000)); // 10分钟前结束

    ContestVO contestVO = contestService.getContestVO(testContest);

    assertNotNull(contestVO);
    assertEquals(Boolean.TRUE, contestVO.getIsRankingFrozen());
  }

  // ==================== 题目ID解析测试 ====================

  @Test
  @DisplayName("题目ID解析 - 正常")
  void testQuestionIds_ParseSuccess() {
    testContest.setQuestionIds("[1,2,3]");

    ContestVO contestVO = contestService.getContestVO(testContest);

    assertNotNull(contestVO.getQuestionIds());
    assertEquals(3, contestVO.getQuestionIds().size());
  }

  @Test
  @DisplayName("题目ID解析 - 空字符串")
  void testQuestionIds_EmptyString() {
    testContest.setQuestionIds("");

    ContestVO contestVO = contestService.getContestVO(testContest);

    assertNotNull(contestVO);
    assertNull(contestVO.getQuestionIds());
  }

  @Test
  @DisplayName("题目ID解析 - null")
  void testQuestionIds_Null() {
    testContest.setQuestionIds(null);

    ContestVO contestVO = contestService.getContestVO(testContest);

    assertNotNull(contestVO);
    assertNull(contestVO.getQuestionIds());
  }

  // ==================== 边界条件测试 ====================

  @Test
  @DisplayName("边界条件 - 零参与人数")
  void testBoundary_ZeroParticipants() {
    testContest.setParticipantCount(0);

    ContestVO contestVO = contestService.getContestVO(testContest);

    assertEquals(Integer.valueOf(0), contestVO.getParticipantCount());
  }

  @Test
  @DisplayName("边界条件 - 大参与人数")
  void testBoundary_LargeParticipants() {
    testContest.setParticipantCount(999999);

    ContestVO contestVO = contestService.getContestVO(testContest);

    assertEquals(Integer.valueOf(999999), contestVO.getParticipantCount());
  }

  @Test
  @DisplayName("边界条件 - 空标题")
  void testBoundary_EmptyTitle() {
    testContest.setTitle("");

    ContestVO contestVO = contestService.getContestVO(testContest);

    assertEquals("", contestVO.getTitle());
  }

  @Test
  @DisplayName("边界条件 - 空描述")
  void testBoundary_EmptyDescription() {
    testContest.setDescription(null);

    ContestVO contestVO = contestService.getContestVO(testContest);

    assertNull(contestVO.getDescription());
  }
}
