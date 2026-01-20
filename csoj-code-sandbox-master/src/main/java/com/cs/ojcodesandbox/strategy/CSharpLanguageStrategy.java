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
@Component("csharp")
@ConditionalOnProperty(name = "docker.available", havingValue = "true", matchIfMissing = false) // Temporarily comment out to allow Docker usage
public class CSharpLanguageStrategy implements CodeSandboxStrategy {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_CSHARP_FILE_NAME = "Main.cs";
    private static final String GLOBAL_CSHARP_PROJECT_NAME = "UserCode.csproj";
    private static final String CONTAINER_PUBLISH_DIR_NAME = "publish_output";
    private static final String OUTPUT_DLL_NAME = "UserCode.dll";
    private static final String DOCKER_IMAGE = "mcr.microsoft.com/dotnet/sdk:latest"; // Example .NET SDK image
    private static final long TIME_OUT = 10000L;

    @Resource
    private DockerService dockerService;

    /**
     * 检查C#编译器是否可用
     * @return 如果编译器可用返回true，否则返回false
     */
    // private boolean isCompilerAvailable() { // Replaced by Docker execution
    //     try {
    //         Process process = Runtime.getRuntime().exec("csc -version");
    //         int exitCode = process.waitFor();
    //         return exitCode == 0;
    //     } catch (Exception e) {
    //         return false;
    //     }
    // }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        try {
            dockerService.pullImage(DOCKER_IMAGE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR.getValue());
            executeCodeResponse.setMessage("拉取 C# Docker 镜像被中断: " + e.getMessage());
            JudgeInfo judgeInfo = new JudgeInfo();
            judgeInfo.setMessage("拉取 C# Docker 镜像被中断: " + e.getMessage());
            executeCodeResponse.setJudgeInfo(judgeInfo);
            return executeCodeResponse;
        } catch (RuntimeException e) {
            // Catching RuntimeException specifically for pullImage failures that might not be InterruptedException
            ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR.getValue());
            executeCodeResponse.setMessage("拉取 C# Docker 镜像失败: " + e.getMessage());
            JudgeInfo judgeInfo = new JudgeInfo();
            judgeInfo.setMessage("拉取 C# Docker 镜像失败: " + e.getMessage());
            executeCodeResponse.setJudgeInfo(judgeInfo);
            return executeCodeResponse;
        }

        // // 首先检查编译器是否可用 // Replaced by Docker execution
        // if (!isCompilerAvailable()) {
        //     ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        //     executeCodeResponse.setMessage("系统错误: C#编译器(csc)不可用，请确保已安装.NET Framework或.NET Core环境");
        //     executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR.getValue());
        //     executeCodeResponse.setJudgeInfo(new JudgeInfo());
        //     executeCodeResponse.getJudgeInfo().setMessage("C#编译器(csc)不可用，请确保已安装.NET Framework或.NET Core环境");
        //     return executeCodeResponse;
        // }

        String code = executeCodeRequest.getCode();
        List<String> inputList = executeCodeRequest.getInputList();

        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;

        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }

        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodeFilePath = userCodeParentPath + File.separator + GLOBAL_CSHARP_FILE_NAME;
        FileUtil.writeString(code, userCodeFilePath, StandardCharsets.UTF_8);

        // Create a minimal .csproj file
        String csprojFilePath = userCodeParentPath + File.separator + GLOBAL_CSHARP_PROJECT_NAME;
        String csprojContent = "<Project Sdk=\"Microsoft.NET.Sdk\">\n" +
                "  <PropertyGroup>\n" +
                "    <OutputType>Exe</OutputType>\n" +
                "    <TargetFramework>net6.0</TargetFramework>\n" +
                "    <AssemblyName>UserCode</AssemblyName>\n" +
                "    <ImplicitUsings>enable</ImplicitUsings>\n" +
                "    <Nullable>enable</Nullable>\n" +
                "    <RootNamespace>UserCode</RootNamespace>\n" +
                "  </PropertyGroup>\n" +
                "</Project>";
        FileUtil.writeString(csprojContent, csprojFilePath, StandardCharsets.UTF_8);

        // Execute C# code in Docker
        // For .NET Core, we can use `dotnet run` within a project structure or compile and run.
        // A simpler approach for a single file might be to copy it into the container and use `dotnet script` or compile then run.
        // Let's try compiling and running.

        // Define Docker commands
        // Note: .NET SDK images typically have `/app` as a working directory.
        // Define paths in the container. userCodeParentPath is mounted to /app.
        String containerProjectFilePath = "/app/" + GLOBAL_CSHARP_PROJECT_NAME;
        String containerPublishDir = "/app/" + CONTAINER_PUBLISH_DIR_NAME;
        String containerDllPath = containerPublishDir + "/" + OUTPUT_DLL_NAME;

        // 首先执行dotnet restore来还原NuGet包
        String restoreCmdInDocker = String.format("dotnet restore %s", containerProjectFilePath);
        System.out.println("[C#] 还原命令: " + restoreCmdInDocker);
        
        ExecuteMessage restoreMessage;
        try {
            restoreMessage = dockerService.executeCommandInContainer(
                    DOCKER_IMAGE,
                    userCodeParentPath,
                    null,
                    restoreCmdInDocker.split(" "),
                    TIME_OUT
            );
            System.out.println("[C#] 还原结果: exit=" + restoreMessage.getExitValue() + ", errorMsg=" + restoreMessage.getErrorMessage() + ", output=" + restoreMessage.getMessage());
            
            // 检查NuGet包还原是否失败
            String restoreOutput = restoreMessage.getMessage() != null ? restoreMessage.getMessage() : "";
            String restoreError = restoreMessage.getErrorMessage() != null ? restoreMessage.getErrorMessage() : "";
            
            // 检查是否包含NuGet错误信息
            if (restoreOutput.contains("error NU") || restoreError.contains("error NU") || 
                restoreOutput.contains("Failed to restore") || restoreError.contains("Failed to restore")) {
                System.out.println("[C#] NuGet包还原失败");
                ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
                executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR.getValue());
                executeCodeResponse.setMessage("C# NuGet包还原失败: " + (restoreOutput.contains("error NU") ? restoreOutput : restoreError));
                JudgeInfo judgeInfo = new JudgeInfo();
                judgeInfo.setMessage("C# NuGet包还原失败，请检查网络连接或NuGet配置");
                executeCodeResponse.setJudgeInfo(judgeInfo);
                FileUtil.del(userCodeParentPath);
                return executeCodeResponse;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[C#] 还原被中断: " + e.getMessage());
            ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR.getValue());
            executeCodeResponse.setMessage("C# 还原被中断: " + e.getMessage());
            JudgeInfo judgeInfo = new JudgeInfo();
            judgeInfo.setMessage("C# 还原被中断: " + e.getMessage());
            executeCodeResponse.setJudgeInfo(judgeInfo);
            FileUtil.del(userCodeParentPath);
            return executeCodeResponse;
        }
        
        // Command to compile C# code inside Docker using dotnet publish
        String compileCmdInDocker = String.format("dotnet publish %s -c Release -o %s -r linux-x64 --no-self-contained", 
                                                containerProjectFilePath, containerPublishDir);
        System.out.println("[C#] 编译命令: " + compileCmdInDocker);


        // For compilation, inputArgsList is usually empty or null
        ExecuteMessage compileMessage;
        try {
            compileMessage = dockerService.executeCommandInContainer(
                    DOCKER_IMAGE,
                    userCodeParentPath, // Host path to be mounted
                    null, // No stdin for compilation typically
                    compileCmdInDocker.split(" "), // Command and its arguments
                    TIME_OUT
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Preserve interrupt status
            System.out.println("[C#] 编译被中断: " + e.getMessage());
            ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR.getValue());
            executeCodeResponse.setMessage("C# 编译被中断: " + e.getMessage());
            JudgeInfo judgeInfo = new JudgeInfo();
            judgeInfo.setMessage("C# 编译被中断: " + e.getMessage());
            executeCodeResponse.setJudgeInfo(judgeInfo);
            FileUtil.del(userCodeParentPath);
            return executeCodeResponse;
        }
        System.out.println("[C#] 编译结果: exit=" + compileMessage.getExitValue() + ", errorMsg=" + compileMessage.getErrorMessage() + ", output=" + compileMessage.getMessage());

        // Check for compilation failure
        boolean compilationFailed = compileMessage.getExitValue() != 0;
        if (!compilationFailed) {
            String compileStdOut = compileMessage.getMessage() != null ? compileMessage.getMessage() : "";
            String compileStdErr = compileMessage.getErrorMessage() != null ? compileMessage.getErrorMessage() : "";
            // Check for common failure indicators from dotnet publish
            if (compileStdOut.contains("Build FAILED.") || compileStdErr.contains("error CS") || 
                compileStdErr.contains("Build FAILED.") || compileStdOut.toLowerCase().contains("unhandled exception") || 
                compileStdErr.toLowerCase().contains("unhandled exception")) {
                compilationFailed = true;
                // If exit code was 0 but output indicates failure, ensure errorMessage is populated
                if (StrUtil.isBlank(compileMessage.getErrorMessage())) {
                    String failureDetails = compileStdOut.contains("Build FAILED.") ? compileStdOut : compileStdErr;
                    if (StrUtil.isBlank(failureDetails) && compileStdOut.toLowerCase().contains("unhandled exception")) {
                        failureDetails = compileStdOut;
                    } else if (StrUtil.isBlank(failureDetails) && compileStdErr.toLowerCase().contains("unhandled exception")) {
                        failureDetails = compileStdErr;
                    }
                    compileMessage.setErrorMessage(StrUtil.isNotBlank(failureDetails) ? failureDetails : "Compilation failed based on output analysis.");
                }
            }
        }

        if (compilationFailed) {
            // Ensure a meaningful error message is passed to handleCompilationError
            if (StrUtil.isBlank(compileMessage.getErrorMessage()) && StrUtil.isNotBlank(compileMessage.getMessage())) {
                 compileMessage.setErrorMessage("Compilation failed. Output: " + compileMessage.getMessage());
            } else if (StrUtil.isBlank(compileMessage.getErrorMessage())) {
                compileMessage.setErrorMessage("Compilation failed with exit code " + compileMessage.getExitValue() + ". Check logs for details.");
            }
            ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.COMPILE_ERROR.getValue());
            executeCodeResponse.setMessage("编译错误: " + compileMessage.getErrorMessage());
            JudgeInfo judgeInfo = new JudgeInfo();
            judgeInfo.setMessage(compileMessage.getErrorMessage());
            executeCodeResponse.setJudgeInfo(judgeInfo);
            FileUtil.del(userCodeParentPath);
            return executeCodeResponse;
        }

        // 检查DLL文件是否存在
        String checkDllCmdInDocker = String.format("ls -la %s", containerPublishDir);
        ExecuteMessage checkDllMessage;
        try {
            checkDllMessage = dockerService.executeCommandInContainer(
                    DOCKER_IMAGE,
                    userCodeParentPath,
                    null,
                    checkDllCmdInDocker.split(" "),
                    TIME_OUT
            );
            System.out.println("[C#] 检查DLL文件: " + checkDllMessage.getMessage());
            
            // 检查输出中是否包含DLL文件
            String checkOutput = checkDllMessage.getMessage() != null ? checkDllMessage.getMessage() : "";
            if (!checkOutput.contains(OUTPUT_DLL_NAME)) {
                System.out.println("[C#] DLL文件未生成");
                ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
                executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR.getValue());
                executeCodeResponse.setMessage("C# DLL文件未生成，编译可能失败: " + checkOutput);
                JudgeInfo judgeInfo = new JudgeInfo();
                judgeInfo.setMessage("C# DLL文件未生成，请检查编译过程");
                executeCodeResponse.setJudgeInfo(judgeInfo);
                FileUtil.del(userCodeParentPath);
                return executeCodeResponse;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[C#] 检查DLL文件被中断: " + e.getMessage());
            ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR.getValue());
            executeCodeResponse.setMessage("C# 检查DLL文件被中断: " + e.getMessage());
            JudgeInfo judgeInfo = new JudgeInfo();
            judgeInfo.setMessage("C# 检查DLL文件被中断");
            executeCodeResponse.setJudgeInfo(judgeInfo);
            FileUtil.del(userCodeParentPath);
            return executeCodeResponse;
        }

        // Execute compiled C# code in Docker
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            // Command to run the compiled C# DLL inside Docker
            String runCmdInDocker = String.format("dotnet %s", containerDllPath);
            System.out.println("[C#] 运行命令: " + runCmdInDocker + " 输入: " + inputArgs);
            // For execution, inputArgs is passed as a list containing one string for stdin
            ExecuteMessage executeMessage;
            try {
                executeMessage = dockerService.executeCommandInContainer(
                        DOCKER_IMAGE,
                        userCodeParentPath, // Host path to be mounted
                        Arrays.asList(inputArgs), // Input for stdin
                        runCmdInDocker.split(" "), // Command and its arguments
                        TIME_OUT
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Preserve interrupt status
                System.out.println("[C#] 执行被中断: " + e.getMessage());
                ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
                executeCodeResponse.setStatus(ExecuteCodeStatusEnum.FAILED.getValue());
                executeCodeResponse.setMessage("C# 执行被中断: " + e.getMessage());
                JudgeInfo judgeInfo = new JudgeInfo();
                judgeInfo.setMessage("C# 执行被中断: " + e.getMessage());
                executeCodeResponse.setJudgeInfo(judgeInfo);
                FileUtil.del(userCodeParentPath);
                return executeCodeResponse;
            }
            System.out.println("[C#] 运行结果: exit=" + executeMessage.getExitValue() + ", errorMsg=" + executeMessage.getErrorMessage() + ", output=" + executeMessage.getMessage());
            executeMessageList.add(executeMessage);
            // Check for runtime errors immediately after each execution
            if (executeMessage.getExitValue() != 0 && StrUtil.isNotBlank(executeMessage.getErrorMessage())) {
                // If there's a runtime error, no need to process further inputs for this request
                // The processExecutionResults will handle formatting the response based on the first error encountered.
                break; 
            }
        }

        // Clean up files
        // FileUtil.del(userCodeParentPath); // Keep this commented out for now for debugging

        return processExecutionResults(executeMessageList, userCodeParentPath);
    }



    /**
     * 处理执行结果
     */
    private ExecuteCodeResponse processExecutionResults(List<ExecuteMessage> executeMessageList, String userCodeParentPath) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        // 取用时最大值，便于判断是否超时
        long maxTime = 0;
        for (ExecuteMessage executeMessage : executeMessageList) {
            String errorMessage = executeMessage.getErrorMessage();
            int exitValue = executeMessage.getExitValue() == null ? 0 : executeMessage.getExitValue();
            // 针对流异常（-5/-6/-7/-8）且有输出且无errorMessage时，判定为SUCCEED，否则FAILED
            if (exitValue != 0) {
                boolean isStreamError = exitValue == -5 || exitValue == -6 || exitValue == -7 || exitValue == -8;
                boolean hasOutput = executeMessage.getMessage() != null && !executeMessage.getMessage().trim().isEmpty();
                boolean hasNoErrorMsg = errorMessage == null || errorMessage.trim().isEmpty();
                if (isStreamError && hasOutput && hasNoErrorMsg) {
                    // 视为成功
                    outputList.add(executeMessage.getMessage());
                    if (executeMessage.getTime() != null) {
                        maxTime = Math.max(maxTime, executeMessage.getTime());
                    }
                    continue;
                } else {
                    executeCodeResponse.setStatus(ExecuteCodeStatusEnum.FAILED.getValue());
                    executeCodeResponse.setMessage(errorMessage);
                    JudgeInfo judgeInfo = new JudgeInfo();
                    judgeInfo.setMessage(errorMessage);
                    executeCodeResponse.setJudgeInfo(judgeInfo);
                    // 清理文件
                    FileUtil.del(userCodeParentPath);
                    return executeCodeResponse;
                }
            }
            outputList.add(executeMessage.getMessage());
            if (executeMessage.getTime() != null) {
                maxTime = Math.max(maxTime, executeMessage.getTime());
            }
        }
        // 正常执行完成
        executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SUCCEED.getValue());
        executeCodeResponse.setMessage("执行成功");
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        judgeInfo.setMemory(0L); // 内存占用暂时为 0
        executeCodeResponse.setJudgeInfo(judgeInfo);
        // 清理文件
        FileUtil.del(userCodeParentPath);
        return executeCodeResponse;
    }
}