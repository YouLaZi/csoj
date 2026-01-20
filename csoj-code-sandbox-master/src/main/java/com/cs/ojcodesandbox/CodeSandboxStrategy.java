package com.cs.ojcodesandbox;

import com.cs.ojcodesandbox.model.ExecuteCodeRequest;
import com.cs.ojcodesandbox.model.ExecuteCodeResponse;

/**
 * 代码沙箱策略接口
 */
public interface CodeSandboxStrategy {

    /**
     * 执行代码
     *
     * @param executeCodeRequest 执行代码请求
     * @return 执行代码响应
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}