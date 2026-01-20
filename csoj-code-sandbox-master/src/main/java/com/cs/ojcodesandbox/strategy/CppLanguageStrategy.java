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

@Component("cpp")
@ConditionalOnProperty(name = "docker.available", havingValue = "true", matchIfMissing = false)
public class CppLanguageStrategy implements CodeSandboxStrategy {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_CPP_CLASS_NAME = "Main.cpp"; // This is the source file name
    private static final String GLOBAL_EXECUTABLE_NAME = "Main"; // Name of the compiled executable
    private static final String DOCKER_IMAGE = "gcc:latest"; // Docker image for C++
    private static final long COMPILE_TIME_OUT = 10000L; // Timeout for compilation
    private static final long RUN_TIME_OUT = 5000L; // Timeout for execution

    private static final long TIME_OUT = 10000L; // This seems to be a general timeout, let's keep RUN_TIME_OUT for clarity in execution

    @Resource
    private DockerService dockerService; // Using Resource annotation for dependency injection

    /**
     * 检查编译器是否可用
     * @return 如果编译器可用返回true，否则返回false
     */
    private boolean isCompilerAvailable() {
        try {
            Process process = Runtime.getRuntime().exec("g++ --version");
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
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
        String userCodeFilePath = userCodeParentPath + File.separator + GLOBAL_CPP_CLASS_NAME; // Use defined constant
        FileUtil.writeString(code, userCodeFilePath, StandardCharsets.UTF_8);

        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        JudgeInfo judgeInfo = new JudgeInfo();
        executeCodeResponse.setJudgeInfo(judgeInfo);

        try {
            // 2. 编译代码 (在 Docker 容器内)
            // g++ /app/Main.cpp -o /app/Main -std=c++11
            String[] compileCmd = {"g++", "/app/" + GLOBAL_CPP_CLASS_NAME, "-o", "/app/" + GLOBAL_EXECUTABLE_NAME, "-std=c++11"};
            System.out.println("[C++] 编译命令: " + Arrays.toString(compileCmd));
            ExecuteMessage compileMessage = dockerService.executeCommandInContainer(
                    DOCKER_IMAGE,
                    userCodeParentPath,
                    new ArrayList<>(), // No input for compilation
                    compileCmd,
                    COMPILE_TIME_OUT
            );

            if (compileMessage.getExitValue() != 0 || StrUtil.isNotBlank(compileMessage.getErrorMessage())) {
                System.err.println("[C++] 编译失败: exit=" + compileMessage.getExitValue() + ", errorMsg=" + compileMessage.getErrorMessage());
                System.err.println("[C++] 编译输出: " + compileMessage.getMessage());
                executeCodeResponse.setStatus(ExecuteCodeStatusEnum.COMPILE_ERROR.getValue());
                String errorMessage = StrUtil.isNotBlank(compileMessage.getErrorMessage()) ? compileMessage.getErrorMessage() : "Compilation failed";
                executeCodeResponse.setMessage("Compilation Error: " + errorMessage);
                judgeInfo.setMessage("Compilation Error: " + errorMessage);
                System.err.println("C++ Compilation Failed. Error Details: " + compileMessage.getErrorMessage()); // 详细错误日志
                System.err.println("C++ Compilation Stdout: " + compileMessage.getMessage()); // 编译时的标准输出（如果有）
                cleanupFiles(userCodeParentPath);
                return executeCodeResponse;
            }

            // 3. 执行代码 (在 Docker 容器内)
            List<String> outputList = new ArrayList<>();
            long maxTime = 0L;

            for (String inputArgs : inputList) {
                String[] runCmd = {"/app/" + GLOBAL_EXECUTABLE_NAME}; // Use defined constant
                ExecuteMessage executeMessage = dockerService.executeCommandInContainer(
                        DOCKER_IMAGE,
                        userCodeParentPath,
                        Arrays.asList(inputArgs.split("\\s+")), // Pass input via stdin
                        runCmd,
                        RUN_TIME_OUT // Use defined constant
                );

                if (executeMessage.getExitValue() != 0 || StrUtil.isNotBlank(executeMessage.getErrorMessage())) {
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
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR.getValue());
            executeCodeResponse.setMessage("Execution interrupted: " + e.getMessage());
            judgeInfo.setMessage("Execution interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR.getValue());
            executeCodeResponse.setMessage("Execution failed: " + e.getMessage());
            judgeInfo.setMessage("Execution failed: " + e.getMessage());
        } finally {
            // 4. 清理文件
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