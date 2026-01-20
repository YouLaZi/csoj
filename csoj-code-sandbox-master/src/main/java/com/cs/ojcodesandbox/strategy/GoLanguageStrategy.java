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


@Component("go")
@ConditionalOnProperty(name = "docker.available", havingValue = "true", matchIfMissing = false)
public class GoLanguageStrategy implements CodeSandboxStrategy {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_GO_FILE_NAME = "main.go";
    private static final String GLOBAL_GO_EXECUTABLE_NAME = "main"; // Docker typically runs Linux, no .exe
    private static final String DOCKER_IMAGE = "golang:latest"; // Or your preferred Go image
    private static final long COMPILE_TIME_OUT = 15000L;
    private static final long RUN_TIME_OUT = 10000L;

    @Resource
    private DockerService dockerService;

    // Compiler availability check is implicitly handled by Docker execution

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("[Go] 开始执行Go代码");
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();

        // 初始化响应结果
        JudgeInfo judgeInfo = new JudgeInfo();
        executeCodeResponse.setJudgeInfo(judgeInfo);

        // 1. 把用户的代码保存为文件
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        // 检查全局代码目录是否存在，不存在则创建
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }

        // 用户代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_GO_FILE_NAME;
        FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);

        // 2. 编译代码
        try {
            System.out.println("[Go] 开始编译代码");
            
            // 首先初始化go module (使用sh -c来确保在正确的工作目录)
            String[] initCmd = {"sh", "-c", "cd /app && go mod init usercode"};
            ExecuteMessage initExecuteMessage = dockerService.executeCommandInContainer(DOCKER_IMAGE, userCodeParentPath, null, initCmd, COMPILE_TIME_OUT);
            System.out.println("[Go] go mod init结果: " + initExecuteMessage);
            
            // 执行go mod tidy来处理依赖
            String[] tidyCmd = {"sh", "-c", "cd /app && go mod tidy"};
            ExecuteMessage tidyExecuteMessage = dockerService.executeCommandInContainer(DOCKER_IMAGE, userCodeParentPath, null, tidyCmd, COMPILE_TIME_OUT);
            System.out.println("[Go] go mod tidy结果: " + tidyExecuteMessage);
            
            // 然后编译代码
            String[] buildCmd = {"sh", "-c", "cd /app && go build -o " + GLOBAL_GO_EXECUTABLE_NAME + " " + GLOBAL_GO_FILE_NAME};
            ExecuteMessage compileExecuteMessage = dockerService.executeCommandInContainer(DOCKER_IMAGE, userCodeParentPath, null, buildCmd, COMPILE_TIME_OUT);
            System.out.println("[Go] 编译结果: " + compileExecuteMessage);

            // 编译错误处理
            if (compileExecuteMessage.getExitValue() != 0) {
                executeCodeResponse.setStatus(ExecuteCodeStatusEnum.COMPILE_ERROR.getValue());
                executeCodeResponse.setMessage(compileExecuteMessage.getErrorMessage());
                // 编译失败，直接返回
                return executeCodeResponse;
            }
        } catch (Exception e) {
            System.out.println("[Go] 编译异常: " + e.getMessage());
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR.getValue());
            executeCodeResponse.setMessage("Go 编译错误: " + e.getMessage());
            cleanupFiles(userCodeParentPath);
            return executeCodeResponse;
        }

        // 3. 执行代码 (在 Docker 容器内)
        try {
            System.out.println("[Go] 开始执行代码");
            List<String> outputList = new ArrayList<>();
            long maxTime = 0L;
            boolean hasOutput = false;

            for (String inputArgs : inputList) {
                String[] runCmd = {"/app/" + GLOBAL_GO_EXECUTABLE_NAME};
                System.out.println("[Go] 运行命令: " + Arrays.toString(runCmd) + " 输入: " + inputArgs);
                ExecuteMessage executeMessage = dockerService.executeCommandInContainer(
                        DOCKER_IMAGE,
                        userCodeParentPath,
                        Arrays.asList(inputArgs.split("\\s+")), // Pass input via stdin
                        runCmd,
                        RUN_TIME_OUT
                );

                System.out.println("[Go] 执行结果: exitValue=" + executeMessage.getExitValue() + 
                                 ", message=" + executeMessage.getMessage() + 
                                 ", errorMessage=" + executeMessage.getErrorMessage());

                // 直接检查是否有输出，如果有输出则视为成功执行
                boolean currentHasOutput = executeMessage.getMessage() != null && !executeMessage.getMessage().trim().isEmpty();
                boolean isStreamError = executeMessage.getExitValue() != null && 
                                      (executeMessage.getExitValue() == -5 || 
                                       executeMessage.getExitValue() == -6 || 
                                       executeMessage.getExitValue() == -7 || 
                                       executeMessage.getExitValue() == -8);
                boolean isEOFError = executeMessage.getErrorMessage() != null && 
                                    (executeMessage.getErrorMessage().contains("EOFException") || 
                                     executeMessage.getErrorMessage().contains("ClosedChannelException") ||
                                     executeMessage.getErrorMessage().contains("Callback Error: null"));
                    
                // 如果有输出，则视为成功执行
                if (currentHasOutput) {
                    System.out.println("[Go] 检测到有输出，视为成功执行: " + executeMessage.getMessage().trim());
                    outputList.add(executeMessage.getMessage().trim());
                    hasOutput = true;
                    if (executeMessage.getTime() != null) {
                        maxTime = Math.max(maxTime, executeMessage.getTime());
                    }
                    continue;
                }
                    
                // 如果没有输出，但是有流错误，也可能是成功执行但输出被截断
                if ((isStreamError || isEOFError) && executeMessage.getExitValue() != null) {
                    System.out.println("[Go] 检测到流错误但无输出，添加空输出并继续: " + executeMessage.getErrorMessage());
                    outputList.add(""); // 添加空输出
                    if (executeMessage.getTime() != null) {
                        maxTime = Math.max(maxTime, executeMessage.getTime());
                    }
                    continue;
                }

                // 处理真正的错误情况
                if (executeMessage.getExitValue() != 0 || StrUtil.isNotBlank(executeMessage.getErrorMessage())) {
                    System.out.println("[Go] 运行失败: exit=" + executeMessage.getExitValue() + ", errorMsg=" + executeMessage.getErrorMessage() + ", output=" + executeMessage.getMessage());
                    
                    // 即使有错误也继续处理，不立即返回
                    if (executeMessage.getMessage() != null && !executeMessage.getMessage().trim().isEmpty()) {
                        outputList.add(executeMessage.getMessage().trim());
                        hasOutput = true;
                    } else {
                        outputList.add(""); // 添加空输出以保持一致性
                    }
                    
                    if (executeMessage.getTime() != null) {
                        maxTime = Math.max(maxTime, executeMessage.getTime());
                    }
                    continue;
                }
                
                outputList.add(executeMessage.getMessage().trim());
                hasOutput = true;
                if (executeMessage.getTime() != null) {
                    maxTime = Math.max(maxTime, executeMessage.getTime());
                }
            }

            // 修改：不再无条件地将状态设置为成功，而是根据实际执行情况进行判断
            executeCodeResponse.setOutputList(outputList);
            
            // 判断是否存在真正的错误
            boolean hasError = false;
            for (String output : outputList) {
                if (output == null || output.isEmpty()) {
                    hasError = true;
                    break;
                }
            }
            
            // 根据执行情况设置状态
            if (hasOutput && !hasError) {
                executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SUCCEED.getValue());
                executeCodeResponse.setMessage("OK");
                judgeInfo.setMessage("OK");
                System.out.println("[Go] 执行完成，所有测试用例均有输出，设置状态为SUCCEED(3): " + ExecuteCodeStatusEnum.SUCCEED.getValue());
            } else {
                executeCodeResponse.setStatus(ExecuteCodeStatusEnum.FAILED.getValue());
                executeCodeResponse.setMessage("部分或全部测试用例执行失败");
                judgeInfo.setMessage("部分或全部测试用例执行失败");
                System.out.println("[Go] 执行完成，存在失败的测试用例，设置状态为FAILED(4): " + ExecuteCodeStatusEnum.FAILED.getValue());
            }
            
            judgeInfo.setTime(maxTime);
            judgeInfo.setMemory(0L); // Memory usage not tracked here
            
            // 清理临时文件
            cleanupFiles(userCodeParentPath);
            return executeCodeResponse;
        } catch (Exception e) {
            System.out.println("[Go] 执行异常: " + e.getMessage());
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR.getValue());
            executeCodeResponse.setMessage("Go 执行错误: " + e.getMessage());
            cleanupFiles(userCodeParentPath);
            return executeCodeResponse;
        }
    }

    private void cleanupFiles(String path) {
        if (FileUtil.exist(path)) {
            boolean del = FileUtil.del(path);
            System.out.println("删除 " + path + (del ? " 成功" : " 失败"));
        }
    }
}