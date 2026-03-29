package com.oj.cs.model.vo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/** OAuth 绑定信息 VO */
@Data
public class OAuthBindingVO implements Serializable {

  /** 绑定ID */
  private Long id;

  /** 平台类型 */
  private String platform;

  /** 平台名称 */
  private String platformName;

  /** 第三方昵称 */
  private String nickname;

  /** 第三方头像 */
  private String avatar;

  /** 绑定时间 */
  private Date bindTime;

  /** 是否启用 */
  private boolean enabled;
}
