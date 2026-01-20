package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

/** 代码相似度记录实体 */
@TableName(value = "code_similarity")
@Data
public class CodeSimilarity implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 主键ID */
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  /** 题目ID */
  private Long questionId;

  /** 比较的提交记录1 ID */
  private Long submitId1;

  /** 提交记录1 的用户ID */
  private Long userId1;

  /** 比较的提交记录2 ID */
  private Long submitId2;

  /** 提交记录2 的用户ID */
  private Long userId2;

  /** 相似度分数 (0-100) */
  private Integer similarityScore;

  /** 相似度等级 (HIGH, MEDIUM, LOW) */
  private String similarityLevel;

  /** 相似行数 */
  private Integer similarLines;

  /** 总行数 */
  private Integer totalLines;

  /** 相似代码片段列表（JSON格式） */
  private String similarFragments;

  /** 检测算法类型 */
  private String algorithmType;

  /** 创建时间 */
  private Date createTime;

  /** 创建人 */
  private Long createUser;

  /** 是否已处理（教师确认是否抄袭） */
  private Integer isProcessed;

  /** 处理结果 */
  private String processResult;
}
