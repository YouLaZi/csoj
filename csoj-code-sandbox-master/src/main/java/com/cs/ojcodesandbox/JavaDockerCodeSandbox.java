package com.cs.ojcodesandbox;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.cs.ojcodesandbox.model.ExecuteCodeRequest;
import com.cs.ojcodesandbox.model.ExecuteCodeResponse;
import com.cs.ojcodesandbox.model.ExecuteMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Java Docker 代码沙箱
 * 使用 Docker 容器隔离执行用户提交的 Java 代码
 */
@Slf4j
@Component
public class JavaDockerCodeSandbox extends JavaCodeSandboxTemplate {

    private static final long TIME_OUT = 5000L;

    private static final Boolean FIRST_INIT = true;

    /**
     * 注入 Spring 管理的 DockerClient 单例
     * 避免每次执行都创建新的客户端连接
     */
    @Resource
    private DockerClient dockerClient;

    public static void main(String[] args) {
        JavaDockerCodeSandbox javaNativeCodeSandbox = new JavaDockerCodeSandbox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2", "1 3"));
        String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/unsafeCode/RunFileError.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/simpleCompute/Main.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }

    /**
     * 2、编译文件（在Docker环境中，编译通常在镜像构建或容器启动时完成）
     * @param userCodeFile 用户代码文件
     * @param language 编程语言
     * @return 编译消息
     */
    @Override
    public ExecuteMessage compileFile(File userCodeFile, String language) {
        // 在Docker容器中编译Java代码
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();

        // 使用注入的 DockerClient 单例，避免重复创建连接
        if (dockerClient == null) {
            // 仅在非Spring环境下（如main方法直接运行）创建临时客户端
            dockerClient = createDockerClient();
        }

        // 使用临时容器进行编译
        String image = "openjdk:8-alpine";
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        HostConfig hostConfig = new HostConfig();
        hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/app")));
        
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withCmd("javac", "/app/Main.java")
                .exec();
        
        String containerId = createContainerResponse.getId();
        
        try {
            // 启动容器进行编译
            dockerClient.startContainerCmd(containerId).exec();
            
            // 等待编译完成
            WaitContainerResultCallback waitCallback = new WaitContainerResultCallback();
            dockerClient.waitContainerCmd(containerId).exec(waitCallback);
            int exitCode = waitCallback.awaitStatusCode();
            
            // 获取编译输出
            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();
            
            LogContainerCmd logContainerCmd = dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true);
            
            logContainerCmd.exec(new ResultCallback.Adapter<Frame>() {
                @Override
                public void onNext(Frame frame) {
                    String log = new String(frame.getPayload()).trim();
                    if (frame.getStreamType() == StreamType.STDERR) {
                        errorOutput.append(log);
                    } else {
                        output.append(log);
                    }
                }
            }).awaitCompletion();
            
            ExecuteMessage executeMessage = new ExecuteMessage();
            executeMessage.setExitValue(exitCode);
            executeMessage.setMessage(output.toString());
            executeMessage.setErrorMessage(errorOutput.toString());
            
            return executeMessage;
            
        } catch (Exception e) {
            ExecuteMessage executeMessage = new ExecuteMessage();
            executeMessage.setExitValue(1);
            executeMessage.setErrorMessage("编译失败: " + e.getMessage());
            return executeMessage;
        } finally {
            // 清理容器
            try {
                dockerClient.removeContainerCmd(containerId).withForce(true).exec();
            } catch (Exception e) {
                System.err.println("清理编译容器失败: " + e.getMessage());
            }
        }
    }

    /**
     * 3、创建容器，把文件复制到容器内
     * @param userCodeFile
     * @param inputList
     * @return
     */
    @Override
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList, String language) {
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();

        // 使用注入的 DockerClient 单例，避免重复创建连接
        if (dockerClient == null) {
            // 仅在非Spring环境下（如main方法直接运行）创建临时客户端
            dockerClient = createDockerClient();
        }

        // 拉取镜像
        String image = "openjdk:8-alpine";
        if (FIRST_INIT) {
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
                @Override
                public void onNext(PullResponseItem item) {
                    System.out.println("下载镜像：" + item.getStatus());
                    super.onNext(item);
                }
            };
            try {
                pullImageCmd
                        .exec(pullImageResultCallback)
                        .awaitCompletion();
            } catch (InterruptedException e) {
                System.out.println("拉取镜像异常");
                throw new RuntimeException(e);
            }
        }

        System.out.println("下载完成");

        // 创建容器

        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        HostConfig hostConfig = new HostConfig();
        hostConfig.withMemory(100 * 1000 * 1000L);
        hostConfig.withMemorySwap(0L);
        hostConfig.withCpuCount(1L);
        // 移除有问题的seccomp配置，使用默认安全配置
        // hostConfig.withSecurityOpts(Arrays.asList("seccomp=unconfined"));
        hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/app")));
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withNetworkDisabled(true)
                .withReadonlyRootfs(true)
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)
                .exec();
        System.out.println(createContainerResponse);
        String containerId = createContainerResponse.getId();

        // 启动容器
        dockerClient.startContainerCmd(containerId).exec();

        // docker exec keen_blackwell java -cp /app Main 1 3
        // 执行命令并获取结果
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            StopWatch stopWatch = new StopWatch();
            String[] inputArgsArray = inputArgs.split(" ");
            String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main"}, inputArgsArray);
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmdArray)
                    .withAttachStderr(true)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .exec();
            System.out.println("创建执行命令：" + execCreateCmdResponse);

            ExecuteMessage executeMessage = new ExecuteMessage();
            final String[] message = {null};
            final String[] errorMessage = {null};
            long time = 0L;
            // 判断是否超时
            final boolean[] timeout = {true};
            String execId = execCreateCmdResponse.getId();
            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
                @Override
                public void onComplete() {
                    // 如果执行完成，则表示没超时
                    timeout[0] = false;
                    super.onComplete();
                }

                @Override
                public void onNext(Frame frame) {
                    StreamType streamType = frame.getStreamType();
                    if (StreamType.STDERR.equals(streamType)) {
                        errorMessage[0] = new String(frame.getPayload());
                        System.out.println("输出错误结果：" + errorMessage[0]);
                    } else {
                        message[0] = new String(frame.getPayload());
                        System.out.println("输出结果：" + message[0]);
                    }
                    super.onNext(frame);
                }
            };

            final long[] maxMemory = {0L};

            // 获取占用的内存
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {

                @Override
                public void onNext(Statistics statistics) {
                    // System.out.println("内存占用：" + statistics.getMemoryStats().getUsage());
                    maxMemory[0] = Math.max(statistics.getMemoryStats().getUsage(), maxMemory[0]);
                }

                @Override
                public void close() throws IOException {

                }

                @Override
                public void onStart(Closeable closeable) {

                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onComplete() {

                }
            });
            statsCmd.exec(statisticsResultCallback);
            try {
                stopWatch.start();
                dockerClient.execStartCmd(execId)
                        .exec(execStartResultCallback)
                        .awaitCompletion(TIME_OUT, TimeUnit.SECONDS);
                stopWatch.stop();
                time = stopWatch.getLastTaskTimeMillis();
                statsCmd.close();
            } catch (InterruptedException e) {
                System.out.println("程序执行异常");
                throw new RuntimeException(e);
            }
            executeMessage.setMessage(message[0]);
            executeMessage.setErrorMessage(errorMessage[0]);
            executeMessage.setTime(time);
            executeMessage.setMemory(maxMemory[0]);
            executeMessageList.add(executeMessage);
        }
        
        // 清理容器资源
        if (containerId != null) {
            try {
                dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                System.out.println("容器已清理: " + containerId);
            } catch (Exception e) {
                System.err.println("清理容器失败: " + e.getMessage());
            }
        }
        
        return executeMessageList;
    }




    /**
     * 获取当前平台下的 Docker Host 地址，优先读取 DOCKER_HOST 环境变量
     */
    private static String getPlatformDockerHost() {
        String dockerHost = System.getenv("DOCKER_HOST");
        if (dockerHost == null || dockerHost.isEmpty()) {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                dockerHost = "npipe:////./pipe/docker_engine";
            } else {
                dockerHost = "unix:///var/run/docker.sock";
            }
        }
        // 强制非 Windows 平台返回 unix socket，防止误判
        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("win")) {
            dockerHost = "unix:///var/run/docker.sock";
        }
        return dockerHost;
    }

    /**
     * 创建 DockerClient 实例（仅在非Spring环境下使用）
     * @return DockerClient 实例
     */
    private static DockerClient createDockerClient() {
        String dockerHost = getPlatformDockerHost();
        log.info("创建临时DockerClient，Docker Host: {}", dockerHost);

        com.github.dockerjava.core.DefaultDockerClientConfig config =
            com.github.dockerjava.core.DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost(dockerHost)
            .build();

        com.github.dockerjava.transport.DockerHttpClient httpClient =
            new com.github.dockerjava.httpclient5.ApacheDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())
            .sslConfig(config.getSSLConfig())
            .connectionTimeout(java.time.Duration.ofSeconds(30))
            .responseTimeout(java.time.Duration.ofSeconds(120))
            .build();

        return com.github.dockerjava.core.DockerClientImpl.getInstance(config, httpClient);
    }
}


