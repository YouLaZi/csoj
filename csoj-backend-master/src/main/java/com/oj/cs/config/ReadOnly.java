package com.oj.cs.config;

import java.lang.annotation.*;

/** 只读注解 标注的方法将使用从库（读库） */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ReadOnly {}
