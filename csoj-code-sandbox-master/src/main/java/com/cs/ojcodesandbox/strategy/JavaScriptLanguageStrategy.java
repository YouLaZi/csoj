package com.cs.ojcodesandbox.strategy;

import com.cs.ojcodesandbox.CodeSandboxStrategy;
import com.cs.ojcodesandbox.service.DockerService;
import com.cs.ojcodesandbox.model.ExecuteMessage;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.IdUtil;
import com.cs.ojcodesandbox.model.ExecuteCodeRequest;
import com.cs.ojcodesandbox.model.ExecuteCodeResponse;
import com.cs.ojcodesandbox.model.JudgeInfo;
import com.cs.ojcodesandbox.model.enums.ExecuteCodeStatusEnum;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource; // Ensure Resource is imported
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component("javascript")
@ConditionalOnProperty(name = "docker.available", havingValue = "true", matchIfMissing = false)
public class JavaScriptLanguageStrategy implements CodeSandboxStrategy {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_JS_FILE_NAME = "main.js"; // Script name inside Docker
    private static final String DOCKER_IMAGE = "node:18-alpine"; // Example Node.js image
    private static final long TIME_OUT = 10000L;

    @Resource
    private DockerService dockerService;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        String code = executeCodeRequest.getCode();
        List<String> inputList = executeCodeRequest.getInputList();

