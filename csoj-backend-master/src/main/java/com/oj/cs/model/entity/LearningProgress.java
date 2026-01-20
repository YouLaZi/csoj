package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/** 学习进度实体 */
@TableName(value = "learning_progress")
@Data
public class LearningProgress implements Serializable {

  /** 主键 */
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  /** 用户ID */
  @TableField("user_id")
  private Long userId;

  /** 已解决题目数量 */
  @TableField("solved_problems")
  private Integer solvedProblems;

  /** 最近学习的主题（JSON格式存储） */
  @TableField("recent_topics")
  private String recentTopics;

  /** 推荐学习的主题（JSON格式存储） */
  @TableField("recommended_topics")
  private String recommendedTopics;

  /** 创建时间 */
  @TableField("create_time")
  private Date createTime;

  /** 更新时间 */
  @TableField("update_time")
  private Date updateTime;

  /** 是否删除 */
  @TableField("is_delete")
  private Integer isDelete;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}
