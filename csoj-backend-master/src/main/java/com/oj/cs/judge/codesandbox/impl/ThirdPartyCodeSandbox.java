package com.oj.cs.judge.codesandbox.impl;

import com.oj.cs.judge.codesandbox.CodeSandbox;
import com.oj.cs.judge.codesandbox.model.ExecuteCodeRequest;
import com.oj.cs.judge.codesandbox.model.ExecuteCodeResponse;

import lombok.extern.slf4j.Slf4j;

/** 第三方代码沙箱（调用网上现成的代码沙箱） */
@Slf4j
public class ThirdPartyCodeSandbox implements CodeSandbox {
  @Override
  public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
    log.warn("第三方代码沙箱暂未实现");
    return null;
  }
}
