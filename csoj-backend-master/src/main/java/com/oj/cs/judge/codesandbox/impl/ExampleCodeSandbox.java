package com.oj.cs.judge.codesandbox.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.oj.cs.judge.codesandbox.CodeSandbox;
import com.oj.cs.judge.codesandbox.model.ExecuteCodeRequest;
import com.oj.cs.judge.codesandbox.model.ExecuteCodeResponse;
import com.oj.cs.judge.codesandbox.model.JudgeInfo;
import com.oj.cs.model.enums.JudgeInfoMessageEnum;
import com.oj.cs.model.enums.QuestionSubmitStatusEnum;

import lombok.extern.slf4j.Slf4j;

/** 示例代码沙箱（仅为了跑通业务流程） */
@Slf4j
@Component
public class ExampleCodeSandbox implements CodeSandbox {
  @Override
  public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
    List<String> inputList = executeCodeRequest.getInputList();
    ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
    executeCodeResponse.setOutputList(inputList);
    executeCodeResponse.setMessage("测试执行成功");
    executeCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
    JudgeInfo judgeInfo = new JudgeInfo();
    judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getText());
    judgeInfo.setMemory(100L);
    judgeInfo.setTime(100L);
    executeCodeResponse.setJudgeInfo(judgeInfo);
    return executeCodeResponse;
  }
}
