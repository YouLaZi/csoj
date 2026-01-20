package com.cs.ojcodesandbox.model.enums;

import lombok.Getter;

/**
 * 执行代码状态枚举
 */
@Getter
public enum ExecuteCodeStatusEnum {
    PENDING(0, "待执行"),
    WAITING(1, "等待中"),
    RUNNING(2, "运行中"),
    SUCCEED(3, "成功"),
    FAILED(4, "失败"),
    TIME_LIMIT_EXCEEDED(5, "超出时间限制"),
    MEMORY_LIMIT_EXCEEDED(6, "超出内存限制"),
    OUTPUT_LIMIT_EXCEEDED(7, "输出超限"),
    DANGEROUS_OPERATION(8, "危险操作"),
    COMPILE_ERROR(9, "编译错误"),
    SYSTEM_ERROR(10, "系统错误");

    private final Integer value;
    private final String text;

    ExecuteCodeStatusEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static ExecuteCodeStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (ExecuteCodeStatusEnum anEnum : ExecuteCodeStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}