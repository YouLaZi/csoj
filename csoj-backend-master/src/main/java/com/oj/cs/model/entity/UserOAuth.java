package com.oj.cs.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

/** 第三方账号绑定实体 */
@Data
@TableName("user_oauth")
public class UserOAuth implements Serializable {

  /** 主键ID */
  @TableId(type = IdType.AUTO)
  private Long id;

  /** 关联的用户ID */
  private Long userId;

  /** 平台类型: GITHUB/GITEE/QQ/WECHAT */
  private String platform;

  /** 第三方平台的用户ID */
  private String platformUserId;

  /** 平台openId (QQ/微信) */
  private String openId;

  /** 统一ID (微信开放平台unionId) */
  private String unionId;

  /** 第三方昵称 */
  private String nickname;

  /** 第三方头像URL */
  private String avatar;

  /** 访问令牌 */
  private String accessToken;

  /** 绑定时间 */
  private Date bindTime;

  /** 更新时间 */
  private Date updateTime;

  /** 是否删除 */
  @TableLogic private Integer isDelete;
}
