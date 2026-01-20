package com.cs.ojcodesandbox.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.command.LogContainerResultCallback;

import java.util.List;

public class DockerDemo {

    public static void main(String[] args) throws InterruptedException {
        // 获取默认的 Docker Client
        com.github.dockerjava.core.DefaultDockerClientConfig config = 
            com.github.dockerjava.core.DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost(getPlatformDockerHost())
            .build();
        com.github.dockerjava.transport.DockerHttpClient httpClient = new com.github.dockerjava.httpclient5.ApacheDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())
            .sslConfig(config.getSSLConfig())
            .build();
        DockerClient dockerClient = com.github.dockerjava.core.DockerClientImpl.getInstance(config, httpClient);
//        PingCmd pingCmd = dockerClient.pingCmd();
//        pingCmd.exec();
        // 拉取镜像
        String image = "nginx:latest";
//        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
//        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
//            @Override
//            public void onNext(PullResponseItem item) {
//                System.out.println("下载镜像：" + item.getStatus());
//                super.onNext(item);
//            }
//        };
//        pullImageCmd
//                .exec(pullImageResultCallback)
//                .awaitCompletion();
//        System.out.println("下载完成");
        // 创建容器
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        // 为了保持容器运行以便检查，我们使用一个长时运行的命令，例如 tail -f /dev/null
        // 原始命令是 .withCmd("echo", "Hello Docker")，它会立即执行完毕导致容器退出
        CreateContainerResponse createContainerResponse = containerCmd
                .withCmd("tail", "-f", "/dev/null") // 使用 tail -f /dev/null 使容器保持运行
                .exec();
        System.out.println(createContainerResponse);
        String containerId = createContainerResponse.getId();

        // 查看容器状态
        ListContainersCmd listContainersCmd = dockerClient.listContainersCmd();
        List<Container> containerList = listContainersCmd.withShowAll(true).exec();
        for (Container container : containerList) {
            System.out.println(container);
        }

        // 启动容器
        dockerClient.startContainerCmd(containerId).exec();

        System.out.println("容器已启动，将保持运行60秒钟以便检查。请使用 Docker Desktop 或 docker ps 命令查看。");
        Thread.sleep(60000L); // 暂停60秒，以便有时间检查容器

        // 查看日志

        LogContainerResultCallback logContainerResultCallback = new LogContainerResultCallback() {
            @Override
            public void onNext(Frame item) {
                System.out.println(item.getStreamType());
                System.out.println("日志：" + new String(item.getPayload()));
                super.onNext(item);
            }
        };


        dockerClient.logContainerCmd(containerId)
                .withStdErr(true)
                .withStdOut(true)
                .exec(logContainerResultCallback)
                .awaitCompletion();

        // 暂时注释掉删除容器和镜像的步骤，以便在Java程序结束后仍能检查容器
        // System.out.println("准备删除容器: " + containerId);
        // dockerClient.removeContainerCmd(containerId).withForce(true).exec();
        // System.out.println("容器已删除。");

        // System.out.println("准备删除镜像: " + image);
        // dockerClient.removeImageCmd(image).exec();
        // System.out.println("镜像已删除。");
        System.out.println("DockerDemo 执行完毕。如果需要，请手动清理容器和镜像。");
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
}
