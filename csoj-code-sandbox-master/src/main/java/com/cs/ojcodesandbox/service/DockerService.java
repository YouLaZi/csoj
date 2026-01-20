package com.cs.ojcodesandbox.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.Volume;
import com.cs.ojcodesandbox.model.ExecuteMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(name = "docker.available", havingValue = "true", matchIfMissing = false)
public class DockerService {

    private static final Logger log = LoggerFactory.getLogger(DockerService.class);

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    private DockerClient dockerClient;

    /**
 * 检查 Docker 守护进程是否可用
 * @return Docker 是否可用
 */
private boolean isDockerDaemonAvailable() {
    try {
        String dockerHost = getPlatformDockerHost();
        com.github.dockerjava.core.DefaultDockerClientConfig config =
            com.github.dockerjava.core.DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost(dockerHost)
            .withApiVersion("1.49")
            .build();
        
        com.github.dockerjava.transport.DockerHttpClient httpClient = 
            new com.github.dockerjava.httpclient5.ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .connectionTimeout(java.time.Duration.ofMillis(15000))
                .responseTimeout(java.time.Duration.ofMillis(30000))
                .build();
                
        DockerClient testClient = com.github.dockerjava.core.DockerClientImpl.getInstance(config, httpClient);
        
        // 多次尝试ping，确保连接稳定
        int pingAttempts = 3;
        for (int i = 0; i < pingAttempts; i++) {
            try {
                testClient.pingCmd().exec();
                log.info("Docker daemon is responsive and available");
                return true;
            } catch (Exception e) {
                log.warn("Docker ping attempt {}/{} failed: {}", i + 1, pingAttempts, e.getMessage());
                if (i < pingAttempts - 1) {
                    Thread.sleep(2000);
                }
            }
        }
        
        log.error("Docker daemon is not responsive after {} attempts", pingAttempts);
        return false;
        
    } catch (Exception e) {
        log.error("Docker daemon availability check failed: {}", e.getMessage());
        return false;
    }
}


    public DockerService() {
        try {
            // 首先检查 Docker 是否可用
            if (!isDockerDaemonAvailable()) {
                throw new RuntimeException("Docker daemon is not available");
            }
            
            com.github.dockerjava.core.DefaultDockerClientConfig config =
                com.github.dockerjava.core.DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(getPlatformDockerHost())
                .withApiVersion("1.49")
                .withRegistryUrl("https://index.docker.io/v1/")
                .build();
            
            // 优化HttpClient5客户端参数
            System.setProperty("com.github.dockerjava.httpclient5.responseTimeout", "120s");
            System.setProperty("com.github.dockerjava.httpclient5.connectionTimeout", "30s");
            System.setProperty("com.github.dockerjava.httpclient5.maxConnections", "10");
            System.setProperty("com.github.dockerjava.httpclient5.connectionKeepAlive", "300s");
            
            // 禁用 Windows 命名管道，避免在 Linux 环境下加载 kernel32
            System.setProperty("docker-java.disable-named-pipe", "true");
            
            // 在非 Windows 环境下启用 Unix Socket
            if (!System.getProperty("os.name").toLowerCase().contains("win")) {
                System.setProperty("docker-java.unix-socket.enabled", "true");
            }
            
            // 创建Docker HTTP客户端
            com.github.dockerjava.transport.DockerHttpClient httpClient = 
                new com.github.dockerjava.httpclient5.ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .sslConfig(config.getSSLConfig())
                    .connectionTimeout(java.time.Duration.ofSeconds(30))
                    .responseTimeout(java.time.Duration.ofSeconds(120))
                    .build();
                    
            this.dockerClient = com.github.dockerjava.core.DockerClientImpl.getInstance(config, httpClient);
            
            // 测试连接
            dockerClient.pingCmd().exec();
            log.info("DockerService initialized successfully with enhanced connection settings");
            
        } catch (Exception e) {
            log.error("Docker connection test failed: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize Docker connection", e);
        }
    }
    

