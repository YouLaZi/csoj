package com.oj.cs.constant;

/** 积分系统常量 */
public interface PointsConstant {

  /** 积分类型 */
  String CHECKIN_TYPE = "签到";

  String QUESTION_SUBMIT_TYPE = "题目提交";
  String QUESTION_ACCEPT_TYPE = "题目通过";
  String POST_SOLUTION_TYPE = "发布题解";

  /** 积分值 */
  int CHECKIN_POINTS = 15;

  int QUESTION_SUBMIT_POINTS = 5;
  int QUESTION_ACCEPT_POINTS = 20;
  int POST_SOLUTION_POINTS = 30;

  /** 积分描述 */
  String CHECKIN_DESC = "每日签到";

  String QUESTION_SUBMIT_DESC = "提交题目";
  String QUESTION_ACCEPT_DESC = "题目通过";
  String POST_SOLUTION_DESC = "发布题解";

  /** 排行榜时间范围 */
  String TIME_RANGE_DAY = "day";

  String TIME_RANGE_WEEK = "week";
  String TIME_RANGE_MONTH = "month";
  String TIME_RANGE_ALL = "all";
}
