package com.cs.ojcodesandbox.pool;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Container;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Docker容器池
 * 用于复用容器，减少创建和销毁容器的开销
 */
@Slf4j
@Component
public class DockerContainerPool {

    /**
     * 空闲容器队列
     */
    private final BlockingQueue<PooledDockerContainer> idleContainers;

    /**
     * 活跃容器计数器
     */
    private final AtomicInteger activeContainers = new AtomicInteger(0);

    /**
     * Docker客户端
     */
    private final DockerClient dockerClient;

    /**
     * 容器配置
     */
    private final String image;
    private final long memoryLimit;
    private final long cpuCount;
    private final int maxPoolSize;
    private final int minPoolSize;
    private final long maxIdleTimeMs;

    /**
     * 容器是否已初始化
     */
    private volatile boolean initialized = false;

    /**
     * 构造函数
     */
    public DockerContainerPool(DockerClient dockerClient,
                               @Value("${docker.pool.image:openjdk:8-alpine}") String image,
                               @Value("${docker.pool.memory-limit:104857600}") long memoryLimit,
                               @Value("${docker.pool.cpu-count:1}") long cpuCount,
                               @Value("${docker.pool.max-size:10}") int maxPoolSize,
                               @Value("${docker.pool.min-size:2}") int minPoolSize,
                               @Value("${docker.pool.max-idle-time:300000}") long maxIdleTimeMs) {
        this.dockerClient = dockerClient;
        this.image = image;
        this.memoryLimit = memoryLimit;
        this.cpuCount = cpuCount;
        this.maxPoolSize = maxPoolSize;
        this.minPoolSize = minPoolSize;
        this.maxIdleTimeMs = maxIdleTimeMs;
        this.idleContainers = new LinkedBlockingQueue<>(maxPoolSize);

        log.info("Docker容器池配置 - 镜像: {}, 内存限制: {} bytes, CPU核数: {}, 最大池大小: {}, 最小池大小: {}",
                image, memoryLimit, cpuCount, maxPoolSize, minPoolSize);

        // 初始化容器池
        initialize();
    }

    /**
     * 初始化容器池，预创建最小数量的容器
     */
    private void initialize() {
        if (initialized) {
            return;
        }

        synchronized (this) {
            if (initialized) {
                return;
            }

            log.info("开始初始化Docker容器池...");
            try {
                // 拉取镜像（如果不存在）
                pullImageIfNotExists();

                // 预创建最小数量的容器
                for (int i = 0; i < minPoolSize; i++) {
                    PooledDockerContainer container = createContainer();
                    if (container != null) {
                        idleContainers.offer(container);
                    }
                }

                initialized = true;
                log.info("Docker容器池初始化完成，当前空闲容器数: {}", idleContainers.size());
            } catch (Exception e) {
                log.error("Docker容器池初始化失败", e);
            }
        }
    }