        // 1. 把用户的代码保存为文件
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        // 判断全局代码目录是否存在，没有则新建
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }

        // 把用户的代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID().toString();
        String userCodeFilePath = userCodeParentPath + File.separator + GLOBAL_JS_FILE_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodeFilePath, StandardCharsets.UTF_8);

        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        JudgeInfo judgeInfo = new JudgeInfo();
        executeCodeResponse.setJudgeInfo(judgeInfo);
        long maxTime = 0L;

        try {
            // JavaScript不需要显式编译步骤，直接运行
            // 2. 执行代码 (在 Docker 容器内)
            for (String inputArgs : inputList) {
                String[] runCmd = {"node", "/app/" + GLOBAL_JS_FILE_NAME};
                System.out.println("[JavaScript] 运行命令: " + Arrays.toString(runCmd) + " 输入: " + inputArgs);
                // DockerService.executeCodeWithLogs 现在返回 ExecuteMessage
                ExecuteMessage executeMessage = dockerService.executeCommandInContainer(
                        DOCKER_IMAGE,
                        userCodeParentPath, // 本地代码目录，会映射到容器的/app
                        Arrays.asList(inputArgs.split("\\s+")), // 将输入参数作为命令的一部分或通过stdin
                        runCmd,
                        TIME_OUT
                );

                if (executeMessage.getExitValue() != 0 || StrUtil.isNotBlank(executeMessage.getErrorMessage())) {
                    System.out.println("[JavaScript] 运行失败: exit=" + executeMessage.getExitValue() + ", errorMsg=" + executeMessage.getErrorMessage() + ", output=" + executeMessage.getMessage());
                    int exitValue = executeMessage.getExitValue() == null ? 0 : executeMessage.getExitValue();
                    boolean isStreamError = exitValue == -5 || exitValue == -6 || exitValue == -7 || exitValue == -8;
                    boolean hasOutput = executeMessage.getMessage() != null && !executeMessage.getMessage().trim().isEmpty();
                    boolean hasNoErrorMsg = executeMessage.getErrorMessage() == null || executeMessage.getErrorMessage().trim().isEmpty();
                    if (isStreamError && hasOutput && hasNoErrorMsg) {
                        outputList.add(executeMessage.getMessage().trim());
                        if (executeMessage.getTime() != null) {
                            maxTime = Math.max(maxTime, executeMessage.getTime());
                        }
                        continue;
                    }
                    if (executeMessage.getTime() != null && executeMessage.getTime() >= TIME_OUT) {
                        executeCodeResponse.setStatus(ExecuteCodeStatusEnum.TIME_LIMIT_EXCEEDED.getValue());
                        executeCodeResponse.setMessage("Time Limit Exceeded");
                        judgeInfo.setMessage("Time Limit Exceeded");
                        judgeInfo.setTime(executeMessage.getTime());
                    } else {
                        executeCodeResponse.setStatus(ExecuteCodeStatusEnum.FAILED.getValue());
                        String errorMessage = StrUtil.isNotBlank(executeMessage.getErrorMessage()) ? executeMessage.getErrorMessage() : "Runtime Error";
                        executeCodeResponse.setMessage(errorMessage);
                        judgeInfo.setMessage(errorMessage);
                    }
                    cleanupFiles(userCodeParentPath);
                    return executeCodeResponse;
                }
                outputList.add(executeMessage.getMessage().trim());
                if (executeMessage.getTime() != null) {
                    maxTime = Math.max(maxTime, executeMessage.getTime());
                }
            }

            executeCodeResponse.setOutputList(outputList);
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SUCCEED.getValue());
            executeCodeResponse.setMessage("OK");
            judgeInfo.setMessage("OK");
            judgeInfo.setTime(maxTime);
            // Memory usage is not easily available from Docker exec like this, set to 0 or handle as per requirements
            judgeInfo.setMemory(0L);

        } catch (InterruptedException e) {
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR.getValue());
            executeCodeResponse.setMessage("Execution interrupted: " + e.getMessage());
            judgeInfo.setMessage("Execution interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR.getValue());
            executeCodeResponse.setMessage("Execution failed: " + e.getMessage());
            judgeInfo.setMessage("Execution failed: " + e.getMessage());
        } finally {
            // 3. 清理文件
            cleanupFiles(userCodeParentPath);
        }
        return executeCodeResponse;
    }

    // 移除了 handleException 和 processExecutionResults 方法，因为逻辑已合并或由DockerService处理

    private ExecuteCodeResponse handleException(String message, String userCodeParentPath, int status) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setMessage(message);
        executeCodeResponse.setStatus(status);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        executeCodeResponse.getJudgeInfo().setMessage(message);
        cleanupFiles(userCodeParentPath);
        return executeCodeResponse;
    }

    private ExecuteCodeResponse processExecutionResults(List<ExecuteMessage> executeMessageList, String userCodeParentPath) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        long maxTime = 0;

        for (ExecuteMessage executeMessage : executeMessageList) {
            String errorMessage = executeMessage.getErrorMessage();
            if (executeMessage.getTime() != null && executeMessage.getTime() >= TIME_OUT) {
                executeCodeResponse.setMessage("执行超时");
                executeCodeResponse.setStatus(ExecuteCodeStatusEnum.FAILED.getValue()); // Runtime error status
                JudgeInfo judgeInfo = new JudgeInfo();
                judgeInfo.setMessage("执行超时");
                judgeInfo.setTime(executeMessage.getTime());
                executeCodeResponse.setJudgeInfo(judgeInfo);
                cleanupFiles(userCodeParentPath);
                return executeCodeResponse;
            }

            if (StrUtil.isNotBlank(errorMessage)) {
                executeCodeResponse.setMessage(errorMessage);
                executeCodeResponse.setStatus(ExecuteCodeStatusEnum.FAILED.getValue()); // Runtime error status
                JudgeInfo judgeInfo = new JudgeInfo();
                judgeInfo.setMessage(errorMessage);
                 if(executeMessage.getTime() != null) {
                    judgeInfo.setTime(executeMessage.getTime());
                }
                executeCodeResponse.setJudgeInfo(judgeInfo);
                cleanupFiles(userCodeParentPath);
                return executeCodeResponse;
            }
            outputList.add(executeMessage.getMessage());
            if (executeMessage.getTime() != null) {
                maxTime = Math.max(maxTime, executeMessage.getTime());
            }
        }

        if (executeCodeResponse.getStatus() == null && outputList.size() == executeMessageList.size()) {
            executeCodeResponse.setMessage("OK");
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SUCCEED.getValue()); // Success
        } else if (executeCodeResponse.getStatus() == null) {
             executeCodeResponse.setMessage("部分执行结果缺失或未知错误");
             executeCodeResponse.setStatus(ExecuteCodeStatusEnum.FAILED.getValue());
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        executeCodeResponse.setJudgeInfo(judgeInfo);

        cleanupFiles(userCodeParentPath);
        return executeCodeResponse;
    }

    private void cleanupFiles(String path) {
        if (FileUtil.exist(path)) {
            boolean del = FileUtil.del(path);
            System.out.println("删除 " + path + (del ? " 成功" : " 失败"));
        }
    }
}