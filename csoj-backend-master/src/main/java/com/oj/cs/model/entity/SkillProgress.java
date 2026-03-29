package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/** 技能进度 */
@TableName(value = "skill_progress")
@Data
public class SkillProgress implements Serializable {

  /** id */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** 用户id */
  private Long userId;

  /** 技能代码(标签) */
  private String skillCode;

  /** 技能名称 */
  private String skillName;

  /** 技能等级 */
  private Integer level;

  /** 经验值 */
  private Integer experience;

  /** 解决问题数 */
  private Integer problemsSolved;

  /** 最后练习时间 */
  private Date lastPracticeTime;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  /** 是否删除 */
  @TableLogic private Integer isDelete;

  @TableField(exist = false)
  private static final long serialVersionUID = 1L;
}
