package com.cs.ojcodesandbox.strategy;

import cn.hutool.core.util.StrUtil;
import com.cs.ojcodesandbox.CodeSandboxStrategy;
// import com.cs.ojcodesandbox.PythonNativeCodeSandbox; // Replaced by DockerService
import com.cs.ojcodesandbox.service.DockerService;
import com.cs.ojcodesandbox.model.ExecuteMessage; // Changed from ExecuteResult
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
// import java.util.stream.Collectors; // Not strictly needed with new logic
import com.cs.ojcodesandbox.model.ExecuteCodeRequest;
import com.cs.ojcodesandbox.model.ExecuteCodeResponse;
import com.cs.ojcodesandbox.model.JudgeInfo;
import com.cs.ojcodesandbox.model.enums.ExecuteCodeStatusEnum;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("python") // Bean name will be "python"
@ConditionalOnProperty(name = "docker.available", havingValue = "true", matchIfMissing = false)
public class PythonLanguageStrategy implements CodeSandboxStrategy {

    @Resource
    private DockerService dockerService;

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_PYTHON_FILE_NAME = "main.py";
    private static final String DOCKER_IMAGE = "python-sandbox:latest"; // Use custom built Python image
    private static final long RUN_TIME_OUT = 30000L; // Increased timeout for Python execution to avoid socket issues

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

        String userCodeParentPath = globalCodePathName + File.separator + IdUtil.simpleUUID();
        String userCodeFilePath = userCodeParentPath + File.separator + GLOBAL_PYTHON_FILE_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodeFilePath, StandardCharsets.UTF_8);

        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        JudgeInfo judgeInfo = new JudgeInfo();
        executeCodeResponse.setJudgeInfo(judgeInfo);

        try {
            // Python不需要显式编译步骤，直接执行
            // 2. 执行代码 (在 Docker 容器内)
            List<String> outputList = new ArrayList<>();
            long maxTime = 0L;

            for (String inputArgs : inputList) {
                String[] runCmd = {"python3", "/app/" + GLOBAL_PYTHON_FILE_NAME};
                System.out.println("[Python] 运行命令: " + Arrays.toString(runCmd) + " 输入: " + inputArgs);
                
                // Ensure input ends with newline for proper Python input() handling
                String processedInput = inputArgs;
                if (!processedInput.isEmpty() && !processedInput.endsWith("\n")) {
                    processedInput += "\n";
                }
                
                ExecuteMessage executeMessage = dockerService.executeCommandInContainer(
                        DOCKER_IMAGE,
                        userCodeParentPath,
                        processedInput.isEmpty() ? new ArrayList<>() : Arrays.asList(processedInput),
                        runCmd,
                        RUN_TIME_OUT
                );

                if (executeMessage.getExitValue() != 0 || StrUtil.isNotBlank(executeMessage.getErrorMessage())) {
                    System.out.println("[Python] 运行结果: exit=" + executeMessage.getExitValue() + ", errorMsg=" + executeMessage.getErrorMessage() + ", output=" + executeMessage.getMessage());
                    int exitValue = executeMessage.getExitValue() == null ? 0 : executeMessage.getExitValue();
                    boolean isStreamError = exitValue == -5 || exitValue == -6 || exitValue == -7 || exitValue == -8;
                    boolean hasOutput = executeMessage.getMessage() != null && !executeMessage.getMessage().trim().isEmpty();
                    boolean hasNoErrorMsg = executeMessage.getErrorMessage() == null || executeMessage.getErrorMessage().trim().isEmpty();
                    
                    // For stream errors with valid output, treat as success
                    if (isStreamError && hasOutput) {
                        System.out.println("[Python] Stream error detected but has valid output, treating as success");
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
                        String errorMessage = cn.hutool.core.util.StrUtil.isNotBlank(executeMessage.getErrorMessage()) ? executeMessage.getErrorMessage() : "Runtime Error";
                        executeCodeResponse.setMessage(errorMessage);
                        judgeInfo.setMessage(errorMessage);
                        System.err.println("Python Execution Failed. Error Details: " + executeMessage.getErrorMessage());
                        System.err.println("Python Execution Stdout: " + executeMessage.getMessage());
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
            System.out.println("[Python] 执行被中断: " + e.getMessage());
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR.getValue());
            executeCodeResponse.setMessage("Execution interrupted: " + e.getMessage());
            judgeInfo.setMessage("Execution interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.println("[Python] 执行异常: " + e.getMessage());
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
}