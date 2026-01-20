package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/** 比赛 */
@TableName(value = "contest")
@Data
public class Contest implements Serializable {

  /** id */
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  /** 比赛名称 */
  private String title;

  /** 比赛描述 */
  private String description;

  /** 比赛类型（PUBLIC-公开赛，PRIVATE-私有赛，PASSWORD-密码赛） */
  private String type;

  /** 比赛密码（type为PASSWORD时使用） */
  private String password;

  /** 关联的题目ID列表（JSON数组格式） */
  private String questionIds;

  /** 开始时间 */
  private Date startTime;

  /** 结束时间 */
  private Date endTime;

  /** 创建用户ID（组织者） */
  private Long userId;

  /** 状态（DRAFT-草稿，ONGOING-进行中，ENDED-已结束） */
  private String status;

  /** 参与人数 */
  private Integer participantCount;

  /** 是否启用排行榜 */
  private Boolean enableRanking;

  /** 是否显示实时排名 */
  private Boolean showRealTimeRanking;

  /** 封榜时间（分钟，结束前多少分钟隐藏排名） */
  private Integer rankingFreezeMinutes;

  /** 创建时间 */
  private Date createTime;

  /** 更新时间 */
  private Date updateTime;

  /** 是否删除 */
  @TableLogic private Integer isDelete;

  private static final long serialVersionUID = 1L;
}
