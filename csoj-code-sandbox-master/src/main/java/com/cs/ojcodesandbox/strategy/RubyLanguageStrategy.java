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

import javax.annotation.Resource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component("ruby")
@ConditionalOnProperty(name = "docker.available", havingValue = "true", matchIfMissing = false)
public class RubyLanguageStrategy implements CodeSandboxStrategy {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_RUBY_FILE_NAME = "main.rb";
    private static final String DOCKER_IMAGE = "ruby:latest"; // Or your preferred Ruby image
    private static final long RUN_TIME_OUT = 10000L; // Added timeout for consistency

    // private static final long TIME_OUT = 10000L; // Timeout handled by DockerService

    @Resource
    private DockerService dockerService;

    // Interpreter availability check is implicitly handled by Docker execution

    private void cleanupFiles(String path) {
        if (FileUtil.exist(path)) {
            boolean del = FileUtil.del(path);
            System.out.println("删除 " + path + (del ? " 成功" : " 失败"));
        }
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        String code = executeCodeRequest.getCode();
        List<String> inputList = executeCodeRequest.getInputList();

        // 1. 把用户的代码保存为文件
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }

        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID().toString();
        String userCodeFilePath = userCodeParentPath + File.separator + GLOBAL_RUBY_FILE_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodeFilePath, StandardCharsets.UTF_8);

        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        JudgeInfo judgeInfo = new JudgeInfo();
        executeCodeResponse.setJudgeInfo(judgeInfo);

        try {
            // Ruby is an interpreted language, no compilation step needed.
            // 2. 执行代码 (在 Docker 容器内)
            List<String> outputList = new ArrayList<>();
            long maxTime = 0L;

            for (String inputArgs : inputList) {
                String[] runCmd = {"ruby", "/app/" + GLOBAL_RUBY_FILE_NAME};
                System.out.println("[Ruby] 运行命令: " + Arrays.toString(runCmd) + " 输入: " + inputArgs);
                ExecuteMessage executeMessage = dockerService.executeCommandInContainer(
                        DOCKER_IMAGE,
                        userCodeParentPath,
                        Arrays.asList(inputArgs.split("\\s+")), // Pass input via stdin
                        runCmd,
                        RUN_TIME_OUT
                );

                if (executeMessage.getExitValue() != 0 || StrUtil.isNotBlank(executeMessage.getErrorMessage())) {
                    System.out.println("[Ruby] 运行失败: exit=" + executeMessage.getExitValue() + ", errorMsg=" + executeMessage.getErrorMessage() + ", output=" + executeMessage.getMessage());
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
                    if (executeMessage.getTime() != null && executeMessage.getTime() >= RUN_TIME_OUT) {
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
            judgeInfo.setMemory(0L); // Memory usage not tracked here

        } catch (InterruptedException e) {
            System.out.println("[Ruby] 执行被中断: " + e.getMessage());
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR.getValue());
            executeCodeResponse.setMessage("Execution interrupted: " + e.getMessage());
            judgeInfo.setMessage("Execution interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.println("[Ruby] 执行异常: " + e.getMessage());
            e.printStackTrace();
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR.getValue());
            executeCodeResponse.setMessage("Execution failed: " + e.getMessage());
            judgeInfo.setMessage("Execution failed: " + e.getMessage());
        } finally {
            // 4. 清理文件
            cleanupFiles(userCodeParentPath);
        }
        return executeCodeResponse;
    }

    // Helper methods handleException, processExecutionResults, and cleanupFiles are no longer needed
    // as their logic is incorporated into the main executeCode method or handled by DockerService.
}