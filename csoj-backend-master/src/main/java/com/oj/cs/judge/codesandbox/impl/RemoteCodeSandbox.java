package com.oj.cs.judge.codesandbox.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.oj.cs.common.ErrorCode;
import com.oj.cs.config.CodeSandboxProperties;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.judge.codesandbox.CodeSandbox;
import com.oj.cs.judge.codesandbox.model.ExecuteCodeRequest;
import com.oj.cs.judge.codesandbox.model.ExecuteCodeResponse;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/** 远程代码沙箱（实际调用接口的沙箱） */
@Slf4j
@Component
public class RemoteCodeSandbox implements CodeSandbox {

  @Autowired private CodeSandboxProperties codeSandboxProperties;

  @Override
  public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
    log.info("远程代码沙箱执行代码，语言: {}", executeCodeRequest.getLanguage());
    String url = codeSandboxProperties.getUrl();
    String json = JSONUtil.toJsonStr(executeCodeRequest);

    try {
      String responseStr =
          HttpUtil.createPost(url)
              .header(codeSandboxProperties.getAuthHeader(), codeSandboxProperties.getAuthSecret())
              .body(json)
              .timeout(codeSandboxProperties.getTimeout())
              .execute()
              .body();

      if (StringUtils.isBlank(responseStr)) {
        log.error("代码沙箱返回结果为空，url: {}", url);
        throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "代码沙箱返回结果为空");
      }

      try {
        ExecuteCodeResponse response = JSONUtil.toBean(responseStr, ExecuteCodeResponse.class);
        log.info("代码沙箱执行成功，状态: {}", response.getStatus());
        return response;
      } catch (Exception parseException) {
        log.error("代码沙箱返回结果解析失败，响应: {}", responseStr, parseException);
        throw new BusinessException(
            ErrorCode.API_REQUEST_ERROR, "代码沙箱返回结果解析失败: " + parseException.getMessage());
      }

    } catch (BusinessException e) {
      // 重新抛出业务异常
      throw e;
    } catch (Exception e) {
      // 捕获所有其他异常（网络异常、超时等）
      log.error("代码沙箱调用失败，url: {}", url, e);
      throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "代码沙箱调用失败: " + e.getMessage());
    }
  }
}
