package com.oj.cs.config;

import java.lang.annotation.*;

/** 读写注解 标注的方法将使用主库（写库） */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ReadWrite {}