    /**
     * 拉取镜像（如果不存在）
     */
    private void pullImageIfNotExists() {
        try {
            // 检查镜像是否存在
            boolean imageExists = dockerClient.listImagesCmd()
                    .withImageNameFilter(image)
                    .exec()
                    .stream()
                    .anyMatch(img -> img.getRepoTags() != null &&
                            java.util.Arrays.stream(img.getRepoTags())
                                    .anyMatch(tag -> tag.contains(image)));

            if (!imageExists) {
                log.info("镜像 {} 不存在，开始拉取...", image);
                dockerClient.pullImageCmd(image)
                        .exec(new com.github.dockerjava.api.async.ResultCallback.Adapter<>() {
                            @Override
                            public void onNext(com.github.dockerjava.api.model.PullResponseItem item) {
                                log.debug("拉取镜像进度: {}", item.getStatus());
                            }
                        })
                        .awaitCompletion(5, TimeUnit.MINUTES);
                log.info("镜像拉取完成");
            } else {
                log.info("镜像 {} 已存在", image);
            }
        } catch (Exception e) {
            log.error("拉取镜像失败", e);
            throw new RuntimeException("拉取镜像失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从池中获取容器
     *
     * @param userCodeParentPath 用户代码父目录路径
     * @return 容器实例
     */
    public PooledDockerContainer borrowContainer(String userCodeParentPath) {
        // 确保容器池已初始化
        if (!initialized) {
            initialize();
        }

        PooledDockerContainer container = null;

        try {
            // 尝试从空闲队列获取容器
            container = idleContainers.poll(1, TimeUnit.SECONDS);

            if (container == null) {
                // 没有空闲容器，检查是否可以创建新容器
                int currentActive = activeContainers.get();
                if (currentActive < maxPoolSize) {
                    if (activeContainers.compareAndSet(currentActive, currentActive + 1)) {
                        container = createContainer();
                        if (container == null) {
                            activeContainers.decrementAndGet();
                            throw new RuntimeException("创建容器失败");
                        }
                        log.info("创建新容器，当前活跃容器数: {}", activeContainers.get());
                    }
                } else {
                    // 等待有容器释放
                    container = idleContainers.poll(30, TimeUnit.SECONDS);
                    if (container == null) {
                        throw new RuntimeException("获取容器超时");
                    }
                }
            } else {
                // 成功获取空闲容器
                activeContainers.incrementAndGet();
                log.info("从池中获取容器，当前活跃容器数: {}，空闲容器数: {}",
                        activeContainers.get(), idleContainers.size());
            }

            // 检查容器是否可用
            if (!isContainerAvailable(container.getContainerId())) {
                // 容器不可用，移除并创建新的
                removeContainer(container);
                container = createContainer();
            }

            // 更新容器的工作目录
            container.setWorkDirectory(userCodeParentPath);
            container.setLastBorrowTime(System.currentTimeMillis());

            return container;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (container != null) {
                returnContainer(container);
            }
            throw new RuntimeException("获取容器被中断", e);
        } catch (Exception e) {
            if (container != null) {
                returnContainer(container);
            }
            throw new RuntimeException("获取容器失败: " + e.getMessage(), e);
        }
    }

    /**
     * 归还容器到池中
     *
     * @param container 容器实例
     */
    public void returnContainer(PooledDockerContainer container) {
        if (container == null) {
            return;
        }

        activeContainers.decrementAndGet();

        try {
            // 检查容器是否超过最大空闲时间
            long idleTime = System.currentTimeMillis() - container.getLastBorrowTime();
            if (idleTime > maxIdleTimeMs) {
                // 超过最大空闲时间，销毁容器
                log.info("容器空闲时间超过阈值 {} ms，销毁容器 {}", maxIdleTimeMs, container.getContainerId());
                removeContainer(container);
                return;
            }

            // 清理容器状态（可选）
            cleanupContainer(container);

            // 归还到池中
            if (idleContainers.size() < maxPoolSize) {
                idleContainers.offer(container);
                log.info("容器已归还到池中，活跃容器数: {}，空闲容器数: {}",
                        activeContainers.get(), idleContainers.size());
            } else {
                // 池已满，销毁容器
                log.info("容器池已满，销毁容器 {}", container.getContainerId());
                removeContainer(container);
            }

        } catch (Exception e) {
            log.error("归还容器失败", e);
            removeContainer(container);
        }
    }

    /**
     * 清理容器状态
     */
    private void cleanupContainer(PooledDockerContainer container) {
        // 停止容器中可能运行的进程
        try {
            dockerClient.stopContainerCmd(container.getContainerId())
                    .exec();
        } catch (Exception e) {
            log.warn("停止容器失败: {}", container.getContainerId(), e);
        }
    }

    /**
     * 创建新容器
     */
    private PooledDockerContainer createContainer() {
        try {
            HostConfig hostConfig = new HostConfig();
            hostConfig.withMemory(memoryLimit);
            hostConfig.withMemorySwap(0L);
            hostConfig.withCpuCount(cpuCount);
            hostConfig.withReadonlyRootfs(false);

            CreateContainerResponse response = dockerClient.createContainerCmd(image)
                    .withHostConfig(hostConfig)
                    .withAttachStdin(true)
                    .withAttachStderr(true)
                    .withAttachStdout(true)
                    .withTty(true)
                    .exec();

            String containerId = response.getId();

            // 启动容器
            dockerClient.startContainerCmd(containerId).exec();

            PooledDockerContainer container = new PooledDockerContainer(containerId, image);
            log.info("创建新容器: {}", containerId);

            return container;

        } catch (Exception e) {
            log.error("创建容器失败", e);
            return null;
        }
    }

    /**
     * 移除容器
     */
    private void removeContainer(PooledDockerContainer container) {
        if (container == null) {
            return;
        }

        try {
            // 停止容器
            dockerClient.stopContainerCmd(container.getContainerId())
                    .exec();
        } catch (Exception e) {
            log.warn("停止容器失败: {}", container.getContainerId(), e);
        }

        try {
            // 删除容器
            dockerClient.removeContainerCmd(container.getContainerId())
                    .withForce(true)
                    .exec();
            log.info("删除容器: {}", container.getContainerId());
        } catch (Exception e) {
            log.error("删除容器失败: {}", container.getContainerId(), e);
        }
    }

    /**
     * 检查容器是否可用
     */
    private boolean isContainerAvailable(String containerId) {
        try {
            InspectContainerResponse response = dockerClient.inspectContainerCmd(containerId).exec();
            return response.getState().getRunning();
        } catch (Exception e) {
            log.warn("检查容器状态失败: {}", containerId, e);
            return false;
        }
    }

    /**
     * 获取池状态
     */
    public PoolStatus getStatus() {
        return new PoolStatus(
                idleContainers.size(),
                activeContainers.get(),
                maxPoolSize,
                minPoolSize
        );
    }

    /**
     * 定时清理孤儿容器（每5分钟执行一次）
     * 清理不属于容器池的、已停止的或超过最大空闲时间的容器
     */
    @Scheduled(fixedRate = 300000)
    public void cleanupOrphanedContainers() {
        try {
            log.debug("开始清理孤儿容器...");

            // 获取所有容器
            List<Container> containers = dockerClient.listContainersCmd()
                    .withShowAll(true)
                    .exec();

            int cleanedCount = 0;
            for (Container container : containers) {
                String containerId = container.getId();
                String containerName = container.getNames() != null && container.getNames().length > 0
                        ? container.getNames()[0] : "unknown";

                // 检查是否是沙箱创建的容器（以特定前缀开头或使用沙箱镜像）
                boolean isSandboxContainer = false;
                String containerImage = container.getImage();
                if (containerImage != null && containerImage.contains("openjdk")) {
                    isSandboxContainer = true;
                }

                // 如果是沙箱容器且已停止，则清理
                if (isSandboxContainer && !"running".equals(container.getState())) {
                    try {
                        dockerClient.removeContainerCmd(containerId)
                                .withForce(true)
                                .exec();
                        log.info("清理孤儿容器: {} ({})", containerId, containerName);
                        cleanedCount++;
                    } catch (Exception e) {
                        log.warn("清理容器失败: {} - {}", containerId, e.getMessage());
                    }
                }
            }

            if (cleanedCount > 0) {
                log.info("孤儿容器清理完成，共清理 {} 个容器", cleanedCount);
            } else {
                log.debug("没有需要清理的孤儿容器");
            }

        } catch (Exception e) {
            log.error("清理孤儿容器时发生异常", e);
        }
    }

    /**
     * 应用关闭时清理所有容器
     */
    @PreDestroy
    public void destroy() {
        log.info("开始清理Docker容器池...");

        // 清理空闲容器
        PooledDockerContainer container;
        while ((container = idleContainers.poll()) != null) {
            removeContainer(container);
        }

        log.info("Docker容器池清理完成");
    }

    /**
     * 池状态信息
     */
    public static class PoolStatus {
        private final int idleCount;
        private final int activeCount;
        private final int maxSize;
        private final int minSize;

        public PoolStatus(int idleCount, int activeCount, int maxSize, int minSize) {
            this.idleCount = idleCount;
            this.activeCount = activeCount;
            this.maxSize = maxSize;
            this.minSize = minSize;
        }

        public int getIdleCount() {
            return idleCount;
        }

        public int getActiveCount() {
            return activeCount;
        }

        public int getMaxSize() {
            return maxSize;
        }

        public int getMinSize() {
            return minSize;
        }

        @Override
        public String toString() {
            return String.format("PoolStatus{idle=%d, active=%d, max=%d, min=%d}",
                    idleCount, activeCount, maxSize, minSize);
        }
    }
}
