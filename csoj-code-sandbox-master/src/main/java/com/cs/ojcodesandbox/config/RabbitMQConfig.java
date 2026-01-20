package com.cs.ojcodesandbox.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.core.Queue;

/**
 * RabbitMQ配置类
 * 配置代码沙箱的请求队列和结果队列
 */
@Configuration
public class RabbitMQConfig {

    // 请求队列名称
    public static final String CODE_SANDBOX_QUEUE = "code_sandbox_queue";
    // 请求交换机名称
    public static final String CODE_SANDBOX_EXCHANGE = "code_sandbox_exchange";
    // 请求路由键
    public static final String CODE_SANDBOX_ROUTING_KEY = "code_sandbox_routing_key";

    // 结果队列名称
    public static final String CODE_SANDBOX_RESULT_QUEUE = "code_sandbox_result_queue";
    // 结果交换机名称
    public static final String CODE_SANDBOX_RESULT_EXCHANGE = "code_sandbox_result_exchange";
    // 结果路由键
    public static final String CODE_SANDBOX_RESULT_ROUTING_KEY = "code_sandbox_result_routing_key";

    // =============== 请求队列配置 ===============

    /**
     * 创建代码沙箱请求队列
     */
    @Bean
    public org.springframework.amqp.core.Queue codeSandboxQueue() {
        return new org.springframework.amqp.core.Queue(CODE_SANDBOX_QUEUE, true);
    }

    /**
     * 创建代码沙箱请求交换机
     */
    @Bean
    public DirectExchange codeSandboxExchange() {
        return new DirectExchange(CODE_SANDBOX_EXCHANGE, true, false);
    }

    /**
     * 将代码沙箱请求队列绑定到交换机
     */
    @Bean
    public Binding codeSandboxBinding() {
        return BindingBuilder.bind(codeSandboxQueue())
                .to(codeSandboxExchange())
                .with(CODE_SANDBOX_ROUTING_KEY);
    }

    // =============== 结果队列配置 ===============

    /**
     * 创建代码沙箱结果队列
     */
    @Bean
    public org.springframework.amqp.core.Queue codeSandboxResultQueue() {
        return new org.springframework.amqp.core.Queue(CODE_SANDBOX_RESULT_QUEUE, true);
    }

    /**
     * 创建代码沙箱结果交换机
     */
    @Bean
    public DirectExchange codeSandboxResultExchange() {
        return new DirectExchange(CODE_SANDBOX_RESULT_EXCHANGE, true, false);
    }

    /**
     * 将代码沙箱结果队列绑定到交换机
     */
    @Bean
    public Binding codeSandboxResultBinding() {
        return BindingBuilder.bind(codeSandboxResultQueue())
                .to(codeSandboxResultExchange())
                .with(CODE_SANDBOX_RESULT_ROUTING_KEY);
    }
}