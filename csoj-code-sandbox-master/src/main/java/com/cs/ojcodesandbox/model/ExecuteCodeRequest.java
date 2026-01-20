package com.cs.ojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeRequest {

    /**
     * 请求ID，用于跟踪异步处理
     */
    @Builder.Default
    private String requestId = UUID.randomUUID().toString();

    /**
     * 输入用例列表
     */
    private List<String> inputList;

    /**
     * 用户提交的代码
     */
    private String code;

    /**
     * 编程语言
     */
    private String language;
}
