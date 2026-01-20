package com.cs.ojcodesandbox.config;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.time.Duration;

/**
 * Docker客户端配置
 */
@Slf4j
@Configuration
public class DockerClientConfig {

    /**
     * 获取当前平台下的 Docker Host 地址
     * 优先读取 DOCKER_HOST 环境变量
     */
    @Value("${docker.host:#{null}}")
    private String dockerHost;

    /**
     * 创建DockerClient实例
     */
    @Bean
    public com.github.dockerjava.api.DockerClient dockerClient() {
        String host = getDockerHost();
        log.info("初始化Docker客户端，Docker Host: {}", host);

        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(host)
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(URI.create(host))
                .sslConfig(config.getSSLConfig())
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(120))
                .build();

        return DockerClientImpl.getInstance(config, httpClient);
    }

    /**
     * 获取Docker Host地址
     */
    private String getDockerHost() {
        // 优先使用配置的dockerHost
        if (dockerHost != null && !dockerHost.isEmpty()) {
            return dockerHost;
        }

        // 读取环境变量
        String envDockerHost = System.getenv("DOCKER_HOST");
        if (envDockerHost != null && !envDockerHost.isEmpty()) {
            return envDockerHost;
        }

        // 根据操作系统判断默认值
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "npipe:////./pipe/docker_engine";
        } else {
            return "unix:///var/run/docker.sock";
        }
    }
}
