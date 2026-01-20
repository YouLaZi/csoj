package com.cs.ojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.cs.ojcodesandbox.model.ExecuteCodeRequest;
import com.cs.ojcodesandbox.model.ExecuteCodeResponse;
import com.cs.ojcodesandbox.model.enums.ExecuteCodeStatusEnum;
import com.cs.ojcodesandbox.model.ExecuteMessage;
import com.cs.ojcodesandbox.model.JudgeInfo;
import com.cs.ojcodesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Java 代码沙箱模板方法的实现
 */
@Slf4j
public abstract class JavaCodeSandboxTemplate implements CodeSandbox {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    protected static final String MAIN_FILE_BASENAME = "Main"; // Renamed from GLOBAL_JAVA_CLASS_NAME, now protected

    protected static final long TIME_OUT = 5000L; // Now protected

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

//        1. 把用户的代码保存为文件
        File userCodeFile = saveCodeToFile(code, language); // Pass language

//        2. 编译代码，得到 class 文件
        ExecuteMessage compileFileExecuteMessage = compileFile(userCodeFile, language); // Pass language
        System.out.println(compileFileExecuteMessage);

        // 3. 执行代码，得到输出结果
        List<ExecuteMessage> executeMessageList = runFile(userCodeFile, inputList, language); // Pass language

//        4. 收集整理输出结果
        ExecuteCodeResponse outputResponse = getOutputResponse(executeMessageList);

//        5. 文件清理
        boolean b = deleteFile(userCodeFile);
        if (!b) {
            log.error("deleteFile error, userCodeFilePath = {}", userCodeFile.getAbsolutePath());
        }
        return outputResponse;
    }


    /**
     * 1. 把用户的代码保存为文件
     * @param code 用户代码
     * @param language 编程语言
     * @return
     */
    public File saveCodeToFile(String code, String language) {
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        // 判断全局代码目录是否存在，没有则新建
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }

        // 把用户的代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + MAIN_FILE_BASENAME + getFileExtension(language);
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
        return userCodeFile;
    }

    /**
     * 获取文件扩展名
     * @param language 编程语言
     * @return 文件扩展名
     */
    protected String getFileExtension(String language) {
        // 默认为 Java，子类可以覆盖此方法以支持其他语言
        if ("java".equalsIgnoreCase(language)) {
            return ".java";
        }
        // 对于不支持的语言，可以抛出异常或返回一个通用扩展名
        // 例如，如果这是一个通用模板，某些语言可能不需要特定的扩展名或编译步骤
        // 或者，可以使此方法抽象，强制子类实现
        throw new UnsupportedOperationException("Language '" + language + "' not supported for default file extension handling.");
    }

    /**
     * 2、编译代码
     * @param userCodeFile 用户代码文件
     * @param language 编程语言
     * @return 编译结果
     */
    public abstract ExecuteMessage compileFile(File userCodeFile, String language);

    /**
     * 3、执行文件，获得执行结果列表
     * @param userCodeFile 用户代码文件
     * @param inputList 输入参数列表
     * @param language 编程语言
     * @return 执行结果列表
     */
    public abstract List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList, String language);

    /**
     * 4、获取输出结果
     * @param executeMessageList
     * @return
     */
    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        // 取用时最大值，便于判断是否超时
        long maxTime = 0;
        for (ExecuteMessage executeMessage : executeMessageList) {
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                executeCodeResponse.setMessage(errorMessage);
                // 用户提交的代码执行中存在错误
                executeCodeResponse.setStatus(ExecuteCodeStatusEnum.FAILED.getValue());
                break;
            }
            outputList.add(executeMessage.getMessage());
            Long time = executeMessage.getTime();
            if (time != null) {
                maxTime = Math.max(maxTime, time);
            }
        }
        // 正常运行完成
        // 只有当所有执行都成功（没有错误信息跳出循环）且输出了结果，才认为是成功
        if (executeCodeResponse.getStatus() == null && outputList.size() == executeMessageList.size()) {
            executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SUCCEED.getValue());
        } else if (executeCodeResponse.getStatus() == null) {
            // 如果没有明确的错误状态，但输出列表与执行消息列表大小不匹配（可能意味着部分执行失败未被捕获为errorMessage）
            // 或者循环正常结束但之前没有设置状态，这里可以根据情况设置一个默认的失败或部分成功的状态
            // 为简单起见，如果前面没有设置错误，且大小匹配，则成功，否则可能是某种未捕获的错误或逻辑不完整
            // 保持之前的逻辑，如果循环未因错误跳出，且大小匹配，则成功
            // 若循环因错误跳出，则 status 已被设为 FAILED
            // 若循环正常结束，但 outputList.size() != executeMessageList.size()，这本身是一个异常情况，应该在前面处理
            // 此处简化：如果上面没有设置 FAILED，并且大小匹配，则 SUCCEED
            // 如果 status 仍为 null (意味着没有错误信息)，但大小不匹配，这不应该发生，但作为回退，可以设为 FAILED
            if (outputList.size() == executeMessageList.size()) {
                 executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SUCCEED.getValue());
            } else if (executeCodeResponse.getMessage() == null) { // 如果没有错误信息，但数量不匹配，也算失败
                 executeCodeResponse.setMessage("部分执行结果缺失");
                 executeCodeResponse.setStatus(ExecuteCodeStatusEnum.FAILED.getValue());
            }
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        // 要借助第三方库来获取内存占用，非常麻烦，此处不做实现
//        judgeInfo.setMemory();
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }

    /**
     * 5、删除文件
     * @param userCodeFile
     * @return
     */
    public boolean deleteFile(File userCodeFile) {
        if (userCodeFile.getParentFile() != null) {
            String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
            return del;
        }
        return true;
    }

    /**
     * 6、获取错误响应
     *
     * @param e
     * @return
     */
    private ExecuteCodeResponse getErrorResponse(Throwable e) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        // 表示代码沙箱错误
        executeCodeResponse.setStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR.getValue());
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }
}
