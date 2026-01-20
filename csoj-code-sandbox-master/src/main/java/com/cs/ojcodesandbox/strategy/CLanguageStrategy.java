package com.cs.ojcodesandbox.strategy;

import com.cs.ojcodesandbox.CodeSandboxStrategy;
import com.cs.ojcodesandbox.service.DockerService;
import com.cs.ojcodesandbox.model.ExecuteMessage;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
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

@Component("c")
@ConditionalOnProperty(name = "docker.available", havingValue = "true", matchIfMissing = false)
public class CLanguageStrategy implements CodeSandboxStrategy {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_C_FILE_NAME = "main.c";
    private static final String GLOBAL_C_EXECUTABLE_NAME = "main"; // Compiled executable name
    private static final String DOCKER_IMAGE = "gcc:latest"; // Docker image for C/C++
    private static final long COMPILE_TIME_OUT = 15000L; // 15 seconds for compilation
    private static final long RUN_TIME_OUT = 10000L; // 10 seconds for execution

    @Resource
    private DockerService dockerService;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        String code = executeCodeRequest.getCode();
        List<String> inputList = executeCodeRequest.getInputList();

        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }

        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID().toString();
        String userCodeFilePath = userCodeParentPath + File.separator + GLOBAL_C_FILE_NAME;
        FileUtil.writeString(code, userCodeFilePath, StandardCharsets.UTF_8);

        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        JudgeInfo judgeInfo = new JudgeInfo();
        executeCodeResponse.setJudgeInfo(judgeInfo);

        try {
            // Compile code inside Docker container
            // gcc /app/main.c -o /app/main -Wall -O2 -static -std=c11
            String[] compileCmd = {"gcc", "/app/" + GLOBAL_C_FILE_NAME, "-o", "/app/" + GLOBAL_C_EXECUTABLE_NAME, "-Wall", "-O2", "-static", "-std=c11"};
            System.out.println("[C] 编译命令: " + Arrays.toString(compileCmd));
            ExecuteMessage compileMessage = dockerService.executeCommandInContainer(
                    DOCKER_IMAGE,
                    userCodeParentPath,
                    new ArrayList<>(), // No input for compilation
                    compileCmd,
                    COMPILE_TIME_OUT
            );

            if (compileMessage.getExitValue() != 0 || StrUtil.isNotBlank(compileMessage.getErrorMessage())) {
                System.out.println("[C] 编译失败: exit=" + compileMessage.getExitValue() + ", errorMsg=" + compileMessage.getErrorMessage());
                executeCodeResponse.setStatus(ExecuteCodeStatusEnum.COMPILE_ERROR.getValue());
                String errorMessage = StrUtil.isNotBlank(compileMessage.getErrorMessage()) ? compileMessage.getErrorMessage() : "Compilation failed";
                executeCodeResponse.setMessage("Compilation Error: " + errorMessage);
                judgeInfo.setMessage("Compilation Error: " + errorMessage);
                cleanupFiles(userCodeParentPath);
                return executeCodeResponse;
            }

            // Execute code inside Docker container
            List<String> outputList = new ArrayList<>();
            long maxTime = 0L;

            for (String inputArgs : inputList) {
                String[] runCmd = {"/app/" + GLOBAL_C_EXECUTABLE_NAME};
                System.out.println("[C] 运行命令: " + Arrays.toString(runCmd) + " 输入: " + inputArgs);
                // For C, input is typically passed via stdin, not as command line arguments directly to the compiled program for competitive programming scenarios.
                // The DockerService's executeCommandInContainer handles passing input via stdin if inputArgsList is provided and the command is set up to read from stdin.
                ExecuteMessage executeMessage = dockerService.executeCommandInContainer(
                        DOCKER_IMAGE,
                        userCodeParentPath,
                        Arrays.asList(inputArgs), // Pass single input string for stdin processing by DockerService
                        runCmd,
                        RUN_TIME_OUT
                );

                if (executeMessage.getExitValue() != 0 || StrUtil.isNotBlank(executeMessage.getErrorMessage())) {
                    System.out.println("[C] 运行失败: exit=" + executeMessage.getExitValue() + ", errorMsg=" + executeMessage.getErrorMessage() + ", output=" + executeMessage.getMessage());
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
                outputList.add(executeMessage.getMessage().trim()); // Trim to remove potential trailing newlines
                if (executeMessage.getTime() != null) {
                    maxTime = Math.max(maxTime, executeMessage.getTime());
                }
            }

            executeCodeResponse.setOutputList(outputList);
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SUCCEED.getValue());
            executeCodeResponse.setMessage("OK");
            judgeInfo.setMessage("OK");
            judgeInfo.setTime(maxTime);
            judgeInfo.setMemory(0L); // Memory usage not accurately tracked by this basic setup; DockerService might provide it

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
            cleanupFiles(userCodeParentPath);
        }
        return executeCodeResponse;
    }

    private void cleanupFiles(String path) {
        if (FileUtil.exist(path)) {
            boolean del = FileUtil.del(path);
            System.out.println("删除 " + path + (del ? " 成功" : " 失败"));
        }
    }
}