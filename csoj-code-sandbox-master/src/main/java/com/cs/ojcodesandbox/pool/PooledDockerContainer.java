package com.cs.ojcodesandbox.pool;

import lombok.Data;

/**
 * 池化的Docker容器包装类
 * 用于跟踪容器状态和使用信息
 */
@Data
public class PooledDockerContainer {

    /**
     * 容器ID
     */
    private final String containerId;

    /**
     * 镜像名称
     */
    private final String imageName;

    /**
     * 工作目录（用户代码目录）
     */
    private String workDirectory;

    /**
     * 最后借用时间
     */
    private long lastBorrowTime;

    /**
     * 创建时间
     */
    private final long createTime;

    /**
     * 使用次数
     */
    private int usageCount;

    /**
     * 容器是否可用
     */
    private boolean available = true;

    /**
     * 构造函数
     *
     * @param containerId 容器ID
     * @param imageName   镜像名称
     */
    public PooledDockerContainer(String containerId, String imageName) {
        this.containerId = containerId;
        this.imageName = imageName;
        this.createTime = System.currentTimeMillis();
        this.lastBorrowTime = createTime;
    }

    /**
     * 增加使用次数
     */
    public void incrementUsage() {
        this.usageCount++;
    }

    /**
     * 获取容器年龄（毫秒）
     */
    public long getAge() {
        return System.currentTimeMillis() - createTime;
    }

    /**
     * 获取空闲时间（毫秒）
     */
    public long getIdleTime() {
        return System.currentTimeMillis() - lastBorrowTime;
    }

    /**
     * 重置借用时间
     */
    public void resetBorrowTime() {
        this.lastBorrowTime = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return String.format("PooledDockerContainer{id=%s, image=%s, usage=%d, age=%dms, idle=%dms, available=%s}",
                containerId.substring(0, Math.min(12, containerId.length())),
                imageName,
                usageCount,
                getAge(),
                getIdleTime(),
                available);
    }
}
