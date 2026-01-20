package com.oj.cs.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.oj.cs.model.dto.mobile.MobilePageResponse;
import com.oj.cs.model.vo.QuestionVO;
import com.oj.cs.model.vo.mobile.MobileQuestionVO;

/** 移动端 API 工具类 用于转换和优化移动端响应数据 */
public class MobileApiUtil {

  /**
   * 将完整 QuestionVO 转换为移动端优化的 MobileQuestionVO
   *
   * @param questionVO 完整题目VO
   * @return 移动端题目VO
   */
  public static MobileQuestionVO toMobileQuestionVO(QuestionVO questionVO) {
    if (questionVO == null) {
      return null;
    }

    MobileQuestionVO mobileVO = new MobileQuestionVO();
    mobileVO.setId(questionVO.getId());
    mobileVO.setTitle(questionVO.getTitle());

    // 设置默认难度（可根据实际情况从其他地方获取）
    mobileVO.setDifficulty("中等");
    mobileVO.setDifficultyLevel(2);

    // 转换标签为数组
    if (questionVO.getTags() != null && !questionVO.getTags().isEmpty()) {
      mobileVO.setTags(questionVO.getTags().toArray(new String[0]));
    } else {
      mobileVO.setTags(new String[0]);
    }

    // 通过状态（默认为false，实际应从用户提交记录查询）
    mobileVO.setPassed(false);

    mobileVO.setSubmitNum(questionVO.getSubmitNum() != null ? questionVO.getSubmitNum() : 0);
    mobileVO.setPassRate(calculatePassRate(questionVO.getSubmitNum(), questionVO.getAcceptedNum()));

    // 得分（JudgeConfig没有score字段，设置为默认值）
    mobileVO.setScore(0);

    // 题目类型（默认为题库）
    mobileVO.setQuestionType(0);

    // 排序权重（默认使用ID）
    mobileVO.setSortOrder(questionVO.getId().intValue());

    // 生成摘要（取内容前100个字符）
    if (questionVO.getContent() != null && questionVO.getContent().length() > 100) {
      mobileVO.setSummary(questionVO.getContent().substring(0, 100) + "...");
    } else {
      mobileVO.setSummary(questionVO.getContent());
    }

    // 收藏状态（默认为false，实际应从用户收藏记录查询）
    mobileVO.setFavourited(false);

    return mobileVO;
  }

  /** 批量转换为移动端VO */
  public static List<MobileQuestionVO> toMobileQuestionVOList(List<QuestionVO> questionVOList) {
    if (questionVOList == null || questionVOList.isEmpty()) {
      return new ArrayList<>();
    }

    return questionVOList.stream()
        .map(MobileApiUtil::toMobileQuestionVO)
        .collect(Collectors.toList());
  }

  /** 构建移动端分页响应 */
  public static <T> MobilePageResponse<T> buildMobilePage(
      List<T> records, Integer current, Integer size, Long total) {
    return MobilePageResponse.build(records, current, size, total);
  }

  /** 构建移动端游标分页响应（用于无限滚动） */
  public static <T> MobilePageResponse<T> buildCursorPage(
      List<T> records, String nextCursor, Boolean hasMore) {
    return MobilePageResponse.buildWithCursor(records, nextCursor, hasMore);
  }

  /** 转换难度级别为数值 */
  public static Integer convertDifficultyLevel(String difficulty) {
    if (difficulty == null) {
      return 0;
    }
    switch (difficulty.toLowerCase()) {
      case "简单":
      case "easy":
        return 1;
      case "中等":
      case "medium":
        return 2;
      case "困难":
      case "hard":
        return 3;
      default:
        return 0;
    }
  }

  /** 计算通过率 */
  private static Double calculatePassRate(Integer submitNum, Integer acceptedNum) {
    if (submitNum == null || submitNum == 0) {
      return 0.0;
    }
    if (acceptedNum == null) {
      acceptedNum = 0;
    }
    return Math.round((acceptedNum * 1000.0 / submitNum)) / 10.0;
  }

  /** 压缩响应数据（移除空字段和长文本） */
  public static void compressResponse(Object response) {
    // 可以使用 AOP 或反射实现动态压缩
    // 这里提供接口定义
  }

  /** 判断请求是否来自移动端 */
  public static boolean isMobileRequest(String userAgent) {
    if (userAgent == null) {
      return false;
    }
    String ua = userAgent.toLowerCase();
    return ua.contains("mobile")
        || ua.contains("android")
        || ua.contains("iphone")
        || ua.contains("ipad")
        || ua.contains("windows phone");
  }

  /** 获取移动端优化的分页大小 移动端通常需要更小的分页大小以减少流量 */
  public static Integer getMobilePageSize(Integer requestSize) {
    if (requestSize == null || requestSize <= 0) {
      return 10; // 移动端默认每页10条
    }
    // 移动端限制最大分页大小为20
    return Math.min(requestSize, 20);
  }
}
