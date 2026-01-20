package com.cs.ojcodesandbox;

import com.cs.ojcodesandbox.model.ExecuteCodeRequest;
import com.cs.ojcodesandbox.model.ExecuteCodeResponse;
import com.cs.ojcodesandbox.model.ExecuteMessage;
import com.cs.ojcodesandbox.utils.ProcessUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Java 原生代码沙箱实现（直接复用模板方法）
 */
@Component
public class JavaNativeCodeSandbox extends JavaCodeSandboxTemplate {

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }

    /**
     * 编译文件 - Java特定实现
     */
    @Override
    public ExecuteMessage compileFile(File userCodeFile, String language) {
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            if (executeMessage.getExitValue() != 0) {
                throw new RuntimeException("编译错误: " + executeMessage.getErrorMessage());
            }
            return executeMessage;
        } catch (Exception e) {
            throw new RuntimeException("编译失败", e);
        }
    }

    /**
     * 执行文件 - Java特定实现
     */
    @Override
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList, String language) {
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        List<ExecuteMessage> executeMessageList = new ArrayList<>();

        for (String inputArgs : inputList) {
            // 注意：这里的 Main 是硬编码的，与 MAIN_FILE_BASENAME 对应
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s %s %s", userCodeParentPath, MAIN_FILE_BASENAME, inputArgs);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                // 超时控制
                new Thread(() -> {
                    try {
                        Thread.sleep(TIME_OUT);
                        System.out.println("超时了，中断");
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                        // InterruptedException might be thrown if the thread is interrupted before timeout
                        // Or if the process is destroyed while sleeping
                        System.err.println("Timeout thread interrupted: " + e.getMessage());
                    }
                }).start();
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
                executeMessageList.add(executeMessage);
            } catch (Exception e) {
                // It's better to wrap the original exception for more context
                throw new RuntimeException("执行错误 for input: " + inputArgs, e);
            }
        }
        return executeMessageList;
    }

    /**
     * 获取文件扩展名 - Java特定实现
     */
    @Override
    protected String getFileExtension(String language) {
        if ("java".equalsIgnoreCase(language)) {
            return ".java";
        }
        // This should ideally not be reached if the factory routes correctly,
        // but as a safeguard:
        throw new UnsupportedOperationException("JavaNativeCodeSandbox only supports 'java' language, received: " + language);
    }
}