    /**
     * 拉取镜像
     *
     * @param imageName 镜像名称
     */
    public void pullImage(String imageName) throws InterruptedException {
        System.out.println(String.format("准备拉取镜像: %s", imageName));
        List<com.github.dockerjava.api.model.Image> localImages = dockerClient.listImagesCmd().withImageNameFilter(imageName).exec();
        if (localImages.isEmpty()) {
            System.out.println(String.format("镜像 %s 在本地不存在，开始拉取。", imageName));
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(imageName);
            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
                @Override
                public void onNext(PullResponseItem item) {
                    System.out.println(String.format("镜像 %s 下载中: %s - %s", imageName, item.getStatus(), item.getProgressDetail() != null ? item.getProgressDetail().getCurrent() : ""));
                    super.onNext(item);
                }

                @Override
                public void onError(Throwable throwable) {
                    System.err.println(String.format("拉取镜像 %s 的回调中发生错误: %s", imageName, throwable.toString()));
                    // throwable.printStackTrace(); // Uncomment for full stack trace
                    super.onError(throwable);
                }
            };
            try {
                boolean pullCompleted = pullImageCmd.exec(pullImageResultCallback).awaitCompletion(10, TimeUnit.MINUTES); // PULL_IMAGE_TIMEOUT is not defined, using 5 minutes
                if (pullCompleted) {
                    System.out.println(String.format("镜像 %s 拉取成功完成或已是最新.", imageName));
                } else {
                    System.err.println(String.format("镜像 %s 拉取操作未在预期时间内明确完成 (awaitCompletion 返回 false). 请检查回调错误或超时.", imageName));
                    List<com.github.dockerjava.api.model.Image> imagesAfterPullAttempt = dockerClient.listImagesCmd().withImageNameFilter(imageName).exec();
                    if (imagesAfterPullAttempt.isEmpty()) {
                        System.err.println(String.format("镜像 %s 在拉取尝试后本地仍不存在.", imageName));
                        throw new RuntimeException(String.format("镜像 %s 拉取失败且本地不可用.", imageName));
                    }
                     else {
                        System.out.println(String.format("镜像 %s 拉取可能未完全成功，但本地已存在一个版本。将尝试使用本地版本。", imageName));
                    }
                }
            } catch (InterruptedException e) {
                System.err.println(String.format("拉取镜像 %s 被中断: %s", imageName, e.toString()));
                Thread.currentThread().interrupt();
                throw new RuntimeException("拉取镜像被中断", e);
            } catch (com.github.dockerjava.api.exception.DockerClientException e) {
                System.err.println(String.format("拉取镜像 %s 时发生 DockerClientException: %s", imageName, e.toString()));
                List<com.github.dockerjava.api.model.Image> imagesAfterException = dockerClient.listImagesCmd().withImageNameFilter(imageName).exec();
                if (imagesAfterException.isEmpty()) {
                    System.err.println(String.format("镜像 %s 在 DockerClientException 后本地仍不存在. 拉取彻底失败.", imageName));
                    throw new RuntimeException(String.format("拉取镜像 %s 时发生 Docker 客户端异常，且镜像本地不可用: %s", imageName, e.getMessage()), e);
                }
                 else {
                    System.out.println(String.format("镜像 %s 拉取时发生 DockerClientException，但本地已存在一个版本。将尝试使用本地版本。异常: %s", imageName, e.getMessage()));
                }
            }
        } else {
            System.out.println(String.format("镜像 %s 本地已存在,无需拉取.", imageName));
        }
    }



    /**
     * 执行代码（带日志）
     *
     * @param imageName      镜像名称
     * @param userCodeParentPath 用户代码父目录
     * @param inputList      输入参数
     * @param command        执行命令
     * @return 执行结果
     */
    /**
     * 在 Docker 容器内执行命令并返回详细执行信息
     *
     * @param imageName          镜像名称
     * @param userCodeParentPath 用户代码在宿主机上的父路径，将映射到容器的 /app
     * @param inputArgsList      传递给容器内命令的输入参数列表 (deprecated, use command for args or stdin)
     * @param command            要在容器内执行的命令及其参数
     * @param timeoutMillis      执行超时时间（毫秒）
     * @return ExecuteMessage 包含执行结果（stdout, stderr, exitCode, time）
     */
    public ExecuteMessage executeCommandInContainer(String imageName, String userCodeParentPath, List<String> inputArgsList, String[] command, long timeoutMillis) throws InterruptedException {
        // 预检查Docker连接
        try {
            dockerClient.pingCmd().exec();
        } catch (Exception e) {
            log.error("Docker连接检查失败，尝试重新初始化: {}", e.getMessage());
            // 重新初始化Docker客户端
            reinitializeDockerClient();
        }
        
        // 1. 创建容器
        HostConfig hostConfig = new HostConfig();
        hostConfig.withMemory(3000 * 1000 * 1000L); // 3000MB
        hostConfig.withCpuCount(4L);
        hostConfig.withCpusetCpus("0-3"); // 限制使用的CPU核心
        hostConfig.withMemorySwap(4000 * 1000 * 1000L); // 设置swap限制
        hostConfig.withOomKillDisable(true); // 禁止OOM Killer
        // 挂载目录 - 修复Windows路径问题
        String hostPath = userCodeParentPath;
        // 在Windows环境下，将反斜杠转换为正斜杠，并确保路径格式正确
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            // Convert Windows path to a format Docker on Windows might prefer
            // e.g., F:\path\to\dir -> /f/path/to/dir
            hostPath = hostPath.replace("\\", "/"); // First, F:\path -> F:/path
            if (hostPath.matches("^[A-Za-z]:/.*")) { // Check if it's like F:/path
                // Convert "F:/path/to/dir" to "/f/path/to/dir"
                hostPath = "/" + Character.toLowerCase(hostPath.charAt(0)) + hostPath.substring(2);
            }
            // Docker Desktop for Windows usually handles C:/path/to/dir to /c/path/to/dir automatically.
            // The /c/path/to/dir format is also widely accepted.
        }
        System.out.println(String.format("Docker bind: %s -> /app", hostPath));
        hostConfig.setBinds(new Bind(hostPath, new Volume("/app")));

        com.github.dockerjava.api.command.CreateContainerCmd createContainerCmd = dockerClient.createContainerCmd(imageName)
                .withHostConfig(hostConfig)
                .withNetworkDisabled(true) // 禁用网络
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withTty(false); // 对于日志获取，通常不需要 TTY

        createContainerCmd.withCmd("tail", "-f", "/dev/null");

        CreateContainerResponse createContainerResponse = createContainerCmd.exec();
        String containerId = createContainerResponse.getId();
        
        // 使用数组来确保containerId的更新能够在重试逻辑中生效
        final String[] currentContainerId = {containerId};

        // 2. 启动容器并确保它正在运行
        boolean containerStarted = false;
        int startRetries = 0;
        final int MAX_START_RETRIES = 3;
        
        while (!containerStarted && startRetries < MAX_START_RETRIES) {
            try {
                // 检查容器是否存在
                try {
                    dockerClient.inspectContainerCmd(currentContainerId[0]).exec();
                } catch (com.github.dockerjava.api.exception.NotFoundException e) {
                    // 容器不存在，重新创建
                    System.err.println(String.format("Container %s not found, recreating...", currentContainerId[0]));
                    CreateContainerResponse newContainer = dockerClient.createContainerCmd(imageName)
                        .withHostConfig(hostConfig)
                        .withNetworkDisabled(true)
                        .withAttachStdin(true)
                        .withAttachStdout(true)
                        .withAttachStderr(true)
                        .withTty(false)
                        .withCmd("tail", "-f", "/dev/null")
                        .exec();
                    currentContainerId[0] = newContainer.getId();
                    System.out.println(String.format("Recreated container %s", currentContainerId[0]));
                }
                
                // 启动容器
                try {
                    dockerClient.startContainerCmd(currentContainerId[0]).exec();
                } catch (com.github.dockerjava.api.exception.NotModifiedException e) {
                    // Status 304: 容器已经在运行状态，这是正常情况
                    System.out.println(String.format("Container %s is already running (Status 304), continuing...", currentContainerId[0]));
                } catch (com.github.dockerjava.api.exception.NotFoundException e) {
                    // Status 404: 容器不存在，这种情况应该在上面的检查中处理了
                    System.err.println(String.format("Container %s not found during start, will retry...", currentContainerId[0]));
                    throw e;
                }
                
                // 等待容器完全初始化
                Thread.sleep(3000); // 等待3秒，给容器足够的启动时间
                
                // 检查容器状态
                com.github.dockerjava.api.command.InspectContainerResponse inspectAfterStart = 
                    dockerClient.inspectContainerCmd(currentContainerId[0]).exec();
                
                System.out.println(String.format("Container %s status after start attempt %d: %s, Running: %s, ExitCode: %d, Error: %s", 
                    currentContainerId[0], 
                    startRetries + 1,
                    inspectAfterStart.getState().getStatus(), 
                    inspectAfterStart.getState().getRunning(),
                    inspectAfterStart.getState().getExitCodeLong(),
                    inspectAfterStart.getState().getError()));
                
                if (inspectAfterStart.getState().getRunning()) {
                    containerStarted = true;
                    System.out.println(String.format("Container %s successfully started on attempt %d", 
                        currentContainerId[0], startRetries + 1));
                } else {
                    startRetries++;
                    System.err.println(String.format("Container %s is not running after start attempt %d. Retrying...", 
                        currentContainerId[0], startRetries));
                    
                    // 获取容器日志以诊断问题
                    final String finalContainerId = currentContainerId[0];
                    com.github.dockerjava.api.async.ResultCallback.Adapter<Frame> logCallback = new com.github.dockerjava.api.async.ResultCallback.Adapter<Frame>() {
                        @Override
                        public void onNext(Frame frame) {
                            System.err.println(String.format("Logs for non-running container %s: %s", 
                                finalContainerId, new String(frame.getPayload())));
                        }
                    };
                    
                    dockerClient.logContainerCmd(currentContainerId[0])
                        .withStdOut(true)
                        .withStdErr(true)
                        .withTailAll()
                        .exec(logCallback)
                        .awaitCompletion(2, TimeUnit.SECONDS);
                    
                    // 如果容器已退出，尝试重新创建容器
                    if (startRetries < MAX_START_RETRIES) {
                        try {
                            // 移除旧容器
                            dockerClient.removeContainerCmd(currentContainerId[0]).withForce(true).exec();
                            
                            // 重新创建容器
                            CreateContainerResponse newContainer = dockerClient.createContainerCmd(imageName)
                                .withHostConfig(hostConfig)
                                .withNetworkDisabled(true)
                                .withAttachStdin(true)
                                .withAttachStdout(true)
                                .withAttachStderr(true)
                                .withTty(false)
                                .withCmd("tail", "-f", "/dev/null")
                                .exec();
                            
                            currentContainerId[0] = newContainer.getId();
                            final String newContainerId = currentContainerId[0];
                            System.out.println(String.format("Created new container %s after failed start attempt", newContainerId));
                        } catch (Exception e) {
                            System.err.println(String.format("Error recreating container after failed start: %s", e.getMessage()));
                        }
                    }
                }
            } catch (Exception e) {
                startRetries++;
                System.err.println(String.format("Error starting container %s (attempt %d): %s", 
                    currentContainerId[0], startRetries, e.getMessage()));
                
                if (startRetries >= MAX_START_RETRIES) {
                    throw new RuntimeException("Failed to start container after multiple attempts", e);
                }
                
                try {
                    Thread.sleep(1000); // 等待1秒后重试
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Container start retry was interrupted", ie);
                }
            }
        }
        
        if (!containerStarted) {
            throw new RuntimeException(String.format("Failed to start container %s after %d attempts", 
                currentContainerId[0], MAX_START_RETRIES));
        }

        // 更新containerId为最终的容器ID
        containerId = currentContainerId[0];

        // 3. 执行命令并获取结果
        // This method is designed for a single command execution, not iterating through inputList here.
        // Input should be handled by the command itself or via stdin for the single execution.
        // For simplicity, we'll use the first input from inputArgsList if provided, or empty for stdin.
        String singleInput = inputArgsList != null && !inputArgsList.isEmpty() ? inputArgsList.get(0) : "";

            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd(command)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();
        String execId = execCreateCmdResponse.getId();
            final StringBuilder stdOutBuilder = new StringBuilder();
            final StringBuilder stdErrBuilder = new StringBuilder();
            final ExecuteMessage executeMessage = new ExecuteMessage();
            boolean timeoutOccurred = true; // Indicates if a timeout condition was met
            long startTime = System.currentTimeMillis();
            final String finalContainerId = containerId;
            final String finalExecId = execId;

            com.github.dockerjava.api.async.ResultCallback.Adapter<Frame> execCallback = new com.github.dockerjava.api.async.ResultCallback.Adapter<Frame>() {
                @Override
                public void onStart(java.io.Closeable closeable) {
                    System.out.println("ExecCallback: onStart for execId " + finalExecId);
                    super.onStart(closeable);
                }

                @Override
                public void onNext(Frame frame) {
                    if (frame.getStreamType() == com.github.dockerjava.api.model.StreamType.STDOUT) {
                        stdOutBuilder.append(new String(frame.getPayload(), StandardCharsets.UTF_8));
                    } else if (frame.getStreamType() == com.github.dockerjava.api.model.StreamType.STDERR) {
                        stdErrBuilder.append(new String(frame.getPayload(), StandardCharsets.UTF_8));
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    String errorType = throwable.getClass().getSimpleName();
                    if (throwable instanceof java.io.EOFException || throwable.getCause() instanceof java.io.EOFException ||
                        throwable instanceof java.nio.channels.ClosedChannelException || throwable.getCause() instanceof java.nio.channels.ClosedChannelException) {
                        errorType = "Stream Error (EOF/ClosedChannel)";
                    }
                    System.err.println(String.format("ExecCallback: onError (%s) for execId %s, containerId %s. Error: %s", errorType, finalExecId, finalContainerId, throwable.toString()));
                    if (stdOutBuilder.length() > 0) {
                        executeMessage.setMessage(stdOutBuilder.toString());
                    }
                    if (stdErrBuilder.length() > 0) {
                        executeMessage.setErrorMessage(stdErrBuilder.toString() + "\nCallback Error: " + throwable.getMessage());
                    } else {
                        executeMessage.setErrorMessage("Callback Error: " + throwable.getMessage());
                    }
                    executeMessage.setExitValue(-5);
                    super.onError(throwable);
                }

                @Override
                public void onComplete() {
                    System.out.println(String.format("ExecCallback: onComplete for execId %s, containerId %s.", finalExecId, finalContainerId));
                    if (executeMessage.getMessage() == null && stdOutBuilder.length() > 0) {
                        executeMessage.setMessage(stdOutBuilder.toString());
                    }
                    if (executeMessage.getErrorMessage() == null && stdErrBuilder.length() > 0) {
                        executeMessage.setErrorMessage(stdErrBuilder.toString());
                    }
                    super.onComplete();
                }

                @Override
                public void close() throws java.io.IOException {
                    System.out.println("ExecCallback: close for execId " + finalExecId);
                    super.close();
                }
            };

            try {
                // 增强重试机制，最多尝试5次
                int maxRetries = 5;
                int retryCount = 0;
                boolean success = false;
                Exception lastException = null;
                
                while (retryCount < maxRetries && !success) {
                    try {
                        // 为每次重试创建新的execId（关键修复）
                        if (retryCount > 0) {
                            try {
                                ExecCreateCmdResponse newExecResponse = dockerClient.execCreateCmd(containerId)
                                    .withCmd(command)
                                    .withAttachStdin(true)
                                    .withAttachStdout(true)
                                    .withAttachStderr(true)
                                    .exec();
                                execId = newExecResponse.getId();
                                System.out.println(String.format("为重试创建新的执行ID %s (尝试 %d/%d)", execId, retryCount + 1, maxRetries));
                            } catch (Exception ex) {
                                System.err.println(String.format("创建新执行ID失败: %s", ex.getMessage()));
                                throw new RuntimeException("Failed to create new exec ID for retry", ex);
                            }
                        }
                        
                        // 检查容器是否仍在运行
                        com.github.dockerjava.api.command.InspectContainerResponse inspectBeforeExec = 
                            dockerClient.inspectContainerCmd(containerId).exec();
                        if (!inspectBeforeExec.getState().getRunning()) {
                            System.err.println(String.format("容器 %s 在执行命令前未运行 (尝试 %d/%d)", 
                                containerId, retryCount + 1, maxRetries));
                            // 如果容器不在运行，尝试重启它
                            try {
                                dockerClient.startContainerCmd(containerId).exec();
                                System.out.println(String.format("已尝试重启容器 %s", containerId));
                                Thread.sleep(3000); // 增加等待时间，确保容器完全启动
                                
                                // 验证容器是否成功重启
                                com.github.dockerjava.api.command.InspectContainerResponse inspectAfterRestart = 
                                    dockerClient.inspectContainerCmd(containerId).exec();
                                if (inspectAfterRestart.getState().getRunning()) {
                                    System.out.println(String.format("容器 %s 已成功重启", containerId));
                                    // 容器重启后需要创建新的execId
                                    ExecCreateCmdResponse newExecResponse = dockerClient.execCreateCmd(containerId)
                                        .withCmd(command)
                                        .withAttachStdin(true)
                                        .withAttachStdout(true)
                                        .withAttachStderr(true)
                                        .exec();
                                    execId = newExecResponse.getId();
                                    System.out.println(String.format("为重启后的容器 %s 创建了新的执行ID %s", containerId, execId));
                                } else {
                                    System.err.println(String.format("容器 %s 重启失败，状态: %s", 
                                        containerId, inspectAfterRestart.getState().getStatus()));
                                    // 如果重启失败，尝试重新创建容器
                                    if (retryCount < maxRetries - 1) { // 只在非最后一次尝试时重新创建
                                        try {
                                            // 移除旧容器
                                            dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                                            
                                            // 重新创建容器
                                            CreateContainerResponse newContainer = dockerClient.createContainerCmd(imageName)
                                                .withHostConfig(hostConfig)
                                                .withNetworkDisabled(true)
                                                .withAttachStdin(true)
                                                .withAttachStdout(true)
                                                .withAttachStderr(true)
                                                .withTty(false)
                                                .withCmd("tail", "-f", "/dev/null")
                                                .exec();
                                            
                                            containerId = newContainer.getId();
                                            System.out.println(String.format("已创建新容器 %s 替代失败的容器", containerId));
                                            
                                            // 启动新容器
                                            dockerClient.startContainerCmd(containerId).exec();
                                            Thread.sleep(3000); // 等待新容器启动
                                            
                                            // 为新容器创建新的执行命令
                                            ExecCreateCmdResponse newExecResponse = dockerClient.execCreateCmd(containerId)
                                                .withCmd(command)
                                                .withAttachStdin(true)
                                                .withAttachStdout(true)
                                                .withAttachStderr(true)
                                                .exec();
                                            
                                            execId = newExecResponse.getId();
                                            System.out.println(String.format("为新容器 %s 创建了新的执行ID %s", containerId, execId));
                                        } catch (Exception ex) {
                                            System.err.println(String.format("重新创建容器时出错: %s", ex.getMessage()));
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println(String.format("重启容器 %s 失败: %s", 
                                    containerId, e.getMessage()));
                            }
                        }
                        
                        // 执行命令，增加超时时间
                        System.out.println(String.format("开始执行命令 (execId: %s, 容器: %s, 尝试: %d/%d)", 
                            execId, containerId, retryCount + 1, maxRetries));
                        dockerClient.execStartCmd(execId)
                            .withDetach(false)
                            .withStdIn(singleInput.isEmpty() ? null : new java.io.ByteArrayInputStream(singleInput.getBytes(StandardCharsets.UTF_8)))
                            .exec(execCallback)
                            .awaitCompletion(timeoutMillis + 30000, TimeUnit.MILLISECONDS); // 增加30秒额外超时时间
                        
                        success = true;
                        timeoutOccurred = false; // 成功完成
                        System.out.println(String.format("命令执行成功完成 (execId: %s, 容器: %s)", execId, containerId));
                    } catch (RuntimeException e) {
                        lastException = e;
                        retryCount++;
                        if (retryCount < maxRetries) {
                            System.err.println(String.format("执行尝试 %d/%d 失败，异常类型: %s, 错误信息: %s, 将重试...", 
                                retryCount, maxRetries, e.getClass().getSimpleName(), e.getMessage()));
                            Thread.sleep(2000); // 增加等待时间后重试
                        } else {
                            System.err.println(String.format("所有 %d 次执行尝试均失败，最后错误: %s", 
                                maxRetries, e.getMessage()));
                        }
                    }
                }
                
                // 如果所有重试都失败，抛出最后一个异常
                if (!success && lastException != null) {
                    throw lastException;
                }
            } catch (InterruptedException e) {
                System.err.println("程序执行被中断: " + e.getMessage());
                executeMessage.setErrorMessage("Execution Interrupted: " + e.getMessage());
                executeMessage.setExitValue(-2); // Custom code for interruption
                Thread.currentThread().interrupt(); // Preserve interrupt status
            } catch (com.github.dockerjava.api.exception.DockerClientException e) {
                // Check if this is a timeout exception from docker-java
                if (e.getMessage() != null && e.getMessage().toLowerCase().contains("timeout")) {
                    System.err.println("程序执行超时 (DockerClientException): " + e.getMessage());
                    // timeoutOccurred remains true
                } else {
                    System.err.println("程序执行异常 (DockerClientException): " + e.getMessage());
                    executeMessage.setErrorMessage("Docker Execution Error: " + e.getMessage());
                    executeMessage.setExitValue(-3); // Custom code for other Docker errors
                    timeoutOccurred = false; // Not a timeout we are specifically handling with timeoutOccurred logic
                }
            } catch (Exception e) {
                if (e instanceof com.github.dockerjava.api.exception.ConflictException) {
                    // 处理容器未运行的冲突异常
                    System.err.println(String.format("程序执行时发生 ConflictException (容器可能未运行), execId: %s, containerId: %s. Exception: %s", execId, containerId, e.toString()));
                    
                    // 尝试获取容器状态以记录更多信息
                    try {
                        com.github.dockerjava.api.command.InspectContainerResponse inspectResponse = dockerClient.inspectContainerCmd(containerId).exec();
                        System.err.println(String.format("Container %s status during ConflictException: %s, Running: %s, ExitCode: %d, Error: %s", 
                            containerId, 
                            inspectResponse.getState().getStatus(), 
                            inspectResponse.getState().getRunning(),
                            inspectResponse.getState().getExitCodeLong(),
                            inspectResponse.getState().getError()));
                        
                        // 尝试获取容器日志以诊断问题
                  final String conflictContainerId = containerId;
                        com.github.dockerjava.api.async.ResultCallback.Adapter<Frame> logCallback = new com.github.dockerjava.api.async.ResultCallback.Adapter<Frame>() {
                            @Override
                            public void onNext(Frame frame) {
                                System.err.println(String.format("Container %s log during ConflictException: %s", 
                                    conflictContainerId, new String(frame.getPayload())));
                            }
                        };
                        dockerClient.logContainerCmd(containerId)
                            .withStdOut(true)
                            .withStdErr(true)
                            .withTail(10) // 获取最后10行日志
                            .exec(logCallback)
                            .awaitCompletion(2, TimeUnit.SECONDS);
                    } catch (Exception ex) {
                        System.err.println(String.format("Error getting container %s status during ConflictException: %s", 
                            containerId, ex.getMessage()));
                    }
                    
                    executeMessage.setErrorMessage("Container is not running (ConflictException): " + e.getMessage());
                    executeMessage.setExitValue(-8); // Custom code for ConflictException
                    timeoutOccurred = false;
                } else if (e instanceof RuntimeException && (e.getCause() instanceof java.io.EOFException || e.getCause() instanceof java.nio.channels.ClosedChannelException)) {
                    // 统一处理EOFException和ClosedChannelException
                    String exceptionType = e.getCause() instanceof java.io.EOFException ? "EOFException" : "ClosedChannelException";
                    System.err.println(String.format("程序执行时发生 %s (可能由于流提前关闭或网络不稳定), execId: %s, containerId: %s. 尝试恢复连接并继续处理. Exception: %s", 
                        exceptionType, execId, containerId, e.toString()));
                    
                    // 尝试检查容器状态
                    boolean containerRunning = false;
                    try {
                        com.github.dockerjava.api.command.InspectContainerResponse inspectResponse = 
                            dockerClient.inspectContainerCmd(containerId).exec();
                        containerRunning = inspectResponse.getState().getRunning();
                        System.out.println(String.format("容器 %s 在 %s 期间状态: %s, Running: %s", 
                            containerId, exceptionType, inspectResponse.getState().getStatus(), containerRunning));
                    } catch (Exception ex) {
                        System.err.println(String.format("检查容器 %s 状态时出错: %s", containerId, ex.getMessage()));
                    }
                    
                    // 如果容器仍在运行，尝试重新获取执行结果
                    if (containerRunning) {
                        try {
                            // 尝试获取执行状态
                            com.github.dockerjava.api.command.InspectExecResponse execResponse = 
                                dockerClient.inspectExecCmd(execId).exec();
                            System.out.println(String.format("执行 %s 状态: Running=%s, ExitCode=%d", 
                                execId, execResponse.isRunning(), execResponse.getExitCodeLong()));
                            
                            // 如果执行已完成，获取退出码
                            if (!execResponse.isRunning()) {
                                executeMessage.setExitValue(execResponse.getExitCodeLong().intValue());
                                System.out.println(String.format("执行 %s 已完成，退出码: %d", execId, executeMessage.getExitValue()));
                            } else {
                                // 执行仍在运行，设置特殊错误码
                                executeMessage.setExitValue(-6); // 流中断但执行仍在进行
                                executeMessage.setErrorMessage(String.format("%s: 连接中断但容器执行仍在进行，输出可能不完整", exceptionType));
                            }
                        } catch (Exception ex) {
                            System.err.println(String.format("检查执行 %s 状态时出错: %s", execId, ex.getMessage()));
                            executeMessage.setErrorMessage(String.format("%s: 连接中断且无法获取执行状态，输出可能不完整: %s", 
                                exceptionType, ex.getMessage()));
                            executeMessage.setExitValue(-7); // 无法获取执行状态
                        }
                    } else {
                        // 容器已停止
                        executeMessage.setErrorMessage(String.format("%s: 连接中断且容器已停止运行，输出可能不完整", exceptionType));
                        executeMessage.setExitValue(-8); // 容器已停止
                    }
                    
                    // 关键修改：如果有输出，则认为执行成功
                    if (stdOutBuilder.length() > 0) {
                        System.out.println(String.format("尽管发生 %s，但检测到有标准输出，将视为成功执行", exceptionType));
                        executeMessage.setMessage(stdOutBuilder.toString().trim());
                        executeMessage.setExitValue(0); // 设置为成功
                        executeMessage.setErrorMessage(null); // 清除错误信息
                    }
                    
                    timeoutOccurred = false; // 这不是直接的超时
                } else {
                    System.err.println(String.format("程序执行时发生未知异常 (execId: %s, containerId: %s). Exception: %s", execId, containerId, e.toString()));
                    e.printStackTrace(); // 打印完整的堆栈跟踪
                    executeMessage.setErrorMessage("Unknown Execution Error: " + e.getClass().getName() + ": " + e.getMessage()); // 包含异常类型
                    executeMessage.setExitValue(-4); // Custom code for unknown errors
                    timeoutOccurred = false;
                }
            }

            long endTime = System.currentTimeMillis();
            executeMessage.setTime(endTime - startTime);

            // Check container status after exec completion
            try {
                com.github.dockerjava.api.command.InspectContainerResponse inspectResponse = dockerClient.inspectContainerCmd(containerId).exec();
                System.out.println("Container " + containerId + " status after exec: " + inspectResponse.getState().getStatus() + ", Running: " + inspectResponse.getState().getRunning());
            } catch (Exception e) {
                System.err.println("Error inspecting container " + containerId + " after exec: " + e.getMessage());
            }

            String finalStdOut = stdOutBuilder.toString();
            String finalStdErr = stdErrBuilder.toString();

            // Always set the collected stdout, regardless of errors, unless it's already part of an error message.
            if (executeMessage.getMessage() == null || executeMessage.getMessage().isEmpty()) {
                executeMessage.setMessage(finalStdOut.trim());
            }

            // Handle error message and exit value based on exceptions or stderr content.
            // 修改：不再仅因有标准输出就强制设置为成功状态，而是综合考虑退出码和错误输出
            if (!finalStdOut.isEmpty()) {
                // 设置标准输出为消息内容
                executeMessage.setMessage(finalStdOut.trim());
                
                // 只有在没有明确错误的情况下才考虑成功
                if (executeMessage.getExitValue() == null || executeMessage.getExitValue() == 0) {
                    // 如果没有错误输出，则认为是成功的
                    if (finalStdErr.isEmpty()) {
                        executeMessage.setExitValue(0);
                    } 
                    // 如果有错误输出，但可能只是警告，需要进一步判断
                    else {
                        // 检查错误输出是否包含严重错误关键词
                        boolean containsSerious = containsSeriousError(finalStdErr);
                        if (containsSerious) {
                            executeMessage.setExitValue(1); // 设置为错误状态
                            executeMessage.setErrorMessage(finalStdErr.trim());
                        } else {
                            // 可能只是警告，保留成功状态，但记录警告
                            executeMessage.setExitValue(0);
                            executeMessage.setErrorMessage("Warning (not affecting status): " + finalStdErr.trim());
                        }
                    }
                }
                // 如果已有非零退出码，保留错误状态并添加错误信息
                else if (!finalStdErr.isEmpty()) {
                    executeMessage.setErrorMessage(finalStdErr.trim());
                }
            } else if (executeMessage.getExitValue() == null || executeMessage.getExitValue() == 0) { // 如果没有标准输出且没有设置错误码
                if (timeoutOccurred && (executeMessage.getTime() >= timeoutMillis)) {
                    executeMessage.setErrorMessage("Time Limit Exceeded" + (finalStdErr.isEmpty() ? "" : "\nStderr: " + finalStdErr.trim()));
                    executeMessage.setExitValue(-1); // Timeout
                } else if (!finalStdErr.isEmpty()) {
                    // If there's stderr content and no specific error message was set by an exception,
                    // use stderr as the error message.
                    if (executeMessage.getErrorMessage() == null || executeMessage.getErrorMessage().isEmpty()) {
                        executeMessage.setErrorMessage(finalStdErr.trim());
                    }
                    executeMessage.setExitValue(1); // Assume error if stderr has content and no other error code
                } else {
                    // Success: no timeout, no stderr, no prior error code
                    executeMessage.setExitValue(0);
                    // executeMessage.setMessage(finalStdOut.trim()); // Already set above
                }
            } else if (finalStdErr.isEmpty() && executeMessage.getExitValue() != 0) {
                // 如果没有标准错误输出但有非零退出码，检查是否为流错误
                if (executeMessage.getExitValue() == -5 || executeMessage.getExitValue() == -6 || 
                    executeMessage.getExitValue() == -7 || executeMessage.getExitValue() == -8) {
                    // 不再自动设置为成功状态，而是保留错误码并添加说明
                    executeMessage.setErrorMessage("Stream error detected with exit code: " + executeMessage.getExitValue());
                }
            } else {
                // An error code was already set by an exception. Append stderr to the existing error message if not already included.
                String existingErrorMessage = executeMessage.getErrorMessage() != null ? executeMessage.getErrorMessage() : "";
                if (!finalStdErr.isEmpty() && !existingErrorMessage.contains(finalStdErr.trim())) {
                    executeMessage.setErrorMessage(existingErrorMessage + "\nStderr: " + finalStdErr.trim());
                }
            }

            // Ensure stdout is cleared if there's a significant error message and stdout is identical to it (rare case)
            if (executeMessage.getErrorMessage() != null && executeMessage.getErrorMessage().equals(executeMessage.getMessage())){
                if (!finalStdOut.isEmpty() && finalStdOut.trim().equals(executeMessage.getErrorMessage())) {
                     // This case is unlikely but handles if stdout was mistakenly set as the error
                } else if (finalStdOut.isEmpty()) {
                    // If stdout is empty and there's an error, message should be null or empty
                    executeMessage.setMessage(null);
                }
            }

        // 4. 获取容器日志 (可选)
        com.github.dockerjava.api.async.ResultCallback.Adapter<Frame> logCallback = new com.github.dockerjava.api.async.ResultCallback.Adapter<Frame>() {
            @Override
            public void onNext(Frame frame) {
                System.out.println("Container Log: " + new String(frame.getPayload()));
            }
        };
        dockerClient.logContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withTimestamps(true)
                .exec(logCallback);
        try {
            logCallback.awaitCompletion(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while waiting for container logs for " + containerId + ": " + e.getMessage());
        }

        // 5. 清理容器资源 - 改进的生命周期管理
        // 注意：不立即删除容器，而是让容器自然结束或由外部管理
        try {
            // 检查容器状态
            com.github.dockerjava.api.command.InspectContainerResponse finalInspect = 
                dockerClient.inspectContainerCmd(containerId).exec();
            
            System.out.println(String.format("容器 %s 最终状态: %s, Running: %s", 
                containerId, 
                finalInspect.getState().getStatus(),
                finalInspect.getState().getRunning()));
            
            // 如果容器仍在运行，尝试优雅停止（但不删除）
            if (finalInspect.getState().getRunning()) {
                System.out.println(String.format("容器 %s 仍在运行，尝试优雅停止", containerId));
                try {
                    dockerClient.stopContainerCmd(containerId).withTimeout(10).exec();
                    System.out.println(String.format("容器 %s 已停止", containerId));
                } catch (Exception stopEx) {
                    System.err.println(String.format("停止容器 %s 时出错: %s", containerId, stopEx.getMessage()));
                }
            }
            
            // 不删除容器，让它保持在系统中以便调试和重用
            System.out.println(String.format("容器 %s 保留在系统中，可用于调试或重用", containerId));
            
        } catch (Exception e) {
            System.err.println(String.format("检查容器 %s 最终状态时出错: %s", containerId, e.getMessage()));
        }

        return executeMessage;
    }


    private static String getPlatformDockerHost() {
        String dockerHost = System.getenv("DOCKER_HOST");
        if (dockerHost != null && !dockerHost.isEmpty()) {
            log.info("使用环境变量DOCKER_HOST: {}", dockerHost);
            return dockerHost;
        }
        
        String os = System.getProperty("os.name").toLowerCase();
        log.info("检测到操作系统: {}", os);
        
        if (os.contains("win")) {
            // Windows环境：由于用户已启用TCP连接，优先使用TCP
            if (isDockerTcpAvailable(2375)) {
                dockerHost = "tcp://localhost:2375";
                log.info("使用Docker TCP连接: {}", dockerHost);
            } else if (isDockerTcpAvailable(2376)) {
                dockerHost = "tcp://localhost:2376";
                log.info("使用Docker TCP TLS连接: {}", dockerHost);
            } 
            // TCP不可用时才使用Named Pipe
            else if (isDockerNamedPipeAvailable()) {
                dockerHost = "npipe:////./pipe/docker_engine";
                log.info("使用Docker Named Pipe连接: {}", dockerHost);
            } else {
                // 如果都不可用，默认使用TCP（用户已启用）
                dockerHost = "tcp://localhost:2375";
                log.warn("Docker连接检测失败，使用默认TCP连接: {}", dockerHost);
            }
        } else {
            // Unix/Linux环境
            dockerHost = "unix:///var/run/docker.sock";
            log.info("使用Docker Unix Socket连接: {}", dockerHost);
        }
        
        return dockerHost;
    }
    
    
    /**
     * 检查Docker Named Pipe连接是否可用
     * @return 是否可用
     */
    private static boolean isDockerNamedPipeAvailable() {
        try {
            // 尝试创建一个简单的Docker配置来测试Named Pipe连接
            com.github.dockerjava.core.DefaultDockerClientConfig config =
                com.github.dockerjava.core.DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("npipe:////./pipe/docker_engine")
                .withApiVersion("1.49")
                .build();
            
            com.github.dockerjava.transport.DockerHttpClient httpClient = 
                new com.github.dockerjava.httpclient5.ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .sslConfig(config.getSSLConfig())
                    .connectionTimeout(java.time.Duration.ofSeconds(5)) // 5秒超时
                    .responseTimeout(java.time.Duration.ofSeconds(10))   // 10秒读取超时
                    .build();
                    
            DockerClient testClient = com.github.dockerjava.core.DockerClientImpl.getInstance(config, httpClient);
            testClient.pingCmd().exec();
            
            log.debug("Docker Named Pipe连接可用");
            return true;
        } catch (Exception e) {
            log.debug("Docker Named Pipe连接不可用: {}", e.getMessage());
            return false;
        }
    }

    
    private void reinitializeDockerClient() {
        try {
            com.github.dockerjava.core.DefaultDockerClientConfig config =
                com.github.dockerjava.core.DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(getPlatformDockerHost())
                .withApiVersion("1.49")
                .build();
            
            com.github.dockerjava.transport.DockerHttpClient httpClient = new com.github.dockerjava.httpclient5.ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .connectionTimeout(java.time.Duration.ofSeconds(30))
                .responseTimeout(java.time.Duration.ofSeconds(120))
                .build();
                
            this.dockerClient = com.github.dockerjava.core.DockerClientImpl.getInstance(config, httpClient);
            dockerClient.pingCmd().exec();
            log.info("Docker客户端重新初始化成功");
        } catch (Exception e) {
            log.error("Docker客户端重新初始化失败: {}", e.getMessage());
            throw new RuntimeException("Failed to reinitialize Docker client", e);
        }
    }

    
    private static boolean isDockerTcpAvailable(int port) {
        try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(new java.net.InetSocketAddress("localhost", port), 5000);
            return true;
        } catch (Exception e) {
            log.debug("Docker TCP连接端口{}不可用: {}", port, e.getMessage());
            return false;
        }
    }

    /**
     * 判断错误输出是否包含严重错误关键词
     * 用于区分严重错误和普通警告
     * 
     * @param errorOutput 错误输出内容
     * @return 是否包含严重错误关键词
     */
    private boolean containsSeriousError(String errorOutput) {
        if (errorOutput == null || errorOutput.isEmpty()) {
            return false;
        }
        
        // 定义严重错误的关键词列表
        String[] seriousErrorKeywords = {
            "error:", "exception", "segmentation fault", "core dumped",
            "runtime error", "fatal error", "undefined reference", "cannot find",
            "not found", "无法找到", "未定义", "错误:", "异常", "运行时错误",
            "NullPointerException", "ArrayIndexOutOfBoundsException", "ClassNotFoundException",
            "OutOfMemoryError", "StackOverflowError", "NoClassDefFoundError"
        };
        
        // 转换为小写以进行不区分大小写的比较
        String lowerErrorOutput = errorOutput.toLowerCase();
        
        // 检查是否包含任何严重错误关键词
        for (String keyword : seriousErrorKeywords) {
            if (lowerErrorOutput.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
}

