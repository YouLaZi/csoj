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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Component("java")
@ConditionalOnProperty(name = "docker.available", havingValue = "true", matchIfMissing = false)
public class JavaLanguageStrategy implements CodeSandboxStrategy {

    @Resource
    private DockerService dockerService;

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";
    private static final String DOCKER_IMAGE = "openjdk:8-jdk-alpine";
    private static final long COMPILE_TIME_OUT = 10000L;
    private static final long RUN_TIME_OUT = 10000L;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        // 首先拉取Docker镜像
        try {
            dockerService.pullImage(DOCKER_IMAGE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR.getValue());
            executeCodeResponse.setMessage("拉取 Java Docker 镜像被中断: " + e.getMessage());
            JudgeInfo judgeInfo = new JudgeInfo();
            judgeInfo.setMessage("拉取 Java Docker 镜像被中断: " + e.getMessage());
            executeCodeResponse.setJudgeInfo(judgeInfo);
            return executeCodeResponse;
        } catch (RuntimeException e) {
            ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR.getValue());
            executeCodeResponse.setMessage("拉取 Java Docker 镜像失败: " + e.getMessage());
            JudgeInfo judgeInfo = new JudgeInfo();
            judgeInfo.setMessage("拉取 Java Docker 镜像失败: " + e.getMessage());
            executeCodeResponse.setJudgeInfo(judgeInfo);
            return executeCodeResponse;
        }

        List<String> inputList = executeCodeRequest.getInputList();
        String originalCode = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        
        // 预处理用户代码，自动添加类定义和导入语句
        String code = preprocessJavaCode(originalCode);

        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        JudgeInfo judgeInfo = new JudgeInfo();
        executeCodeResponse.setJudgeInfo(judgeInfo);
        
        String userCodeParentPath = null;
        try {
            // 1. 保存用户代码到文件
            String userDir = System.getProperty("user.dir");
            String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
            // 判断全局代码目录是否存在，没有则新建
            if (!FileUtil.exist(globalCodePathName)) {
                FileUtil.mkdir(globalCodePathName);
            }
            
            // 把用户的代码隔离存放
            userCodeParentPath = globalCodePathName + File.separator + IdUtil.simpleUUID();
            String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
            File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
            System.out.println("用户代码保存路径: " + userCodeParentPath);
            // 2. 编译代码 (在 Docker 容器内)
            String[] compileCmd = {"javac", "-encoding", "UTF-8", "/app/" + GLOBAL_JAVA_CLASS_NAME};
            System.out.println("[Java] 编译命令: " + Arrays.toString(compileCmd));
            ExecuteMessage compileMessage = dockerService.executeCommandInContainer(
                    DOCKER_IMAGE,
                    userCodeParentPath,
                    new ArrayList<>(), // No input for compilation
                    compileCmd,
                    COMPILE_TIME_OUT
            );

            if (compileMessage.getExitValue() != 0 || StrUtil.isNotBlank(compileMessage.getErrorMessage())) {
                System.out.println("[Java] 编译失败: exit=" + compileMessage.getExitValue() + ", errorMsg=" + compileMessage.getErrorMessage());
                executeCodeResponse.setStatus(ExecuteCodeStatusEnum.COMPILE_ERROR.getValue());
                String errorMessage = StrUtil.isNotBlank(compileMessage.getErrorMessage()) ? compileMessage.getErrorMessage() : "Compilation failed";
                executeCodeResponse.setMessage("Compilation Error: " + errorMessage);
                judgeInfo.setMessage("Compilation Error: " + errorMessage);
                cleanupFiles(userCodeParentPath);
                return executeCodeResponse;
            }

            // 3. 执行代码 (在 Docker 容器内)
            List<String> outputList = new ArrayList<>();
            long maxTime = 0L;

            // 将所有输入合并为一个字符串，用换行符分隔，并在末尾添加换行符确保完整性
            String allInputs = String.join("\n", inputList);
            if (!allInputs.isEmpty()) {
                allInputs += "\n"; // 确保最后一行也有换行符
            }
            String[] runCmd = {"java", "-cp", "/app", "Main"};
            System.out.println("[Java] 运行命令: " + Arrays.toString(runCmd));
            System.out.println("[Java] 输入数据: [" + allInputs.replace("\n", "\\n") + "]");
            System.out.println("[Java] 输入字节长度: " + allInputs.getBytes().length);
            System.out.println("Executing Java code...");
            ExecuteMessage executeMessage = dockerService.executeCommandInContainer(
                    DOCKER_IMAGE,
                    userCodeParentPath,
                    Arrays.asList(allInputs), // Pass all inputs as single string via stdin
                    runCmd,
                    RUN_TIME_OUT
            );

            if (executeMessage.getExitValue() != 0 || StrUtil.isNotBlank(executeMessage.getErrorMessage())) {
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
            String output = executeMessage.getMessage().trim();
            outputList.add(output);
            
            // 增加输出结果提示
            System.out.println("[DEBUG] 测试用例输入: " + allInputs);
            System.out.println("[DEBUG] 代码执行输出: " + output);
            System.out.println("[DEBUG] 输出长度: " + output.length());
            
            if (executeMessage.getTime() != null) {
                maxTime = Math.max(maxTime, executeMessage.getTime());
            }



            executeCodeResponse.setOutputList(outputList);
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SUCCEED.getValue());
            executeCodeResponse.setMessage("OK");
            judgeInfo.setMessage("OK");
            judgeInfo.setTime(maxTime);
            judgeInfo.setMemory(0L); // Memory usage not tracked here
            
            // 输出所有测试用例的执行结果汇总
            System.out.println("[DEBUG] ===== 代码执行结果汇总 =====");
            System.out.println("[DEBUG] 总测试用例数: " + inputList.size());
            System.out.println("[DEBUG] 输出结果数: " + outputList.size());
            for (int i = 0; i < outputList.size(); i++) {
                System.out.println("[DEBUG] 测试用例" + (i+1) + "输出: " + outputList.get(i));
            }
            System.out.println("[DEBUG] =========================");

        } catch (InterruptedException e) {
            System.out.println("[Java] 执行被中断: " + e.getMessage());
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR.getValue());
            executeCodeResponse.setMessage("Execution interrupted: " + e.getMessage());
            judgeInfo.setMessage("Execution interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.println("[Java] 执行异常: " + e.getMessage());
            e.printStackTrace();
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR.getValue());
            executeCodeResponse.setMessage("Execution failed: " + e.getMessage());
            judgeInfo.setMessage("Execution failed: " + e.getMessage());
        } finally {
            // 4. 清理文件
            if (userCodeParentPath != null) {
                cleanupFiles(userCodeParentPath);
            }
        }
        return executeCodeResponse;
    }

    private void cleanupFiles(String path) {
        if (FileUtil.exist(path)) {
            boolean del = FileUtil.del(path);
            System.out.println("删除 " + path + (del ? " 成功" : " 失败"));
        }
    }
    
    /**
     * 简化的Java代码预处理
     * @param originalCode 用户原始代码
     * @return 处理后的完整Java代码
     */
    private String preprocessJavaCode(String originalCode) {
        if (StrUtil.isBlank(originalCode)) {
            return originalCode;
        }
        
        String trimmedCode = originalCode.trim();
        
        // 如果已经包含完整的类定义，只需要标准化类名
        if (trimmedCode.contains("class ")) {
            return normalizeClassName(trimmedCode);
        }
        
        // 否则包装为简单的Main类
        StringBuilder processedCode = new StringBuilder();
        processedCode.append("import java.util.*;\n");
        processedCode.append("import java.io.*;\n\n");
        processedCode.append("public class Main {\n");
        processedCode.append("    public static void main(String[] args) {\n");
        processedCode.append("        Scanner scanner = new Scanner(System.in);\n");
        
        // 缩进用户代码
        String[] lines = trimmedCode.split("\n");
        for (String line : lines) {
            processedCode.append("        ").append(line).append("\n");
        }
        
        processedCode.append("        scanner.close();\n");
        processedCode.append("    }\n");
        processedCode.append("}\n");
        
        return processedCode.toString();
    }
    

    
    /**
     * 简化的类名标准化
     * @param code 包含完整类定义的代码
     * @return 标准化后的代码
     */
    private String normalizeClassName(String code) {
        // 添加必要的导入语句
        if (!code.contains("import java.util")) {
            code = "import java.util.*;\nimport java.io.*;\n\n" + code;
        }
        
        // 替换类名为Main
        code = code.replaceAll("(public\\s+)?class\\s+\\w+", "public class Main");
        
        return code;
    }
    

}