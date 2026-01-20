package com.oj.cs.model.dto.tag;

import java.io.Serializable;

import com.oj.cs.common.PageRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 标签查询请求 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TagQueryRequest extends PageRequest implements Serializable {

  /** 标签名称 */
  private String name;

  /** 标签类型（0-题目标签，1-讨论区标签） */
  private Integer type;

  private static final long serialVersionUID = 1L;
}
