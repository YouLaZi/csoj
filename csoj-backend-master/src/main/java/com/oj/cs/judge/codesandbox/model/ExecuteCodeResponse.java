package com.oj.cs.judge.codesandbox.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeResponse {

  private List<String> outputList;

  /** 接口信息 */
  private String message;

  /** 执行状态 */
  private Integer status;

  /** 判题信息 */
  private JudgeInfo judgeInfo;
}
