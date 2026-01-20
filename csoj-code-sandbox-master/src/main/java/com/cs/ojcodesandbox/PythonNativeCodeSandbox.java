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
 * Python 原生代码沙箱实现
 */
@Component
public class PythonNativeCodeSandbox extends JavaCodeSandboxTemplate {

    private static final String PYTHON_EXECUTABLE = "python"; // Or full path to python interpreter

    /**
     * 检查Python解释器是否可用
     * @return 如果解释器可用返回true，否则返回false
     */
    public boolean isInterpreterAvailable() {
        try {
            Process process = Runtime.getRuntime().exec(PYTHON_EXECUTABLE + " --version");
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }

    /**
     * 编译文件 - Python特定实现 (通常Python不需要显式编译)
     * 这里可以简单返回成功，或者进行一些预处理，如语法检查 (可选)
     */
    @Override
    public ExecuteMessage compileFile(File userCodeFile, String language) {
        // Python is an interpreted language, so no explicit compilation step is needed like Java.
        // We can consider this step successful by default.
        // Optionally, one could add a syntax check here using a linter.
        ExecuteMessage executeMessage = new ExecuteMessage();
        executeMessage.setExitValue(0); // Success
        executeMessage.setMessage("Python code does not require compilation.");
        return executeMessage;
    }

    /**
     * 执行文件 - Python特定实现
     */
    @Override
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList, String language) {
        List<ExecuteMessage> executeMessageList = new ArrayList<>();

        for (String inputArgs : inputList) {
            String runCmd = String.format("%s %s %s", PYTHON_EXECUTABLE, userCodeFile.getAbsolutePath(), inputArgs);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                // 超时控制 (与Java实现类似)
                new Thread(() -> {
                    try {
                        Thread.sleep(TIME_OUT); // TIME_OUT should be defined in the parent or this class
                        System.out.println("Python script timeout, interrupting.");
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                        System.err.println("Python timeout thread interrupted: " + e.getMessage());
                    }
                }).start();
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行Python脚本");
                executeMessageList.add(executeMessage);
            } catch (Exception e) {
                ExecuteMessage errorExecuteMessage = new ExecuteMessage();
                errorExecuteMessage.setExitValue(-1); // Indicate error
                errorExecuteMessage.setErrorMessage("Python script execution failed: " + e.getMessage());
                executeMessageList.add(errorExecuteMessage);
                // Consider whether to stop on first error or collect all results
                // throw new RuntimeException("Python script execution error for input: " + inputArgs, e);
            }
        }
        return executeMessageList;
    }

    /**
     * 获取文件扩展名 - Python特定实现
     */
    @Override
    protected String getFileExtension(String language) {
        if ("python".equalsIgnoreCase(language)) {
            return ".py";
        }
        throw new UnsupportedOperationException("PythonNativeCodeSandbox only supports 'python' language, received: " + language);
    }
}